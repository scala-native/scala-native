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

import org.junit._
import org.junit.Assert._
import org.scalanative.testsuite.utils.Platform

object CompletableFutureTest {
  import JSR166Test._
  class CFException extends RuntimeException {}

  abstract class CheckedAction(val m: ExecutionMode) {
    var invocationCount = 0

    def invoked(): Unit = {
      m.checkExecutionMode()
      assertEquals(
        0, {
          invocationCount += 1; invocationCount - 1
        }
      )
    }

    def assertNotinvoked(): Unit = {
      assertEquals(0, invocationCount)
    }

    def assertinvoked(): Unit = {
      assertEquals(1, invocationCount)
    }
  }

  abstract class CheckedItemAction(m: ExecutionMode) extends CheckedAction(m) {
    var value: Item = null.asInstanceOf[Item]

    def assertValue(expected: Item): Unit = {
      assertinvoked()
      assertEquals(expected, value)
    }
  }

  class ItemSupplier(m: ExecutionMode, val value: Item) extends CheckedAction(m) with Supplier[Item] {
    override def get(): Item = {
      invoked()
      value
    }
  }

  // A function that handles and produces null values as well.
  def inc(x: Item): Item =
    if (x == null) null
    else new Item(x.value + 1)

  class NoopConsumer(m: ExecutionMode) extends CheckedItemAction(m) with Consumer[Item] {
    override def accept(x: Item): Unit = {
      invoked()
      value = x
    }
  }

  class IncFunction(m: ExecutionMode) extends CheckedItemAction(m) with Function[Item, Item] {
    override def apply(x: Item): Item = {
      invoked()
      value = inc(x)
      value
    }
  }

  // Choose non-commutative actions for better coverage
  // A non-commutative function that handles and produces null values as well.
  def subtract(x: Item, y: Item): Item = if (x == null && y == null) null
  else
    new Item(
      (if (x == null) 42
       else x.value) - (if (y == null) 99
                        else y.value)
    )

  class SubtractAction(m: ExecutionMode) extends CheckedItemAction(m) with BiConsumer[Item, Item] {
    override def accept(x: Item, y: Item): Unit = {
      invoked()
      value = subtract(x, y)
    }
  }

  class SubtractFunction(m: ExecutionMode) extends CheckedItemAction(m) with BiFunction[Item, Item, Item] {
    override def apply(x: Item, y: Item): Item = {
      invoked()
      value = subtract(x, y)
      value
    }
  }

  class Noop(m: ExecutionMode) extends CheckedAction(m) with Runnable {
    override def run(): Unit = {
      invoked()
    }
  }

  class FailingSupplier(m: ExecutionMode) extends CheckedAction(m) with Supplier[Item] {
    final val ex: CFException = new CFException

    override def get(): Item = {
      invoked()
      throw ex
    }
  }

  class FailingConsumer(m: ExecutionMode) extends CheckedItemAction(m) with Consumer[Item] {
    final val ex: CFException = new CFException

    override def accept(x: Item): Unit = {
      invoked()
      value = x
      throw ex
    }
  }

  class FailingBiConsumer(m: ExecutionMode) extends CheckedItemAction(m) with BiConsumer[Item, Item] {
    final val ex: CFException = new CFException

    override def accept(x: Item, y: Item): Unit = {
      invoked()
      value = subtract(x, y)
      throw ex
    }
  }

  class FailingFunction(m: ExecutionMode) extends CheckedItemAction(m) with Function[Item, Item] {
    final val ex: CFException = new CFException

    override def apply(x: Item): Item = {
      invoked()
      value = x
      throw ex
    }
  }

  class FailingBiFunction(m: ExecutionMode) extends CheckedItemAction(m) with BiFunction[Item, Item, Item] {
    final val ex: CFException = new CFException

    override def apply(x: Item, y: Item): Item = {
      invoked()
      value = subtract(x, y)
      throw ex
    }
  }

  class FailingRunnable(m: ExecutionMode) extends CheckedAction(m) with Runnable {
    final val ex: CFException = new CFException

    override def run(): Unit = {
      invoked()
      throw ex
    }
  }

  class CompletableFutureInc(m: ExecutionMode)
      extends CheckedItemAction(m)
      with Function[Item, CompletableFuture[Item]] {
    override def apply(x: Item): CompletableFuture[Item] = {
      invoked()
      value = x
      CompletableFuture.completedFuture(inc(x))
    }
  }

  class FailingExceptionalCompletableFutureFunction(m: ExecutionMode)
      extends CheckedAction(m)
      with Function[Throwable, CompletableFuture[Item]] {
    final val ex: CFException = new CFException

    override def apply(x: Throwable): CompletableFuture[Item] = {
      invoked()
      throw ex
    }
  }

  class ExceptionalCompletableFutureFunction(m: ExecutionMode)
      extends CheckedAction(m)
      with Function[Throwable, CompletionStage[Item]] {
    final val value = three

    override def apply(x: Throwable): CompletionStage[Item] = {
      invoked()
      CompletableFuture.completedFuture(value)
    }
  }

  class FailingCompletableFutureFunction(m: ExecutionMode)
      extends CheckedItemAction(m)
      with Function[Item, CompletableFuture[Item]] {
    final val ex: CFException = new CFException

    override def apply(x: Item): CompletableFuture[Item] = {
      invoked()
      value = x
      throw ex
    }
  }

  class CountingRejectingExecutor extends Executor {
    final val ex = new RejectedExecutionException
    final val count = new AtomicInteger(0)

    override def execute(r: Runnable): Unit = {
      count.getAndIncrement()
      throw ex
    }
  }

  // Used for explicit executor tests
  object ThreadExecutor {
    val tg = new ThreadGroup("ThreadExecutor")

    def startedCurrentThread(): Boolean = Thread.currentThread.getThreadGroup() eq tg
  }

  final class ThreadExecutor extends Executor {
    final val count = new AtomicInteger(0)

    override def execute(r: Runnable): Unit = {
      count.getAndIncrement()
      new Thread(ThreadExecutor.tg, r).start()
    }
  }

  val defaultExecutorIsCommonPool: Boolean = ForkJoinPool.getCommonPoolParallelism() > 1

  /** Permits the testing of parallel code for the 3 different execution modes without copy/pasting all the test
   *  methods.
   */
  trait ExecutionMode {
    def checkExecutionMode(): Unit
    def runAsync(a: Runnable): CompletableFuture[Void]
    def supplyAsync[U <: AnyRef](a: Supplier[U]): CompletableFuture[U]
    def thenRun[T <: AnyRef](f: CompletableFuture[T], a: Runnable): CompletableFuture[Void]
    def thenAccept[T <: AnyRef](f: CompletableFuture[T], a: Consumer[_ >: T]): CompletableFuture[Void]
    def thenApply[T <: AnyRef, U <: AnyRef](f: CompletableFuture[T], a: Function[_ >: T, U]): CompletableFuture[U]
    def thenCompose[T <: AnyRef, U <: AnyRef](
        f: CompletableFuture[T],
        a: Function[_ >: T, _ <: CompletionStage[U]]
    ): CompletableFuture[U]
    def handle[T <: AnyRef, U <: AnyRef](
        f: CompletableFuture[T],
        a: BiFunction[_ >: T, Throwable, _ <: U]
    ): CompletableFuture[U]
    def whenComplete[T <: AnyRef](f: CompletableFuture[T], a: BiConsumer[_ >: T, _ >: Throwable]): CompletableFuture[T]
    def runAfterBoth[T <: AnyRef, U <: AnyRef](
        f: CompletableFuture[T],
        g: CompletableFuture[U],
        a: Runnable
    ): CompletableFuture[Void]
    def thenAcceptBoth[T <: AnyRef, U <: AnyRef](
        f: CompletableFuture[T],
        g: CompletionStage[_ <: U],
        a: BiConsumer[_ >: T, _ >: U]
    ): CompletableFuture[Void]
    def thenCombine[T <: AnyRef, U <: AnyRef, V <: AnyRef](
        f: CompletableFuture[T],
        g: CompletionStage[_ <: U],
        a: BiFunction[_ >: T, _ >: U, _ <: V]
    ): CompletableFuture[V]
    def runAfterEither[T <: AnyRef](
        f: CompletableFuture[T],
        g: CompletionStage[_],
        a: Runnable
    ): CompletableFuture[Void]
    def acceptEither[T <: AnyRef](
        f: CompletableFuture[T],
        g: CompletionStage[_ <: T],
        a: Consumer[_ >: T]
    ): CompletableFuture[Void]
    def applyToEither[T <: AnyRef, U <: AnyRef](
        f: CompletableFuture[T],
        g: CompletionStage[_ <: T],
        a: Function[_ >: T, U]
    ): CompletableFuture[U]
  }
  object ExecutionMode {
    val values = Array(SYNC, ASYNC, EXECUTOR)
    case object SYNC extends SYNC
    abstract class SYNC extends ExecutionMode {
      def checkExecutionMode(): Unit = {
        assertFalse(
          s"Invalid thread group in ${Thread.currentThread()}, expected group=${ThreadExecutor.tg}",
          ThreadExecutor.startedCurrentThread()
        )
        // Invalid on Scala Native, requires https://github.com/scala-native/scala-native/pull/4116
        // assertNull(s"Unexpected execution in pool: ${ForkJoinTask.getPool()}", ForkJoinTask.getPool())
      }
      def runAsync(a: Runnable): CompletableFuture[Void] = throw new UnsupportedOperationException()
      def supplyAsync[U <: AnyRef](a: Supplier[U]): CompletableFuture[U] = throw new UnsupportedOperationException()
      def thenRun[T <: AnyRef](f: CompletableFuture[T], a: Runnable): CompletableFuture[Void] = f.thenRun(a)
      def thenAccept[T <: AnyRef](f: CompletableFuture[T], a: Consumer[_ >: T]): CompletableFuture[Void] =
        f.thenAccept(a)
      def thenApply[T <: AnyRef, U <: AnyRef](f: CompletableFuture[T], a: Function[_ >: T, U]): CompletableFuture[U] =
        f.thenApply(a)
      def thenCompose[T <: AnyRef, U <: AnyRef](
          f: CompletableFuture[T],
          a: Function[_ >: T, _ <: CompletionStage[U]]
      ): CompletableFuture[U] = f.thenCompose(a)
      def handle[T <: AnyRef, U <: AnyRef](
          f: CompletableFuture[T],
          a: BiFunction[_ >: T, Throwable, _ <: U]
      ): CompletableFuture[U] =
        f.handle(a)
      def whenComplete[T <: AnyRef](
          f: CompletableFuture[T],
          a: BiConsumer[_ >: T, _ >: Throwable]
      ): CompletableFuture[T] =
        f.whenComplete(a)
      def runAfterBoth[T <: AnyRef, U <: AnyRef](
          f: CompletableFuture[T],
          g: CompletableFuture[U],
          a: Runnable
      ): CompletableFuture[Void] =
        f.runAfterBoth(g, a)
      def thenAcceptBoth[T <: AnyRef, U <: AnyRef](
          f: CompletableFuture[T],
          g: CompletionStage[_ <: U],
          a: BiConsumer[_ >: T, _ >: U]
      ): CompletableFuture[Void] = f.thenAcceptBoth(g, a)
      def thenCombine[T <: AnyRef, U <: AnyRef, V <: AnyRef](
          f: CompletableFuture[T],
          g: CompletionStage[_ <: U],
          a: BiFunction[_ >: T, _ >: U, _ <: V]
      ): CompletableFuture[V] = f.thenCombine(g, a)
      def runAfterEither[T <: AnyRef](
          f: CompletableFuture[T],
          g: CompletionStage[_],
          a: Runnable
      ): CompletableFuture[Void] =
        f.runAfterEither(g, a)
      def acceptEither[T <: AnyRef](
          f: CompletableFuture[T],
          g: CompletionStage[_ <: T],
          a: Consumer[_ >: T]
      ): CompletableFuture[Void] = f.acceptEither(g, a)
      def applyToEither[T <: AnyRef, U <: AnyRef](
          f: CompletableFuture[T],
          g: CompletionStage[_ <: T],
          a: Function[_ >: T, U]
      ): CompletableFuture[U] = f.applyToEither(g, a)
    }
    case object ASYNC extends ASYNC
    abstract class ASYNC extends ExecutionMode {
      def checkExecutionMode(): Unit = {
        // Invalid on Scala Native, requires https://github.com/scala-native/scala-native/pull/4116
        // assertEquals(defaultExecutorIsCommonPool, ForkJoinPool.commonPool() == ForkJoinTask.getPool())
      }
      def runAsync(a: Runnable): CompletableFuture[Void] = CompletableFuture.runAsync(a)
      def supplyAsync[U <: AnyRef](a: Supplier[U]): CompletableFuture[U] = CompletableFuture.supplyAsync(a)
      def thenRun[T <: AnyRef](f: CompletableFuture[T], a: Runnable): CompletableFuture[Void] = f.thenRunAsync(a)
      def thenAccept[T <: AnyRef](f: CompletableFuture[T], a: Consumer[_ >: T]): CompletableFuture[Void] =
        f.thenAcceptAsync(a)
      def thenApply[T <: AnyRef, U <: AnyRef](f: CompletableFuture[T], a: Function[_ >: T, U]): CompletableFuture[U] =
        f.thenApplyAsync(a)
      def thenCompose[T <: AnyRef, U <: AnyRef](
          f: CompletableFuture[T],
          a: Function[_ >: T, _ <: CompletionStage[U]]
      ): CompletableFuture[U] = f.thenComposeAsync(a)
      def handle[T <: AnyRef, U <: AnyRef](
          f: CompletableFuture[T],
          a: BiFunction[_ >: T, Throwable, _ <: U]
      ): CompletableFuture[U] =
        f.handleAsync(a)
      def whenComplete[T <: AnyRef](
          f: CompletableFuture[T],
          a: BiConsumer[_ >: T, _ >: Throwable]
      ): CompletableFuture[T] =
        f.whenCompleteAsync(a)
      def runAfterBoth[T <: AnyRef, U <: AnyRef](
          f: CompletableFuture[T],
          g: CompletableFuture[U],
          a: Runnable
      ): CompletableFuture[Void] =
        f.runAfterBothAsync(g, a)
      def thenAcceptBoth[T <: AnyRef, U <: AnyRef](
          f: CompletableFuture[T],
          g: CompletionStage[_ <: U],
          a: BiConsumer[_ >: T, _ >: U]
      ): CompletableFuture[Void] = f.thenAcceptBothAsync(g, a)
      def thenCombine[T <: AnyRef, U <: AnyRef, V <: AnyRef](
          f: CompletableFuture[T],
          g: CompletionStage[_ <: U],
          a: BiFunction[_ >: T, _ >: U, _ <: V]
      ): CompletableFuture[V] = f.thenCombineAsync(g, a)
      def runAfterEither[T <: AnyRef](
          f: CompletableFuture[T],
          g: CompletionStage[_],
          a: Runnable
      ): CompletableFuture[Void] =
        f.runAfterEitherAsync(g, a)
      def acceptEither[T <: AnyRef](
          f: CompletableFuture[T],
          g: CompletionStage[_ <: T],
          a: Consumer[_ >: T]
      ): CompletableFuture[Void] = f.acceptEitherAsync(g, a)
      def applyToEither[T <: AnyRef, U <: AnyRef](
          f: CompletableFuture[T],
          g: CompletionStage[_ <: T],
          a: Function[_ >: T, U]
      ): CompletableFuture[U] = f.applyToEitherAsync(g, a)
    }
    object EXECUTOR extends EXECUTOR
    abstract class EXECUTOR extends ExecutionMode {
      def checkExecutionMode(): Unit = {
        // Invalid on Scala Native, requires https://github.com/scala-native/scala-native/pull/4116
        // assertTrue(ThreadExecutor.startedCurrentThread())
      }
      def runAsync(a: Runnable): CompletableFuture[Void] = CompletableFuture.runAsync(a, new ThreadExecutor())
      def supplyAsync[U <: AnyRef](a: Supplier[U]): CompletableFuture[U] =
        CompletableFuture.supplyAsync(a, new ThreadExecutor())
      def thenRun[T <: AnyRef](f: CompletableFuture[T], a: Runnable): CompletableFuture[Void] =
        f.thenRunAsync(a, new ThreadExecutor())
      def thenAccept[T <: AnyRef](f: CompletableFuture[T], a: Consumer[_ >: T]): CompletableFuture[Void] =
        f.thenAcceptAsync(a, new ThreadExecutor())
      def thenApply[T <: AnyRef, U <: AnyRef](f: CompletableFuture[T], a: Function[_ >: T, U]): CompletableFuture[U] =
        f.thenApplyAsync(a, new ThreadExecutor())
      def thenCompose[T <: AnyRef, U <: AnyRef](
          f: CompletableFuture[T],
          a: Function[_ >: T, _ <: CompletionStage[U]]
      ): CompletableFuture[U] = f.thenComposeAsync(a, new ThreadExecutor())
      def handle[T <: AnyRef, U <: AnyRef](
          f: CompletableFuture[T],
          a: BiFunction[_ >: T, Throwable, _ <: U]
      ): CompletableFuture[U] =
        f.handleAsync(a, new ThreadExecutor())
      def whenComplete[T <: AnyRef](
          f: CompletableFuture[T],
          a: BiConsumer[_ >: T, _ >: Throwable]
      ): CompletableFuture[T] =
        f.whenCompleteAsync(a, new ThreadExecutor())
      def runAfterBoth[T <: AnyRef, U <: AnyRef](
          f: CompletableFuture[T],
          g: CompletableFuture[U],
          a: Runnable
      ): CompletableFuture[Void] =
        f.runAfterBothAsync(g, a, new ThreadExecutor())
      def thenAcceptBoth[T <: AnyRef, U <: AnyRef](
          f: CompletableFuture[T],
          g: CompletionStage[_ <: U],
          a: BiConsumer[_ >: T, _ >: U]
      ): CompletableFuture[Void] = f.thenAcceptBothAsync(g, a, new ThreadExecutor())
      def thenCombine[T <: AnyRef, U <: AnyRef, V <: AnyRef](
          f: CompletableFuture[T],
          g: CompletionStage[_ <: U],
          a: BiFunction[_ >: T, _ >: U, _ <: V]
      ): CompletableFuture[V] = f.thenCombineAsync(g, a, new ThreadExecutor())
      def runAfterEither[T <: AnyRef](
          f: CompletableFuture[T],
          g: CompletionStage[_],
          a: Runnable
      ): CompletableFuture[Void] =
        f.runAfterEitherAsync(g, a, new ThreadExecutor())
      def acceptEither[T <: AnyRef](
          f: CompletableFuture[T],
          g: CompletionStage[_ <: T],
          a: Consumer[_ >: T]
      ): CompletableFuture[Void] = f.acceptEitherAsync(g, a, new ThreadExecutor())
      def applyToEither[T <: AnyRef, U <: AnyRef](
          f: CompletableFuture[T],
          g: CompletionStage[_ <: T],
          a: Function[_ >: T, U]
      ): CompletableFuture[U] = f.applyToEitherAsync(g, a, new ThreadExecutor())
    }
  }

