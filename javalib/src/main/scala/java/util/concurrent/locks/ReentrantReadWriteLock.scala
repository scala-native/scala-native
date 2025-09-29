/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.locks

import java.util
import java.util.concurrent.TimeUnit
import scala.scalanative.annotation.safePublish

object ReentrantReadWriteLock {

  private[locks] object Sync {
    final val SHARED_SHIFT = 16
    final val SHARED_UNIT = (1 << SHARED_SHIFT)
    final val MAX_COUNT = (1 << SHARED_SHIFT) - 1
    final val EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1

    final def sharedCount(c: Int): Int = c >>> SHARED_SHIFT

    final def exclusiveCount(c: Int): Int = c & EXCLUSIVE_MASK

    final class HoldCounter {
      var count: Int = 0 // initially 0

      // Use id, not reference, to avoid garbage retention
      final val tid: Long = LockSupport.getThreadId(Thread.currentThread())
    }

    final class ThreadLocalHoldCounter extends ThreadLocal[Sync.HoldCounter] {
      override def initialValue(): Sync.HoldCounter = new Sync.HoldCounter
    }
    private def unmatchedUnlockException: IllegalMonitorStateException =
      new IllegalMonitorStateException(
        "attempt to unlock read lock, not locked by current thread"
      )
  }

