package java.nio.channels

import java.net.{DatagramSocket, ProtocolFamily, SocketAddress, SocketOption}
import java.nio.ByteBuffer
import java.nio.channels.spi.{AbstractSelectableChannel, SelectorProvider}

abstract class DatagramChannel(provider: SelectorProvider)
    extends AbstractSelectableChannel(provider)
    with ByteChannel
    with ScatteringByteChannel
    with GatheringByteChannel
    with MulticastChannel {

  override def validOps: Int = ???

  def bind(local: SocketAddress): DatagramChannel
  def setOption[T](name: SocketOption[T], value: T): DatagramChannel
  def socket(): DatagramSocket
  def isConnected(): Boolean
  def connect(remote: SocketAddress): DatagramChannel
  def disconnect(): DatagramChannel
  def getRemoteAddress(): SocketAddress
  def receive(dst: java.nio.ByteBuffer): SocketAddress
  def send(src: java.nio.ByteBuffer, target: SocketAddress): Int

  final override def read(dsts: Array[ByteBuffer]): Long =
    read(dsts, 0, dsts.length)
  final override def write(srcs: Array[ByteBuffer]): Long =
    write(srcs, 0, srcs.length)
}

object DatagramChannel {
  def open(): DatagramChannel = SelectorProvider.provider.openDatagramChannel()

  def open(family: ProtocolFamily): DatagramChannel =
    SelectorProvider.provider.openDatagramChannel(family)
}
