package scala.scalanative
package nir
package serialization

import java.nio.ByteBuffer
import scala.collection.mutable

import nir.serialization.{Tags => T}
import Global.Member

final class BinaryDeserializer(_buffer: => ByteBuffer) {
  private lazy val buffer = _buffer
  import buffer._

  private lazy val header: Map[Global, Int] = {
    buffer.position(0)

    val magic    = getInt
    val compat   = getInt
    val revision = getInt

    assert(magic == Versions.magic, "Can't read non-NIR file.")
    assert(compat == Versions.compat && revision <= Versions.revision,
           "Can't read binary-incompatible version of NIR.")

    val (_, _, _, pairs) = scoped(getSeq((getGlobal, getInt)))
    val map              = pairs.toMap
    this.deps = null
    map
  }

  private var deps: mutable.Set[Dep]        = _
  private var links: mutable.Set[Attr.Link] = _
  private var dyns: mutable.Set[String]     = _

  private def scoped[T](f: => T)
    : (mutable.Set[Dep], mutable.Set[Attr.Link], mutable.Set[String], T) = {
    this.deps = mutable.Set.empty[Dep]
    this.links = mutable.Set.empty[Attr.Link]
    this.dyns = mutable.Set.empty[String]
    val res   = f
    val deps  = this.deps
    val links = this.links
    val dyns  = this.dyns
    this.deps = null
    this.links = null
    this.dyns = null
    (deps, links, dyns, res)
  }

  final def globals: Set[Global] = header.keySet

  final def deserialize(
      g: Global): Option[(Seq[Dep], Seq[Attr.Link], Seq[String], Defn)] =
    header.get(g).map {
      case offset =>
        buffer.position(offset)
        val (deps, links, dyns, defn) = scoped(getDefn)
        deps -= Dep.Direct(g)
        (deps.toSeq, links.toSeq, dyns.toSeq, defn)
    }

  private def getSeq[T](getT: => T): Seq[T] =
    (1 to getInt).map(_ => getT).toSeq

  private def getOpt[T](getT: => T): Option[T] =
    if (get == 0) None else Some(getT)

  private def getInts(): Seq[Int] = getSeq(getInt)

  private def getStrings(): Seq[String] = getSeq(getString)
  private def getString(): String = {
    val arr = new Array[Byte](getInt)
    get(arr)
    new String(arr, "UTF-8")
  }

  private def getBool(): Boolean = get != 0

  private def getAttrs(): Attrs = {
    val buf = mutable.UnrolledBuffer.empty[Attr]

    (1 to getInt).foreach { _ =>
      getInt match {
        case T.MayInlineAttr    => buf += Attr.MayInline
        case T.InlineHintAttr   => buf += Attr.InlineHint
        case T.NoInlineAttr     => buf += Attr.NoInline
        case T.AlwaysInlineAttr => buf += Attr.AlwaysInline

        case T.DynAttr => buf += Attr.Dyn

        case T.PureAttr     => buf += Attr.Pure
        case T.ExternAttr   => buf += Attr.Extern
        case T.OverrideAttr => buf += Attr.Override(getGlobal)

        case T.LinkAttr      => links += Attr.Link(getString)
        case T.PinAlwaysAttr => deps += Dep.Direct(getGlobalNoDep)
        case T.PinWeakAttr   => deps += Dep.Weak(getGlobalNoDep)
        case T.PinIfAttr =>
          deps += Dep.Conditional(getGlobalNoDep, getGlobalNoDep)
      }
    }

    Attrs.fromSeq(buf)
  }

  private def getBin(): Bin = getInt match {
    case T.IaddBin => Bin.Iadd
    case T.FaddBin => Bin.Fadd
    case T.IsubBin => Bin.Isub
    case T.FsubBin => Bin.Fsub
    case T.ImulBin => Bin.Imul
    case T.FmulBin => Bin.Fmul
    case T.SdivBin => Bin.Sdiv
    case T.UdivBin => Bin.Udiv
    case T.FdivBin => Bin.Fdiv
    case T.SremBin => Bin.Srem
    case T.UremBin => Bin.Urem
    case T.FremBin => Bin.Frem
    case T.ShlBin  => Bin.Shl
    case T.LshrBin => Bin.Lshr
    case T.AshrBin => Bin.Ashr
    case T.AndBin  => Bin.And
    case T.OrBin   => Bin.Or
    case T.XorBin  => Bin.Xor
  }

