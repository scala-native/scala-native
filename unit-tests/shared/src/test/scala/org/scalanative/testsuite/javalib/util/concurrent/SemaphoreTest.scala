/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */
package org.scalanative.testsuite.javalib.util.concurrent

import java.lang.Thread.State
import java.util._
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent._

import org.junit.Assert._
import org.junit._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

object SemaphoreTest {

  /** Subclass to expose protected methods
   */
  class PublicSemaphore(permits: Int, fair: Boolean)
      extends Semaphore(permits, fair) {
    def this(permits: Int) = this(permits, true)

    override def getQueuedThreads: Collection[Thread] = super.getQueuedThreads
    def hasQueuedThread(t: Thread): Boolean = super.getQueuedThreads.contains(t)
    override def reducePermits(reduction: Int): Unit = {
      super.reducePermits(reduction)
    }
  }
  sealed trait AcquireMethod {

    /** Acquires 1 permit. */
    // Intentionally meta-circular
    def acquire(s: Semaphore): Unit = acquire(s, 1)

    def acquire(s: Semaphore, permits: Int): Unit =
      0.until(permits).foreach(_ => acquire(s))

    def parkedState = Thread.State.WAITING
  }
  object AcquireMethod {
    import JSR166Test.LONG_DELAY_MS
    case object acquire extends AcquireMethod {
      override def acquire(s: Semaphore): Unit = s.acquire()
    }
    case object acquireN extends AcquireMethod {
      override def acquire(s: Semaphore, permits: Int): Unit =
        s.acquire(permits)
    }
    case object acquireUninterruptibly extends AcquireMethod {
      override def acquire(s: Semaphore): Unit = s.acquireUninterruptibly()
    }
    case object acquireUninterruptiblyN extends AcquireMethod {
      override def acquire(s: Semaphore, permits: Int): Unit =
        s.acquireUninterruptibly(permits)
    }
    case object tryAcquire extends AcquireMethod {
      override def acquire(s: Semaphore): Unit = assertTrue(s.tryAcquire())
    }
    case object tryAcquireN extends AcquireMethod {
      override def acquire(s: Semaphore, permits: Int): Unit = assertTrue(
        s.tryAcquire(permits)
      )
    }
    case object tryAcquireTimed extends AcquireMethod {
      override def acquire(s: Semaphore): Unit = assertTrue(
        s.tryAcquire(2 * LONG_DELAY_MS, MILLISECONDS)
      )
      override def parkedState: State = Thread.State.TIMED_WAITING
    }
    case object tryAcquireTimedN extends AcquireMethod {
      override def acquire(s: Semaphore, permits: Int): Unit = assertTrue(
        s.tryAcquire(permits, 2 * LONG_DELAY_MS, MILLISECONDS)
      )
      override def parkedState: State = Thread.State.TIMED_WAITING
    }

  }
}
class SemaphoreTest extends JSR166Test {
  import JSR166Test._
  import SemaphoreTest._

  /** A runnable calling acquire
   */
  class InterruptibleLockRunnable(val lock: Semaphore) extends CheckedRunnable {
    override def realRun(): Unit = {
      try lock.acquire()
      catch {
        case ignored: InterruptedException =>

      }
    }
  }

  /** A runnable calling acquire that expects to be interrupted
   */
  class InterruptedLockRunnable(val lock: Semaphore)
      extends CheckedInterruptedRunnable {
    @throws[InterruptedException]
    override def realRun(): Unit = { lock.acquire() }
  }

  /** Spin-waits until s.hasQueuedThread(t) becomes true.
   */
  def waitForQueuedThread(s: PublicSemaphore, t: Thread): Unit = {
    val startTime = System.nanoTime
    while (!s.hasQueuedThread(t)) {
      if (millisElapsedSince(startTime) > LONG_DELAY_MS)
        throw new AssertionError("timed out")
      Thread.`yield`()
    }
    assertTrue(s.hasQueuedThreads)
    assertTrue(t.isAlive)
  }

  /** Spin-waits until s.hasQueuedThreads() becomes true.
   */
  def waitForQueuedThreads(s: Semaphore): Unit = {
    val startTime = System.nanoTime
    while (!s.hasQueuedThreads) {
      if (millisElapsedSince(startTime) > LONG_DELAY_MS)
        throw new AssertionError("timed out")
      Thread.`yield`()
    }
  }

