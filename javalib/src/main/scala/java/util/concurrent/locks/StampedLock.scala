/* Ported from JSR-166 Expert Group main CVS (Concurrent Versions System)
 * repository as described at Doug Lea "Concurrency JSR-166 Interest Site"
 *    https://gee.cs.oswego.edu/dl/concurrency-interest/.
 *
 *  file: src/main/java/util/concurrent/locks/StampedLock.java
 *  revision 1.118, dated: 2022-03-18
 */

/* Scala Native (SN) Notes:
 *
 *   * Shield your eyes: This code is intended to be Scala which stays very
 *     close to JSR-166 .java original. This aids detecting porting errors.
 *     It is _Not_Intended_ to be idiomatic Scala.
 *
 *   * For the curious, or those debugging cleanQueue() loop bugs,
 *     the CLH queue mentioned above headAtomic is a
 *     CLH (Craig, Landin, and Hagersten) queue. The algorithm is
 *     documented on the web (but not Wikipedia).
 *
 *   * Remaining issues
 *       ? how to translate '@ReservedStackAccess'??
 *       ? check with a guru about handling of SerialVersionUID?
 */

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.locks

import java.util.concurrent.TimeUnit

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic
import scala.scalanative.libc.stdatomic.memory_order.{
  memory_order_acquire, memory_order_relaxed, memory_order_release
}
import scala.scalanative.libc.stdatomic.{AtomicInt, AtomicLongLong, AtomicRef}
import scala.scalanative.runtime.{Intrinsics, fromRawPtr}

/*
 * A capability-based lock with three modes for controlling read/write
 * access.  The state of a StampedLock consists of a version and mode.
 * Lock acquisition methods return a stamp that represents and
 * controls access with respect to a lock state; "try" versions of
 * these methods may instead return the special value zero to
 * represent failure to acquire access. Lock release and conversion
 * methods require stamps as arguments, and fail if they do not match
 * the state of the lock. The three modes are:
 *
 * <ul>
 *
 *  <li><b>Writing.</b> Method {@link #writeLock} possibly blocks
 *   waiting for exclusive access, returning a stamp that can be used
 *   in method {@link #unlockWrite} to release the lock. Untimed and
 *   timed versions of {@code tryWriteLock} are also provided. When
 *   the lock is held in write mode, no read locks may be obtained,
 *   and all optimistic read validations will fail.
 *
 *  <li><b>Reading.</b> Method {@link #readLock} possibly blocks
 *   waiting for non-exclusive access, returning a stamp that can be
 *   used in method {@link #unlockRead} to release the lock. Untimed
 *   and timed versions of {@code tryReadLock} are also provided.
 *
 *  <li><b>Optimistic Reading.</b> Method {@link #tryOptimisticRead}
 *   returns a non-zero stamp only if the lock is not currently held in
 *   write mode.  Method {@link #validate} returns true if the lock has not
 *   been acquired in write mode since obtaining a given stamp, in which
 *   case all actions prior to the most recent write lock release
 *   happen-before actions following the call to {@code tryOptimisticRead}.
 *   This mode can be thought of as an extremely weak version of a
 *   read-lock, that can be broken by a writer at any time.  The use of
 *   optimistic read mode for short read-only code segments often reduces
 *   contention and improves throughput.  However, its use is inherently
 *   fragile.  Optimistic read sections should only read fields and hold
 *   them in local variables for later use after validation. Fields read
 *   while in optimistic read mode may be wildly inconsistent, so usage
 *   applies only when you are familiar enough with data representations to
 *   check consistency and/or repeatedly invoke method {@code validate()}.
 *   For example, such steps are typically required when first reading an
 *   object or array reference, and then accessing one of its fields,
 *   elements or methods.
 *
 * </ul>
 *
 * <p>This class also supports methods that conditionally provide
 * conversions across the three modes. For example, method {@link
 * #tryConvertToWriteLock} attempts to "upgrade" a mode, returning
 * a valid write stamp if (1) already in writing mode (2) in reading
 * mode and there are no other readers or (3) in optimistic read mode
 * and the lock is available. The forms of these methods are designed to
 * help reduce some of the code bloat that otherwise occurs in
 * retry-based designs.
 *
 * <p>StampedLocks are designed for use as internal utilities in the
 * development of thread-safe components. Their use relies on
 * knowledge of the internal properties of the data, objects, and
 * methods they are protecting.  They are not reentrant, so locked
 * bodies should not call other unknown methods that may try to
 * re-acquire locks (although you may pass a stamp to other methods
 * that can use or convert it).  The use of read lock modes relies on
 * the associated code sections being side-effect-free.  Unvalidated
 * optimistic read sections cannot call methods that are not known to
 * tolerate potential inconsistencies.  Stamps use finite
 * representations, and are not cryptographically secure (i.e., a
 * valid stamp may be guessable). Stamp values may recycle after (no
 * sooner than) one year of continuous operation. A stamp held without
 * use or validation for longer than this period may fail to validate
 * correctly.  StampedLocks are serializable, but always deserialize
 * into initial unlocked state, so they are not useful for remote
 * locking.
 *
 * <p>Like {@link java.util.concurrent.Semaphore Semaphore}, but unlike most
 * {@link Lock} implementations, StampedLocks have no notion of ownership.
 * Locks acquired in one thread can be released or converted in another.
 *
 * <p>The scheduling policy of StampedLock does not consistently
 * prefer readers over writers or vice versa.  All "try" methods are
 * best-effort and do not necessarily conform to any scheduling or
 * fairness policy. A zero return from any "try" method for acquiring
 * or converting locks does not carry any information about the state
 * of the lock; a subsequent invocation may succeed.
 *
 * <p>Because it supports coordinated usage across multiple lock
 * modes, this class does not directly implement the {@link Lock} or
 * {@link ReadWriteLock} interfaces. However, a StampedLock may be
 * viewed {@link #asReadLock()}, {@link #asWriteLock()}, or {@link
 * #asReadWriteLock()} in applications requiring only the associated
 * set of functionality.
 *
 * <p><b>Memory Synchronization.</b> Methods with the effect of
 * successfully locking in any mode have the same memory
 * synchronization effects as a <em>Lock</em> action, as described in
 * Chapter 17 of <cite>The Java Language Specification</cite>.
 * Methods successfully unlocking in write mode have the same memory
 * synchronization effects as an <em>Unlock</em> action.  In optimistic
 * read usages, actions prior to the most recent write mode unlock action
 * are guaranteed to happen-before those following a tryOptimisticRead
 * only if a later validate returns true; otherwise there is no guarantee
 * that the reads between tryOptimisticRead and validate obtain a
 * consistent snapshot.
 *
 * <p><b>Sample Usage.</b> The following illustrates some usage idioms
 * in a class that maintains simple two-dimensional points. The sample
 * code illustrates some try/catch conventions even though they are
 * not strictly needed here because no exceptions can occur in their
 * bodies.
 *
 * <pre> {@code
 * class Point {
 *   private double x, y;
 *   private final StampedLock sl = new StampedLock();
 *
 *   // an exclusively locked method
 *   void move(double deltaX, double deltaY) {
 *     long stamp = sl.writeLock();
 *     try {
 *       x += deltaX;
 *       y += deltaY;
 *     } finally {
 *       sl.unlockWrite(stamp);
 *     }
 *   }
 *
 *   // a read-only method
 *   // upgrade from optimistic read to read lock
 *   double distanceFromOrigin() {
 *     long stamp = sl.tryOptimisticRead();
 *     try {
 *       retryHoldingLock: for (;; stamp = sl.readLock()) {
 *         if (stamp == 0L)
 *           continue retryHoldingLock;
 *         // possibly racy reads
 *         double currentX = x;
 *         double currentY = y;
 *         if (!sl.validate(stamp))
 *           continue retryHoldingLock;
 *         return Math.hypot(currentX, currentY);
 *       }
 *     } finally {
 *       if (StampedLock.isReadLockStamp(stamp))
 *         sl.unlockRead(stamp);
 *     }
 *   }
 *
 *   // upgrade from optimistic read to write lock
 *   void moveIfAtOrigin(double newX, double newY) {
 *     long stamp = sl.tryOptimisticRead();
 *     try {
 *       retryHoldingLock: for (;; stamp = sl.writeLock()) {
 *         if (stamp == 0L)
 *           continue retryHoldingLock;
 *         // possibly racy reads
 *         double currentX = x;
 *         double currentY = y;
 *         if (!sl.validate(stamp))
 *           continue retryHoldingLock;
 *         if (currentX != 0.0 || currentY != 0.0)
 *           break;
 *         stamp = sl.tryConvertToWriteLock(stamp);
 *         if (stamp == 0L)
 *           continue retryHoldingLock;
 *         // exclusive access
 *         x = newX;
 *         y = newY;
 *         return;
 *       }
 *     } finally {
 *       if (StampedLock.isWriteLockStamp(stamp))
 *         sl.unlockWrite(stamp);
 *     }
 *   }
 *
 *   // upgrade read lock to write lock
 *   void moveIfAtOrigin2(double newX, double newY) {
 *     long stamp = sl.readLock();
 *     try {
 *       while (x == 0.0 && y == 0.0) {
 *         long ws = sl.tryConvertToWriteLock(stamp);
 *         if (ws != 0L) {
 *           stamp = ws;
 *           x = newX;
 *           y = newY;
 *           break;
 *         }
 *         else {
 *           sl.unlockRead(stamp);
 *           stamp = sl.writeLock();
 *         }
 *       }
 *     } finally {
 *       sl.unlock(stamp);
 *     }
 *   }
 * }}</pre>
 *
 * @jls 17.4 Memory Model
 * @since 1.8
 * @author Doug Lea
 */
