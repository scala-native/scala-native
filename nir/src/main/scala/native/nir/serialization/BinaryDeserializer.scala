package native
package nir
package serialization

import java.nio.ByteBuffer
import native.nir.{Tags => T}

class BinaryDeserializer(bb: ByteBuffer) {
  import bb._

  def getSeq[T](getT: => T): Seq[T] =
    (1 to getInt).map(_ => getT).toSeq

  def getString(): String = {
    val arr = new Array[Byte](getInt)
    get(arr)
    new String(arr)
  }

  def getBin(): Bin = getInt match {
    case T.AddBin  => Bin.Add
    case T.SubBin  => Bin.Sub
    case T.MulBin  => Bin.Mul
    case T.DivBin  => Bin.Div
    case T.ModBin  => Bin.Mod
    case T.ShlBin  => Bin.Shl
    case T.LshrBin => Bin.Lshr
    case T.AshrBin => Bin.Ashr
    case T.AndBin  => Bin.And
    case T.OrBin   => Bin.Or
    case T.XorBin  => Bin.Xor
  }

  def getComp(): Comp = getInt match {
    case T.EqComp  => Comp.Eq
    case T.NeqComp => Comp.Neq
    case T.LtComp  => Comp.Lt
    case T.LteComp => Comp.Lte
    case T.GtComp  => Comp.Gt
    case T.GteComp => Comp.Gte
  }

  def getConv(): Conv = getInt match {
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

  def getDefns(): Seq[Defn] = getSeq(getDefn)
  def getDefn(): Defn = getInt match {
    case T.VarDefn      => Defn.Var(getName, getType, getVal)
    case T.DeclareDefn  => Defn.Declare(getName, getType)
    case T.DefineDefn   => Defn.Define(getName, getType, getBlocks)
    case T.StructDefn   => Defn.Struct(getName, getDefns)
    case T.IntefaceDefn => Defn.Interface(getName, getNames, getDefns)
    case T.ClassDefn    => Defn.Class(getName, getName, getNames, getDefns)
    case T.ModuleDefn   => Defn.Module(getName, getName, getNames, getDefns)
  }

  def getBlocks(): Seq[Block] = getSeq(getBlock)
  def getBlock(): Block = Block(getName, getParams, getInstrs)

  def getInstrs(): Seq[Instr] = getSeq(getInstr)
  def getInstr(): Instr = Instr(getName, getOp, getType)

  def getParams(): Seq[Param] = getSeq(getParam)
  def getParam(): Param = Param(getName, getType)

  def getNexts(): Seq[Next] = getSeq(getNext)
  def getNext(): Next = Next(getName, getVals)

  def getCases(): Seq[Case] = getSeq(getCase)
  def getCase(): Case = Case(getVal, getNext)

  def getNames(): Seq[Name] = getSeq(getName)
  def getName(): Name = getInt match {
    case T.NoneName        => Name.None
    case T.FreshName       => Name.Fresh(getInt)
    case T.LocalName       => Name.Local(getString)
    case T.PrimName        => Name.Prim(getString)
    case T.ForeignName     => Name.Foreign(getString)
    case T.NestedName      => Name.Nested(getName, getName)
    case T.ClassName       => Name.Class(getString)
    case T.ModuleName      => Name.Module(getString)
    case T.InterfaceName   => Name.Interface(getString)
    case T.FieldName       => Name.Field(getString)
    case T.ConstructorName => Name.Constructor(getNames)
    case T.MethodName      => Name.Method(getString, getNames, getName)
    case T.AccessorName    => Name.Accessor(getName)
    case T.DataName        => Name.Data(getName)
    case T.VtableName      => Name.Vtable(getName)
    case T.ArrayName       => Name.Array(getName)
  }

  def getOp(): Op = getInt match {
    case T.UndefinedOp    => Op.Undefined
    case T.RetOp          => Op.Ret(getVal)
    case T.ThrowOp        => Op.Throw(getVal)
    case T.JumpOp         => Op.Jump(getNext)
    case T.IfOp           => Op.If(getVal, getNext, getNext)
    case T.SwitchOp       => Op.Switch(getVal, getNext, getCases)
    case T.InvokeOp       => Op.Invoke(getVal, getVals, getNext, getNext)

    case T.CallOp         => Op.Call(getType, getVal, getVals)
    case T.LoadOp         => Op.Load(getType, getVal)
    case T.StoreOp        => Op.Store(getType, getVal, getVal)
    case T.ElemOp         => Op.Elem(getType, getVal, getVals)
    case T.ExtractOp      => Op.Extract(getType, getVal, getVal)
    case T.InsertOp       => Op.Insert(getType, getVal, getVal, getVal)
    case T.AllocOp        => Op.Alloc(getType)
    case T.AllocaOp       => Op.Alloca(getType)
    case T.SizeOp         => Op.Size(getType)
    case T.BinOp          => Op.Bin(getBin, getType, getVal, getVal)
    case T.CompOp         => Op.Comp(getComp, getType, getVal, getVal)
    case T.ConvOp         => Op.Conv(getConv, getType, getVal)

    case T.FieldElemOp    => Op.FieldElem(getType, getName, getVal)
    case T.MethodElemOp   => Op.MethodElem(getType, getName, getVal)
    case T.AllocClassOp   => Op.AllocClass(getType)
    case T.AllocArrayOp   => Op.AllocArray(getType, getVal)
    case T.EqualsOp       => Op.Equals(getVal, getVal)
    case T.HashCodeOp     => Op.HashCode(getVal)
    case T.GetClassOp     => Op.GetClass(getVal)
    case T.ClassOfOp      => Op.ClassOf(getType)
    case T.AsInstanceOfOp => Op.AsInstanceOf(getVal, getType)
    case T.IsInstanceOfOp => Op.IsInstanceOf(getVal, getType)
    case T.ArrayLengthOp  => Op.ArrayLength(getVal)
    case T.ArrayElemOp    => Op.ArrayElem(getType, getVal, getVal)
    case T.BoxOp          => Op.Box(getType, getVal)
    case T.UnboxOp        => Op.Unbox(getType, getVal)
    case T.MonitorEnterOp => Op.MonitorEnter(getVal)
    case T.MonitorExitOp  => Op.MonitorExit(getVal)
  }

  def getTypes(): Seq[Type] = getSeq(getType)
  def getType(): Type = getInt match {
    case T.NoneType       => Type.None
    case T.VoidType       => Type.Void
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
    case T.StructType     => Type.Struct(getName)
    case T.UnitType       => Type.Unit
    case T.NothingType    => Type.Nothing
    case T.NullType       => Type.Null
    case T.ClassType      => Type.Class(getName)
    case T.ArrayClassType => Type.ArrayClass(getType)
  }

  def getVals(): Seq[Val] = getSeq(getVal)
  def getVal(): Val = getInt match {
    case T.NoneVal   => Val.None
    case T.ZeroVal   => Val.Zero
    case T.TrueVal   => Val.True
    case T.FalseVal  => Val.False
    case T.I8Val     => Val.I8(get)
    case T.I16Val    => Val.I16(getShort)
    case T.I32Val    => Val.I32(getInt)
    case T.I64Val    => Val.I64(getLong)
    case T.F32Val    => Val.F32(getFloat)
    case T.F64Val    => Val.F64(getDouble)
    case T.StructVal => Val.Struct(getType, getVals)
    case T.ArrayVal  => Val.Array(getType, getVals)
    case T.NameVal   => Val.Name(getType, getName)
    case T.NullVal   => Val.Null
    case T.UnitVal   => Val.Unit
  }
}
