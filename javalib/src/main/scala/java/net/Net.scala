package java.net

import java.io.FileDescriptor
import java.nio.channels.UnsupportedAddressTypeException
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix
import scala.scalanative.posix.netinet.{in, tcp}
import scala.scalanative.posix.sys.socketOps._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

private[java] trait Net {
  import Net._

  def POLLIN: Int
  def POLLOUT: Int

  def prepareSockaddrIn(
      isa: InetSocketAddress
  )(implicit
      zone: Zone
  ): (Ptr[posix.sys.socket.sockaddr], posix.sys.socket.socklen_t) = {
    val addr = isa.getAddress
    val port = isa.getPort
    addr match {
      case ipv4: Inet4Address =>
        val sa4 = alloc[in.sockaddr_in]()
        val sa4Len = sizeof[in.sockaddr_in].toUInt
        SocketHelpers.prepareSockaddrIn4(ipv4, port, sa4)
        (sa4.asInstanceOf[Ptr[posix.sys.socket.sockaddr]], sa4Len)
      case ipv6: Inet6Address =>
        val sa6 = alloc[in.sockaddr_in6]()
        val sa6Len = sizeof[in.sockaddr_in6].toUInt
        SocketHelpers.prepareSockaddrIn6(ipv6, port, sa6)
        (sa6.asInstanceOf[Ptr[posix.sys.socket.sockaddr]], sa6Len)
      case _ => throw new UnsupportedAddressTypeException()
    }
  }

  def socket(family: ProtocolFamily, stream: Boolean): FileDescriptor
  def checkAddress(sa: SocketAddress): InetSocketAddress

  def bind(fd: FileDescriptor, local: SocketAddress): Unit = {
    val isa = local match {
      case null =>
        new InetSocketAddress(SocketHelpers.getWildcardAddressForBind(), 0)
      case _ => checkAddress(local)
    }
    val bindRes = Zone.acquire { implicit z =>
      val (sa, len) = prepareSockaddrIn(isa)
      posix.sys.socket.bind(fd.fd, sa, len)
    }

    if (bindRes < 0) {
      throw new BindException(
        "Couldn't bind to an address: " + isa.getAddress.getHostAddress()
      )
    }
  }

  def connect(fd: FileDescriptor, remote: SocketAddress): Unit = {
    val isa = checkAddress(remote)

    val connectRes = Zone.acquire { implicit z =>
      val (sa, len) = prepareSockaddrIn(isa)
      posix.sys.socket.connect(fd.fd, sa, len)
    }
    if (connectRes < 0) {
      throw new ConnectException(
        "Couldn't connect to an address: " + isa.getAddress.getHostAddress()
      )
    }
  }

  def disconnect(fd: FileDescriptor): Unit = {
    val sa = stackalloc[posix.sys.socket.sockaddr]()
    val len = sizeof[posix.sys.socket.sockaddr].toUInt
    sa.sa_family = posix.sys.socket.AF_UNSPEC.toUShort
    posix.sys.socket.connect(fd.fd, sa, len)
  }

  def join(
      fd: FileDescriptor,
      family: ProtocolFamily,
      group: InetAddress,
      interf: NetworkInterface,
      source: InetAddress
  ): Unit =
    changeMembership(Membership.Join, fd, family, group, interf, source)

  def drop(
      fd: FileDescriptor,
      family: ProtocolFamily,
      group: InetAddress,
      interf: NetworkInterface,
      source: InetAddress
  ): Unit =
    changeMembership(Membership.Drop, fd, family, group, interf, source)

  protected def changeMembership(
      membership: Membership,
      fd: FileDescriptor,
      family: ProtocolFamily,
      group: InetAddress,
      interf: NetworkInterface,
      source: InetAddress
  ): Unit

  def close(fd: FileDescriptor): Unit
  def localAddress(fd: FileDescriptor, family: ProtocolFamily): SocketAddress
  def configureBlocking(fd: FileDescriptor, blocking: Boolean): Unit

  private def optionLevel(name: SocketOption[_]): CInt = name match {
    case StandardSocketOptions.IP_TOS => SocketHelpers.getIPPROTO()
    case _                            => posix.sys.socket.SOL_SOCKET
  }

  private def nativeOptionName(name: SocketOption[_]): CInt = name match {
    // case StandardSocketOptions.IP_MULTICAST_IF =>
    // case StandardSocketOptions.IP_MULTICAST_LOOP =>
    case StandardSocketOptions.IP_MULTICAST_TTL =>
      SocketHelpers.getMulticastTtlSocketOption()
    case StandardSocketOptions.IP_TOS =>
      SocketHelpers.getTrafficClassSocketOption()
    case StandardSocketOptions.SO_KEEPALIVE => posix.sys.socket.SO_KEEPALIVE
    case StandardSocketOptions.SO_LINGER    => posix.sys.socket.SO_LINGER
    // case SocketOptions.SO_LINGER           => posix.sys.socket.SO_LINGER
    case StandardSocketOptions.SO_RCVBUF    => posix.sys.socket.SO_RCVBUF
    case StandardSocketOptions.SO_SNDBUF    => posix.sys.socket.SO_SNDBUF
    case StandardSocketOptions.SO_REUSEADDR => posix.sys.socket.SO_REUSEADDR
    // case StandardSocketOptions.SO_REUSEPORT => posix.sys.socket.SO_REUSEPORT
    case StandardSocketOptions.SO_BROADCAST => posix.sys.socket.SO_BROADCAST
    case StandardSocketOptions.TCP_NODELAY  => tcp.TCP_NODELAY
  }

  def getSocketOption[T](
      fd: FileDescriptor,
      name: SocketOption[T]
  ): T = {
    val level = optionLevel(name)
    val optName = nativeOptionName(name)
    val optValue = stackalloc[CInt]().asInstanceOf[Ptr[Byte]]
    val optLen = stackalloc[posix.sys.socket.socklen_t]()
    !optLen = sizeof[CInt].toUInt

    if (posix.sys.socket.getsockopt(
          fd.fd,
          level,
          optName,
          optValue,
          optLen
        ) != 0) {
      throw new SocketException(
        s"Exception while getting socket option: ${name.name}, errno: ${lastError()}"
      )
    }

    name.`type` match {
      case JInteger =>
        // cast to T required for scala 2
        Integer.valueOf(!(optValue.asInstanceOf[Ptr[CInt]])).asInstanceOf[T]
      case JBoolean =>
        // cast to T required for scala 2
        Boolean.box(!(optValue.asInstanceOf[Ptr[CInt]]) != 0).asInstanceOf[T]
    }
  }

  protected def getMulticastInterfaceOption(
      fd: FileDescriptor
  ): InetSocketAddress = {
    val optValue = stackalloc[Ptr[posix.sys.socket.sockaddr]]()
    val optLen = stackalloc[posix.sys.socket.socklen_t]()
    !optLen = sizeof[posix.sys.socket.socklen_t].toUInt

    if (posix.sys.socket.getsockopt(
          fd.fd,
          in.IPPROTO_IP,
          in.IP_MULTICAST_IF,
          optValue.asInstanceOf[Ptr[Byte]],
          optLen
        ) != 0) {
      throw new SocketException(
        s"Exception while getting socket option with id: IP_MULTICAST_IF, errno: ${lastError()}"
      )
    }

    SocketHelpers.sockaddrStorageToInetSocketAddress((!optValue))
  }

  def setSocketOption[T](
      fd: FileDescriptor,
      name: SocketOption[T],
      value: T
  ): Unit = {
    val level = optionLevel(name)
    val optName = nativeOptionName(name)
    val optValue = stackalloc[CInt]()
    name.`type` match {
      case JInteger =>
        !optValue = value.asInstanceOf[Int]
      case JBoolean =>
        !optValue = if (value.asInstanceOf[Boolean]) 1 else 0
    }
    val optLen = sizeof[CInt].toUInt

    if (posix.sys.socket.setsockopt(
          fd.fd,
          level,
          optName,
          optValue.asInstanceOf[Ptr[Byte]],
          optLen
        ) != 0) {
      throw new SocketException(
        s"Exception while setting socket option: ${name.name}, errno: ${lastError()}"
      )
    }
  }

  @inline def lastError(): CInt =
    if (isWindows) scala.scalanative.windows.WinSocketApi.WSAGetLastError()
    else scala.scalanative.libc.errno.errno

}

