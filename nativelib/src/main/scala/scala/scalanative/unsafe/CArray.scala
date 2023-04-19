package scala.scalanative
package unsafe

import scalanative.annotation.alwaysinline
import scalanative.runtime.RawPtr
import scalanative.runtime.Intrinsics._

final class CArray[T, N <: Nat] private[scalanative] (
    private[scalanative] val rawptr: RawPtr
) {
  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: CArray[_, _] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def toString: String =
    "CArray@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def at(idx: Int)(implicit tag: Tag[T]): Ptr[T] = {
    val ptr = new Ptr[T](rawptr)
    ptr + idx
  }

  @alwaysinline def apply(idx: Int)(implicit tag: Tag[T]): T = {
    val ptr = new Ptr[T](rawptr)
    ptr(idx)
  }

  @alwaysinline def update(idx: Int, value: T)(implicit tag: Tag[T]): Unit = {
    val ptr = new Ptr[T](rawptr)
    ptr(idx) = value
  }

  @alwaysinline def length(implicit tag: Tag[N]): Int = {
    tag.asInstanceOf[Tag.NatTag].toInt
  }
}
