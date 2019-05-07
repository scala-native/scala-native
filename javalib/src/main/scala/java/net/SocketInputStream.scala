package java.net

import java.io.InputStream

// Ported from Apache Harmony
private[net] class SocketInputStream(socket: PlainSocketImpl)
    extends InputStream {

  override def close(): Unit = socket.close

  override def available: Int = socket.available

  override def read(): Int = {
    val buffer = new Array[Byte](1)
    socket.read(buffer, 0, 1) match {
      case -1 => -1
      case _  => buffer(0) & 0xFF // Convert to Int with _no_ sign extension.
    }
  }

  override def read(buffer: Array[Byte]) = read(buffer, 0, buffer.length)

  override def read(buffer: Array[Byte], offset: Int, count: Int) = {
    if (buffer == null) throw new NullPointerException("Buffer is null")

    if (count == 0) 0

    if (offset < 0 || offset >= buffer.length)
      throw new ArrayIndexOutOfBoundsException(
        "Offset out of bounds: " + offset)

    if (count < 0 || offset + count > buffer.length)
      throw new ArrayIndexOutOfBoundsException(
        "Reading would result in buffer overflow")

    socket.read(buffer, offset, count)
  }

}
