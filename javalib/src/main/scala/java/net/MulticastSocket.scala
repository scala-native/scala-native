package java.net

import java.util.Objects
import java.{util => ju}

class MulticastSocket(bindaddr: SocketAddress)
    extends DatagramSocket(
      AbstractPlainDatagramSocketImpl(),
      0,
      SocketHelpers.getWildcardAddress(),
      false
    ) {

  private var interfaceAddress: InetAddress = null

  try {
    setReuseAddress(true)
    if (bindaddr != null) bind(bindaddr)
  } catch {
    case e: Exception =>
      close()
      throw e
  }

  def this() = this(MulticastSocket.wildcard(0))

  def this(port: Int) = this(MulticastSocket.wildcard(port))

  private def checkClosed(): Unit =
    if (isClosed()) throw new SocketException("Socket is closed")

  def setTimeToLive(ttl: Int): Unit = {
    if (ttl < 0 || ttl > 255)
      throw new IllegalArgumentException(s"Invalid TTL/hop value: $ttl")
    checkClosed()
    impl.setTimeToLive(ttl)
  }

  def getTimeToLive(): Int = {
    checkClosed()
    impl.getTimeToLive()
  }

  @deprecated("Use joinGroup(SocketAddress, NetworkInterface)", "14")
  def joinGroup(mcastaddr: InetAddress): Unit = {
    Objects.requireNonNull(mcastaddr)
    checkClosed()
    impl.join(mcastaddr)
  }

  @deprecated("Use leaveGroup(SocketAddress, NetworkInterface)", "14")
  def leaveGroup(mcastaddr: InetAddress): Unit = {
    Objects.requireNonNull(mcastaddr)
    checkClosed()
    impl.leave(mcastaddr)
  }

  @deprecated("Use setNetworkInterface(NetworkInterface)", "14")
  def setInterface(inf: InetAddress): Unit = {
    if (inf == null) throw new SocketException("Invalid value 'null'")
    checkClosed()
    val netIf = NetworkInterface.getByInetAddress(inf)
    if (netIf == null)
      throw new SocketException(
        s"No network interface found with address ${inf.getHostAddress()}"
      )
    impl.setOption(SocketOptions.IP_MULTICAST_IF2, netIf)
    interfaceAddress = inf
  }

  @deprecated("Use getNetworkInterface()", "14")
  def getInterface(): InetAddress = {
    checkClosed()
    val netIf = impl
      .getOption(SocketOptions.IP_MULTICAST_IF2)
      .asInstanceOf[NetworkInterface]
    if (netIf == null) MulticastSocket.anyLocalAddress
    else if (interfaceAddress != null &&
        NetworkInterface.getByInetAddress(interfaceAddress) == netIf)
      interfaceAddress
    else {
      val addrs = netIf.getInetAddresses()
      if (addrs.hasMoreElements()) addrs.nextElement()
      else MulticastSocket.anyLocalAddress
    }
  }

  def setNetworkInterface(netIf: NetworkInterface): Unit = {
    setOption(StandardSocketOptions.IP_MULTICAST_IF, netIf)
    interfaceAddress = null
  }

  def getNetworkInterface(): NetworkInterface = {
    val netIf = getOption(StandardSocketOptions.IP_MULTICAST_IF)
    if (netIf != null) netIf
    else MulticastSocket.placeholderInterface()
  }

  @deprecated("Use setOption(IP_MULTICAST_LOOP, Boolean)", "14")
  def setLoopbackMode(disable: Boolean): Unit = {
    checkClosed()
    impl.setOption(SocketOptions.IP_MULTICAST_LOOP, Boolean.box(!disable))
  }

  @deprecated("Use getOption(IP_MULTICAST_LOOP)", "14")
  def getLoopbackMode(): Boolean = {
    checkClosed()
    !impl.getOption(SocketOptions.IP_MULTICAST_LOOP).asInstanceOf[Boolean]
  }

  @deprecated("Use setTimeToLive(Int) and send(DatagramPacket)", "1.4")
  def send(p: DatagramPacket, ttl: Byte): Unit = synchronized {
    checkClosed()
    val oldTtl = getTimeToLive()
    setTimeToLive(ttl & 0xff)
    try send(p)
    finally setTimeToLive(oldTtl)
  }
}

private object MulticastSocket {
  private def wildcard(port: Int): SocketAddress =
    new InetSocketAddress(SocketHelpers.getWildcardAddressForBind(), port)

  private def anyLocalAddress: InetAddress =
    InetAddress.getByAddress(new Array[Byte](4))

  private def placeholderInterface(): NetworkInterface =
    new NetworkInterface("0.0.0.0") {
      override def getIndex(): Int = 0
      override def getInetAddresses(): ju.Enumeration[InetAddress] =
        ju.Collections.enumeration(
          ju.Collections.singletonList(anyLocalAddress)
        )
      override def getInterfaceAddresses(): ju.List[InterfaceAddress] =
        ju.Collections.emptyList()
      override def inetAddresses(): ju.stream.Stream[InetAddress] =
        ju.stream.Stream.of(anyLocalAddress)
    }
}
