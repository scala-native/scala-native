package scala.scalanative
package nir

import util.unsupported

sealed abstract class Type {
  def elemty(path: Seq[Val]): Type = (this, path) match {
    case (_, Seq()) =>
      this
    case (Type.Struct(_, tys), Val.I32(idx) +: rest) =>
      tys(idx).elemty(rest)
    case (Type.Array(ty, n), idx +: rest) =>
      ty.elemty(rest)
    case _ =>
      unsupported(s"${this}.elemty($path)")
  }
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

  sealed abstract case class I(width: Int) extends Type
  final object Bool                        extends I(1)
  final object I8                          extends I(8)
  final object I16                         extends I(16)
  final object I32                         extends I(32)
  final object I64                         extends I(64)

  sealed abstract case class F(width: Int) extends Type
  final object F32                         extends F(32)
  final object F64                         extends F(64)

  final case class Array(ty: Type, n: Int)              extends Type
  final case class Function(args: Seq[Arg], ret: Type)  extends Type
  final case class Struct(name: Global, tys: Seq[Type]) extends Type with Named

  // high-level types
  final case object Nothing extends Type

  sealed abstract class RefKind         extends Type
  final case object Unit                extends RefKind
  final case class Class(name: Global)  extends RefKind with Named
  final case class Trait(name: Global)  extends RefKind with Named
  final case class Module(name: Global) extends RefKind with Named
}

object Boxes {
  val box = Map(
    'B' -> Type.Class(Global.Top("java.lang.Boolean")),
    'C' -> Type.Class(Global.Top("java.lang.Character")),
    'z' -> Type.Class(Global.Top("scala.scalanative.native.UByte")),
    'Z' -> Type.Class(Global.Top("java.lang.Byte")),
    's' -> Type.Class(Global.Top("scala.scalanative.native.UShort")),
    'S' -> Type.Class(Global.Top("java.lang.Short")),
    'i' -> Type.Class(Global.Top("scala.scalanative.native.UInt")),
    'I' -> Type.Class(Global.Top("java.lang.Integer")),
    'l' -> Type.Class(Global.Top("scala.scalanative.native.ULong")),
    'L' -> Type.Class(Global.Top("java.lang.Long")),
    'F' -> Type.Class(Global.Top("java.lang.Float")),
    'D' -> Type.Class(Global.Top("java.lang.Double"))
  )

  val unbox = Map(
    'B' -> Type.Bool,
    'C' -> Type.I16,
    'z' -> Type.I8,
    'Z' -> Type.I8,
    's' -> Type.I16,
    'S' -> Type.I16,
    'i' -> Type.I32,
    'I' -> Type.I32,
    'l' -> Type.I64,
    'L' -> Type.I64,
    'F' -> Type.F32,
    'D' -> Type.F64
  )

}
