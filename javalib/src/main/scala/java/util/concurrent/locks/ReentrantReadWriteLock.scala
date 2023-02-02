/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.locks

import java.util
import java.util.concurrent.TimeUnit

object ReentrantReadWriteLock {

  /** Synchronization implementation for ReentrantReadWriteLock. Subclassed into
   *  fair and nonfair versions.
   */
  private[locks] object Sync {
    final val SHARED_SHIFT: Int = 16
    final val SHARED_UNIT: Int = (1 << SHARED_SHIFT)
    final val MAX_COUNT: Int = (1 << SHARED_SHIFT) - 1
    final val EXCLUSIVE_MASK: Int = (1 << SHARED_SHIFT) - 1

    /** Returns the number of shared holds represented in count. */
    final def sharedCount(c: Int): Int = c >>> SHARED_SHIFT

    /** Returns the number of exclusive holds represented in count. */
    final def exclusiveCount(c: Int): Int = c & EXCLUSIVE_MASK

    /** A counter for per-thread read hold counts. Maintained as a ThreadLocal;
     *  cached in cachedHoldCounter.
     */
    final class HoldCounter {
      var count: Int = 0 // initially 0

      // Use id, not reference, to avoid garbage retention
      final val tid: Long =
        LockSupport.getThreadId(Thread.currentThread())
    }

    /** ThreadLocal subclass. Easiest to explicitly define for sake of
     *  deserialization mechanics.
     */
    final class ThreadLocalHoldCounter extends ThreadLocal[Sync.HoldCounter] {
      override def initialValue(): Sync.HoldCounter = new Sync.HoldCounter
    }
    private def unmatchedUnlockException: IllegalMonitorStateException =
      new IllegalMonitorStateException(
        "attempt to unlock read lock, not locked by current thread"
      )
  }

