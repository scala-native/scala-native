/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util._
import java.util.concurrent._

import org.junit._
import org.junit.Assert._
import scala.scalanative.junit.utils.AssumesHelper

import JSR166Test._

object ForkJoinPool8Test {
  final class FJException(cause: Throwable) extends RuntimeException {
    def this() = this(null)
  }

  /** A recursive action failing in base case. */
  final class FailingFibAction(val number: Int) extends RecursiveAction {
    var result = 0
    override def compute(): Unit = {
      val n = number
      if (n <= 1) throw new ForkJoinPool8Test.FJException
      else {
        val f1 = new ForkJoinPool8Test.FailingFibAction(n - 1)
        val f2 = new ForkJoinPool8Test.FailingFibAction(n - 2)
        ForkJoinTask.invokeAll(f1, f2)
        result = f1.result + f2.result
      }
    }
  }
  // CountedCompleter versions
  abstract class CCF(parent: CountedCompleter[_], var number: Int)
      extends CountedCompleter[AnyRef](parent, 1) {
    var rnumber = 0
    override final def compute(): Unit = {
      var p: CountedCompleter[_] = null
      var f = this
      var n = number
      while (n >= 2) {
        new ForkJoinPool8Test.RCCF(f, n - 2).fork
        f = new ForkJoinPool8Test.LCCF(f, { n -= 1; n })
      }
      f.number = n
      f.onCompletion(f)
      p = f.getCompleter()
      if (p != null) p.tryComplete()
      else f.quietlyComplete()
    }
  }
  final class LCCF(parent: CountedCompleter[_], n: Int)
      extends ForkJoinPool8Test.CCF(parent, n) {
    override final def onCompletion(caller: CountedCompleter[_]): Unit = {
      val p = getCompleter.asInstanceOf[ForkJoinPool8Test.CCF]
      val n = number + rnumber
      if (p != null) p.number = n
      else number = n
    }
  }
  final class RCCF(parent: CountedCompleter[_], n: Int)
      extends ForkJoinPool8Test.CCF(parent, n) {
    override final def onCompletion(caller: CountedCompleter[_]): Unit = {
      val p = getCompleter.asInstanceOf[ForkJoinPool8Test.CCF]
      val n = number + rnumber
      if (p != null) p.rnumber = n
      else number = n
    }
  }

  /** Version of CCF with forced failure in left completions. */
  abstract class FailingCCF(parent: CountedCompleter[_], var number: Int)
      extends CountedCompleter[AnyRef](parent, 1) {
    val rnumber = 0
    override final def compute(): Unit = {
      var p: CountedCompleter[_] = null
      var f = this
      var n = number
      while (n >= 2) {
        new ForkJoinPool8Test.RFCCF(f, n - 2).fork
        f = new ForkJoinPool8Test.LFCCF(f, { n -= 1; n })
      }
      f.number = n
      f.onCompletion(f)
      p = f.getCompleter
      if (p != null) p.tryComplete()
      else f.quietlyComplete()
    }
  }
  final class LFCCF(parent: CountedCompleter[_], n: Int)
      extends ForkJoinPool8Test.FailingCCF(parent, n) {
    override final def onCompletion(caller: CountedCompleter[_]): Unit = {
      val p = getCompleter.asInstanceOf[ForkJoinPool8Test.FailingCCF]
      val n = number + rnumber
      if (p != null) p.number = n
      else number = n
    }
  }
  final class RFCCF(parent: CountedCompleter[_], n: Int)
      extends ForkJoinPool8Test.FailingCCF(parent, n) {
    override final def onCompletion(caller: CountedCompleter[_]): Unit = {
      completeExceptionally(new ForkJoinPool8Test.FJException)
    }
  }
}

class ForkJoinPool8Test extends JSR166Test {
  import ForkJoinPool8Test._

  /** Common pool exists and has expected parallelism.
   */
  @Test def testCommonPoolParallelism(): Unit = {
    assertEquals(
      ForkJoinPool.getCommonPoolParallelism,
      ForkJoinPool.commonPool.getParallelism
    )
  }

