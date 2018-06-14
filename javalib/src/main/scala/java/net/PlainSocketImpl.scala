package java.net

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
import scala.scalanative.posix.sys.SelectFdSet
import scala.scalanative.posix.sys.{time, timeOps}, time._, timeOps._
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

  override def bind(addr: InetAddress, port: Int): Unit = {
    val hints = stackalloc[addrinfo]
    val ret   = stackalloc[Ptr[addrinfo]]
    string.memset(hints.cast[Ptr[Byte]], 0, sizeof[addrinfo])
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
    println("LeeT: PSockImpl Accept: Begin - using CArray fd_set")
//    if (fd.fd == -1) {
//      println("LeeT: PSockImpl Accept: socket is closed")
//      throw new SocketException("Socket is closed")
//    }
    if (timeout > 0) {
      println("LeeT: PSockImpl Accept: timeout > 0 begin")

//    var selectRes: Int = -99

      Zone { implicit z =>
        println(
          "LeeT: PSockImpl Accept: using zone fd_set - begin - no FD_ZERO")
        println("LeeT: PSockImpl Accept: zone end _after_ selectRes testing")

//      val fdset = stackalloc[fd_set]
        /*      val fdsetPtr = stackalloc[CLongInt](FD_SETSIZE / (8.0 * sizeof[CLongInt])
                                            .ceil.toInt).cast[Ptr[fd_set]]
      zeroAndSet(fdsetPtr)
         */
        val fdsetPtr = SelectFdSet.create(fd.fd)

//      val fdset = alloc[Byte](FD_SETSIZE / 8).cast[Ptr[fd_set]]
//    FD_ZERO(fdset) // Zone alloc returns cleared memory, no need to do twice
//    FD_SET(fd.fd, fdset)

//      val fdset = stackalloc[CLongInt](FD_SETSIZE / (8 * sizeof[CLongInt])
//    println("LeeT: PSockImpl Accept: before my zero&Set")
//    FD_ZERO(fdset)
//    FD_SET(fd.fd, fdset)
//      zeroAndSet(fdset)
//    println("LeeT: PSockImpl Accept: after my zero&Set")
        println("LeeT: PSockImpl Accept: t>0 after zero&Set")

//      val time = stackalloc[timeval]
        val time = alloc[timeval]
        time.tv_sec = timeout / 1000
        time.tv_usec = (timeout % 1000) * 1000

        println("LeeT: PSockImpl Accept: before select()")
        val selectRes = select(fd.fd + 1, fdsetPtr, null, null, time)
//      selectRes = select(fd.fd + 1, fdset, null, null, time)

//    println("LeeT: PSockImpl Accept: using zone fd_set - end")

//    } // Zone

        println(s"LeeT: PSockImpl Accept: after select() status: ${selectRes}")

        selectRes match {
          case 0 =>
            println(
              "LeeT: PSockImpl Accept: selectRes0 about to thow my SocketTmoExc")
//          throw new SocketTimeoutException(
//            s"Accept timed out, SO_TIMEOUT was set to: ${timeout}")

            throw new SocketTimeoutException(
              "Accept timed out, SO_TIMEOUT was set to: LT-????")

//    println("LeeT: PSockImpl Accept: selectRes0 about to thow SocketTmoExc")
//          throw new SocketTimeoutException(
//            "Accept timed out, "
//              + "SO_TIMEOUT was set to: " + timeout)
          case -1 =>
            println(
              "LeeT: PSockImpl Accept: selectRes-1 about to thow SocketExc")
            throw new SocketException("Accept failed")
          case _ => {
            println("LeeT: PSockImpl Accept: selectResWildcard OK")
          }
        }
        println("LeeT: PSockImpl Accept: using zone fd_set - long zone - end")

      } // Zone

    }

    println("LeeT: PSockImpl Accept: after selectRes cases")

    val storage = stackalloc[Byte](sizeof[in.sockaddr_in6])
    val len     = stackalloc[socket.socklen_t]
    !len = sizeof[in.sockaddr_in6].toUInt

    println("LeeT: PSockImpl Accept: before infinite TMO accept")

    val newFd = socket.accept(fd.fd, storage.cast[Ptr[socket.sockaddr]], len)
    if (newFd == -1) {
      println("LeeT: PSockImpl Accept: after infinite TMO accept w SocketExcep")
      throw new SocketException("Accept failed")
    }
    println("LeeT: PSockImpl Accept: after infinite TMO accept")
    val family = storage.cast[Ptr[socket.sockaddr_storage]].ss_family.toInt
    val ipstr  = stackalloc[CChar](in.INET6_ADDRSTRLEN)

    println("LeeT: PSockImpl Accept: point 1")
    if (family == socket.AF_INET) {
      val sa = storage.cast[Ptr[in.sockaddr_in]]
      inet.inet_ntop(socket.AF_INET,
                     sa.sin_addr.cast[Ptr[Byte]],
                     ipstr,
                     in.INET6_ADDRSTRLEN.toUInt)
      s.port = inet.ntohs(sa.sin_port).toInt
    } else {
      val sa = storage.cast[Ptr[in.sockaddr_in6]]
      inet.inet_ntop(socket.AF_INET6,
                     sa.sin6_addr.cast[Ptr[Byte]],
                     ipstr,
                     in.INET6_ADDRSTRLEN.toUInt)
      s.port = inet.ntohs(sa.sin6_port).toInt
    }

    println("LeeT: PSockImpl Accept: before InetAddress.getByName")

    Zone { implicit z =>
      s.address = InetAddress.getByName(fromCString(ipstr))
    }
    println("LeeT: PSockImpl Accept: after InetAddress.getByName")

    s.fd = new FileDescriptor(newFd)
    s.localport = this.localport
    println("LeeT: PSockImpl Accept: End")
  }

  override def connect(host: String, port: Int): Unit = {
    val addr = InetAddress.getByName(host)
    connect(addr, port)
  }

  override def connect(address: InetAddress, port: Int): Unit = {
    connect(new InetSocketAddress(address, port), 0)
  }

  override def connect(address: SocketAddress, timeout: Int): Unit = {
    val inetAddr = address.asInstanceOf[InetSocketAddress]
    val hints    = stackalloc[addrinfo]
    val ret      = stackalloc[Ptr[addrinfo]]
    string.memset(hints.cast[Ptr[Byte]], 0, sizeof[addrinfo])
    hints.ai_family = socket.AF_UNSPEC
    hints.ai_flags = AI_NUMERICHOST | AI_NUMERICSERV
    hints.ai_socktype = socket.SOCK_STREAM

    Zone { implicit z =>
      val cIP = toCString(inetAddr.getAddress.getHostAddress)
      if (getaddrinfo(cIP, toCString(inetAddr.getPort.toString), hints, ret) != 0) {
        throw new ConnectException(
          "Couldn't resolve address: "
            + inetAddr.getAddress.getHostAddress)
      }
    }

    val family = (!ret).ai_family

    if (timeout == 0) {
      val connectRes = socket.connect(fd.fd, (!ret).ai_addr, (!ret).ai_addrlen)
      freeaddrinfo(!ret)

      if (connectRes < 0) {
        throw new ConnectException(
          "Couldn't connect to address: "
            + inetAddr.getAddress.getHostAddress +
            " on port: " + inetAddr.getPort)
      }
    } else {
      Zone { implicit z =>
        val opts = fcntl(fd.fd, F_GETFL, 0) | O_NONBLOCK
        fcntl(fd.fd, F_SETFL, opts)

//      val fdset = stackalloc[fd_set]
//      zeroAndSet(fdset)
        /*
      val fdsetPtr = stackalloc[CLongInt](FD_SETSIZE / (8.0 * sizeof[CLongInt])
                                            .ceil.toInt).cast[Ptr[fd_set]]
      zeroAndSet(fdsetPtr)
         */
//      val fdsetPtr =  scalanative.posix.sys.SelectFdSet.create(fd.fd)

        val fdsetPtr = SelectFdSet.create(fd.fd)

        val time = stackalloc[timeval]
        time.tv_sec = timeout / 1000
        time.tv_usec = (timeout % 1000) * 1000
        socket.connect(fd.fd, (!ret).ai_addr, (!ret).ai_addrlen)
        freeaddrinfo(!ret)

//      if (select(fd.fd + 1, null, fdset, null, time) != 1) {
        if (select(fd.fd + 1, null, fdsetPtr, null, time) != 1) {
          fcntl(fd.fd, F_SETFL, opts & ~O_NONBLOCK)
          throw new SocketTimeoutException("Connect timed out")
        } else {
          fcntl(fd.fd, F_SETFL, opts & ~O_NONBLOCK)
          val so_error = stackalloc[CInt].cast[Ptr[Byte]]
          val len      = stackalloc[socket.socklen_t]
          !len = sizeof[CInt].toUInt
          socket.getsockopt(fd.fd,
                            socket.SOL_SOCKET,
                            socket.SO_ERROR,
                            so_error,
                            len)
          if (!(so_error.cast[Ptr[CInt]]) != 0) {
            throw new ConnectException(
              "Couldn't connect to address: " +
                inetAddr.getAddress.getHostAddress
                + " on port: " + inetAddr.getPort)
          }
        }
      }
    }

    this.address = inetAddr.getAddress
    this.port = inetAddr.getPort

    this.localport = fetchLocalPort(family).getOrElse {
      throw new ConnectException(
        "Couldn't resolve a local port when connecting")
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
      ioctl(fd.fd, FIONREAD, bytesAvailable.cast[Ptr[Byte]])
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
      stackalloc[socket.linger].cast[Ptr[Byte]]
    } else {
      stackalloc[CInt].cast[Ptr[Byte]]
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
        Boolean.box(!(opt.cast[Ptr[CInt]]) != 0)
      case SocketOptions.SO_LINGER =>
        val linger = opt.cast[Ptr[socket.linger]]
        if (linger.l_onoff != 0) {
          Integer.valueOf(linger.l_linger)
        } else {
          Integer.valueOf(-1)
        }
      case SocketOptions.SO_TIMEOUT =>
        Integer.valueOf(this.timeout)
      case _ =>
        Integer.valueOf(!(opt.cast[Ptr[CInt]]))
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
        ptr.cast[Ptr[Byte]]
      case SocketOptions.SO_LINGER =>
        val ptr    = stackalloc[socket.linger]
        val linger = value.asInstanceOf[Int]

        if (linger == -1) ptr.l_onoff = 0
        else ptr.l_onoff = 1

        ptr.l_linger = linger
        ptr.cast[Ptr[Byte]]
      case SocketOptions.SO_TIMEOUT =>
        val ptr      = stackalloc[timeval]
        val mseconds = value.asInstanceOf[Int]

        this.timeout = mseconds

        ptr.tv_sec = mseconds / 1000
        ptr.tv_usec = (mseconds % 1000) * 1000

        ptr.cast[Ptr[Byte]]
      case _ =>
        val ptr = stackalloc[CInt]
        !ptr = value.asInstanceOf[Int]
        ptr.cast[Ptr[Byte]]
    }

    if (socket.setsockopt(fd.fd, level, optValue, opt, len) == -1) {
      throw new SocketException(
        "Exception while setting socket option with id: "
          + optID + ", errno: " + errno.errno)
    }
  }

}
