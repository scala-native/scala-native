package scala.scalanative
package nscplugin
import scala.tools.nsc.Global

trait NirGenType[G <: Global with Singleton] { self: NirGenPhase[G] =>
  import SimpleType.{fromSymbol, fromType}
  import global._
  import definitions._
  import nirAddons._
  import nirDefinitions._

  sealed case class SimpleType(
      sym: Symbol,
      targs: Seq[SimpleType] = Seq.empty
  ) {
    def isInterface: Boolean =
      sym.isInterface

    def isScalaModule: Boolean =
      sym.isModuleClass && !sym.isLifted

    def isStaticInNIR: Boolean =
      sym.owner.isExternType || sym.isStaticMember

    def isExternType: Boolean =
      (isScalaModule || sym.isTraitOrInterface) &&
        sym.annotations.exists(_.symbol == ExternClass)

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

  object SimpleType {
    import scala.language.implicitConversions
    private val ObjectClassType = SimpleType(ObjectClass, Seq.empty)
    implicit def fromType(t: Type): SimpleType =
      t.normalize match {
        case ThisType(ArrayClass)  => ObjectClassType
        case ThisType(sym)         => SimpleType(sym, Seq.empty)
        case SingleType(_, sym)    => SimpleType(sym, Seq.empty)
        case ConstantType(_)       => fromType(t.underlying)
        case TypeRef(_, sym, args) => SimpleType(sym, args.map(fromType))
        case ClassInfoType(_, _, ArrayClass) =>
          abort("ClassInfoType to ArrayClass!")
        case ClassInfoType(_, _, sym) => SimpleType(sym, Seq.empty)
        case t: AnnotatedType         => fromType(t.underlying)
        case t: ExistentialType =>
          fromType(t.underlying).copy(targs = List(ObjectClassType))
        case tpe: ErasedValueType => SimpleType(tpe.valueClazz, Seq.empty)
      }

    implicit def fromSymbol(sym: Symbol): SimpleType =
      SimpleType(sym, Seq.empty)
  }

  def genArrayCode(st: SimpleType): Char =
    genPrimCode(st.targs.head)

  def genBoxType(st: SimpleType): nir.Type = st.sym match {
    case CharClass    => genType(BoxedCharacterClass)
    case BooleanClass => genType(BoxedBooleanClass)
    case ByteClass    => genType(BoxedByteClass)
    case ShortClass   => genType(BoxedShortClass)
    case IntClass     => genType(BoxedIntClass)
    case LongClass    => genType(BoxedLongClass)
    case FloatClass   => genType(BoxedFloatClass)
    case DoubleClass  => genType(BoxedDoubleClass)
    case _            => genType(st)
  }

  def genExternType(st: SimpleType): nir.Type =
    genType(st) match {
      case _ if st.isCFuncPtrClass =>
        nir.Type.Ptr
      case refty: nir.Type.Ref if nir.Type.boxClasses.contains(refty.name) =>
        nir.Type.unbox(nir.Type.Ref(refty.name))
      case ty =>
        ty
    }

  def genType(
      st: SimpleType,
      deconstructValueTypes: Boolean = false
  ): nir.Type = st.sym match {
    case CharClass    => nir.Type.Char
    case BooleanClass => nir.Type.Bool
    case ByteClass    => nir.Type.Byte
    case ShortClass   => nir.Type.Short
    case IntClass     => nir.Type.Int
    case LongClass    => nir.Type.Long
    case FloatClass   => nir.Type.Float
    case DoubleClass  => nir.Type.Double
    case NullClass    => nir.Type.Null
    case NothingClass => nir.Type.Nothing
    case RawPtrClass  => nir.Type.Ptr
    case RawSizeClass => nir.Type.Size
    case _            => genRefType(st, deconstructValueTypes)
  }

  def genRefType(
      st: SimpleType,
      deconstructValueTypes: Boolean = false
  ): nir.Type = st.sym match {
    case ObjectClass      => nir.Rt.Object
    case UnitClass        => nir.Type.Unit
    case BoxedUnitClass   => nir.Rt.BoxedUnit
    case NullClass        => genRefType(RuntimeNullClass)
    case ArrayClass       => nir.Type.Array(genType(st.targs.head))
    case _ if st.isStruct => genStruct(st)
    case _ if deconstructValueTypes =>
      if (st.isAnonymousStruct) genAnonymousStruct(st)
      else if (st.isFixedSizeArray) genFixedSizeArray(st)
      else {
        val ref = nir.Type.Ref(genTypeName(st.sym))
        nir.Type.unbox.getOrElse(nir.Type.normalize(ref), ref)
      }
    case _ => nir.Type.Ref(genTypeName(st.sym))
  }

  def genFixedSizeArray(st: SimpleType): nir.Type = {
    def natClassToInt(st: SimpleType): Int =
      if (st.targs.isEmpty) NatBaseClass.indexOf(st.sym)
      else
        st.targs.foldLeft(0) {
          case (acc, st) => acc * 10 + NatBaseClass.indexOf(st.sym)
        }

    val SimpleType(_, Seq(elemType, size)) = st
    val tpe = genType(elemType, deconstructValueTypes = true)
    val elems = natClassToInt(size)
    nir.Type
      .ArrayValue(tpe, elems)
      .ensuring(
        _.n >= 0,
        s"fixed size array size needs to be positive integer, got $size"
      )
  }

  def genTypeValue(st: SimpleType): nir.Val =
    genPrimCode(st.sym) match {
      case _ if st.sym == UnitClass =>
        genTypeValue(RuntimePrimitive('U'))
      case _ if st.sym == ArrayClass =>
        genTypeValue(RuntimeArrayClass(genPrimCode(st.targs.head)))
      case 'O' =>
        nir.Val.ClassOf(genTypeName(st.sym))
      case code =>
        genTypeValue(RuntimePrimitive(code))
    }

  def genStructFields(st: SimpleType): Seq[nir.Type] = {
    for {
      f <- st.sym.info.decls if f.isField
    } yield {
      genType(f.tpe)
    }
  }.toSeq

  def genStruct(st: SimpleType): nir.Type = {
    val fields = genStructFields(st)

    nir.Type.StructValue(fields)
  }

  def genAnonymousStruct(st: SimpleType): nir.Type = {
    val fields = st.targs.map(genType(_, deconstructValueTypes = true))
    nir.Type.StructValue(fields)
  }

  def genPrimCode(st: SimpleType): Char = st.sym match {
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

  def genMethodSig(sym: Symbol): nir.Type.Function =
    genMethodSigImpl(sym, isExtern = false)

  def genExternMethodSig(sym: Symbol): nir.Type.Function =
    genMethodSigImpl(sym, isExtern = true)

  private def genMethodSigImpl(
      sym: Symbol,
      isExtern: Boolean
  ): nir.Type.Function = {
    def resolve() = {
      require(sym.isMethod || sym.isStaticMember, "symbol is not a method")

      val tpe = sym.tpe
      val owner = sym.owner
      val paramtys = genMethodSigParamsImpl(sym, isExtern)
      val selfty =
        if (isExtern || sym.isStaticInNIR) None
        else Some(genType(owner.tpe))
      val retty =
        if (sym.isClassConstructor) nir.Type.Unit
        else if (isExtern) genExternType(sym.tpe.resultType)
        else genType(sym.tpe.resultType)

      nir.Type.Function(selfty ++: paramtys, retty)
    }
    cachedMethodSig.getOrElseUpdate((sym, isExtern), resolve())
  }

  private def genMethodSigParamsImpl(
      sym: Symbol,
      isExtern: Boolean
  ): Seq[nir.Type] = {
    val params = sym.tpe.params
    if (!isExtern && !sym.owner.isExternType)
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
        if (wereRepeated(p.name)) nir.Type.Vararg
        else genExternType(p.tpe)
      }
    }
  }
}
