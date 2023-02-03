/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */
package org.scalanative.testsuite.javalib.util.concurrent
package locks

import org.junit.Assert._
import org.junit.{Test, Ignore}
import org.scalanative.testsuite.javalib.util.concurrent.JSR166Test
import JSR166Test._

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks._

import java.util.{Arrays, Collection, HashSet, Date}

object ReentrantReadWriteLockTest {

  /** Subclass to expose protected methods
   */
  class PublicReentrantReadWriteLock(fair: Boolean)
      extends ReentrantReadWriteLock(fair) {
    def this() = this(false)

    override def getOwner(): Thread = super.getOwner()
    override def getQueuedThreads(): util.Collection[Thread] =
      super.getQueuedThreads()
    override def getWaitingThreads(c: Condition): util.Collection[Thread] =
      super.getWaitingThreads(c)
  }
  sealed trait AwaitMethod
  object AwaitMethod {
    case object await extends AwaitMethod
    case object awaitTimed extends AwaitMethod
    case object awaitNanos extends AwaitMethod
    case object awaitUntil extends AwaitMethod

    def values() = Array(await, awaitTimed, awaitNanos, awaitUntil)
  }
}
class ReentrantReadWriteLockTest extends JSR166Test {

  /** A runnable calling lockInterruptibly
   */
  class InterruptibleLockRunnable(val lock: ReentrantReadWriteLock)
      extends CheckedRunnable {
    @throws[InterruptedException]
    override def realRun(): Unit = { lock.writeLock.lockInterruptibly() }
  }

  /** A runnable calling lockInterruptibly that expects to be interrupted
   */
  class InterruptedLockRunnable(val lock: ReentrantReadWriteLock)
      extends CheckedInterruptedRunnable {
    @throws[InterruptedException]
    override def realRun(): Unit = { lock.writeLock.lockInterruptibly() }
  }

  /** Releases write lock, checking that it had a hold count of 1.
   */
  def releaseWriteLock(
      lock: ReentrantReadWriteLockTest.PublicReentrantReadWriteLock
  ): Unit = {
    val writeLock = lock.writeLock
    assertWriteLockedByMoi(lock)
    assertEquals(1, lock.getWriteHoldCount)
    writeLock.unlock()
    assertNotWriteLocked(lock)
  }

  /** Spin-waits until lock.hasQueuedThread(t) becomes true.
   */
  def waitForQueuedThread(
      lock: ReentrantReadWriteLockTest.PublicReentrantReadWriteLock,
      t: Thread
  ): Unit = {
    val startTime = System.nanoTime
    while ({ !lock.hasQueuedThread(t) }) {
      if (millisElapsedSince(startTime) > LONG_DELAY_MS)
        throw new AssertionError("timed out")
      Thread.`yield`()
    }
    assertTrue(t.isAlive)
    assertNotSame(t, lock.getOwner())
  }

  /** Checks that lock is not write-locked.
   */
  def assertNotWriteLocked(
      lock: ReentrantReadWriteLockTest.PublicReentrantReadWriteLock
  ): Unit = {
    assertFalse(lock.isWriteLocked)
    assertFalse(lock.isWriteLockedByCurrentThread)
    assertFalse(lock.writeLock.isHeldByCurrentThread)
    assertEquals(0, lock.getWriteHoldCount)
    assertEquals(0, lock.writeLock.getHoldCount)
    assertNull(lock.getOwner())
  }

  /** Checks that lock is write-locked by the given thread.
   */
  def assertWriteLockedBy(
      lock: ReentrantReadWriteLockTest.PublicReentrantReadWriteLock,
      t: Thread
  ): Unit = {
    assertTrue(lock.isWriteLocked)
    assertSame(t, lock.getOwner())
    assertEquals(t eq Thread.currentThread, lock.isWriteLockedByCurrentThread)
    assertEquals(
      t eq Thread.currentThread,
      lock.writeLock.isHeldByCurrentThread
    )
    assertEquals(t eq Thread.currentThread, lock.getWriteHoldCount > 0)
    assertEquals(t eq Thread.currentThread, lock.writeLock.getHoldCount > 0)
    assertEquals(0, lock.getReadLockCount)
  }

  /** Checks that lock is write-locked by the current thread.
   */
  def assertWriteLockedByMoi(
      lock: ReentrantReadWriteLockTest.PublicReentrantReadWriteLock
  ): Unit = { assertWriteLockedBy(lock, Thread.currentThread) }

  /** Checks that condition c has no waiters.
   */
  def assertHasNoWaiters(
      lock: ReentrantReadWriteLockTest.PublicReentrantReadWriteLock,
      c: Condition
  ): Unit = { assertHasWaiters(lock, c, Array.empty[Thread]: _*) }

  /** Checks that condition c has exactly the given waiter threads.
   */
  def assertHasWaiters(
      lock: ReentrantReadWriteLockTest.PublicReentrantReadWriteLock,
      c: Condition,
      threads: Thread*
  ): Unit = {
    lock.writeLock.lock()
    assertEquals(threads.length > 0, lock.hasWaiters(c))
    assertEquals(threads.length, lock.getWaitQueueLength(c))
    assertEquals(threads.length == 0, lock.getWaitingThreads(c).isEmpty)
    assertEquals(threads.length, lock.getWaitingThreads(c).size)
    assertEquals(
      new util.HashSet[Thread](lock.getWaitingThreads(c)),
      new util.HashSet[Thread](util.Arrays.asList(threads: _*))
    )
    lock.writeLock.unlock()
  }

  /** Awaits condition "indefinitely" using the specified AwaitMethod.
   */
  @throws[InterruptedException]
  def await(
      c: Condition,
      awaitMethod: ReentrantReadWriteLockTest.AwaitMethod
  ): Unit = {
    val timeoutMillis = 2 * LONG_DELAY_MS
    import ReentrantReadWriteLockTest.AwaitMethod._
    awaitMethod match {
      case ReentrantReadWriteLockTest.AwaitMethod.`await` =>
        c.await()

      case `awaitTimed` =>
        assertTrue(c.await(timeoutMillis, MILLISECONDS))

      case `awaitNanos` =>
        val timeoutNanos = MILLISECONDS.toNanos(timeoutMillis)
        val nanosRemaining = c.awaitNanos(timeoutNanos)
        assertTrue(nanosRemaining > timeoutNanos / 2)
        assertTrue(nanosRemaining <= timeoutNanos)

      case `awaitUntil` =>
        assertTrue(c.awaitUntil(delayedDate(timeoutMillis)))
    }
  }

  /** Constructor sets given fairness, and is in unlocked state
   */
  @Test def testConstructor(): Unit = {
    var lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock
    assertFalse(lock.isFair)
    assertNotWriteLocked(lock)
    assertEquals(0, lock.getReadLockCount)

    lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(true)
    assertTrue(lock.isFair)
    assertNotWriteLocked(lock)
    assertEquals(0, lock.getReadLockCount)

    lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(false)
    assertFalse(lock.isFair)
    assertNotWriteLocked(lock)
    assertEquals(0, lock.getReadLockCount)
  }

