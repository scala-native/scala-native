package java.nio.channels

import java.nio.channels.spi._
import java.nio.ByteBuffer
import java.io.{FileDescriptor, IOException, InputStream, OutputStream}
import java.net._

import scala.scalanative.native._
import scala.scalanative.runtime.ByteArray
import scala.scalanative.posix.fcntl._
import scala.scalanative.posix.sys.{socket => sockInterop}
import scala.scalanative.posix.errno._
import scala.scalanative.posix.sys.uio._
import scala.scalanative.posix.sys.uioOps._

import scala.reflect._

// Part of the class ported from Apache Harmony
private[channels] class SocketChannelImpl(provider: SelectorProvider)
    extends SocketChannel(provider)
    with FileDescriptorHandler {

  object Status {
    val Uninitialized = 0
    val Unconnected   = 1
    val Pending       = 2
    val Connected     = 3
    val Closed        = 4
  }

  var fd: FileDescriptor = new FileDescriptor
  private val impl       = new PlainSocketImpl
  private val sock       = new SocketAdapter(this, impl)

  private var bound                            = false
  private var isInputShut                      = false
  private var isOutputShut                     = false
  private var localAddress: InetSocketAddress  = null
  private var remoteAddress: InetSocketAddress = null
  private var status                           = Status.Uninitialized

  private val writeLock = new Object
  private val readLock  = new Object

  create()

  private def create(): Unit = {
    impl.create(true)
    fd = impl.fd
  }

  def implCloseSelectableChannel(): Unit = {
    if (status != Status.Closed) {
      status = Status.Closed
      if (!socket.isClosed) {
        socket.close
      }
      fd = impl.fd
    }
  }

  def implConfigureBlocking(block: Boolean): Unit = blockingLock.synchronized {
    val opts = if (block) {
      fcntl(fd.fd, F_GETFL, 0) & (~O_NONBLOCK)
    } else {
      fcntl(fd.fd, F_GETFL, 0) | O_NONBLOCK
    }

    if (fcntl(fd.fd, F_SETFL, opts) == -1) {
      throw new IOException("Couldn't set nonblocking mode")
    }
  }

  def bind(local: SocketAddress): SocketChannel = {
    checkClosed
    if (local != null && !local.isInstanceOf[InetSocketAddress]) {
      throw new UnsupportedAddressTypeException
    }
    if (bound) {
      throw new AlreadyBoundException
    }
    if (status == Status.Pending) {
      throw new ConnectionPendingException
    }

    val addr = if (local == null) {
      new InetSocketAddress(0)
    } else {
      local.asInstanceOf[InetSocketAddress]
    }

    impl.bind(addr.getAddress, addr.getPort)
    this.bound = true
    this.localAddress = new InetSocketAddress(addr.getAddress, impl.localport)
    this
  }

  def connect(remote: SocketAddress): Boolean = {
    checkUnconnected()

    val inetAddr = validateAddress(remote)

    try {
      impl.connect(inetAddr.getAddress, inetAddr.getPort)
    } catch {
      case e: WouldBlockException => status = Status.Pending
    }

    if (isBlocking) {
      status = Status.Connected
      this.localAddress = new InetSocketAddress(impl.localAddr, impl.localport)
    }

    this.remoteAddress = inetAddr

    isBlocking
  }

  def finishConnect(): Boolean = synchronized {
    checkClosed()

    if (status == Status.Connected) {
      return true
    }

    if (status != Status.Pending) {
      throw new NoConnectionPendingException
    }

    val error = stackalloc[CInt]
    val len   = stackalloc[sockInterop.socklen_t]
    !len = sizeof[CInt].toUInt
    val retval = sockInterop.getsockopt(fd.fd,
                                        sockInterop.SOL_SOCKET,
                                        sockInterop.SO_ERROR,
                                        error.cast[Ptr[Byte]],
                                        len)

    if (retval != 0) {
      throw new SocketException("Error while checking if connect finished")
    }

    if (!error == 0) {
      status = Status.Connected
      bound = true
      this.localAddress = new InetSocketAddress(impl.localAddr, impl.localport)
      true
    } else if (!error == EALREADY) {
      false
    } else {
      throw new SocketException(
        "Error while attempting to finish channel connection")
    }
  }

  def getLocalAddress: SocketAddress = {
    checkClosed()
    localAddress
  }

  def getRemoteAddress: SocketAddress = {
    checkClosed()
    remoteAddress
  }

  def isConnected: Boolean = status == Status.Connected

  def isConnectionPending: Boolean = status == Status.Pending

  def read(dsts: Array[ByteBuffer], offset: Int, length: Int): Long = {
    if (!isIndexValid(dsts, offset, length)) {
      throw new IndexOutOfBoundsException
    }

    checkClosed()
    if (!isConnected) {
      throw new NotYetConnectedException
    }

    if (isInputShut) {
      throw new ClosedChannelException
    }

    val vectors = stackalloc[iovec](length - offset)
    for (i <- 0 until (length - offset)) {
      val buf = dsts(offset + i)
      val arr = buf.array
      (vectors + i).iov_base =
        arr.asInstanceOf[ByteArray].at(buf.arrayOffset).cast[Ptr[Byte]]
      (vectors + i).iov_len = arr.length - buf.arrayOffset
    }

    val bytesWritten = readv(fd.fd, vectors, length - offset)

    if (bytesWritten == -1) {
      if (errno.errno != EWOULDBLOCK && errno.errno != EAGAIN) {
        throw new IOException(
          "Error while reading scattering buffers through a socket channel")
      }
      0
    } else {
      bytesWritten
    }

  }

  def read(dst: ByteBuffer): Int = {
    if (dst == null) {
      throw new NullPointerException
    }

    checkClosed()

    if (!isConnected) {
      throw new NotYetConnectedException
    }

    if (isInputShut) {
      -1
    } else {
      val readCount = readImpl(dst)
      if (readCount > 0) {
        dst.position(dst.position + readCount)
      }
      readCount
    }
  }

  private def readImpl(target: ByteBuffer): Int = readLock.synchronized {
    var offset = target.position
    val length = target.remaining

    // this is very similar to PlainSocketImpl.read method, except
    // the handling of EAGAIN and EWOULDBLOCK errors, maybe it can be
    // extracted to a static method?

    val array = target.array
    offset += target.arrayOffset

    val bytesNum = sockInterop
      .recv(fd.fd, array.asInstanceOf[ByteArray].at(offset), length, 0)
      .toInt

    if (bytesNum < 0) {
      val error = errno.errno
      if (error == EAGAIN || error == EWOULDBLOCK) {
        0
      } else {
        throw new SocketException("Error while reading from socket channel")
      }
    } else {
      bytesNum
    }
  }

  def getOption[T](name: SocketOption[T]): T = {
    // TODO
    checkClosed()
    name match {
      case SoSndBuf =>
        sock.getSendBufferSize.asInstanceOf[T]
      /*case StandardSocketOptions.SO_RCVBUF =>
        sock.getReceiveBufferSize.asInstanceOf[T]
      case StandardSocketOptions.SO_KEEPALIVE =>
        sock.getKeepAlive.asInstanceOf[T]
      case StandardSocketOptions.SO_REUSEADDR =>
        sock.getReuseAddress.asInstanceOf[T]
      case StandardSocketOptions.SO_LINGER =>
        sock.getSoLinger.asInstanceOf[T]*/
      case TcpNoDelay =>
        sock.getTcpNoDelay.asInstanceOf[T]
      case _ => throw new UnsupportedOperationException
    }
  }

  def setOption[T](name: SocketOption[T], value: T): SocketChannel = {
    checkClosed()
    name match {
      case SoSndBuf =>
        sock.setSendBufferSize(value.asInstanceOf[Int])
      /*case "SO_RCVBUF" =>
        if (!value.isInstanceOf[Int])
          throw new IllegalArgumentException
        sock.setReceiveBufferSize(value.asInstanceOf[Int])
      case "SO_KEEPALIVE" =>
        if (!value.isInstanceOf[Boolean])
          throw new IllegalArgumentException
        sock.setKeepAlive(value.asInstanceOf[Boolean])
      case "SO_REUSEADDR" =>
        if (!value.isInstanceOf[Boolean])
          throw new IllegalArgumentException
        sock.setReuseAddress(value.asInstanceOf[Boolean])
      case "SO_LINGER" =>
        if (!value.isInstanceOf[Int])
          throw new IllegalArgumentException
        val linger = value.asInstanceOf[Int]
        if (linger >= 0)
          sock.setSoLinger(true, linger)
        else
          sock.setSoLinger(false, linger)*/
      case TcpNoDelay =>
        sock.setTcpNoDelay(value.asInstanceOf[Boolean])
      case _ => throw new UnsupportedOperationException
    }
    this
  }

  def shutdownInput: SocketChannel = readLock.synchronized {
    impl.shutdownInput
    isInputShut = true
    this
  }

  def shutdownOutput: SocketChannel = writeLock.synchronized {
    impl.shutdownOutput
    isOutputShut = true
    this
  }

  def socket: Socket = sock

  def write(srcs: Array[ByteBuffer], offset: Int, length: Int): Long = {
    if (!isIndexValid(srcs, offset, length)) {
      throw new IndexOutOfBoundsException
    }

    checkClosed()
    if (!isConnected) {
      throw new NotYetConnectedException
    }

    if (isOutputShut) {
      throw new ClosedChannelException
    }

    val vectors = stackalloc[iovec](length - offset)
    for (i <- 0 until (length - offset)) {
      val buf = srcs(offset + i)
      val arr = buf.array
      (vectors + i).iov_base =
        arr.asInstanceOf[ByteArray].at(buf.arrayOffset).cast[Ptr[Byte]]
      (vectors + i).iov_len = arr.length - buf.arrayOffset
    }

    val bytesWritten = writev(fd.fd, vectors, length - offset)

    var bytesRemaining = bytesWritten

    if (bytesWritten == -1) {
      if (errno.errno != EWOULDBLOCK && errno.errno != EAGAIN) {
        throw new IOException(
          "Error while writing gathered buffers through a socket channel")
      }
    }

    for (i <- offset until (length + offset) if (bytesRemaining > 0)) {
      if (bytesRemaining > srcs(i).remaining) {
        bytesRemaining -= srcs(i).remaining
        srcs(i).position(srcs(i).limit)
      } else {
        srcs(i).position(srcs(i).position + bytesRemaining.toInt)
        bytesRemaining = 0
      }
    }

    if (bytesWritten == -1) {
      0
    } else {
      bytesWritten
    }
  }

  private def isIndexValid(targets: Array[ByteBuffer],
                           offset: Int,
                           length: Int): Boolean =
    (length >= 0) && (offset >= 0) && (length + offset <= targets.length)

  def write(src: ByteBuffer): Int = {
    if (src == null) {
      throw new NullPointerException
    }

    checkClosed()

    if (!isConnected) {
      throw new NotYetConnectedException
    }

    if (isOutputShut) {
      throw new ClosedChannelException
    } else if (!src.hasRemaining) {
      0
    } else {
      writeImpl(src)
    }
  }

  private def writeImpl(source: ByteBuffer): Int = writeLock.synchronized {
    var writeCount = 0
    var pos        = source.position
    val length     = source.remaining

    pos += source.arrayOffset
    if (isBlocking) {
      writeCount = impl.write(source.array, pos, length).toInt
    } else {
      writeCount = send(source.array, pos, length)
    }

    source.position(pos + writeCount)

    writeCount
  }

  private def send(arr: Array[Byte], offset: Int, length: Int): Int = {
    val result = sockInterop
      .send(fd.fd, arr.asInstanceOf[ByteArray].at(offset), length, 0)
      .toInt

    if (result == -1) {
      if (errno.errno != EAGAIN && errno.errno != EWOULDBLOCK) {
        throw new IOException(
          "Error while sending data through a socket channel")
      } else {
        0
      }
    } else {
      result
    }
  }

  private def checkClosed() = {
    if (!isOpen) {
      throw new ClosedChannelException
    }
  }

  private def checkUnconnected() = {
    checkClosed()
    if (status == Status.Connected) {
      throw new AlreadyConnectedException
    }
    if (status == Status.Pending) {
      throw new ConnectionPendingException
    }
  }

  private def validateAddress(socketAddress: SocketAddress) = {
    if (socketAddress == null) {
      throw new NullPointerException
    }
    if (!socketAddress.isInstanceOf[InetSocketAddress]) {
      throw new UnsupportedAddressTypeException
    }
    val inetSocketAddr = socketAddress.asInstanceOf[InetSocketAddress]
    if (inetSocketAddr.isUnresolved) {
      throw new UnresolvedAddressException
    }
    inetSocketAddr
  }

  private class SocketAdapter(channel: SocketChannelImpl,
                              impl: PlainSocketImpl)
      extends Socket {

    created = true

    override def bind(bindpoint: SocketAddress): Unit = {
      try {
        channel.bind(bindpoint)
      } catch {
        case e: Exception => fromNioException(e)
      }
    }

    override def getChannel: SocketChannel = channel

    override def isBound: Boolean = channel.bound

    override def isConnected: Boolean =
      super.isConnected || channel.isConnected

    override def getLocalAddress: InetAddress = {
      if (channel.bound) {
        channel.localAddress.getAddress
      } else {
        null
      }
    }

    override def getLocalSocketAddress: SocketAddress = channel.localAddress

    override def getRemoteSocketAddress: SocketAddress = channel.remoteAddress

    override def getInetAddress(): InetAddress = {
      if (channel.remoteAddress == null && super.getInetAddress != null) {
        channel.remoteAddress =
          new InetSocketAddress(super.getInetAddress, super.getPort)
      }

      if (!isConnected || channel.remoteAddress == null) {
        null
      } else {
        channel.remoteAddress.getAddress
      }
    }

    override def getPort: Int = {
      if (channel.isConnected) {
        channel.remoteAddress.getPort
      } else {
        0
      }
    }

    override def getInputStream: InputStream = {
      if (!channel.isOpen) {
        throw new SocketException("Socket is closed")
      } else if (!channel.isConnected) {
        throw new SocketException("Socket is not connected")
      } else if (isInputShutdown) {
        throw new SocketException("Socket input is shutdown")
      }

      new SocketChannelInputStream(channel)
    }

    override def getOutputStream: OutputStream = {
      if (!channel.isOpen) {
        throw new SocketException("Socket is closed")
      } else if (!channel.isConnected) {
        throw new SocketException("Socket is not connected")
      } else if (isOutputShutdown) {
        throw new SocketException("Socket output is shutdown")
      }

      new SocketChannelOutputStream(channel)
    }

    override def getLocalPort: Int = {
      if (channel.bound) {
        channel.localAddress.getPort
      } else {
        -1
      }
    }

    override def close: Unit = channel.close

    override def isClosed: Boolean = !channel.isOpen

    override def connect(endpoint: SocketAddress): Unit =
      connect(endpoint, 0)

    override def connect(endpoint: SocketAddress, timeout: Int): Unit = {
      if (!channel.isBlocking) {
        throw new IllegalBlockingModeException
      }

      if (isConnected) {
        // Java docs don't mention this exception but both
        // Harmony and OpenJDK throw it anyway
        throw new AlreadyConnectedException
      }

      super.connect(endpoint, timeout)

      channel.bound = true
      channel.status = Status.Connected
      channel.localAddress =
        new InetSocketAddress(impl.localAddr, impl.localport)
      // checks for InetSocketAddress are done by super.connect
      channel.remoteAddress = endpoint.asInstanceOf[InetSocketAddress]
    }

    private def fromNioException(exception: Exception): Exception =
      exception match {
        // TODO
        case e: AlreadyBoundException => throw new BindException
      }
  }
}