  private[locks] abstract class Sync() extends AbstractQueuedSynchronizer {

    private var readHolds = new Sync.ThreadLocalHoldCounter

    setState(getState()) // ensures visibility of readHolds

    private var cachedHoldCounter: Sync.HoldCounter = _

    private var firstReader: Thread = _
    private var firstReaderHoldCount: Int = 0

    def readerShouldBlock: Boolean

    def writerShouldBlock: Boolean
    /*
     * Note that tryRelease and tryAcquire can be called by
     * Conditions. So it is possible that their arguments contain
     * both read and write holds that are all released during a
     * condition wait and re-established in tryAcquire.
     */
    override protected final def tryRelease(releases: Int): Boolean = {
      if (!(isHeldExclusively())) throw new IllegalMonitorStateException
      val nextc: Int = getState() - releases
      val free: Boolean = Sync.exclusiveCount(nextc) == 0
      if (free) setExclusiveOwnerThread(null)
      setState(nextc)
      free
    }

    override protected final def tryAcquire(acquires: Int): Boolean = {
      /*
       * Walkthrough:
       * 1. If read count nonzero or write count nonzero
       *    and owner is a different thread, fail.
       * 2. If count would saturate, fail. (This can only
       *    happen if count is already nonzero.)
       * 3. Otherwise, this thread is eligible for lock if
       *    it is either a reentrant acquire or
       *    queue policy allows it. If so, update state
       *    and set owner.
       */
      val current: Thread = Thread.currentThread()
      val c: Int = getState()
      val w: Int = Sync.exclusiveCount(c)
      if (c != 0) { // (Note: if c != 0 and w == 0 then shared count != 0)
        if (w == 0 || (current ne getExclusiveOwnerThread())) return false
        if (w + Sync.exclusiveCount(acquires) > Sync.MAX_COUNT)
          throw new Error("Maximum lock count exceeded")
        // Reentrant acquire
        setState(c + acquires)
        return true
      }
      if (writerShouldBlock || !(compareAndSetState(c, c + acquires))) {
        return false
      }
      setExclusiveOwnerThread(current)
      true
    }

    override protected final def tryReleaseShared(
        unused: Int
    ): Boolean = {
      val current: Thread = Thread.currentThread()
      if (firstReader eq current) { // assert firstReaderHoldCount > 0;
        if (firstReaderHoldCount == 1) { firstReader = null }
        else { firstReaderHoldCount -= 1 }
      } else {
        var rh: Sync.HoldCounter = cachedHoldCounter
        if (rh == null || rh.tid != LockSupport.getThreadId(current)) {
          rh = readHolds.get()
        }
        val count: Int = rh.count
        if (count <= 1) {
          readHolds.remove()
          if (count <= 0) { throw Sync.unmatchedUnlockException }
        }
        rh.count -= 1
      }

      while (true) {
        val c: Int = getState()
        val nextc: Int = c - Sync.SHARED_UNIT
        if (compareAndSetState(c, nextc)) {
          // Releasing the read lock has no effect on readers,
          // but it may allow waiting writers to proceed if
          // both read and write locks are now free.
          return nextc == 0
        }
      }
      false // unreachable
    }

    override protected final def tryAcquireShared(unused: Int): Int = {
      /*
       * Walkthrough:
       * 1. If write lock held by another thread, fail.
       * 2. Otherwise, this thread is eligible for
       *    lock wrt state, so ask if it should block
       *    because of queue policy. If not, try
       *    to grant by CASing state and updating count.
       *    Note that step does not check for reentrant
       *    acquires, which is postponed to full version
       *    to avoid having to check hold count in
       *    the more typical non-reentrant case.
       * 3. If step 2 fails either because thread
       *    apparently not eligible or CAS fails or count
       *    saturated, chain to version with full retry loop.
       */
      val current: Thread = Thread.currentThread()
      val c: Int = getState()
      if (Sync.exclusiveCount(c) != 0 &&
          (getExclusiveOwnerThread() ne current)) {
        return -(1)
      }

      val r: Int = Sync.sharedCount(c)
      if (!readerShouldBlock &&
          r < Sync.MAX_COUNT &&
          compareAndSetState(c, c + Sync.SHARED_UNIT)) {
        if (r == 0) {
          firstReader = current
          firstReaderHoldCount = 1
        } else {
          if (firstReader eq current) { firstReaderHoldCount += 1 }
          else {
            var rh: Sync.HoldCounter = cachedHoldCounter
            if (rh == null || rh.tid != LockSupport.getThreadId(current)) {
              rh = readHolds.get()
              cachedHoldCounter = rh
            } else if (rh.count == 0) readHolds.set(rh)
            rh.count += 1
          }
        }
        return 1
      }
      fullTryAcquireShared(current)
    }

    private[locks] final def fullTryAcquireShared(current: Thread): Int = {
      /*
       * This code is in part redundant with that in
       * tryAcquireShared but is simpler overall by not
       * complicating tryAcquireShared with interactions between
       * retries and lazily reading hold counts.
       */
      var rh: Sync.HoldCounter = null

      while (true) {
        val c: Int = getState()
        if (Sync.exclusiveCount(c) != 0) {
          if (getExclusiveOwnerThread() ne current) return -1
          // else we hold the exclusive lock; blocking here
          // would cause deadlock.
        } else {
          if (readerShouldBlock) { // Make sure we're not acquiring read lock reentrantly
            if (firstReader ne current) {
              if (rh == null) {
                rh = cachedHoldCounter
                if (rh == null || rh.tid != LockSupport.getThreadId(current)) {
                  rh = readHolds.get()
                  if (rh.count == 0) { readHolds.remove() }
                }
              }
              if (rh.count == 0) { return -(1) }
            }
          }
        }
        if (Sync.sharedCount(c) == Sync.MAX_COUNT) {
          throw new Error("Maximum lock count exceeded")
        }
        if (compareAndSetState(c, c + Sync.SHARED_UNIT)) {
          if (Sync.sharedCount(c) == 0) {
            firstReader = current
            firstReaderHoldCount = 1
          } else {
            if (firstReader eq current) { firstReaderHoldCount += 1 }
            else {
              if (rh == null) { rh = cachedHoldCounter }
              if (rh == null || rh.tid != LockSupport.getThreadId(current)) {
                rh = readHolds.get()
              } else { if (rh.count == 0) { readHolds.set(rh) } }
              rh.count += 1
              cachedHoldCounter = rh // cache for release

            }
          }
          return 1
        }
      }
      -1 // unreachable
    }

    private[locks] final def tryWriteLock: Boolean = {
      val current: Thread = Thread.currentThread()
      val c: Int = getState()
      if (c != 0) {
        val w: Int = Sync.exclusiveCount(c)
        if (w == 0 || (current ne getExclusiveOwnerThread())) { return false }
        if (w == Sync.MAX_COUNT) {
          throw new Error("Maximum lock count exceeded")
        }
      }
      if (!compareAndSetState(c, c + 1)) false
      else {
        setExclusiveOwnerThread(current)
        true
      }
    }

    private[locks] final def tryReadLock: Boolean = {
      val current: Thread = Thread.currentThread()

      while (true) {
        val c: Int = getState()
        if (Sync.exclusiveCount(c) != 0 &&
            (getExclusiveOwnerThread() ne current)) {
          return false
        }
        val r: Int = Sync.sharedCount(c)
        if (r == Sync.MAX_COUNT) {
          throw new Error("Maximum lock count exceeded")
        }
        if (compareAndSetState(c, c + Sync.SHARED_UNIT)) {
          if (r == 0) {
            firstReader = current
            firstReaderHoldCount = 1
          } else {
            if (firstReader eq current) { firstReaderHoldCount += 1 }
            else {
              var rh: Sync.HoldCounter = cachedHoldCounter
              if (rh == null || rh.tid != LockSupport.getThreadId(current)) {
                rh = readHolds.get()
                cachedHoldCounter = rh
              } else if (rh.count == 0) { readHolds.set(rh) }
              rh.count += 1
            }
          }
          return true
        }
      }
      false // unreachable
    }

    override protected[ReentrantReadWriteLock] final def isHeldExclusively()
        : Boolean = {
      // While we must in general read state before owner,
      // we don't need to do so to check if current thread is owner
      getExclusiveOwnerThread() eq Thread.currentThread()
    }

    private[locks] final def newCondition: ConditionObject = new ConditionObject

    private[locks] final def getOwner: Thread = {
      // Must read state before owner to ensure memory consistency
      if (Sync.exclusiveCount(getState()) == 0) null
      else getExclusiveOwnerThread()
    }

    private[locks] final def getReadLockCount: Int =
      Sync.sharedCount(getState())

    private[locks] final def isWriteLocked: Boolean =
      Sync.exclusiveCount(getState()) != 0

    private[locks] final def getWriteHoldCount: Int =
      if (isHeldExclusively()) Sync.exclusiveCount(getState())
      else 0

    private[locks] final def getReadHoldCount: Int = {
      if (getReadLockCount == 0) return 0
      val current: Thread = Thread.currentThread()
      if (firstReader eq current) return firstReaderHoldCount
      val rh: Sync.HoldCounter = cachedHoldCounter
      if (rh != null && rh.tid == LockSupport.getThreadId(current)) {
        return rh.count
      }
      val count: Int = readHolds.get().count
      if (count == 0) readHolds.remove()
      count
    }

    private[locks] final def getCount: Int = getState()
  }

