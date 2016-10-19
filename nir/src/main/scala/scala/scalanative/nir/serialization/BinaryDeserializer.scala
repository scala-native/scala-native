package scala.scalanative
package nir
package serialization

import java.nio.ByteBuffer
import scala.collection.mutable
import nir.{Tags => T}

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

    val (_, _, pairs) = scoped(getSeq((getGlobal, getInt)))
    val map           = pairs.toMap
    this.deps = null
    map
  }

  private var deps: mutable.Set[Dep]        = _
  private var links: mutable.Set[Attr.Link] = _
  private def scoped[T](
      f: => T): (mutable.Set[Dep], mutable.Set[Attr.Link], T) = {
    this.deps = mutable.Set.empty[Dep]
    this.links = mutable.Set.empty[Attr.Link]
    val res   = f
    val deps  = this.deps
    val links = this.links
    this.deps = null
    this.links = null
    (deps, links, res)
  }

  final def deserialize(g: Global): Option[(Seq[Dep], Seq[Attr.Link], Defn)] =
    header.get(g).map {
      case offset =>
        buffer.position(offset)
        val (deps, links, defn) = scoped(getDefn)
        deps -= Dep.Direct(g)
        (deps.toSeq, links.toSeq, defn)
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
    new String(arr)
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

        case T.PureAttr     => buf += Attr.Pure
        case T.ExternAttr   => buf += Attr.Extern
        case T.OverrideAttr => buf += Attr.Override(getGlobal)

        case T.LinkAttr      => links += Attr.Link(getString)
        case T.PinAlwaysAttr => deps += Dep.Direct(getGlobalNoDep)
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
    case T.NoneInst  => Inst.None
    case T.LabelInst => Inst.Label(getLocal, getParams)
    case T.LetInst   => Inst.Let(getLocal, getOp)

    case T.UnreachableInst => Inst.Unreachable
    case T.RetInst         => Inst.Ret(getVal)
    case T.JumpInst        => Inst.Jump(getNext)
    case T.IfInst          => Inst.If(getVal, getNext, getNext)
    case T.SwitchInst      => Inst.Switch(getVal, getNext, getNexts)
    case T.InvokeInst =>
      Inst.Invoke(getType, getVal, getVals, getNext, getNext)

    case T.ThrowInst => Inst.Throw(getVal)
    case T.TryInst   => Inst.Try(getNext, getNext)
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
    case T.SuccNext  => Next.Succ(getLocal)
    case T.FailNext  => Next.Fail(getLocal)
    case T.LabelNext => Next.Label(getLocal, getVals)
    case T.CaseNext  => Next.Case(getVal, getLocal)
  }

  private def getOp(): Op = getInt match {
    case T.CallOp       => Op.Call(getType, getVal, getVals)
    case T.LoadOp       => Op.Load(getType, getVal)
    case T.StoreOp      => Op.Store(getType, getVal, getVal)
    case T.ElemOp       => Op.Elem(getType, getVal, getVals)
    case T.ExtractOp    => Op.Extract(getVal, getInts)
    case T.InsertOp     => Op.Insert(getVal, getVal, getInts)
    case T.StackallocOp => Op.Stackalloc(getType, getVal)
    case T.BinOp        => Op.Bin(getBin, getType, getVal, getVal)
    case T.CompOp       => Op.Comp(getComp, getType, getVal, getVal)
    case T.ConvOp       => Op.Conv(getConv, getType, getVal)
    case T.SelectOp     => Op.Select(getVal, getVal, getVal)

    case T.ClassallocOp => Op.Classalloc(getGlobal)
    case T.FieldOp      => Op.Field(getType, getVal, getGlobal)
    case T.MethodOp     => Op.Method(getType, getVal, getGlobal)
    case T.ModuleOp     => Op.Module(getGlobal)
    case T.AsOp         => Op.As(getType, getVal)
    case T.IsOp         => Op.Is(getType, getVal)
    case T.CopyOp       => Op.Copy(getVal)
    case T.SizeofOp     => Op.Sizeof(getType)
    case T.ClosureOp    => Op.Closure(getType, getVal, getVals)
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
    case T.I8Type       => Type.I8
    case T.I16Type      => Type.I16
    case T.I32Type      => Type.I32
    case T.I64Type      => Type.I64
    case T.F32Type      => Type.F32
    case T.F64Type      => Type.F64
    case T.ArrayType    => Type.Array(getType, getInt)
    case T.FunctionType => Type.Function(getArgs, getType)
    case T.StructType   => Type.Struct(getGlobal, getTypes)

    case T.UnitType    => Type.Unit
    case T.NothingType => Type.Nothing
    case T.ClassType   => Type.Class(getGlobal)
    case T.TraitType   => Type.Trait(getGlobal)
    case T.ModuleType  => Type.Module(getGlobal)
  }

  private def getArgs(): Seq[Arg] = getSeq(getArg)
  private def getArg(): Arg       = Arg(getType, getOpt(getPassConvention))
  private def getPassConvention(): PassConv = getInt match {
    case T.Byval => PassConv.Byval(getType)
    case T.Sret  => PassConv.Sret(getType)
  }

  private def getVals(): Seq[Val] = getSeq(getVal)
  private def getVal(): Val = getInt match {
    case T.NoneVal   => Val.None
    case T.TrueVal   => Val.True
    case T.FalseVal  => Val.False
    case T.ZeroVal   => Val.Zero(getType)
    case T.UndefVal  => Val.Undef(getType)
    case T.I8Val     => Val.I8(get)
    case T.I16Val    => Val.I16(getShort)
    case T.I32Val    => Val.I32(getInt)
    case T.I64Val    => Val.I64(getLong)
    case T.F32Val    => Val.F32(getFloat)
    case T.F64Val    => Val.F64(getDouble)
    case T.StructVal => Val.Struct(getGlobal, getVals)
    case T.ArrayVal  => Val.Array(getType, getVals)
    case T.CharsVal  => Val.Chars(getString)
    case T.LocalVal  => Val.Local(getLocal, getType)
    case T.GlobalVal => Val.Global(getGlobal, getType)

    case T.UnitVal   => Val.Unit
    case T.ConstVal  => Val.Const(getVal)
    case T.StringVal => Val.String(getString)
  }
}
