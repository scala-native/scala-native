package native
package nir

import native.util.unsupported

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

  def unboxed = this match {
    case Intr.unit   => Intr.unit
    case Intr.char   => Type.I16
    case Intr.bool   => Type.Bool
    case Intr.byte   => Type.I8
    case Intr.short  => Type.I16
    case Intr.int    => Type.I32
    case Intr.long   => Type.I64
    case Intr.float  => Type.F32
    case Intr.double => Type.F64
    case ty          => util.unsupported(ty)
  }
}
object Type {
  // low-level types
  final case object None                                extends Type
  final case object Void                                extends Type
  final case object Label                               extends Type
  final case object Vararg                              extends Type
  sealed abstract case class I(width: Int)              extends Type
  final object Bool                                     extends I(1)
  final object I8                                       extends I(8)
  final object I16                                      extends I(16)
  final object I32                                      extends I(32)
  final object I64                                      extends I(64)
  sealed abstract case class F(width: Int)              extends Type
  final object F32                                      extends F(32)
  final object F64                                      extends F(64)
  // TODO: change from [ty x N] to [N x ty] for consistency with llvm
  final case class Array   (ty: Type, n: Int)           extends Type
  final case class Ptr     (ty: Type)                   extends Type
  final case class Function(args: Seq[Type], ret: Type) extends Type
  final case class Struct  (name: Global)               extends Type
  final case class AnonStruct(tys: Seq[Type])           extends Type

  // high-level types
  final case object Size                          extends Type
  final case object Nothing                       extends Type
  sealed abstract class ClassKind                 extends Type
  final case object Null                          extends ClassKind
  final case class Class(name: Global)            extends ClassKind
  final case class InterfaceClass(name: Global)   extends ClassKind
  final case class ModuleClass(name: Global)      extends ClassKind
}
