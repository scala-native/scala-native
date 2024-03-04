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
  protected def tryPollOnConnect(timeout: Int): Unit
  protected def tryPollOnAccept(): Unit

  protected[net] var fd = new FileDescriptor
  protected[net] var localport = 0
  protected[net] var address: InetAddress = _
  protected[net] var port = 0

  protected var timeout = 0
  private var listening = false

  private final val useIPv4Only = SocketHelpers.getUseIPv4Stack()

  override def getInetAddress: InetAddress = address
  override def getFileDescriptor: FileDescriptor = fd

  final protected var isClosed: Boolean =
    fd == InvalidSocketDescriptor

  @inline private def throwIfClosed(methodName: String): Unit =
    if (isClosed) throw new SocketException(s"$methodName: Socket is closed")

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

  private def bind4(addr: InetAddress, port: Int): Unit = {
    val sa4 = stackalloc[in.sockaddr_in]()
    val sa4Len = sizeof[in.sockaddr_in].toUInt
    SocketHelpers.prepareSockaddrIn4(addr, port, sa4)

    val bindRes = socket.bind(
      fd.fd,
      sa4.asInstanceOf[Ptr[socket.sockaddr]],
      sa4Len
    )

    if (bindRes < 0)
      throwCannotBind(addr)

    this.localport = fetchLocalPort(socket.AF_INET).getOrElse {
      throwCannotBind(addr)
    }
  }

  private def bind6(addr: InetAddress, port: Int): Unit = {
    val sa6 = stackalloc[in.sockaddr_in6]()
    val sa6Len = sizeof[in.sockaddr_in6].toUInt

    // By contract, all the bytes in sa6 are zero going in.
    SocketHelpers.prepareSockaddrIn6(addr, port, sa6)

    val bindRes = socket.bind(
      fd.fd,
      sa6.asInstanceOf[Ptr[socket.sockaddr]],
      sa6Len
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

  override def create(stream: Boolean): Unit = {
    val family = Net.getGaiHintsProtocolFamily()
    fd = Net.socket(family, stream)
  }

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

    val storage = stackalloc[socket.sockaddr_storage]()
    val address = storage.asInstanceOf[Ptr[socket.sockaddr]]
    val addressLen = stackalloc[socket.socklen_t]()
    !addressLen = sizeof[in.sockaddr_in6].toUInt

    val newFd = socket.accept(fd.fd, address, addressLen)
    if (newFd == -1) {
      throw new SocketException("Accept failed")
    }

    val insAddr = SocketHelpers.sockaddrStorageToInetSocketAddress(address)
    s.address = insAddr.getAddress
    s.port = insAddr.getPort
    s.localport = this.localport
    s.fd = new FileDescriptor(newFd)
  }

  private def connect4(addr: InetAddress, port: Int, timeout: Int): Unit = {
    val sa4 = stackalloc[in.sockaddr_in]()
    val sa4Len = sizeof[in.sockaddr_in].toUInt

    SocketHelpers.prepareSockaddrIn4(addr, port, sa4)

    if (timeout != 0) Net.configureBlocking(fd, blocking = false)

    val connectRet = socket.connect(
      fd.fd,
      sa4.asInstanceOf[Ptr[socket.sockaddr]],
      sa4Len
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
        throw new ConnectException(
          s"Could not connect to address: $addr on port: $port, errno: ${lastError()}"
        )
      }
    }

    this.address = addr
    this.port = port
    this.localport = fetchLocalPort(socket.AF_INET).getOrElse {
      throw new ConnectException(
        "Could not resolve a local port when connecting"
      )
    }
  }

  private def connect6(addr: InetAddress, port: Int, timeout: Int): Unit = {
    val sa6 = stackalloc[in.sockaddr_in6]()
    val sa6Len = sizeof[in.sockaddr_in6].toUInt

    // By contract, all the bytes in sa6 are zero going in.
    SocketHelpers.prepareSockaddrIn6(addr, port, sa6)

    if (timeout != 0) Net.configureBlocking(fd, blocking = false)

    val connectRet = socket.connect(
      fd.fd,
      sa6.asInstanceOf[Ptr[socket.sockaddr]],
      sa6Len
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
        throw new ConnectException(
          s"Could not connect to address: $addr on port: $port, errno: ${lastError()}"
        )
      }
    }

    this.address = addr
    this.port = port
    this.localport = fetchLocalPort(sa6.sin6_family.toInt).getOrElse {
      throw new ConnectException(
        "Could not resolve a local port when connecting"
      )
    }
  }

  private lazy val connectFunc =
    if (useIPv4Only) connect4(_: InetAddress, _: Int, _: Int)
    else connect6(_: InetAddress, _: Int, _: Int)

  override def connect(host: String, port: Int): Unit = {
    throwIfClosed("connect")
    val addr = InetAddress.getByName(host)
    connectFunc(addr, port, 0)
  }
  override def connect(address: InetAddress, port: Int): Unit = {
    throwIfClosed("connect")
    connectFunc(address, port, 0)
  }

  override def connect(address: SocketAddress, timeout: Int): Unit = {
    throwIfClosed("connect")
    val insAddr = address match {
      case insAddr: InetSocketAddress => insAddr
      case _ => throw new IllegalArgumentException("Unsupported address type")
    }
    val addr = insAddr.getAddress
    val port = insAddr.getPort
    connectFunc(addr, port, timeout)
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
      def interruptDetected = mapLastError(
        onUnix = { _ == EINTR },
        onWindows = { _ == WSAEINTR }
      )

      bytesNum match {
        case _ if (bytesNum > 0) => bytesNum

        case 0 => if (count == 0) 0 else -1

        case _ if timeoutDetected =>
          throw new SocketTimeoutException("Socket timeout while reading data")

        case _ if interruptDetected && !Thread.interrupted() =>
          read(buffer, offset, count)

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
          + optValue + ", errno: " + lastError()
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
