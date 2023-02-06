/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.scalanative.testsuite.javalib.util.concurrent

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{Test, Ignore}
import JSR166Test._

import java.util.concurrent.TimeUnit._
import java.util
import java.util._
import java.util.concurrent._
import java.util.concurrent.atomic._
import org.scalanative.testsuite.utils.Platform

object ForkJoinTaskTest {
  // Runs with "mainPool" use > 1 thread. singletonPool tests use 1
  val mainPoolSize: Int = Math.max(2, Runtime.getRuntime.availableProcessors)
  private def mainPool = new ForkJoinPool(mainPoolSize)
  private def singletonPool = new ForkJoinPool(1)
  private def asyncSingletonPool = new ForkJoinPool(
    1,
    ForkJoinPool.defaultForkJoinWorkerThreadFactory,
    null,
    true
  )
  /*
   * Testing coverage notes:
   *
   * To test extension methods and overrides, most tests use
   * BinaryAsyncAction extension class that processes joins
   * differently than supplied Recursive forms.
   */
  final class FJException() extends RuntimeException {}
  object BinaryAsyncAction {
    val controlStateUpdater: AtomicIntegerFieldUpdater[BinaryAsyncAction] =
      AtomicIntegerFieldUpdater.newUpdater(
        classOf[BinaryAsyncAction],
        "controlState"
      )
  }
  abstract class BinaryAsyncAction protected () extends ForkJoinTask[Void] {
    private var atomicControlState = new AtomicInteger(0)
    def controlState = atomicControlState.get()
    private var parent: BinaryAsyncAction = _
    private var sibling: BinaryAsyncAction = _
    override final def getRawResult(): Void = null
    override final protected def setRawResult(mustBeNull: Void): Unit = {}
    final def linkSubtasks(
        x: BinaryAsyncAction,
        y: BinaryAsyncAction
    ): Unit = {
      x.parent = this
      y.parent = this
      x.sibling = y
      y.sibling = x
    }
    protected def onComplete(
        x: BinaryAsyncAction,
        y: BinaryAsyncAction
    ): Unit = {}
    protected def onException = true
    def linkAndForkSubtasks(
        x: BinaryAsyncAction,
        y: BinaryAsyncAction
    ): Unit = {
      linkSubtasks(x, y)
      y.fork
      x.fork
    }
    private def completeThis(): Unit = { super.complete(null) }
    private def completeThisExceptionally(ex: Throwable): Unit = {
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
        if (p == null || p.compareAndSetControlState(0, 1))
          break = true
        else {
          try p.onComplete(a, s)
          catch {
            case rex: Throwable =>
              p.completeExceptionally(rex)
              return
          }
          a = p
        }
      }
    }
    override final def completeExceptionally(ex: Throwable): Unit = {
      var a = this
      var break = false
      while (!break) {
        a.completeThisExceptionally(ex)
        val s = a.sibling
        if (s != null && !s.isDone) s.completeExceptionally(ex)
        a = a.parent
        if (a == null) break = true
      }
    }
    final def getParent: BinaryAsyncAction = parent
    def getSibling: BinaryAsyncAction = sibling
    override def reinitialize(): Unit = {
      parent = null
      sibling = null
      super.reinitialize()
    }
    final protected def getControlState: Int = controlState
    final protected def compareAndSetControlState(
        expect: Int,
        update: Int
    ): Boolean = atomicControlState.compareAndSet(expect, update)
    final protected def setControlState(value: Int): Unit =
      atomicControlState.set(value)
    final protected def incrementControlState(): Unit = {
      BinaryAsyncAction.controlStateUpdater.incrementAndGet(this)
    }
    final protected def decrementControlState(): Unit = {
      BinaryAsyncAction.controlStateUpdater.decrementAndGet(this)
    }
  }
  final case class AsyncFib(var number: Int) extends BinaryAsyncAction {
    val startNumber = number
    override final def exec(): Boolean = {
      var f = this
      var n = f.number
      while (n > 1) {
        val p = f
        val r = new AsyncFib(n - 2)
        n -= 1
        f = new AsyncFib(n)
        p.linkSubtasks(r, f)
        r.fork()
      }
      f.complete()
      false
    }
    override protected def onComplete(
        x: BinaryAsyncAction,
        y: BinaryAsyncAction
    ): Unit = {
      val (AsyncFib(xNum), AsyncFib(yNum)) = (x, y): @unchecked
      this.number = xNum + yNum
    }
  }
  final class FailingAsyncFib(var number: Int) extends BinaryAsyncAction {
    override final def exec: Boolean = {
      var f = this
      var n = f.number
      while (n > 1) {
        val p = f
        val r =
          new FailingAsyncFib(n - 2)
        f = new FailingAsyncFib({ n -= 1; n })
        p.linkSubtasks(r, f)
        r.fork
      }
      f.complete()
      false
    }
    override protected def onComplete(
        x: BinaryAsyncAction,
        y: BinaryAsyncAction
    ): Unit = { completeExceptionally(new FJException) }
  }
}

