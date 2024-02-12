package java.nio.channels


import java.nio.channels.spi._

abstract class SelectableChannel extends AbstractInterruptibleChannel with Channel {
  def provider: SelectorProvider
  def validOps: Int
  def isRegistered: Boolean
  def keyFor(sel: Selector): SelectionKey
  def register(sel: Selector, ops: Int, att: Object): SelectionKey
  def register(sel: Selector, ops: Int): SelectionKey =
    register(sel, ops, null)
  def configureBlocking(block: Boolean): SelectableChannel
  def isBlocking: Boolean
  def blockingLock: Object
}
