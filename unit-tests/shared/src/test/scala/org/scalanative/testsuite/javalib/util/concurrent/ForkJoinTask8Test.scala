/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util
import java.util.concurrent.*

import org.junit.*
import org.junit.Assert.*
import scala.scalanative.junit.utils.AssumesHelper
import JSR166Test.*

import scala.util.control.Breaks.*

object ForkJoinTask8Test {
  /*
   * Testing notes: This differs from ForkJoinTaskTest mainly by
   * defining a version of BinaryAsyncAction that uses JDK8 task
   * tags for control state, thereby testing getForkJoinTaskTag,
   * setForkJoinTaskTag, and compareAndSetForkJoinTaskTag across
   * various contexts. Most of the test methods using it are
   * otherwise identical, but omitting retest of those dealing with
   * cancellation, which is not represented in this tag scheme.
   */
  val INITIAL_STATE: Short = -1
  val COMPLETE_STATE: Short = 0
  val EXCEPTION_STATE: Short = 1

  // Runs with "mainPool" use > 1 thread. singletonPool tests use 1
  val mainPoolSize: Int = Math.max(2, Runtime.getRuntime.availableProcessors)

  def mainPool = new ForkJoinPool(mainPoolSize)
  def singletonPool = new ForkJoinPool(1)
  def asyncSingletonPool = new ForkJoinPool(
    1,
    ForkJoinPool.defaultForkJoinWorkerThreadFactory,
    null,
    true
  )
  final class FJException extends RuntimeException {}

  abstract class BinaryAsyncAction protected extends ForkJoinTask[Void] {
    setForkJoinTaskTag(INITIAL_STATE)
    @volatile private var parent: BinaryAsyncAction = _
    @volatile private var sibling: BinaryAsyncAction = _
    override final def getRawResult: Void = null
    override protected final def setRawResult(mustBeNull: Void): Unit = {}
    final def linkSubtasks(
        x: BinaryAsyncAction,
        y: BinaryAsyncAction
    ): Unit = {
      y.parent = this
      x.parent = this
      x.sibling = y
      y.sibling = x
    }
    protected def onComplete(
        x: BinaryAsyncAction,
        y: BinaryAsyncAction
    ): Unit = {
      if (this.getForkJoinTaskTag != COMPLETE_STATE || x.getForkJoinTaskTag != COMPLETE_STATE || y.getForkJoinTaskTag != COMPLETE_STATE) {
        completeThisExceptionally(new FJException)
      }
    }
    protected def onException = true
    def linkAndForkSubtasks(
        x: BinaryAsyncAction,
        y: BinaryAsyncAction
    ): Unit = {
      linkSubtasks(x, y)
      y.fork
      x.fork
    }
    private def completeThis(): Unit = {
      setForkJoinTaskTag(COMPLETE_STATE)
      super.complete(null)
    }
    private def completeThisExceptionally(ex: Throwable): Unit = {
      setForkJoinTaskTag(EXCEPTION_STATE)
      super.completeExceptionally(ex)
    }
    override def cancel(mayInterruptIfRunning: Boolean): Boolean = {
      if (super.cancel(mayInterruptIfRunning)) {
        completeExceptionally(new FJException)
        return true
      }
      false
    }
    final def complete(): Unit = {
      var a = this
      var break = false
      while (!break) {
        val s = a.sibling
        val p = a.parent
        a.sibling = null
        a.parent = null
        a.completeThis()
        if (p == null ||
            p.compareAndSetForkJoinTaskTag(INITIAL_STATE, COMPLETE_STATE)) {
          break = true
        }
        if (!break) {
          try p.onComplete(a, s)
          catch {
            case rex: Throwable =>
              p.completeExceptionally(rex)
              break = true
          }
          a = p
        }
      }
    }
    override final def completeExceptionally(ex: Throwable): Unit = {
      var a = this
      breakable {
        while (true) {
          a.completeThisExceptionally(ex)
          val s = a.sibling
          if (s != null && !s.isDone) s.completeExceptionally(ex)
          a = a.parent
          if (a == null) break()
        }
      }
    }
    final def getParent: BinaryAsyncAction = parent
    def getSibling: BinaryAsyncAction = sibling
    override def reinitialize(): Unit = {
      sibling = null
      parent = sibling
      super.reinitialize()
    }
  }
  final class FailingAsyncFib(var number: Int) extends BinaryAsyncAction {
    override final def exec: Boolean = {
      try {
        var f = this
        var n = f.number
        while (n > 1) {
          val p = f
          val r = new FailingAsyncFib(n - 2)
          f = new FailingAsyncFib({ n -= 1; n })
          p.linkSubtasks(r, f)
          r.fork
        }
        f.complete()
      } catch {
        case ex: Throwable =>
          compareAndSetForkJoinTaskTag(INITIAL_STATE, EXCEPTION_STATE)
      }
      if (getForkJoinTaskTag == EXCEPTION_STATE)
        throw new FJException
      false
    }
    override protected def onComplete(
        x: BinaryAsyncAction,
        y: BinaryAsyncAction
    ): Unit = { completeExceptionally(new FJException) }
  }
}