  private[locks] final class NonfairSync extends ReentrantReadWriteLock.Sync {
    override final def writerShouldBlock: Boolean =
      false // writers can always barge
    override final def readerShouldBlock: Boolean = {
      /* As a heuristic to avoid indefinite writer starvation,
       * block if the thread that momentarily appears to be head
       * of queue, if one exists, is a waiting writer.  This is
       * only a probabilistic effect since a new reader will not
       * block if there is a waiting writer behind other enabled
       * readers that have not yet drained from the queue.
       */
      apparentlyFirstQueuedIsExclusive()
    }
  }

  private[locks] final class FairSync extends ReentrantReadWriteLock.Sync {
    override final def writerShouldBlock: Boolean = hasQueuedPredecessors()
    override final def readerShouldBlock: Boolean = hasQueuedPredecessors()
  }

  class ReadLock private (
      @safePublish private final val sync: ReentrantReadWriteLock.Sync
  ) extends Lock
      with Serializable {
    protected[ReentrantReadWriteLock] def this(lock: ReentrantReadWriteLock) =
      this(lock.sync)

    override def lock(): Unit = sync.acquireShared(1)

    @throws[InterruptedException]
    override def lockInterruptibly(): Unit = sync.acquireSharedInterruptibly(1)

    override def tryLock(): Boolean = sync.tryReadLock

    @throws[InterruptedException]
    override def tryLock(timeout: Long, unit: TimeUnit): Boolean =
      sync.tryAcquireSharedNanos(1, unit.toNanos(timeout))

    override def unlock(): Unit = { sync.releaseShared(1) }

    override def newCondition(): Condition =
      throw new UnsupportedOperationException

    override def toString: String = {
      val r: Int = sync.getReadLockCount
      return super.toString + "[Read locks = " + r + "]"
    }
  }

