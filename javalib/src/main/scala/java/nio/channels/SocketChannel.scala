package java.nio.channels

import java.nio._
import java.nio.channels.spi._

import java.net.{SocketAddress, Socket, SocketOption, StandardSocketOptions}

abstract class SocketChannel protected[channels] (provider: SelectorProvider)
    extends AbstractSelectableChannel(provider)
    with ByteChannel
    with ScatteringByteChannel
    with GatheringByteChannel
    with NetworkChannel {

  def bind(local: SocketAddress): SocketChannel
  def connect(remote: SocketAddress): Boolean
  def finishConnect: Boolean
  def getLocalAddress: SocketAddress
  def getRemoteAddress: SocketAddress
  def isConnected: Boolean
  def isConnectionPending: Boolean
  def read(dst: ByteBuffer): Int
  def read(dsts: Array[ByteBuffer], offset: Int, length: Int): Long
  def shutdownInput: SocketChannel
  def shutdownOutput: SocketChannel
  def socket: Socket
  def write(src: ByteBuffer): Int
  def write(srcs: Array[ByteBuffer], offset: Int, length: Int): Long

  final def validOps: Int =
    SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE

  lazy val supportedOptions: java.util.Set[SocketOption[_]] = {
    val set = new java.util.HashSet[SocketOption[_]](6)
    /*set.add(StandardSocketOptions.SO_SNDBUF)
    set.add(StandardSocketOptions.SO_RCVBUF)
    set.add(StandardSocketOptions.SO_KEEPALIVE)
    set.add(StandardSocketOptions.SO_REUSEADDR)
    set.add(StandardSocketOptions.SO_LINGER)
    set.add(StandardSocketOptions.TCP_NODELAY)*/
    set
  }

  final def read(dsts: Array[ByteBuffer]): Long = synchronized {
    read(dsts, 0, dsts.length)
  }

  final def write(srcs: Array[ByteBuffer]): Long = synchronized {
    write(srcs, 0, srcs.length)
  }
}

object SocketChannel {

  def open: SocketChannel = SelectorProvider.provider.openSocketChannel

  def open(remote: SocketAddress): SocketChannel = {
    val socketChannel = open
    if (socketChannel != null) {
      socketChannel.connect(remote)
    }
    socketChannel
  }
}
