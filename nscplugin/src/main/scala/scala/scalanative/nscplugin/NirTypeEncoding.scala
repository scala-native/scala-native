package scala.scalanative
package nscplugin

import util._

trait NirTypeEncoding { self: NirCodeGen =>
  import global._
  import definitions._
  import nirAddons._
  import nirDefinitions._
  import SimpleType.{fromType, fromSymbol}

  final case class SimpleType(sym: Symbol, targs: Seq[SimpleType] = Seq.empty) {
    def isInterface: Boolean =
      sym.isInterface

    def isScalaModule: Boolean =
      sym.isModule || sym.isModuleClass || sym.isImplClass

    def isExternModule: Boolean =
      isScalaModule && sym.annotations.exists(_.symbol == ExternClass)

    def isStruct: Boolean =
      sym.annotations.exists(_.symbol == StructClass)

    def isField: Boolean =
      !sym.isMethod && sym.isTerm && !isScalaModule
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

  def genTypeName(sym: Symbol): nir.Global

  def genArrayCode(st: SimpleType): Char =
    genPrimCode(st.targs.head)

  def genType(st: SimpleType, box: Boolean): nir.Type = st.sym match {
    // format: off
    case CharClass    => if (!box) nir.Type.Char  else genRefType(BoxedCharacterClass)
    case BooleanClass => if (!box) nir.Type.Bool else genRefType(BoxedBooleanClass)
    case ByteClass    => if (!box) nir.Type.Byte   else genRefType(BoxedByteClass)
    case ShortClass   => if (!box) nir.Type.Short  else genRefType(BoxedShortClass)
    case IntClass     => if (!box) nir.Type.Int  else genRefType(BoxedIntClass)
    case LongClass    => if (!box) nir.Type.Long  else genRefType(BoxedLongClass)
    case FloatClass   => if (!box) nir.Type.Float  else genRefType(BoxedFloatClass)
    case DoubleClass  => if (!box) nir.Type.Double  else genRefType(BoxedDoubleClass)
    case UByteClass   => if (!box) nir.Type.UByte   else genRefType(st)
    case UShortClass  => if (!box) nir.Type.UShort  else genRefType(st)
    case UIntClass    => if (!box) nir.Type.UInt  else genRefType(st)
    case ULongClass   => if (!box) nir.Type.ULong  else genRefType(st)
    case PtrClass     => nir.Type.Ptr
    // format: on
    case sym if CStructClass.contains(sym) =>
      nir.Type.Struct(nir.Global.None, st.targs.map(genType(_, box = false)))
    case CArrayClass =>
      genCArrayType(st)
    case _ =>
      genRefType(st)
  }

  def genCArrayType(st: SimpleType): nir.Type = st.targs match {
    case Seq() =>
      nir.Type.Array(nir.Rt.Object, 0)
    case Seq(targ, tnat) =>
      val ty = genType(targ, box = false)
      val n  = genNatType(tnat)
      nir.Type.Array(ty, n)
  }

  def genNatType(st: SimpleType): Int = {
    def base(st: SimpleType): Int = st.sym match {
      case sym if NatBaseClass.contains(sym) =>
        NatBaseClass.indexOf(sym)
      case _ =>
        scalanative.util.unsupported("base nat type expected")
    }
    def digits(st: SimpleType): List[Int] = st.sym match {
      case sym if NatBaseClass.contains(sym) =>
        base(st) :: Nil
      case NatDigitClass =>
        base(st.targs(0)) :: digits(st.targs(1))
      case _ =>
        scalanative.util.unsupported("nat type expected")
    }

    digits(st).foldLeft(0)(_ * 10 + _)
  }

  def genRefType(st: SimpleType): nir.Type = st.sym match {
    case ObjectClass  => nir.Rt.Object
    case UnitClass    => nir.Type.Unit
    case NothingClass => nir.Type.Nothing
    case NullClass    => genRefType(RuntimeNullClass)
    case ArrayClass =>
      genRefType(RuntimeArrayClass(genPrimCode(st.targs.head)))
    case _ if st.isStruct      => genStruct(st)
    case _ if st.isScalaModule => nir.Type.Module(genTypeName(st.sym))
    case _ if st.isInterface   => nir.Type.Trait(genTypeName(st.sym))
    case _                     => nir.Type.Class(genTypeName(st.sym))
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
      genType(f.tpe, box = false)
    }
  }.toSeq

  def genStruct(st: SimpleType): nir.Type = {
    val name   = genTypeName(st.sym)
    val fields = genStructFields(st)

    nir.Type.Struct(name, fields)
  }

  def genPrimCode(st: SimpleType): Char = st.sym match {
    case CharClass    => 'C'
    case BooleanClass => 'B'
    case UByteClass   => 'z'
    case ByteClass    => 'Z'
    case UShortClass  => 's'
    case ShortClass   => 'S'
    case UIntClass    => 'i'
    case IntClass     => 'I'
    case ULongClass   => 'l'
    case LongClass    => 'L'
    case FloatClass   => 'F'
    case DoubleClass  => 'D'
    case _            => 'O'
  }

  def genBoxType(st: SimpleType): nir.Type = st.sym match {
    case BooleanClass =>
      nir.Type.Class(nir.Global.Top("java.lang.Boolean"))
    case CharClass =>
      nir.Type.Class(nir.Global.Top("java.lang.Character"))
    case UByteClass =>
      nir.Type.Class(nir.Global.Top("scala.scalanative.native.UByte"))
    case ByteClass =>
      nir.Type.Class(nir.Global.Top("java.lang.Byte"))
    case UShortClass =>
      nir.Type.Class(nir.Global.Top("scala.scalanative.native.UShort"))
    case ShortClass =>
      nir.Type.Class(nir.Global.Top("java.lang.Short"))
    case UIntClass =>
      nir.Type.Class(nir.Global.Top("scala.scalanative.native.UInt"))
    case IntClass =>
      nir.Type.Class(nir.Global.Top("java.lang.Integer"))
    case ULongClass =>
      nir.Type.Class(nir.Global.Top("scala.scalanative.native.ULong"))
    case LongClass =>
      nir.Type.Class(nir.Global.Top("java.lang.Long"))
    case FloatClass =>
      nir.Type.Class(nir.Global.Top("java.lang.Float"))
    case DoubleClass =>
      nir.Type.Class(nir.Global.Top("java.lang.Double"))
    case _ =>
      unsupported("Box type must be primitive type.")
  }

}
