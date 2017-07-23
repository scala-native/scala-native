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
import scala.scalanative.posix.sys.selectOps._
import scala.scalanative.posix.unistd.{close => cClose}
import java.io.{FileDescriptor, IOException, OutputStream, InputStream}

private[net] class PlainSocketImpl extends SocketImpl {

  protected[net] var fd                = -1
  protected[net] var localport         = 0
  protected[net] var addr: InetAddress = null
  protected[net] var port              = 0

  override def getInetAddress: InetAddress       = addr
  override def getFileDescriptor: FileDescriptor = new FileDescriptor(fd)

  override def create(streaming: Boolean): Unit = {
    val sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM, 0)
    if (sock < 0) throw new IOException("Couldn't create a socket")
    fd = sock
  }

  //override def bind(addr: InetAddress, port: Int): Unit

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

    if (timeout == 0) {
      val connectRes = socket.connect(fd, (!ret).ai_addr, (!ret).ai_addrlen)
      freeaddrinfo(!ret)

      if (connectRes < 0) {
        throw new ConnectException(
          "Couldn't connect to address: "
            + inetAddr.getAddress.getHostAddress +
            " on port: " + inetAddr.getPort)
      } else {
        addr = inetAddr.getAddress
        port = inetAddr.getPort
        localport = if ((!ret).ai_family == socket.AF_INET) {
          (!ret).cast[Ptr[in.sockaddr_in]].sin_port.toInt
        } else {
          (!ret).cast[Ptr[in.sockaddr_in6]].sin6_port.toInt
        }
      }
    } else {
      val opts = fcntl(fd, F_GETFL, 0) | O_NONBLOCK
      fcntl(fd, F_SETFL, opts)

      val fdset = stackalloc[fd_set]
      !fdset._1 = stackalloc[CLongInt](FD_SETSIZE / sizeof[CLongInt])
      FD_ZERO(fdset)
      FD_SET(fd, fdset)

      val time = stackalloc[timeval]
      time.tv_sec = timeout / 1000
      time.tv_usec = (timeout % 1000) * 1000
      socket.connect(fd, (!ret).ai_addr, (!ret).ai_addrlen)
      freeaddrinfo(!ret)

      if (select(fd + 1, null, fdset, null, time) != 1) {
        fcntl(fd, F_SETFL, opts & ~O_NONBLOCK)
        throw new SocketTimeoutException("Timeout while connecting to socket")
      } else {
        fcntl(fd, F_SETFL, opts & ~O_NONBLOCK)
        val so_error = stackalloc[CInt].cast[Ptr[Byte]]
        val len      = stackalloc[socket.socklen_t]
        !len = sizeof[CInt].toUInt
        socket.getsockopt(fd,
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

    addr = inetAddr.getAddress
    port = inetAddr.getPort
    localport = if ((!ret).ai_family == socket.AF_INET) {
      (!ret).cast[Ptr[in.sockaddr_in]].sin_port.toInt
    } else {
      (!ret).cast[Ptr[in.sockaddr_in6]].sin6_port.toInt
    }
  }

  override def close: Unit = {
    if (fd != -1) {
      cClose(fd)
      fd = -1
    }
  }

  override def getOutputStream: OutputStream = {
    // TODO: is fd valid?
    new SocketOutputStream(this)
  }

  override def getInputStream: InputStream = {
    // TODO: is fd valid?
    new SocketInputStream(this)
  }

  override def shutdownOutput: Unit = {
    socket.shutdown(fd, 1) match {
      case 0 => shutOutput = true
      case _ =>
        throw new IOException("Error while shutting down socket's output")
    }
  }

  def write(buffer: Array[Byte], offset: Int, count: Int): Long = {
    if (shutOutput) {
      throw new IOException("Trying to write to a shut down socket")
    } else if (fd == -1) {
      0
    } else {
      Zone { implicit z =>
        val cArr = stackalloc[Byte](count)
        for (i <- 0 until count) {
          !(cArr + i) = buffer(i + offset)
        }
        var sent: Long = 0
        while (sent < count) {
          sent += socket.send(fd, cArr + sent, count - sent, 0)
        }
        sent
      }
    }
  }

  def read(buffer: Array[Byte], offset: Int, count: Int): Int = {
    if (shutInput) -1

    val cBuff    = stackalloc[Byte](count)
    val bytesNum = socket.recv(fd, cBuff, count, 0).toInt
    if (bytesNum <= 0) {
      if (errno.errno == EAGAIN || errno.errno == EWOULDBLOCK) {
        throw new SocketTimeoutException("Socket timeout while reading data")
      }
      -1
    } else {
      for (i <- 0 until bytesNum) {
        buffer(offset + i) = cBuff(i)
      }
      bytesNum
    }
  }

  override def available: Int = {
    val bytesAvailable = stackalloc[CInt]
    ioctl(fd, FIONREAD, bytesAvailable.cast[Ptr[Byte]])
    !bytesAvailable match {
      case -1 =>
        throw new IOException(
          "Error while trying to estimate available bytes to read")
      case x => x
    }
  }

  // We can't directly map values in SocketOptions to the native ones,
  // because some of them have the same value, but require different levels
  // for example IP_TOS and TCP_NODELAY have the same value on my machine
  private def nativeValueFromOption(option: Int) = option match {
    case SocketOptions.IP_MULTICAST_IF   => in.IP_MULTICAST_IF
    case SocketOptions.IP_MULTICAST_LOOP => in.IP_MULTICAST_LOOP
    case SocketOptions.IP_TOS            => in.IP_TOS
    case SocketOptions.SO_BROADCAST      => socket.SO_BROADCAST
    case SocketOptions.SO_KEEPALIVE      => socket.SO_KEEPALIVE
    case SocketOptions.SO_LINGER         => socket.SO_LINGER
    case SocketOptions.SO_TIMEOUT        => socket.SO_RCVTIMEO
    case SocketOptions.SO_OOBINLINE      => socket.SO_OOBINLINE
    case SocketOptions.SO_RCVBUF         => socket.SO_RCVBUF
    case SocketOptions.SO_SNDBUF         => socket.SO_SNDBUF
    case SocketOptions.SO_REUSEADDR      => socket.SO_REUSEADDR
    case SocketOptions.TCP_NODELAY       => tcp.TCP_NODELAY
    case _                               => throw new SocketException("This shouldn't happen")
  }

  override def getOption(optID: Int): Object = {
    if (fd == -1) {
      throw new SocketException("Socket is closed")
    }

    val level = optID match {
      case SocketOptions.TCP_NODELAY => in.IPPROTO_TCP
      case SocketOptions.IP_TOS      => in.IPPROTO_IP
      case _                         => socket.SOL_SOCKET
    }

    val optValue = nativeValueFromOption(optID)

    val opt = if (optID == SocketOptions.SO_LINGER) {
      stackalloc[socket.linger].cast[Ptr[Byte]]
    } else if (optID == SocketOptions.SO_TIMEOUT) {
      stackalloc[timeval].cast[Ptr[Byte]]
    } else {
      stackalloc[CInt].cast[Ptr[Byte]]
    }

    val len = stackalloc[socket.socklen_t]
    !len = if (optID == SocketOptions.SO_LINGER) {
      sizeof[socket.linger].toUInt
    } else if (optID == SocketOptions.SO_TIMEOUT) {
      sizeof[timeval].toUInt
    } else {
      sizeof[CInt].toUInt
    }

    if (socket.getsockopt(fd, level, optValue, opt, len) == -1) {
      throw new SocketException(
        "Exception while getting socket option with id: "
          + optValue + ", errno: " + errno.errno)
    }
    if (optID == SocketOptions.TCP_NODELAY || optID == SocketOptions.SO_KEEPALIVE
        || optID == SocketOptions.SO_REUSEADDR) {
      Boolean.box(!(opt.cast[Ptr[CInt]]) != 0)
    } else if (optID == SocketOptions.SO_LINGER) {
      val linger = opt.cast[Ptr[socket.linger]]
      if (linger.l_onoff != 0) {
        Integer.valueOf(linger.l_linger)
      } else {
        Integer.valueOf(-1)
      }
    } else if (optID == SocketOptions.SO_TIMEOUT) {
      val tv  = opt.cast[Ptr[timeval]]
      val sec = tv.tv_sec * 1000 + tv.tv_usec / 1000
      Integer.valueOf(sec.toInt)
    } else {
      Integer.valueOf(!(opt.cast[Ptr[CInt]]))
    }
  }

  override def setOption(optID: Int, value: Object): Unit = {
    if (fd == -1) {
      throw new SocketException("Socket is closed")
    }

    val level = optID match {
      case SocketOptions.IP_TOS      => in.IPPROTO_IP
      case SocketOptions.TCP_NODELAY => in.IPPROTO_TCP
      case _                         => socket.SOL_SOCKET
    }
    val optValue = nativeValueFromOption(optID)

    var opt: Ptr[Byte] = stackalloc[Byte]
    var len =
      if (optID == SocketOptions.SO_LINGER) sizeof[socket.linger].toUInt
      else if (optID == SocketOptions.SO_TIMEOUT) sizeof[timeval].toUInt
      else sizeof[CInt].toUInt

    if (optID == SocketOptions.TCP_NODELAY || optID == SocketOptions.SO_KEEPALIVE
        || optID == SocketOptions.SO_REUSEADDR) {
      val ptr = stackalloc[CInt]
      !ptr = if (value.asInstanceOf[Boolean]) 1 else 0
      opt = ptr.cast[Ptr[Byte]]
    } else if (optID == SocketOptions.SO_LINGER) {
      val ptr    = stackalloc[socket.linger]
      val linger = value.asInstanceOf[Int]

      if (linger == -1) ptr.l_onoff = 0
      else ptr.l_onoff = 1

      ptr.l_linger = linger
      opt = ptr.cast[Ptr[Byte]]
    } else if (optID == SocketOptions.SO_TIMEOUT) {
      val ptr      = stackalloc[timeval]
      val mseconds = value.asInstanceOf[Int]

      ptr.tv_sec = mseconds / 1000
      ptr.tv_usec = (mseconds % 1000) * 1000

      opt = ptr.cast[Ptr[Byte]]
    } else {
      val ptr = stackalloc[CInt]
      !ptr = value.asInstanceOf[Int]
      opt = ptr.cast[Ptr[Byte]]
    }

    if (socket.setsockopt(fd, level, optValue, opt, len) == -1) {
      throw new SocketException(
        "Exception while setting socket option with id: "
          + optID + ", errno: " + errno.errno)
    }
  }

}
