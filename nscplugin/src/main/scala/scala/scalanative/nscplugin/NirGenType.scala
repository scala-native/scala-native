package scala.scalanative
package nscplugin

import util._

trait NirGenType { self: NirGenPhase =>
  import global._
  import definitions._
  import nirAddons._
  import nirDefinitions._
  import SimpleType.{fromType, fromSymbol}

  final case class SimpleType(sym: Symbol, targs: Seq[SimpleType] = Seq.empty) {
    def isInterface: Boolean =
      sym.isInterface

    def isScalaModule: Boolean =
      sym.isModuleClass && !sym.isImplClass && !sym.isLifted

    def isExternModule: Boolean =
      isScalaModule && sym.annotations.exists(_.symbol == ExternClass)

    def isStruct: Boolean =
      sym.annotations.exists(_.symbol == StructClass)

    def isField: Boolean =
      !sym.isMethod && sym.isTerm && !isScalaModule

    def isCFuncPtrClass: Boolean =
      CFuncPtrClass.contains(sym) || {
        sym.info.parents.exists { parent =>
          CFuncPtrClass.contains(parent.typeSymbol)
        }
      }
  }

  object SimpleType {
    import scala.language.implicitConversions

    implicit def fromType(t: Type): SimpleType =
      t.normalize match {
        case ThisType(ArrayClass)  => SimpleType(ObjectClass, Seq.empty)
        case ThisType(sym)         => SimpleType(sym, Seq.empty)
        case SingleType(_, sym)    => SimpleType(sym, Seq.empty)
        case ConstantType(_)       => fromType(t.underlying)
        case TypeRef(_, sym, args) => SimpleType(sym, args.map(fromType))
        case ClassInfoType(_, _, ArrayClass) =>
          abort("ClassInfoType to ArrayClass!")
        case ClassInfoType(_, _, sym) => SimpleType(sym, Seq.empty)
        case t: AnnotatedType         => fromType(t.underlying)
        case tpe: ErasedValueType     => SimpleType(tpe.valueClazz, Seq())
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

  def genType(st: SimpleType): nir.Type = st.sym match {
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
    case _            => genRefType(st)
  }

  def genRefType(st: SimpleType): nir.Type = st.sym match {
    case ObjectClass      => nir.Rt.Object
    case UnitClass        => nir.Type.Unit
    case BoxedUnitClass   => nir.Rt.BoxedUnit
    case NullClass        => genRefType(RuntimeNullClass)
    case ArrayClass       => nir.Type.Array(genType(st.targs.head))
    case _ if st.isStruct => genStruct(st)
    case _                => nir.Type.Ref(genTypeName(st.sym))
  }

  def genTypeValue(st: SimpleType): nir.Val =
    genPrimCode(st.sym) match {
      case _ if st.sym == UnitClass =>
        genTypeValue(RuntimePrimitive('U'))
      case _ if st.sym == ArrayClass =>
        genTypeValue(RuntimeArrayClass(genPrimCode(st.targs.head)))
      case 'O' =>
        nir.Val.Global(genTypeName(st.sym), nir.Type.Ptr)
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

  private def genMethodSigImpl(sym: Symbol,
                               isExtern: Boolean): nir.Type.Function = {
    require(sym.isMethod || sym.isStaticMember)

    val tpe      = sym.tpe
    val owner    = sym.owner
    val paramtys = genMethodSigParamsImpl(sym, isExtern)
    val selfty =
      if (isExtern || owner.isExternModule || owner.isImplClass) None
      else Some(genType(owner.tpe))
    val retty =
      if (sym.isClassConstructor) nir.Type.Unit
      else if (isExtern) genExternType(sym.tpe.resultType)
      else genType(sym.tpe.resultType)

    nir.Type.Function(selfty ++: paramtys, retty)
  }

  private def genMethodSigParamsImpl(sym: Symbol,
                                     isExtern: Boolean): Seq[nir.Type] = {
    val wereRepeated = exitingPhase(currentRun.typerPhase) {
      for {
        params <- sym.tpe.paramss
        param  <- params
      } yield {
        param.name -> isScalaRepeatedParamType(param.tpe)
      }
    }.toMap

    sym.tpe.params.map {
      case p
          if wereRepeated.getOrElse(p.name, false) &&
            sym.owner.isExternModule =>
        nir.Type.Vararg

      case p =>
        if (isExtern) {
          genExternType(p.tpe)
        } else {
          genType(p.tpe)
        }
    }
  }
}
