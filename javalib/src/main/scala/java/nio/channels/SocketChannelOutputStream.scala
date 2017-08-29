package java.nio.channels

import java.io.OutputStream
import java.nio.ByteBuffer

private[channels] class SocketChannelOutputStream(channel: SocketChannel)
    extends OutputStream {

  override def close(): Unit = channel.close()

  override def write(b: Array[Byte], off: Int, len: Int): Unit = {
    if (0 > off || 0 > len || len + off > b.length) {
      throw new IndexOutOfBoundsException
    }

    val buf = ByteBuffer.wrap(b, off, len)

    if (!channel.isBlocking) {
      throw new IllegalBlockingModeException
    }

    channel.write(buf)
  }

  override def write(b: Int): Unit = {
    if (!channel.isBlocking) {
      throw new IllegalBlockingModeException
    }

    val buf = ByteBuffer.allocate(1)
    buf.put(0, (b & 0xFF).toByte)

    channel.write(buf)
  }

}
