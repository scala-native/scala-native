package native
package nir
package plugin

import scala.tools.nsc._

trait GenTypeKinds extends SubComponent with NativeBuiltins {
  import global._, definitions._

  def genClassName(sym: Symbol): nir.Global

  sealed abstract class Kind
  final case class PrimitiveKind(sym: Symbol) extends Kind
  final case class BottomKind(sym: Symbol) extends Kind
  final case class BuiltinClassKind(sym: Symbol) extends Kind
  final case class ClassKind(sym: Symbol) extends Kind
  final case class ArrayKind(of: Kind) extends Kind

  val UnitKind     = PrimitiveKind(UnitClass)
  val BooleanKind  = PrimitiveKind(BooleanClass)
  val ByteKind     = PrimitiveKind(ByteClass)
  val CharKind     = PrimitiveKind(CharClass)
  val ShortKind    = PrimitiveKind(ShortClass)
  val IntKind      = PrimitiveKind(IntClass)
  val LongKind     = PrimitiveKind(LongClass)
  val FloatKind    = PrimitiveKind(FloatClass)
  val DoubleKind   = PrimitiveKind(DoubleClass)

  val NullKind     = BottomKind(NullClass)
  val NothingKind  = BottomKind(NothingClass)

  val JObjectKind  = BuiltinClassKind(ObjectClass)
  val JClassKind   = BuiltinClassKind(ClassClass)
  val JStringKind  = BuiltinClassKind(StringClass)
  val JCharKind    = BuiltinClassKind(BoxedCharacterClass)
  val JBooleanKind = BuiltinClassKind(BoxedBooleanClass)
  val JByteKind    = BuiltinClassKind(BoxedByteClass)
  val JShortKind   = BuiltinClassKind(BoxedShortClass)
  val JIntKind     = BuiltinClassKind(BoxedIntClass)
  val JLongKind    = BuiltinClassKind(BoxedLongClass)
  val JFloatKind   = BuiltinClassKind(BoxedFloatClass)
  val JDoubleKind  = BuiltinClassKind(BoxedDoubleClass)

  def genRefKind(sym: Symbol, targs: List[Type] = Nil): Kind = sym match {
    case NullClass           => NullKind
    case NothingClass        => NothingKind
    case ArrayClass          => ArrayKind(genKind(targs.head))
    case ObjectClass         => JObjectKind
    case ClassClass          => JClassKind
    case StringClass         => JStringKind
    case BoxedCharacterClass => JCharKind
    case BoxedBooleanClass   => JBooleanKind
    case BoxedByteClass      => JByteKind
    case BoxedShortClass     => JShortKind
    case BoxedIntClass       => JIntKind
    case BoxedLongClass      => JLongKind
    case BoxedFloatClass     => JFloatKind
    case BoxedDoubleClass    => JDoubleKind
    case _                   => ClassKind(sym)
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

  def genKind(t: Type): Kind =  t.normalize match {
    case ThisType(ArrayClass)            => JObjectKind
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
        case NullClass    => nir.Type.NullClass
        case NothingClass => nir.Type.Nothing
      }
    case BuiltinClassKind(sym) =>
      sym match {
        case ObjectClass         => nir.Type.ObjectClass
        case ClassClass          => nir.Type.ClassClass
        case StringClass         => nir.Type.StringClass
        case BoxedCharacterClass => nir.Type.CharacterClass
        case BoxedBooleanClass   => nir.Type.BooleanClass
        case BoxedByteClass      => nir.Type.ByteClass
        case BoxedShortClass     => nir.Type.ShortClass
        case BoxedIntClass       => nir.Type.IntegerClass
        case BoxedLongClass      => nir.Type.LongClass
        case BoxedFloatClass     => nir.Type.FloatClass
        case BoxedDoubleClass    => nir.Type.DoubleClass
      }
    case ClassKind(sym) =>
      val name = genClassName(sym)
      if (isModule(sym))
        nir.Type.ModuleClass(name)
      else if (sym.isInterface)
        nir.Type.InterfaceClass(name)
      else
        nir.Type.Class(name)
    case ArrayKind(of) =>
      nir.Type.ArrayClass(toIRType(of))
  }

  def isModule(sym: Symbol): Boolean =
    sym.isModuleClass || sym.isImplClass

  def isForeignExternModule(sym: Symbol): Boolean =
    isModule(sym) && sym.annotations.find(_.tpe =:= ExternClass.tpe).isDefined
}