  /** Zero, negative, and positive initial values are allowed in constructor
   */
  @Test def testConstructor(): Unit = { testConstructor(false) }
  @Test def testConstructor_fair(): Unit = { testConstructor(true) }
  def testConstructor(fair: Boolean): Unit = {
    for (permits <- Array[Int](-42, -1, 0, 1, 42)) {
      val s = new Semaphore(permits, fair)
      assertEquals(permits, s.availablePermits)
      assertEquals(fair, s.isFair)
    }
  }

  /** Constructor without fairness argument behaves as nonfair
   */
  @Test def testConstructorDefaultsToNonFair(): Unit = {
    for (permits <- Array[Int](-42, -1, 0, 1, 42)) {
      val s = new Semaphore(permits)
      assertEquals(permits, s.availablePermits)
      assertFalse(s.isFair)
    }
  }

  /** tryAcquire succeeds when sufficient permits, else fails
   */
  @Test def testTryAcquireInSameThread(): Unit = {
    testTryAcquireInSameThread(false)
  }
  @Test def testTryAcquireInSameThread_fair(): Unit = {
    testTryAcquireInSameThread(true)
  }
  def testTryAcquireInSameThread(fair: Boolean): Unit = {
    val s = new Semaphore(2, fair)
    assertEquals(2, s.availablePermits)
    assertTrue(s.tryAcquire)
    assertTrue(s.tryAcquire)
    assertEquals(0, s.availablePermits)
    assertFalse(s.tryAcquire)
    assertFalse(s.tryAcquire)
    assertEquals(0, s.availablePermits)
  }

  /** timed tryAcquire times out
   */
  @throws[InterruptedException]
  @Test def testTryAcquire_timeout(): Unit = {
    val fair = randomBoolean()
    val s = new Semaphore(0, fair)
    val startTime = System.nanoTime
    assertFalse(s.tryAcquire(timeoutMillis(), MILLISECONDS))
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
  }

  /** timed tryAcquire(N) times out
   */
  @throws[InterruptedException]
  @Test def testTryAcquireN_timeout(): Unit = {
    val fair = randomBoolean()
    val s = new Semaphore(2, fair)
    val startTime = System.nanoTime
    assertFalse(s.tryAcquire(3, timeoutMillis(), MILLISECONDS))
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
  }

