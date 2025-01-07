package scala.scalanative
package runtime
package javalib

import scala.scalanative.annotation.alwaysinline
import scala.concurrent.duration.FiniteDuration

object Proxy {
  @alwaysinline
  def executeUncaughtExceptionHandler(
      handler: Thread.UncaughtExceptionHandler,
      thread: Thread,
      ex: java.lang.Throwable
  ): Unit = scala.scalanative.runtime.executeUncaughtExceptionHandler(
    handler = handler,
    thread = thread,
    throwable = ex
  )

  def GC_collect(): Unit = GC.collect()
  type GCWeakReferencesCollectedCallback = GC.WeakReferencesCollectedCallback
  def GC_setWeakReferencesCollectedCallback(
      callback: GCWeakReferencesCollectedCallback
  ): Unit = GC.setWeakReferencesCollectedCallback(callback)

  def disableGracefullShutdown(): Unit =
    MainThreadShutdownContext.gracefully = false

  def stealWork(maxSteals: Int): Unit =
    concurrent.NativeExecutionContext.queueInternal.stealWork(maxSteals)
  def stealWork(timeout: FiniteDuration): Unit =
    concurrent.NativeExecutionContext.queueInternal.stealWork(timeout)

}
