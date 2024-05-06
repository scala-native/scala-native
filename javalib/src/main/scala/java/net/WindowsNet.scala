package java.net

import java.net.ipOps._
import java.io.{FileDescriptor, IOException}
import java.nio.channels.{
  UnresolvedAddressException,
  UnsupportedAddressTypeException
}
import scala.scalanative.posix
import scala.scalanative.posix.arpa.inet
import scala.scalanative.posix.netdb.addrinfo
import scala.scalanative.posix.netinet.in
import scala.scalanative.posix.netinet.tcp
import scala.scalanative.posix.netinet.inOps._
import scala.scalanative.posix.sys.{socket => unixsocket}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows._

object WindowsNet extends Net {
  import WinSocketApi._
  import WinSocketApiExt._
  import WinSocketApiOps._

  private val jInteger = classOf[java.lang.Integer]
  private val jBoolean = classOf[java.lang.Boolean]

  WinSocketApiOps.init()

  @inline override def POLLIN: Int = WinSocketApiExt.POLLIN
  @inline override def POLLOUT: Int = WinSocketApiExt.POLLOUT

  override def socket(
      family: ProtocolFamily,
      stream: Boolean
  ): FileDescriptor = {
    val addressFamily = family match {
      case StandardProtocolFamily.INET => unixsocket.AF_INET
      // case StandardProtocolFamily.INET6 => unixSocket.AF_INET6
      case _ =>
        throw new UnsupportedOperationException(
          s"Protocol family not supported: ${family.name()}"
        )
    }
    val socketType =
      if (stream) unixsocket.SOCK_STREAM else unixsocket.SOCK_DGRAM

    val socket = WSASocketW(
      addressFamily = addressFamily,
      socketType = socketType,
      protocol = 0, // chosen by provider
      protocolInfo = null,
      group = 0.toUInt,
      flags = WSA_FLAG_OVERLAPPED
    )
    if (socket == InvalidSocket) {
      throw new IOException(s"Couldn't create socket: ${WSAGetLastError()}")
    }

    new FileDescriptor(FileDescriptor.FileHandle(socket), readOnly = false)
  }

  override def checkAddress(sa: SocketAddress): InetSocketAddress = sa match {
    case null =>
      throw new NullPointerException()
    case isa: InetSocketAddress if isa.isUnresolved =>
      throw new UnresolvedAddressException()
    case isa: InetSocketAddress if isa.getAddress.isInstanceOf[Inet4Address] =>
      isa
    case _ =>
      throw new UnsupportedAddressTypeException()
  }

  override protected def changeMembership(
      membership: Net.Membership,
      fd: FileDescriptor,
      family: ProtocolFamily,
      group: InetAddress,
      interf: NetworkInterface,
      source: InetAddress
  ): Unit = {
    require(group.isMulticastAddress(), "Address is not a multicast address")
    if (source != null) {
      require(
        !source.isAnyLocalAddress(),
        "Source address is a wildcard address"
      )
      require(
        !source.isMulticastAddress(),
        "Source address is multicast address"
      )
      require(
        source.getClass == group.getClass,
        "Source address is different type to group"
      )
    }

    val (name, optName) = membership match {
      case Net.Membership.Join => ("IP_ADD_MEMBERSHIP", ip.IP_ADD_MEMBERSHIP)
      case Net.Membership.Drop => ("IP_DROP_MEMBERSHIP", ip.IP_DROP_MEMBERSHIP)
    }

    val (optValue, optLen) = (family, group) match {
      case (StandardProtocolFamily.INET, group: Inet4Address) =>
        val mName = stackalloc[ip.ip_mreq]()
        if (interf != null) {
          val ifAddrs = interf.getInetAddresses()
          if (!ifAddrs.hasMoreElements()) {
            throw new SocketException(
              s"bad argument for $name: No IP addresses bound to interface"
            )
          }
          val addrPtr = group.getAddress().at(0).asInstanceOf[Ptr[in.in_addr_t]]
          val ifAddrPtr =
            ifAddrs
              .nextElement()
              .getAddress()
              .at(0)
              .asInstanceOf[Ptr[in.in_addr_t]]
          mName.imr_multiaddr.s_addr = inet.htonl(!addrPtr)
          mName.imr_address.s_addr = inet.htonl(!ifAddrPtr)
        } else {
          val addrPtr = group.getAddress().at(0).asInstanceOf[Ptr[in.in_addr_t]]
          val ifAddrPtr = getMulticastInterfaceOption(fd).getAddress
            .getAddress()
            .at(0)
            .asInstanceOf[Ptr[in.in_addr_t]]
          mName.imr_multiaddr.s_addr = inet.htonl(!addrPtr)
          mName.imr_address.s_addr = inet.htonl(!ifAddrPtr)
        }
        (mName.asInstanceOf[Ptr[Byte]], sizeof[ip.ip_mreq].toUInt)
      // case (StandardProtocolFamily.INET6, group: Inet6Address) =>
      case _ =>
        throw new IllegalArgumentException("Address type not supported")
    }

    if (posix.sys.socket.setsockopt(
          fd.fd,
          in.IPPROTO_IP,
          optName,
          optValue,
          optLen
        ) != 0) {
      throw new SocketException(
        s"Exception while setting socket option $name, errno: ${WSAGetLastError()}"
      )
    }
  }

