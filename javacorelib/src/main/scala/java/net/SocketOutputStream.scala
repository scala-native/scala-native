package java.net

import java.io.OutputStream

// Ported from Apache Harmony
private[net] class SocketOutputStream(socket: PlainSocketImpl)
    extends OutputStream {

  override def close(): Unit = {
    socket.close()
  }

  override def write(b: Array[Byte]) = {
    socket.write(b, 0, b.length)
  }

  override def write(b: Array[Byte], off: Int, len: Int) = {
    if (b == null)
      throw new NullPointerException("Buffer parameter is null")

    if (off < 0 || off > b.length || len < 0 || len > b.length - off)
      throw new ArrayIndexOutOfBoundsException("Invalid length or offset")

    socket.write(b, off, len)
  }

  override def write(b: Int) = {
    socket.write(Array[Byte](b.toByte), 0, 1)
  }
}
