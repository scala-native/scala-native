package java.util.zip

// Ported from Apache Harmony. Extensive changes for Scala Native.

// This class is best used from a single thread.

import java.io.{ByteArrayOutputStream, IOException, OutputStream}
import java.nio.charset.{Charset, StandardCharsets}

import scala.collection.mutable.ArrayBuffer

class ZipOutputStream(_out: OutputStream, charset: Charset)
    extends DeflaterOutputStream(
      _out,
      new Deflater(Deflater.DEFAULT_COMPRESSION, true)
    )
    with ZipConstants {
  import ZipOutputStream._

  def this(out: OutputStream) = this(out, StandardCharsets.UTF_8)

  private var archiveComment: String = null
  private var entries = new ArrayBuffer[String]()
  private var compressMethod = DEFLATED
  private var compressLevel = Deflater.DEFAULT_COMPRESSION
  private var cDir = new ByteArrayOutputStream()
  private var currentEntry: ZipEntry = null
  private val crc = new CRC32()
  private var offset = 0
  private var curOffset = 0
  private var nameBytes: Array[Byte] = null
  // LFH/CDH `extra` payloads for the current entry. The LFH form may
  // carry the full mtime/atime/ctime triple via UT (0x5455); the CDH
  // form by spec carries only mtime, so we recompute it. Both default
  // to the entry's existing `extra` when no FileTime is set.
  private var lfhExtra: Array[Byte] = null
  private var cdhExtra: Array[Byte] = null

  private var gpFlags: Short = 0 // Zip general purpose flags

  private val gpCharsetFlag =
    if (charset == StandardCharsets.UTF_8) ZipByteConversions.UTF8_ENABLED_MASK
    else 0

  // Per JVM, silent truncation of comment length
  private def limitCommentLength(cb: Array[Byte]): Short =
    Math.min(cb.length, 0xffff).toShort

  override def close(): Unit = {
    if (out != null) {
      finish()
      out.close()
      out = null
    }
  }

  def closeEntry(): Unit = {
    if ((cDir == null) || (currentEntry == null)) {
      () // Centeral Directory has been finish()'ed or no work to be done
    } else {
      if (currentEntry.getMethod() == DEFLATED)
        super.finish()

      // Verify values for STORED types
      if (currentEntry.getMethod() == STORED) {
        if (crc.getValue() != currentEntry.crc) {
          throw new ZipException("Crc mismatch")
        } else if (currentEntry.size != crc.tbytes) {
          throw new ZipException("Size mismatch")
        }
      }

      curOffset = LOCHDR

      // Write the DataDescriptor
      if (currentEntry.getMethod() != STORED) {
        curOffset += EXTHDR
        writeLong(out, EXTSIG)
        writeLong(out, { currentEntry.crc = crc.getValue(); currentEntry.crc })
        writeLong(
          out, {
            currentEntry.compressedSize = `def`.getTotalOut();
            currentEntry.compressedSize
          }
        )
        writeLong(
          out, {
            currentEntry.size = `def`.getTotalIn(); currentEntry.size
          }
        )
      }

      // Update the CentralDirectory
      writeLong(cDir, CENSIG)
      writeShort(cDir, ZIPLocalHeaderVersionNeeded) // Version created
      writeShort(cDir, ZIPLocalHeaderVersionNeeded) // Version to extract

      writeShort(cDir, gpFlags)

      writeShort(cDir, currentEntry.getMethod())
      writeShort(cDir, currentEntry.time)
      writeShort(cDir, currentEntry.modDate)
      writeLong(cDir, crc.getValue())
      if (currentEntry.getMethod() == DEFLATED) {
        curOffset += writeLong(cDir, `def`.getTotalOut()).toInt
        writeLong(cDir, `def`.getTotalIn())
      } else {
        curOffset += writeLong(cDir, crc.tbytes).toInt
        writeLong(cDir, crc.tbytes)
      }
      // `curOffset` tracks the on-disk size of the LFH (header + name
      // + extra + data + optional data descriptor). The CDH may store
      // a different `extra` size (UT in the CDH only carries mtime,
      // while the LFH variant can also carry atime/ctime), so we use
      // the LFH length here and pass `cdhExtra.length` separately to
      // the CDH write.
      curOffset += writeShort(cDir, nameBytes.length)
      curOffset += (if (lfhExtra != null) lfhExtra.length else 0)
      if (cdhExtra != null) {
        writeShort(cDir, cdhExtra.length)
      } else {
        writeShort(cDir, 0)
      }

      val entryCommentBytes = ZipByteConversions.bytesFromString(
        currentEntry.getComment(),
        gpFlags,
        charset
      )

      val entryCommentLength = limitCommentLength(entryCommentBytes)
      writeShort(cDir, entryCommentLength)

      writeShort(cDir, 0) // Disk Start
      writeShort(cDir, 0) // Internal File Attributes
      writeLong(cDir, 0) // External File Attributes
      writeLong(cDir, offset)

      cDir.write(nameBytes)

      if (cdhExtra != null) {
        cDir.write(cdhExtra)
      }
      offset += curOffset

      if (entryCommentLength > 0)
        cDir.write(entryCommentBytes, 0, entryCommentLength)

      currentEntry = null
      crc.reset()
      `def`.reset()
      done = false
    }
  }

  override def finish(): Unit = {
    if (out == null)
      throw new IOException("Stream closed")

    if (currentEntry != null)
      closeEntry()

    if (cDir != null) {
      val cdirSize = cDir.size()
      // Write Central Dir End
      writeLong(cDir, ENDSIG)
      writeShort(cDir, 0) // Disk Number
      writeShort(cDir, 0) // Start Disk
      writeShort(cDir, entries.size) // Number of entries
      writeShort(cDir, entries.size) // Number of entries (yes, twice)
      writeLong(cDir, cdirSize) // Size of central dir
      writeLong(cDir, offset) // Offset of central dir

      if ((archiveComment == null) || archiveComment.length == 0) {
        writeShort(cDir, 0)
      } else {
        val archiveCommentBytes =
          ZipByteConversions.bytesFromString(archiveComment, gpFlags, charset)

        val archiveCommentLength = limitCommentLength(archiveCommentBytes)

        writeShort(cDir, archiveCommentLength)
        cDir.write(archiveCommentBytes, 0, archiveCommentLength)
      }

      // Write the central dir
      out.write(cDir.toByteArray())
      cDir = null
    }
  }

  def putNextEntry(ze: ZipEntry): Unit = {
    if (currentEntry != null)
      closeEntry()

    if (ze.getMethod() == STORED
        || (compressMethod == STORED && ze.getMethod() == -1)) {
      if (ze.crc == -1) {
        throw new ZipException("Crc mismatch")
      }

      if (ze.size == -1 && ze.compressedSize == -1) {
        throw new ZipException("Size mismatch")
      }

      if (ze.size != ze.compressedSize && ze.compressedSize != -1
          && ze.size != -1) {
        throw new ZipException("Size mismatch")
      }
    }

    if (cDir == null) {
      () // Central Directory has been finish()'ed.
    } else {
      if (entries.contains(ze.name)) {
        /* [MSG "archive.29", "Entry already exists: {0}"] */
        throw new ZipException(s"Entry already exists: ${ze.name}")
      }

      val gpMethodFlag =
        if (ze.getMethod() == STORED) 0
        else ZIPDataDescriptorFlag

      // Set & use global variable so identical flags are used in closeEntry().
      gpFlags = (gpMethodFlag | gpCharsetFlag).toShort

      nameBytes = ZipByteConversions.bytesFromString(ze.name, gpFlags, charset)

      val nameLength = nameBytes.length

      if (nameLength > 0xffff) {
        /* [MSG "archive.2A", "Name too long: {0}"] */
        throw new IllegalArgumentException(s"Name too long: ${ze.name}")
      }

      // Settle method/time on the entry before building the extras
      // and writing the LFH. Both bits are part of the LFH the writer
      // will produce, and FileTime presence affects the UT block.
      if (ze.getMethod() == -1) ze.setMethod(compressMethod)
      if (ze.time == -1 || ze.modDate == -1) {
        if (ze.mtime != Long.MinValue) {
          val preservedMtime = ze.mtime
          val preservedMtimeFromExtra = ze.mtimeFromExtra
          ze.setTime(preservedMtime)
          ze.mtime = preservedMtime
          ze.mtimeFromExtra = preservedMtimeFromExtra
        } else {
          ze.setTime(System.currentTimeMillis())
        }
      }

      // Build the extra blocks. Whenever any FileTime is set on the
      // entry we strip any pre-existing UT (0x5455) or NTFS (0x000A)
      // timestamp blocks first, so re-writes don't (a) leave duplicate
      // records when we emit a replacement, or (b) leave stale records
      // when we *cannot* emit a replacement (e.g. mtime is outside the
      // UT-representable range) — in either case the user's intent is
      // the freshly-set FileTime, not whatever the source archive
      // happened to carry. LFH carries the full mtime/atime/ctime
      // triple; CDH carries only mtime per APPNOTE 4.5.7.
      val hasFileTimes =
        ze.mtime != Long.MinValue ||
          ze.atime != Long.MinValue ||
          ze.ctime != Long.MinValue
      val cleanExtra =
        if (hasFileTimes) ZipEntry.stripTimestampBlocks(ze.extra)
        else ze.extra
      val lfhUT = ZipEntry.buildUTExtraBlock(ze, true)
      val cdhUT = ZipEntry.buildUTExtraBlock(ze, false)
      val newLfhExtra = ZipEntry.mergeExtra(cleanExtra, lfhUT)
      val newCdhExtra = ZipEntry.mergeExtra(cleanExtra, cdhUT)

      // The on-disk LFH/CDH extra-length field is 16 bits. setExtra
      // already caps user-supplied bytes at 0xffff, but appending a UT
      // block can push the total past that limit, after which
      // writeShort(...) would silently truncate the length and leave
      // out.write(lfhExtra) writing past the declared bound — yielding
      // an unreadable archive. Validate BEFORE any state commit or
      // LFH write so a thrown exception leaves the stream and the
      // `entries` set untouched and the caller can recover.
      if (newLfhExtra != null && newLfhExtra.length > 0xffff)
        throw new ZipException(s"LFH extra too large: ${newLfhExtra.length}")
      if (newCdhExtra != null && newCdhExtra.length > 0xffff)
        throw new ZipException(s"CDH extra too large: ${newCdhExtra.length}")

      // Commit: from here on `currentEntry` is set and the on-disk
      // LFH is being produced.
      `def`.setLevel(compressLevel)
      currentEntry = ze
      entries += currentEntry.name
      lfhExtra = newLfhExtra
      cdhExtra = newCdhExtra

      writeLong(out, LOCSIG) // Entry header
      writeShort(out, ZIPLocalHeaderVersionNeeded) // Extraction version

      writeShort(out, gpFlags)

      writeShort(out, currentEntry.getMethod())
      writeShort(out, currentEntry.time)
      writeShort(out, currentEntry.modDate)

      if (currentEntry.getMethod() == STORED) {
        if (currentEntry.size == -1) {
          currentEntry.size = currentEntry.compressedSize
        } else if (currentEntry.compressedSize == -1) {
          currentEntry.compressedSize = currentEntry.size
        }
        writeLong(out, currentEntry.crc)
        writeLong(out, currentEntry.size)
        writeLong(out, currentEntry.size)
      } else {
        writeLong(out, 0)
        writeLong(out, 0)
        writeLong(out, 0)
      }

      writeShort(out, nameLength)

      if (lfhExtra != null) {
        writeShort(out, lfhExtra.length)
      } else {
        writeShort(out, 0)
      }

      out.write(nameBytes)

      if (lfhExtra != null)
        out.write(lfhExtra)
    }
  }

  def setComment(comment: String): Unit = {
    if (comment.length() > 0xffff)
      throw new IllegalArgumentException("String is too long")

    this.archiveComment = comment
  }

  def setLevel(level: Int): Unit = {
    if (level < Deflater.DEFAULT_COMPRESSION || level > Deflater.BEST_COMPRESSION) {
      throw new IllegalArgumentException()
    } else {
      compressLevel = level
    }
  }

  def setMethod(method: Int): Unit = {
    if (method != STORED && method != DEFLATED) {
      throw new IllegalArgumentException()
    }
    compressMethod = method
  }

  private def writeLong(os: OutputStream, i: Long): Long = {
    // Write out the long value as an unsigned int
    os.write((i & 0xff).toInt)
    os.write(((i >> 8) & 0xff).toInt)
    os.write(((i >> 16) & 0xff).toInt)
    os.write(((i >> 24) & 0xff).toInt)
    i
  }

  private def writeShort(os: OutputStream, i: Int): Int = {
    os.write(i & 0xff)
    os.write((i >> 8) & 0xff)
    i
  }

  override def write(buffer: Array[Byte], off: Int, nbytes: Int): Unit = {
    // avoid int overflow, check null buf
    if ((off < 0 || (nbytes < 0) || off > buffer.length)
        || (buffer.length - off < nbytes)) {
      throw new IndexOutOfBoundsException()
    }

    if (currentEntry == null) {
      throw new ZipException("No active entry")
    }

    if (currentEntry.getMethod() == STORED) {
      out.write(buffer, off, nbytes)
    } else {
      super.write(buffer, off, nbytes)
    }
    crc.update(buffer, off, nbytes)
  }

}

object ZipOutputStream {

  private[zip] final val ZIPLocalHeaderVersionNeeded = 20
  private[zip] final val ZIPDataDescriptorFlag = 8

  final val DEFLATED = 8
  final val STORED = 0

  private def utf8Count(value: String): Int = {
    var total = 0
    var i = value.length - 1
    while (i >= 0) {
      val ch = value.charAt(i)
      if (ch < 0x80) {
        total += 1
      } else if (ch < 0x800) {
        total += 2
      } else {
        total += 3
      }
      i -= 1
    }
    total
  }
}
