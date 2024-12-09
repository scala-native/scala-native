package java.io

/* The protected fields combined with the expectation that close is not synchronized with read
 * requires some care.
 *
 * References:
 *
 * - https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4225348
 * - https://issues.apache.org/jira/browse/HARMONY-6014
 *
 * Mark support makes lenient use of the "might" and "may"s in the spec.
 */
class BufferedInputStream(_in: InputStream, initialSize: Int)
    extends FilterInputStream(_in)
    with Closeable
    with AutoCloseable {

  if (initialSize <= 0) throw new IllegalArgumentException("Buffer size <= 0")

  def this(in: InputStream) = this(in, 8192)

  // per spec close will release system resources. This implies buf should be set to null
  // post close to ensure GC can release this resource
  /** The internal buffer array where the data is stored. */
  protected var buf = new Array[Byte](initialSize)

  /** The index one greater than the index of the last valid byte in the buffer.
   */
  protected var count: Int = 0

  private var closed: Boolean = false

  /** The maximum read ahead allowed after a call to the mark method before*
   *  subsequent calls to the reset method fail.
   */
  protected var marklimit: Int = 0

  /** The value of the pos field at the time the last mark method was called. */
  protected var markpos: Int = -1

  /** The current position in the buffer. */
  protected var pos: Int = 0

  override def available(): Int = {
    val in = this.in
    val buf = this.buf
    ensureOpen(in, buf)
    synchronized {
      in.available() + count - pos
    }
  }

  // from spec: "closing a previously closed stream has no effect"
  override def close(): Unit = {
    if (!closed) {
      closed = true
      val in = this.in
      if (in != null)
        in.close()
      // from spec "releases any system resources associated".
      // implies
      this.in = null
      buf = null
    }
  }

  override def mark(readLimit: Int): Unit = synchronized {
    marklimit = readLimit
    markpos = pos
  }

  override def markSupported(): Boolean = true

  // can block
  // returns -1 on end of stream
  // otherwise returns next byte of data
  // or throws IOException
  override def read(): Int = {
    val in = this.in
    val buf = this.buf
    ensureOpen(in, buf)
    synchronized {
      if (pos < count) {
        val res = buf(pos).toInt & 0xff
        pos += 1
        res
      } else {
        fillBuffer(buf, in) match {
          case None => -1
          case Some(nextBuffer) =>
            val res = nextBuffer(pos).toInt & 0xff
            pos += 1
            res
        }
      }
    }
  }

  override def read(b: Array[Byte], off: Int, len: Int): Int = {
    val in = this.in
    val buf = this.buf
    ensureOpen(in, buf)

    if (off < 0 || len < 0 || len > b.length - off)
      throw new IndexOutOfBoundsException

    if (len == 0) 0
    else
      synchronized {
        unsafeRead(b, off, len, buf, in)
      }
  }

  override def reset(): Unit = {
    val in = this.in
    val buf = this.buf
    ensureOpen(in, buf)

    synchronized {
      if (markpos == -1) throw new IOException("Mark invalid")
      pos = markpos
    }
  }

  // per spec: if n is < 0 then no bytes are skipped and the return value is 0
  override def skip(n: Long): Long = {
    val in = this.in
    val buf = this.buf
    ensureOpen(in, buf)

    if (n <= 0L) 0L
    else {
      synchronized {
        skipUnsafe(requested = n, initialBuffer = buf, source = in)
      }
    }
  }

  // https://github.com/scala-native/scala-native/pull/1767#discussion_r423120768
  // to be called like this (to avoid allocating a Tuple2):
  //   val in = this.in
  //   val buf = this.buf
  //   ensureOpen(in, buf)
  private def ensureOpen(in: InputStream, buf: Array[Byte]): Unit = {
    /* First read `in` and `buf`, then `closed`. Since `closed` is the first thing
     *  that is set to `true` in `close()`, we know that if `closed` is false, the
     *  `in` and `buf` are non-null.
     */
    if (closed)
      throw new IOException("Operation on closed stream")
  }

  /* Reads up to sourceBuffer.length bytes from the source input stream.
   * This will also replace `buf` if a larger buffer is required to handle mark.
   *
   * postcondition: markpos invalidated if pos - markpos exceeds marklimit. Not exactly at the
   * boundary tho as this is only a "may" in spec.
   *
   * @returns buf
   */
  private def fillBuffer(
      sourceBuffer: Array[Byte],
      source: InputStream
  ): Option[Array[Byte]] = {
    if (markpos != -1 && (pos - markpos <= marklimit))
      fillMarkedBuffer(sourceBuffer, source)
    else
      fillUnmarkedBuffer(sourceBuffer, source)
  }

  private def fillUnmarkedBuffer(
      sourceBuffer: Array[Byte],
      source: InputStream
  ): Option[Array[Byte]] = {
    // mark is always invalidated in this case
    marklimit = 0
    markpos = -1

    val bytesRead = source.read(sourceBuffer)

    if (bytesRead == -1) {
      pos = 0
      count = 0
      None
    } else {
      pos = 0
      count = bytesRead
      Some(sourceBuffer)
    }
  }

  /* For mark (markpos == -1) the logic is:
   * If there is space in the current buffer: read into buffer starting at `count`
   *
   * If there is no space in the current buffer: create a larger buffer, copy old, read into larger
   * buffer starting at `count`.
   *
   * The mark is not invalidated in this method: Per spec there is no requirement to invalidate mark
   * *exactly* when pos - markpos exceeds marklimit. This is "generous" and migh permit a reset
   * beyond marklimit.
   */
  private def fillMarkedBuffer(
      sourceBuffer: Array[Byte],
      source: InputStream
  ): Option[Array[Byte]] = {
    val buffer = if (count < sourceBuffer.length) {
      sourceBuffer
    } else {
      val newBuffer = new Array[Byte](sourceBuffer.length * 2)
      sourceBuffer.copyToArray(newBuffer)
      buf = newBuffer
      if (closed)
        buf = null
      newBuffer
    }

    val bytesRead = source.read(buffer, count, buffer.length - count)
    if (bytesRead == -1)
      None
    else {
      count += bytesRead
      Some(buffer)
    }
  }

  private def unsafeRead(
      targetBuffer: Array[Byte],
      initialOffset: Int,
      requested: Int,
      initialBuffer: Array[Byte],
      source: InputStream
  ): Int = {
    var sourceBuffer: Array[Byte] = initialBuffer
    var remaining: Int = requested
    var targetOffset: Int = initialOffset
    var bytesRead: Int = 0

    while (remaining > 0) {
      if (pos + remaining <= count) {
        // all remaining can be read from the source buffer
        System.arraycopy(
          sourceBuffer,
          pos,
          targetBuffer,
          targetOffset,
          remaining
        )
        pos += remaining
        bytesRead += remaining
        remaining = 0
      } else {
        val available = count - pos
        if (available > 0) {
          System.arraycopy(
            sourceBuffer,
            pos,
            targetBuffer,
            targetOffset,
            available
          )
        }

        // fill source buffer from source stream
        fillBuffer(sourceBuffer, source) match {
          // end of source stream
          case None =>
            if (available == 0)
              bytesRead = -1
            else
              bytesRead += available

            remaining = 0

          // source read into nextBuffer
          case Some(nextBuffer) =>
            sourceBuffer = nextBuffer
            targetOffset += available
            bytesRead += available

            remaining -= available
        }
      }
    }

    bytesRead
  }

  private def skipUnsafe(
      requested: Long,
      initialBuffer: Array[Byte],
      source: InputStream
  ) = {
    var sourceBuffer: Array[Byte] = initialBuffer
    var remaining: Int = requested.toInt
    var bytesSkipped: Long = 0L

    while (remaining > 0) {
      if (pos + remaining <= count) {
        pos += remaining
        bytesSkipped += remaining
        remaining = 0
      } else {
        val available = count - pos

        // fill source buffer from in stream
        fillBuffer(sourceBuffer, source) match {
          // end of source stream
          case None =>
            if (available == 0)
              bytesSkipped = -1
            else
              bytesSkipped += available

            remaining = 0

          // source read into nextBuffer
          case Some(nextBuffer) =>
            sourceBuffer = nextBuffer
            bytesSkipped += available

            remaining -= available
        }
      }
    }

    bytesSkipped
  }
}
