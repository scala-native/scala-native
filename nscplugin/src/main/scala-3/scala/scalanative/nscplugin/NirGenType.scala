package scala.scalanative
package nscplugin

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
import core.TypeError
import dotty.tools.dotc.report
import dotty.tools.dotc.typer.TyperPhase
import scala.scalanative.nscplugin.CompilerCompat.SymUtilsCompat.*

import scala.scalanative.util.unsupported

trait NirGenType(using Context) {
  self: NirCodeGen =>

  private lazy val UnsignedTypes = Set(
    defnNir.UByteClass,
    defnNir.UShortClass,
    defnNir.UIntClass,
    defnNir.ULongClass,
    defnNir.USizeClass
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
      sym.owner.isExternType ||
      sym.hasAnnotation(defnNir.ExternClass) ||
      (sym.is(Accessor) && sym.field.isExtern)
      // NonExtern is added PrepNativeInterop
    } && !sym.hasAnnotation(defnNir.NonExternClass)

    def isExtensionMethod: Boolean =
      sym.flags.isAllOf(Extension | Method) || {
        sym.flags.isAllOf(Final | Implicit | Method)
      }

    def isExternType: Boolean =
      (isScalaModule || sym.isTraitOrInterface) &&
        sym.hasAnnotation(defnNir.ExternClass)

    def isBlocking: Boolean =
      sym.exists && sym.hasAnnotation(defnNir.BlockingClass)

    def isStruct: Boolean =
      sym.hasAnnotation(defnNir.StructClass)

    def isAnonymousStruct: Boolean = defnNir.CStructClasses.contains(sym)