  /** Common pool cannot be shut down
   */
  @Test def testCommonPoolShutDown(): Unit = {
    assertFalse(ForkJoinPool.commonPool.isShutdown)
    assertFalse(ForkJoinPool.commonPool.isTerminating)
    assertFalse(ForkJoinPool.commonPool.isTerminated)
    ForkJoinPool.commonPool.shutdown()
    assertFalse(ForkJoinPool.commonPool.isShutdown)
    assertFalse(ForkJoinPool.commonPool.isTerminating)
    assertFalse(ForkJoinPool.commonPool.isTerminated)
    ForkJoinPool.commonPool.shutdownNow
    assertFalse(ForkJoinPool.commonPool.isShutdown)
    assertFalse(ForkJoinPool.commonPool.isTerminating)
    assertFalse(ForkJoinPool.commonPool.isTerminated)
  }
  /*
   * All of the following test methods are adaptations of those for
   * RecursiveAction and CountedCompleter, but with all actions
   * executed in the common pool, generally implicitly via
   * checkInvoke.
   */
  private def checkInvoke(a: ForkJoinTask[_]): Unit = {
    checkNotDone(a)
    assertNull(a.invoke)
    checkCompletedNormally(a)
  }
  def checkNotDone(a: ForkJoinTask[_]): Unit = {
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
        case fail: Throwable               =>
          threadUnexpectedException(fail)
      }
    }
    try {
      a.get(randomExpiredTimeout(), randomTimeUnit())
      shouldThrow()
    } catch {
      case success: TimeoutException =>
      case fail: Throwable           =>
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
    try {
      assertNull(a.get())
      assertNull(a.get(randomTimeout(), randomTimeUnit()))
    } catch {
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
  }

  def checkCancelled(a: ForkJoinTask[_]): Unit = {
    assertTrue(a.isDone)
    assertTrue(a.isCancelled)
    assertFalse(a.isCompletedNormally)
    assertTrue(a.isCompletedAbnormally)
    assertTrue(a.getException.isInstanceOf[CancellationException])
    assertNull(a.getRawResult)
    try {
      a.join()
      shouldThrow()
    } catch {
      case success: CancellationException =>
      case fail: Throwable                =>
        threadUnexpectedException(fail)
    }
    try {
      a.get
      shouldThrow()
    } catch {
      case success: CancellationException =>
      case fail: Throwable                =>
        threadUnexpectedException(fail)
    }
    try {
      a.get(randomTimeout(), randomTimeUnit())
      shouldThrow()
    } catch {
      case success: CancellationException =>
      case fail: Throwable                =>
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
    checkInvoke(a)
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
    checkInvoke(a)
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
    checkInvoke(a)
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
        f.completeExceptionally(new ForkJoinPool8Test.FJException)
        assertSame(f, f.fork)
        currentThread.interrupt()
        try {
          f.join
          shouldThrow()
        } catch {
          case success: ForkJoinPool8Test.FJException =>
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
        f.completeExceptionally(new ForkJoinPool8Test.FJException)
        assertSame(f, f.fork)
        currentThread.interrupt()
        f.quietlyJoin
        Thread.interrupted
        checkCompletedAbnormally(f, f.getException)
      }
    }
    checkInvoke(a)
    a.reinitialize()
    checkInvoke(a)
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
    checkInvoke(a)
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
    checkInvoke(a)
  }

  /** timed get with null time unit throws NPE
   */
  @Test def testForkTimedGetNPE(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        assertSame(f, f.fork)
        assertThrows(
          classOf[NullPointerException],
          () => f.get(randomTimeout(), null)
        )
      }
    }
    checkInvoke(a)
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
    checkInvoke(a)
  }

  /** invoke task throws exception when task completes abnormally
   */
  @Test def testAbnormalInvoke(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.FailingFibAction(8)
        try {
          f.invoke
          shouldThrow()
        } catch {
          case success: ForkJoinPool8Test.FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    checkInvoke(a)
  }

  /** quietlyInvoke task returns when task completes abnormally
   */
  @Test def testAbnormalQuietlyInvoke(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.FailingFibAction(8)
        f.quietlyInvoke()
        assertTrue(f.getException.isInstanceOf[ForkJoinPool8Test.FJException])
        checkCompletedAbnormally(f, f.getException)
      }
    }
    checkInvoke(a)
  }

  /** join of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.FailingFibAction(8)
        assertSame(f, f.fork)
        try {
          f.join
          shouldThrow()
        } catch {
          case success: ForkJoinPool8Test.FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    checkInvoke(a)
  }

  /** get of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.FailingFibAction(8)
        assertSame(f, f.fork)
        try {
          f.get
          shouldThrow()
        } catch {
          case success: ExecutionException =>
            val cause = success.getCause
            assertTrue(cause.isInstanceOf[ForkJoinPool8Test.FJException])
            checkCompletedAbnormally(f, cause)
        }
      }
    }
    checkInvoke(a)
  }

  /** timed get of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkTimedGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.FailingFibAction(8)
        assertSame(f, f.fork)
        try {
          f.get(LONG_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case success: ExecutionException =>
            val cause = success.getCause
            assertTrue(cause.isInstanceOf[ForkJoinPool8Test.FJException])
            checkCompletedAbnormally(f, cause)
        }
      }
    }
    checkInvoke(a)
  }

  /** quietlyJoin of a forked task returns when task completes abnormally
   */
  @Test def testAbnormalForkQuietlyJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.FailingFibAction(8)
        assertSame(f, f.fork)
        f.quietlyJoin()
        assertTrue(f.getException.isInstanceOf[ForkJoinPool8Test.FJException])
        checkCompletedAbnormally(f, f.getException)
      }
    }
    checkInvoke(a)
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
    checkInvoke(a)
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
    checkInvoke(a)
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
    checkInvoke(a)
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
    checkInvoke(a)
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
    checkInvoke(a)
  }

  /** inForkJoinPool of non-FJ task returns false
   */
  @Test def testInForkJoinPool2(): Unit = {
    AssumesHelper.assumeNotExecutedInForkJoinPool()
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        assertFalse(ForkJoinTask.inForkJoinPool)
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
    checkInvoke(a)
  }

  /** A reinitialized abnormally completed task may be re-invoked
   */
  @Test def testReinitializeAbnormal(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.FailingFibAction(8)
        checkNotDone(f)
        for (i <- 0 until 3) {
          try {
            f.invoke
            shouldThrow()
          } catch {
            case success: ForkJoinPool8Test.FJException =>
              checkCompletedAbnormally(f, success)
          }
          f.reinitialize()
          checkNotDone(f)
        }
      }
    }
    checkInvoke(a)
  }

  /** invoke task throws exception after invoking completeExceptionally
   */
  @Test def testCompleteExceptionally(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        f.completeExceptionally(new ForkJoinPool8Test.FJException)
        try {
          f.invoke
          shouldThrow()
        } catch {
          case success: ForkJoinPool8Test.FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    checkInvoke(a)
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
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(t1, t2) invokes all task arguments
   */
  @Test def testInvokeAll2(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        val g = new FibAction(9)
        ForkJoinTask.invokeAll(f, g)
        checkCompletedNormally(f)
        assertEquals(21, f.result)
        checkCompletedNormally(g)
        assertEquals(34, g.result)
      }
    }
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(tasks) with 1 argument invokes task
   */
  @Test def testInvokeAll1(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        ForkJoinTask.invokeAll(f)
        checkCompletedNormally(f)
        assertEquals(21, f.result)
      }
    }
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(tasks) with > 2 argument invokes tasks
   */
  @Test def testInvokeAll3(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        val g = new FibAction(9)
        val h = new FibAction(7)
        ForkJoinTask.invokeAll(f, g, h)
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
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(collection) invokes all tasks in the collection
   */
  @Test def testInvokeAllCollection(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        val g = new FibAction(9)
        val h = new FibAction(7)
        val set = new HashSet[RecursiveAction]
        set.add(f)
        set.add(g)
        set.add(h)
        ForkJoinTask.invokeAll(set)
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
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(tasks) with any null task throws NPE
   */
  @Test def testInvokeAllNPE(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        val g = new FibAction(9)
        val h: FibAction = null
        try {
          ForkJoinTask.invokeAll(f, g, h)
          shouldThrow()
        } catch {
          case success: NullPointerException =>

        }
      }
    }
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(t1, t2) throw exception if any task does
   */
  @Test def testAbnormalInvokeAll2(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        val g = new ForkJoinPool8Test.FailingFibAction(9)
        try {
          ForkJoinTask.invokeAll(f, g)
          shouldThrow()
        } catch {
          case success: ForkJoinPool8Test.FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(tasks) with 1 argument throws exception if task
   *  does
   */
  @Test def testAbnormalInvokeAll1(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new ForkJoinPool8Test.FailingFibAction(9)
        try {
          ForkJoinTask.invokeAll(g)
          shouldThrow()
        } catch {
          case success: ForkJoinPool8Test.FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(tasks) with > 2 argument throws exception if any
   *  task does
   */
  @Test def testAbnormalInvokeAll3(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FibAction(8)
        val g = new ForkJoinPool8Test.FailingFibAction(9)
        val h = new FibAction(7)
        try {
          ForkJoinTask.invokeAll(f, g, h)
          shouldThrow()
        } catch {
          case success: ForkJoinPool8Test.FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(collection) throws exception if any task does
   */
  @Test def testAbnormalInvokeAllCollection(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.FailingFibAction(8)
        val g = new FibAction(9)
        val h = new FibAction(7)
        val set = new HashSet[RecursiveAction]
        set.add(f)
        set.add(g)
        set.add(h)
        try {
          ForkJoinTask.invokeAll(set)
          shouldThrow()
        } catch {
          case success: ForkJoinPool8Test.FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    checkInvoke(a)
  }

  /** invoke returns when task completes normally. isCompletedAbnormally and
   *  isCancelled return false for normally completed tasks; getRawResult
   *  returns null.
   */
  @Test def testInvokeCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
        assertNull(f.invoke)
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    checkInvoke(a)
  }

  /** quietlyInvoke task returns when task completes normally.
   *  isCompletedAbnormally and isCancelled return false for normally completed
   *  tasks
   */
  @Test def testQuietlyInvokeCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
        f.quietlyInvoke()
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    checkInvoke(a)
  }

  /** join of a forked task returns when task completes
   */
  @Test def testForkJoinCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
        assertSame(f, f.fork)
        assertNull(f.join)
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    checkInvoke(a)
  }

  /** get of a forked task returns when task completes
   */
  @Test def testForkGetCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
        assertSame(f, f.fork)
        assertNull(f.get)
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    checkInvoke(a)
  }

  /** timed get of a forked task returns when task completes
   */
  @Test def testForkTimedGetCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
        assertSame(f, f.fork)
        assertNull(f.get(LONG_DELAY_MS, MILLISECONDS))
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    checkInvoke(a)
  }

  /** timed get with null time unit throws NPE
   */
  @Test def testForkTimedGetNPECC(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
        assertSame(f, f.fork)
        assertThrows(
          classOf[java.lang.NullPointerException],
          () => f.get(randomTimeout(), null)
        )
      }
    }
    checkInvoke(a)
  }

  /** quietlyJoin of a forked task returns when task completes
   */
  @Test def testForkQuietlyJoinCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
        assertSame(f, f.fork)
        f.quietlyJoin()
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    checkInvoke(a)
  }

  /** invoke task throws exception when task completes abnormally
   */
  @Test def testAbnormalInvokeCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LFCCF(null, 8)
        try {
          f.invoke
          shouldThrow()
        } catch {
          case success: ForkJoinPool8Test.FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    checkInvoke(a)
  }

  /** quietlyInvoke task returns when task completes abnormally
   */
  @Test def testAbnormalQuietlyInvokeCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LFCCF(null, 8)
        f.quietlyInvoke()
        assertTrue(f.getException.isInstanceOf[ForkJoinPool8Test.FJException])
        checkCompletedAbnormally(f, f.getException)
      }
    }
    checkInvoke(a)
  }

  /** join of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkJoinCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LFCCF(null, 8)
        assertSame(f, f.fork)
        try {
          f.join
          shouldThrow()
        } catch {
          case success: ForkJoinPool8Test.FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    checkInvoke(a)
  }

  /** get of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkGetCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LFCCF(null, 8)
        assertSame(f, f.fork)
        try {
          f.get
          shouldThrow()
        } catch {
          case success: ExecutionException =>
            val cause = success.getCause
            assertTrue(cause.isInstanceOf[ForkJoinPool8Test.FJException])
            checkCompletedAbnormally(f, cause)
        }
      }
    }
    checkInvoke(a)
  }

  /** timed get of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkTimedGetCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LFCCF(null, 8)
        assertSame(f, f.fork)
        try {
          f.get(LONG_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case success: ExecutionException =>
            val cause = success.getCause
            assertTrue(cause.isInstanceOf[ForkJoinPool8Test.FJException])
            checkCompletedAbnormally(f, cause)
        }
      }
    }
    checkInvoke(a)
  }

  /** quietlyJoin of a forked task returns when task completes abnormally
   */
  @Test def testAbnormalForkQuietlyJoinCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LFCCF(null, 8)
        assertSame(f, f.fork)
        f.quietlyJoin()
        assertTrue(f.getException.isInstanceOf[ForkJoinPool8Test.FJException])
        checkCompletedAbnormally(f, f.getException)
      }
    }
    checkInvoke(a)
  }

  /** invoke task throws exception when task cancelled
   */
  @Test def testCancelledInvokeCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
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
    checkInvoke(a)
  }

  /** join of a forked task throws exception when task cancelled
   */
  @Test def testCancelledForkJoinCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
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
    checkInvoke(a)
  }

  /** get of a forked task throws exception when task cancelled
   */
  @Test def testCancelledForkGetCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
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
    checkInvoke(a)
  }

  /** timed get of a forked task throws exception when task cancelled
   */
  @throws[Exception]
  @Test def testCancelledForkTimedGetCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
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
    checkInvoke(a)
  }

  /** quietlyJoin of a forked task returns when task cancelled
   */
  @Test def testCancelledForkQuietlyJoinCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
        assertTrue(f.cancel(true))
        assertSame(f, f.fork)
        f.quietlyJoin()
        checkCancelled(f)
      }
    }
    checkInvoke(a)
  }

  /** getPool of non-FJ task returns null
   */
  @Test def testGetPool2CC(): Unit = {
    AssumesHelper.assumeNotExecutedInForkJoinPool()
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = { assertNull(ForkJoinTask.getPool) }
    }
    assertNull(a.invoke)
  }

  /** inForkJoinPool of non-FJ task returns false
   */
  @Test def testInForkJoinPool2CC(): Unit = {
    AssumesHelper.assumeNotExecutedInForkJoinPool()
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        assertFalse(ForkJoinTask.inForkJoinPool)
      }
    }
    assertNull(a.invoke)
  }

  /** setRawResult(null) succeeds
   */
  @Test def testSetRawResultCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        setRawResult(null)
        assertNull(getRawResult)
      }
    }
    assertNull(a.invoke)
  }

  /** invoke task throws exception after invoking completeExceptionally
   */
  @Test def testCompleteExceptionally2CC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
        f.completeExceptionally(new ForkJoinPool8Test.FJException)
        try {
          f.invoke
          shouldThrow()
        } catch {
          case success: ForkJoinPool8Test.FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(t1, t2) invokes all task arguments
   */
  @Test def testInvokeAll2CC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
        val g = new ForkJoinPool8Test.LCCF(null, 9)
        ForkJoinTask.invokeAll(f, g)
        assertEquals(21, f.number)
        assertEquals(34, g.number)
        checkCompletedNormally(f)
        checkCompletedNormally(g)
      }
    }
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(tasks) with 1 argument invokes task
   */
  @Test def testInvokeAll1CC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
        ForkJoinTask.invokeAll(f)
        checkCompletedNormally(f)
        assertEquals(21, f.number)
      }
    }
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(tasks) with > 2 argument invokes tasks
   */
  @Test def testInvokeAll3CC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
        val g = new ForkJoinPool8Test.LCCF(null, 9)
        val h = new ForkJoinPool8Test.LCCF(null, 7)
        ForkJoinTask.invokeAll(f, g, h)
        assertEquals(21, f.number)
        assertEquals(34, g.number)
        assertEquals(13, h.number)
        checkCompletedNormally(f)
        checkCompletedNormally(g)
        checkCompletedNormally(h)
      }
    }
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(collection) invokes all tasks in the collection
   */
  @Test def testInvokeAllCollectionCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
        val g = new ForkJoinPool8Test.LCCF(null, 9)
        val h = new ForkJoinPool8Test.LCCF(null, 7)
        val set = new HashSet[CountedCompleter[_]]
        set.add(f)
        set.add(g)
        set.add(h)
        ForkJoinTask.invokeAll(set)
        assertEquals(21, f.number)
        assertEquals(34, g.number)
        assertEquals(13, h.number)
        checkCompletedNormally(f)
        checkCompletedNormally(g)
        checkCompletedNormally(h)
      }
    }
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(tasks) with any null task throws NPE
   */
  @Test def testInvokeAllNPECC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
        val g = new ForkJoinPool8Test.LCCF(null, 9)
        val h: ForkJoinPool8Test.CCF = null
        assertThrows(
          classOf[NullPointerException],
          () => ForkJoinTask.invokeAll(f, g, h)
        )
      }
    }
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(t1, t2) throw exception if any task does
   */
  @Test def testAbnormalInvokeAll2CC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
        val g = new ForkJoinPool8Test.LFCCF(null, 9)
        try {
          ForkJoinTask.invokeAll(f, g)
          shouldThrow()
        } catch {
          case success: ForkJoinPool8Test.FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(tasks) with 1 argument throws exception if task
   *  does
   */
  @Test def testAbnormalInvokeAll1CC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new ForkJoinPool8Test.LFCCF(null, 9)
        try {
          ForkJoinTask.invokeAll(g)
          shouldThrow()
        } catch {
          case success: ForkJoinPool8Test.FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(tasks) with > 2 argument throws exception if any
   *  task does
   */
  @Test def testAbnormalInvokeAll3CC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LCCF(null, 8)
        val g = new ForkJoinPool8Test.LFCCF(null, 9)
        val h = new ForkJoinPool8Test.LCCF(null, 7)
        try {
          ForkJoinTask.invokeAll(f, g, h)
          shouldThrow()
        } catch {
          case success: ForkJoinPool8Test.FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    checkInvoke(a)
  }

  /** ForkJoinTask.invokeAll(collection) throws exception if any task does
   */
  @Test def testAbnormalInvokeAllCollectionCC(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new ForkJoinPool8Test.LFCCF(null, 8)
        val g = new ForkJoinPool8Test.LCCF(null, 9)
        val h = new ForkJoinPool8Test.LCCF(null, 7)
        val set = new HashSet[CountedCompleter[_]]
        set.add(f)
        set.add(g)
        set.add(h)
        val ex = assertThrows(
          classOf[ForkJoinPool8Test.FJException],
          () => ForkJoinTask.invokeAll(set)
        )
        checkCompletedAbnormally(f, ex)
      }
    }
    checkInvoke(a)
  }

  /** awaitQuiescence by a worker is equivalent in effect to
   *  ForkJoinTask.helpQuiesce()
   */
  @throws[Exception]
  @Test def testAwaitQuiescence1(): Unit =
    usingPoolCleaner(new ForkJoinPool()) { p =>
      val startTime = System.nanoTime
      assertTrue(p.isQuiescent)
      val a: CheckedRecursiveAction = () => {
        val f = new FibAction(8)
        assertSame(f, f.fork)
        assertSame(p, ForkJoinTask.getPool)
        val quiescent = p.awaitQuiescence(LONG_DELAY_MS, MILLISECONDS)
        assertTrue(quiescent)
        assertFalse(p.isQuiescent)
        while (!f.isDone) {
          assertFalse(p.getAsyncMode)
          assertFalse(p.isShutdown)
          assertFalse(p.isTerminating)
          assertFalse(p.isTerminated)
          Thread.`yield`()
        }
        assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
        assertFalse(p.isQuiescent)
        assertEquals(0, ForkJoinTask.getQueuedTaskCount)
        assertEquals(21, f.result)
      }
      p.execute(a)
      while (!a.isDone || !p.isQuiescent) {
        assertFalse(p.getAsyncMode)
        assertFalse(p.isShutdown)
        assertFalse(p.isTerminating)
        assertFalse(p.isTerminated)
        Thread.`yield`()
      }
      assertEquals(0, p.getQueuedTaskCount)
      assertFalse(p.getAsyncMode)
      assertEquals(0, p.getQueuedSubmissionCount)
      assertFalse(p.hasQueuedSubmissions)
      while (p.getActiveThreadCount != 0 &&
          millisElapsedSince(startTime) < LONG_DELAY_MS) Thread.`yield`()
      assertFalse(p.isShutdown)
      assertFalse(p.isTerminating)
      assertFalse(p.isTerminated)
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    }

  /** awaitQuiescence returns when pool isQuiescent() or the indicated timeout
   *  elapsed
   */
  @throws[Exception]
  @Test def testAwaitQuiescence2(): Unit = {
    /*
     * """It is possible to disable or limit the use of threads in the
     * common pool by setting the parallelism property to zero. However
     * doing so may cause unjoined tasks to never be executed."""
     */
    if ("0" == System.getProperty(
          "java.util.concurrent.ForkJoinPool.common.parallelism"
        )) return
    usingPoolCleaner(new ForkJoinPool()) { p =>
      assertTrue(p.isQuiescent)
      val startTime = System.nanoTime
      val a: CheckedRecursiveAction = () => {
        val f = new FibAction(8)
        assertSame(f, f.fork)
        while (!f.isDone && millisElapsedSince(startTime) < LONG_DELAY_MS) {
          assertFalse(p.getAsyncMode)
          assertFalse(p.isShutdown)
          assertFalse(p.isTerminating)
          assertFalse(p.isTerminated)
          Thread.`yield`()
        }
        assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
        assertEquals(0, ForkJoinTask.getQueuedTaskCount)
        assertEquals(21, f.result)
      }
      p.execute(a)
      assertTrue(p.awaitQuiescence(LONG_DELAY_MS, MILLISECONDS))
      assertTrue(p.isQuiescent)
      assertTrue(a.isDone)
      assertEquals(0, p.getQueuedTaskCount)
      assertFalse(p.getAsyncMode)
      assertEquals(0, p.getQueuedSubmissionCount)
      assertFalse(p.hasQueuedSubmissions)
      while (p.getActiveThreadCount != 0 &&
          millisElapsedSince(startTime) < LONG_DELAY_MS) {
        Thread.`yield`()
      }
      assertFalse(p.isShutdown)
      assertFalse(p.isTerminating)
      assertFalse(p.isTerminated)
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
    }
  }
}
