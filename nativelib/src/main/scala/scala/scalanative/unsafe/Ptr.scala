package scala.scalanative
package unsafe

import scala.language.implicitConversions
import scala.scalanative.annotation.alwaysinline
import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.runtime._
import scala.scalanative.unsigned.USize

final class Ptr[T] private[scalanative] (
    private[scalanative] val rawptr: RawPtr
) {
  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: Ptr[_] =>
        other.rawptr == rawptr
      case _ =>
        false
    })

  @alwaysinline override def toString: String =
    "Ptr@" + java.lang.Long.toHexString(castRawPtrToLong(rawptr))

  @alwaysinline def toInt: scala.Int =
    Intrinsics.castRawPtrToInt(rawptr)

  @alwaysinline def toLong: scala.Long =
    Intrinsics.castRawPtrToLong(rawptr)

  @alwaysinline def unary_!(implicit tag: Tag[T]): T =
    tag.load(this)

  @alwaysinline def `unary_!_=`(value: T)(implicit tag: Tag[T]): Unit =
    tag.store(this, value)

  @alwaysinline def +(offset: Size)(implicit tag: Tag[T]): Ptr[T] =
    new Ptr(elemRawPtr(rawptr, (offset * tag.size.toSize).rawSize))

  @alwaysinline def +(offset: USize)(implicit tag: Tag[T]): Ptr[T] =
    new Ptr(elemRawPtr(rawptr, (offset * tag.size).rawSize))

  @alwaysinline def -(offset: Size)(implicit tag: Tag[T]): Ptr[T] =
    new Ptr(elemRawPtr(rawptr, (-offset * tag.size.toSize).rawSize))

  @alwaysinline def -(offset: USize)(implicit tag: Tag[T]): Ptr[T] =
    new Ptr(elemRawPtr(rawptr, (-((offset * tag.size).toSize)).rawSize))

  @alwaysinline def -(other: Ptr[T])(implicit tag: Tag[T]): CPtrDiff = {
    val left = if (is32BitPlatform) this.toInt.toSize else this.toLong.toSize
    val right = if (is32BitPlatform) other.toInt.toSize else other.toLong.toSize
    (left - right) / tag.size.toSize
  }

  @alwaysinline def apply(offset: USize)(implicit tag: Tag[T]): T =
    (this + offset).`unary_!`

  @alwaysinline def apply(offset: Size)(implicit tag: Tag[T]): T =
    (this + offset).`unary_!`

  @alwaysinline def update(offset: USize, value: T)(implicit
      tag: Tag[T]
  ): Unit =
    (this + offset).`unary_!_=`(value)

  @alwaysinline def update(offset: Size, value: T)(implicit tag: Tag[T]): Unit =
    (this + offset).`unary_!_=`(value)
}

object Ptr {
  @alwaysinline implicit def ptrToCArray[T <: CArray[_, _]](ptr: Ptr[T])(
      implicit tag: Tag[T]
  ): T = !ptr

  @alwaysinline implicit def ptrToCStruct[T <: CStruct](ptr: Ptr[T])(implicit
      tag: Tag[T]
  ): T = !ptr
}
