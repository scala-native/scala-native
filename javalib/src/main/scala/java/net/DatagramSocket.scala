package java.net

import java.io.Closeable
import java.nio.channels.DatagramChannel

class DatagramSocket protected (
    private[net] var impl: DatagramSocketImpl,
    private[net] var localPort: Int,
    private[net] var localAddr: InetAddress,
    shouldStartup: Boolean
) extends Closeable {
  import DatagramSocket._

  private[net] var connectState: Int = ST_NOT_CONNECTED
  private[net] var connectedAddress: InetAddress = null
  private[net] var connectedPort: Int = -1

  private[net] var created = false
  private[net] var bound = false
  private[net] var closed = false

  private var explicitFilter = false
  private var bytesLeftToFilter = 0

  if (shouldStartup) {
    this.startup()
  }

  def this() =
    this(
      AbstractPlainDatagramSocketImpl(),
      0,
      SocketHelpers.getWildcardAddress(),
      true
    )
  protected[net] def this(impl: DatagramSocketImpl) =
    this(impl, 0, null, false)

  def this(bindaddr: SocketAddress) = {
    this(
      AbstractPlainDatagramSocketImpl(),
      0,
      SocketHelpers.getWildcardAddress(),
      false
    )
    if (bindaddr != null) {
      this.bind(bindaddr)
    }
  }

  def this(port: Int) =
    this(
      AbstractPlainDatagramSocketImpl(),
      port,
      SocketHelpers.getWildcardAddress(),
      true
    )

  def this(port: Int, laddr: InetAddress) =
    this(AbstractPlainDatagramSocketImpl(), port, laddr, true)

  private def create(): Unit = {
    impl.create()
    created = true
  }

  private def startup() = {
    this.create()
    try {
      this.bind(new InetSocketAddress(localAddr, localPort))
      bound = true
    } catch {
      case e: Exception =>
        this.close()
        throw e
    }
  }

  private def checkClosedAndCreate(): Unit = {
    if (closed)
      throw new SocketException("Socket is closed")

    if (!created)
      this.create()
  }

  def bind(addr: SocketAddress): Unit = {
    if (isBound()) {
      throw new SocketException("already bound")
    }

    if (addr != null && !addr.isInstanceOf[InetSocketAddress]) {
      throw new IllegalArgumentException(
        "Endpoint is of unsupported SocketAddress subclass"
      )
    }

    val inetAddr =
      if (addr == null ||
          addr.asInstanceOf[InetSocketAddress].getAddress == null)
        new InetSocketAddress(InetAddress.getLoopbackAddress(), 0)
      else {
        addr.asInstanceOf[InetSocketAddress]
      }

    checkClosedAndCreate()

    impl.bind(inetAddr.getPort, inetAddr.getAddress)
    this.localAddr = inetAddr.getAddress
    this.localPort = impl.localport
    bound = true
  }

  private[net] def checkAddress(addr: InetAddress, op: String) = addr match {
    case null                              =>
    case _: Inet4Address | _: Inet6Address =>
    case _ => new IllegalArgumentException(op + ": invalid address type")
  }

  def connect(address: InetAddress, port: Int): Unit = {
    checkAddress(address, "connect")
    impl.connect(address, port)

    connectState = ST_CONNECTED
    connectedAddress = address
    connectedPort = port

    // Do we need to filter some packets?
    val avail = impl.dataAvailable()
    if (avail == -1) {
      throw new SocketException();
    }
    explicitFilter = avail > 0;
    if (explicitFilter) {
      bytesLeftToFilter = getReceiveBufferSize();
    }
  }

  def connect(address: SocketAddress): Unit = address match {
    case iaddr: InetSocketAddress =>
      connect(iaddr.getAddress, iaddr.getPort)
    case _ =>
      throw new IllegalArgumentException(
        "Invalid address argument to connect - " +
          "either of unsupported SocketAddress subclass or null"
      )
  }

  def disconnect(): Unit = {
    if (!isClosed()) {
      impl.disconnect()
      connectState = ST_NOT_CONNECTED
      connectedAddress = null
      connectedPort = -1
      explicitFilter = false
      bytesLeftToFilter = 0
    }
  }

  def isBound(): Boolean = bound

  def isConnected(): Boolean = connectState != ST_NOT_CONNECTED

  def getInetAddress(): InetAddress = connectedAddress

  def getPort(): Int = connectedPort

  def getRemoteSocketAddress(): SocketAddress = {
    if (!isConnected()) null
    else new InetSocketAddress(connectedAddress, connectedPort)
  }

  def getLocalSocketAddress(): SocketAddress = {
    if (isClosed()) null
    else if (!bound) null
    else new InetSocketAddress(localAddr, localPort)
  }

  def send(p: DatagramPacket): Unit = {
    if (isClosed())
      throw new SocketException("Socket is closed")

    checkAddress(p.getAddress(), "send")

    if (connectState == ST_NOT_CONNECTED) {
      // TODO security manager ?
    } else {
      val packetAddress = p.getAddress()
      if (packetAddress == null) {
        p.setAddress(connectedAddress);
        p.setPort(connectedPort);
      } else if (packetAddress != connectedAddress ||
          p.getPort() != connectedPort) {
        throw new IllegalArgumentException(
          "connected address and packet address differ"
        )
      }
    }

    // Check whether the socket is bound
    if (!isBound())
      this.bind(new InetSocketAddress(0))
    // call the  method to send
    impl.send(p)
  }

  def receive(p: DatagramPacket): Unit = {
    if (!isBound())
      bind(new InetSocketAddress(0))

    var tmp: DatagramPacket = null;
    if (explicitFilter) {
      // "explicitFilter" may be set when
      // a socket is bound but not connected for a period of time,
      // packets from other sources might be queued on socket.
      var stop = false
      while (!stop) {
        // peek at the packet to see who it is from.
        val peekPacket = new DatagramPacket(new Array[Byte](1), 1)
        val peekPort = impl.peekData(peekPacket)
        val peekAddress = peekPacket.getAddress()

        if ((!(connectedAddress == peekAddress)) || (connectedPort != peekPort)) {
          // throw the packet away and silently continue
          tmp = new DatagramPacket(new Array[Byte](1024), 1024)
          impl.receive(tmp)
          if (explicitFilter) {
            if (checkFiltering(tmp)) {
              stop = true
            }
          }
        } else {
          stop = true
        }
      }
    }

    impl.receive(p)
  }

  private def checkFiltering(p: DatagramPacket): Boolean = {
    bytesLeftToFilter = bytesLeftToFilter - p.getLength()
    val done = bytesLeftToFilter <= 0 || impl.dataAvailable() <= 0
    if (done) explicitFilter = false
    done
  }

  def getLocalAddress(): InetAddress = localAddr

  def getLocalPort(): Int = localPort

  def setSoTimeout(timeout: Int): Unit = {
    checkClosedAndCreate()
    impl.setOption(SocketOptions.SO_TIMEOUT, Integer.valueOf(timeout))
  }

  def getSoTimeout(): Int = {
    checkClosedAndCreate()
    impl.getOption(SocketOptions.SO_TIMEOUT).asInstanceOf[Int]
  }

  def setSendBufferSize(size: Int): Unit = {
    checkClosedAndCreate()
    impl.setOption(SocketOptions.SO_SNDBUF, Integer.valueOf(size))
  }

  def getSendBufferSize(): Int = {
    checkClosedAndCreate()
    impl.getOption(SocketOptions.SO_SNDBUF).asInstanceOf[Int]
  }

  def setReceiveBufferSize(size: Int): Unit = {
    checkClosedAndCreate()
    impl.setOption(SocketOptions.SO_RCVBUF, Integer.valueOf(size))
  }

  def getReceiveBufferSize(): Int = {
    checkClosedAndCreate()
    impl.getOption(SocketOptions.SO_RCVBUF).asInstanceOf[Int]
  }

  def setReuseAddress(on: Boolean): Unit = {
    checkClosedAndCreate()
    impl.setOption(SocketOptions.SO_REUSEADDR, Boolean.box(on))
  }

  def getReuseAddress(): Boolean = {
    checkClosedAndCreate()
    impl.getOption(SocketOptions.SO_REUSEADDR).asInstanceOf[Boolean]
  }

  def setBroadcast(on: Boolean): Unit = {
    checkClosedAndCreate()
    impl.setOption(SocketOptions.SO_BROADCAST, Boolean.box(on))
  }

  def getBroadcast(): Boolean = {
    checkClosedAndCreate()
    impl.getOption(SocketOptions.SO_BROADCAST).asInstanceOf[Boolean]
  }

  def setTrafficClass(tc: Int): Unit = {
    checkClosedAndCreate()
    impl.setOption(SocketOptions.IP_TOS, Integer.valueOf(tc))
  }

  def getTrafficClass(): Int = {
    checkClosedAndCreate()
    impl.getOption(SocketOptions.IP_TOS).asInstanceOf[Int]
  }

  // Since: Java 17
  def joinGroup(
      mcastaddr: SocketAddress,
      netIf: NetworkInterface
  ): Unit = impl.joinGroup(mcastaddr, netIf)

  // Since: Java 17
  def leaveGroup(
      mcastaddr: SocketAddress,
      netIf: NetworkInterface
  ): Unit = impl.leaveGroup(mcastaddr, netIf)

  override def close(): Unit = {
    localAddr = null
    localPort = -1
    closed = true
    impl.close()
  }

  def isClosed(): Boolean = closed

  def getChannel(): DatagramChannel = null

}

object DatagramSocket {
  private[net] val ST_NOT_CONNECTED = 0
  private[net] val ST_CONNECTED = 1
  private[net] val ST_CONNECTED_NO_IMPL = 2
}
