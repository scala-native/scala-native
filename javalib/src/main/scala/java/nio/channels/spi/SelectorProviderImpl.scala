package java.nio.channels.spi

import java.nio.channels._
import java.net.ProtocolFamily

private[spi] class SelectorProviderImpl extends SelectorProvider {

  def openDatagramChannel(): DatagramChannel = DatagramChannelImpl(this)
  def openDatagramChannel(family: ProtocolFamily): DatagramChannel =
    DatagramChannelImpl(this)
  def openPipe(): Pipe = ???
  def openSelector(): AbstractSelector = new SelectorImpl(this)
  def openServerSocketChannel(): ServerSocketChannel = ???
  def openSocketChannel(): SocketChannel = ???

}
