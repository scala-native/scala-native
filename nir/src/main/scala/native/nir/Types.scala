package native
package nir

sealed abstract class Type {
  def unboxed = this match {
    case Intrinsic.char   => Type.I16
    case Intrinsic.bool   => Type.Bool
    case Intrinsic.byte   => Type.I8
    case Intrinsic.short  => Type.I16
    case Intrinsic.int    => Type.I32
    case Intrinsic.long   => Type.I64
    case Intrinsic.float  => Type.F32
    case Intrinsic.double => Type.F64
    case _                => util.unreachable
  }
}
object Type {
  // low-level types
  final case object None                   extends Type
  final case object Void                   extends Type
  final case object Size                   extends Type
  final case object Bool                   extends Type
  sealed abstract case class I(width: Int) extends Type
  final object I8                          extends I(8)
  final object I16                         extends I(16)
  final object I32                         extends I(32)
  final object I64                         extends I(64)
  sealed abstract case class F(width: Int) extends Type
  final object F32                         extends F(32)
  final object F64                         extends F(64)

  // TODO: change from [ty x N] to [N x ty]
  final case class Array   (ty: Type, n: Int)           extends Type
  final case class Ptr     (ty: Type)                   extends Type
  final case class Function(args: Seq[Type], ret: Type) extends Type
  final case class Struct  (name: Global)               extends Type

  // high-level types
  sealed abstract class ClassKind               extends Type
  final case object Unit                        extends Type
  final case object Nothing                     extends Type
  final case object Null                        extends ClassKind
  final case class Class(name: Global)          extends ClassKind
  final case class InterfaceClass(name: Global) extends ClassKind
  final case class ModuleClass(name: Global)    extends ClassKind
  final case class ArrayClass(ty: Type)         extends ClassKind
}
