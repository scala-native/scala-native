package native
package nir
package serialization

import java.nio.ByteBuffer
import native.nir.{Tags => T}

class BinarySerializer(buffer: ByteBuffer) {
  import buffer._

  def putSeq[T](putT: T => Unit)(seq: Seq[T]) = {
    putInt(seq.length)
    seq.foreach(putT)
  }

  def putString(v: String) = {
    val bytes = v.getBytes
    putInt(bytes.length); put(bytes)
  }

  def putBin(bin: Bin) = bin match {
    case Bin.Add  => T.AddBin
    case Bin.Sub  => T.SubBin
    case Bin.Mul  => T.MulBin
    case Bin.Div  => T.DivBin
    case Bin.Mod  => T.ModBin
    case Bin.Shl  => T.ShlBin
    case Bin.Lshr => T.LshrBin
    case Bin.Ashr => T.AshrBin
    case Bin.And  => T.AndBin
    case Bin.Or   => T.OrBin
    case Bin.Xor  => T.XorBin
  }

  def putComp(comp: Comp) = comp match {
    case Comp.Eq  => T.EqComp
    case Comp.Neq => T.NeqComp
    case Comp.Lt  => T.LtComp
    case Comp.Lte => T.LteComp
    case Comp.Gt  => T.GtComp
    case Comp.Gte => T.GteComp
  }

  def putConv(conv: Conv) = conv match {
    case Conv.Trunc    => T.TruncConv
    case Conv.Zext     => T.ZextConv
    case Conv.Sext     => T.SextConv
    case Conv.Fptrunc  => T.FptruncConv
    case Conv.Fpext    => T.FpextConv
    case Conv.Fptoui   => T.FptouiConv
    case Conv.Fptosi   => T.FptosiConv
    case Conv.Uitofp   => T.UitofpConv
    case Conv.Sitofp   => T.SitofpConv
    case Conv.Ptrtoint => T.PtrtointConv
    case Conv.Inttoptr => T.InttoptrConv
    case Conv.Bitcast  => T.BitcastConv
  }

  def putDefns(defns: Seq[Defn]): Unit = putSeq(putDefn)(defns)
  def putDefn(value: Defn): Unit = value match {
    case Defn.Var(name, ty, value) =>
      putInt(T.VarDefn); putName(name); putType(ty); putVal(value)
    case Defn.Declare(name, ty) =>
      putInt(T.DeclareDefn); putName(name); putType(ty)
    case Defn.Define(name, ty, blocks) =>
      putInt(T.DefineDefn); putName(name); putType(ty); putBlocks(blocks)
    case Defn.Struct(name, members) =>
      putInt(T.StructDefn); putName(name); putDefns(members)
    case Defn.Interface(name, ifaces, members) =>
      putInt(T.IntefaceDefn); putName(name); putNames(ifaces); putDefns(members)
    case Defn.Class(name, parent, ifaces, members) =>
      putInt(T.ClassDefn); putName(name); putName(parent); putNames(ifaces); putDefns(members)
    case Defn.Module(name, parent, ifaces, members) =>
      putInt(T.ModuleDefn); putName(name); putName(parent); putNames(ifaces); putDefns(members)
  }

  def putBlocks(blocks: Seq[Block]) = putSeq(putBlock)(blocks)
  def putBlock(block: Block) = {
    putName(block.name)
    putParams(block.params)
    putInstrs(block.instrs)
  }

  def putInstrs(instrs: Seq[Instr]) = putSeq(putInstr)(instrs)
  def putInstr(instr: Instr) = {
    putName(instr.name)
    putOp(instr.op)
    putType(instr.ty)
  }

  def putParams(params: Seq[Param]) = putSeq(putParam)(params)
  def putParam(param: Param) = {
    putName(param.name)
    putType(param.ty)
  }

  def putNexts(nexts: Seq[Next]) = putSeq(putNext)(nexts)
  def putNext(next: Next) = {
    putName(next.name)
    putVals(next.args)
  }

