package java.nio.channels

import java.io.InputStream
import java.nio.ByteBuffer

// Ported from Apache Harmony
private[channels] class SocketChannelInputStream(channel: SocketChannel)
    extends InputStream {

  override def close(): Unit = channel.close

  override def read(): Int = {
    if (!channel.isBlocking) {
      throw new IllegalBlockingModeException
    }

    val buf    = ByteBuffer.allocate(1)
    val result = channel.read(buf)

    if (result == -1) {
      result
    } else {
      buf.get & 0xFF
    }
  }

  override def read(b: Array[Byte], off: Int, len: Int): Int = {
    if (0 > off || 0 > len || len + off > b.length) {
      throw new IndexOutOfBoundsException
    }

    if (!channel.isBlocking) {
      throw new IllegalBlockingModeException
    }

    val buf = ByteBuffer.wrap(b, off, len)
    channel.read(buf)
  }

}
