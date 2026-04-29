package org.scalanative.testsuite.javalib.util.concurrent

import java.lang.reflect.InvocationTargetException
import java.util.concurrent.{CompletableFuture, CompletionStage}

import org.junit.Assume.assumeTrue

object CompletableFutureTestPlatform {
  private val minimalCompletionStageMethod =
    try Some(classOf[CompletableFuture[_]].getMethod("minimalCompletionStage"))
    catch {
      case _: NoSuchMethodException => None
    }

  def assumeMinimalCompletionStage(): Unit =
    assumeTrue(
      "CompletableFuture.minimalCompletionStage requires JDK 9+",
      minimalCompletionStageMethod.isDefined
    )

  def minimalCompletionStage[T](future: CompletableFuture[T]): CompletionStage[T] =
    try
      minimalCompletionStageMethod.get
        .invoke(future)
        .asInstanceOf[CompletionStage[T]]
    catch {
      case e: InvocationTargetException =>
        throw e.getCause()
    }
}
