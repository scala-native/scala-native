package java.nio.channels

import java.nio.channels.spi._

private[channels] final class SelectionKeyImpl(
    ch: AbstractSelectableChannel,
    private var operations: Int,
    private var att: Object,
    sel: SelectorImpl
) extends AbstractSelectionKey {

  private[channels] var readyOperations = 0

  attach(att)

  override def channel: SelectableChannel = ch

  override def interestOps(): Int = {
    checkValid
    sel.keysLock.synchronized {
      operations
    }
  }

  override def interestOps(ops: Int): SelectionKey = {
    checkValid
    if ((ops & ~ch.validOps) != 0) {
      throw new IllegalArgumentException
    }
    sel.keysLock.synchronized {
      operations = ops
      sel.modKey(this)
    }
    this
  }

  override def readyOps: Int = {
    checkValid
    readyOperations
  }

  override def selector: Selector = sel

  private def checkValid: Unit = {
    if (!isValid) {
      throw new CancelledKeyException
    }
  }
}
