package java.nio
package channels

import java.io.{
  InputStream, InputStreamReader, OutputStream, OutputStreamWriter, Reader,
  Writer
}
import java.nio.charset.{Charset, CharsetDecoder, CharsetEncoder}
import java.util.Objects

object Channels {
  private def writeFully(channel: WritableByteChannel, src: ByteBuffer) =
    while (src.hasRemaining()) channel.write(src)

  def newInputStream(channel: ReadableByteChannel): InputStream = {
    Objects.requireNonNull(channel, "ch")
    new InputStream {
      private val buffer = ByteBuffer.allocate(1)

      override def read(buf: Array[Byte], offset: Int, count: Int): Int = {
        Objects.checkFromIndexSize(offset, count, buf.length)
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
      private val buffer = ByteBuffer.allocate(1)
      override def write(b: Int): Unit = {
        buffer.clear()
        buffer.put(0, b.toByte)
        writeFully(channel, buffer)
      }
      override def write(b: Array[Byte], off: Int, len: Int): Unit = {
        Objects.checkFromIndexSize(off, len, b.length)
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

        val len = src.remaining()
        if (src.hasArray()) {
          out.write(src.array(), src.arrayOffset() + src.position(), len)
          src.position(src.position() + len)
        } else {
          val array = new Array[Byte](len)
          src.get(array)
          out.write(array)
        }
        len
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
    dec.reset()
    new InputStreamReader(newInputStream(ch), dec, minBufferCap)
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
    enc.reset()
    new OutputStreamWriter(newOutputStream(ch), enc)
  }

  def newWriter(ch: WritableByteChannel, csName: String): Writer =
    newWriter(ch, Charset.forName(csName).newEncoder(), -1)

  def newWriter(ch: WritableByteChannel, charset: Charset): Writer =
    newWriter(ch, charset.newEncoder(), -1)

}