private[java] object Net extends Net {

  private val JInteger = classOf[java.lang.Integer]
  private val JBoolean = classOf[java.lang.Boolean]

  private[net] sealed trait Membership
  private[net] object Membership {
    case object Join extends Membership
    case object Drop extends Membership
  }

  def getGaiHintsProtocolFamily(): ProtocolFamily = {
    if (SocketHelpers.getUseIPv4Stack()) StandardProtocolFamily.INET
    else StandardProtocolFamily.INET6
  }
  @inline private def netImpl: Net =
    if (isWindows) WindowsNet else UnixNet

  @inline def POLLIN = netImpl.POLLIN
  @inline def POLLOUT = netImpl.POLLOUT
  @inline override def socket(
      family: ProtocolFamily,
      stream: Boolean
  ): FileDescriptor =
    netImpl.socket(family, stream)
  @inline override def checkAddress(sa: SocketAddress): InetSocketAddress =
    netImpl.checkAddress(sa)
  @inline override def bind(fd: FileDescriptor, local: SocketAddress): Unit =
    netImpl.bind(fd, local)

  @inline override def changeMembership(
      membership: Membership,
      fd: FileDescriptor,
      family: ProtocolFamily,
      group: InetAddress,
      interf: NetworkInterface,
      source: InetAddress
  ): Unit =
    netImpl.changeMembership(membership, fd, family, group, interf, source)

  @inline override def close(fd: FileDescriptor): Unit =
    netImpl.close(fd)
  @inline override def localAddress(
      fd: FileDescriptor,
      family: ProtocolFamily
  ): SocketAddress =
    netImpl.localAddress(fd, family)
  @inline override def configureBlocking(
      fd: FileDescriptor,
      blocking: Boolean
  ): Unit =
    netImpl.configureBlocking(fd, blocking)
  @inline override def getSocketOption[T](
      fd: FileDescriptor,
      name: SocketOption[T]
  ): T =
    netImpl.getSocketOption(fd, name)
  @inline override def setSocketOption[T](
      fd: FileDescriptor,
      name: SocketOption[T],
      value: T
  ): Unit =
    netImpl.setSocketOption(fd, name, value)
}