  /** Demo utility method for external reliable toCompletableFuture */
  def toCompletableFuture[T <: AnyRef](stage: CompletionStage[T]): CompletableFuture[T] = {
    val f = new CompletableFuture[T]
    stage.handle((t: T, ex: Throwable) => {
      if (ex != null) f.completeExceptionally(ex)
      else f.complete(t)
      null.asInstanceOf[T]
    })
    f
  }

  /** Demo utility method to join a CompletionStage */
  def join[T <: AnyRef](stage: CompletionStage[T]): T = toCompletableFuture(stage).join()

  // For testing default implementations
  // Only non-default interface methods defined.
  //     static <U> U.join()(CompletionStage<U> stage) {
  //         CompletableFuture<U> f = new CompletableFuture<>();
  //         stage.whenComplete((v, ex) -> {
  //             if (ex != null) f.completeExceptionally(ex); else f.complete(v);
  //         });
  //         return f.join();
  //     }
  //     static <U> boolean isDone(CompletionStage<U> stage) {
  //         CompletableFuture<U> f = new CompletableFuture<>();
  //         stage.whenComplete((v, ex) -> {
  //             if (ex != null) f.completeExceptionally(ex); else f.complete(v);
  //         });
  //         return f.isDone();
  //     }
  //     static <U> U.join()2(CompletionStage<U> stage) {
  //         return stage.toCompletableFuture().copy().join();
  //     }
  //     static <U> boolean isDone2(CompletionStage<U> stage) {
  //         return stage.toCompletableFuture().copy().isDone();
  //     }
  final class DelegatedCompletionStage[T](val cf: CompletableFuture[T]) extends CompletionStage[T] {
    override def toCompletableFuture: CompletableFuture[T] = cf

    override def thenRun(action: Runnable): CompletionStage[Void] = cf.thenRun(action)

    override def thenRunAsync(action: Runnable): CompletionStage[Void] = cf.thenRunAsync(action)

    override def thenRunAsync(action: Runnable, executor: Executor): CompletionStage[Void] =
      cf.thenRunAsync(action, executor)

    override def thenAccept(action: Consumer[_ >: T]): CompletionStage[Void] = cf.thenAccept(action)

    override def thenAcceptAsync(action: Consumer[_ >: T]): CompletionStage[Void] = cf.thenAcceptAsync(action)

    override def thenAcceptAsync(action: Consumer[_ >: T], executor: Executor): CompletionStage[Void] =
      cf.thenAcceptAsync(action, executor)

    override def thenApply[U <: AnyRef](a: Function[_ >: T, _ <: U]): CompletionStage[U] = cf.thenApply(a)

    override def thenApplyAsync[U <: AnyRef](fn: Function[_ >: T, _ <: U]): CompletionStage[U] = cf.thenApplyAsync(fn)

    override def thenApplyAsync[U <: AnyRef](fn: Function[_ >: T, _ <: U], executor: Executor): CompletionStage[U] =
      cf.thenApplyAsync(fn, executor)

    override def thenCombine[U <: AnyRef, V <: AnyRef](
        other: CompletionStage[_ <: U],
        fn: BiFunction[_ >: T, _ >: U, _ <: V]
    ): CompletionStage[V] = cf.thenCombine(other, fn)

    override def thenCombineAsync[U <: AnyRef, V <: AnyRef](
        other: CompletionStage[_ <: U],
        fn: BiFunction[_ >: T, _ >: U, _ <: V]
    ): CompletionStage[V] = cf.thenCombineAsync(other, fn)

    override def thenCombineAsync[U <: AnyRef, V <: AnyRef](
        other: CompletionStage[_ <: U],
        fn: BiFunction[_ >: T, _ >: U, _ <: V],
        executor: Executor
    ): CompletionStage[V] = cf.thenCombineAsync(other, fn, executor)

    override def thenAcceptBoth[U <: AnyRef](
        other: CompletionStage[_ <: U],
        action: BiConsumer[_ >: T, _ >: U]
    ): CompletionStage[Void] = cf.thenAcceptBoth(other, action)

    override def thenAcceptBothAsync[U <: AnyRef](
        other: CompletionStage[_ <: U],
        action: BiConsumer[_ >: T, _ >: U]
    ): CompletionStage[Void] = cf.thenAcceptBothAsync(other, action)

    override def thenAcceptBothAsync[U <: AnyRef](
        other: CompletionStage[_ <: U],
        action: BiConsumer[_ >: T, _ >: U],
        executor: Executor
    ): CompletionStage[Void] = cf.thenAcceptBothAsync(other, action, executor)

    override def runAfterBoth(other: CompletionStage[_], action: Runnable): CompletionStage[Void] =
      cf.runAfterBoth(other, action)

    override def runAfterBothAsync(other: CompletionStage[_], action: Runnable): CompletionStage[Void] =
      cf.runAfterBothAsync(other, action)

    override def runAfterBothAsync(
        other: CompletionStage[_],
        action: Runnable,
        executor: Executor
    ): CompletionStage[Void] = cf.runAfterBothAsync(other, action, executor)

    override def applyToEither[U <: AnyRef](
        other: CompletionStage[_ <: T],
        fn: Function[_ >: T, U]
    ): CompletionStage[U] =
      cf.applyToEither(other, fn)

    override def applyToEitherAsync[U <: AnyRef](
        other: CompletionStage[_ <: T],
        fn: Function[_ >: T, U]
    ): CompletionStage[U] =
      cf.applyToEitherAsync(other, fn)

    override def applyToEitherAsync[U <: AnyRef](
        other: CompletionStage[_ <: T],
        fn: Function[_ >: T, U],
        executor: Executor
    ): CompletionStage[U] = cf.applyToEitherAsync(other, fn, executor)

    override def acceptEither(other: CompletionStage[_ <: T], action: Consumer[_ >: T]): CompletionStage[Void] =
      cf.acceptEither(other, action)

    override def acceptEitherAsync(other: CompletionStage[_ <: T], action: Consumer[_ >: T]): CompletionStage[Void] =
      cf.acceptEitherAsync(other, action)

    override def acceptEitherAsync(
        other: CompletionStage[_ <: T],
        action: Consumer[_ >: T],
        executor: Executor
    ): CompletionStage[Void] = cf.acceptEitherAsync(other, action, executor)

    override def runAfterEither(other: CompletionStage[_], action: Runnable): CompletionStage[Void] =
      cf.runAfterEither(other, action)

    override def runAfterEitherAsync(other: CompletionStage[_], action: Runnable): CompletionStage[Void] =
      cf.runAfterEitherAsync(other, action)

    override def runAfterEitherAsync(
        other: CompletionStage[_],
        action: Runnable,
        executor: Executor
    ): CompletionStage[Void] = cf.runAfterEitherAsync(other, action, executor)

    override def thenCompose[U <: AnyRef](fn: Function[_ >: T, _ <: CompletionStage[U]]): CompletionStage[U] =
      cf.thenCompose(fn)

    override def thenComposeAsync[U <: AnyRef](fn: Function[_ >: T, _ <: CompletionStage[U]]): CompletionStage[U] =
      cf.thenComposeAsync(fn)

    override def thenComposeAsync[U <: AnyRef](
        fn: Function[_ >: T, _ <: CompletionStage[U]],
        executor: Executor
    ): CompletionStage[U] = cf.thenComposeAsync(fn, executor)

    override def handle[U <: AnyRef](fn: BiFunction[_ >: T, Throwable, _ <: U]): CompletionStage[U] = cf.handle(fn)

    override def handleAsync[U <: AnyRef](fn: BiFunction[_ >: T, Throwable, _ <: U]): CompletionStage[U] =
      cf.handleAsync(fn)

    override def handleAsync[U <: AnyRef](
        fn: BiFunction[_ >: T, Throwable, _ <: U],
        executor: Executor
    ): CompletionStage[U] =
      cf.handleAsync(fn, executor)

    override def whenComplete(action: BiConsumer[_ >: T, _ >: Throwable]): CompletionStage[T] = cf.whenComplete(action)

    override def whenCompleteAsync(action: BiConsumer[_ >: T, _ >: Throwable]): CompletionStage[T] =
      cf.whenCompleteAsync(action)

    override def whenCompleteAsync(action: BiConsumer[_ >: T, _ >: Throwable], executor: Executor): CompletionStage[T] =
      cf.whenCompleteAsync(action, executor)

    override def exceptionally(fn: Function[Throwable, _ <: T]): CompletionStage[T] = cf.exceptionally(fn)
  }
}

class CompletableFutureTest extends JSR166Test {
  import JSR166Test._
  import CompletableFutureTest._

  def checkIncomplete[T <: AnyRef](f: CompletableFuture[T]): Unit = {
    assertFalse(f.isDone)
    assertFalse(f.isCancelled)
    assertTrue(f.toString.matches(".*\\[.*Not completed.*\\]"))
    val result =
      try f.getNow(null.asInstanceOf[T])
      catch { case fail: Throwable => threadUnexpectedException(fail) }
    assertNull(result)
    try {
      f.get(randomExpiredTimeout(), randomTimeUnit())
      shouldThrow()
    } catch {
      case success: TimeoutException =>
      case fail: Throwable           => threadUnexpectedException(fail)
    }
  }