class ForkJoinTaskTest extends JSR166Test {
  import ForkJoinTaskTest._
  private def testInvokeOnPool(pool: ForkJoinPool, a: RecursiveAction): Unit =
    usingPoolCleaner(pool) { pool =>
      assertFalse("isDone", a.isDone())
      assertFalse("isCompletedNormally", a.isCompletedNormally())
      assertFalse("isCompletedAbnormally", a.isCompletedAbnormally())
      assertFalse("isCancelled", a.isCancelled())
      assertNull("getException", a.getException())
      assertNull("getRawResult", a.getRawResult())

      assertNull("pool invoke", pool.invoke(a))

      assertTrue("isDone 2", a.isDone())
      assertTrue("isCompletedNormally 2", a.isCompletedNormally())
      assertFalse("isCompletedAbnormally 2", a.isCompletedAbnormally())
      assertFalse("isCancelled 2", a.isCancelled())
      assertNull("getException 2", a.getException())
      assertNull("getRawResult 2", a.getRawResult())
    }

  def checkNotDone(a: ForkJoinTask[_]): Unit = {
    assertFalse(a.isDone)
    assertFalse(a.isCompletedNormally)
    assertFalse(a.isCompletedAbnormally)
    assertFalse(a.isCancelled)
    assertNull(a.getException)
    assertNull(a.getRawResult)
    try {
      a.get(randomExpiredTimeout(), randomTimeUnit())
      shouldThrow()
    } catch {
      case success: TimeoutException =>

      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
  }
  def checkCompletedNormally[T](a: ForkJoinTask[T]): Unit = {
    checkCompletedNormally(a.asInstanceOf[ForkJoinTask[Any]], null)
  }
  def checkCompletedNormally[T](a: ForkJoinTask[T], expectedValue: T): Unit = {
    assertTrue("isDone", a.isDone)
    assertFalse("isCancelled", a.isCancelled)
    assertTrue("isCompletedNormally", a.isCompletedNormally)
    assertFalse("isCompletedNormally", a.isCompletedAbnormally)
    assertNull("getException", a.getException)
    assertSame("getRawResult", expectedValue, a.getRawResult)
    locally {
      Thread.currentThread.interrupt()
      val startTime = System.nanoTime
      assertSame("join", expectedValue, a.join)
      assertTrue("timeout", millisElapsedSince(startTime) < LONG_DELAY_MS)
      Thread.interrupted
    }
    locally {
      Thread.currentThread.interrupt()
      val startTime = System.nanoTime
      a.quietlyJoin() // should be no-op

      assertTrue("timeout 2", millisElapsedSince(startTime) < LONG_DELAY_MS)
      Thread.interrupted
    }
    assertFalse("cancel", a.cancel(false))
    assertFalse("cancel force", a.cancel(true))
    try {
      val v1 = a.get
      val v2 = a.get(randomTimeout(), randomTimeUnit())
      assertSame("v1", expectedValue, v1)
      assertSame("v2", expectedValue, v2)
    } catch {
      case fail: Throwable => threadUnexpectedException(fail)
    }
  }

  def checkCancelled(a: ForkJoinTask[_]): Unit = {
    assertTrue("isDone", a.isDone)
    assertTrue("isCanceled", a.isCancelled)
    assertFalse("isCompletedNormally", a.isCompletedNormally)
    assertTrue("isCompletedAbnormally", a.isCompletedAbnormally)
    assertTrue(
      "isCancellationException",
      a.getException.isInstanceOf[CancellationException]
    )
    assertNull("result is null", a.getRawResult)
    assertTrue("cancel", a.cancel(false))
    assertTrue("cancel force", a.cancel(true))
    try {
      Thread.currentThread.interrupt()
      a.join
      shouldThrow()
    } catch {
      case success: CancellationException => ()
      case fail: Throwable                => threadUnexpectedException(fail)
    }
    Thread.interrupted()
    val startTime = System.nanoTime()
    a.quietlyJoin()
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)

    try {
      a.get
      shouldThrow()
    } catch {
      case success: CancellationException => ()
      case fail: Throwable                => threadUnexpectedException(fail)
    }
    try {
      a.get(randomTimeout(), randomTimeUnit())
      shouldThrow()
    } catch {
      case success: CancellationException => ()
      case fail: Throwable                => threadUnexpectedException(fail)
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
      Thread.currentThread.interrupt()
      a.join
      shouldThrow()
    } catch {
      case expected: Throwable =>
        assertSame(t.getClass, expected.getClass)
    }
    Thread.interrupted
    val startTime = System.nanoTime
    a.quietlyJoin()
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

  /** invoke returns when task completes normally. isCompletedAbnormally and
   *  isCancelled return false for normally completed tasks; getRawResult
   *  returns null.
   */
  @Test def testInvoke(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertNull("invoke", f.invoke())
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** quietlyInvoke task returns when task completes normally.
   *  isCompletedAbnormally and isCancelled return false for normally completed
   *  tasks
   */
  @Test def testQuietlyInvoke(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        f.quietlyInvoke()
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** join of a forked task returns when task completes
   */
  @Test def testForkJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertNull(f.join)
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** get of a forked task returns when task completes
   */
  @Test def testForkGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertNull(f.get)
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** timed get of a forked task returns when task completes
   */
  @Test def testForkTimedGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertNull(f.get(LONG_DELAY_MS, MILLISECONDS))
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** timed get with null time unit throws NPE
   */
  @Test def testForkTimedGetNPE(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        try {
          f.get(randomTimeout(), null)
          shouldThrow()
        } catch {
          case success: NullPointerException =>

        }
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** quietlyJoin of a forked task returns when task completes
   */
  @Test def testForkQuietlyJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        f.quietlyJoin()
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** helpQuiesce returns when tasks are complete.
   *  ForkJoinTask.getQueuedTaskCount() returns 0 when quiescent
   */
  @Test def testForkHelpQuiesce(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        ForkJoinTask.helpQuiesce()
        while ({ !f.isDone }) { // wait out race
        }
        assertEquals(21, f.number)
        assertEquals(0, ForkJoinTask.getQueuedTaskCount())
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** invoke task throws exception when task completes abnormally
   */
  @Test def testAbnormalInvoke(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
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
    testInvokeOnPool(mainPool, a)
  }

  /** quietlyInvoke task returns when task completes abnormally
   */
  @Test def testAbnormalQuietlyInvoke(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new FailingAsyncFib(8)
        f.quietlyInvoke()
        assertTrue(f.getException.isInstanceOf[FJException])
        checkCompletedAbnormally(f, f.getException)
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** join of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f =
          new FailingAsyncFib(8)
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
    testInvokeOnPool(mainPool, a)
  }

  /** get of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f =
          new FailingAsyncFib(8)
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
    testInvokeOnPool(mainPool, a)
  }

  /** timed get of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkTimedGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f =
          new FailingAsyncFib(8)
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
    testInvokeOnPool(mainPool, a)
  }

  /** quietlyJoin of a forked task returns when task completes abnormally
   */
  @Test def testAbnormalForkQuietlyJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f =
          new FailingAsyncFib(8)
        assertSame(f, f.fork)
        f.quietlyJoin()
        assertTrue(f.getException.isInstanceOf[FJException])
        checkCompletedAbnormally(f, f.getException)
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** invoke task throws exception when task cancelled
   */
  @Test def testCancelledInvoke(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
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
    testInvokeOnPool(mainPool, a)
  }

  /** join of a forked task throws exception when task cancelled
   */
  @Test def testCancelledForkJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
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
    testInvokeOnPool(mainPool, a)
  }

  /** get of a forked task throws exception when task cancelled
   */
  @Test def testCancelledForkGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
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
    testInvokeOnPool(mainPool, a)
  }

  /** timed get of a forked task throws exception when task cancelled
   */
  @throws[Exception]
  @Test def testCancelledForkTimedGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
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
    testInvokeOnPool(mainPool, a)
  }

  /** quietlyJoin of a forked task returns when task cancelled
   */
  @Test def testCancelledForkQuietlyJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertTrue(f.cancel(true))
        assertSame(f, f.fork)
        f.quietlyJoin()
        checkCancelled(f)
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** ForkJoinTask.getPool() of executing task returns its pool
   */
  @Test def testGetPool(): Unit = {
    val mainPool = ForkJoinTaskTest.mainPool
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        assertSame(mainPool, ForkJoinTask.getPool())
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** ForkJoinTask.getPool() of non-FJ task returns null
   */
  @Ignore(
    "Test-infrastructure limitation, all tests are executed in ForkJoinPool due to usage of Future in RPCCore"
  )
  @Test def testGetPool2(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        assertNull(ForkJoinTask.getPool())
      }
    }
    assertNull(a.invoke)
  }

  /** inForkJoinPool of executing task returns true
   */
  @Test def testInForkJoinPool(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        assertTrue(ForkJoinTask.inForkJoinPool())
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** inForkJoinPool of non-FJ task returns false
   */
  @Ignore("Test-infrastructure limitation, see testGetPool2")
  @Test def testInForkJoinPool2(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        assertFalse(ForkJoinTask.inForkJoinPool())
      }
    }
    assertNull(a.invoke)
  }

