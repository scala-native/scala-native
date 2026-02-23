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

  def GC_Boehm_weakRefSlotCreate(referent: AnyRef): RawPtr =
    GC.Boehm.weakRefSlotCreate(Intrinsics.castObjectToRawPtr(referent))
  def GC_Boehm_weakRefSlotGet[T <: AnyRef](slot: RawPtr): T = {
    Intrinsics
      .castRawPtrToObject(GC.Boehm.weakRefSlotGet(slot))
      .asInstanceOf[T]
  }
  def GC_Boehm_weakRefSlotClear(slot: RawPtr): Unit =
    GC.Boehm.weakRefSlotClear(slot)

  def disableGracefullShutdown(): Unit =
    MainThreadShutdownContext.gracefully = false

  def stealWork(maxSteals: Int): Unit =
    concurrent.NativeExecutionContext.queueInternal.stealWork(maxSteals)
  def stealWork(timeout: FiniteDuration): Unit =
    concurrent.NativeExecutionContext.queueInternal.stealWork(timeout)

  def stackTraceIterator(): Iterator[StackTraceElement] =
    StackTrace.stackTraceIterator()

}