  def putCases(kases: Seq[Case]) = putSeq(putCase)(kases)
  def putCase(kase: Case) = {
    putVal(kase.value)
    putNext(kase.next)
  }

  def putNames(names: Seq[Name]): Unit = putSeq(putName)(names)
  def putName(name: Name): Unit = name match {
    case Name.None                  => putInt(T.NoneName)
    case Name.Fresh(id)             => putInt(T.FreshName); putInt(id)
    case Name.Local(id)             => putInt(T.LocalName); putString(id)
    case Name.Prim(id)              => putInt(T.PrimName); putString(id)
    case Name.Foreign(id)           => putInt(T.ForeignName); putString(id)
    case Name.Nested(owner, member) => putInt(T.NestedName); putName(owner); putName(member)
    case Name.Class(id)             => putInt(T.ClassName); putString(id)
    case Name.Module(id)            => putInt(T.ModuleName); putString(id)
    case Name.Interface(id)         => putInt(T.InterfaceName); putString(id)
    case Name.Field(id)             => putInt(T.FieldName); putString(id)
    case Name.Constructor(args)     => putInt(T.ConstructorName); putNames(args)
    case Name.Method(id, args, ret) => putInt(T.MethodName); putString(id); putNames(args); putName(ret)
    case Name.Accessor(owner)       => putInt(T.AccessorName); putName(owner)
    case Name.Data(owner)           => putInt(T.DataName); putName(owner)
    case Name.Vtable(owner)         => putInt(T.VtableName); putName(owner)
    case Name.Array(of)             => putInt(T.ArrayName); putName(of)
  }

  def putOp(op: Op) = op match {
    case Op.Undefined =>
      putInt(T.UndefinedOp)
    case Op.Ret(v) =>
      putInt(T.RetOp); putVal(v)
    case Op.Throw(v) =>
      putInt(T.ThrowOp); putVal(v)
    case Op.Jump(next) =>
      putInt(T.JumpOp); putNext(next)
    case Op.If(v, thenp, elsep) =>
      putInt(T.IfOp); putVal(v); putNext(thenp); putNext(elsep)
    case Op.Switch(v, default, cases) =>
      putInt(T.SwitchOp); putVal(v); putNext(default); putCases(cases)
    case Op.Invoke(f, args, succ, fail) =>
      putInt(T.InvokeOp); putVal(f); putVals(args); putNext(succ); putNext(fail)

    case Op.Call(ty, v, args) =>
      putInt(T.CallOp); putType(ty); putVal(v); putVals(args)
    case Op.Load(ty, ptr) =>
      putInt(T.LoadOp); putType(ty); putVal(ptr)
    case Op.Store(ty, value, ptr) =>
      putInt(T.StoreOp); putType(ty); putVal(value); putVal(ptr)
    case Op.Elem(ty, v, indexes) =>
      putInt(T.ElemOp); putType(ty); putVal(v); putVals(indexes)
    case Op.Extract(ty, v, index) =>
      putInt(T.ExtractOp); putType(ty); putVal(v); putVal(index)
    case Op.Insert(ty, v, value, index) =>
      putInt(T.InsertOp); putType(ty); putVal(v); putVal(value); putVal(index)
    case Op.Alloc(ty) =>
      putInt(T.AllocOp); putType(ty)
    case Op.Alloca(ty) =>
      putInt(T.AllocaOp); putType(ty)
    case Op.Size(ty) =>
      putInt(T.SizeOp); putType(ty)
    case Op.Bin(bin, ty, l, r) =>
      putInt(T.BinOp); putBin(bin); putType(ty); putVal(l); putVal(r)
    case Op.Comp(comp, ty, l, r) =>
      putInt(T.CompOp); putComp(comp); putType(ty); putVal(l); putVal(r)
    case Op.Conv(conv, ty, v) =>
      putInt(T.ConvOp); putConv(conv); putType(ty); putVal(v)

    case Op.FieldElem(ty, name, v) =>
      putInt(T.FieldElemOp); putName(name); putVal(v)
    case Op.MethodElem(ty, name, v) =>
      putInt(T.MethodElemOp); putName(name); putVal(v)
    case Op.AllocClass(ty) =>
      putInt(T.AllocClassOp); putType(ty)
    case Op.AllocArray(ty, v) =>
      putInt(T.AllocArrayOp); putType(ty); putVal(v)
    case Op.Equals(l, r) =>
      putInt(T.EqualsOp); putVal(l); putVal(r)
    case Op.HashCode(v) =>
      putInt(T.HashCodeOp); putVal(v)
    case Op.GetClass(v) =>
      putInt(T.GetClassOp); putVal(v)
    case Op.ClassOf(ty) =>
      putInt(T.ClassOfOp); putType(ty)
    case Op.AsInstanceOf(v, ty) =>
      putInt(T.AsInstanceOfOp); putVal(v); putType(ty)
    case Op.IsInstanceOf(v, ty) =>
      putInt(T.IsInstanceOfOp); putVal(v); putType(ty)
    case Op.ArrayLength(v) =>
      putInt(T.ArrayLengthOp); putVal(v)
    case Op.ArrayElem(ty, v, index) =>
      putInt(T.ArrayElemOp); putType(ty); putVal(v); putVal(index)
    case Op.Box(ty, v) =>
      putInt(T.BoxOp); putType(ty); putVal(v)
    case Op.Unbox(ty, v) =>
      putInt(T.UnboxOp); putType(ty); putVal(v)
    case Op.MonitorEnter(v) =>
      putInt(T.MonitorEnterOp); putVal(v)
    case Op.MonitorExit(v) =>
      putInt(T.MonitorExitOp); putVal(v)
  }