  private def getInsts(): Seq[Inst] = getSeq(getInst)
  private def getInst(): Inst = getInt match {
    case T.NoneInst        => Inst.None
    case T.LabelInst       => Inst.Label(getLocal, getParams)
    case T.LetInst         => Inst.Let(getLocal, getOp)
    case T.UnreachableInst => Inst.Unreachable
    case T.RetInst         => Inst.Ret(getVal)
    case T.JumpInst        => Inst.Jump(getNext)
    case T.IfInst          => Inst.If(getVal, getNext, getNext)
    case T.SwitchInst      => Inst.Switch(getVal, getNext, getNexts)
    case T.ThrowInst       => Inst.Throw(getVal, getNext)
  }

  private def getComp(): Comp = getInt match {
    case T.IeqComp => Comp.Ieq
    case T.IneComp => Comp.Ine
    case T.UgtComp => Comp.Ugt
    case T.UgeComp => Comp.Uge
    case T.UltComp => Comp.Ult
    case T.UleComp => Comp.Ule
    case T.SgtComp => Comp.Sgt
    case T.SgeComp => Comp.Sge
    case T.SltComp => Comp.Slt
    case T.SleComp => Comp.Sle

    case T.FeqComp => Comp.Feq
    case T.FneComp => Comp.Fne
    case T.FgtComp => Comp.Fgt
    case T.FgeComp => Comp.Fge
    case T.FltComp => Comp.Flt
    case T.FleComp => Comp.Fle
  }

  private def getConv(): Conv = getInt match {
    case T.TruncConv    => Conv.Trunc
    case T.ZextConv     => Conv.Zext
    case T.SextConv     => Conv.Sext
    case T.FptruncConv  => Conv.Fptrunc
    case T.FpextConv    => Conv.Fpext
    case T.FptouiConv   => Conv.Fptoui
    case T.FptosiConv   => Conv.Fptosi
    case T.UitofpConv   => Conv.Uitofp
    case T.SitofpConv   => Conv.Sitofp
    case T.PtrtointConv => Conv.Ptrtoint
    case T.InttoptrConv => Conv.Inttoptr
    case T.BitcastConv  => Conv.Bitcast
  }

  private def getDefns(): Seq[Defn] = getSeq(getDefn)
  private def getDefn(): Defn = getInt match {
    case T.VarDefn =>
      Defn.Var(getAttrs, getGlobal, getType, getVal)

    case T.ConstDefn =>
      Defn.Const(getAttrs, getGlobal, getType, getVal)

    case T.DeclareDefn =>
      Defn.Declare(getAttrs, getGlobal, getType)

    case T.DefineDefn =>
      Defn.Define(getAttrs, getGlobal, getType, getInsts)

    case T.StructDefn =>
      Defn.Struct(getAttrs, getGlobal, getTypes)

    case T.TraitDefn =>
      Defn.Trait(getAttrs, getGlobal, getGlobals)

    case T.ClassDefn =>
      Defn.Class(getAttrs, getGlobal, getGlobalOpt, getGlobals)

    case T.ModuleDefn =>
      Defn.Module(getAttrs, getGlobal, getGlobalOpt, getGlobals)
  }

  private def getGlobals(): Seq[Global]      = getSeq(getGlobal)
  private def getGlobalOpt(): Option[Global] = getOpt(getGlobal)
  private def getGlobal(): Global = {
    val name = getGlobalNoDep
    if (name != Global.None) {
      deps += Dep.Direct(name)
    }
    name
  }

  private def getGlobalNoDep(): Global = getInt match {
    case T.NoneGlobal   => Global.None
    case T.TopGlobal    => Global.Top(getString)
    case T.MemberGlobal => Global.Member(getGlobal, getString)
  }

  private def getLocal(): Local = Local(getString, getInt)

  private def getNexts(): Seq[Next] = getSeq(getNext)
  private def getNext(): Next = getInt match {
    case T.NoneNext   => Next.None
    case T.UnwindNext => Next.Unwind(getLocal)
    case T.LabelNext  => Next.Label(getLocal, getVals)
    case T.CaseNext   => Next.Case(getVal, getLocal)
  }

