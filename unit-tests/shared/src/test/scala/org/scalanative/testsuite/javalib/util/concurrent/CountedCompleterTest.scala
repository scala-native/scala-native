/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.scalanative.testsuite.javalib.util.concurrent

import java.util
import java.util._
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent._
import java.util.concurrent.atomic._
import java.util.function._

import org.junit.Assert._
import org.junit.{Ignore, Test}

import JSR166Test._

object CountedCompleterTest {
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
  final class FJException() extends RuntimeException {}
}
class CountedCompleterTest extends JSR166Test {
  private def testInvokeOnPool(pool: ForkJoinPool, a: ForkJoinTask[_]): Unit =
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

  def checkNotDone(a: CountedCompleter[Any]): Unit = {
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
      case success: TimeoutException => ()
      case fail: Throwable           => threadUnexpectedException(fail)
    }
  }

  def checkCompletedNormally(a: CountedCompleter[Any]): Unit = {
    assertTrue(a.isDone)
    assertFalse(a.isCancelled)
    assertTrue(a.isCompletedNormally)
    assertFalse(a.isCompletedAbnormally)
    assertNull(a.getException)
    assertNull(a.getRawResult)
    locally {
      Thread.currentThread.interrupt()
      val startTime = System.nanoTime
      assertNull(a.join)
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
      Thread.interrupted
      Thread.currentThread.interrupt()
    }
    locally {
      val startTime = System.nanoTime
      a.quietlyJoin() // should be no-op

      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)
      Thread.interrupted
    }
    assertFalse(a.cancel(false))
    assertFalse(a.cancel(true))
    var v1 = null.asInstanceOf[Any]
    var v2 = null.asInstanceOf[Any]
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

  def checkCancelled(a: CountedCompleter[Any]): Unit = {
    assertTrue(a.isDone)
    assertTrue(a.isCancelled)
    assertFalse(a.isCompletedNormally)
    assertTrue(a.isCompletedAbnormally)
    assertTrue(a.getException.isInstanceOf[CancellationException])
    assertNull(a.getRawResult)
    assertTrue(a.cancel(false))
    assertTrue(a.cancel(true))
    try {
      Thread.currentThread.interrupt()
      a.join
      shouldThrow()
    } catch {
      case success: CancellationException =>

      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    Thread.interrupted
    val startTime = System.nanoTime
    a.quietlyJoin()
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS)

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

  def checkCompletedAbnormally(a: CountedCompleter[Any], t: Throwable): Unit = {
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
        assertEquals(t.getClass, expected.getClass)
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
        assertEquals(t.getClass, success.getCause.getClass)
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    try {
      a.get(randomTimeout(), randomTimeUnit())
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertEquals(t.getClass, success.getCause.getClass)
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    try {
      a.invoke
      shouldThrow()
    } catch {
      case success: Throwable => t.getClass == success.getClass
    }
  }

  abstract class CheckedCC(p: CountedCompleter[Any], n: Int)
      extends CountedCompleter[Any](p, n) {
    def this(p: CountedCompleter[Any]) = this(p, 0)
    def this() = this(null)
    final val computeNAtomic = new AtomicInteger(0)
    final val onCompletionNAtomic = new AtomicInteger(0)
    final val onExceptionalCompletionNAtomic = new AtomicInteger(0)
    final val setRawResultNAtomic = new AtomicInteger(0)
    final val rawResultAtomic = new AtomicReference[Any](null)
    def computeN: Int = computeNAtomic.get
    def onCompletionN: Int = onCompletionNAtomic.get
    def onExceptionalCompletionN: Int = onExceptionalCompletionNAtomic.get
    def setRawResultN: Int = setRawResultNAtomic.get

    protected def realCompute(): Unit

    override final def compute(): Unit = {
      computeNAtomic.incrementAndGet
      realCompute()
    }

    override def toString(): String = super
      .toString() + s"[$n, ${computeNAtomic.get()}, ${onCompletionNAtomic.get()}, ${onExceptionalCompletionNAtomic
        .get()}, ${setRawResultNAtomic.get()}, ${rawResultAtomic.get()}]"

    override def onCompletion(caller: CountedCompleter[_]): Unit = {
      onCompletionNAtomic.incrementAndGet
      super.onCompletion(caller)
    }

    override def onExceptionalCompletion(
        ex: Throwable,
        caller: CountedCompleter[_]
    ): Boolean = {
      onExceptionalCompletionNAtomic.incrementAndGet
      assertNotNull(ex)
      assertTrue(isCompletedAbnormally)
      assertTrue(super.onExceptionalCompletion(ex, caller))
      true
    }
    override protected def setRawResult(t: Any): Unit = {
      setRawResultNAtomic.incrementAndGet
      rawResultAtomic.set(t)
      super.setRawResult(t)
    }
    def checkIncomplete(): Unit = {
      assertEquals(0, computeN)
      assertEquals(0, onCompletionN)
      assertEquals(0, onExceptionalCompletionN)
      assertEquals(0, setRawResultN)
      checkNotDone(this)
    }
    def checkCompletes(rawResult: Any): Unit = {
      checkIncomplete()
      val pendingCount = getPendingCount
      complete(rawResult)
      assertEquals(pendingCount, getPendingCount)
      assertEquals(0, computeN)
      assertEquals(1, onCompletionN)
      assertEquals(0, onExceptionalCompletionN)
      assertEquals(1, setRawResultN)
      assertSame(rawResult, this.rawResultAtomic.get)
      checkCompletedNormally(this)
    }
    def checkCompletesExceptionally(ex: Throwable): Unit = {
      checkIncomplete()
      completeExceptionally(ex)
      checkCompletedExceptionally(ex)
    }
    def checkCompletedExceptionally(ex: Throwable): Unit = {
      assertEquals(0, computeN)
      assertEquals(0, onCompletionN)
      assertEquals(1, onExceptionalCompletionN)
      assertEquals(0, setRawResultN)
      assertNull(this.rawResultAtomic.get)
      checkCompletedAbnormally(this, ex)
    }
  }
  final class NoopCC(
      p: CountedCompleter[Any] = null,
      initialPendingCount: Int = 0
  ) extends CheckedCC(p, initialPendingCount) {
    override protected def realCompute(): Unit = ()
  }

  /** A newly constructed CountedCompleter is not completed; complete() causes
   *  completion. pendingCount is ignored.
   */
  @Test def testComplete(): Unit = {
    for (x <- Array[Any](java.lang.Boolean.TRUE, null)) {
      for (pendingCount <- Array[Int](0, 42)) {
        testComplete(new NoopCC(), x, pendingCount)
        testComplete(new NoopCC(new NoopCC), x, pendingCount)
      }
    }
  }
  def testComplete(cc: NoopCC, x: Any, pendingCount: Int): Unit = {
    cc.setPendingCount(pendingCount)
    cc.checkCompletes(x)
    assertEquals(pendingCount, cc.getPendingCount)
  }

  /** completeExceptionally completes exceptionally
   */
  @Test def testCompleteExceptionally(): Unit = {
    new NoopCC()
      .checkCompletesExceptionally(new CountedCompleterTest.FJException)
    new NoopCC(new NoopCC)
      .checkCompletesExceptionally(new CountedCompleterTest.FJException)
  }

  /** completeExceptionally(null) surprisingly has the same effect as
   *  completeExceptionally(new RuntimeException())
   */
  @Test def testCompleteExceptionally_null(): Unit = {
    val a = new NoopCC
    a.completeExceptionally(null)
    try {
      a.invoke
      shouldThrow()
    } catch {
      case success: RuntimeException =>
        assertSame(success.getClass, classOf[RuntimeException])
        assertNull(success.getCause)
        a.checkCompletedExceptionally(success)
    }
  }

  /** setPendingCount sets the reported pending count
   */
  @Test def testSetPendingCount(): Unit = {
    val a = new NoopCC
    assertEquals(0, a.getPendingCount)
    val vals = Array(-1, 0, 1, Integer.MIN_VALUE, Integer.MAX_VALUE)
    for (`val` <- vals) {
      a.setPendingCount(`val`)
      assertEquals(`val`, a.getPendingCount)
    }
  }

  /** addToPendingCount adds to the reported pending count
   */
  @Test def testAddToPendingCount(): Unit = {
    val a = new NoopCC
    assertEquals(0, a.getPendingCount)
    a.addToPendingCount(1)
    assertEquals(1, a.getPendingCount)
    a.addToPendingCount(27)
    assertEquals(28, a.getPendingCount)
    a.addToPendingCount(-28)
    assertEquals(0, a.getPendingCount)
  }

  /** decrementPendingCountUnlessZero decrements reported pending count unless
   *  zero
   */
  @Test def testDecrementPendingCountUnlessZero(): Unit = {
    val a = new NoopCC(null, 2)
    assertEquals(2, a.getPendingCount)
    assertEquals(2, a.decrementPendingCountUnlessZero)
    assertEquals(1, a.getPendingCount)
    assertEquals(1, a.decrementPendingCountUnlessZero)
    assertEquals(0, a.getPendingCount)
    assertEquals(0, a.decrementPendingCountUnlessZero)
    assertEquals(0, a.getPendingCount)
    a.setPendingCount(-1)
    assertEquals(-1, a.decrementPendingCountUnlessZero)
    assertEquals(-2, a.getPendingCount)
  }

  /** compareAndSetPendingCount compares and sets the reported pending count
   */
  @Test def testCompareAndSetPendingCount(): Unit = {
    val a = new NoopCC
    assertEquals(0, a.getPendingCount)
    assertTrue(a.compareAndSetPendingCount(0, 1))
    assertEquals(1, a.getPendingCount)
    assertTrue(a.compareAndSetPendingCount(1, 2))
    assertEquals(2, a.getPendingCount)
    assertFalse(a.compareAndSetPendingCount(1, 3))
    assertEquals(2, a.getPendingCount)
  }

  /** getCompleter returns parent or null if at root
   */
  @Test def testGetCompleter(): Unit = {
    val a = new NoopCC
    assertNull(a.getCompleter)
    val b = new NoopCC(a)
    assertSame(a, b.getCompleter)
    val c = new NoopCC(b)
    assertSame(b, c.getCompleter)
  }

  /** getRoot returns self if no parent, else parent's root
   */
  @Test def testGetRoot(): Unit = {
    val a = new NoopCC
    val b = new NoopCC(a)
    val c = new NoopCC(b)
    assertSame(a, a.getRoot)
    assertSame(a, b.getRoot)
    assertSame(a, c.getRoot)
  }

  /** tryComplete decrements pending count unless zero, in which case causes
   *  completion
   */
  @Test def testTryComplete(): Unit = {
    val a = new NoopCC
    assertEquals(0, a.getPendingCount)
    var n = 3
    a.setPendingCount(n)

    while ({ n > 0 }) {
      assertEquals(n, a.getPendingCount)
      a.tryComplete()
      a.checkIncomplete()
      assertEquals(n - 1, a.getPendingCount)

      n -= 1
    }
    a.tryComplete()
    assertEquals(0, a.computeN)
    assertEquals(1, a.onCompletionN)
    assertEquals(0, a.onExceptionalCompletionN)
    assertEquals(0, a.setRawResultN)
    checkCompletedNormally(a)
  }

  /** propagateCompletion decrements pending count unless zero, in which case
   *  causes completion, without invoking onCompletion
   */
  @Test def testPropagateCompletion(): Unit = {
    val a = new NoopCC
    assertEquals(0, a.getPendingCount)
    var n = 3
    a.setPendingCount(n)

    while ({ n > 0 }) {
      assertEquals(n, a.getPendingCount)
      a.propagateCompletion()
      a.checkIncomplete()
      assertEquals(n - 1, a.getPendingCount)

      n -= 1
    }
    a.propagateCompletion()
    assertEquals(0, a.computeN)
    assertEquals(0, a.onCompletionN)
    assertEquals(0, a.onExceptionalCompletionN)
    assertEquals(0, a.setRawResultN)
    checkCompletedNormally(a)
  }

  /** firstComplete returns this if pending count is zero else null
   */
  @Test def testFirstComplete(): Unit = {
    val a = new NoopCC
    a.setPendingCount(1)
    assertNull(a.firstComplete)
    a.checkIncomplete()
    assertSame(a, a.firstComplete)
    a.checkIncomplete()
  }

  /** firstComplete.nextComplete returns parent if pending count is zero else
   *  null
   */
  @Test def testNextComplete(): Unit = {
    val a = new NoopCC
    val b = new NoopCC(a)
    a.setPendingCount(1)
    b.setPendingCount(1)
    assertNull(b.firstComplete)
    assertSame(b, b.firstComplete)
    assertNull(b.nextComplete)
    a.checkIncomplete()
    b.checkIncomplete()
    assertSame(a, b.nextComplete)
    assertSame(a, b.nextComplete)
    a.checkIncomplete()
    b.checkIncomplete()
    assertNull(a.nextComplete)
    b.checkIncomplete()
    checkCompletedNormally(a)
  }

  /** quietlyCompleteRoot completes root task and only root task
   */
  @Test def testQuietlyCompleteRoot(): Unit = {
    val a = new NoopCC
    val b = new NoopCC(a)
    val c = new NoopCC(b)
    a.setPendingCount(1)
    b.setPendingCount(1)
    c.setPendingCount(1)
    c.quietlyCompleteRoot()
    assertTrue(a.isDone)
    assertFalse(b.isDone)
    assertFalse(c.isDone)
  }

  /** Version of Fibonacci with different classes for left vs right forks
   */
  // Invocation tests use some interdependent task classes
  // to better test propagation etc
  abstract class CCF(
      val parent: CountedCompleter[Any],
      @volatile var number: Int
  ) extends CheckedCC(parent, 1) {
    @volatile var rnumber = 0

    override protected final def realCompute(): Unit = {
      var f = this
      var n = number
      while (n >= 2) {
        new RCCF(f, n - 2).fork()
        n -= 1
        f = new LCCF(f, n)
      }
      f.complete(null)
    }
    override def toString(): String =
      super.toString() + s" n=$number, rn=${rnumber}"
  }
  final class LCCF(parent: CountedCompleter[Any], val n: Int)
      extends CCF(parent, n) {
    def this(n: Int) = this(null, n)
    override final def onCompletion(caller: CountedCompleter[_]): Unit = {
      super.onCompletion(caller)
      val p = getCompleter.asInstanceOf[CCF]
      val n = number + rnumber
      if (p != null) p.number = n
      else number = n
    }
  }
  final class RCCF(parent: CountedCompleter[Any], val n: Int)
      extends CCF(parent, n) {
    override final def onCompletion(caller: CountedCompleter[_]): Unit = {
      super.onCompletion(caller)
      val p = getCompleter.asInstanceOf[CCF]
      val n = number + rnumber
      if (p != null) p.rnumber = n
      else number = n
    }
  }
  // Version of CCF with forced failure in left completions
  abstract class FailingCCF(parent: CountedCompleter[Any], var number: Int)
      extends CheckedCC(parent, 1) {
    val rnumber = 0
    override protected final def realCompute(): Unit = {
      var f = this
      var n = number
      while ({ n >= 2 }) {
        new RFCCF(f, n - 2).fork
        f = new LFCCF(f, { n -= 1; n })
      }
      f.complete(null)
    }
  }
  final class LFCCF(val parent: CountedCompleter[Any], val n: Int)
      extends FailingCCF(parent, n) {
    def this(n: Int) = this(null, n)

    override final def onCompletion(caller: CountedCompleter[_]): Unit = {
      super.onCompletion(caller)
      val p = getCompleter.asInstanceOf[FailingCCF]
      val n = number + rnumber
      if (p != null) p.number = n
      else number = n
    }
  }
  final class RFCCF(val parent: CountedCompleter[Any], val n: Int)
      extends FailingCCF(parent, n) {
    override final def onCompletion(caller: CountedCompleter[_]): Unit = {
      super.onCompletion(caller)
      completeExceptionally(new CountedCompleterTest.FJException)
    }
  }

  /** invoke returns when task completes normally. isCompletedAbnormally and
   *  isCancelled return false for normally completed tasks; getRawResult
   *  returns null.
   */
  @Test def testInvoke(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        assertNull(f.invoke)
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** quietlyInvoke task returns when task completes normally.
   *  isCompletedAbnormally and isCancelled return false for normally completed
   *  tasks
   */
  @Test def testQuietlyInvoke(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        f.quietlyInvoke()
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** join of a forked task returns when task completes
   */
  @Test def testForkJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        assertSame(f, f.fork)
        assertNull(f.join)
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** get of a forked task returns when task completes
   */
  @Test def testForkGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        assertSame(f, f.fork)
        assertNull(f.get)
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** timed get of a forked task returns when task completes
   */
  @Test def testForkTimedGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        assertSame(f, f.fork)
        assertNull(f.get(LONG_DELAY_MS, MILLISECONDS))
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** timed get with null time unit throws NPE
   */
  @Test def testForkTimedGetNPE(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        assertSame(f, f.fork)
        try {
          f.get(randomTimeout(), null)
          shouldThrow()
        } catch {
          case success: NullPointerException =>

        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** quietlyJoin of a forked task returns when task completes
   */
  @Test def testForkQuietlyJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        assertSame(f, f.fork)
        f.quietlyJoin()
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** helpQuiesce returns when tasks are complete. getQueuedTaskCount returns 0
   *  when quiescent
   */
  @Test def testForkHelpQuiesce(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        assertSame(f, f.fork)
        helpQuiesce()
        while ({ !f.isDone }) { // wait out race
        }
        assertEquals(21, f.number)
        assertEquals(0, getQueuedTaskCount)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** invoke task throws exception when task completes abnormally
   */
  @Test def testAbnormalInvoke(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LFCCF(8)
        try {
          f.invoke
          shouldThrow()
        } catch {
          case success: CountedCompleterTest.FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** quietlyInvoke task returns when task completes abnormally
   */
  @Test def testAbnormalQuietlyInvoke(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LFCCF(8)
        f.quietlyInvoke()
        assertTrue(
          f.getException.isInstanceOf[CountedCompleterTest.FJException]
        )
        checkCompletedAbnormally(f, f.getException)
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** join of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LFCCF(8)
        assertSame(f, f.fork)
        try {
          f.join
          shouldThrow()
        } catch {
          case success: CountedCompleterTest.FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** get of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new LFCCF(8)
        assertSame(f, f.fork)
        try {
          f.get
          shouldThrow()
        } catch {
          case success: ExecutionException =>
            val cause = success.getCause
            assertTrue(cause.isInstanceOf[CountedCompleterTest.FJException])
            checkCompletedAbnormally(f, cause)
        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** timed get of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkTimedGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new LFCCF(8)
        assertSame(f, f.fork)
        try {
          f.get(LONG_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case success: ExecutionException =>
            val cause = success.getCause
            assertTrue(cause.isInstanceOf[CountedCompleterTest.FJException])
            checkCompletedAbnormally(f, cause)
        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** quietlyJoin of a forked task returns when task completes abnormally
   */
  @Test def testAbnormalForkQuietlyJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LFCCF(8)
        assertSame(f, f.fork)
        f.quietlyJoin()
        assertTrue(
          f.getException.isInstanceOf[CountedCompleterTest.FJException]
        )
        checkCompletedAbnormally(f, f.getException)
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** invoke task throws exception when task cancelled
   */
  @Test def testCancelledInvoke(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
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
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** join of a forked task throws exception when task cancelled
   */
  @Test def testCancelledForkJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
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
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** get of a forked task throws exception when task cancelled
   */
  @Test def testCancelledForkGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
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
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** timed get of a forked task throws exception when task cancelled
   */
  @throws[Exception]
  @Test def testCancelledForkTimedGet(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
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
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** quietlyJoin of a forked task returns when task cancelled
   */
  @Test def testCancelledForkQuietlyJoin(): Unit = {
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        assertTrue(f.cancel(true))
        assertSame(f, f.fork)
        f.quietlyJoin()
        checkCancelled(f)
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** getPool of executing task returns its pool
   */
  @Test def testGetPool(): Unit = {
    import ForkJoinTask._
    val mainPool = CountedCompleterTest.mainPool
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        assertSame(mainPool, getPool)
      }
    }
    testInvokeOnPool(mainPool, a)
  }

  /** getPool of non-FJ task returns null
   */
  @Ignore(
    "Test-infrastructure limitation, all tests are executed in ForkJoinPool due to usage of Future in RPCCore"
  )
  @Test def testGetPool2(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = { assertNull(getPool) }
    }
    assertNull(a.invoke)
  }

  /** inForkJoinPool of executing task returns true
   */
  @Test def testInForkJoinPool(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        assertTrue(inForkJoinPool)
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** inForkJoinPool of non-FJ task returns false
   */
  @Ignore(
    "Test-infrastructure limitation, all tests are executed in ForkJoinPool due to usage of Future in RPCCore"
  )
  @Test def testInForkJoinPool2(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        assertFalse(inForkJoinPool)
      }
    }
    assertNull(a.invoke)
  }

  /** setRawResult(null) succeeds
   */
  @Test def testSetRawResult(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        setRawResult(null.asInstanceOf[Void])
        assertNull(getRawResult)
      }
    }
    assertNull(a.invoke)
  }

  /** invoke task throws exception after invoking completeExceptionally
   */
  @Test def testCompleteExceptionally2(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val n = new LCCF(8)
        val f = new LCCF(n, 8)
        val ex = new CountedCompleterTest.FJException
        f.completeExceptionally(ex)
        f.checkCompletedExceptionally(ex)
        n.checkCompletedExceptionally(ex)
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** invokeAll(t1, t2) invokes all task arguments
   */
  @Test def testInvokeAll2(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        val g = new LCCF(9)
        invokeAll(f, g)
        assertEquals(21, f.number)
        assertEquals(34, g.number)
        checkCompletedNormally(f)
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** invokeAll(tasks) with 1 argument invokes task
   */
  @Test def testInvokeAll1(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        invokeAll(f)
        checkCompletedNormally(f)
        assertEquals(21, f.number)
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** invokeAll(tasks) with > 2 argument invokes tasks
   */
  @Test def testInvokeAll3(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        val g = new LCCF(9)
        val h = new LCCF(7)
        invokeAll(f, g, h)
        assertEquals(21, f.number)
        assertEquals(34, g.number)
        assertEquals(13, h.number)
        checkCompletedNormally(f)
        checkCompletedNormally(g)
        checkCompletedNormally(h)
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** invokeAll(collection) invokes all tasks in the collection
   */
  @Test def testInvokeAllCollection(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        val g = new LCCF(9)
        val h = new LCCF(7)
        val set = new HashSet[ForkJoinTask[_]]
        set.add(f)
        set.add(g)
        set.add(h)
        invokeAll(set)
        assertEquals(21, f.number)
        assertEquals(34, g.number)
        assertEquals(13, h.number)
        checkCompletedNormally(f)
        checkCompletedNormally(g)
        checkCompletedNormally(h)
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** invokeAll(tasks) with any null task throws NPE
   */
  @Test def testInvokeAllNPE(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        val g = new LCCF(9)
        val h = null
        try {
          invokeAll(f, g, h)
          shouldThrow()
        } catch {
          case success: NullPointerException =>
        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** invokeAll(t1, t2) throw exception if any task does
   */
  @Test def testAbnormalInvokeAll2(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        val g = new LFCCF(9)
        try {
          invokeAll(f, g)
          shouldThrow()
        } catch {
          case success: CountedCompleterTest.FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** invokeAll(tasks) with 1 argument throws exception if task does
   */
  @Test def testAbnormalInvokeAll1(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g = new LFCCF(9)
        try {
          invokeAll(g)
          shouldThrow()
        } catch {
          case success: CountedCompleterTest.FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** invokeAll(tasks) with > 2 argument throws exception if any task does
   */
  @Test def testAbnormalInvokeAll3(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        val g = new LFCCF(9)
        val h = new LCCF(7)
        try {
          invokeAll(f, g, h)
          shouldThrow()
        } catch {
          case success: CountedCompleterTest.FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** invokeAll(collection) throws exception if any task does
   */
  @Test def testAbnormalInvokeAllCollection(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LFCCF(8)
        val g = new LCCF(9)
        val h = new LCCF(7)
        val set = new HashSet[ForkJoinTask[_]]
        set.add(f)
        set.add(g)
        set.add(h)
        try {
          invokeAll(set)
          shouldThrow()
        } catch {
          case success: CountedCompleterTest.FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.mainPool, a)
  }

  /** tryUnfork returns true for most recent unexecuted task, and suppresses
   *  execution
   */
  @Test def testTryUnfork(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g = new LCCF(9)
        assertSame(g, g.fork)
        val f = new LCCF(8)
        assertSame(f, f.fork)
        assertTrue(f.tryUnfork)
        helpQuiesce()
        checkNotDone(f)
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }

  /** getSurplusQueuedTaskCount returns > 0 when there are more tasks than
   *  threads
   */
  @Test def testGetSurplusQueuedTaskCount(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val h = new LCCF(7)
        assertSame(h, h.fork)
        val g = new LCCF(9)
        assertSame(g, g.fork)
        val f = new LCCF(8)
        assertSame(f, f.fork)
        assertTrue(getSurplusQueuedTaskCount > 0)
        helpQuiesce()
        assertEquals(0, getSurplusQueuedTaskCount)
        checkCompletedNormally(f)
        checkCompletedNormally(g)
        checkCompletedNormally(h)
      }
    }

    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }

  /** peekNextLocalTask returns most recent unexecuted task.
   */
  @Test def testPeekNextLocalTask(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g = new LCCF(9)
        assertSame(g, g.fork)
        val f = new LCCF(8)
        assertSame(f, f.fork)
        assertSame(f, peekNextLocalTask)
        assertNull(f.join)
        checkCompletedNormally(f)
        helpQuiesce()
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }

  /** pollNextLocalTask returns most recent unexecuted task without executing it
   */
  @Test def testPollNextLocalTask(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g = new LCCF(9)
        assertSame(g, g.fork)
        val f = new LCCF(8)
        assertSame(f, f.fork)
        assertSame(f, pollNextLocalTask)
        helpQuiesce()
        checkNotDone(f)
        assertEquals(34, g.number)
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }

  /** pollTask returns an unexecuted task without executing it
   */
  @Test def testPollTask(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g = new LCCF(9)
        assertSame(g, g.fork)
        val f = new LCCF(8)
        assertSame(f, f.fork)
        assertSame(f, pollTask)
        helpQuiesce()
        checkNotDone(f)
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }

  /** peekNextLocalTask returns least recent unexecuted task in async mode
   */
  @Test def testPeekNextLocalTaskAsync(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g = new LCCF(9)
        assertSame(g, g.fork)
        val f = new LCCF(8)
        assertSame(f, f.fork)
        assertSame(g, peekNextLocalTask)
        assertNull(f.join)
        helpQuiesce()
        checkCompletedNormally(f)
        assertEquals(34, g.number)
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(CountedCompleterTest.asyncSingletonPool, a)
  }

  /** pollNextLocalTask returns least recent unexecuted task without executing
   *  it, in async mode
   */
  @Test def testPollNextLocalTaskAsync(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g = new LCCF(9)
        assertSame(g, g.fork)
        val f = new LCCF(8)
        assertSame(f, f.fork)
        assertSame(g, pollNextLocalTask)
        helpQuiesce()
        assertEquals(21, f.number)
        checkCompletedNormally(f)
        checkNotDone(g)
      }
    }
    testInvokeOnPool(CountedCompleterTest.asyncSingletonPool, a)
  }

  /** pollTask returns an unexecuted task without executing it, in async mode
   */
  @Test def testPollTaskAsync(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g = new LCCF(9)
        assertSame(g, g.fork)
        val f = new LCCF(8)
        assertSame(f, f.fork)
        assertSame(g, pollTask)
        helpQuiesce()
        assertEquals(21, f.number)
        checkCompletedNormally(f)
        checkNotDone(g)
      }
    }
    testInvokeOnPool(CountedCompleterTest.asyncSingletonPool, a)
  }
  // versions for singleton pools
  @Test def testInvokeSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        assertNull(f.invoke)
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testQuietlyInvokeSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        f.quietlyInvoke()
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testForkJoinSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        assertSame(f, f.fork)
        assertNull(f.join)
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testForkGetSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        assertSame(f, f.fork)
        assertNull(f.get)
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testForkTimedGetSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        assertSame(f, f.fork)
        assertNull(f.get(LONG_DELAY_MS, MILLISECONDS))
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testForkTimedGetNPESingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        assertSame(f, f.fork)
        try {
          f.get(randomTimeout(), null)
          shouldThrow()
        } catch {
          case success: NullPointerException =>

        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testForkQuietlyJoinSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        assertSame(f, f.fork)
        f.quietlyJoin()
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testForkHelpQuiesceSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        assertSame(f, f.fork)
        helpQuiesce()
        assertEquals(0, getQueuedTaskCount)
        assertEquals(21, f.number)
        checkCompletedNormally(f)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testAbnormalInvokeSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LFCCF(8)
        try {
          f.invoke
          shouldThrow()
        } catch {
          case success: CountedCompleterTest.FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testAbnormalQuietlyInvokeSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LFCCF(8)
        f.quietlyInvoke()
        assertTrue(
          f.getException.isInstanceOf[CountedCompleterTest.FJException]
        )
        checkCompletedAbnormally(f, f.getException)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }

  @Test def testAbnormalForkJoinSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LFCCF(8)
        assertSame(f, f.fork)
        try {
          f.join
          shouldThrow()
        } catch {
          case success: CountedCompleterTest.FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testAbnormalForkGetSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new LFCCF(8)
        assertSame(f, f.fork)
        try {
          f.get
          shouldThrow()
        } catch {
          case success: ExecutionException =>
            val cause = success.getCause
            assertTrue(cause.isInstanceOf[CountedCompleterTest.FJException])
            checkCompletedAbnormally(f, cause)
        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }

  @Test def testAbnormalForkTimedGetSingleton(): Unit = {
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new LFCCF(8)
        assertSame(f, f.fork)
        try {
          f.get(LONG_DELAY_MS, MILLISECONDS)
          shouldThrow()
        } catch {
          case success: ExecutionException =>
            val cause = success.getCause
            assertTrue(cause.isInstanceOf[CountedCompleterTest.FJException])
            checkCompletedAbnormally(f, cause)
        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testAbnormalForkQuietlyJoinSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LFCCF(8)
        assertSame(f, f.fork)
        f.quietlyJoin()
        assertTrue(
          f.getException.isInstanceOf[CountedCompleterTest.FJException]
        )
        checkCompletedAbnormally(f, f.getException)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testCancelledInvokeSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
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
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testCancelledForkJoinSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
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
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testCancelledForkGetSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
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
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @throws[Exception]
  @Test def testCancelledForkTimedGetSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      @throws[Exception]
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
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
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testCancelledForkQuietlyJoinSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        assertTrue(f.cancel(true))
        assertSame(f, f.fork)
        f.quietlyJoin()
        checkCancelled(f)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testCompleteExceptionallySingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val n = new LCCF(8)
        val f = new LCCF(n, 8)
        val ex = new CountedCompleterTest.FJException
        f.completeExceptionally(ex)
        f.checkCompletedExceptionally(ex)
        n.checkCompletedExceptionally(ex)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testInvokeAll2Singleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        val g = new LCCF(9)
        invokeAll(f, g)
        assertEquals(21, f.number)
        assertEquals(34, g.number)
        checkCompletedNormally(f)
        checkCompletedNormally(g)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testInvokeAll1Singleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        invokeAll(f)
        checkCompletedNormally(f)
        assertEquals(21, f.number)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testInvokeAll3Singleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        val g = new LCCF(9)
        val h = new LCCF(7)
        invokeAll(f, g, h)
        assertEquals(21, f.number)
        assertEquals(34, g.number)
        assertEquals(13, h.number)
        checkCompletedNormally(f)
        checkCompletedNormally(g)
        checkCompletedNormally(h)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testInvokeAllCollectionSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        val g = new LCCF(9)
        val h = new LCCF(7)
        val set = new HashSet[ForkJoinTask[_]]
        set.add(f)
        set.add(g)
        set.add(h)
        invokeAll(set)
        assertEquals(21, f.number)
        assertEquals(34, g.number)
        assertEquals(13, h.number)
        checkCompletedNormally(f)
        checkCompletedNormally(g)
        checkCompletedNormally(h)
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testInvokeAllNPESingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        val g = new LCCF(9)
        val h = null
        try {
          invokeAll(f, g, h)
          shouldThrow()
        } catch {
          case success: NullPointerException =>

        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }

  @Test def testAbnormalInvokeAll2Singleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        val g = new LFCCF(9)
        try {
          invokeAll(f, g)
          shouldThrow()
        } catch {
          case success: CountedCompleterTest.FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testAbnormalInvokeAll1Singleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val g = new LFCCF(9)
        try {
          invokeAll(g)
          shouldThrow()
        } catch {
          case success: CountedCompleterTest.FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testAbnormalInvokeAll3Singleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LCCF(8)
        val g = new LFCCF(9)
        val h = new LCCF(7)
        try {
          invokeAll(f, g, h)
          shouldThrow()
        } catch {
          case success: CountedCompleterTest.FJException =>
            checkCompletedAbnormally(g, success)
        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }
  @Test def testAbnormalInvokeAllCollectionSingleton(): Unit = {
    import ForkJoinTask._
    val a = new CheckedRecursiveAction() {
      override protected def realCompute(): Unit = {
        val f = new LFCCF(8)
        val g = new LCCF(9)
        val h = new LCCF(7)
        val set = new HashSet[ForkJoinTask[_]]
        set.add(f)
        set.add(g)
        set.add(h)
        try {
          invokeAll(set)
          shouldThrow()
        } catch {
          case success: CountedCompleterTest.FJException =>
            checkCompletedAbnormally(f, success)
        }
      }
    }
    testInvokeOnPool(CountedCompleterTest.singletonPool, a)
  }

  // Since Java 8

  /** CountedCompleter class javadoc code sample, version 1. */
  def forEach1[E](array: Array[E], action: Consumer[E]): Unit = {
    class Task(val parent: Task, val lo: Int, val hi: Int)
        extends CountedCompleter[Void](parent) {
      override def compute(): Unit = {
        if (hi - lo >= 2) {
          val mid = (lo + hi) >>> 1
          // must set pending count before fork
          setPendingCount(2)
          new Task(this, mid, hi).fork // right child

          new Task(this, lo, mid).fork // left child

        } else if (hi > lo) action.accept(array(lo))
        tryComplete()
      }
    }
    new Task(null, 0, array.length).invoke
  }

  /** CountedCompleter class javadoc code sample, version 2. */
  def forEach2[E](array: Array[E], action: Consumer[E]): Unit = {
    class Task(val parent: Task, val lo: Int, val hi: Int)
        extends CountedCompleter[Void](parent) {
      override def compute(): Unit = {
        if (hi - lo >= 2) {
          val mid = (lo + hi) >>> 1
          setPendingCount(1) // looks off by one, but correct!

          new Task(this, mid, hi).fork
          new Task(this, lo, mid).compute() // direct invoke

        } else {
          if (hi > lo) action.accept(array(lo))
          tryComplete()
        }
      }
    }
    new Task(null, 0, array.length).invoke
  }

  /** CountedCompleter class javadoc code sample, version 3. */
  def forEach3[E](array: Array[E], action: Consumer[E]): Unit = {
    class Task(val parent: Task, val lo: Int, val hi: Int)
        extends CountedCompleter[Void](parent) {
      override def compute(): Unit = {
        var n = hi - lo

        while ({
          n >= 2
        }) {
          addToPendingCount(1)
          new Task(this, lo + n / 2, lo + n).fork

          n /= 2
        }
        if (n > 0) action.accept(array(lo))
        propagateCompletion()
      }
    }
    new Task(null, 0, array.length).invoke
  }

  /** CountedCompleter class javadoc code sample, version 4. */
  def forEach4[E](array: Array[E], action: Consumer[E]): Unit = {
    class Task(val parent: Task, val lo: Int, val hi: Int)
        extends CountedCompleter[Void](
          parent,
          31 - Integer.numberOfLeadingZeros(hi - lo)
        ) {
      override def compute(): Unit = {
        var n = hi - lo
        while ({
          n >= 2
        }) {
          new Task(this, lo + n / 2, lo + n).fork
          n /= 2
        }
        action.accept(array(lo))
        propagateCompletion()
      }
    }
    if (array.length > 0) new Task(null, 0, array.length).invoke
  }

  def testRecursiveDecomposition(
      action: BiConsumer[Array[Integer], Consumer[Integer]]
  ): Unit = {
    val n = ThreadLocalRandom.current.nextInt(8)
    val a = new Array[Integer](n)
    for (i <- 0 until n) {
      a(i) = i + 1
    }
    val ai = new AtomicInteger(0)
    action.accept(a, ai.addAndGet(_))
    assertEquals(n * (n + 1) / 2, ai.get)
  }

  /** Variants of divide-by-two recursive decomposition into leaf tasks, as
   *  described in the CountedCompleter class javadoc code samples
   */
  @Test def testRecursiveDecomposition(): Unit = {
    testRecursiveDecomposition(forEach1)
    testRecursiveDecomposition(forEach2)
    testRecursiveDecomposition(forEach3)
    testRecursiveDecomposition(forEach4)
  }

}
