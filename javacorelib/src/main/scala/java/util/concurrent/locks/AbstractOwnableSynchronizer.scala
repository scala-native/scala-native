package java.util.concurrent.locks

import java.io.Serializable

abstract class AbstractOwnableSynchronizer protected () extends Serializable {
  private var exclusiveOwner: Thread = _

  protected final def setExclusiveOwnerThread(thread: Thread): Unit =
    exclusiveOwner = thread
  protected final def getExclusiveOwnerThread(): Thread =
    exclusiveOwner
}
