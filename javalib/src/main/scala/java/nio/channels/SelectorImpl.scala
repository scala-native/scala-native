package java.nio.channels

import java.nio.channels.spi.{AbstractSelectableChannel, AbstractSelector, SelectorProvider}
import java.util

class SelectorImpl(provider: SelectorProvider)
  extends AbstractSelector(provider) {

  override protected def implCloseSelector(): Unit = ???

  override private[nio] def register(ch: AbstractSelectableChannel, ops: Int, att: Object): SelectionKey = ???

  override def keys: util.Set[SelectionKey] = ???

  override def select: Int = ???

  override def select(timeout: Long): Int = ???

  override def selectedKeys: util.Set[SelectionKey] = ???

  override def selectNow: Int = ???

  override def wakeup(): Selector = ???
}
