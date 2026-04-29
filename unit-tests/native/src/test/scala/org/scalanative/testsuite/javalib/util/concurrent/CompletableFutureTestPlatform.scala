package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.{CompletableFuture, CompletionStage}

import scala.language.reflectiveCalls

object CompletableFutureTestPlatform {
  private type CompletableFutureWithMinimalCompletionStage[T] =
    AnyRef { def minimalCompletionStage(): CompletionStage[T] }

  def assumeMinimalCompletionStage(): Unit = ()

  def minimalCompletionStage[T](
      future: CompletableFuture[T]
  ): CompletionStage[T] =
    future
      .asInstanceOf[CompletableFutureWithMinimalCompletionStage[T]]
      .minimalCompletionStage()
}
