package java.nio.channels.spi

import java.nio.channels._

import java.util.{Set => JSet}

import scala.collection.mutable.Set
import scala.collection.JavaConverters._

abstract class AbstractSelector(provider: SelectorProvider) extends Selector {

  // TODO this should be atomic boolean
  @volatile private var opened = true

  private val cancelledKeySet = Set.empty[SelectionKey]

  final protected[spi] def begin: Unit = ???

  protected[spi] def cancelledKeys: JSet[SelectionKey] = cancelledKeySet.asJava

  final def close: Unit = {
    if (opened) {
      implCloseSelector
      opened = false
    }
  }

  final protected[spi] def deregister(key: AbstractSelectionKey): Unit = {
    key.channel.asInstanceOf[AbstractSelectableChannel].deregister(key)
    key.valid = false
  }

  final protected[spi] def end: Unit = ???

  final def isOpen: Boolean = opened

  final def provider(): SelectorProvider = this.provider

  protected[spi] def implCloseSelector: Unit

  protected[spi] def register(ch: AbstractSelectableChannel,
                              ops: Int,
                              att: Object): SelectionKey

  private[spi] def cancel(key: SelectionKey): Unit =
    cancelledKeySet.synchronized {
      cancelledKeySet.add(key)
    }
}