  /** write-locking and read-locking an unlocked lock succeed
   */
  @Test def testLock(): Unit = { testLock(false) }
  @Test def testLock_fair(): Unit = { testLock(true) }
  def testLock(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    assertNotWriteLocked(lock)
    lock.writeLock.lock()
    assertWriteLockedByMoi(lock)
    lock.writeLock.unlock()
    assertNotWriteLocked(lock)
    assertEquals(0, lock.getReadLockCount)
    lock.readLock.lock()
    assertNotWriteLocked(lock)
    assertEquals(1, lock.getReadLockCount)
    lock.readLock.unlock()
    assertNotWriteLocked(lock)
    assertEquals(0, lock.getReadLockCount)
  }

  /** getWriteHoldCount returns number of recursive holds
   */
  @Test def testGetWriteHoldCount(): Unit = { testGetWriteHoldCount(false) }
  @Test def testGetWriteHoldCount_fair(): Unit = { testGetWriteHoldCount(true) }
  def testGetWriteHoldCount(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    for (i <- 1 to SIZE) {
      lock.writeLock.lock()
      assertEquals(i, lock.getWriteHoldCount)
    }
    for (i <- SIZE until 0 by -1) {
      lock.writeLock.unlock()
      assertEquals(i - 1, lock.getWriteHoldCount)
    }
  }

  /** writelock.getHoldCount returns number of recursive holds
   */
  @Test def testGetHoldCount(): Unit = { testGetHoldCount(false) }
  @Test def testGetHoldCount_fair(): Unit = { testGetHoldCount(true) }
  def testGetHoldCount(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    for (i <- 1 to SIZE) {
      lock.writeLock.lock()
      assertEquals(i, lock.writeLock.getHoldCount)
    }
    for (i <- SIZE until 0 by -1) {
      lock.writeLock.unlock()
      assertEquals(i - 1, lock.writeLock.getHoldCount)
    }
  }

  /** getReadHoldCount returns number of recursive holds
   */
  @Test def testGetReadHoldCount(): Unit = { testGetReadHoldCount(false) }
  @Test def testGetReadHoldCount_fair(): Unit = { testGetReadHoldCount(true) }
  def testGetReadHoldCount(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    for (i <- 1 to SIZE) {
      lock.readLock.lock()
      assertEquals(i, lock.getReadHoldCount)
    }
    for (i <- SIZE until 0 by -1) {
      lock.readLock.unlock()
      assertEquals(i - 1, lock.getReadHoldCount)
    }
  }

  /** write-unlocking an unlocked lock throws IllegalMonitorStateException
   */
  @Test def testWriteUnlock_IMSE(): Unit = { testWriteUnlock_IMSE(false) }
  @Test def testWriteUnlock_IMSE_fair(): Unit = { testWriteUnlock_IMSE(true) }
  def testWriteUnlock_IMSE(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    try {
      lock.writeLock.unlock()
      shouldThrow()
    } catch {
      case success: IllegalMonitorStateException =>

    }
  }

  /** read-unlocking an unlocked lock throws IllegalMonitorStateException
   */
  @Test def testReadUnlock_IMSE(): Unit = { testReadUnlock_IMSE(false) }
  @Test def testReadUnlock_IMSE_fair(): Unit = { testReadUnlock_IMSE(true) }
  def testReadUnlock_IMSE(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    try {
      lock.readLock.unlock()
      shouldThrow()
    } catch {
      case success: IllegalMonitorStateException =>

    }
  }

