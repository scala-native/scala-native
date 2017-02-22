package scala.scalanative
package nir

import util.unsupported

sealed abstract class Type {

  final def elemty(path: Seq[Val]): Type = (this, path) match {
    case (_, Seq()) =>
      this
    case (Type.Struct(_, tys), Val.Int(idx) +: rest) =>
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

  final case object None extends Type

  // low-level second-class types

  final case object Void   extends Type
  final case object Vararg extends Type

  // low-level primitive types

  sealed abstract class Primitive(val width: Int) extends Type
  final case object Ptr                           extends Primitive(64)

  sealed abstract class I(width: Int, val signed: Boolean)
      extends Primitive(width)
  object I {
    def unapply(i: I): Some[(Int, Boolean)] = Some((i.width, i.signed))
  }
  final case object Bool   extends I(1, signed = false)
  final case object Char   extends I(16, signed = false)
  final case object Byte   extends I(8, signed = true)
  final case object UByte  extends I(8, signed = false)
  final case object Short  extends I(16, signed = true)
  final case object UShort extends I(16, signed = false)
  final case object Int    extends I(32, signed = true)
  final case object UInt   extends I(32, signed = false)
  final case object Long   extends I(64, signed = true)
  final case object ULong  extends I(64, signed = false)

  sealed abstract class F(width: Int) extends Primitive(width)
  object F { def unapply(f: F): Some[Int] = Some(f.width) }
  final case object Float  extends F(32)
  final case object Double extends F(64)

  // low-level composite types

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
    Type.Class(Global.Top("java.lang.Boolean"))               -> Type.Bool,
    Type.Class(Global.Top("java.lang.Character"))             -> Type.Char,
    Type.Class(Global.Top("scala.scalanative.native.UByte"))  -> Type.UByte,
    Type.Class(Global.Top("java.lang.Byte"))                  -> Type.Byte,
    Type.Class(Global.Top("scala.scalanative.native.UShort")) -> Type.UShort,
    Type.Class(Global.Top("java.lang.Short"))                 -> Type.Short,
    Type.Class(Global.Top("scala.scalanative.native.UInt"))   -> Type.UInt,
    Type.Class(Global.Top("java.lang.Integer"))               -> Type.Int,
    Type.Class(Global.Top("scala.scalanative.native.ULong"))  -> Type.ULong,
    Type.Class(Global.Top("java.lang.Long"))                  -> Type.Long,
    Type.Class(Global.Top("java.lang.Float"))                 -> Type.Float,
    Type.Class(Global.Top("java.lang.Double"))                -> Type.Double
  )

  val box = unbox.map { case (k, v) => (v, k) }

}