  class WriteLock private (private final val sync: ReentrantReadWriteLock.Sync)
      extends Lock
      with Serializable {
    protected[ReentrantReadWriteLock] def this(lock: ReentrantReadWriteLock) =
      this(lock.sync)

    override def lock(): Unit = sync.acquire(1)

    @throws[InterruptedException]
    override def lockInterruptibly(): Unit = sync.acquireInterruptibly(1)

    override def tryLock(): Boolean = sync.tryWriteLock

    @throws[InterruptedException]
    override def tryLock(timeout: Long, unit: TimeUnit): Boolean =
      sync.tryAcquireNanos(1, unit.toNanos(timeout))

    override def unlock(): Unit = { sync.release(1) }

    override def newCondition(): Condition = sync.newCondition

    override def toString(): String = {
      val o: Thread = sync.getOwner
      super.toString() + {
        if (o == null) "[Unlocked]"
        else "[Locked by thread " + o.getName() + "]"
      }
    }

    def isHeldByCurrentThread: Boolean = sync.isHeldExclusively()

    def getHoldCount: Int = sync.getWriteHoldCount
  }
}

class ReentrantReadWriteLock(val fair: Boolean)
    extends ReadWriteLock
    with Serializable {
  def this() = this(false)

  private[locks] final val sync: ReentrantReadWriteLock.Sync =
    if (fair) new ReentrantReadWriteLock.FairSync
    else new ReentrantReadWriteLock.NonfairSync

  @safePublish
  private final val readerLock = new ReentrantReadWriteLock.ReadLock(this)

  @safePublish
  private final val writerLock = new ReentrantReadWriteLock.WriteLock(this)

  override def writeLock(): ReentrantReadWriteLock.WriteLock = this.writerLock
  override def readLock(): ReentrantReadWriteLock.ReadLock = this.readerLock

  final def isFair: Boolean = sync.isInstanceOf[ReentrantReadWriteLock.FairSync]

  protected def getOwner: Thread = sync.getOwner

  def getReadLockCount: Int = sync.getReadLockCount

  def isWriteLocked: Boolean = sync.isWriteLocked

  def isWriteLockedByCurrentThread: Boolean = sync.isHeldExclusively()

  def getWriteHoldCount: Int = sync.getWriteHoldCount

  def getReadHoldCount: Int = sync.getReadHoldCount

  protected def getQueuedWriterThreads: util.Collection[Thread] =
    sync.getExclusiveQueuedThreads()

  protected def getQueuedReaderThreads: util.Collection[Thread] =
    sync.getSharedQueuedThreads()

  final def hasQueuedThreads: Boolean = sync.hasQueuedThreads()

  final def hasQueuedThread(thread: Thread): Boolean = sync.isQueued(thread)

  final def getQueueLength: Int = sync.getQueueLength()

  protected def getQueuedThreads: util.Collection[Thread] =
    sync.getQueuedThreads()

  def hasWaiters(condition: Condition): Boolean = condition match {
    case cond: AbstractQueuedSynchronizer#ConditionObject =>
      sync.hasWaiters(cond)
    case null => throw new NullPointerException
    case _    => throw new IllegalArgumentException("not owner")
  }

  def getWaitQueueLength(condition: Condition): Int = condition match {
    case cond: AbstractQueuedSynchronizer#ConditionObject =>
      sync.getWaitQueueLength(cond)
    case null => throw new NullPointerException
    case _    => throw new IllegalArgumentException("not owner")
  }

  protected def getWaitingThreads(
      condition: Condition
  ): util.Collection[Thread] =
    condition match {
      case cond: AbstractQueuedSynchronizer#ConditionObject =>
        sync.getWaitingThreads(cond)
      case null => throw new NullPointerException
      case _    => throw new IllegalArgumentException("not owner")
    }

  override def toString(): String = {
    val c: Int = sync.getCount
    val w: Int = ReentrantReadWriteLock.Sync.exclusiveCount(c)
    val r: Int = ReentrantReadWriteLock.Sync.sharedCount(c)
    super.toString() + "[Write locks = " + w + ", Read locks = " + r + "]"
  }
}
