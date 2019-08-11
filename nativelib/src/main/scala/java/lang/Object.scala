package java.lang

import scala.scalanative.native._
import scala.scalanative.runtime, runtime.ClassTypeOps
import scala.scalanative.runtime.Intrinsics._

class _Object {
  var __monitor: scala.scalanative.runtime.Monitor = _
  @inline def __equals(that: _Object): scala.Boolean =
    this eq that

  @inline def __hashCode(): scala.Int = {
    val addr = this.cast[Word]
    addr.toInt ^ (addr >> 32).toInt
  }

  @inline def __toString(): String =
    getClass.getName + "@" + Integer.toHexString(hashCode)

  @inline def __getClass(): _Class[_] =
    new _Class(runtime.getType(this).cast[Ptr[runtime.Type]])

  @inline def __notify(): Unit =
    runtime.getMonitor(this)._notify

  @inline def __notifyAll(): Unit =
    runtime.getMonitor(this)._notifyAll

  @inline def __wait(): Unit =
    runtime.getMonitor(this)._wait

  @inline def __wait(timeout: scala.Long): Unit =
    runtime.getMonitor(this)._wait(timeout)

  @inline def __wait(timeout: scala.Long, nanos: Int): Unit =
    runtime.getMonitor(this)._wait(timeout, nanos)

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
    val addr = this.cast[Word]
    addr.toInt ^ (addr >> 32).toInt
  }

  protected def __clone(): _Object = {
    val ty    = runtime.getType(this)
    val size  = ty.size
    val clone = runtime.GC.alloc(ty, size)
    `llvm.memcpy.p0i8.p0i8.i64`(clone.cast[Ptr[scala.Byte]],
                                this.cast[Ptr[scala.Byte]],
                                size,
                                1,
                                false)
    clone.cast[_Object]
  }

  protected def __finalize(): Unit = ()
}
