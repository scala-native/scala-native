/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util
import java.util.concurrent._

import org.junit._
import org.junit.Assert._

object RecursiveActionTest {
  private def mainPool = new ForkJoinPool
  private def singletonPool = new ForkJoinPool(1)
  private def asyncSingletonPool = new ForkJoinPool(
    1,
    ForkJoinPool.defaultForkJoinWorkerThreadFactory,
    null,
    true
  )
  final class FJException(cause: Throwable) extends RuntimeException {
    def this() = this(null)
  }

  /** A recursive action failing in base case. */
  final class FailingFibAction(val number: Int) extends RecursiveAction {
    var result = 0
    override def compute(): Unit = {
      val n = number
      if (n <= 1) throw new RecursiveActionTest.FJException
      else {
        val f1 = new RecursiveActionTest.FailingFibAction(n - 1)
        val f2 = new RecursiveActionTest.FailingFibAction(n - 2)
        ForkJoinTask.invokeAll(f1, f2)
        result = f1.result + f2.result
      }
    }
  }

  /** Demo from RecursiveAction javadoc */
  object SortTask { // implementation details follow:
    val THRESHOLD = 100
  }
  class SortTask(val array: Array[Long], val lo: Int, val hi: Int)
      extends RecursiveAction {
    def this(array: Array[Long]) = this(array, 0, array.length)
    override protected def compute(): Unit = {
      if (hi - lo < SortTask.THRESHOLD) sortSequentially(lo, hi)
      else {
        val mid = (lo + hi) >>> 1
        ForkJoinTask.invokeAll(
          new RecursiveActionTest.SortTask(array, lo, mid),
          new RecursiveActionTest.SortTask(array, mid, hi)
        )
        merge(lo, mid, hi)
      }
    }
    def sortSequentially(lo: Int, hi: Int): Unit = {
      util.Arrays.sort(array, lo, hi)
    }
    def merge(lo: Int, mid: Int, hi: Int): Unit = {
      val buf = util.Arrays.copyOfRange(array, lo, mid)
      var i = 0
      var j = lo
      var k = mid
      while (i < buf.length) {
        array(j) =
          if (k == hi || buf(i) < array(k)) buf({ i += 1; i - 1 })
          else array({ k += 1; k - 1 })
        j += 1
      }
    }
  }
}
class RecursiveActionTest extends JSR166Test {
  import RecursiveActionTest._
  import JSR166Test._
  import ForkJoinTask._

