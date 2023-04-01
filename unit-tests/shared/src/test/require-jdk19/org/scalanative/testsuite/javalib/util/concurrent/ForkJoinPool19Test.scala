package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent._
import java.util.concurrent.TimeUnit.MILLISECONDS

import org.junit._
import org.junit.Assert._

object ForkJoinPool19Test {
  final class FJException(cause: Throwable) extends RuntimeException(cause) {
    def this() = this(null)
  }

  final class FailingFibAction(val number: Int) extends RecursiveAction {
    var result = 0
    override def compute(): Unit = {
      val n = number
      if (n <= 1) throw new FJException
      else {
        val f1 = new FailingFibAction(n - 1)
        val f2 = new FailingFibAction(n - 2)
        ForkJoinTask.invokeAll(f1, f2)
        result = f1.result + f2.result
      }
    }
  }
}
class ForkJoinPool19Test extends JSR166Test {
  import ForkJoinPool19Test._
  import JSR166Test._

  /** SetParallelism sets reported parallellism and returns previous value
   */
  @Test def testSetParallelism(): Unit = {
    val p = new ForkJoinPool(2)
    assertEquals(2, p.getParallelism)
    assertEquals(2, p.setParallelism(3))
    assertEquals(3, p.setParallelism(2))
    p.shutdown()
  }

  /** SetParallelism throws exception if argument out of bounds
   */
  @Test def testSetParallelismBadArgs(): Unit = {
    val p = new ForkJoinPool(2)
    try {
      p.setParallelism(0)
      shouldThrow()
    } catch {
      case success: Exception =>
    }
    try {
      p.setParallelism(Integer.MAX_VALUE)
      shouldThrow()
    } catch {
      case success: Exception =>

    }
    assertEquals(2, p.getParallelism)
    p.shutdown()
  }

  private def testInvokeOnPool(pool: ForkJoinPool, a: RecursiveAction): Unit =
    usingPoolCleaner(pool) { pool =>
      checkNotDone(a)
      assertNull(pool.invoke(a))
      checkCompletedNormally(a)
    }

