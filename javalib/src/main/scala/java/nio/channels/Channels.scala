package java.nio
package channels

import java.io.{InputStream, OutputStream, Reader, Writer}
import java.nio.charset.{Charset, CharsetDecoder, CharsetEncoder}
import java.util.Objects

object Channels {
  def newInputStream(channel: ReadableByteChannel): InputStream = {
    Objects.requireNonNull(channel, "ch")
    new InputStream {
      private lazy val buffer = ByteBuffer.allocate(1)

      override def read(buf: Array[Byte], offset: Int, count: Int): Int = {
        channel.read(ByteBuffer.wrap(buf, offset, count))
      }

      override def read(): Int = {
        buffer.position(0)
        val read = channel.read(buffer)
        if (read <= 0) read
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
        buffer.position(0)
        buffer.put(0, b.toByte)
        channel.write(buffer)
      }
      override def write(b: Array[Byte], off: Int, len: Int): Unit = {
        channel.write(ByteBuffer.wrap(b, off, len))
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

        var eof = false
        var written = 0
        val capacity = dst.capacity()

        while ({
          val readByte = in.read()
          if (readByte == -1) {
            eof = true
            false
          } else {
            dst.put(readByte.toByte)
            written += 1
            capacity > written
          }
        }) ()

        if ((written == 0) && eof) -1
        else written
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
        var i = 0
        while (src.hasRemaining()) {
          array(i) = src.get()
          i += 1
        }
        out.write(array)
        i
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
    new Reader {
      private val capacity =
        if (minBufferCap == -1) 1024 else minBufferCap.max(1024)
      private lazy val byteBuffer = {
        val buffer = ByteBuffer.allocate(capacity)
        buffer.limit(0)
        buffer
      }
      private lazy val charBuffer = {
        val buffer = CharBuffer.allocate(capacity)
        buffer.limit(0)
        buffer
      }

      private def fillBuffers() = {
        if (!byteBuffer.hasRemaining()) {
          byteBuffer.clear()
          ch.read(byteBuffer)
          byteBuffer.limit(byteBuffer.position())
          byteBuffer.rewind()
        }
        if (!charBuffer.hasRemaining()) {
          charBuffer.clear()
          dec.decode(byteBuffer, charBuffer, false)
          charBuffer.limit(charBuffer.position())
          charBuffer.rewind()
        }
      }
      override def read(cbuf: Array[Char], off: Int, len: Int): Int = {
        if (len < 0 || len < 0 || cbuf.length < off + len)
          throw new IndexOutOfBoundsException()

        var read = 0
        while (read < len) {
          fillBuffers()

          while (charBuffer.hasRemaining() && read < len) {
            cbuf(off + read) = charBuffer.get()
            read += 1
          }
        }
        read
      }
      override def read(): Int = {
        fillBuffers()
        charBuffer.get()
      }
      override def close(): Unit = {
        ch.close()
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
    new Writer {
      private val capacity =
        if (minBufferCap == -1) 1028 else minBufferCap.max(1028)
      private lazy val charBuffer = CharBuffer.allocate(capacity)

      override def write(cbuf: Array[Char], off: Int, len: Int): Unit = {
        if (len < 0 || len < 0 || cbuf.length < off + len)
          throw new IndexOutOfBoundsException()

        synchronized {
          var written = 0
          while (written < len) {
            val toPut = capacity.min(len - written)
            charBuffer.put(cbuf, off + written, toPut)
            if (!charBuffer.hasRemaining()) flush()
            written += toPut
          }
        }
      }
      override def write(c: Int): Unit = synchronized {
        charBuffer.put(c.toChar)
        if (!charBuffer.hasRemaining()) flush()
      }
      override def flush() = synchronized {
        charBuffer.limit(charBuffer.position())
        charBuffer.position(0)
        val encoded = enc.encode(charBuffer)
        ch.write(encoded)
        charBuffer.clear()
      }
      override def close() = ch.close()
    }
  }

  def newWriter(ch: WritableByteChannel, csName: String): Writer =
    newWriter(ch, Charset.forName(csName).newEncoder(), -1)

  def newWriter(ch: WritableByteChannel, charset: Charset): Writer =
    newWriter(ch, charset.newEncoder(), -1)

}
