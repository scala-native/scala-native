/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.TimeUnit._
import java.util.concurrent._
import java.util.HashSet

import org.junit.{Test, Ignore}
import org.junit.Assert._
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class RecursiveTaskTest extends JSR166Test {
  import JSR166Test._

  final class FJException() extends RuntimeException()

  /** An invalid return value for Fib. */
  final val NoResult = -17

  private def mainPool() = new ForkJoinPool()
  private def singletonPool() = new ForkJoinPool(1)
  private def asyncSingletonPool() =
    new ForkJoinPool(
      1,
      ForkJoinPool.defaultForkJoinWorkerThreadFactory,
      null,
      true
    )

  private def testInvokeOnPool[T](pool: ForkJoinPool, a: RecursiveTask[T]): T =
    usingPoolCleaner(pool) { pool =>
      checkNotDone(a)
      val result = pool.invoke(a)
      checkCompletedNormally(a, result)
      result
    }

  def checkNotDone(a: RecursiveTask[_]) = {
    assertFalse("isDone", a.isDone())
    assertFalse("isCompletedNormally", a.isCompletedNormally())
    assertFalse("isCompletedAbnormally", a.isCompletedAbnormally())
    assertFalse("isCancelled", a.isCancelled())
    assertNull("exception", a.getException())
    assertNull("rawResult", a.getRawResult())

    if (!ForkJoinTask.inForkJoinPool()) {
      Thread.currentThread().interrupt()
      try {
        a.get()
        shouldThrow()
      } catch {
        case _: InterruptedException => ()
        case fail: Throwable         => threadUnexpectedException(fail)
      }

      Thread.currentThread().interrupt()
      try {
        a.get(randomTimeout(), randomTimeUnit())
        shouldThrow()
      } catch {
        case _: InterruptedException => ()
        case fail: Throwable         => threadUnexpectedException(fail)
      }
    }

    try {
      a.get(randomExpiredTimeout(), randomTimeUnit())
      shouldThrow()
    } catch {
      case success: TimeoutException => ()
      case fail: Throwable           => threadUnexpectedException(fail)
    }
  }

  def checkCompletedNormally[T](a: RecursiveTask[T], expectedValue: T) = {
    assertTrue(a.isDone())
    assertFalse(a.isCancelled())
    assertTrue(a.isCompletedNormally())
    assertFalse(a.isCompletedAbnormally())
    assertNull(a.getException())
    assertSame(expectedValue, a.getRawResult())
    assertSame(expectedValue, a.join())
    assertFalse(a.cancel(false))
    assertFalse(a.cancel(true))
    try {
      val v1 = a.get()
      val v2 = a.get(randomTimeout(), randomTimeUnit())
      assertSame(expectedValue, v1)
      assertSame(expectedValue, v2)
    } catch {
      case fail: Throwable => threadUnexpectedException(fail)
    }
  }

  /** Waits for the task to complete, and checks that when it does, it will have
   *  an Integer result equals to the given int.
   */
  def checkCompletesNormally(a: RecursiveTask[Integer], expectedValue: Int) = {
    val r = a.join()
    assertEquals(expectedValue, r)
    checkCompletedNormally(a, r)
  }

  /** Like checkCompletesNormally, but verifies that the task has already
   *  completed.
   */
  def checkCompletedNormally(
      a: RecursiveTask[Integer],
      expectedValue: Int
  ): Unit = {
    val r = a.getRawResult()
    assertEquals(expectedValue, r)
    checkCompletedNormally(a, r: Integer)
  }

  def checkCancelled(a: RecursiveTask[_]) = {
    assertTrue(a.isDone())
    assertTrue(a.isCancelled())
    assertFalse(a.isCompletedNormally())
    assertTrue(a.isCompletedAbnormally())
    assertTrue(a.getException().isInstanceOf[CancellationException])
    assertNull(a.getRawResult())
    try {
      a.join()
      shouldThrow()
    } catch {
      case success: CancellationException => ()
      case fail: Throwable                => threadUnexpectedException(fail)
    }
    try {
      a.get()
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

  def checkCompletedAbnormally(a: RecursiveTask[_], t: Throwable) = {
    assertTrue(a.isDone())
    assertFalse(a.isCancelled())
    assertFalse(a.isCompletedNormally())
    assertTrue(a.isCompletedAbnormally())
    assertSame(t.getClass(), a.getException().getClass())
    assertNull(a.getRawResult())
    assertFalse(a.cancel(false))
    assertFalse(a.cancel(true))

    assertThrows(t.getClass(), a.join())
    try {
      a.get()
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertSame(t.getClass(), success.getCause().getClass())
      case fail: Throwable => threadUnexpectedException(fail)
    }
    try {
      a.get(randomTimeout(), randomTimeUnit())
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertSame(t.getClass(), success.getCause().getClass())
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
  }

  /** A simple recursive task for testing. */
  final class FibTask(val number: Int) extends CheckedRecursiveTask[Integer] {
    // public accessor
    def doCompute(): Integer = compute()
    def realCompute(): Integer = {
      val n = number
      if (n <= 1) n
      else {
        val f1 = new FibTask(n - 1)
        f1.fork()
        new FibTask(n - 2).compute() + f1.join()
      }
    }
    def publicSetRawResult(result: Integer): Unit = setRawResult(result)
  }

  /** A recursive action failing in base case. */
  final class FailingFibTask(val number: Int) extends RecursiveTask[Integer] {
    this.setRawResult(null)

    override def compute(): Integer = {
      val n = number
      if (n <= 1) throw new FJException()
      val f1 = new FailingFibTask(n - 1)
      f1.fork()
      new FibTask(n - 2).doCompute() + f1.join()
    }
  }

  /** invoke returns value when task completes normally. isCompletedAbnormally
   *  and isCancelled return false for normally completed tasks. getRawResult of
   *  a completed non-null task returns value;
   */
  @Test def testInvoke(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        val r = f.invoke()
        assertEquals(21, r)
        checkCompletedNormally(f, r)
        r
      }
    }
    assertEquals(
      21,
      testInvokeOnPool(mainPool(), a).toInt
    )
  }

  /** quietlyInvoke task returns when task completes normally.
   *  isCompletedAbnormally and isCancelled return false for normally completed
   *  tasks
   */
  @Test def testQuietlyInvoke(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        f.quietlyInvoke()
        checkCompletedNormally(f, 21)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** join of a forked task returns when task completes
   */
  @Test def testForkJoin(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        assertSame(f, f.fork())
        val r = f.join
        assertEquals(21, r)
        checkCompletedNormally(f, r)
        r
      }
    }
    assertEquals(21, testInvokeOnPool(mainPool(), a))
  }

  /** get of a forked task returns when task completes
   */
  def testForkGet(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        assertSame(f, f.fork())
        val r = f.get()
        assertEquals(21, r)
        checkCompletedNormally(f, r)
        r
      }
    }
    assertEquals(21, testInvokeOnPool(mainPool(), a))
  }

  /** timed get of a forked task returns when task completes
   */
  @Test def testForkTimedGet(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        assertSame(f, f.fork())
        val r = f.get(LONG_DELAY_MS, MILLISECONDS)
        assertEquals(21, r)
        checkCompletedNormally(f, r)
        r
      }
    }
    assertEquals(21, testInvokeOnPool(mainPool(), a))
  }

  /** quietlyJoin of a forked task returns when task completes
   */
  @Test def testForkQuietlyJoin(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        assertSame(f, f.fork())
        f.quietlyJoin()
        val r = f.getRawResult()
        assertEquals(21, r)
        checkCompletedNormally(f, r)
        r
      }
    }
    assertEquals(21, testInvokeOnPool(mainPool(), a))
  }

  /** helpQuiesce returns when tasks are complete. getQueuedTaskCount returns 0
   *  when quiescent
   */
  @Test def testhelpQuiesce(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        assertSame(f, f.fork())
        ForkJoinTask.helpQuiesce()
        while (!f.isDone()) () // wait out race
        assertEquals(0, ForkJoinTask.getQueuedTaskCount())
        checkCompletedNormally(f, 21)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** invoke task throws exception when task completes abnormally
   */
  @Test def testAbnormalInvoke(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FailingFibTask(8)
        checkCompletedAbnormally(
          f,
          assertThrows(classOf[FJException], f.invoke())
        )
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** quietlyInvoke task returns when task completes abnormally
   */
  @Test def testAbnormalQuietlyInvoke(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FailingFibTask(8)
        f.quietlyInvoke()
        assertTrue(f.getException().isInstanceOf[FJException])
        checkCompletedAbnormally(f, f.getException())
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** join of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkJoin(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FailingFibTask(8)
        assertSame(f, f.fork())
        checkCompletedAbnormally(
          f,
          assertThrows(classOf[FJException], f.join())
        )
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** get of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkGet(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FailingFibTask(8)
        assertSame(f, f.fork())
        val ex = assertThrows(classOf[ExecutionException], f.get())
        val cause = ex.getCause()
        assertTrue(cause.isInstanceOf[FJException])
        checkCompletedAbnormally(f, cause)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** timed get of a forked task throws exception when task completes abnormally
   */
  @Test def testAbnormalForkTimedGet(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FailingFibTask(8)
        assertSame(f, f.fork())
        val ex = assertThrows(
          classOf[ExecutionException],
          f.get(LONG_DELAY_MS, MILLISECONDS)
        )
        val cause = ex.getCause()
        assertTrue(cause.isInstanceOf[FJException])
        checkCompletedAbnormally(f, cause)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** quietlyJoin of a forked task returns when task completes abnormally
   */
  @Test def testAbnormalForkQuietlyJoin(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FailingFibTask(8)
        assertSame(f, f.fork())
        f.quietlyJoin()
        assertTrue(f.getException().isInstanceOf[FJException])
        checkCompletedAbnormally(f, f.getException())
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** invoke task throws exception when task cancelled
   */
  @Test def testCancelledInvoke(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        assertTrue(f.cancel(true))
        assertThrows(classOf[CancellationException], f.invoke())
        checkCancelled(f)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** join of a forked task throws exception when task cancelled
   */
  @Test def testCancelledForkJoin(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        assertTrue(f.cancel(true))
        assertSame(f, f.fork())
        assertThrows(classOf[CancellationException], f.join())
        checkCancelled(f)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** get of a forked task throws exception when task cancelled
   */
  @Test def testCancelledForkGet(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        assertTrue(f.cancel(true))
        assertSame(f, f.fork())
        assertThrows(classOf[CancellationException], f.get())
        checkCancelled(f)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** timed get of a forked task throws exception when task cancelled
   */
  @Test def testCancelledForkTimedGet(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        assertTrue(f.cancel(true))
        assertSame(f, f.fork())
        assertThrows(
          classOf[CancellationException],
          f.get(LONG_DELAY_MS, MILLISECONDS)
        )
        checkCancelled(f)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** quietlyJoin of a forked task returns when task cancelled
   */
  @Test def testCancelledForkQuietlyJoin(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        assertTrue(f.cancel(true))
        assertSame(f, f.fork())
        f.quietlyJoin()
        checkCancelled(f)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** getPool of executing task returns its pool
   */
  @Test def testGetPool(): Unit = {
    val mainPool = this.mainPool()
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        assertSame(mainPool, ForkJoinTask.getPool())
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool, a))
  }

  /** getPool of non-FJ task returns null
   */
  @Ignore(
    "Test-infrastructure limitation, all tests are executed in ForkJoinPool due to usage of Future in RPCCore"
  )
  @Test def testGetPool2(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        assertNull(ForkJoinTask.getPool())
        NoResult
      }
    }
    assertSame(NoResult, a.invoke)
  }

  /** inForkJoinPool of executing task returns true
   */
  @Test def testInForkJoinPool(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        assertTrue(ForkJoinTask.inForkJoinPool())
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** inForkJoinPool of non-FJ task returns false
   */
  @Ignore(
    "Test-infrastructure limitation, all tests are executed in ForkJoinPool due to usage of Future in RPCCore"
  )
  @Test def testInForkJoinPool2(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        assertFalse(ForkJoinTask.inForkJoinPool())
        NoResult
      }
    }
    assertSame(NoResult, a.invoke)
  }

  /** The value set by setRawResult is returned by getRawResult
   */
  @Test def testSetRawResult(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        setRawResult(NoResult)
        assertSame(NoResult, getRawResult())
        NoResult
      }
    }
    assertSame(NoResult, a.invoke)
  }

  /** A reinitialized normally completed task may be re-invoked
   */
  @Test def testReinitialize(): Unit = {
    val a: RecursiveTask[Integer] = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {

        val f = new FibTask(8)
        checkNotDone(f)
        for (i <- 0 until 3) {
          val r = f.invoke()
          assertEquals(21, r)
          checkCompletedNormally(f, r)
          f.reinitialize()
          f.publicSetRawResult(null: Integer)
          checkNotDone(f)
        }
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** A reinitialized abnormally completed task may be re-invoked
   */
  @Test def testReinitializeAbnormal(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FailingFibTask(8)
        checkNotDone(f)
        for (i <- 0 until 3) {
          checkCompletedAbnormally(
            f,
            assertThrows(classOf[FJException], f.invoke())
          )
          f.reinitialize()
          checkNotDone(f)
        }
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** invoke task throws exception after invoking completeExceptionally
   */
  @Test def testCompleteExceptionally(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        f.completeExceptionally(new FJException())
        checkCompletedAbnormally(
          f,
          assertThrows(classOf[FJException], f.invoke())
        )
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** invoke task suppresses execution invoking complete
   */
  @Test def testComplete(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        f.complete(NoResult)
        val r = f.invoke()
        assertSame(NoResult, r)
        checkCompletedNormally(f, NoResult)
        r
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** invokeAll(t1, t2) invokes all task arguments
   */
  @Test def testInvokeAll2(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        val g = new FibTask(9)
        ForkJoinTask.invokeAll(f, g)
        checkCompletedNormally(f, 21)
        checkCompletedNormally(g, 34)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** invokeAll(tasks) with 1 argument invokes task
   */
  @Test def testInvokeAll1(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        ForkJoinTask.invokeAll(f)
        checkCompletedNormally(f, 21)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** invokeAll(tasks) with > 2 argument invokes tasks
   */
  @Test def testInvokeAll3(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        val g = new FibTask(9)
        val h = new FibTask(7)
        ForkJoinTask.invokeAll(f, g, h)
        assertTrue(f.isDone())
        assertTrue(g.isDone())
        assertTrue(h.isDone())
        checkCompletedNormally(f, 21)
        checkCompletedNormally(g, 34)
        checkCompletedNormally(h, 13)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** invokeAll(collection) invokes all tasks in the collection
   */
  @Test def testInvokeAllCollection(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        val g = new FibTask(9)
        val h = new FibTask(7)
        val set = new HashSet[RecursiveTask[Integer]]()
        set.add(f)
        set.add(g)
        set.add(h)
        ForkJoinTask.invokeAll(set)
        assertTrue(f.isDone())
        assertTrue(g.isDone())
        assertTrue(h.isDone())
        checkCompletedNormally(f, 21)
        checkCompletedNormally(g, 34)
        checkCompletedNormally(h, 13)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** invokeAll(tasks) with any null task throws NPE
   */
  @Test def testInvokeAllNPE(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        val g = new FibTask(9)
        val h = null: FibTask
        assertThrows(
          classOf[NullPointerException],
          ForkJoinTask.invokeAll(f, g, h)
        )
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** invokeAll(t1, t2) throw exception if any task does
   */
  @Test def testAbnormalInvokeAll2(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        val g = new FailingFibTask(9)
        checkCompletedAbnormally(
          g,
          assertThrows(classOf[FJException], ForkJoinTask.invokeAll(f, g))
        )
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** invokeAll(tasks) with 1 argument throws exception if task does
   */
  @Test def testAbnormalInvokeAll1(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val g = new FailingFibTask(9)
        checkCompletedAbnormally(
          g,
          assertThrows(classOf[FJException], ForkJoinTask.invokeAll(g))
        )
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** invokeAll(tasks) with > 2 argument throws exception if any task does
   */
  @Test def testAbnormalInvokeAll3(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FibTask(8)
        val g = new FailingFibTask(9)
        val h = new FibTask(7)
        checkCompletedAbnormally(
          g,
          assertThrows(classOf[FJException], ForkJoinTask.invokeAll(f, g, h))
        )
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** invokeAll(collection) throws exception if any task does
   */
  @Test def testAbnormalInvokeAllCollection(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val f = new FailingFibTask(8)
        val g = new FibTask(9)
        val h = new FibTask(7)
        val set = new HashSet[RecursiveTask[Integer]]()
        set.add(f)
        set.add(g)
        set.add(h)
        checkCompletedAbnormally(
          f,
          assertThrows(classOf[FJException], ForkJoinTask.invokeAll(set))
        )
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(mainPool(), a))
  }

  /** tryUnfork returns true for most recent unexecuted task, and suppresses
   *  execution
   */
  @Test def testTryUnfork(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val g = new FibTask(9)
        assertSame(g, g.fork())
        val f = new FibTask(8)
        assertSame(f, f.fork())
        assertTrue(f.tryUnfork())
        ForkJoinTask.helpQuiesce()
        checkNotDone(f)
        checkCompletedNormally(g, 34)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(singletonPool(), a))
  }

  /** getSurplusQueuedTaskCount returns > 0 when there are more tasks than
   *  threads
   */
  @Test def testGetSurplusQueuedTaskCount(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val h = new FibTask(7)
        assertSame(h, h.fork())
        val g = new FibTask(9)
        assertSame(g, g.fork())
        val f = new FibTask(8)
        assertSame(f, f.fork())
        assertTrue(ForkJoinTask.getSurplusQueuedTaskCount() > 0)
        ForkJoinTask.helpQuiesce()
        assertEquals(0, ForkJoinTask.getSurplusQueuedTaskCount())
        checkCompletedNormally(f, 21)
        checkCompletedNormally(g, 34)
        checkCompletedNormally(h, 13)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(singletonPool(), a))
  }

  /** peekNextLocalTask returns most recent unexecuted task.
   */
  @Test def testPeekNextLocalTask(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val g = new FibTask(9)
        assertSame(g, g.fork())
        val f = new FibTask(8)
        assertSame(f, f.fork())
        assertSame(f, ForkJoinTask.peekNextLocalTask())
        checkCompletesNormally(f, 21)
        ForkJoinTask.helpQuiesce()
        checkCompletedNormally(g, 34)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(singletonPool(), a))
  }

  /** pollNextLocalTask returns most recent unexecuted task without executing it
   */
  @Test def testPollNextLocalTask(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val g = new FibTask(9)
        assertSame(g, g.fork())
        val f = new FibTask(8)
        assertSame(f, f.fork())
        assertSame(f, ForkJoinTask.pollNextLocalTask())
        ForkJoinTask.helpQuiesce()
        checkNotDone(f)
        checkCompletedNormally(g, 34)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(singletonPool(), a))
  }

  /** pollTask returns an unexecuted task without executing it
   */
  @Test def testPollTask(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val g = new FibTask(9)
        assertSame(g, g.fork())
        val f = new FibTask(8)
        assertSame(f, f.fork())
        assertSame(f, ForkJoinTask.pollTask())
        ForkJoinTask.helpQuiesce()
        checkNotDone(f)
        checkCompletedNormally(g, 34)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(singletonPool(), a))
  }

  /** peekNextLocalTask returns least recent unexecuted task in async mode
   */
  @Test def testPeekNextLocalTaskAsync(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val g = new FibTask(9)
        assertSame(g, g.fork())
        val f = new FibTask(8)
        assertSame(f, f.fork())
        assertSame(g, ForkJoinTask.peekNextLocalTask())
        assertEquals(21, f.join())
        ForkJoinTask.helpQuiesce()
        checkCompletedNormally(f, 21)
        checkCompletedNormally(g, 34)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(asyncSingletonPool(), a))
  }

  /** pollNextLocalTask returns least recent unexecuted task without executing
   *  it, in async mode
   */
  @Test def testPollNextLocalTaskAsync(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {
        val g = new FibTask(9)
        assertSame(g, g.fork())
        val f = new FibTask(8)
        assertSame(f, f.fork())
        assertSame(g, ForkJoinTask.pollNextLocalTask())
        ForkJoinTask.helpQuiesce()
        checkCompletedNormally(f, 21)
        checkNotDone(g)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(asyncSingletonPool(), a))
  }

  /** pollTask returns an unexecuted task without executing it, in async mode
   */
  @Test def testPollTaskAsync(): Unit = {
    val a = new CheckedRecursiveTask[Integer] {
      protected def realCompute(): Integer = {

        val g = new FibTask(9)
        assertSame(g, g.fork())
        val f = new FibTask(8)
        assertSame(f, f.fork())
        assertSame(g, ForkJoinTask.pollTask())
        ForkJoinTask.helpQuiesce()
        checkCompletedNormally(f, 21)
        checkNotDone(g)
        NoResult
      }
    }
    assertSame(NoResult, testInvokeOnPool(asyncSingletonPool(), a))
  }
}
