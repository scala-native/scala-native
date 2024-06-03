package java.net

import java.net.ipOps._
import java.io.{FileDescriptor, IOException}
import java.nio.channels.{
  UnresolvedAddressException,
  UnsupportedAddressTypeException
}
import scala.scalanative.posix
import scala.scalanative.posix.arpa.inet
import scala.scalanative.posix.errno._
import scala.scalanative.posix.fcntl._
import scala.scalanative.posix.netinet.in
import scala.scalanative.posix.netinet.tcp
import scala.scalanative.posix.netinet.inOps._
import scala.scalanative.posix.sys.{socket => unixsocket}
import scala.scalanative.posix.unistd
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

private[net] object UnixNet extends Net {

  private val jInteger = classOf[java.lang.Integer]
  private val jBoolean = classOf[java.lang.Boolean]

  @inline override def POLLIN: Int = posix.pollEvents.POLLIN
  @inline override def POLLOUT: Int = posix.pollEvents.POLLOUT

  override def socket(
      family: ProtocolFamily,
      stream: Boolean
  ): FileDescriptor = {
    val addressFamily = family match {
      case StandardProtocolFamily.INET  => unixsocket.AF_INET
      case StandardProtocolFamily.INET6 => unixsocket.AF_INET6
      case _ =>
        throw new UnsupportedOperationException(
          s"Protocol family not supported: ${family.name()}"
        )
    }
    val socketType =
      if (stream) unixsocket.SOCK_STREAM else unixsocket.SOCK_DGRAM

    val sock = unixsocket.socket(addressFamily, socketType, 0)
    if (sock < 0) {
      throw new IOException(
        s"Could not create socket in address family ${family.name()}: $errno"
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
      case (StandardProtocolFamily.INET6, group: Inet6Address) =>
        val mName = stackalloc[ip.ip_mreqn]()
        if (interf != null) {
          val addrPtr = group.getAddress().at(0).asInstanceOf[Ptr[in.in_addr_t]]
          val ifIdx = interf.getIndex()
          mName.imr_multiaddr.s_addr = inet.htonl(!addrPtr)
          mName.imr_address.s_addr = 0.toUInt
          mName.imr_ifindex = ifIdx
        } else {
          val addrPtr = group.getAddress().at(0).asInstanceOf[Ptr[in.in_addr_t]]
          val ifAddrPtr = getMulticastInterfaceOption(fd).getAddress
            .getAddress()
            .at(0)
            .asInstanceOf[Ptr[in.in_addr_t]]
          mName.imr_multiaddr.s_addr = inet.htonl(!addrPtr)
          mName.imr_address.s_addr = inet.htonl(!ifAddrPtr)
          mName.imr_ifindex = 0
        }
        (mName.asInstanceOf[Ptr[Byte]], sizeof[ip.ip_mreqn].toUInt)
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
        s"Exception while setting socket option $name, errno: $errno"
      )
    }
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
