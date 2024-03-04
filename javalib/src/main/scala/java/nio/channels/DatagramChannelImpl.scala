package java.nio.channels

import java.io.FileDescriptor
import java.net._
import java.nio.ByteBuffer
import java.nio.channels.spi.SelectorProvider
import java.util

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
  private var remoteAddress: SocketAddress = _

  // TODO even if DGRAM, channel is stream ?
  private val fd: FileDescriptor = Net.socket(family, stream = true)
  private val localAddress: SocketAddress = Net.localAddress(fd, family)

  @inline private def throwIfClosed(): Unit =
    if (!isOpen()) throw new ClosedChannelException()

  @inline private def blockingDetected = mapLastError(
    onUnix = { err => err == EAGAIN || err == EWOULDBLOCK },
    onWindows = { err => err == WSAEWOULDBLOCK || err == WSAETIMEDOUT }
  )

  override def bind(local: SocketAddress): DatagramChannel = {
    throwIfClosed()
    if (isBound) throw new AlreadyBoundException
    Net.bind(fd, local)
    isBound = true
    this
  }

  override def socket(): DatagramSocket = ???
  override def isConnected(): Boolean = remoteAddress != null

  override def connect(remote: SocketAddress): DatagramChannel = {
    if (isConnected()) throw new AlreadyConnectedException()
    if (!isBound) {
      Net.bind(fd, null)
      isBound = true
    }
    Net.connect(fd, remote)
    remoteAddress = remote
    // clear receive buffer
    // TODO blocking ?
    val buffer = ByteBuffer.allocate(100)
    while (read(buffer) > 0) {
      buffer.clear()
    }
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
      buf: ByteBuffer,
      flag: CInt,
      op: String
  ): SocketAddress = {
    val storage = stackalloc[posix.sys.socket.sockaddr_storage]()
    val destAddr = storage.asInstanceOf[Ptr[posix.sys.socket.sockaddr]]
    val addressLen = stackalloc[posix.sys.socket.socklen_t]()
    !addressLen = sizeof[posix.sys.socket.sockaddr_storage].toUInt

    val buffer = buf.array()
    val offset = buf.position()
    val length = buffer.length - offset

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

    bytesNum match {
      case _ if bytesNum >= 0 =>
        SocketHelpers.sockaddrStorageToInetSocketAddress(destAddr)
      case _ if !isBlocking && blockingDetected =>
        null
      case _ =>
        throw new SocketException(s"read failed, errno: ${lastError()}")
    }
  }

  override def receive(dst: ByteBuffer): SocketAddress = {
    throwIfClosed()
    recvfrom(dst, 0, "receive")
  }

  override def send(src: ByteBuffer, target: SocketAddress): Int = {
    throwIfClosed()
    val isa = Net.checkAddress(target)
    if (isConnected() && remoteAddress != isa)
      throw new AlreadyConnectedException()
    if (isa.getPort == 0) throw new SocketException("Can't send to port 0")

    if (!isBound) bind(null)

    val (sa, saLen) = Net.prepareSockaddrIn(isa)
    val pos = if (src.hasArray()) src.position() else 0
    val len = src.remaining()
    val cArr =
      if (src.hasArray()) src.array().at(pos)
      else {
        val bs = new Array[Byte](src.remaining())
        src.get(bs)
        src.position(src.position()) // reset position
        bs.at(0)
      }

    var ret = posix.sys.socket.sendto(
      fd.fd,
      cArr,
      len.toUInt,
      posix.sys.socket.MSG_NOSIGNAL,
      sa,
      saLen
    )

    if (isBlocking) {
      while (ret < 0 && blockingDetected) {
        ret = posix.sys.socket.sendto(
          fd.fd,
          cArr,
          len.toUInt,
          posix.sys.socket.MSG_NOSIGNAL,
          sa,
          saLen
        )
      }
    }

    if (ret < 0) {
      throw new SocketException(s"send failed, errno: ${lastError()}")
    }

    // check sent == len ?
    // datagram must send full buffer or error
    val sent = ret.toInt
    src.position(pos + sent)
    sent
  }

  override def join(
      group: InetAddress,
      networkInterface: NetworkInterface
  ): MembershipKey = ???

  override def join(
      group: InetAddress,
      networkInterface: NetworkInterface,
      source: InetAddress
  ): MembershipKey = ???

  override protected def implCloseSelectableChannel(): Unit = ???

  override def read(dst: ByteBuffer): Int =
    ???

  override def read(dsts: Array[ByteBuffer], offset: Int, length: Int): Long =
    ???

  override def write(src: ByteBuffer): Int = ???

  override def write(srcs: Array[ByteBuffer], offset: Int, length: Int): Long =
    ???

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
