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
import org.junit.Assume._
import org.junit.{Test, Ignore}
import org.scalanative.testsuite.javalib.util.concurrent.JSR166Test
import JSR166Test._
import ReentrantLockTest.AwaitMethod
import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

import java.util.concurrent.locks.{ReentrantLock, Condition}
import java.util.concurrent.{
  CountDownLatch,
  CyclicBarrier,
  ThreadLocalRandom,
  TimeUnit
}
import java.util.concurrent.TimeUnit._
import java.util.concurrent.atomic.AtomicBoolean
import java.util.Date
import java.util

object ReentrantLockTest {

  /** Subclass to expose protected methods
   */
  class PublicReentrantLock(fair: Boolean) extends ReentrantLock(fair) {
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
  def randomAwaitMethod() = {
    val awaitMethods = AwaitMethod.values()
    awaitMethods(ThreadLocalRandom.current.nextInt(awaitMethods.length))
  }
}

class ReentrantLockTest extends JSR166Test {

  /** A checked runnable calling lockInterruptibly
   */
  class InterruptibleLockRunnable(val lock: ReentrantLock)
      extends CheckedRunnable {
    @throws[InterruptedException]
    override def realRun(): Unit = { lock.lockInterruptibly() }
  }

  /** A checked runnable calling lockInterruptibly that expects to be
   *  interrupted
   */
  class InterruptedLockRunnable(val lock: ReentrantLock)
      extends CheckedInterruptedRunnable {
    @throws[InterruptedException]
    override def realRun(): Unit = { lock.lockInterruptibly() }
  }

  /** Releases write lock, checking that it had a hold count of 1.
   */
  def releaseLock(lock: ReentrantLockTest.PublicReentrantLock): Unit = {
    assertLockedByMoi(lock)
    lock.unlock()
    assertFalse(lock.isHeldByCurrentThread)
    assertNotLocked(lock)
  }

