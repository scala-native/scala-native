package native
package nir
package plugin

import scala.tools.nsc._

trait GenTypeKinds extends SubComponent {
  import global._, definitions._

  def genClassName(sym: Symbol): nir.Name

  sealed abstract class Kind
  final case class PrimitiveKind(sym: Symbol) extends Kind
  final case class BottomKind(sym: Symbol) extends Kind
  final case class ClassKind(sym: Symbol) extends Kind
  final case class ArrayKind(of: Kind) extends Kind

  val ObjectKind  = ClassKind(ObjectClass)

  val NullKind    = BottomKind(NullClass)
  val NothingKind = BottomKind(NothingClass)

  val UnitKind    = PrimitiveKind(UnitClass)
  val BooleanKind = PrimitiveKind(BooleanClass)
  val ByteKind    = PrimitiveKind(ByteClass)
  val CharKind    = PrimitiveKind(CharClass)
  val ShortKind   = PrimitiveKind(ShortClass)
  val IntKind     = PrimitiveKind(IntClass)
  val LongKind    = PrimitiveKind(LongClass)
  val FloatKind   = PrimitiveKind(FloatClass)
  val DoubleKind  = PrimitiveKind(DoubleClass)

  def genRefKind(sym: Symbol, targs: List[Type] = Nil): Kind = sym match {
    case NullClass    => NullKind
    case NothingClass => NothingKind
    case ArrayClass   => ArrayKind(genKind(targs.head))
    case _            => ClassKind(sym)
  }

  lazy val genPrimitiveKind: PartialFunction[Symbol, Kind] = {
    case UnitClass    => UnitKind
    case BooleanClass => BooleanKind
    case ByteClass    => ByteKind
    case CharClass    => CharKind
    case ShortClass   => ShortKind
    case IntClass     => IntKind
    case LongClass    => LongKind
    case FloatClass   => FloatKind
    case DoubleClass  => DoubleKind
  }

  def genPrimitiveOrRefKind(sym: Symbol, targs: List[Type] = Nil): Kind =
    genPrimitiveKind.applyOrElse(sym, genRefKind((_: Symbol), targs))

  def genKind(t: Type): Kind = t.normalize match {
    case ThisType(ArrayClass)            => ObjectKind
    case ThisType(sym)                   => genRefKind(sym)
    case SingleType(_, sym)              => genPrimitiveOrRefKind(sym)
    case ConstantType(_)                 => genKind(t.underlying)
    case TypeRef(_, sym, args)           => genPrimitiveOrRefKind(sym, args)
    case ClassInfoType(_, _, ArrayClass) => abort("ClassInfoType to ArrayClass!")
    case ClassInfoType(_, _, sym)        => genPrimitiveOrRefKind(sym)
    case t: AnnotatedType                => genKind(t.underlying)
    case tpe: ErasedValueType            => genRefKind(tpe.valueClazz)
  }

  def genType(tpe: Type): nir.Type = toIRType(genKind(tpe))

  def toIRType(kind: Kind): nir.Type = kind match {
    case PrimitiveKind(sym) =>
      sym match {
        case UnitClass    => nir.Type.Unit
        case BooleanClass => nir.Type.Bool
        case ByteClass    => nir.Type.I8
        case CharClass    => nir.Type.I16
        case ShortClass   => nir.Type.I16
        case IntClass     => nir.Type.I32
        case LongClass    => nir.Type.I64
        case FloatClass   => nir.Type.F32
        case DoubleClass  => nir.Type.F64
      }
    case BottomKind(sym) =>
      sym match {
        case NullClass    => nir.Type.Null
        case NothingClass => nir.Type.Nothing
      }
    case ClassKind(sym) => nir.Type.Class(genClassName(sym))
    case ArrayKind(of)  => nir.Type.ArrayClass(toIRType(of))
  }
}
