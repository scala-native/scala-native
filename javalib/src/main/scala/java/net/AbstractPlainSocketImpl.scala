package java.net

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._

import scalanative.libc.string.memcpy

import scala.scalanative.posix.arpa.inet
// Import posix name errno as variable, not class or type.
import scala.scalanative.posix.{errno => posixErrno}, posixErrno._
import scala.scalanative.posix.netinet.in
import scala.scalanative.posix.netinet.inOps._
import scala.scalanative.posix.netinet.tcp
import scala.scalanative.posix.netdb._
import scala.scalanative.posix.netdbOps._
import scala.scalanative.posix.string.strerror
import scala.scalanative.posix.sys.ioctl._
import scala.scalanative.posix.sys.socket
import scala.scalanative.posix.sys.socketOps._
import scala.scalanative.posix.sys.time._
import scala.scalanative.posix.sys.timeOps._
import scala.scalanative.posix.unistd

import scala.scalanative.meta.LinktimeInfo.isWindows
import java.io.{FileDescriptor, IOException, OutputStream, InputStream}
import scala.scalanative.windows._
import scala.scalanative.windows.WinSocketApi._
import scala.scalanative.windows.WinSocketApiExt._

private[net] abstract class AbstractPlainSocketImpl extends SocketImpl {
  import AbstractPlainSocketImpl._

  protected def setSocketFdBlocking(fd: FileDescriptor, blocking: Boolean): Unit
  protected def tryPollOnConnect(timeout: Int): Unit
  protected def tryPollOnAccept(): Unit

  protected[net] var fd = new FileDescriptor
  protected[net] var localport = 0
  protected[net] var address: InetAddress = null
  protected[net] var port = 0

  protected var timeout = 0
  private var listening = false

  private final val useIPv4Only = SocketHelpers.getUseIPv4Stack()

  override def getInetAddress: InetAddress = address
  override def getFileDescriptor: FileDescriptor = fd
  final protected var isClosed: Boolean =
    fd == InvalidSocketDescriptor

  private def throwIfClosed(methodName: String): Unit = {
    if (isClosed) {
      throw new SocketException(s"$methodName: Socket is closed")
    }
  }

  private def throwCannotBind(addr: InetAddress): Nothing = {
    throw new BindException(
      "Couldn't bind to an address: " + addr.getHostAddress() +
        " on port: " + port.toString
    )
  }

  private def fetchLocalPort(family: Int): Option[Int] = {
    val len = stackalloc[socket.socklen_t]()
    val portOpt = if (family == socket.AF_INET) {
      val sin = stackalloc[in.sockaddr_in]()
      !len = sizeof[in.sockaddr_in].toUInt

      if (socket.getsockname(
            fd.fd,
            sin.asInstanceOf[Ptr[socket.sockaddr]],
            len
          ) == -1) {
        None
      } else {
        Some(sin.sin_port)
      }
    } else {
      val sin = stackalloc[in.sockaddr_in6]()
      !len = sizeof[in.sockaddr_in6].toUInt

      if (socket.getsockname(
            fd.fd,
            sin.asInstanceOf[Ptr[socket.sockaddr]],
            len
          ) == -1) {
        None
      } else {
        Some(sin.sin6_port)
      }
    }

    portOpt.map(inet.ntohs(_).toInt)
  }

  /* Fill in the given sockaddr_in6 with the given InetAddress, either
   * Inet4Address or Inet6Address, and the given port.
   * Set the af_family for IPv6.  On return, the sockaddr_in6 should
   * be ready to use in bind() or connect().
   *
   * By contract, all the bytes in sa6 are zero coming in.
   */
  private def prepareSockaddrIn6(
      inetAddress: InetAddress,
      port: Int,
      sa6: Ptr[in.sockaddr_in6]
  ): Unit = {

    /* BEWARE: This is Unix-only code.
     *   Currently (2022-08-27) execution on Windows never get here. IPv4Only
     *   is forced on.  If that ever changes, this method may need
     *   Windows code.
     *
     *   Having the complexity in one place, it should make adding
     *   Windows support easier.
     */

    sa6.sin6_family = socket.AF_INET6.toUShort
    sa6.sin6_port = inet.htons(port.toUShort)

    val src = inetAddress.getAddress()

    if (inetAddress.isInstanceOf[Inet6Address]) {
      val from = src.asInstanceOf[scala.scalanative.runtime.Array[Byte]].at(0)
      val dst = sa6.sin6_addr.at1.at(0).asInstanceOf[Ptr[Byte]]
      memcpy(dst, from, 16.toUInt)
    } else { // Use IPv4mappedIPv6 address
      val dst = sa6.sin6_addr.toPtr.s6_addr

      // By contract, the leading bytes are already zero already.
      val FF = 255.toUByte
      dst(10) = FF // set the IPv4mappedIPv6 indicator bytes
      dst(11) = FF

      // add the IPv4 trailing bytes, unrolling small loop
      dst(12) = src(0).toUByte
      dst(13) = src(1).toUByte
      dst(14) = src(2).toUByte
      dst(15) = src(3).toUByte
    }
  }

  private def bind4(addr: InetAddress, port: Int): Unit = {
    val hints = stackalloc[addrinfo]()
    val ret = stackalloc[Ptr[addrinfo]]()
    hints.ai_family = socket.AF_UNSPEC
    hints.ai_flags = AI_NUMERICHOST
    hints.ai_socktype = socket.SOCK_STREAM

    Zone { implicit z =>
      val cIP = toCString(addr.getHostAddress())
      if (getaddrinfo(cIP, toCString(port.toString), hints, ret) != 0) {
        throw new BindException(
          "Couldn't resolve address: " + addr.getHostAddress()
        )
      }
    }

    val bindRes = socket.bind(fd.fd, (!ret).ai_addr, (!ret).ai_addrlen)

    val family = (!ret).ai_family
    freeaddrinfo(!ret)

    if (bindRes < 0) {
      throwCannotBind(addr)
    }

    this.localport = fetchLocalPort(family).getOrElse {
      throwCannotBind(addr)
    }
  }

  private def bind6(addr: InetAddress, port: Int): Unit = {
    val sa6 = stackalloc[in.sockaddr_in6]()

    // By contract, all the bytes in sa6 are zero going in.
    prepareSockaddrIn6(addr, port, sa6)

    val bindRes = socket.bind(
      fd.fd,
      sa6.asInstanceOf[Ptr[socket.sockaddr]],
      sizeof[in.sockaddr_in6].toUInt
    )

    if (bindRes < 0)
      throwCannotBind(addr)

    this.localport = fetchLocalPort(sa6.sin6_family.toInt).getOrElse {
      throwCannotBind(addr)
    }
  }

  private lazy val bindFunc =
    if (useIPv4Only) bind4(_: InetAddress, _: Int)
    else bind6(_: InetAddress, _: Int)

  override def bind(addr: InetAddress, port: Int): Unit = {
    throwIfClosed("bind")

    bindFunc(addr, port)
  }

  override def listen(backlog: Int): Unit = {
    if (socket.listen(fd.fd, backlog) == -1) {
      throw new SocketException("Listen failed")
    }
    listening = true
  }

  override def accept(s: SocketImpl): Unit = {
    throwIfClosed("accept") // Do not send negative fd.fd to poll()

    if (timeout > 0)
      tryPollOnAccept()

    val storage = stackalloc[Byte](sizeof[in.sockaddr_in6])
    val len = stackalloc[socket.socklen_t]()
    !len = sizeof[in.sockaddr_in6].toUInt

    val newFd =
      socket.accept(fd.fd, storage.asInstanceOf[Ptr[socket.sockaddr]], len)
    if (newFd == -1) {
      throw new SocketException("Accept failed")
    }
    val family =
      storage.asInstanceOf[Ptr[socket.sockaddr_storage]].ss_family.toInt
    val ipstr: Ptr[CChar] = stackalloc[CChar](in.INET6_ADDRSTRLEN.toUSize)

    val (srcPtr, port) = if (family == socket.AF_INET) {
      val sa = storage.asInstanceOf[Ptr[in.sockaddr_in]]
      val port = inet.ntohs(sa.sin_port).toInt
      (sa.sin_addr.at1.asInstanceOf[Ptr[Byte]], port)
    } else {
      val sa = storage.asInstanceOf[Ptr[in.sockaddr_in6]]
      val port = inet.ntohs(sa.sin6_port).toInt
      (sa.sin6_addr.at1.at(0).asInstanceOf[Ptr[Byte]], port)
    }

    val ret = inet.inet_ntop(
      family,
      srcPtr,
      ipstr,
      in.INET6_ADDRSTRLEN.toUInt
    )

    if (ret == null) {
      throw new IOException(
        s"inet_ntop failed: ${fromCString(strerror(errno))}"
      )
    }

    s.address = InetAddress.getByName(fromCString(ipstr))
    s.port = port
    s.localport = this.localport
    s.fd = new FileDescriptor(newFd)
  }

  override def connect(host: String, port: Int): Unit = {
    val addr = InetAddress.getByName(host)
    connect(addr, port)
  }

  override def connect(address: InetAddress, port: Int): Unit = {
    connect(new InetSocketAddress(address, port), 0)
  }

  private def connect4(address: SocketAddress, timeout: Int): Unit = {
    val inetAddr = address.asInstanceOf[InetSocketAddress]
    val hints = stackalloc[addrinfo]()
    val ret = stackalloc[Ptr[addrinfo]]()
    hints.ai_family = socket.AF_UNSPEC
    hints.ai_flags = AI_NUMERICHOST | AI_NUMERICSERV
    hints.ai_socktype = socket.SOCK_STREAM
    val remoteAddress = inetAddr.getAddress.getHostAddress()

    Zone { implicit z =>
      val cIP = toCString(remoteAddress)
      val cPort = toCString(inetAddr.getPort.toString)

      val retCode = getaddrinfo(cIP, cPort, hints, ret)

      if (retCode != 0) {
        throw new ConnectException(
          s"Could not resolve address: ${remoteAddress}"
            + s" on port: ${inetAddr.getPort}"
            + s" return code: ${retCode}"
        )
      }
    }

    val family = (!ret).ai_family
    if (timeout != 0)
      setSocketFdBlocking(fd, blocking = false)

    val connectRet = socket.connect(fd.fd, (!ret).ai_addr, (!ret).ai_addrlen)

    freeaddrinfo(!ret) // Must be after last use of ai_addr.

    if (connectRet < 0) {
      def inProgress = mapLastError(
        onUnix = _ == EINPROGRESS,
        onWindows = {
          case WSAEINPROGRESS | WSAEWOULDBLOCK => true
          case _                               => false
        }
      )
      if (timeout > 0 && inProgress) {
        tryPollOnConnect(timeout)
      } else {
        throw new ConnectException(
          s"Could not connect to address: ${remoteAddress}"
            + s" on port: ${inetAddr.getPort}"
            + s", errno: ${lastError()}"
        )
      }
    }

    this.address = inetAddr.getAddress
    this.port = inetAddr.getPort
    this.localport = fetchLocalPort(family).getOrElse {
      throw new ConnectException(
        "Could not resolve a local port when connecting"
      )
    }
  }

  private def connect6(address: SocketAddress, timeout: Int): Unit = {
    val insAddr = address.asInstanceOf[InetSocketAddress]

    val sa6 = stackalloc[in.sockaddr_in6]()

    // By contract, all the bytes in sa6 are zero going in.
    prepareSockaddrIn6(insAddr.getAddress, insAddr.getPort, sa6)

    if (timeout != 0)
      setSocketFdBlocking(fd, blocking = false)

    val connectRet = socket.connect(
      fd.fd,
      sa6.asInstanceOf[Ptr[socket.sockaddr]],
      sizeof[in.sockaddr_in6].toUInt
    )

    if (connectRet < 0) {
      def inProgress = mapLastError(
        onUnix = _ == EINPROGRESS,
        onWindows = {
          case WSAEINPROGRESS | WSAEWOULDBLOCK => true
          case _                               => false
        }
      )

      if (timeout > 0 && inProgress) {
        tryPollOnConnect(timeout)
      } else {
        val ra = insAddr.getAddress.getHostAddress()
        throw new ConnectException(
          s"Could not connect to address: ${ra}"
            + s" on port: ${insAddr.getPort}"
            + s", errno: ${lastError()}"
        )
      }
    }

    this.address = insAddr.getAddress
    this.port = insAddr.getPort
    this.localport = fetchLocalPort(sa6.sin6_family.toInt).getOrElse {
      throw new ConnectException(
        "Could not resolve a local port when connecting"
      )
    }
  }

  private lazy val connectFunc =
    if (useIPv4Only) connect4(_: SocketAddress, _: Int)
    else connect6(_: SocketAddress, _: Int)

  override def connect(address: SocketAddress, timeout: Int): Unit = {
    throwIfClosed("connect") // Do not send negative fd.fd to poll()

    connectFunc(address, timeout)
  }

  override def close(): Unit = {
    if (!isClosed) {
      if (isWindows) WinSocketApi.closeSocket(fd.handle)
      else unistd.close(fd.fd)
      fd = InvalidSocketDescriptor
      isClosed = true
    }
  }

  override def getOutputStream: OutputStream = {
    if (isClosed) {
      throw new SocketException("Socket is closed")
    }
    if (shutOutput) {
      throw new SocketException("Socket output is shutdown")
    }
    new SocketOutputStream(this)
  }

  override def getInputStream: InputStream = {
    if (isClosed) {
      throw new SocketException("Socket is closed")
    }
    if (shutInput) {
      throw new SocketException("Socket input is shutdown")
    }
    new SocketInputStream(this)
  }

  override def shutdownOutput(): Unit = {
    socket.shutdown(fd.fd, 1) match {
      case 0 => shutOutput = true
      case _ =>
        throw new SocketException("Error while shutting down socket's output")
    }
  }

  override def shutdownInput(): Unit = {
    socket.shutdown(fd.fd, 0) match {
      case 0 => shutInput = true
      case _ =>
        throw new SocketException("Error while shutting down socket's input")
    }
  }

  def write(buffer: Array[Byte], offset: Int, count: Int): Int = {
    if (shutOutput) {
      throw new IOException("Trying to write to a shut down socket")
    } else if (isClosed) {
      0
    } else {
      val cArr = buffer.at(offset)
      var sent = 0
      while (sent < count) {
        val ret = socket
          .send(fd.fd, cArr + sent, (count - sent).toUInt, socket.MSG_NOSIGNAL)
          .toInt
        if (ret < 0) {
          throw new IOException("Could not send the packet to the client")
        }
        sent += ret
      }
      sent
    }
  }

  def read(buffer: Array[Byte], offset: Int, count: Int): Int = {
    if (shutInput) -1
    else {
      val bytesNum = socket
        .recv(fd.fd, buffer.at(offset), count.toUInt, 0)
        .toInt

      def timeoutDetected = mapLastError(
        onUnix = { err => err == EAGAIN || err == EWOULDBLOCK },
        onWindows = { err => err == WSAEWOULDBLOCK || err == WSAETIMEDOUT }
      )

      bytesNum match {
        case _ if (bytesNum > 0) => bytesNum

        case 0 => if (count == 0) 0 else -1

        case _ if timeoutDetected =>
          throw new SocketTimeoutException("Socket timeout while reading data")

        case _ =>
          throw new SocketException(s"read failed, errno: ${lastError()}")
      }
    }
  }

  override def available(): Int = {
    if (shutInput) {
      0
    } else {
      val bytesAvailable = stackalloc[CInt]()
      ioctl(fd.fd, FIONREAD, bytesAvailable.asInstanceOf[Ptr[Byte]])
      !bytesAvailable match {
        case -1 =>
          throw new IOException(
            "Error while trying to estimate available bytes to read"
          )
        case x => x
      }
    }
  }

  // We can't directly map values in SocketOptions to the native ones,
  // because some of them have the same value, but require different levels
  // for example IP_TOS and TCP_NODELAY have the same value on my machine
  private def nativeValueFromOption(option: Int) = option match {
    case SocketOptions.IP_TOS =>
      SocketHelpers.getTrafficClassSocketOption()
    case SocketOptions.SO_KEEPALIVE => socket.SO_KEEPALIVE
    case SocketOptions.SO_LINGER    => socket.SO_LINGER
    case SocketOptions.SO_TIMEOUT   => socket.SO_RCVTIMEO
    case SocketOptions.SO_OOBINLINE => socket.SO_OOBINLINE
    case SocketOptions.SO_RCVBUF    => socket.SO_RCVBUF
    case SocketOptions.SO_SNDBUF    => socket.SO_SNDBUF
    case SocketOptions.SO_REUSEADDR => socket.SO_REUSEADDR
    case SocketOptions.TCP_NODELAY  => tcp.TCP_NODELAY
    case _                          => sys.error(s"Unknown option: $option")
  }

  override def getOption(optID: Int): Object = {
    if (isClosed) {
      throw new SocketException("Socket is closed")
    }

    if (listening && optID == SocketOptions.SO_TIMEOUT) {
      return Integer.valueOf(this.timeout)
    }

    val level = optID match {
      case SocketOptions.TCP_NODELAY => in.IPPROTO_TCP
      case SocketOptions.IP_TOS      => SocketHelpers.getIPPROTO()
      case _                         => socket.SOL_SOCKET
    }

    val optValue = nativeValueFromOption(optID)

    val opt = if (optID == SocketOptions.SO_LINGER) {
      stackalloc[socket.linger]().asInstanceOf[Ptr[Byte]]
    } else {
      stackalloc[CInt]().asInstanceOf[Ptr[Byte]]
    }

    val len = stackalloc[socket.socklen_t]()
    !len = if (optID == SocketOptions.SO_LINGER) {
      sizeof[socket.linger].toUInt
    } else {
      sizeof[CInt].toUInt
    }

    if (socket.getsockopt(fd.fd, level, optValue, opt, len) != 0) {
      throw new SocketException(
        "Exception while getting socket option with id: "
          + optValue + ", errno: " + lastError()
      )
    }

    optID match {
      case SocketOptions.TCP_NODELAY | SocketOptions.SO_KEEPALIVE |
          SocketOptions.SO_REUSEADDR | SocketOptions.SO_OOBINLINE =>
        Boolean.box(!(opt.asInstanceOf[Ptr[CInt]]) != 0)
      case SocketOptions.SO_LINGER =>
        val linger = opt.asInstanceOf[Ptr[socket.linger]]
        if (linger.l_onoff != 0) {
          Integer.valueOf(linger.l_linger)
        } else {
          Integer.valueOf(-1)
        }
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

    if (listening && optID == SocketOptions.SO_TIMEOUT) {
      this.timeout = value.asInstanceOf[Int]
      return
    }

    val level = optID match {
      case SocketOptions.IP_TOS      => SocketHelpers.getIPPROTO()
      case SocketOptions.TCP_NODELAY => in.IPPROTO_TCP
      case _                         => socket.SOL_SOCKET
    }
    val optValue = nativeValueFromOption(optID)

    val len = {
      optID match {
        case SocketOptions.SO_LINGER => sizeof[socket.linger]
        case SocketOptions.SO_TIMEOUT =>
          if (isWindows) sizeof[DWord]
          else sizeof[timeval]
        case _ => sizeof[CInt]
      }
    }.toUInt

    val opt = optID match {
      case SocketOptions.TCP_NODELAY | SocketOptions.SO_KEEPALIVE |
          SocketOptions.SO_REUSEADDR | SocketOptions.SO_OOBINLINE =>
        val ptr = stackalloc[CInt]()
        !ptr = if (value.asInstanceOf[Boolean]) 1 else 0
        ptr.asInstanceOf[Ptr[Byte]]
      case SocketOptions.SO_LINGER =>
        val ptr = stackalloc[socket.linger]()
        val linger = value.asInstanceOf[Int]
        if (linger == -1) {
          ptr.l_onoff = 0
          ptr.l_linger = 0
        } else {
          ptr.l_onoff = 1
          ptr.l_linger = linger
        }

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

    if (socket.setsockopt(fd.fd, level, optValue, opt, len) != 0) {
      throw new SocketException(
        "Exception while setting socket option with id: "
          + optID + ", errno: " + lastError()
      )
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

private[net] object AbstractPlainSocketImpl {
  final val InvalidSocketDescriptor = new FileDescriptor()

  def apply(): AbstractPlainSocketImpl = {
    if (isWindows)
      new WindowsPlainSocketImpl()
    else
      new UnixPlainSocketImpl()
  }
}