  /** Spin-waits until lock.hasQueuedThread(t) becomes true.
   */
  def waitForQueuedThread(
      lock: ReentrantLockTest.PublicReentrantLock,
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

  /** Checks that lock is not locked.
   */
  def assertNotLocked(lock: ReentrantLockTest.PublicReentrantLock): Unit = {
    assertFalse(lock.isLocked)
    assertFalse(lock.isHeldByCurrentThread)
    assertNull(lock.getOwner())
    assertEquals(0, lock.getHoldCount)
  }

  /** Checks that lock is locked by the given thread.
   */
  def assertLockedBy(
      lock: ReentrantLockTest.PublicReentrantLock,
      t: Thread
  ): Unit = {
    assertTrue(lock.isLocked)
    assertSame(t, lock.getOwner())
    assertEquals(t eq Thread.currentThread, lock.isHeldByCurrentThread)
    assertEquals(t eq Thread.currentThread, lock.getHoldCount > 0)
  }

  /** Checks that lock is locked by the current thread.
   */
  def assertLockedByMoi(lock: ReentrantLockTest.PublicReentrantLock): Unit = {
    assertLockedBy(lock, Thread.currentThread)
  }

  /** Checks that condition c has no waiters.
   */
  def assertHasNoWaiters(
      lock: ReentrantLockTest.PublicReentrantLock,
      c: Condition
  ): Unit = { assertHasWaiters(lock, c, Array.empty[Thread]: _*) }

  /** Checks that condition c has exactly the given waiter threads.
   */
  def assertHasWaiters(
      lock: ReentrantLockTest.PublicReentrantLock,
      c: Condition,
      threads: Thread*
  ): Unit = {
    lock.lock()
    assertEquals(threads.length > 0, lock.hasWaiters(c))
    assertEquals(threads.length, lock.getWaitQueueLength(c))
    assertEquals(threads.length == 0, lock.getWaitingThreads(c).isEmpty)
    assertEquals(threads.length, lock.getWaitingThreads(c).size)
    assertEquals(
      new util.HashSet[Thread](lock.getWaitingThreads(c)),
      new util.HashSet[Thread](util.Arrays.asList(threads: _*))
    )
    lock.unlock()
  }

  /** Awaits condition "indefinitely" using the specified AwaitMethod.
   */
  @throws[InterruptedException]
  def await(c: Condition, awaitMethod: ReentrantLockTest.AwaitMethod): Unit = {
    val timeoutMillis = 2 * LONG_DELAY_MS
    import AwaitMethod._
    awaitMethod match {
      case AwaitMethod.`await` =>
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
    var lock = new ReentrantLockTest.PublicReentrantLock()
    assertFalse(lock.isFair)
    assertNotLocked(lock)
    lock = new ReentrantLockTest.PublicReentrantLock(true)
    assertTrue(lock.isFair)
    assertNotLocked(lock)
    lock = new ReentrantLockTest.PublicReentrantLock(false)
    assertFalse(lock.isFair)
    assertNotLocked(lock)
  }

  /** locking an unlocked lock succeeds
   */
  @Test def testLock(): Unit = { testLock(false) }
  @Test def testLock_fair(): Unit = { testLock(true) }
  def testLock(fair: Boolean): Unit = {
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    lock.lock()
    assertLockedByMoi(lock)
    releaseLock(lock)
  }

  /** Unlocking an unlocked lock throws IllegalMonitorStateException
   */
  @Test def testUnlock_IMSE(): Unit = { testUnlock_IMSE(false) }
  @Test def testUnlock_IMSE_fair(): Unit = { testUnlock_IMSE(true) }
  def testUnlock_IMSE(fair: Boolean): Unit = {
    val lock = new ReentrantLock(fair)
    try {
      lock.unlock()
      shouldThrow()
    } catch {
      case success: IllegalMonitorStateException =>

    }
  }

  /** tryLock on an unlocked lock succeeds
   */
  @Test def testTryLock(): Unit = { testTryLock(false) }
  @Test def testTryLock_fair(): Unit = { testTryLock(true) }
  def testTryLock(fair: Boolean): Unit = {
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    assertTrue(lock.tryLock)
    assertLockedByMoi(lock)
    assertTrue(lock.tryLock)
    assertLockedByMoi(lock)
    lock.unlock()
    releaseLock(lock)
  }

  /** hasQueuedThreads reports whether there are waiting threads
   */
  @Test def testHasQueuedThreads(): Unit = { testHasQueuedThreads(false) }
  @Test def testHasQueuedThreads_fair(): Unit = { testHasQueuedThreads(true) }
  def testHasQueuedThreads(fair: Boolean): Unit = {
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    val t1 = new Thread(new InterruptedLockRunnable(lock))
    val t2 = new Thread(new InterruptibleLockRunnable(lock))
    assertFalse(lock.hasQueuedThreads)
    lock.lock()
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
    lock.unlock()
    awaitTermination(t2)
    assertFalse(lock.hasQueuedThreads)
  }

  /** getQueueLength reports number of waiting threads
   */
  @Test def testGetQueueLength(): Unit = { testGetQueueLength(false) }
  @Test def testGetQueueLength_fair(): Unit = { testGetQueueLength(true) }
  def testGetQueueLength(fair: Boolean): Unit = {
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    val t1 = new Thread(new InterruptedLockRunnable(lock))
    val t2 = new Thread(new InterruptibleLockRunnable(lock))
    assertEquals(0, lock.getQueueLength)
    lock.lock()
    t1.start()
    waitForQueuedThread(lock, t1)
    assertEquals(1, lock.getQueueLength)
    t2.start()
    waitForQueuedThread(lock, t2)
    assertEquals(2, lock.getQueueLength)
    t1.interrupt()
    awaitTermination(t1)
    assertEquals(1, lock.getQueueLength)
    lock.unlock()
    awaitTermination(t2)
    assertEquals(0, lock.getQueueLength)
  }

  /** hasQueuedThread(null) throws NPE
   */
  @Test def testHasQueuedThreadNPE(): Unit = { testHasQueuedThreadNPE(false) }
  @Test def testHasQueuedThreadNPE_fair(): Unit = {
    testHasQueuedThreadNPE(true)
  }
  def testHasQueuedThreadNPE(fair: Boolean): Unit = {
    val lock = new ReentrantLock(fair)
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
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    val t1 = new Thread(new InterruptedLockRunnable(lock))
    val t2 = new Thread(new InterruptibleLockRunnable(lock))
    assertFalse(lock.hasQueuedThread(t1))
    assertFalse(lock.hasQueuedThread(t2))
    lock.lock()
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
    lock.unlock()
    awaitTermination(t2)
    assertFalse(lock.hasQueuedThread(t1))
    assertFalse(lock.hasQueuedThread(t2))
  }

  /** getQueuedThreads() includes waiting threads
   */
  @Test def testGetQueuedThreads(): Unit = { testGetQueuedThreads(false) }
  @Test def testGetQueuedThreadfair(): Unit = { testGetQueuedThreads(true) }
  def testGetQueuedThreads(fair: Boolean): Unit = {
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    val t1 = new Thread(new InterruptedLockRunnable(lock))
    val t2 = new Thread(new InterruptibleLockRunnable(lock))
    assertTrue(lock.getQueuedThreads().isEmpty)
    lock.lock()
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
    lock.unlock()
    awaitTermination(t2)
    assertTrue(lock.getQueuedThreads().isEmpty)
  }

  /** timed tryLock is interruptible
   */
  @Test def testTryLock_Interruptible(): Unit = {
    testTryLock_Interruptible(false)
  }
  @Test def testTryLock_Interruptible_fair(): Unit = {
    testTryLock_Interruptible(true)
  }
  def testTryLock_Interruptible(fair: Boolean): Unit = {
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    lock.lock()
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.tryLock(2 * LONG_DELAY_MS, MILLISECONDS)
      }
    })
    waitForQueuedThread(lock, t)
    t.interrupt()
    awaitTermination(t)
    releaseLock(lock)
  }

