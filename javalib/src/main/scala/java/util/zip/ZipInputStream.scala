package java.util.zip

import java.nio.charset.{Charset, StandardCharsets}
import java.io.{
  EOFException,
  IOException,
  InputStream,
  PushbackInputStream,
  UTFDataFormatException
}
import java.util.jar.{Attributes, JarEntry}

// Ported from Apache Harmony

class ZipInputStream(_in: InputStream, charset: Charset)
    extends InflaterInputStream(new PushbackInputStream(_in, 4096),
                                new Inflater(true))
    with ZipConstants {
  import ZipInputStream._

  def this(in: InputStream) = this(in, StandardCharsets.UTF_8)

  private var entriesEnd: Boolean         = false
  private var hasDD: Boolean              = false
  private var entryIn: Int                = 0
  private var inRead: Int                 = 0
  private var lastRead: Int               = 0
  private[zip] var currentEntry: ZipEntry = null
  private final var hdrBuf: Array[Byte]   = new Array[Byte](LOCHDR - LOCVER)
  private final var crc: CRC32            = new CRC32
  private var nameBuf: Array[Byte]        = new Array[Byte](256)
  private var charBuf: Array[Char]        = new Array[Char](256)

  override def close(): Unit =
    if (!closed) {
      closeEntry() // Close the current entry
      super.close()
    }

  def closeEntry(): Unit = {
    if (closed) {
      throw new IOException("Stream is closed")
    }
    if (currentEntry == null) {
      return
    }
    if (currentEntry.isInstanceOf[JarEntry]) {
      val temp = currentEntry.asInstanceOf[JarEntry].getAttributes()
      if (temp != null && temp.containsKey("hidden")) {
        return
      }
    }

    /*
     * The following code is careful to leave the ZipInputStream in a
     * consistent state, even when close() results in an exception. It does
     * so by:
     *  - pushing bytes back into the source stream
     *  - reading a data descriptor footer from the source stream
     *  - resetting fields that manage the entry being closed
     */

    // Ensure all entry bytes are read
    var failure: Exception = null
    try {
      skip(Long.MaxValue)
    } catch {
      case e: Exception =>
        failure = e
    }

    var inB, out: Int = 0
    if (currentEntry.compressionMethod == DEFLATED) {
      inB = inf.getTotalIn()
      out = inf.getTotalOut()
    } else {
      inB = inRead
      out = inRead
    }
    val diff = entryIn - inB
    // Pushback any required bytes
    if (diff != 0) {
      in.asInstanceOf[PushbackInputStream].unread(buf, len - diff, diff)
    }

    try {
      readAndVerifyDataDescriptor(inB, out)
    } catch {
      case e: Exception =>
        if (failure == null) { // otherwise we're already going to throw
          failure = e
        }
    }

    inf.reset()
    lastRead = 0
    inRead = 0
    entryIn = 0
    len = 0
    crc.reset()
    currentEntry = null

    if (failure != null) {
      failure match {
        case _: IOException | _: RuntimeException => throw failure
        case e =>
          val error = new AssertionError()
          error.initCause(failure)
          throw error
      }
    }

  }

  private def readAndVerifyDataDescriptor(inB: Int, out: Int): Unit = {
    if (hasDD) {
      in.read(hdrBuf, 0, EXTHDR)
      if (getLong(hdrBuf, 0) != EXTSIG) {
        throw new ZipException("Unknown format")
      }
      currentEntry.crc = getLong(hdrBuf, EXTCRC)
      currentEntry.compressedSize = getLong(hdrBuf, EXTSIZ)
      currentEntry.size = getLong(hdrBuf, EXTLEN)
    }
    if (currentEntry.crc != crc.getValue()) {
      throw new ZipException("Crc mismatch")
    }
    if (currentEntry.compressedSize != inB || currentEntry.size != out) {
      throw new ZipException("Size mismatch")
    }
  }

  def getNextEntry(): ZipEntry = {
    closeEntry()
    if (entriesEnd) {
      null
    } else {
      var x, count = 0
      while (count != 4) {
        count += {
          val read = in.read(hdrBuf, count, 4 - count)
          x = read
          read
        }
        if (x == -1) {
          return null
        }
      }
      val hdr = getLong(hdrBuf, 0)
      if (hdr == CENSIG) {
        entriesEnd = true
        return null
      }
      if (hdr != LOCSIG) {
        return null
      }

      // Read the local header
      count = 0
      while (count != (LOCHDR - LOCVER)) {
        count += {
          val read = in.read(hdrBuf, count, (LOCHDR - LOCVER) - count)
          x = read
          read
        }
        if (x == -1) {
          throw new EOFException()
        }
      }
      val version = getShort(hdrBuf, 0) & 0xff
      if (version > ZIPLocalHeaderVersionNeeded) {
        throw new ZipException("Cannot read version")
      }
      val flags = getShort(hdrBuf, LOCFLG - LOCVER)
      hasDD = ((flags & ZIPDataDescriptorFlag) == ZIPDataDescriptorFlag)
      val cetime              = getShort(hdrBuf, LOCTIM - LOCVER)
      val cemodDate           = getShort(hdrBuf, LOCTIM - LOCVER + 2)
      val cecompressionMethod = getShort(hdrBuf, LOCHOW - LOCVER)
      var cecrc               = 0L
      var cecompressedSize    = 0L
      var cesize              = -1L
      if (!hasDD) {
        cecrc = getLong(hdrBuf, LOCCRC - LOCVER)
        cecompressedSize = getLong(hdrBuf, LOCSIZ - LOCVER)
        cesize = getLong(hdrBuf, LOCLEN - LOCVER)
      }
      val flen = getShort(hdrBuf, LOCNAM - LOCVER)
      if (flen == 0) {
        throw new ZipException("Entry is not named")
      }
      val elen = getShort(hdrBuf, LOCEXT - LOCVER)

      count = 0
      if (flen > nameBuf.length) {
        nameBuf = new Array[Byte](flen)
        charBuf = new Array[Char](flen)
      }
      while (count != flen) {
        count += {
          val read = in.read(nameBuf, count, flen - count)
          x = read
          read
        }
        if (x == -1) {
          throw new EOFException()
        }
      }
      currentEntry = createZipEntry(
        convertUTF8WithBuf(nameBuf, charBuf, 0, flen))
      currentEntry.time = cetime
      currentEntry.modDate = cemodDate
      currentEntry.setMethod(cecompressionMethod)
      if (cesize != -1) {
        currentEntry.setCrc(cecrc)
        currentEntry.setSize(cesize)
        currentEntry.setCompressedSize(cecompressedSize)
      }
      if (elen > 0) {
        count = 0
        val e = new Array[Byte](elen)
        while (count != elen) {
          count += {
            val read = in.read(e, count, elen - count)
            x = read
            read
          }
          if (x == -1) {
            throw new EOFException()
          }
        }
        currentEntry.setExtra(e)
      }
      currentEntry
    }
  }

  override def read(buffer: Array[Byte], start: Int, length: Int): Int = {
    if (closed) {
      throw new IOException("Stream is closed")
    }
    if (inf.finished() || currentEntry == null) {
      return -1
    }
    // avoid int overflow, check null buffer
    if (start > buffer.length || length < 0 || start < 0
        || buffer.length - start < length) {
      throw new ArrayIndexOutOfBoundsException()
    }

    if (currentEntry.compressionMethod == STORED) {
      val csize = currentEntry.size.toInt

      if (inRead >= csize) {
        return -1
      }

      if (lastRead >= len) {
        lastRead = 0
        if ({ len = in.read(buf); len } == -1) {
          eof = true
          return -1
        }
        entryIn += len
      }
      var toRead = if (length > (len - lastRead)) len - lastRead else length
      if ((csize - inRead) < toRead) {
        toRead = csize - inRead
      }
      System.arraycopy(buf, lastRead, buffer, start, toRead)
      lastRead += toRead
      inRead += toRead
      crc.update(buffer, start, toRead)
      return toRead
    }
    if (inf.needsInput()) {
      fill()
      if (len > 0) {
        entryIn += len
      }
    }
    var read = 0
    try {
      read = inf.inflate(buffer, start, length)
    } catch {
      case e: DataFormatException =>
        throw new ZipException(e.getMessage())
    }
    if (read == 0 && inf.finished()) {
      -1
    } else {
      crc.update(buffer, start, read)
      read
    }
  }

  override def skip(value: Long): Long = {
    if (value < 0) {
      throw new IllegalArgumentException()
    }

    var skipped = 0L
    val b       = new Array[Byte](Math.min(value, 2048L).toInt)
    while (skipped != value) {
      val rem = value - skipped
      val x =
        read(b, 0, if (b.length > rem) rem.toInt else b.length)
      if (x == -1) {
        return skipped
      }
      skipped += x
    }
    skipped
  }

  override def available(): Int = {
    if (closed) {
      throw new IOException("Stream is closed")
    } else if (currentEntry == null || inRead < currentEntry.size) {
      1
    } else {
      0
    }
  }

  protected def createZipEntry(name: String): ZipEntry =
    new ZipEntry(name)

  private def getShort(buffer: Array[Byte], off: Int): Int =
    (buffer(off) & 0xFF) | ((buffer(off + 1) & 0xFF) << 8)

  private def getLong(buffer: Array[Byte], off: Int): Long = {
    var l = 0L
    l |= (buffer(off) & 0xFF)
    l |= (buffer(off + 1) & 0xFF) << 8
    l |= (buffer(off + 2) & 0xFF) << 16
    l |= (buffer(off + 3) & 0xFF).toLong << 24
    l
  }

  private def convertUTF8WithBuf(buf: Array[Byte],
                                 out: Array[Char],
                                 offset: Int,
                                 utfSize: Int): String = {
    var count, s, a = 0
    while (count < utfSize) {
      count += 1
      out(s) = buf(offset + count - 1).toChar
      if (out(s) < '\u0080') {
        s += 1
      } else if (({ a = out(s); a } & 0xe0) == 0xc0) {
        if (count >= utfSize)
          throw new UTFDataFormatException(
            s"Second byte at $count doesn't match UTF8 specification.")

        val b = buf(count)
        count += 1
        if ((b & 0xC0) != 0x80)
          throw new UTFDataFormatException(
            s"Second byte at ${count - 1} doesn't match UTF8 specification.")

        out(s) = (((a & 0x1F) << 6) | (b & 0x3F)).toChar
        s += 1
      } else if ((a & 0xf0) == 0xe0) {
        if (count + 1 >= utfSize)
          throw new UTFDataFormatException(
            s"Third byte at ${count + 1} doesn't match UTF8 specification.")

        val b = buf(count)
        count += 1
        val c = buf(count)
        count += 1
        if (((b & 0xC0) != 0x80) || ((c & 0xC0) != 0x80))
          throw new UTFDataFormatException(
            s"Second or third byte at ${count - 2} doesnt match UTF8 specification.")

        out(s) = (((a & 0x0F) << 12) | ((b & 0x3F) << 6) | (c & 0x3F)).toChar
        s += 1
      } else {
        throw new UTFDataFormatException(
          s"Input at ${count - 1} doesn't match UTF8 specification")
      }
    }
    new String(out, 0, s);
  }

}

object ZipInputStream {
  final val DEFLATED: Int                    = 8
  final val STORED: Int                      = 0
  final val ZIPDataDescriptorFlag: Int       = 8
  final val ZIPLocalHeaderVersionNeeded: Int = 20
}
