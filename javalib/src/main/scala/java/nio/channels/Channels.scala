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
  // def newChannel(in: InputStream): ReadableByteChannel
  // def newChannel(out: OutputStream): WritableByteChannel

  // def newReader(
  //     ch: ReadableByteChannel,
  //     dec: CharsetDecoder,
  //     minBufferCap: Int
  // ): Reader
  // def newReader(ch: ReadableByteChannel, csName: String): Reader
  // def newReader(ch: ReadableByteChannel, charset: Charset): Reader

  // def newWriter(
  //     ch: WritableByteChannel,
  //     enc: CharsetEncoder,
  //     minBufferCap: Int
  // ): Writer
  // def newWriter(ch: WritableByteChannel, csName: String): Writer
  // def newWriter(ch: WritableByteChannel, charset: Charset): Writer

}