  /** tryLock on a locked lock fails
   */
  @Test def testTryLockWhenLocked(): Unit = { testTryLockWhenLocked(false) }
  @Test def testTryLockWhenLocked_fair(): Unit = { testTryLockWhenLocked(true) }
  def testTryLockWhenLocked(fair: Boolean): Unit = {
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    lock.lock()
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = { assertFalse(lock.tryLock) }
    })
    awaitTermination(t)
    releaseLock(lock)
  }

  /** Timed tryLock on a locked lock times out
   */
  @Test def testTryLock_Timeout(): Unit = { testTryLock_Timeout(false) }
  @Test def testTryLock_Timeout_fair(): Unit = { testTryLock_Timeout(true) }
  def testTryLock_Timeout(fair: Boolean): Unit = {
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    val timeoutMillis = JSR166Test.timeoutMillis()
    lock.lock()
    val t = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        val startTime = System.nanoTime
        assertFalse(lock.tryLock(timeoutMillis, MILLISECONDS))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis)
      }
    })
    awaitTermination(t)
    releaseLock(lock)
  }

  /** getHoldCount returns number of recursive holds
   */
  @Test def testGetHoldCount(): Unit = { testGetHoldCount(false) }
  @Test def testGetHoldCount_fair(): Unit = { testGetHoldCount(true) }
  def testGetHoldCount(fair: Boolean): Unit = {
    val lock = new ReentrantLock(fair)
    for (i <- 1 to SIZE) {
      lock.lock()
      assertEquals(i, lock.getHoldCount)
    }
    for (i <- SIZE until 0 by -1) {
      lock.unlock()
      assertEquals(i - 1, lock.getHoldCount)
    }
  }

  /** isLocked is true when locked and false when not
   */
  @Test def testIsLocked(): Unit = { testIsLocked(false) }
  @Test def testIsLocked_fair(): Unit = { testIsLocked(true) }
  def testIsLocked(fair: Boolean): Unit = {
    val lock = new ReentrantLock(fair)
    try {
      assertFalse(lock.isLocked)
      lock.lock()
      assertTrue(lock.isLocked)
      lock.lock()
      assertTrue(lock.isLocked)
      lock.unlock()
      assertTrue(lock.isLocked)
      lock.unlock()
      assertFalse(lock.isLocked)
      val barrier = new CyclicBarrier(2)
      val t = newStartedThread(new CheckedRunnable() {
        @throws[Exception]
        override def realRun(): Unit = {
          lock.lock()
          assertTrue(lock.isLocked)
          barrier.await
          barrier.await
          lock.unlock()
        }
      })
      barrier.await
      assertTrue(lock.isLocked)
      barrier.await
      awaitTermination(t)
      assertFalse(lock.isLocked)
    } catch {
      case fail: Exception =>
        threadUnexpectedException(fail)
    }
  }

  /** lockInterruptibly succeeds when unlocked, else is interruptible
   */
  @Test def testLockInterruptibly(): Unit = { testLockInterruptibly(false) }
  @Test def testLockInterruptibly_fair(): Unit = { testLockInterruptibly(true) }
  def testLockInterruptibly(fair: Boolean): Unit = {
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    try lock.lockInterruptibly()
    catch {
      case fail: InterruptedException =>
        threadUnexpectedException(fail)
    }
    assertLockedByMoi(lock)
    val t = newStartedThread(
      new InterruptedLockRunnable(lock)
    )
    waitForQueuedThread(lock, t)
    t.interrupt()
    assertTrue(lock.isLocked)
    assertTrue(lock.isHeldByCurrentThread)
    awaitTermination(t)
    releaseLock(lock)
  }

  /** Calling await without holding lock throws IllegalMonitorStateException
   */
  @Test def testAwait_IMSE(): Unit = { testAwait_IMSE(false) }
  @Test def testAwait_IMSE_fair(): Unit = { testAwait_IMSE(true) }
  def testAwait_IMSE(fair: Boolean): Unit = {
    val lock = new ReentrantLock(fair)
    val c = lock.newCondition
    for (awaitMethod <- ReentrantLockTest.AwaitMethod.values()) {
      val startTime = System.nanoTime
      try {
        await(c, awaitMethod)
        shouldThrow()
      } catch {
        case success: IllegalMonitorStateException =>

        case e: InterruptedException =>
          threadUnexpectedException(e)
      }
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    }
  }

  /** Calling signal without holding lock throws IllegalMonitorStateException
   */
  @Test def testSignal_IMSE(): Unit = { testSignal_IMSE(false) }
  @Test def testSignal_IMSE_fair(): Unit = { testSignal_IMSE(true) }
  def testSignal_IMSE(fair: Boolean): Unit = {
    val lock = new ReentrantLock(fair)
    val c = lock.newCondition
    try {
      c.signal()
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
    val lock = new ReentrantLock(fair)
    val c = lock.newCondition
    val timeoutMillis = JSR166Test.timeoutMillis()
    val timeoutNanos = MILLISECONDS.toNanos(timeoutMillis)
    lock.lock()
    val startTime = System.nanoTime
    try {
      val nanosRemaining = c.awaitNanos(timeoutNanos)
      assertTrue(nanosRemaining <= 0)
    } catch {
      case fail: InterruptedException =>
        threadUnexpectedException(fail)
    }
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis)
    lock.unlock()
  }

  /** timed await without a signal times out
   */
  @Test def testAwait_Timeout(): Unit = { testAwait_Timeout(false) }
  @Test def testAwait_Timeout_fair(): Unit = { testAwait_Timeout(true) }
  def testAwait_Timeout(fair: Boolean): Unit = {
    val lock = new ReentrantLock(fair)
    val c = lock.newCondition
    val timeoutMillis = JSR166Test.timeoutMillis()
    lock.lock()
    val startTime = System.nanoTime
    try assertFalse(c.await(timeoutMillis, MILLISECONDS))
    catch {
      case fail: InterruptedException =>
        threadUnexpectedException(fail)
    }
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis)
    lock.unlock()
  }

  /** awaitUntil without a signal times out
   */
  @Test def testAwaitUntil_Timeout(): Unit = { testAwaitUntil_Timeout(false) }
  @Test def testAwaitUntil_Timeout_fair(): Unit = {
    testAwaitUntil_Timeout(true)
  }
  def testAwaitUntil_Timeout(fair: Boolean): Unit = {
    val lock = new ReentrantLock(fair)
    val c = lock.newCondition
    lock.lock()
    // We shouldn't assume that nanoTime and currentTimeMillis
    // use the same time source, so don't use nanoTime here.
    val delayedDate = this.delayedDate(timeoutMillis())
    try assertFalse(c.awaitUntil(delayedDate))
    catch {
      case fail: InterruptedException =>
        threadUnexpectedException(fail)
    }
    assertTrue(new Date().getTime >= delayedDate.getTime)
    lock.unlock()
  }

  /** await returns when signalled
   */
  @Test def testAwait(): Unit = { testAwait(false) }
  @Test def testAwait_fair(): Unit = { testAwait(true) }
  def testAwait(fair: Boolean): Unit = {
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    val c = lock.newCondition
    val locked = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.lock()
        locked.countDown()
        c.await()
        lock.unlock()
      }
    })
    await(locked)
    lock.lock()
    assertHasWaiters(lock, c, t)
    c.signal()
    assertHasNoWaiters(lock, c)
    assertTrue(t.isAlive)
    lock.unlock()
    awaitTermination(t)
  }

  /** hasWaiters throws NPE if null
   */
  @Test def testHasWaitersNPE(): Unit = { testHasWaitersNPE(false) }
  @Test def testHasWaitersNPE_fair(): Unit = { testHasWaitersNPE(true) }
  def testHasWaitersNPE(fair: Boolean): Unit = {
    val lock = new ReentrantLock(fair)
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
    val lock = new ReentrantLock(fair)
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
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
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
    val lock = new ReentrantLock(fair)
    val c = lock.newCondition
    val lock2 = new ReentrantLock(fair)
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
    val lock = new ReentrantLock(fair)
    val c = lock.newCondition
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
    val lock = new ReentrantLock(fair)
    val c = lock.newCondition
    val lock2 = new ReentrantLock(fair)
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
    val lock = new ReentrantLock(fair)
    val c = lock.newCondition
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
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    val c = lock.newCondition
    val lock2 = new ReentrantLockTest.PublicReentrantLock(fair)
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
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    val c = lock.newCondition
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
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    val c = lock.newCondition
    val pleaseSignal = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.lock()
        assertHasNoWaiters(lock, c)
        assertFalse(lock.hasWaiters(c))
        pleaseSignal.countDown()
        c.await()
        assertHasNoWaiters(lock, c)
        assertFalse(lock.hasWaiters(c))
        lock.unlock()
      }
    })
    await(pleaseSignal)
    lock.lock()
    assertHasWaiters(lock, c, t)
    assertTrue(lock.hasWaiters(c))
    c.signal()
    assertHasNoWaiters(lock, c)
    assertFalse(lock.hasWaiters(c))
    lock.unlock()
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
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    val c = lock.newCondition
    val locked1 = new CountDownLatch(1)
    val locked2 = new CountDownLatch(1)
    val t1 = new Thread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.lock()
        assertFalse(lock.hasWaiters(c))
        assertEquals(0, lock.getWaitQueueLength(c))
        locked1.countDown()
        c.await()
        lock.unlock()
      }
    })
    val t2 = new Thread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.lock()
        assertTrue(lock.hasWaiters(c))
        assertEquals(1, lock.getWaitQueueLength(c))
        locked2.countDown()
        c.await()
        lock.unlock()
      }
    })
    lock.lock()
    assertEquals(0, lock.getWaitQueueLength(c))
    lock.unlock()
    t1.start()
    await(locked1)
    lock.lock()
    assertHasWaiters(lock, c, t1)
    assertEquals(1, lock.getWaitQueueLength(c))
    lock.unlock()
    t2.start()
    await(locked2)
    lock.lock()
    assertHasWaiters(lock, c, t1, t2)
    assertEquals(2, lock.getWaitQueueLength(c))
    c.signalAll()
    assertHasNoWaiters(lock, c)
    lock.unlock()
    awaitTermination(t1)
    awaitTermination(t2)
    assertHasNoWaiters(lock, c)
  }

  /** getWaitingThreads returns only and all waiting threads
   */
  @Test def testGetWaitingThreads(): Unit = { testGetWaitingThreads(false) }
  @Test def testGetWaitingThreads_fair(): Unit = { testGetWaitingThreads(true) }
  def testGetWaitingThreads(fair: Boolean): Unit = {
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    val c = lock.newCondition
    val locked1 = new CountDownLatch(1)
    val locked2 = new CountDownLatch(1)
    val t1 = new Thread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.lock()
        assertTrue(lock.getWaitingThreads(c).isEmpty)
        locked1.countDown()
        c.await()
        lock.unlock()
      }
    })
    val t2 = new Thread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.lock()
        assertFalse(lock.getWaitingThreads(c).isEmpty)
        locked2.countDown()
        c.await()
        lock.unlock()
      }
    })
    lock.lock()
    assertTrue(lock.getWaitingThreads(c).isEmpty)
    lock.unlock()
    t1.start()
    await(locked1)
    lock.lock()
    assertHasWaiters(lock, c, t1)
    assertTrue(lock.getWaitingThreads(c).contains(t1))
    assertFalse(lock.getWaitingThreads(c).contains(t2))
    assertEquals(1, lock.getWaitingThreads(c).size)
    lock.unlock()
    t2.start()
    await(locked2)
    lock.lock()
    assertHasWaiters(lock, c, t1, t2)
    assertTrue(lock.getWaitingThreads(c).contains(t1))
    assertTrue(lock.getWaitingThreads(c).contains(t2))
    assertEquals(2, lock.getWaitingThreads(c).size)
    c.signalAll()
    assertHasNoWaiters(lock, c)
    lock.unlock()
    awaitTermination(t1)
    awaitTermination(t2)
    assertHasNoWaiters(lock, c)
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
    val lock = new ReentrantLock(fair)
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
    testInterruptible(false, ReentrantLockTest.AwaitMethod.await)
  }
  @Test def testInterruptible_await_fair(): Unit = {
    testInterruptible(true, ReentrantLockTest.AwaitMethod.await)
  }
  @Test def testInterruptible_awaitTimed(): Unit = {
    testInterruptible(false, ReentrantLockTest.AwaitMethod.awaitTimed)
  }
  @Test def testInterruptible_awaitTimed_fair(): Unit = {
    testInterruptible(true, ReentrantLockTest.AwaitMethod.awaitTimed)
  }
  @Test def testInterruptible_awaitNanos(): Unit = {
    testInterruptible(false, ReentrantLockTest.AwaitMethod.awaitNanos)
  }
  @Test def testInterruptible_awaitNanos_fair(): Unit = {
    testInterruptible(true, ReentrantLockTest.AwaitMethod.awaitNanos)
  }
  @Test def testInterruptible_awaitUntil(): Unit = {
    testInterruptible(false, ReentrantLockTest.AwaitMethod.awaitUntil)
  }
  @Test def testInterruptible_awaitUntil_fair(): Unit = {
    testInterruptible(true, ReentrantLockTest.AwaitMethod.awaitUntil)
  }
  def testInterruptible(
      fair: Boolean,
      awaitMethod: ReentrantLockTest.AwaitMethod
  ): Unit = {
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    val c = lock.newCondition
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.lock()
        assertLockedByMoi(lock)
        assertHasNoWaiters(lock, c)
        pleaseInterrupt.countDown()
        try await(c, awaitMethod)
        finally {
          assertLockedByMoi(lock)
          assertHasNoWaiters(lock, c)
          lock.unlock()
          assertFalse(Thread.interrupted)
        }
      }
    })
    await(pleaseInterrupt)
    assertHasWaiters(lock, c, t)
    t.interrupt()
    awaitTermination(t)
    assertNotLocked(lock)
  }

  /** signalAll wakes up all threads
   */
  @Test def testSignalAll_await(): Unit =
    testSignalAll(false, ReentrantLockTest.AwaitMethod.await)
  @Test def testSignalAll_await_fair(): Unit =
    testSignalAll(true, ReentrantLockTest.AwaitMethod.await)
  @Test def testSignalAll_awaitTimed(): Unit =
    testSignalAll(false, ReentrantLockTest.AwaitMethod.awaitTimed)
  @Test def testSignalAll_awaitTimed_fair(): Unit =
    testSignalAll(true, ReentrantLockTest.AwaitMethod.awaitTimed)
  @Test def testSignalAll_awaitNanos(): Unit =
    testSignalAll(false, ReentrantLockTest.AwaitMethod.awaitNanos)
  @Test def testSignalAll_awaitNanos_fair(): Unit =
    testSignalAll(true, ReentrantLockTest.AwaitMethod.awaitNanos)
  @Test def testSignalAll_awaitUntil(): Unit =
    testSignalAll(false, ReentrantLockTest.AwaitMethod.awaitUntil)
  @Test def testSignalAll_awaitUntil_fair(): Unit =
    testSignalAll(true, ReentrantLockTest.AwaitMethod.awaitUntil)
  def testSignalAll(
      fair: Boolean,
      awaitMethod: ReentrantLockTest.AwaitMethod
  ): Unit = {
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    val c = lock.newCondition
    val pleaseSignal = new CountDownLatch(2)
    class Awaiter extends CheckedRunnable {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.lock()
        pleaseSignal.countDown()
        await(c, awaitMethod)
        lock.unlock()
      }
    }
    val t1 = newStartedThread(new Awaiter)
    val t2 = newStartedThread(new Awaiter)
    await(pleaseSignal)
    lock.lock()
    assertHasWaiters(lock, c, t1, t2)
    c.signalAll()
    assertHasNoWaiters(lock, c)
    lock.unlock()
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** signal wakes up waiting threads in FIFO order
   */
  @Test def testSignalWakesFifo(): Unit = { testSignalWakesFifo(false) }
  @Test def testSignalWakesFifo_fair(): Unit = { testSignalWakesFifo(true) }
  def testSignalWakesFifo(fair: Boolean): Unit = {
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    val c = lock.newCondition
    val locked1 = new CountDownLatch(1)
    val locked2 = new CountDownLatch(1)
    val t1 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.lock()
        locked1.countDown()
        c.await()
        lock.unlock()
      }
    })
    await(locked1)
    val t2 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.lock()
        locked2.countDown()
        c.await()
        lock.unlock()
      }
    })
    await(locked2)
    lock.lock()
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
    lock.unlock()
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** await after multiple reentrant locking preserves lock count
   */
  @Test def testAwaitLockCount(): Unit = { testAwaitLockCount(false) }
  @Test def testAwaitLockCount_fair(): Unit = { testAwaitLockCount(true) }
  def testAwaitLockCount(fair: Boolean): Unit = {
    val lock = new ReentrantLockTest.PublicReentrantLock(fair)
    val c = lock.newCondition
    val pleaseSignal = new CountDownLatch(2)
    val t1 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.lock()
        assertLockedByMoi(lock)
        assertEquals(1, lock.getHoldCount)
        pleaseSignal.countDown()
        c.await()
        assertLockedByMoi(lock)
        assertEquals(1, lock.getHoldCount)
        lock.unlock()
      }
    })
    val t2 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        lock.lock()
        lock.lock()
        assertLockedByMoi(lock)
        assertEquals(2, lock.getHoldCount)
        pleaseSignal.countDown()
        c.await()
        assertLockedByMoi(lock)
        assertEquals(2, lock.getHoldCount)
        lock.unlock()
        lock.unlock()
      }
    })
    await(pleaseSignal)
    lock.lock()
    assertHasWaiters(lock, c, t1, t2)
    assertEquals(1, lock.getHoldCount)
    c.signalAll()
    assertHasNoWaiters(lock, c)
    lock.unlock()
    awaitTermination(t1)
    awaitTermination(t2)
  }

  // No Object Input Stream
  // /** A serialized lock deserializes as unlocked
  //  */
  // @Test def testSerialization(): Unit = {}
  // @Test def testSerialization_fair(): Unit = {}

  /** toString indicates current lock state
   */
  @Test def testToString(): Unit = { testToString(false) }
  @Test def testToString_fair(): Unit = { testToString(true) }
  def testToString(fair: Boolean): Unit = {
    val lock = new ReentrantLock(fair)
    assertTrue(lock.toString.contains("Unlocked"))
    lock.lock()
    assertTrue(lock.toString.contains("Locked by"))
    lock.unlock()
    assertTrue(lock.toString.contains("Unlocked"))
  }

  /** Tests scenario for JDK-8187408 AbstractQueuedSynchronizer wait queue
   *  corrupted when thread awaits without holding the lock
   */
  @throws[InterruptedException]
  @Test def testBug8187408(): Unit = {
    assumeFalse(
      "Fails due to bug in the JVM",
      Platform.executingInJVMOnJDK8OrLower
    )
    val rnd = ThreadLocalRandom.current
    val awaitMethod = ReentrantLockTest.randomAwaitMethod()
    val nThreads = rnd.nextInt(2, 10)
    val lock = new ReentrantLock
    val cond = lock.newCondition
    val done = new CountDownLatch(nThreads)
    val threads = new util.ArrayList[Thread]
    val rogue: Runnable = () => {
      def foo() = {
        while (done.getCount > 0) try {
          // call await without holding lock?!
          await(cond, awaitMethod)
          throw new AssertionError("should throw")
        } catch {
          case success: IllegalMonitorStateException =>
          case fail: Throwable => threadUnexpectedException(fail)
        }
      }
      foo()
    }
    val rogueThread = new Thread(rogue, "rogue")
    threads.add(rogueThread)
    rogueThread.start()
    val waiter: Runnable = () => {
      def foo() = {
        lock.lock()
        try {
          done.countDown()
          cond.await()
        } catch {
          case fail: Throwable =>
            threadUnexpectedException(fail)
        } finally lock.unlock()
      }
      foo()
    }
    for (i <- 0 until nThreads) {
      val thread = new Thread(waiter, "waiter")
      threads.add(thread)
      thread.start()
    }
    assertTrue(done.await(LONG_DELAY_MS, MILLISECONDS))
    lock.lock()
    try assertEquals(nThreads, lock.getWaitQueueLength(cond))
    finally {
      cond.signalAll()
      lock.unlock()
    }
    threads.forEach { thread =>
      thread.join(LONG_DELAY_MS)
      assertFalse(thread.isAlive)
    }
  }

  /** ThreadMXBean reports the blockers that we expect.
   */
  // @Test def testBlockers(): Unit = ()

  // Tests ported from Scala.js
  @Test def lockAndUnlock(): Unit = {
    val lock = new ReentrantLock()
    assertFalse(lock.isLocked)
    lock.lock()
    assertTrue(lock.isLocked)
    lock.unlock()
    assertFalse(lock.isLocked)
  }

  @Test def tryLock(): Unit = {
    val lock = new ReentrantLock()
    assertFalse(lock.isLocked)
    lock.tryLock()
    assertTrue(lock.isLocked)
    lock.unlock()
    assertFalse(lock.isLocked)
    lock.tryLock(1L, TimeUnit.SECONDS)
    assertTrue(lock.isLocked)
    lock.unlock()
    assertFalse(lock.isLocked)
    Thread.currentThread().interrupt()
    assertThrows(
      classOf[InterruptedException],
      lock.tryLock(1L, TimeUnit.SECONDS)
    )
  }

  @Test def lockInterruptibly(): Unit = {
    val lock = new ReentrantLock()
    assertFalse(lock.isLocked)
    lock.lockInterruptibly()
    assertTrue(lock.isLocked)
    lock.unlock()
    assertFalse(lock.isLocked)
    Thread.currentThread().interrupt()
    assertThrows(classOf[InterruptedException], lock.lockInterruptibly)
  }

  @Test def isHeldByCurrentThread(): Unit = {
    val lock = new ReentrantLock()
    assertFalse(lock.isHeldByCurrentThread())
    lock.lock()
    assertTrue(lock.isHeldByCurrentThread())
  }

  @Test def isFair(): Unit = {
    val l1 = new ReentrantLock()
    assertFalse(l1.isFair)
    val l2 = new ReentrantLock(false)
    assertFalse(l2.isFair)
    val l3 = new ReentrantLock(true)
    assertTrue(l3.isFair)
  }

  @Test def getHoldCount(): Unit = {
    val lock = new ReentrantLock()
    assertFalse(lock.isLocked)
    assertEquals(0, lock.getHoldCount())
    lock.lock()
    assertTrue(lock.isLocked)
    assertEquals(1, lock.getHoldCount())
    lock.lock()
    assertTrue(lock.isLocked)
    assertEquals(2, lock.getHoldCount())
    lock.unlock()
    assertTrue(lock.isLocked)
    assertEquals(1, lock.getHoldCount())
    lock.unlock()
    assertFalse(lock.isLocked)
    assertEquals(0, lock.getHoldCount())
    assertThrows(classOf[IllegalMonitorStateException], lock.unlock)
  }
}
