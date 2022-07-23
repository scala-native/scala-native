// ported from android luni 2e317a02b5a8f9b319488ab9311521e8b4f87a0a

package java.util.zip

import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Arrays

class InflaterOutputStream private (
    protected val inf: Inflater,
    protected val buf: Array[Byte]
) extends FilterOutputStream {

  private var closed = false

  def this(out: OutputStream) {
    this(out, new Inflater())
  }

  def this(out: OutputStream, inf: Inflater) {
    this(out, inf, InflaterOutputStream.DEFAULT_BUFFER_SIZE)
  }

  def this(out: OutputStream, inf: Inflater, bufferSize: Int) {
    this(out)
    if (out == null) {
      throw new NullPointerException("out == null")
    } else if (inf == null) {
      throw new NullPointerException("inf == null")
    }
    if (bufferSize <= 0) {
      throw new IllegalArgumentException("bufferSize <= 0: " + bufferSize)
    }
    this.inf = inf
    this.buf = new byte[bufferSize]
  }

  def close(): Unit = {
    if (!closed) {
      finish()
      inf.end()
      out.close()
      closed = true
    }
  }

  def flush(): Unit = {
    finish()
    out.flush()
  }

  def finish(): Unit = {
    checkClosed()
    write()
  }

  def write(b: Int): Unit = {
    write(Array(b.toByte), 0, 1)
  }

  def write(bytes: Array[Byte], offset: Int, byteCount: Int): Unit = {
    checkClosed()
    Arrays.checkOffsetAndCount(bytes.length, offset, byteCount)
    inf.setInput(bytes, offset, byteCount)
    write()
  }

  private def write(): Unit = {
    try {
      var inflated = inf.inflate(buf)
      while (inflated > 0) {
        out.write(buf, 0, inflated)
        inf.inflate(buf)
      }
    } catch
      (DataFormatException e) {
        throw new ZipException()
      }
  }

  private def checkClosed(): Unit = {
    if (closed) {
      throw new IOException()
    }
  }
}

object InflaterOutputStream {
  private final val DEFAULT_BUFFER_SIZE = 1024
}
