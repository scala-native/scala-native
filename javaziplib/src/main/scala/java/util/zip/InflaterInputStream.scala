package java.util.zip

// Ported from Apache Harmony

import java.io.{EOFException, FilterInputStream, IOException, InputStream}

class InflaterInputStream private (in: InputStream,
                                   protected var inf: Inflater,
                                   protected var buf: Array[Byte])
    extends FilterInputStream(in) {
  def this(in: InputStream, inf: Inflater, len: Int) =
    this(in, inf, new Array[Byte](len))
  def this(in: InputStream, inf: Inflater) =
    this(in, inf, InflaterInputStream.BUF_SIZE)
  def this(in: InputStream) = this(in, new Inflater())

  if (buf.length <= 0) {
    throw new IllegalArgumentException()
  }

  protected var len: Int           = 0
  private[zip] var closed: Boolean = false
  private[zip] var eof: Boolean    = false

  override def read(): Int = {
    val b = new Array[Byte](1)
    if (read(b, 0, 1) == -1) {
      -1
    } else {
      b(0) & 0xFF
    }
  }

  override def read(buffer: Array[Byte], off: Int, nbytes: Int): Int = {
    if (closed) {
      throw new IOException("Stream is closed")
    }

    if (null == buffer) {
      throw new NullPointerException()
    }

    if (off < 0 || nbytes < 0 || off + nbytes > buffer.length) {
      throw new IndexOutOfBoundsException()
    }

    if (nbytes == 0) {
      return 0
    }

    if (eof) {
      return -1
    }

    // avoid int overflow, check null buffer
    if (off > buffer.length || nbytes < 0 || off < 0
        || buffer.length - off < nbytes) {
      throw new ArrayIndexOutOfBoundsException()
    }

    do {
      if (inf.needsInput()) {
        fill()
      }
      // Invariant: if reading returns -1 or throws, eof must be true.
      // It may also be true if the next read() should return -1.
      try {
        val result = inf.inflate(buffer, off, nbytes)
        eof = inf.finished()
        if (result > 0) {
          return result
        } else if (eof) {
          return -1
        } else if (inf.needsDictionary()) {
          eof = true
          return -1
        } else if (len == -1) {
          eof = true
          throw new EOFException()
          // If result == 0, fill() and try again
        }
      } catch {
        case e: DataFormatException =>
          eof = true
          if (len == -1) {
            throw new EOFException()
          }
          throw new IOException().initCause(e).asInstanceOf[IOException]
      }
    } while (true)

    throw new IllegalStateException()
  }

  protected def fill(): Unit = {
    if (closed) {
      throw new IOException("Stream is closed")
    } else if ({ len = in.read(buf); len > 0 }) {
      inf.setInput(buf, 0, len)
    }
  }

  override def skip(nbytes: Long): Long = {
    if (nbytes >= 0) {
      if (buf == null) {
        buf =
          new Array[Byte](Math.min(nbytes, InflaterInputStream.BUF_SIZE).toInt)
      }
      var count, rem: Long = 0L
      while (count < nbytes) {
        val x = read(buf,
                     0,
                     if ({ rem = nbytes - count; rem > buf.length }) buf.length
                     else rem.toInt)
        if (x == -1) {
          return count
        }
        count += x
      }
      return count
    }
    throw new IllegalArgumentException()
  }

  override def available(): Int = {
    if (closed) {
      throw new IOException("Stream is closed")
    } else if (eof) {
      0
    } else {
      1
    }
  }

  override def close(): Unit = {
    if (!closed) {
      inf.end()
      closed = true
      eof = true
      super.close()
    }
  }

  override def mark(readLimit: Int): Unit =
    () // Do nothing

  override def reset(): Unit =
    throw new IOException()

  override def markSupported(): Boolean =
    false

}

private[zip] object InflaterInputStream {
  final val BUF_SIZE: Int = 512
}
