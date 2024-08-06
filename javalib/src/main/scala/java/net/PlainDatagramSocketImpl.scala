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

private[net] class PlainDatagramSocketImpl extends DatagramSocketImpl {
  import PlainDatagramSocketImpl._

  protected[net] var fd = new FileDescriptor
  protected[net] var localport = 0
  protected[net] var remoteAddress: SocketAddress = _
  protected[net] var connectedAddress: SocketAddress = _

  protected[net] var socket: DatagramSocket = _

  private final val family = Net.getGaiHintsProtocolFamily()
  protected[net] var timeout = 0

  override def getFileDescriptor(): FileDescriptor = fd

  override def setDatagramSocket(socket: DatagramSocket): Unit =
    this.socket = socket

  override def getDatagramSocket(): DatagramSocket =
    socket

  final protected var isClosed: Boolean =
    fd == InvalidSocketDescriptor

  @inline private def throwIfClosed(methodName: String): Unit =
    if (isClosed) throw new SocketException(s"$methodName: Socket is closed")

  override def bind(port: Int, laddr: InetAddress): Unit = {
    throwIfClosed("bind")
    Net.bind(fd, family, new InetSocketAddress(laddr, port))
    localport = Net.localAddress(fd, family).getPort
  }

  override def create(): Unit = {
    val s = Net.socket(family, stream = false)

    // enable broadcast by default
    val broadcastPrt = stackalloc[CInt]()
    !broadcastPrt = 1
    if (posix.sys.socket.setsockopt(
          s.fd,
          posix.sys.socket.SOL_SOCKET,
          posix.sys.socket.SO_BROADCAST,
          broadcastPrt.asInstanceOf[Ptr[Byte]],
          sizeof[CInt].toUInt
        ) < 0) {
      Net.close(s)
      throw new IOException(s"Could not set SO_BROADCAST on socket: $errno")
    }
    fd = s
  }

  override def send(p: DatagramPacket): Unit = {
    throwIfClosed("send")
    val insAddr = p.getSocketAddress().asInstanceOf[InetSocketAddress]
    val ret = Zone.acquire { implicit z =>
      val (sa, saLen) = Net.prepareSockaddrIn(family, insAddr)

      val buffer = p.getData()
      val cArr = buffer.at(p.getOffset())
      val len = p.getLength()
      posix.sys.socket.sendto(
        fd.fd,
        cArr,
        len.toUInt,
        posix.sys.socket.MSG_NOSIGNAL,
        sa,
        saLen
      )
    }

    if (ret < 0) {
      throw new IOException("Could not send the datagram packet to the client")
    }
  }

  override def connect(address: InetAddress, port: Int): Unit = {
    throwIfClosed("connect")
    val sa = new InetSocketAddress(address, port)
    Net.connect(fd, family, sa)
    connectedAddress = sa
  }

  override def disconnect(): Unit = {
    throwIfClosed("disconnect")
    Net.disconnect(fd)
    connectedAddress = null
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
    if (timeout > 0) Net.tryPoll(fd, timeout, op)

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
        throw new SocketException(s"$op failed, errno: ${lastError()}")
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
      if (family == StandardProtocolFamily.INET) ip.IP_MULTICAST_TTL
      else ip6.IPV6_MULTICAST_HOPS
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
      if (family == StandardProtocolFamily.INET) ip.IP_MULTICAST_TTL
      else ip6.IPV6_MULTICAST_HOPS
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

  override def join(inetaddr: InetAddress): Unit = {
    throwIfClosed("join")
    Net.join(fd, family, inetaddr, null, null)
  }

  override def leave(inetaddr: InetAddress): Unit = {
    throwIfClosed("leave")
    Net.drop(fd, family, inetaddr, null, null)
  }

  override def joinGroup(
      mcastaddr: SocketAddress,
      netIf: NetworkInterface
  ): Unit = {
    throwIfClosed("joinGroup")
    mcastaddr match {
      case inetaddr: InetSocketAddress =>
        val addr = inetaddr.getAddress
        Net.join(fd, family, addr, null, null)
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
        val addr = inetaddr.getAddress
        Net.drop(fd, family, addr, null, null)
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

private[net] object PlainDatagramSocketImpl {
  final val InvalidSocketDescriptor = new FileDescriptor()
  def apply(): PlainDatagramSocketImpl = new PlainDatagramSocketImpl()
}
