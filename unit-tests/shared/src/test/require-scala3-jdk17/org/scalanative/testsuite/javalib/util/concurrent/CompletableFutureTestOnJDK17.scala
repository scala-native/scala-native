// scalafmt: { maxColumn = 120}
// Compiled only on Scala 2.13 and Scala 3
// Type inference under Scala 2.12 reports ~90 errors, mostly becouse Java types are not covariant (by-design)
// Revision 1.226, Committed 	Mar 22 2022

/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.CompletableFuture.failedFuture
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util
import java.util.Objects
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.ArrayList

import org.junit.*
import org.junit.Assert.*

import CompletableFutureTest.*
object CompletableFutureTestOnJDK17 {

  /** Permits the testing of parallel code for the 3 different execution modes without copy/pasting all the test
   *  methods.
   */
  trait ExecutionMode extends CompletableFutureTest.ExecutionMode {
    def exceptionally[T <: AnyRef](f: CompletableFuture[T], fn: Function[Throwable, ? <: T]): CompletableFuture[T]
    def exceptionallyCompose[T <: AnyRef](
        f: CompletableFuture[T],
        fn: Function[Throwable, ? <: CompletionStage[T]]
    ): CompletableFuture[T]
  }
  object ExecutionMode {
    val values = Array(SYNC, ASYNC, EXECUTOR)
    case object SYNC extends CompletableFutureTest.ExecutionMode.SYNC with ExecutionMode {
      def exceptionally[T <: AnyRef](f: CompletableFuture[T], fn: Function[Throwable, ? <: T]): CompletableFuture[T] =
        f.exceptionally(fn)
      def exceptionallyCompose[T <: AnyRef](
          f: CompletableFuture[T],
          fn: Function[Throwable, ? <: CompletionStage[T]]
      ): CompletableFuture[T] = f.exceptionallyCompose(fn)
    }
    case object ASYNC extends CompletableFutureTest.ExecutionMode.ASYNC with ExecutionMode {
      def exceptionally[T <: AnyRef](f: CompletableFuture[T], fn: Function[Throwable, ? <: T]): CompletableFuture[T] =
        f.exceptionallyAsync(fn)
      def exceptionallyCompose[T <: AnyRef](
          f: CompletableFuture[T],
          fn: Function[Throwable, ? <: CompletionStage[T]]
      ): CompletableFuture[T] = f.exceptionallyComposeAsync(fn)
    }
    case object EXECUTOR extends CompletableFutureTest.ExecutionMode.EXECUTOR with ExecutionMode {
      def exceptionally[T <: AnyRef](f: CompletableFuture[T], fn: Function[Throwable, ? <: T]): CompletableFuture[T] =
        f.exceptionallyAsync(fn, new ThreadExecutor())
      def exceptionallyCompose[T <: AnyRef](
          f: CompletableFuture[T],
          fn: Function[Throwable, ? <: CompletionStage[T]]
      ): CompletableFuture[T] = f.exceptionallyComposeAsync(fn, new ThreadExecutor())
    }
  }

  object Monad {
    class ZeroException extends RuntimeException("monadic zero") {}

    // "return", "unit"
    def unit[T](value: T): CompletableFuture[T] = completedFuture(value)

    // monadic zero ?
    def zero[T](): CompletableFuture[T] = failedFuture(new Monad.ZeroException)

    // >=>
    def compose[T, U, V](
        f: Function[T, CompletableFuture[U]],
        g: Function[U, CompletableFuture[V]]
    ): Function[T, CompletableFuture[V]] = (x: T) => f.apply(x).thenCompose(g)

    def assertZero[T](f: CompletableFuture[T]): Unit = {
      try {
        val res = f.getNow(null.asInstanceOf[AnyRef].asInstanceOf[T])
        throw new AssertionError("should throw, got: " + f)
      } catch {
        case success: CompletionException =>
          assertTrue(success.getCause().isInstanceOf[Monad.ZeroException])
      }
    }

    def assertFutureEquals[T](f: CompletableFuture[T], g: CompletableFuture[T]): Unit = {
      var fval: T = null.asInstanceOf[T]
      var gval: T = null.asInstanceOf[T]
      var fex: Throwable = null
      var gex: Throwable = null
      try fval = f.get()
      catch {
        case ex: ExecutionException =>
          fex = ex.getCause()
        case ex: Throwable =>
          fex = ex
      }
      try gval = g.get()
      catch {
        case ex: ExecutionException =>
          gex = ex.getCause()
        case ex: Throwable =>
          gex = ex
      }
      if fex != null || gex != null then assertSame(fex.getClass(), gex.getClass())
      else assertEquals(fval, gval)
    }

    class PlusFuture[T <: AnyRef] extends CompletableFuture[T] {
      val firstFailure = new AtomicReference[Throwable](null)
    }

    /** Implements "monadic plus". */
    def plus[T <: AnyRef](f: CompletableFuture[? <: T], g: CompletableFuture[? <: T]): CompletableFuture[T] = {
      val plus = new Monad.PlusFuture[T]
      val action: BiConsumer[T, Throwable] = (result: T, ex: Throwable) => {
        try
          if ex == null then {
            if plus.complete(result) then if plus.firstFailure.get() != null then plus.firstFailure.set(null)
          } else if plus.firstFailure.compareAndSet(null, ex) then {
            if plus.isDone then plus.firstFailure.set(null)
          } else {
            // first failure has precedence
            val first = plus.firstFailure.getAndSet(null)
            // may fail with "Self-suppression not permitted"
            try first.addSuppressed(ex)
            catch { case ignored: Exception => }
            plus.completeExceptionally(first)
          }
        catch { case unexpected: Throwable => plus.completeExceptionally(unexpected) }
      }
      f.whenComplete(action)
      g.whenComplete(action)
      plus
    }
  }
}

class CompletableFutureTestOnJDK17 extends CompletableFutureTest {
  import JSR166Test.*
  import CompletableFutureTest.{ExecutionMode as _, *}
  import CompletableFutureTestOnJDK17.*

