/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */
package org.scalanative.testsuite.javalib.util.concurrent

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{Test, Ignore}
import org.scalanative.testsuite.utils.Platform
import JSR166Test._

import java.util.concurrent.TimeUnit._
import java.util
import java.util._
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.FutureTask

object FutureTaskTest {

  /** Subclass to expose protected methods
   */
  object PublicFutureTask {
    def apply[T](callable: Callable[T]) = new PublicCallableTask(callable)
    def apply(runnable: Runnable, result: AnyRef = seven) =
      new PublicRunnableTask(runnable, result)
  }

  sealed trait PublicFutureTask { self: FutureTask[AnyRef] =>
    val runCounter: AtomicInteger
    final protected val doneCounter = new AtomicInteger(0)
    final protected val runAndResetCounter = new AtomicInteger(0)
    final protected val setCounter = new AtomicInteger(0)
    final protected val setExceptionCounter = new AtomicInteger(0)
    def runCount(): Int = this.runCounter.get()
    def doneCount(): Int = this.doneCounter.get()
    def runAndResetCount(): Int = runAndResetCounter.get()
    def setCount(): Int = setCounter.get()
    def setExceptionCount(): Int = setExceptionCounter.get()
    def doDone(): Unit
    def doRunAndReset(): Boolean
    def doSet(x: AnyRef): Unit
    def doSetException(t: Throwable): Unit
  }

  final class PublicRunnableTask(
      runnable: Runnable,
      result: AnyRef,
      val runCounter: AtomicInteger = new AtomicInteger(0)
  ) extends FutureTask(
        new Runnable() {
          override def run(): Unit = {
            runCounter.getAndIncrement()
            runnable.run()
          }
        },
        result
      )
      with PublicFutureTask {
    def doDone(): Unit = done()
    override protected def done(): Unit = {
      assertTrue(isDone())
      doneCounter.incrementAndGet()
      super.done()
    }

    def doRunAndReset(): Boolean = runAndReset()
    override protected def runAndReset(): Boolean = {
      runAndResetCounter.incrementAndGet()
      super.runAndReset()
    }

    def doSet(x: AnyRef): Unit = set(x)
    override protected def set(x: AnyRef): Unit = {
      setCounter.incrementAndGet()
      super.set(x)
    }

    def doSetException(t: Throwable): Unit = setException(t)
    override protected def setException(t: Throwable): Unit = {
      setExceptionCounter.incrementAndGet()
      super.setException(t)
    }

  }

  final class PublicCallableTask[T](
      callable: Callable[T],
      val runCounter: AtomicInteger = new AtomicInteger(0)
  ) extends FutureTask[AnyRef](
        new Callable[AnyRef]() {
          override def call(): AnyRef = {
            runCounter.getAndIncrement()
            callable.call().asInstanceOf[AnyRef]
          }
        }
      )
      with PublicFutureTask {
    def doDone(): Unit = done()
    override protected def done(): Unit = {
      assertTrue(isDone())
      doneCounter.incrementAndGet()
      super.done()
    }

    def doRunAndReset(): Boolean = runAndReset()
    override protected def runAndReset(): Boolean = {
      runAndResetCounter.incrementAndGet()
      super.runAndReset()
    }

    def doSet(x: AnyRef): Unit = set(x)
    override protected def set(x: AnyRef): Unit = {
      setCounter.incrementAndGet()
      super.set(x)
    }

    def doSetException(t: Throwable): Unit = setException(t)
    override protected def setException(t: Throwable): Unit = {
      setExceptionCounter.incrementAndGet()
      super.setException(t)
    }
  }
}

class FutureTaskTest extends JSR166Test {
  type PublicFutureTask = FutureTask[AnyRef]
    with FutureTaskTest.PublicFutureTask