  private def testInvokeOnPool(pool: ForkJoinPool, a: RecursiveAction): Unit = {
    usingPoolCleaner(pool) { p =>
      checkNotDone(a)
      assertNull(pool.invoke(a))
      checkCompletedNormally(a)
    }
  }
  def checkNotDone(a: RecursiveAction): Unit = {
    assertFalse(a.isDone)
    assertFalse(a.isCompletedNormally)
    assertFalse(a.isCompletedAbnormally)
    assertFalse(a.isCancelled)
    assertNull(a.getException)
    assertNull(a.getRawResult)
    if (!ForkJoinTask.inForkJoinPool) {
      Thread.currentThread.interrupt()
      try {
        a.get
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
  def checkCompletedNormally(a: RecursiveAction): Unit = {
    assertTrue(a.isDone)
    assertFalse(a.isCancelled)
    assertTrue(a.isCompletedNormally)
    assertFalse(a.isCompletedAbnormally)
    assertNull(a.getException)
    assertNull(a.getRawResult)
    assertNull(a.join)
    assertFalse(a.cancel(false))
    assertFalse(a.cancel(true))
    var v1: AnyRef = null
    var v2: AnyRef = null
    try {
      v1 = a.get
      v2 = a.get(randomTimeout(), randomTimeUnit())
    } catch {
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    assertNull(v1)
    assertNull(v2)
  }
  def checkCancelled(a: RecursiveAction): Unit = {
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
  def checkCompletedAbnormally(a: RecursiveAction, t: Throwable): Unit = {
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
        invokeAll(f1, f2)
        result = f1.result + f2.result
      }
    }
  }

  /** invoke returns when task completes normally. isCompletedAbnormally and
   *  isCancelled return false for normally completed tasks. getRawResult of a
   *  RecursiveAction returns null;
   */
  @Test def testInvoke(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertNull(f.invoke)
        assertEquals(21, f.result)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** quietlyInvoke task returns when task completes normally.
   *  isCompletedAbnormally and isCancelled return false for normally completed
   *  tasks
   */
  @Test def testQuietlyInvoke(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        f.quietlyInvoke
        assertEquals(21, f.result)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** join of a forked task returns when task completes
   */
  @Test def testForkJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertSame(f, f.fork)
        assertNull(f.join)
        assertEquals(21, f.result)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** join/quietlyJoin of a forked task succeeds in the presence of interrupts
   */
  @Test def testJoinIgnoresInterrupts(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        var f = new FibAction(8)
        val currentThread = Thread.currentThread
        // test join()
        assertSame(f, f.fork)
        currentThread.interrupt()
        assertNull(f.join)
        Thread.interrupted
        assertEquals(21, f.result)
        checkCompletedNormally(f)
        f = new FibAction(8)
        f.cancel(true)
        assertSame(f, f.fork)
        currentThread.interrupt()
        try {
          f.join
          shouldThrow()
        } catch {
          case success: CancellationException =>
            Thread.interrupted
            checkCancelled(f)
        }
        f = new FibAction(8)
        f.completeExceptionally(new RecursiveActionTest.FJException)
        assertSame(f, f.fork)
        currentThread.interrupt()
        try {
          f.join
          shouldThrow()
        } catch {
          case success: RecursiveActionTest.FJException =>
            Thread.interrupted
            checkCompletedAbnormally(f, success)
        }
        // test quietlyJoin()
        f = new FibAction(8)
        assertSame(f, f.fork)
        currentThread.interrupt()
        f.quietlyJoin
        Thread.interrupted
        assertEquals(21, f.result)
        checkCompletedNormally(f)
        f = new FibAction(8)
        f.cancel(true)
        assertSame(f, f.fork)
        currentThread.interrupt()
        f.quietlyJoin
        Thread.interrupted
        checkCancelled(f)
        f = new FibAction(8)
        f.completeExceptionally(new RecursiveActionTest.FJException)
        assertSame(f, f.fork)
        currentThread.interrupt()
        f.quietlyJoin
        Thread.interrupted
        checkCompletedAbnormally(f, f.getException)
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
    a.reinitialize()
    testInvokeOnPool(RecursiveActionTest.singletonPool, a)
  }

  /** join/quietlyJoin of a forked task when not in ForkJoinPool succeeds in the
   *  presence of interrupts
   */
  @Test def testJoinIgnoresInterruptsOutsideForkJoinPool(): Unit = {
    val sq = new SynchronousQueue[Array[FibAction]]
    val a = new CheckedRecursiveAction() {
      @throws[InterruptedException]
      protected def realCompute(): Unit = {
        val fibActions = new Array[FibAction](6)
        for (i <- 0 until fibActions.length) {
          fibActions(i) = new FibAction(8)
        }
        fibActions(1).cancel(false)
        fibActions(2).completeExceptionally(new RecursiveActionTest.FJException)
        fibActions(4).cancel(true)
        fibActions(5).completeExceptionally(new RecursiveActionTest.FJException)
        for (fibAction <- fibActions) { fibAction.fork }
        sq.put(fibActions)
        helpQuiesce
      }
    }
    val r: CheckedRunnable = () => {
      val fibActions = sq.take
      var f: FibAction = null
      val currentThread = Thread.currentThread
      // test join() ------------
      f = fibActions(0)
      assertFalse(ForkJoinTask.inForkJoinPool)
      currentThread.interrupt()
      assertNull(f.join)
      assertTrue(Thread.interrupted)
      assertEquals(21, f.result)
      checkCompletedNormally(f)
      f = fibActions(1)
      currentThread.interrupt()
      try {
        f.join
        shouldThrow()
      } catch {
        case success: CancellationException =>
          assertTrue(Thread.interrupted)
          checkCancelled(f)
      }
      f = fibActions(2)
      currentThread.interrupt()
      try {
        f.join
        shouldThrow()
      } catch {
        case success: RecursiveActionTest.FJException =>
          assertTrue(Thread.interrupted)
          checkCompletedAbnormally(f, success)
      }
      // test quietlyJoin() ---------
      f = fibActions(3)
      currentThread.interrupt()
      f.quietlyJoin
      assertTrue(Thread.interrupted)
      assertEquals(21, f.result)
      checkCompletedNormally(f)
      f = fibActions(4)
      currentThread.interrupt()
      f.quietlyJoin
      assertTrue(Thread.interrupted)
      checkCancelled(f)
      f = fibActions(5)
      currentThread.interrupt()
      f.quietlyJoin
      assertTrue(Thread.interrupted)
      assertTrue(f.getException.isInstanceOf[RecursiveActionTest.FJException])
      checkCompletedAbnormally(f, f.getException)
    }
    locally {
      val t: Thread = newStartedThread(r)
      testInvokeOnPool(RecursiveActionTest.mainPool, a)
      awaitTermination(t)
    }
    a.reinitialize()
    locally {
      val t = newStartedThread(r)
      testInvokeOnPool(RecursiveActionTest.singletonPool, a)
      awaitTermination(t)
    }
  }

  /** get of a forked task returns when task completes
   */
  @Test def testForkGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertSame(f, f.fork)
        assertNull(f.get)
        assertEquals(21, f.result)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** timed get of a forked task returns when task completes
   */
  @Test def testForkTimedGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertSame(f, f.fork)
        assertNull(f.get(LONG_DELAY_MS, MILLISECONDS))
        assertEquals(21, f.result)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** timed get with null time unit throws NPE
   */
  @Test def testForkTimedGetNPE(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertSame(f, f.fork)
        try {
          f.get(randomTimeout(), null)
          shouldThrow()
        } catch {
          case success: NullPointerException =>

        }
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** quietlyJoin of a forked task returns when task completes
   */
  @Test def testForkQuietlyJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertSame(f, f.fork)
        f.quietlyJoin
        assertEquals(21, f.result)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** helpQuiesce returns when tasks are complete. getQueuedTaskCount returns 0
   *  when quiescent
   */
  @Test def testForkHelpQuiesce(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertSame(f, f.fork)
        helpQuiesce
        while (!f.isDone) { // wait out race
        }
        assertEquals(21, f.result)
        assertEquals(0, getQueuedTaskCount)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** invoke task throws exception when task completes abnormally
   */
  @Test def testAbnormalInvoke(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new RecursiveActionTest.FailingFibAction(8)
        try {
          f.invoke
          shouldThrow()
        } catch {
          case success: RecursiveActionTest.FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** quietlyInvoke task returns when task completes abnormally
   */
  @Test def testAbnormalQuietlyInvoke(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new RecursiveActionTest.FailingFibAction(8)
        f.quietlyInvoke()
        assertTrue(f.getException.isInstanceOf[RecursiveActionTest.FJException])
        checkCompletedAbnormally(f, f.getException)
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** join of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new RecursiveActionTest.FailingFibAction(8)
        assertSame(f, f.fork)
        try {
          f.join
          shouldThrow()
        } catch {
          case success: RecursiveActionTest.FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** get of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new RecursiveActionTest.FailingFibAction(8)
        assertSame(f, f.fork)
        try {
          f.get
          shouldThrow()
        } catch {
          case success: ExecutionException =>
            val cause = success.getCause
            assertTrue(cause.isInstanceOf[RecursiveActionTest.FJException])
            checkCompletedAbnormally(f, cause)
        }
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** timed get of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkTimedGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new RecursiveActionTest.FailingFibAction(8)
        assertSame(f, f.fork)
        try {
          f.get(LONG_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case success: ExecutionException =>
            val cause = success.getCause
            assertTrue(cause.isInstanceOf[RecursiveActionTest.FJException])
            checkCompletedAbnormally(f, cause)
        }
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** quietlyJoin of a forked task returns when task completes abnormally
   */
  @Test def testAbnormalForkQuietlyJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new RecursiveActionTest.FailingFibAction(8)
        assertSame(f, f.fork)
        f.quietlyJoin()
        assertTrue(f.getException.isInstanceOf[RecursiveActionTest.FJException])
        checkCompletedAbnormally(f, f.getException)
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** invoke task throws exception when task cancelled
   */
  @Test def testCancelledInvoke(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertTrue(f.cancel(true))
        try {
          f.invoke
          shouldThrow()
        } catch {
          case success: CancellationException =>
            checkCancelled(f)
        }
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** join of a forked task throws exception when task cancelled
   */
  @Test def testCancelledForkJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertTrue(f.cancel(true))
        assertSame(f, f.fork)
        try {
          f.join
          shouldThrow()
        } catch {
          case success: CancellationException =>
            checkCancelled(f)
        }
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** get of a forked task throws exception when task cancelled
   */
  @Test def testCancelledForkGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertTrue(f.cancel(true))
        assertSame(f, f.fork)
        try {
          f.get
          shouldThrow()
        } catch {
          case success: CancellationException =>
            checkCancelled(f)
        }
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** timed get of a forked task throws exception when task cancelled
   */
  @Test def testCancelledForkTimedGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertTrue(f.cancel(true))
        assertSame(f, f.fork)
        try {
          f.get(LONG_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case success: CancellationException =>
            checkCancelled(f)
        }
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** quietlyJoin of a forked task returns when task cancelled
   */
  @Test def testCancelledForkQuietlyJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertTrue(f.cancel(true))
        assertSame(f, f.fork)
        f.quietlyJoin
        checkCancelled(f)
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** getPool of executing task returns its pool
   */
  @Test def testGetPool(): Unit = {
    val mainPool = RecursiveActionTest.mainPool
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = { assertSame(mainPool, getPool) }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** getPool of non-FJ task returns null
   */
  @Ignore("All Scala Native tests are exuected in the pool")
  @Test def testGetPool2(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = { assertNull(getPool) }
    }
    assertNull(a.invoke)
  }

  /** inForkJoinPool of executing task returns true
   */
  @Test def testInForkJoinPool(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = { assertTrue(inForkJoinPool) }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** inForkJoinPool of non-FJ task returns false
   */
  @Ignore("All Scala Native etests are exuected in the pool")
  @Test def testInForkJoinPool2(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = { assertFalse(inForkJoinPool) }
    }
    assertNull(a.invoke)
  }

  /** getPool of current thread in pool returns its pool
   */
  @Test def testWorkerGetPool(): Unit = {
    val mainPool = RecursiveActionTest.mainPool
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val w = Thread.currentThread.asInstanceOf[ForkJoinWorkerThread]
        assertSame(mainPool, w.getPool)
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** getPoolIndex of current thread in pool returns 0 <= value < poolSize
   */
  @Test def testWorkerGetPoolIndex(): Unit = {
    val mainPool = RecursiveActionTest.mainPool
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val w = Thread.currentThread.asInstanceOf[ForkJoinWorkerThread]
        assertTrue(w.getPoolIndex >= 0)
        // pool size can shrink after assigning index, so cannot check
        // assertTrue(w.getPoolIndex() < mainPool.getPoolSize());
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** setRawResult(null) succeeds
   */
  @Test def testSetRawResult(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        setRawResult(null)
        assertNull(getRawResult)
      }
    }
    assertNull(a.invoke)
  }

  /** A reinitialized normally completed task may be re-invoked
   */
  @Test def testReinitialize(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        checkNotDone(f)
        for (i <- 0 until 3) {
          assertNull(f.invoke)
          assertEquals(21, f.result)
          checkCompletedNormally(f)
          f.reinitialize
          checkNotDone(f)
        }
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** A reinitialized abnormally completed task may be re-invoked
   */
  @Test def testReinitializeAbnormal(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new RecursiveActionTest.FailingFibAction(8)
        checkNotDone(f)
        for (i <- 0 until 3) {
          try {
            f.invoke
            shouldThrow()
          } catch {
            case success: RecursiveActionTest.FJException =>
              checkCompletedAbnormally(f, success)
          }
          f.reinitialize()
          checkNotDone(f)
        }
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** invoke task throws exception after invoking completeExceptionally
   */
  @Test def testCompleteExceptionally(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        f.completeExceptionally(new RecursiveActionTest.FJException)
        try {
          f.invoke
          shouldThrow()
        } catch {
          case success: RecursiveActionTest.FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** invoke task suppresses execution invoking complete
   */
  @Test def testComplete(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        f.complete(null)
        assertNull(f.invoke)
        assertEquals(0, f.result)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** invokeAll(t1, t2) invokes all task arguments
   */
  @Test def testInvokeAll2(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        val g = new FibAction(9)
        invokeAll(f, g)
        checkCompletedNormally(f)
        assertEquals(21, f.result)
        checkCompletedNormally(g)
        assertEquals(34, g.result)
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** invokeAll(tasks) with 1 argument invokes task
   */
  @Test def testInvokeAll1(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        invokeAll(f)
        checkCompletedNormally(f)
        assertEquals(21, f.result)
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** invokeAll(tasks) with > 2 argument invokes tasks
   */
  @Test def testInvokeAll3(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        val g = new FibAction(9)
        val h = new FibAction(7)
        invokeAll(f, g, h)
        assertTrue(f.isDone)
        assertTrue(g.isDone)
        assertTrue(h.isDone)
        checkCompletedNormally(f)
        assertEquals(21, f.result)
        checkCompletedNormally(g)
        assertEquals(34, g.result)
        checkCompletedNormally(g)
        assertEquals(13, h.result)
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** invokeAll(collection) invokes all tasks in the collection
   */
  @Test def testInvokeAllCollection(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        val g = new FibAction(9)
        val h = new FibAction(7)
        val set = new util.HashSet[FibAction]
        set.add(f)
        set.add(g)
        set.add(h)
        invokeAll(set)
        assertTrue(f.isDone)
        assertTrue(g.isDone)
        assertTrue(h.isDone)
        checkCompletedNormally(f)
        assertEquals(21, f.result)
        checkCompletedNormally(g)
        assertEquals(34, g.result)
        checkCompletedNormally(g)
        assertEquals(13, h.result)
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** invokeAll(tasks) with any null task throws NPE
   */
  @Test def testInvokeAllNPE(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        val g = new FibAction(9)
        val h: FibAction = null
        try {
          invokeAll(f, g, h)
          shouldThrow()
        } catch {
          case success: NullPointerException =>

        }
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** invokeAll(t1, t2) throw exception if any task does
   */
  @Test def testAbnormalInvokeAll2(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        val g = new RecursiveActionTest.FailingFibAction(9)
        try {
          invokeAll(f, g)
          shouldThrow()
        } catch {
          case success: RecursiveActionTest.FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** invokeAll(tasks) with 1 argument throws exception if task does
   */
  @Test def testAbnormalInvokeAll1(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new RecursiveActionTest.FailingFibAction(9)
        try {
          invokeAll(g)
          shouldThrow()
        } catch {
          case success: RecursiveActionTest.FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** invokeAll(tasks) with > 2 argument throws exception if any task does
   */
  @Test def testAbnormalInvokeAll3(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        val g = new RecursiveActionTest.FailingFibAction(9)
        val h = new FibAction(7)
        try {
          invokeAll(f, g, h)
          shouldThrow()
        } catch {
          case success: RecursiveActionTest.FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** invokeAll(collection) throws exception if any task does
   */
  @Test def testAbnormalInvokeAllCollection(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new RecursiveActionTest.FailingFibAction(8)
        val g = new FibAction(9)
        val h = new FibAction(7)
        val set = new util.HashSet[RecursiveAction]
        set.add(f)
        set.add(g)
        set.add(h)
        try {
          invokeAll(set)
          shouldThrow()
        } catch {
          case success: RecursiveActionTest.FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(RecursiveActionTest.mainPool, a)
  }

  /** tryUnfork returns true for most recent unexecuted task, and suppresses
   *  execution
   */
  @Test def testTryUnfork(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new FibAction(9)
        assertSame(g, g.fork)
        val f = new FibAction(8)
        assertSame(f, f.fork)
        assertTrue(f.tryUnfork)
        helpQuiesce
        checkNotDone(f)
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(RecursiveActionTest.singletonPool, a)
  }

  /** getSurplusQueuedTaskCount returns > 0 when there are more tasks than
   *  threads
   */
  @Test def testGetSurplusQueuedTaskCount(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val h = new FibAction(7)
        assertSame(h, h.fork)
        val g = new FibAction(9)
        assertSame(g, g.fork)
        val f = new FibAction(8)
        assertSame(f, f.fork)
        assertTrue(getSurplusQueuedTaskCount > 0)
        helpQuiesce
        assertEquals(0, getSurplusQueuedTaskCount)
        checkCompletedNormally(f)
        checkCompletedNormally(g)
        checkCompletedNormally(h)
      }
    }
    testInvokeOnPool(RecursiveActionTest.singletonPool, a)
  }

  /** peekNextLocalTask returns most recent unexecuted task.
   */
  @Test def testPeekNextLocalTask(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new FibAction(9)
        assertSame(g, g.fork)
        val f = new FibAction(8)
        assertSame(f, f.fork)
        assertSame(f, peekNextLocalTask)
        assertNull(f.join)
        checkCompletedNormally(f)
        helpQuiesce
        checkCompletedNormally(f)
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(RecursiveActionTest.singletonPool, a)
  }

  /** pollNextLocalTask returns most recent unexecuted task without executing it
   */
  @Test def testPollNextLocalTask(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new FibAction(9)
        assertSame(g, g.fork)
        val f = new FibAction(8)
        assertSame(f, f.fork)
        assertSame(f, pollNextLocalTask)
        helpQuiesce
        checkNotDone(f)
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(RecursiveActionTest.singletonPool, a)
  }

  /** pollTask returns an unexecuted task without executing it
   */
  @Test def testPollTask(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new FibAction(9)
        assertSame(g, g.fork)
        val f = new FibAction(8)
        assertSame(f, f.fork)
        assertSame(f, pollTask)
        helpQuiesce
        checkNotDone(f)
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(RecursiveActionTest.singletonPool, a)
  }

  /** peekNextLocalTask returns least recent unexecuted task in async mode
   */
  @Test def testPeekNextLocalTaskAsync(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new FibAction(9)
        assertSame(g, g.fork)
        val f = new FibAction(8)
        assertSame(f, f.fork)
        assertSame(g, peekNextLocalTask)
        assertNull(f.join)
        helpQuiesce
        checkCompletedNormally(f)
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(RecursiveActionTest.asyncSingletonPool, a)
  }

  /** pollNextLocalTask returns least recent unexecuted task without executing
   *  it, in async mode
   */
  @Test def testPollNextLocalTaskAsync(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new FibAction(9)
        assertSame(g, g.fork)
        val f = new FibAction(8)
        assertSame(f, f.fork)
        assertSame(g, pollNextLocalTask)
        helpQuiesce
        checkCompletedNormally(f)
        checkNotDone(g)
      }
    }
    testInvokeOnPool(RecursiveActionTest.asyncSingletonPool, a)
  }

  /** pollTask returns an unexecuted task without executing it, in async mode
   */
  @Test def testPollTaskAsync(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new FibAction(9)
        assertSame(g, g.fork)
        val f = new FibAction(8)
        assertSame(f, f.fork)
        assertSame(g, pollTask)
        helpQuiesce
        checkCompletedNormally(f)
        checkNotDone(g)
      }
    }
    testInvokeOnPool(RecursiveActionTest.asyncSingletonPool, a)
  }

  /** SortTask demo works as advertised
   */
  @Test def testSortTaskDemo(): Unit = {
    val rnd = ThreadLocalRandom.current
    val array = new Array[Long](1007)
    for (i <- 0 until array.length) { array(i) = rnd.nextLong }
    val arrayClone = array.clone
    testInvokeOnPool(
      RecursiveActionTest.mainPool,
      new RecursiveActionTest.SortTask(array)
    )
    util.Arrays.sort(arrayClone)
    assertTrue(util.Arrays.equals(array, arrayClone))
  }
}
