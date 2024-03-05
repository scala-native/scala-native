package scala.scalanative.runtime

import scala.scalanative.runtime._
import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.unsigned._
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

// emmited as java.lang.Object
private[runtime] class _Object {
  @inline def __equals(that: _Object): scala.Boolean =
    this eq that

  @inline def __hashCode(): scala.Int = {
    val addr = castRawPtrToLong(castObjectToRawPtr(this))
    addr.toInt ^ (addr >> 32).toInt
  }

  @inline def __toString(): String =
    getClass.getName + "@" + Integer.toHexString(hashCode)

  @inline def __getClass(): _Class[_] = {
    val ptr = castObjectToRawPtr(this)
    val rtti = loadRawPtr(ptr)
    castRawPtrToObject(rtti).asInstanceOf[_Class[_]]
  }

  @inline def __notify(): Unit = if (isMultithreadingEnabled) {
    getMonitor(this)._notify()
  }

  @inline def __notifyAll(): Unit = if (isMultithreadingEnabled) {
    getMonitor(this)._notifyAll()
  }

  @inline def __wait(): Unit = if (isMultithreadingEnabled) {
    getMonitor(this)._wait()
  }

  @inline def __wait(timeout: scala.Long): Unit = if (isMultithreadingEnabled) {
    getMonitor(this)._wait(timeout)
  }

  @inline def __wait(timeout: scala.Long, nanos: Int): Unit =
    if (isMultithreadingEnabled) {
      getMonitor(this)._wait(timeout, nanos)
    }

  protected def __clone(): _Object = this match {
    case _: Cloneable =>
      val cls = __getClass()
      val size = cls.size
      val clone = GC.alloc(cls.asInstanceOf[Class[_]], size)
      val src = castObjectToRawPtr(this)
      ffi.memcpy(clone, src, Intrinsics.castIntToRawSize(size))
      if (isMultithreadingEnabled) {
        // Reset object monitor
        storeRawSize(
          elemRawPtr(clone, MemoryLayout.Array.LockWordOffset),
          castIntToRawSize(0)
        )
      }
      castRawPtrToObject(clone).asInstanceOf[_Object]
    case _ =>
      throw new CloneNotSupportedException(
        "Doesn't implement Cloneable interface!"
      )
  }

  protected def __finalize(): Unit = ()
}
