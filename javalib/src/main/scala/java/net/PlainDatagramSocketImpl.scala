package java.net

import java.io.FileDescriptor
import java.io.IOException
import java.io.InterruptedIOException

import scala.scalanative.native._
import scala.scalanative.runtime.ByteArray
import scala.scalanative.posix.errno._
import scala.scalanative.posix.sys.socket
import scala.scalanative.posix.sys.socketOps._
import scala.scalanative.posix.netinet.in
import scala.scalanative.posix.netinet.inOps._
import scala.scalanative.posix.netinet.tcp
import scala.scalanative.posix.arpa.inet
import scala.scalanative.posix.netdb._
import scala.scalanative.posix.netdbOps._
import scala.scalanative.posix.sys.ioctl._
import scala.scalanative.posix.fcntl._
import scala.scalanative.posix.sys.select._
import scala.scalanative.posix.sys.selectOps._
import scala.scalanative.posix.unistd.{close => cClose}
import scala.scalanative.posix.arpa.inet

// Ported from Apache Harmony

class PlainDatagramSocketImpl extends DatagramSocketImpl {
  import PlainDatagramSocketImpl._

  fd = new FileDescriptor()

  private val bindToDevice                         = false
  private var ipaddress                            = Array[Byte](0, 0, 0, 0)
  private var ttl                                  = 1
  @volatile private var isNativeConnected: Boolean = _
  var receiveTimeout: Int                          = _
  val streaming: Boolean                           = true
  var shutdownInput: Boolean                       = _
  private var connectedAddress: InetAddress        = _
  private var connectedPort                        = -1
  private var trafficClass: Int                    = _

  def this(fd: FileDescriptor, localPort: Int) = {
    this()
    this.fd = fd
    this.localPort = localPort
  }

  private def fetchLocalPort(family: Int): Option[Int] = {
    val len = stackalloc[socket.socklen_t]
    val portOpt = if (family == socket.AF_INET) {
      val sin = stackalloc[in.sockaddr_in]
      !len = sizeof[in.sockaddr_in].toUInt

      if (socket.getsockname(fd.fd, sin.cast[Ptr[socket.sockaddr]], len) == -1) {
        None
      } else {
        Some(sin.sin_port)
      }
    } else {
      val sin = stackalloc[in.sockaddr_in6]
      !len = sizeof[in.sockaddr_in6].toUInt

      if (socket.getsockname(fd.fd, sin.cast[Ptr[socket.sockaddr]], len) == -1) {
        None
      } else {
        Some(sin.sin6_port)
      }
    }

    portOpt.map(inet.ntohs(_).toInt)
  }

  def bind(port: Int, addr: InetAddress): Unit = {
    val hints = stackalloc[addrinfo]
    val ret   = stackalloc[Ptr[addrinfo]]
    string.memset(hints.cast[Ptr[Byte]], 0, sizeof[addrinfo])
    hints.ai_family = socket.AF_UNSPEC
    hints.ai_flags = AI_NUMERICHOST
    hints.ai_socktype = socket.SOCK_DGRAM

    Zone { implicit z =>
      val cIP = toCString(addr.getHostAddress)
      if (getaddrinfo(cIP, toCString(port.toString), hints, ret) != 0) {
        throw new BindException(
          "Couldn't resolve address: " + addr.getHostAddress)
      }
    }

    val bindRes = socket.bind(fd.fd, (!ret).ai_addr, (!ret).ai_addrlen)

    val family = (!ret).ai_family
    freeaddrinfo(!ret)

    if (bindRes < 0) {
      throw new BindException(
        "Couldn't bind to an address: " + addr.getHostAddress +
          " on port: " + port.toString)
    }

    localPort = fetchLocalPort(family).getOrElse {
      throw new BindException(
        "Couldn't bind to address: " + addr.getHostAddress + " on port: " + port)
    }
    try {
      setOption(SocketOptions.SO_BROADCAST, Boolean.box(true))
    } catch {
      case e: IOException =>
    }
  }

  def close(): Unit = fd.synchronized {
    if (fd.valid()) {
      try {
        cClose(fd.fd)
      } catch {
        case e: IOException =>
      }
      fd = new FileDescriptor()
    }
  }

