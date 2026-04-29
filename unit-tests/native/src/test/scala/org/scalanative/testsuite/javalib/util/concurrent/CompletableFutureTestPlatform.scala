package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.{CompletableFuture, CompletionStage}

object CompletableFutureTestPlatform {
  def assumeMinimalCompletionStage(): Unit = ()

  def minimalCompletionStage[T](
      future: CompletableFuture[T]
  ): CompletionStage[T] =
    future.minimalCompletionStage()
}
