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
    case _                   => native.util.unreachable
  }
}
object Type {
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

  final case class Array   (ty: Type, n: Int)           extends Type
  final case class Ptr     (ty: Type)                   extends Type
  final case class Function(args: Seq[Type], ret: Type) extends Type
  final case class Struct  (name: Name)                 extends Type

  // scala
  final case object Unit                extends Type
  final case object Nothing             extends Type
  final case object NullClass           extends Type
  final case object ObjectClass         extends Type
  final case object ClassClass          extends Type
  final case object StringClass         extends Type
  final case object CharacterClass      extends Type
  final case object BooleanClass        extends Type
  final case object ByteClass           extends Type
  final case object ShortClass          extends Type
  final case object IntegerClass        extends Type
  final case object LongClass           extends Type
  final case object FloatClass          extends Type
  final case object DoubleClass         extends Type
  final case class Class(name: Name)    extends Type
  final case class ArrayClass(ty: Type) extends Type
}
