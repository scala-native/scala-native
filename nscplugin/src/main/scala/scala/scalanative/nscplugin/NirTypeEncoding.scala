package scala.scalanative
package nscplugin

import scala.tools.nsc._

trait NirTypeEncoding { self: NirCodeGen =>
  import global._
  import definitions._
  import nirAddons._
  import nirDefinitions._

  def genClassName(sym: Symbol): nir.Global

  def genArrayCode(tpe: Type): Char = {
    val (ArrayClass, Seq(targ)) = decomposeType(tpe)

    genPrimCode(targ)
  }

  /** Converts a type to its type symbol and type arguments. */
  def decomposeType(t: Type): (Symbol, Seq[Type]) = t.normalize match {
    case ThisType(ArrayClass)            => (ObjectClass, Seq())
    case ThisType(sym)                   => (sym, Seq())
    case SingleType(_, sym)              => (sym, Seq())
    case ConstantType(_)                 => decomposeType(t.underlying)
    case TypeRef(_, sym, args)           => (sym, args)
    case ClassInfoType(_, _, ArrayClass) => abort("ClassInfoType to ArrayClass!")
    case ClassInfoType(_, _, sym)        => (sym, Seq())
    case t: AnnotatedType                => decomposeType(t.underlying)
    case tpe: ErasedValueType            => (tpe.valueClazz, Seq())
  }

	def genType(t: Type): nir.Type = {
    val (sym, args) = decomposeType(t)

    genType(sym, args)
  }

  def genType(sym: Symbol, targs: Seq[Type] = Seq()): nir.Type = sym match {
    case ArrayClass           => genType(NArrayClass(genPrimCode(targs.head)))
    case ObjectClass          => nir.Rt.Object
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

  def genPrimCode(tpe: Type): Char = {
    val (sym, _) = decomposeType(tpe)

    genPrimCode(sym)
  }

  def genPrimCode(elem: Symbol): Char = elem match {
    case CharClass    => 'C'
    case BooleanClass => 'B'
    case ByteClass    => 'Z'
    case ShortClass   => 'S'
    case IntClass     => 'I'
    case LongClass    => 'L'
    case FloatClass   => 'F'
    case DoubleClass  => 'D'
    case _            => 'O'
  }

  def isModule(sym: Symbol): Boolean =
    sym.isModule || sym.isModuleClass || sym.isImplClass

  def isExternalModule(sym: Symbol): Boolean =
    isModule(sym) && sym.annotations.exists(_.symbol == ExternClass)
}
