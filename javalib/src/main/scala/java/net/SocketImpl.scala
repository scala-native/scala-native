package java.net

import java.io.{FileDescriptor, InputStream, OutputStream}

abstract class SocketImpl extends SocketOptions {
  protected[net] var localport: Int
  protected[net] var port: Int
  protected[net] var address: InetAddress
  protected[net] var fd: FileDescriptor
  protected[net] var shutInput = false
  protected[net] var shutOutput = false

  protected[net] def accept(s: SocketImpl): Unit
  protected[net] def available(): Int
  protected[net] def bind(host: InetAddress, port: Int): Unit
  protected[net] def close(): Unit
  protected[net] def connect(address: InetAddress, port: Int): Unit
  protected[net] def connect(address: SocketAddress, timeout: Int): Unit
  protected[net] def connect(host: String, port: Int): Unit
  protected[net] def create(stream: Boolean): Unit
  protected[net] def getFileDescriptor: FileDescriptor
  protected[net] def getInetAddress: InetAddress
  protected[net] def getInputStream: InputStream
  protected[net] def getLocalPort: Int = localport
  protected[net] def getOutputStream: OutputStream
  protected[net] def getPort: Int = port
  protected[net] def listen(backlog: Int): Unit
  // protected[net] def sendUrgentData(data: Int): Unit
  // protected[net] def setPerformancePreferences(connectionTime: Int, latency: Int, bandwith: Int): Unit
  protected[net] def shutdownInput(): Unit
  protected[net] def shutdownOutput(): Unit
  protected[net] def shutdownBoth(): Unit
  // protected[net] def supportsUrgentData: Boolean

  override def toString: String =
    s"PlainSocketImpl[addr=$address,port=$port,localport=$localport]"
}
