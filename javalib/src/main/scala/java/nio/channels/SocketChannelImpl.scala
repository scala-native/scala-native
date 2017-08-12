package java.nio.channels

import java.nio.channels.spi._
import java.io.FileDescriptor

private[channels] class SocketChannelImpl(provider: SelectorProvider) 
extends SocketChannel(provider) with FileDescriptorHandler {

  var fd: FileDescriptor = new FileDescriptor(0)

  def implCloseSelectableChannel: Unit = ???

  def implConfigureBlocking(block: Boolean): Unit = ()

  def isRegistered: Boolean = ???

}
