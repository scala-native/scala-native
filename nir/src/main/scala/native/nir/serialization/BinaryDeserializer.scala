package native
package nir
package serialization

import java.nio.ByteBuffer
import scala.collection.mutable
import native.nir.{Tags => T}

final class BinaryDeserializer(bb: ByteBuffer) {
  import bb._

  private val externs = mutable.UnrolledBuffer.empty[Name]

  private def ext(n: Name): Name = {
    externs += n
    n
  }

  final def deserialize(): (Seq[Name], Seq[Defn]) = {
    val defns = getDefns
    (externs, defns)
  }

  private def getSeq[T](getT: => T): Seq[T] =
    (1 to getInt).map(_ => getT).toSeq

  private def getString(): String = {
    val arr = new Array[Byte](getInt)
    get(arr)
    new String(arr)
  }

  private def getAttrs(): Seq[Attr] = getSeq(getAttr)
  private def getAttr(): Attr = getInt match {
    case T.UsgnAttr => Attr.Usgn
  }

  private def getBin(): Bin = getInt match {
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

  private def getComp(): Comp = getInt match {
    case T.EqComp  => Comp.Eq
    case T.NeqComp => Comp.Neq
    case T.LtComp  => Comp.Lt
    case T.LteComp => Comp.Lte
    case T.GtComp  => Comp.Gt
    case T.GteComp => Comp.Gte
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
    case T.VarDefn      => Defn.Var(getName, getType, getVal)
    case T.DeclareDefn  => Defn.Declare(getName, getType)
    case T.DefineDefn   => Defn.Define(getName, getType, getBlocks)
    case T.StructDefn   => Defn.Struct(getName, getDefns)
    case T.IntefaceDefn => Defn.Interface(getName, getTypes, getDefns)
    case T.ClassDefn    => Defn.Class(getName, getType, getTypes, getDefns)
    case T.ModuleDefn   => Defn.Module(getName, getType, getTypes, getDefns)
  }

  private def getBlocks(): Seq[Block] = getSeq(getBlock)
  private def getBlock(): Block = Block(getName, getParams, getInstrs)

  private def getInstrs(): Seq[Instr] = getSeq(getInstr)
  private def getInstr(): Instr = Instr(getName, getAttrs, getOp)

  private def getParams(): Seq[Param] = getSeq(getParam)
  private def getParam(): Param = Param(getName, getType)

  private def getNexts(): Seq[Next] = getSeq(getNext)
  private def getNext(): Next = Next(getName, getVals)

  private def getCases(): Seq[Case] = getSeq(getCase)
  private def getCase(): Case = Case(getVal, getNext)

  private def getNames(): Seq[Name] = getSeq(getName)
  private def getName(): Name = getInt match {
    case T.NoneName        => Name.None
    case T.FreshName       => Name.Fresh(getInt)
    case T.LocalName       => Name.Local(getString)
    case T.PrimName        => Name.Prim(getString)
    case T.ForeignName     => Name.Foreign(getString)
    case T.NestedName      => Name.Nested(getName, getName)
    case T.ClassName       => ext(Name.Class(getString))
    case T.ModuleName      => ext(Name.Module(getString))
    case T.InterfaceName   => ext(Name.Interface(getString))
    case T.FieldName       => Name.Field(getString)
    case T.ConstructorName => Name.Constructor(getNames)
    case T.MethodName      => Name.Method(getString, getNames, getName)
    case T.ArrayName       => Name.Array(getName)
    case T.TaggedName      => Name.Tagged(getName, getString)
  }

  private def getOp(): Op = getInt match {
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
    case T.AsInstanceOfOp => Op.AsInstanceOf(getType, getVal)
    case T.IsInstanceOfOp => Op.IsInstanceOf(getType, getVal)
    case T.ArrayLengthOp  => Op.ArrayLength(getVal)
    case T.ArrayElemOp    => Op.ArrayElem(getType, getVal, getVal)
    case T.BoxOp          => Op.Box(getType, getVal)
    case T.UnboxOp        => Op.Unbox(getType, getVal)
    case T.MonitorEnterOp => Op.MonitorEnter(getVal)
    case T.MonitorExitOp  => Op.MonitorExit(getVal)
    case T.StringConcatOp => Op.StringConcat(getVal, getVal)
    case T.ToStringOp     => Op.ToString(getVal, getVal)
    case T.FromStringOp   => Op.FromString(getType, getVal, getVal)
  }

  private def getTypes(): Seq[Type] = getSeq(getType)
  private def getType(): Type = getInt match {
    case T.NoneType           => Type.None
    case T.VoidType           => Type.Void
    case T.SizeType           => Type.Size
    case T.BoolType           => Type.Bool
    case T.I8Type             => Type.I8
    case T.I16Type            => Type.I16
    case T.I32Type            => Type.I32
    case T.I64Type            => Type.I64
    case T.F32Type            => Type.F32
    case T.F64Type            => Type.F64
    case T.ArrayType          => Type.Array(getType, getInt)
    case T.PtrType            => Type.Ptr(getType)
    case T.FunctionType       => Type.Function(getTypes, getType)
    case T.StructType         => Type.Struct(getName)

    case T.UnitType           => Type.Unit
    case T.NothingType        => Type.Nothing
    case T.NullClassType      => Type.NullClass
    case T.ObjectClassType    => Type.ObjectClass
    case T.ClassClassType     => Type.ClassClass
    case T.StringClassType    => Type.StringClass
    case T.CharacterClassType => Type.CharacterClass
    case T.BooleanClassType   => Type.BooleanClass
    case T.ByteClassType      => Type.ByteClass
    case T.ShortClassType     => Type.ShortClass
    case T.IntegerClassType   => Type.IntegerClass
    case T.LongClassType      => Type.LongClass
    case T.FloatClassType     => Type.FloatClass
    case T.DoubleClassType    => Type.DoubleClass
    case T.ClassType          => Type.Class(getName)
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
    case T.StructVal => Val.Struct(getType, getVals)
    case T.ArrayVal  => Val.Array(getType, getVals)
    case T.NameVal   => Val.Name(getName, getType)

    case T.UnitVal   => Val.Unit
    case T.NullVal   => Val.Null
    case T.StringVal => Val.String(getString)
    case T.ClassVal  => Val.Class(getType)
  }
}
