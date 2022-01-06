package scala.scalanative.nscplugin
import scala.language.implicitConversions

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.ast.Trees._
import dotty.tools.dotc.core
import core.Contexts._
import core.Flags._
import core.Names._
import core.Types._
import core.Symbols._
import core.StdNames._
import core.TypeErasure._
import dotty.tools.dotc.transform.SymUtils._

import scala.scalanative.nir

trait NirGenType(using Context) {
  self: NirCodeGen =>

  private lazy val UnsignedTypes = Set(
    defnNir.UByteClass,
    defnNir.UShortClass,
    defnNir.UIntClass,
    defnNir.ULongClass
  )

  extension (sym: Symbol)
    def isTraitOrInterface: Boolean =
      sym.is(Trait) || sym.isAllOf(JavaInterface)

    def isScalaModule: Boolean =
      sym.is(ModuleClass, butNot = Lifted)

    def isStaticModule: Boolean =
      sym.is(Module) && sym.isStatic

    def isStaticMethod: Boolean =
      sym.is(Method) && {
        sym.is(JavaStatic) || sym.isScalaStatic
      }

    def isStaticInNIR: Boolean =
      sym.is(JavaStatic) || sym.isScalaStatic || sym.isExtern

    def isExtern: Boolean = sym.exists && {
      sym.owner.isExternModule ||
      sym.hasAnnotation(defnNir.ExternClass) ||
      (sym.is(Accessor) && sym.field.isExtern)
    }

    def isExternModule: Boolean =
      isScalaModule && sym.hasAnnotation(defnNir.ExternClass)

    def isStruct: Boolean =
      sym.hasAnnotation(defnNir.StructClass)

    def isUnsignedType: Boolean =
      sym.isClass && UnsignedTypes.contains(sym.asClass)

    /** Tests if this type inherits from CFuncPtr */
    def isCFuncPtrClass: Boolean = {
      sym == defnNir.CFuncPtrClass ||
      sym.info.parents.exists(_.typeSymbol == defnNir.CFuncPtrClass)
    }

    /** Tests if this type is implementations of CFuncPtr */
    def isCFuncPtrNClass: Boolean = {
      defnNir.CFuncPtrNClass.contains(sym) || {
        sym.info.parents.exists { parent =>
          defnNir.CFuncPtrNClass.contains(parent.typeSymbol)
        }
      }
    }
  end extension

  extension (tpe: Type)
    def isPrimitiveValueType: Boolean = {
      tpe.widenDealias match {
        case JavaArrayType(_)   => false
        case _: ErasedValueType => false
        case t                  => t.typeSymbol.asClass.isPrimitiveValueClass
      }
    }
  end extension

  sealed case class SimpleType(
      sym: Symbol,
      targs: Seq[SimpleType] = Seq.empty
  )

  given fromSymbol: Conversion[Symbol, SimpleType] = { sym =>
    SimpleType(sym, sym.typeParams.map(fromSymbol))
  }
  given fromType: Conversion[Type, SimpleType] = {
    _.widenDealias match {
      case ThisType(tref) =>
        if (tref == defn.ArrayType)
          SimpleType(defn.ObjectClass, Nil)
        else
          SimpleType(tref.symbol, Nil)
      case JavaArrayType(elemTpe) =>
        SimpleType(defn.ArrayClass, fromType(elemTpe) :: Nil)
      case ConstantType(c)            => fromType(c.tpe)
      case ClassInfo(_, sym, _, _, _) => fromSymbol(sym)
      case t @ TypeRef(tpe, _) =>
        SimpleType(t.symbol, tpe.argTypes.map(fromType))
      case t @ TermRef(_, _) => fromType(t.info.resultType)
      case t => throw new RuntimeException(s"unknown fromType($t)")
    }
  }

  def genBoxType(st: SimpleType): nir.Type =
    BoxTypesForSymbol.getOrElse(st.sym.asClass, genType(st))

  private lazy val BoxTypesForSymbol = Map(
    defn.CharClass -> genType(defn.BoxedCharClass),
    defn.BooleanClass -> genType(defn.BoxedBooleanClass),
    defn.ByteClass -> genType(defn.BoxedByteClass),
    defn.ShortClass -> genType(defn.BoxedShortClass),
    defn.IntClass -> genType(defn.BoxedIntClass),
    defn.LongClass -> genType(defn.BoxedLongClass),
    defn.FloatClass -> genType(defn.BoxedFloatClass),
    defn.DoubleClass -> genType(defn.BoxedDoubleClass)
  )

  def genExternType(st: SimpleType): nir.Type = {
    if (st.sym.isCFuncPtrClass)
      nir.Type.Ptr
    else
      genType(st) match {
        case refty: nir.Type.Ref if nir.Type.boxClasses.contains(refty.name) =>
          nir.Type.unbox(nir.Type.Ref(refty.name))
        case ty =>
          ty
      }
  }