  private def getOp(): Op = getInt match {
    case T.CallOp       => Op.Call(getType, getVal, getVals, getNext)
    case T.LoadOp       => Op.Load(getType, getVal, isVolatile = false)
    case T.StoreOp      => Op.Store(getType, getVal, getVal, isVolatile = false)
    case T.ElemOp       => Op.Elem(getType, getVal, getVals)
    case T.ExtractOp    => Op.Extract(getVal, getInts)
    case T.InsertOp     => Op.Insert(getVal, getVal, getInts)
    case T.StackallocOp => Op.Stackalloc(getType, getVal)
    case T.BinOp        => Op.Bin(getBin, getType, getVal, getVal)
    case T.CompOp       => Op.Comp(getComp, getType, getVal, getVal)
    case T.ConvOp       => Op.Conv(getConv, getType, getVal)
    case T.SelectOp     => Op.Select(getVal, getVal, getVal)

    case T.ClassallocOp => Op.Classalloc(getGlobal)
    case T.FieldOp      => Op.Field(getVal, getGlobal)
    case T.MethodOp     => Op.Method(getVal, getGlobal)
    case T.DynmethodOp =>
      val dynmethod = Op.Dynmethod(getVal, getString)
      dyns += dynmethod.signature
      dynmethod
    case T.ModuleOp  => Op.Module(getGlobal, getNext)
    case T.AsOp      => Op.As(getType, getVal)
    case T.IsOp      => Op.Is(getType, getVal)
    case T.CopyOp    => Op.Copy(getVal)
    case T.SizeofOp  => Op.Sizeof(getType)
    case T.ClosureOp => Op.Closure(getType, getVal, getVals)
    case T.BoxOp     => Op.Box(getType, getVal)
    case T.UnboxOp   => Op.Unbox(getType, getVal)
  }

  private def getParams(): Seq[Val.Local] = getSeq(getParam)
  private def getParam(): Val.Local       = Val.Local(getLocal, getType)

  private def getTypes(): Seq[Type] = getSeq(getType)
  private def getType(): Type = getInt match {
    case T.NoneType     => Type.None
    case T.VoidType     => Type.Void
    case T.VarargType   => Type.Vararg
    case T.PtrType      => Type.Ptr
    case T.BoolType     => Type.Bool
    case T.CharType     => Type.Char
    case T.ByteType     => Type.Byte
    case T.UByteType    => Type.UByte
    case T.ShortType    => Type.Short
    case T.UShortType   => Type.UShort
    case T.IntType      => Type.Int
    case T.UIntType     => Type.UInt
    case T.LongType     => Type.Long
    case T.ULongType    => Type.ULong
    case T.FloatType    => Type.Float
    case T.DoubleType   => Type.Double
    case T.ArrayType    => Type.Array(getType, getInt)
    case T.FunctionType => Type.Function(getTypes, getType)
    case T.StructType   => Type.Struct(getGlobal, getTypes)

    case T.UnitType    => Type.Unit
    case T.NothingType => Type.Nothing
    case T.ClassType   => Type.Class(getGlobal)
    case T.TraitType   => Type.Trait(getGlobal)
    case T.ModuleType  => Type.Module(getGlobal)
  }

  private def getVals(): Seq[Val] = getSeq(getVal)
  private def getVal(): Val = getInt match {
    case T.NoneVal   => Val.None
    case T.TrueVal   => Val.True
    case T.FalseVal  => Val.False
    case T.ZeroVal   => getZero()
    case T.UndefVal  => Val.Undef(getType)
    case T.ByteVal   => Val.Byte(get)
    case T.ShortVal  => Val.Short(getShort)
    case T.IntVal    => Val.Int(getInt)
    case T.LongVal   => Val.Long(getLong)
    case T.FloatVal  => Val.Float(getFloat)
    case T.DoubleVal => Val.Double(getDouble)
    case T.StructVal => Val.Struct(getGlobal, getVals)
    case T.ArrayVal  => Val.Array(getType, getVals)
    case T.CharsVal  => Val.Chars(getString)
    case T.LocalVal  => Val.Local(getLocal, getType)
    case T.GlobalVal => Val.Global(getGlobal, getType)

    case T.UnitVal   => Val.Unit
    case T.ConstVal  => Val.Const(getVal)
    case T.StringVal => Val.String(getString)
  }
  private def getZero(): Val = {
    val ty = getType
    ty match {
      case Type.Ptr | _: Type.RefKind => Val.Null
      case _                          => Val.Zero(ty)
    }
  }
}
