/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent
package locks

import java.util._
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer
import java.util.concurrent.TimeUnit._

import org.junit._
import org.junit.Assert._
import org.junit.Assume._

import org.scalanative.testsuite.utils.Platform
import scala.util.control.Breaks

class AbstractQueuedLongSynchronizerTest extends JSR166Test {
  import JSR166Test._

  class Mutex extends AbstractQueuedLongSynchronizer {
    import Mutex._

    /** Owner thread is untracked, so this is really just isLocked(). */
    override def isHeldExclusively() = {
      val state = getState()
      assertTrue(state == UNLOCKED || state == LOCKED)
      state == LOCKED
    }

    override def tryAcquire(acquires: Long) = {
      assertEquals(LOCKED, acquires)
      compareAndSetState(UNLOCKED, LOCKED)
    }

    override def tryRelease(releases: Long): Boolean = {
      if (getState() != LOCKED) throw new IllegalMonitorStateException()
      setState(UNLOCKED)
      true
    }

    def tryAcquireNanos(nanos: Long): Boolean = tryAcquireNanos(LOCKED, nanos)
    def tryAcquire(): Boolean = tryAcquire(LOCKED)
    def tryRelease(): Boolean = tryRelease(LOCKED)
    def acquire(): Unit = acquire(LOCKED)
    def acquireInterruptibly(): Unit = acquireInterruptibly(LOCKED)
    def release(): Unit = release(LOCKED)

    /** Faux-Implements Lock.newCondition(). */
    def newCondition(): ConditionObject = new ConditionObject()
  }
  object Mutex {

    /** An eccentric value for locked synchronizer state. */
    final val LOCKED = (1L << 63) | (1L << 15)
    final val UNLOCKED = 0L
  }

  /** A simple mutex class, adapted from the class javadoc. Exclusive acquire
   *  tests exercise this as a sample user extension. Other methods/features of
   *  AbstractQueuedLongSynchronizer are tested via other test classes,
   *  including those for ReentrantLock, ReentrantReadWriteLock, and Semaphore.
   *
   *  Unlike the javadoc sample, we don't track owner thread via
   *  AbstractOwnableSynchronizer methods.
   */

  /** A minimal latch class, to test shared mode.
   */
  class BooleanLatch extends AbstractQueuedLongSynchronizer {
    def isSignalled(): Boolean = getState() != 0
    override def tryAcquireShared(ignore: Long): Long =
      if (isSignalled()) 1 else -1
    override def tryReleaseShared(ingore: Long): Boolean = {
      setState(1L << 62)
      true
    }
  }

  /** A runnable calling acquireInterruptibly that does not expect to be
   *  interrupted.
   */
  class InterruptibleSyncRunnable(sync: Mutex) extends CheckedRunnable {
    def realRun(): Unit = sync.acquireInterruptibly()
  }

  /** A runnable calling acquireInterruptibly that expects to be interrupted.
   */
  class InterruptedSyncRunnable(sync: Mutex)
      extends CheckedInterruptedRunnable {
    def realRun(): Unit = sync.acquireInterruptibly()
  }

  /** A constant to clarify calls to checking methods below. */
  final val NO_THREADS = Array.empty[Thread]

  /** Spin-waits until sync.isQueued(t) becomes true.
   */
  def waitForQueuedThread(sync: AbstractQueuedLongSynchronizer, t: Thread) = {
    val startTime = System.nanoTime()
    while (!sync.isQueued(t)) {
      if (millisElapsedSince(startTime) > LONG_DELAY_MS)
        throw new AssertionError("timed out")
      Thread.`yield`()
    }
    assertTrue(t.isAlive())
  }

  /** Checks that sync has exactly the given queued threads.
   */
  def assertHasQueuedThreads(
      sync: AbstractQueuedLongSynchronizer,
      expected: Thread*
  ) = {
    val actual = sync.getQueuedThreads()
    assertEquals(expected.length > 0, sync.hasQueuedThreads())
    assertEquals(expected.length, sync.getQueueLength())
    assertEquals(expected.length, actual.size())
    assertEquals(expected.length == 0, actual.isEmpty())
    val expectedThreads = new HashSet[Thread]()
    expected.foreach(expectedThreads.add(_))
    assertEquals(
      expectedThreads,
      new HashSet(actual)
    )
  }

  /** Checks that sync has exactly the given (exclusive) queued threads.
   */
  def assertHasExclusiveQueuedThreads(
      sync: AbstractQueuedLongSynchronizer,
      expected: Thread*
  ) = {
    assertHasQueuedThreads(sync, expected: _*)
    assertEquals(
      new HashSet(sync.getExclusiveQueuedThreads()),
      new HashSet(sync.getQueuedThreads())
    )
    assertEquals(0, sync.getSharedQueuedThreads().size())
    assertTrue(sync.getSharedQueuedThreads().isEmpty())
  }

  /** Checks that sync has exactly the given (shared) queued threads.
   */
  def assertHasSharedQueuedThreads(
      sync: AbstractQueuedLongSynchronizer,
      expected: Thread*
  ) = {
    assertHasQueuedThreads(sync, expected: _*)
    assertEquals(
      new HashSet(sync.getSharedQueuedThreads()),
      new HashSet(sync.getQueuedThreads())
    )
    assertEquals(0, sync.getExclusiveQueuedThreads().size())
    assertTrue(sync.getExclusiveQueuedThreads().isEmpty())
  }

  /** Checks that condition c has exactly the given waiter threads, after
   *  acquiring mutex.
   */
  def assertHasWaitersUnlocked(
      sync: Mutex,
      c: AbstractQueuedLongSynchronizer#ConditionObject,
      threads: Thread*
  ) = {
    sync.acquire()
    assertHasWaitersLocked(sync, c, threads: _*)
    sync.release()
  }

  /** Checks that condition c has exactly the given waiter threads.
   */
  def assertHasWaitersLocked(
      sync: Mutex,
      c: AbstractQueuedLongSynchronizer#ConditionObject,
      threads: Thread*
  ) = {
    assertEquals("hasWaiters", threads.length > 0, sync.hasWaiters(c))
    assertEquals(threads.length, sync.getWaitQueueLength(c))
    assertEquals(
      "getWaitingThreads.isEmpty",
      threads.length == 0,
      sync.getWaitingThreads(c).isEmpty()
    )
    assertEquals(threads.length, sync.getWaitingThreads(c).size())
    val expected = new HashSet[Thread]()
    threads.foreach(expected.add(_))
    assertEquals(
      expected,
      new HashSet(sync.getWaitingThreads(c))
    )
  }

