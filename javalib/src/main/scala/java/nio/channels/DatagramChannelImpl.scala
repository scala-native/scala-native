package java.nio.channels

import java.io.FileDescriptor
import java.net._
import java.nio.ByteBuffer
import java.nio.channels.spi.SelectorProvider
import java.util
import java.util.Objects
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix
import scala.scalanative.posix.errno.{EAGAIN, EWOULDBLOCK, errno}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.WinSocketApi.WSAGetLastError
import scala.scalanative.windows.WinSocketApiExt.{WSAETIMEDOUT, WSAEWOULDBLOCK}

class DatagramChannelImpl(family: ProtocolFamily, provider: SelectorProvider)
    extends DatagramChannel(provider) {

  private var isBound: Boolean = false
  private var localAddress: SocketAddress = _
  private var remoteAddress: SocketAddress = _

  private val fd: FileDescriptor = Net.socket(family, stream = false)

  @inline private def throwIfClosed(): Unit =
    if (!isOpen()) throw new ClosedChannelException()

  @inline private def throwIfNotConnected(): Unit =
    if (!isConnected()) throw new NotYetConnectedException()

  @inline private def blockingDetected = mapLastError(
    onUnix = { err => err == EAGAIN || err == EWOULDBLOCK },
    onWindows = { err => err == WSAEWOULDBLOCK || err == WSAETIMEDOUT }
  )

  private def flushQueue(): Unit = {
    val state = isBlocking
    configureBlocking(false)
    val buffer = ByteBuffer.allocate(100)
    while (recvfrom(buffer, 0, "connect")._1 > 0) {
      buffer.clear()
    }
    configureBlocking(state)
  }

  override def bind(local: SocketAddress): DatagramChannel = {
    throwIfClosed()
    if (isBound) throw new AlreadyBoundException
    Net.bind(fd, family, local)
    localAddress = Net.localAddress(fd, family)
    isBound = true
    this
  }

  override def socket(): DatagramSocket = ???
  override def isConnected(): Boolean = remoteAddress != null

  override def connect(remote: SocketAddress): DatagramChannel = {
    throwIfClosed()
    if (isConnected()) throw new AlreadyConnectedException()
    if (!isBound) {
      Net.bind(fd, family, null)
      isBound = true
    }
    Net.connect(fd, family, remote)
    remoteAddress = remote
    flushQueue()
    this
  }

  override def disconnect(): DatagramChannel = {
    throwIfClosed()
    if (isConnected()) {
      Net.disconnect(fd)
      remoteAddress = null
    }
    this
  }

  override def getRemoteAddress(): SocketAddress = {
    throwIfClosed()
    remoteAddress
  }

  private def recvfrom(
      dst: ByteBuffer,
      flag: CInt,
      op: String
  ): (Int, SocketAddress) = {
    val storage = stackalloc[posix.sys.socket.sockaddr_storage]()
    val destAddr = storage.asInstanceOf[Ptr[posix.sys.socket.sockaddr]]
    val addressLen = stackalloc[posix.sys.socket.socklen_t]()
    !addressLen = sizeof[posix.sys.socket.sockaddr_storage].toUInt

    val buffer = dst.array()
    val offset = dst.position()
    val length = buffer.length - offset

    val n = posix.sys.socket
      .recvfrom(
        fd.fd,
        buffer.at(offset),
        length.toUInt,
        flag,
        destAddr,
        addressLen
      )
      .toInt

    n match {
      case _ if n >= 0 =>
        dst.position(offset + n)
        (n, SocketHelpers.sockaddrStorageToInetSocketAddress(destAddr))
      case _ if !isBlocking && blockingDetected =>
        (0, null)
      case _ =>
        throw new SocketException(s"$op failed, errno: ${lastError()}")
    }
  }

  private def recv(
      dst: ByteBuffer,
      flag: CInt,
      op: String
  ): Int = {
    val buffer = dst.array()
    val offset = dst.position()
    val length = buffer.length - offset

    val n = posix.sys.socket
      .recv(
        fd.fd,
        buffer.at(offset),
        length.toUInt,
        flag
      )
      .toInt

    n match {
      case _ if n >= 0 =>
        dst.position(offset + n)
        n
      case _ if !isBlocking && blockingDetected =>
        0
      case _ =>
        throw new SocketException(s"$op failed, errno: ${lastError()}")
    }
  }

  override def receive(dst: ByteBuffer): SocketAddress = {
    throwIfClosed()
    if (!isBound) bind(null)
    val (_, source) = recvfrom(dst, 0, "receive")
    source
  }

  private def sendto(
      src: ByteBuffer,
      target: SocketAddress,
      op: String
  ): Int = {
    val buffer = src.array()
    val offset = src.position()
    val length = src.remaining()

    val cArr = buffer.at(offset)
    val cLen = length.toUInt

    val bytesSent = Zone.acquire { implicit z =>
      val isa = target.asInstanceOf[InetSocketAddress]
      val (sa, saLen) = Net.prepareSockaddrIn(family, isa)
      var n = posix.sys.socket
        .sendto(
          fd.fd,
          cArr,
          cLen,
          posix.sys.socket.MSG_NOSIGNAL,
          sa,
          saLen
        )
        .toInt
      while (n < 0 && isBlocking && blockingDetected) {
        n = posix.sys.socket
          .sendto(
            fd.fd,
            cArr,
            cLen,
            posix.sys.socket.MSG_NOSIGNAL,
            sa,
            saLen
          )
          .toInt
      }
      n
    }

    if (bytesSent < 0) {
      throw new SocketException(s"$op failed, errno: ${lastError()}")
    }

    // check sent == len ?
    // datagram must send full buffer or error
    src.position(offset + bytesSent)
    bytesSent
  }

  private def send(
      src: ByteBuffer,
      op: String
  ): Int = {
    val buffer = src.array()
    val offset = src.position()
    val length = src.remaining()

    val cArr = buffer.at(offset)
    val cLen = length.toUInt

    var bytesSent = posix.sys.socket
      .send(
        fd.fd,
        cArr,
        cLen,
        posix.sys.socket.MSG_NOSIGNAL
      )
      .toInt
    while (bytesSent < 0 && isBlocking && blockingDetected) {
      bytesSent = posix.sys.socket
        .send(
          fd.fd,
          cArr,
          cLen,
          posix.sys.socket.MSG_NOSIGNAL
        )
        .toInt
    }

    if (bytesSent < 0) {
      throw new SocketException(s"$op failed, errno: ${lastError()}")
    }

    // check sent == len ?
    // datagram must send full buffer or error
    src.position(offset + bytesSent)
    bytesSent
  }

  override def send(src: ByteBuffer, target: SocketAddress): Int = {
    throwIfClosed()
    val isa = Net.checkAddress(target)
    if (isConnected() && remoteAddress != isa)
      throw new AlreadyConnectedException()
    if (isa.getPort == 0) throw new SocketException("Can't send to port 0")

    if (!isBound) bind(null)

    sendto(src, isa, "send")
  }

  override def join(
      group: InetAddress,
      networkInterface: NetworkInterface
  ): MembershipKey = join0(group, networkInterface, null)

  override def join(
      group: InetAddress,
      networkInterface: NetworkInterface,
      source: InetAddress
  ): MembershipKey = {
    Objects.requireNonNull(source)
    join0(group, networkInterface, source)
  }

  private def join0(
      group: InetAddress,
      networkInterface: NetworkInterface,
      source: InetAddress
  ): MembershipKey = {
    throwIfClosed()
    Net.join(fd, family, group, networkInterface, source)
    // TODO implement membership registry
    new MembershipKeyImpl(this, group, networkInterface, source)
  }

  private[channels] def drop(key: MembershipKeyImpl): Unit = {
    key.invalidate()
    Net.drop(
      fd,
      family,
      key.group(),
      key.networkInterface(),
      key.sourceAddress()
    )
  }

  override protected def implCloseSelectableChannel(): Unit = {
    Net.close(fd)
  }

  override def read(dst: ByteBuffer): Int = {
    throwIfClosed()
    throwIfNotConnected()
    recv(dst, 0, "read")
  }

  override def read(dsts: Array[ByteBuffer], offset: Int, length: Int): Long = {
    Objects.checkFromIndexSize(offset, length, dsts.length)
    throwIfClosed()
    throwIfNotConnected()

    var bytesRead = 0L
    var partialRead = false
    var i = 0

    while (i < length && !partialRead) {
      val dst = dsts(offset + i)
      val len = dst.remaining()
      val n = recv(dst, 0, "read")

      bytesRead += n
      if (n < len) {
        partialRead = true
      }
      i += 1
    }

    bytesRead
  }

  override def write(src: ByteBuffer): Int = {
    throwIfClosed()
    throwIfNotConnected()

    send(src, "write")
  }

  override def write(
      srcs: Array[ByteBuffer],
      offset: Int,
      length: Int
  ): Long = {
    Objects.checkFromIndexSize(offset, length, srcs.length)
    throwIfClosed()
    throwIfNotConnected()

    var bytesWritten = 0L
    var partialWrite = false
    var i = 0

    while (i < length && !partialWrite) {
      val src = srcs(offset + i)
      val len = src.remaining()
      val (n, _) = recvfrom(src, 0, "write")

      bytesWritten += n
      if (n < len) {
        partialWrite = true
      }
      i += 1
    }

    bytesWritten
  }

  override def getLocalAddress: SocketAddress = {
    throwIfClosed()
    localAddress
  }

  override protected def implConfigureBlocking(block: Boolean): Unit =
    Net.configureBlocking(fd, block)

  override def setOption[T](
      name: SocketOption[T],
      value: T
  ): DatagramChannel = {
    // TODO check if option is supported
    throwIfClosed()
    Net.setSocketOption(fd, name, value)
    this
  }
  override def getOption[T](name: SocketOption[T]): T = {
    // TODO check if option is supported
    throwIfClosed()
    Net.getSocketOption(fd, name)
  }

  override def supportedOptions: util.Set[SocketOption[_]] = ???

  private def lastError(): CInt = mapLastError(identity, identity)
  private def mapLastError[T](
      onUnix: CInt => T,
      onWindows: CInt => T
  ): T = {
    if (isWindows) onWindows(WSAGetLastError())
    else onUnix(errno)
  }
}

object DatagramChannelImpl {

  def apply(provider: SelectorProvider): DatagramChannelImpl = {
    val family = Net.getGaiHintsProtocolFamily()
    new DatagramChannelImpl(family, provider)
  }

  def apply(
      provider: SelectorProvider,
      family: ProtocolFamily
  ): DatagramChannelImpl =
    new DatagramChannelImpl(family, provider)
}