  override def close(fd: FileDescriptor): Unit = closeSocket(fd.handle)

  override def localAddress(
      fd: FileDescriptor,
      family: ProtocolFamily
  ): SocketAddress = {
    val len = stackalloc[unixsocket.socklen_t]()
    val saddr = family match {
      case StandardProtocolFamily.INET =>
        !len = sizeof[in.sockaddr_in].toUInt
        stackalloc[in.sockaddr_in]().asInstanceOf[Ptr[unixsocket.sockaddr]]
//      case StandardProtocolFamily.INET6 =>
//        !len = sizeof[in.sockaddr_in6].toUInt
//        stackalloc[in.sockaddr_in6]().asInstanceOf[Ptr[unixsocket.sockaddr]]
      case _ =>
        throw new UnsupportedOperationException(
          s"Protocol family not supported: ${family.name()}"
        )
    }

    if (unixsocket.getsockname(fd.fd, saddr, len) == -1) {
      throw new IOException("TODO")
    }

    // Not most efficient but factorises code
    SocketHelpers.sockaddrStorageToInetSocketAddress(saddr)
  }

  override def configureBlocking(
      fd: FileDescriptor,
      blocking: Boolean
  ): Unit = {
    val mode = stackalloc[Int]()
    !mode = if (blocking) 0 else 1
    if (ioctlSocket(fd.handle, FIONBIO, mode) != 0) {
      throw new SocketException(
        s"Failed to set socket ${if (!blocking) "non-" else ""}blocking"
      )
    }
  }

  private def optionLevel(name: SocketOption[_]): CInt = name match {
    case StandardSocketOptions.IP_TOS => SocketHelpers.getIPPROTO()
    case _                            => unixsocket.SOL_SOCKET
  }

  private def nativeOptionName(name: SocketOption[_]): CInt = name match {
    // case StandardSocketOptions.IP_MULTICAST_IF =>
    // case StandardSocketOptions.IP_MULTICAST_LOOP =>
    case StandardSocketOptions.IP_MULTICAST_TTL =>
      SocketHelpers.getMulticastTtlSocketOption()
    case StandardSocketOptions.IP_TOS =>
      SocketHelpers.getTrafficClassSocketOption()
    case StandardSocketOptions.SO_KEEPALIVE => unixsocket.SO_KEEPALIVE
    case StandardSocketOptions.SO_LINGER    => unixsocket.SO_LINGER
    //      case SocketOptions.SO_LINGER    => socket.SO_LINGER
    case StandardSocketOptions.SO_RCVBUF    => unixsocket.SO_RCVBUF
    case StandardSocketOptions.SO_SNDBUF    => unixsocket.SO_SNDBUF
    case StandardSocketOptions.SO_REUSEADDR => unixsocket.SO_REUSEADDR
    // case StandardSocketOptions.SO_REUSEPORT => unixsocket.SO_REUSEPORT
    case StandardSocketOptions.SO_BROADCAST => unixsocket.SO_BROADCAST
    case StandardSocketOptions.TCP_NODELAY  => tcp.TCP_NODELAY
  }

  override def getSocketOption[T](
      fd: FileDescriptor,
      name: SocketOption[T]
  ): T = {
    val level = optionLevel(name)
    val optName = nativeOptionName(name)
    val optValue = stackalloc[CInt]().asInstanceOf[Ptr[Byte]]
    val optLen = stackalloc[unixsocket.socklen_t]()
    !optLen = sizeof[CInt].toUInt

    if (posix.sys.socket.getsockopt(
          fd.fd,
          level,
          optName,
          optValue,
          optLen
        ) != 0) {
      throw new SocketException(
        s"Exception while getting socket option: , errno: ${WSAGetLastError()}"
      )
    }

    name.`type` match {
      case `jInteger` =>
        Integer.valueOf(!(optValue.asInstanceOf[Ptr[CInt]]))
      case `jBoolean` =>
        Boolean.box(!(optValue.asInstanceOf[Ptr[CInt]]) != 0)
    }
  }

  private def getMulticastInterfaceOption(
      fd: FileDescriptor
  ): InetSocketAddress = {
    val optValue = stackalloc[Ptr[in.sockaddr_in]]()
    val optLen = stackalloc[posix.sys.socket.socklen_t]()
    !optLen = sizeof[in.sockaddr_in].toUInt

    if (posix.sys.socket.getsockopt(
          fd.fd,
          in.IPPROTO_IP,
          in.IP_MULTICAST_IF,
          optValue.asInstanceOf[Ptr[Byte]],
          optLen
        ) != 0) {
      throw new SocketException(
        s"Exception while getting socket option with id: IP_MULTICAST_IF, errno: ${WSAGetLastError()}"
      )
    }

    SocketHelpers.sockaddrStorageToInetSocketAddress(
      (!optValue).asInstanceOf[Ptr[unixsocket.sockaddr]]
    )
  }

  override def setSocketOption[T](
      fd: FileDescriptor,
      name: SocketOption[T],
      value: T
  ): Unit = {
    val level = optionLevel(name)
    val optName = nativeOptionName(name)
    val optValue = stackalloc[CInt]()
    name.`type` match {
      case `jInteger` =>
        !optValue = value.asInstanceOf[Int]
      case `jBoolean` =>
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
        s"Exception while setting socket option: ${name.name}, errno: ${WSAGetLastError()}"
      )
    }
  }
}
