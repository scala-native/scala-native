package scala.scalanative.nio.fs.zipfs

import java.nio.ByteBuffer
import java.nio.channels.{
  ClosedChannelException, NonReadableChannelException,
  NonWritableChannelException, SeekableByteChannel
}

/* Writable in-memory channel backed by a growable byte array. On
 * `close()`, the captured contents are handed to `onClose`, which
 * installs the appropriate inode in the ZipFileSystem.
 *
 * Supports the full SeekableByteChannel surface: read+write, truncate,
 * arbitrary position. Append mode is encoded as `appendOnly = true`
 * so writes always go to the end and read is rejected
 * (java.nio.channels semantics: APPEND implies WRITE without READ).
 */
private[zipfs] final class ZipWritableByteChannel(
    initial: Array[Byte],
    readable: Boolean,
    writable: Boolean,
    appendOnly: Boolean,
    deleteOnClose: Boolean,
    onClose: (Array[Byte], Boolean) => Unit
) extends SeekableByteChannel {

  // Heap-resident growable buffer.
  private var data: Array[Byte] = initial.clone()
  private var len: Int = initial.length
  private var pos: Long = if (appendOnly) len.toLong else 0L
  private var open: Boolean = true

  override def isOpen(): Boolean = open

  override def close(): Unit = {
    if (open) {
      open = false
      val finalBytes =
        if (len == data.length) data
        else {
          val out = new Array[Byte](len)
          System.arraycopy(data, 0, out, 0, len)
          out
        }
      onClose(finalBytes, deleteOnClose)
    }
  }

  private def ensureOpen(): Unit =
    if (!open) throw new ClosedChannelException()

  override def read(dst: ByteBuffer): Int = {
    ensureOpen()
    if (!readable) throw new NonReadableChannelException()
    if (dst.remaining() == 0) 0
    else if (pos >= len.toLong) -1
    else {
      val available = (len.toLong - pos).toInt
      val n = math.min(dst.remaining(), available)
      dst.put(data, pos.toInt, n)
      pos += n
      n
    }
  }

  override def write(src: ByteBuffer): Int = {
    ensureOpen()
    if (!writable) throw new NonWritableChannelException()
    val n = src.remaining()
    if (appendOnly) pos = len.toLong
    val end = pos + n
    if (end > Int.MaxValue)
      throw new java.io.IOException("ZipFS entry exceeds Int.MaxValue bytes")
    ensureCapacity(end.toInt)
    src.get(data, pos.toInt, n)
    pos = end
    if (pos.toInt > len) len = pos.toInt
    n
  }

  private def ensureCapacity(min: Int): Unit = {
    if (min > data.length) {
      var newCap = math.max(data.length * 2, 32)
      while (newCap < min) newCap = newCap * 2
      val grown = new Array[Byte](newCap)
      System.arraycopy(data, 0, grown, 0, len)
      data = grown
    }
  }

  override def position(): Long = {
    ensureOpen()
    pos
  }

  override def position(newPosition: Long): SeekableByteChannel = {
    ensureOpen()
    if (newPosition < 0L)
      throw new IllegalArgumentException(s"negative position: $newPosition")
    // APPEND mode: spec says position is undefined / pinned to end;
    // honour the user request silently — write() will reset to len.
    pos = newPosition
    this
  }

  override def size(): Long = {
    ensureOpen()
    len.toLong
  }

  override def truncate(size: Long): SeekableByteChannel = {
    ensureOpen()
    if (!writable) throw new NonWritableChannelException()
    if (size < 0L) throw new IllegalArgumentException(s"negative size: $size")
    if (size < len.toLong) {
      len = size.toInt
      if (pos > len.toLong) pos = len.toLong
    }
    this
  }
}
