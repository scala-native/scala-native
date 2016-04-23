package scala.scalanative
package nir
package serialization

import java.nio.ByteBuffer
import scala.collection.mutable
import nir.{Tags => T}

final class BinaryDeserializer(buffer: ByteBuffer) {
  import buffer._

  private lazy val header: Map[Global, Int] = {
    buffer.position(0)
    val (_, pairs) = scoped(getSeq((getGlobal, getInt)))
    val map = pairs.toMap
    this.deps = null
    println(map)
    map
  }

  private var deps: mutable.UnrolledBuffer[Global] = _
  private def scoped[T](f: => T): (Seq[Global], T) = {
    this.deps = mutable.UnrolledBuffer.empty[Global]
    val res = f
    val deps = this.deps
    this.deps = null
    (deps, res)
  }

  final def deserialize(g: Global): Option[(Seq[Global], Defn)] =
    header.get(g).map { case offset =>
      println(s"deserializing $g at $offset")
      buffer.position(offset)
      scoped(getDefn)
    }

  private def getSeq[T](getT: => T): Seq[T] =
    (1 to getInt).map(_ => getT).toSeq

  private def getInts(): Seq[Int] = getSeq(getInt)

  private def getStrings(): Seq[String] = getSeq(getString)
  private def getString(): String = {
    val arr = new Array[Byte](getInt)
    get(arr)
    new String(arr)
  }

  private def getBool(): Boolean = get != 0