    def isFixedSizeArray: Boolean = sym == defnNir.CArrayClass

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
        // t.typeSymbol may not be a ClassSymbol when it is an opaque type
        // https://github.com/scala-native/scala-native/issues/3700
        case t => t.typeSymbol.isPrimitiveValueClass
      }
    }
    def isUnsignedType: Boolean =
      tpe.typeSymbol.isClass && UnsignedTypes.contains(tpe.typeSymbol.asClass)

    def isAnonymousStruct: Boolean =
      defnNir.CStructClasses.contains(tpe.typeSymbol)

    def isFixedSizeArray: Boolean = tpe.typeSymbol == defnNir.CArrayClass
  end extension

  def genBoxType(tpe: Type): nir.Type =
    val nirType = genType(tpe)
    BoxTypesForPrimitive.getOrElse(nirType, genRefType(tpe))

  private lazy val BoxTypesForPrimitive = Map[nir.Type, nir.Type](
    nir.Type.Char -> genRefType(defn.BoxedCharClass.info),
    nir.Type.Bool -> genRefType(defn.BoxedBooleanClass.info),
    nir.Type.Byte -> genRefType(defn.BoxedByteClass.info),
    nir.Type.Short -> genRefType(defn.BoxedShortClass.info),
    nir.Type.Int -> genRefType(defn.BoxedIntClass.info),
    nir.Type.Long -> genRefType(defn.BoxedLongClass.info),
    nir.Type.Float -> genRefType(defn.BoxedFloatClass.info),
    nir.Type.Double -> genRefType(defn.BoxedDoubleClass.info),
    nir.Type.Unit -> nir.Rt.BoxedUnit
  )

  lazy val jlStringBuilderAppendForSymbol = defnNir.jlStringBuilderAppendAlts
    .flatMap(sym =>
      val sig = genMethodSig(sym)
      def name = genMethodName(sym)
      sig match
        case nir.Type.Function(Seq(_, arg), _) =>
          Some(
            nir.Type.normalize(arg) -> (nir.Val.Global(name, nir.Type.Ptr), sig)
          )
        case _ => None
    )
    .toMap

  def genExternType(tpe: Type): nir.Type = {
    if (tpe.widenDealias.typeSymbol.isCFuncPtrClass)
      nir.Type.Ptr
    else
      genType(tpe) match {
        case refty: nir.Type.Ref if nir.Type.boxClasses.contains(refty.name) =>
          nir.Type.unbox(nir.Type.Ref(refty.name))
        case ty =>
          ty
      }
  }

  inline def genType(
      tpe: Type,
      deconstructValueTypes: Boolean = false
  ): nir.Type = genNIRType { sym =>
    PrimitiveSymbolToNirTypes
      .get(sym)
      .getOrElse {
        if sym.isStruct then genStruct(tpe)
        else if deconstructValueTypes then {
          if sym.isAnonymousStruct then genAnonymousStruct(tpe)
          else if sym.isFixedSizeArray then genFixedSizeArray(tpe)
          else {
            val ref = nir.Type.Ref(genTypeName(sym))
            nir.Type.unbox.getOrElse(nir.Type.normalize(ref), ref)
          }
        } else nir.Type.Ref(genTypeName(sym))
      }
  }(tpe)

  private lazy val PrimitiveSymbolToNirTypes = Map[Symbol, nir.Type](
    defn.CharClass -> nir.Type.Char,
    defn.BooleanClass -> nir.Type.Bool,
    defn.ByteClass -> nir.Type.Byte,
    defn.ShortClass -> nir.Type.Short,
    defn.IntClass -> nir.Type.Int,
    defn.LongClass -> nir.Type.Long,
    defn.FloatClass -> nir.Type.Float,
    defn.DoubleClass -> nir.Type.Double,
    defn.UnitClass -> nir.Type.Unit,
    defn.NullClass -> nir.Type.Null,
    defn.NothingClass -> nir.Type.Nothing,
    defnNir.RawPtrClass -> nir.Type.Ptr,
    defnNir.RawSizeClass -> nir.Type.Size
  )

  def genRefType(tpe: Type): nir.Type.RefKind =
    genNIRType { sym =>
      if sym.isPrimitiveValueClass then genBoxType(sym.info)
      else if sym == defn.NothingClass then nir.Rt.RuntimeNothing
      else if sym == defn.NullClass then nir.Rt.RuntimeNull
      else nir.Type.Ref(genTypeName(sym))
    }(tpe) match
      case t: nir.Type.RefKind => t
      case t => unsupported("Unexpected non ref kind type - $t")

  private def genNIRType[T](toNIRType: Symbol => nir.Type)(
      tpe: Type
  ): nir.Type = {
    inline def fromSymbol(sym: Symbol) =
      if sym == defn.AnyClass || sym == defn.ObjectClass then nir.Rt.Object
      else toNIRType(sym)
    inline def recurse(t: Type) = genNIRType(toNIRType)(t)

    tpe.widenDealias match {
      // Array type such as Array[Int] (kept by erasure)
      case JavaArrayType(el) => nir.Type.Array(genType(el))
      case defn.ArrayOf(el)  => nir.Type.Array(genType(el))

      case t: TypeRef =>
        // See comment on nonClassTypeRefToBType in Scala JVM backend BCodeHelpers
        if (!t.symbol.isClass) nir.Rt.Object
        else fromSymbol(t.symbol)

      case ClassInfo(_, sym, _, _, _) => fromSymbol(sym)

      /* AnnotatedType should (probably) be eliminated by erasure. However we know it happens for
       * meta-annotated annotations (@(ann @getter) val x = 0), so we don't emit a warning.
       * The type in the AnnotationInfo is an AnnotatedTpe.
       */
      case AnnotatedType(t, _) => recurse(t)
      case AppliedType(t, _)   => fromSymbol(t.typeSymbol)
      case WildcardType        => nir.Rt.Object
      case TypeBounds(_, hi)   => recurse(hi)
    }
  }

  def genTypeValue(tpe: Type): nir.Val =
    // FIXME: Backward compatibility for 0.5.x series - we're generating scala.scalanative.runtime.PrimitiveX types instead of proper ones
    val refType =
      defnNir.RuntimePrimitive
        .get(tpe.typeSymbol)
        .map(_.info)
        .getOrElse(tpe)
    nir.Val.ClassOf(genRefType(refType).className)

  private def genAnonymousStruct(tpe: Type): nir.Type =
    nir.Type.StructValue(
      tpe.argTypes
        .map(genType(_, deconstructValueTypes = true))
    )

  private def genStruct(tpe: Type): nir.Type = {
    // In Scala 2 we used fields to create struct type, but this seems to be broken in Scala 3 -
    // when compiling original file (e.g. in nativelib) we do get correct list of fields,
    // however in the place of usage in other project (e.g. javalib) symbol info does contain only accessors,
    // but no information about fields.
    // .class field do contain information about fields, so probably TASTy (which is used)
    // in compilation in dependent projects does not contain this information
    // Since structs in the current form are a legacy feature, and are used only to
    // receive output from native function returning Struct by value (only in LLVMIntriniscs)
    // we can leave it as it is in the current, simplified form using constructor arguments
    def ctorParams =
      tpe
        .member(nme.CONSTRUCTOR)
        .symbol
        .paramSymss
        .head
        .map(_.info.resultType)
        .map(genType(_))

    nir.Type.StructValue(ctorParams)
  }

  private def genFixedSizeArray(tpe: Type): nir.Type = {
    def parseDigit(tpe: Type): Int = {
      val sym = tpe.widenDealias.typeSymbol
      try defnNir.NatBaseClasses.indexOf(sym)
      catch {
        case e: TypeError =>
          // Can happen when Nat class is not yet availble, etc. usages withing nativelib
          sym.name.toSimpleName.toString match
            case s"Nat$$_${digit}" if digit.length == 1 =>
              digit.toIntOption.getOrElse(throw e)
            case _ => throw e
      }
    }
    def natClassToInt(tpe: Type): Int =
      if tpe.argTypes.isEmpty then parseDigit(tpe)
      else
        tpe.argTypes.foldLeft(0) {
          case (acc, tpe) => acc * 10 + parseDigit(tpe)
        }

    val List(elemType, size) = tpe.argTypes: @unchecked
    nir.Type
      .ArrayValue(
        genType(elemType, deconstructValueTypes = true),
        natClassToInt(size)
      )
      .ensuring(
        _.n >= 0,
        s"fixed size array size needs to be positive integer, got ${size.show}"
      )
  }

  def genMethodSig(
      sym: Symbol,
      statically: Boolean = false
  ): nir.Type.Function =
    genMethodSigImpl(sym, statically = statically, isExtern = false)

  def genExternMethodSig(sym: Symbol): nir.Type.Function =
    genMethodSigImpl(sym, isExtern = true, statically = true)

  private def genMethodSigImpl(
      sym: Symbol,
      isExtern: Boolean,
      statically: Boolean
  ): nir.Type.Function = {
    def resolve() = {
      require(
        sym.is(Method) || sym.isStatic,
        s"symbol ${sym.owner} $sym is not a method"
      )

      val owner = sym.owner
      val paramtys = genMethodSigParamsImpl(sym, isExtern)
      val selfty = Option.unless(statically || isExtern || sym.isStaticInNIR) {
        genType(owner.info.resultType)
      }
      val resultType = sym.info.resultType
      val retty =
        if (sym.isConstructor) nir.Type.Unit
        else if (isExtern) genExternType(resultType)
        else genType(resultType)
      nir.Type.Function(selfty ++: paramtys, retty)
    }

    cachedMethodSig.getOrElseUpdate((sym, isExtern, statically), resolve())
  }

  def genMethodSigParamsImpl(
      sym: Symbol,
      isExternHint: Boolean
  )(using Context): Seq[nir.Type] = {
    import core.Phases._
    val isExtern = isExternHint || sym.isExtern
    val repeatedParams = if (isExtern) {
      atPhase(typerPhase) {
        sym.paramInfo.stripPoly match {
          // @extern def foo(a: Int): Int
          case MethodTpe(paramNames, paramTypes, _) =>
            for (name, tpe) <- paramNames zip paramTypes
            yield name -> tpe.isRepeatedParam
          case t if t.isVarArgsMethod =>
            report.warning(
              "Unable to resolve method sig params for symbol, extern VarArgs would not work",
              sym.srcPos
            )
            Nil
          case _ => Nil
        }
      }.toMap
    } else Map.empty

    val info = sym.info
    for {
      (paramTypes, paramNames) <- info.paramInfoss zip info.paramNamess
      (paramType, paramName) <- paramTypes zip paramNames
    } yield {
      def isRepeated = repeatedParams.getOrElse(paramName, false)
      if (isExtern && isRepeated) nir.Type.Vararg
      else if (isExtern) genExternType(paramType)
      else genType(paramType)
    }
  }
}