@SerialVersionUID(-6001602636862214147L)
class StampedLock extends Serializable {
  import StampedLock.*

  /*
   * Algorithmic notes:
   *
   * The design employs elements of Sequence locks
   * (as used in linux kernels; see Lameter's
   * http://www.lameter.com/gelato2005.pdf
   * and elsewhere; see
   * Boehm's http://www.hpl.hp.com/techreports/2012/HPL-2012-68.html)
   * and Ordered RW locks (see Shirako et al
   * http://dl.acm.org/citation.cfm?id=2312015)
   *
   * Conceptually, the primary state of the lock includes a sequence
   * number that is odd when write-locked and even otherwise.
   * However, this is offset by a reader count that is non-zero when
   * read-locked.  The read count is ignored when validating
   * "optimistic" seqlock-reader-style stamps.  Because we must use
   * a small finite number of bits (currently 7) for readers, a
   * supplementary reader overflow word is used when the number of
   * readers exceeds the count field. We do this by treating the max
   * reader count value (RBITS) as a spinlock protecting overflow
   * updates.
   *
   * Waiters use a modified form of CLH lock used in
   * AbstractQueuedSynchronizer (AQS; see its internal documentation
   * for a fuller account), where each node is either a ReaderNode
   * or WriterNode. Implementation of queued Writer mode is
   * identical to AQS except for lock-state operations.  Sets of
   * waiting readers are grouped (linked) under a common node (field
   * cowaiters) so act as a single node with respect to most CLH
   * mechanics.  This simplifies the scheduling policy to a
   * mainly-FIFO scheme that incorporates elements of Phase-Fair
   * locks (see Brandenburg & Anderson, especially
   * http://www.cs.unc.edu/~bbb/diss/).  Method release does not
   * itself wake up cowaiters. This is done by the primary thread,
   * but helped by other cowaiters as they awaken.
   *
   * These rules apply to threads actually queued. Threads may also
   * try to acquire locks before or in the process of enqueueing
   * regardless of preference rules, and so may "barge" their way
   * in.  Methods writeLock and readLock (but not the other variants
   * of each) first unconditionally try to CAS state, falling back
   * to test-and-test-and-set retries on failure, slightly shrinking
   * race windows on initial attempts, thus making success more
   * likely. Also, when some threads cancel (via interrupt or
   * timeout), phase-fairness is at best roughly approximated.
   *
   * Nearly all of these mechanics are carried out in methods
   * acquireWrite and acquireRead, that, as typical of such code,
   * sprawl out because actions and retries rely on consistent sets
   * of locally cached reads.
   *
   * For an explanation of the use of acquireFence, see
   * http://gee.cs.oswego.edu/dl/html/j9mm.html as well as Boehm's
   * paper (above). Note that sequence validation (mainly method
   * validate()) requires stricter ordering rules than apply to
   * normal volatile reads (of "state").  To ensure that writeLock
   * acquisitions strictly precede subsequent writes in cases where
   * this is not already forced, we use a storeStoreFence.
   *
   * The memory layout keeps lock state and queue pointers together
   * (normally on the same cache line). This usually works well for
   * read-mostly loads. In most other cases, the natural tendency of
   * CLH locks to reduce memory contention lessens motivation to
   * further spread out contended locations, but might be subject to
   * future improvements.
   */

