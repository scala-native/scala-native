package java.io

// Ported from Apache Harmony

class PushbackInputStream(_in: InputStream, size: Int)
    extends FilterInputStream(_in) {

  if (size <= 0) {
    throw new IllegalArgumentException("size must be > 0")
  }

  protected var buf: Array[Byte] =
    if (_in == null) null else new Array[Byte](size)
  protected var pos: Int = size

  def this(in: InputStream) = this(in, 1)

  override def available(): Int =
    if (buf == null) {
      throw new IOException()
    } else {
      buf.length - pos + in.available()
    }

  override def close(): Unit =
    if (in != null) {
      in.close()
      in = null
      buf = null
    }

  override def markSupported(): Boolean =
    false

  override def read(): Int =
    if (buf == null) {
      throw new IOException()
    } else if (pos < buf.length) { // Is there a pushback byte available?
      val b = buf(pos) & 0xFF
      pos += 1
      b
    } else {
      in.read()
    }

  override def read(buffer: Array[Byte], offset: Int, length: Int): Int =
    if (buf == null) {
      throw new IOException("Stream is closed")
    } else if (offset > buffer.length || offset < 0) {
      throw new ArrayIndexOutOfBoundsException(s"Offset out of bounds: $offset")
    } else if (length < 0 || length > buffer.length - offset) {
      throw new ArrayIndexOutOfBoundsException(s"Length out of bounds: $length")
    } else {
      var copiedBytes = 0
      var copyLength  = 0
      var newOffset   = offset

      if (pos < buf.length) {
        copyLength =
          if (buf.length - pos >= length) length else buf.length - pos
        System.arraycopy(buf, pos, buffer, newOffset, copyLength)
        newOffset += copyLength
        copiedBytes += copyLength
        // Use up the bytes in the local buffer
        pos += copyLength
      }
      // Have we copied enough?
      if (copyLength == length) {
        length
      } else {
        val inCopied = in.read(buffer, newOffset, length - copiedBytes)
        if (inCopied > 0) {
          inCopied + copiedBytes
        } else if (copiedBytes == 0) {
          inCopied
        } else {
          copiedBytes
        }
      }
    }

  override def skip(count: Long): Long =
    if (in == null) {
      throw new IOException("Stream is closed")
    } else if (count <= 0) {
      0
    } else {
      var numSkipped = 0L
      if (pos < buf.length) {
        numSkipped += (if (count < buf.length - pos) count
                       else buf.length - pos)
        pos += numSkipped.toInt
      }
      if (numSkipped < count) {
        numSkipped += in.skip(count - numSkipped)
      }
      numSkipped
    }

  def unread(buffer: Array[Byte]): Unit =
    unread(buffer, 0, buffer.length)

  def unread(buffer: Array[Byte], offset: Int, length: Int): Unit =
    if (length > pos) {
      throw new IOException("Pushback buffer full")
    } else if (offset > buffer.length || offset < 0) {
      throw new ArrayIndexOutOfBoundsException(s"Offset out of bounds: $offset")
    } else if (length < 0 || length > buffer.length - offset) {
      throw new ArrayIndexOutOfBoundsException(s"Length ouf of bounds: $length")
    } else if (buf == null) {
      throw new IOException("Stream is closed")
    } else {
      System.arraycopy(buffer, offset, buf, pos - length, length)
      pos = pos - length
    }

  def unread(oneByte: Int): Unit =
    if (buf == null) {
      throw new IOException()
    } else if (pos == 0) {
      throw new IOException("Pushback buffer full")
    } else {
      pos -= 1
      buf(pos) = oneByte.toByte
    }

  override def mark(readLimit: Int): Unit =
    ()

  override def reset(): Unit =
    throw new IOException()

}