  /** exceptionally action is not invoked() when source completes normally, and source result is propagated
   */
  @Test def testExceptionally_normalCompletion(): Unit = {
    for m <- ExecutionMode.values do {
      for createIncomplete <- Array[Boolean](true, false) do {
        for v1 <- Array[Item](one, null) do {
          val ran = new AtomicInteger(0)
          val f = new CompletableFuture[Item]
          if !createIncomplete then assertTrue(f.complete(v1))
          val g = m.exceptionally(
            f,
            (t: Throwable) => {
              ran.getAndIncrement()
              throw new AssertionError("should not be called")

            }
          )
          if createIncomplete then assertTrue(f.complete(v1))
          checkCompletedNormally(g, v1)
          checkCompletedNormally(f, v1)
          assertEquals(0, ran.get())
        }
      }
    }
  }

  /** exceptionally action completes with function value on source exception
   */
  @Test def testExceptionally_exceptionalCompletion(): Unit = {
    for m <- ExecutionMode.values do {
      for createIncomplete <- Array[Boolean](true, false) do {
        for v1 <- Array[Item](one, null) do {
          val ran = new AtomicInteger(0)
          val ex = new CFException
          val f = new CompletableFuture[Item]
          if !createIncomplete then f.completeExceptionally(ex)
          val g = m.exceptionally(
            f,
            (t: Throwable) => {
              m.checkExecutionMode()
              assertSame(t, ex)
              ran.getAndIncrement()
              v1

            }
          )
          if createIncomplete then f.completeExceptionally(ex)
          checkCompletedNormally(g, v1)
          assertEquals(1, ran.get())
        }
      }
    }
  }

  /** If an "exceptionally action" throws an exception, it completes exceptionally with that exception
   */
  @Test def testExceptionally_exceptionalCompletionActionFailed(): Unit = {
    for m <- ExecutionMode.values do {
      for createIncomplete <- Array[Boolean](true, false) do {
        val ran = new AtomicInteger(0)
        val ex1 = new CFException
        val ex2 = new CFException
        val f = new CompletableFuture[Item]
        if !createIncomplete then f.completeExceptionally(ex1)
        val g = m.exceptionally(
          f,
          (t: Throwable) => {
            m.checkExecutionMode()
            assertSame(t, ex1)
            ran.getAndIncrement()
            throw ex2

          }
        )
        if createIncomplete then f.completeExceptionally(ex1)
        checkCompletedWithWrappedException(g, ex2)
        checkCompletedExceptionally(f, ex1)
        assertEquals(1, ran.get())
      }
    }
  }

  /** exceptionallyCompose result completes normally after normal completion of source
   */
  @Test def testExceptionallyCompose_normalCompletion(): Unit = {
    for m <- ExecutionMode.values do {
      for createIncomplete <- Array[Boolean](true, false) do {
        for v1 <- Array[Item](one, null) do {
          val f = new CompletableFuture[Item]
          val r = new ExceptionalCompletableFutureFunction(m)
          if !createIncomplete then assertTrue(f.complete(v1))
          val g = m.exceptionallyCompose(f, r)
          if createIncomplete then assertTrue(f.complete(v1))
          checkCompletedNormally(f, v1)
          checkCompletedNormally(g, v1)
          r.assertNotinvoked()
        }
      }
    }
  }

  /** exceptionallyCompose result completes normally after exceptional completion of source
   */
  @Test def testExceptionallyCompose_exceptionalCompletion(): Unit = {
    for m <- ExecutionMode.values do {
      for createIncomplete <- Array[Boolean](true, false) do {
        val ex = new CFException
        val r = new ExceptionalCompletableFutureFunction(m)
        val f = new CompletableFuture[Item]
        if !createIncomplete then f.completeExceptionally(ex)
        val g = m.exceptionallyCompose(f, r)
        if createIncomplete then f.completeExceptionally(ex)
        checkCompletedExceptionally[Item](f, ex)
        checkCompletedNormally[Item](g, r.value)
        r.assertinvoked()
      }
    }
  }

  /** exceptionallyCompose completes exceptionally on exception if action does
   */
  @Test def testExceptionallyCompose_actionFailed(): Unit = {
    for m <- ExecutionMode.values do {
      for createIncomplete <- Array[Boolean](true, false) do {
        val ex = new CFException
        val f = new CompletableFuture[Item]
        val r = new FailingExceptionalCompletableFutureFunction(m)
        if !createIncomplete then f.completeExceptionally(ex)
        val g = m.exceptionallyCompose(f, r)
        if createIncomplete then f.completeExceptionally(ex)
        checkCompletedExceptionally(f, ex)
        checkCompletedWithWrappedException(g, r.ex)
        r.assertinvoked()
      }
    }
  }

  /** exceptionallyCompose result completes exceptionally if the result of the action does
   */
  @Test def testExceptionallyCompose_actionReturnsFailingFuture(): Unit = {
    for m <- ExecutionMode.values do {
      for order <- 0 until 6 do {
        val ex0 = new CFException
        val ex = new CFException
        val f = new CompletableFuture[Item]
        val g = new CompletableFuture[Item]
        var h: CompletableFuture[Item] = null
        // Test all permutations of orders
        order match {
          case 0 =>
            assertTrue(f.completeExceptionally(ex0))
            assertTrue(g.completeExceptionally(ex))
            h = m.exceptionallyCompose(f, (x: Throwable) => g)

          case 1 =>
            assertTrue(f.completeExceptionally(ex0))
            h = m.exceptionallyCompose(f, (x: Throwable) => g)
            assertTrue(g.completeExceptionally(ex))

          case 2 =>
            assertTrue(g.completeExceptionally(ex))
            assertTrue(f.completeExceptionally(ex0))
            h = m.exceptionallyCompose(f, (x: Throwable) => g)

          case 3 =>
            assertTrue(g.completeExceptionally(ex))
            h = m.exceptionallyCompose(f, (x: Throwable) => g)
            assertTrue(f.completeExceptionally(ex0))

          case 4 =>
            h = m.exceptionallyCompose(f, (x: Throwable) => g)
            assertTrue(f.completeExceptionally(ex0))
            assertTrue(g.completeExceptionally(ex))

          case 5 =>
            h = m.exceptionallyCompose(f, (x: Throwable) => g)
            assertTrue(f.completeExceptionally(ex0))
            assertTrue(g.completeExceptionally(ex))

          case _ =>
            throw new AssertionError
        }
        checkCompletedExceptionally(g, ex)
        checkCompletedWithWrappedException(h, ex)
        checkCompletedExceptionally(f, ex0)
      }
    }
  }

