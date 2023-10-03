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

  @alwaysinline def +(offset: Word)(implicit tag: Tag[T]): Ptr[T] =
    new Ptr(elemRawPtr(rawptr, offset * sizeof[T].toLong))

  @alwaysinline def +(offset: UWord)(implicit tag: Tag[T]): Ptr[T] =
    new Ptr(elemRawPtr(rawptr, (offset * sizeof[T]).toLong))

  @alwaysinline def -(offset: Word)(implicit tag: Tag[T]): Ptr[T] =
    new Ptr(elemRawPtr(rawptr, -offset * sizeof[T].toLong))

  @alwaysinline def -(offset: UWord)(implicit tag: Tag[T]): Ptr[T] =
    new Ptr(elemRawPtr(rawptr, -(offset * sizeof[T]).toLong))

  @alwaysinline def -(other: Ptr[T])(implicit tag: Tag[T]): CPtrDiff = {
    val left = castRawPtrToLong(rawptr)
    val right = castRawPtrToLong(other.rawptr)
    (left - right) / sizeof[T].toLong
  }

  @alwaysinline def apply(offset: Int)(implicit tag: Tag[T]): T =
    tag.load(
      elemRawPtr(rawptr, offset * tag.size.toInt)
    )

  @alwaysinline def apply(offset: UWord)(implicit tag: Tag[T]): T =
    tag.load(
      elemRawPtr(rawptr, offset.toLong * tag.size.toLong)
    )

  @alwaysinline def apply(offset: Word)(implicit tag: Tag[T]): T =
    tag.load(
      elemRawPtr(rawptr, offset * tag.size.toLong)
    )
  @alwaysinline def update(offset: Word, value: T)(implicit tag: Tag[T]): Unit =
    tag.store(
      elemRawPtr(rawptr, offset * tag.size.toLong),
      value
    )

  @alwaysinline def update(offset: UWord, value: T)(implicit
      tag: Tag[T]
  ): Unit =
    tag.store(
      elemRawPtr(rawptr, offset.toLong * tag.size.toLong),
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