class ForkJoinTask8Test extends JSR166Test {
  import ForkJoinTask.*
  import ForkJoinTask8Test.*

  // Compute fib naively and efficiently
  final val fib: Array[Int] = {
    val fib = new Array[Int](10)
    fib(0) = 0
    fib(1) = 1
    for (i <- 2 until fib.length) { fib(i) = fib(i - 1) + fib(i - 2) }
    fib
  }

  private def testInvokeOnPool(pool: ForkJoinPool, a: RecursiveAction): Unit =
    usingPoolCleaner(pool) { pool =>
      assertFalse(a.isDone)
      assertFalse(a.isCompletedNormally)
      assertFalse(a.isCompletedAbnormally)
      assertFalse(a.isCancelled)
      assertNull(a.getException)
      assertNull(a.getRawResult)

      assertNull(pool.invoke(a))

      assertTrue(a.isDone)
      assertTrue(a.isCompletedNormally)
      assertFalse(a.isCompletedAbnormally)
      assertFalse(a.isCancelled)
      assertNull(a.getException)
      assertNull(a.getRawResult)
    }

  def checkNotDone(a: ForkJoinTask[?]): Unit = {
    assertFalse(a.isDone)
    assertFalse(a.isCompletedNormally)
    assertFalse(a.isCompletedAbnormally)
    assertFalse(a.isCancelled)
    assertNull(a.getException)
    assertNull(a.getRawResult)
    if (a.isInstanceOf[BinaryAsyncAction])
      assertEquals(
        INITIAL_STATE,
        a.asInstanceOf[BinaryAsyncAction].getForkJoinTaskTag
      )
    try {
      a.get(randomExpiredTimeout(), randomTimeUnit())
      shouldThrow()
    } catch {
      case success: TimeoutException =>
      case fail: Throwable           =>
        threadUnexpectedException(fail)
    }
  }

  def checkCompletedNormally[T](a: ForkJoinTask[T]): Unit = {
    checkCompletedNormally(a, null.asInstanceOf[T])
  }

