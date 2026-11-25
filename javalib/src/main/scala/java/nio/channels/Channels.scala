package java.nio
package channels

import java.io.{IOException, InputStream, OutputStream, Reader, Writer}
import java.nio.charset.{Charset, CharsetDecoder, CharsetEncoder}
import java.util.Objects

object Channels {
  private def checkBounds(length: Int, off: Int, len: Int): Unit =
    if (off < 0 || len < 0 || len > length - off)
      throw new IndexOutOfBoundsException()

  private def writeFully(channel: WritableByteChannel, src: ByteBuffer) =
    while (src.hasRemaining()) channel.write(src)

  def newInputStream(channel: ReadableByteChannel): InputStream = {
    Objects.requireNonNull(channel, "ch")
    new InputStream {
      private lazy val buffer = ByteBuffer.allocate(1)

      override def read(buf: Array[Byte], offset: Int, count: Int): Int = {
        checkBounds(buf.length, offset, count)
        if (count == 0) 0
        else channel.read(ByteBuffer.wrap(buf, offset, count))
      }

      override def read(): Int = {
        var read = 0
        while (read == 0) {
          buffer.clear()
          read = channel.read(buffer)
        }
        if (read < 0) -1
        else buffer.get(0) & 0xff
      }

      override def close(): Unit =
        channel.close()
    }
  }

  def newOutputStream(channel: WritableByteChannel): OutputStream = {
    Objects.requireNonNull(channel, "ch")
    new OutputStream {
      private lazy val buffer = ByteBuffer.allocate(1)
      override def write(b: Int): Unit = {
        buffer.clear()
        buffer.put(0, b.toByte)
        writeFully(channel, buffer)
      }
      override def write(b: Array[Byte], off: Int, len: Int): Unit = {
        checkBounds(b.length, off, len)
        writeFully(channel, ByteBuffer.wrap(b, off, len))
      }
      override def close(): Unit =
        channel.close()
    }
  }

  // def newInputStream(ch: AsynchronousByteChannel): InputStream
  // def newOutputStream(ch: AsynchronousByteChannel): OutputStream

  def newChannel(in: InputStream): ReadableByteChannel = {
    Objects.requireNonNull(in, "in")
    new ReadableByteChannel {
      var closed = false
      override def read(dst: ByteBuffer): Int = synchronized {
        if (closed)
          throw new ClosedChannelException()

        val len = dst.remaining()
        if (len == 0) 0
        else if (dst.hasArray()) {
          val read =
            in.read(dst.array(), dst.arrayOffset() + dst.position(), len)
          if (read > 0) dst.position(dst.position() + read)
          read
        } else {
          val buf = new Array[Byte](len.min(8192))
          val read = in.read(buf, 0, buf.length)
          if (read > 0) dst.put(buf, 0, read)
          read
        }
      }

      override def close(): Unit = synchronized {
        in.close()
        closed = true
      }

      override def isOpen(): Boolean = synchronized { !closed }
    }
  }

  def newChannel(out: OutputStream): WritableByteChannel = {
    Objects.requireNonNull(out, "out")
    new WritableByteChannel {
      var closed = false
      override def write(src: ByteBuffer): Int = synchronized {
        if (closed) throw new ClosedChannelException()

        val array = Array.ofDim[Byte](src.remaining())
        src.get(array)
        out.write(array)
        array.length
      }
      override def close(): Unit = synchronized {
        out.close()
        closed = true
      }
      override def isOpen(): Boolean = synchronized { !closed }
    }
  }

