package java.util.concurrent
package locks

abstract class AbstractOwnableSynchronizer extends java.io.Serializable {

  private var exclusiveOwnerThread: Thread = _

  protected final def setExclusiveOwnerThread(t: Thread): Unit =
    exclusiveOwnerThread = t

  protected final def getExclusiveOwnerThread: Thread = exclusiveOwnerThread

}

object AbstractOwnableSynchronizer {

  private final val serialVersionUID: Long = 3737899427754241961L

}
