package java.nio.channels

import java.io.FileDescriptor
import java.net._
import java.nio.ByteBuffer
import java.nio.channels.spi.SelectorProvider
import java.util
class DatagramChannelImpl(family: ProtocolFamily, provider: SelectorProvider) extends DatagramChannel(provider) {

  private var isClosed: Boolean = false
  private var localAddress: SocketAddress = _
  private var remoteAddress: SocketAddress = _

  // TODO even if DGRAM, channel is stream ?
  private val fd: FileDescriptor = Net.socket(family, stream = true)
  private def throwIfClosed(): Unit = {
    if (isClosed) throw new ClosedChannelException()
  }
  override def bind(local: SocketAddress): DatagramChannel = ???

  override def setOption[T](name: SocketOption[T], value: T): DatagramChannel = ???
  override def socket(): DatagramSocket = ???

  override def isConnected(): Boolean = ???

  override def connect(remote: SocketAddress): DatagramChannel = ???

  override def disconnect(): DatagramChannel = ???

  override def getRemoteAddress(): SocketAddress = {
    throwIfClosed()
    remoteAddress
  }

  override def receive(dst: ByteBuffer): SocketAddress = ???

  override def send(src: ByteBuffer, target: SocketAddress): Int = ???

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

  override def write(srcs: Array[ByteBuffer], offset: Int, length: Int): Long = ???

  override def read(dsts: Array[ByteBuffer], offset: Int, length: Int): Long =
    ???

  override def read(dst: ByteBuffer): Int = ???

  override def getLocalAddress: SocketAddress = {
    throwIfClosed()
    localAddress
  }

  override protected def implConfigureBlocking(block: Boolean): Unit = Net.setSocketBlocking(fd, block)
  override def getOption[T](name: SocketOption[T]): T = ???

  override def supportedOptions: util.Set[SocketOption[_]] = ???

  override def write(src: ByteBuffer): Int = ???

}

object DatagramChannelImpl {

  private final val supportedFamilies: Set[ProtocolFamily] = Set(
    StandardProtocolFamily.INET,
    StandardProtocolFamily.INET6
  )

  def apply(provider: SelectorProvider): DatagramChannelImpl = {
    val family = SocketHelpers.getGaiHintsProtocolFamily()
    new DatagramChannelImpl(family, provider)
  }

  def apply(provider: SelectorProvider, family: ProtocolFamily): DatagramChannelImpl = {
    if (!supportedFamilies.contains(family)) throw new UnsupportedOperationException("Protocol family not supported")
    if (SocketHelpers.getUseIPv4Stack() && family == StandardProtocolFamily.INET6) throw new UnsupportedOperationException("IPv6 not available")
    new DatagramChannelImpl(family, provider)
  }
}
