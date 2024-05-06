package java.net

import java.io.FileDescriptor
import java.nio.channels.UnsupportedAddressTypeException
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix
import scala.scalanative.posix.netinet.in
import scala.scalanative.posix.sys.socketOps._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

trait Net {
  import Net._

  def POLLIN: Int
  def POLLOUT: Int

  def prepareSockaddrIn(
      isa: InetSocketAddress
  ): (Ptr[posix.sys.socket.sockaddr], posix.sys.socket.socklen_t) = {
    val addr = isa.getAddress
    val port = isa.getPort
    addr match {
      case ipv4: Inet4Address =>
        val sa4 = stackalloc[in.sockaddr_in]()
        val sa4Len = sizeof[in.sockaddr_in].toUInt
        SocketHelpers.prepareSockaddrIn4(ipv4, port, sa4)
        (sa4.asInstanceOf[Ptr[posix.sys.socket.sockaddr]], sa4Len)
      case ipv6: Inet6Address =>
        val sa6 = stackalloc[in.sockaddr_in6]()
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
      case null => new InetSocketAddress(SocketHelpers.getLoopbackAddress(), 0)
      case _    => checkAddress(local)
    }
    val (sa, len) = prepareSockaddrIn(isa)
    val bindRes = posix.sys.socket.bind(fd.fd, sa, len)

    if (bindRes < 0) {
      throw new BindException(
        "Couldn't bind to an address: " + isa.getAddress.getHostAddress()
      )
    }
  }
  def connect(fd: FileDescriptor, remote: SocketAddress): Unit = {
    val isa = checkAddress(remote)
    val (sa, len) = prepareSockaddrIn(isa)
    val connectRes = posix.sys.socket.connect(fd.fd, sa, len)
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
  def getSocketOption[T](fd: FileDescriptor, name: SocketOption[T]): T
  def setSocketOption[T](
      fd: FileDescriptor,
      name: SocketOption[T],
      value: T
  ): Unit

}

object Net extends Net {

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
