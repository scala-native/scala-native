package java.net

import java.io.{InputStream, OutputStream, IOException}

class Socket protected(private val impl: SocketImpl) {

  private var isCreated = false
  private var isBound = false
  private var isConnected = false
  private var isClosed = false
  private var isInputShutdown = false
  private var isOutputShutdown = false
  private var streaming = true // TODO

  private def startup(dstAddr: InetAddress, dstPort: Int,
                      localAddr: InetAddress, localPort: Int) = {
    if(localPort < 0 || localPort > 65535)
      throw new IllegalArgumentException("Localport must be between 0 and 65535")

    val addr = if(localAddr == null) InetAddress.getLoopbackAddress else localAddr

    impl.create(streaming)
    val isCreated = true
    try {
      isBound = true
      impl.connect(dstAddr, dstPort)
      isConnected = true
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
  
  def close: Unit = impl.close

  override def toString: String = {
    if(isConnected)
      impl.toString
    else
      "Socket[unconnected]"
  }

  def getOutputStream: OutputStream = impl.getOutputStream

  def getInputStream: InputStream = impl.getInputStream

}