  def putTypes(tys: Seq[Type]): Unit = putSeq(putType)(tys)
  def putType(ty: Type): Unit = ty match {
    case Type.None                => putInt(T.NoneType)
    case Type.Void                => putInt(T.VoidType)
    case Type.Bool                => putInt(T.BoolType)
    case Type.I8                  => putInt(T.I8Type)
    case Type.I16                 => putInt(T.I16Type)
    case Type.I32                 => putInt(T.I32Type)
    case Type.I64                 => putInt(T.I64Type)
    case Type.F32                 => putInt(T.F32Type)
    case Type.F64                 => putInt(T.F64Type)
    case Type.Array(ty, n)        => putInt(T.ArrayType); putType(ty); putInt(n)
    case Type.Ptr(ty)             => putInt(T.PtrType); putType(ty)
    case Type.Function(args, ret) => putInt(T.FunctionType); putTypes(args); putType(ret)
    case Type.Struct(n)           => putInt(T.StructType); putName(n)
    case Type.Unit                => putInt(T.UnitType)
    case Type.Nothing             => putInt(T.NothingType)
    case Type.Null                => putInt(T.NullType)
    case Type.Class(n)            => putInt(T.ClassType); putName(n)
    case Type.ArrayClass(ty)      => putInt(T.ArrayClassType); putType(ty)
  }

  def putVals(values: Seq[Val]): Unit = putSeq(putVal)(values)
  def putVal(value: Val): Unit = value match {
    case Val.None       => putInt(T.NoneVal)
    case Val.Zero       => putInt(T.ZeroVal)
    case Val.True       => putInt(T.TrueVal)
    case Val.False      => putInt(T.FalseVal)
    case Val.I8(v)      => putInt(T.I8Val); put(v)
    case Val.I16(v)     => putInt(T.I16Val); putShort(v)
    case Val.I32(v)     => putInt(T.I32Val); putInt(v)
    case Val.I64(v)     => putInt(T.I64Val); putLong(v)
    case Val.F32(v)     => putInt(T.F32Val); putFloat(v)
    case Val.F64(v)     => putInt(T.F64Val); putDouble(v)
    case Val.Struct(vs) => putInt(T.StructVal); putVals(vs)
    case Val.Array(vs)  => putInt(T.ArrayVal); putVals(vs)
    case Val.Name(n)    => putInt(T.NameVal); putName(n)
    case Val.Null       => putInt(T.NullVal)
    case Val.Unit       => putInt(T.UnitVal)
  }
}