  private def checkInvoke(a: ForkJoinTask[_]): Unit = {
    checkNotDone(a)
    assertNull(a.invoke)
    checkCompletedNormally(a)
  }
  def checkNotDone(a: ForkJoinTask[_]): Unit = {
    assertFalse(a.isDone())
    assertFalse(a.isCompletedNormally())
    assertFalse(a.isCompletedAbnormally())
    assertFalse(a.isCancelled())
    assertNull(a.getException())
    assertNull(a.getRawResult())
    if (!ForkJoinTask.inForkJoinPool()) {
      Thread.currentThread.interrupt()
      try {
        a.get()
        shouldThrow()
      } catch {
        case success: InterruptedException =>
        case fail: Throwable =>
          threadUnexpectedException(fail)
      }
      Thread.currentThread.interrupt()
      try {
        a.get(randomTimeout(), randomTimeUnit())
        shouldThrow()
      } catch {
        case success: InterruptedException =>
        case fail: Throwable =>
          threadUnexpectedException(fail)
      }
    }
    try {
      a.get(randomExpiredTimeout(), randomTimeUnit())
      shouldThrow()
    } catch {
      case success: TimeoutException =>
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
  }

  def checkCompletedNormally(a: ForkJoinTask[_]): Unit = {
    assertTrue(a.isDone)
    assertFalse(a.isCancelled)
    assertTrue(a.isCompletedNormally)
    assertFalse(a.isCompletedAbnormally)
    assertNull(a.getException)
    assertNull(a.getRawResult)
    assertNull(a.join)
    assertFalse(a.cancel(false))
    assertFalse(a.cancel(true))
    var v1, v2: Any = null.asInstanceOf[Any]
    try {
      v1 = a.get()
      v2 = a.get(randomTimeout(), randomTimeUnit())
      (v1, v2)
    } catch {
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    assertNull(v1)
    assertNull(v2)
  }

  def checkCancelled(a: ForkJoinTask[_]): Unit = {
    assertTrue(a.isDone)
    assertTrue(a.isCancelled)
    assertFalse(a.isCompletedNormally)
    assertTrue(a.isCompletedAbnormally)
    assertTrue(a.getException.isInstanceOf[CancellationException])
    assertNull(a.getRawResult)
    try {
      a.join
      shouldThrow()
    } catch {
      case success: CancellationException =>

      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    try {
      a.get
      shouldThrow()
    } catch {
      case success: CancellationException =>
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    try {
      a.get(randomTimeout(), randomTimeUnit())
      shouldThrow()
    } catch {
      case success: CancellationException =>

      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
  }
  def checkCompletedAbnormally(a: ForkJoinTask[_], t: Throwable): Unit = {
    assertTrue(a.isDone)
    assertFalse(a.isCancelled)
    assertFalse(a.isCompletedNormally)
    assertTrue(a.isCompletedAbnormally)
    assertSame(t.getClass, a.getException.getClass)
    assertNull(a.getRawResult)
    assertFalse(a.cancel(false))
    assertFalse(a.cancel(true))
    try {
      a.join
      shouldThrow()
    } catch {
      case expected: Throwable =>
        assertSame(expected.getClass, t.getClass)
    }
    try {
      a.get
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertSame(t.getClass, success.getCause.getClass)
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    try {
      a.get(randomTimeout(), randomTimeUnit())
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertSame(t.getClass, success.getCause.getClass)
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
  }

  /** A simple recursive action for testing. */
  final class FibAction(val number: Int) extends CheckedRecursiveAction {
    var result = 0
    protected def realCompute(): Unit = {
      val n = number
      if (n <= 1) result = n
      else {
        val f1 = new FibAction(n - 1)
        val f2 = new FibAction(n - 2)
        ForkJoinTask.invokeAll(f1, f2)
        result = f1.result + f2.result
      }
    }
  }

  /** lazySubmit submits a task that is not executed until new workers are
   *  created or it is explicitly joined by a worker.
   */
  @Test def testLazySubmit(): Unit = {
    val p = new ForkJoinPool()
    val f = new FibAction(8)
    val j = new RecursiveAction() {
      protected def compute(): Unit = f.join()
    }
    val a: RecursiveAction = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        p.invoke(new FibAction(8))
        p.lazySubmit(f)
        p.invoke(new FibAction(8))
        p.invoke(j)
        assertEquals(21, f.result)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(p, a)
  }

  /** quietlyInvoke task returns when task completes normally.
   *  isCompletedAbnormally and isCancelled return false for normally completed
   *  tasks
   */
  @Test def testQuietlyInvoke(): Unit = {
    val a: RecursiveAction = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        f.quietlyInvoke
        assertEquals(21, f.result)
        checkCompletedNormally(f)
      }
    }
    checkInvoke(a)
  }

  /** join of a forked task returns when task completes
   */
  @Test def testForkJoin(): Unit = {
    val a: RecursiveAction = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertSame(f, f.fork)
        assertNull(f.join)
        assertEquals(21, f.result)
        checkCompletedNormally(f)
      }
    }
    checkInvoke(a)
  }

  /** timed quietlyJoinUninterruptibly of a forked task succeeds in the presence
   *  of interrupts
   */
  @Test def testTimedQuietlyJoinUninterruptiblyInterrupts(): Unit = {
    val a: RecursiveAction = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        var f: FibAction = null
        val currentThread = Thread.currentThread
        // test quietlyJoin()
        f = new FibAction(8)
        assertSame(f, f.fork)
        currentThread.interrupt()
        f.quietlyJoinUninterruptibly(LONG_DELAY_MS, MILLISECONDS)
        Thread.interrupted
        assertEquals(21, f.result)
        checkCompletedNormally(f)
        f = new FibAction(8)
        f.cancel(true)
        assertSame(f, f.fork)
        currentThread.interrupt()
        f.quietlyJoinUninterruptibly(LONG_DELAY_MS, MILLISECONDS)
        Thread.interrupted
        checkCancelled(f)
        f = new FibAction(8)
        f.completeExceptionally(new FJException)
        assertSame(f, f.fork)
        currentThread.interrupt()
        f.quietlyJoinUninterruptibly(LONG_DELAY_MS, MILLISECONDS)
        Thread.interrupted
        checkCompletedAbnormally(f, f.getException)
      }
    }
    checkInvoke(a)
    a.reinitialize()
    checkInvoke(a)
  }

  /** timed quietlyJoin throws IE in the presence of interrupts
   */
  @Test def testTimedQuietlyJoinInterrupts(): Unit = {
    val a: RecursiveAction = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        var f: FibAction = null
        val currentThread = Thread.currentThread
        f = new FibAction(8)
        assertSame(f, f.fork)
        currentThread.interrupt()
        try f.quietlyJoin(LONG_DELAY_MS, MILLISECONDS)
        catch {
          case success: InterruptedException =>

        }
        Thread.interrupted
        f.quietlyJoin
        f = new FibAction(8)
        f.cancel(true)
        assertSame(f, f.fork)
        currentThread.interrupt()
        try f.quietlyJoin(LONG_DELAY_MS, MILLISECONDS)
        catch {
          case success: InterruptedException =>

        }
        f.quietlyJoin
        checkCancelled(f)
      }
    }
    checkInvoke(a)
    a.reinitialize()
    checkInvoke(a)
  }

  /** timed quietlyJoin of a forked task returns when task completes
   */
  @Test def testForkTimedQuietlyJoin(): Unit = {
    val a: RecursiveAction = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertSame(f, f.fork)
        assertTrue(f.quietlyJoin(LONG_DELAY_MS, MILLISECONDS))
        assertEquals(21, f.result)
        checkCompletedNormally(f)
      }
    }
    checkInvoke(a)
  }

  /** timed quietlyJoin with null time unit throws NPE
   */
  @Test def testForkTimedQuietlyJoinNPE(): Unit = {
    val a: RecursiveAction = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertSame(f, f.fork)
        try {
          f.quietlyJoin(randomTimeout(), null)
          shouldThrow()
        } catch {
          case success: NullPointerException =>

        }
      }
    }
    checkInvoke(a)
  }

  /** quietlyInvoke task returns when task completes abnormally
   */
  @Test def testAbnormalTimedQuietlyJoin(): Unit = {
    val a: RecursiveAction = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new FailingFibAction(8)
        assertSame(f, f.fork)
        assertTrue(f.quietlyJoin(LONG_DELAY_MS, MILLISECONDS))
        assertTrue(f.getException.isInstanceOf[FJException])
        checkCompletedAbnormally(f, f.getException)
      }
    }
    checkInvoke(a)
  }

  /** timed quietlyJoinUninterruptibly of a forked task returns when task
   *  completes
   */
  @Test def testForkTimedQuietlyJoinUninterruptibly(): Unit = {
    val a: RecursiveAction = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertSame(f, f.fork)
        assertTrue(f.quietlyJoinUninterruptibly(LONG_DELAY_MS, MILLISECONDS))
        assertEquals(21, f.result)
        checkCompletedNormally(f)
      }
    }
    checkInvoke(a)
  }

  /** timed quietlyJoinUninterruptibly with null time unit throws NPE
   */
  @Test def testForkTimedQuietlyJoinUninterruptiblyNPE(): Unit = {
    val a: RecursiveAction = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertSame(f, f.fork)
        try {
          f.quietlyJoinUninterruptibly(randomTimeout(), null)
          shouldThrow()
        } catch {
          case success: NullPointerException =>

        }
      }
    }
    checkInvoke(a)
  }

  /** quietlyInvoke task returns when task completes abnormally
   */
  @Test def testAbnormalTimedQuietlyJoinUninterruptibly(): Unit = {
    val a: RecursiveAction = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FailingFibAction(8)
        assertSame(f, f.fork)
        assertTrue(f.quietlyJoinUninterruptibly(LONG_DELAY_MS, MILLISECONDS))
        assertTrue(f.getException.isInstanceOf[FJException])
        checkCompletedAbnormally(f, f.getException)
      }
    }
    checkInvoke(a)
  }

  /** adaptInterruptible(callable).toString() contains toString of wrapped task
   */
  @Test def testAdaptInterruptible_Callable_toString(): Unit = {
    if (testImplementationDetails) {
      val c: Callable[String] = () => ""
      val task = ForkJoinTask.adaptInterruptible(c)
      assertEquals(
        identityString(task) + "[Wrapped task = " + c.toString + "]",
        task.toString
      )
    }
  }

  /** Implicitly closing a new pool using try-with-resources terminates it
   */
  @Test def testClose(): Unit = {
    val f = new FibAction(8)
    val p = new ForkJoinPool()
    try p.execute(f)
    finally p.close()
    checkCompletedNormally(f)
    assertTrue(p != null && p.isTerminated())
  }

  @Test def testCloseCommonPool(): Unit = {
    val f = new FibAction(8)
    val p = ForkJoinPool.commonPool()
    try p.execute(f)
    finally p.close()
    assertFalse(p.isShutdown())
    assertFalse(p.isTerminating())
    assertFalse(p.isTerminated())
  }
}
