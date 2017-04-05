package java.util.concurrent.locks

import java.io.Serializable

abstract class AbstractOwnableSynchronizer() extends Serializable {
  protected def getExclusiveOwnerThread(): Thread
  protected def setExclusiveOwnerThread(thread: Thread): Unit
}