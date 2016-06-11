package scala.scalanative
package nscplugin

import scala.tools.nsc._

trait NirTypeEncoding { self: NirCodeGen =>
  import global._
  import definitions._
  import nirAddons._
  import nirDefinitions._

  def genTypeName(sym: Symbol): nir.Global

  def genArrayCode(tpe: Type): Char = {
    val (ArrayClass, Seq(targ)) = decomposeType(tpe)

    genPrimCode(targ)
  }

  /** Converts a type to its type symbol and type arguments. */
  def decomposeType(t: Type): (Symbol, Seq[Type]) = t.normalize match {
    case ThisType(ArrayClass)  => (ObjectClass, Seq())
    case ThisType(sym)         => (sym, Seq())
    case SingleType(_, sym)    => (sym, Seq())
    case ConstantType(_)       => decomposeType(t.underlying)
    case TypeRef(_, sym, args) => (sym, args)
    case ClassInfoType(_, _, ArrayClass) =>
      abort("ClassInfoType to ArrayClass!")
    case ClassInfoType(_, _, sym) => (sym, Seq())
    case t: AnnotatedType         => decomposeType(t.underlying)
    case tpe: ErasedValueType     => (tpe.valueClazz, Seq())
  }

  def genType(t: Type, retty: Boolean = false): nir.Type = {
    val (sym, args) = decomposeType(t)

    genTypeSym(sym, args, retty)
  }

  def genTypeSym(sym: Symbol,
                 targs: Seq[Type] = Seq(),
                 retty: Boolean = false): nir.Type = sym match {
    case ArrayClass                 => genTypeSym(RuntimeArrayClass(genPrimCode(targs.head)))
    case UnitClass | BoxedUnitClass => nir.Type.Unit
    case NothingClass               => nir.Type.Nothing
    case NullClass                  => genTypeSym(RuntimeNullClass)
    case ObjectClass                => nir.Rt.Object
    case CharClass                  => nir.Type.I16
    case BooleanClass               => nir.Type.Bool
    case ByteClass                  => nir.Type.I8
    case ShortClass                 => nir.Type.I16
    case IntClass                   => nir.Type.I32
    case LongClass                  => nir.Type.I64
    case FloatClass                 => nir.Type.F32
    case DoubleClass                => nir.Type.F64
    case PtrClass                   => nir.Type.Ptr
    case _ if isStruct(sym)         => genStruct(sym)
    case _ if isModule(sym)         => nir.Type.Module(genTypeName(sym))
    case _ if sym.isInterface       => nir.Type.Trait(genTypeName(sym))
    case _                          => nir.Type.Class(genTypeName(sym))
  }

  def genTypeValue(ty: Type): nir.Val = {
    val (sym, _) = decomposeType(ty)
    genTypeSymValue(sym)
  }

  def genTypeSymValue(sym: Symbol): nir.Val =
    genPrimCode(sym) match {
      case 'O'  => nir.Val.Global(genTypeName(sym), nir.Type.Ptr)
      case code => genTypeSymValue(RuntimePrimitiveStruct(code))
    }

  def genStructFields(sym: Symbol): Seq[nir.Type] = {
    for {
      f <- sym.info.decls if isField(f)
    } yield {
      genType(f.tpe)
    }
  }.toSeq

  def genStruct(sym: Symbol): nir.Type = {
    val name   = genTypeName(sym)
    val fields = genStructFields(sym)

    nir.Type.Struct(name, fields)
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

  def isExternModule(sym: Symbol): Boolean =
    isModule(sym) && sym.annotations.exists(_.symbol == ExternClass)

  def isStruct(sym: Symbol): Boolean =
    sym.annotations.exists(_.symbol == StructClass)

  def isField(sym: Symbol): Boolean =
    !sym.isMethod && sym.isTerm && !isModule(sym)
}
