package native
package nir
package serialization

import java.nio.ByteBuffer
import native.nir.{Tags => T}

class BinaryDeserializer(bb: ByteBuffer) {
  import bb._

  def getSeq[T](getT: => T): Seq[T] =
    (1 to getInt).map(_ => getT).toSeq

  private def getString(): String = {
    val arr = new Array[Byte](getInt)
    get(arr)
    new String(arr)
  }

  def getBuiltin(): Builtin = getInt match {
    case T.AddBuiltin      => Builtin.Add
    case T.SubBuiltin      => Builtin.Sub
    case T.MulBuiltin      => Builtin.Mul
    case T.DivBuiltin      => Builtin.Div
    case T.ModBuiltin      => Builtin.Mod
    case T.ShlBuiltin      => Builtin.Shl
    case T.LshrBuiltin     => Builtin.Lshr
    case T.AshrBuiltin     => Builtin.Ashr
    case T.AndBuiltin      => Builtin.And
    case T.OrBuiltin       => Builtin.Or
    case T.XorBuiltin      => Builtin.Xor
    case T.EqBuiltin       => Builtin.Eq
    case T.NeqBuiltin      => Builtin.Neq
    case T.LtBuiltin       => Builtin.Lt
    case T.LteBuiltin      => Builtin.Lte
    case T.GtBuiltin       => Builtin.Gt
    case T.GteBuiltin      => Builtin.Gte
    case T.TruncBuiltin    => Builtin.Trunc
    case T.ZextBuiltin     => Builtin.Zext
    case T.SextBuiltin     => Builtin.Sext
    case T.FptruncBuiltin  => Builtin.Fptrunc
    case T.FpextBuiltin    => Builtin.Fpext
    case T.FptouiBuiltin   => Builtin.Fptoui
    case T.FptosiBuiltin   => Builtin.Fptosi
    case T.UitofpBuiltin   => Builtin.Uitofp
    case T.SitofpBuiltin   => Builtin.Sitofp
    case T.PtrtointBuiltin => Builtin.Ptrtoint
    case T.InttoptrBuiltin => Builtin.Inttoptr
    case T.BitcastBuiltin  => Builtin.Bitcast
  }

  def getDefns(): Seq[Defn] = getSeq(getDefn)
  def getDefn(): Defn = getInt match {
    case T.ExternDefn   => Defn.Extern(getName)
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
  def getNext(): Next = Next(getVal, getName, getVals)

  def getNames(): Seq[Name] = getSeq(getName)
  def getName(): Name = getInt match {
    case T.NoneName        => Name.None
    case T.FreshName       => Name.Fresh(getInt)
    case T.LocalName       => Name.Local(getString)
    case T.ExternName      => Name.Extern(getString)
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
    case T.BrOp           => Op.Br(getVal, getNext, getNext)
    case T.SwitchOp       => Op.Switch(getVal, getNext, getNexts)
    case T.InvokeOp       => Op.Invoke(getVal, getVals, getNext, getNext)
    case T.CallOp         => Op.Call(getVal, getVals)
    case T.LoadOp         => Op.Load(getType, getVal)
    case T.StoreOp        => Op.Store(getType, getVal, getVal)
    case T.ElemOp         => Op.Elem(getVal, getVals)
    case T.ExtractOp      => Op.Extract(getVal, getVal)
    case T.InsertOp       => Op.Insert(getVal, getVal, getVal)
    case T.AllocOp        => Op.Alloc(getType)
    case T.AllocaOp       => Op.Alloca(getType)
    case T.SizeOp         => Op.Size(getType)
    case T.BuiltinOp      => Op.Builtin(getBuiltin, getTypes, getVals)
    case T.FieldElemOp    => Op.FieldElem(getName, getVal)
    case T.MethodElemOp   => Op.MethodElem(getName, getVal)
    case T.AllocClassOp   => Op.AllocClass(getType)
    case T.AllocArrayOp   => Op.AllocArray(getType, getVal)
    case T.EqualsOp       => Op.Equals(getVal, getVal)
    case T.HashCodeOp     => Op.HashCode(getVal)
    case T.GetClassOp     => Op.GetClass(getVal)
    case T.ClassOfOp      => Op.ClassOf(getType)
    case T.AsInstanceOfOp => Op.AsInstanceOf(getVal, getType)
    case T.IsInstanceOfOp => Op.IsInstanceOf(getVal, getType)
    case T.ArrayLengthOp  => Op.ArrayLength(getVal)
    case T.ArrayElemOp    => Op.ArrayElem(getVal, getVal)
    case T.BoxOp          => Op.Box(getVal, getType)
    case T.UnboxOp        => Op.Unbox(getVal, getType)
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
    case T.StructVal => Val.Struct(getVals)
    case T.ArrayVal  => Val.Array(getVals)
    case T.NameVal   => Val.Name(getName)
    case T.NullVal   => Val.Null
    case T.UnitVal   => Val.Unit
  }
}
