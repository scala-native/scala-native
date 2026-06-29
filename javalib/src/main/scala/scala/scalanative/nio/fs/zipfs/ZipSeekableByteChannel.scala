package scala.scalanative.nio.fs.zipfs

import java.io.{IOException, InputStream}
import java.nio.ByteBuffer
import java.nio.channels.{
  ClosedChannelException, NonWritableChannelException, SeekableByteChannel
}

/** Read-only `SeekableByteChannel` over the inflated bytes of a single zip
 *  entry. Slurps the whole entry into a heap `ByteBuffer` at construction time
 *  — fine for the v1 target (NIR files, typically kilobytes). Switch to lazy
 *  buffering only when profiling shows it.
 */
private[zipfs] final class ZipSeekableByteChannel(
    data: Array[Byte]
) extends SeekableByteChannel {

  private val buf: ByteBuffer = ByteBuffer.wrap(data).asReadOnlyBuffer()
  private var _open: Boolean = true
  // Tracked separately from the underlying ByteBuffer because the JDK
  // SeekableByteChannel contract allows positions past `size()` — the
  // ByteBuffer position is capped at `capacity`, so we keep the user-set
  // value here and only project onto `buf` when actually reading.
  private var _pos: Long = 0L

  override def isOpen(): Boolean = _open

  override def close(): Unit = { _open = false }

  private def ensureOpen(): Unit =
    if (!_open) throw new ClosedChannelException()

  override def read(dst: ByteBuffer): Int = {
    ensureOpen()
    // A zero-length read returns 0 even at EOF — checked before the
    // EOF test (channel-semantics contract).
    if (dst.remaining() == 0) 0
    else if (_pos >= buf.capacity().toLong) -1
    else {
      buf.position(_pos.toInt)
      val n = math.min(dst.remaining(), buf.remaining())
      // Copy n bytes from buf into dst without disturbing buf's state
      // beyond what we project via `_pos`.
      val slice = buf.slice()
      slice.limit(n)
      dst.put(slice)
      _pos += n
      n
    }
  }

  override def write(src: ByteBuffer): Int = {
    // Closed-state check wins over the read-only check, matching what
    // FileChannel does: ClosedChannelException is thrown irrespective of
    // why the write would have otherwise failed.
    ensureOpen()
    throw new NonWritableChannelException()
  }

  override def position(): Long = {
    ensureOpen()
    _pos
  }

  override def position(newPosition: Long): SeekableByteChannel = {
    ensureOpen()
    if (newPosition < 0L)
      throw new IllegalArgumentException(s"negative position: $newPosition")
    // SeekableByteChannel spec: positions beyond `size()` are legal and
    // must not resize the channel. Read at or past EOF returns -1.
    _pos = newPosition
    this
  }

  override def size(): Long = {
    ensureOpen()
    buf.capacity().toLong
  }

  override def truncate(size: Long): SeekableByteChannel = {
    ensureOpen()
    throw new NonWritableChannelException()
  }
}

private[zipfs] object ZipSeekableByteChannel {

  /** Drain `is` into a byte array of the expected size. `expectedSize` may be
   *  -1 for entries with unknown size (stream until EOF).
   */
  def slurp(is: InputStream, expectedSize: Long): Array[Byte] = {
    try {
      if (expectedSize >= 0L && expectedSize <= Int.MaxValue) {
        val n = expectedSize.toInt
        val out = new Array[Byte](n)
        var read = 0
        while (read < n) {
          val r = is.read(out, read, n - read)
          if (r < 0) {
            // Short entry — truncate to what we got.
            val trimmed = new Array[Byte](read)
            System.arraycopy(out, 0, trimmed, 0, read)
            return trimmed
          }
          read += r
        }
        out
      } else {
        val baos = new java.io.ByteArrayOutputStream(8192)
        val buf = new Array[Byte](8192)
        var r = is.read(buf)
        while (r >= 0) {
          baos.write(buf, 0, r)
          r = is.read(buf)
        }
        baos.toByteArray()
      }
    } finally is.close()
  }
}