  /** Completion methods throw NullPointerException with null arguments
   */
  @Test def testNPE_Jdk17(): Unit = {
    val f = new CompletableFuture[Item]
    val g = new CompletableFuture[Item]
    val nullFuture = null.asInstanceOf[CompletableFuture[Item]]
    val exec = new ThreadExecutor
    assertEachThrows(
      classOf[NullPointerException],
      () => CompletableFuture.delayedExecutor(1L, SECONDS, null),
      () => CompletableFuture.delayedExecutor(1L, null, exec),
      () => CompletableFuture.delayedExecutor(1L, null),
      () => f.orTimeout(1L, null),
      () => f.completeOnTimeout(fortytwo, 1L, null),
      () => CompletableFuture.failedFuture(null),
      () => CompletableFuture.failedStage(null)
    )
    assertEquals(0, exec.count.get())
  }

  /** newIncompleteFuture returns an incomplete CompletableFuture
   */
  // jdk9
  @Test def testNewIncompleteFuture(): Unit = {
    for v1 <- Array[Item](one, null) do {
      val f = new CompletableFuture[Item]
      val g = f.newIncompleteFuture[Item]
      checkIncomplete(f)
      checkIncomplete(g)
      f.complete(v1)
      checkCompletedNormally(f, v1)
      checkIncomplete(g)
      g.complete(v1)
      checkCompletedNormally(g, v1)
      assertSame(g.getClass(), classOf[CompletableFuture[? <: AnyRef]])
    }
  }

  /** completedStage returns a completed CompletionStage
   */
  @Test def testCompletedStage(): Unit = {
    val x = new AtomicInteger(0)
    val r = new AtomicReference[Throwable]
    val f = CompletableFuture.completedStage(one: Item)
    f.whenComplete((v: Item, e: Throwable) => {
      if e != null then r.set(e)
      else x.set(v.value)
    })
    assertEquals(x.get(), 1)
    assertNull(r.get())
  }

  /** defaultExecutor by default returns the commonPool if it supports more than one thread.
   */
  @Test def testDefaultExecutor(): Unit = {
    val f = new CompletableFuture[Item]
    val e = f.defaultExecutor
    val c = ForkJoinPool.commonPool
    if ForkJoinPool.getCommonPoolParallelism() > 1 then assertSame(e, c)
    else assertNotSame(e, c)
  }

  /** failedFuture returns a CompletableFuture completed exceptionally with the given Exception
   */
  @Test def testFailedFuture(): Unit = {
    val ex = new CFException
    val f = CompletableFuture.failedFuture(ex)
    checkCompletedExceptionally(f, ex)
  }

