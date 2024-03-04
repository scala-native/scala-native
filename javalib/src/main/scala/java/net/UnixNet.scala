package java.net

import java.io.{FileDescriptor, IOException}
import java.nio.channels.{
  UnresolvedAddressException,
  UnsupportedAddressTypeException
}
import scala.scalanative.posix
import scala.scalanative.posix.errno._
import scala.scalanative.posix.fcntl._
import scala.scalanative.posix.netinet.in
import scala.scalanative.posix.netinet.tcp
import scala.scalanative.posix.sys.{socket => unixsocket}
import scala.scalanative.posix.unistd
import scala.scalanative.unsafe.{CInt, Ptr, sizeof, stackalloc}

object UnixNet extends Net {

  private val jInteger = classOf[java.lang.Integer]
  private val jBoolean = classOf[java.lang.Boolean]

  @inline override def POLLIN: Int = posix.pollEvents.POLLIN
  @inline override def POLLOUT: Int = posix.pollEvents.POLLOUT

  override def socket(
      family: ProtocolFamily,
      stream: Boolean
  ): FileDescriptor = {
    val af = family match {
      case StandardProtocolFamily.INET  => unixsocket.AF_INET
      case StandardProtocolFamily.INET6 => unixsocket.AF_INET6
      case _ =>
        throw new UnsupportedOperationException(
          s"Protocol family not supported: ${family.name()}"
        )
    }
    val socketType =
      if (stream) unixsocket.SOCK_STREAM else unixsocket.SOCK_DGRAM

    val sock = unixsocket.socket(af, socketType, 0)
    if (sock < 0) {
      throw new IOException(
        s"Could not create socket in address family: ${family.name()}"
      )
    }
    val fileHandle = FileDescriptor.FileHandle(sock)
    new FileDescriptor(fileHandle, readOnly = false)
  }

  override def checkAddress(sa: SocketAddress): InetSocketAddress = sa match {
    case null =>
      throw new NullPointerException()
    case isa: InetSocketAddress if isa.isUnresolved =>
      throw new UnresolvedAddressException()
    case isa: InetSocketAddress =>
      isa
    case _ =>
      throw new UnsupportedAddressTypeException()
  }

  override def close(fd: FileDescriptor): Unit = unistd.close(fd.fd)

  override def localAddress(
      fd: FileDescriptor,
      family: ProtocolFamily
  ): SocketAddress = {
    val len = stackalloc[unixsocket.socklen_t]()
    val saddr = family match {
      case StandardProtocolFamily.INET =>
        !len = sizeof[in.sockaddr_in].toUInt
        stackalloc[in.sockaddr_in]().asInstanceOf[Ptr[unixsocket.sockaddr]]
      case StandardProtocolFamily.INET6 =>
        !len = sizeof[in.sockaddr_in6].toUInt
        stackalloc[in.sockaddr_in6]().asInstanceOf[Ptr[unixsocket.sockaddr]]
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
    updateSocketFdOpts(fd.fd) { oldOpts =>
      if (blocking) oldOpts & ~O_NONBLOCK
      else oldOpts | O_NONBLOCK
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
        s"Exception while getting socket option: ${name.name}, errno: $errno"
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
        s"Exception while setting socket option: ${name.name}, errno: $errno"
      )
    }
  }

  @inline
  private def getSocketFdOpts(fdFd: Int): CInt = {
    val opts = fcntl(fdFd, F_GETFL, 0)
    if (opts == -1)
      throw new ConnectException(
        s"connect failed, fcntl F_GETFL, errno: $errno"
      )
    opts
  }

  @inline
  private def setSocketFdOpts(fdFd: Int, opts: Int): Unit = {
    val ret = fcntl(fdFd, F_SETFL, opts)
    if (ret == -1)
      throw new ConnectException(
        s"connect failed, fcntl F_SETFL for opts: $opts, errno: $errno"
      )
  }

  @inline
  private def updateSocketFdOpts(fdFd: Int)(mapping: CInt => CInt): Int = {
    val oldOpts = getSocketFdOpts(fdFd)
    setSocketFdOpts(fdFd, mapping(oldOpts))
    oldOpts
  }

}
