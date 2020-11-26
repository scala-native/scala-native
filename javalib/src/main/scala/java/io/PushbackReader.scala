package java.io

// Ported from Apache Harmony

class PushbackReader(in: Reader, size: Int) extends FilterReader(in) {

  if (size <= 0) throw new IllegalArgumentException("size <= 0")

  private var buf: Array[Char] = new Array[Char](size)
  private var pos: Int         = size

  def this(in: Reader) = this(in, 1)

  override def close(): Unit = lock.synchronized {
    buf = null
    in.close()
  }

  override def mark(readAheadLimit: Int): Unit =
    throw new IOException("mark/reset not supported")

  override def markSupported(): Boolean =
    false

  override def read(): Int = lock.synchronized {
    if (buf == null) {
      throw new IOException("Stream closed")
    }

    if (pos < buf.length) {
      val r = buf(pos)
      pos += 1
      r
    } else {
      in.read()
    }
  }

  override def read(buffer: Array[Char], offset: Int, count: Int): Int =
    lock.synchronized {
      if (buf == null) {
        throw new IOException("Stream closed")
      }

      if (offset < 0 || count < 0 || offset > buffer.length - count) {
        throw new IndexOutOfBoundsException()
      }

      var copiedChars = 0
      var newOffset   = offset
      var copyLength  = 0

      if (pos < buf.length) {
        copyLength = if (buf.length - pos >= count) count else buf.length - pos
        System.arraycopy(buf, pos, buffer, newOffset, copyLength)
        newOffset += copyLength
        copiedChars += copyLength
        pos += copyLength
      }

      if (copyLength == count) {
        count
      } else {
        val inCopied = in.read(buffer, newOffset, count - copiedChars)
        if (inCopied > 0) {
          inCopied + copiedChars
        } else if (copiedChars == 0) {
          inCopied
        } else {
          copiedChars
        }
      }
    }

  override def ready(): Boolean = lock.synchronized {
    if (buf == null) {
      throw new IOException("Stream closed")
    }

    buf.length - pos > 0 || in.ready()
  }

  override def reset(): Unit =
    throw new IOException("mark/reset not supported")

  def unread(buffer: Array[Char]): Unit =
    unread(buffer, 0, buffer.length)

  def unread(buffer: Array[Char], offset: Int, length: Int): Unit =
    lock.synchronized {
      if (buf == null) {
        throw new IOException("Stream closed")
      }
      if (length > pos) {
        throw new IOException("Pushback buffer full")
      }
      if (offset > buffer.length - length || offset < 0) {
        throw new ArrayIndexOutOfBoundsException()
      }
      if (length < 0) {
        throw new ArrayIndexOutOfBoundsException()
      }

      var i = offset + length - 1
      while (i >= offset) {
        unread(buffer(i))
        i -= 1
      }
    }

  def unread(oneChar: Int): Unit = lock.synchronized {
    if (buf == null) {
      throw new IOException("Stream closed")
    }
    if (pos == 0) {
      throw new IOException("Pushback buffer overflow")
    }
    pos -= 1
    buf(pos) = oneChar.toChar
  }

  override def skip(count: Long): Long = lock.synchronized {
    if (buf == null) {
      throw new IOException("Stream closed")
    }
    if (count == 0) {
      0
    } else {
      val availableFromBuffer = buf.length - pos
      if (availableFromBuffer > 0) {
        val requiredFromIn = count - availableFromBuffer
        if (requiredFromIn <= 0) {
          pos += count.toInt
          count
        } else {
          pos += availableFromBuffer
          availableFromBuffer + in.skip(requiredFromIn)
        }
      } else {
        in.skip(count)
      }
    }
  }

}
