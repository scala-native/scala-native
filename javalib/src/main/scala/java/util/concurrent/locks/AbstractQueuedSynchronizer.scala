package java.util
package concurrent
package locks

abstract class AbstractQueuedSynchronizer extends AbstractOwnableSynchronizer {
  def acquire(arg: Int): Unit
  def acquireInterruptibly(arg: Int): Unit
  def acquireShared(arg: Int): Unit
  def acquireSharedInterruptibly(arg: Int): Unit

  def getFirstQueuedThread(): Thread
  
  def getExclusiveQueuedThreads(): Collection[Thread]
  def getQueuedThreads(): Collection[Thread]
  def getSharedQueuedThreads(): Collection[Thread]
  def getWaitingThreads(condition: ConditionObject): Collection[Thread]

  def getQueueLength(): Int
  def getWaitQueueLength(condition: ConditionObject): Int
  
  def hasContended(): Boolean
  def hasQueuedPredecessors(): Boolean
  def hasQueuedThreads(): Boolean
  def hasWaiters(condition: ConditionObject): Boolean
  def isQueued(thread: Thread): Boolean
  def owns(condition: ConditionObject): Boolean
  def release(arg: Int): Boolean
  def releaseShared(arg: Int): Boolean
  def tryAcquireNanos(arg: Int, nanosTimeout: Long): Boolean
  def tryAcquireSharedNanos(arg: Int, nanosTimeout: Long): Boolean
  
  protected def getState(): Int
  protected def compareAndSetState(expect: Int, update: Int): Boolean
  protected def isHeldExclusively(): Boolean
  protected def setState(newState: Int): Void
  protected def tryAcquire(arg: Int): Boolean
  protected def tryAcquireShared(arg: Int): Int
  protected def tryRelease(arg: Int): Boolean
  protected def tryReleaseShared(arg: Int): Boolean

  class ConditionObject {
    def await(): Unit = ???
    def awaitUninterruptibly(): Unit = ???
    def signal(): Unit = ???
    def signalAll(): Unit = ???

    def awaitNanos(nanosTimeout: Long): Long = ???
    def await(time: Long, unit: TimeUnit): Boolean = ???
    def awaitUntil(deadline: Date): Boolean = ???

    protected def getWaitingThreads(): Collection[Thread] = ???
    protected def getWaitQueueLength(): Int = ???
    protected def hasWaiters(): Boolean = ???
  }
}