  /* Head of CLH queue */
  @alwaysinline private def headAtomic = new AtomicRef[Node](
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "head"))
  )
  @transient @volatile private var head: Node = _

  /* Tail (last) of CLH queue */
  @alwaysinline private def tailAtomic = new AtomicRef[Node](
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "tail"))
  )
  @transient @volatile private var tail: Node = _

  // views
  @transient var readLockView: ReadLockView = _
  @transient var writeLockView: WriteLockView = _
  @transient var readWriteLockView: ReadWriteLockView = _

  /* Lock sequence/state */

  // A new lock is initially in unlocked state.

  // private[locks] makes var visibile to classFieldRawPtr but not to world.
  @transient @volatile private[locks] var state: Long = ORIGIN

  /* extra reader count when state read count saturated */
  @transient private var readerOverflow = 0

  // internal lock methods

  @alwaysinline private def stateAtomic = new AtomicLongLong(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "state"))
  )

  private def casState(expect: Long, update: Long): Boolean =
    stateAtomic.compareExchangeStrong(expect, update)

  // @ReservedStackAccess
  private def tryAcquireWrite(): Long = {
    val s = state
    val nextState = s | WBIT

    if (((s & ABITS) == 0L) && casState(s, nextState)) {
      stdatomic.atomic_thread_fence(memory_order_release)
      nextState
    } else 0L
  }

  // @ReservedStackAccess
  private def tryAcquireRead(): Long = {
    while (true) {
      val s = state
      val m = s & ABITS
      var nextState = s + RUNIT

      if (m < RFULL) {
        if (casState(s, nextState)) {
          return nextState
        }
      } else if (m == WBIT) {
        return 0L
      } else {
        if (({ nextState = tryIncReaderOverflow(s); nextState }) != 0L)
          return nextState
      }
    }

    0L // Should never get here, keep compiler happy.
  }

  /*
   * Returns an unlocked state, incrementing the version and
   * avoiding special failure value 0L.
   *
   * @param s a write-locked state (or stamp)
   */
  private def unlockWriteState(s: Long): Long = {
    val sPlus = s + WBIT
    if (sPlus == 0L) ORIGIN
    else sPlus
  }

  private def releaseWrite(s: Long): Long = {
    val nextState = { state = unlockWriteState(s); state }
    signalNext(head)
    nextState
  }

  /*
   * Exclusively acquires the lock, blocking if necessary
   * until available.
   *
   * @return a write stamp that can be used to unlock or convert mode
   */
  // @ReservedStackAccess
  def writeLock(): Long = {
    // try unconditional CAS confirming weak read
    val s = stateAtomic.load(memory_order_relaxed) & ~ABITS
    val nextState = s | WBIT

    if (casState(s, nextState)) {
      stateAtomic.store(nextState, memory_order_relaxed)
      nextState
    } else {
      acquireWrite(false, false, 0L)
    }
  }

  /*
   * Exclusively acquires the lock if it is immediately available.
   *
   * @return a write stamp that can be used to unlock or convert mode,
   * or zero if the lock is not available
   */
  def tryWriteLock(): Long =
    tryAcquireWrite()

  /*
   * Exclusively acquires the lock if it is available within the
   * given time and the current thread has not been interrupted.
   * Behavior under timeout and interruption matches that specified
   * for method {@link Lock#tryLock(long,TimeUnit)}.
   *
   * @param time the maximum time to wait for the lock
   * @param unit the time unit of the {@code time} argument
   * @return a write stamp that can be used to unlock or convert mode,
   * or zero if the lock is not available
   * @throws InterruptedException if the current thread is interrupted
   * before acquiring the lock
   */
  def tryWriteLock(time: Long, unit: TimeUnit): Long = {
    val nanos = unit.toNanos(time)

    if (!Thread.interrupted()) {
      var nextState = tryAcquireWrite()

      if (nextState != 0L)
        return nextState
      if (nanos <= 0L)
        return 0L

      nextState = acquireWrite(true, true, System.nanoTime() + nanos)
      if (nextState != INTERRUPTED)
        return nextState
    }

    throw new InterruptedException()
  }

  /*
   * Exclusively acquires the lock, blocking if necessary
   * until available or the current thread is interrupted.
   * Behavior under interruption matches that specified
   * for method {@link Lock#lockInterruptibly()}.
   *
   * @return a write stamp that can be used to unlock or convert mode
   * @throws InterruptedException if the current thread is interrupted
   * before acquiring the lock
   */
  def writeLockInterruptibly(): Long = {
    var nextState = 0L

    if (!Thread.interrupted() &&
        ({ nextState = tryAcquireWrite(); nextState } != 0L ||
        ({
          nextState = acquireWrite(true, false, 0L); nextState
        } != INTERRUPTED)))
      return nextState

    throw new InterruptedException()
  }

  /*
   * Non-exclusively acquires the lock, blocking if necessary
   * until available.
   *
   * @return a read stamp that can be used to unlock or convert mode
   */
  // @ReservedStackAccess
  def readLock(): Long = {
    // unconditionally optimistically try non-overflow case once
    val s = stateAtomic.load(memory_order_relaxed) & RSAFE
    val nextState = s + RUNIT

    if (casState(s, nextState))
      nextState
    else
      acquireRead(false, false, 0L)
  }

  /*
   * Non-exclusively acquires the lock if it is immediately available.
   *
   * @return a read stamp that can be used to unlock or convert mode,
   * or zero if the lock is not available
   */
  def tryReadLock(): Long =
    tryAcquireRead()

  /*
   * Non-exclusively acquires the lock if it is available within the
   * given time and the current thread has not been interrupted.
   * Behavior under timeout and interruption matches that specified
   * for method {@link Lock#tryLock(long,TimeUnit)}.
   *
   * @param time the maximum time to wait for the lock
   * @param unit the time unit of the {@code time} argument
   * @return a read stamp that can be used to unlock or convert mode,
   * or zero if the lock is not available
   * @throws InterruptedException if the current thread is interrupted
   * before acquiring the lock
   */
  def tryReadLock(time: Long, unit: TimeUnit): Long = {
    val nanos = unit.toNanos(time)

    if (!Thread.interrupted()) {
      var nextState = tryAcquireRead()

      if ((tail == head) && nextState != 0L)
        return nextState
      if (nanos <= 0L)
        return 0L

      nextState = acquireRead(true, true, System.nanoTime() + nanos)
      if (nextState != INTERRUPTED)
        return nextState
    }

    throw new InterruptedException()
  }

  /*
   * Non-exclusively acquires the lock, blocking if necessary
   * until available or the current thread is interrupted.
   * Behavior under interruption matches that specified
   * for method {@link Lock#lockInterruptibly()}.
   *
   * @return a read stamp that can be used to unlock or convert mode
   * @throws InterruptedException if the current thread is interrupted
   * before acquiring the lock
   */
  def readLockInterruptibly(): Long = {
    var nextState = 0L

    if (!Thread.interrupted() &&
        (({ nextState = tryAcquireRead(); nextState } != 0L) ||
        ({
          nextState = acquireRead(true, false, 0L); nextState
        }) != INTERRUPTED))
      return nextState

    throw new InterruptedException()
  }

  /*
   * Returns a stamp that can later be validated, or zero
   * if exclusively locked.
   *
   * @return a valid optimistic read stamp, or zero if exclusively locked
   */
  def tryOptimisticRead(): Long = {
    val s = state
    if ((state & WBIT) == 0L) (s & SBITS)
    else 0L
  }

  /*
   * Returns true if the lock has not been exclusively acquired
   * since issuance of the given stamp. Always returns false if the
   * stamp is zero. Always returns true if the stamp represents a
   * currently held lock. Invoking this method with a value not
   * obtained from {@link #tryOptimisticRead} or a locking method
   * for this lock has no defined effect or result.
   *
   * @param stamp a stamp
   * @return {@code true} if the lock has not been exclusively acquired
   * since issuance of the given stamp; else false
   */
  def validate(stamp: Long): Boolean = {
    stdatomic.atomic_thread_fence(memory_order_acquire)
    (stamp & SBITS) == (state & SBITS)
  }

  /*
   * If the lock state matches the given stamp, releases the
   * exclusive lock.
   *
   * @param stamp a stamp returned by a write-lock operation
   * @throws IllegalMonitorStateException if the stamp does
   * not match the current state of this lock
   */
  // @ReservedStackAccess
  def unlockWrite(stamp: Long): Unit = {
    if (state != stamp || (stamp & WBIT) == 0L)
      throw new IllegalMonitorStateException()
    releaseWrite(stamp)
  }

  /*
   * If the lock state matches the given stamp, releases the
   * non-exclusive lock.
   *
   * @param stamp a stamp returned by a read-lock operation
   * @throws IllegalMonitorStateException if the stamp does
   * not match the current state of this lock
   */
  // @ReservedStackAccess
  def unlockRead(stamp: Long): Unit = {
    if ((stamp & RBITS) != 0L) {
      var s = 0L
      var m = 0L
      while (({ s = state; state } & SBITS) == (stamp & SBITS) &&
          ({ m = s & RBITS; m } != 0L)) {
        if (m < RFULL) {
          if (casState(s, s - RUNIT)) {
            if (m == RUNIT)
              signalNext(head)
            return
          }
        } else if (tryDecReaderOverflow(s) != 0L)
          return
      }
    }

    throw new IllegalMonitorStateException()
  }

  /*
   * If the lock state matches the given stamp, releases the
   * corresponding mode of the lock.
   *
   * @param stamp a stamp returned by a lock operation
   * @throws IllegalMonitorStateException if the stamp does
   * not match the current state of this lock
   */
  def unlock(stamp: Long): Unit = {
    if ((stamp & WBIT) != 0L)
      unlockWrite(stamp)
    else
      unlockRead(stamp)
  }

  /*
   * If the lock state matches the given stamp, atomically performs one of
   * the following actions. If the stamp represents holding a write
   * lock, returns it.  Or, if a read lock, if the write lock is
   * available, releases the read lock and returns a write stamp.
   * Or, if an optimistic read, returns a write stamp only if
   * immediately available. This method returns zero in all other
   * cases.
   *
   * @param stamp a stamp
   * @return a valid write stamp, or zero on failure
   */
  def tryConvertToWriteLock(stamp: Long): Long = {
    val a = stamp & ABITS
    var m = 0L
    var s = 0L
    var nextState = 0L

    var done = false

    while (!done
        && (({ s = state; s } & SBITS) == (stamp & SBITS))) {
      if ({ m = s & ABITS; m } == 0L) {
        if (a != 0L)
          done = true
        else if (casState(s, { nextState = s | WBIT; nextState })) {
          stdatomic.atomic_thread_fence(memory_order_release)
          return nextState
        }
      } else if (m == WBIT) {
        if (a != m)
          done = true
        else
          return stamp
      } else if (m == RUNIT && a != 0L) {
        if (casState(s, { nextState = s - RUNIT + WBIT; nextState }))
          return nextState
      } else
        done = true
    }

    0L
  }

  /*
   * If the lock state matches the given stamp, atomically performs one of
   * the following actions. If the stamp represents holding a write
   * lock, releases it and obtains a read lock.  Or, if a read lock,
   * returns it. Or, if an optimistic read, acquires a read lock and
   * returns a read stamp only if immediately available. This method
   * returns zero in all other cases.
   *
   * @param stamp a stamp
   * @return a valid read stamp, or zero on failure
   */
  def tryConvertToReadLock(stamp: Long): Long = {
    var a = 0L
    var s = 0L
    var nextState = 0L

    var done = false

    while (!done
        && (({ s = state; s } & SBITS) == (stamp & SBITS))) {
      if ({ a = stamp & ABITS; a } >= WBIT) {
        if (s != stamp) // write stamp
          done = true
        else {
          state = unlockWriteState(s) + RUNIT
          nextState = state
          signalNext(head)
          return nextState
        }
      } else if (a == 0L) { // optimistic read stamp
        if ((s & ABITS) < RFULL) {
          if (casState(s, { nextState = s + RUNIT; nextState }))
            return nextState
        } else if (({ nextState = tryIncReaderOverflow(s); nextState }) != 0L)
          return nextState
      } else { // already a read stamp
        if ((s & ABITS) == 0L)
          done = true
        else
          return stamp
      }
    }

    0L
  }

  /*
   * If the lock state matches the given stamp then, atomically, if the stamp
   * represents holding a lock, releases it and returns an
   * observation stamp.  Or, if an optimistic read, returns it if
   * validated. This method returns zero in all other cases, and so
   * may be useful as a form of "tryUnlock".
   *
   * @param stamp a stamp
   * @return a valid optimistic read stamp, or zero on failure
   */
  def tryConvertToOptimisticRead(stamp: Long): Long = {
    var a = 0L
    var m = 0L
    var s = 0L
    var nextState = 0L

    stdatomic.atomic_thread_fence(memory_order_acquire)

    var done = false

    while (!done
        && (({ s = state; s } & SBITS) == (stamp & SBITS))) {
      if ({ a = stamp & ABITS; a } >= WBIT) {
        if (s != stamp) // write stamp
          done = true
        else
          return releaseWrite(s)
      } else if (a == 0L) { // already an optimistic read stamp
        return stamp
      } else if ({ m = s & ABITS; m } == 0L) { // invalid read stamp
        done = true
      } else if (m < RFULL) {
        if (casState(s, { nextState = s - RUNIT; nextState })) {
          if (m == RUNIT)
            signalNext(head)
          return nextState & SBITS
        }
      } else if (({ nextState = tryDecReaderOverflow(s); nextState }) != 0L)
        return nextState & SBITS
    }

    0L
  }

  /*
   * Releases the write lock if it is held, without requiring a
   * stamp value. This method may be useful for recovery after
   * errors.
   *
   * @return {@code true} if the lock was held, else false
   */
  // @ReservedStackAccess
  def tryUnlockWrite(): Boolean = {
    val s = state

    if ((s & WBIT) != 0L) {
      releaseWrite(s)
      true
    } else {
      false
    }
  }

  /*
   * Releases one hold of the read lock if it is held, without
   * requiring a stamp value. This method may be useful for recovery
   * after errors.
   *
   * @return {@code true} if the read lock was held, else false
   */
  // @ReservedStackAccess
  def tryUnlockRead(): Boolean = {
    var s = 0L
    var m = 0L

    while (({ m = { s = state; s } & ABITS; m } != 0L) && (m < WBIT)) {
      if (m < RFULL) {
        if (casState(s, s - RUNIT)) {
          if (m == RUNIT)
            signalNext(head)
          return true
        }
      } else if (tryDecReaderOverflow(s) != 0L)
        return true
    }

    false
  }

  // status monitoring methods

  /*
   * Returns combined state-held and overflow read count for given
   * state s.
   */
  def getReadLockCount(s: Long): Int = {
    var readers = s & RBITS

    if (readers >= RFULL)
      readers = RFULL + readerOverflow

    readers.toInt
  }

  /*
   * Returns {@code true} if the lock is currently held exclusively.
   *
   * @return {@code true} if the lock is currently held exclusively
   */
  def isWriteLocked(): Boolean =
    (state & WBIT) != 0L

  /*
   * Returns {@code true} if the lock is currently held non-exclusively.
   *
   * @return {@code true} if the lock is currently held non-exclusively
   */
  def isReadLocked(): Boolean =
    (state & RBITS) != 0L

  /*
   * Queries the number of read locks held for this lock. This
   * method is designed for use in monitoring system state, not for
   * synchronization control.
   * @return the number of read locks held
   */
  def getReadLockCount(): Int =
    getReadLockCount(state)

  /*
   * Returns a string identifying this lock, as well as its lock
   * state.  The state, in brackets, includes the String {@code
   * "Unlocked"} or the String {@code "Write-locked"} or the String
   * {@code "Read-locks:"} followed by the current number of
   * read-locks held.
   *
   * @return a string identifying this lock, as well as its lock state
   */
  override def toString(): String = {
    val s = state
    val suffix =
      if ((s & ABITS) == 0L) "[Unlocked]"
      else if ((s & WBIT) != 0L) "[Write-locked]"
      else s"[Read-locks:${getReadLockCount(s)}]"
    s"${super.toString()}${suffix}"
  }

  // views

  /*
   * Returns a plain {@link Lock} view of this StampedLock in which
   * the {@link Lock#lock} method is mapped to {@link #readLock},
   * and similarly for other methods. The returned Lock does not
   * support a {@link Condition}; method {@link Lock#newCondition()}
   * throws {@code UnsupportedOperationException}.
   *
   * @return the lock
   */
  def asReadLock(): Lock = {
    val v = readLockView
    if (v != null) v
    else {
      readLockView = new ReadLockView
      readLockView
    }
  }

  /*
   * Returns a plain {@link Lock} view of this StampedLock in which
   * the {@link Lock#lock} method is mapped to {@link #writeLock},
   * and similarly for other methods. The returned Lock does not
   * support a {@link Condition}; method {@link Lock#newCondition()}
   * throws {@code UnsupportedOperationException}.
   *
   * @return the lock
   */
  def asWriteLock(): Lock = {
    val v = writeLockView
    if (v != null) v
    else {
      writeLockView = new WriteLockView
      writeLockView
    }
  }

  /*
   * Returns a {@link ReadWriteLock} view of this StampedLock in
   * which the {@link ReadWriteLock#readLock()} method is mapped to
   * {@link #asReadLock()}, and {@link ReadWriteLock#writeLock()} to
   * {@link #asWriteLock()}.
   *
   * @return the lock
   */
  def asReadWriteLock(): ReadWriteLock = {
    val v = readWriteLockView
    if (v != null) v
    else {
      readWriteLockView = new ReadWriteLockView
      readWriteLockView
    }
  }

  // view classes

  final class ReadLockView extends Lock {
    def lock(): Unit =
      readLock()

    def lockInterruptibly(): Unit =
      readLockInterruptibly()

    def tryLock(): Boolean =
      tryReadLock() != 0L

    def tryLock(time: Long, unit: java.util.concurrent.TimeUnit): Boolean =
      tryReadLock(time, unit) != 0L

    def unlock(): Unit =
      unstampedUnlockRead()

    def newCondition(): java.util.concurrent.locks.Condition =
      throw new UnsupportedOperationException()
  }

  final class WriteLockView extends Lock {
    def lock(): Unit =
      writeLock()

    def lockInterruptibly(): Unit =
      writeLockInterruptibly()

    def tryLock(): Boolean =
      tryWriteLock() != 0L

    def tryLock(time: Long, unit: java.util.concurrent.TimeUnit): Boolean =
      tryWriteLock(time, unit) != 0L

    def unlock(): Unit =
      unstampedUnlockWrite()

    def newCondition(): java.util.concurrent.locks.Condition =
      throw new UnsupportedOperationException()
  }

  final class ReadWriteLockView extends ReadWriteLock {
    def readLock(): Lock =
      asReadLock()

    def writeLock(): Lock =
      asWriteLock()
  }

  // Unlock methods without stamp argument checks for view classes.
  // Needed because view-class lock methods throw away stamps.

  final def unstampedUnlockWrite(): Unit = {
    val s = state
    if ((s & WBIT) == 0L)
      throw new IllegalMonitorStateException()
    releaseWrite(s)
  }

  final def unstampedUnlockRead(): Unit = {
    val s = state
    val m = s & RBITS

    while (m > 0L) {
      if (m < RFULL) {
        if (casState(s, s - RUNIT)) {
          if (m == RUNIT)
            signalNext(head)
          return
        }
      } else if (tryDecReaderOverflow(s) != 0L)
        return
    }

    throw new IllegalMonitorStateException()
  }

  // Scala Native does not support ObjectInputStream.
  //  private def readObject(s: ObjectInputStream): Unit

  // overflow handling methods

  /*
   * Tries to increment readerOverflow by first setting state
   * access bits value to RBITS, indicating hold of spinlock,
   * then updating, then releasing.
   *
   * @param s a reader overflow stamp: (s & ABITS) >= RFULL
   * @return new stamp on success, else zero
   */
  private def tryIncReaderOverflow(s: Long): Long = {
    // assert (s & ABITS) >= RFULL

    if ((s & ABITS) != RFULL) {
      Thread.onSpinWait()
    } else if (casState(s, s | RBITS)) {
      readerOverflow += 1
      return { state = s; s }
    }

    0L
  }

  /*
   * Tries to decrement readerOverflow.
   *
   * @param s a reader overflow stamp: (s & ABITS) >= RFULL
   * @return new stamp on success, else zero
   */
  private def tryDecReaderOverflow(s: Long): Long = {
    // assert (s & ABITS) >= RFULL

    if ((s & ABITS) != RFULL)
      Thread.onSpinWait()
    else if (casState(s, s | RBITS)) {
      val r = readerOverflow
      var nextState = 0L

      if (r > 0) {
        readerOverflow = r - 1
        nextState = s
      } else {
        nextState = s - RUNIT
      }

      return { state = nextState; state }
    }

    0L
  }

  // queue link methods
  private def casTail(c: Node, v: Node): Boolean =
    tailAtomic.compareExchangeStrong(c, v)

  /* tries once to CAS a new dummy node for head */
  private def tryInitializeHead(): Unit = {
    val h = new WriterNode()

    if (headAtomic.compareExchangeStrong(null.asInstanceOf[WriterNode], h))
      tail = h
  }

  /*
   * For explanation, see above and AbstractQueuedSynchronizer
   * internal documentation.
   *
   * @param interruptible true if should check interrupts and if so
   * return INTERRUPTED
   * @param timed if true use timed waits
   * @param time the System.nanoTime value to timeout at (and return zero)
   * @return next state, or INTERRUPTED
   */
  private def acquireWrite(
      interruptible: Boolean,
      timed: Boolean,
      time: Long
  ): Long = {
    // retries upon unpark of first thread
    var spins = 0
    var postSpins = 0
    var interrupted = false
    var first = false
    var node: WriterNode = null

    var breakSeen = false

    while (!breakSeen) {
      var continueSeen = false

      val pred: Node =
        if (node == null) null
        else node.prev

      if (!first && pred != null &&
          ! { first = (head == pred); first }) {
        if (pred.status < 0) {
          cleanQueue() // predecessor cancelled
          continueSeen = true
        } else if (pred.prev == null) {
          Thread.onSpinWait() // ensure serialization
          continueSeen = true
        }
      }

      if (!continueSeen) {
        val s = state
        val nextState = s | WBIT

        if ((first || pred == null) && (s & ABITS) == 0L &&
            casState(s, nextState)) {
          stdatomic.atomic_thread_fence(memory_order_release)
          if (first) {
            node.prev = null
            head = node
            pred.next = null
            node.waiter = null
            if (interrupted)
              Thread.currentThread().interrupt()
          }
          return nextState
        } else if (node == null) { // retry before enqueuing
          node = new WriterNode()
        } else if (pred == null) { // try to enqueue
          val t = tail
          node.setPrevRelaxed(t)
          if (t == null)
            tryInitializeHead()
          else if (!casTail(t, node))
            node.setPrevRelaxed(null); // back out
          else
            t.next = node;
        } else if (first && spins != 0) { // reduce unfairness
          spins -= 1
          Thread.onSpinWait()
        } else if (node.status == 0) { // enable signal
          if (node.waiter == null)
            node.waiter = Thread.currentThread()
          node.status = WAITING
        } else {
          var nanos = 0L
          postSpins = ((postSpins << 1) | 1)
          spins = postSpins
          if (!timed)
            LockSupport.park(this)
          else if ({ nanos = time - System.nanoTime(); nanos } > 0L)
            LockSupport.parkNanos(this, nanos)
          else
            breakSeen = true

          node.clearStatus()

          interrupted |= Thread.interrupted()
          if (interrupted && interruptible)
            breakSeen = true
        }
      }

    }

    cancelAcquire(node, interrupted)
  }

  /*
   * See above for explanation.
   *
   * @param interruptible true if should check interrupts and if so
   * return INTERRUPTED
   * @param timed if true use timed waits
   * @param time the System.nanoTime value to timeout at (and return zero)
   * @return next state, or INTERRUPTED
   */
  private def acquireRead(
      interruptible: Boolean,
      timed: Boolean,
      time: Long
  ): Long = {
    var interrupted = false
    var node: ReaderNode = null

    /*
     * Loop:
     *   if empty, try to acquire
     *   if tail is Reader, try to cowait; restart if leader stale or cancels
     *   else try to create and enqueue node, and wait in 2nd loop below
     */

    var nextState = 0L
    var breakSeen = false

    while (!breakSeen) {
      var leader: ReaderNode = null
      var tailPred: Node = null
      val t = tail

      if ((t == null || { tailPred = t.prev; tailPred } == null) &&
          ({
            nextState = tryAcquireRead(); nextState
          }) != 0L) { // try now if empty
        return nextState
      } else if (t == null) {
        tryInitializeHead()
      } else if (tailPred == null || !(t.isInstanceOf[ReaderNode])) {
        if (node == null)
          node = new ReaderNode()

        if (tail == t) {
          node.setPrevRelaxed(t)
          if (casTail(t, node)) {
            t.next = node
            breakSeen = true // node is leader; wait in loop below
          } else {
            node.setPrevRelaxed(null)
          }
        }
      } else if (({ leader = t.asInstanceOf[ReaderNode]; leader }) == tail) {
        // try to cowait
        var attached = false
        var done = false

        while (!done) {
          if (leader.status < 0 || leader.prev == null)
            done = true
          else if (node == null)
            node = new ReaderNode()
          else if (node.waiter == null)
            node.waiter = Thread.currentThread()
          else if (!attached) {
            val c = leader.cowaiters.asInstanceOf[ReaderNode]
            node.setCowaitersRelaxed(c)
            attached = leader.casCowaiters(c, node)
            if (!attached)
              node.setCowaitersRelaxed(null)
          } else {
            var nanos = 0L

            if (!timed)
              LockSupport.park(this)
            else if ({ nanos = time - System.nanoTime(); nanos } > 0L)
              LockSupport.parkNanos(this, nanos)
            interrupted |= Thread.interrupted()
            if ((interrupted && interruptible) ||
                (timed && nanos <= 0L))
              return cancelCowaiter(node, leader, interrupted);
          }
        }

        if (node != null)
          node.waiter = null;
        val ns = tryAcquireRead()
        signalCowaiters(leader)
        if (interrupted)
          Thread.currentThread().interrupt()

        if (ns != 0L)
          return ns

        node = null // restart if stale, missed, or leader cancelled
      }
    }

    // node is leader of a cowait group; almost same as acquireWrite

    // retries upon unpark of first thread
    var spins = 0
    var postSpins = 0

    var first = false
    var pred: Node = null

    nextState = 0L
    breakSeen = false

    while (!breakSeen) {
      var continueSeen = false

      if (!first && ({ pred = node.prev; pred } != null) &&
          ! { first = (head == pred); first }) {
        if (pred.status < 0) {
          cleanQueue() // predecessor cancelled
          continueSeen = true
        } else if (pred.prev == null) {
          Thread.onSpinWait(); // ensure serialization
          continueSeen = true
        }
      }

      if (!continueSeen) {
        if ((first || pred == null) &&
            { nextState = tryAcquireRead(); nextState } != 0L) {
          if (first) {
            node.prev = null
            head = node
            pred.next = null
            node.waiter = null
          }
          signalCowaiters(node)
          if (interrupted)
            Thread.currentThread().interrupt()
          return nextState
        } else if (first && spins != 0) {
          spins -= 1
          Thread.onSpinWait()
        } else if (node.status == 0) {
          if (node.waiter == null)
            node.waiter = Thread.currentThread()
          node.status = WAITING
        } else {
          var nanos = 0L
          postSpins = ((postSpins << 1) | 1)
          spins = postSpins
          if (!timed)
            LockSupport.park(this)
          else if ({ nanos = time - System.nanoTime(); nanos } > 0L)
            LockSupport.parkNanos(this, nanos)
          else
            breakSeen = true

          if (!breakSeen) {
            node.clearStatus()

            interrupted |= Thread.interrupted()
            if (interrupted && interruptible)
              breakSeen = true
          }
        }
      }
    }

    cancelAcquire(node, interrupted);
  }

  // Cancellation support

  /*
   * Possibly repeatedly traverses from tail, unsplicing cancelled
   * nodes until none are found. Unparks nodes that may have been
   * relinked to be next eligible acquirer.
   */
  private def cleanQueue(): Unit = {
    while (true) { // restart point
      var q: Node = tail
      var p: Node = null
      var s: Node = null
      var n: Node = null

      var breakSeen = false

      while (!breakSeen) { // (p, q, s) triples
        if (q == null || { p = q.prev; p } == null)
          return // end of list

        val inconsistent =
          if (s == null)
            tail != q
          else
            (s.prev != q || s.status < 0)

        if (inconsistent)
          breakSeen = true // inconsistent

        if (!breakSeen && (q.status < 0)) { // cancelled
          val casNextResult =
            if (s == null) casTail(q, p)
            else s.casPrev(q, p)

          if (casNextResult && (q.prev == p)) {
            p.casNext(q, s) // OK if fails
            if (p.prev == null)
              signalNext(p)
          }

          breakSeen = true
        }

        if (!breakSeen && ({ n = p.next; n } != q)) { // help finish
          if (n != null && q.prev == p && q.status >= 0) {
            p.casNext(n, q)
            if (p.prev == null)
              signalNext(p)
          }
          breakSeen = true
        }

        if (!breakSeen) {
          s = q
          q = q.prev
        }
      } // inner
    } // outer
  }

  /*
   * If leader exists, possibly repeatedly traverses cowaiters,
   * unsplicing the given cancelled node until not found.
   */
  private def unlinkCowaiter(node: ReaderNode, leader: ReaderNode): Unit = {
    if (leader != null) {
      while (leader.prev != null && leader.status >= 0) {
        var p: ReaderNode = leader
        var q: ReaderNode = null

        while (true) {
          if (({ q = p.cowaiters; q }) == null)
            return
          if (q == node) {
            p.casCowaiters(q, q.cowaiters)
            // recheck even if succeeded
          } else {
            p = q
          }
        }
      }
    }
  }

  /*
   * If node non-null, forces cancel status and unsplices it from
   * queue, wakes up any cowaiters, and possibly wakes up successor
   * to recheck status.
   *
   * @param node the waiter (may be null if not yet enqueued)
   * @param interrupted if already interrupted
   * @return INTERRUPTED if interrupted or Thread.interrupted, else zero
   */
  private def cancelAcquire(node: Node, interrupted: Boolean): Long = {
    if (node != null) {
      node.waiter = null
      node.status = CANCELLED
      cleanQueue()
      if (node.isInstanceOf[ReaderNode])
        signalCowaiters(node.asInstanceOf[ReaderNode])
    }

    if (interrupted || Thread.interrupted()) INTERRUPTED
    else 0L
  }

  /*
   * If node non-null, forces cancel status and unsplices from
   * leader's cowaiters list unless/until it is also cancelled.
   *
   * @param node if non-null, the waiter
   * @param leader if non-null, the node heading cowaiters list
   * @param interrupted if already interrupted
   * @return INTERRUPTED if interrupted or Thread.interrupted, else zero
   */
  private def cancelCowaiter(
      node: ReaderNode,
      leader: ReaderNode,
      interrupted: Boolean
  ): Long = {
    if (node != null) {
      node.waiter = null;
      node.status = CANCELLED;
      unlinkCowaiter(node, leader);
    }

    if (interrupted || Thread.interrupted()) INTERRUPTED
    else 0L
  }
}