  /** write-lockInterruptibly is interruptible
   */
  @Test def testWriteLockInterruptibly_Interruptible(): Unit = {
    testWriteLockInterruptibly_Interruptible(false)
  }
  @Test def testWriteLockInterruptibly_Interruptible_fair(): Unit = {
    testWriteLockInterruptibly_Interruptible(true)
  }
  def testWriteLockInterruptibly_Interruptible(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    lock.writeLock.lock()
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = { lock.writeLock.lockInterruptibly() }
    })
    waitForQueuedThread(lock, t)
    t.interrupt()
    awaitTermination(t)
    releaseWriteLock(lock)
  }

  /** timed write-tryLock is interruptible
   */
  @Test def testWriteTryLock_Interruptible(): Unit = {
    testWriteTryLock_Interruptible(false)
  }
  @Test def testWriteTryLock_Interruptible_fair(): Unit = {
    testWriteTryLock_Interruptible(true)
  }
  def testWriteTryLock_Interruptible(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    lock.writeLock.lock()
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.writeLock.tryLock(2 * LONG_DELAY_MS, MILLISECONDS)
      }
    })
    waitForQueuedThread(lock, t)
    t.interrupt()
    awaitTermination(t)
    releaseWriteLock(lock)
  }

  /** read-lockInterruptibly is interruptible
   */
  @Test def testReadLockInterruptibly_Interruptible(): Unit = {
    testReadLockInterruptibly_Interruptible(false)
  }
  @Test def testReadLockInterruptibly_Interruptible_fair(): Unit = {
    testReadLockInterruptibly_Interruptible(true)
  }
  def testReadLockInterruptibly_Interruptible(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    lock.writeLock.lock()
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = { lock.readLock.lockInterruptibly() }
    })
    waitForQueuedThread(lock, t)
    t.interrupt()
    awaitTermination(t)
    releaseWriteLock(lock)
  }

  /** timed read-tryLock is interruptible
   */
  @Test def testReadTryLock_Interruptible(): Unit = {
    testReadTryLock_Interruptible(false)
  }
  @Test def testReadTryLock_Interruptible_fair(): Unit = {
    testReadTryLock_Interruptible(true)
  }
  def testReadTryLock_Interruptible(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    lock.writeLock.lock()
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.readLock.tryLock(2 * LONG_DELAY_MS, MILLISECONDS)
      }
    })
    waitForQueuedThread(lock, t)
    t.interrupt()
    awaitTermination(t)
    releaseWriteLock(lock)
  }

  /** write-tryLock on an unlocked lock succeeds
   */
  @Test def testWriteTryLock(): Unit = { testWriteTryLock(false) }
  @Test def testWriteTryLock_fair(): Unit = { testWriteTryLock(true) }
  def testWriteTryLock(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    assertTrue(lock.writeLock.tryLock)
    assertWriteLockedByMoi(lock)
    assertTrue(lock.writeLock.tryLock)
    assertWriteLockedByMoi(lock)
    lock.writeLock.unlock()
    releaseWriteLock(lock)
  }

  /** write-tryLock fails if locked
   */
  @Test def testWriteTryLockWhenLocked(): Unit = {
    testWriteTryLockWhenLocked(false)
  }
  @Test def testWriteTryLockWhenLocked_fair(): Unit = {
    testWriteTryLockWhenLocked(true)
  }
  def testWriteTryLockWhenLocked(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    lock.writeLock.lock()
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = { assertFalse(lock.writeLock.tryLock) }
    })
    awaitTermination(t)
    releaseWriteLock(lock)
  }

  /** read-tryLock fails if locked
   */
  @Test def testReadTryLockWhenLocked(): Unit = {
    testReadTryLockWhenLocked(false)
  }
  @Test def testReadTryLockWhenLocked_fair(): Unit = {
    testReadTryLockWhenLocked(true)
  }
  def testReadTryLockWhenLocked(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    lock.writeLock.lock()
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = { assertFalse(lock.readLock.tryLock) }
    })
    awaitTermination(t)
    releaseWriteLock(lock)
  }

  /** Multiple threads can hold a read lock when not write-locked
   */
  @Test def testMultipleReadLocks(): Unit = { testMultipleReadLocks(false) }
  @Test def testMultipleReadLocks_fair(): Unit = { testMultipleReadLocks(true) }
  def testMultipleReadLocks(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    lock.readLock.lock()
    val t = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        assertTrue(lock.readLock.tryLock)
        lock.readLock.unlock()
        assertTrue(lock.readLock.tryLock(LONG_DELAY_MS, MILLISECONDS))
        lock.readLock.unlock()
        lock.readLock.lock()
        lock.readLock.unlock()
      }
    })
    awaitTermination(t)
    lock.readLock.unlock()
  }

  /** A writelock succeeds only after a reading thread unlocks
   */
  @Test def testWriteAfterReadLock(): Unit = { testWriteAfterReadLock(false) }
  @Test def testWriteAfterReadLock_fair(): Unit = {
    testWriteAfterReadLock(true)
  }
  def testWriteAfterReadLock(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    lock.readLock.lock()
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        assertEquals(1, lock.getReadLockCount)
        lock.writeLock.lock()
        assertEquals(0, lock.getReadLockCount)
        lock.writeLock.unlock()
      }
    })
    waitForQueuedThread(lock, t)
    assertNotWriteLocked(lock)
    assertEquals(1, lock.getReadLockCount)
    lock.readLock.unlock()
    assertEquals(0, lock.getReadLockCount)
    awaitTermination(t)
    assertNotWriteLocked(lock)
  }

  /** A writelock succeeds only after reading threads unlock
   */
  @Test def testWriteAfterMultipleReadLocks(): Unit = {
    testWriteAfterMultipleReadLocks(false)
  }
  @Test def testWriteAfterMultipleReadLocks_fair(): Unit = {
    testWriteAfterMultipleReadLocks(true)
  }
  def testWriteAfterMultipleReadLocks(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    lock.readLock.lock()
    lock.readLock.lock()
    val t1 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        lock.readLock.lock()
        assertEquals(3, lock.getReadLockCount)
        lock.readLock.unlock()
      }
    })
    awaitTermination(t1)
    val t2 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        assertEquals(2, lock.getReadLockCount)
        lock.writeLock.lock()
        assertEquals(0, lock.getReadLockCount)
        lock.writeLock.unlock()
      }
    })
    waitForQueuedThread(lock, t2)
    assertNotWriteLocked(lock)
    assertEquals(2, lock.getReadLockCount)
    lock.readLock.unlock()
    lock.readLock.unlock()
    assertEquals(0, lock.getReadLockCount)
    awaitTermination(t2)
    assertNotWriteLocked(lock)
  }

  /** A thread that tries to acquire a fair read lock (non-reentrantly) will
   *  block if there is a waiting writer thread
   */
  @Test def testReaderWriterReaderFairFifo(): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(true)
    val t1GotLock = new AtomicBoolean(false)
    lock.readLock.lock()
    val t1 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        assertEquals(1, lock.getReadLockCount)
        lock.writeLock.lock()
        assertEquals(0, lock.getReadLockCount)
        t1GotLock.set(true)
        lock.writeLock.unlock()
      }
    })
    waitForQueuedThread(lock, t1)
    val t2 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        assertEquals(1, lock.getReadLockCount)
        lock.readLock.lock()
        assertEquals(1, lock.getReadLockCount)
        assertTrue(t1GotLock.get)
        lock.readLock.unlock()
      }
    })
    waitForQueuedThread(lock, t2)
    assertTrue(t1.isAlive)
    assertNotWriteLocked(lock)
    assertEquals(1, lock.getReadLockCount)
    lock.readLock.unlock()
    awaitTermination(t1)
    awaitTermination(t2)
    assertNotWriteLocked(lock)
  }

  /** Readlocks succeed only after a writing thread unlocks
   */
  @Test def testReadAfterWriteLock(): Unit = { testReadAfterWriteLock(false) }
  @Test def testReadAfterWriteLock_fair(): Unit = {
    testReadAfterWriteLock(true)
  }
  def testReadAfterWriteLock(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    lock.writeLock.lock()
    val t1 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        lock.readLock.lock()
        lock.readLock.unlock()
      }
    })
    val t2 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        lock.readLock.lock()
        lock.readLock.unlock()
      }
    })
    waitForQueuedThread(lock, t1)
    waitForQueuedThread(lock, t2)
    releaseWriteLock(lock)
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** Read trylock succeeds if write locked by current thread
   */
  @Test def testReadHoldingWriteLock(): Unit = {
    testReadHoldingWriteLock(false)
  }
  @Test def testReadHoldingWriteLock_fair(): Unit = {
    testReadHoldingWriteLock(true)
  }
  def testReadHoldingWriteLock(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    lock.writeLock.lock()
    assertTrue(lock.readLock.tryLock)
    lock.readLock.unlock()
    lock.writeLock.unlock()
  }

  /** Read trylock succeeds (barging) even in the presence of waiting readers
   *  and/or writers
   */
  @Test def testReadTryLockBarging(): Unit = { testReadTryLockBarging(false) }
  @Test def testReadTryLockBarging_fair(): Unit = {
    testReadTryLockBarging(true)
  }
  def testReadTryLockBarging(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    lock.readLock.lock()
    val t1 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        lock.writeLock.lock()
        lock.writeLock.unlock()
      }
    })
    waitForQueuedThread(lock, t1)
    val t2 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        lock.readLock.lock()
        lock.readLock.unlock()
      }
    })
    if (fair) waitForQueuedThread(lock, t2)
    val t3 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        lock.readLock.tryLock
        lock.readLock.unlock()
      }
    })
    assertTrue(lock.getReadLockCount > 0)
    awaitTermination(t3)
    assertTrue(t1.isAlive)
    if (fair) assertTrue(t2.isAlive)
    lock.readLock.unlock()
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** Read lock succeeds if write locked by current thread even if other threads
   *  are waiting for readlock
   */
  @Test def testReadHoldingWriteLock2(): Unit = {
    testReadHoldingWriteLock2(false)
  }
  @Test def testReadHoldingWriteLock2_fair(): Unit = {
    testReadHoldingWriteLock2(true)
  }
  def testReadHoldingWriteLock2(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    lock.writeLock.lock()
    lock.readLock.lock()
    lock.readLock.unlock()
    val t1 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        lock.readLock.lock()
        lock.readLock.unlock()
      }
    })
    val t2 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        lock.readLock.lock()
        lock.readLock.unlock()
      }
    })
    waitForQueuedThread(lock, t1)
    waitForQueuedThread(lock, t2)
    assertWriteLockedByMoi(lock)
    lock.readLock.lock()
    lock.readLock.unlock()
    releaseWriteLock(lock)
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** Read lock succeeds if write locked by current thread even if other threads
   *  are waiting for writelock
   */
  @Test def testReadHoldingWriteLock3(): Unit = {
    testReadHoldingWriteLock3(false)
  }
  @Test def testReadHoldingWriteLock3_fair(): Unit = {
    testReadHoldingWriteLock3(true)
  }
  def testReadHoldingWriteLock3(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    lock.writeLock.lock()
    lock.readLock.lock()
    lock.readLock.unlock()
    val t1 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        lock.writeLock.lock()
        lock.writeLock.unlock()
      }
    })
    val t2 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        lock.writeLock.lock()
        lock.writeLock.unlock()
      }
    })
    waitForQueuedThread(lock, t1)
    waitForQueuedThread(lock, t2)
    assertWriteLockedByMoi(lock)
    lock.readLock.lock()
    lock.readLock.unlock()
    assertWriteLockedByMoi(lock)
    lock.writeLock.unlock()
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** Write lock succeeds if write locked by current thread even if other
   *  threads are waiting for writelock
   */
  @Test def testWriteHoldingWriteLock4(): Unit = {
    testWriteHoldingWriteLock4(false)
  }
  @Test def testWriteHoldingWriteLock4_fair(): Unit = {
    testWriteHoldingWriteLock4(true)
  }
  def testWriteHoldingWriteLock4(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    lock.writeLock.lock()
    lock.writeLock.lock()
    lock.writeLock.unlock()
    val t1 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        lock.writeLock.lock()
        lock.writeLock.unlock()
      }
    })
    val t2 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        lock.writeLock.lock()
        lock.writeLock.unlock()
      }
    })
    waitForQueuedThread(lock, t1)
    waitForQueuedThread(lock, t2)
    assertWriteLockedByMoi(lock)
    assertEquals(1, lock.getWriteHoldCount)
    lock.writeLock.lock()
    assertWriteLockedByMoi(lock)
    assertEquals(2, lock.getWriteHoldCount)
    lock.writeLock.unlock()
    assertWriteLockedByMoi(lock)
    assertEquals(1, lock.getWriteHoldCount)
    lock.writeLock.unlock()
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** Read tryLock succeeds if readlocked but not writelocked
   */
  @Test def testTryLockWhenReadLocked(): Unit = {
    testTryLockWhenReadLocked(false)
  }
  @Test def testTryLockWhenReadLocked_fair(): Unit = {
    testTryLockWhenReadLocked(true)
  }
  def testTryLockWhenReadLocked(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    lock.readLock.lock()
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        assertTrue(lock.readLock.tryLock)
        lock.readLock.unlock()
      }
    })
    awaitTermination(t)
    lock.readLock.unlock()
  }

  /** write tryLock fails when readlocked
   */
  @Test def testWriteTryLockWhenReadLocked(): Unit = {
    testWriteTryLockWhenReadLocked(false)
  }
  @Test def testWriteTryLockWhenReadLocked_fair(): Unit = {
    testWriteTryLockWhenReadLocked(true)
  }
  def testWriteTryLockWhenReadLocked(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    lock.readLock.lock()
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = { assertFalse(lock.writeLock.tryLock) }
    })
    awaitTermination(t)
    lock.readLock.unlock()
  }

  /** write timed tryLock times out if locked
   */
  @Test def testWriteTryLock_Timeout(): Unit = {
    testWriteTryLock_Timeout(false)
  }
  @Test def testWriteTryLock_Timeout_fair(): Unit = {
    testWriteTryLock_Timeout(true)
  }
  def testWriteTryLock_Timeout(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    val timeoutMillis = JSR166Test.timeoutMillis()
    lock.writeLock.lock()
    val t = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        val startTime = System.nanoTime
        assertFalse(lock.writeLock.tryLock(timeoutMillis, MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis)
      }
    })
    awaitTermination(t)
    releaseWriteLock(lock)
  }

  /** read timed tryLock times out if write-locked
   */
  @Test def testReadTryLock_Timeout(): Unit = { testReadTryLock_Timeout(false) }
  @Test def testReadTryLock_Timeout_fair(): Unit = {
    testReadTryLock_Timeout(true)
  }
  def testReadTryLock_Timeout(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    lock.writeLock.lock()
    val t = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        val startTime = System.nanoTime
        val timeoutMillis = JSR166Test.timeoutMillis()
        assertFalse(lock.readLock.tryLock(timeoutMillis, MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis)
      }
    })
    awaitTermination(t)
    assertTrue(lock.writeLock.isHeldByCurrentThread)
    lock.writeLock.unlock()
  }

  /** write lockInterruptibly succeeds if unlocked, else is interruptible
   */
  @Test def testWriteLockInterruptibly(): Unit = {
    testWriteLockInterruptibly(false)
  }
  @Test def testWriteLockInterruptibly_fair(): Unit = {
    testWriteLockInterruptibly(true)
  }
  def testWriteLockInterruptibly(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    try lock.writeLock.lockInterruptibly()
    catch {
      case fail: InterruptedException =>
        threadUnexpectedException(fail)
    }
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = { lock.writeLock.lockInterruptibly() }
    })
    waitForQueuedThread(lock, t)
    t.interrupt()
    assertTrue(lock.writeLock.isHeldByCurrentThread)
    awaitTermination(t)
    releaseWriteLock(lock)
  }

  /** read lockInterruptibly succeeds if lock free else is interruptible
   */
  @Test def testReadLockInterruptibly(): Unit = {
    testReadLockInterruptibly(false)
  }
  @Test def testReadLockInterruptibly_fair(): Unit = {
    testReadLockInterruptibly(true)
  }
  def testReadLockInterruptibly(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    try {
      lock.readLock.lockInterruptibly()
      lock.readLock.unlock()
      lock.writeLock.lockInterruptibly()
    } catch {
      case fail: InterruptedException =>
        threadUnexpectedException(fail)
    }
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = { lock.readLock.lockInterruptibly() }
    })
    waitForQueuedThread(lock, t)
    t.interrupt()
    awaitTermination(t)
    releaseWriteLock(lock)
  }

  /** Calling await without holding lock throws IllegalMonitorStateException
   */
  @Test def testAwait_IMSE(): Unit = { testAwait_IMSE(false) }
  @Test def testAwait_IMSE_fair(): Unit = { testAwait_IMSE(true) }
  def testAwait_IMSE(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    for (awaitMethod <- ReentrantReadWriteLockTest.AwaitMethod.values()) {
      val startTime = System.nanoTime
      try {
        await(c, awaitMethod)
        shouldThrow()
      } catch {
        case success: IllegalMonitorStateException =>

        case fail: InterruptedException =>
          threadUnexpectedException(fail)
      }
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    }
  }

  /** Calling signal without holding lock throws IllegalMonitorStateException
   */
  @Test def testSignal_IMSE(): Unit = { testSignal_IMSE(false) }
  @Test def testSignal_IMSE_fair(): Unit = { testSignal_IMSE(true) }
  def testSignal_IMSE(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    try {
      c.signal()
      shouldThrow()
    } catch {
      case success: IllegalMonitorStateException =>

    }
  }

  /** Calling signalAll without holding lock throws IllegalMonitorStateException
   */
  @Test def testSignalAll_IMSE(): Unit = { testSignalAll_IMSE(false) }
  @Test def testSignalAll_IMSE_fair(): Unit = { testSignalAll_IMSE(true) }
  def testSignalAll_IMSE(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    try {
      c.signalAll()
      shouldThrow()
    } catch {
      case success: IllegalMonitorStateException =>

    }
  }

  /** awaitNanos without a signal times out
   */
  @Test def testAwaitNanos_Timeout(): Unit = { testAwaitNanos_Timeout(false) }
  @Test def testAwaitNanos_Timeout_fair(): Unit = {
    testAwaitNanos_Timeout(true)
  }
  def testAwaitNanos_Timeout(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    val timeoutMillis = JSR166Test.timeoutMillis()
    lock.writeLock.lock()
    val startTime = System.nanoTime
    val timeoutNanos = MILLISECONDS.toNanos(timeoutMillis)
    try {
      val nanosRemaining = c.awaitNanos(timeoutNanos)
      assertTrue(nanosRemaining <= 0)
    } catch {
      case fail: InterruptedException =>
        threadUnexpectedException(fail)
    }
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis)
    lock.writeLock.unlock()
  }

  /** timed await without a signal times out
   */
  @Test def testAwait_Timeout(): Unit = { testAwait_Timeout(false) }
  @Test def testAwait_Timeout_fair(): Unit = { testAwait_Timeout(true) }
  def testAwait_Timeout(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    val timeoutMillis = JSR166Test.timeoutMillis()
    lock.writeLock.lock()
    val startTime = System.nanoTime
    try assertFalse(c.await(timeoutMillis, MILLISECONDS))
    catch {
      case fail: InterruptedException =>
        threadUnexpectedException(fail)
    }
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis)
    lock.writeLock.unlock()
  }

  /** awaitUntil without a signal times out
   */
  @Test def testAwaitUntil_Timeout(): Unit = { testAwaitUntil_Timeout(false) }
  @Test def testAwaitUntil_Timeout_fair(): Unit = {
    testAwaitUntil_Timeout(true)
  }
  def testAwaitUntil_Timeout(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    lock.writeLock.lock()
    // We shouldn't assume that nanoTime and currentTimeMillis
    // use the same time source, so don't use nanoTime here.
    val delayedDate = this.delayedDate(timeoutMillis())
    try assertFalse(c.awaitUntil(delayedDate))
    catch {
      case fail: InterruptedException =>
        threadUnexpectedException(fail)
    }
    assertTrue(new Date().getTime >= delayedDate.getTime)
    lock.writeLock.unlock()
  }

  /** await returns when signalled
   */
  @Test def testAwait(): Unit = { testAwait(false) }
  @Test def testAwait_fair(): Unit = { testAwait(true) }
  def testAwait(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    val locked = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.writeLock.lock()
        locked.countDown()
        c.await()
        lock.writeLock.unlock()
      }
    })
    await(locked)
    lock.writeLock.lock()
    assertHasWaiters(lock, c, t)
    c.signal()
    assertHasNoWaiters(lock, c)
    assertTrue(t.isAlive)
    lock.writeLock.unlock()
    awaitTermination(t)
  }

  /** awaitUninterruptibly is uninterruptible
   */
  @Test def testAwaitUninterruptibly(): Unit = {
    testAwaitUninterruptibly(false)
  }
  @Test def testAwaitUninterruptibly_fair(): Unit = {
    testAwaitUninterruptibly(true)
  }
  def testAwaitUninterruptibly(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair).writeLock
    val condition = lock.newCondition
    val pleaseInterrupt = new CountDownLatch(2)
    val t1 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = { // Interrupt before awaitUninterruptibly
        lock.lock()
        pleaseInterrupt.countDown()
        Thread.currentThread.interrupt()
        condition.awaitUninterruptibly()
        assertTrue(Thread.interrupted)
        lock.unlock()
      }
    })
    val t2 = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = { // Interrupt during awaitUninterruptibly
        lock.lock()
        pleaseInterrupt.countDown()
        condition.awaitUninterruptibly()
        assertTrue(Thread.interrupted)
        lock.unlock()
      }
    })
    await(pleaseInterrupt)
    t2.interrupt()
    lock.lock()
    lock.unlock()
    assertThreadBlocks(t1, Thread.State.WAITING)
    assertThreadBlocks(t2, Thread.State.WAITING)
    lock.lock()
    condition.signalAll()
    lock.unlock()
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** await/awaitNanos/awaitUntil is interruptible
   */
  @Test def testInterruptible_await(): Unit = {
    testInterruptible(false, ReentrantReadWriteLockTest.AwaitMethod.await)
  }
  @Test def testInterruptible_await_fair(): Unit = {
    testInterruptible(true, ReentrantReadWriteLockTest.AwaitMethod.await)
  }
  @Test def testInterruptible_awaitTimed(): Unit = {
    testInterruptible(false, ReentrantReadWriteLockTest.AwaitMethod.awaitTimed)
  }
  @Test def testInterruptible_awaitTimed_fair(): Unit = {
    testInterruptible(true, ReentrantReadWriteLockTest.AwaitMethod.awaitTimed)
  }
  @Test def testInterruptible_awaitNanos(): Unit = {
    testInterruptible(false, ReentrantReadWriteLockTest.AwaitMethod.awaitNanos)
  }
  @Test def testInterruptible_awaitNanos_fair(): Unit = {
    testInterruptible(true, ReentrantReadWriteLockTest.AwaitMethod.awaitNanos)
  }
  @Test def testInterruptible_awaitUntil(): Unit = {
    testInterruptible(false, ReentrantReadWriteLockTest.AwaitMethod.awaitUntil)
  }
  @Test def testInterruptible_awaitUntil_fair(): Unit = {
    testInterruptible(true, ReentrantReadWriteLockTest.AwaitMethod.awaitUntil)
  }
  def testInterruptible(
      fair: Boolean,
      awaitMethod: ReentrantReadWriteLockTest.AwaitMethod
  ): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    val locked = new CountDownLatch(1)
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.writeLock.lock()
        assertWriteLockedByMoi(lock)
        assertHasNoWaiters(lock, c)
        locked.countDown()
        try await(c, awaitMethod)
        finally {
          assertWriteLockedByMoi(lock)
          assertHasNoWaiters(lock, c)
          lock.writeLock.unlock()
          assertFalse(Thread.interrupted)
        }
      }
    })
    await(locked)
    assertHasWaiters(lock, c, t)
    t.interrupt()
    awaitTermination(t)
    assertNotWriteLocked(lock)
  }

  /** signalAll wakes up all threads
   */
  @Test def testSignalAll_await(): Unit = {
    testSignalAll(false, ReentrantReadWriteLockTest.AwaitMethod.await)
  }
  @Test def testSignalAll_await_fair(): Unit = {
    testSignalAll(true, ReentrantReadWriteLockTest.AwaitMethod.await)
  }
  @Test def testSignalAll_awaitTimed(): Unit = {
    testSignalAll(false, ReentrantReadWriteLockTest.AwaitMethod.awaitTimed)
  }
  @Test def testSignalAll_awaitTimed_fair(): Unit = {
    testSignalAll(true, ReentrantReadWriteLockTest.AwaitMethod.awaitTimed)
  }
  @Test def testSignalAll_awaitNanos(): Unit = {
    testSignalAll(false, ReentrantReadWriteLockTest.AwaitMethod.awaitNanos)
  }
  @Test def testSignalAll_awaitNanos_fair(): Unit = {
    testSignalAll(true, ReentrantReadWriteLockTest.AwaitMethod.awaitNanos)
  }
  @Test def testSignalAll_awaitUntil(): Unit = {
    testSignalAll(false, ReentrantReadWriteLockTest.AwaitMethod.awaitUntil)
  }
  @Test def testSignalAll_awaitUntil_fair(): Unit = {
    testSignalAll(true, ReentrantReadWriteLockTest.AwaitMethod.awaitUntil)
  }
  def testSignalAll(
      fair: Boolean,
      awaitMethod: ReentrantReadWriteLockTest.AwaitMethod
  ): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    val locked = new CountDownLatch(2)
    val writeLock = lock.writeLock
    class Awaiter extends CheckedRunnable {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        writeLock.lock()
        locked.countDown()
        await(c, awaitMethod)
        writeLock.unlock()
      }
    }
    val t1 = newStartedThread(new Awaiter)
    val t2 = newStartedThread(new Awaiter)
    await(locked)
    writeLock.lock()
    assertHasWaiters(lock, c, t1, t2)
    c.signalAll()
    assertHasNoWaiters(lock, c)
    writeLock.unlock()
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** signal wakes up waiting threads in FIFO order
   */
  @Test def testSignalWakesFifo(): Unit = { testSignalWakesFifo(false) }
  @Test def testSignalWakesFifo_fair(): Unit = { testSignalWakesFifo(true) }
  def testSignalWakesFifo(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    val locked1 = new CountDownLatch(1)
    val locked2 = new CountDownLatch(1)
    val writeLock = lock.writeLock
    val t1 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        writeLock.lock()
        locked1.countDown()
        c.await()
        writeLock.unlock()
      }
    })
    await(locked1)
    val t2 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        writeLock.lock()
        locked2.countDown()
        c.await()
        writeLock.unlock()
      }
    })
    await(locked2)
    writeLock.lock()
    assertHasWaiters(lock, c, t1, t2)
    assertFalse(lock.hasQueuedThreads)
    c.signal()
    assertHasWaiters(lock, c, t2)
    assertTrue(lock.hasQueuedThread(t1))
    assertFalse(lock.hasQueuedThread(t2))
    c.signal()
    assertHasNoWaiters(lock, c)
    assertTrue(lock.hasQueuedThread(t1))
    assertTrue(lock.hasQueuedThread(t2))
    writeLock.unlock()
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** await after multiple reentrant locking preserves lock count
   */
  @Test def testAwaitLockCount(): Unit = { testAwaitLockCount(false) }
  @Test def testAwaitLockCount_fair(): Unit = { testAwaitLockCount(true) }
  def testAwaitLockCount(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    val locked = new CountDownLatch(2)
    val t1 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.writeLock.lock()
        assertWriteLockedByMoi(lock)
        assertEquals(1, lock.writeLock.getHoldCount)
        locked.countDown()
        c.await()
        assertWriteLockedByMoi(lock)
        assertEquals(1, lock.writeLock.getHoldCount)
        lock.writeLock.unlock()
      }
    })
    val t2 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.writeLock.lock()
        lock.writeLock.lock()
        assertWriteLockedByMoi(lock)
        assertEquals(2, lock.writeLock.getHoldCount)
        locked.countDown()
        c.await()
        assertWriteLockedByMoi(lock)
        assertEquals(2, lock.writeLock.getHoldCount)
        lock.writeLock.unlock()
        lock.writeLock.unlock()
      }
    })
    await(locked)
    lock.writeLock.lock()
    assertHasWaiters(lock, c, t1, t2)
    c.signalAll()
    assertHasNoWaiters(lock, c)
    lock.writeLock.unlock()
    awaitTermination(t1)
    awaitTermination(t2)
  }

  // No object input stream in Scala NAtive
  // /** A serialized lock deserializes as unlocked
  //  */
  // @Test def testSerialization(): Unit = {}
  // @Test def testSerialization_fair(): Unit = {}

  /** hasQueuedThreads reports whether there are waiting threads
   */
  @Test def testHasQueuedThreads(): Unit = { testHasQueuedThreads(false) }
  @Test def testHasQueuedThreads_fair(): Unit = { testHasQueuedThreads(true) }
  def testHasQueuedThreads(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    val t1 = new Thread(
      new InterruptedLockRunnable(lock)
    )
    val t2 = new Thread(
      new InterruptibleLockRunnable(lock)
    )
    assertFalse(lock.hasQueuedThreads)
    lock.writeLock.lock()
    assertFalse(lock.hasQueuedThreads)
    t1.start()
    waitForQueuedThread(lock, t1)
    assertTrue(lock.hasQueuedThreads)
    t2.start()
    waitForQueuedThread(lock, t2)
    assertTrue(lock.hasQueuedThreads)
    t1.interrupt()
    awaitTermination(t1)
    assertTrue(lock.hasQueuedThreads)
    lock.writeLock.unlock()
    awaitTermination(t2)
    assertFalse(lock.hasQueuedThreads)
  }

  /** hasQueuedThread(null) throws NPE
   */
  @Test def testHasQueuedThreadNPE(): Unit = { testHasQueuedThreadNPE(false) }
  @Test def testHasQueuedThreadNPE_fair(): Unit = {
    testHasQueuedThreadNPE(true)
  }
  def testHasQueuedThreadNPE(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    try {
      lock.hasQueuedThread(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** hasQueuedThread reports whether a thread is queued
   */
  @Test def testHasQueuedThread(): Unit = { testHasQueuedThread(false) }
  @Test def testHasQueuedThread_fair(): Unit = { testHasQueuedThread(true) }
  def testHasQueuedThread(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    val t1 = new Thread(
      new InterruptedLockRunnable(lock)
    )
    val t2 = new Thread(
      new InterruptibleLockRunnable(lock)
    )
    assertFalse(lock.hasQueuedThread(t1))
    assertFalse(lock.hasQueuedThread(t2))
    lock.writeLock.lock()
    t1.start()
    waitForQueuedThread(lock, t1)
    assertTrue(lock.hasQueuedThread(t1))
    assertFalse(lock.hasQueuedThread(t2))
    t2.start()
    waitForQueuedThread(lock, t2)
    assertTrue(lock.hasQueuedThread(t1))
    assertTrue(lock.hasQueuedThread(t2))
    t1.interrupt()
    awaitTermination(t1)
    assertFalse(lock.hasQueuedThread(t1))
    assertTrue(lock.hasQueuedThread(t2))
    lock.writeLock.unlock()
    awaitTermination(t2)
    assertFalse(lock.hasQueuedThread(t1))
    assertFalse(lock.hasQueuedThread(t2))
  }

  /** getQueueLength reports number of waiting threads
   */
  @Test def testGetQueueLength(): Unit = { testGetQueueLength(false) }
  @Test def testGetQueueLength_fair(): Unit = { testGetQueueLength(true) }
  def testGetQueueLength(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    val t1 = new Thread(
      new InterruptedLockRunnable(lock)
    )
    val t2 = new Thread(
      new InterruptibleLockRunnable(lock)
    )
    assertEquals(0, lock.getQueueLength)
    lock.writeLock.lock()
    t1.start()
    waitForQueuedThread(lock, t1)
    assertEquals(1, lock.getQueueLength)
    t2.start()
    waitForQueuedThread(lock, t2)
    assertEquals(2, lock.getQueueLength)
    t1.interrupt()
    awaitTermination(t1)
    assertEquals(1, lock.getQueueLength)
    lock.writeLock.unlock()
    awaitTermination(t2)
    assertEquals(0, lock.getQueueLength)
  }

  /** getQueuedThreads() includes waiting threads
   */
  @Test def testGetQueuedThreads(): Unit = { testGetQueuedThreads(false) }
  @Test def testGetQueuedThreads_fair(): Unit = { testGetQueuedThreads(true) }
  def testGetQueuedThreads(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    val t1 = new Thread(
      new InterruptedLockRunnable(lock)
    )
    val t2 = new Thread(
      new InterruptibleLockRunnable(lock)
    )
    assertTrue(lock.getQueuedThreads().isEmpty)
    lock.writeLock.lock()
    assertTrue(lock.getQueuedThreads().isEmpty)
    t1.start()
    waitForQueuedThread(lock, t1)
    assertEquals(1, lock.getQueuedThreads().size)
    assertTrue(lock.getQueuedThreads().contains(t1))
    t2.start()
    waitForQueuedThread(lock, t2)
    assertEquals(2, lock.getQueuedThreads().size)
    assertTrue(lock.getQueuedThreads().contains(t1))
    assertTrue(lock.getQueuedThreads().contains(t2))
    t1.interrupt()
    awaitTermination(t1)
    assertFalse(lock.getQueuedThreads().contains(t1))
    assertTrue(lock.getQueuedThreads().contains(t2))
    assertEquals(1, lock.getQueuedThreads().size)
    lock.writeLock.unlock()
    awaitTermination(t2)
    assertTrue(lock.getQueuedThreads().isEmpty)
  }

  /** hasWaiters throws NPE if null
   */
  @Test def testHasWaitersNPE(): Unit = { testHasWaitersNPE(false) }
  @Test def testHasWaitersNPE_fair(): Unit = { testHasWaitersNPE(true) }
  def testHasWaitersNPE(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    try {
      lock.hasWaiters(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** getWaitQueueLength throws NPE if null
   */
  @Test def testGetWaitQueueLengthNPE(): Unit = {
    testGetWaitQueueLengthNPE(false)
  }
  @Test def testGetWaitQueueLengthNPE_fair(): Unit = {
    testGetWaitQueueLengthNPE(true)
  }
  def testGetWaitQueueLengthNPE(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    try {
      lock.getWaitQueueLength(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** getWaitingThreads throws NPE if null
   */
  @Test def testGetWaitingThreadsNPE(): Unit = {
    testGetWaitingThreadsNPE(false)
  }
  @Test def testGetWaitingThreadsNPE_fair(): Unit = {
    testGetWaitingThreadsNPE(true)
  }
  def testGetWaitingThreadsNPE(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    try {
      lock.getWaitingThreads(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** hasWaiters throws IllegalArgumentException if not owned
   */
  @Test def testHasWaitersIAE(): Unit = { testHasWaitersIAE(false) }
  @Test def testHasWaitersIAE_fair(): Unit = { testHasWaitersIAE(true) }
  def testHasWaitersIAE(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    val lock2 = new ReentrantReadWriteLock(fair)
    try {
      lock2.hasWaiters(c)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** hasWaiters throws IllegalMonitorStateException if not locked
   */
  @Test def testHasWaitersIMSE(): Unit = { testHasWaitersIMSE(false) }
  @Test def testHasWaitersIMSE_fair(): Unit = { testHasWaitersIMSE(true) }
  def testHasWaitersIMSE(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    try {
      lock.hasWaiters(c)
      shouldThrow()
    } catch {
      case success: IllegalMonitorStateException =>

    }
  }

  /** getWaitQueueLength throws IllegalArgumentException if not owned
   */
  @Test def testGetWaitQueueLengthIAE(): Unit = {
    testGetWaitQueueLengthIAE(false)
  }
  @Test def testGetWaitQueueLengthIAE_fair(): Unit = {
    testGetWaitQueueLengthIAE(true)
  }
  def testGetWaitQueueLengthIAE(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    val lock2 = new ReentrantReadWriteLock(fair)
    try {
      lock2.getWaitQueueLength(c)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** getWaitQueueLength throws IllegalMonitorStateException if not locked
   */
  @Test def testGetWaitQueueLengthIMSE(): Unit = {
    testGetWaitQueueLengthIMSE(false)
  }
  @Test def testGetWaitQueueLengthIMSE_fair(): Unit = {
    testGetWaitQueueLengthIMSE(true)
  }
  def testGetWaitQueueLengthIMSE(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    try {
      lock.getWaitQueueLength(c)
      shouldThrow()
    } catch {
      case success: IllegalMonitorStateException =>

    }
  }

  /** getWaitingThreads throws IllegalArgumentException if not owned
   */
  @Test def testGetWaitingThreadsIAE(): Unit = {
    testGetWaitingThreadsIAE(false)
  }
  @Test def testGetWaitingThreadsIAE_fair(): Unit = {
    testGetWaitingThreadsIAE(true)
  }
  def testGetWaitingThreadsIAE(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    val lock2 =
      new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    try {
      lock2.getWaitingThreads(c)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** getWaitingThreads throws IllegalMonitorStateException if not locked
   */
  @Test def testGetWaitingThreadsIMSE(): Unit = {
    testGetWaitingThreadsIMSE(false)
  }
  @Test def testGetWaitingThreadsIMSE_fair(): Unit = {
    testGetWaitingThreadsIMSE(true)
  }
  def testGetWaitingThreadsIMSE(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    try {
      lock.getWaitingThreads(c)
      shouldThrow()
    } catch {
      case success: IllegalMonitorStateException =>

    }
  }

  /** hasWaiters returns true when a thread is waiting, else false
   */
  @Test def testHasWaiters(): Unit = { testHasWaiters(false) }
  @Test def testHasWaiters_fair(): Unit = { testHasWaiters(true) }
  def testHasWaiters(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    val locked = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.writeLock.lock()
        assertHasNoWaiters(lock, c)
        assertFalse(lock.hasWaiters(c))
        locked.countDown()
        c.await()
        assertHasNoWaiters(lock, c)
        assertFalse(lock.hasWaiters(c))
        lock.writeLock.unlock()
      }
    })
    await(locked)
    lock.writeLock.lock()
    assertHasWaiters(lock, c, t)
    assertTrue(lock.hasWaiters(c))
    c.signal()
    assertHasNoWaiters(lock, c)
    assertFalse(lock.hasWaiters(c))
    lock.writeLock.unlock()
    awaitTermination(t)
    assertHasNoWaiters(lock, c)
  }

  /** getWaitQueueLength returns number of waiting threads
   */
  @Test def testGetWaitQueueLength(): Unit = { testGetWaitQueueLength(false) }
  @Test def testGetWaitQueueLength_fair(): Unit = {
    testGetWaitQueueLength(true)
  }
  def testGetWaitQueueLength(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    val locked = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.writeLock.lock()
        assertEquals(0, lock.getWaitQueueLength(c))
        locked.countDown()
        c.await()
        lock.writeLock.unlock()
      }
    })
    await(locked)
    lock.writeLock.lock()
    assertHasWaiters(lock, c, t)
    assertEquals(1, lock.getWaitQueueLength(c))
    c.signal()
    assertHasNoWaiters(lock, c)
    assertEquals(0, lock.getWaitQueueLength(c))
    lock.writeLock.unlock()
    awaitTermination(t)
  }

  /** getWaitingThreads returns only and all waiting threads
   */
  @Test def testGetWaitingThreads(): Unit = { testGetWaitingThreads(false) }
  @Test def testGetWaitingThreads_fair(): Unit = { testGetWaitingThreads(true) }
  def testGetWaitingThreads(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLockTest.PublicReentrantReadWriteLock(fair)
    val c = lock.writeLock.newCondition
    val locked1 = new CountDownLatch(1)
    val locked2 = new CountDownLatch(1)
    val t1 = new Thread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.writeLock.lock()
        assertTrue(lock.getWaitingThreads(c).isEmpty)
        locked1.countDown()
        c.await()
        lock.writeLock.unlock()
      }
    })
    val t2 = new Thread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.writeLock.lock()
        assertFalse(lock.getWaitingThreads(c).isEmpty)
        locked2.countDown()
        c.await()
        lock.writeLock.unlock()
      }
    })
    lock.writeLock.lock()
    assertTrue(lock.getWaitingThreads(c).isEmpty)
    lock.writeLock.unlock()
    t1.start()
    await(locked1)
    t2.start()
    await(locked2)
    lock.writeLock.lock()
    assertTrue(lock.hasWaiters(c))
    assertTrue(lock.getWaitingThreads(c).contains(t1))
    assertTrue(lock.getWaitingThreads(c).contains(t2))
    assertEquals(2, lock.getWaitingThreads(c).size)
    c.signalAll()
    assertHasNoWaiters(lock, c)
    lock.writeLock.unlock()
    awaitTermination(t1)
    awaitTermination(t2)
    assertHasNoWaiters(lock, c)
  }

  /** toString indicates current lock state
   */
  @Test def testToString(): Unit = { testToString(false) }
  @Test def testToString_fair(): Unit = { testToString(true) }
  def testToString(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    assertTrue(lock.toString.contains("Write locks = 0"))
    assertTrue(lock.toString.contains("Read locks = 0"))
    lock.writeLock.lock()
    assertTrue(lock.toString.contains("Write locks = 1"))
    assertTrue(lock.toString.contains("Read locks = 0"))
    lock.writeLock.lock()
    assertTrue(lock.toString.contains("Write locks = 2"))
    assertTrue(lock.toString.contains("Read locks = 0"))
    lock.writeLock.unlock()
    lock.writeLock.unlock()
    lock.readLock.lock()
    assertTrue(lock.toString.contains("Write locks = 0"))
    assertTrue(lock.toString.contains("Read locks = 1"))
    lock.readLock.lock()
    assertTrue(lock.toString.contains("Write locks = 0"))
    assertTrue(lock.toString.contains("Read locks = 2"))
  }

  /** readLock.toString indicates current lock state
   */
  @Test def testReadLockToString(): Unit = { testReadLockToString(false) }
  @Test def testReadLockToString_fair(): Unit = { testReadLockToString(true) }
  def testReadLockToString(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    assertTrue(lock.readLock.toString.contains("Read locks = 0"))
    lock.readLock.lock()
    assertTrue(lock.readLock.toString.contains("Read locks = 1"))
    lock.readLock.lock()
    assertTrue(lock.readLock.toString.contains("Read locks = 2"))
    lock.readLock.unlock()
    assertTrue(lock.readLock.toString.contains("Read locks = 1"))
    lock.readLock.unlock()
    assertTrue(lock.readLock.toString.contains("Read locks = 0"))
  }

  /** writeLock.toString indicates current lock state
   */
  @Test def testWriteLockToString(): Unit = { testWriteLockToString(false) }
  @Test def testWriteLockToString_fair(): Unit = { testWriteLockToString(true) }
  def testWriteLockToString(fair: Boolean): Unit = {
    val lock = new ReentrantReadWriteLock(fair)
    assertTrue(lock.writeLock.toString.contains("Unlocked"))
    lock.writeLock.lock()
    assertTrue(lock.writeLock.toString.contains("Locked by"))
    lock.writeLock.unlock()
    assertTrue(lock.writeLock.toString.contains("Unlocked"))
  }

  /* ThreadMXBean reports the blockers that we expect.*/
  // @Test def testBlockers(): Unit = ()
}
