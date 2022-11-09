package java.net

import java.io.Closeable

class ServerSocket(
    private var port: Int,
    private var backlog: Int,
    private var bindAddr: InetAddress
) extends Closeable {

  private val impl = AbstractPlainSocketImpl()

  private var created = false
  private var bound = false
  private var closed = false

  if (bindAddr == null)
    bindAddr = SocketHelpers.getWildcardAddress()

  if (port >= 0)
    startup()

  def this() =
    this(-1, 50, null)

  def this(port: Int) =
    this(port, 50, null)

  def this(port: Int, backlog: Int) =
    this(port, backlog, null)

  private def create(): Unit = {
    // Sockets & ServerSockets always stream.
    impl.create(stream = true)
    created = true
  }

  private def startup(): Unit = {
    this.create()
    bind(new InetSocketAddress(bindAddr, port), backlog)
    bound = true
  }

  private def checkClosedAndCreate: Unit = {
    if (closed)
      throw new SocketException("Socket is closed")

    if (!created)
      this.create()
  }

  def accept: Socket = {
    val s = new Socket
    implAccept(s)
    s
  }

  protected[net] def implAccept(s: Socket): Unit = {
    impl.accept(s.impl)

    s.port = s.impl.port
    s.localPort = s.impl.localport
    s.addr = s.impl.address
    s.localAddr = this.bindAddr

    s.created = true
    s.bound = true
    s.connected = true
  }

  def bind(endpoint: SocketAddress): Unit = bind(endpoint, 50)

  def bind(endpoint: SocketAddress, backlog: Int): Unit = {
    if (endpoint != null && !endpoint.isInstanceOf[InetSocketAddress]) {
      throw new IllegalArgumentException(
        "Endpoint is of unsupported " +
          "SocketAddress subclass"
      )
    }

    val addr =
      if (endpoint == null ||
          endpoint.asInstanceOf[InetSocketAddress].getAddress == null)
        new InetSocketAddress(InetAddress.getLoopbackAddress(), 0)
      else {
        endpoint.asInstanceOf[InetSocketAddress]
      }

    checkClosedAndCreate

    this.bindAddr = addr.getAddress
    impl.bind(this.bindAddr, addr.getPort)
    this.port = impl.localport
    bound = true
    impl.listen(backlog)
  }

  // def getChannel: ServerSocketChannel
  def getInetAddress: InetAddress = bindAddr
  def getLocalSocketAddress: SocketAddress =
    new InetSocketAddress(bindAddr, port)
  def getLocalPort: Int = port

  def getReceiveBufferSize: Int = {
    checkClosedAndCreate
    impl.getOption(SocketOptions.SO_RCVBUF).asInstanceOf[Int]
  }

  def getReuseAddress: Boolean = {
    checkClosedAndCreate
    impl.getOption(SocketOptions.SO_REUSEADDR).asInstanceOf[Boolean]
  }

  def getSoTimeout: Int = {
    checkClosedAndCreate
    impl.getOption(SocketOptions.SO_TIMEOUT).asInstanceOf[Int]
  }

  def isBound: Boolean = bound
  def isClosed: Boolean = closed

  // def setPerformancePreferences(connectionTime: Int, latency: Int, bandwith: Int): Unit

  def setReceiveBufferSize(size: Int): Unit = {
    checkClosedAndCreate
    impl.setOption(SocketOptions.SO_RCVBUF, Integer.valueOf(size))
  }

  def setReuseAddress(on: Boolean): Unit = {
    checkClosedAndCreate
    impl.setOption(SocketOptions.SO_REUSEADDR, Boolean.box(on))
  }

  def setSoTimeout(timeout: Int): Unit = {
    checkClosedAndCreate
    impl.setOption(SocketOptions.SO_TIMEOUT, Integer.valueOf(timeout))
  }

  override def close(): Unit = {
    impl.close()
    closed = true
  }

  override def toString: String = {
    if (bound) {
      "ServerSocket[addr=" + bindAddr.toString + ",localport=" + port + "]"
    } else {
      "ServerSocket[unbound]"
    }
  }

}
