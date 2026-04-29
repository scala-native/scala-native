package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.{CompletableFuture, CompletionStage}

import org.junit.Assume.assumeTrue

object CompletableFutureTestPlatform {
  def assumeMinimalCompletionStage(): Unit =
    assumeTrue(
      "CompletableFuture.minimalCompletionStage is covered by require-jdk17 tests",
      false
    )

  def minimalCompletionStage[T](
      future: CompletableFuture[T]
  ): CompletionStage[T] =
    throw new AssertionError("unreachable")
}