  abstract private[locks] class Sync() extends AbstractQueuedSynchronizer {

    /** The number of reentrant read locks held by current thread. Initialized
     *  only in constructor and readObject. Removed whenever a thread's read
     *  hold count drops to 0.
     */
    private var readHolds = new Sync.ThreadLocalHoldCounter

    setState(getState()) // ensures visibility of readHolds

    /** The hold count of the last thread to successfully acquire readLock. This
     *  saves ThreadLocal lookup in the common case where the next thread to
     *  release is the last one to acquire. This is non-volatile since it is
     *  just used as a heuristic, and would be great for threads to cache.
     *
     *  <p>Can outlive the Thread for which it is caching the read hold count,
     *  but avoids garbage retention by not retaining a reference to the Thread.
     *
     *  <p>Accessed via a benign data race; relies on the memory model's final
     *  field and out-of-thin-air guarantees.
     */
    private var cachedHoldCounter: Sync.HoldCounter = _

    /** firstReader is the first thread to have acquired the read lock.
     *  firstReaderHoldCount is firstReader's hold count.
     *
     *  <p>More precisely, firstReader is the unique thread that last changed
     *  the shared count from 0 to 1, and has not released the read lock since
     *  then; null if there is no such thread.
     *
     *  <p>Cannot cause garbage retention unless the thread terminated without
     *  relinquishing its read locks, since tryReleaseShared sets it to null.
     *
     *  <p>Accessed via a benign data race; relies on the memory model's
     *  out-of-thin-air guarantees for references.
     *
     *  <p>This allows tracking of read holds for uncontended read locks to be
     *  very cheap.
     */
    private var firstReader: Thread = _
    private var firstReaderHoldCount: Int = 0

    /** Returns true if the current thread, when trying to acquire the read
     *  lock, and otherwise eligible to do so, should block because of policy
     *  for overtaking other waiting threads.
     */
    def readerShouldBlock: Boolean

    /** Returns true if the current thread, when trying to acquire the write
     *  lock, and otherwise eligible to do so, should block because of policy
     *  for overtaking other waiting threads.
     */
    def writerShouldBlock: Boolean
    /*
     * Note that tryRelease and tryAcquire can be called by
     * Conditions. So it is possible that their arguments contain
     * both read and write holds that are all released during a
     * condition wait and re-established in tryAcquire.
     */
    override final protected def tryRelease(releases: Int): Boolean = {
      if (!(isHeldExclusively())) throw new IllegalMonitorStateException
      val nextc: Int = getState() - releases
      val free: Boolean = Sync.exclusiveCount(nextc) == 0
      if (free) setExclusiveOwnerThread(null)
      setState(nextc)
      free
    }

    override final protected def tryAcquire(acquires: Int): Boolean = {
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

    override final protected def tryReleaseShared(
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

    override final protected def tryAcquireShared(unused: Int): Int = {
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

    /** Full version of acquire for reads, that handles CAS misses and reentrant
     *  reads not dealt with in tryAcquireShared.
     */
    final private[locks] def fullTryAcquireShared(current: Thread): Int = {
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

    /** Performs tryLock for write, enabling barging in both modes. This is
     *  identical in effect to tryAcquire except for lack of calls to
     *  writerShouldBlock.
     */
    final private[locks] def tryWriteLock: Boolean = {
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

    /** Performs tryLock for read, enabling barging in both modes. This is
     *  identical in effect to tryAcquireShared except for lack of calls to
     *  readerShouldBlock.
     */
    final private[locks] def tryReadLock: Boolean = {
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

    override final protected[ReentrantReadWriteLock] def isHeldExclusively()
        : Boolean = {
      // While we must in general read state before owner,
      // we don't need to do so to check if current thread is owner
      getExclusiveOwnerThread() eq Thread.currentThread()
    }

    final private[locks] def newCondition: ConditionObject = new ConditionObject

    final private[locks] def getOwner: Thread = {
      // Must read state before owner to ensure memory consistency
      if (Sync.exclusiveCount(getState()) == 0) null
      else getExclusiveOwnerThread()
    }

    final private[locks] def getReadLockCount: Int =
      Sync.sharedCount(getState())

    final private[locks] def isWriteLocked: Boolean =
      Sync.exclusiveCount(getState()) != 0

    final private[locks] def getWriteHoldCount: Int =
      if (isHeldExclusively()) Sync.exclusiveCount(getState())
      else 0

    final private[locks] def getReadHoldCount: Int = {
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

    final private[locks] def getCount: Int = getState()
  }

  /** Nonfair version of Sync */
  final private[locks] class NonfairSync extends ReentrantReadWriteLock.Sync {
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

  /** Fair version of Sync */
  final private[locks] class FairSync extends ReentrantReadWriteLock.Sync {
    override final def writerShouldBlock: Boolean = hasQueuedPredecessors()
    override final def readerShouldBlock: Boolean = hasQueuedPredecessors()
  }

  /** The lock returned by method [[ReentrantReadWriteLock#readLock]].
   */
  class ReadLock private (final private val sync: ReentrantReadWriteLock.Sync)
      extends Lock
      with Serializable {
    protected[ReentrantReadWriteLock] def this(lock: ReentrantReadWriteLock) =
      this(lock.sync)

    /** Acquires the read lock.
     *
     *  <p>Acquires the read lock if the write lock is not held by another
     *  thread and returns immediately.
     *
     *  <p>If the write lock is held by another thread then the current thread
     *  becomes disabled for thread scheduling purposes and lies dormant until
     *  the read lock has been acquired.
     */
    override def lock(): Unit = sync.acquireShared(1)

    /** Acquires the read lock unless the current thread is
     *  [[java.lang.Thread.interrupt interrupted]].
     *
     *  <p>Acquires the read lock if the write lock is not held by another
     *  thread and returns immediately.
     *
     *  <p>If the write lock is held by another thread then the current thread
     *  becomes disabled for thread scheduling purposes and lies dormant until
     *  one of two things happens:
     *
     *  <ul>
     *
     *  <li>The read lock is acquired by the current thread; or
     *
     *  <li>Some other thread {@linkplain java.lang.Thread.interrupt interrupts}
     *  the current thread.
     *
     *  </ul>
     *
     *  <p>If the current thread:
     *
     *  <ul>
     *
     *  <li>has its interrupted status set on entry to this method; or
     *
     *  <li>is {@linkplain java.lang.Thread.interrupt interrupted} while
     *  acquiring the read lock,
     *
     *  </ul>
     *
     *  then [[java.lang.InterruptedException]] is thrown and the current
     *  thread's interrupted status is cleared.
     *
     *  <p>In this implementation, as this method is an explicit interruption
     *  point, preference is given to responding to the interrupt over normal or
     *  reentrant acquisition of the lock.
     *
     *  @throws java.lang.InterruptedException
     *    if the current thread is interrupted
     */
    @throws[InterruptedException]
    override def lockInterruptibly(): Unit = sync.acquireSharedInterruptibly(1)

    /** Acquires the read lock only if the write lock is not held by another
     *  thread at the time of invocation.
     *
     *  <p>Acquires the read lock if the write lock is not held by another
     *  thread and returns immediately with the value {@code true}. Even when
     *  this lock has been set to use a fair ordering policy, a call to
     *  `tryLock()` <em>will</em> immediately acquire the read lock if it is
     *  available, whether or not other threads are currently waiting for the
     *  read lock. This &quot;barging&quot; behavior can be useful in certain
     *  circumstances, even though it breaks fairness. If you want to honor the
     *  fairness setting for this lock, then use `tryLock(Long,TimeUnit)` which
     *  is almost equivalent (it also detects interruption).
     *
     *  <p>If the write lock is held by another thread then this method will
     *  return immediately with the value {@code false}.
     *
     *  @return
     *    {@code true} if the read lock was acquired
     */
    override def tryLock(): Boolean = sync.tryReadLock

    /** Acquires the read lock if the write lock is not held by another thread
     *  within the given waiting time and the current thread has not been
     *  {@linkplain java.lang.Thread.interrupt interrupted}.
     *
     *  <p>Acquires the read lock if the write lock is not held by another
     *  thread and returns immediately with the value {@code true}. If this lock
     *  has been set to use a fair ordering policy then an available lock
     *  <em>will not</em> be acquired if any other threads are waiting for the
     *  lock. This is in contrast to the [[tryLock()*]] method. If you want a
     *  timed {@code tryLock} that does permit barging on a fair lock then
     *  combine the timed and un-timed forms together:
     *
     *  <pre> {@code if (lock.tryLock() || lock.tryLock(timeout, unit)) { ...
     *  }}</pre>
     *
     *  <p>If the write lock is held by another thread then the current thread
     *  becomes disabled for thread scheduling purposes and lies dormant until
     *  one of three things happens:
     *
     *  <ul>
     *
     *  <li>The read lock is acquired by the current thread; or
     *
     *  <li>Some other thread {@linkplain java.lang.Thread.interrupt interrupts}
     *  the current thread; or
     *
     *  <li>The specified waiting time elapses.
     *
     *  </ul>
     *
     *  <p>If the read lock is acquired then the value {@code true} is returned.
     *
     *  <p>If the current thread:
     *
     *  <ul>
     *
     *  <li>has its interrupted status set on entry to this method; or
     *
     *  <li>is {@linkplain java.lang.Thread.interrupt interrupted} while
     *  acquiring the read lock,
     *
     *  </ul> then [[java.lang.InterruptedException]] is thrown and the current
     *  thread's interrupted status is cleared.
     *
     *  <p>If the specified waiting time elapses then the value {@code false} is
     *  returned. If the time is less than or equal to zero, the method will not
     *  wait at all.
     *
     *  <p>In this implementation, as this method is an explicit interruption
     *  point, preference is given to responding to the interrupt over normal or
     *  reentrant acquisition of the lock, and over reporting the elapse of the
     *  waiting time.
     *
     *  @param timeout
     *    the time to wait for the read lock
     *  @param unit
     *    the time unit of the timeout argument
     *  @return
     *    {@code true} if the read lock was acquired
     *  @throws java.lang.InterruptedException
     *    if the current thread is interrupted
     *  @throws java.lang.NullPointerException
     *    if the time unit is null
     */
    @throws[InterruptedException]
    override def tryLock(timeout: Long, unit: TimeUnit): Boolean =
      sync.tryAcquireSharedNanos(1, unit.toNanos(timeout))

    /** Attempts to release this lock.
     *
     *  <p>If the number of readers is now zero then the lock is made available
     *  for write lock attempts. If the current thread does not hold this lock
     *  then [[java.lang.IllegalMonitorStateException]] is thrown.
     *
     *  @throws java.lang.IllegalMonitorStateException
     *    if the current thread does not hold this lock
     */
    override def unlock(): Unit = { sync.releaseShared(1) }

    /** Throws {@code UnsupportedOperationException} because {@code ReadLocks}
     *  do not support conditions.
     *
     *  @throws java.lang.UnsupportedOperationException
     *    always
     */
    override def newCondition(): Condition =
      throw new UnsupportedOperationException

    /** Returns a string identifying this lock, as well as its lock state. The
     *  state, in brackets, includes the String {@code "Read locks ="} followed
     *  by the number of held read locks.
     *
     *  @return
     *    a string identifying this lock, as well as its lock state
     */
    override def toString: String = {
      val r: Int = sync.getReadLockCount
      return super.toString + "[Read locks = " + r + "]"
    }
  }

  /** The lock returned by method [[ReentrantReadWriteLock#writeLock]].
   */
  class WriteLock private (final private val sync: ReentrantReadWriteLock.Sync)
      extends Lock
      with Serializable {
    protected[ReentrantReadWriteLock] def this(lock: ReentrantReadWriteLock) =
      this(lock.sync)

    /** Acquires the write lock.
     *
     *  <p>Acquires the write lock if neither the read nor write lock are held
     *  by another thread and returns immediately, setting the write lock hold
     *  count to one.
     *
     *  <p>If the current thread already holds the write lock then the hold
     *  count is incremented by one and the method returns immediately.
     *
     *  <p>If the lock is held by another thread then the current thread becomes
     *  disabled for thread scheduling purposes and lies dormant until the write
     *  lock has been acquired, at which time the write lock hold count is set
     *  to one.
     */
    override def lock(): Unit = sync.acquire(1)

    /** Queries if this write lock is held by the current thread. Identical in
     *  effect to [[ReentrantReadWriteLock#isWriteLockedByCurrentThread]].
     *
     *  @return
     *    {@code true} if the current thread holds this lock and {@code false}
     *    otherwise
     *  @since 1.6
     */
    def isHeldByCurrentThread: Boolean = sync.isHeldExclusively()

    /** Queries the number of holds on this write lock by the current thread. A
     *  thread has a hold on a lock for each lock action that is not matched by
     *  an unlock action. Identical in effect to
     *  [[ReentrantReadWriteLock#getWriteHoldCount]].
     *
     *  @return
     *    the number of holds on this lock by the current thread, or zero if
     *    this lock is not held by the current thread
     *  @since 1.6
     */
    def getHoldCount: Int = sync.getWriteHoldCount
  }
}

/** An implementation of [[ReadWriteLock]] supporting similar semantics to
 *  [[ReentrantLock]]. <p>This class has the following properties:
 *
 *  <ul> <li><b>Acquisition order</b>
 *
 *  <p>This class does not impose a reader or writer preference ordering for
 *  lock access. However, it does support an optional <em>fairness</em> policy.
 *
 *  <dl> <dt><b><i>Non-fair mode (default)</i></b> <dd>When constructed as
 *  non-fair (the default), the order of entry to the read and write lock is
 *  unspecified, subject to reentrancy constraints. A nonfair lock that is
 *  continuously contended may indefinitely postpone one or more reader or
 *  writer threads, but will normally have higher throughput than a fair lock.
 *
 *  <dt><b><i>Fair mode</i></b> <dd>When constructed as fair, threads contend
 *  for entry using an approximately arrival-order policy. When the currently
 *  held lock is released, either the longest-waiting single writer thread will
 *  be assigned the write lock, or if there is a group of reader threads waiting
 *  longer than all waiting writer threads, that group will be assigned the read
 *  lock.
 *
 *  <p>A thread that tries to acquire a fair read lock (non-reentrantly) will
 *  block if either the write lock is held, or there is a waiting writer thread.
 *  The thread will not acquire the read lock until after the oldest currently
 *  waiting writer thread has acquired and released the write lock. Of course,
 *  if a waiting writer abandons its wait, leaving one or more reader threads as
 *  the longest waiters in the queue with the write lock free, then those
 *  readers will be assigned the read lock.
 *
 *  <p>A thread that tries to acquire a fair write lock (non-reentrantly) will
 *  block unless both the read lock and write lock are free (which implies there
 *  are no waiting threads). (Note that the non-blocking [[ReadLock#tryLock()]]
 *  and [[WriteLock#tryLock()]] methods do not honor this fair setting and will
 *  immediately acquire the lock if it is possible, regardless of waiting
 *  threads.) </dl>
 *
 *  <li><b>Reentrancy</b>
 *
 *  <p>This lock allows both readers and writers to reacquire read or write
 *  locks in the style of a [[ReentrantLock]]. Non-reentrant readers are not
 *  allowed until all write locks held by the writing thread have been released.
 *
 *  <p>Additionally, a writer can acquire the read lock, but not vice-versa.
 *  Among other applications, reentrancy can be useful when write locks are held
 *  during calls or callbacks to methods that perform reads under read locks. If
 *  a reader tries to acquire the write lock it will never succeed.
 *
 *  <li><b>Lock downgrading</b> <p>Reentrancy also allows downgrading from the
 *  write lock to a read lock, by acquiring the write lock, then the read lock
 *  and then releasing the write lock. However, upgrading from a read lock to
 *  the write lock is <b>not</b> possible.
 *
 *  <li><b>Interruption of lock acquisition</b> <p>The read lock and write lock
 *  both support interruption during lock acquisition.
 *
 *  <li><b>[[Condition]] support</b> <p>The write lock provides a [[Condition]]
 *  implementation that behaves in the same way, with respect to the write lock,
 *  as the [[Condition]] implementation provided by
 *  [[ReentrantLock#newCondition]] does for [[ReentrantLock]]. This
 *  [[Condition]] can, of course, only be used with the write lock.
 *
 *  <p>The read lock does not support a [[Condition]] and
 *  `readLock().newCondition()` throws {@code UnsupportedOperationException}.
 *
 *  <li><b>Instrumentation</b> <p>This class supports methods to determine
 *  whether locks are held or contended. These methods are designed for
 *  monitoring system state, not for synchronization control. </ul>
 *
 *  <p>Serialization of this class behaves in the same way as built-in locks: a
 *  deserialized lock is in the unlocked state, regardless of its state when
 *  serialized.
 *
 *  <p><b>Sample usages</b>. Here is a code sketch showing how to perform lock
 *  downgrading after updating a cache (exception handling is particularly
 *  tricky when handling multiple locks in a non-nested fashion):
 *
 *  ReentrantReadWriteLocks can be used to improve concurrency in some uses of
 *  some kinds of Collections. This is typically worthwhile only when the
 *  collections are expected to be large, accessed by more reader threads than
 *  writer threads, and entail operations with overhead that outweighs
 *  synchronization overhead. For example, here is a class using a TreeMap that
 *  is expected to be large and concurrently accessed.
 *
 *  <h2>Implementation Notes</h2>
 *
 *  <p>This lock supports a maximum of 65535 recursive write locks and 65535
 *  read locks. Attempts to exceed these limits result in [[Error]] throws from
 *  locking methods.
 *
 *  @since 1.5
 *  @author
 *    Doug Lea
 */
class ReentrantReadWriteLock(val fair: Boolean)
    extends ReadWriteLock
    with Serializable {
  def this() = this(false)

  /** Performs all synchronization mechanics */
  final private[locks] val sync: ReentrantReadWriteLock.Sync =
    if (fair) new ReentrantReadWriteLock.FairSync
    else new ReentrantReadWriteLock.NonfairSync

  /** Inner class providing readlock */
  final private val readerLock = new ReentrantReadWriteLock.ReadLock(this)

  /** Inner class providing writelock */
  final private val writerLock = new ReentrantReadWriteLock.WriteLock(this)

  /** Creates a new {@code ReentrantReadWriteLock} with default (nonfair)
   *  ordering properties.
   */
  override def writeLock(): ReentrantReadWriteLock.WriteLock = this.writerLock
  override def readLock(): ReentrantReadWriteLock.ReadLock = this.readerLock

  /** Returns {@code true} if this lock has fairness set true.
   *
   *  @return
   *    {@code true} if this lock has fairness set true
   */
  final def isFair: Boolean = sync.isInstanceOf[ReentrantReadWriteLock.FairSync]

  /** Returns the thread that currently owns the write lock, or {@code null} if
   *  not owned. When this method is called by a thread that is not the owner,
   *  the return value reflects a best-effort approximation of current lock
   *  status. For example, the owner may be momentarily {@code null} even if
   *  there are threads trying to acquire the lock but have not yet done so.
   *  This method is designed to facilitate construction of subclasses that
   *  provide more extensive lock monitoring facilities.
   *
   *  @return
   *    the owner, or {@code null} if not owned
   */
  protected def getOwner: Thread = sync.getOwner

  /** Queries the number of read locks held for this lock. This method is
   *  designed for use in monitoring system state, not for synchronization
   *  control.
   *  @return
   *    the number of read locks held
   */
  def getReadLockCount: Int = sync.getReadLockCount

  /** Queries if the write lock is held by any thread. This method is designed
   *  for use in monitoring system state, not for synchronization control.
   *
   *  @return
   *    {@code true} if any thread holds the write lock and {@code false}
   *    otherwise
   */
  def isWriteLocked: Boolean = sync.isWriteLocked

  /** Queries if the write lock is held by the current thread.
   *
   *  @return
   *    `true` if the current thread holds the write lock and `false` otherwise
   */
  def isWriteLockedByCurrentThread: Boolean = sync.isHeldExclusively()

  /** Queries the number of reentrant write holds on this lock by the current
   *  thread. A writer thread has a hold on a lock for each lock action that is
   *  not matched by an unlock action.
   *
   *  @return
   *    the number of holds on the write lock by the current thread, or zero if
   *    the write lock is not held by the current thread
   */
  def getWriteHoldCount: Int = sync.getWriteHoldCount

  /** Queries the number of reentrant read holds on this lock by the current
   *  thread. A reader thread has a hold on a lock for each lock action that is
   *  not matched by an unlock action.
   *
   *  @return
   *    the number of holds on the read lock by the current thread, or zero if
   *    the read lock is not held by the current thread
   *  @since 1.6
   */
  def getReadHoldCount: Int = sync.getReadHoldCount

  /** Returns a collection containing threads that may be waiting to acquire the
   *  write lock. Because the actual set of threads may change dynamically while
   *  constructing this result, the returned collection is only a best-effort
   *  estimate. The elements of the returned collection are in no particular
   *  order. This method is designed to facilitate construction of subclasses
   *  that provide more extensive lock monitoring facilities.
   *
   *  @return
   *    the collection of threads
   */
  protected def getQueuedWriterThreads: util.Collection[Thread] =
    sync.getExclusiveQueuedThreads()

  /** Returns a collection containing threads that may be waiting to acquire the
   *  read lock. Because the actual set of threads may change dynamically while
   *  constructing this result, the returned collection is only a best-effort
   *  estimate. The elements of the returned collection are in no particular
   *  order. This method is designed to facilitate construction of subclasses
   *  that provide more extensive lock monitoring facilities.
   *
   *  @return
   *    the collection of threads
   */
  protected def getQueuedReaderThreads: util.Collection[Thread] =
    sync.getSharedQueuedThreads()

  /** Queries whether any threads are waiting to acquire the read or write lock.
   *  Note that because cancellations may occur at any time, a {@code true}
   *  return does not guarantee that any other thread will ever acquire a lock.
   *  This method is designed primarily for use in monitoring of the system
   *  state.
   *
   *  @return
   *    {@code true} if there may be other threads waiting to acquire the lock
   */
  final def hasQueuedThreads: Boolean = sync.hasQueuedThreads()

  /** Queries whether the given thread is waiting to acquire either the read or
   *  write lock. Note that because cancellations may occur at any time, a
   *  {@code true} return does not guarantee that this thread will ever acquire
   *  a lock. This method is designed primarily for use in monitoring of the
   *  system state.
   *
   *  @param thread
   *    the thread
   *  @return
   *    {@code true} if the given thread is queued waiting for this lock
   *  @throws java.lang.NullPointerException
   *    if the thread is null
   */
  final def hasQueuedThread(thread: Thread): Boolean = sync.isQueued(thread)

  /** Returns an estimate of the number of threads waiting to acquire either the
   *  read or write lock. The value is only an estimate because the number of
   *  threads may change dynamically while this method traverses internal data
   *  structures. This method is designed for use in monitoring system state,
   *  not for synchronization control.
   *
   *  @return
   *    the estimated number of threads waiting for this lock
   */
  final def getQueueLength: Int = sync.getQueueLength()

  /** Returns a collection containing threads that may be waiting to acquire
   *  either the read or write lock. Because the actual set of threads may
   *  change dynamically while constructing this result, the returned collection
   *  is only a best-effort estimate. The elements of the returned collection
   *  are in no particular order. This method is designed to facilitate
   *  construction of subclasses that provide more extensive monitoring
   *  facilities.
   *
   *  @return
   *    the collection of threads
   */
  protected def getQueuedThreads: util.Collection[Thread] =
    sync.getQueuedThreads()

  /** Queries whether any threads are waiting on the given condition associated
   *  with the write lock. Note that because timeouts and interrupts may occur
   *  at any time, a {@code true} return does not guarantee that a future
   *  `signal` will awaken any threads. This method is designed primarily for
   *  use in monitoring of the system state.
   *
   *  @param condition
   *    the condition
   *  @return
   *    {@code true} if there are any waiting threads
   *  @throws java.lang.IllegalMonitorStateException
   *    if this lock is not held
   *  @throws java.lang.IllegalArgumentException
   *    if the given condition is not associated with this lock
   *  @throws java.lang.NullPointerException
   *    if the condition is null
   */
  def hasWaiters(condition: Condition): Boolean = condition match {
    case cond: AbstractQueuedSynchronizer#ConditionObject =>
      sync.hasWaiters(cond)
    case null => throw new NullPointerException
    case _    => throw new IllegalArgumentException("not owner")
  }

  /** Returns an estimate of the number of threads waiting on the given
   *  condition associated with the write lock. Note that because timeouts and
   *  interrupts may occur at any time, the estimate serves only as an upper
   *  bound on the actual number of waiters. This method is designed for use in
   *  monitoring of the system state, not for synchronization control.
   *
   *  @param condition
   *    the condition
   *  @return
   *    the estimated number of waiting threads
   *  @throws java.lang.IllegalMonitorStateException
   *    if this lock is not held
   *  @throws java.lang.IllegalArgumentException
   *    if the given condition is not associated with this lock
   *  @throws java.lang.NullPointerException
   *    if the condition is null
   */
  def getWaitQueueLength(condition: Condition): Int = condition match {
    case cond: AbstractQueuedSynchronizer#ConditionObject =>
      sync.getWaitQueueLength(cond)
    case null => throw new NullPointerException
    case _    => throw new IllegalArgumentException("not owner")
  }

  /** Returns a collection containing those threads that may be waiting on the
   *  given condition associated with the write lock. Because the actual set of
   *  threads may change dynamically while constructing this result, the
   *  returned collection is only a best-effort estimate. The elements of the
   *  returned collection are in no particular order. This method is designed to
   *  facilitate construction of subclasses that provide more extensive
   *  condition monitoring facilities.
   *
   *  @param condition
   *    the condition
   *  @return
   *    the collection of threads
   *  @throws java.lang.IllegalMonitorStateException
   *    if this lock is not held
   *  @throws java.lang.IllegalArgumentException
   *    if the given condition is not associated with this lock
   *  @throws java.lang.NullPointerException
   *    if the condition is null
   */
  protected def getWaitingThreads(
      condition: Condition
  ): util.Collection[Thread] =
    condition match {
      case cond: AbstractQueuedSynchronizer#ConditionObject =>
        sync.getWaitingThreads(cond)
      case null => throw new NullPointerException
      case _    => throw new IllegalArgumentException("not owner")
    }

  /** Returns a string identifying this lock, as well as its lock state. The
   *  state, in brackets, includes the String {@code "Write locks ="} followed
   *  by the number of reentrantly held write locks, and the String {@code "Read
   *  locks ="} followed by the number of held read locks.
   *
   *  @return
   *    a string identifying this lock, as well as its lock state
   */
  override def toString(): String = {
    val c: Int = sync.getCount
    val w: Int = ReentrantReadWriteLock.Sync.exclusiveCount(c)
    val r: Int = ReentrantReadWriteLock.Sync.sharedCount(c)
    super.toString() + "[Write locks = " + w + ", Read locks = " + r + "]"
  }
}
