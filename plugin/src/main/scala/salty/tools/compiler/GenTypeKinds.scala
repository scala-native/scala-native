package salty.tools
package compiler

import scala.tools.nsc._
import salty.ir

trait GenTypeKinds extends SubComponent {
  import global._, definitions._

  def genClassDefn(sym: Symbol): ir.Node

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

  def genType(tpe: Type): ir.Node = toIRType(genKind(tpe))

  def toIRType(kind: Kind): ir.Node = kind match {
    case PrimitiveKind(sym) =>
      sym match {
        case UnitClass    => ir.Builtin.Unit
        case BooleanClass => ir.Builtin.Bool
        case ByteClass    => ir.Builtin.I8
        case CharClass    => ir.Builtin.I16
        case ShortClass   => ir.Builtin.I16
        case IntClass     => ir.Builtin.I32
        case LongClass    => ir.Builtin.I64
        case FloatClass   => ir.Builtin.F32
        case DoubleClass  => ir.Builtin.F64
      }
    case BottomKind(sym) =>
      sym match {
        case NullClass    => ir.Builtin.Null
        case NothingClass => ir.Builtin.Nothing
      }
    case ClassKind(sym) => genClassDefn(sym)
    case ArrayKind(of) => ir.Defn.Slice(toIRType(of))
  }
}
