package java.util
package concurrent.locks

import java.util.concurrent.TimeUnit

// Ported from Harmony

abstract class ReentrantLock extends Lock with java.io.Serializable {

  import ReentrantLock._

  private final var sync: Sync = new ReentrantLock.NonfairSync()

  def this(fair: Boolean) = {
    this()
    if (fair) sync = new ReentrantLock.FairSync()
  }

  def lock(): Unit = sync.lock()

  def lockInterruptibly(): Unit = sync.acquireInterruptibly(1)

  override def tryLock(): Boolean = sync.nonfairTryAcquire(1)

  override def tryLock(timeout: Long, unit: TimeUnit): Boolean =
    sync.tryAcquireNanos(1, unit.toNanos(timeout))

  def unlock(): Unit = sync.release(1)

  override def newCondition(): Condition = sync.newCondition()

  def getHoldCount: Int = sync.getHoldCount

  def isHeldByCurrentThread: Boolean = sync.isHeldExclusively

  def isLocked: Boolean = sync.isLocked

  def isFair: Boolean = sync.isInstanceOf[FairSync]

  protected def getOwner: Thread = sync.getOwner

  final def hasQueuedThreads: Boolean = sync.hasQueuedThreads

  final def hasQueuedThreads(thread: Thread): Boolean = sync.isQueued(thread)

  final def getQueueLength: Int = sync.getQueueLength

  protected def getQeueuedThreads: java.util.Collection[Thread] =
    sync.getQueuedThreads

  def hasWaiters(condition: Condition): Boolean = {
    if (condition == null)
      throw new NullPointerException()
    if (!condition.isInstanceOf[Sync#ConditionObject])
      throw new IllegalArgumentException("not owner")
    sync.hasWaiters(condition.asInstanceOf[Sync#ConditionObject])
  }

  def getWaitQueueLength(condition: Condition) = {
    if (condition == null)
      throw new NullPointerException()
    if (!condition.isInstanceOf[Sync#ConditionObject])
      throw new IllegalArgumentException("not owner")
    sync.getWaitQueueLength(condition.asInstanceOf[Sync#ConditionObject])
  }

  protected def getWaitingThreads(
      condition: Condition): java.util.Collection[Thread] = {
    if (condition == null)
      throw new NullPointerException()
    if (!condition.isInstanceOf[Sync#ConditionObject])
      throw new IllegalArgumentException("not owner")
    sync.getWaitingThreads(condition.asInstanceOf[Sync#ConditionObject])
  }

  override def toString: String = {
    val o: Thread = sync.getOwner
    val s: String = {
      if (o == null) "[Unlocked]"
      else "[Locked by thread " + o.getName + "]"
    }

    super.toString + s
  }

}

object ReentrantLock {

  private final val serialVersionUID: Long = 7373984872572414699L

  abstract class Sync extends AbstractQueuedSynchronizer { self =>

    def lock(): Unit

    final def nonfairTryAcquire(acquires: Int): Boolean = {
      val current: Thread = Thread.currentThread()
      val c: Int          = getState
      if (c == 0) {
        if (compareAndSetState(0, acquires)) {
          setExclusiveOwnerThread(current)
          return true
        }
      } else if (current == getExclusiveOwnerThread) {
        val nextc = c + acquires
        if (nextc < 0) throw new Error("Maximum lock count exceeded")
        setState(nextc)
        return true
      }
      false
    }

    override protected final def tryRelease(releases: Int): Boolean = {
      val c: Int = getState - releases
      if (Thread.currentThread() != getExclusiveOwnerThread)
        throw new IllegalMonitorStateException()
      var free: Boolean = false
      if (c == 0) {
        free = true
        setExclusiveOwnerThread(null)
      }
      setState(c)
      free
    }

    override protected[locks] final def isHeldExclusively: Boolean =
      getExclusiveOwnerThread == Thread.currentThread()

    final def newCondition(): ConditionObject = new ConditionObject()

    final def getOwner: Thread =
      if (getState == 0) null else getExclusiveOwnerThread

    final def getHoldCount: Int = if (isHeldExclusively()) getState else 0

    final def isLocked: Boolean = getState != 0

    private def readObject(s: java.io.ObjectInputStream): Unit = {
      s.defaultReadObject()
      setState(0)
    }

  }

  object Sync {

    private final val serialVersionUID: Long = -5179523762034025860L

  }

  final class NonfairSync extends Sync {

    def lock(): Unit = {
      if (compareAndSetState(0, 1))
        setExclusiveOwnerThread(Thread.currentThread())
      else
        acquire(1)
    }

    override protected def tryAcquire(acquires: Int): Boolean =
      nonfairTryAcquire(acquires)

  }

  object NonfairSync {

    private final val serialVersionUID: Long = 7316153563782823691L

  }

  final class FairSync extends Sync {

    def lock(): Unit = acquire(1)

    override protected def tryAcquire(acquires: Int): Boolean = {
      val current: Thread = Thread.currentThread()
      val c: Int          = getState
      if (c == 0) {
        if (!hasQueuedPredecessors &&
            compareAndSetState(0, acquires)) {
          setExclusiveOwnerThread(current)
          return true
        }
      } else if (current == getExclusiveOwnerThread) {
        val nextc: Int = c + acquires
        if (nextc < 0)
          throw new Error("Maximum lock count exceeded")
        setState(nextc)
        return true
      }
      false
    }

  }

  object FairSync {

    private final val serialVersionUID: Long = -3000897897090466540L

  }

}