  /** copy returns a CompletableFuture that is completed normally, with the same value, when source is.
   */
  @Test def testCopy_normalCompletion(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      for v1 <- Array[Item](one, null) do {
        val f = new CompletableFuture[Item]
        if !createIncomplete then assertTrue(f.complete(v1))
        val g = f.copy
        if createIncomplete then {
          checkIncomplete(f)
          checkIncomplete(g)
          assertTrue(f.complete(v1))
        }
        checkCompletedNormally(f, v1)
        checkCompletedNormally(g, v1)
      }
    }
  }

  /** copy returns a CompletableFuture that is completed exceptionally when source is.
   */
  @Test def testCopy_exceptionalCompletion(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      val ex = new CFException
      val f = new CompletableFuture[Item]
      if !createIncomplete then f.completeExceptionally(ex)
      val g = f.copy
      if createIncomplete then {
        checkIncomplete(f)
        checkIncomplete(g)
        f.completeExceptionally(ex)
      }
      checkCompletedExceptionally(f, ex)
      checkCompletedWithWrappedException(g, ex)
    }
  }

  /** Completion of a copy does not complete its source.
   */
  @Test def testCopy_oneWayPropagation(): Unit = {
    val f = new CompletableFuture[Item]
    assertTrue(f.copy.complete(one))
    assertTrue(f.copy.complete(null))
    assertTrue(f.copy.cancel(true))
    assertTrue(f.copy.cancel(false))
    assertTrue(f.copy.completeExceptionally(new CFException))
    checkIncomplete(f)
  }

  /** minimalCompletionStage returns a CompletableFuture that is completed normally, with the same value, when source
   *  is.
   */
  @Test def testMinimalCompletionStage(): Unit = {
    val f = new CompletableFuture[Item]
    val g = f.minimalCompletionStage
    val x = new AtomicInteger(0)
    val r = new AtomicReference[Throwable]
    checkIncomplete(f)
    g.whenComplete((v: Item, e: Throwable) => {
      if e != null then r.set(e)
      else x.set(v.value)

    })
    f.complete(one)
    checkCompletedNormally[Item](f, one)
    assertEquals(x.get(), 1)
    assertNull(r.get())
  }

  /** minimalCompletionStage returns a CompletableFuture that is completed exceptionally when source is.
   */
  @Test def testMinimalCompletionStage2(): Unit = {
    val f = new CompletableFuture[Item]
    val g = f.minimalCompletionStage
    val x = new AtomicInteger(0)
    val r = new AtomicReference[Throwable]
    g.whenComplete((v: Item, e: Throwable) => {
      if e != null then r.set(e)
      else x.set(v.value)

    })
    checkIncomplete(f)
    val ex = new CFException
    f.completeExceptionally(ex)
    checkCompletedExceptionally(f, ex)
    assertEquals(x.get(), 0)
    assertEquals(r.get().getCause(), ex)
  }

  /** failedStage returns a CompletionStage completed exceptionally with the given Exception
   */
  @Test def testFailedStage(): Unit = {
    val ex = new CFException
    val f = CompletableFuture.failedStage[Item](ex)
    val x = new AtomicInteger(0)
    val r = new AtomicReference[Throwable]
    f.whenComplete((v: Item, e: Throwable) => {
      if e != null then r.set(e)
      else x.set(v.value)

    })
    assertEquals(x.get(), 0)
    assertEquals(r.get(), ex)
  }

  /** completeAsync completes with value of given supplier
   */
  @Test def testCompleteAsync(): Unit = {
    for v1 <- Array[Item](one, null) do {
      val f = new CompletableFuture[Item]
      f.completeAsync(() => v1)
      f.join()
      checkCompletedNormally(f, v1)
    }
  }

  /** completeAsync completes exceptionally if given supplier throws
   */
  @Test def testCompleteAsync2(): Unit = {
    val f = new CompletableFuture[Item]
    val ex = new CFException
    f.completeAsync(() => {
      throw ex

    })
    try {
      f.join()
      shouldThrow()
    } catch {
      case success: CompletionException =>

    }
    checkCompletedWithWrappedException(f, ex)
  }

  /** completeAsync with given executor completes with value of given supplier
   */
  @Test def testCompleteAsync3(): Unit = {
    for v1 <- Array[Item](one, null) do {
      val f = new CompletableFuture[Item]
      val executor = new ThreadExecutor
      f.completeAsync(() => v1, executor)
      assertSame(v1, f.join())
      checkCompletedNormally(f, v1)
      assertEquals(1, executor.count.get())
    }
  }

  /** completeAsync with given executor completes exceptionally if given supplier throws
   */
  @Test def testCompleteAsync4(): Unit = {
    val f = new CompletableFuture[Item]
    val ex = new CFException
    val executor = new ThreadExecutor
    f.completeAsync(
      () => {
        throw ex

      },
      executor
    )
    try {
      f.join()
      shouldThrow()
    } catch {
      case success: CompletionException =>

    }
    checkCompletedWithWrappedException(f, ex)
    assertEquals(1, executor.count.get())
  }

  /** orTimeout completes with TimeoutException if not complete
   */
  @Test def testOrTimeout_timesOut(): Unit = {
    val timeoutMillis = JSR166Test.timeoutMillis()
    val f = new CompletableFuture[Item]
    val startTime = System.nanoTime
    assertSame(f, f.orTimeout(timeoutMillis, MILLISECONDS))
    checkCompletedWithTimeoutException(f)
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis)
  }

  /** orTimeout completes normally if completed before timeout
   */
  @Test def testOrTimeout_completed(): Unit = {
    for v1 <- Array[Item](one, null) do {
      val f = new CompletableFuture[Item]
      val g = new CompletableFuture[Item]
      val startTime = System.nanoTime
      f.complete(v1)
      assertSame(f, f.orTimeout(LONG_DELAY_MS, MILLISECONDS))
      assertSame(g, g.orTimeout(LONG_DELAY_MS, MILLISECONDS))
      g.complete(v1)
      checkCompletedNormally(f, v1)
      checkCompletedNormally(g, v1)
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS / 2)
    }
  }

  /** completeOnTimeout completes with given value if not complete
   */
  @Test def testCompleteOnTimeout_timesOut(): Unit = {
    testInParallel(() => testCompleteOnTimeout_timesOut(fortytwo), () => testCompleteOnTimeout_timesOut(null))
  }

  /** completeOnTimeout completes with given value if not complete
   */
  def testCompleteOnTimeout_timesOut(v: Item): Unit = {
    val timeoutMillis = JSR166Test.timeoutMillis()
    val f = new CompletableFuture[Item]
    val startTime = System.nanoTime
    assertSame(f, f.completeOnTimeout(v, timeoutMillis, MILLISECONDS))
    assertSame(v, f.join())
    assertTrue(millisElapsedSince(startTime) >= timeoutMillis)
    f.complete(ninetynine) // should have no effect

    checkCompletedNormally(f, v)
  }

  /** completeOnTimeout has no effect if completed within timeout
   */
  @Test def testCompleteOnTimeout_completed(): Unit = {
    for v1 <- Array[Item](one, null) do {
      val f = new CompletableFuture[Item]
      val g = new CompletableFuture[Item]
      val startTime = System.nanoTime
      f.complete(v1)
      assertEquals(f, f.completeOnTimeout(minusOne, LONG_DELAY_MS, MILLISECONDS))
      assertEquals(g, g.completeOnTimeout(minusOne, LONG_DELAY_MS, MILLISECONDS))
      g.complete(v1)
      checkCompletedNormally(f, v1)
      checkCompletedNormally(g, v1)
      assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS / 2)
    }
  }

  /** delayedExecutor returns an executor that delays submission
   */
  @Test def testDelayedExecutor(): Unit = {
    testInParallel(
      () => testDelayedExecutor(null, null),
      () => testDelayedExecutor(null, one),
      () => testDelayedExecutor(new ThreadExecutor, one),
      () => testDelayedExecutor(new ThreadExecutor, one)
    )
  }

  @throws[Exception]
  def testDelayedExecutor(executor: Executor, v: Item): Unit = {
    val timeoutMillis = JSR166Test.timeoutMillis()
    // Use an "unreasonably long" long timeout to catch lingering threads
    val longTimeoutMillis = 1000 * 60 * 60 * 24
    var delayer: Executor = null
    var longDelayer: Executor = null
    if executor == null then {
      delayer = CompletableFuture.delayedExecutor(timeoutMillis, MILLISECONDS)
      longDelayer = CompletableFuture.delayedExecutor(longTimeoutMillis, MILLISECONDS)
    } else {
      delayer = CompletableFuture.delayedExecutor(timeoutMillis, MILLISECONDS, executor)
      longDelayer = CompletableFuture.delayedExecutor(longTimeoutMillis, MILLISECONDS, executor)
    }
    val startTime = System.nanoTime
    val f = CompletableFuture.supplyAsync(() => v, delayer)
    val g = CompletableFuture.supplyAsync(() => v, longDelayer)
    assertNull(g.getNow(null))
    assertSame(v, f.get(LONG_DELAY_MS, MILLISECONDS))
    val millisElapsed = millisElapsedSince(startTime)
    assertTrue(millisElapsed >= timeoutMillis)
    assertTrue(millisElapsed < LONG_DELAY_MS / 2)
    checkCompletedNormally(f, v)
    checkIncomplete(g)
    assertTrue(g.cancel(true))
  }

  /** minimalStage.toCompletableFuture() returns a CompletableFuture that is completed normally, with the same value,
   *  when source is.
   */
  @Test def testMinimalCompletionStage_toCompletableFuture_normalCompletion(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      for v1 <- Array[Item](one, null) do {
        val f = new CompletableFuture[Item]
        val minimal = f.minimalCompletionStage
        if !createIncomplete then assertTrue(f.complete(v1))
        val g = minimal.toCompletableFuture
        if createIncomplete then {
          checkIncomplete(f)
          checkIncomplete(g)
          assertTrue(f.complete(v1))
        }
        checkCompletedNormally(f, v1)
        checkCompletedNormally(g, v1)
      }
    }
  }

  /** minimalStage.toCompletableFuture() returns a CompletableFuture that is completed exceptionally when source is.
   */
  @Test def testMinimalCompletionStage_toCompletableFuture_exceptionalCompletion(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      val ex = new CFException
      val f = new CompletableFuture[Item]
      val minimal = f.minimalCompletionStage
      if !createIncomplete then f.completeExceptionally(ex)
      val g = minimal.toCompletableFuture
      if createIncomplete then {
        checkIncomplete(f)
        checkIncomplete(g)
        f.completeExceptionally(ex)
      }
      checkCompletedExceptionally(f, ex)
      checkCompletedWithWrappedException(g, ex)
    }
  }

  /** minimalStage.toCompletableFuture() gives mutable CompletableFuture
   */
  @Test def testMinimalCompletionStage_toCompletableFuture_mutable(): Unit = {
    for v1 <- Array[Item](one, null) do {
      val f = new CompletableFuture[Item]
      val minimal = f.minimalCompletionStage
      val g = minimal.toCompletableFuture
      assertTrue(g.complete(v1))
      checkCompletedNormally(g, v1)
      checkIncomplete(f)
      checkIncomplete(minimal.toCompletableFuture)
    }
  }

  /** minimalStage.toCompletableFuture().join() awaits completion
   */
  @throws[Exception]
  @Test def testMinimalCompletionStage_toCompletableFutureJoin(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      for v1 <- Array[Item](one, null) do {
        val f = new CompletableFuture[Item]
        if !createIncomplete then assertTrue(f.complete(v1))
        val minimal = f.minimalCompletionStage
        if createIncomplete then assertTrue(f.complete(v1))
        assertEquals(v1, minimal.toCompletableFuture.join())
        assertEquals(v1, minimal.toCompletableFuture.get())
        checkCompletedNormally(minimal.toCompletableFuture, v1)
      }
    }
  }

  /** Completion of a toCompletableFuture copy of a minimal stage does not complete its source.
   */
  @Test def testMinimalCompletionStage_toCompletableFuture_oneWayPropagation(): Unit = {
    val f = new CompletableFuture[Item]
    val g = f.minimalCompletionStage
    assertTrue(g.toCompletableFuture.complete(one))
    assertTrue(g.toCompletableFuture.complete(null))
    assertTrue(g.toCompletableFuture.cancel(true))
    assertTrue(g.toCompletableFuture.cancel(false))
    assertTrue(g.toCompletableFuture.completeExceptionally(new CFException))
    checkIncomplete[Item](g.toCompletableFuture)
    f.complete(one)
    checkCompletedNormally[Item](g.toCompletableFuture, one)
  }

  /** joining a minimal stage "by hand" works
   */
  @Test def testMinimalCompletionStage_join_by_hand(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      for v1 <- Array[Item](one, null) do {
        val f = new CompletableFuture[Item]
        val minimal = f.minimalCompletionStage
        val g = new CompletableFuture[Item]
        if !createIncomplete then assertTrue(f.complete(v1))
        minimal.thenAccept((x: Item) => g.complete(x))
        if createIncomplete then assertTrue(f.complete(v1))
        g.join()
        checkCompletedNormally(g, v1)
        checkCompletedNormally(f, v1)
        assertEquals(v1, join(minimal))
      }
    }
  }

  /** CompletableFuture is an additive monad - sort of.
   *  https://en.wikipedia.org/wiki/Monad_(functional_programming)#Additive_monads
   */
  @throws[Throwable]
  @Test def testAdditiveMonad(): Unit = {
    import java.lang.Long as jlLong
    val unit: Function[jlLong, CompletableFuture[jlLong]] = Monad.unit
    val zero: CompletableFuture[jlLong] = Monad.zero()
    // Some mutually non-commutative functions
    val triple: Function[jlLong, CompletableFuture[jlLong]] = (x: jlLong) => Monad.unit(3 * x)
    val inc: Function[jlLong, CompletableFuture[jlLong]] = (x: jlLong) => Monad.unit(x + 1)
    // unit is a right identity: m >>= unit === m
    Monad.assertFutureEquals(inc.apply(5L: jlLong).thenCompose(unit), inc.apply(5L: jlLong))
    // unit is a left identity: (unit x) >>= f === f x
    Monad.assertFutureEquals(unit.apply(5L: jlLong).thenCompose(inc), inc.apply(5L: jlLong))
    // associativity: (m >>= f) >>= g === m >>= ( \x -> (f x >>= g) )
    Monad.assertFutureEquals(
      unit.apply(5L: jlLong).thenCompose(inc).thenCompose(triple),
      unit.apply(5L: jlLong).thenCompose((x: jlLong) => inc.apply(x).thenCompose(triple))
    )
    // The case for CompletableFuture as an additive monad is weaker...
    // zero is a monadic zero
    Monad.assertZero(zero)
    // left zero: zero >>= f === zero
    Monad.assertZero(zero.thenCompose(inc))
    // right zero: f >>= (\x -> zero) === zero
    Monad.assertZero(inc.apply(5L).thenCompose((x: jlLong) => zero))
    // f plus zero === f
    Monad.assertFutureEquals(
      Monad.unit(5L: jlLong),
      Monad.plus(Monad.unit(5L: jlLong), zero)
    )
    // zero plus f === f
    Monad.assertFutureEquals(
      Monad.unit(5L: jlLong),
      Monad.plus(zero, Monad.unit(5L: jlLong))
    )
    // zero plus zero === zero
    Monad.assertZero(Monad.plus(zero, zero))
    val f = Monad.plus(Monad.unit(5L: jlLong), Monad.unit(8L: jlLong))
    // non-determinism
    assertTrue((f.get() eq (5L: jlLong)) || (f.get() eq (8L: jlLong)))

    val godot = new CompletableFuture[jlLong]
    // f plus godot === f (doesn't wait for godot)
    Monad.assertFutureEquals(
      Monad.unit(5L: jlLong),
      Monad.plus(Monad.unit(5L: jlLong), godot)
    )
    // godot plus f === f (doesn't wait for godot)
    Monad.assertFutureEquals(
      Monad.unit(5L: jlLong),
      Monad.plus(godot, Monad.unit(5L: jlLong))
    )
  }

  /** Test long recursive chains of CompletableFutures with cascading completions */
  @SuppressWarnings(Array("FutureReturnValueIgnored"))
  @throws[Throwable]
  @Test def testRecursiveChains(): Unit = {
    for m <- ExecutionMode.values do {
      for addDeadEnds <- Array[Boolean](true, false) do {
        val `val` = 42
        val n =
          if expensiveTests then 1000
          else 2
        val head = new CompletableFuture[Item]
        var tail = head
        for i <- 0 until n do {
          if addDeadEnds then m.thenApply(tail, (v: Item) => new Item(v.value + 1))
          tail = m.thenApply(tail, (v: Item) => new Item(v.value + 1))
          if addDeadEnds then m.applyToEither(tail, tail, (v: Item) => new Item(v.value + 1))
          tail = m.applyToEither(tail, tail, (v: Item) => new Item(v.value + 1))
          if addDeadEnds then m.thenCombine(tail, tail, (v: Item, w: Item) => new Item(v.value + 1))
          tail = m.thenCombine(tail, tail, (v: Item, w: Item) => new Item(v.value + 1))
        }
        head.complete(itemFor(`val`))
        mustEqual(`val` + 3 * n, tail.join())
      }
    }
  }

  /** A single CompletableFuture with many dependents. A demo of scalability - runtime is O(n).
   */
  @SuppressWarnings(Array("FutureReturnValueIgnored"))
  @throws[Throwable]
  @Test def testManyDependents(): Unit = {
    val n =
      if expensiveTests then 1000000
      else 10
    val head = new CompletableFuture[Void]
    val complete = CompletableFuture.completedFuture(null.asInstanceOf[Void])
    val count = new AtomicInteger(0)
    for i <- 0 until n do {
      head.thenRun(() => count.getAndIncrement())
      head.thenAccept((x: Void) => count.getAndIncrement())
      head.thenApply((x: Void) => count.getAndIncrement())
      head.runAfterBoth(complete, () => count.getAndIncrement())
      head.thenAcceptBoth(complete, (x: Void, y: Void) => count.getAndIncrement())
      head.thenCombine(complete, (x: Void, y: Void) => count.getAndIncrement())
      complete.runAfterBoth(head, () => count.getAndIncrement())
      complete.thenAcceptBoth(head, (x: Void, y: Void) => count.getAndIncrement())
      complete.thenCombine(head, (x: Void, y: Void) => count.getAndIncrement())
      head.runAfterEither(new CompletableFuture[Void], () => count.getAndIncrement())
      head.acceptEither(new CompletableFuture[Void], (x: Void) => count.getAndIncrement())
      head.applyToEither(new CompletableFuture[Void], (x: Void) => count.getAndIncrement())
      new CompletableFuture[Void]().runAfterEither(head, () => count.getAndIncrement())
      new CompletableFuture[Void]().acceptEither(head, (x: Void) => count.getAndIncrement())
      new CompletableFuture[Void]().applyToEither(head, (x: Void) => count.getAndIncrement())
    }
    head.complete(null)
    assertEquals(5 * 3 * n, count.get())
  }

  /** ant -Dvmoptions=-Xmx8m -Djsr166.expensiveTests=true -Djsr166.tckTestClass=CompletableFutureTest tck */
  @SuppressWarnings(Array("FutureReturnValueIgnored"))
  @throws[Throwable]
  @Test def testCoCompletionGarbageRetention(): Unit = {
    val n =
      if expensiveTests then 1000000
      else 10
    val incomplete = new CompletableFuture[Item]
    var f: CompletableFuture[Item] = null
    for i <- 0 until n do {
      f = new CompletableFuture[Item]
      f.runAfterEither(incomplete, () => {})
      f.complete(null)
      f = new CompletableFuture[Item]
      f.acceptEither(incomplete, (x: Item) => {})
      f.complete(null)
      f = new CompletableFuture[Item]
      f.applyToEither(incomplete, (x: Item) => x)
      f.complete(null)
      f = new CompletableFuture[Item]
      CompletableFuture.anyOf(f, incomplete)
      f.complete(null)
    }
    for i <- 0 until n do {
      f = new CompletableFuture[Item]
      incomplete.runAfterEither(f, () => {})
      f.complete(null)
      f = new CompletableFuture[Item]
      incomplete.acceptEither(f, (x: Item) => {})
      f.complete(null)
      f = new CompletableFuture[Item]
      incomplete.applyToEither(f, (x: Item) => x)
      f.complete(null)
      f = new CompletableFuture[Item]
      CompletableFuture.anyOf(incomplete, f)
      f.complete(null)
    }
  }

  /** Reproduction recipe for: 8160402: Garbage retention with CompletableFuture.anyOf cvs update -D '2016-05-01'
   *  ./src/main/java/util/concurrent/CompletableFuture.java && ant -Dvmoptions=-Xmx8m -Djsr166.expensiveTests=true
   *  -Djsr166.tckTestClass=CompletableFutureTest -Djsr166.methodFilter=testAnyOfGarbageRetention tck; cvs update -A
   */
  @throws[Throwable]
  @Test def testAnyOfGarbageRetention(): Unit = {
    for v <- Array[Item](one, null) do {
      val n =
        if expensiveTests then 100000
        else 10
      @SuppressWarnings(Array("unchecked")) val fs =
        new Array[CompletableFuture[? <: AnyRef]](100).asInstanceOf[Array[CompletableFuture[Item]]]
      for i <- 0 until fs.length do {
        fs(i) = new CompletableFuture[Item]
      }
      fs(fs.length - 1).complete(v)
      for i <- 0 until n do {
        checkCompletedNormally(CompletableFuture.anyOf(fs*), v)
      }
    }
  }

  /** Checks for garbage retention with allOf.
   *
   *  As of 2016-07, fails with OOME: ant -Dvmoptions=-Xmx8m -Djsr166.expensiveTests=true
   *  -Djsr166.tckTestClass=CompletableFutureTest -Djsr166.methodFilter=testCancelledAllOfGarbageRetention tck
   */
  @throws[Throwable]
  @Test def testCancelledAllOfGarbageRetention(): Unit = {
    val n =
      if expensiveTests then 100000
      else 10
    @SuppressWarnings(Array("unchecked")) val fs =
      new Array[CompletableFuture[? <: AnyRef]](100).asInstanceOf[Array[CompletableFuture[Item]]]
    for i <- 0 until fs.length do {
      fs(i) = new CompletableFuture[Item]
    }
    for i <- 0 until n do {
      assertTrue(CompletableFuture.allOf(fs*).cancel(false))
    }
  }

  /** Checks for garbage retention when a dependent future is cancelled and garbage-collected. 8161600: Garbage
   *  retention when source CompletableFutures are never completed
   *
   *  As of 2016-07, fails with OOME: ant -Dvmoptions=-Xmx8m -Djsr166.expensiveTests=true
   *  -Djsr166.tckTestClass=CompletableFutureTest -Djsr166.methodFilter=testCancelledGarbageRetention tck
   */
  @throws[Throwable]
  @Test def testCancelledGarbageRetention(): Unit = {
    val n =
      if expensiveTests then 100000
      else 10
    val neverCompleted = new CompletableFuture[Item]
    for i <- 0 until n do {
      assertTrue(neverCompleted.thenRun(() => {}).cancel(true))
    }
  }

  /** Checks for garbage retention when MinimalStage.toCompletableFuture() is invoked() many times. 8161600: Garbage
   *  retention when source CompletableFutures are never completed
   *
   *  As of 2016-07, fails with OOME: ant -Dvmoptions=-Xmx8m -Djsr166.expensiveTests=true
   *  -Djsr166.tckTestClass=CompletableFutureTest -Djsr166.methodFilter=testToCompletableFutureGarbageRetention tck
   */
  @throws[Throwable]
  @Test def testToCompletableFutureGarbageRetention(): Unit = {
    val n =
      if expensiveTests then 900000
      else 10
    val neverCompleted = new CompletableFuture[Item]
    val minimal = neverCompleted.minimalCompletionStage
    for i <- 0 until n do {
      assertTrue(minimal.toCompletableFuture.cancel(true))
    }
  }

  /** default-implemented exceptionallyAsync action is not invoked() when source completes normally, and source result
   *  is propagated
   */
  @Test def testDefaultExceptionallyAsync_normalCompletion(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      for v1 <- Array[Item](one, null) do {
        val ran = new AtomicInteger(0)
        val f = new CompletableFuture[Item]
        val d = new DelegatedCompletionStage[Item](f)
        if !createIncomplete then assertTrue(f.complete(v1))
        val g = d.exceptionallyAsync((t: Throwable) => {
          ran.getAndIncrement()
          throw new AssertionError("should not be called")

        })
        if createIncomplete then assertTrue(f.complete(v1))
        checkCompletedNormally(g.toCompletableFuture, v1)
        checkCompletedNormally(f, v1)
        assertEquals(0, ran.get())
      }
    }
  }

  /** default-implemented exceptionallyAsync action completes with function value on source exception
   */
  @Test def testDefaultExceptionallyAsync_exceptionalCompletion(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      for v1 <- Array[Item](one, null) do {
        val ran = new AtomicInteger(0)
        val ex = new CFException
        val f = new CompletableFuture[Item]
        val d = new DelegatedCompletionStage[Item](f)
        if !createIncomplete then f.completeExceptionally(ex)
        val g = d.exceptionallyAsync((t: Throwable) => {
          assertSame(t, ex)
          ran.getAndIncrement()
          v1

        })
        if createIncomplete then f.completeExceptionally(ex)
        checkCompletedNormally(g.toCompletableFuture, v1)
        checkCompletedExceptionally(f, ex)
        assertEquals(1, ran.get())
      }
    }
  }

  /** Under default implementation, if an "exceptionally action" throws an exception, it completes exceptionally with
   *  that exception
   */
  @Test def testDefaultExceptionallyAsync_exceptionalCompletionActionFailed(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      val ran = new AtomicInteger(0)
      val ex1 = new CFException
      val ex2 = new CFException
      val f = new CompletableFuture[Item]
      val d = new DelegatedCompletionStage[Item](f)
      if !createIncomplete then f.completeExceptionally(ex1)
      val g = d.exceptionallyAsync((t: Throwable) => {
        assertSame(t, ex1)
        ran.getAndIncrement()
        throw ex2

      })
      if createIncomplete then f.completeExceptionally(ex1)
      checkCompletedWithWrappedException(g.toCompletableFuture, ex2)
      checkCompletedExceptionally(f, ex1)
      checkCompletedExceptionally(d.toCompletableFuture, ex1)
      assertEquals(1, ran.get())
    }
  }

  /** default-implemented exceptionallyCompose result completes normally after normal completion of source
   */
  @Test def testDefaultExceptionallyCompose_normalCompletion(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      for v1 <- Array[Item](one, null) do {
        val f = new CompletableFuture[Item]
        val r = new ExceptionalCompletableFutureFunction(ExecutionMode.SYNC)
        val d = new DelegatedCompletionStage[Item](f)
        if !createIncomplete then assertTrue(f.complete(v1))
        val g = d.exceptionallyCompose(r)
        if createIncomplete then assertTrue(f.complete(v1))
        checkCompletedNormally(f, v1)
        checkCompletedNormally(g.toCompletableFuture, v1)
        r.assertNotinvoked()
      }
    }
  }

  /** default-implemented exceptionallyCompose result completes normally after exceptional completion of source
   */
  @Test def testDefaultExceptionallyCompose_exceptionalCompletion(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      val ex = new CFException
      val r = new ExceptionalCompletableFutureFunction(ExecutionMode.SYNC)
      val f = new CompletableFuture[Item]
      val d = new DelegatedCompletionStage[Item](f)
      if !createIncomplete then f.completeExceptionally(ex)
      val g = d.exceptionallyCompose(r)
      if createIncomplete then f.completeExceptionally(ex)
      checkCompletedExceptionally[Item](f, ex)
      checkCompletedNormally[Item](g.toCompletableFuture, r.value)
      r.assertinvoked()
    }
  }

  /** default-implemented exceptionallyCompose completes exceptionally on exception if action does
   */
  @Test def testDefaultExceptionallyCompose_actionFailed(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      val ex = new CFException
      val f = new CompletableFuture[Item]
      val r =
        new FailingExceptionalCompletableFutureFunction(ExecutionMode.SYNC)
      val d = new DelegatedCompletionStage[Item](f)
      if !createIncomplete then f.completeExceptionally(ex)
      val g = d.exceptionallyCompose(r)
      if createIncomplete then f.completeExceptionally(ex)
      checkCompletedExceptionally(f, ex)
      checkCompletedWithWrappedException(g.toCompletableFuture, r.ex)
      r.assertinvoked()
    }
  }

  /** default-implemented exceptionallyComposeAsync result completes normally after normal completion of source
   */
  @Test def testDefaultExceptionallyComposeAsync_normalCompletion(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      for v1 <- Array[Item](one, null) do {
        val f = new CompletableFuture[Item]
        val r =
          new ExceptionalCompletableFutureFunction(ExecutionMode.ASYNC)
        val d = new DelegatedCompletionStage[Item](f)
        if !createIncomplete then assertTrue(f.complete(v1))
        val g = d.exceptionallyComposeAsync(r)
        if createIncomplete then assertTrue(f.complete(v1))
        checkCompletedNormally(f, v1)
        checkCompletedNormally(g.toCompletableFuture, v1)
        r.assertNotinvoked()
      }
    }
  }

  /** default-implemented exceptionallyComposeAsync result completes normally after exceptional completion of source
   */
  @Test def testDefaultExceptionallyComposeAsync_exceptionalCompletion(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      val ex = new CFException
      val r = new ExceptionalCompletableFutureFunction(ExecutionMode.ASYNC)
      val f = new CompletableFuture[Item]
      val d = new DelegatedCompletionStage[Item](f)
      if !createIncomplete then f.completeExceptionally(ex)
      val g = d.exceptionallyComposeAsync(r)
      if createIncomplete then f.completeExceptionally(ex)
      checkCompletedExceptionally(f, ex)
      checkCompletedNormally[Item](g.toCompletableFuture, r.value)
      r.assertinvoked()
    }
  }

  /** default-implemented exceptionallyComposeAsync completes exceptionally on exception if action does
   */
  @Test def testDefaultExceptionallyComposeAsync_actionFailed(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      val ex = new CFException
      val f = new CompletableFuture[Item]
      val r =
        new FailingExceptionalCompletableFutureFunction(ExecutionMode.ASYNC)
      val d = new DelegatedCompletionStage[Item](f)
      if !createIncomplete then f.completeExceptionally(ex)
      val g = d.exceptionallyComposeAsync(r)
      if createIncomplete then f.completeExceptionally(ex)
      checkCompletedExceptionally(f, ex)
      checkCompletedWithWrappedException(g.toCompletableFuture, r.ex)
      r.assertinvoked()
    }
  }

  /** default-implemented exceptionallyComposeAsync result completes normally after normal completion of source
   */
  @Test def testDefaultExceptionallyComposeAsyncExecutor_normalCompletion(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      for v1 <- Array[Item](one, null) do {
        val f = new CompletableFuture[Item]
        val r =
          new ExceptionalCompletableFutureFunction(ExecutionMode.EXECUTOR)
        val d = new DelegatedCompletionStage[Item](f)
        if !createIncomplete then assertTrue(f.complete(v1))
        val g = d.exceptionallyComposeAsync(r, new ThreadExecutor)
        if createIncomplete then assertTrue(f.complete(v1))
        checkCompletedNormally(f, v1)
        checkCompletedNormally(g.toCompletableFuture, v1)
        r.assertNotinvoked()
      }
    }
  }

  /** default-implemented exceptionallyComposeAsync result completes normally after exceptional completion of source
   */
  @Test def testDefaultExceptionallyComposeAsyncExecutor_exceptionalCompletion(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      val ex = new CFException
      val r =
        new ExceptionalCompletableFutureFunction(ExecutionMode.EXECUTOR)
      val f = new CompletableFuture[Item]
      val d = new DelegatedCompletionStage[Item](f)
      if !createIncomplete then f.completeExceptionally(ex)
      val g = d.exceptionallyComposeAsync(r, new ThreadExecutor)
      if createIncomplete then f.completeExceptionally(ex)
      checkCompletedExceptionally(f, ex)
      checkCompletedNormally[Item](g.toCompletableFuture, r.value)
      r.assertinvoked()
    }
  }

  /** default-implemented exceptionallyComposeAsync completes exceptionally on exception if action does
   */
  @Test def testDefaultExceptionallyComposeAsyncExecutor_actionFailed(): Unit = {
    for createIncomplete <- Array[Boolean](true, false) do {
      val ex = new CFException
      val f = new CompletableFuture[Item]
      val r = new FailingExceptionalCompletableFutureFunction(
        ExecutionMode.EXECUTOR
      )
      val d = new DelegatedCompletionStage[Item](f)
      if !createIncomplete then f.completeExceptionally(ex)
      val g = d.exceptionallyComposeAsync(r, new ThreadExecutor)
      if createIncomplete then f.completeExceptionally(ex)
      checkCompletedExceptionally(f, ex)
      checkCompletedWithWrappedException(g.toCompletableFuture, r.ex)
      r.assertinvoked()
    }
  }
}
