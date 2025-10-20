/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util.function.{BiConsumer, BiFunction, Consumer, Function}

trait CompletionStage[T <: AnyRef] {
  def thenApply[U <: AnyRef](fn: Function[? >: T, ? <: U]): CompletionStage[U]
  def thenApplyAsync[U <: AnyRef](
      fn: Function[? >: T, ? <: U]
  ): CompletionStage[U]
  def thenApplyAsync[U <: AnyRef](
      fn: Function[? >: T, ? <: U],
      executor: Executor
  ): CompletionStage[U]
  def thenAccept(action: Consumer[? >: T]): CompletionStage[Void]
  def thenAcceptAsync(action: Consumer[? >: T]): CompletionStage[Void]
  def thenAcceptAsync(
      action: Consumer[? >: T],
      executor: Executor
  ): CompletionStage[Void]
  def thenRun(action: Runnable): CompletionStage[Void]
  def thenRunAsync(action: Runnable): CompletionStage[Void]
  def thenRunAsync(action: Runnable, executor: Executor): CompletionStage[Void]
  def thenCombine[U <: AnyRef, V <: AnyRef](
      other: CompletionStage[? <: U],
      fn: BiFunction[? >: T, ? >: U, ? <: V]
  ): CompletionStage[V]
  def thenCombineAsync[U <: AnyRef, V <: AnyRef](
      other: CompletionStage[? <: U],
      fn: BiFunction[? >: T, ? >: U, ? <: V]
  ): CompletionStage[V]
  def thenCombineAsync[U <: AnyRef, V <: AnyRef](
      other: CompletionStage[? <: U],
      fn: BiFunction[? >: T, ? >: U, ? <: V],
      executor: Executor
  ): CompletionStage[V]
  def thenAcceptBoth[U <: AnyRef](
      other: CompletionStage[? <: U],
      action: BiConsumer[? >: T, ? >: U]
  ): CompletionStage[Void]
  def thenAcceptBothAsync[U <: AnyRef](
      other: CompletionStage[? <: U],
      action: BiConsumer[? >: T, ? >: U]
  ): CompletionStage[Void]
  def thenAcceptBothAsync[U <: AnyRef](
      other: CompletionStage[? <: U],
      action: BiConsumer[? >: T, ? >: U],
      executor: Executor
  ): CompletionStage[Void]
  def runAfterBoth(
      other: CompletionStage[? <: AnyRef],
      action: Runnable
  ): CompletionStage[Void]
  def runAfterBothAsync(
      other: CompletionStage[? <: AnyRef],
      action: Runnable
  ): CompletionStage[Void]
  def runAfterBothAsync(
      other: CompletionStage[? <: AnyRef],
      action: Runnable,
      executor: Executor
  ): CompletionStage[Void]
  def applyToEither[U <: AnyRef](
      other: CompletionStage[? <: T],
      fn: Function[? >: T, U]
  ): CompletionStage[U]
  def applyToEitherAsync[U <: AnyRef](
      other: CompletionStage[? <: T],
      fn: Function[? >: T, U]
  ): CompletionStage[U]
  def applyToEitherAsync[U <: AnyRef](
      other: CompletionStage[? <: T],
      fn: Function[? >: T, U],
      executor: Executor
  ): CompletionStage[U]
  def acceptEither(
      other: CompletionStage[? <: T],
      action: Consumer[? >: T]
  ): CompletionStage[Void]
  def acceptEitherAsync(
      other: CompletionStage[? <: T],
      action: Consumer[? >: T]
  ): CompletionStage[Void]
  def acceptEitherAsync(
      other: CompletionStage[? <: T],
      action: Consumer[? >: T],
      executor: Executor
  ): CompletionStage[Void]
  def runAfterEither(
      other: CompletionStage[? <: AnyRef],
      action: Runnable
  ): CompletionStage[Void]
  def runAfterEitherAsync(
      other: CompletionStage[? <: AnyRef],
      action: Runnable
  ): CompletionStage[Void]
  def runAfterEitherAsync(
      other: CompletionStage[? <: AnyRef],
      action: Runnable,
      executor: Executor
  ): CompletionStage[Void]
  def thenCompose[U <: AnyRef](
      fn: Function[? >: T, ? <: CompletionStage[U]]
  ): CompletionStage[U]
  def thenComposeAsync[U <: AnyRef](
      fn: Function[? >: T, ? <: CompletionStage[U]]
  ): CompletionStage[U]
  def thenComposeAsync[U <: AnyRef](
      fn: Function[? >: T, ? <: CompletionStage[U]],
      executor: Executor
  ): CompletionStage[U]
  def handle[U <: AnyRef](
      fn: BiFunction[? >: T, Throwable, ? <: U]
  ): CompletionStage[U]
  def handleAsync[U <: AnyRef](
      fn: BiFunction[? >: T, Throwable, ? <: U]
  ): CompletionStage[U]
  def handleAsync[U <: AnyRef](
      fn: BiFunction[? >: T, Throwable, ? <: U],
      executor: Executor
  ): CompletionStage[U]
  def whenComplete(
      action: BiConsumer[? >: T, ? >: Throwable]
  ): CompletionStage[T]
  def whenCompleteAsync(
      action: BiConsumer[? >: T, ? >: Throwable]
  ): CompletionStage[T]
  def whenCompleteAsync(
      action: BiConsumer[? >: T, ? >: Throwable],
      executor: Executor
  ): CompletionStage[T]
  def exceptionally(fn: Function[Throwable, ? <: T]): CompletionStage[T]
  def exceptionallyAsync(
      fn: Function[Throwable, ? <: T]
  ): CompletionStage[T] = {
    handle[CompletionStage[T]] { (r: T, ex: Throwable) =>
      if (ex == null) this
      else this.handleAsync { (r1: T, ex1: Throwable) => fn.apply(ex1) }
    }.thenCompose(Function.identity())
  }

  def exceptionallyAsync(
      fn: Function[Throwable, ? <: T],
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
      fn: Function[Throwable, ? <: CompletionStage[T]]
  ): CompletionStage[T] =
    handle[CompletionStage[T]]((r: T, ex: Throwable) =>
      if (ex == null) this
      else fn.apply(ex)
    ).thenCompose(Function.identity())

  def exceptionallyComposeAsync(
      fn: Function[Throwable, ? <: CompletionStage[T]]
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
      fn: Function[Throwable, ? <: CompletionStage[T]],
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