  private def getAttrs(): Seq[Attr] = getSeq(getAttr)
  private def getAttr(): Attr = getInt match {
    case T.InlineHintAttr => Attr.InlineHint
    case T.NoInlineAttr   => Attr.NoInline
    case T.MustInlineAttr => Attr.MustInline

    case T.PrivateAttr             => Attr.Private
    case T.InternalAttr            => Attr.Internal
    case T.AvailableExternallyAttr => Attr.AvailableExternally
    case T.LinkOnceAttr            => Attr.LinkOnce
    case T.WeakAttr                => Attr.Weak
    case T.CommonAttr              => Attr.Common
    case T.AppendingAttr           => Attr.Appending
    case T.ExternWeakAttr          => Attr.ExternWeak
    case T.LinkOnceODRAttr         => Attr.LinkOnceODR
    case T.WeakODRAttr             => Attr.WeakODR
    case T.ExternalAttr            => Attr.External

    case T.OverrideAttr => Attr.Override(getGlobal)
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

  private def getBlocks(): Seq[Block] = getSeq(getBlock)
  private def getBlock(): Block = Block(getLocal, getParams, getInsts, getCf)

  private def getCf(): Cf = getInt match {
    case T.UnreachableCf => Cf.Unreachable
    case T.RetCf         => Cf.Ret(getVal)
    case T.JumpCf        => Cf.Jump(getNext)
    case T.IfCf          => Cf.If(getVal, getNext, getNext)
    case T.SwitchCf      => Cf.Switch(getVal, getNext, getNexts)
    case T.InvokeCf      => Cf.Invoke(getType, getVal, getVals, getNext, getNext)
    case T.ResumeCf      => Cf.Resume(getVal)

    case T.TryCf => Cf.Try(getNext, getNext)
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
      Defn.Define(getAttrs, getGlobal, getType, getBlocks)

    case T.StructDefn =>
      Defn.Struct(getAttrs, getGlobal, getTypes)

    case T.TraitDefn =>
      Defn.Trait(getAttrs, getGlobal, getGlobals)

    case T.ClassDefn =>
      Defn.Class(getAttrs, getGlobal, getGlobal, getGlobals)

    case T.ModuleDefn =>
      Defn.Module(getAttrs, getGlobal, getGlobal, getGlobals)
  }

  private def getGlobals(): Seq[Global] = getSeq(getGlobal)
  private def getGlobal(): Global = {
    val name = new Global(getStrings, getBool)
    deps += name
    name
  }

  private def getInsts(): Seq[Inst] = getSeq(getInst)
  private def getInst(): Inst = Inst(getLocal, getOp)

  private def getLocal(): Local = Local(getString, getInt)

  private def getNexts(): Seq[Next] = getSeq(getNext)
  private def getNext(): Next = getInt match {
    case T.SuccNext  => Next.Succ(getLocal)
    case T.FailNext  => Next.Fail(getLocal)
    case T.LabelNext => Next.Label(getLocal, getVals)
    case T.CaseNext  => Next.Case(getVal, getLocal)
  }

  private def getOp(): Op = getInt match {
    case T.CallOp    => Op.Call(getType, getVal, getVals)
    case T.LoadOp    => Op.Load(getType, getVal)
    case T.StoreOp   => Op.Store(getType, getVal, getVal)
    case T.ElemOp    => Op.Elem(getType, getVal, getVals)
    case T.ExtractOp => Op.Extract(getVal, getInts)
    case T.InsertOp  => Op.Insert(getVal, getVal, getInts)
    case T.AllocaOp  => Op.Alloca(getType)
    case T.BinOp     => Op.Bin(getBin, getType, getVal, getVal)
    case T.CompOp    => Op.Comp(getComp, getType, getVal, getVal)
    case T.ConvOp    => Op.Conv(getConv, getType, getVal)

    case T.AllocOp   => Op.Alloc(getType)
    case T.FieldOp   => Op.Field(getType, getVal, getGlobal)
    case T.MethodOp  => Op.Method(getType, getVal, getGlobal)
    case T.ModuleOp  => Op.Module(getGlobal)
    case T.AsOp      => Op.As(getType, getVal)
    case T.IsOp      => Op.Is(getType, getVal)
    case T.CopyOp    => Op.Copy(getVal)
    case T.SizeOfOp  => Op.SizeOf(getType)
    case T.TypeOfOp  => Op.TypeOf(getType)
    case T.ClosureOp => Op.Closure(getType, getVal, getVals)
  }

  private def getParams(): Seq[Val.Local] = getSeq(getParam)
  private def getParam(): Val.Local = Val.Local(getLocal, getType)

  private def getTypes(): Seq[Type] = getSeq(getType)
  private def getType(): Type = getInt match {
    case T.NoneType       => Type.None
    case T.VoidType       => Type.Void
    case T.LabelType      => Type.Label
    case T.VarargType     => Type.Vararg
    case T.BoolType       => Type.Bool
    case T.I8Type         => Type.I8
    case T.I16Type        => Type.I16
    case T.I32Type        => Type.I32
    case T.I64Type        => Type.I64
    case T.F32Type        => Type.F32
    case T.F64Type        => Type.F64
    case T.ArrayType      => Type.Array(getType, getInt)
    case T.PtrType        => Type.Ptr(getType)
    case T.FunctionType   => Type.Function(getTypes, getType)
    case T.StructType     => Type.Struct(getGlobal)
    case T.AnonStructType => Type.AnonStruct(getTypes)

    case T.SizeType       => Type.Size
    case T.UnitType       => Type.Unit
    case T.NothingType    => Type.Nothing
    case T.ClassType      => Type.Class(getGlobal)
    case T.ClassValueType => Type.ClassValue(getGlobal)
    case T.TraitType      => Type.Trait(getGlobal)
    case T.ModuleType     => Type.Module(getGlobal)
  }

  private def getVals(): Seq[Val] = getSeq(getVal)
  private def getVal(): Val = getInt match {
    case T.NoneVal   => Val.None
    case T.TrueVal   => Val.True
    case T.FalseVal  => Val.False
    case T.ZeroVal   => Val.Zero(getType)
    case T.I8Val     => Val.I8(get)
    case T.I16Val    => Val.I16(getShort)
    case T.I32Val    => Val.I32(getInt)
    case T.I64Val    => Val.I64(getLong)
    case T.F32Val    => Val.F32(getFloat)
    case T.F64Val    => Val.F64(getDouble)
    case T.StructVal => Val.Struct(getGlobal, getVals)
    case T.ArrayVal  => Val.Array(getType, getVals)
    case T.LocalVal  => Val.Local(getLocal, getType)
    case T.GlobalVal => Val.Global(getGlobal, getType)

    case T.BitcastVal => Val.Bitcast(getType, getVal)

    case T.UnitVal       => Val.Unit
    case T.StringVal     => Val.String(getString)
    case T.ClassValueVal => Val.ClassValue(getGlobal, getVals)
  }
}
