package scala.scalanative
package nscplugin

import scala.tools.nsc._

trait NirTypeEncoding extends SubComponent {
  import global._, definitions._

  def genClassName(sym: Symbol): nir.Global

  private def genSymType(sym: Symbol, targs: List[Type] = Nil) = sym match {
    case ArrayClass           => nir.Nrt.Array(genArrayElementCode(targs.head))
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

	def genType(t: Type): nir.Type = t.normalize match {
    case ThisType(ArrayClass)            => nir.Nrt.Object
    case ThisType(sym)                   => genSymType(sym)
    case SingleType(_, sym)              => genSymType(sym)
    case ConstantType(_)                 => genType(t.underlying)
    case TypeRef(_, sym, args)           => genSymType(sym, args)
    case ClassInfoType(_, _, ArrayClass) => abort("ClassInfoType to ArrayClass!")
    case ClassInfoType(_, _, sym)        => genSymType(sym)
    case t: AnnotatedType                => genType(t.underlying)
    case tpe: ErasedValueType            => genSymType(tpe.valueClazz)
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
