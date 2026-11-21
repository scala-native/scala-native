package scala.scalanative
package runtime
package javalib

import scala.concurrent.duration.FiniteDuration

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.runtime.Intrinsics

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

  def GC_Boehm_weakRefInit(field: RawPtr): Unit = GC.Boehm.weakRefInit(field)
  def GC_Boehm_weakRefGet(field: RawPtr): AnyRef =
    Intrinsics.castRawPtrToObject(GC.Boehm.weakRefGet(field))
  def GC_Boehm_weakRefClear(field: RawPtr): Unit = GC.Boehm.weakRefClear(field)

  def disableGracefullShutdown(): Unit =
    MainThreadShutdownContext.gracefully = false

  def stealWork(maxSteals: Int): Unit =
    concurrent.NativeExecutionContext.queueInternal.stealWork(maxSteals)
  def stealWork(timeout: FiniteDuration): Unit =
    concurrent.NativeExecutionContext.queueInternal.stealWork(timeout)

  def stackTraceIterator(): Iterator[StackTraceElement] =
    StackTrace.stackTraceIterator()

  def defaultVirtualThreadScheduler: VirtualThreadScheduler =
    VirtualThread.defaultScheduler
}
