package java.lang

import scala.scalanative.native._
import scala.scalanative.runtime, runtime.ClassTypeOps
import scala.scalanative.runtime.Intrinsics._

class _Object {
  @inline def __equals(that: _Object): scala.Boolean =
    this eq that

  @inline def __hashCode(): scala.Int =
    this.cast[Word].hashCode

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

  @inline def __scala_==(other: _Object): scala.Boolean =
    __equals(other)

  @inline def __scala_## : scala.Int =
    __hashCode

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
