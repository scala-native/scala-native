package java.net

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.libc._
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
import scala.scalanative.posix.poll._
import scala.scalanative.posix.pollEvents._
import scala.scalanative.posix.pollOps._
import scala.scalanative.posix.sys.ioctl._
import scala.scalanative.posix.fcntl._
import scala.scalanative.posix.sys.select._
import scala.scalanative.posix.sys.time._
import scala.scalanative.posix.sys.timeOps._
import scala.scalanative.posix.unistd.{close => cClose}
import java.io.{FileDescriptor, IOException, OutputStream, InputStream}

private[net] class PlainSocketImpl extends SocketImpl {

  protected[net] var fd                   = new FileDescriptor
  protected[net] var localport            = 0
  protected[net] var address: InetAddress = null
  protected[net] var port                 = 0

  private var timeout   = 0
  private var listening = false

  override def getInetAddress: InetAddress       = address
  override def getFileDescriptor: FileDescriptor = fd

  private def throwIfClosed(osFd: Int, methodName: String): Unit = {
    if (osFd == -1) {
      throw new SocketException(s"${methodName}: Socket is closed")
    }
  }
  override def create(streaming: Boolean): Unit = {
    val sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM, 0)
    if (sock < 0) throw new IOException("Couldn't create a socket")
    fd = new FileDescriptor(sock)
  }

  private def fetchLocalPort(family: Int): Option[Int] = {
    val len = stackalloc[socket.socklen_t]
    val portOpt = if (family == socket.AF_INET) {
      val sin = stackalloc[in.sockaddr_in]
      !len = sizeof[in.sockaddr_in].toUInt

      if (socket.getsockname(fd.fd, sin.asInstanceOf[Ptr[socket.sockaddr]], len) == -1) {
        None
      } else {
        Some(sin.sin_port)
      }
    } else {
      val sin = stackalloc[in.sockaddr_in6]
      !len = sizeof[in.sockaddr_in6].toUInt

      if (socket.getsockname(fd.fd, sin.asInstanceOf[Ptr[socket.sockaddr]], len) == -1) {
        None
      } else {
        Some(sin.sin6_port)
      }
    }

    portOpt.map(inet.ntohs(_).toInt)
  }

  override def bind(addr: InetAddress, port: Int): Unit = {
    val hints = stackalloc[addrinfo]
    val ret   = stackalloc[Ptr[addrinfo]]
    string.memset(hints.asInstanceOf[Ptr[Byte]], 0, sizeof[addrinfo])
    hints.ai_family = socket.AF_UNSPEC
    hints.ai_flags = AI_NUMERICHOST
    hints.ai_socktype = socket.SOCK_STREAM

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

    this.localport = fetchLocalPort(family).getOrElse {
      throw new BindException(
        "Couldn't bind to address: " + addr.getHostAddress + " on port: " + port)
    }
  }

  override def listen(backlog: Int): Unit = {
    if (socket.listen(fd.fd, backlog) == -1) {
      throw new SocketException("Listen failed")
    }
    listening = true
  }

  override def accept(s: SocketImpl): Unit = {

    throwIfClosed(fd.fd, "accept") // Do not send negative fd.fd to poll()

    if (timeout > 0) {
      val nAlloc = 1

      val pollFdPtr = stackalloc[struct_pollfd](nAlloc)

      pollFdPtr.fd = fd.fd
      pollFdPtr.events = POLLIN
      pollFdPtr.revents = 0

      val pollRes = poll(pollFdPtr, nAlloc.toUInt, timeout)

      pollRes match {
        case err if err < 0 =>
          throw new SocketException(
            s"accept failed, poll errno: ${errno.errno}")

        case 0 =>
          throw new SocketTimeoutException(
            s"accept timed out, SO_TIMEOUT: ${timeout}")

        case _ => // success, carry on
      }

      val revents = pollFdPtr.revents

      if (((revents & POLLERR) | (revents & POLLHUP)) != 0) {
        throw new SocketException("Accept poll failed, POLLERR or POLLHUP")
      } else if ((revents & POLLNVAL) != 0) {
        throw new SocketException(
          s"accept failed, invalid poll request: ${revents}")
      } else if (((revents & POLLIN) | (revents & POLLOUT)) == 0) {
        throw new SocketException(
          "accept failed, neither POLLIN nor POLLOUT set, " +
            s"revents, ${revents}")
      }
    }

    val storage = stackalloc[Byte](sizeof[in.sockaddr_in6])
    val len     = stackalloc[socket.socklen_t]
    !len = sizeof[in.sockaddr_in6].toUInt

    val newFd =
      socket.accept(fd.fd, storage.asInstanceOf[Ptr[socket.sockaddr]], len)
    if (newFd == -1) {
      throw new SocketException("Accept failed")
    }
    val family =
      storage.asInstanceOf[Ptr[socket.sockaddr_storage]].ss_family.toInt
    val ipstr = stackalloc[CChar](in.INET6_ADDRSTRLEN)

    if (family == socket.AF_INET) {
      val sa = storage.asInstanceOf[Ptr[in.sockaddr_in]]
      inet.inet_ntop(socket.AF_INET,
                     sa.sin_addr.asInstanceOf[Ptr[Byte]],
                     ipstr,
                     in.INET6_ADDRSTRLEN.toUInt)
      s.port = inet.ntohs(sa.sin_port).toInt
    } else {
      val sa = storage.asInstanceOf[Ptr[in.sockaddr_in6]]
      inet.inet_ntop(socket.AF_INET6,
                     sa.sin6_addr.asInstanceOf[Ptr[Byte]],
                     ipstr,
                     in.INET6_ADDRSTRLEN.toUInt)
      s.port = inet.ntohs(sa.sin6_port).toInt
    }

    Zone { implicit z =>
      s.address = InetAddress.getByName(fromCString(ipstr))
    }

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
  @inline
  private def connectGetFdOpts(fdFd: Int): Int = {
    val opts = fcntl(fdFd, F_GETFL, 0)

    if (opts == -1) {
      throw new ConnectException(
        "connect failed, fcntl F_GETFL" +
          s", errno: ${errno.errno}")
    }

    opts
  }

  @inline
  private def connectSetFdOpts(fdFd: Int, opts: Int): Unit = {
    val ret = fcntl(fdFd, F_SETFL, opts)

    if (ret == -1) {
      throw new ConnectException(
        "connect failed, " +
          s"fcntl F_SETFL for opts: ${opts}" +
          s", errno: ${errno.errno}")
    }
  }

  @inline
  private def connectSetFdNoBlock(fdFd: Int): Int = {
    val oldOpts = connectGetFdOpts(fdFd)
    val newOpts = oldOpts | O_NONBLOCK
    connectSetFdOpts(fdFd, newOpts)
    oldOpts
  }

  private def connectPollTimeout(timeout: Int, fdFd: Int, opts: Int): Unit = {
    val nAlloc = 1

    val pollFdPtr = stackalloc[struct_pollfd](nAlloc)

    pollFdPtr.fd = fd.fd
    pollFdPtr.events = POLLIN | POLLOUT
    pollFdPtr.revents = 0

    val pollRes = poll(pollFdPtr, nAlloc.toUInt, timeout)

    connectSetFdOpts(fdFd, opts & ~O_NONBLOCK)

    pollRes match {
      case err if err < 0 =>
        throw new SocketException(s"connect failed, poll errno: ${errno.errno}")

      case 0 =>
        throw new SocketTimeoutException(
          s"connect timed out, SO_TIMEOUT: ${timeout}")

      case _ =>
        val revents = pollFdPtr.revents

        if ((revents & POLLNVAL) != 0) {
          throw new ConnectException(
            s"connect failed, invalid poll request: ${revents}")
        } else if ((revents & (POLLERR | POLLHUP)) != 0) {
          throw new ConnectException(
            s"connect failed, POLLERR or POLLHUP set: ${revents}")
        }
    }
  }

  override def connect(address: SocketAddress, timeout: Int): Unit = {

    throwIfClosed(fd.fd, "connect") // Do not send negative fd.fd to poll()

    val inetAddr = address.asInstanceOf[InetSocketAddress]
    val hints    = stackalloc[addrinfo]
    val ret      = stackalloc[Ptr[addrinfo]]
    string.memset(hints.asInstanceOf[Ptr[Byte]], 0, sizeof[addrinfo])
    hints.ai_family = socket.AF_UNSPEC
    hints.ai_flags = AI_NUMERICHOST | AI_NUMERICSERV
    hints.ai_socktype = socket.SOCK_STREAM
    val remoteAddress = inetAddr.getAddress.getHostAddress

    Zone { implicit z =>
      val cIP   = toCString(remoteAddress)
      val cPort = toCString(inetAddr.getPort.toString)

      val retCode = getaddrinfo(cIP, cPort, hints, ret)

      if (retCode != 0) {
        throw new ConnectException(
          s"Could not resolve address: ${remoteAddress}"
            + s" on port: ${inetAddr.getPort}"
            + s" return code: ${retCode}")
      }
    }

    val family = (!ret).ai_family

    val oldOpts = if (timeout == 0) 0 else connectSetFdNoBlock(fd.fd)

    val connectRet = socket.connect(fd.fd, (!ret).ai_addr, (!ret).ai_addrlen)

    freeaddrinfo(!ret) // Must be after last use of ai_addr.

    if (connectRet < 0) {
      if ((timeout > 0) && (errno.errno == EINPROGRESS)) {
        connectPollTimeout(timeout, fd.fd, oldOpts)
      } else {
        throw new ConnectException(
          s"Could not connect to address: ${remoteAddress}"
            + s" on port: ${inetAddr.getPort}"
            + s", errno: ${errno.errno}")
      }
    }

    this.address = inetAddr.getAddress
    this.port = inetAddr.getPort
    this.localport = fetchLocalPort(family).getOrElse {
      throw new ConnectException(
        "Could not resolve a local port when connecting")
    }
  }

  override def close(): Unit = {
    if (fd.fd != -1) {
      cClose(fd.fd)
      fd = new FileDescriptor
    }
  }

  override def getOutputStream: OutputStream = {
    if (fd.fd == -1) {
      throw new SocketException("Socket is closed")
    }
    if (shutOutput) {
      throw new SocketException("Socket output is shutdown")
    }
    new SocketOutputStream(this)
  }

  override def getInputStream: InputStream = {
    if (fd.fd == -1) {
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
    } else if (fd.fd == -1) {
      0
    } else {
      val cArr = buffer.asInstanceOf[ByteArray].at(offset)
      var sent = 0
      while (sent < count) {
        val ret = socket
          .send(fd.fd, cArr + sent, count - sent, socket.MSG_NOSIGNAL)
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

    val bytesNum = socket
      .recv(fd.fd, buffer.asInstanceOf[ByteArray].at(offset), count, 0)
      .toInt
    if (bytesNum <= 0) {
      if (errno.errno == EAGAIN || errno.errno == EWOULDBLOCK) {
        throw new SocketTimeoutException("Socket timeout while reading data")
      }
      -1
    } else {
      bytesNum
    }
  }

  override def available: Int = {
    if (shutInput) {
      0
    } else {
      val bytesAvailable = stackalloc[CInt]
      ioctl(fd.fd, FIONREAD, bytesAvailable.asInstanceOf[Ptr[Byte]])
      !bytesAvailable match {
        case -1 =>
          throw new IOException(
            "Error while trying to estimate available bytes to read")
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
    if (fd.fd == -1) {
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
          + optValue + ", errno: " + errno.errno)
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
    if (fd.fd == -1) {
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
        val ptr    = stackalloc[socket.linger]
        val linger = value.asInstanceOf[Int]

        if (linger == -1) ptr.l_onoff = 0
        else ptr.l_onoff = 1

        ptr.l_linger = linger
        ptr.asInstanceOf[Ptr[Byte]]
      case SocketOptions.SO_TIMEOUT =>
        val ptr      = stackalloc[timeval]
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
          + optID + ", errno: " + errno.errno)
    }
  }

}