  def create(): Unit = {
    var sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, 0)
    if (sock < 0) {
      throw new IOException("Couldn't create a socket")
    }
    fd = new FileDescriptor(sock)
  }

  override def finalize(): Unit = close()

  private def getSocketOption(anOption: Int): Object = {
    if (!fd.valid()) {
      throw new SocketException("Bad socket.")
    }

    val level = anOption match {
      case SocketOptions.IP_TOS | IP_MULTICAST_TTL |
          SocketOptions.IP_MULTICAST_IF | SocketOptions.IP_MULTICAST_LOOP =>
        in.IPPROTO_IP
      case SocketOptions.IP_MULTICAST_IF2 => in.IPPROTO_IPV6
      case SocketOptions.TCP_NODELAY      => in.IPPROTO_TCP
      case _                              => socket.SOL_SOCKET
    }
    val optValue = nativeValueFromOption(anOption)

    val opt = anOption match {
      case SocketOptions.SO_LINGER => stackalloc[socket.linger].cast[Ptr[Byte]]
      case SocketOptions.IP_MULTICAST_IF =>
        stackalloc[in.sockaddr_in].cast[Ptr[Byte]]
      case _ => stackalloc[CInt].cast[Ptr[Byte]]
    }

    val len = stackalloc[socket.socklen_t]
    !len = anOption match {
      case SocketOptions.SO_LINGER       => sizeof[socket.linger].toUInt
      case SocketOptions.IP_MULTICAST_IF => sizeof[in.sockaddr_in].toUInt
      case _                             => sizeof[CInt].toUInt
    }

    val result = socket.getsockopt(fd.fd, level, optValue, opt, len)
    if (result != 0) {
      throw new SocketException(fromCString(string.strerror(errno.errno)))
    }

    anOption match {
      case SocketOptions.SO_LINGER => {
        val linger = opt.cast[Ptr[socket.linger]]
        if (linger.l_onoff != 0) {
          Integer.valueOf(linger.l_linger)
        } else {
          Integer.valueOf(-1)
        }
      }
      case SocketOptions.TCP_NODELAY => {
        if (((anOption >> 16) & BROKEN_TCP_NODELAY) != 0)
          null
        else
          Boolean.box(!(opt.cast[Ptr[CInt]]) != 0)
      }
      case IP_MULTICAST_TTL => {
        if (((anOption >> 16) & BROKEN_MULTICAST_TTL) != 0)
          Byte.box(0)
        else
          Byte.box(!(opt.cast[Ptr[Byte]]))
      }
      case SocketOptions.IP_MULTICAST_IF => {
        if (((anOption >> 16) & BROKEN_MULTICAST_IF) != 0) {
          null
        } else {
          val sin = opt.cast[Ptr[socket.sockaddr]]
          getAddressFromBytes(sin.sa_family, sin)
        }
      }
      case SocketOptions.SO_BROADCAST | SocketOptions.SO_REUSEADDR |
          SocketOptions.SO_KEEPALIVE | SocketOptions.SO_OOBINLINE |
          SocketOptions.IP_MULTICAST_LOOP =>
        Boolean.box(!(opt.cast[Ptr[CInt]]) != 0)
      case _ =>
        Integer.valueOf(!(opt.cast[Ptr[CInt]]))
    }
  }

  private def nativeValueFromOption(option: Int) = option match {
    case SocketOptions.IP_TOS            => in.IP_TOS
    case SocketOptions.SO_KEEPALIVE      => socket.SO_KEEPALIVE
    case SocketOptions.SO_LINGER         => socket.SO_LINGER
    case SocketOptions.SO_TIMEOUT        => socket.SO_RCVTIMEO
    case SocketOptions.SO_OOBINLINE      => socket.SO_OOBINLINE
    case SocketOptions.SO_RCVBUF         => socket.SO_RCVBUF
    case SocketOptions.SO_SNDBUF         => socket.SO_SNDBUF
    case SocketOptions.SO_REUSEADDR      => socket.SO_REUSEADDR
    case SocketOptions.SO_BROADCAST      => socket.SO_BROADCAST
    case SocketOptions.TCP_NODELAY       => tcp.TCP_NODELAY
    case SocketOptions.IP_MULTICAST_IF   => in.IP_MULTICAST_IF
    case SocketOptions.IP_MULTICAST_IF2  => in.IPV6_MULTICAST_IF
    case SocketOptions.IP_MULTICAST_LOOP => in.IP_MULTICAST_LOOP
    case IP_MULTICAST_TTL                => in.IP_MULTICAST_TTL
    case IP_MULTICAST_ADD                => in.IP_ADD_MEMBERSHIP
    case IP_MULTICAST_DROP               => in.IP_DROP_MEMBERSHIP
    case _                               => sys.error(s"Unknown option: $option")
  }

  def getOption(optID: Int): Object = optID match {
    case SocketOptions.SO_TIMEOUT => Int.box(receiveTimeout)
    case SocketOptions.IP_TOS     => Int.box(trafficClass)
    case _ => {
      val result: Object = getSocketOption(optID)
      if (optID == SocketOptions.IP_MULTICAST_IF) {
        try {
          InetAddress.getByAddress(ipaddress)
        } catch {
          case e: UnknownHostException => null
        }
      }
      result
    }
  }

  protected[net] def getTTL(): Byte = {
    Byte.box(getOption(IP_MULTICAST_TTL).asInstanceOf[Byte])
  }

  def getTimeToLive(): Int = {
    Byte.box(getOption(IP_MULTICAST_TTL).asInstanceOf[Byte]) & 0xFF
  }

  def join(addr: InetAddress): Unit =
    setOption(IP_MULTICAST_ADD, addr)

  def joinGroup(addr: SocketAddress, netInterface: NetworkInterface): Unit = {
    if (addr.isInstanceOf[InetSocketAddress]) {
      val groupAddr = addr.asInstanceOf[InetSocketAddress].getAddress
      setOption(IP_MULTICAST_ADD, addr)
    }
  }

  def leave(addr: InetAddress): Unit =
    setOption(IP_MULTICAST_DROP, addr)

  def leaveGroup(addr: SocketAddress, netInterface: NetworkInterface): Unit = {
    if (addr.isInstanceOf[InetSocketAddress]) {
      val groupAddr = addr.asInstanceOf[InetSocketAddress].getAddress
      setOption(IP_MULTICAST_DROP, addr)
    }
  }

  private def selectRead(fd: FileDescriptor, timeout: Int): Unit = {
    if (timeout > 0) {
      val fdset = stackalloc[fd_set]
      !fdset._1 = stackalloc[CLongInt](FD_SETSIZE / (8 * sizeof[CLongInt]))
      FD_ZERO(fdset)
      FD_SET(fd.fd, fdset)

      val time = stackalloc[timeval]
      time.tv_sec = timeout / 1000
      time.tv_usec = (timeout % 1000) * 1000

      val selectRes = select(fd.fd + 1, fdset, null, null, time)
      selectRes match {
        case 0 =>
          throw new SocketTimeoutException(
            "Accept timed out, "
              + "SO_TIMEOUT was set to: " + timeout)
        case -1 =>
          throw new SocketException(fromCString(string.strerror(errno.errno)))
        case _ => {}
      }
    }
  }

  private def recvConnectedDatagram(packet: DatagramPacket,
                                    peek: Boolean): Int = {
    val localCount = if (packet.length < 65536) packet.length else 65536
    Zone { implicit z =>
      val message = alloc[Byte](localCount)
      var flags   = 0
      if (!fd.valid()) {
        throw new SocketException("Bad Socket.")
      }
      selectRead(fd, receiveTimeout)

      if (peek) {
        flags = socket.MSG_PEEK
      }
      val res = socket.recv(fd.fd, message, localCount, flags).toInt

      if (res < 0 || errno.errno == ECONNREFUSED) {
        throw new PortUnreachableException(
          fromCString(string.strerror(errno.errno)))
      } else if (res < 0) {
        throw new SocketException(fromCString(string.strerror(errno.errno)))
      }

      packet.length = res.toInt

      if (res > 0) {
        for (i <- 0 until localCount) {
          packet.data(i) = !(message + i)
        }
      }
      res.toInt
    }
  }

  private def getAddressFromBytes(family: UShort,
                                  sin: Ptr[socket.sockaddr]): InetAddress = {
    if (sin.sa_family == socket.AF_INET.toUShort) {
      val addr4     = sin.cast[Ptr[in.sockaddr_in]]
      val addr4in   = addr4.sin_addr.in_addr
      val addrBytes = Array.fill[Byte](4)(0)
      for (i <- 3 to 0 by -1) {
        addrBytes(i) = (addr4in >> i * 8).toByte
      }
      new Inet4Address(addrBytes)
    } else {
      val addr6     = sin.cast[Ptr[in.sockaddr_in6]]
      val addr6in   = addr6.sin6_addr
      val addrBytes = Array.fill[Byte](16)(0)
      for (i <- 0 until 16) {
        addrBytes(i) = (!((addr6in._1)._1 + i)).toByte
      }
      new Inet6Address(addrBytes)
    }
  }

  private def receiveDatagram(pack: DatagramPacket, peek: Boolean): Int = {
    val len = stackalloc[socket.socklen_t]
    !len = sizeof[in.sockaddr_in6].toUInt
    val sin = stackalloc[socket.sockaddr]

    val localCount = if (pack.length < 65536) pack.length else 65536
    val message    = stackalloc[Byte](localCount)
    if (!fd.valid()) {
      throw new SocketException("Bad Socket.")
    }
    selectRead(fd, receiveTimeout)
    var flags = 0
    if (peek) {
      flags = socket.MSG_PEEK
    }
    val result =
      socket.recvfrom(fd.fd, message, localCount, flags, sin, len).toInt

    pack.port = if (!sin._1 == socket.AF_INET.toUShort) {
      val addr4 = sin.cast[Ptr[in.sockaddr_in]]
      inet.ntohs(!addr4._2).toInt
    } else {
      val addr6 = sin.cast[Ptr[in.sockaddr_in6]]
      inet.ntohs(!(addr6 + 2).cast[Ptr[in.in_port_t]]).toInt
    }
    pack.setAddress(getAddressFromBytes(sin.sa_family, sin))

    pack.length = result.toInt

    if (result > 0) {
      for (i <- 0 until localCount) {
        pack.data(i) = !(message + i)
      }
    }
    result.toInt
  }

  protected[net] def peek(sender: InetAddress): Int = {
    if (isNativeConnected) {
      val storageArray = Array.fill[Byte](10)(0)
      val pack         = new DatagramPacket(storageArray, storageArray.size)

      recvConnectedDatagram(pack, true)

      for (i <- 0 until connectedAddress.ipAddress.size) {
        sender.ipAddress(i) = connectedAddress.ipAddress(i)
      }
      connectedPort
    } else {
      val len = stackalloc[socket.socklen_t]
      !len = sizeof[in.sockaddr_in6].toUInt
      val sin = stackalloc[socket.sockaddr]

      Zone { implicit z =>
        val message = alloc[Byte](1)
        if (!fd.valid()) {
          throw new SocketException("Bad Socket.")
        }
        selectRead(fd, receiveTimeout)
        val result =
          socket.recvfrom(fd.fd, message, 1, socket.MSG_PEEK, sin, len).toInt

        if (result < 0) {
          throw new SocketException(fromCString(string.strerror(errno.errno)))
        }
      }

      val port = if (!sin._1 != socket.AF_INET.toUShort) {
        val addr4   = sin.cast[Ptr[in.sockaddr_in]]
        val addr4in = addr4.sin_addr.in_addr
        for (i <- 3 to 0 by -1) {
          sender.ipAddress(i) = (addr4in >> i * 8).toByte
        }
        inet.ntohs(!addr4._2)
      } else {
        val addr6   = sin.cast[Ptr[in.sockaddr_in6]]
        val addr6in = addr6.sin6_addr
        for (i <- 0 until 16) {
          sender.ipAddress(i) = (!((addr6in._1)._1 + i)).toByte
        }
        inet.ntohs(!(addr6 + 2).cast[Ptr[in.in_port_t]])
      }
      port.toInt
    }
  }

  def receive(pack: DatagramPacket): Unit = {
    try {
      if (isNativeConnected) {
        recvConnectedDatagram(pack, false)
        updatePacketRcvAddress(pack)
      } else {
        receiveDatagram(pack, false)
      }
    } catch {
      case e: InterruptedIOException =>
        throw new SocketTimeoutException(e.getMessage())
    }
  }

  private def sendMsgConn(fd: FileDescriptor,
                          msg: Ptr[Byte],
                          sent: Int,
                          length: Int,
                          flags: Int): Int = {
    if (!fd.valid()) {
      if (sent == 0) {
        throw new SocketException("Bad Socket.")
      } else {
        throw new InterruptedIOException("The call was cancelled.")
      }
    }
    val result = socket.send(fd.fd, (msg + sent), length, flags).toInt
    if (result < 0) {
      result
    } else {
      val newLength = length - result.toInt
      val newSent   = sent + result.toInt
      if (newLength > 0)
        sendMsgConn(fd, msg, newSent, newLength, flags)
      else
        newSent
    }
  }

  private def sendMsg(fd: FileDescriptor,
                      msg: Ptr[Byte],
                      sent: Int,
                      length: Int,
                      flags: Int,
                      destAddr: Ptr[socket.sockaddr],
                      addrlen: socket.socklen_t): Int = {
    val result =
      socket.sendto(fd.fd, (msg + sent), length, flags, destAddr, addrlen).toInt
    if (result < 0) {
      result
    } else {
      val newLength = length - result.toInt
      val newSent   = sent + result.toInt
      if (newLength > 0)
        sendMsg(fd, msg, newSent, newLength, flags, destAddr, addrlen).toInt
      else
        newSent
    }
  }

  def getSockAddr(address: InetAddress, port: Int): Ptr[socket.sockaddr] =
    address match {
      case in4: Inet4Address => {
        val in4addr =
          stdlib.malloc(sizeof[in.sockaddr_in]).cast[Ptr[in.sockaddr_in]]
        in4addr.sin_family = socket.AF_INET.toUShort
        in4addr.sin_port = inet.htons(port.toUShort)
        val in4addr_b = in4addr.sin_addr
        in4addr_b.in_addr = 0.toUInt
        for (i <- 0 until 4) {
          in4addr_b.in_addr = in4addr_b.in_addr | (in4
            .ipAddress(i)
            .toUByte << (i * 8))
        }
        in4addr.cast[Ptr[socket.sockaddr]]
      }
      case in6: Inet6Address => {
        val in6addr =
          stdlib.malloc(sizeof[in.sockaddr_in6]).cast[Ptr[in.sockaddr_in6]]
        in6addr.sin6_family = socket.AF_INET6.toUShort
        in6addr.sin6_port = inet.htons(port.toUShort)
        in6addr.sin6_flowinfo = ((trafficClass & 0xFF) << 20).toUShort
        val in6addr_b = in6addr.sin6_addr
        for (i <- 0 until 16) {
          !(in6addr_b._1._1 + i) = in6.ipAddress(i).toUByte
        }
        in6addr.sin6_scope_id = 0.toUInt
        in6addr.cast[Ptr[socket.sockaddr]]
      }
    }

  def send(pack: DatagramPacket): Unit = {
    Zone { implicit z =>
      val message = alloc[Byte](pack.length)
      for (i <- 0 until pack.length) {
        !(message + i) = pack.data(i)
      }
      if (isNativeConnected) {
        val result = sendMsgConn(fd, message, 0, pack.length, 0)

        if (result < 0 || errno.errno == ECONNREFUSED) {
          throw new PortUnreachableException(
            fromCString(string.strerror(errno.errno)))
        } else if (result < 0) {
          throw new SocketException(fromCString(string.strerror(errno.errno)))
        }
      } else {
        val sockaddr: Ptr[socket.sockaddr] =
          getSockAddr(pack.address, pack.port)
        val addrLen = pack.address match {
          case in4: Inet4Address => sizeof[in.sockaddr_in]
          case in6: Inet6Address => sizeof[in.sockaddr_in6]
        }
        if (!fd.valid()) {
          throw new SocketException("Bad socket.")
        }
        val result =
          sendMsg(fd, message, 0, pack.length, 0, sockaddr, addrLen.toUInt)
        stdlib.free(sockaddr.cast[Ptr[Byte]])

        if (result < 0) {
          throw new SocketException(fromCString(string.strerror(errno.errno)))
        }
        result
      }
    }
  }

  private def setSocketOption(anOption: Int, value: Object): Unit = {
    if (!fd.valid()) {
      throw new SocketException("Bad socket.")
    }

    val level = anOption match {
      case SocketOptions.IP_TOS | IP_MULTICAST_ADD | IP_MULTICAST_DROP |
          IP_MULTICAST_TTL | SocketOptions.IP_MULTICAST_IF |
          SocketOptions.IP_MULTICAST_LOOP =>
        in.IPPROTO_IP
      case SocketOptions.IP_MULTICAST_IF2 => in.IPPROTO_IPV6
      case SocketOptions.TCP_NODELAY      => in.IPPROTO_TCP
      case _                              => socket.SOL_SOCKET
    }
    val optValue = nativeValueFromOption(anOption)

    val len: UInt = anOption match {
      case SocketOptions.SO_LINGER              => sizeof[socket.linger].toUInt
      case SocketOptions.SO_TIMEOUT             => sizeof[timeval].toUInt
      case SocketOptions.IP_MULTICAST_IF        => sizeof[in.sockaddr_in].toUInt
      case IP_MULTICAST_ADD | IP_MULTICAST_DROP => sizeof[in.ip_mreq].toUInt
      case _                                    => sizeof[CInt].toUInt
    }

    val opt: Ptr[Byte] = anOption match {
      case SocketOptions.SO_LINGER => {
        val ptr    = stackalloc[socket.linger]
        val linger = value.asInstanceOf[Int]

        ptr.l_onoff = if (linger == -1) 0 else 1
        ptr.l_linger = linger
        ptr.cast[Ptr[Byte]]
      }
      case SocketOptions.TCP_NODELAY => {
        if (((anOption >> 16) & BROKEN_TCP_NODELAY) != 0) {
          val ptr = stackalloc[CInt]
          !ptr = if (value.asInstanceOf[Boolean]) 1 else 0
          ptr.cast[Ptr[Byte]]
        } else {
          return
        }
      }
      case IP_MULTICAST_TTL => {
        if (((anOption >> 16) & BROKEN_TCP_NODELAY) != 0) {
          val ptr = stackalloc[CInt]
          !ptr = value.asInstanceOf[Int]
          ptr.cast[Ptr[Byte]]
        } else {
          return
        }
      }
      case IP_MULTICAST_ADD => {
        val ptr  = stackalloc[in.ip_mreq]
        val addr = value.asInstanceOf[Inet4Address]
        for (i <- 0 until 4) {
          ptr.imr_multiaddr.in_addr = ptr.imr_multiaddr.in_addr | (addr
            .ipAddress(i)
            .toUByte << (i * 8))
        }
        ptr.imr_interface.in_addr = 0.toUInt
        ptr.cast[Ptr[Byte]]
      }
      case IP_MULTICAST_DROP => {
        val ptr  = stackalloc[in.ip_mreq]
        val addr = value.asInstanceOf[Inet4Address]
        for (i <- 0 until 4) {
          ptr.imr_multiaddr.in_addr = ptr.imr_multiaddr.in_addr | (addr
            .ipAddress(i)
            .toUByte << (i * 8))
        }
        ptr.imr_interface.in_addr = 0.toUInt
        ptr.cast[Ptr[Byte]]
      }
      case SocketOptions.IP_MULTICAST_IF => {
        val ptr = stackalloc[in.sockaddr_in]
        ptr.sin_family = socket.AF_INET.toUShort
        ptr.sin_port = 0.toUShort
        val addr = value.asInstanceOf[Inet4Address]
        for (i <- 0 until 4) {
          ptr.sin_addr.in_addr = ptr.sin_addr.in_addr | (addr
            .ipAddress(i)
            .toUByte << (i * 8))
        }
        ptr.cast[Ptr[Byte]]
      }

      case SocketOptions.SO_BROADCAST | SocketOptions.SO_REUSEADDR |
          SocketOptions.SO_KEEPALIVE | SocketOptions.SO_OOBINLINE |
          SocketOptions.IP_MULTICAST_LOOP => {
        val ptr = stackalloc[CInt]
        !ptr = if (value.asInstanceOf[Boolean]) 1 else 0
        ptr.cast[Ptr[Byte]]
      }

      case _ => {
        val ptr = stackalloc[CInt]
        !ptr = value.asInstanceOf[Int]
        ptr.cast[Ptr[Byte]]
      }
    }

    if (socket.setsockopt(fd.fd, level, optValue, opt, len) != 0) {
      throw new SocketException(fromCString(string.strerror(errno.errno)))
    }
  }

  def setOption(optionID: Int, value: Object): Unit = {
    var optID = optionID

    if (optID == SocketOptions.SO_TIMEOUT) {
      receiveTimeout = Int.unbox(value)
    } else {
      try {
        setSocketOption(optID, value)
      } catch {
        case e: SocketException =>
          if (optID != SocketOptions.IP_TOS) {
            throw e
          }
      }
      if (optID == SocketOptions.IP_MULTICAST_IF) {
        val inet = value.asInstanceOf[InetAddress]
        if (InetAddress.bytesToInt(inet.getAddress(), 0) == 0 || inet
              .isLoopbackAddress()) {
          ipaddress = value.asInstanceOf[InetAddress].getAddress()
        } else {
          var local: InetAddress = null
          try {
            local = InetAddress.getLocalHost()
          } catch {
            case e: UnknownHostException =>
              throw new SocketException("getLocalHost():" + e.toString())
          }
          if (inet == local)
            ipaddress = value.asInstanceOf[InetAddress].getAddress()
          else
            throw new SocketException(value + " != getLocalHost() : " + local)
        }
      }
      if (optID == SocketOptions.IP_TOS) {
        trafficClass = Int.unbox(value)
      }
    }
  }

  protected[net] def setTimeToLive(ttl: Int): Unit = {
    setOption(IP_MULTICAST_TTL, Byte.box((ttl & 0xFF).asInstanceOf[Byte]))
    this.ttl = ttl
  }

  protected[net] def setTTL(ttl: Byte): Unit = {
    setOption(IP_MULTICAST_TTL, Byte.box(ttl))
    this.ttl = ttl
  }

  override def connect(inetAddr: InetAddress, port: Int): Unit = {
    if (!fd.valid()) {
      throw new SocketException("Bad Socket.")
    }
    val sockAddr = getSockAddr(inetAddr, port)
    val addrLen = inetAddr match {
      case in4: Inet4Address => sizeof[in.sockaddr_in].toUInt
      case in6: Inet6Address => sizeof[in.sockaddr_in].toUInt
    }
    val result = socket.connect(fd.fd, sockAddr, addrLen)
    if (result != 0) {
      throw new ConnectException(fromCString(string.strerror(errno.errno)))
    }
    stdlib.free(sockAddr.cast[Ptr[Byte]])

    try {
      connectedAddress = InetAddress.getByAddress(inetAddr.getAddress)
    } catch {
      case e: UnknownHostException =>
        val hostName = inetAddr.getHostName()
        throw new SocketException(s"Host is unresolved\: $hostName")
    }
    connectedPort = port
    isNativeConnected = true
  }

  override def disconnect(): Unit = {
    try {
      if (!fd.valid()) {
        throw new SocketException("Bad Socket.")
      }
      val sockAddr = stackalloc[socket.sockaddr]
      !sockAddr._1 = socket.AF_UNSPEC.toUShort
      val addrLen = sizeof[in.sockaddr_in].toUInt
      val result  = socket.connect(fd.fd, sockAddr, addrLen)
      if (result != 0) {
        throw new SocketException(fromCString(string.strerror(errno.errno)))
      }
    } catch {
      case e: Exception => // eat exception
    }
    connectedPort = -1
    connectedAddress = null
    isNativeConnected = false
  }

  def peekData(pack: DatagramPacket): Int = {
    try {
      if (isNativeConnected) {
        recvConnectedDatagram(pack, true)
        updatePacketRcvAddress(pack)
      } else {
        receiveDatagram(pack, true)
      }
    } catch {
      case e: InterruptedIOException =>
        throw new SocketTimeoutException(e.getMessage())
    }
    pack.getPort()
  }

  private def updatePacketRcvAddress(packet: DatagramPacket): Unit = {
    packet.setAddress(connectedAddress)
    packet.setPort(connectedPort)
  }
}

object PlainDatagramSocketImpl {
  private[net] final val IP_MULTICAST_ADD  = 19
  private[net] final val IP_MULTICAST_DROP = 20
  private[net] final val IP_MULTICAST_TTL  = 17

  private final val BROKEN_MULTICAST_IF       = 1
  private final val BROKEN_MULTICAST_TTL      = 2
  private final val BROKEN_TCP_NODELAY        = 4
}
