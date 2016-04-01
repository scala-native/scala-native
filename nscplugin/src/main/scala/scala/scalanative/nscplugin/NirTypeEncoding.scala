package scala.scalanative
package nscplugin

import scala.tools.nsc._

trait NirTypeEncoding extends NirDefinitions {
  import global._, definitions._

  def genClassName(sym: Symbol): nir.Global

  def extractType(t: Type): (Symbol, Seq[Type]) = t.normalize match {
    case ThisType(ArrayClass)            => (ObjectClass, Seq())
    case ThisType(sym)                   => (sym, Seq())
    case SingleType(_, sym)              => (sym, Seq())
    case ConstantType(_)                 => extractType(t.underlying)
    case TypeRef(_, sym, args)           => (sym, args)
    case ClassInfoType(_, _, ArrayClass) => abort("ClassInfoType to ArrayClass!")
    case ClassInfoType(_, _, sym)        => (sym, Seq())
    case t: AnnotatedType                => extractType(t.underlying)
    case tpe: ErasedValueType            => (tpe.valueClazz, Seq())
  }

	def genType(t: Type): nir.Type = {
    val (sym, args) = extractType(t)

    genTypeInternal(sym, args)
  }

  private def genTypeInternal(sym: Symbol, targs: Seq[Type]) = sym match {
    case ArrayClass           => genArrayType(targs.head)
    case ObjectClass          => nir.Nrt.Object
    case UnitClass            => nir.Type.Unit
    case CharClass            => nir.Type.I16
    case BooleanClass         => nir.Type.Bool
    case ByteClass            => nir.Type.I8
    case ShortClass           => nir.Type.I16
    case IntClass             => nir.Type.I32
    case LongClass            => nir.Type.I64
    case FloatClass           => nir.Type.F32
    case DoubleClass          => nir.Type.F64
    case NullClass            => nir.Type.Null
    case NothingClass         => nir.Type.Nothing
    case _ if isModule(sym)   => nir.Type.Module(genClassName(sym))
    case _ if sym.isInterface => nir.Type.Trait(genClassName(sym))
    case _                    => nir.Type.Class(genClassName(sym))
  }

  private def genArrayType(targ: Type): nir.Type = {
    val (sym, _) = extractType(targ)
    val array    = sym match {
      case CharClass    => CharArrayClass
      case BooleanClass => BooleanArrayClass
      case ByteClass    => ByteArrayClass
      case ShortClass   => ShortArrayClass
      case IntClass     => IntArrayClass
      case LongClass    => LongArrayClass
      case FloatClass   => FloatArrayClass
      case DoubleClass  => DoubleArrayClass
      case _            => RefArrayClass
    }

    genType(array.info)
  }

  def isModule(sym: Symbol): Boolean =
    sym.isModule || sym.isModuleClass || sym.isImplClass

  def genArrayElementCode(tpe: Type): Char = ??? /*kind match {
    case ArrayKind(PrimitiveKind(CharClass   )) => 'C'
    case ArrayKind(PrimitiveKind(BooleanClass)) => 'B'
    case ArrayKind(PrimitiveKind(ByteClass   )) => 'Z'
    case ArrayKind(PrimitiveKind(ShortClass  )) => 'S'
    case ArrayKind(PrimitiveKind(IntClass    )) => 'I'
    case ArrayKind(PrimitiveKind(LongClass   )) => 'L'
    case ArrayKind(PrimitiveKind(FloatClass  )) => 'F'
    case ArrayKind(PrimitiveKind(DoubleClass )) => 'D'
    case _                                      => 'O'
  }*/
}
