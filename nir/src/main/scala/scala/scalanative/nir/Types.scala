package scala.scalanative
package nir

import util.unsupported

sealed abstract class Type {
  final def elemty(path: Seq[Val]): Type = (this, path) match {
    case (_, Seq()) =>
      this
    case (Type.Struct(_, tys), Val.I32(idx) +: rest) =>
      tys(idx).elemty(rest)
    case (Type.Array(ty, n), idx +: rest) =>
      ty.elemty(rest)
    case _ =>
      unsupported(s"${this}.elemty($path)")
  }

  final def show: String = nir.Show(this)

}

object Type {
  sealed trait Named extends Type {
    def name: Global
  }

  // low-level types
  final case object None   extends Type
  final case object Void   extends Type
  final case object Vararg extends Type
  final case object Ptr    extends Type

  // low-level integer and float types
  // with some knowledge of the initial Scala type.

  sealed abstract class Width { val value: Int }
  object Width {
    implicit val self1: W1.type   = W1
    implicit val self8: W8.type   = W8
    implicit val self16: W16.type = W16
    implicit val self32: W32.type = W32
    implicit val self64: W64.type = W64
  }
  case object W1  extends Width { val value: Int = 1  }
  case object W8  extends Width { val value: Int = 8  }
  case object W16 extends Width { val value: Int = 16 }
  case object W32 extends Width { val value: Int = 32 }
  case object W64 extends Width { val value: Int = 64 }

  sealed abstract class ConvFrom {
    type W <: Width
    val width: W
  }
  object ConvFrom {
    type Aux[T <: Width] = ConvFrom { type W = T }
  }

  implicit class ConvFromOps[T <: Width, S <: Width](val x: ConvFrom.Aux[T])
      extends AnyVal
      with Ordered[ConvFrom.Aux[S]] {
    def compare(y: ConvFrom.Aux[S]) =
      x.width.value.compare(y.width.value)
  }

  case object BoolSource extends ConvFrom {
    type W = W1.type; val width = implicitly[W]
  }

  case object ByteSource extends ConvFrom {
    type W = W8.type; val width = implicitly[W]
  }
  case object UByteSource extends ConvFrom {
    type W = W8.type; val width = implicitly[W]
  }

  case object CharSource extends ConvFrom {
    type W = W16.type; val width = implicitly[W]
  }
  case object ShortSource extends ConvFrom {
    type W = W16.type; val width = implicitly[W]
  }
  case object UShortSource extends ConvFrom {
    type W = W16.type; val width = implicitly[W]
  }

  case object IntSource extends ConvFrom {
    type W = W32.type; val width = implicitly[W]
  }
  case object UIntSource extends ConvFrom {
    type W = W32.type; val width = implicitly[W]
  }

  case object LongSource extends ConvFrom {
    type W = W64.type; val width = implicitly[W]
  }
  case object ULongSource extends ConvFrom {
    type W = W64.type; val width = implicitly[W]
  }

  case object FloatSource extends ConvFrom {
    type W = W32.type; val width = implicitly[W]
  }
  case object DoubleSource extends ConvFrom {
    type W = W64.type; val width = implicitly[W]
  }

  sealed abstract class I extends Type {
    type T <: Width
    val from: ConvFrom.Aux[T]
  }
  object I {
    def unapply(x: I): Option[ConvFrom.Aux[x.from.W]] = Some(x.from)
  }

  final case object Bool extends I {
    type T = BoolSource.W
    val from = BoolSource
  }
  sealed abstract case class I8(from: ConvFrom.Aux[W8.type]) extends I {
    type T = W8.type
  }
  final object I8         extends I8(ByteSource)
  final object UnsignedI8 extends I8(UByteSource)

  sealed abstract case class I16(from: ConvFrom.Aux[W16.type]) extends I {
    type T = W16.type
  }
  final object I16         extends I16(ShortSource)
  final object CharI16     extends I16(CharSource)
  final object UnsignedI16 extends I16(UShortSource)

  sealed abstract case class I32(from: ConvFrom.Aux[W32.type]) extends I {
    type T = W32.type
  }
  final object I32         extends I32(IntSource)
  final object UnsignedI32 extends I32(UIntSource)

  sealed abstract case class I64(from: ConvFrom.Aux[W64.type]) extends I {
    type T = W64.type
  }
  final object I64         extends I64(LongSource)
  final object UnsignedI64 extends I64(ULongSource)

  sealed abstract class F extends Type {
    type T <: Width
    val from: ConvFrom.Aux[T]
  }
  object F {
    def unapply(x: F): Option[ConvFrom.Aux[x.from.W]] = Some(x.from)
  }
  final case object F32 extends F { type T = W32.type; val from = FloatSource }
  final case object F64 extends F {
    type T = W64.type; val from = DoubleSource
  }

  final case class Array(ty: Type, n: Int)              extends Type
  final case class Function(args: Seq[Type], ret: Type) extends Type
  final case class Struct(name: Global, tys: Seq[Type]) extends Type with Named

  // high-level types
  final case object Nothing extends Type

  sealed abstract class RefKind         extends Type
  final case object Unit                extends RefKind
  final case class Class(name: Global)  extends RefKind with Named
  final case class Trait(name: Global)  extends RefKind with Named
  final case class Module(name: Global) extends RefKind with Named

  val unbox = Map[Type, Type](
    Type.Class(Global.Top("java.lang.Boolean"))   -> Type.Bool,
    Type.Class(Global.Top("java.lang.Character")) -> Type.CharI16,
    Type
      .Class(Global.Top("scala.scalanative.native.UByte")) -> Type.UnsignedI8,
    Type.Class(Global.Top("java.lang.Byte"))               -> Type.I8,
    Type
      .Class(Global.Top("scala.scalanative.native.UShort")) -> Type.UnsignedI16,
    Type.Class(Global.Top("java.lang.Short"))               -> Type.I16,
    Type
      .Class(Global.Top("scala.scalanative.native.UInt")) -> Type.UnsignedI32,
    Type.Class(Global.Top("java.lang.Integer"))           -> Type.I32,
    Type
      .Class(Global.Top("scala.scalanative.native.ULong")) -> Type.UnsignedI64,
    Type.Class(Global.Top("java.lang.Long"))               -> Type.I64,
    Type.Class(Global.Top("java.lang.Float"))              -> Type.F32,
    Type.Class(Global.Top("java.lang.Double"))             -> Type.F64
  )

  val box = Map[Type, Type](
    Type.Bool -> Type.Class(Global.Top("java.lang.Boolean")),
    Type.I8   -> Type.Class(Global.Top("java.lang.Byte")),
    Type.I16  -> Type.Class(Global.Top("java.lang.Short")),
    Type.I32  -> Type.Class(Global.Top("java.lang.Integer")),
    Type.I64  -> Type.Class(Global.Top("java.lang.Long")),
    Type.F32  -> Type.Class(Global.Top("java.lang.Float")),
    Type.F64  -> Type.Class(Global.Top("java.lang.Double"))
  )

  val unboxInverse = unbox.map { case (k, v) => (v, k) }
}
