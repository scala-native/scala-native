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
    Type.Class(Global.Top("java.lang.Character"))             -> Type.I16,
    Type.Class(Global.Top("scala.scalanative.native.UByte"))  -> Type.I8,
    Type.Class(Global.Top("java.lang.Byte"))                  -> Type.I8,
    Type.Class(Global.Top("scala.scalanative.native.UShort")) -> Type.I16,
    Type.Class(Global.Top("java.lang.Short"))                 -> Type.I16,
    Type.Class(Global.Top("scala.scalanative.native.UInt"))   -> Type.I32,
    Type.Class(Global.Top("java.lang.Integer"))               -> Type.I32,
    Type.Class(Global.Top("scala.scalanative.native.ULong"))  -> Type.I64,
    Type.Class(Global.Top("java.lang.Long"))                  -> Type.I64,
    Type.Class(Global.Top("java.lang.Float"))                 -> Type.F32,
    Type.Class(Global.Top("java.lang.Double"))                -> Type.F64
  )
}
