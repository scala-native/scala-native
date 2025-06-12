package java.net

import java.io.Closeable

class ServerSocket(
    private var port: Int,
    private var backlog: Int,
    private var bindAddr: InetAddress
) extends Closeable {

  private val impl = PlainSocketImpl()

  private var created = false
  private var bound = false
  private var closed = false

  if (port >= 0)
    startup()

  def this() =
    this(-1, 50, null)

  def this(port: Int) =
    this(port, 50, null)

  def this(port: Int, backlog: Int) =
    this(port, backlog, null)

  private def create(): Unit = {
    impl.create(stream = true) // Sockets & ServerSockets always stream.
    created = true
  }

  private def startup(): Unit = {
    this.create()
    bind(new InetSocketAddress(bindAddr, port), backlog)
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
    s.localAddr = SocketHelpers.fetchFdLocalAddress(s.impl.fd.fd)

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

    val ep = endpoint.asInstanceOf[InetSocketAddress]
    val (addr, port, choseWildcardAddress) = {
      val effectivePort = if (endpoint == null) 0 else ep.getPort
      if ((endpoint == null) || ep.getAddress == null) {
        (SocketHelpers.getWildcardAddressForBind(), effectivePort, true)
      } else {
        (ep.getAddress, effectivePort, false)
      }
    }

    checkClosedAndCreate

    /* When presented with a true IPv6 wildcard address, Scala JVM on
     * Linux & macOS will bind using an IPv6 address, so that the listen
     * will happen for both IPv6 & IPv4.
     *
     * On macOS "netstat -a | grep LISTEN"" will show a tcp46 socket in
     * use (example uses port 8090, your results may vary.):
     *   "tcp46      0      0  *.8090 *.* LISTEN"
     *
     * Linux shows:
     *    "tcp6       0      0 [::]:8090 [::]:* LISTEN"
     * Shows "tcp6" but also listening on the IPv4 address is implied.
     *
     * The tricky part is that they display (toString()) the local
     * InetAddress as IPv4 (0.0.0.0).
     *   "LocalSocketAddress: |0.0.0.0/0.0.0.0:8090|"
     *
     * This section, and SocketHelpers.getWildcardAddressForBind()
     * will need to be revisited for robust FreeBSD support.
     * See also notes in getWildcardAddressForBind().
     */

    val trickyAddr =
      if (addr != SocketHelpers.getWildcardAddress()) addr
      else SocketHelpers.getWildcardAddressForBind()

    impl.bind(trickyAddr, port)

    this.bindAddr = addr
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
