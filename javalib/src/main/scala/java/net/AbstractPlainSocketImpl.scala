package java.net

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.libc._
import scala.scalanative.runtime.ByteArray
import scala.scalanative.posix.errno._
import scala.scalanative.posix.sys.socket
import scala.scalanative.posix.sys.socketOps._
import scala.scalanative.posix.sys.ioctl._
import scala.scalanative.posix.netinet.in
import scala.scalanative.posix.netinet.inOps._
import scala.scalanative.posix.netinet.tcp
import scala.scalanative.posix.arpa.inet
import scala.scalanative.posix.netdb._
import scala.scalanative.posix.netdbOps._
import scala.scalanative.posix.sys.time._
import scala.scalanative.posix.sys.timeOps._
import scala.scalanative.meta.LinktimeInfo.isWindows
import java.io.{FileDescriptor, IOException, OutputStream, InputStream}

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
    val len = stackalloc[socket.socklen_t]
    val portOpt = if (family == socket.AF_INET) {
      val sin = stackalloc[in.sockaddr_in]
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
      val sin = stackalloc[in.sockaddr_in6]
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

  override def bind(addr: InetAddress, port: Int): Unit = {
    val hints = stackalloc[addrinfo]
    val ret = stackalloc[Ptr[addrinfo]]
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

  override def listen(backlog: Int): Unit = {
    if (socket.listen(fd.fd, backlog) == -1) {
      throw new SocketException("Listen failed")
    }
    listening = true
  }

  override def accept(s: SocketImpl): Unit = {
    throwIfClosed("accept") // Do not send negative fd.fd to poll()

    if (timeout > 0) {
      tryPollOnAccept()
    }

    val storage = stackalloc[Byte](sizeof[in.sockaddr_in6])
    val len = stackalloc[socket.socklen_t]
    !len = sizeof[in.sockaddr_in6].toUInt

    val newFd =
      socket.accept(fd.fd, storage.asInstanceOf[Ptr[socket.sockaddr]], len)
    if (newFd == -1) {
      throw new SocketException("Accept failed")
    }
    val family =
      storage.asInstanceOf[Ptr[socket.sockaddr_storage]].ss_family.toInt
    val ipstr = stackalloc[CChar](in.INET6_ADDRSTRLEN.toULong)

    if (family == socket.AF_INET) {
      val sa = storage.asInstanceOf[Ptr[in.sockaddr_in]]
      inet.inet_ntop(
        socket.AF_INET,
        sa.sin_addr.asInstanceOf[Ptr[Byte]],
        ipstr,
        in.INET6_ADDRSTRLEN.toUInt
      )
      s.port = inet.ntohs(sa.sin_port).toInt
    } else {
      val sa = storage.asInstanceOf[Ptr[in.sockaddr_in6]]
      inet.inet_ntop(
        socket.AF_INET6,
        sa.sin6_addr.asInstanceOf[Ptr[Byte]],
        ipstr,
        in.INET6_ADDRSTRLEN.toUInt
      )
      s.port = inet.ntohs(sa.sin6_port).toInt
    }

    Zone { implicit z => s.address = InetAddress.getByName(fromCString(ipstr)) }

    s.fd = new FileDescriptor(newFd)
    s.localport = this.localport
  }

  override def connect(host: String, port: Int): Unit = {
    val addr = InetAddress.getByName(host)
    connect(addr, port)
  }

  override def connect(address: InetAddress, port: Int): Unit = {
    connect(new InetSocketAddress(address, port), 0)
  }

  override def connect(address: SocketAddress, timeout: Int): Unit = {

    throwIfClosed("connect") // Do not send negative fd.fd to poll()

    val inetAddr = address.asInstanceOf[InetSocketAddress]
    val hints = stackalloc[addrinfo]
    val ret = stackalloc[Ptr[addrinfo]]
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
      if ((timeout > 0) && (errno.errno == EINPROGRESS)) {
        tryPollOnConnect(timeout)
      } else {
        throw new ConnectException(
          s"Could not connect to address: ${remoteAddress}"
            + s" on port: ${inetAddr.getPort}"
            + s", errno: ${errno.errno}"
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

  override def close(): Unit = {
    if (!isClosed) {
      fd.close()
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
      val cArr = buffer.asInstanceOf[ByteArray].at(offset)
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
        .recv(fd.fd, buffer.asInstanceOf[ByteArray].at(offset), count.toUInt, 0)
        .toInt

      bytesNum match {
        case _ if (bytesNum > 0) => bytesNum

        case 0 => if (count == 0) 0 else -1

        case _ if ((errno.errno == EAGAIN) || (errno.errno == EWOULDBLOCK)) =>
          throw new SocketTimeoutException("Socket timeout while reading data")

        case _ =>
          throw new SocketException(s"read failed, errno: ${errno.errno}")
      }
    }
  }

  override def available(): Int = {
    if (shutInput) {
      0
    } else {
      val bytesAvailable = stackalloc[CInt]
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
    case SocketOptions.IP_TOS       => in.IP_TOS
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
      case SocketOptions.IP_TOS      => in.IPPROTO_IP
      case _                         => socket.SOL_SOCKET
    }

    val optValue = nativeValueFromOption(optID)

    val opt = if (optID == SocketOptions.SO_LINGER) {
      stackalloc[socket.linger].asInstanceOf[Ptr[Byte]]
    } else {
      stackalloc[CInt].asInstanceOf[Ptr[Byte]]
    }

    val len = stackalloc[socket.socklen_t]
    !len = if (optID == SocketOptions.SO_LINGER) {
      sizeof[socket.linger].toUInt
    } else {
      sizeof[CInt].toUInt
    }

    if (socket.getsockopt(fd.fd, level, optValue, opt, len) == -1) {
      throw new SocketException(
        "Exception while getting socket option with id: "
          + optValue + ", errno: " + errno.errno
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
      case SocketOptions.IP_TOS      => in.IPPROTO_IP
      case SocketOptions.TCP_NODELAY => in.IPPROTO_TCP
      case _                         => socket.SOL_SOCKET
    }
    val optValue = nativeValueFromOption(optID)

    var opt: Ptr[Byte] = stackalloc[Byte]
    var len = if (optID == SocketOptions.SO_LINGER) {
      sizeof[socket.linger].toUInt
    } else if (optID == SocketOptions.SO_TIMEOUT) {
      sizeof[timeval].toUInt
    } else {
      sizeof[CInt].toUInt
    }

    opt = optID match {
      case SocketOptions.TCP_NODELAY | SocketOptions.SO_KEEPALIVE |
          SocketOptions.SO_REUSEADDR | SocketOptions.SO_OOBINLINE =>
        val ptr = stackalloc[CInt]
        !ptr = if (value.asInstanceOf[Boolean]) 1 else 0
        ptr.asInstanceOf[Ptr[Byte]]
      case SocketOptions.SO_LINGER =>
        val ptr = stackalloc[socket.linger]
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
        val ptr = stackalloc[timeval]
        val mseconds = value.asInstanceOf[Int]

        this.timeout = mseconds

        ptr.tv_sec = mseconds / 1000
        ptr.tv_usec = (mseconds % 1000) * 1000

        ptr.asInstanceOf[Ptr[Byte]]
      case _ =>
        val ptr = stackalloc[CInt]
        !ptr = value.asInstanceOf[Int]
        ptr.asInstanceOf[Ptr[Byte]]
    }

    if (socket.setsockopt(fd.fd, level, optValue, opt, len) == -1) {
      throw new SocketException(
        "Exception while setting socket option with id: "
          + optID + ", errno: " + errno.errno
      )
    }
  }
}

private[net] object AbstractPlainSocketImpl {
  final val InvalidSocketDescriptor = new FileDescriptor

  def apply(): AbstractPlainSocketImpl = {
    if (isWindows) new WindowsPlainSocketImpl()
    else
      new UnixPlainSocketImpl()
  }
}