  @inline
  def genType(st: SimpleType): nir.Type = {
    PrimitiveSymbolToNirTypes.getOrElse(st.sym, genRefType(st))
  }

  private lazy val PrimitiveSymbolToNirTypes = Map[Symbol, nir.Type](
    defn.CharClass -> nir.Type.Char,
    defn.BooleanClass -> nir.Type.Bool,
    defn.ByteClass -> nir.Type.Byte,
    defn.ShortClass -> nir.Type.Short,
    defn.IntClass -> nir.Type.Int,
    defn.LongClass -> nir.Type.Long,
    defn.FloatClass -> nir.Type.Float,
    defn.DoubleClass -> nir.Type.Double,
    defn.NullClass -> nir.Type.Null,
    defn.NothingClass -> nir.Type.Nothing,
    defnNir.RawPtrClass -> nir.Type.Ptr
  )

  def genRefType(st: SimpleType): nir.Type = {
    val SimpleType(sym, targs) = st
    if (sym == defn.ObjectClass) nir.Rt.Object
    else if (sym == defn.UnitClass) nir.Type.Unit
    else if (sym == defn.BoxedUnitClass) nir.Rt.BoxedUnit
    else if (sym == defn.NullClass) genRefType(defn.NullClass)
    else if (sym == defn.ArrayClass) nir.Type.Array(genType(targs.head))
    else if (sym.isStruct) genStruct(st)
    else nir.Type.Ref(genTypeName(sym))
  }

  def genTypeValue(st: SimpleType): nir.Val =
    if (st.sym == defn.UnitClass)
      genTypeValue(defnNir.RuntimePrimitive('U'))
    else if (st.sym == defn.ArrayClass)
      genTypeValue(defnNir.RuntimeArrayClass(genPrimCode(st.targs.head)))
    else
      genPrimCode(st) match {
        case 'O'  => nir.Val.ClassOf(genTypeName(st.sym))
        case code => genTypeValue(defnNir.RuntimePrimitive(code))
      }

  private def genStruct(st: SimpleType): nir.Type = {
    val symInfo = st.sym.info
    // In Scala 2 we used fields to create struct type, but this seems to be broken in Scala 3 -
    // when compiling original file (eg. in nativelib) we do get correct list of fields,
    // however in the place of usage in other project (eg. javalib) symbol info does contain only accessors,
    // but no information about fields.
    // .class fiele do contain information about fields, so probablly TASTy (which is used)
    // in compilation in dependent projects does not contains this information
    // Since structs in the current form are a legacy feature, and are used only to
    // receive output from native function returning Struct by value (only in LLVMIntriniscs)
    // we can leave it as it is in the current, simplified form using constructor arguments

    def ctorParams =
      symInfo
        .member(nme.CONSTRUCTOR)
        .symbol
        .paramSymss
        .head
        .map(_.info.resultType)
        .map(genType(_))

    nir.Type.StructValue(ctorParams)
  }

  def genArrayCode(st: SimpleType): Char =
    genPrimCode(st.targs.head)

  def genPrimCode(st: SimpleType): Char =
    SymbolToPrimCode.getOrElse(st.sym, 'O')

  private lazy val SymbolToPrimCode: Map[Symbol, Char] = Map(
    defn.CharClass -> 'C',
    defn.BooleanClass -> 'B',
    defn.ByteClass -> 'Z',
    defn.ShortClass -> 'S',
    defn.IntClass -> 'I',
    defn.LongClass -> 'L',
    defn.FloatClass -> 'F',
    defn.DoubleClass -> 'D'
  )

  def genMethodSig(sym: Symbol): nir.Type.Function =
    genMethodSigImpl(sym, isExtern = false)

  def genExternMethodSig(sym: Symbol): nir.Type.Function =
    genMethodSigImpl(sym, isExtern = true)

  private def genMethodSigImpl(
      sym: Symbol,
      isExtern: Boolean
  ): nir.Type.Function = {
    require(
      sym.is(Method) || sym.isStatic,
      s"symbol ${sym.owner} $sym is not a method"
    )

    val owner = sym.owner
    val paramtys = genMethodSigParamsImpl(sym, isExtern)
    val selfty = Option.unless(isExtern || sym.isStaticInNIR) {
      genType(owner)
    }
    val resultType = sym.info.resultType
    val retty =
      if (sym.isConstructor) nir.Type.Unit
      else if (isExtern) genExternType(resultType)
      else genType(resultType)

    nir.Type.Function(selfty ++: paramtys, retty)
  }

  private def genMethodSigParamsImpl(
      sym: Symbol,
      isExtern: Boolean
  ): Seq[nir.Type] = {

    for {
      paramList <- sym.info.paramInfoss
      param <- paramList
    } yield {
      if (param.isRepeatedParam && sym.isExtern)
        nir.Type.Vararg
      else if (isExtern) genExternType(param)
      else genType(param)
    }
  }
}