  def checkIsDone[T <: AnyRef](f: Future[T]): Unit = {
    assertTrue(f.isDone())
    assertFalse(f.cancel(false))
    assertFalse(f.cancel(true))
    f match {
      case pf: PublicFutureTask @unchecked =>
        assertEquals(1, pf.doneCount())
        assertFalse(pf.doRunAndReset())
        assertEquals(1, pf.doneCount())
        var r = null: AnyRef
        var exInfo = null: AnyRef
        try r = f.get
        catch {
          case t: CancellationException =>
            exInfo = classOf[CancellationException]
          case t: ExecutionException =>
            exInfo = t.getCause
          case t: Throwable =>
            threadUnexpectedException(t)
        }
        // Check that run and runAndReset have no effect.
        val savedRunCount = pf.runCount()
        pf.run()
        pf.doRunAndReset()
        assertEquals(savedRunCount, pf.runCount())
        var r2 = null: AnyRef
        try r2 = f.get
        catch {
          case t: CancellationException =>
            assertSame(exInfo, classOf[CancellationException])
          case t: ExecutionException =>
            assertSame(exInfo, t.getCause)
          case t: Throwable =>
            threadUnexpectedException(t)
        }
        if (exInfo == null) assertSame(r, r2)
        assertTrue(f.isDone())

      case _ => ()
    }

  }
  def checkNotDone[T](f: Future[T]): Unit = {
    assertFalse(f.isDone())
    assertFalse(f.isCancelled)
    f match {
      case pf: PublicFutureTask @unchecked =>
        assertEquals(0, pf.doneCount())
        assertEquals(0, pf.setCount())
        assertEquals(0, pf.setExceptionCount())
      case _ => ()
    }
  }

  def checkIsRunning(f: Future[AnyRef]): Unit = {
    checkNotDone(f)
    f match {
      case ft: FutureTask[_] =>
        // Check that run methods do nothing
        ft.run()
        f match {
          case pf: PublicFutureTask =>
            val savedRunCount = pf.runCount()
            pf.run()
            assertFalse(pf.doRunAndReset())
            assertEquals(savedRunCount, pf.runCount())
          case _ => ()
        }
        checkNotDone(f)
      case _ => ()
    }
  }

