package java.io

class BufferedInputStream(_in: InputStream, size: Int)
    extends FilterInputStream(_in)
    with Closeable
    with AutoCloseable {

  if (size <= 0) throw new IllegalArgumentException("Buffer size <= 0")

  def this(in: InputStream) = this(in, 8192)

  /** The internal buffer array where the data is stored. */
  protected[this] var buf = new Array[Byte](size)

  /** The index one greater than the index of the last valid byte in the buffer. */
  protected[this] var count = 0

  /** The maximum read ahead allowed after a call to the mark method before subsequent calls to the reset method fail.. */
  private[this] var markLimit = 0

  /** The value of the pos field at the time the last mark method was called. */
  private[this] var markpos = -1

  /** The current position in the buffer. */
  protected[this] var pos = 0

  private[this] var closed = false

  /** The position of the last element in the buffer excluded */
  private[this] var end = 0

  override def available(): Int = {
    if (closed) throw new IOException()
    end - pos + in.available()
  }

  override def close(): Unit = {
    closed = true
  }

  override def mark(readLimit: Int): Unit = {
    if (!closed) {
      val srcBuf = buf
      if (buf.size < readLimit)
        buf = new Array[Byte](readLimit)

      // Move data to beginning of buffer
      if (pos != 0 || (buf ne srcBuf))
        System.arraycopy(srcBuf, pos, buf, 0, end - pos)

      // Update internal state
      end -= pos
      pos = 0
      markpos = 0
    }
  }

  override def markSupported(): Boolean = true

  override def read(): Int = {
    ensureOpen()

    if (prepareRead()) {
      val res = buf(pos).toInt
      pos += 1
      res
    } else -1
  }

  override def read(b: Array[Byte], off: Int, len: Int): Int = {
    ensureOpen()

    if (off < 0 || len < 0 || len > b.length - off)
      throw new IndexOutOfBoundsException

    if (len == 0) 0
    else if (prepareRead()) {
      val count = Math.min(len, end - pos)
      System.arraycopy(this.buf, pos, b, off, count)
      pos += count
      count
    } else -1
  }

  override def reset(): Unit = {
    ensureOpen()

    if (markpos == -1) throw new IOException("Mark invalid")
    pos = 0
  }

  override def skip(n: Long): Long = {
    if (n < 0) throw new IllegalArgumentException("n negative")
    else if (pos < end) {
      val count = Math.min(n, end - pos).toInt
      pos += count
      count.toLong
    } else {
      markpos = -1
      in.skip(n)
    }
  }

  /** Prepare the buffer for reading. Returns false if EOF */
  private def prepareRead(): Boolean =
    pos < end || fillBuffer()

  /** Tries to fill the buffer. Returns false if EOF */
  private def fillBuffer(): Boolean = {
    if (markpos >= 0 && end < buf.length) {
      // we may not do a full re-read, since we'll damage the mark.
      val read = in.read(buf, end, buf.length - end)
      if (read > 0) // protect from adding -1
        end += read
      read > 0
    } else {
      // Full re-read
      markpos = -1
      end = in.read(buf)
      pos = 0
      end > 0
    }
  }

  private def ensureOpen(): Unit = {
    if (closed)
      throw new IOException("Operation on closed stream")
  }
}