  def checkCompletedNormally[T](a: ForkJoinTask[T], expectedValue: T): Unit = {
    assertTrue(a.isDone)
    assertFalse(a.isCancelled)
    assertTrue(a.isCompletedNormally)
    assertFalse(a.isCompletedAbnormally)
    assertNull(a.getException)
    assertSame(expectedValue, a.getRawResult)
    if (a.isInstanceOf[BinaryAsyncAction])
      assertEquals(
        COMPLETE_STATE,
        a.asInstanceOf[BinaryAsyncAction].getForkJoinTaskTag
      )
    locally {
      Thread.currentThread.interrupt()
      val startTime = System.nanoTime
      assertSame(expectedValue, a.join)
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
      Thread.interrupted
    }

    locally {
      Thread.currentThread.interrupt()
      val startTime = System.nanoTime
      a.quietlyJoin() // should be no-op
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
      Thread.interrupted
    }
    assertFalse(a.cancel(false))
    assertFalse(a.cancel(true))
    try {
      assertSame(expectedValue, a.get())
      assertSame(expectedValue, a.get(randomTimeout(), randomTimeUnit()))
    } catch {
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
  }

  def checkCompletedAbnormally(a: ForkJoinTask[?], t: Throwable): Unit = {
    assertTrue(a.isDone)
    assertFalse(a.isCancelled)
    assertFalse(a.isCompletedNormally)
    assertTrue(a.isCompletedAbnormally)
    assertSame(t.getClass, a.getException.getClass)
    assertNull(a.getRawResult)
    assertFalse(a.cancel(false))
    assertFalse(a.cancel(true))
    if (a.isInstanceOf[BinaryAsyncAction])
      assertTrue(
        a.asInstanceOf[BinaryAsyncAction].getForkJoinTaskTag != INITIAL_STATE
      )
    try {
      Thread.currentThread.interrupt()
      a.join
      shouldThrow()
    } catch {
      case expected: Throwable =>
        assertSame(t.getClass, expected.getClass)
    }
    Thread.interrupted
    val startTime = System.nanoTime
    a.quietlyJoin() // should be no-op

    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)

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
  final class AsyncFib(var number: Int) extends BinaryAsyncAction {
    val expectedResult = fib(number)
    override final def exec: Boolean = {
      try {
        var f = this
        var n = f.number
        while (n > 1) {
          val p = f
          val r = new AsyncFib(n - 2)
          f = new AsyncFib({ n -= 1; n })
          p.linkSubtasks(r, f)
          r.fork
        }
        f.complete()
      } catch {
        case ex: Throwable =>
          compareAndSetForkJoinTaskTag(
            INITIAL_STATE,
            EXCEPTION_STATE
          )
      }
      if (getForkJoinTaskTag == EXCEPTION_STATE)
        throw new FJException
      false
    }
    override protected def onComplete(
        x: BinaryAsyncAction,
        y: BinaryAsyncAction
    ): Unit = {
      number = x.asInstanceOf[AsyncFib].number + y
        .asInstanceOf[AsyncFib]
        .number
      super.onComplete(x, y)
    }
    def checkCompletedNormally(): Unit = {
      assertEquals(expectedResult, number)
      ForkJoinTask8Test.this.checkCompletedNormally(this)
    }
  }

  /** invoke returns when task completes normally. isCompletedAbnormally and
   *  isCancelled return false for normally completed tasks; getRawResult
   *  returns null.
   */
  @Test def testInvoke(): Unit = { testInvoke(mainPool) }
  @Test def testInvoke_Singleton(): Unit = {
    testInvoke(singletonPool)
  }
  def testInvoke(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertNull(f.invoke)
        f.checkCompletedNormally()
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** quietlyInvoke task returns when task completes normally.
   *  isCompletedAbnormally and isCancelled return false for normally completed
   *  tasks
   */
  @Test def testQuietlyInvoke(): Unit = {
    testQuietlyInvoke(mainPool)
  }
  @Test def testQuietlyInvoke_Singleton(): Unit = {
    testQuietlyInvoke(singletonPool)
  }
  def testQuietlyInvoke(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        f.quietlyInvoke()
        f.checkCompletedNormally()
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** join of a forked task returns when task completes
   */
  @Test def testForkJoin(): Unit = { testForkJoin(mainPool) }
  @Test def testForkJoin_Singleton(): Unit = {
    testForkJoin(singletonPool)
  }
  def testForkJoin(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertNull(f.join)
        f.checkCompletedNormally()
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** get of a forked task returns when task completes
   */
  @Test def testForkGet(): Unit = { testForkGet(mainPool) }
  @Test def testForkGet_Singleton(): Unit = {
    testForkGet(singletonPool)
  }
  def testForkGet(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertNull(f.get)
        f.checkCompletedNormally()
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** timed get of a forked task returns when task completes
   */
  @Test def testForkTimedGet(): Unit = {
    testForkTimedGet(mainPool)
  }
  @Test def testForkTimedGet_Singleton(): Unit = {
    testForkTimedGet(singletonPool)
  }
  def testForkTimedGet(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertNull(f.get(LONG_DELAY_MS, MILLISECONDS))
        f.checkCompletedNormally()
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** timed get with null time unit throws NullPointerException
   */
  @Test def testForkTimedGetNullTimeUnit(): Unit = {
    testForkTimedGetNullTimeUnit(mainPool)
  }
  @Test def testForkTimedGetNullTimeUnit_Singleton(): Unit = {
    testForkTimedGet(singletonPool)
  }
  def testForkTimedGetNullTimeUnit(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertThrows(
          classOf[NullPointerException],
          () => f.get(randomTimeout(), null)
        )
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** quietlyJoin of a forked task returns when task completes
   */
  @Test def testForkQuietlyJoin(): Unit = {
    testForkQuietlyJoin(mainPool)
  }
  @Test def testForkQuietlyJoin_Singleton(): Unit = {
    testForkQuietlyJoin(singletonPool)
  }
  def testForkQuietlyJoin(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        f.quietlyJoin()
        f.checkCompletedNormally()
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** helpQuiesce returns when tasks are complete. getQueuedTaskCount returns 0
   *  when quiescent
   */
  @Test def testForkHelpQuiesce(): Unit = {
    testForkHelpQuiesce(mainPool)
  }
  @Test def testForkHelpQuiesce_Singleton(): Unit = {
    testForkHelpQuiesce(singletonPool)
  }
  def testForkHelpQuiesce(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        helpQuiesce
        while (!f.isDone) { // wait out race
        }
        assertEquals(0, getQueuedTaskCount)
        f.checkCompletedNormally()
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** invoke task throws exception when task completes abnormally
   */
  @Test def testAbnormalInvoke(): Unit = {
    testAbnormalInvoke(mainPool)
  }
  @Test def testAbnormalInvoke_Singleton(): Unit = {
    testAbnormalInvoke(singletonPool)
  }
  def testAbnormalInvoke(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FailingAsyncFib(8)
        try {
          f.invoke
          shouldThrow()
        } catch {
          case success: FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** quietlyInvoke task returns when task completes abnormally
   */
  @Test def testAbnormalQuietlyInvoke(): Unit = {
    testAbnormalQuietlyInvoke(mainPool)
  }
  @Test def testAbnormalQuietlyInvoke_Singleton(): Unit = {
    testAbnormalQuietlyInvoke(singletonPool)
  }
  def testAbnormalQuietlyInvoke(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FailingAsyncFib(8)
        f.quietlyInvoke()
        assertTrue(f.getException.isInstanceOf[FJException])
        checkCompletedAbnormally(f, f.getException)
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** join of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkJoin(): Unit = {
    testAbnormalForkJoin(mainPool)
  }
  @Test def testAbnormalForkJoin_Singleton(): Unit = {
    testAbnormalForkJoin(singletonPool)
  }
  def testAbnormalForkJoin(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FailingAsyncFib(8)
        assertSame(f, f.fork)
        try {
          f.join
          shouldThrow()
        } catch {
          case success: FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** get of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkGet(): Unit = {
    testAbnormalForkGet(mainPool)
  }
  @Test def testAbnormalForkGet_Singleton(): Unit = {
    testAbnormalForkJoin(singletonPool)
  }
  def testAbnormalForkGet(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new FailingAsyncFib(8)
        assertSame(f, f.fork)
        try {
          f.get
          shouldThrow()
        } catch {
          case success: ExecutionException =>
            val cause = success.getCause
            assertTrue(cause.isInstanceOf[FJException])
            checkCompletedAbnormally(f, cause)
        }
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** timed get of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkTimedGet(): Unit = {
    testAbnormalForkTimedGet(mainPool)
  }
  @Test def testAbnormalForkTimedGet_Singleton(): Unit = {
    testAbnormalForkTimedGet(singletonPool)
  }
  def testAbnormalForkTimedGet(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      protected def realCompute(): Unit = {
        val f = new FailingAsyncFib(8)
        assertSame(f, f.fork)
        try {
          f.get(LONG_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case success: ExecutionException =>
            val cause = success.getCause
            assertTrue(cause.isInstanceOf[FJException])
            checkCompletedAbnormally(f, cause)
        }
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** quietlyJoin of a forked task returns when task completes abnormally
   */
  @Test def testAbnormalForkQuietlyJoin(): Unit = {
    testAbnormalForkQuietlyJoin(mainPool)
  }
  @Test def testAbnormalForkQuietlyJoin_Singleton(): Unit = {
    testAbnormalForkQuietlyJoin(singletonPool)
  }
  def testAbnormalForkQuietlyJoin(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FailingAsyncFib(8)
        assertSame(f, f.fork)
        f.quietlyJoin()
        assertTrue(f.getException.isInstanceOf[FJException])
        checkCompletedAbnormally(f, f.getException)
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** getPool of executing task returns its pool
   */
  @Test def testGetPool(): Unit = { testGetPool(mainPool) }
  @Test def testGetPool_Singleton(): Unit = {
    testGetPool(singletonPool)
  }
  def testGetPool(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = { assertSame(pool, getPool) }
    }
    testInvokeOnPool(pool, a)
  }

  /** getPool of non-FJ task returns null
   */
  @Test def testGetPool2(): Unit = {
    AssumesHelper.assumeNotExecutedInForkJoinPool()
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = { assertNull(getPool) }
    }
    assertNull(a.invoke)
  }

  /** inForkJoinPool of executing task returns true
   */
  @Test def testInForkJoinPool(): Unit = {
    testInForkJoinPool(mainPool)
  }
  @Test def testInForkJoinPool_Singleton(): Unit = {
    testInForkJoinPool(singletonPool)
  }
  def testInForkJoinPool(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = { assertTrue(inForkJoinPool) }
    }
    testInvokeOnPool(pool, a)
  }

  /** inForkJoinPool of non-FJ task returns false
   */
  @Test def testInForkJoinPool2(): Unit = {
    AssumesHelper.assumeNotExecutedInForkJoinPool()
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = { assertFalse(inForkJoinPool) }
    }
    assertNull(a.invoke)
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

  /** invoke task throws exception after invoking completeExceptionally
   */
  @Test def testCompleteExceptionally(): Unit = {
    testCompleteExceptionally(mainPool)
  }
  @Test def testCompleteExceptionally_Singleton(): Unit = {
    testCompleteExceptionally(singletonPool)
  }
  def testCompleteExceptionally(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        f.completeExceptionally(new FJException)
        try {
          f.invoke
          shouldThrow()
        } catch {
          case success: FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** invokeAll(tasks) with 1 argument invokes task
   */
  @Test def testInvokeAll1(): Unit = {
    testInvokeAll1(mainPool)
  }
  @Test def testInvokeAll1_Singleton(): Unit = {
    testInvokeAll1(singletonPool)
  }
  def testInvokeAll1(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        invokeAll(f)
        f.checkCompletedNormally()
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** invokeAll(t1, t2) invokes all task arguments
   */
  @Test def testInvokeAll2(): Unit = {
    testInvokeAll2(mainPool)
  }
  @Test def testInvokeAll2_Singleton(): Unit = {
    testInvokeAll2(singletonPool)
  }
  def testInvokeAll2(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val tasks = Array(
          new AsyncFib(8),
          new AsyncFib(9)
        )
        invokeAll(tasks(0), tasks(1))
        for (task <- tasks) { assertTrue(task.isDone) }
        for (task <- tasks) { task.checkCompletedNormally() }
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** invokeAll(tasks) with > 2 argument invokes tasks
   */
  @Test def testInvokeAll3(): Unit = {
    testInvokeAll3(mainPool)
  }
  @Test def testInvokeAll3_Singleton(): Unit = {
    testInvokeAll3(singletonPool)
  }
  def testInvokeAll3(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val tasks = Array(
          new AsyncFib(8),
          new AsyncFib(9),
          new AsyncFib(7)
        )
        invokeAll(tasks(0), tasks(1), tasks(2))
        for (task <- tasks) { assertTrue(task.isDone) }
        for (task <- tasks) { task.checkCompletedNormally() }
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** invokeAll(collection) invokes all tasks in the collection
   */
  @Test def testInvokeAllCollection(): Unit = {
    testInvokeAllCollection(mainPool)
  }
  @Test def testInvokeAllCollection_Singleton(): Unit = {
    testInvokeAllCollection(singletonPool)
  }
  def testInvokeAllCollection(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val tasks = Array(
          new AsyncFib(8),
          new AsyncFib(9),
          new AsyncFib(7)
        )
        invokeAll(util.Arrays.asList(tasks*))
        for (task <- tasks) { assertTrue(task.isDone) }
        for (task <- tasks) { task.checkCompletedNormally() }
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** invokeAll(tasks) with any null task throws NullPointerException
   */
  @Test def testInvokeAllNullTask(): Unit = {
    testInvokeAllNullTask(mainPool)
  }
  @Test def testInvokeAllNullTask_Singleton(): Unit = {
    testInvokeAllNullTask(singletonPool)
  }
  def testInvokeAllNullTask(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val nul: AsyncFib = null
        assertEachThrows(
          classOf[NullPointerException],
          () => invokeAll(nul),
          () => invokeAll(nul, nul),
          () =>
            invokeAll(
              new AsyncFib(8),
              new AsyncFib(9),
              nul
            ),
          () =>
            invokeAll(
              new AsyncFib(8),
              nul,
              new AsyncFib(9)
            ),
          () =>
            invokeAll(
              nul,
              new AsyncFib(8),
              new AsyncFib(9)
            )
        )
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** invokeAll(tasks) with 1 argument throws exception if task does
   */
  @Test def testAbnormalInvokeAll1(): Unit = {
    testAbnormalInvokeAll1(mainPool)
  }
  @Test def testAbnormalInvokeAll1_Singleton(): Unit = {
    testAbnormalInvokeAll1(singletonPool)
  }
  def testAbnormalInvokeAll1(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new FailingAsyncFib(9)
        try {
          invokeAll(g)
          shouldThrow()
        } catch {
          case success: FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** invokeAll(t1, t2) throw exception if any task does
   */
  @Test def testAbnormalInvokeAll2(): Unit = {
    testAbnormalInvokeAll2(mainPool)
  }
  @Test def testAbnormalInvokeAll2_Singleton(): Unit = {
    testAbnormalInvokeAll2(singletonPool)
  }
  def testAbnormalInvokeAll2(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        val g = new FailingAsyncFib(9)
        val tasks = Array(f, g)
        shuffle(tasks)
        try {
          invokeAll(tasks(0), tasks(1))
          shouldThrow()
        } catch {
          case success: FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** invokeAll(tasks) with > 2 argument throws exception if any task does
   */
  @Test def testAbnormalInvokeAll3(): Unit = {
    testAbnormalInvokeAll3(mainPool)
  }
  @Test def testAbnormalInvokeAll3_Singleton(): Unit = {
    testAbnormalInvokeAll3(singletonPool)
  }
  def testAbnormalInvokeAll3(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        val g = new FailingAsyncFib(9)
        val h = new AsyncFib(7)
        val tasks = Array(f, g, h)
        shuffle(tasks)
        try {
          invokeAll(tasks(0), tasks(1), tasks(2))
          shouldThrow()
        } catch {
          case success: FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** invokeAll(collection) throws exception if any task does
   */
  @Test def testAbnormalInvokeAllCollection(): Unit = {
    testAbnormalInvokeAllCollection(mainPool)
  }
  @Test def testAbnormalInvokeAllCollection_Singleton(): Unit = {
    testAbnormalInvokeAllCollection(singletonPool)
  }
  def testAbnormalInvokeAllCollection(pool: ForkJoinPool): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new FailingAsyncFib(8)
        val g = new AsyncFib(9)
        val h = new AsyncFib(7)
        val tasks: Array[BinaryAsyncAction] = Array(f, g, h)
        shuffle(tasks)
        try {
          ForkJoinTask.invokeAll(util.Arrays.asList(tasks*))
          shouldThrow()
        } catch {
          case success: FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(pool, a)
  }

  /** tryUnfork returns true for most recent unexecuted task, and suppresses
   *  execution
   */
  @Test def testTryUnfork(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new AsyncFib(9)
        assertSame(g, g.fork)
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertTrue(f.tryUnfork)
        helpQuiesce
        checkNotDone(f)
        g.checkCompletedNormally()
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  /** getSurplusQueuedTaskCount returns > 0 when there are more tasks than
   *  threads
   */
  @Test def testGetSurplusQueuedTaskCount(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val h = new AsyncFib(7)
        assertSame(h, h.fork)
        val g = new AsyncFib(9)
        assertSame(g, g.fork)
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertTrue(getSurplusQueuedTaskCount > 0)
        helpQuiesce
        assertEquals(0, getSurplusQueuedTaskCount)
        f.checkCompletedNormally()
        g.checkCompletedNormally()
        h.checkCompletedNormally()
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  /** peekNextLocalTask returns most recent unexecuted task.
   */
  @Test def testPeekNextLocalTask(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new AsyncFib(9)
        assertSame(g, g.fork)
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertSame(f, peekNextLocalTask)
        assertNull(f.join)
        f.checkCompletedNormally()
        helpQuiesce
        g.checkCompletedNormally()
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  /** pollNextLocalTask returns most recent unexecuted task without executing it
   */
  @Test def testPollNextLocalTask(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new AsyncFib(9)
        assertSame(g, g.fork)
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertSame(f, pollNextLocalTask)
        helpQuiesce
        checkNotDone(f)
        g.checkCompletedNormally()
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  /** pollTask returns an unexecuted task without executing it
   */
  @Test def testPollTask(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new AsyncFib(9)
        assertSame(g, g.fork)
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertSame(f, pollTask)
        helpQuiesce
        checkNotDone(f)
        g.checkCompletedNormally()
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  /** peekNextLocalTask returns least recent unexecuted task in async mode
   */
  @Test def testPeekNextLocalTaskAsync(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new AsyncFib(9)
        assertSame(g, g.fork)
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertSame(g, peekNextLocalTask)
        assertNull(f.join)
        helpQuiesce
        f.checkCompletedNormally()
        g.checkCompletedNormally()
      }
    }
    testInvokeOnPool(asyncSingletonPool, a)
  }

  /** pollNextLocalTask returns least recent unexecuted task without executing
   *  it, in async mode
   */
  @Test def testPollNextLocalTaskAsync(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new AsyncFib(9)
        assertSame(g, g.fork)
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertSame(g, pollNextLocalTask)
        helpQuiesce
        f.checkCompletedNormally()
        checkNotDone(g)
      }
    }
    testInvokeOnPool(asyncSingletonPool, a)
  }

  /** pollTask returns an unexecuted task without executing it, in async mode
   */
  @Test def testPollTaskAsync(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val g = new AsyncFib(9)
        assertSame(g, g.fork)
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertSame(g, pollTask)
        helpQuiesce()
        f.checkCompletedNormally()
        checkNotDone(g)
      }
    }
    testInvokeOnPool(asyncSingletonPool, a)
  }

  /** ForkJoinTask.quietlyComplete returns when task completes normally without
   *  setting a value. The most recent value established by setRawResult(V) (or
   *  null by default) is returned from invoke.
   */
  @Test def testQuietlyComplete(): Unit = {
    val a = new CheckedRecursiveAction() {
      protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        f.quietlyComplete()
        assertEquals(8, f.number)
        assertTrue(f.isDone)
        assertFalse(f.isCancelled)
        assertTrue(f.isCompletedNormally)
        assertFalse(f.isCompletedAbnormally)
        assertNull(f.getException)
      }
    }
    testInvokeOnPool(mainPool, a)
  }
}
