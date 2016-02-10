package native
package nir

sealed abstract class Type {
  def unboxed = this match {
    case Type.CharacterClass => Type.I16
    case Type.BooleanClass   => Type.Bool
    case Type.ByteClass      => Type.I8
    case Type.ShortClass     => Type.I16
    case Type.IntegerClass   => Type.I32
    case Type.LongClass      => Type.I64
    case Type.FloatClass     => Type.F32
    case Type.DoubleClass    => Type.F64
    case _                   => util.unreachable
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
  final case object Unit    extends Type
  final case object Nothing extends Type

  sealed abstract class ClassKind               extends Type
  sealed abstract class BuiltinClassKind        extends ClassKind
  final case object NullClass                   extends BuiltinClassKind // #null
  final case object ObjectClass                 extends BuiltinClassKind // #object
  final case object ClassClass                  extends BuiltinClassKind // #class
  final case object StringClass                 extends BuiltinClassKind // #string
  final case object CharacterClass              extends BuiltinClassKind // #character
  final case object BooleanClass                extends BuiltinClassKind // #boolean
  final case object ByteClass                   extends BuiltinClassKind // #byte
  final case object ShortClass                  extends BuiltinClassKind // #short
  final case object IntegerClass                extends BuiltinClassKind // #integer
  final case object LongClass                   extends BuiltinClassKind // #long
  final case object FloatClass                  extends BuiltinClassKind // #float
  final case object DoubleClass                 extends BuiltinClassKind // #double
  final case class Class(name: Global)          extends ClassKind
  final case class InterfaceClass(name: Global) extends ClassKind
  final case class ModuleClass(name: Global)    extends ClassKind
  final case class ArrayClass(ty: Type)         extends ClassKind
}
