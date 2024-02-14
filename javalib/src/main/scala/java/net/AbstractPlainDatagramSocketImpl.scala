package java.net

import java.net.ipOps._
import java.io.{FileDescriptor, IOException}
import scala.scalanative.libc.string.memcpy
import scala.scalanative.meta.LinktimeInfo.{isLinux, isWindows}
import scala.scalanative.posix
import scala.scalanative.posix.arpa.inet
import scala.scalanative.posix.errno._
import scala.scalanative.posix.netdb._
import scala.scalanative.posix.netdbOps._
import scala.scalanative.posix.netinet.in
import scala.scalanative.posix.netinet.inOps._
import scala.scalanative.posix.sys.ioctl.{FIONREAD, ioctl}
import scala.scalanative.posix.sys.socketOps._
import scala.scalanative.posix.sys.time._
import scala.scalanative.posix.sys.timeOps._
import scala.scalanative.posix.unistd
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.WinSocketApi.WSAGetLastError
import scala.scalanative.windows._
import scala.scalanative.windows.WinSocketApi._
import scala.scalanative.windows.WinSocketApiExt._

private[net] abstract class AbstractPlainDatagramSocketImpl
    extends DatagramSocketImpl {
  import AbstractPlainDatagramSocketImpl._

  protected def tryPoll(op: String): Unit

  protected[net] var fd = new FileDescriptor
  protected[net] var localport = 0
  protected[net] var address: InetAddress = _
  protected[net] var port = 0

  protected[net] var socket: DatagramSocket = _

  private final val useIPv4Only = SocketHelpers.getUseIPv4Stack()

  protected[net] var timeout = 0
  protected[net] var connected = false
  protected[net] var connectedAddress: InetAddress = _
  protected[net] var connectedPort = -1

  override def getFileDescriptor(): FileDescriptor = fd

  override def setDatagramSocket(socket: DatagramSocket): Unit =
    this.socket = socket

  override def getDatagramSocket(): DatagramSocket =
    socket

  final protected var isClosed: Boolean =
    fd == InvalidSocketDescriptor

  private def throwIfClosed(methodName: String): Unit = {
    if (isClosed) {
      throw new SocketException(s"$methodName: Socket is closed")
    }
  }

  private def throwCannotBind(addr: InetAddress): Nothing = {
    throw new BindException(
      "Couldn't bind to an address: " + addr.getHostAddress()
    )
  }

  private def fetchLocalPort(family: Int): Option[Int] = {
    val len = stackalloc[posix.sys.socket.socklen_t]()
    val portOpt = if (family == posix.sys.socket.AF_INET) {
      val sin = stackalloc[in.sockaddr_in]()
      !len = sizeof[in.sockaddr_in].toUInt

      if (posix.sys.socket.getsockname(
            fd.fd,
            sin.asInstanceOf[Ptr[posix.sys.socket.sockaddr]],
            len
          ) == -1) {
        None
      } else {
        Some(sin.sin_port)
      }
    } else {
      val sin = stackalloc[in.sockaddr_in6]()
      !len = sizeof[in.sockaddr_in6].toUInt

      if (posix.sys.socket.getsockname(
            fd.fd,
            sin.asInstanceOf[Ptr[posix.sys.socket.sockaddr]],
            len
          ) == -1) {
        None
      } else {
        Some(sin.sin6_port)
      }
    }

    portOpt.map(inet.ntohs(_).toInt)
  }

  private def prepareSockaddrIn4(
      inetAddress: InetAddress,
      port: Int,
      hints: Ptr[addrinfo],
      ret: Ptr[Ptr[addrinfo]]
  ): Unit = {
    hints.ai_family = posix.sys.socket.AF_UNSPEC
    hints.ai_flags = AI_NUMERICHOST
    hints.ai_socktype = posix.sys.socket.SOCK_DGRAM

    Zone.acquire { implicit z =>
      val cIP = toCString(inetAddress.getHostAddress())
      if (getaddrinfo(cIP, toCString(port.toString), hints, ret) != 0) {
        throw new BindException(
          "Couldn't resolve address: " + inetAddress.getHostAddress()
        )
      }
    }
  }

  private def bind4(addr: InetAddress, port: Int): Unit = {
    val hints = stackalloc[addrinfo]()
    val sa4Ptr = stackalloc[Ptr[addrinfo]]()

    prepareSockaddrIn4(addr, port, hints, sa4Ptr)
    val sa4 = (!sa4Ptr).ai_addr
    val sa4Len = (!sa4Ptr).ai_addrlen
    val sa4Family = (!sa4Ptr).ai_family

    val bindRes = posix.sys.socket.bind(
      fd.fd,
      sa4,
      sa4Len
    )

    freeaddrinfo(!sa4Ptr)

    if (bindRes < 0) {
      throwCannotBind(addr)
    }

    this.localport = fetchLocalPort(sa4Family).getOrElse {
      throwCannotBind(addr)
    }
  }

  private def bind6(addr: InetAddress, port: Int): Unit = {
    val sa6 = stackalloc[in.sockaddr_in6]()
    val sa6Len = sizeof[in.sockaddr_in6].toUInt

    // By contract, all the bytes in sa6 are zero going in.
    SocketHelpers.prepareSockaddrIn6(addr, port, sa6)

    val bindRes = posix.sys.socket.bind(
      fd.fd,
      sa6.asInstanceOf[Ptr[posix.sys.socket.sockaddr]],
      sa6Len
    )

    if (bindRes < 0) {
      throwCannotBind(addr)
    }

    this.localport = fetchLocalPort(sa6.sin6_family.toInt).getOrElse {
      throwCannotBind(addr)
    }
  }

  private lazy val bindFunc =
    if (useIPv4Only) bind4(_: InetAddress, _: Int)
    else bind6(_: InetAddress, _: Int)

  override def bind(port: Int, laddr: InetAddress): Unit = {
    throwIfClosed("bind")
    bindFunc(laddr, port)
  }

  private def send4(p: DatagramPacket): Unit = {
    val insAddr = p.getSocketAddress().asInstanceOf[InetSocketAddress]
    val hints = stackalloc[addrinfo]()
    val sa4Ptr = stackalloc[Ptr[addrinfo]]()
    prepareSockaddrIn4(insAddr.getAddress, insAddr.getPort, hints, sa4Ptr)
    val sa4 = (!sa4Ptr).ai_addr
    val sa4Len = (!sa4Ptr).ai_addrlen

    val buffer = p.getData()
    val cArr = buffer.at(p.getOffset())
    val len = p.getLength()
    val ret = posix.sys.socket.sendto(
      fd.fd,
      cArr,
      len.toUInt,
      posix.sys.socket.MSG_NOSIGNAL,
      sa4,
      sa4Len
    )

    freeaddrinfo(!sa4Ptr)

    if (ret < 0) {
      throw new IOException("Could not send the datagram packet to the client")
    }
  }

  private def send6(p: DatagramPacket): Unit = {
    val insAddr = p.getSocketAddress().asInstanceOf[InetSocketAddress]
    val sa6 = stackalloc[in.sockaddr_in6]()
    val sa6Len = sizeof[in.sockaddr_in6].toUInt

    // By contract, all the bytes in sa6 are zero going in.
    SocketHelpers.prepareSockaddrIn6(insAddr.getAddress, insAddr.getPort, sa6)

    val buffer = p.getData()
    val cArr = buffer.at(p.getOffset())
    val len = p.getLength()
    val ret = posix.sys.socket.sendto(
      fd.fd,
      cArr,
      len.toUInt,
      posix.sys.socket.MSG_NOSIGNAL,
      sa6.asInstanceOf[Ptr[posix.sys.socket.sockaddr]],
      sa6Len
    )
    if (ret < 0) {
      throw new IOException("Could not send the datagram packet to the client")
    }
  }

  override def send(p: DatagramPacket): Unit = {
    throwIfClosed("send")
    if (useIPv4Only) send4(p)
    else send6(p)
  }

  private def connect4(address: InetAddress, port: Int): Unit = {
    val hints = stackalloc[addrinfo]()
    val sa4Ptr = stackalloc[Ptr[addrinfo]]()
    prepareSockaddrIn4(address, port, hints, sa4Ptr)
    val sa4 = (!sa4Ptr).ai_addr
    val sa4Len = (!sa4Ptr).ai_addrlen

    val connectRet = posix.sys.socket.connect(fd.fd, sa4, sa4Len)

    if (connectRet < 0) {
      throw new ConnectException(
        s"Could not connect to address: $address"
          + s" on port: $port"
          + s", errno: ${lastError()}"
      )
    }
  }

  private def connect6(address: InetAddress, port: Int): Unit = {
    val sa6 = stackalloc[in.sockaddr_in6]()
    val sa6Len = sizeof[in.sockaddr_in6].toUInt

    // By contract, all the bytes in sa6 are zero going in.
    SocketHelpers.prepareSockaddrIn6(address, port, sa6)

    val connectRet = posix.sys.socket.connect(
      fd.fd,
      sa6.asInstanceOf[Ptr[posix.sys.socket.sockaddr]],
      sa6Len
    )

    if (connectRet < 0) {
      throw new ConnectException(
        s"Could not connect to address: $address"
          + s" on port: $port"
          + s", errno: ${lastError()}"
      )
    }
  }

  private lazy val connectFunc =
    if (useIPv4Only) connect4(_: InetAddress, _: Int)
    else connect6(_: InetAddress, _: Int)

  override def connect(address: InetAddress, port: Int): Unit = {
    throwIfClosed("connect")
    connectFunc(address, port)
    connectedAddress = address
    connectedPort = port
    connected = true
  }

  override def disconnect(): Unit = {
    throwIfClosed("disconnect")
    connectFunc(SocketHelpers.getWildcardAddress(), 0)
    connectedAddress = null
    connectedPort = -1
    connected = false
  }

  override def close(): Unit = {
    if (!isClosed) {
      if (isWindows) WinSocketApi.closeSocket(fd.handle)
      else unistd.close(fd.fd)
      fd = InvalidSocketDescriptor
      isClosed = true
    }
  }

  private def recvfrom(p: DatagramPacket, flag: CInt, op: String): Unit = {
    if (timeout > 0)
      tryPoll(op)

    val storage = stackalloc[posix.sys.socket.sockaddr_storage]()
    val destAddr = storage.asInstanceOf[Ptr[posix.sys.socket.sockaddr]]
    val addressLen = stackalloc[posix.sys.socket.socklen_t]()
    !addressLen = sizeof[posix.sys.socket.sockaddr_storage].toUInt

    val buffer = p.getData()
    val offset = p.getOffset()
    val length = p.getLength()

    val bytesNum = posix.sys.socket
      .recvfrom(
        fd.fd,
        buffer.at(offset),
        length.toUInt,
        flag,
        destAddr,
        addressLen
      )
      .toInt

    def timeoutDetected = mapLastError(
      onUnix = { err => err == EAGAIN || err == EWOULDBLOCK },
      onWindows = { err => err == WSAEWOULDBLOCK || err == WSAETIMEDOUT }
    )

    bytesNum match {
      case _ if bytesNum >= 0 =>
        p.setSocketAddress(
          SocketHelpers.sockaddrStorageToInetSocketAddress(destAddr)
        )
        p.setLength(bytesNum)
      case _ if timeoutDetected =>
        throw new SocketTimeoutException("Socket timeout while reading data")
      case _ =>
        throw new SocketException(s"read failed, errno: ${lastError()}")
    }
  }

  override def peekData(p: DatagramPacket): Int = {
    throwIfClosed("peekData")
    recvfrom(p, posix.sys.socket.MSG_PEEK, "peekData")
    p.getPort()
  }

  override def receive(p: DatagramPacket): Unit = {
    throwIfClosed("receive")
    recvfrom(p, 0, "receive")
  }

  override def setTTL(ttl: Byte): Unit =
    throw new UnsupportedOperationException(
      "Deprecated method setTTL. Use setTimeToLive instead"
    )

  override def getTTL(): Byte =
    throw new UnsupportedOperationException(
      "Deprecated method getTTL. Use getTimeToLive instead"
    )

  override def setTimeToLive(ttl: Int): Unit = {
    if (isClosed) {
      throw new SocketException("Socket is closed")
    }
    val level = in.IPPROTO_IP
    val optValue =
      if (useIPv4Only) ip.IP_MULTICAST_TTL else ip6.IPV6_MULTICAST_HOPS
    val opt = stackalloc[CInt]()
    val len = sizeof[CInt].toUInt
    !opt = ttl

    if (posix.sys.socket.setsockopt(
          fd.fd,
          level,
          optValue,
          opt.asInstanceOf[Ptr[Byte]],
          len
        ) != 0) {
      throw new SocketException(
        "Exception while setting socket option with id: IP_MULTICAST_TTL, errno: " + lastError()
      )
    }
  }

  override def getTimeToLive(): Int = {
    if (isClosed) {
      throw new SocketException("Socket is closed")
    }
    val level = in.IPPROTO_IP
    val optValue =
      if (useIPv4Only) ip.IP_MULTICAST_TTL else ip6.IPV6_MULTICAST_HOPS
    val opt = stackalloc[CInt]()
    val len = stackalloc[posix.sys.socket.socklen_t]()
    !len = sizeof[CInt].toUInt

    if (posix.sys.socket.getsockopt(
          fd.fd,
          level,
          optValue,
          opt.asInstanceOf[Ptr[Byte]],
          len
        ) != 0) {
      throw new SocketException(
        "Exception while getting socket option with id: IP_MULTICAST_TTL, errno: " + lastError()
      )
    }

    Integer.valueOf(!opt)
  }

  def mcastJoinLeave4(
      inetaddr: InetAddress,
      netIf: NetworkInterface,
      join: Boolean
  ): Unit = {
    val mName = stackalloc[ip.ip_mreq]()
    if (netIf != null) {
      val ifAddrs = netIf.getInetAddresses()
      if (!ifAddrs.hasMoreElements()) {
        throw new SocketException(
          "bad argument for IP_ADD_MEMBERSHIP: No IP addresses bound to interface"
        )
      }
      val addrPtr = inetaddr.getAddress().at(0).asInstanceOf[Ptr[in.in_addr_t]]
      val ifAddrPtr =
        ifAddrs.nextElement().getAddress().at(0).asInstanceOf[Ptr[in.in_addr_t]]
      mName.imr_multiaddr.s_addr = inet.htonl(!addrPtr)
      mName.imr_address.s_addr = inet.htonl(!ifAddrPtr)
    } else {
      val opt = stackalloc[Ptr[in.sockaddr_in]]()
      val len = stackalloc[posix.sys.socket.socklen_t]()
      !len = sizeof[in.sockaddr_in].toUInt

      if (posix.sys.socket.getsockopt(
            fd.fd,
            in.IPPROTO_IP,
            in.IP_MULTICAST_IF,
            opt.asInstanceOf[Ptr[Byte]],
            len
          ) != 0) {
        throw new SocketException(
          "Exception while getting socket option with id: IP_MULTICAST_IF, errno: " + lastError()
        )
      }

      val addrPtr = inetaddr.getAddress().at(0).asInstanceOf[Ptr[in.in_addr_t]]
      val ifAddrPtr = (!opt).sin_addr.asInstanceOf[Ptr[in.in_addr_t]]
      mName.imr_multiaddr.s_addr = inet.htonl(!addrPtr)
      mName.imr_address.s_addr = inet.htonl(!ifAddrPtr)
    }

    // join / leave the multicast group.
    val optID = if (join) ip.IP_ADD_MEMBERSHIP else ip.IP_DROP_MEMBERSHIP
    if (posix.sys.socket.setsockopt(
          fd.fd,
          in.IPPROTO_IP,
          optID,
          mName.asInstanceOf[Ptr[Byte]],
          sizeof[ip.ip_mreq].toUInt
        ) != 0) {
      throw new SocketException(
        "Exception while setting socket option with id: "
          + optID + ", errno: " + lastError()
      )
    }
  }

  def mcastJoinLeave6(
      inetaddr: InetAddress,
      netIf: NetworkInterface,
      join: Boolean
  ): Unit = {
    val mName = stackalloc[ip.ip_mreqn]()
    if (netIf != null) {
      val addrPtr = inetaddr.getAddress().at(0).asInstanceOf[Ptr[in.in_addr_t]]
      val ifIdx = netIf.getIndex()
      mName.imr_multiaddr.s_addr = inet.htonl(!addrPtr)
      mName.imr_address.s_addr = 0.toUInt
      mName.imr_ifindex = ifIdx
    } else {
      val opt = stackalloc[Ptr[in.sockaddr_in]]()
      val len = stackalloc[posix.sys.socket.socklen_t]()
      !len = sizeof[in.sockaddr_in].toUInt

      if (posix.sys.socket.getsockopt(
            fd.fd,
            in.IPPROTO_IP,
            in.IP_MULTICAST_IF,
            opt.asInstanceOf[Ptr[Byte]],
            len
          ) != 0) {
        throw new SocketException(
          "Exception while getting socket option with id: IP_MULTICAST_IF, errno: " + lastError()
        )
      }
      val addrPtr = inetaddr.getAddress().at(0).asInstanceOf[Ptr[in.in_addr_t]]
      val ifAddrPtr = (!opt).sin_addr.asInstanceOf[Ptr[in.in_addr_t]]
      mName.imr_multiaddr.s_addr = inet.htonl(!addrPtr)
      mName.imr_address.s_addr = inet.htonl(!ifAddrPtr)
      mName.imr_ifindex = 0
    }

    // join / leave the multicast group.
    val optID = if (join) ip.IP_ADD_MEMBERSHIP else ip.IP_DROP_MEMBERSHIP
    if (posix.sys.socket.setsockopt(
          fd.fd,
          in.IPPROTO_IP,
          optID,
          mName.asInstanceOf[Ptr[Byte]],
          sizeof[ip.ip_mreqn].toUInt
        ) != 0) {
      throw new SocketException(
        "Exception while setting socket option with id: "
          + optID + ", errno: " + lastError()
      )
    }
  }

  def mcastJoinLeave(
      inetaddr: InetAddress,
      netIf: NetworkInterface,
      join: Boolean
  ): Unit = {
    if (useIPv4Only) mcastJoinLeave4(inetaddr, netIf, join)
    else mcastJoinLeave6(inetaddr, netIf, join)
  }

  override def join(inetaddr: InetAddress): Unit = {
    throwIfClosed("join")
    mcastJoinLeave(inetaddr, null, true)
  }

  override def leave(inetaddr: InetAddress): Unit = {
    throwIfClosed("leave")
    mcastJoinLeave(inetaddr, null, false)
  }

  override def joinGroup(
      mcastaddr: SocketAddress,
      netIf: NetworkInterface
  ): Unit = {
    throwIfClosed("joinGroup")
    mcastaddr match {
      case inetaddr: InetSocketAddress =>
        mcastJoinLeave(inetaddr.getAddress, netIf, true)
      case _ =>
        throw new IllegalArgumentException("Unsupported address type")
    }
  }

  override def leaveGroup(
      mcastaddr: SocketAddress,
      netIf: NetworkInterface
  ): Unit = {
    throwIfClosed("leaveGroup")
    mcastaddr match {
      case inetaddr: InetSocketAddress =>
        mcastJoinLeave(inetaddr.getAddress, netIf, false)
      case _ =>
        throw new IllegalArgumentException("Unsupported address type")
    }
  }

  // We can't directly map values in SocketOptions to the native ones,
  // because some of them have the same value, but require different levels
  // for example IP_TOS and TCP_NODELAY have the same value on my machine
  private def nativeValueFromOption(option: Int) = option match {
    case SocketOptions.IP_TOS =>
      SocketHelpers.getTrafficClassSocketOption()
    case SocketOptions.SO_TIMEOUT   => posix.sys.socket.SO_RCVTIMEO
    case SocketOptions.SO_RCVBUF    => posix.sys.socket.SO_RCVBUF
    case SocketOptions.SO_SNDBUF    => posix.sys.socket.SO_SNDBUF
    case SocketOptions.SO_REUSEADDR => posix.sys.socket.SO_REUSEADDR
    case SocketOptions.SO_BROADCAST => posix.sys.socket.SO_BROADCAST
    case _                          => sys.error(s"Unknown option: $option")
  }

  override def getOption(optID: Int): Object = {
    if (isClosed) {
      throw new SocketException("Socket is closed")
    }

    if (optID == SocketOptions.SO_TIMEOUT) {
      return Integer.valueOf(this.timeout)
    }

    val level = optID match {
      case SocketOptions.IP_TOS => SocketHelpers.getIPPROTO()
      case _                    => posix.sys.socket.SOL_SOCKET
    }

    val optValue = nativeValueFromOption(optID)

    val opt = stackalloc[CInt]().asInstanceOf[Ptr[Byte]]

    val len = stackalloc[posix.sys.socket.socklen_t]()
    !len = sizeof[CInt].toUInt

    if (posix.sys.socket.getsockopt(fd.fd, level, optValue, opt, len) != 0) {
      throw new SocketException(
        "Exception while getting socket option with id: "
          + optValue + ", errno: " + lastError()
      )
    }

    optID match {
      case SocketOptions.SO_REUSEADDR | SocketOptions.SO_BROADCAST =>
        Boolean.box(!(opt.asInstanceOf[Ptr[CInt]]) != 0)
      case SocketOptions.SO_TIMEOUT =>
        Integer.valueOf(this.timeout)
      case _ =>
        Integer.valueOf(!(opt.asInstanceOf[Ptr[CInt]]))
    }
  }

  override def setOption(optID: Int, value: Object): Unit = {
    if (isClosed) {
      throw new SocketException("Socket is closed")
    }

    if (optID == SocketOptions.SO_TIMEOUT) {
      this.timeout = value.asInstanceOf[Int]
      return
    }

    val level = optID match {
      case SocketOptions.IP_TOS => SocketHelpers.getIPPROTO()
      case _                    => posix.sys.socket.SOL_SOCKET
    }
    val optValue = nativeValueFromOption(optID)

    val len = {
      optID match {
        case SocketOptions.SO_TIMEOUT =>
          if (isWindows) sizeof[DWord]
          else sizeof[timeval]
        case _ => sizeof[CInt]
      }
    }.toUInt

    val opt = optID match {
      case SocketOptions.SO_REUSEADDR | SocketOptions.SO_BROADCAST =>
        val ptr = stackalloc[CInt]()
        !ptr = if (value.asInstanceOf[Boolean]) 1 else 0
        ptr.asInstanceOf[Ptr[Byte]]
      case SocketOptions.SO_TIMEOUT =>
        val mseconds = value.asInstanceOf[Int]
        this.timeout = mseconds

        if (isWindows) {
          val ptr = stackalloc[DWord]()
          !ptr = mseconds.toUInt
          ptr.asInstanceOf[Ptr[Byte]]
        } else {
          val ptr = stackalloc[timeval]()

          ptr.tv_sec = mseconds / 1000
          ptr.tv_usec = (mseconds % 1000) * 1000

          ptr.asInstanceOf[Ptr[Byte]]
        }

      case _ =>
        val ptr = stackalloc[CInt]()
        !ptr = value.asInstanceOf[Int]
        ptr.asInstanceOf[Ptr[Byte]]
    }

    if (posix.sys.socket.setsockopt(fd.fd, level, optValue, opt, len) != 0) {
      throw new SocketException(
        "Exception while setting socket option with id: "
          + optValue + ", errno: " + lastError()
      )
    }
  }

  override def dataAvailable(): Int = {
    if (isClosed) {
      throw new SocketException("Socket closed")
    } else {
      val bytesAvailable = stackalloc[CInt]()
      ioctl(fd.fd, FIONREAD, bytesAvailable.asInstanceOf[Ptr[Byte]])
      !bytesAvailable match {
        case x if x < 0 => -1
        case x          => x
      }
    }
  }

  private def lastError(): CInt = mapLastError(identity, identity)

  private def mapLastError[T](
      onUnix: CInt => T,
      onWindows: CInt => T
  ): T = {
    if (isWindows)
      onWindows(WSAGetLastError())
    else
      onUnix(errno)
  }

}

private[net] object AbstractPlainDatagramSocketImpl {
  final val InvalidSocketDescriptor = new FileDescriptor()

  def apply(): AbstractPlainDatagramSocketImpl = {
    if (isWindows)
      new WindowsPlainDatagramSocketImpl()
    else
      new UnixPlainDatagramSocketImpl()
  }
}