  /** setRawResult(null) succeeds
   */
  @Test def testSetRawResult(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        setRawResult(null)
        assertNull(getRawResult)
      }
    }
    assertNull(a.invoke)
  }

  /** invoke task throws exception after invoking completeExceptionally
   */
  @Test def testCompleteExceptionally(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
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
    testInvokeOnPool(mainPool, a)
  }

  /** completeExceptionally(null) surprisingly has the same effect as
   *  completeExceptionally(new RuntimeException())
   */
  @Test def testCompleteExceptionally_null(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        f.completeExceptionally(null)
        try {
          f.invoke
          shouldThrow()
        } catch {
          case success: RuntimeException =>
            assertSame(success.getClass, classOf[RuntimeException])
            assertNull(success.getCause)
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** ForkJoinTask.invokeAll(t1, t2) invokes all task arguments
   */
  @Test def testInvokeAll2(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        val g = new AsyncFib(9)
        ForkJoinTask.invokeAll(f, g)
        assertEquals(21, f.number)
        assertEquals(34, g.number)
        checkCompletedNormally(f)
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** ForkJoinTask.invokeAll(tasks) with 1 argument invokes task
   */
  @Test def testInvokeAll1(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        ForkJoinTask.invokeAll(f)
        checkCompletedNormally(f)
        assertEquals(21, f.number)
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** ForkJoinTask.invokeAll(tasks) with > 2 argument invokes tasks
   */
  @Test def testInvokeAll3(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        val g = new AsyncFib(9)
        val h = new AsyncFib(7)
        ForkJoinTask.invokeAll(f, g, h)
        assertEquals(21, f.number)
        assertEquals(34, g.number)
        assertEquals(13, h.number)
        checkCompletedNormally(f)
        checkCompletedNormally(g)
        checkCompletedNormally(h)
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** ForkJoinTask.invokeAll(collection) invokes all tasks in the collection
   */
  @Test def testInvokeAllCollection(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        val g = new AsyncFib(9)
        val h = new AsyncFib(7)
        val set = new HashSet[ForkJoinTask[_]]
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
    testInvokeOnPool(mainPool, a)
  }

  /** ForkJoinTask.invokeAll(tasks) with any null task throws NPE
   */
  @Test def testInvokeAllNPE(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        val g = new AsyncFib(9)
        val h = null
        try {
          ForkJoinTask.invokeAll(f, g, h)
          shouldThrow()
        } catch {
          case success: NullPointerException =>

        }
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** ForkJoinTask.invokeAll(t1, t2) throw exception if any task does
   */
  @Test def testAbnormalInvokeAll2(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        val g = new FailingAsyncFib(9)
        val tasks = Array(f, g)
        shuffle(tasks)
        try {
          ForkJoinTask.invokeAll(tasks: _*)
          shouldThrow()
        } catch {
          case success: FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** ForkJoinTask.invokeAll(tasks) with 1 argument throws exception if task
   *  does
   */
  @Test def testAbnormalInvokeAll1(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g = new FailingAsyncFib(9)
        try {
          ForkJoinTask.invokeAll(g)
          shouldThrow()
        } catch {
          case success: FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** ForkJoinTask.invokeAll(tasks) with > 2 argument throws exception if any
   *  task does
   */
  @Test def testAbnormalInvokeAll3(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        val g =
          new FailingAsyncFib(9)
        val h = new AsyncFib(7)
        val tasks = Array(f, g, h)
        shuffle(tasks)
        try {
          ForkJoinTask.invokeAll(tasks: _*)
          shouldThrow()
        } catch {
          case success: FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** ForkJoinTask.invokeAll(collection) throws exception if any task does
   */
  @Test def testAbnormalInvokeAllCollection(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f =
          new FailingAsyncFib(8)
        val g = new AsyncFib(9)
        val h = new AsyncFib(7)
        val tasks = Array(f, g, h)
        shuffle(tasks)
        try {
          ForkJoinTask.invokeAll(Arrays.asList(tasks: _*))
          shouldThrow()
        } catch {
          case success: FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** tryUnfork returns true for most recent unexecuted task, and suppresses
   *  execution
   */
  @Test def testTryUnfork(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g = new AsyncFib(9)
        assertSame(g, g.fork)
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertTrue(f.tryUnfork)
        ForkJoinTask.helpQuiesce()
        checkNotDone(f)
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  /** getSurplusQueuedTaskCount returns > 0 when there are more tasks than
   *  threads
   */
  @Test def testGetSurplusQueuedTaskCount(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val h = new AsyncFib(7)
        assertSame(h, h.fork)
        val g = new AsyncFib(9)
        assertSame(g, g.fork)
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertTrue(ForkJoinTask.getSurplusQueuedTaskCount > 0)
        ForkJoinTask.helpQuiesce()
        assertEquals(0, ForkJoinTask.getSurplusQueuedTaskCount)
        checkCompletedNormally(f)
        checkCompletedNormally(g)
        checkCompletedNormally(h)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  /** peekNextLocalTask returns most recent unexecuted task.
   */
  @Test def testPeekNextLocalTask(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g = new AsyncFib(9)
        assertSame(g, g.fork)
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertSame(f, ForkJoinTask.peekNextLocalTask)
        assertNull(f.join)
        checkCompletedNormally(f)
        ForkJoinTask.helpQuiesce()
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  /** pollNextLocalTask returns most recent unexecuted task without executing it
   */
  @Test def testPollNextLocalTask(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g = new AsyncFib(9)
        assertSame(g, g.fork)
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertSame(f, ForkJoinTask.pollNextLocalTask)
        ForkJoinTask.helpQuiesce()
        checkNotDone(f)
        assertEquals(34, g.number)
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  /** pollTask returns an unexecuted task without executing it
   */
  @Test def testPollTask(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g = new AsyncFib(9)
        assertSame(g, g.fork)
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertSame(f, ForkJoinTask.pollTask)
        ForkJoinTask.helpQuiesce()
        checkNotDone(f)
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  /** peekNextLocalTask returns least recent unexecuted task in async mode
   */
  @Test def testPeekNextLocalTaskAsync(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g = new AsyncFib(9)
        assertSame(g, g.fork)
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertSame(g, ForkJoinTask.peekNextLocalTask)
        assertNull(f.join)
        ForkJoinTask.helpQuiesce()
        checkCompletedNormally(f)
        assertEquals(34, g.number)
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(asyncSingletonPool, a)
  }

  /** pollNextLocalTask returns least recent unexecuted task without executing
   *  it, in async mode
   */
  @Test def testPollNextLocalTaskAsync(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g = new AsyncFib(9)
        assertSame(g, g.fork)
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertSame(g, ForkJoinTask.pollNextLocalTask)
        ForkJoinTask.helpQuiesce()
        assertEquals(21, f.number)
        checkCompletedNormally(f)
        checkNotDone(g)
      }
    }
    testInvokeOnPool(asyncSingletonPool, a)
  }

  /** pollTask returns an unexecuted task without executing it, in async mode
   */
  @Test def testPollTaskAsync(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g = new AsyncFib(9)
        assertSame(g, g.fork)
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertSame(g, ForkJoinTask.pollTask)
        ForkJoinTask.helpQuiesce()
        assertEquals(21, f.number)
        checkCompletedNormally(f)
        checkNotDone(g)
      }
    }
    testInvokeOnPool(asyncSingletonPool, a)
  }
  // versions for singleton pools
  @Test def testInvokeSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertNull(f.invoke)
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testQuietlyInvokeSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        f.quietlyInvoke()
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testForkJoinSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertNull(f.join)
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testForkGetSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertNull(f.get)
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testForkTimedGetSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        assertNull(f.get(LONG_DELAY_MS, MILLISECONDS))
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testForkTimedGetNPESingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        try {
          f.get(randomTimeout(), null)
          shouldThrow()
        } catch {
          case success: NullPointerException =>

        }
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testForkQuietlyJoinSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        f.quietlyJoin()
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testForkHelpQuiesceSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertSame(f, f.fork)
        ForkJoinTask.helpQuiesce()
        assertEquals(0, ForkJoinTask.getQueuedTaskCount())
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testAbnormalInvokeSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f =
          new FailingAsyncFib(8)
        try {
          f.invoke
          shouldThrow()
        } catch {
          case success: FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testAbnormalQuietlyInvokeSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f =
          new FailingAsyncFib(8)
        f.quietlyInvoke()
        assertTrue(f.getException.isInstanceOf[FJException])
        checkCompletedAbnormally(f, f.getException)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testAbnormalForkJoinSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f =
          new FailingAsyncFib(8)
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
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testAbnormalForkGetSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f =
          new FailingAsyncFib(8)
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
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testAbnormalForkTimedGetSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f =
          new FailingAsyncFib(8)
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
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testAbnormalForkQuietlyJoinSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f =
          new FailingAsyncFib(8)
        assertSame(f, f.fork)
        f.quietlyJoin()
        assertTrue(f.getException.isInstanceOf[FJException])
        checkCompletedAbnormally(f, f.getException)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testCancelledInvokeSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
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
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testCancelledForkJoinSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
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
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testCancelledForkGetSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
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
    testInvokeOnPool(singletonPool, a)
  }
  @throws[Exception]
  @Test def testCancelledForkTimedGetSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
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
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testCancelledForkQuietlyJoinSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        assertTrue(f.cancel(true))
        assertSame(f, f.fork)
        f.quietlyJoin()
        checkCancelled(f)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testCompleteExceptionallySingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        f.completeExceptionally(new FJException())
        try {
          f.invoke()
          shouldThrow()
        } catch {
          case success: FJException => checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testInvokeAll2Singleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        val g = new AsyncFib(9)
        ForkJoinTask.invokeAll(f, g)
        assertEquals(21, f.number)
        assertEquals(34, g.number)
        checkCompletedNormally(f)
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testInvokeAll1Singleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        ForkJoinTask.invokeAll(f)
        checkCompletedNormally(f)
        assertEquals(21, f.number)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testInvokeAll3Singleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        val g = new AsyncFib(9)
        val h = new AsyncFib(7)
        ForkJoinTask.invokeAll(f, g, h)
        assertEquals(21, f.number)
        assertEquals(34, g.number)
        assertEquals(13, h.number)
        checkCompletedNormally(f)
        checkCompletedNormally(g)
        checkCompletedNormally(h)
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testInvokeAllCollectionSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        val g = new AsyncFib(9)
        val h = new AsyncFib(7)
        val set = new HashSet[ForkJoinTask[_]]
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
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testInvokeAllNPESingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        val g = new AsyncFib(9)
        val h = null
        try {
          ForkJoinTask.invokeAll(f, g, h)
          shouldThrow()
        } catch {
          case success: NullPointerException => ()
        }
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testAbnormalInvokeAll2Singleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        val g = new FailingAsyncFib(9)
        val tasks = Array(f, g)
        shuffle(tasks)
        try {
          ForkJoinTask.invokeAll(tasks: _*)
          shouldThrow()
        } catch {
          case success: FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testAbnormalInvokeAll1Singleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g =
          new FailingAsyncFib(9)
        try {
          ForkJoinTask.invokeAll(g)
          shouldThrow()
        } catch {
          case success: FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testAbnormalInvokeAll3Singleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        val g = new FailingAsyncFib(9)
        val h = new AsyncFib(7)
        val tasks = Array(f, g, h)
        shuffle(tasks)
        try {
          ForkJoinTask.invokeAll(tasks: _*)
          shouldThrow()
        } catch {
          case success: FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  @Test def testAbnormalInvokeAllCollectionSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new FailingAsyncFib(8)
        val g = new AsyncFib(9)
        val h = new AsyncFib(7)
        val tasks = Array(f, g, h)
        shuffle(tasks)
        try {
          ForkJoinTask.invokeAll(Arrays.asList(tasks: _*))
          shouldThrow()
        } catch {
          case success: FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(singletonPool, a)
  }

  /** ForkJoinTask.quietlyComplete returns when task completes normally without
   *  setting a value. The most recent value established by setRawResult(V) (or
   *  null by default) is returned from invoke.
   */
  @Test def testQuietlyComplete(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new AsyncFib(8)
        f.quietlyComplete()
        assertEquals(8, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** adapt(runnable).toString() contains toString of wrapped task
   */
  @Test def testAdapt_Runnable_toString(): Unit = {
    assumeFalse(
      "Output difference since JDK11",
      Platform.executingInJVMOnLowerThenJDK11
    )
    if (testImplementationDetails) {
      val r: Runnable = () => {
        def foo() = {}
        foo()
      }
      val task = ForkJoinTask.adapt(r)
      assertEquals(
        identityString(task) + "[Wrapped task = " + r.toString + "]",
        task.toString
      )
    }
  }

  /** adapt(runnable, x).toString() contains toString of wrapped task
   */
  @Test def testAdapt_Runnable_withResult_toString(): Unit = {
    assumeFalse(
      "Output difference since JDK11",
      Platform.executingInJVMOnLowerThenJDK11
    )
    if (testImplementationDetails) {
      val r: Runnable = () => {
        def foo() = {}
        foo()
      }
      val task = ForkJoinTask.adapt(r, "")
      assertEquals(
        identityString(task) + "[Wrapped task = " + r.toString + "]",
        task.toString
      )
    }
  }

  /** adapt(callable).toString() contains toString of wrapped task
   */
  @Test def testAdapt_Callable_toString(): Unit = {
    assumeFalse(
      "Output difference since JDK11",
      Platform.executingInJVMOnLowerThenJDK11
    )
    if (testImplementationDetails) {
      val c: Callable[String] = () => ""
      val task = ForkJoinTask.adapt(c)
      assertEquals(
        identityString(task) + "[Wrapped task = " + c.toString + "]",
        task.toString
      )
    }
  }
}
