package java.io

// Ported from Apache Harmony
class BufferedOutputStream(out: OutputStream, size: Int)
    extends FilterOutputStream(out) {

  if (size <= 0) throw new IllegalArgumentException("Buffer size <= 0")

  protected var buf: Array[Byte] = new Array[Byte](size)
  protected var count: Int       = 0

  def this(out: OutputStream) = this(out, 8192)

  override def flush(): Unit = {
    flushInternal()
    out.flush()
  }

  override def write(buffer: Array[Byte], offset: Int, length: Int): Unit = {
    val internalBuffer = buf

    if (internalBuffer != null && length >= internalBuffer.length) {
      flushInternal()
      out.write(buffer, offset, length)
    } else {
      if (buffer == null) {
        throw new NullPointerException("Buffer is null")
      }

      if (offset < 0 || offset > buffer.length - length) {
        throw new ArrayIndexOutOfBoundsException(
          s"Offset out of bounds: $offset")
      }

      if (length < 0) {
        throw new ArrayIndexOutOfBoundsException(
          s"Length out of bounds: $length")
      }

      if (internalBuffer == null) {
        throw new IOException("Stream is closed")
      }

      // flush the internal buffer first if we have not enough space left
      if (length >= (internalBuffer.length - count)) {
        flushInternal()
      }

      // the length is always less than (internalBuffer.length - count) here so arraycopy is safe
      System.arraycopy(buffer, offset, internalBuffer, count, length)
      count += length
    }
  }

  override def close(): Unit = {
    if (buf != null) {
      try super.close()
      finally buf = null
    }
  }

  override def write(oneByte: Int): Unit = {
    val internalBuffer = buf
    if (internalBuffer == null) {
      throw new IOException("Stream is closed")
    }

    if (count == internalBuffer.length) {
      out.write(internalBuffer, 0, count)
      count = 0
    }
    internalBuffer(count) = oneByte.toByte
    count += 1
  }

  private def flushInternal() {
    if (count > 0) {
      out.write(buf, 0, count)
      count = 0
    }
  }
}
