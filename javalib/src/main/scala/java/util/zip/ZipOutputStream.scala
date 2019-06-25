package java.util.zip

// Ported from Apache Harmony

import java.io.{ByteArrayOutputStream, IOException, OutputStream}
import java.nio.charset.{Charset, StandardCharsets}

import scala.collection.mutable.ArrayBuffer

class ZipOutputStream(_out: OutputStream, charset: Charset)
    extends DeflaterOutputStream(
      _out,
      new Deflater(Deflater.DEFAULT_COMPRESSION, true))
    with ZipConstants {
  import ZipOutputStream._

  def this(out: OutputStream) = this(out, StandardCharsets.UTF_8)

  private var comment: String        = null
  private var entries                = new ArrayBuffer[String]()
  private var compressMethod         = DEFLATED
  private var compressLevel          = Deflater.DEFAULT_COMPRESSION
  private var cDir                   = new ByteArrayOutputStream()
  private var currentEntry: ZipEntry = null
  private val crc                    = new CRC32()
  private var offset                 = 0
  private var curOffset              = 0
  private var nameLength             = 0
  private var nameBytes: Array[Byte] = null

  override def close(): Unit = {
    if (out != null) {
      finish()
      out.close()
      out = null
    }
  }

  def closeEntry() {
    if (cDir == null) {
      throw new IOException()
    } else if (currentEntry == null) {
      ()
    } else if (currentEntry.getMethod() == DEFLATED) {
      super.finish()
    }

    // Verify values for STORED types
    if (currentEntry.getMethod == STORED) {
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
      writeLong(out, {
        currentEntry.compressedSize = `def`.getTotalOut;
        currentEntry.compressedSize
      })
      writeLong(out, {
        currentEntry.size = `def`.getTotalIn; currentEntry.size
      })
    }
    // Update the CentralDirectory
    writeLong(cDir, CENSIG)
    writeShort(cDir, ZIPLocalHeaderVersionNeeded) // Version created
    writeShort(cDir, ZIPLocalHeaderVersionNeeded) // Version to extract
    writeShort(
      cDir,
      if (currentEntry.getMethod() == STORED) 0 else ZIPDataDescriptorFlag)
    writeShort(cDir, currentEntry.getMethod())
    writeShort(cDir, currentEntry.time)
    writeShort(cDir, currentEntry.modDate)
    writeLong(cDir, crc.getValue())
    if (currentEntry.getMethod() == DEFLATED) {
      curOffset += writeLong(cDir, `def`.getTotalOut).toInt
      writeLong(cDir, `def`.getTotalIn)
    } else {
      curOffset += writeLong(cDir, crc.tbytes).toInt
      writeLong(cDir, crc.tbytes)
    }
    curOffset += writeShort(cDir, nameLength)
    if (currentEntry.extra != null) {
      curOffset += writeShort(cDir, currentEntry.extra.length)
    } else {
      writeShort(cDir, 0)
    }
    var c: String = null
    if ({ c = currentEntry.getComment(); c != null }) {
      writeShort(cDir, c.length())
    } else {
      writeShort(cDir, 0)
    }
    writeShort(cDir, 0) // Disk Start
    writeShort(cDir, 0) // Internal File Attributes
    writeLong(cDir, 0)  // External File Attributes
    writeLong(cDir, offset)
    cDir.write(nameBytes)
    if (currentEntry.extra != null) {
      cDir.write(currentEntry.extra)
    }
    offset += curOffset
    if (c != null) {
      cDir.write(c.getBytes())
    }
    currentEntry = null
    crc.reset()
    `def`.reset()
    done = false
  }

  override def finish(): Unit = {
    if (out == null) {
      throw new IOException("Stream is closed")
    } else if (cDir == null) {
      ()
    } else if (entries.size == 0) {
      throw new ZipException("No entries")
    } else if (currentEntry != null) {
      closeEntry()
    }

    val cdirSize = cDir.size()
    // Write Central Dir End
    writeLong(cDir, ENDSIG)
    writeShort(cDir, 0)            // Disk Number
    writeShort(cDir, 0)            // Start Disk
    writeShort(cDir, entries.size) // Number of entries
    writeShort(cDir, entries.size) // Number of entries (yes, twice)
    writeLong(cDir, cdirSize)      // Size of central dir
    writeLong(cDir, offset)        // Offset of central dir
    if (comment != null) {
      writeShort(cDir, comment.length())
      cDir.write(comment.getBytes())
    } else {
      writeShort(cDir, 0)
    }
    // Write the central dir
    out.write(cDir.toByteArray())
    cDir = null

  }

  def putNextEntry(ze: ZipEntry): Unit = {
    if (currentEntry != null) {
      closeEntry()
    }
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
      throw new IOException("Stream is closed")
    }
    if (entries.contains(ze.name)) {
      /* [MSG "archive.29", "Entry already exists: {0}"] */
      throw new ZipException(s"Entry already exists: ${ze.name}")
    }
    nameLength = utf8Count(ze.name);
    if (nameLength > 0xffff) {
      /* [MSG "archive.2A", "Name too long: {0}"] */
      throw new IllegalArgumentException(s"Name too long: ${ze.name}")
    }

    `def`.setLevel(compressLevel)
    currentEntry = ze
    entries += currentEntry.name
    if (currentEntry.getMethod() == -1) {
      currentEntry.setMethod(compressMethod)
    }
    writeLong(out, LOCSIG)                       // Entry header
    writeShort(out, ZIPLocalHeaderVersionNeeded) // Extraction version
    writeShort(
      out,
      if (currentEntry.getMethod() == STORED) 0 else ZIPDataDescriptorFlag)
    writeShort(out, currentEntry.getMethod())
    if (currentEntry.getTime() == -1) {
      currentEntry.setTime(System.currentTimeMillis())
    }
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
    if (currentEntry.extra != null) {
      writeShort(out, currentEntry.extra.length)
    } else {
      writeShort(out, 0)
    }
    nameBytes = toUTF8Bytes(currentEntry.name, nameLength)
    out.write(nameBytes)
    if (currentEntry.extra != null) {
      out.write(currentEntry.extra)
    }
  }

  def setComment(comment: String): Unit = {
    if (comment.length() > 0xFFFF) {
      throw new IllegalArgumentException("String is too long")
    } else {
      this.comment = comment
    }
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
    os.write((i & 0xFF).toInt)
    os.write(((i >> 8) & 0xFF).toInt)
    os.write(((i >> 16) & 0xFF).toInt)
    os.write(((i >> 24) & 0xFF).toInt)
    i
  }

  private def writeShort(os: OutputStream, i: Int): Int = {
    os.write(i & 0xFF)
    os.write((i >> 8) & 0xFF)
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

  private[zip] final val ZIPLocalHeaderVersionNeeded: Int = 20
  private[zip] final val ZIPDataDescriptorFlag: Int       = 8

  final val DEFLATED: Int = 8
  final val STORED: Int   = 0

  private def utf8Count(value: String): Int = {
    var total = 0
    var i     = value.length - 1
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

  private def toUTF8Bytes(value: String, length: Int): Array[Byte] = {
    val result = new Array[Byte](length)
    var pos    = result.length
    var i      = value.length - 1
    while (i >= 0) {
      val ch = value.charAt(i)
      if (ch < 0x80) {
        pos -= 1
        result(pos) = ch.toByte
      } else if (ch < 0x800) {
        pos -= 1
        result(pos) = (0x80 | (ch & 0x3f)).toByte
        pos -= 1
        result(pos) = (0xc0 | (ch >> 6)).toByte
      } else {
        pos -= 1
        result(pos) = (0x80 | (ch & 0x3f)).toByte
        pos -= 1
        result(pos) = (0x80 | ((ch >> 6) & 0x3f)).toByte
        pos -= 1
        result(pos) = (0xe0 | (ch >> 12)).toByte
      }
      i -= 1
    }
    result
  }

}
