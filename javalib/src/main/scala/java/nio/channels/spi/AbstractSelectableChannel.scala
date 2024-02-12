package java.nio.channels.spi

import java.nio.channels.{CancelledKeyException, ClosedChannelException, IllegalBlockingModeException, IllegalSelectorException, SelectableChannel, SelectionKey, Selector}
import scala.collection.mutable.ArrayBuffer

abstract class AbstractSelectableChannel(val provider: SelectorProvider) extends SelectableChannel {

  private val keyList = new ArrayBuffer[SelectionKey]

  private val lock     = new Object
  private var blocking = true

  final override def isRegistered: Boolean = synchronized {
    !keyList.isEmpty
  }

  final override def keyFor(sel: Selector): SelectionKey = synchronized {
    keyList.find(key => key != null && key.selector == sel).orNull
  }

  final override def register(sel: Selector, ops: Int, att: Object): SelectionKey = {
    if (!isOpen()) throw new ClosedChannelException
    if (!((ops & ~validOps) == 0)) throw new IllegalArgumentException

    lock.synchronized {
      if (isBlocking) throw new IllegalBlockingModeException
      if (!sel.isOpen) {
        if (ops == 0) throw new IllegalSelectorException
        throw new NullPointerException
      }
      var key = keyFor(sel)
      if (key == null) {
        key = sel.asInstanceOf[AbstractSelector].register(this, ops, att)
        keyList += key
      } else {
        if (!key.isValid) throw new CancelledKeyException
        key.interestOps(ops)
        key.attach(att)
      }
      key
    }
  }

  final override def configureBlocking(block: Boolean): SelectableChannel = {
    if (isOpen() && blocking == block) {
      this
    } else if (isOpen()) {
      lock.synchronized {
        if (block && containsValidKeys) throw new IllegalBlockingModeException
        implConfigureBlocking(block)
        blocking = block
      }
      this
    } else {
      throw new ClosedChannelException
    }
  }

  final override def isBlocking: Boolean = lock.synchronized {
    blocking
  }

  final override def blockingLock: Object = lock

  protected def implCloseSelectableChannel(): Unit
  protected def implConfigureBlocking(block: Boolean): Unit

  protected[spi] final def implCloseChannel(): Unit = synchronized {
    implCloseSelectableChannel()
    keyList.foreach(key => if (key != null) key.cancel())
  }

  private def containsValidKeys: Boolean = synchronized {
    keyList.exists(key => key != null && key.isValid)
  }

  private[spi] def deregister(k: SelectionKey): Unit = synchronized {
    if (keyList != null) {
      keyList -= k
    }
  }

}
