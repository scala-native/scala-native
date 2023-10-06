package scala.scalanative
package unsafe

import scala.language.implicitConversions
import scala.scalanative.annotation.alwaysinline
import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.runtime._
import scala.scalanative.unsigned._

final class Ptr[T] private[scalanative] (
    private[scalanative] val rawptr: RawPtr
) {
  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(castRawPtrToLong(rawptr))

  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: Ptr[_] => other.rawptr == rawptr
      case _             => false
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

  @alwaysinline def +(offset: Int)(implicit tag: Tag[T]): Ptr[T] =
    new Ptr[T](elemRawPtr(rawptr, castIntToRawSize(offset * tag.size.toInt)))

  @alwaysinline def +(offset: Size)(implicit tag: Tag[T]): Ptr[T] =
    new Ptr[T](elemRawPtr(rawptr, toRawSize(offset * tag.size.toSize)))

  @alwaysinline def +(offset: USize)(implicit tag: Tag[T]): Ptr[T] =
    new Ptr[T](elemRawPtr(rawptr, toRawSize(offset * tag.size)))

  @alwaysinline def -(offset: Int)(implicit tag: Tag[T]): Ptr[T] =
    new Ptr[T](
      elemRawPtr(rawptr, castIntToRawSize(-offset * tag.size.toInt))
    )

  @alwaysinline def -(offset: Size)(implicit tag: Tag[T]): Ptr[T] =
    new Ptr[T](elemRawPtr(rawptr, toRawSize(-offset * tag.size.toSize)))

  @alwaysinline def -(offset: USize)(implicit tag: Tag[T]): Ptr[T] =
    new Ptr[T](
      elemRawPtr(rawptr, toRawSize(-offset.toSize * tag.size.toSize))
    )

  @alwaysinline def -(other: Ptr[T])(implicit tag: Tag[T]): CPtrDiff = {
    if (is32BitPlatform) (this.toInt - other.toInt).toSize / tag.size.toSize
    else (this.toLong - other.toLong).toSize / tag.size.toSize
  }

  @alwaysinline def apply(offset: Int)(implicit tag: Tag[T]): T =
    tag.load(
      elemRawPtr(rawptr, castIntToRawSize(offset * tag.size.toInt))
    )

  @alwaysinline def apply(offset: USize)(implicit tag: Tag[T]): T =
    tag.load(elemRawPtr(rawptr, toRawSize(offset * tag.size)))

  @alwaysinline def apply(offset: Size)(implicit tag: Tag[T]): T =
    tag.load(elemRawPtr(rawptr, toRawSize(offset * tag.size.toSize)))

  @alwaysinline def update(offset: Int, value: T)(implicit
      tag: Tag[T]
  ): Unit =
    tag.store(
      elemRawPtr(rawptr, castIntToRawSize(offset * tag.size.toInt)),
      value
    )

  @alwaysinline def update(offset: USize, value: T)(implicit
      tag: Tag[T]
  ): Unit =
    tag.store(
      elemRawPtr(rawptr, toRawSize(offset * tag.size)),
      value
    )

  @alwaysinline def update(offset: Size, value: T)(implicit
      tag: Tag[T]
  ): Unit =
    tag.store(
      elemRawPtr(rawptr, toRawSize(offset * tag.size.toSize)),
      value
    )
}

object Ptr {
  @alwaysinline implicit def ptrToCArray[T <: CArray[_, _]](ptr: Ptr[T])(
      implicit tag: Tag[T]
  ): T = !ptr

  @alwaysinline implicit def ptrToCStruct[T <: CStruct](ptr: Ptr[T])(implicit
      tag: Tag[T]
  ): T = !ptr
}