  /** acquire(), acquire(N), timed tryAcquired, timed tryAcquire(N) are
   *  interruptible
   */
  @Test def testInterruptible_acquire(): Unit = {
    testInterruptible(false, AcquireMethod.acquire)
  }
  @Test def testInterruptible_acquire_fair(): Unit = {
    testInterruptible(true, AcquireMethod.acquire)
  }
  @Test def testInterruptible_acquireN(): Unit = {
    testInterruptible(false, AcquireMethod.acquireN)
  }
  @Test def testInterruptible_acquireN_fair(): Unit = {
    testInterruptible(true, AcquireMethod.acquireN)
  }
  @Test def testInterruptible_tryAcquireTimed(): Unit = {
    testInterruptible(false, AcquireMethod.tryAcquireTimed)
  }
  @Test def testInterruptible_tryAcquireTimed_fair(): Unit = {
    testInterruptible(true, AcquireMethod.tryAcquireTimed)
  }
  @Test def testInterruptible_tryAcquireTimedN(): Unit = {
    testInterruptible(false, AcquireMethod.tryAcquireTimedN)
  }
  @Test def testInterruptible_tryAcquireTimedN_fair(): Unit = {
    testInterruptible(true, AcquireMethod.tryAcquireTimedN)
  }
  def testInterruptible(
      fair: Boolean,
      acquirer: AcquireMethod
  ): Unit = {
    val s = new PublicSemaphore(0, fair)
    val pleaseInterrupt = new CyclicBarrier(2)
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        // Interrupt before acquire
        Thread.currentThread.interrupt()
        try {
          acquirer.acquire(s)
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
        // Interrupt before acquire(N)
        Thread.currentThread.interrupt()
        try {
          acquirer.acquire(s, 3)
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
        // Interrupt during acquire
        await(pleaseInterrupt)
        try {
          acquirer.acquire(s)
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
        // Interrupt during acquire(N)
        await(pleaseInterrupt)
        try {
          acquirer.acquire(s, 3)
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
      }
    })
    var n = 2
    while ({ n -= 1; n + 1 } > 0) {
      await(pleaseInterrupt)
      assertThreadBlocks(t, acquirer.parkedState)
      t.interrupt()
    }
    awaitTermination(t)
  }

  /** acquireUninterruptibly(), acquireUninterruptibly(N) are uninterruptible
   */
  @Test def testUninterruptible_acquireUninterruptibly(): Unit = {
    testUninterruptible(
      false,
      AcquireMethod.acquireUninterruptibly
    )
  }
  @Test def testUninterruptible_acquireUninterruptibly_fair(): Unit = {
    testUninterruptible(
      true,
      AcquireMethod.acquireUninterruptibly
    )
  }
  @Test def testUninterruptible_acquireUninterruptiblyN(): Unit = {
    testUninterruptible(
      false,
      AcquireMethod.acquireUninterruptiblyN
    )
  }
  @Test def testUninterruptible_acquireUninterruptiblyN_fair(): Unit = {
    testUninterruptible(
      true,
      AcquireMethod.acquireUninterruptiblyN
    )
  }
  def testUninterruptible(
      fair: Boolean,
      acquirer: AcquireMethod
  ): Unit = {
    val s = new PublicSemaphore(0, fair)
    val pleaseInterrupt = new Semaphore(-1, fair)
    val t1 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        // Interrupt before acquire
        pleaseInterrupt.release()
        Thread.currentThread.interrupt()
        acquirer.acquire(s)
        assertTrue(Thread.interrupted)
      }
    })
    val t2 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        // Interrupt during acquire
        pleaseInterrupt.release()
        acquirer.acquire(s)
        assertTrue(Thread.interrupted)
      }
    })
    await(pleaseInterrupt)
    waitForQueuedThread(s, t1)
    waitForQueuedThread(s, t2)
    t2.interrupt()
    assertThreadBlocks(t1, Thread.State.WAITING)
    assertThreadBlocks(t2, Thread.State.WAITING)
    s.release(2)
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** hasQueuedThreads reports whether there are waiting threads
   */
  @Test def testHasQueuedThreads(): Unit = { testHasQueuedThreads(false) }
  @Test def testHasQueuedThreads_fair(): Unit = { testHasQueuedThreads(true) }
  def testHasQueuedThreads(fair: Boolean): Unit = {
    val lock = new PublicSemaphore(1, fair)
    assertFalse(lock.hasQueuedThreads)
    lock.acquireUninterruptibly()
    val t1 = newStartedThread(new InterruptedLockRunnable(lock))
    waitForQueuedThread(lock, t1)
    assertTrue(lock.hasQueuedThreads)
    val t2 = newStartedThread(new InterruptibleLockRunnable(lock))
    waitForQueuedThread(lock, t2)
    assertTrue(lock.hasQueuedThreads)
    t1.interrupt()
    awaitTermination(t1)
    assertTrue(lock.hasQueuedThreads)
    lock.release()
    awaitTermination(t2)
    assertFalse(lock.hasQueuedThreads)
  }

  /** getQueueLength reports number of waiting threads
   */
  @Test def testGetQueueLength(): Unit = { testGetQueueLength(false) }
  @Test def testGetQueueLength_fair(): Unit = { testGetQueueLength(true) }
  def testGetQueueLength(fair: Boolean): Unit = {
    val lock = new PublicSemaphore(1, fair)
    assertEquals(0, lock.getQueueLength)
    lock.acquireUninterruptibly()
    val t1 = newStartedThread(new InterruptedLockRunnable(lock))
    waitForQueuedThread(lock, t1)
    assertEquals(1, lock.getQueueLength)
    val t2 = newStartedThread(new InterruptibleLockRunnable(lock))
    waitForQueuedThread(lock, t2)
    assertEquals(2, lock.getQueueLength)
    t1.interrupt()
    awaitTermination(t1)
    assertEquals(1, lock.getQueueLength)
    lock.release()
    awaitTermination(t2)
    assertEquals(0, lock.getQueueLength)
  }

  /** getQueuedThreads includes waiting threads
   */
  @Test def testGetQueuedThreads(): Unit = { testGetQueuedThreads(false) }
  @Test def testGetQueuedThreads_fair(): Unit = { testGetQueuedThreads(true) }
  def testGetQueuedThreads(fair: Boolean): Unit = {
    val lock = new PublicSemaphore(1, fair)
    assertTrue(lock.getQueuedThreads.isEmpty)
    lock.acquireUninterruptibly()
    assertTrue(lock.getQueuedThreads.isEmpty)
    val t1 = newStartedThread(new InterruptedLockRunnable(lock))
    waitForQueuedThread(lock, t1)
    assertTrue(lock.getQueuedThreads.contains(t1))
    val t2 = newStartedThread(new InterruptibleLockRunnable(lock))
    waitForQueuedThread(lock, t2)
    assertTrue(lock.getQueuedThreads.contains(t1))
    assertTrue(lock.getQueuedThreads.contains(t2))
    t1.interrupt()
    awaitTermination(t1)
    assertFalse(lock.getQueuedThreads.contains(t1))
    assertTrue(lock.getQueuedThreads.contains(t2))
    lock.release()
    awaitTermination(t2)
    assertTrue(lock.getQueuedThreads.isEmpty)
  }

  /** drainPermits reports and removes given number of permits
   */
  @Test def testDrainPermits(): Unit = { testDrainPermits(false) }
  @Test def testDrainPermits_fair(): Unit = { testDrainPermits(true) }
  def testDrainPermits(fair: Boolean): Unit = {
    val s = new Semaphore(0, fair)
    assertEquals(0, s.availablePermits)
    assertEquals(0, s.drainPermits)
    s.release(10)
    assertEquals(10, s.availablePermits)
    assertEquals(10, s.drainPermits)
    assertEquals(0, s.availablePermits)
    assertEquals(0, s.drainPermits)
  }

  /** release(-N) throws IllegalArgumentException
   */
  @Test def testReleaseIAE(): Unit = { testReleaseIAE(false) }
  @Test def testReleaseIAE_fair(): Unit = { testReleaseIAE(true) }
  def testReleaseIAE(fair: Boolean): Unit = {
    val s = new Semaphore(10, fair)
    try {
      s.release(-1)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** reducePermits(-N) throws IllegalArgumentException
   */
  @Test def testReducePermitsIAE(): Unit = { testReducePermitsIAE(false) }
  @Test def testReducePermitsIAE_fair(): Unit = { testReducePermitsIAE(true) }
  def testReducePermitsIAE(fair: Boolean): Unit = {
    val s = new PublicSemaphore(10, fair)
    try {
      s.reducePermits(-1)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** reducePermits reduces number of permits
   */
  @Test def testReducePermits(): Unit = { testReducePermits(false) }
  @Test def testReducePermits_fair(): Unit = { testReducePermits(true) }
  def testReducePermits(fair: Boolean): Unit = {
    val s = new PublicSemaphore(10, fair)
    assertEquals(10, s.availablePermits)
    s.reducePermits(0)
    assertEquals(10, s.availablePermits)
    s.reducePermits(1)
    assertEquals(9, s.availablePermits)
    s.reducePermits(10)
    assertEquals(-1, s.availablePermits)
    s.reducePermits(10)
    assertEquals(-11, s.availablePermits)
    s.reducePermits(0)
    assertEquals(-11, s.availablePermits)
  }

  // @Test def testSerialization(): Unit = { testSerialization(false) }
  // @Test def testSerialization_fair(): Unit = { testSerialization(true) }
  // def testSerialization(fair: Boolean): Unit = ???

  /** tryAcquire(n) succeeds when sufficient permits, else fails
   */
  @Test def testTryAcquireNInSameThread(): Unit = {
    testTryAcquireNInSameThread(false)
  }
  @Test def testTryAcquireNInSameThread_fair(): Unit = {
    testTryAcquireNInSameThread(true)
  }
  def testTryAcquireNInSameThread(fair: Boolean): Unit = {
    val s = new Semaphore(2, fair)
    assertEquals(2, s.availablePermits)
    assertFalse(s.tryAcquire(3))
    assertEquals(2, s.availablePermits)
    assertTrue(s.tryAcquire(2))
    assertEquals(0, s.availablePermits)
    assertFalse(s.tryAcquire(1))
    assertFalse(s.tryAcquire(2))
    assertEquals(0, s.availablePermits)
  }

  /** acquire succeeds if permits available
   */
  @Test def testReleaseAcquireSameThread_acquire(): Unit = {
    testReleaseAcquireSameThread(false, AcquireMethod.acquire)
  }
  @Test def testReleaseAcquireSameThread_acquire_fair(): Unit = {
    testReleaseAcquireSameThread(true, AcquireMethod.acquire)
  }
  @Test def testReleaseAcquireSameThread_acquireN(): Unit = {
    testReleaseAcquireSameThread(false, AcquireMethod.acquireN)
  }
  @Test def testReleaseAcquireSameThread_acquireN_fair(): Unit = {
    testReleaseAcquireSameThread(true, AcquireMethod.acquireN)
  }
  @Test def testReleaseAcquireSameThread_acquireUninterruptibly(): Unit = {
    testReleaseAcquireSameThread(
      false,
      AcquireMethod.acquireUninterruptibly
    )
  }
  @Test def testReleaseAcquireSameThread_acquireUninterruptibly_fair(): Unit = {
    testReleaseAcquireSameThread(
      true,
      AcquireMethod.acquireUninterruptibly
    )
  }
  @Test def testReleaseAcquireSameThread_acquireUninterruptiblyN(): Unit = {
    testReleaseAcquireSameThread(
      false,
      AcquireMethod.acquireUninterruptibly
    )
  }
  @Test def testReleaseAcquireSameThread_acquireUninterruptiblyN_fair()
      : Unit = {
    testReleaseAcquireSameThread(
      true,
      AcquireMethod.acquireUninterruptibly
    )
  }
  @Test def testReleaseAcquireSameThread_tryAcquire(): Unit = {
    testReleaseAcquireSameThread(false, AcquireMethod.tryAcquire)
  }
  @Test def testReleaseAcquireSameThread_tryAcquire_fair(): Unit = {
    testReleaseAcquireSameThread(true, AcquireMethod.tryAcquire)
  }
  @Test def testReleaseAcquireSameThread_tryAcquireN(): Unit = {
    testReleaseAcquireSameThread(false, AcquireMethod.tryAcquireN)
  }
  @Test def testReleaseAcquireSameThread_tryAcquireN_fair(): Unit = {
    testReleaseAcquireSameThread(true, AcquireMethod.tryAcquireN)
  }
  @Test def testReleaseAcquireSameThread_tryAcquireTimed(): Unit = {
    testReleaseAcquireSameThread(
      false,
      AcquireMethod.tryAcquireTimed
    )
  }
  @Test def testReleaseAcquireSameThread_tryAcquireTimed_fair(): Unit = {
    testReleaseAcquireSameThread(
      true,
      AcquireMethod.tryAcquireTimed
    )
  }
  @Test def testReleaseAcquireSameThread_tryAcquireTimedN(): Unit = {
    testReleaseAcquireSameThread(
      false,
      AcquireMethod.tryAcquireTimedN
    )
  }
  @Test def testReleaseAcquireSameThread_tryAcquireTimedN_fair(): Unit = {
    testReleaseAcquireSameThread(
      true,
      AcquireMethod.tryAcquireTimedN
    )
  }
  def testReleaseAcquireSameThread(
      fair: Boolean,
      acquirer: AcquireMethod
  ): Unit = {
    val s = new Semaphore(1, fair)
    for (i <- 1 until 6) {
      s.release(i)
      assertEquals(1 + i, s.availablePermits)
      try acquirer.acquire(s, i)
      catch {
        case e: InterruptedException =>
          threadUnexpectedException(e)
      }
      assertEquals(1, s.availablePermits)
    }
  }

  /** release in one thread enables acquire in another thread
   */
  @Test def testReleaseAcquireDifferentThreads_acquire(): Unit = {
    testReleaseAcquireDifferentThreads(
      false,
      AcquireMethod.acquire
    )
  }
  @Test def testReleaseAcquireDifferentThreads_acquire_fair(): Unit = {
    testReleaseAcquireDifferentThreads(
      true,
      AcquireMethod.acquire
    )
  }
  @Test def testReleaseAcquireDifferentThreads_acquireN(): Unit = {
    testReleaseAcquireDifferentThreads(
      false,
      AcquireMethod.acquireN
    )
  }
  @Test def testReleaseAcquireDifferentThreads_acquireN_fair(): Unit = {
    testReleaseAcquireDifferentThreads(
      true,
      AcquireMethod.acquireN
    )
  }
  @Test def testReleaseAcquireDifferentThreads_acquireUninterruptibly()
      : Unit = {
    testReleaseAcquireDifferentThreads(
      false,
      AcquireMethod.acquireUninterruptibly
    )
  }
  @Test def testReleaseAcquireDifferentThreads_acquireUninterruptibly_fair()
      : Unit = {
    testReleaseAcquireDifferentThreads(
      true,
      AcquireMethod.acquireUninterruptibly
    )
  }
  @Test def testReleaseAcquireDifferentThreads_acquireUninterruptiblyN()
      : Unit = {
    testReleaseAcquireDifferentThreads(
      false,
      AcquireMethod.acquireUninterruptibly
    )
  }
  @Test def testReleaseAcquireDifferentThreads_acquireUninterruptiblyN_fair()
      : Unit = {
    testReleaseAcquireDifferentThreads(
      true,
      AcquireMethod.acquireUninterruptibly
    )
  }
  @Test def testReleaseAcquireDifferentThreads_tryAcquireTimed(): Unit = {
    testReleaseAcquireDifferentThreads(
      false,
      AcquireMethod.tryAcquireTimed
    )
  }
  @Test def testReleaseAcquireDifferentThreads_tryAcquireTimed_fair(): Unit = {
    testReleaseAcquireDifferentThreads(
      true,
      AcquireMethod.tryAcquireTimed
    )
  }
  @Test def testReleaseAcquireDifferentThreads_tryAcquireTimedN(): Unit = {
    testReleaseAcquireDifferentThreads(
      false,
      AcquireMethod.tryAcquireTimedN
    )
  }
  @Test def testReleaseAcquireDifferentThreads_tryAcquireTimedN_fair(): Unit = {
    testReleaseAcquireDifferentThreads(
      true,
      AcquireMethod.tryAcquireTimedN
    )
  }
  def testReleaseAcquireDifferentThreads(
      fair: Boolean,
      acquirer: AcquireMethod
  ): Unit = {
    val s = new Semaphore(0, fair)
    val rounds = 4
    val startTime = System.nanoTime
    val t = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        for (i <- 0 until rounds) {
          assertFalse(s.hasQueuedThreads)
          if (i % 2 == 0) acquirer.acquire(s)
          else acquirer.acquire(s, 3)
        }
      }
    })
    for (i <- 0 until rounds) {
      while (!(s.availablePermits == 0 && s.hasQueuedThreads)) Thread.`yield`()
      assertTrue(t.isAlive)
      if (i % 2 == 0) s.release()
      else s.release(3)
    }
    awaitTermination(t)
    assertEquals(0, s.availablePermits)
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
  }

  /** fair locks are strictly FIFO
   */
  @Test def testFairLocksFifo(): Unit = {
    val s = new PublicSemaphore(1, true)
    val pleaseRelease = new CountDownLatch(1)
    val t1 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        // Will block; permits are available, but not three
        s.acquire(3)
      }
    })
    waitForQueuedThread(s, t1)
    val t2 = newStartedThread(new CheckedRunnable() {
      @throws[InterruptedException]
      override def realRun(): Unit = {
        // Will fail, even though 1 permit is available
        assertFalse(s.tryAcquire(randomExpiredTimeout(), randomTimeUnit()))
        assertFalse(s.tryAcquire(1, randomExpiredTimeout(), randomTimeUnit()))
        // untimed tryAcquire will barge and succeed
        assertTrue(s.tryAcquire)
        s.release(2)
        assertTrue(s.tryAcquire(2))
        s.release()
        pleaseRelease.countDown()
        // Will queue up behind t1, even though 1 permit is available
        s.acquire()
      }
    })
    await(pleaseRelease)
    waitForQueuedThread(s, t2)
    s.release(2)
    awaitTermination(t1)
    assertTrue(t2.isAlive)
    s.release()
    awaitTermination(t2)
  }

  /** toString indicates current number of permits
   */
  @Test def testToString(): Unit = { testToString(false) }
  @Test def testToString_fair(): Unit = { testToString(true) }
  def testToString(fair: Boolean): Unit = {
    val s = new PublicSemaphore(0, fair)
    assertTrue(s.toString.contains("Permits = 0"))
    s.release()
    assertTrue(s.toString.contains("Permits = 1"))
    s.release(2)
    assertTrue(s.toString.contains("Permits = 3"))
    s.reducePermits(5)
    assertTrue(s.toString.contains("Permits = -2"))
  }

  // tests ported from Scala.js
  @Test def ctorNegativePermits(): Unit = {
    val sem = new Semaphore(-1)
    assertEquals(-1, sem.availablePermits())
    assertFalse(sem.tryAcquire())
    sem.release()
    assertEquals(0, sem.availablePermits())
  }

  @Test def drain(): Unit = {
    val sem = new Semaphore(3)
    assertEquals(3, sem.drainPermits())
    assertEquals(0, sem.availablePermits())
  }

  @Test def drainNegative(): Unit = {
    val sem = new Semaphore(-3)
    assertEquals(-3, sem.drainPermits())
    assertEquals(0, sem.availablePermits())
  }

  @Test def tryAcquire(): Unit = {
    val sem = new Semaphore(1)
    assertTrue(sem.tryAcquire())
    assertEquals(0, sem.availablePermits())
    assertFalse(sem.tryAcquire())
    assertEquals(0, sem.availablePermits())
  }

  @Test def tryAcquirePermits(): Unit = {
    val sem = new Semaphore(5)
    assertTrue(sem.tryAcquire(3))
    assertEquals(2, sem.availablePermits())
    assertFalse(sem.tryAcquire(3))
    assertEquals(2, sem.availablePermits())
    assertTrue(sem.tryAcquire(2))
    assertEquals(0, sem.availablePermits())
    assertThrows(classOf[IllegalArgumentException], sem.tryAcquire(-1))
    assertEquals(0, sem.availablePermits())
  }

  @Test def release(): Unit = {
    val sem = new Semaphore(0)
    assertEquals(0, sem.availablePermits())
    sem.release()
    assertEquals(1, sem.availablePermits())
  }

  @Test def releasePermits(): Unit = {
    val sem = new Semaphore(1)
    assertEquals(1, sem.availablePermits())
    sem.release(2)
    assertEquals(3, sem.availablePermits())
    assertThrows(classOf[IllegalArgumentException], sem.release(-1))
    assertEquals(3, sem.availablePermits())
  }

  @Test def reducePermitsIntoNegative(): Unit = {
    class ReducibleSemaphore(permits: Int) extends Semaphore(permits) {
      // Simply expose the method.
      override def reducePermits(reduction: Int): Unit =
        super.reducePermits(reduction)
    }

    val sem = new ReducibleSemaphore(1)
    assertEquals(1, sem.availablePermits())
    assertTrue(sem.tryAcquire())
    assertFalse(sem.tryAcquire())
    assertEquals(0, sem.availablePermits())

    sem.reducePermits(2)
    assertEquals(-2, sem.availablePermits())
    assertFalse(sem.tryAcquire())

    sem.release(3)
    assertEquals(1, sem.availablePermits())

    assertThrows(classOf[IllegalArgumentException], sem.reducePermits(-1))
    assertEquals(1, sem.availablePermits())

    assertTrue(sem.tryAcquire())
  }

  @Test def queuedThreads(): Unit = {
    val sem = new Semaphore(0)

    assertFalse(sem.hasQueuedThreads())
    assertEquals(0, sem.getQueueLength())
  }

  @Test def overrideQueuedThreads(): Unit = {
    /* Check that the accessor methods *do not* delegate to `getQueuedThreads`.
     * See the comment in the implementation of Semaphore for why.
     */

    class EternallyQueuedSemaphore extends Semaphore(0) {
      override protected def getQueuedThreads(): Collection[Thread] =
        Collections.singleton(Thread.currentThread())
    }

    val sem = new EternallyQueuedSemaphore

    assertFalse(sem.hasQueuedThreads())
    assertEquals(0, sem.getQueueLength())
  }
}