  def checkCompletedNormally[T <: AnyRef](f: CompletableFuture[T], expectedValue: T): Unit = {
    checkTimedGet(f, expectedValue)
    assertEquals(expectedValue, f.join())
    assertEquals(expectedValue, f.getNow(null.asInstanceOf[T]))
    var result: T = null.asInstanceOf[T]
    try result = f.get()
    catch {
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    assertEquals(expectedValue, result)
    assertTrue(f.isDone)
    assertFalse(f.isCancelled)
    assertFalse(f.isCompletedExceptionally)
    assertTrue(f.toString.matches(".*\\[.*Completed normally.*\\]"))
  }

  /** Returns the "raw" internal exceptional completion of f, without any additional wrapping with CompletionException.
   */
  def exceptionalCompletion[T <: AnyRef](f: CompletableFuture[T]): Throwable = {
    // handle (and whenComplete and exceptionally) can distinguish
    // between "direct" and "wrapped" exceptional completion
    f.handle[Throwable]((u: T, t: Throwable) => t).join()
  }

  def checkCompletedExceptionally[T <: AnyRef](
      f: CompletableFuture[T],
      wrapped: Boolean,
      checker: Consumer[Throwable]
  ): Unit = {
    var cause = exceptionalCompletion(f)
    if (wrapped) {
      assertTrue(cause.isInstanceOf[CompletionException])
      cause = cause.getCause()
    }
    checker.accept(cause)
    val startTime = System.nanoTime
    try {
      f.get(LONG_DELAY_MS, MILLISECONDS)
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertSame(cause, success.getCause())
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS / 2)
    try {
      f.join()
      shouldThrow()
    } catch {
      case success: CompletionException =>
        assertSame(cause, success.getCause())
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    try {
      f.getNow(null.asInstanceOf[T])
      shouldThrow()
    } catch {
      case success: CompletionException =>
        assertSame(cause, success.getCause())
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    try {
      f.get()
      shouldThrow()
    } catch {
      case success: ExecutionException =>
        assertSame(cause, success.getCause())
      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    assertFalse(f.isCancelled)
    assertTrue(f.isDone)
    assertTrue(f.isCompletedExceptionally)
    assertTrue(f.toString.matches(".*\\[.*Completed exceptionally.*\\]"))
  }

  def checkCompletedWithWrappedCFException[T <: AnyRef](f: CompletableFuture[T]): Unit = {
    checkCompletedExceptionally(
      f,
      true,
      (t: Throwable) => assertTrue(t.isInstanceOf[CFException])
    )
  }

  def checkCompletedWithWrappedCancellationException[T <: AnyRef](f: CompletableFuture[T]): Unit = {
    checkCompletedExceptionally(f, true, (t: Throwable) => assertTrue(t.isInstanceOf[CancellationException]))
  }

  def checkCompletedWithTimeoutException[T <: AnyRef](f: CompletableFuture[T]): Unit = {
    checkCompletedExceptionally(f, false, (t: Throwable) => assertTrue(t.isInstanceOf[TimeoutException]))
  }

  def checkCompletedWithWrappedException[T <: AnyRef](f: CompletableFuture[T], ex: Throwable): Unit = {
    checkCompletedExceptionally(f, true, (t: Throwable) => assertSame(t, ex))
  }

  def checkCompletedExceptionally[T <: AnyRef](f: CompletableFuture[T], ex: Throwable): Unit = {
    checkCompletedExceptionally(f, false, (t: Throwable) => assertSame(t, ex))
  }

  def checkCancelled[T <: AnyRef](f: CompletableFuture[T]): Unit = {
    val startTime = System.nanoTime
    try {
      f.get(LONG_DELAY_MS, MILLISECONDS)
      shouldThrow()
    } catch {
      case success: CancellationException =>

      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS / 2)
    try {
      f.join()
      shouldThrow()
    } catch {
      case success: CancellationException =>

    }
    try {
      f.getNow(null.asInstanceOf[T])
      shouldThrow()
    } catch {
      case success: CancellationException =>

    }
    try {
      f.get()
      shouldThrow()
    } catch {
      case success: CancellationException =>

      case fail: Throwable =>
        threadUnexpectedException(fail)
    }
    assertTrue(exceptionalCompletion(f).isInstanceOf[CancellationException])
    assertTrue(f.isDone)
    assertTrue(f.isCompletedExceptionally)
    assertTrue(f.isCancelled)
    assertTrue(f.toString.matches(".*\\[.*Completed exceptionally.*\\]"))
  }

  /** A newly constructed CompletableFuture is incomplete, as indicated by methods isDone, isCancelled, and getNow
   */
  @Test def testConstructor(): Unit = {
    val f = new CompletableFuture[Item]
    checkIncomplete(f)
  }

  /** complete completes normally, as indicated by methods isDone, isCancelled,.join(), get(), and getNow
   */
  @Test def testComplete(): Unit = {
    for (v1 <- Array[Item](one, null)) {
      val f = new CompletableFuture[Item]
      checkIncomplete(f)
      assertTrue(f.complete(v1))
      assertFalse(f.complete(v1))
      checkCompletedNormally(f, v1)
    }
  }

  /** completeExceptionally completes exceptionally, as indicated by methods isDone, isCancelled,.join(), get(), and
   *  getNow
   */
  @Test def testCompleteExceptionally(): Unit = {
    val f = new CompletableFuture[Item]
    val ex = new CFException
    checkIncomplete(f)
    f.completeExceptionally(ex)
    checkCompletedExceptionally(f, ex)
  }

  /** cancel completes exceptionally and reports cancelled, as indicated by methods isDone, isCancelled,.join(), get(),
   *  and getNow
   */
  @Test def testCancel(): Unit = {
    for (mayInterruptIfRunning <- Array[Boolean](true, false)) {
      val f = new CompletableFuture[Item]
      checkIncomplete(f)
      assertTrue(f.cancel(mayInterruptIfRunning))
      assertTrue(f.cancel(mayInterruptIfRunning))
      assertTrue(f.cancel(!mayInterruptIfRunning))
      checkCancelled(f)
    }
  }

  /** obtrudeValue forces completion with given value
   */
  @Test def testObtrudeValue(): Unit = {
    var f = new CompletableFuture[Item]
    checkIncomplete[Item](f)
    assertTrue(f.complete(one))
    checkCompletedNormally[Item](f, one)
    f.obtrudeValue(three)
    checkCompletedNormally[Item](f, three)
    f.obtrudeValue(two)
    checkCompletedNormally[Item](f, two)
    f = new CompletableFuture[Item]
    f.obtrudeValue(three)
    checkCompletedNormally[Item](f, three)
    f.obtrudeValue(null)
    checkCompletedNormally[Item](f, null)
    f = new CompletableFuture[Item]
    f.completeExceptionally(new CFException)
    f.obtrudeValue(four)
    checkCompletedNormally[Item](f, four)
  }

  /** obtrudeException forces completion with given exception
   */
  @Test def testObtrudeException(): Unit = {
    for (v1 <- Array[Item](one, null)) {
      var ex: CFException = null
      var f: CompletableFuture[Item] = null
      f = new CompletableFuture[Item]
      assertTrue(f.complete(v1))
      for (i <- 0 until 2) {
        f.obtrudeException({ ex = new CFException; ex })
        checkCompletedExceptionally(f, ex)
      }
      f = new CompletableFuture[Item]
      for (i <- 0 until 2) {
        f.obtrudeException({ ex = new CFException; ex })
        checkCompletedExceptionally(f, ex)
      }
      f = new CompletableFuture[Item]
      f.completeExceptionally(new CFException)
      f.obtrudeValue(v1)
      checkCompletedNormally(f, v1)
      f.obtrudeException({ ex = new CFException; ex })
      checkCompletedExceptionally(f, ex)
      f.completeExceptionally(new CFException)
      checkCompletedExceptionally(f, ex)
      assertFalse(f.complete(v1))
      checkCompletedExceptionally(f, ex)
    }
  }

  /** getNumberOfDependents() returns number of dependent tasks
   */
  @Test def testGetNumberOfDependents(): Unit = {
    for (m <- ExecutionMode.values) {
      for (v1 <- Array[Item](one, null)) {
        val f = new CompletableFuture[Item]
        assertEquals(0, f.getNumberOfDependents())
        val g = m.thenRun(f, new Noop(m))
        assertEquals(1, f.getNumberOfDependents())
        assertEquals(0, g.getNumberOfDependents())
        val h = m.thenRun(f, new Noop(m))
        assertEquals(2, f.getNumberOfDependents())
        assertEquals(0, h.getNumberOfDependents())
        assertTrue(f.complete(v1))
        checkCompletedNormally(g, null)
        checkCompletedNormally(h, null)
        assertEquals(0, f.getNumberOfDependents())
        assertEquals(0, g.getNumberOfDependents())
        assertEquals(0, h.getNumberOfDependents())
      }
    }
  }

  /** toString indicates current completion state
   */
  @Test def testToString_incomplete(): Unit = {
    val f = new CompletableFuture[String]
    assertTrue(f.toString.matches(".*\\[.*Not completed.*\\]"))
    if (testImplementationDetails) assertEquals(identityString(f) + "[Not completed]", f.toString)
  }

  @Test def testToString_normal(): Unit = {
    val f = new CompletableFuture[String]
    assertTrue(f.complete("foo"))
    assertTrue(f.toString.matches(".*\\[.*Completed normally.*\\]"))
    if (testImplementationDetails) assertEquals(identityString(f) + "[Completed normally]", f.toString)
  }

  @Test def testToString_exception(): Unit = {
    val f = new CompletableFuture[String]
    assertTrue(f.completeExceptionally(new IndexOutOfBoundsException))
    assertTrue(f.toString.matches(".*\\[.*Completed exceptionally.*\\]"))
    if (testImplementationDetails)
      if (!Platform.executingInJVMOnLowerThanJDK17)
        assertTrue(f.toString.startsWith(identityString(f) + "[Completed exceptionally: "))
  }

  @Test def testToString_cancelled(): Unit = {
    for (mayInterruptIfRunning <- Array[Boolean](true, false)) {
      val f = new CompletableFuture[String]
      assertTrue(f.cancel(mayInterruptIfRunning))
      assertTrue(f.toString.matches(".*\\[.*Completed exceptionally.*\\]"))
      if (testImplementationDetails)
        if (!Platform.executingInJVMOnLowerThanJDK17)
          assertTrue(f.toString.startsWith(identityString(f) + "[Completed exceptionally: "))
    }
  }

  /** completedFuture returns a completed CompletableFuture with given value
   */
  @Test def testCompletedFuture(): Unit = {
    val f = CompletableFuture.completedFuture("test")
    checkCompletedNormally(f, "test")
  }

  /** whenComplete action executes on normal completion, propagating source result.
   */
  @Test def testWhenComplete_normalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (createIncomplete <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          val ran = new AtomicInteger(0)
          val f = new CompletableFuture[Item]
          if (!createIncomplete) assertTrue(f.complete(v1))
          val g = m.whenComplete(
            f,
            (result: Item, t: Throwable) => {
              m.checkExecutionMode()
              assertSame(result, v1)
              assertNull(t)
              ran.getAndIncrement()

            }
          )
          if (createIncomplete) assertTrue(f.complete(v1))
          checkCompletedNormally(g, v1)
          checkCompletedNormally(f, v1)
          assertEquals(1, ran.get())
        }
      }
    }
  }

  /** whenComplete action executes on exceptional completion, propagating source result.
   */
  @Test def testWhenComplete_exceptionalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (createIncomplete <- Array[Boolean](true, false)) {
        val ran = new AtomicInteger(0)
        val ex = new CFException
        val f = new CompletableFuture[Item]
        if (!createIncomplete) f.completeExceptionally(ex)
        val g = m.whenComplete(
          f,
          (result: Item, t: Throwable) => {
            m.checkExecutionMode()
            assertNull(result)
            assertSame(t, ex)
            ran.getAndIncrement()

          }
        )
        if (createIncomplete) f.completeExceptionally(ex)
        checkCompletedWithWrappedException(g, ex)
        checkCompletedExceptionally(f, ex)
        assertEquals(1, ran.get())
      }
    }
  }

  /** whenComplete action executes on cancelled source, propagating CancellationException.
   */
  @Test def testWhenComplete_sourceCancelled(): Unit = {
    for (m <- ExecutionMode.values) {
      for (mayInterruptIfRunning <- Array[Boolean](true, false)) {
        for (createIncomplete <- Array[Boolean](true, false)) {
          val ran = new AtomicInteger(0)
          val f = new CompletableFuture[Item]
          if (!createIncomplete) assertTrue(f.cancel(mayInterruptIfRunning))
          val g = m.whenComplete(
            f,
            (result: Item, t: Throwable) => {
              m.checkExecutionMode()
              assertNull(result)
              assertTrue(t.isInstanceOf[CancellationException])
              ran.getAndIncrement()

            }
          )
          if (createIncomplete) assertTrue(f.cancel(mayInterruptIfRunning))
          checkCompletedWithWrappedCancellationException(g)
          checkCancelled(f)
          assertEquals(1, ran.get())
        }
      }
    }
  }

  /** If a whenComplete action throws an exception when triggered by a normal completion, it completes exceptionally
   */
  @Test def testWhenComplete_sourceCompletedNormallyActionFailed(): Unit = {
    for (createIncomplete <- Array[Boolean](true, false)) {
      for (m <- ExecutionMode.values) {
        for (v1 <- Array[Item](one, null)) {
          val ran = new AtomicInteger(0)
          val ex = new CFException
          val f = new CompletableFuture[Item]
          if (!createIncomplete) assertTrue(f.complete(v1))
          val g = m.whenComplete(
            f,
            (result: Item, t: Throwable) => {
              m.checkExecutionMode()
              assertSame(result, v1)
              assertNull(t)
              ran.getAndIncrement()
              throw ex

            }
          )
          if (createIncomplete) assertTrue(f.complete(v1))
          checkCompletedWithWrappedException(g, ex)
          checkCompletedNormally(f, v1)
          assertEquals(1, ran.get())
        }
      }
    }
  }

  /** If a whenComplete action throws an exception when triggered by a source completion that also throws an exception,
   *  the source exception takes precedence (unlike handle)
   */
  @Test def testWhenComplete_sourceFailedActionFailed(): Unit = {
    for (createIncomplete <- Array[Boolean](true, false)) {
      for (m <- ExecutionMode.values) {
        val ran = new AtomicInteger(0)
        val ex1 = new CFException
        val ex2 = new CFException
        val f = new CompletableFuture[Item]
        if (!createIncomplete) f.completeExceptionally(ex1)
        val g = m.whenComplete(
          f,
          (result: Item, t: Throwable) => {
            m.checkExecutionMode()
            assertSame(t, ex1)
            assertNull(result)
            ran.getAndIncrement()
            throw ex2

          }
        )
        if (createIncomplete) f.completeExceptionally(ex1)
        checkCompletedWithWrappedException(g, ex1)
        checkCompletedExceptionally(f, ex1)
        if (testImplementationDetails) if (!Platform.executingInJVMOnLowerThanJDK17) {
          assertEquals(1, ex1.getSuppressed().length)
          assertSame(ex2, ex1.getSuppressed()(0))
        }
        assertEquals(1, ran.get())
      }
    }
  }

  /** handle action completes normally with function value on normal completion of source
   */
  @Test def testHandle_normalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (createIncomplete <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          val f = new CompletableFuture[Item]
          val ran = new AtomicInteger(0)
          if (!createIncomplete) assertTrue(f.complete(v1))
          val g = m.handle(
            f,
            (result: Item, t: Throwable) => {
              m.checkExecutionMode()
              assertSame(result, v1)
              assertNull(t)
              ran.getAndIncrement()
              inc(v1)
            }
          )
          if (createIncomplete) assertTrue(f.complete(v1))
          checkCompletedNormally(g, inc(v1))
          checkCompletedNormally(f, v1)
          assertEquals(1, ran.get())
        }
      }
    }
  }

  /** handle action completes normally with function value on exceptional completion of source
   */
  @Test def testHandle_exceptionalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (createIncomplete <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          val f = new CompletableFuture[Item]
          val ran = new AtomicInteger(0)
          val ex = new CFException
          if (!createIncomplete) f.completeExceptionally(ex)
          val g = m.handle(
            f,
            (result: Item, t: Throwable) => {
              m.checkExecutionMode()
              assertNull(result)
              assertSame(t, ex)
              ran.getAndIncrement()
              v1

            }
          )
          if (createIncomplete) f.completeExceptionally(ex)
          checkCompletedNormally(g, v1)
          checkCompletedExceptionally(f, ex)
          assertEquals(1, ran.get())
        }
      }
    }
  }

  /** handle action completes normally with function value on cancelled source
   */
  @Test def testHandle_sourceCancelled(): Unit = {
    for (m <- ExecutionMode.values) {
      for (mayInterruptIfRunning <- Array[Boolean](true, false)) {
        for (createIncomplete <- Array[Boolean](true, false)) {
          for (v1 <- Array[Item](one, null)) {
            val f = new CompletableFuture[Item]
            val ran = new AtomicInteger(0)
            if (!createIncomplete) assertTrue(f.cancel(mayInterruptIfRunning))
            val g = m.handle(
              f,
              (result: Item, t: Throwable) => {
                m.checkExecutionMode()
                assertNull(result)
                assertTrue(t.isInstanceOf[CancellationException])
                ran.getAndIncrement()
                v1

              }
            )
            if (createIncomplete) assertTrue(f.cancel(mayInterruptIfRunning))
            checkCompletedNormally(g, v1)
            checkCancelled(f)
            assertEquals(1, ran.get())
          }
        }
      }
    }
  }

  /** If a "handle action" throws an exception when triggered by a normal completion, it completes exceptionally
   */
  @Test def testHandle_sourceCompletedNormallyActionFailed(): Unit = {
    for (m <- ExecutionMode.values) {
      for (createIncomplete <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          val f = new CompletableFuture[Item]
          val ran = new AtomicInteger(0)
          val ex = new CFException
          if (!createIncomplete) assertTrue(f.complete(v1))
          val g = m.handle(
            f,
            (result: Item, t: Throwable) => {
              m.checkExecutionMode()
              assertSame(result, v1)
              assertNull(t)
              ran.getAndIncrement()
              throw ex

            }
          )
          if (createIncomplete) assertTrue(f.complete(v1))
          checkCompletedWithWrappedException(g, ex)
          checkCompletedNormally(f, v1)
          assertEquals(1, ran.get())
        }
      }
    }
  }

  /** If a "handle action" throws an exception when triggered by a source completion that also throws an exception, the
   *  action exception takes precedence (unlike whenComplete)
   */
  @Test def testHandle_sourceFailedActionFailed(): Unit = {
    for (createIncomplete <- Array[Boolean](true, false)) {
      for (m <- ExecutionMode.values) {
        val ran = new AtomicInteger(0)
        val ex1 = new CFException
        val ex2 = new CFException
        val f = new CompletableFuture[Item]
        if (!createIncomplete) f.completeExceptionally(ex1)
        val g = m.handle(
          f,
          (result: Item, t: Throwable) => {
            m.checkExecutionMode()
            assertNull(result)
            assertSame(ex1, t)
            ran.getAndIncrement()
            throw ex2

          }
        )
        if (createIncomplete) f.completeExceptionally(ex1)
        checkCompletedWithWrappedException(g, ex2)
        checkCompletedExceptionally(f, ex1)
        assertEquals(1, ran.get())
      }
    }
  }

  /** runAsync completes after running Runnable
   */
  @Test def testRunAsync_normalCompletion(): Unit = {
    val executionModes = Array(ExecutionMode.ASYNC, ExecutionMode.EXECUTOR)
    for (m <- executionModes) {
      val r = new Noop(m)
      val f = m.runAsync(r)
      assertNull(f.join())
      checkCompletedNormally(f, null)
      r.assertinvoked()
    }
  }

  /** failing runAsync completes exceptionally after running Runnable
   */
  @Test def testRunAsync_exceptionalCompletion(): Unit = {
    val executionModes = Array(ExecutionMode.ASYNC, ExecutionMode.EXECUTOR)
    for (m <- executionModes) {
      val r = new FailingRunnable(m)
      val f = m.runAsync(r)
      checkCompletedWithWrappedException(f, r.ex)
      r.assertinvoked()
    }
  }

  @SuppressWarnings(Array("FutureReturnValueIgnored")) @Test def testRunAsync_rejectingExecutor(): Unit = {
    val e = new CountingRejectingExecutor
    try {
      CompletableFuture.runAsync(() => {}, e)
      shouldThrow()
    } catch {
      case t: Throwable =>
        assertSame(e.ex, t)
    }
    assertEquals(1, e.count.get())
  }

  /** supplyAsync completes with result of supplier
   */
  @Test def testSupplyAsync_normalCompletion(): Unit = {
    val executionModes = Array(ExecutionMode.ASYNC, ExecutionMode.EXECUTOR)
    for (m <- executionModes) {
      for (v1 <- Array[Item](one, null)) {
        val r = new ItemSupplier(m, v1)
        val f = m.supplyAsync(r)
        assertSame(v1, f.join())
        checkCompletedNormally(f, v1)
        r.assertinvoked()
      }
    }
  }

  /** Failing supplyAsync completes exceptionally
   */
  @Test def testSupplyAsync_exceptionalCompletion(): Unit = {
    val executionModes = Array(ExecutionMode.ASYNC, ExecutionMode.EXECUTOR)
    for (m <- executionModes) {
      val r = new FailingSupplier(m)
      val f = m.supplyAsync(r)
      checkCompletedWithWrappedException(f, r.ex)
      r.assertinvoked()
    }
  }

  @SuppressWarnings(Array("FutureReturnValueIgnored")) @Test def testSupplyAsync_rejectingExecutor(): Unit = {
    val e = new CountingRejectingExecutor
    try {
      CompletableFuture.supplyAsync(() => null, e)
      shouldThrow()
    } catch {
      case t: Throwable =>
        assertSame(e.ex, t)
    }
    assertEquals(1, e.count.get())
  }

  /** thenRun result completes normally after normal completion of source
   */
  // seq completion methods
  @Test def testThenRun_normalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (v1 <- Array[Item](one, null)) {
        val f = new CompletableFuture[Item]
        val rs = new Array[Noop](6)
        for (i <- 0 until rs.length) {
          rs(i) = new Noop(m)
        }
        val h0 = m.thenRun(f, rs(0))
        val h1 = m.runAfterBoth(f, f, rs(1))
        val h2 = m.runAfterEither(f, f, rs(2))
        checkIncomplete(h0)
        checkIncomplete(h1)
        checkIncomplete(h2)
        assertTrue(f.complete(v1))
        val h3 = m.thenRun(f, rs(3))
        val h4 = m.runAfterBoth(f, f, rs(4))
        val h5 = m.runAfterEither(f, f, rs(5))
        checkCompletedNormally(h0, null)
        checkCompletedNormally(h1, null)
        checkCompletedNormally(h2, null)
        checkCompletedNormally(h3, null)
        checkCompletedNormally(h4, null)
        checkCompletedNormally(h5, null)
        checkCompletedNormally(f, v1)
        for (r <- rs) {
          r.assertinvoked()
        }
      }
    }
  }

  /** thenRun result completes exceptionally after exceptional completion of source
   */
  @Test def testThenRun_exceptionalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      val ex = new CFException
      val f = new CompletableFuture[Item]
      val rs = new Array[Noop](6)
      for (i <- 0 until rs.length) {
        rs(i) = new Noop(m)
      }
      val h0 = m.thenRun(f, rs(0))
      val h1 = m.runAfterBoth(f, f, rs(1))
      val h2 = m.runAfterEither(f, f, rs(2))
      checkIncomplete(h0)
      checkIncomplete(h1)
      checkIncomplete(h2)
      assertTrue(f.completeExceptionally(ex))
      val h3 = m.thenRun(f, rs(3))
      val h4 = m.runAfterBoth(f, f, rs(4))
      val h5 = m.runAfterEither(f, f, rs(5))
      checkCompletedWithWrappedException(h0, ex)
      checkCompletedWithWrappedException(h1, ex)
      checkCompletedWithWrappedException(h2, ex)
      checkCompletedWithWrappedException(h3, ex)
      checkCompletedWithWrappedException(h4, ex)
      checkCompletedWithWrappedException(h5, ex)
      checkCompletedExceptionally(f, ex)
      for (r <- rs) {
        r.assertNotinvoked()
      }
    }
  }

  /** thenRun result completes exceptionally if source cancelled
   */
  @Test def testThenRun_sourceCancelled(): Unit = {
    for (m <- ExecutionMode.values) {
      for (mayInterruptIfRunning <- Array[Boolean](true, false)) {
        val f = new CompletableFuture[Item]
        val rs = new Array[Noop](6)
        for (i <- 0 until rs.length) {
          rs(i) = new Noop(m)
        }
        val h0 = m.thenRun(f, rs(0))
        val h1 = m.runAfterBoth(f, f, rs(1))
        val h2 = m.runAfterEither(f, f, rs(2))
        checkIncomplete(h0)
        checkIncomplete(h1)
        checkIncomplete(h2)
        assertTrue(f.cancel(mayInterruptIfRunning))
        val h3 = m.thenRun(f, rs(3))
        val h4 = m.runAfterBoth(f, f, rs(4))
        val h5 = m.runAfterEither(f, f, rs(5))
        checkCompletedWithWrappedCancellationException(h0)
        checkCompletedWithWrappedCancellationException(h1)
        checkCompletedWithWrappedCancellationException(h2)
        checkCompletedWithWrappedCancellationException(h3)
        checkCompletedWithWrappedCancellationException(h4)
        checkCompletedWithWrappedCancellationException(h5)
        checkCancelled(f)
        for (r <- rs) {
          r.assertNotinvoked()
        }
      }
    }
  }

  /** thenRun result completes exceptionally if action does
   */
  @Test def testThenRun_actionFailed(): Unit = {
    for (m <- ExecutionMode.values) {
      for (v1 <- Array[Item](one, null)) {
        val f = new CompletableFuture[Item]
        val rs = new Array[FailingRunnable](6)
        for (i <- 0 until rs.length) {
          rs(i) = new FailingRunnable(m)
        }
        val h0 = m.thenRun(f, rs(0))
        val h1 = m.runAfterBoth(f, f, rs(1))
        val h2 = m.runAfterEither(f, f, rs(2))
        assertTrue(f.complete(v1))
        val h3 = m.thenRun(f, rs(3))
        val h4 = m.runAfterBoth(f, f, rs(4))
        val h5 = m.runAfterEither(f, f, rs(5))
        checkCompletedWithWrappedException(h0, rs(0).ex)
        checkCompletedWithWrappedException(h1, rs(1).ex)
        checkCompletedWithWrappedException(h2, rs(2).ex)
        checkCompletedWithWrappedException(h3, rs(3).ex)
        checkCompletedWithWrappedException(h4, rs(4).ex)
        checkCompletedWithWrappedException(h5, rs(5).ex)
        checkCompletedNormally(f, v1)
      }
    }
  }

  /** thenApply result completes normally after normal completion of source
   */
  @Test def testThenApply_normalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (v1 <- Array[Item](one, null)) {
        val f = new CompletableFuture[Item]
        val rs = new Array[IncFunction](4)
        for (i <- 0 until rs.length) {
          rs(i) = new IncFunction(m)
        }
        val h0 = m.thenApply(f, rs(0))
        val h1 = m.applyToEither(f, f, rs(1))
        checkIncomplete(h0)
        checkIncomplete(h1)
        assertTrue(f.complete(v1))
        val h2 = m.thenApply(f, rs(2))
        val h3 = m.applyToEither(f, f, rs(3))
        checkCompletedNormally(h0, inc(v1))
        checkCompletedNormally(h1, inc(v1))
        checkCompletedNormally(h2, inc(v1))
        checkCompletedNormally(h3, inc(v1))
        checkCompletedNormally(f, v1)
        for (r <- rs) {
          r.assertValue(inc(v1))
        }
      }
    }
  }

  /** thenApply result completes exceptionally after exceptional completion of source
   */
  @Test def testThenApply_exceptionalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      val ex = new CFException
      val f = new CompletableFuture[Item]
      val rs = new Array[IncFunction](4)
      for (i <- 0 until rs.length) {
        rs(i) = new IncFunction(m)
      }
      val h0 = m.thenApply(f, rs(0))
      val h1 = m.applyToEither(f, f, rs(1))
      assertTrue(f.completeExceptionally(ex))
      val h2 = m.thenApply(f, rs(2))
      val h3 = m.applyToEither(f, f, rs(3))
      checkCompletedWithWrappedException(h0, ex)
      checkCompletedWithWrappedException(h1, ex)
      checkCompletedWithWrappedException(h2, ex)
      checkCompletedWithWrappedException(h3, ex)
      checkCompletedExceptionally(f, ex)
      for (r <- rs) {
        r.assertNotinvoked()
      }
    }
  }

  /** thenApply result completes exceptionally if source cancelled
   */
  @Test def testThenApply_sourceCancelled(): Unit = {
    for (m <- ExecutionMode.values) {
      for (mayInterruptIfRunning <- Array[Boolean](true, false)) {
        val f = new CompletableFuture[Item]
        val rs = new Array[IncFunction](4)
        for (i <- 0 until rs.length) {
          rs(i) = new IncFunction(m)
        }
        val h0 = m.thenApply(f, rs(0))
        val h1 = m.applyToEither(f, f, rs(1))
        assertTrue(f.cancel(mayInterruptIfRunning))
        val h2 = m.thenApply(f, rs(2))
        val h3 = m.applyToEither(f, f, rs(3))
        checkCompletedWithWrappedCancellationException(h0)
        checkCompletedWithWrappedCancellationException(h1)
        checkCompletedWithWrappedCancellationException(h2)
        checkCompletedWithWrappedCancellationException(h3)
        checkCancelled(f)
        for (r <- rs) {
          r.assertNotinvoked()
        }
      }
    }
  }

  /** thenApply result completes exceptionally if action does
   */
  @Test def testThenApply_actionFailed(): Unit = {
    for (m <- ExecutionMode.values) {
      for (v1 <- Array[Item](one, null)) {
        val f = new CompletableFuture[Item]
        val rs = new Array[FailingFunction](4)
        for (i <- 0 until rs.length) {
          rs(i) = new FailingFunction(m)
        }
        val h0 = m.thenApply(f, rs(0))
        val h1 = m.applyToEither(f, f, rs(1))
        assertTrue(f.complete(v1))
        val h2 = m.thenApply(f, rs(2))
        val h3 = m.applyToEither(f, f, rs(3))
        checkCompletedWithWrappedException(h0, rs(0).ex)
        checkCompletedWithWrappedException(h1, rs(1).ex)
        checkCompletedWithWrappedException(h2, rs(2).ex)
        checkCompletedWithWrappedException(h3, rs(3).ex)
        checkCompletedNormally(f, v1)
      }
    }
  }

  /** thenAccept result completes normally after normal completion of source
   */
  @Test def testThenAccept_normalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (v1 <- Array[Item](one, null)) {
        val f = new CompletableFuture[Item]
        val rs = new Array[NoopConsumer](4)
        for (i <- 0 until rs.length) {
          rs(i) = new NoopConsumer(m)
        }
        val h0 = m.thenAccept(f, rs(0))
        val h1 = m.acceptEither(f, f, rs(1))
        checkIncomplete(h0)
        checkIncomplete(h1)
        assertTrue(f.complete(v1))
        val h2 = m.thenAccept(f, rs(2))
        val h3 = m.acceptEither(f, f, rs(3))
        checkCompletedNormally(h0, null)
        checkCompletedNormally(h1, null)
        checkCompletedNormally(h2, null)
        checkCompletedNormally(h3, null)
        checkCompletedNormally(f, v1)
        for (r <- rs) {
          r.assertValue(v1)
        }
      }
    }
  }

  /** thenAccept result completes exceptionally after exceptional completion of source
   */
  @Test def testThenAccept_exceptionalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      val ex = new CFException
      val f = new CompletableFuture[Item]
      val rs = new Array[NoopConsumer](4)
      for (i <- 0 until rs.length) {
        rs(i) = new NoopConsumer(m)
      }
      val h0 = m.thenAccept(f, rs(0))
      val h1 = m.acceptEither(f, f, rs(1))
      assertTrue(f.completeExceptionally(ex))
      val h2 = m.thenAccept(f, rs(2))
      val h3 = m.acceptEither(f, f, rs(3))
      checkCompletedWithWrappedException(h0, ex)
      checkCompletedWithWrappedException(h1, ex)
      checkCompletedWithWrappedException(h2, ex)
      checkCompletedWithWrappedException(h3, ex)
      checkCompletedExceptionally(f, ex)
      for (r <- rs) {
        r.assertNotinvoked()
      }
    }
  }

  /** thenAccept result completes exceptionally if source cancelled
   */
  @Test def testThenAccept_sourceCancelled(): Unit = {
    for (m <- ExecutionMode.values) {
      for (mayInterruptIfRunning <- Array[Boolean](true, false)) {
        val f = new CompletableFuture[Item]
        val rs = new Array[NoopConsumer](4)
        for (i <- 0 until rs.length) {
          rs(i) = new NoopConsumer(m)
        }
        val h0 = m.thenAccept(f, rs(0))
        val h1 = m.acceptEither(f, f, rs(1))
        assertTrue(f.cancel(mayInterruptIfRunning))
        val h2 = m.thenAccept(f, rs(2))
        val h3 = m.acceptEither(f, f, rs(3))
        checkCompletedWithWrappedCancellationException(h0)
        checkCompletedWithWrappedCancellationException(h1)
        checkCompletedWithWrappedCancellationException(h2)
        checkCompletedWithWrappedCancellationException(h3)
        checkCancelled(f)
        for (r <- rs) {
          r.assertNotinvoked()
        }
      }
    }
  }

  /** thenAccept result completes exceptionally if action does
   */
  @Test def testThenAccept_actionFailed(): Unit = {
    for (m <- ExecutionMode.values) {
      for (v1 <- Array[Item](one, null)) {
        val f = new CompletableFuture[Item]
        val rs = new Array[FailingConsumer](4)
        for (i <- 0 until rs.length) {
          rs(i) = new FailingConsumer(m)
        }
        val h0 = m.thenAccept(f, rs(0))
        val h1 = m.acceptEither(f, f, rs(1))
        assertTrue(f.complete(v1))
        val h2 = m.thenAccept(f, rs(2))
        val h3 = m.acceptEither(f, f, rs(3))
        checkCompletedWithWrappedException(h0, rs(0).ex)
        checkCompletedWithWrappedException(h1, rs(1).ex)
        checkCompletedWithWrappedException(h2, rs(2).ex)
        checkCompletedWithWrappedException(h3, rs(3).ex)
        checkCompletedNormally(f, v1)
      }
    }
  }

  /** thenCombine result completes normally after normal completion of sources
   */
  @Test def testThenCombine_normalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (fFirst <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          for (v2 <- Array[Item](two, null)) {
            val f = new CompletableFuture[Item]
            val g = new CompletableFuture[Item]
            val rs = new Array[SubtractFunction](6)
            for (i <- 0 until rs.length) {
              rs(i) = new SubtractFunction(m)
            }
            val fst =
              if (fFirst) f
              else g
            val snd =
              if (!fFirst) f
              else g
            val w1 =
              if (fFirst) v1
              else v2
            val w2 =
              if (!fFirst) v1
              else v2
            val h0 = m.thenCombine(f, g, rs(0))
            val h1 = m.thenCombine(fst, fst, rs(1))
            assertTrue(fst.complete(w1))
            val h2 = m.thenCombine(f, g, rs(2))
            val h3 = m.thenCombine(fst, fst, rs(3))
            checkIncomplete(h0)
            rs(0).assertNotinvoked()
            checkIncomplete(h2)
            rs(2).assertNotinvoked()
            checkCompletedNormally(h1, subtract(w1, w1))
            checkCompletedNormally(h3, subtract(w1, w1))
            rs(1).assertValue(subtract(w1, w1))
            rs(3).assertValue(subtract(w1, w1))
            assertTrue(snd.complete(w2))
            val h4 = m.thenCombine(f, g, rs(4))
            checkCompletedNormally(h0, subtract(v1, v2))
            checkCompletedNormally(h2, subtract(v1, v2))
            checkCompletedNormally(h4, subtract(v1, v2))
            rs(0).assertValue(subtract(v1, v2))
            rs(2).assertValue(subtract(v1, v2))
            rs(4).assertValue(subtract(v1, v2))
            checkCompletedNormally(f, v1)
            checkCompletedNormally(g, v2)
          }
        }
      }
    }
  }

  /** thenCombine result completes exceptionally after exceptional completion of either source
   */
  @throws[Throwable]
  @Test def testThenCombine_exceptionalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (fFirst <- Array[Boolean](true, false)) {
        for (failFirst <- Array[Boolean](true, false)) {
          for (v1 <- Array[Item](one, null)) {
            val f = new CompletableFuture[Item]
            val g = new CompletableFuture[Item]
            val ex = new CFException
            val r1 = new SubtractFunction(m)
            val r2 = new SubtractFunction(m)
            val r3 = new SubtractFunction(m)
            val fst =
              if (fFirst) f
              else g
            val snd =
              if (!fFirst) f
              else g
            val complete1: Callable[Boolean] =
              if (failFirst) () => fst.completeExceptionally(ex)
              else () => fst.complete(v1)
            val complete2: Callable[Boolean] =
              if (failFirst) () => snd.complete(v1)
              else () => snd.completeExceptionally(ex)
            val h1 = m.thenCombine(f, g, r1)
            assertTrue(complete1.call())
            val h2 = m.thenCombine(f, g, r2)
            checkIncomplete(h1)
            checkIncomplete(h2)
            assertTrue(complete2.call())
            val h3 = m.thenCombine(f, g, r3)
            checkCompletedWithWrappedException(h1, ex)
            checkCompletedWithWrappedException(h2, ex)
            checkCompletedWithWrappedException(h3, ex)
            r1.assertNotinvoked()
            r2.assertNotinvoked()
            r3.assertNotinvoked()
            checkCompletedNormally(
              if (failFirst) snd
              else fst,
              v1
            )
            checkCompletedExceptionally(
              if (failFirst) fst
              else snd,
              ex
            )
          }
        }
      }
    }
  }

  /** thenCombine result completes exceptionally if either source cancelled
   */
  @throws[Throwable]
  @Test def testThenCombine_sourceCancelled(): Unit = {
    for (m <- ExecutionMode.values) {
      for (mayInterruptIfRunning <- Array[Boolean](true, false)) {
        for (fFirst <- Array[Boolean](true, false)) {
          for (failFirst <- Array[Boolean](true, false)) {
            for (v1 <- Array[Item](one, null)) {
              val f = new CompletableFuture[Item]
              val g = new CompletableFuture[Item]
              val r1 = new SubtractFunction(m)
              val r2 = new SubtractFunction(m)
              val r3 = new SubtractFunction(m)
              val fst =
                if (fFirst) f
                else g
              val snd =
                if (!fFirst) f
                else g
              val complete1: Callable[Boolean] =
                if (failFirst) () => fst.cancel(mayInterruptIfRunning)
                else () => fst.complete(v1)
              val complete2: Callable[Boolean] =
                if (failFirst) () => snd.complete(v1)
                else () => snd.cancel(mayInterruptIfRunning)
              val h1 = m.thenCombine(f, g, r1)
              assertTrue(complete1.call())
              val h2 = m.thenCombine(f, g, r2)
              checkIncomplete(h1)
              checkIncomplete(h2)
              assertTrue(complete2.call())
              val h3 = m.thenCombine(f, g, r3)
              checkCompletedWithWrappedCancellationException(h1)
              checkCompletedWithWrappedCancellationException(h2)
              checkCompletedWithWrappedCancellationException(h3)
              r1.assertNotinvoked()
              r2.assertNotinvoked()
              r3.assertNotinvoked()
              checkCompletedNormally(
                if (failFirst) snd
                else fst,
                v1
              )
              checkCancelled(
                if (failFirst) fst
                else snd
              )
            }
          }
        }
      }
    }
  }

  /** thenCombine result completes exceptionally if action does
   */
  @Test def testThenCombine_actionFailed(): Unit = {
    for (m <- ExecutionMode.values) {
      for (fFirst <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          for (v2 <- Array[Item](two, null)) {
            val f = new CompletableFuture[Item]
            val g = new CompletableFuture[Item]
            val r1 = new FailingBiFunction(m)
            val r2 = new FailingBiFunction(m)
            val r3 = new FailingBiFunction(m)
            val fst =
              if (fFirst) f
              else g
            val snd =
              if (!fFirst) f
              else g
            val w1 =
              if (fFirst) v1
              else v2
            val w2 =
              if (!fFirst) v1
              else v2
            val h1 = m.thenCombine(f, g, r1)
            assertTrue(fst.complete(w1))
            val h2 = m.thenCombine(f, g, r2)
            assertTrue(snd.complete(w2))
            val h3 = m.thenCombine(f, g, r3)
            checkCompletedWithWrappedException(h1, r1.ex)
            checkCompletedWithWrappedException(h2, r2.ex)
            checkCompletedWithWrappedException(h3, r3.ex)
            r1.assertinvoked()
            r2.assertinvoked()
            r3.assertinvoked()
            checkCompletedNormally(f, v1)
            checkCompletedNormally(g, v2)
          }
        }
      }
    }
  }

  /** thenAcceptBoth result completes normally after normal completion of sources
   */
  @Test def testThenAcceptBoth_normalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (fFirst <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          for (v2 <- Array[Item](two, null)) {
            val f = new CompletableFuture[Item]
            val g = new CompletableFuture[Item]
            val r1 = new SubtractAction(m)
            val r2 = new SubtractAction(m)
            val r3 = new SubtractAction(m)
            val fst =
              if (fFirst) f
              else g
            val snd =
              if (!fFirst) f
              else g
            val w1 =
              if (fFirst) v1
              else v2
            val w2 =
              if (!fFirst) v1
              else v2
            val h1 = m.thenAcceptBoth(f, g, r1)
            assertTrue(fst.complete(w1))
            val h2 = m.thenAcceptBoth(f, g, r2)
            checkIncomplete(h1)
            checkIncomplete(h2)
            r1.assertNotinvoked()
            r2.assertNotinvoked()
            assertTrue(snd.complete(w2))
            val h3 = m.thenAcceptBoth(f, g, r3)
            checkCompletedNormally(h1, null)
            checkCompletedNormally(h2, null)
            checkCompletedNormally(h3, null)
            r1.assertValue(subtract(v1, v2))
            r2.assertValue(subtract(v1, v2))
            r3.assertValue(subtract(v1, v2))
            checkCompletedNormally(f, v1)
            checkCompletedNormally(g, v2)
          }
        }
      }
    }
  }

  /** thenAcceptBoth result completes exceptionally after exceptional completion of either source
   */
  @throws[Throwable]
  @Test def testThenAcceptBoth_exceptionalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (fFirst <- Array[Boolean](true, false)) {
        for (failFirst <- Array[Boolean](true, false)) {
          for (v1 <- Array[Item](one, null)) {
            val f = new CompletableFuture[Item]
            val g = new CompletableFuture[Item]
            val ex = new CFException
            val r1 = new SubtractAction(m)
            val r2 = new SubtractAction(m)
            val r3 = new SubtractAction(m)
            val fst =
              if (fFirst) f
              else g
            val snd =
              if (!fFirst) f
              else g
            val complete1: Callable[Boolean] =
              if (failFirst) () => fst.completeExceptionally(ex)
              else () => fst.complete(v1)
            val complete2: Callable[Boolean] =
              if (failFirst) () => snd.complete(v1)
              else () => snd.completeExceptionally(ex)
            val h1 = m.thenAcceptBoth(f, g, r1)
            assertTrue(complete1.call())
            val h2 = m.thenAcceptBoth(f, g, r2)
            checkIncomplete(h1)
            checkIncomplete(h2)
            assertTrue(complete2.call())
            val h3 = m.thenAcceptBoth(f, g, r3)
            checkCompletedWithWrappedException(h1, ex)
            checkCompletedWithWrappedException(h2, ex)
            checkCompletedWithWrappedException(h3, ex)
            r1.assertNotinvoked()
            r2.assertNotinvoked()
            r3.assertNotinvoked()
            checkCompletedNormally(
              if (failFirst) snd
              else fst,
              v1
            )
            checkCompletedExceptionally(
              if (failFirst) fst
              else snd,
              ex
            )
          }
        }
      }
    }
  }

  /** thenAcceptBoth result completes exceptionally if either source cancelled
   */
  @throws[Throwable]
  @Test def testThenAcceptBoth_sourceCancelled(): Unit = {
    for (m <- ExecutionMode.values) {
      for (mayInterruptIfRunning <- Array[Boolean](true, false)) {
        for (fFirst <- Array[Boolean](true, false)) {
          for (failFirst <- Array[Boolean](true, false)) {
            for (v1 <- Array[Item](one, null)) {
              val f = new CompletableFuture[Item]
              val g = new CompletableFuture[Item]
              val r1 = new SubtractAction(m)
              val r2 = new SubtractAction(m)
              val r3 = new SubtractAction(m)
              val fst =
                if (fFirst) f
                else g
              val snd =
                if (!fFirst) f
                else g
              val complete1: Callable[Boolean] =
                if (failFirst) () => fst.cancel(mayInterruptIfRunning)
                else () => fst.complete(v1)
              val complete2: Callable[Boolean] =
                if (failFirst) () => snd.complete(v1)
                else () => snd.cancel(mayInterruptIfRunning)
              val h1 = m.thenAcceptBoth(f, g, r1)
              assertTrue(complete1.call())
              val h2 = m.thenAcceptBoth(f, g, r2)
              checkIncomplete(h1)
              checkIncomplete(h2)
              assertTrue(complete2.call())
              val h3 = m.thenAcceptBoth(f, g, r3)
              checkCompletedWithWrappedCancellationException(h1)
              checkCompletedWithWrappedCancellationException(h2)
              checkCompletedWithWrappedCancellationException(h3)
              r1.assertNotinvoked()
              r2.assertNotinvoked()
              r3.assertNotinvoked()
              checkCompletedNormally(
                if (failFirst) snd
                else fst,
                v1
              )
              checkCancelled(
                if (failFirst) fst
                else snd
              )
            }
          }
        }
      }
    }
  }

  /** thenAcceptBoth result completes exceptionally if action does
   */
  @Test def testThenAcceptBoth_actionFailed(): Unit = {
    for (m <- ExecutionMode.values) {
      for (fFirst <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          for (v2 <- Array[Item](two, null)) {
            val f = new CompletableFuture[Item]
            val g = new CompletableFuture[Item]
            val r1 = new FailingBiConsumer(m)
            val r2 = new FailingBiConsumer(m)
            val r3 = new FailingBiConsumer(m)
            val fst =
              if (fFirst) f
              else g
            val snd =
              if (!fFirst) f
              else g
            val w1 =
              if (fFirst) v1
              else v2
            val w2 =
              if (!fFirst) v1
              else v2
            val h1 = m.thenAcceptBoth(f, g, r1)
            assertTrue(fst.complete(w1))
            val h2 = m.thenAcceptBoth(f, g, r2)
            assertTrue(snd.complete(w2))
            val h3 = m.thenAcceptBoth(f, g, r3)
            checkCompletedWithWrappedException(h1, r1.ex)
            checkCompletedWithWrappedException(h2, r2.ex)
            checkCompletedWithWrappedException(h3, r3.ex)
            r1.assertinvoked()
            r2.assertinvoked()
            r3.assertinvoked()
            checkCompletedNormally(f, v1)
            checkCompletedNormally(g, v2)
          }
        }
      }
    }
  }

  /** runAfterBoth result completes normally after normal completion of sources
   */
  @Test def testRunAfterBoth_normalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (fFirst <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          for (v2 <- Array[Item](two, null)) {
            val f = new CompletableFuture[Item]
            val g = new CompletableFuture[Item]
            val r1 = new Noop(m)
            val r2 = new Noop(m)
            val r3 = new Noop(m)
            val fst =
              if (fFirst) f
              else g
            val snd =
              if (!fFirst) f
              else g
            val w1 =
              if (fFirst) v1
              else v2
            val w2 =
              if (!fFirst) v1
              else v2
            val h1 = m.runAfterBoth(f, g, r1)
            assertTrue(fst.complete(w1))
            val h2 = m.runAfterBoth(f, g, r2)
            checkIncomplete(h1)
            checkIncomplete(h2)
            r1.assertNotinvoked()
            r2.assertNotinvoked()
            assertTrue(snd.complete(w2))
            val h3 = m.runAfterBoth(f, g, r3)
            checkCompletedNormally(h1, null)
            checkCompletedNormally(h2, null)
            checkCompletedNormally(h3, null)
            r1.assertinvoked()
            r2.assertinvoked()
            r3.assertinvoked()
            checkCompletedNormally(f, v1)
            checkCompletedNormally(g, v2)
          }
        }
      }
    }
  }

  /** runAfterBoth result completes exceptionally after exceptional completion of either source
   */
  @throws[Throwable]
  @Test def testRunAfterBoth_exceptionalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (fFirst <- Array[Boolean](true, false)) {
        for (failFirst <- Array[Boolean](true, false)) {
          for (v1 <- Array[Item](one, null)) {
            val f = new CompletableFuture[Item]
            val g = new CompletableFuture[Item]
            val ex = new CFException
            val r1 = new Noop(m)
            val r2 = new Noop(m)
            val r3 = new Noop(m)
            val fst =
              if (fFirst) f
              else g
            val snd =
              if (!fFirst) f
              else g
            val complete1: Callable[Boolean] =
              if (failFirst) () => fst.completeExceptionally(ex)
              else () => fst.complete(v1)
            val complete2: Callable[Boolean] =
              if (failFirst) () => snd.complete(v1)
              else () => snd.completeExceptionally(ex)
            val h1 = m.runAfterBoth(f, g, r1)
            assertTrue(complete1.call())
            val h2 = m.runAfterBoth(f, g, r2)
            checkIncomplete(h1)
            checkIncomplete(h2)
            assertTrue(complete2.call())
            val h3 = m.runAfterBoth(f, g, r3)
            checkCompletedWithWrappedException(h1, ex)
            checkCompletedWithWrappedException(h2, ex)
            checkCompletedWithWrappedException(h3, ex)
            r1.assertNotinvoked()
            r2.assertNotinvoked()
            r3.assertNotinvoked()
            checkCompletedNormally(
              if (failFirst) snd
              else fst,
              v1
            )
            checkCompletedExceptionally(
              if (failFirst) fst
              else snd,
              ex
            )
          }
        }
      }
    }
  }

  /** runAfterBoth result completes exceptionally if either source cancelled
   */
  @throws[Throwable]
  @Test def testRunAfterBoth_sourceCancelled(): Unit = {
    for (m <- ExecutionMode.values) {
      for (mayInterruptIfRunning <- Array[Boolean](true, false)) {
        for (fFirst <- Array[Boolean](true, false)) {
          for (failFirst <- Array[Boolean](true, false)) {
            for (v1 <- Array[Item](one, null)) {
              val f = new CompletableFuture[Item]
              val g = new CompletableFuture[Item]
              val r1 = new Noop(m)
              val r2 = new Noop(m)
              val r3 = new Noop(m)
              val fst =
                if (fFirst) f
                else g
              val snd =
                if (!fFirst) f
                else g
              val complete1: Callable[Boolean] =
                if (failFirst) () => fst.cancel(mayInterruptIfRunning)
                else () => fst.complete(v1)
              val complete2: Callable[Boolean] =
                if (failFirst) () => snd.complete(v1)
                else () => snd.cancel(mayInterruptIfRunning)
              val h1 = m.runAfterBoth(f, g, r1)
              assertTrue(complete1.call())
              val h2 = m.runAfterBoth(f, g, r2)
              checkIncomplete(h1)
              checkIncomplete(h2)
              assertTrue(complete2.call())
              val h3 = m.runAfterBoth(f, g, r3)
              checkCompletedWithWrappedCancellationException(h1)
              checkCompletedWithWrappedCancellationException(h2)
              checkCompletedWithWrappedCancellationException(h3)
              r1.assertNotinvoked()
              r2.assertNotinvoked()
              r3.assertNotinvoked()
              checkCompletedNormally(
                if (failFirst) snd
                else fst,
                v1
              )
              checkCancelled(
                if (failFirst) fst
                else snd
              )
            }
          }
        }
      }
    }
  }

  /** runAfterBoth result completes exceptionally if action does
   */
  @Test def testRunAfterBoth_actionFailed(): Unit = {
    for (m <- ExecutionMode.values) {
      for (fFirst <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          for (v2 <- Array[Item](two, null)) {
            val f = new CompletableFuture[Item]
            val g = new CompletableFuture[Item]
            val r1 = new FailingRunnable(m)
            val r2 = new FailingRunnable(m)
            val r3 = new FailingRunnable(m)
            val fst =
              if (fFirst) f
              else g
            val snd =
              if (!fFirst) f
              else g
            val w1 =
              if (fFirst) v1
              else v2
            val w2 =
              if (!fFirst) v1
              else v2
            val h1 = m.runAfterBoth(f, g, r1)
            assertTrue(fst.complete(w1))
            val h2 = m.runAfterBoth(f, g, r2)
            assertTrue(snd.complete(w2))
            val h3 = m.runAfterBoth(f, g, r3)
            checkCompletedWithWrappedException(h1, r1.ex)
            checkCompletedWithWrappedException(h2, r2.ex)
            checkCompletedWithWrappedException(h3, r3.ex)
            r1.assertinvoked()
            r2.assertinvoked()
            r3.assertinvoked()
            checkCompletedNormally(f, v1)
            checkCompletedNormally(g, v2)
          }
        }
      }
    }
  }

  /** applyToEither result completes normally after normal completion of either source
   */
  @Test def testApplyToEither_normalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (v1 <- Array[Item](one, null)) {
        for (v2 <- Array[Item](two, null)) {
          val f = new CompletableFuture[Item]
          val g = new CompletableFuture[Item]
          val rs = new Array[IncFunction](6)
          for (i <- 0 until rs.length) {
            rs(i) = new IncFunction(m)
          }
          val h0 = m.applyToEither(f, g, rs(0))
          val h1 = m.applyToEither(g, f, rs(1))
          checkIncomplete(h0)
          checkIncomplete(h1)
          rs(0).assertNotinvoked()
          rs(1).assertNotinvoked()
          f.complete(v1)
          checkCompletedNormally(h0, inc(v1))
          checkCompletedNormally(h1, inc(v1))
          val h2 = m.applyToEither(f, g, rs(2))
          val h3 = m.applyToEither(g, f, rs(3))
          checkCompletedNormally(h2, inc(v1))
          checkCompletedNormally(h3, inc(v1))
          g.complete(v2)
          // unspecified behavior - both source completions available
          val h4 = m.applyToEither(f, g, rs(4))
          val h5 = m.applyToEither(g, f, rs(5))
          rs(4).assertValue(h4.join())
          rs(5).assertValue(h5.join())
          assertTrue(
            Objects.equals(inc(v1), h4.join()) || Objects.equals(
              inc(v2),
              h4.join()
            )
          )
          assertTrue(
            Objects.equals(inc(v1), h5.join()) || Objects.equals(
              inc(v2),
              h5.join()
            )
          )
          checkCompletedNormally(f, v1)
          checkCompletedNormally(g, v2)
          checkCompletedNormally(h0, inc(v1))
          checkCompletedNormally(h1, inc(v1))
          checkCompletedNormally(h2, inc(v1))
          checkCompletedNormally(h3, inc(v1))
          for (i <- 0 until 4) {
            rs(i).assertValue(inc(v1))
          }
        }
      }
    }
  }

  /** applyToEither result completes exceptionally after exceptional completion of either source
   */
  @Test def testApplyToEither_exceptionalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (v1 <- Array[Item](one, null)) {
        val f = new CompletableFuture[Item]
        val g = new CompletableFuture[Item]
        val ex = new CFException
        val rs = new Array[IncFunction](6)
        for (i <- 0 until rs.length) {
          rs(i) = new IncFunction(m)
        }
        val h0 = m.applyToEither(f, g, rs(0))
        val h1 = m.applyToEither(g, f, rs(1))
        checkIncomplete(h0)
        checkIncomplete(h1)
        rs(0).assertNotinvoked()
        rs(1).assertNotinvoked()
        f.completeExceptionally(ex)
        checkCompletedWithWrappedException(h0, ex)
        checkCompletedWithWrappedException(h1, ex)
        val h2 = m.applyToEither(f, g, rs(2))
        val h3 = m.applyToEither(g, f, rs(3))
        checkCompletedWithWrappedException(h2, ex)
        checkCompletedWithWrappedException(h3, ex)
        g.complete(v1)
        // unspecified behavior - both source completions available
        val h4 = m.applyToEither(f, g, rs(4))
        val h5 = m.applyToEither(g, f, rs(5))
        try {
          assertEquals(inc(v1), h4.join())
          rs(4).assertValue(inc(v1))
        } catch {
          case ok: CompletionException =>
            checkCompletedWithWrappedException(h4, ex)
            rs(4).assertNotinvoked()
        }
        try {
          assertEquals(inc(v1), h5.join())
          rs(5).assertValue(inc(v1))
        } catch {
          case ok: CompletionException =>
            checkCompletedWithWrappedException(h5, ex)
            rs(5).assertNotinvoked()
        }
        checkCompletedExceptionally(f, ex)
        checkCompletedNormally(g, v1)
        checkCompletedWithWrappedException(h0, ex)
        checkCompletedWithWrappedException(h1, ex)
        checkCompletedWithWrappedException(h2, ex)
        checkCompletedWithWrappedException(h3, ex)
        checkCompletedWithWrappedException(h4, ex)
        for (i <- 0 until 4) {
          rs(i).assertNotinvoked()
        }
      }
    }
  }

  @Test def testApplyToEither_exceptionalCompletion2(): Unit = {
    for (m <- ExecutionMode.values) {
      for (fFirst <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          val f = new CompletableFuture[Item]
          val g = new CompletableFuture[Item]
          val ex = new CFException
          val rs = new Array[IncFunction](6)
          for (i <- 0 until rs.length) {
            rs(i) = new IncFunction(m)
          }
          val h0 = m.applyToEither(f, g, rs(0))
          val h1 = m.applyToEither(g, f, rs(1))
          assertTrue(
            if (fFirst) f.complete(v1)
            else g.completeExceptionally(ex)
          )
          assertTrue(
            if (!fFirst) f.complete(v1)
            else g.completeExceptionally(ex)
          )
          val h2 = m.applyToEither(f, g, rs(2))
          val h3 = m.applyToEither(g, f, rs(3))
          // unspecified behavior - both source completions available
          try {
            assertEquals(inc(v1), h0.join())
            rs(0).assertValue(inc(v1))
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedException(h0, ex)
              rs(0).assertNotinvoked()
          }
          try {
            assertEquals(inc(v1), h1.join())
            rs(1).assertValue(inc(v1))
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedException(h1, ex)
              rs(1).assertNotinvoked()
          }
          try {
            assertEquals(inc(v1), h2.join())
            rs(2).assertValue(inc(v1))
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedException(h2, ex)
              rs(2).assertNotinvoked()
          }
          try {
            assertEquals(inc(v1), h3.join())
            rs(3).assertValue(inc(v1))
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedException(h3, ex)
              rs(3).assertNotinvoked()
          }
          checkCompletedNormally(f, v1)
          checkCompletedExceptionally(g, ex)
        }
      }
    }
  }

  /** applyToEither result completes exceptionally if either source cancelled
   */
  @Test def testApplyToEither_sourceCancelled(): Unit = {
    for (m <- ExecutionMode.values) {
      for (mayInterruptIfRunning <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          val f = new CompletableFuture[Item]
          val g = new CompletableFuture[Item]
          val rs = new Array[IncFunction](6)
          for (i <- 0 until rs.length) {
            rs(i) = new IncFunction(m)
          }
          val h0 = m.applyToEither(f, g, rs(0))
          val h1 = m.applyToEither(g, f, rs(1))
          checkIncomplete(h0)
          checkIncomplete(h1)
          rs(0).assertNotinvoked()
          rs(1).assertNotinvoked()
          f.cancel(mayInterruptIfRunning)
          checkCompletedWithWrappedCancellationException(h0)
          checkCompletedWithWrappedCancellationException(h1)
          val h2 = m.applyToEither(f, g, rs(2))
          val h3 = m.applyToEither(g, f, rs(3))
          checkCompletedWithWrappedCancellationException(h2)
          checkCompletedWithWrappedCancellationException(h3)
          g.complete(v1)
          // unspecified behavior - both source completions available
          val h4 = m.applyToEither(f, g, rs(4))
          val h5 = m.applyToEither(g, f, rs(5))
          try {
            assertEquals(inc(v1), h4.join())
            rs(4).assertValue(inc(v1))
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedCancellationException(h4)
              rs(4).assertNotinvoked()
          }
          try {
            assertEquals(inc(v1), h5.join())
            rs(5).assertValue(inc(v1))
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedCancellationException(h5)
              rs(5).assertNotinvoked()
          }
          checkCancelled(f)
          checkCompletedNormally(g, v1)
          checkCompletedWithWrappedCancellationException(h0)
          checkCompletedWithWrappedCancellationException(h1)
          checkCompletedWithWrappedCancellationException(h2)
          checkCompletedWithWrappedCancellationException(h3)
          for (i <- 0 until 4) {
            rs(i).assertNotinvoked()
          }
        }
      }
    }
  }

  @Test def testApplyToEither_sourceCancelled2(): Unit = {
    for (m <- ExecutionMode.values) {
      for (mayInterruptIfRunning <- Array[Boolean](true, false)) {
        for (fFirst <- Array[Boolean](true, false)) {
          for (v1 <- Array[Item](one, null)) {
            val f = new CompletableFuture[Item]
            val g = new CompletableFuture[Item]
            val rs = new Array[IncFunction](6)
            for (i <- 0 until rs.length) {
              rs(i) = new IncFunction(m)
            }
            val h0 = m.applyToEither(f, g, rs(0))
            val h1 = m.applyToEither(g, f, rs(1))
            assertTrue(
              if (fFirst) f.complete(v1)
              else g.cancel(mayInterruptIfRunning)
            )
            assertTrue(
              if (!fFirst) f.complete(v1)
              else g.cancel(mayInterruptIfRunning)
            )
            val h2 = m.applyToEither(f, g, rs(2))
            val h3 = m.applyToEither(g, f, rs(3))
            // unspecified behavior - both source completions available
            try {
              assertEquals(inc(v1), h0.join())
              rs(0).assertValue(inc(v1))
            } catch {
              case ok: CompletionException =>
                checkCompletedWithWrappedCancellationException(h0)
                rs(0).assertNotinvoked()
            }
            try {
              assertEquals(inc(v1), h1.join())
              rs(1).assertValue(inc(v1))
            } catch {
              case ok: CompletionException =>
                checkCompletedWithWrappedCancellationException(h1)
                rs(1).assertNotinvoked()
            }
            try {
              assertEquals(inc(v1), h2.join())
              rs(2).assertValue(inc(v1))
            } catch {
              case ok: CompletionException =>
                checkCompletedWithWrappedCancellationException(h2)
                rs(2).assertNotinvoked()
            }
            try {
              assertEquals(inc(v1), h3.join())
              rs(3).assertValue(inc(v1))
            } catch {
              case ok: CompletionException =>
                checkCompletedWithWrappedCancellationException(h3)
                rs(3).assertNotinvoked()
            }
            checkCompletedNormally(f, v1)
            checkCancelled(g)
          }
        }
      }
    }
  }

  /** applyToEither result completes exceptionally if action does
   */
  @Test def testApplyToEither_actionFailed(): Unit = {
    for (m <- ExecutionMode.values) {
      for (v1 <- Array[Item](one, null)) {
        for (v2 <- Array[Item](two, null)) {
          val f = new CompletableFuture[Item]
          val g = new CompletableFuture[Item]
          val rs = new Array[FailingFunction](6)
          for (i <- 0 until rs.length) {
            rs(i) = new FailingFunction(m)
          }
          val h0 = m.applyToEither(f, g, rs(0))
          val h1 = m.applyToEither(g, f, rs(1))
          f.complete(v1)
          val h2 = m.applyToEither(f, g, rs(2))
          val h3 = m.applyToEither(g, f, rs(3))
          checkCompletedWithWrappedException(h0, rs(0).ex)
          checkCompletedWithWrappedException(h1, rs(1).ex)
          checkCompletedWithWrappedException(h2, rs(2).ex)
          checkCompletedWithWrappedException(h3, rs(3).ex)
          for (i <- 0 until 4) {
            rs(i).assertValue(v1)
          }
          g.complete(v2)
          // unspecified behavior - both source completions available
          val h4 = m.applyToEither(f, g, rs(4))
          val h5 = m.applyToEither(g, f, rs(5))
          checkCompletedWithWrappedException(h4, rs(4).ex)
          assertTrue(Objects.equals(v1, rs(4).value) || Objects.equals(v2, rs(4).value))
          checkCompletedWithWrappedException(h5, rs(5).ex)
          assertTrue(Objects.equals(v1, rs(5).value) || Objects.equals(v2, rs(5).value))
          checkCompletedNormally(f, v1)
          checkCompletedNormally(g, v2)
        }
      }
    }
  }

  /** acceptEither result completes normally after normal completion of either source
   */
  @Test def testAcceptEither_normalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (v1 <- Array[Item](one, null)) {
        for (v2 <- Array[Item](two, null)) {
          val f = new CompletableFuture[Item]
          val g = new CompletableFuture[Item]
          val rs = new Array[NoopConsumer](6)
          for (i <- 0 until rs.length) {
            rs(i) = new NoopConsumer(m)
          }
          val h0 = m.acceptEither(f, g, rs(0))
          val h1 = m.acceptEither(g, f, rs(1))
          checkIncomplete(h0)
          checkIncomplete(h1)
          rs(0).assertNotinvoked()
          rs(1).assertNotinvoked()
          f.complete(v1)
          checkCompletedNormally(h0, null)
          checkCompletedNormally(h1, null)
          rs(0).assertValue(v1)
          rs(1).assertValue(v1)
          val h2 = m.acceptEither(f, g, rs(2))
          val h3 = m.acceptEither(g, f, rs(3))
          checkCompletedNormally(h2, null)
          checkCompletedNormally(h3, null)
          rs(2).assertValue(v1)
          rs(3).assertValue(v1)
          g.complete(v2)
          // unspecified behavior - both source completions available
          val h4 = m.acceptEither(f, g, rs(4))
          val h5 = m.acceptEither(g, f, rs(5))
          checkCompletedNormally(h4, null)
          checkCompletedNormally(h5, null)
          assertTrue(Objects.equals(v1, rs(4).value) || Objects.equals(v2, rs(4).value))
          assertTrue(Objects.equals(v1, rs(5).value) || Objects.equals(v2, rs(5).value))
          checkCompletedNormally(f, v1)
          checkCompletedNormally(g, v2)
          checkCompletedNormally(h0, null)
          checkCompletedNormally(h1, null)
          checkCompletedNormally(h2, null)
          checkCompletedNormally(h3, null)
          for (i <- 0 until 4) {
            rs(i).assertValue(v1)
          }
        }
      }
    }
  }

  /** acceptEither result completes exceptionally after exceptional completion of either source
   */
  @Test def testAcceptEither_exceptionalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (v1 <- Array[Item](one, null)) {
        val f = new CompletableFuture[Item]
        val g = new CompletableFuture[Item]
        val ex = new CFException
        val rs = new Array[NoopConsumer](6)
        for (i <- 0 until rs.length) {
          rs(i) = new NoopConsumer(m)
        }
        val h0 = m.acceptEither(f, g, rs(0))
        val h1 = m.acceptEither(g, f, rs(1))
        checkIncomplete(h0)
        checkIncomplete(h1)
        rs(0).assertNotinvoked()
        rs(1).assertNotinvoked()
        f.completeExceptionally(ex)
        checkCompletedWithWrappedException(h0, ex)
        checkCompletedWithWrappedException(h1, ex)
        val h2 = m.acceptEither(f, g, rs(2))
        val h3 = m.acceptEither(g, f, rs(3))
        checkCompletedWithWrappedException(h2, ex)
        checkCompletedWithWrappedException(h3, ex)
        g.complete(v1)
        // unspecified behavior - both source completions available
        val h4 = m.acceptEither(f, g, rs(4))
        val h5 = m.acceptEither(g, f, rs(5))
        try {
          assertNull(h4.join())
          rs(4).assertValue(v1)
        } catch {
          case ok: CompletionException =>
            checkCompletedWithWrappedException(h4, ex)
            rs(4).assertNotinvoked()
        }
        try {
          assertNull(h5.join())
          rs(5).assertValue(v1)
        } catch {
          case ok: CompletionException =>
            checkCompletedWithWrappedException(h5, ex)
            rs(5).assertNotinvoked()
        }
        checkCompletedExceptionally(f, ex)
        checkCompletedNormally(g, v1)
        checkCompletedWithWrappedException(h0, ex)
        checkCompletedWithWrappedException(h1, ex)
        checkCompletedWithWrappedException(h2, ex)
        checkCompletedWithWrappedException(h3, ex)
        checkCompletedWithWrappedException(h4, ex)
        for (i <- 0 until 4) {
          rs(i).assertNotinvoked()
        }
      }
    }
  }

  @Test def testAcceptEither_exceptionalCompletion2(): Unit = {
    for (m <- ExecutionMode.values) {
      for (fFirst <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          val f = new CompletableFuture[Item]
          val g = new CompletableFuture[Item]
          val ex = new CFException
          val rs = new Array[NoopConsumer](6)
          for (i <- 0 until rs.length) {
            rs(i) = new NoopConsumer(m)
          }
          val h0 = m.acceptEither(f, g, rs(0))
          val h1 = m.acceptEither(g, f, rs(1))
          assertTrue(
            if (fFirst) f.complete(v1)
            else g.completeExceptionally(ex)
          )
          assertTrue(
            if (!fFirst) f.complete(v1)
            else g.completeExceptionally(ex)
          )
          val h2 = m.acceptEither(f, g, rs(2))
          val h3 = m.acceptEither(g, f, rs(3))
          // unspecified behavior - both source completions available
          try {
            assertNull(h0.join())
            rs(0).assertValue(v1)
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedException(h0, ex)
              rs(0).assertNotinvoked()
          }
          try {
            assertNull(h1.join())
            rs(1).assertValue(v1)
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedException(h1, ex)
              rs(1).assertNotinvoked()
          }
          try {
            assertNull(h2.join())
            rs(2).assertValue(v1)
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedException(h2, ex)
              rs(2).assertNotinvoked()
          }
          try {
            assertNull(h3.join())
            rs(3).assertValue(v1)
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedException(h3, ex)
              rs(3).assertNotinvoked()
          }
          checkCompletedNormally(f, v1)
          checkCompletedExceptionally(g, ex)
        }
      }
    }
  }

  /** acceptEither result completes exceptionally if either source cancelled
   */
  @Test def testAcceptEither_sourceCancelled(): Unit = {
    for (m <- ExecutionMode.values) {
      for (mayInterruptIfRunning <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          val f = new CompletableFuture[Item]
          val g = new CompletableFuture[Item]
          val rs = new Array[NoopConsumer](6)
          for (i <- 0 until rs.length) {
            rs(i) = new NoopConsumer(m)
          }
          val h0 = m.acceptEither(f, g, rs(0))
          val h1 = m.acceptEither(g, f, rs(1))
          checkIncomplete(h0)
          checkIncomplete(h1)
          rs(0).assertNotinvoked()
          rs(1).assertNotinvoked()
          f.cancel(mayInterruptIfRunning)
          checkCompletedWithWrappedCancellationException(h0)
          checkCompletedWithWrappedCancellationException(h1)
          val h2 = m.acceptEither(f, g, rs(2))
          val h3 = m.acceptEither(g, f, rs(3))
          checkCompletedWithWrappedCancellationException(h2)
          checkCompletedWithWrappedCancellationException(h3)
          g.complete(v1)
          // unspecified behavior - both source completions available
          val h4 = m.acceptEither(f, g, rs(4))
          val h5 = m.acceptEither(g, f, rs(5))
          try {
            assertNull(h4.join())
            rs(4).assertValue(v1)
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedCancellationException(h4)
              rs(4).assertNotinvoked()
          }
          try {
            assertNull(h5.join())
            rs(5).assertValue(v1)
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedCancellationException(h5)
              rs(5).assertNotinvoked()
          }
          checkCancelled(f)
          checkCompletedNormally(g, v1)
          checkCompletedWithWrappedCancellationException(h0)
          checkCompletedWithWrappedCancellationException(h1)
          checkCompletedWithWrappedCancellationException(h2)
          checkCompletedWithWrappedCancellationException(h3)
          for (i <- 0 until 4) {
            rs(i).assertNotinvoked()
          }
        }
      }
    }
  }

  /** acceptEither result completes exceptionally if action does
   */
  @Test def testAcceptEither_actionFailed(): Unit = {
    for (m <- ExecutionMode.values) {
      for (v1 <- Array[Item](one, null)) {
        for (v2 <- Array[Item](two, null)) {
          val f = new CompletableFuture[Item]
          val g = new CompletableFuture[Item]
          val rs = new Array[FailingConsumer](6)
          for (i <- 0 until rs.length) {
            rs(i) = new FailingConsumer(m)
          }
          val h0 = m.acceptEither(f, g, rs(0))
          val h1 = m.acceptEither(g, f, rs(1))
          f.complete(v1)
          val h2 = m.acceptEither(f, g, rs(2))
          val h3 = m.acceptEither(g, f, rs(3))
          checkCompletedWithWrappedException(h0, rs(0).ex)
          checkCompletedWithWrappedException(h1, rs(1).ex)
          checkCompletedWithWrappedException(h2, rs(2).ex)
          checkCompletedWithWrappedException(h3, rs(3).ex)
          for (i <- 0 until 4) {
            rs(i).assertValue(v1)
          }
          g.complete(v2)
          // unspecified behavior - both source completions available
          val h4 = m.acceptEither(f, g, rs(4))
          val h5 = m.acceptEither(g, f, rs(5))
          checkCompletedWithWrappedException(h4, rs(4).ex)
          assertTrue(Objects.equals(v1, rs(4).value) || Objects.equals(v2, rs(4).value))
          checkCompletedWithWrappedException(h5, rs(5).ex)
          assertTrue(Objects.equals(v1, rs(5).value) || Objects.equals(v2, rs(5).value))
          checkCompletedNormally(f, v1)
          checkCompletedNormally(g, v2)
        }
      }
    }
  }

  /** runAfterEither result completes normally after normal completion of either source
   */
  @Test def testRunAfterEither_normalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (v1 <- Array[Item](one, null)) {
        for (v2 <- Array[Item](two, null)) {
          for (pushNop <- Array[Boolean](true, false)) {
            val f = new CompletableFuture[Item]
            val g = new CompletableFuture[Item]
            val rs = new Array[Noop](6)
            for (i <- 0 until rs.length) {
              rs(i) = new Noop(m)
            }
            val h0 = m.runAfterEither(f, g, rs(0))
            val h1 = m.runAfterEither(g, f, rs(1))
            checkIncomplete(h0)
            checkIncomplete(h1)
            rs(0).assertNotinvoked()
            rs(1).assertNotinvoked()
            if (pushNop) { // ad hoc test of intra-completion interference
              m.thenRun(f, () => {})
              m.thenRun(g, () => {})
            }
            f.complete(v1)
            checkCompletedNormally(h0, null)
            checkCompletedNormally(h1, null)
            rs(0).assertinvoked()
            rs(1).assertinvoked()
            val h2 = m.runAfterEither(f, g, rs(2))
            val h3 = m.runAfterEither(g, f, rs(3))
            checkCompletedNormally(h2, null)
            checkCompletedNormally(h3, null)
            rs(2).assertinvoked()
            rs(3).assertinvoked()
            g.complete(v2)
            val h4 = m.runAfterEither(f, g, rs(4))
            val h5 = m.runAfterEither(g, f, rs(5))
            checkCompletedNormally(f, v1)
            checkCompletedNormally(g, v2)
            checkCompletedNormally(h0, null)
            checkCompletedNormally(h1, null)
            checkCompletedNormally(h2, null)
            checkCompletedNormally(h3, null)
            checkCompletedNormally(h4, null)
            checkCompletedNormally(h5, null)
            for (i <- 0 until 6) {
              rs(i).assertinvoked()
            }
          }
        }
      }
    }
  }

  /** runAfterEither result completes exceptionally after exceptional completion of either source
   */
  @Test def testRunAfterEither_exceptionalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (v1 <- Array[Item](one, null)) {
        val f = new CompletableFuture[Item]
        val g = new CompletableFuture[Item]
        val ex = new CFException
        val rs = new Array[Noop](6)
        for (i <- 0 until rs.length) {
          rs(i) = new Noop(m)
        }
        val h0 = m.runAfterEither(f, g, rs(0))
        val h1 = m.runAfterEither(g, f, rs(1))
        checkIncomplete(h0)
        checkIncomplete(h1)
        rs(0).assertNotinvoked()
        rs(1).assertNotinvoked()
        assertTrue(f.completeExceptionally(ex))
        checkCompletedWithWrappedException(h0, ex)
        checkCompletedWithWrappedException(h1, ex)
        val h2 = m.runAfterEither(f, g, rs(2))
        val h3 = m.runAfterEither(g, f, rs(3))
        checkCompletedWithWrappedException(h2, ex)
        checkCompletedWithWrappedException(h3, ex)
        assertTrue(g.complete(v1))
        // unspecified behavior - both source completions available
        val h4 = m.runAfterEither(f, g, rs(4))
        val h5 = m.runAfterEither(g, f, rs(5))
        try {
          assertNull(h4.join())
          rs(4).assertinvoked()
        } catch {
          case ok: CompletionException =>
            checkCompletedWithWrappedException(h4, ex)
            rs(4).assertNotinvoked()
        }
        try {
          assertNull(h5.join())
          rs(5).assertinvoked()
        } catch {
          case ok: CompletionException =>
            checkCompletedWithWrappedException(h5, ex)
            rs(5).assertNotinvoked()
        }
        checkCompletedExceptionally(f, ex)
        checkCompletedNormally(g, v1)
        checkCompletedWithWrappedException(h0, ex)
        checkCompletedWithWrappedException(h1, ex)
        checkCompletedWithWrappedException(h2, ex)
        checkCompletedWithWrappedException(h3, ex)
        checkCompletedWithWrappedException(h4, ex)
        for (i <- 0 until 4) {
          rs(i).assertNotinvoked()
        }
      }
    }
  }

  @Test def testRunAfterEither_exceptionalCompletion2(): Unit = {
    for (m <- ExecutionMode.values) {
      for (fFirst <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          val f = new CompletableFuture[Item]
          val g = new CompletableFuture[Item]
          val ex = new CFException
          val rs = new Array[Noop](6)
          for (i <- 0 until rs.length) {
            rs(i) = new Noop(m)
          }
          val h0 = m.runAfterEither(f, g, rs(0))
          val h1 = m.runAfterEither(g, f, rs(1))
          assertTrue(
            if (fFirst) f.complete(v1)
            else g.completeExceptionally(ex)
          )
          assertTrue(
            if (!fFirst) f.complete(v1)
            else g.completeExceptionally(ex)
          )
          val h2 = m.runAfterEither(f, g, rs(2))
          val h3 = m.runAfterEither(g, f, rs(3))
          // unspecified behavior - both source completions available
          try {
            assertNull(h0.join())
            rs(0).assertinvoked()
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedException(h0, ex)
              rs(0).assertNotinvoked()
          }
          try {
            assertNull(h1.join())
            rs(1).assertinvoked()
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedException(h1, ex)
              rs(1).assertNotinvoked()
          }
          try {
            assertNull(h2.join())
            rs(2).assertinvoked()
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedException(h2, ex)
              rs(2).assertNotinvoked()
          }
          try {
            assertNull(h3.join())
            rs(3).assertinvoked()
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedException(h3, ex)
              rs(3).assertNotinvoked()
          }
          checkCompletedNormally(f, v1)
          checkCompletedExceptionally(g, ex)
        }
      }
    }
  }

  /** runAfterEither result completes exceptionally if either source cancelled
   */
  @Test def testRunAfterEither_sourceCancelled(): Unit = {
    for (m <- ExecutionMode.values) {
      for (mayInterruptIfRunning <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          val f = new CompletableFuture[Item]
          val g = new CompletableFuture[Item]
          val rs = new Array[Noop](6)
          for (i <- 0 until rs.length) {
            rs(i) = new Noop(m)
          }
          val h0 = m.runAfterEither(f, g, rs(0))
          val h1 = m.runAfterEither(g, f, rs(1))
          checkIncomplete(h0)
          checkIncomplete(h1)
          rs(0).assertNotinvoked()
          rs(1).assertNotinvoked()
          f.cancel(mayInterruptIfRunning)
          checkCompletedWithWrappedCancellationException(h0)
          checkCompletedWithWrappedCancellationException(h1)
          val h2 = m.runAfterEither(f, g, rs(2))
          val h3 = m.runAfterEither(g, f, rs(3))
          checkCompletedWithWrappedCancellationException(h2)
          checkCompletedWithWrappedCancellationException(h3)
          assertTrue(g.complete(v1))
          // unspecified behavior - both source completions available
          val h4 = m.runAfterEither(f, g, rs(4))
          val h5 = m.runAfterEither(g, f, rs(5))
          try {
            assertNull(h4.join())
            rs(4).assertinvoked()
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedCancellationException(h4)
              rs(4).assertNotinvoked()
          }
          try {
            assertNull(h5.join())
            rs(5).assertinvoked()
          } catch {
            case ok: CompletionException =>
              checkCompletedWithWrappedCancellationException(h5)
              rs(5).assertNotinvoked()
          }
          checkCancelled(f)
          checkCompletedNormally(g, v1)
          checkCompletedWithWrappedCancellationException(h0)
          checkCompletedWithWrappedCancellationException(h1)
          checkCompletedWithWrappedCancellationException(h2)
          checkCompletedWithWrappedCancellationException(h3)
          for (i <- 0 until 4) {
            rs(i).assertNotinvoked()
          }
        }
      }
    }
  }

  /** runAfterEither result completes exceptionally if action does
   */
  @Test def testRunAfterEither_actionFailed(): Unit = {
    for (m <- ExecutionMode.values) {
      for (v1 <- Array[Item](one, null)) {
        for (v2 <- Array[Item](two, null)) {
          val f = new CompletableFuture[Item]
          val g = new CompletableFuture[Item]
          val rs = new Array[FailingRunnable](6)
          for (i <- 0 until rs.length) {
            rs(i) = new FailingRunnable(m)
          }
          val h0 = m.runAfterEither(f, g, rs(0))
          val h1 = m.runAfterEither(g, f, rs(1))
          assertTrue(f.complete(v1))
          val h2 = m.runAfterEither(f, g, rs(2))
          val h3 = m.runAfterEither(g, f, rs(3))
          checkCompletedWithWrappedException(h0, rs(0).ex)
          checkCompletedWithWrappedException(h1, rs(1).ex)
          checkCompletedWithWrappedException(h2, rs(2).ex)
          checkCompletedWithWrappedException(h3, rs(3).ex)
          for (i <- 0 until 4) {
            rs(i).assertinvoked()
          }
          assertTrue(g.complete(v2))
          val h4 = m.runAfterEither(f, g, rs(4))
          val h5 = m.runAfterEither(g, f, rs(5))
          checkCompletedWithWrappedException(h4, rs(4).ex)
          checkCompletedWithWrappedException(h5, rs(5).ex)
          checkCompletedNormally(f, v1)
          checkCompletedNormally(g, v2)
          for (i <- 0 until 6) {
            rs(i).assertinvoked()
          }
        }
      }
    }
  }

  /** thenCompose result completes normally after normal completion of source
   */
  @Test def testThenCompose_normalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (createIncomplete <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          val f = new CompletableFuture[Item]
          val r = new CompletableFutureInc(m)
          if (!createIncomplete) assertTrue(f.complete(v1))
          val g = m.thenCompose(f, r)
          if (createIncomplete) assertTrue(f.complete(v1))
          checkCompletedNormally(g, inc(v1))
          checkCompletedNormally(f, v1)
          r.assertValue(v1)
        }
      }
    }
  }

  /** thenCompose result completes exceptionally after exceptional completion of source
   */
  @Test def testThenCompose_exceptionalCompletion(): Unit = {
    for (m <- ExecutionMode.values) {
      for (createIncomplete <- Array[Boolean](true, false)) {
        val ex = new CFException
        val r = new CompletableFutureInc(m)
        val f = new CompletableFuture[Item]
        if (!createIncomplete) f.completeExceptionally(ex)
        val g = m.thenCompose(f, r)
        if (createIncomplete) f.completeExceptionally(ex)
        checkCompletedWithWrappedException(g, ex)
        checkCompletedExceptionally(f, ex)
        r.assertNotinvoked()
      }
    }
  }

  /** thenCompose result completes exceptionally if action does
   */
  @Test def testThenCompose_actionFailed(): Unit = {
    for (m <- ExecutionMode.values) {
      for (createIncomplete <- Array[Boolean](true, false)) {
        for (v1 <- Array[Item](one, null)) {
          val f = new CompletableFuture[Item]
          val r = new FailingCompletableFutureFunction(m)
          if (!createIncomplete) assertTrue(f.complete(v1))
          val g = m.thenCompose(f, r)
          if (createIncomplete) assertTrue(f.complete(v1))
          checkCompletedWithWrappedException(g, r.ex)
          checkCompletedNormally(f, v1)
        }
      }
    }
  }

  /** thenCompose result completes exceptionally if source cancelled
   */
  @Test def testThenCompose_sourceCancelled(): Unit = {
    for (m <- ExecutionMode.values) {
      for (createIncomplete <- Array[Boolean](true, false)) {
        for (mayInterruptIfRunning <- Array[Boolean](true, false)) {
          val f = new CompletableFuture[Item]
          val r = new CompletableFutureInc(m)
          if (!createIncomplete) assertTrue(f.cancel(mayInterruptIfRunning))
          val g = m.thenCompose(f, r)
          if (createIncomplete) {
            checkIncomplete(g)
            assertTrue(f.cancel(mayInterruptIfRunning))
          }
          checkCompletedWithWrappedCancellationException(g)
          checkCancelled(f)
        }
      }
    }
  }

  /** thenCompose result completes exceptionally if the result of the action does
   */
  @Test def testThenCompose_actionReturnsFailingFuture(): Unit = {
    for (m <- ExecutionMode.values) {
      for (order <- 0 until 6) {
        for (v1 <- Array[Item](one, null)) {
          val ex = new CFException
          val f = new CompletableFuture[Item]
          val g = new CompletableFuture[Item]
          var h: CompletableFuture[Item] = null
          // Test all permutations of orders
          order match {
            case 0 =>
              assertTrue(f.complete(v1))
              assertTrue(g.completeExceptionally(ex))
              h = m.thenCompose(f, (x: Item) => g)

            case 1 =>
              assertTrue(f.complete(v1))
              h = m.thenCompose(f, (x: Item) => g)
              assertTrue(g.completeExceptionally(ex))

            case 2 =>
              assertTrue(g.completeExceptionally(ex))
              assertTrue(f.complete(v1))
              h = m.thenCompose(f, (x: Item) => g)

            case 3 =>
              assertTrue(g.completeExceptionally(ex))
              h = m.thenCompose(f, (x: Item) => g)
              assertTrue(f.complete(v1))

            case 4 =>
              h = m.thenCompose(f, (x: Item) => g)
              assertTrue(f.complete(v1))
              assertTrue(g.completeExceptionally(ex))

            case 5 =>
              h = m.thenCompose(f, (x: Item) => g)
              assertTrue(f.complete(v1))
              assertTrue(g.completeExceptionally(ex))

            case _ =>
              throw new AssertionError
          }
          checkCompletedExceptionally(g, ex)
          checkCompletedWithWrappedException(h, ex)
          checkCompletedNormally(f, v1)
        }
      }
    }
  }

  /** allOf(no component futures) returns a future completed normally with the value null
   */
  // other static methods
  @throws[Exception]
  @Test def testAllOf_empty(): Unit = {
    val f = CompletableFuture.allOf()
    checkCompletedNormally(f, null)
  }

  /** allOf returns a future completed normally with the value null when all components complete normally
   */
  @throws[Exception]
  @Test def testAllOf_normal(): Unit = {
    for (k <- 1 until 10) {
      @SuppressWarnings(Array("unchecked")) val fs =
        new Array[CompletableFuture[_ <: AnyRef]](k).asInstanceOf[Array[CompletableFuture[Item]]]
      for (i <- 0 until k) {
        fs(i) = new CompletableFuture[Item]
      }
      val f = CompletableFuture.allOf(fs: _*)
      for (i <- 0 until k) {
        checkIncomplete(f)
        checkIncomplete(CompletableFuture.allOf(fs: _*))
        fs(i).complete(one)
      }
      checkCompletedNormally(f, null)
      checkCompletedNormally(CompletableFuture.allOf(fs: _*), null)
    }
  }

  @throws[Exception]
  @Test def testAllOf_normal_backwards(): Unit = {
    for (k <- 1 until 10) {
      @SuppressWarnings(Array("unchecked")) val fs =
        new Array[CompletableFuture[_ <: AnyRef]](k).asInstanceOf[Array[CompletableFuture[Item]]]
      for (i <- 0 until k) {
        fs(i) = new CompletableFuture[Item]
      }
      val f = CompletableFuture.allOf(fs: _*)
      for (i <- k - 1 to 0 by -1) {
        checkIncomplete(f)
        checkIncomplete(CompletableFuture.allOf(fs: _*))
        fs(i).complete(one)
      }
      checkCompletedNormally(f, null)
      checkCompletedNormally(CompletableFuture.allOf(fs: _*), null)
    }
  }

  @throws[Exception]
  @Test def testAllOf_exceptional(): Unit = {
    for (k <- 1 until 10) {
      @SuppressWarnings(Array("unchecked")) val fs =
        new Array[CompletableFuture[_ <: AnyRef]](k).asInstanceOf[Array[CompletableFuture[Item]]]
      val ex = new CFException
      for (i <- 0 until k) {
        fs(i) = new CompletableFuture[Item]
      }
      val f = CompletableFuture.allOf(fs: _*)
      for (i <- 0 until k) {
        val I = itemFor(i)
        checkIncomplete(f)
        checkIncomplete(CompletableFuture.allOf(fs: _*))
        if (i != k / 2) {
          fs(i).complete(I)
          checkCompletedNormally(fs(i), I)
        } else {
          fs(i).completeExceptionally(ex)
          checkCompletedExceptionally(fs(i), ex)
        }
      }
      checkCompletedWithWrappedException(f, ex)
      checkCompletedWithWrappedException(CompletableFuture.allOf(fs: _*), ex)
    }
  }

  /** anyOf(no component futures) returns an incomplete future
   */
  @throws[Exception]
  @Test def testAnyOf_empty(): Unit = {
    for (v1 <- Array[Item](one, null)) {
      val f = CompletableFuture.anyOf()
      checkIncomplete(f)
      f.complete(v1)
      checkCompletedNormally(f, v1)
    }
  }

  /** anyOf returns a future completed normally with a value when a component future does
   */
  @throws[Exception]
  @Test def testAnyOf_normal(): Unit = {
    for (k <- 0 until 10) {
      @SuppressWarnings(Array("unchecked")) val fs =
        new Array[CompletableFuture[_ <: AnyRef]](k).asInstanceOf[Array[CompletableFuture[Item]]]
      for (i <- 0 until k) {
        fs(i) = new CompletableFuture[Item]
      }
      val f = CompletableFuture.anyOf(fs: _*)
      checkIncomplete(f)
      for (i <- 0 until k) {
        fs(i).complete(itemFor(i))
        checkCompletedNormally(f, zero: Item)
        val x = CompletableFuture.anyOf(fs: _*).join().asInstanceOf[Item]
        assertTrue(0 <= x.value && x.value <= i)
      }
    }
  }

  @throws[Exception]
  @Test def testAnyOf_normal_backwards(): Unit = {
    for (k <- 0 until 10) {
      @SuppressWarnings(Array("unchecked")) val fs =
        new Array[CompletableFuture[_ <: AnyRef]](k).asInstanceOf[Array[CompletableFuture[Item]]]
      for (i <- 0 until k) {
        fs(i) = new CompletableFuture[Item]
      }
      val f = CompletableFuture.anyOf(fs: _*)
      checkIncomplete(f)
      for (i <- k - 1 to 0 by -1) {
        fs(i).complete(itemFor(i))
        checkCompletedNormally(f, itemFor(k - 1))
        val x = CompletableFuture.anyOf(fs: _*).join().asInstanceOf[Item]
        assertTrue(i <= x.value && x.value <= k - 1)
      }
    }
  }

  /** anyOf result completes exceptionally when any component does.
   */
  @throws[Exception]
  @Test def testAnyOf_exceptional(): Unit = {
    for (k <- 0 until 10) {
      @SuppressWarnings(Array("unchecked")) val fs =
        new Array[CompletableFuture[_ <: AnyRef]](k).asInstanceOf[Array[CompletableFuture[Item]]]
      val exs = new Array[CFException](k)
      for (i <- 0 until k) {
        fs(i) = new CompletableFuture[Item]
        exs(i) = new CFException
      }
      val f = CompletableFuture.anyOf(fs: _*)
      checkIncomplete(f)
      for (i <- 0 until k) {
        fs(i).completeExceptionally(exs(i))
        checkCompletedWithWrappedException(f, exs(0))
        checkCompletedWithWrappedCFException(CompletableFuture.anyOf(fs: _*))
      }
    }
  }

  @throws[Exception]
  @Test def testAnyOf_exceptional_backwards(): Unit = {
    for (k <- 0 until 10) {
      @SuppressWarnings(Array("unchecked")) val fs =
        new Array[CompletableFuture[_ <: AnyRef]](k).asInstanceOf[Array[CompletableFuture[AnyRef]]]
      val exs = new Array[CFException](k)
      for (i <- 0 until k) {
        fs(i) = new CompletableFuture[AnyRef]
        exs(i) = new CFException
      }
      val f = CompletableFuture.anyOf(fs: _*)
      checkIncomplete(f)
      for (i <- k - 1 to 0 by -1) {
        fs(i).completeExceptionally(exs(i))
        checkCompletedWithWrappedException(f, exs(k - 1))
        checkCompletedWithWrappedCFException(CompletableFuture.anyOf(fs: _*))
      }
    }
  }

  /** Completion methods throw NullPointerException with null arguments
   */
  @SuppressWarnings(Array("FutureReturnValueIgnored")) @Test def testNPE(): Unit = {
    val f = new CompletableFuture[Item]
    val g = new CompletableFuture[Item]
    val nullFuture = null.asInstanceOf[CompletableFuture[Item]]
    val exec = new ThreadExecutor
    assertEachThrows(
      classOf[NullPointerException],
      () => CompletableFuture.supplyAsync(null),
      () => CompletableFuture.supplyAsync(null, exec),
      () =>
        CompletableFuture.supplyAsync(
          new ItemSupplier(ExecutionMode.SYNC, fortytwo),
          null
        ),
      () => CompletableFuture.runAsync(null),
      () => CompletableFuture.runAsync(null, exec),
      () => CompletableFuture.runAsync(() => {}, null),
      () => f.completeExceptionally(null),
      () => f.thenApply(null),
      () => f.thenApplyAsync(null),
      () => f.thenApplyAsync((x: Item) => x, null),
      () => f.thenApplyAsync(null, exec),
      () => f.thenAccept(null),
      () => f.thenAcceptAsync(null),
      () => f.thenAcceptAsync((x: Item) => {}, null),
      () => f.thenAcceptAsync(null, exec),
      () => f.thenRun(null),
      () => f.thenRunAsync(null),
      () => f.thenRunAsync(() => {}, null),
      () => f.thenRunAsync(null, exec),
      () => f.thenCombine(g, null),
      () => f.thenCombineAsync(g, null),
      () => f.thenCombineAsync(g, null, exec),
      () => f.thenCombine(nullFuture, (x: Item, y: Any) => x),
      () => f.thenCombineAsync(nullFuture, (x: Item, y: Any) => x),
      () => f.thenCombineAsync(nullFuture, (x: Item, y: Any) => x, exec),
      () => f.thenCombineAsync(g, (x: Item, y: Any) => x, null),
      () => f.thenAcceptBoth(g, null),
      () => f.thenAcceptBothAsync(g, null),
      () => f.thenAcceptBothAsync(g, null, exec),
      () => f.thenAcceptBoth(nullFuture, (x: Item, y: Item) => {}),
      () => f.thenAcceptBothAsync(nullFuture, (x: Item, y: Item) => {}),
      () => f.thenAcceptBothAsync(nullFuture, (x: Item, y: Item) => {}, exec),
      () => f.thenAcceptBothAsync(g, (x: Item, y: Item) => {}, null),
      () => f.runAfterBoth(g, null),
      () => f.runAfterBothAsync(g, null),
      () => f.runAfterBothAsync(g, null, exec),
      () => f.runAfterBoth(nullFuture, () => {}),
      () => f.runAfterBothAsync(nullFuture, () => {}),
      () => f.runAfterBothAsync(nullFuture, () => {}, exec),
      () => f.runAfterBothAsync(g, () => {}, null),
      () => f.applyToEither(g, null),
      () => f.applyToEitherAsync(g, null),
      () => f.applyToEitherAsync(g, null, exec),
      () => f.applyToEither(nullFuture, (x: Item) => x),
      () => f.applyToEitherAsync(nullFuture, (x: Item) => x),
      () => f.applyToEitherAsync(nullFuture, (x: Item) => x, exec),
      () => f.applyToEitherAsync(g, (x: Item) => x, null),
      () => f.acceptEither(g, null),
      () => f.acceptEitherAsync(g, null),
      () => f.acceptEitherAsync(g, null, exec),
      () => f.acceptEither(nullFuture, (x: Item) => {}),
      () => f.acceptEitherAsync(nullFuture, (x: Item) => {}),
      () => f.acceptEitherAsync(nullFuture, (x: Item) => {}, exec),
      () => f.acceptEitherAsync(g, (x: Item) => {}, null),
      () => f.runAfterEither(g, null),
      () => f.runAfterEitherAsync(g, null),
      () => f.runAfterEitherAsync(g, null, exec),
      () => f.runAfterEither(nullFuture, () => {}),
      () => f.runAfterEitherAsync(nullFuture, () => {}),
      () => f.runAfterEitherAsync(nullFuture, () => {}, exec),
      () => f.runAfterEitherAsync(g, () => {}, null),
      () => f.thenCompose(null),
      () => f.thenComposeAsync(null),
      () =>
        f.thenComposeAsync(
          new CompletableFutureInc(ExecutionMode.EXECUTOR),
          null
        ),
      () => f.thenComposeAsync(null, exec),
      () => f.exceptionally(null),
      () => f.handle(null),
      () => CompletableFuture.allOf(null.asInstanceOf[CompletableFuture[_ <: AnyRef]]),
      () => CompletableFuture.allOf(null.asInstanceOf[Array[CompletableFuture[_ <: AnyRef]]]: _*),
      () => CompletableFuture.allOf(f, null),
      () => CompletableFuture.allOf(null, f),
      () => CompletableFuture.anyOf(null.asInstanceOf[CompletableFuture[_ <: AnyRef]]),
      () => CompletableFuture.anyOf(null.asInstanceOf[Array[CompletableFuture[_ <: AnyRef]]]: _*),
      () => CompletableFuture.anyOf(f, null),
      () => CompletableFuture.anyOf(null, f),
      () => f.obtrudeException(null)
    )
    assertEquals(0, exec.count.get())
  }

  /** Test submissions to an executor that rejects all tasks.
   */
  @Test def testRejectingExecutor(): Unit = {
    for (v <- Array[Item](one, null)) {
      val e = new CountingRejectingExecutor
      val complete = CompletableFuture.completedFuture(v)
      val incomplete = new CompletableFuture[Item]
      val futures = new ArrayList[CompletableFuture[_ <: AnyRef]]
      val srcs = new ArrayList[CompletableFuture[Item]]
      srcs.add(complete)
      srcs.add(incomplete)
      srcs.forEach { src =>
        val fs = new ArrayList[CompletableFuture[_ <: AnyRef]]
        fs.add(src.thenRunAsync(() => {}, e))
        fs.add(src.thenAcceptAsync((z: Item) => {}, e))
        fs.add(src.thenApplyAsync((z: Item) => z, e))
        fs.add(src.thenCombineAsync(src, (x: Item, y: Item) => x, e))
        fs.add(src.thenAcceptBothAsync(src, (x: Item, y: Item) => {}, e))
        fs.add(src.runAfterBothAsync(src, () => {}, e))
        fs.add(src.applyToEitherAsync(src, (z: Item) => z, e))
        fs.add(src.acceptEitherAsync(src, (z: Item) => {}, e))
        fs.add(src.runAfterEitherAsync(src, () => {}, e))
        fs.add(src.thenComposeAsync((z: Item) => null, e))
        fs.add(src.whenCompleteAsync((z: Item, t: Throwable) => {}, e))
        fs.add(src.handleAsync((z: Item, t: Throwable) => null, e))
        fs.forEach { future =>
          if (src.isDone) checkCompletedWithWrappedException(future, e.ex)
          else checkIncomplete(future)
        }
        futures.addAll(fs)
      }

      locally {
        val fs = new ArrayList[CompletableFuture[_ <: AnyRef]]
        fs.add(complete.thenCombineAsync(incomplete, (x: Item, y: Item) => x, e))
        fs.add(incomplete.thenCombineAsync(complete, (x: Item, y: Item) => x, e))
        fs.add(complete.thenAcceptBothAsync(incomplete, (x: Item, y: Item) => {}, e))
        fs.add(incomplete.thenAcceptBothAsync(complete, (x: Item, y: Item) => {}, e))
        fs.add(complete.runAfterBothAsync(incomplete, () => {}, e))
        fs.add(incomplete.runAfterBothAsync(complete, () => {}, e))

        fs.forEach(checkIncomplete(_))
        futures.addAll(fs)
      }
      locally {
        val fs = new ArrayList[CompletableFuture[_ <: AnyRef]]
        fs.add(complete.applyToEitherAsync(incomplete, (z: Item) => z, e))
        fs.add(incomplete.applyToEitherAsync(complete, (z: Item) => z, e))
        fs.add(complete.acceptEitherAsync(incomplete, (z: Item) => {}, e))
        fs.add(incomplete.acceptEitherAsync(complete, (z: Item) => {}, e))
        fs.add(complete.runAfterEitherAsync(incomplete, () => {}, e))
        fs.add(incomplete.runAfterEitherAsync(complete, () => {}, e))
        fs.forEach(checkCompletedWithWrappedException(_, e.ex))
        futures.addAll(fs)
      }

      incomplete.complete(v)
      futures.forEach(checkCompletedWithWrappedException(_, e.ex))
      assertEquals(futures.size, e.count.get())
    }
  }

  /** Test submissions to an executor that rejects all tasks, but should never be invoked() because the dependent future
   *  is explicitly completed.
   */
  @Test def testRejectingExecutorNeverinvoked(): Unit = {
    for (v <- Array[Item](one, null)) {
      val e = new CountingRejectingExecutor
      val complete = CompletableFuture.completedFuture(v)
      val incomplete = new CompletableFuture[Item]
      val fs = new ArrayList[CompletableFuture[_ <: AnyRef]]
      fs.add(incomplete.thenRunAsync(() => {}, e))
      fs.add(incomplete.thenAcceptAsync((z: Item) => {}, e))
      fs.add(incomplete.thenApplyAsync((z: Item) => z, e))
      fs.add(incomplete.thenCombineAsync(incomplete, (x: Item, y: Item) => x, e))
      fs.add(incomplete.thenAcceptBothAsync(incomplete, (x: Item, y: Item) => {}, e))
      fs.add(incomplete.runAfterBothAsync(incomplete, () => {}, e))
      fs.add(incomplete.applyToEitherAsync(incomplete, (z: Item) => z, e))
      fs.add(incomplete.acceptEitherAsync(incomplete, (z: Item) => {}, e))
      fs.add(incomplete.runAfterEitherAsync(incomplete, () => {}, e))
      fs.add(incomplete.thenComposeAsync((z: Item) => null, e))
      fs.add(incomplete.whenCompleteAsync((z: Item, t: Throwable) => {}, e))
      fs.add(incomplete.handleAsync((z: Item, t: Throwable) => null, e))
      fs.add(complete.thenCombineAsync(incomplete, (x: Item, y: Item) => x, e))
      fs.add(incomplete.thenCombineAsync(complete, (x: Item, y: Item) => x, e))
      fs.add(complete.thenAcceptBothAsync(incomplete, (x: Item, y: Item) => {}, e))
      fs.add(incomplete.thenAcceptBothAsync(complete, (x: Item, y: Item) => {}, e))
      fs.add(complete.runAfterBothAsync(incomplete, () => {}, e))
      fs.add(incomplete.runAfterBothAsync(complete, () => {}, e))
      fs.forEach(checkIncomplete(_))
      fs.forEach(_.asInstanceOf[CompletableFuture[AnyRef]].complete(null.asInstanceOf[AnyRef]))
      incomplete.complete(v)

      fs.asInstanceOf[ArrayList[CompletableFuture[AnyRef]]].forEach(checkCompletedNormally(_, null))
      assertEquals(0, e.count.get())
    }
  }

  /** toCompletableFuture returns this CompletableFuture.
   */
  @Test def testToCompletableFuture(): Unit = {
    val f = new CompletableFuture[Item]
    assertSame(f, f.toCompletableFuture)
  }
}
