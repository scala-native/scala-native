package scala.scalanative
package nscplugin

import scala.tools.nsc.Global

trait NirGenType[G <: Global with Singleton] { self: NirGenPhase[G] =>
  import global._
  import global.definitions._

  import nirAddons._
  import nirDefinitions._

  implicit class SymbolExtensions(sym: Symbol) {
    def isInterface: Boolean =
      sym.isInterface

    def isScalaModule: Boolean =
      sym.isModuleClass && !sym.isLifted

    def isStaticInNIR: Boolean =
      sym.isExtern || sym.isStaticMember

    def isExternType: Boolean =
      (isScalaModule || sym.isTraitOrInterface) &&
        sym.annotations.exists(_.symbol == ExternClass)

    def isExtern: Boolean = (sym.isExternType || sym.owner.isExternType) &&
      !sym.annotations.exists(_.symbol == NonExternClass)

    def isBlocking: Boolean =
      sym.annotations.exists(_.symbol == BlockingClass)

    def isStruct: Boolean =
      sym.annotations.exists(_.symbol == StructClass)

    def isAnonymousStruct: Boolean =
      CStructClass.contains(sym)

    def isFixedSizeArray: Boolean = sym == CArrayClass

    def isField: Boolean =
      !sym.isMethod && sym.isTerm && !isScalaModule

    /** Tests if this type inherits from CFuncPtr */
    def isCFuncPtrClass: Boolean =
      sym == CFuncPtrClass ||
        sym.info.parents.exists(_.typeSymbol == CFuncPtrClass)

    /** Tests if this type is implementations of CFuncPtr */
    def isCFuncPtrNClass: Boolean =
      CFuncPtrNClass.contains(sym) || {
        sym.info.parents.exists { parent =>
          CFuncPtrNClass.contains(parent.typeSymbol)
        }
      }

    def isVolatile: Boolean = isField && sym.hasAnnotation(VolatileAttr)
  }

  def genBoxType(tpe: Type): nir.Type = tpe.typeSymbol match {
    case CharClass    => genType(BoxedCharacterClass.info)
    case BooleanClass => genType(BoxedBooleanClass.info)
    case ByteClass    => genType(BoxedByteClass.info)
    case ShortClass   => genType(BoxedShortClass.info)
    case IntClass     => genType(BoxedIntClass.info)
    case LongClass    => genType(BoxedLongClass.info)
    case FloatClass   => genType(BoxedFloatClass.info)
    case DoubleClass  => genType(BoxedDoubleClass.info)
    case _            => genType(tpe)
  }

  def genExternType(tpe: Type): nir.Type =
    genType(tpe) match {
      case _ if tpe.typeSymbol.isCFuncPtrClass =>
        nir.Type.Ptr
      case refty: nir.Type.Ref if nir.Type.boxClasses.contains(refty.name) =>
        nir.Type.unbox(nir.Type.Ref(refty.name))
      case ty =>
        ty
    }

  def genType(
      tpe: Type,
      deconstructValueTypes: Boolean = false
  ): nir.Type = genNIRType { sym =>
    PrimitiveSymbolToNirTypes
      .get(sym)
      .getOrElse {
        if (sym.isStruct) genStruct(tpe)
        else if (deconstructValueTypes) {
          if (sym.isAnonymousStruct) genAnonymousStruct(tpe)
          else if (sym.isFixedSizeArray) genFixedSizeArray(tpe)
          else {
            val ref = nir.Type.Ref(genTypeName(sym))
            nir.Type.unbox.getOrElse(nir.Type.normalize(ref), ref)
          }
        } else nir.Type.Ref(genTypeName(sym))
      }
  }(tpe)

  private lazy val PrimitiveSymbolToNirTypes = Map[Symbol, nir.Type](
    CharClass -> nir.Type.Char,
    BooleanClass -> nir.Type.Bool,
    ByteClass -> nir.Type.Byte,
    ShortClass -> nir.Type.Short,
    IntClass -> nir.Type.Int,
    LongClass -> nir.Type.Long,
    FloatClass -> nir.Type.Float,
    DoubleClass -> nir.Type.Double,
    UnitClass -> nir.Type.Unit,
    NullClass -> nir.Type.Null,
    NothingClass -> nir.Type.Nothing,
    RawPtrClass -> nir.Type.Ptr,
    RawSizeClass -> nir.Type.Size
  )

  def genRefType(tpe: Type): nir.Type.RefKind =
    genNIRType { sym =>
      if (sym.isPrimitiveValueClass) genBoxType(sym.info)
      else if (sym == NothingClass) nir.Rt.RuntimeNothing
      else if (sym == NullClass) nir.Rt.RuntimeNull
      else nir.Type.Ref(genTypeName(sym))
    }(tpe) match {
      case t: nir.Type.RefKind => t
      case t => util.unsupported("Unexpected non ref kind type - $t")
    }

