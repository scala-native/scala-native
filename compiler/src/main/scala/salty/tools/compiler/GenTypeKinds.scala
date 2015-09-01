package salty.tools
package compiler

import scala.tools.nsc._
import salty.ir
import salty.ir.{Type => Ty}

trait GenTypeKinds extends SubComponent with GenNameEncoding {
  import global._, definitions._

  sealed abstract class Kind
  final case class PrimitiveKind(sym: Symbol) extends Kind
  final case class BottomKind(sym: Symbol) extends Kind
  final case class ClassKind(sym: Symbol) extends Kind {
    def name = encodeClassName(sym)
  }
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

  def genType(tpe: Type): ir.Type = toIRType(genKind(tpe))

  def toIRType(kind: Kind): ir.Type = kind match {
    case PrimitiveKind(sym) =>
      sym match {
        case UnitClass    => Ty.Unit
        case BooleanClass => Ty.Bool
        case ByteClass    => Ty.I8
        case CharClass    => Ty.I16
        case ShortClass   => Ty.I16
        case IntClass     => Ty.I32
        case LongClass    => Ty.I64
        case FloatClass   => Ty.F32
        case DoubleClass  => Ty.F64
      }
    case BottomKind(sym) =>
      sym match {
        case NullClass    => Ty.Null
        case NothingClass => Ty.Nothing
      }
    case ClassKind(sym) =>
      Ty.Ref(Ty.Named(encodeClassName(sym)))
    case ArrayKind(of) =>
      Ty.Slice(toIRType(of))
  }
}
