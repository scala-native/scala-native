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

  def GC_collect(): Unit = GC.collect()
  type GCWeakReferencesCollectedCallback = GC.WeakReferencesCollectedCallback
  def GC_setWeakReferencesCollectedCallback(
      callback: GCWeakReferencesCollectedCallback
  ): Unit = GC.setWeakReferencesCollectedCallback(callback)
  
  def skipWaitingForNonDeamonThreads(): Unit = JoinNonDaemonThreads.skip()
}
