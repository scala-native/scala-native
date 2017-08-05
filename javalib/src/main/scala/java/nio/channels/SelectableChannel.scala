package java.nio.channels

import java.nio.channels.spi._

abstract class SelectableChannel protected[channels] ()
    extends AbstractInterruptibleChannel
    with Channel {

  def blockingLock: Object
  def configureBlocking(block: Boolean): SelectableChannel
  def isBlocking: Boolean
  def isRegistered: Boolean
  def keyFor(sel: Selector): SelectionKey
  def provider: SelectorProvider
  def register(sel: Selector, ops: Int, att: Object): SelectionKey
  def validOps: Int

  def register(sel: Selector, ops: Int): SelectionKey =
    register(sel, ops, null)

}
