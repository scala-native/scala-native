package scala.scalanative
package nir

import util.unsupported

sealed abstract class Type {
  def elemty(path: Seq[Int]) = (this, path) match {
    case (_, Seq()) =>
      this
    case (Type.AnonStruct(tys), idx +: rest) =>
      tys(idx)
    case (Type.Array(ty, n), idx +: rest) =>
      assert(idx >= 0 && idx < n)
      ty
    case _ =>
      unsupported(s"${this}.elemty($path)")
  }
}
object Type {
  // low-level types
  final case object None   extends Type
  final case object Void   extends Type
  final case object Label  extends Type
  final case object Vararg extends Type
  final case object Ptr    extends Type

  sealed abstract case class I(width: Int) extends Type
  final object Bool extends I(1)
  final object I8   extends I(8)
  final object I16  extends I(16)
  final object I32  extends I(32)
  final object I64  extends I(64)

  sealed abstract case class F(width: Int) extends Type
  final object F32 extends F(32)
  final object F64 extends F(64)

  final case class Array(ty: Type, n: Int)              extends Type
  final case class Function(args: Seq[Type], ret: Type) extends Type
  final case class Struct(name: Global)                 extends Type
  final case class AnonStruct(tys: Seq[Type])           extends Type

  // high-level types
  final case object Size    extends Type
  final case object Unit    extends Type
  final case object Nothing extends Type

  sealed abstract class RefKind extends Type
  final case object Null extends RefKind
  final case class Class(name: Global)      extends RefKind
  final case class ClassValue(name: Global) extends Type
  final case class Trait(name: Global)      extends RefKind
  final case class Module(name: Global)     extends RefKind
}
