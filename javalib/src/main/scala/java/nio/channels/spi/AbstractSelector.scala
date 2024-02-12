package java.nio.channels.spi

import java.nio.channels.{SelectionKey, Selector}
import java.util.concurrent.atomic.AtomicBoolean
import scala.collection.mutable

abstract class AbstractSelector(override val provider: SelectorProvider) extends Selector {

  private val opened = new AtomicBoolean(true)

  private val cancelledKeySet = mutable.Set.empty[SelectionKey]

  final protected def begin(): Unit = ???

  final def close(): Unit = {
    if (opened.getAndSet(false)) {
      implCloseSelector()
    }
  }

  final protected def deregister(key: AbstractSelectionKey): Unit = {
    key.channel.asInstanceOf[AbstractSelectableChannel].deregister(key)
    key.valid = false
  }

  final protected def end(): Unit = ???

  final def isOpen: Boolean = opened.get()
  protected def implCloseSelector(): Unit

  private[nio] def register(ch: AbstractSelectableChannel, ops: Int, att: Object): SelectionKey
  private[nio] def cancel(key: SelectionKey): Unit =
    cancelledKeySet.synchronized {
      cancelledKeySet.add(key)
    }
}
