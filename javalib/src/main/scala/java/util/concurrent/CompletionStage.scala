/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util.function.{BiConsumer, BiFunction, Consumer, Function}

trait CompletionStage[T <: AnyRef] {
  def thenApply[U <: AnyRef](fn: Function[_ >: T, _ <: U]): CompletionStage[U]
  def thenApplyAsync[U <: AnyRef](
      fn: Function[_ >: T, _ <: U]
  ): CompletionStage[U]
  def thenApplyAsync[U <: AnyRef](
      fn: Function[_ >: T, _ <: U],
      executor: Executor
  ): CompletionStage[U]
  def thenAccept(action: Consumer[_ >: T]): CompletionStage[Void]
  def thenAcceptAsync(action: Consumer[_ >: T]): CompletionStage[Void]
  def thenAcceptAsync(
      action: Consumer[_ >: T],
      executor: Executor
  ): CompletionStage[Void]
  def thenRun(action: Runnable): CompletionStage[Void]
  def thenRunAsync(action: Runnable): CompletionStage[Void]
  def thenRunAsync(action: Runnable, executor: Executor): CompletionStage[Void]
  def thenCombine[U <: AnyRef, V <: AnyRef](
      other: CompletionStage[_ <: U],
      fn: BiFunction[_ >: T, _ >: U, _ <: V]
  ): CompletionStage[V]
  def thenCombineAsync[U <: AnyRef, V <: AnyRef](
      other: CompletionStage[_ <: U],
      fn: BiFunction[_ >: T, _ >: U, _ <: V]
  ): CompletionStage[V]
  def thenCombineAsync[U <: AnyRef, V <: AnyRef](
      other: CompletionStage[_ <: U],
      fn: BiFunction[_ >: T, _ >: U, _ <: V],
      executor: Executor
  ): CompletionStage[V]
  def thenAcceptBoth[U <: AnyRef](
      other: CompletionStage[_ <: U],
      action: BiConsumer[_ >: T, _ >: U]
  ): CompletionStage[Void]
  def thenAcceptBothAsync[U <: AnyRef](
      other: CompletionStage[_ <: U],
      action: BiConsumer[_ >: T, _ >: U]
  ): CompletionStage[Void]
  def thenAcceptBothAsync[U <: AnyRef](
      other: CompletionStage[_ <: U],
      action: BiConsumer[_ >: T, _ >: U],
      executor: Executor
  ): CompletionStage[Void]
  def runAfterBoth(
      other: CompletionStage[_ <: AnyRef],
      action: Runnable
  ): CompletionStage[Void]
  def runAfterBothAsync(
      other: CompletionStage[_ <: AnyRef],
      action: Runnable
  ): CompletionStage[Void]
  def runAfterBothAsync(
      other: CompletionStage[_ <: AnyRef],
      action: Runnable,
      executor: Executor
  ): CompletionStage[Void]
  def applyToEither[U <: AnyRef](
      other: CompletionStage[_ <: T],
      fn: Function[_ >: T, U]
  ): CompletionStage[U]
  def applyToEitherAsync[U <: AnyRef](
      other: CompletionStage[_ <: T],
      fn: Function[_ >: T, U]
  ): CompletionStage[U]
  def applyToEitherAsync[U <: AnyRef](
      other: CompletionStage[_ <: T],
      fn: Function[_ >: T, U],
      executor: Executor
  ): CompletionStage[U]
  def acceptEither(
      other: CompletionStage[_ <: T],
      action: Consumer[_ >: T]
  ): CompletionStage[Void]
  def acceptEitherAsync(
      other: CompletionStage[_ <: T],
      action: Consumer[_ >: T]
  ): CompletionStage[Void]
  def acceptEitherAsync(
      other: CompletionStage[_ <: T],
      action: Consumer[_ >: T],
      executor: Executor
  ): CompletionStage[Void]
  def runAfterEither(
      other: CompletionStage[_ <: AnyRef],
      action: Runnable
  ): CompletionStage[Void]
  def runAfterEitherAsync(
      other: CompletionStage[_ <: AnyRef],
      action: Runnable
  ): CompletionStage[Void]
  def runAfterEitherAsync(
      other: CompletionStage[_ <: AnyRef],
      action: Runnable,
      executor: Executor
  ): CompletionStage[Void]
  def thenCompose[U <: AnyRef](
      fn: Function[_ >: T, _ <: CompletionStage[U]]
  ): CompletionStage[U]
  def thenComposeAsync[U <: AnyRef](
      fn: Function[_ >: T, _ <: CompletionStage[U]]
  ): CompletionStage[U]
  def thenComposeAsync[U <: AnyRef](
      fn: Function[_ >: T, _ <: CompletionStage[U]],
      executor: Executor
  ): CompletionStage[U]
  def handle[U <: AnyRef](
      fn: BiFunction[_ >: T, Throwable, _ <: U]
  ): CompletionStage[U]
  def handleAsync[U <: AnyRef](
      fn: BiFunction[_ >: T, Throwable, _ <: U]
  ): CompletionStage[U]
  def handleAsync[U <: AnyRef](
      fn: BiFunction[_ >: T, Throwable, _ <: U],
      executor: Executor
  ): CompletionStage[U]
  def whenComplete(
      action: BiConsumer[_ >: T, _ >: Throwable]
  ): CompletionStage[T]
  def whenCompleteAsync(
      action: BiConsumer[_ >: T, _ >: Throwable]
  ): CompletionStage[T]
  def whenCompleteAsync(
      action: BiConsumer[_ >: T, _ >: Throwable],
      executor: Executor
  ): CompletionStage[T]
  def exceptionally(fn: Function[Throwable, _ <: T]): CompletionStage[T]
  def exceptionallyAsync(
      fn: Function[Throwable, _ <: T]
  ): CompletionStage[T] = {
    handle[CompletionStage[T]] { (r: T, ex: Throwable) =>
      if (ex == null) this
      else this.handleAsync { (r1: T, ex1: Throwable) => fn.apply(ex1) }
    }.thenCompose(Function.identity())
  }

