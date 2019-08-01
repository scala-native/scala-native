package java.net

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.libc._
import scala.scalanative.runtime.ByteArray
import java.io.{FileDescriptor, IOException, OutputStream, InputStream}

private[net] class PlainSocketImpl extends SocketImpl {

  protected[net] var fd                   = new FileDescriptor
  protected[net] var localport            = 0
  protected[net] var address: InetAddress = null
  protected[net] var port                 = 0

  private var timeout   = 0
  private var listening = false

  override def getInetAddress: InetAddress       = address
  override def getFileDescriptor: FileDescriptor = fd

  override def create(streaming: Boolean): Unit = {
    throw new IOException("Not implemented")
  }

  override def bind(addr: InetAddress, port: Int): Unit = {
    throw new IOException("Not implemented")
  }

  override def listen(backlog: Int): Unit = {
    throw new IOException("Not implemented")

  }

  override def accept(s: SocketImpl): Unit = {
    throw new IOException("Not implemented")
  }

  override def connect(host: String, port: Int): Unit = {
    throw new IOException("Not implemented")
  }

  override def connect(address: InetAddress, port: Int): Unit = {
    throw new IOException("Not implemented")
  }

  override def connect(address: SocketAddress, timeout: Int): Unit = {
    throw new IOException("Not implemented")
  }

  override def close(): Unit = {
    throw new IOException("Not implemented")
  }

  override def getOutputStream: OutputStream = {
    throw new IOException("Not implemented")
    new SocketOutputStream(this)
  }

  override def getInputStream: InputStream = {
    throw new IOException("Not implemented")
    new SocketInputStream(this)
  }

  override def shutdownOutput(): Unit = {
    throw new IOException("Not implemented")
  }

  override def shutdownInput(): Unit = {
    throw new IOException("Not implemented")
  }

  def write(buffer: Array[Byte], offset: Int, count: Int): Int = {
    throw new IOException("Not implemented")
  }

  def read(buffer: Array[Byte], offset: Int, count: Int): Int = {
    throw new IOException("Not implemented")
  }

  override def available: Int = {
    throw new IOException("Not implemented")
  }

  override def getOption(optID: Int): Object = {
    throw new IOException("Not implemented")
  }

  override def setOption(optID: Int, value: Object): Unit = {
    throw new IOException("Not implemented")
  }

}
