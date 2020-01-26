package java.util.zip

// Ported from Apache Harmony

import java.io.{EOFException, IOException, InputStream}

class GZIPInputStream(in: InputStream, size: Int)
    extends InflaterInputStream(in, new Inflater(true), size) {

  protected var crc: CRC32   = new CRC32()
  protected var eos: Boolean = false

  def this(in: InputStream) = this(in, InflaterInputStream.BUF_SIZE)

  {
    val header = new Array[Byte](10)
    readFully(header, 0, header.length)
    if (getShort(header, 0) != GZIPInputStream.GZIP_MAGIC) {
      throw new IOException("Unknown format")
    }
    val flags = header(3)
    val hcrc  = (flags & GZIPInputStream.FHCRC) != 0
    if (hcrc) {
      crc.update(header, 0, header.length)
    }
    if ((flags & GZIPInputStream.FEXTRA) != 0) {
      readFully(header, 0, 2)
      if (hcrc) {
        crc.update(header, 0, 2)
      }
      var length = getShort(header, 0)
      while (length > 0) {
        val max    = if (length > buf.length) buf.length else length
        val result = in.read(buf, 0, max)
        if (result == -1) {
          throw new EOFException()
        }
        if (hcrc) {
          crc.update(buf, 0, result)
        }
        length -= result
      }
    }
    if ((flags & GZIPInputStream.FNAME) != 0) {
      readZeroTerminated(hcrc)
    }
    if ((flags & GZIPInputStream.FCOMMENT) != 0) {
      readZeroTerminated(hcrc)
    }
    if (hcrc) {
      readFully(header, 0, 2)
      val crc16 = getShort(header, 0)
      if ((crc.getValue() & 0xffff) != crc16) {
        throw new IOException("Crc mismatch")
      }
      crc.reset()
    }
  }

  override def close(): Unit = {
    eos = true
    super.close()
  }

  private def getLong(buffer: Array[Byte], off: Int): Long = {
    var l: Long = 0L
    l = l | (buffer(off) & 0xFF)
    l = l | ((buffer(off + 1) & 0xFF) << 8)
    l = l | ((buffer(off + 2) & 0xFF) << 16)
    l = l | ((buffer(off + 3) & 0xFF).toLong << 24)
    l
  }

  private def getShort(buffer: Array[Byte], off: Int): Int = {
    (buffer(off) & 0xFF) | ((buffer(off + 1) & 0xFF) << 8)
  }

  override def read(buffer: Array[Byte], off: Int, nbytes: Int): Int = {
    if (closed) {
      throw new IOException("Stream closed")
    } else if (eos) {
      -1
    } else if (off > buffer.length || nbytes < 0 || off < 0 || buffer.length - off < nbytes) {
      throw new ArrayIndexOutOfBoundsException()
    } else {
      val bytesRead =
        try super.read(buffer, off, nbytes)
        finally eos = eof

      if (bytesRead != -1) {
        crc.update(buffer, off, bytesRead)
      }

      if (eos) {
        verifyCrc()
      }

      bytesRead
    }
  }

  private def verifyCrc(): Unit = {
    val size        = inf.getRemaining()
    val trailerSize = 8
    val b           = new Array[Byte](trailerSize)
    val copySize    = if (size > trailerSize) trailerSize else size

    System.arraycopy(buf, len - size, b, 0, copySize)
    readFully(b, copySize, trailerSize - copySize)

    if (getLong(b, 0) != crc.getValue()) {
      throw new IOException("Crc mismatch")
    } else if (getLong(b, 4).toInt != inf.getTotalOut()) {
      throw new IOException("Size mismatch")
    }
  }

  private def readFully(buffer: Array[Byte], offset: Int, length: Int) {
    var result: Int = 0
    var off: Int    = offset
    var l: Int      = length
    while (l > 0) {
      result = in.read(buffer, off, l)
      if (result == -1) {
        throw new EOFException()
      }
      off += result
      l -= result
    }
  }

  private def readZeroTerminated(hcrc: Boolean): Unit = {
    var result: Int = 0
    while ({ result = in.read; result > 0 }) {
      if (hcrc) {
        crc.update(result)
      }
    }
    if (result == -1) {
      throw new EOFException()
    }
    if (hcrc) {
      crc.update(result)
    }
  }

}

object GZIPInputStream {
  final val GZIP_MAGIC: Int = 0x8b1f

  private final val FCOMMENT: Int = 16
  private final val FEXTRA: Int   = 4
  private final val FHCRC: Int    = 2
  private final val FNAME: Int    = 8
}
