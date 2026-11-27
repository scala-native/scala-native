package java.net

import java.io.{FileDescriptor, IOException}
import java.net.ipOps._

import scala.scalanative.libc.string.memcpy
import scala.scalanative.meta.LinktimeInfo.{isFreeBSD, isLinux, isWindows}
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
import scala.scalanative.windows.WinSocketApi._
import scala.scalanative.windows.WinSocketApiExt._
import scala.scalanative.windows._

private[net] abstract class AbstractPlainDatagramSocketImpl
    extends DatagramSocketImpl {
  import AbstractPlainDatagramSocketImpl._

  protected def tryPoll(op: String): Unit

  protected[net] var fd = new FileDescriptor
  protected[net] var localport = 0
  protected[net] var address: InetAddress = _
  protected[net] var port = 0

  protected[net] var socket: DatagramSocket = _

  private var useIPv4Only = SocketHelpers.getUseIPv4Stack()
  private var joinedIPv6Group = false
  private var multicastInterface: NetworkInterface = null

  protected[net] var timeout = 0
  protected[net] var connected = false
  protected[net] var connectedAddress: InetAddress = _
  protected[net] var connectedPort = -1

  override def getFileDescriptor(): FileDescriptor = fd

  override def setDatagramSocket(socket: DatagramSocket): Unit =
    this.socket = socket

  override def getDatagramSocket(): DatagramSocket =
    socket

  protected final var isClosed: Boolean =
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
    if (family == posix.sys.socket.AF_INET) {
      val sin = stackalloc[in.sockaddr_in]()
      !len = sizeof[in.sockaddr_in].toUInt

      if (posix.sys.socket.getsockname(
            fd.fd,
            sin.asInstanceOf[Ptr[posix.sys.socket.sockaddr]],
            len
          ) == -1) {
        None
      } else {
        Some(SocketHelpersNative.getSockaddrInPort(sin))
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
        Some(SocketHelpersNative.getSockaddrIn6Port(sin))
      }
    }
  }

  private def bind4(addr: InetAddress, port: Int): Unit = {
    val sa4 = stackalloc[in.sockaddr_in]()
    val sa4Len = sizeof[in.sockaddr_in].toUInt
    SocketHelpers.prepareSockaddrIn4(addr, port, sa4)

    val bindRes = posix.sys.socket.bind(
      fd.fd,
      sa4.asInstanceOf[Ptr[posix.sys.socket.sockaddr]],
      sa4Len
    )

    if (bindRes < 0) {
      throwCannotBind(addr)
    }

    this.localport = fetchLocalPort(posix.sys.socket.AF_INET).getOrElse {
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

  override def bind(port: Int, laddr: InetAddress): Unit = {
    throwIfClosed("bind")
    if (useIPv4Only) bind4(laddr, port)
    else bind6(laddr, port)
  }

  private def send4(p: DatagramPacket): Unit = {
    val insAddr = p.getSocketAddress().asInstanceOf[InetSocketAddress]
    val sa4 = stackalloc[in.sockaddr_in]()
    val sa4Len = sizeof[in.sockaddr_in].toUInt
    SocketHelpers.prepareSockaddrIn4(insAddr.getAddress, insAddr.getPort, sa4)

    val buffer = p.getData()
    val cArr = buffer.at(p.getOffset())
    val len = p.getLength()
    val ret = posix.sys.socket.sendto(
      fd.fd,
      cArr,
      len.toUInt,
      posix.sys.socket.MSG_NOSIGNAL,
      sa4.asInstanceOf[Ptr[posix.sys.socket.sockaddr]],
      sa4Len
    )

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
    val sa4 = stackalloc[in.sockaddr_in]()
    val sa4Len = sizeof[in.sockaddr_in].toUInt
    SocketHelpers.prepareSockaddrIn4(address, port, sa4)

    val connectRet = posix.sys.socket.connect(
      fd.fd,
      sa4.asInstanceOf[Ptr[posix.sys.socket.sockaddr]],
      sa4Len
    )

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

  private def connectTo(address: InetAddress, port: Int): Unit =
    if (useIPv4Only) connect4(address, port)
    else connect6(address, port)

  override def connect(address: InetAddress, port: Int): Unit = {
    throwIfClosed("connect")
    connectTo(address, port)
    connectedAddress = address
    connectedPort = port
    connected = true
  }

  override def disconnect(): Unit = {
    throwIfClosed("disconnect")
    connectTo(SocketHelpers.getWildcardAddress(), 0)
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
    throwIfClosed("setTimeToLive")
    setMulticastOption(
      "IP_MULTICAST_TTL",
      ip.IP_MULTICAST_TTL,
      ip6.IPV6_MULTICAST_HOPS,
      ttl
    )
  }

  override def getTimeToLive(): Int = {
    throwIfClosed("getTimeToLive")
    getMulticastOption(
      "IP_MULTICAST_TTL",
      ip.IP_MULTICAST_TTL,
      ip6.IPV6_MULTICAST_HOPS
    )
  }

  private def throwOptionError(op: String, name: String): Nothing =
    throw new SocketException(
      s"Exception while ${op}ting socket option $name, errno: ${lastError()}"
    )

  private def setIntOption(level: CInt, optName: CInt, value: Int): Boolean = {
    val opt = stackalloc[CInt]()
    !opt = value
    posix.sys.socket.setsockopt(
      fd.fd,
      level,
      optName,
      opt.asInstanceOf[Ptr[Byte]],
      sizeof[CInt].toUInt
    ) == 0
  }

  private def getIntOption(level: CInt, optName: CInt, name: String): Int = {
    val opt = stackalloc[CInt]()
    !opt = 0
    val len = stackalloc[posix.sys.socket.socklen_t]()
    !len = sizeof[CInt].toUInt
    if (posix.sys.socket.getsockopt(
          fd.fd,
          level,
          optName,
          opt.asInstanceOf[Ptr[Byte]],
          len
        ) != 0)
      throwOptionError("get", name)
    // Some BSDs report IPv4 multicast options as a single byte
    if ((!len).toInt == 1) (!opt.asInstanceOf[Ptr[Byte]]) & 0xff
    else !opt
  }

  /* On dual-stack sockets the IPv6 option is authoritative; the IPv4 one is
   * set best-effort since Linux uses it for IPv4 traffic and macOS rejects it.
   */
  private def setMulticastOption(
      name: String,
      v4Opt: CInt,
      v6Opt: CInt,
      value: Int
  ): Unit =
    if (useIPv4Only) {
      if (!setIPv4MulticastOption(v4Opt, value))
        throwOptionError("set", name)
    } else {
      if (!setIntOption(in.IPPROTO_IPV6, v6Opt, value))
        throwOptionError("set", name)
      setIPv4MulticastOption(v4Opt, value)
    }

  // OpenBSD accepts IPv4 multicast TTL and loop only as a one-byte value
  private def setIPv4MulticastOption(optName: CInt, value: Int): Boolean =
    setIntOption(in.IPPROTO_IP, optName, value) || {
      val opt = stackalloc[Byte]()
      !opt = value.toByte
      posix.sys.socket.setsockopt(
        fd.fd,
        in.IPPROTO_IP,
        optName,
        opt,
        1.toUInt
      ) == 0
    }

  private def getMulticastOption(
      name: String,
      v4Opt: CInt,
      v6Opt: CInt
  ): Int =
    if (useIPv4Only) getIntOption(in.IPPROTO_IP, v4Opt, name)
    else getIntOption(in.IPPROTO_IPV6, v6Opt, name)

  private def setMulticastInterface(netIf: NetworkInterface): Unit = {
    var v4Addr: Array[Byte] = null
    val it = netIf.getInetAddresses()
    while (v4Addr == null && it.hasMoreElements()) it.nextElement() match {
      case a: Inet4Address => v4Addr = a.getAddress()
      case _               => ()
    }
    def setV4(): Boolean = v4Addr != null && {
      val addr = stackalloc[in.in_addr]()
      memcpy(addr.asInstanceOf[Ptr[Byte]], v4Addr.at(0), 4.toUSize)
      posix.sys.socket.setsockopt(
        fd.fd,
        in.IPPROTO_IP,
        in.IP_MULTICAST_IF,
        addr.asInstanceOf[Ptr[Byte]],
        sizeof[in.in_addr].toUInt
      ) == 0
    }

    if (useIPv4Only) {
      if (!setV4()) throwOptionError("set", "IP_MULTICAST_IF")
    } else {
      val index = netIf.getIndex()
      if (setIntOption(in.IPPROTO_IPV6, in.IPV6_MULTICAST_IF, index)) setV4()
      else if (!(isOptionUnsupported() && setV4()))
        throwOptionError("set", "IP_MULTICAST_IF")
    }
    multicastInterface = netIf
  }

  // qemu-user rejects IP_MULTICAST_IF reads and IPV6_MULTICAST_IF entirely
  private def isOptionUnsupported(): Boolean =
    mapLastError(_ == ENOPROTOOPT, _ => false)

  private def getMulticastInterface(): NetworkInterface = {
    if (useIPv4Only) {
      val addr = stackalloc[in.in_addr]()
      addr._1 = in.INADDR_ANY
      val len = stackalloc[posix.sys.socket.socklen_t]()
      !len = sizeof[in.in_addr].toUInt
      if (posix.sys.socket.getsockopt(
            fd.fd,
            in.IPPROTO_IP,
            in.IP_MULTICAST_IF,
            addr.asInstanceOf[Ptr[Byte]],
            len
          ) != 0) {
        if (isOptionUnsupported()) return multicastInterface
        throwOptionError("get", "IP_MULTICAST_IF")
      }
      if (addr._1 == in.INADDR_ANY) null
      else {
        val bytes = new Array[Byte](4)
        memcpy(bytes.at(0), addr.asInstanceOf[Ptr[Byte]], 4.toUSize)
        NetworkInterface.getByInetAddress(InetAddress.getByAddress(bytes))
      }
    } else {
      val opt = stackalloc[CInt]()
      !opt = 0
      val len = stackalloc[posix.sys.socket.socklen_t]()
      !len = sizeof[CInt].toUInt
      if (posix.sys.socket.getsockopt(
            fd.fd,
            in.IPPROTO_IPV6,
            in.IPV6_MULTICAST_IF,
            opt.asInstanceOf[Ptr[Byte]],
            len
          ) != 0) {
        if (isOptionUnsupported()) return multicastInterface
        throwOptionError("get", "IP_MULTICAST_IF")
      }
      val index = !opt
      if (index == 0) null
      else NetworkInterface.getByIndex(index)
    }
  }

  private def setMembership(
      level: CInt,
      optID: CInt,
      req: Ptr[Byte],
      len: CSize
  ): Boolean =
    posix.sys.socket.setsockopt(fd.fd, level, optID, req, len.toUInt) == 0

  private def mcastJoinLeave4(
      group: Array[Byte],
      netIf: NetworkInterface,
      join: Boolean
  ): Boolean = {
    var ifAddr: Array[Byte] = null
    if (netIf != null) {
      val it = netIf.getInetAddresses()
      while (ifAddr == null && it.hasMoreElements()) it.nextElement() match {
        case a: Inet4Address => ifAddr = a.getAddress()
        case _               => ()
      }
    }
    if (netIf != null && ifAddr == null) false
    else {
      val req = stackalloc[ip.ip_mreq]()
      memcpy(req.at1, group.at(0), 4.toUSize)
      if (ifAddr == null) req.imr_address.s_addr = in.INADDR_ANY
      else memcpy(req.at2, ifAddr.at(0), 4.toUSize)
      val optID = if (join) ip.IP_ADD_MEMBERSHIP else ip.IP_DROP_MEMBERSHIP
      setMembership(
        in.IPPROTO_IP,
        optID,
        req.asInstanceOf[Ptr[Byte]],
        sizeof[ip.ip_mreq]
      )
    }
  }

  private def mcastJoinLeave6(
      group: Array[Byte],
      netIf: NetworkInterface,
      join: Boolean
  ): Boolean = {
    val req = stackalloc[in.ipv6_mreq]()
    memcpy(req.at1, group.at(0), 16.toUSize)
    req.ipv6mr_interface = (if (netIf == null) 0 else netIf.getIndex()).toUInt
    val optID = if (join) in.IPV6_JOIN_GROUP else in.IPV6_LEAVE_GROUP
    setMembership(
      in.IPPROTO_IPV6,
      optID,
      req.asInstanceOf[Ptr[Byte]],
      sizeof[in.ipv6_mreq]
    )
  }

  private def mcastJoinLeave(
      group: InetAddress,
      netIf: NetworkInterface,
      join: Boolean
  ): Unit = {
    if (group == null || !group.isMulticastAddress())
      throw new SocketException("Not a multicast address")

    val addr = group.getAddress()
    val done = group match {
      case _: Inet4Address =>
        mcastJoinLeave4(addr, netIf, join) || (!useIPv4Only && {
          val v4Errno = if (isFreeBSD) errno else 0
          // macOS & BSDs accept IPv4 groups on IPv6 sockets only v4-mapped
          val mapped = new Array[Byte](16)
          mapped(10) = 0xff.toByte
          mapped(11) = 0xff.toByte
          System.arraycopy(addr, 0, mapped, 12, 4)
          mcastJoinLeave6(mapped, netIf, join) ||
            joinAsIPv4OnFreeBSD(addr, netIf, join, v4Errno)
        })
      case _ =>
        if (useIPv4Only)
          throw new SocketException(
            "IPv6 multicast group is not supported on an IPv4 socket"
          )
        val ok = mcastJoinLeave6(addr, netIf, join)
        if (ok && join) joinedIPv6Group = true
        ok
    }

    if (!done) {
      val op = if (join) "join" else "leave"
      throw new SocketException(
        s"Could not $op multicast group ${group.getHostAddress()}, errno: " +
          lastError()
      )
    }
  }

  private def joinAsIPv4OnFreeBSD(
      group: Array[Byte],
      netIf: NetworkInterface,
      join: Boolean,
      v4Errno: CInt
  ): Boolean =
    if (isFreeBSD) {
      join && v4Errno == EINVAL && switchToIPv4() &&
      mcastJoinLeave4(group, netIf, join)
    } else false

  /* FreeBSD before 16 cannot join IPv4 groups on IPv6 sockets, see ip6(4).
   * Replace the socket with an IPv4 one using the same address, port, peer
   * and options, unless it already relies on IPv6.
   */
  private def switchToIPv4(): Boolean = {
    val storage = stackalloc[posix.sys.socket.sockaddr_storage]()
    val sa = storage.asInstanceOf[Ptr[posix.sys.socket.sockaddr]]
    val len = stackalloc[posix.sys.socket.socklen_t]()
    !len = sizeof[posix.sys.socket.sockaddr_storage].toUInt
    val local =
      if (posix.sys.socket.getsockname(fd.fd, sa, len) != 0) null
      else SocketHelpers.sockaddrStorageToInetSocketAddress(sa)
    val localAddr = local match {
      case null                                         => null
      case l if l.getAddress.isInstanceOf[Inet4Address] => l.getAddress
      case l if l.getAddress.isAnyLocalAddress()        =>
        InetAddress.getByAddress(new Array[Byte](4))
      case _ => null
    }
    val canSwitch = localAddr != null && !joinedIPv6Group &&
      (!connected || connectedAddress.isInstanceOf[Inet4Address])
    canSwitch && {
      import posix.sys.socket.{SOL_SOCKET, SO_REUSEADDR, SO_REUSEPORT}
      import posix.sys.socket.{SO_BROADCAST, SO_RCVBUF, SO_SNDBUF}
      val intOpts =
        Array(SO_REUSEADDR, SO_REUSEPORT, SO_BROADCAST, SO_RCVBUF, SO_SNDBUF)
          .map(o => o -> getIntOption(SOL_SOCKET, o, "SOL_SOCKET"))
      val tclass = getIntOption(in.IPPROTO_IPV6, ip6.IPV6_TCLASS, "IPV6_TCLASS")
      val hops =
        getIntOption(
          in.IPPROTO_IPV6,
          ip6.IPV6_MULTICAST_HOPS,
          "IP_MULTICAST_TTL"
        )
      val loop =
        getIntOption(
          in.IPPROTO_IPV6,
          in.IPV6_MULTICAST_LOOP,
          "IP_MULTICAST_LOOP"
        )
      val fdFlags = posix.fcntl.fcntl(fd.fd, posix.fcntl.F_GETFD, 0)
      val flFlags = posix.fcntl.fcntl(fd.fd, posix.fcntl.F_GETFL, 0)

      val sock = posix.sys.socket.socket(
        posix.sys.socket.AF_INET,
        posix.sys.socket.SOCK_DGRAM,
        0
      )
      sock >= 0 && {
        val replaced = unistd.dup2(sock, fd.fd) >= 0
        unistd.close(sock)
        replaced && {
          useIPv4Only = true
          if (fdFlags >= 0)
            posix.fcntl.fcntl(fd.fd, posix.fcntl.F_SETFD, fdFlags)
          if (flFlags >= 0)
            posix.fcntl.fcntl(fd.fd, posix.fcntl.F_SETFL, flFlags)
          for ((o, v) <- intOpts) setIntOption(SOL_SOCKET, o, v)
          setIntOption(in.IPPROTO_IP, in.IP_TOS, tclass)
          setIPv4MulticastOption(ip.IP_MULTICAST_TTL, hops)
          setIPv4MulticastOption(in.IP_MULTICAST_LOOP, loop)
          val netIf = multicastInterface
          if (netIf != null)
            try setMulticastInterface(netIf)
            catch { case _: SocketException => () }
          if (local.getPort != 0) bind4(localAddr, local.getPort)
          if (connected) connect4(connectedAddress, connectedPort)
          true
        }
      }
    }
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
      if (useIPv4Only) in.IP_TOS else ip6.IPV6_TCLASS
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

    optID match {
      case SocketOptions.SO_TIMEOUT =>
        return Integer.valueOf(this.timeout)
      case SocketOptions.IP_MULTICAST_LOOP =>
        return Boolean.box(
          getMulticastOption(
            "IP_MULTICAST_LOOP",
            in.IP_MULTICAST_LOOP,
            in.IPV6_MULTICAST_LOOP
          ) != 0
        )
      case SocketOptions.IP_MULTICAST_IF2 =>
        return getMulticastInterface()
      case _ => ()
    }

    val level = optID match {
      case SocketOptions.IP_TOS =>
        if (useIPv4Only) in.IPPROTO_IP else in.IPPROTO_IPV6
      case _ => posix.sys.socket.SOL_SOCKET
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

    optID match {
      case SocketOptions.SO_TIMEOUT =>
        this.timeout = value.asInstanceOf[Int]
        return
      case SocketOptions.IP_MULTICAST_LOOP =>
        setMulticastOption(
          "IP_MULTICAST_LOOP",
          in.IP_MULTICAST_LOOP,
          in.IPV6_MULTICAST_LOOP,
          if (value.asInstanceOf[Boolean]) 1 else 0
        )
        return
      case SocketOptions.IP_MULTICAST_IF2 =>
        setMulticastInterface(value.asInstanceOf[NetworkInterface])
        return
      case _ => ()
    }

    val level = optID match {
      case SocketOptions.IP_TOS =>
        if (useIPv4Only) in.IPPROTO_IP else in.IPPROTO_IPV6
      case _ => posix.sys.socket.SOL_SOCKET
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
