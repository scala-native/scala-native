package java.net

import java.io.{InputStream, OutputStream, IOException, Closeable}

class Socket protected(private val impl: SocketImpl) extends Closeable {

  private var created = false
  private var bound = false
  private var connected = false
  private var closed = false
  private var inputShutdown = false
  private var outputShutdown = false
  private var streaming = true // TODO

  private def startup(dstAddr: InetAddress, dstPort: Int,
                      localAddr: InetAddress, localPort: Int) = {
    if(localPort < 0 || localPort > 65535)
      throw new IllegalArgumentException("Localport must be between 0 and 65535")

    val addr = if(localAddr == null) InetAddress.getLoopbackAddress else localAddr

    impl.create(streaming)
    val created = true
    try {
      bound = true
      impl.connect(dstAddr, dstPort)
      connected = true
    } catch {
      case e: IOException => {
        impl.close
        throw e
      }
    }
  }

  def this(address: InetAddress, port: Int) = {
    this(new PlainSocketImpl())
    startup(address, port, null, 0)  
  }

  def this(address: InetAddress, port: Int, localAddr: InetAddress, localPort: Int) = {
    this(new PlainSocketImpl())
    startup(address, port, localAddr, localPort)
  }

  def this(host: String, port: Int) = {
    this(new PlainSocketImpl())
    val addr = InetAddress.getByName(host)
    startup(addr, port, null, 0)
  }

  def this(host: String, port: Int, localAddr: InetAddress, localPort: Int) = {
    this(new PlainSocketImpl())
    val addr = InetAddress.getByName(host)
    startup(addr, port, localAddr, localPort)
  }

  // def this(host: InetAddress, port: Int, stream: Boolean)
  // def this(proxy: Proxy)
  // def this(host: String, port: Int, stream: Boolean)

  // def bind(bindpoint: SocketAddress): Unit
  // def connect(endpoint: SocketAddress): Unit
  // def connect(endpoint: SocketAddress, timeout: Int): Unit

  // def getChannel: SocketChannel

  // def getInetAddress: InetAddress
  // def getKeepAlive: Boolean
  // def getLocalAddress: Boolean
  // def getLocalPort: Int
  // def localSocketAddress: SocketAddress
  // def getOOBInline: Boolean
  // def getPort: Int
  // def getReceiveBufferSize: Int
  // def getRemoteSocketAddress: SocketAddress
  // def getReuseAddress: Boolean
  // def getSendBufferSize: Int
  // def getSoLinger: Int
  // def getSoTimeout: Int
  // def getTcpNoDelay: Boolean
  // def getTrafficClass: Int

  def isBound: Boolean = bound
  def isClosed: Boolean = closed
  def isConnected: Boolean = connected
  def isInputShutdown: Boolean = inputShutdown
  def isOutputShutdown: Boolean = outputShutdown

  // def sendUrgentData(data: Int): Unit
  // def setKeepAlive(on: Boolean): Unit
  // def setOOBInline(on: Boolean): Unit
  // def setPerformancePreferences(connectionTime: Int, latency: Int, bandwith: Int): Unit
  // def setReceiveBufferSize(size: Int): Unit
  // def setReuseAddress(on: Boolean): Unit
  // def setSendBufferSize(size: Int): Unit
  // def setSoLinger(on: Boolean, linger: Int): Unit
  // def setSoTimeout(timeout: Int): Unit
  // def setTcpNoDelay(on: Boolean): Unit
  // def setTrafficClass(tc: Int): Unit

  def shutdownInput: Unit = {
    impl.shutdownInput
    inputShutdown = true
  }

  def shutdownOutput: Unit = {
    impl.shutdownOutput
    outputShutdown = true
  }

  
  override def close: Unit = impl.close

  override def toString: String = {
    if(connected)
      impl.toString
    else
      "Socket[unconnected]"
  }

  def getOutputStream: OutputStream = impl.getOutputStream

  def getInputStream: InputStream = impl.getInputStream

}

object Socket {
  // def setSocketImplFactory(fac: SocketImplFactory): Unit
}