  def exceptionallyAsync(
      fn: Function[Throwable, _ <: T],
      executor: Executor
  ): CompletionStage[T] = handle[CompletionStage[T]] { (r: T, ex: Throwable) =>
    if (ex == null) this
    else
      this.handleAsync[T](
        { (r1: T, ex1: Throwable) => fn.apply(ex1) }: BiFunction[
          T,
          Throwable,
          T
        ],
        executor
      )
  }.thenCompose(Function.identity())

  def exceptionallyCompose(
      fn: Function[Throwable, _ <: CompletionStage[T]]
  ): CompletionStage[T] =
    handle[CompletionStage[T]]((r: T, ex: Throwable) =>
      if (ex == null) this
      else fn.apply(ex)
    ).thenCompose(Function.identity())

  def exceptionallyComposeAsync(
      fn: Function[Throwable, _ <: CompletionStage[T]]
  ): CompletionStage[T] = {
    handle[CompletionStage[T]] { (r: T, ex: Throwable) =>
      if (ex == null) this
      else
        this
          .handleAsync[CompletionStage[T]]({ (r1: T, ex1: Throwable) =>
            fn.apply(ex1)
          }: BiFunction[T, Throwable, CompletionStage[T]])
          .thenCompose(Function.identity())
    }.thenCompose(Function.identity())
  }

  def exceptionallyComposeAsync(
      fn: Function[Throwable, _ <: CompletionStage[T]],
      executor: Executor
  ): CompletionStage[T] = {
    handle[CompletionStage[T]] { (r: T, ex: Throwable) =>
      if (ex == null) this
      else
        this
          .handleAsync[CompletionStage[T]](
            { (r1: T, ex1: Throwable) =>
              fn.apply(ex1)
            }: BiFunction[T, Throwable, CompletionStage[T]],
            executor
          )
          .thenCompose(Function.identity())
    }.thenCompose(Function.identity())
  }

  def toCompletableFuture(): CompletableFuture[T]
}
