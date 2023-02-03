/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util
package concurrent.locks

import java.util.concurrent.TimeUnit

@SerialVersionUID(7373984872572414699L)
object ReentrantLock {

  @SerialVersionUID(-5179523762034025860L)
  abstract private[locks] class Sync extends AbstractQueuedSynchronizer {

    final private[locks] def tryLock(): Boolean = {
      val current = Thread.currentThread()
      var c = getState()
      if (c == 0) {
        if (compareAndSetState(0, 1)) {
          setExclusiveOwnerThread(current)
          return true
        }
      } else if (getExclusiveOwnerThread() eq current) {
        c += 1
        if (c < 0) { // overflow
          throw new Error("Maximum lock count exceeded")
        }
        setState(c)
        return true
      }
      false
    }

    private[locks] def initialTryLock(): Boolean

    final private[locks] def lock(): Unit = {
      if (!initialTryLock()) acquire(1)
    }

    @throws[InterruptedException]
    final private[locks] def lockInterruptibly(): Unit = {
      if (Thread.interrupted()) throw new InterruptedException
      if (!initialTryLock()) acquireInterruptibly(1)
    }

    @throws[InterruptedException]
    final private[locks] def tryLockNanos(nanos: Long) = {
      if (Thread.interrupted()) throw new InterruptedException
      initialTryLock() || tryAcquireNanos(1, nanos)
    }

    override final protected def tryRelease(releases: Int): Boolean = {
      val c = getState() - releases
      if (getExclusiveOwnerThread() ne Thread.currentThread())
        throw new IllegalMonitorStateException
      val free = c == 0
      if (free) setExclusiveOwnerThread(null)
      setState(c)
      free
    }

    override final protected[ReentrantLock] def isHeldExclusively(): Boolean = {
      // While we must in general read state before owner,
      // we don't need to do so to check if current thread is owner
      getExclusiveOwnerThread() eq Thread.currentThread()
    }

    final private[locks] def newCondition() = new ConditionObject()
    final private[locks] def getOwner() = {
      if (getState() == 0) null
      else getExclusiveOwnerThread()
    }

    final private[locks] def getHoldCount() = {
      if (isHeldExclusively()) getState()
      else 0
    }

    final private[locks] def isLocked() = getState() != 0

    //
    // @throws[java.io.IOException]
    // @throws[ClassNotFoundException]
    // private def readObject(s: ObjectInputStream): Unit = {
    //   s.defaultReadObject()
    //   setState(0) // reset to unlocked state
    // }
  }

  @SerialVersionUID(7316153563782823691L)
  final private[locks] class NonfairSync extends ReentrantLock.Sync {
    override final private[locks] def initialTryLock() = {
      val current = Thread.currentThread()
      if (compareAndSetState(0, 1)) { // first attempt is unguarded
        setExclusiveOwnerThread(current)
        true
      } else if (getExclusiveOwnerThread() eq current) {
        val c = getState() + 1
        if (c < 0) throw new Error("Maximum lock count exceeded")
        setState(c)
        true
      } else false
    }

    override final protected def tryAcquire(acquires: Int): Boolean = {
      if (getState() == 0 && compareAndSetState(0, acquires)) {
        setExclusiveOwnerThread(Thread.currentThread())
        true
      } else false
    }
  }

  @SerialVersionUID(-3000897897090466540L)
  final private[locks] class FairSync extends ReentrantLock.Sync {

    override final private[locks] def initialTryLock(): Boolean = {
      val current = Thread.currentThread()
      var c = getState()
      if (c == 0) {
        if (!hasQueuedThreads() && compareAndSetState(0, 1)) {
          setExclusiveOwnerThread(current)
          return true
        }
      } else if (getExclusiveOwnerThread() eq current) {
        if ({ c += 1; c } < 0) throw new Error("Maximum lock count exceeded")
        setState(c)
        return true
      }
      false
    }

    override final protected def tryAcquire(acquires: Int): Boolean = {
      if (getState() == 0 &&
          !hasQueuedPredecessors() &&
          compareAndSetState(0, acquires)) {
        setExclusiveOwnerThread(Thread.currentThread())
        true
      } else false
    }
  }
}

@SerialVersionUID(7373984872572414699L)
class ReentrantLock private (sync: ReentrantLock.Sync)
    extends Lock
    with Serializable {

  def this(fair: Boolean) = {
    this(
      if (fair) new ReentrantLock.FairSync
      else new ReentrantLock.NonfairSync
    )
  }

  def this() = this(false)

  override def lock(): Unit = sync.lock()

  @throws[InterruptedException]
  override def lockInterruptibly(): Unit = sync.lockInterruptibly()

  override def tryLock(): Boolean = sync.tryLock()

  @throws[InterruptedException]
  override def tryLock(timeout: Long, unit: TimeUnit): Boolean =
    sync.tryLockNanos(unit.toNanos(timeout))

  override def unlock(): Unit = { sync.release(1) }

  override def newCondition(): Condition = sync.newCondition()

  def getHoldCount(): Int = sync.getHoldCount()

  def isHeldByCurrentThread(): Boolean = sync.isHeldExclusively()

  def isLocked(): Boolean = sync.isLocked()

  final def isFair(): Boolean = sync.isInstanceOf[ReentrantLock.FairSync]

  protected def getOwner(): Thread = sync.getOwner()

  final def hasQueuedThreads(): Boolean = sync.hasQueuedThreads()

  final def hasQueuedThread(thread: Thread): Boolean = sync.isQueued(thread)

  final def getQueueLength(): Int = sync.getQueueLength()

  protected def getQueuedThreads(): Collection[Thread] = sync.getQueuedThreads()

  def hasWaiters(condition: Condition): Boolean = condition match {
    case null => throw new NullPointerException()
    case cond: AbstractQueuedSynchronizer#ConditionObject =>
      sync.hasWaiters(cond)
    case _ => throw new IllegalArgumentException("not owner")
  }

  def getWaitQueueLength(condition: Condition): Int = condition match {
    case null => throw new NullPointerException
    case cond: AbstractQueuedSynchronizer#ConditionObject =>
      sync.getWaitQueueLength(cond)
    case _ => throw new IllegalArgumentException("not owner")
  }

  protected def getWaitingThreads(condition: Condition): Collection[Thread] =
    condition match {
      case null => throw new NullPointerException
      case cond: AbstractQueuedSynchronizer#ConditionObject =>
        sync.getWaitingThreads(cond)
      case _ => throw new IllegalArgumentException("not owner")
    }

  override def toString(): String = {
    val o = sync.getOwner()
    super.toString() + (if (o == null) "[Unlocked]"
                        else s"[Locked by thread ${o.getName()}]")
  }
}
