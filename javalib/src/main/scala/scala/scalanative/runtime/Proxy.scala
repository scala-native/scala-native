package scala.scalanative.runtime

import scala.scalanative.annotation.alwaysinline

object Proxy {
  @alwaysinline
  def executeUncaughtExceptionHandler(
      handler: Thread.UncaughtExceptionHandler,
      thread: Thread,
      ex: Throwable
  ): Unit = scala.scalanative.runtime.executeUncaughtExceptionHandler(
    handler = handler,
    thread = thread,
    throwable = ex
  )
}