@SerialVersionUID(-6001602636862214147L)
object StampedLock {

  /* SN: Note well the notes at top of companion class, especially the
   * 'memory layout' section.
   */

  /* The number o bits to use for reader count before overflowing */
  final val LG_READERS = 7 // 127 readers

  // Values for lock state and stamp operations
  final val RUNIT = 1L
  final val WBIT = 1L << LG_READERS
  final val RBITS = WBIT - 1L
  final val RFULL = RBITS - 1L
  final val ABITS = RBITS | WBIT
  final val SBITS = ~RBITS // note overlap with ABITS
  // not writing and conservatively non-overflowing
  final val RSAFE = ~(3L << (LG_READERS - 1))

  /*
   * 3 stamp modes can be distinguished by examining (m = stamp & ABITS):
   * write mode: m == WBIT
   * optimistic read mode: m == 0L (even when read lock is held)
   * read mode: m > 0L && m <= RFULL (the stamp is a copy of state, but the
   * read hold count in the stamp is unused other than to determine mode)
   *
   * This differs slightly from the encoding of state:
   * (state & ABITS) == 0L indicates the lock is currently unlocked.
   * (state & ABITS) == RBITS is a special transient value
   * indicating spin-locked to manipulate reader bits overflow.
   */

  /* Initial value for lock state; avoids failure value zero. */
  final val ORIGIN = WBIT << 1

