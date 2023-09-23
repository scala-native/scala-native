package java.util.concurrent

import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function
import scala.annotation.unchecked.uncheckedVariance

trait CompletionStage[T] {

  def thenApply[U](f: Function[_ >: T, _ <: U]): CompletionStage[U]

  def thenApplyAsync[U](f: Function[_ >: T, _ <: U]): CompletionStage[U]

  def thenApplyAsync[U](
      f: Function[_ >: T, _ <: U],
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

  def thenCombine[U, V](
      other: CompletionStage[_ <: U],
      f: BiFunction[_ >: T, _ >: U, _ <: V]
  ): CompletionStage[V]

  def thenCombineAsync[U, V](
      other: CompletionStage[_ <: U],
      f: BiFunction[_ >: T, _ >: U, _ <: V]
  ): CompletionStage[V]

  def thenCombineAsync[U, V](
      other: CompletionStage[_ <: U],
      f: BiFunction[_ >: T, _ >: U, _ <: V],
      executor: Executor
  ): CompletionStage[V]

  def thenAcceptBoth[U](
      other: CompletionStage[_ <: U],
      action: BiConsumer[_ >: T, _ >: U]
  ): CompletionStage[Void]

  def thenAcceptBothAsync[U](
      other: CompletionStage[_ <: U],
      action: BiConsumer[_ >: T, _ >: U]
  ): CompletionStage[Void]

  def thenAcceptBothAsync[U](
      other: CompletionStage[_ <: U],
      action: BiConsumer[_ >: T, _ >: U],
      executor: Executor
  ): CompletionStage[Void]

  def runAfterBoth(
      other: CompletionStage[_],
      action: Runnable
  ): CompletionStage[Void]

  def runAfterBothAsync(
      other: CompletionStage[_],
      action: Runnable
  ): CompletionStage[Void]

  def runAfterBothAsync(
      other: CompletionStage[_],
      action: Runnable,
      executor: Executor
  ): CompletionStage[Void]

  def applyToEither[U](
      other: CompletionStage[_ <: T],
      f: Function[_ >: T, U]
  ): CompletionStage[U]

  def applyToEitherAsync[U](
      other: CompletionStage[_ <: T],
      f: Function[_ >: T, U]
  ): CompletionStage[U]

  def applyToEitherAsync[U](
      other: CompletionStage[_ <: T],
      f: Function[_ >: T, U],
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
      other: CompletionStage[_],
      action: Runnable
  ): CompletionStage[Void]

  def runAfterEitherAsync(
      other: CompletionStage[_],
      action: Runnable
  ): CompletionStage[Void]

  def runAfterEitherAsync(
      other: CompletionStage[_],
      action: Runnable,
      executor: Executor
  ): CompletionStage[Void]

  def thenCompose[U](
      f: Function[_ >: T, _ <: CompletionStage[U]]
  ): CompletionStage[U]

  def thenComposeAsync[U](
      f: Function[_ >: T, _ <: CompletionStage[U]]
  ): CompletionStage[U]

  def thenComposeAsync[U](
      f: Function[_ >: T, _ <: CompletionStage[U]],
      executor: Executor
  ): CompletionStage[U]

  def handle[U](f: BiFunction[_ >: T, Throwable, _ <: U]): CompletionStage[U]

  def handleAsync[U](
      f: BiFunction[_ >: T, Throwable, _ <: U]
  ): CompletionStage[U]

  def handleAsync[U](
      f: BiFunction[_ >: T, Throwable, _ <: U],
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

  def exceptionally(f: Function[Throwable, _ <: T]): CompletionStage[T]

  def exceptionallyAsync(f: Function[Throwable, _ <: T]): CompletionStage[T] = {
    handle[CompletionStage[T]]((_: T, e: Throwable) => {
      if (e eq null)
        this
      else
        this.handleAsync[T](new BiFunction[T, Throwable, T] {
          override def apply(t: T, u: Throwable): T = f(u)
        })
    }).thenCompose(Function.identity())
  }

  def exceptionallyAsync(
      f: Function[Throwable, _ <: T],
      executor: Executor
  ): CompletionStage[T] = {
    handle[CompletionStage[T]]((_, e) => {
      if (e eq null)
        this
      else
        this.handleAsync[T](
          new BiFunction[T, Throwable, T] {
            override def apply(t: T, u: Throwable): T = f(u)
          },
          executor
        )
    }).thenCompose(Function.identity())
  }

  def exceptionallyCompose(
      f: Function[Throwable, _ <: CompletionStage[T]]
  ): CompletionStage[T] = {
    handle[CompletionStage[T]]((_, e) => {
      if (e eq null)
        this
      else f(e)
    }).thenCompose(Function.identity())
  }

  def exceptionallyComposeAsync(
      f: Function[Throwable, _ <: CompletionStage[T]]
  ): CompletionStage[T] = {
    handle[CompletionStage[T]]((_, e) => {
      if (e eq null)
        this
      else
        this
          .handleAsync[CompletionStage[T]](
            new BiFunction[T, Throwable, CompletionStage[T]] {
              override def apply(t: T, u: Throwable): CompletionStage[T] = f(u)
            }
          )
          .thenCompose(Function.identity())
    }).thenCompose(Function.identity())
  }

  def exceptionallyComposeAsync(
      f: Function[Throwable, _ <: CompletionStage[T]],
      executor: Executor
  ): CompletionStage[T] = {
    handle[CompletionStage[T]]((_, e: Throwable) =>
      if (e eq null)
        this
      else {
        this
          .handleAsync[CompletionStage[T]](
            new BiFunction[T, Throwable, CompletionStage[T]] {
              override def apply(t: T, u: Throwable): CompletionStage[T] = f(u)
            },
            executor
          )
          .thenCompose(Function.identity())
      }
    ).thenCompose(Function.identity())
  }

  def toCompletableFuture: CompletableFuture[T]
}