  private def genNIRType[T](toNIRType: Symbol => nir.Type)(
      tpe: Type
  ): nir.Type = {
    def fromSymbol(sym: Symbol) =
      if (sym == AnyClass || sym == ObjectClass) nir.Rt.Object
      else toNIRType(sym)
    def recurse(t: Type) = genNIRType(toNIRType)(t)

    tpe.dealiasWiden match {
      // Array type such as Array[Int] (kept by erasure)
      case TypeRef(_, ArrayClass, List(arg)) => nir.Type.Array(genType(arg))
      case tp @ TypeRef(parents, sym, args)  =>
        // See comment on nonClassTypeRefToBType in Scala JVM backend BCodeHelpers
        if (!sym.isClass) nir.Rt.Object
        else if (sym.isRefinementClass) nir.Rt.Object
        else fromSymbol(sym)
      case ClassInfoType(_, _, sym) => fromSymbol(sym)

      /* AnnotatedType should (probably) be eliminated by erasure. However we know it happens for
       * meta-annotated annotations (@(ann @getter) val x = 0), so we don't emit a warning.
       * The type in the AnnotationInfo is an AnnotatedTpe.
       */
      case ThisType(ArrayClass)   => nir.Rt.Object
      case ThisType(sym)          => fromSymbol(sym)
      case SingleType(_, sym)     => fromSymbol(sym)
      case ConstantType(_)        => recurse(tpe.underlying)
      case AnnotatedType(_, at)   => recurse(at)
      case ExistentialType(_, et) => recurse(et)
    }
  }

  def genFixedSizeArray(tpe: Type): nir.Type = {
    def parseNatDigit(tpe: Type): Int =
      tpe.dealias.typeSymbol.simpleName.toString().last - '0'
    def natClassToInt(tpe: Type): Int = {
      if (tpe.typeArgs.isEmpty) parseNatDigit(tpe)
      else
        tpe.typeArgs.foldLeft(0) {
          case (acc, tpe) => acc * 10 + parseNatDigit(tpe)
        }
    }

    val List(elemType, size) = tpe.typeArgs

    val elemTpe = genType(elemType, deconstructValueTypes = true)
    val elems = natClassToInt(size)
    nir.Type
      .ArrayValue(elemTpe, elems)
      .ensuring(
        _.n >= 0,
        s"fixed size array size needs to be positive integer, got $size"
      )
  }

  def genTypeValue(tpe: Type): nir.Val = {
    // FIXME: Backward compatibility for 0.5.x series - we're generating scala.scalanative.runtime.PrimitiveX types instead of proper ones
    val refType =
      RuntimePrimitive
        .get(tpe.typeSymbol)
        .map(_.info)
        .getOrElse(tpe)
    nir.Val.ClassOf(genRefType(refType).className)
  }

  def genStructFields(sym: Symbol): Seq[nir.Type] = {
    for {
      f <- sym.info.decls if f.isField
    } yield {
      genType(f.tpe)
    }
  }.toSeq

  def genStruct(tpe: Type): nir.Type = {
    val fields = genStructFields(tpe.typeSymbol)

    nir.Type.StructValue(fields)
  }

  def genAnonymousStruct(tpe: Type): nir.Type = {
    val fields = tpe.typeArgs.map(genType(_, deconstructValueTypes = true))
    nir.Type.StructValue(fields)
  }

  def genPrimCode(tpe: Type): Char = tpe.typeSymbol match {
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
      require(sym.isMethod || sym.isStaticMember, "symbol is not a method")

      val tpe = sym.tpe
      val owner = sym.owner
      val paramtys = genMethodSigParamsImpl(sym, isExtern)
      val selfty =
        if (statically || isExtern || sym.isStaticInNIR) None
        else Some(genType(owner.tpe))
      val retty =
        if (sym.isClassConstructor) nir.Type.Unit
        else if (isExtern) genExternType(sym.tpe.resultType)
        else genType(sym.tpe.resultType)

      nir.Type.Function(selfty ++: paramtys, retty)
    }
    cachedMethodSig.getOrElseUpdate((sym, isExtern, statically), resolve())
  }

  private def genMethodSigParamsImpl(
      sym: Symbol,
      isExternHint: Boolean
  ): Seq[nir.Type] = {
    val params = sym.tpe.params
    val isExtern = isExternHint || sym.isExtern
    if (!isExtern)
      params.map { p => genType(p.tpe) }
    else {
      val wereRepeated = exitingPhase(currentRun.typerPhase) {
        for {
          params <- sym.tpe.paramss
          param <- params
        } yield {
          param.name -> isScalaRepeatedParamType(param.tpe)
        }
      }.toMap

      params.map { p =>
        if (isExtern && wereRepeated(p.name)) nir.Type.Vararg
        else if (isExtern) genExternType(p.tpe)
        else genType(p.tpe)
      }
    }
  }

  lazy val jlStringBuilderAppendForSymbol =
    nirDefinitions.jlStringBuilderAppendAlts.flatMap { sym =>
      val sig = genMethodSig(sym)
      def name = genMethodName(sym)
      sig match {
        case nir.Type.Function(Seq(_, arg), _)
            if sym.owner == nirDefinitions.jlStringBuilderRef =>
          Some(
            nir.Type.normalize(arg) -> (nir.Val.Global(name, nir.Type.Ptr), sig)
          )
        case _ => None
      }
    }.toMap

}