  sealed trait AwaitMethod
  object AwaitMethod {
    case object await extends AwaitMethod
    case object awaitTimed extends AwaitMethod
    case object awaitNanos extends AwaitMethod
    case object awaitUntil extends AwaitMethod
    val values = Array(await, awaitTimed, awaitNanos, awaitUntil)
  }
  import AwaitMethod._

  /** Awaits condition using the specified AwaitMethod.
   */
  def await(
      c: AbstractQueuedLongSynchronizer#ConditionObject,
      awaitMethod: AwaitMethod
  ) = {
    val timeoutMillis = 2 * LONG_DELAY_MS
    awaitMethod match {
      case AwaitMethod.`await` => c.await()
      case `awaitTimed` => assertTrue(c.await(timeoutMillis, MILLISECONDS))
      case `awaitNanos` =>
        val timeoutNanos = MILLISECONDS.toNanos(timeoutMillis)
        val nanosRemaining = c.awaitNanos(timeoutNanos)
        assertTrue(nanosRemaining > 0)
      case `awaitUntil` =>
        assertTrue(c.awaitUntil(delayedDate(timeoutMillis)))
    }
  }

  /** Checks that awaiting the given condition times out (using the default
   *  timeout duration).
   */
  def assertAwaitTimesOut(
      c: AbstractQueuedLongSynchronizer#ConditionObject,
      awaitMethod: AwaitMethod
  ): Unit = {
    val timeoutMillis = JSR166Test.timeoutMillis()
    try
      awaitMethod match {
        case `awaitTimed` =>
          val startTime = System.nanoTime()
          assertFalse(c.await(timeoutMillis, MILLISECONDS))
          assertTrue(millisElapsedSince(startTime) >= timeoutMillis)
        case `awaitNanos` =>
          val startTime = System.nanoTime()
          val timeoutNanos = MILLISECONDS.toNanos(timeoutMillis)
          val nanosRemaining = c.awaitNanos(timeoutNanos)
          assertTrue(nanosRemaining <= 0)
          assertTrue(nanosRemaining > -MILLISECONDS.toNanos(LONG_DELAY_MS))
          assertTrue(millisElapsedSince(startTime) >= timeoutMillis)
        case `awaitUntil` =>
          // We shouldn't assume that nanoTime and currentTimeMillis
          // use the same time source, so don't use nanoTime here.
          val delayedDate: Date = this.delayedDate(timeoutMillis)
          assertFalse(c.awaitUntil(this.delayedDate(timeoutMillis)))
          assertTrue(new java.util.Date().getTime() >= delayedDate.getTime())
        case _ => throw new UnsupportedOperationException()
      }
    catch { case ie: InterruptedException => threadUnexpectedException(ie) }
  }

  /** isHeldExclusively is false upon construction
   */
  @Test def testIsHeldExclusively(): Unit =
    assertFalse(new Mutex().isHeldExclusively())

  /** acquiring released sync succeeds
   */
  @Test def testAcquire(): Unit = {
    val sync = new Mutex()
    sync.acquire()
    assertTrue(sync.isHeldExclusively())
    sync.release()
    assertFalse(sync.isHeldExclusively())
  }

  /** tryAcquire on a released sync succeeds
   */
  @Test def testTryAcquire(): Unit = {
    val sync = new Mutex()
    assertTrue(sync.tryAcquire())
    assertTrue(sync.isHeldExclusively())
    sync.release()
    assertFalse(sync.isHeldExclusively())
  }

  /** hasQueuedThreads reports whether there are waiting threads
   */
  @Test def testHasQueuedThreads(): Unit = {
    val sync = new Mutex()
    assertFalse(sync.hasQueuedThreads())
    sync.acquire()
    val t1 = newStartedThread(new InterruptedSyncRunnable(sync))
    waitForQueuedThread(sync, t1)
    assertTrue(sync.hasQueuedThreads())
    val t2 = newStartedThread(new InterruptibleSyncRunnable(sync))
    waitForQueuedThread(sync, t2)
    assertTrue(sync.hasQueuedThreads())
    t1.interrupt()
    awaitTermination(t1)
    assertTrue(sync.hasQueuedThreads())
    sync.release()
    awaitTermination(t2)
    assertFalse(sync.hasQueuedThreads())
  }

  /** isQueued(null) throws NullPointerException
   */
  @Test def testIsQueuedNPE(): Unit = {
    val sync = new Mutex()
    try {
      sync.isQueued(null)
      shouldThrow()
    } catch { case success: NullPointerException => () }
  }

  /** isQueued reports whether a thread is queued
   */
  @Test def testIsQueued(): Unit = {
    val sync = new Mutex()
    val t1 = new Thread(new InterruptedSyncRunnable(sync))
    val t2 = new Thread(new InterruptibleSyncRunnable(sync))
    assertFalse(sync.isQueued(t1))
    assertFalse(sync.isQueued(t2))
    sync.acquire()
    t1.start()
    waitForQueuedThread(sync, t1)
    assertTrue(sync.isQueued(t1))
    assertFalse(sync.isQueued(t2))
    t2.start()
    waitForQueuedThread(sync, t2)
    assertTrue(sync.isQueued(t1))
    assertTrue(sync.isQueued(t2))
    t1.interrupt()
    awaitTermination(t1)
    assertFalse(sync.isQueued(t1))
    assertTrue(sync.isQueued(t2))
    sync.release()
    awaitTermination(t2)
    assertFalse(sync.isQueued(t1))
    assertFalse(sync.isQueued(t2))
  }

