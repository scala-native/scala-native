package native
package nir
package serialization

import java.nio.ByteBuffer
import scala.collection.mutable
import native.nir.{Tags => T}

final class BinaryDeserializer(bb: ByteBuffer) {
  import bb._

  private val externs = mutable.UnrolledBuffer.empty[Global]

  private def ext(n: Global) = {
    externs += n
    n
  }

  final def deserialize(): (Seq[Global], Seq[Defn]) = {
    val defns = getDefns
    (externs, defns)
  }

  private def getSeq[T](getT: => T): Seq[T] =
    (1 to getInt).map(_ => getT).toSeq

  private def getOpt[T](getT: => T): Option[T] =
    if (get == 0) None
    else Some(getT)

  private def getStrings(): Seq[String] = getSeq(getString)
  private def getString(): String = {
    val arr = new Array[Byte](getInt)
    get(arr)
    new String(arr)
  }

  private def getBool(): Boolean = get != 0

  private def getAdvice(): Advice = getInt match {
    case T.NoAdvice   => Advice.No
    case T.HintAdvice => Advice.Hint
    case T.MustAdvice => Advice.Must
  }

  private def getAttrs(): Seq[Attr] = getSeq(getAttr)
  private def getAttr(): Attr = getInt match {
    case T.InlineAttr   => Attr.Inline(getAdvice)
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
  private def getBlock(): Block = Block(getLocal, getParams, getInsts)

  private def getCases(): Seq[Case] = getSeq(getCase)
  private def getCase(): Case = Case(getVal, getNext)

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
    case T.VarDefn      => Defn.Var(getAttrs, getGlobal, getType, getVal)
    case T.DeclareDefn  => Defn.Declare(getAttrs, getGlobal, getType)
    case T.DefineDefn   => Defn.Define(getAttrs, getGlobal, getType, getBlocks)
    case T.StructDefn   => Defn.Struct(getAttrs, getGlobal, getTypes)
    case T.IntefaceDefn => Defn.Interface(getAttrs, getGlobal, getGlobals, getDefns)
    case T.ClassDefn    => Defn.Class(getAttrs, getGlobal, getGlobal, getGlobals, getDefns)
    case T.ModuleDefn   => Defn.Module(getAttrs, getGlobal, getGlobal, getGlobals, getDefns)
  }

  private def getGlobals(): Seq[Global] = getSeq(getGlobal)
  private def getGlobalOpt(): Option[Global] = getOpt(getGlobal)
  private def getGlobal(): Global = new Global(getStrings, getBool)

  private def getInsts(): Seq[Inst] = getSeq(getInst)
  private def getInst(): Inst = Inst(getLocalOpt, getOp)

  private def getLocalOpt(): Option[Local] = getOpt(getLocal)
  private def getLocal(): Local = Local(getString, getInt)

  private def getNexts(): Seq[Next] = getSeq(getNext)
  private def getNext(): Next = Next(getLocal, getVals)

  private def getOp(): Op = getInt match {
    case T.UnreachableOp => Op.Unreachable
    case T.RetOp         => Op.Ret(getVal)
    case T.JumpOp        => Op.Jump(getNext)
    case T.IfOp          => Op.If(getVal, getNext, getNext)
    case T.SwitchOp      => Op.Switch(getVal, getNext, getCases)
    case T.InvokeOp      => Op.Invoke(getType, getVal, getVals, getNext, getNext)

    case T.ThrowOp => Op.Throw(getVal)
    case T.TryOp   => Op.Try(getNext, getNext)

    case T.CallOp    => Op.Call(getType, getVal, getVals)
    case T.LoadOp    => Op.Load(getType, getVal)
    case T.StoreOp   => Op.Store(getType, getVal, getVal)
    case T.ElemOp    => Op.Elem(getType, getVal, getVals)
    case T.ExtractOp => Op.Extract(getType, getVal, getVal)
    case T.InsertOp  => Op.Insert(getType, getVal, getVal, getVal)
    case T.AllocaOp  => Op.Alloca(getType)
    case T.BinOp     => Op.Bin(getBin, getType, getVal, getVal)
    case T.CompOp    => Op.Comp(getComp, getType, getVal, getVal)
    case T.ConvOp    => Op.Conv(getConv, getType, getVal)

    case T.AllocOp     => Op.Alloc(getType)
    case T.FieldOp     => Op.Field(getType, getVal, getGlobal)
    case T.MethodOp    => Op.Method(getType, getVal, getGlobal)
    case T.ModuleOp    => Op.Module(getGlobal)
    case T.AsOp        => Op.As(getType, getVal)
    case T.IsOp        => Op.Is(getType, getVal)
    case T.ArrAllocOp  => Op.ArrAlloc(getType, getVal)
    case T.ArrLengthOp => Op.ArrLength(getVal)
    case T.ArrElemOp   => Op.ArrElem(getType, getVal, getVal)
    case T.CopyOp      => Op.Copy(getVal)
  }

  private def getParams(): Seq[Val.Local] = getSeq(getParam)
  private def getParam(): Val.Local = Val.Local(getLocal, getType)

  private def getTypes(): Seq[Type] = getSeq(getType)
  private def getType(): Type = getInt match {
    case T.NoneType     => Type.None
    case T.VoidType     => Type.Void
    case T.SizeType     => Type.Size
    case T.BoolType     => Type.Bool
    case T.I8Type       => Type.I8
    case T.I16Type      => Type.I16
    case T.I32Type      => Type.I32
    case T.I64Type      => Type.I64
    case T.F32Type      => Type.F32
    case T.F64Type      => Type.F64
    case T.ArrayType    => Type.Array(getType, getInt)
    case T.PtrType      => Type.Ptr(getType)
    case T.FunctionType => Type.Function(getTypes, getType)
    case T.StructType   => Type.Struct(ext(getGlobal))

    case T.UnitType           => Type.Unit
    case T.NothingType        => Type.Nothing
    case T.ClassType          => Type.Class(ext(getGlobal))
    case T.InterfaceClassType => Type.InterfaceClass(ext(getGlobal))
    case T.ModuleClassType    => Type.ModuleClass(ext(getGlobal))
    case T.ArrayClassType     => Type.ArrayClass(getType)
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

    case T.UnitVal   => Val.Unit
    case T.NullVal   => Val.Null
    case T.StringVal => Val.String(getString)
    case T.SizeVal   => Val.Size(getType)
    case T.ClassVal  => Val.Class(getType)
  }
}
