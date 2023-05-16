package java.lang

import scala.scalanative.runtime._
import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.unsigned._
import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled

class _Object {
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

  @inline def __scala_==(that: _Object): scala.Boolean = {
    // This implementation is only called for classes that don't override
    // equals. Otherwise, whenever equals is overriden, we also update the
    // vtable entry for scala_== to point to the override directly.
    this eq that
  }

  @inline def __scala_## : scala.Int = {
    // This implementation is only called for classes that don't override
    // hashCode. Otherwise, whenever hashCode is overriden, we also update the
    // vtable entry for scala_## to point to the override directly.
    val addr = castRawPtrToLong(castObjectToRawPtr(this))
    addr.toInt ^ (addr >> 32).toInt
  }

  protected def __clone(): _Object = {
    val cls = __getClass()
    val size = cls.size.toUSize
    val clone = GC.alloc(cls.asInstanceOf[Class[_]], size)
    val src = castObjectToRawPtr(this)
    libc.memcpy(clone, src, size)
    castRawPtrToObject(clone).asInstanceOf[_Object]
  }

  protected def __finalize(): Unit = ()
}