  /** getFirstQueuedThread returns first waiting thread or null if none
   */
  @Test def testGetFirstQueuedThread(): Unit = {
    val sync = new Mutex()
    assertNull(sync.getFirstQueuedThread())
    sync.acquire()
    val t1 = newStartedThread(new InterruptedSyncRunnable(sync))
    waitForQueuedThread(sync, t1)
    assertEquals(t1, sync.getFirstQueuedThread())
    val t2 = newStartedThread(new InterruptibleSyncRunnable(sync))
    waitForQueuedThread(sync, t2)
    assertEquals(t1, sync.getFirstQueuedThread())
    t1.interrupt()
    awaitTermination(t1)
    assertEquals(t2, sync.getFirstQueuedThread())
    sync.release()
    awaitTermination(t2)
    assertNull(sync.getFirstQueuedThread())
  }

  /** hasContended reports false if no thread has ever blocked, else true
   */
  @Test def testHasContended(): Unit = {
    val sync = new Mutex()
    assertFalse(sync.hasContended())
    sync.acquire()
    assertFalse(sync.hasContended())
    val t1 = newStartedThread(new InterruptedSyncRunnable(sync))
    waitForQueuedThread(sync, t1)
    assertTrue(sync.hasContended())
    val t2 = newStartedThread(new InterruptibleSyncRunnable(sync))
    waitForQueuedThread(sync, t2)
    assertTrue(sync.hasContended())
    t1.interrupt()
    awaitTermination(t1)
    assertTrue(sync.hasContended())
    sync.release()
    awaitTermination(t2)
    assertTrue(sync.hasContended())
  }

  /** getQueuedThreads returns all waiting threads
   */
  @Test def testGetQueuedThreads(): Unit = {
    val sync = new Mutex()
    val t1 = new Thread(new InterruptedSyncRunnable(sync))
    val t2 = new Thread(new InterruptibleSyncRunnable(sync))
    assertHasExclusiveQueuedThreads(sync, NO_THREADS: _*)
    sync.acquire()
    assertHasExclusiveQueuedThreads(sync, NO_THREADS: _*)
    t1.start()
    waitForQueuedThread(sync, t1)
    assertHasExclusiveQueuedThreads(sync, t1)
    assertTrue(sync.getQueuedThreads().contains(t1))
    assertFalse(sync.getQueuedThreads().contains(t2))
    t2.start()
    waitForQueuedThread(sync, t2)
    assertHasExclusiveQueuedThreads(sync, t1, t2)
    assertTrue(sync.getQueuedThreads().contains(t1))
    assertTrue(sync.getQueuedThreads().contains(t2))
    t1.interrupt()
    awaitTermination(t1)
    assertHasExclusiveQueuedThreads(sync, t2)
    sync.release()
    awaitTermination(t2)
    assertHasExclusiveQueuedThreads(sync, NO_THREADS: _*)
  }

  /** getExclusiveQueuedThreads returns all exclusive waiting threads
   */
  @Test def testGetExclusiveQueuedThreads(): Unit = {
    val sync = new Mutex()
    val t1 = new Thread(new InterruptedSyncRunnable(sync))
    val t2 = new Thread(new InterruptibleSyncRunnable(sync))
    assertHasExclusiveQueuedThreads(sync, NO_THREADS: _*)
    sync.acquire()
    assertHasExclusiveQueuedThreads(sync, NO_THREADS: _*)
    t1.start()
    waitForQueuedThread(sync, t1)
    assertHasExclusiveQueuedThreads(sync, t1)
    assertTrue(sync.getExclusiveQueuedThreads().contains(t1))
    assertFalse(sync.getExclusiveQueuedThreads().contains(t2))
    t2.start()
    waitForQueuedThread(sync, t2)
    assertHasExclusiveQueuedThreads(sync, t1, t2)
    assertTrue(sync.getExclusiveQueuedThreads().contains(t1))
    assertTrue(sync.getExclusiveQueuedThreads().contains(t2))
    t1.interrupt()
    awaitTermination(t1)
    assertHasExclusiveQueuedThreads(sync, t2)
    sync.release()
    awaitTermination(t2)
    assertHasExclusiveQueuedThreads(sync, NO_THREADS: _*)
  }

  /** getSharedQueuedThreads does not include exclusively waiting threads
   */
  @Test def testGetSharedQueuedThreads_Exclusive(): Unit = {
    val sync = new Mutex()
    assertTrue(sync.getSharedQueuedThreads().isEmpty())
    sync.acquire()
    assertTrue(sync.getSharedQueuedThreads().isEmpty())
    val t1 = newStartedThread(new InterruptedSyncRunnable(sync))
    waitForQueuedThread(sync, t1)
    assertTrue(sync.getSharedQueuedThreads().isEmpty())
    val t2 = newStartedThread(new InterruptibleSyncRunnable(sync))
    waitForQueuedThread(sync, t2)
    assertTrue(sync.getSharedQueuedThreads().isEmpty())
    t1.interrupt()
    awaitTermination(t1)
    assertTrue(sync.getSharedQueuedThreads().isEmpty())
    sync.release()
    awaitTermination(t2)
    assertTrue(sync.getSharedQueuedThreads().isEmpty())
  }

