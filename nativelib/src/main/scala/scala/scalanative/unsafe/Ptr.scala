package scala.scalanative
package unsafe

import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.scalanative.annotation.alwaysinline
import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.runtime._

final class Ptr[T] private[scalanative] (
    private[scalanative] val rawptr: RawPtr) {
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

  @alwaysinline def +(offset: Word)(implicit tag: Tag[T]): Ptr[T] =
    new Ptr(elemRawPtr(rawptr, offset * sizeof[T]))

  @alwaysinline def -(offset: Word)(implicit tag: Tag[T]): Ptr[T] =
    new Ptr(elemRawPtr(rawptr, -offset * sizeof[T]))

  @alwaysinline def -(other: Ptr[T])(implicit tag: Tag[T]): CPtrDiff = {
    val left  = castRawPtrToLong(rawptr)
    val right = castRawPtrToLong(other.rawptr)
    (left - right) / sizeof[T]
  }

  @alwaysinline def apply(offset: Word)(implicit tag: Tag[T]): T =
    (this + offset).unary_!

  @alwaysinline def update(offset: Word, value: T)(implicit tag: Tag[T]): Unit =
    (this + offset).`unary_!_=`(value)
}

object Ptr {
  @alwaysinline implicit def ptrToCArray[T <: CArray[_, _]](ptr: Ptr[T])(
      implicit tag: Tag[T]): T = !ptr

  @alwaysinline implicit def ptrToCStruct[T <: CStruct](ptr: Ptr[T])(
      implicit tag: Tag[T]): T = !ptr

  @alwaysinline implicit def ptrToCFuncPtr[F <: CFuncPtr](ptr: Ptr[Byte]): F =
    macro MacroImpl.toCFuncPtr[F]

  @alwaysinline implicit def cFuncPtrToPtr[T](ptr: CFuncPtr): Ptr[Byte] = {
    Boxes.boxToPtr[Byte](Boxes.unboxToCFuncRawPtr(ptr))
  }

  private object MacroImpl {
    import scala.reflect.macros.blackbox.Context
    def toCFuncPtr[F: c.WeakTypeTag](c: Context)(ptr: c.Tree): c.Tree = {
      import c.universe._

      val runtime   = q"_root_.scala.scalanative.runtime"
      val callCFunc = q"$runtime.Intrinsics.callCFuncPtr"
      val unboxPtr  = q"$runtime.Boxes.unboxToPtr"

      val F       = weakTypeOf[F].dealias
      val tps     = F.typeArgs.map(_.dealias)
      val argTps  = tps.init
      val retType = tps.last

      val (args, argSigs) = argTps.zipWithIndex.map {
        case (tpe, idx) =>
          val arg = TermName("arg" + (idx + 1))
          q"$arg" -> q"$arg: $tpe"
      }.unzip
      val allArgs = q"$unboxPtr($ptr)" +: args

      q"""new $F{
		    override def apply(..$argSigs): $retType = $callCFunc[..$tps](..$allArgs)
      }"""
    }
  }
}