  def checkCompletedNormally[T <: AnyRef](
      f: Future[T],
      expectedValue: T
  ): Unit = {
    checkIsDone(f)
    assertFalse(f.isCancelled)
    var v1 = null: AnyRef
    var v2 = null: AnyRef
    try {
      v1 = f.get
      v2 = f.get(randomTimeout(), randomTimeUnit())
    } catch {
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    assertSame(expectedValue, v1)
    assertSame(expectedValue, v2)
  }

  def checkCancelled(f: Future[AnyRef]): Unit = {
    checkIsDone(f)
    assertTrue(f.isCancelled)
    try {
      f.get
      shouldThrow()
    } catch {
      case success: CancellationException =>

      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    try {
      f.get(randomTimeout(), randomTimeUnit())
      shouldThrow()
    } catch {
      case success: CancellationException =>

      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
  }
  def tryToConfuseDoneTask(pf: PublicFutureTask): Unit = {
    pf.doSet(new Object {})
    pf.doSetException(new Error)
    for (mayInterruptIfRunning <- Array[java.lang.Boolean](true, false)) {
      pf.cancel(mayInterruptIfRunning)
    }
  }
  def checkCompletedAbnormally(f: Future[AnyRef], t: Throwable): Unit = {
    checkIsDone(f)
    assertFalse(f.isCancelled)
    try {
      f.get
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertSame(t, success.getCause)
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    try {
      f.get(randomTimeout(), randomTimeUnit())
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertSame(t, success.getCause)
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
  }
  class Counter extends CheckedRunnable {
    final val count = new AtomicInteger(0)
    def get: Int = count.get
    override def realRun(): Unit = { count.getAndIncrement }
  }

  /** creating a future with a null callable throws NullPointerException
   */
  @Test def testConstructor(): Unit = {
    try {
      new FutureTask(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** creating a future with null runnable throws NullPointerException
   */
  @Test def testConstructor2(): Unit = {
    try {
      new FutureTask(null, java.lang.Boolean.TRUE)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** isDone is true when a task completes
   */
  @Test def testIsDone(): Unit = {
    val task =
      FutureTaskTest.PublicFutureTask(new NoOpCallable)
    assertFalse(task.isDone())
    task.run()
    assertTrue(task.isDone())
    checkCompletedNormally(task, java.lang.Boolean.TRUE)
    assertEquals(1, task.runCount())
  }

  /** runAndReset of a non-cancelled task succeeds
   */
  @Test def testRunAndReset(): Unit = {
    val task =
      FutureTaskTest.PublicFutureTask(new NoOpCallable)
    for (i <- 0 until 3) {
      assertTrue(task.doRunAndReset())
      checkNotDone(task)
      assertEquals(i + 1, task.runCount())
      assertEquals(i + 1, task.runAndResetCount())
      assertEquals(0, task.setCount())
      assertEquals(0, task.setExceptionCount())
    }
  }

  /** runAndReset after cancellation fails
   */
  @Test def testRunAndResetAfterCancel(): Unit = {
    for (mayInterruptIfRunning <- Array[java.lang.Boolean](true, false)) {
      val task =
        FutureTaskTest.PublicFutureTask(new NoOpCallable)
      assertTrue(task.cancel(mayInterruptIfRunning))
      for (i <- 0 until 3) {
        assertFalse(task.doRunAndReset())
        assertEquals(0, task.runCount())
        assertEquals(i + 1, task.runAndResetCount())
        assertEquals(0, task.setCount())
        assertEquals(0, task.setExceptionCount())
      }
      tryToConfuseDoneTask(task)
      checkCancelled(task)
    }
  }

  /** setting value causes get to return it
   */
  @throws[Exception]
  @Test def testSet(): Unit = {
    val task =
      FutureTaskTest.PublicFutureTask(new NoOpCallable)
    task.doSet(one)
    for (i <- 0 until 3) {
      assertSame(one, task.get)
      assertSame(one, task.get(LONG_DELAY_MS, MILLISECONDS))
      assertEquals(1, task.setCount())
    }
    tryToConfuseDoneTask(task)
    checkCompletedNormally(task, one)
    assertEquals(0, task.runCount())
  }

  /** setException causes get to throw ExecutionException
   */
  @throws[Exception]
  @Test def testSetException_get(): Unit = {
    val nse = new NoSuchElementException
    val task =
      FutureTaskTest.PublicFutureTask(new NoOpCallable)
    task.doSetException(nse)
    try {
      task.get
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertSame(nse, success.getCause)
        checkCompletedAbnormally(task, nse)
    }
    try {
      task.get(LONG_DELAY_MS, MILLISECONDS)
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertSame(nse, success.getCause)
        checkCompletedAbnormally(task, nse)
    }
    assertEquals(1, task.setExceptionCount())
    assertEquals(0, task.setCount())
    tryToConfuseDoneTask(task)
    checkCompletedAbnormally(task, nse)
    assertEquals(0, task.runCount())
  }

  /** cancel(false) before run succeeds
   */
  @Test def testCancelBeforeRun(): Unit = {
    val task =
      FutureTaskTest.PublicFutureTask(new NoOpCallable)
    assertTrue(task.cancel(false))
    task.run()
    assertEquals(0, task.runCount())
    assertEquals(0, task.setCount())
    assertEquals(0, task.setExceptionCount())
    assertTrue(task.isCancelled)
    assertTrue(task.isDone())
    tryToConfuseDoneTask(task)
    assertEquals(0, task.runCount())
    checkCancelled(task)
  }

  /** cancel(true) before run succeeds
   */
  @Test def testCancelBeforeRun2(): Unit = {
    val task =
      FutureTaskTest.PublicFutureTask(new NoOpCallable)
    assertTrue(task.cancel(true))
    task.run()
    assertEquals(0, task.runCount())
    assertEquals(0, task.setCount())
    assertEquals(0, task.setExceptionCount())
    assertTrue(task.isCancelled)
    assertTrue(task.isDone())
    tryToConfuseDoneTask(task)
    assertEquals(0, task.runCount())
    checkCancelled(task)
  }

  /** cancel(false) of a completed task fails
   */
  @Test def testCancelAfterRun(): Unit = {
    val task =
      FutureTaskTest.PublicFutureTask(new NoOpCallable)
    task.run()
    assertFalse(task.cancel(false))
    assertEquals(1, task.runCount())
    assertEquals(1, task.setCount())
    assertEquals(0, task.setExceptionCount())
    tryToConfuseDoneTask(task)
    checkCompletedNormally(task, java.lang.Boolean.TRUE)
    assertEquals(1, task.runCount())
  }

  /** cancel(true) of a completed task fails
   */
  @Test def testCancelAfterRun2(): Unit = {
    val task =
      FutureTaskTest.PublicFutureTask(new NoOpCallable)
    task.run()
    assertFalse(task.cancel(true))
    assertEquals(1, task.runCount())
    assertEquals(1, task.setCount())
    assertEquals(0, task.setExceptionCount())
    tryToConfuseDoneTask(task)
    checkCompletedNormally(task, java.lang.Boolean.TRUE)
    assertEquals(1, task.runCount())
  }

  /** cancel(true) interrupts a running task that subsequently succeeds
   */
  @Test def testCancelInterrupt(): Unit = {
    val pleaseCancel = new CountDownLatch(1)
    val task =
      FutureTaskTest.PublicFutureTask(new CheckedRunnable() {
        override def realRun(): Unit = {
          pleaseCancel.countDown()
          try {
            delay(LONG_DELAY_MS)
            shouldThrow()
          } catch {
            case success: InterruptedException =>

          }
          assertFalse(Thread.interrupted)
        }
      })
    val t = newStartedThread(task)
    await(pleaseCancel)
    assertTrue(task.cancel(true))
    assertTrue(task.isCancelled)
    assertTrue(task.isDone())
    awaitTermination(t)
    assertEquals(1, task.runCount())
    assertEquals(1, task.setCount())
    assertEquals(0, task.setExceptionCount())
    tryToConfuseDoneTask(task)
    checkCancelled(task)
  }

  /** cancel(true) tries to interrupt a running task, but Thread.interrupt
   *  throws (simulating a restrictive security manager)
   */
  @Test def testCancelInterrupt_ThrowsSecurityException(): Unit = {
    val pleaseCancel = new CountDownLatch(1)
    val cancelled = new CountDownLatch(1)
    val task =
      FutureTaskTest.PublicFutureTask(new CheckedRunnable() {
        override def realRun(): Unit = {
          pleaseCancel.countDown()
          await(cancelled)
          assertFalse(Thread.interrupted)
        }
      })
    val t = new Thread(task) { // Simulate a restrictive security manager.
      override def interrupt(): Unit = { throw new SecurityException }
    }
    t.setDaemon(true)
    t.start()
    await(pleaseCancel)
    try {
      task.cancel(true)
      shouldThrow()
    } catch {
      case success: SecurityException =>

    }
    // We failed to deliver the interrupt, but the world retains
    // its sanity, as if we had done task.cancel(false)
    assertTrue(task.isCancelled)
    assertTrue(task.isDone())
    assertEquals(1, task.runCount())
    assertEquals(1, task.doneCount())
    assertEquals(0, task.setCount())
    assertEquals(0, task.setExceptionCount())
    cancelled.countDown()
    awaitTermination(t)
    assertEquals(1, task.setCount())
    assertEquals(0, task.setExceptionCount())
    tryToConfuseDoneTask(task)
    checkCancelled(task)
  }

  /** cancel(true) interrupts a running task that subsequently throws
   */
  @Test def testCancelInterrupt_taskFails(): Unit = {
    val pleaseCancel = new CountDownLatch(1)
    val task = FutureTaskTest.PublicFutureTask(new Runnable() {
      override def run(): Unit = {
        pleaseCancel.countDown()
        try {
          delay(LONG_DELAY_MS)
          threadShouldThrow()
        } catch {
          case success: InterruptedException => ()
          case t: Throwable                  => threadUnexpectedException(t)
        }
        throw new RuntimeException
      }
    })
    val t = newStartedThread(task)
    await(pleaseCancel)
    assertTrue(task.cancel(true))
    assertTrue(task.isCancelled)
    awaitTermination(t)
    assertEquals(1, task.runCount())
    assertEquals(0, task.setCount())
    assertEquals(1, task.setExceptionCount())
    tryToConfuseDoneTask(task)
    checkCancelled(task)
  }

  /** cancel(false) does not interrupt a running task
   */
  @Test def testCancelNoInterrupt(): Unit = {
    val pleaseCancel = new CountDownLatch(1)
    val cancelled = new CountDownLatch(1)
    val task = FutureTaskTest.PublicFutureTask(
      new CheckedCallable[java.lang.Boolean]() {
        override def realCall(): java.lang.Boolean = {
          pleaseCancel.countDown()
          await(cancelled)
          assertFalse(Thread.interrupted)
          java.lang.Boolean.TRUE
        }
      }
    )
    val t = newStartedThread(task)
    await(pleaseCancel)
    assertTrue(task.cancel(false))
    assertTrue(task.isCancelled)
    cancelled.countDown()
    awaitTermination(t)
    assertEquals(1, task.runCount())
    assertEquals(1, task.setCount())
    assertEquals(0, task.setExceptionCount())
    tryToConfuseDoneTask(task)
    checkCancelled(task)
  }

  /** run in one thread causes get in another thread to retrieve value
   */
  @Test def testGetRun(): Unit = {
    val pleaseRun = new CountDownLatch(2)
    val task = FutureTaskTest.PublicFutureTask(
      new CheckedCallable[AnyRef]() {
        override def realCall(): AnyRef = two
      }
    )
    val t1 = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = {
        pleaseRun.countDown()
        assertSame(two, task.get)
      }
    })
    val t2 = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = {
        pleaseRun.countDown()
        assertSame(two, task.get(2 * LONG_DELAY_MS, MILLISECONDS))
      }
    })
    await(pleaseRun)
    checkNotDone(task)
    assertTrue(t1.isAlive)
    assertTrue(t2.isAlive)
    task.run()
    checkCompletedNormally(task, two)
    assertEquals(1, task.runCount())
    assertEquals(1, task.setCount())
    assertEquals(0, task.setExceptionCount())
    awaitTermination(t1)
    awaitTermination(t2)
    tryToConfuseDoneTask(task)
    checkCompletedNormally(task, two)
  }

  /** set in one thread causes get in another thread to retrieve value
   */
  @Test def testdoSet(): Unit = {
    val pleaseSet = new CountDownLatch(2)
    val task = FutureTaskTest.PublicFutureTask(
      new CheckedCallable[AnyRef]() {
        @throws[InterruptedException]
        override def realCall(): AnyRef = two
      }
    )
    val t1 = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = {
        pleaseSet.countDown()
        assertSame(two, task.get)
      }
    })
    val t2 = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = {
        pleaseSet.countDown()
        assertSame(two, task.get(2 * LONG_DELAY_MS, MILLISECONDS))
      }
    })
    await(pleaseSet)
    checkNotDone(task)
    assertTrue(t1.isAlive)
    assertTrue(t2.isAlive)
    task.doSet(two)
    assertEquals(0, task.runCount())
    assertEquals(1, task.setCount())
    assertEquals(0, task.setExceptionCount())
    tryToConfuseDoneTask(task)
    checkCompletedNormally(task, two)
    awaitTermination(t1)
    awaitTermination(t2)
  }

  /** Cancelling a task causes timed get in another thread to throw
   *  CancellationException
   */
  @Test def testTimedGet_Cancellation(): Unit = {
    testTimedGet_Cancellation(false)
  }
  @Test def testTimedGet_Cancellation_interrupt(): Unit = {
    testTimedGet_Cancellation(true)
  }

  def testTimedGet_Cancellation(
      mayInterruptIfRunning: java.lang.Boolean
  ): Unit = {
    val pleaseCancel = new CountDownLatch(3)
    val cancelled = new CountDownLatch(1)
    val callable = new CheckedCallable[AnyRef]() {
      @throws[InterruptedException]
      override def realCall(): AnyRef = {
        pleaseCancel.countDown()
        if (mayInterruptIfRunning)
          try delay(2 * LONG_DELAY_MS)
          catch {
            case success: InterruptedException =>

          }
        else await(cancelled)
        two
      }
    }
    val task = FutureTaskTest.PublicFutureTask(callable)
    val t1 = new ThreadShouldThrow(classOf[CancellationException]) {
      @throws[Exception]
      override def realRun(): Unit = {
        pleaseCancel.countDown()
        task.get
      }
    }
    val t2 = new ThreadShouldThrow(classOf[CancellationException]) {
      @throws[Exception]
      override def realRun(): Unit = {
        pleaseCancel.countDown()
        task.get(2 * LONG_DELAY_MS, MILLISECONDS)
      }
    }
    t1.start()
    t2.start()
    val t3 = newStartedThread(task)
    await(pleaseCancel)
    checkIsRunning(task)
    task.cancel(mayInterruptIfRunning)
    checkCancelled(task)
    awaitTermination(t1)
    awaitTermination(t2)
    cancelled.countDown()
    awaitTermination(t3)
    assertEquals("runCount", 1, task.runCount())
    assertEquals("setCunt", 1, task.setCount())
    assertEquals("exceptionCount", 0, task.setExceptionCount())
    tryToConfuseDoneTask(task)
    checkCancelled(task)
  }

  /** A runtime exception in task causes get to throw ExecutionException
   */
  @throws[InterruptedException]
  @Test def testGet_ExecutionException(): Unit = {
    val e = new ArithmeticException
    val task = FutureTaskTest.PublicFutureTask(new Callable[Any]() {
      override def call = throw e
    })
    task.run()
    assertEquals(1, task.runCount())
    assertEquals(0, task.setCount())
    assertEquals(1, task.setExceptionCount())
    try {
      task.get
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertSame(e, success.getCause)
        tryToConfuseDoneTask(task)
        checkCompletedAbnormally(task, success.getCause)
    }
  }

  /** A runtime exception in task causes timed get to throw ExecutionException
   */
  @throws[Exception]
  @Test def testTimedGet_ExecutionException2(): Unit = {
    val e = new ArithmeticException
    val task = FutureTaskTest.PublicFutureTask(new Callable[Any]() {
      override def call = throw e
    })
    task.run()
    try {
      task.get(LONG_DELAY_MS, MILLISECONDS)
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertSame(e, success.getCause)
        tryToConfuseDoneTask(task)
        checkCompletedAbnormally(task, success.getCause)
    }
  }

  /** get is interruptible
   */
  @Test def testGet_Interruptible(): Unit = {
    val pleaseInterrupt = new CountDownLatch(1)
    val task = new FutureTask(new NoOpCallable)
    val t = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = {
        Thread.currentThread.interrupt()
        try {
          task.get
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
        pleaseInterrupt.countDown()
        try {
          task.get
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
      }
    })
    await(pleaseInterrupt)
    t.interrupt()
    awaitTermination(t)
    checkNotDone(task)
  }

  /** timed get is interruptible
   */
  @Test def testTimedGet_Interruptible(): Unit = {
    val pleaseInterrupt = new CountDownLatch(1)
    val task = new FutureTask(new NoOpCallable)
    val t = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = {
        Thread.currentThread.interrupt()
        try {
          task.get(randomTimeout(), randomTimeUnit())
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
        pleaseInterrupt.countDown()
        try {
          task.get(LONGER_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case success: InterruptedException =>

        }
        assertFalse(Thread.interrupted)
      }
    })
    await(pleaseInterrupt)
    if (randomBoolean())
      assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    t.interrupt()
    awaitTermination(t)
    checkNotDone(task)
  }

  /** A timed out timed get throws TimeoutException
   */
  @throws[Exception]
  @Test def testGet_TimeoutException(): Unit = {
    val task = new FutureTask(new NoOpCallable)
    val startTime = System.nanoTime
    try {
      task.get(timeoutMillis(), MILLISECONDS)
      shouldThrow()
    } catch {
      case success: TimeoutException =>
        assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
    }
  }

  /** timed get with null TimeUnit throws NullPointerException
   */
  @throws[Exception]
  @Test def testGet_NullTimeUnit(): Unit = {
    val task = new FutureTask(new NoOpCallable)
    val timeouts = Array(java.lang.Long.MIN_VALUE, 0L, java.lang.Long.MAX_VALUE)
    for (timeout <- timeouts) {
      try {
        task.get(timeout, null)
        shouldThrow()
      } catch {
        case success: NullPointerException =>

      }
    }
    task.run()
    for (timeout <- timeouts) {
      try {
        task.get(timeout, null)
        shouldThrow()
      } catch {
        case success: NullPointerException =>

      }
    }
  }

  /** timed get with most negative timeout works correctly (i.e. no underflow
   *  bug)
   */
  @throws[Exception]
  @Test def testGet_NegativeInfinityTimeout(): Unit = {
    assumeFalse(
      "Fails due to bug in the JVM",
      Platform.executingInJVMOnJDK8OrLower
    )
    val pool = Executors.newFixedThreadPool(10)
    val nop = new Runnable() { override def run(): Unit = {} }
    val task = new FutureTask[Void](nop, null)
    val futures = new util.ArrayList[Future[AnyRef]]
    val r: Runnable = new Runnable() {
      override def run(): Unit = {
        for (timeout <- Array[Long](0L, -1L, java.lang.Long.MIN_VALUE)) {
          try {
            task.get(timeout, NANOSECONDS)
            shouldThrow()
          } catch {
            case success: TimeoutException => ()
            case fail: Throwable           => threadUnexpectedException(fail)
          }
        }
      }
    }
    for (i <- 0 until 10) {
      val f = pool.submit(r)
      futures.add(f.asInstanceOf[Future[AnyRef]])
    }
    try {
      joinPool(pool)
      futures.forEach(checkCompletedNormally(_, null))
    } finally task.run() // last resort to help terminate
  }

  /** toString indicates current completion state
   */
  @Test def testToString_incomplete(): Unit = {
    assumeFalse(
      "Fails due to bug in the JVM",
      Platform.executingInJVMOnJDK8OrLower
    )
    val f = new FutureTask[String](() => "")
    assertTrue(f.toString.matches(".*\\[.*Not completed.*\\]"))
    if (testImplementationDetails)
      assertTrue(
        f.toString.startsWith(identityString(f) + "[Not completed, task =")
      )
  }
  @Test def testToString_normal(): Unit = {
    assumeFalse(
      "Fails due to bug in the JVM",
      Platform.executingInJVMOnJDK8OrLower
    )
    val f = new FutureTask[String](() => "")
    f.run()
    assertTrue(f.toString.matches(".*\\[.*Completed normally.*\\]"))
    if (testImplementationDetails)
      assertEquals(identityString(f) + "[Completed normally]", f.toString)
  }
  @Test def testToString_exception(): Unit = {
    assumeFalse(
      "Fails due to bug in the JVM",
      Platform.executingInJVMOnJDK8OrLower
    )
    val f = new FutureTask[String](() => {
      def foo() =
        throw new ArithmeticException
      foo()
    })
    f.run()
    assertTrue(f.toString.matches(".*\\[.*Completed exceptionally.*\\]"))
    if (testImplementationDetails)
      assertTrue(
        f.toString.startsWith(identityString(f) + "[Completed exceptionally: ")
      )
  }
  @Test def testToString_cancelled(): Unit = {
    assumeFalse(
      "Fails due to bug in the JVM",
      Platform.executingInJVMOnJDK8OrLower
    )
    for (mayInterruptIfRunning <- Array[java.lang.Boolean](true, false)) {
      val f = new FutureTask[String](() => "")
      assertTrue(f.cancel(mayInterruptIfRunning))
      assertTrue(f.toString.matches(".*\\[.*Cancelled.*\\]"))
      if (testImplementationDetails)
        assertEquals(identityString(f) + "[Cancelled]", f.toString)
    }
  }
}