  /** getSharedQueuedThreads returns all shared waiting threads
   */
  @Test def testGetSharedQueuedThreads_Shared(): Unit = {
    val l = new BooleanLatch()
    assertHasSharedQueuedThreads(l, NO_THREADS: _*)
    val t1 = newStartedThread(new CheckedInterruptedRunnable() {
      def realRun(): Unit = {
        l.acquireSharedInterruptibly(0)
      }
    })
    waitForQueuedThread(l, t1)
    assertHasSharedQueuedThreads(l, t1)
    val t2 = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        l.acquireSharedInterruptibly(0)
      }
    })
    waitForQueuedThread(l, t2)
    assertHasSharedQueuedThreads(l, t1, t2)
    t1.interrupt()
    awaitTermination(t1)
    assertHasSharedQueuedThreads(l, t2)
    assertTrue(l.releaseShared(0))
    awaitTermination(t2)
    assertHasSharedQueuedThreads(l, NO_THREADS: _*)
  }

  /** tryAcquireNanos is interruptible
   */
  @Test def testTryAcquireNanos_Interruptible(): Unit = {
    val sync = new Mutex()
    sync.acquire()
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      def realRun(): Unit = {
        sync.tryAcquireNanos(MILLISECONDS.toNanos(2 * LONG_DELAY_MS))
      }
    })

    waitForQueuedThread(sync, t)
    t.interrupt()
    awaitTermination(t)
  }

  /** tryAcquire on exclusively held sync fails
   */
  @Test def testTryAcquireWhenSynced(): Unit = {
    val sync = new Mutex()
    sync.acquire()
    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        assertFalse(sync.tryAcquire())
      }
    })

    awaitTermination(t)
    sync.release()
  }

  /** tryAcquireNanos on an exclusively held sync times out
   */
  @Test def testAcquireNanos_Timeout(): Unit = {
    val sync = new Mutex()
    sync.acquire()
    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        val startTime = System.nanoTime()
        val nanos = MILLISECONDS.toNanos(timeoutMillis())
        assertFalse(sync.tryAcquireNanos(nanos))
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
      }
    })

    awaitTermination(t)
    sync.release()
  }

  /** getState is true when acquired and false when not
   */
  @Test def testGetState(): Unit = {
    val sync = new Mutex()
    sync.acquire()
    assertTrue(sync.isHeldExclusively())
    sync.release()
    assertFalse(sync.isHeldExclusively())

    val acquired = new BooleanLatch()
    val done = new BooleanLatch()
    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        sync.acquire()
        assertTrue(acquired.releaseShared(0))
        done.acquireShared(0)
        sync.release()
      }
    })

    acquired.acquireShared(0)
    assertTrue(sync.isHeldExclusively())
    assertTrue(done.releaseShared(0))
    awaitTermination(t)
    assertFalse(sync.isHeldExclusively())
  }

  /** acquireInterruptibly succeeds when released, else is interruptible
   */
  @Test def testAcquireInterruptibly() = {
    val sync = new Mutex()
    val threadStarted = new BooleanLatch()
    sync.acquireInterruptibly()
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      def realRun(): Unit = {
        assertTrue(threadStarted.releaseShared(0))
        sync.acquireInterruptibly()
      }
    })

    threadStarted.acquireShared(0)
    waitForQueuedThread(sync, t)
    t.interrupt()
    awaitTermination(t)
    assertTrue(sync.isHeldExclusively())
  }

  /** owns is true for a condition created by sync else false
   */
  @Test def testOwns(): Unit = {
    val sync = new Mutex()
    val c = sync.newCondition()
    val sync2 = new Mutex()
    assertTrue(sync.owns(c))
    assertFalse(sync2.owns(c))
  }

  /** Calling await without holding sync throws IllegalMonitorStateException
   */
  @Test def testAwait_IMSE(): Unit = {
    val sync = new Mutex()
    val c = sync.newCondition()
    for (awaitMethod <- AwaitMethod.values) {
      val startTime = System.nanoTime()
      try {
        await(c, awaitMethod)
        shouldThrow()
      } catch {
        case success: IllegalMonitorStateException => ()
        case e: InterruptedException => threadUnexpectedException(e)
      }
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    }
  }

  /** Calling signal without holding sync throws IllegalMonitorStateException
   */
  @Test def testSignal_IMSE(): Unit = {
    val sync = new Mutex()
    val c = sync.newCondition()
    try {
      c.signal()
      shouldThrow()
    } catch { case success: IllegalMonitorStateException => () }
    assertHasWaitersUnlocked(sync, c, NO_THREADS: _*)
  }

  /** Calling signalAll without holding sync throws IllegalMonitorStateException
   */
  @Test def testSignalAll_IMSE(): Unit = {
    val sync = new Mutex()
    val c = sync.newCondition()
    try {
      c.signalAll()
      shouldThrow()
    } catch { case success: IllegalMonitorStateException => () }
  }

  /** await/awaitNanos/awaitUntil without a signal times out
   */
  @Test def testAwaitTimed_Timeout(): Unit = testAwait_Timeout(
    AwaitMethod.awaitTimed
  )
  @Test def testAwaitNanos_Timeout(): Unit = testAwait_Timeout(
    AwaitMethod.awaitNanos
  )
  @Test def testAwaitUntil_Timeout(): Unit = testAwait_Timeout(
    AwaitMethod.awaitUntil
  )
  private def testAwait_Timeout(awaitMethod: AwaitMethod) = {
    val sync = new Mutex()
    val c = sync.newCondition()
    sync.acquire()
    assertAwaitTimesOut(c, awaitMethod)
    sync.release()
  }

  /** await/awaitNanos/awaitUntil returns when signalled
   */
  @Test def testSignal_await(): Unit = testSignal(AwaitMethod.await)
  @Test def testSignal_awaitTimed(): Unit = testSignal(AwaitMethod.awaitTimed)
  @Test def testSignal_awaitNanos(): Unit = testSignal(AwaitMethod.awaitNanos)
  @Test def testSignal_awaitUntil(): Unit = testSignal(AwaitMethod.awaitUntil)

  private def testSignal(awaitMethod: AwaitMethod) = {
    val sync = new Mutex()
    val c = sync.newCondition()
    val acquired = new BooleanLatch()
    val t = newStartedThread(new CheckedRunnable() {
      protected def realRun() = {
        sync.acquire()
        assertTrue(acquired.releaseShared(0))
        await(c, awaitMethod)
        sync.release()
      }
    })

    acquired.acquireShared(0)
    sync.acquire()
    assertHasWaitersLocked(sync, c, t)
    assertHasExclusiveQueuedThreads(sync, NO_THREADS: _*)
    c.signal()
    assertHasWaitersLocked(sync, c, NO_THREADS: _*)
    assertHasExclusiveQueuedThreads(sync, t)
    sync.release()
    awaitTermination(t)
  }

  /** hasWaiters(null) throws NullPointerException
   */
  @Test def testHasWaitersNPE(): Unit = {
    val sync = new Mutex()
    try {
      sync.hasWaiters(null)
      shouldThrow()
    } catch { case success: NullPointerException => () }
  }

  /** getWaitQueueLength(null) throws NullPointerException
   */
  @Test def testGetWaitQueueLengthNPE(): Unit = {
    val sync = new Mutex()
    try {
      sync.getWaitQueueLength(null)
      shouldThrow()
    } catch { case success: NullPointerException => () }
  }

  /** getWaitingThreads(null) throws NullPointerException
   */
  @Test def testGetWaitingThreadsNPE(): Unit = {
    val sync = new Mutex()
    try {
      sync.getWaitingThreads(null)
      shouldThrow()
    } catch { case success: NullPointerException => () }
  }

  /** hasWaiters throws IllegalArgumentException if not owned
   */
  @Test def testHasWaitersIAE(): Unit = {
    val sync = new Mutex()
    val c = sync.newCondition()
    val sync2 = new Mutex()
    try {
      sync2.hasWaiters(c)
      shouldThrow()
    } catch { case success: IllegalArgumentException => () }
    assertHasWaitersUnlocked(sync, c, NO_THREADS: _*)
  }

  /** hasWaiters throws IllegalMonitorStateException if not synced
   */
  @Test def testHasWaitersIMSE(): Unit = {
    val sync = new Mutex()
    val c = sync.newCondition()
    try {
      sync.hasWaiters(c)
      shouldThrow()
    } catch { case success: IllegalMonitorStateException => () }
    assertHasWaitersUnlocked(sync, c, NO_THREADS: _*)
  }

  /** getWaitQueueLength throws IllegalArgumentException if not owned
   */
  @Test def testGetWaitQueueLengthIAE(): Unit = {
    val sync = new Mutex()
    val c = sync.newCondition()
    val sync2 = new Mutex()
    try {
      sync2.getWaitQueueLength(c)
      shouldThrow()
    } catch { case success: IllegalArgumentException => () }
    assertHasWaitersUnlocked(sync, c, NO_THREADS: _*)
  }

  /** getWaitQueueLength throws IllegalMonitorStateException if not synced
   */
  @Test def testGetWaitQueueLengthIMSE(): Unit = {
    val sync = new Mutex()
    val c = sync.newCondition()
    try {
      sync.getWaitQueueLength(c)
      shouldThrow()
    } catch { case success: IllegalMonitorStateException => () }
    assertHasWaitersUnlocked(sync, c, NO_THREADS: _*)
  }

  /** getWaitingThreads throws IllegalArgumentException if not owned
   */
  @Test def testGetWaitingThreadsIAE(): Unit = {
    val sync = new Mutex()
    val c = sync.newCondition()
    val sync2 = new Mutex()
    try {
      sync2.getWaitingThreads(c)
      shouldThrow()
    } catch { case success: IllegalArgumentException => () }
    assertHasWaitersUnlocked(sync, c, NO_THREADS: _*)
  }

  /** getWaitingThreads throws IllegalMonitorStateException if not synced
   */
  @Test def testGetWaitingThreadsIMSE(): Unit = {
    val sync = new Mutex()
    val c = sync.newCondition()
    try {
      sync.getWaitingThreads(c)
      shouldThrow()
    } catch { case success: IllegalMonitorStateException => () }
    assertHasWaitersUnlocked(sync, c, NO_THREADS: _*)
  }

  /** hasWaiters returns true when a thread is waiting, else false
   */
  @Test def testHasWaiters(): Unit = {
    val sync = new Mutex()
    val c = sync.newCondition()
    val acquired = new BooleanLatch()
    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        sync.acquire()
        assertHasWaitersLocked(sync, c, NO_THREADS: _*)
        assertFalse(sync.hasWaiters(c))
        assertTrue(acquired.releaseShared(0))
        c.await()
        sync.release()
      }
    })

    acquired.acquireShared(0)
    sync.acquire()
    assertHasWaitersLocked(sync, c, t)
    assertHasExclusiveQueuedThreads(sync, NO_THREADS: _*)
    assertTrue(sync.hasWaiters(c))
    c.signal()
    assertHasWaitersLocked(sync, c, NO_THREADS: _*)
    assertHasExclusiveQueuedThreads(sync, t)
    assertFalse(sync.hasWaiters(c))
    sync.release()

    awaitTermination(t)
    assertHasWaitersUnlocked(sync, c, NO_THREADS: _*)
  }

  /** getWaitQueueLength returns number of waiting threads
   */
  @Test def testGetWaitQueueLength(): Unit = {
    val sync = new Mutex()
    val c = sync.newCondition()
    val acquired1 = new BooleanLatch()
    val acquired2 = new BooleanLatch()
    val t1 = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        sync.acquire()
        assertHasWaitersLocked(sync, c, NO_THREADS: _*)
        assertEquals(0, sync.getWaitQueueLength(c))
        assertTrue(acquired1.releaseShared(0))
        c.await()
        sync.release()
      }
    })
    acquired1.acquireShared(0)
    sync.acquire()
    assertHasWaitersLocked(sync, c, t1)
    assertEquals(1, sync.getWaitQueueLength(c))
    sync.release()

    val t2 = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        sync.acquire()
        assertHasWaitersLocked(sync, c, t1)
        assertEquals(1, sync.getWaitQueueLength(c))
        assertTrue(acquired2.releaseShared(0))
        c.await()
        sync.release()
      }
    })
    acquired2.acquireShared(0)
    sync.acquire()
    assertHasWaitersLocked(sync, c, t1, t2)
    assertHasExclusiveQueuedThreads(sync, NO_THREADS: _*)
    assertEquals(2, sync.getWaitQueueLength(c))
    c.signalAll()
    assertHasWaitersLocked(sync, c, NO_THREADS: _*)
    assertHasExclusiveQueuedThreads(sync, t1, t2)
    assertEquals(0, sync.getWaitQueueLength(c))
    sync.release()

    awaitTermination(t1)
    awaitTermination(t2)
    assertHasWaitersUnlocked(sync, c, NO_THREADS: _*)
  }

  /** getWaitingThreads returns only and all waiting threads
   */
  @Test def testGetWaitingThreads(): Unit = {
    val sync = new Mutex()
    val c = sync.newCondition()
    val acquired1 = new BooleanLatch()
    val acquired2 = new BooleanLatch()
    val t1 = new Thread(new CheckedRunnable() {
      def realRun(): Unit = {
        sync.acquire()
        assertHasWaitersLocked(sync, c, NO_THREADS: _*)
        assertTrue(sync.getWaitingThreads(c).isEmpty())
        assertTrue(acquired1.releaseShared(0))
        c.await()
        sync.release()
      }
    })

    val t2 = new Thread(new CheckedRunnable() {
      def realRun(): Unit = {
        sync.acquire()
        assertHasWaitersLocked(sync, c, t1)
        assertTrue(sync.getWaitingThreads(c).contains(t1))
        assertFalse(sync.getWaitingThreads(c).isEmpty())
        assertEquals(1, sync.getWaitingThreads(c).size())
        assertTrue(acquired2.releaseShared(0))
        c.await()
        sync.release()
      }
    })

    sync.acquire()
    assertHasWaitersLocked(sync, c, NO_THREADS: _*)
    assertFalse(sync.getWaitingThreads(c).contains(t1))
    assertFalse(sync.getWaitingThreads(c).contains(t2))
    assertTrue(sync.getWaitingThreads(c).isEmpty())
    assertEquals(0, sync.getWaitingThreads(c).size())
    sync.release()

    t1.start()
    acquired1.acquireShared(0)
    sync.acquire()
    assertHasWaitersLocked(sync, c, t1)
    assertTrue(sync.getWaitingThreads(c).contains(t1))
    assertFalse(sync.getWaitingThreads(c).contains(t2))
    assertFalse(sync.getWaitingThreads(c).isEmpty())
    assertEquals(1, sync.getWaitingThreads(c).size())
    sync.release()

    t2.start()
    acquired2.acquireShared(0)
    sync.acquire()
    assertHasWaitersLocked(sync, c, t1, t2)
    assertHasExclusiveQueuedThreads(sync, NO_THREADS: _*)
    assertTrue(sync.getWaitingThreads(c).contains(t1))
    assertTrue(sync.getWaitingThreads(c).contains(t2))
    assertFalse(sync.getWaitingThreads(c).isEmpty())
    assertEquals(2, sync.getWaitingThreads(c).size())
    c.signalAll()
    assertHasWaitersLocked(sync, c, NO_THREADS: _*)
    assertHasExclusiveQueuedThreads(sync, t1, t2)
    assertFalse(sync.getWaitingThreads(c).contains(t1))
    assertFalse(sync.getWaitingThreads(c).contains(t2))
    assertTrue(sync.getWaitingThreads(c).isEmpty())
    assertEquals(0, sync.getWaitingThreads(c).size())
    sync.release()

    awaitTermination(t1)
    awaitTermination(t2)
    assertHasWaitersUnlocked(sync, c, NO_THREADS: _*)
  }

  /** awaitUninterruptibly is uninterruptible
   */
  @Test def testAwaitUninterruptibly(): Unit = {
    val sync = new Mutex()
    val condition = sync.newCondition()
    val pleaseInterrupt = new BooleanLatch()
    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        sync.acquire()
        assertTrue(pleaseInterrupt.releaseShared(0))
        condition.awaitUninterruptibly()
        assertTrue(Thread.interrupted())
        assertHasWaitersLocked(sync, condition, NO_THREADS: _*)
        sync.release()
      }
    })

    pleaseInterrupt.acquireShared(0)
    sync.acquire()
    assertHasWaitersLocked(sync, condition, t)
    sync.release()
    t.interrupt()
    assertHasWaitersUnlocked(sync, condition, t)
    assertThreadBlocks(t, Thread.State.WAITING)
    sync.acquire()
    assertHasWaitersLocked(sync, condition, t)
    assertHasExclusiveQueuedThreads(sync, NO_THREADS: _*)
    condition.signal()
    assertHasWaitersLocked(sync, condition, NO_THREADS: _*)
    assertHasExclusiveQueuedThreads(sync, t)
    sync.release()
    awaitTermination(t)
  }

  /** await/awaitNanos/awaitUntil is interruptible
   */
  @Test def testInterruptible_await(): Unit = testInterruptible(
    AwaitMethod.await
  )
  @Test def testInterruptible_awaitTimed(): Unit = testInterruptible(
    AwaitMethod.awaitTimed
  )
  @Test def testInterruptible_awaitNanos(): Unit = testInterruptible(
    AwaitMethod.awaitNanos
  )
  @Test def testInterruptible_awaitUntil(): Unit = testInterruptible(
    AwaitMethod.awaitUntil
  )
  private def testInterruptible(awaitMethod: AwaitMethod) = {
    val sync = new Mutex()
    val c = sync.newCondition()
    val pleaseInterrupt = new BooleanLatch()
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      protected def realRun() = {
        sync.acquire()
        assertTrue(pleaseInterrupt.releaseShared(0))
        await(c, awaitMethod)
      }
    })

    pleaseInterrupt.acquireShared(0)
    t.interrupt()
    awaitTermination(t)
  }

  /** signalAll wakes up all threads
   */
  @Test def testSignalAll_await(): Unit = testSignalAll(AwaitMethod.await)
  @Test def testSignalAll_awaitTimed(): Unit = testSignalAll(
    AwaitMethod.awaitTimed
  )
  @Test def testSignalAll_awaitNanos(): Unit = testSignalAll(
    AwaitMethod.awaitNanos
  )
  @Test def testSignalAll_awaitUntil(): Unit = testSignalAll(
    AwaitMethod.awaitUntil
  )
  private def testSignalAll(awaitMethod: AwaitMethod) = {
    val sync = new Mutex()
    val c = sync.newCondition()
    val acquired1 = new BooleanLatch()
    val acquired2 = new BooleanLatch()
    val t1 = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        sync.acquire()
        acquired1.releaseShared(0)
        await(c, awaitMethod)
        sync.release()
      }
    })

    val t2 = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        sync.acquire()
        acquired2.releaseShared(0)
        await(c, awaitMethod)
        sync.release()
      }
    })

    acquired1.acquireShared(0)
    acquired2.acquireShared(0)
    sync.acquire()
    assertHasWaitersLocked(sync, c, t1, t2)
    assertHasExclusiveQueuedThreads(sync, NO_THREADS: _*)
    c.signalAll()
    assertHasWaitersLocked(sync, c, NO_THREADS: _*)
    assertHasExclusiveQueuedThreads(sync, t1, t2)
    sync.release()
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** toString indicates current state
   */
  @Test def testToString(): Unit = {
    val sync = new Mutex()
    assertTrue(sync.toString().contains("State = " + Mutex.UNLOCKED))
    sync.acquire()
    assertTrue(sync.toString().contains("State = " + Mutex.LOCKED))
  }

  /** A serialized AQS deserializes with current state, but no queued threads
   */
  @Ignore("No ObjectInputStreams in Scala Native") @Test def testSerialization()
      : Unit = {
    // val sync = new Mutex()
    // assertFalse(serialClone(sync).isHeldExclusively())
    // sync.acquire()
    // val t = newStartedThread(new InterruptedSyncRunnable(sync))
    // waitForQueuedThread(sync, t)
    // assertTrue(sync.isHeldExclusively())

    // val clone = serialClone(sync)
    // assertTrue(clone.isHeldExclusively())
    // assertHasExclusiveQueuedThreads(sync, t)
    // assertHasExclusiveQueuedThreads(clone, NO_THREADS: _*)
    // t.interrupt()
    // awaitTermination(t)
    // sync.release()
    // assertFalse(sync.isHeldExclusively())
    // assertTrue(clone.isHeldExclusively())
    // assertHasExclusiveQueuedThreads(sync, NO_THREADS: _*)
    // assertHasExclusiveQueuedThreads(clone, NO_THREADS: _*)
  }

  /** tryReleaseShared setting state changes getState
   */
  @Test def testGetStateWithReleaseShared(): Unit = {
    val l = new BooleanLatch()
    assertFalse(l.isSignalled())
    assertTrue(l.releaseShared(0))
    assertTrue(l.isSignalled())
  }

  /** releaseShared has no effect when already signalled
   */
  @Test def testReleaseShared(): Unit = {
    val l = new BooleanLatch()
    assertFalse(l.isSignalled())
    assertTrue(l.releaseShared(0))
    assertTrue(l.isSignalled())
    assertTrue(l.releaseShared(0))
    assertTrue(l.isSignalled())
  }

  /** acquireSharedInterruptibly returns after release, but not before
   */
  @Test def testAcquireSharedInterruptibly(): Unit = {
    val l = new BooleanLatch()

    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        assertFalse(l.isSignalled())
        l.acquireSharedInterruptibly(0)
        assertTrue(l.isSignalled())
        l.acquireSharedInterruptibly(0)
        assertTrue(l.isSignalled())
      }
    })

    waitForQueuedThread(l, t)
    assertFalse(l.isSignalled())
    assertThreadBlocks(t, Thread.State.WAITING)
    assertHasSharedQueuedThreads(l, t)
    assertTrue(l.releaseShared(0))
    assertTrue(l.isSignalled())
    awaitTermination(t)
  }

  /** tryAcquireSharedNanos returns after release, but not before
   */
  @Test def testTryAcquireSharedNanos(): Unit = {
    val l = new BooleanLatch()

    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        assertFalse(l.isSignalled())
        val nanos = MILLISECONDS.toNanos(2 * LONG_DELAY_MS)
        assertTrue(l.tryAcquireSharedNanos(0, nanos))
        assertTrue(l.isSignalled())
        assertTrue(l.tryAcquireSharedNanos(0, nanos))
        assertTrue(l.isSignalled())
      }
    })

    waitForQueuedThread(l, t)
    assertFalse(l.isSignalled())
    assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    assertTrue(l.releaseShared(0))
    assertTrue(l.isSignalled())
    awaitTermination(t)
  }

  /** acquireSharedInterruptibly is interruptible
   */
  @Test def testAcquireSharedInterruptibly_Interruptible(): Unit = {
    val l = new BooleanLatch()
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      def realRun(): Unit = {
        assertFalse(l.isSignalled())
        l.acquireSharedInterruptibly(0)
      }
    })

    waitForQueuedThread(l, t)
    assertFalse(l.isSignalled())
    t.interrupt()
    awaitTermination(t)
    assertFalse(l.isSignalled())
  }

  /** tryAcquireSharedNanos is interruptible
   */
  @Test def testTryAcquireSharedNanos_Interruptible(): Unit = {
    val l = new BooleanLatch()
    val t = newStartedThread(new CheckedInterruptedRunnable() {
      def realRun(): Unit = {
        assertFalse(l.isSignalled())
        val nanos = MILLISECONDS.toNanos(2 * LONG_DELAY_MS)
        l.tryAcquireSharedNanos(0, nanos)
      }
    })

    waitForQueuedThread(l, t)
    assertFalse(l.isSignalled())
    t.interrupt()
    awaitTermination(t)
    assertFalse(l.isSignalled())
  }

  /** tryAcquireSharedNanos times out if not released before timeout
   */
  @Test def testTryAcquireSharedNanos_Timeout(): Unit = {
    val l = new BooleanLatch()
    val observedQueued = new BooleanLatch()
    val t = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        assertFalse(l.isSignalled())
        var millis = timeoutMillis()
        while (!observedQueued.isSignalled()) {
          val nanos = MILLISECONDS.toNanos(millis)
          val startTime = System.nanoTime()
          assertFalse(l.tryAcquireSharedNanos(0, nanos))
          assertTrue(millisElapsedSince(startTime) >= millis)
          millis *= 2
        }
        assertFalse(l.isSignalled())
      }
    })

    waitForQueuedThread(l, t)
    observedQueued.releaseShared(0)
    assertFalse(l.isSignalled())
    awaitTermination(t)
    assertFalse(l.isSignalled())
  }

  /** awaitNanos/timed await with 0 wait times out immediately
   */
  @Test def testAwait_Zero() = {
    val sync = new Mutex()
    val c = sync.newCondition()
    sync.acquire()
    assertTrue(c.awaitNanos(0L) <= 0)
    assertFalse(c.await(0L, NANOSECONDS))
    sync.release()
  }

  /** awaitNanos/timed await with maximum negative wait times does not underflow
   */
  @Test def testAwait_NegativeInfinity() = {
    assumeFalse(
      "Fails due to bug in the JVM",
      Platform.executingInJVMOnJDK8OrLower
    )
    val sync = new Mutex()
    val c = sync.newCondition()
    sync.acquire()
    assertTrue(c.awaitNanos(java.lang.Long.MIN_VALUE) <= 0)
    assertFalse(c.await(java.lang.Long.MIN_VALUE, NANOSECONDS))
    sync.release()
  }

  /** JDK-8191483: AbstractQueuedLongSynchronizer cancel/cancel race ant */
  @Test def testCancelCancelRace() = {
    assumeFalse(
      "Fails due to bug in the JVM",
      Platform.executingInJVMOnJDK8OrLower
    )
    class Sync extends AbstractQueuedLongSynchronizer {
      override def tryAcquire(acquires: Long): Boolean =
        !hasQueuedPredecessors() && compareAndSetState(0, 1)
      override protected def tryRelease(releases: Long): Boolean =
        compareAndSetState(1, 0)
    }

    val s = new Sync()
    s.acquire(1) // acquire to force other threads to enqueue

    // try to trigger double cancel race with two background threads
    val threads = new ArrayList[Thread]()
    val failedAcquire: Runnable = () => {
      try {
        s.acquireInterruptibly(1)
        shouldThrow()
      } catch { case success: InterruptedException => () }
    }
    for (i <- 0 until 2) {
      val thread = new Thread(failedAcquire)
      thread.start()
      threads.add(thread)
    }
    Thread.sleep(100)
    threads.forEach(_.interrupt())
    threads.forEach(awaitTermination(_))
    s.release(1)

    // no one holds lock now, we should be able to acquire
    if (!s.tryAcquire(1))
      throw new RuntimeException(
        String.format(
          "Broken: hasQueuedPredecessors=%s hasQueuedThreads=%s queueLength=%d firstQueuedThread=%s",
          s.hasQueuedPredecessors(): java.lang.Boolean,
          s.hasQueuedThreads(): java.lang.Boolean,
          s.getQueueLength(): Integer,
          s.getFirstQueuedThread()
        )
      )
  }

  /** Tests scenario for JDK-8191937: Lost interrupt in
   *  AbstractQueuedLongSynchronizer when tryAcquire methods throw ant
   *  -Djsr166.tckTestClass=AbstractQueuedLongSynchronizerTest
   *  -Djsr166.methodFilter=testInterruptedFailingAcquire
   *  -Djsr166.runsPerTest=10000 tck
   */
  @Test def testInterruptedFailingAcquire() = {
    assumeFalse(
      "Fails due to bug in the JVM",
      Platform.executingInJVMOnJDK8OrLower
    )
    class PleaseThrow extends RuntimeException {}
    val ex = new PleaseThrow()
    val thrown = new AtomicBoolean()

    // A synchronizer only offering a choice of failure modes
    class Sync extends AbstractQueuedLongSynchronizer {
      @volatile var pleaseThrow: Boolean = false
      def maybeThrow() = {
        if (pleaseThrow) {
          // assert: tryAcquire methods can throw at most once
          if (!thrown.compareAndSet(false, true))
            throw new AssertionError()
          throw ex
        }
      }

      override protected def tryAcquire(ignored: Long): Boolean = {
        maybeThrow()
        false
      }
      override protected def tryAcquireShared(ignored: Long): Long = {
        maybeThrow()
        -1
      }
      override def tryRelease(ignored: Long) = true
      override def tryReleaseShared(ignored: Long) = true
    }

    val s = new Sync()
    val acquireInterruptibly = randomBoolean()
    val uninterruptibleAcquireActions = Array[Action](
      () => s.acquire(1),
      () => s.acquireShared(1)
    )
    val nanosTimeout = MILLISECONDS.toNanos(2 * LONG_DELAY_MS)
    val interruptibleAcquireActions = Array[Action](
      () => s.acquireInterruptibly(1),
      () => s.acquireSharedInterruptibly(1),
      () => s.tryAcquireNanos(1, nanosTimeout),
      () => s.tryAcquireSharedNanos(1, nanosTimeout)
    )
    val releaseActions = Array[Action](
      () => s.release(1),
      () => s.releaseShared(1)
    )
    val acquireAction: Action =
      if (acquireInterruptibly) chooseRandomly(interruptibleAcquireActions)
      else chooseRandomly(uninterruptibleAcquireActions)
    val releaseAction = chooseRandomly(releaseActions)

    // From os_posix.cpp:
    //
    // NOTE that since there is no "lock" around the interrupt and
    // is_interrupted operations, there is the possibility that the
    // interrupted flag (in osThread) will be "false" but that the
    // low-level events will be in the signaled state. This is
    // intentional. The effect of this is that Object.wait() and
    // LockSupport.park() will appear to have a spurious wakeup, which
    // is allowed and not harmful, and the possibility is so rare that
    // it is not worth the added complexity to add yet another lock.
    val thread = newStartedThread(new CheckedRunnable() {
      def realRun(): Unit = {
        try {
          acquireAction.run()
          shouldThrow()
        } catch {
          case possible: InterruptedException =>
            assertTrue(acquireInterruptibly)
            assertFalse(Thread.interrupted())
          case possible: PleaseThrow => awaitInterrupted()
        }
      }
    })
    Breaks.breakable {
      lazy val startTime = System.nanoTime()
      while (true) {
        waitForThreadToEnterWaitState(thread)
        if (s.getFirstQueuedThread() == thread
            && s.hasQueuedPredecessors()
            && s.hasQueuedThreads()
            && s.getQueueLength() == 1
            && s.hasContended()) Breaks.break()
        else if (millisElapsedSince(startTime) > LONG_DELAY_MS)
          fail(
            "timed out waiting for AQS state: "
              + "thread state=" + thread.getState()
              + ", queued threads=" + s.getQueuedThreads()
          )
        Thread.`yield`()
      }
    }

    s.pleaseThrow = true
    // release and interrupt, in random order
    if (randomBoolean()) {
      thread.interrupt()
      releaseAction.run()
    } else {
      releaseAction.run()
      thread.interrupt()
    }
    awaitTermination(thread)

    if (!acquireInterruptibly)
      assertTrue(thrown.get())

    assertNull(s.getFirstQueuedThread())
    assertFalse(s.hasQueuedPredecessors())
    assertFalse(s.hasQueuedThreads())
    assertEquals(0, s.getQueueLength())
    assertTrue(s.getQueuedThreads().isEmpty())
    assertTrue(s.hasContended())
  }

}
