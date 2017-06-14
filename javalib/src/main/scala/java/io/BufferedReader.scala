package java.io

import scala.collection.immutable.{Stream => SStream}
import java.util.stream.{Stream, WrappedScalaStream}

class BufferedReader(in: Reader, sz: Int) extends Reader {

  if (sz <= 0) throw new IllegalArgumentException("Buffer size <= 0")

  def this(in: Reader) = this(in, 4096)

  private[this] var buf = new Array[Char](sz)

  /** Last valid value in the buffer (exclusive) */
  private[this] var end = 0

  /** Next position to read from buffer */
  private[this] var pos = 0

  private[this] var closed = false

  private[this] var validMark = false

  override def close(): Unit = if (!closed) {
    in.close()
    closed = true
  }

  override def mark(readAheadLimit: Int): Unit = {
    ensureOpen()

    val srcBuf = buf
    if (buf.size < readAheadLimit)
      buf = new Array[Char](readAheadLimit)

    // Move data to beginning of buffer
    if (pos != 0 || (buf ne srcBuf))
      System.arraycopy(srcBuf, pos, buf, 0, end - pos)

    // Update internal state
    end -= pos
    pos = 0
    validMark = true
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

  override def read(cbuf: Array[Char], off: Int, len: Int): Int = {
    ensureOpen()

    if (off < 0 || len < 0 || len > cbuf.length - off)
      throw new IndexOutOfBoundsException

    if (len == 0) 0
    else if (prepareRead()) {
      val count = Math.min(len, end - pos)
      System.arraycopy(this.buf, pos, cbuf, off, count)
      pos += count
      count
    } else -1
  }

  def readLine(): String = {
    ensureOpen()

    val sb = new java.lang.StringBuilder(80)

    while (prepareRead() && buf(pos) != '\n' && buf(pos) != '\r') {
      sb.append(buf(pos))
      pos += 1
    }

    val res = sb.toString

    if (pos >= end) {
      // We have reached the end of the stream (prepareRead() returned false)
      if (res == "") null
      else res
    } else {
      // Consume terminator
      pos += 1

      // Check whether we have a \r\n. This may overrun the buffer
      // and then push a value back which may unnecessarily invalidate
      // the mark. This mimics java behavior
      if (buf(pos - 1) == '\r' && prepareRead() && buf(pos) == '\n')
        pos += 1 // consume '\n'

      res
    }
  }

  override def ready(): Boolean = {
    ensureOpen()
    pos < end || in.ready()
  }

  override def reset(): Unit = {
    ensureOpen()

    if (!validMark) throw new IOException("Mark invalid")
    pos = 0
  }

  override def skip(n: Long): Long = {
    if (n < 0) throw new IllegalArgumentException("n negative")
    else if (pos < end) {
      val count = Math.min(n, end - pos).toInt
      pos += count
      count.toLong
    } else {
      validMark = false
      in.skip(n)
    }
  }

  def lines(): Stream[String] =
    new WrappedScalaStream(toScalaStream(), None)

  /** Prepare the buffer for reading. Returns false if EOF */
  private def prepareRead(): Boolean =
    pos < end || fillBuffer()

  /** Tries to fill the buffer. Returns false if EOF */
  private def fillBuffer(): Boolean = {
    if (validMark && end < buf.length) {
      // we may not do a full re-read, since we'll damage the mark.
      val read = in.read(buf, end, buf.length - end)
      if (read > 0) // protect from adding -1
        end += read
      read > 0
    } else {
      // Full re-read
      validMark = false
      end = in.read(buf)
      pos = 0
      end > 0
    }
  }

  private def ensureOpen(): Unit = {
    if (closed)
      throw new IOException("Operation on closed stream")
  }

  private[io] def chompNewLine(): Unit = {
    if ((pos != end || fillBuffer()) && buf(pos) == '\n') {
      pos += 1
    }
  }

  private def toScalaStream(): SStream[String] = {
    Option(readLine()) match {
      case None       => SStream.empty
      case Some(line) => line #:: toScalaStream()
    }
  }

}
