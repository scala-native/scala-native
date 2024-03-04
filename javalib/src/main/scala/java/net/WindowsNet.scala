package java.net

import java.io.{FileDescriptor, IOException}
import java.nio.channels.{
  UnresolvedAddressException,
  UnsupportedAddressTypeException
}
import scala.scalanative.posix
import scala.scalanative.posix.netdb.addrinfo
import scala.scalanative.posix.netinet.{in, tcp}
import scala.scalanative.posix.sys.{socket => unixsocket}
import scala.scalanative.unsafe.{CInt, Ptr, sizeof, stackalloc}
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
        s"Exception while getting socket option: ${name.name}, errno: ${WSAGetLastError()}"
      )
    }

    name.`type` match {
      case `jInteger` =>
        Integer.valueOf(!(optValue.asInstanceOf[Ptr[CInt]]))
      case `jBoolean` =>
        Boolean.box(!(optValue.asInstanceOf[Ptr[CInt]]) != 0)
    }
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