  // Special value from cancelled acquire methods so caller can throw IE
  final val INTERRUPTED = 1L

  // Bits for Node.status
  final val WAITING = 1
  final val CANCELLED = 0x80000000 // must be negative

  /* CLH nodes */
  abstract class Node {
    @volatile var prev: Node = _ // initially attached via casTail
    @volatile var next: Node = _ // visibly nonnull when signallable
    var waiter: Thread = _ // visibly nonnull when enqueued
    @volatile var status: Int = 0 // written by owner, atomic bit ops by others

    @alwaysinline private def prevAtomic = new AtomicRef[Node](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "prev"))
    )

    @alwaysinline private def nextAtomic = new AtomicRef[Node](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "next"))
    )

    @alwaysinline private def statusAtomic = new AtomicInt(
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "status"))
    )

    final def casPrev(c: Node, v: Node): Boolean = // for cleanQueue
      prevAtomic.compareExchangeWeak(c, v)

    final def casNext(c: Node, v: Node): Boolean = // for cleanQueue
      nextAtomic.compareExchangeWeak(c, v)

    final def getAndUnsetStatus(v: Int): Int = // for signalling
      statusAtomic.fetchAnd(~v)

    final def setPrevRelaxed(p: Node): Unit = // for off-queue assignment
      prevAtomic.store(p)

    final def setStatusRelaxed(s: Int) = // for off-queue assignment
      statusAtomic.store(s)

    final def clearStatus(): Unit = // for reducing unneeded signals
      statusAtomic.store(0, memory_order_relaxed) // U.putIntOpaque
  }

  final class WriterNode extends Node {} // node for writers

  final class ReaderNode extends Node { // node for readers
    @volatile var cowaiters: ReaderNode = _ // list of linked readers

    @alwaysinline private def cowaitersAtomic = new AtomicRef[ReaderNode](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "cowaiters"))
    )

    final def casCowaiters(c: ReaderNode, v: ReaderNode): Boolean =
      cowaitersAtomic.compareExchangeWeak(c, v)

    final def setCowaitersRelaxed(p: ReaderNode): Unit =
      cowaitersAtomic.store(p, memory_order_relaxed)
  }

  // status monitoring methods
  /*
   * Tells whether a stamp represents holding a lock exclusively.
   * This method may be useful in conjunction with
   * {@link #tryConvertToWriteLock}, for example: <pre> {@code
   * long stamp = sl.tryOptimisticRead();
   * try {
   *   ...
   *   stamp = sl.tryConvertToWriteLock(stamp);
   *   ...
   * } finally {
   *   if (StampedLock.isWriteLockStamp(stamp))
   *     sl.unlockWrite(stamp);
   * }}</pre>
   *
   * @param stamp a stamp returned by a previous StampedLock operation
   * @return {@code true} if the stamp was returned by a successful
   *   write-lock operation
   * @since 10
   */
  def isWriteLockStamp(stamp: Long): Boolean =
    (stamp & ABITS) == WBIT

  /*
   * Tells whether a stamp represents holding a lock non-exclusively.
   * This method may be useful in conjunction with
   * {@link #tryConvertToReadLock}, for example: <pre> {@code
   * long stamp = sl.tryOptimisticRead();
   * try {
   *   ...
   *   stamp = sl.tryConvertToReadLock(stamp);
   *   ...
   * } finally {
   *   if (StampedLock.isReadLockStamp(stamp))
   *     sl.unlockRead(stamp);
   * }}</pre>
   *
   * @param stamp a stamp returned by a previous StampedLock operation
   * @return {@code true} if the stamp was returned by a successful
   *   read-lock operation
   * @since 10
   */
  def isReadLockStamp(stamp: Long): Boolean =
    (stamp & RBITS) != 0L

  /*
   * Tells whether a stamp represents holding a lock.
   * This method may be useful in conjunction with
   * {@link #tryConvertToReadLock} and {@link #tryConvertToWriteLock},
   * for example: <pre> {@code
   * long stamp = sl.tryOptimisticRead();
   * try {
   *   ...
   *   stamp = sl.tryConvertToReadLock(stamp);
   *   ...
   *   stamp = sl.tryConvertToWriteLock(stamp);
   *   ...
   * } finally {
   *   if (StampedLock.isLockStamp(stamp))
   *     sl.unlock(stamp);
   * }}</pre>
   *
   * @param stamp a stamp returned by a previous StampedLock operation
   * @return {@code true} if the stamp was returned by a successful
   *   read-lock or write-lock operation
   * @since 10
   */
  def isLockStamp(stamp: Long): Boolean =
    (stamp & ABITS) != 0L

  /*
   * Tells whether a stamp represents a successful optimistic read.
   *
   * @param stamp a stamp returned by a previous StampedLock operation
   * @return {@code true} if the stamp was returned by a successful
   *   optimistic read operation, that is, a non-zero return from
   *   {@link #tryOptimisticRead()} or
   *   {@link #tryConvertToOptimisticRead(long)}
   * @since 10
   */
  def isOptimisticReadStamp(stamp: Long): Boolean =
    (stamp & ABITS) == 0L && (stamp != 0L)

  // release methods

  /*
   * Wakes up the successor of given node, if one exists, and unsets its
   * WAITING status to avoid park race. This may fail to wake up an
   * eligible thread when one or more have been cancelled, but
   * cancelAcquire ensures liveness.
   */
  final def signalNext(h: Node): Unit = {
    if (h != null) {
      val s = h.next
      if ((s != null) && (s.status > 0)) {
        s.getAndUnsetStatus(WAITING);
        LockSupport.unpark(s.waiter);
      }
    }
  }

  /*
   * Removes and unparks all cowaiters of node, if it exists.
   */
  final def signalCowaiters(node: ReaderNode): Unit = {
    if (node != null) {
      var c: ReaderNode = null
      while ({ c = node.cowaiters; c } != null) {
        if (node.casCowaiters(c, c.cowaiters))
          LockSupport.unpark(c.waiter)
      }
    }
  }
}