  def newReader(
      ch: ReadableByteChannel,
      dec: CharsetDecoder,
      minBufferCap: Int
  ): Reader = {
    Objects.requireNonNull(ch, "ch")
    Objects.requireNonNull(dec, "dec")
    new Reader {
      private val capacity =
        if (minBufferCap == -1) 1024 else minBufferCap.max(1024)
      private val byteBuffer = ByteBuffer.allocate(capacity)
      private val charBuffer = CharBuffer.allocate(capacity)
      byteBuffer.limit(0)
      charBuffer.limit(0)
      private var endOfInput = false
      private var flushed = false
      private var closed = false

      private def ensureOpen(): Unit =
        if (closed) throw new IOException("Stream closed")

      // Decodes at least one char into charBuffer, returns false at EOF
      private def fill(): Boolean = {
        charBuffer.clear()
        try {
          while (charBuffer.position() == 0 && !flushed) {
            val result = dec.decode(byteBuffer, charBuffer, endOfInput)
            if (result.isError()) result.throwException()
            if (result.isUnderflow() && charBuffer.position() == 0) {
              if (endOfInput) {
                val flushResult = dec.flush(charBuffer)
                if (flushResult.isUnderflow()) flushed = true
              } else {
                byteBuffer.compact()
                try {
                  if (ch.read(byteBuffer) < 0) endOfInput = true
                } finally byteBuffer.flip()
              }
            }
          }
        } finally charBuffer.flip()
        charBuffer.hasRemaining()
      }

      override def read(cbuf: Array[Char], off: Int, len: Int): Int =
        lock.synchronized {
          checkBounds(cbuf.length, off, len)
          ensureOpen()
          if (len == 0) 0
          else if (!charBuffer.hasRemaining() && !fill()) -1
          else {
            val n = len.min(charBuffer.remaining())
            charBuffer.get(cbuf, off, n)
            n
          }
        }

      override def ready(): Boolean = lock.synchronized {
        ensureOpen()
        charBuffer.hasRemaining()
      }

      override def close(): Unit = lock.synchronized {
        if (!closed) {
          closed = true
          ch.close()
        }
      }
    }
  }

  def newReader(ch: ReadableByteChannel, csName: String): Reader =
    newReader(ch, Charset.forName(csName).newDecoder(), -1);

  def newReader(ch: ReadableByteChannel, charset: Charset): Reader =
    newReader(ch, charset.newDecoder(), -1);

  def newWriter(
      ch: WritableByteChannel,
      enc: CharsetEncoder,
      minBufferCap: Int
  ): Writer = {
    Objects.requireNonNull(ch, "ch")
    Objects.requireNonNull(enc, "enc")
    new Writer {
      private val capacity =
        if (minBufferCap == -1) 1024 else minBufferCap.max(1024)
      private val charBuffer = CharBuffer.allocate(capacity)
      private val byteBuffer = ByteBuffer.allocate(capacity)
      private var closed = false

      private def ensureOpen(): Unit =
        if (closed) throw new IOException("Stream closed")

      private def writeBytes(): Unit = {
        byteBuffer.flip()
        try writeFully(ch, byteBuffer)
        finally byteBuffer.clear()
      }

      private def encodeChars(endOfInput: Boolean): Unit = {
        charBuffer.flip()
        try {
          var overflow = true
          while (overflow) {
            val result = enc.encode(charBuffer, byteBuffer, endOfInput)
            if (result.isError()) result.throwException()
            overflow = result.isOverflow()
            if (overflow) writeBytes()
          }
        } finally charBuffer.compact()
      }

      override def write(cbuf: Array[Char], off: Int, len: Int): Unit = {
        checkBounds(cbuf.length, off, len)
        lock.synchronized {
          ensureOpen()
          var written = 0
          while (written < len) {
            val toPut = charBuffer.remaining().min(len - written)
            charBuffer.put(cbuf, off + written, toPut)
            written += toPut
            if (!charBuffer.hasRemaining()) encodeChars(endOfInput = false)
          }
        }
      }

      override def flush(): Unit = lock.synchronized {
        ensureOpen()
        encodeChars(endOfInput = false)
        writeBytes()
      }

      override def close(): Unit = lock.synchronized {
        if (!closed) {
          try {
            encodeChars(endOfInput = true)
            while (enc.flush(byteBuffer).isOverflow()) writeBytes()
            writeBytes()
          } finally {
            closed = true
            ch.close()
          }
        }
      }
    }
  }

  def newWriter(ch: WritableByteChannel, csName: String): Writer =
    newWriter(ch, Charset.forName(csName).newEncoder(), -1)

  def newWriter(ch: WritableByteChannel, charset: Charset): Writer =
    newWriter(ch, charset.newEncoder(), -1)

}
