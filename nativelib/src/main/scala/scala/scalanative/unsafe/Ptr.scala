package scala.scalanative
package unsafe

import scala.language.implicitConversions

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.meta.LinktimeInfo.is32BitPlatform
import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.runtime._
import scala.scalanative.unsigned._

final class Ptr[T] private[scalanative] (
    private[scalanative] val rawptr: RawPtr
) {
  @alwaysinline override def hashCode: Int =
    java.lang.Long.hashCode(toLong)

  @alwaysinline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: Ptr[_] => other.rawptr == rawptr
      case _             => false
    })

  @alwaysinline override def toString: String =
    "Ptr@" + java.lang.Long.toHexString(toLong)

  @alwaysinline def toInt: scala.Int =
    Intrinsics.castRawPtrToInt(rawptr)

  @alwaysinline def toLong: scala.Long =
    Intrinsics.castRawPtrToLong(rawptr)

  @alwaysinline def unary_!(implicit tag: Tag[T]): T =
    tag.load(rawptr)

  @alwaysinline def `unary_!_=`(value: T)(implicit tag: Tag[T]): Unit =
    tag.store(rawptr, value)

  // move forward

  @alwaysinline def +(offset: RawSize): Ptr[T] =
    new Ptr[T](elemRawPtr(rawptr, offset))

  @alwaysinline def +(offset: Int)(implicit tag: Tag[T]): Ptr[T] =
    this + (offset * tag.size).toRawSize

  @alwaysinline def +(offset: Long)(implicit tag: Tag[T]): Ptr[T] =
    this + (offset * tag.size.toLong).toRawSize

  @alwaysinline def +(offset: Size)(implicit tag: Tag[T]): Ptr[T] =
    this + (offset * tag.size.toSize).rawSize

  @alwaysinline def +(offset: USize)(implicit tag: Tag[T]): Ptr[T] =
    this + (offset * tag.size.toUSize).rawSize

  // move backward

  @alwaysinline def -(offset: Int)(implicit tag: Tag[T]): Ptr[T] =
    this + (-offset)

  @alwaysinline def -(offset: Long)(implicit tag: Tag[T]): Ptr[T] =
    this + (-offset)

  @alwaysinline def -(offset: Size)(implicit tag: Tag[T]): Ptr[T] =
    this + (-offset)

  @alwaysinline def -(offset: USize)(implicit tag: Tag[T]): Ptr[T] =
    this + (-offset.toSize)

  // difference
  @alwaysinline def -(other: Ptr[T])(implicit tag: Tag[T]): CPtrDiff = {
    if (is32BitPlatform) (this.toInt - other.toInt).toSize / tag.size.toSize
    else (this.toLong - other.toLong).toSize / tag.size.toSize
  }

  // load

  @alwaysinline private def load(offset: RawSize)(implicit tag: Tag[T]): T =
    tag.load(elemRawPtr(rawptr, offset))

  @alwaysinline def apply(offset: Int)(implicit tag: Tag[T]): T =
    load((offset * tag.size).toRawSize)

  @alwaysinline def apply(offset: Long)(implicit tag: Tag[T]): T =
    load((offset * tag.size.toLong).toRawSize)

  @alwaysinline def apply(offset: USize)(implicit tag: Tag[T]): T =
    load((offset * tag.size.toUSize).rawSize)

  @alwaysinline def apply(offset: Size)(implicit tag: Tag[T]): T =
    load((offset * tag.size.toSize).rawSize)

  // store

  @alwaysinline
  private def store(offset: RawSize, value: T)(implicit tag: Tag[T]): Unit =
    tag.store(elemRawPtr(rawptr, offset), value)

  @alwaysinline
  def update(offset: Int, value: T)(implicit tag: Tag[T]): Unit =
    store((offset * tag.size).toRawSize, value)

  @alwaysinline
  def update(offset: Long, value: T)(implicit tag: Tag[T]): Unit =
    store((offset * tag.size.toLong).toRawSize, value)

  @alwaysinline
  def update(offset: USize, value: T)(implicit tag: Tag[T]): Unit =
    store((offset * tag.size.toUSize).rawSize, value)

  @alwaysinline
  def update(offset: Size, value: T)(implicit tag: Tag[T]): Unit =
    store((offset * tag.size.toSize).rawSize, value)
}

object Ptr {
  @alwaysinline implicit def ptrToCArray[T <: CArray[_, _]](ptr: Ptr[T])(
      implicit tag: Tag[T]
  ): T = !ptr

  @alwaysinline implicit def ptrToCStruct[T <: CStruct](ptr: Ptr[T])(implicit
      tag: Tag[T]
  ): T = !ptr
}
