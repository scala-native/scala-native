package java.nio.channels.spi

import java.nio.channels._
import java.net.ProtocolFamily

abstract class SelectorProvider protected () {

  def openDatagramChannel(): DatagramChannel
  def openDatagramChannel(family: ProtocolFamily): DatagramChannel
  def openPipe(): Pipe
  def openSelector(): AbstractSelector
  def openServerSocketChannel(): ServerSocketChannel
  def openSocketChannel(): SocketChannel

  // There is no JVM, so there is no way to inherit this, hence null
  def inheritedChannel: Channel = null
}

object SelectorProvider {

  // Scala Native does not support class loading, so we
  // just return the default provider
  lazy val provider: SelectorProvider = new SelectorProviderImpl

}
