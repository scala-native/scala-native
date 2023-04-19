// ported from android luni 2e317a02b5a8f9b319488ab9311521e8b4f87a0a

package java.util.zip

import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream

class InflaterOutputStream private (
    out: OutputStream,
    protected val inf: Inflater,
    protected val buf: Array[Byte]
) extends FilterOutputStream(out) {

  private var closed = false

  def this(out: OutputStream, inf: Inflater, bufferSize: Int) = {
    this(
      out,
      inf,
      if (bufferSize <= 0)
        throw new IllegalArgumentException("bufferSize <= 0: " + bufferSize)
      else new Array[Byte](bufferSize)
    )
    if (out == null) {
      throw new NullPointerException("out == null")
    } else if (inf == null) {
      throw new NullPointerException("inf == null")
    }
  }

  def this(out: OutputStream, inf: Inflater) = {
    this(out, inf, InflaterOutputStream.DEFAULT_BUFFER_SIZE)
  }

  def this(out: OutputStream) = {
    this(out, new Inflater())
  }

  override def close(): Unit = {
    if (!closed) {
      finish()
      inf.end()
      out.close()
      closed = true
    }
  }

  override def flush(): Unit = {
    finish()
    out.flush()
  }

  def finish(): Unit = {
    checkClosed()
    write()
  }

  override def write(b: Int): Unit = {
    write(Array(b.toByte), 0, 1)
  }

  override def write(bytes: Array[Byte], offset: Int, byteCount: Int): Unit = {
    checkClosed()
    checkOffsetAndCount(bytes.length, offset, byteCount)
    inf.setInput(bytes, offset, byteCount)
    write()
  }

  private def checkOffsetAndCount(
      arrayLength: Int,
      offset: Int,
      count: Int
  ): Unit = {
    if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
      throw new ArrayIndexOutOfBoundsException(
        "length=" + arrayLength + "; regionStart=" + offset
          + "; regionLength=" + count
      )
    }
  }

  private def write(): Unit = {
    try {
      var inflated = inf.inflate(buf)
      while (inflated > 0) {
        out.write(buf, 0, inflated)
        inflated = inf.inflate(buf)
      }
    } catch {
      case _: DataFormatException =>
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
