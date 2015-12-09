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

  def putBuiltin(builtin: Builtin) = builtin match {
    case Builtin.Add      => T.AddBuiltin
    case Builtin.Sub      => T.SubBuiltin
    case Builtin.Mul      => T.MulBuiltin
    case Builtin.Div      => T.DivBuiltin
    case Builtin.Mod      => T.ModBuiltin
    case Builtin.Shl      => T.ShlBuiltin
    case Builtin.Lshr     => T.LshrBuiltin
    case Builtin.Ashr     => T.AshrBuiltin
    case Builtin.And      => T.AndBuiltin
    case Builtin.Or       => T.OrBuiltin
    case Builtin.Xor      => T.XorBuiltin
    case Builtin.Eq       => T.EqBuiltin
    case Builtin.Neq      => T.NeqBuiltin
    case Builtin.Lt       => T.LtBuiltin
    case Builtin.Lte      => T.LteBuiltin
    case Builtin.Gt       => T.GtBuiltin
    case Builtin.Gte      => T.GteBuiltin
    case Builtin.Trunc    => T.TruncBuiltin
    case Builtin.Zext     => T.ZextBuiltin
    case Builtin.Sext     => T.SextBuiltin
    case Builtin.Fptrunc  => T.FptruncBuiltin
    case Builtin.Fpext    => T.FpextBuiltin
    case Builtin.Fptoui   => T.FptouiBuiltin
    case Builtin.Fptosi   => T.FptosiBuiltin
    case Builtin.Uitofp   => T.UitofpBuiltin
    case Builtin.Sitofp   => T.SitofpBuiltin
    case Builtin.Ptrtoint => T.PtrtointBuiltin
    case Builtin.Inttoptr => T.InttoptrBuiltin
    case Builtin.Bitcast  => T.BitcastBuiltin
  }

  def putDefns(defns: Seq[Defn]): Unit = putSeq(putDefn)(defns)
  def putDefn(value: Defn): Unit = value match {
    case Defn.Extern(name) =>
      putInt(T.ExternDefn); putName(name)
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
    putVal(next.value)
    putName(next.name)
    putVals(next.args)
  }

  def putNames(names: Seq[Name]): Unit = putSeq(putName)(names)
  def putName(name: Name): Unit = name match {
    case Name.None                  => putInt(T.NoneName)
    case Name.Fresh(id)             => putInt(T.FreshName); putInt(id)
    case Name.Local(id)             => putInt(T.LocalName); putString(id)
    case Name.Extern(id)            => putInt(T.ExternName); putString(id)
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
    case Op.Br(v, thenp, elsep) =>
      putInt(T.BrOp); putVal(v); putNext(thenp); putNext(elsep)
    case Op.Switch(v, default, cases) =>
      putInt(T.SwitchOp); putVal(v); putNext(default); putNexts(cases)
    case Op.Invoke(f, args, succ, fail) =>
      putInt(T.InvokeOp); putVal(f); putVals(args); putNext(succ); putNext(fail)
    case Op.Call(v, args) =>
      putInt(T.CallOp); putVal(v); putVals(args)
    case Op.Load(ty, ptr) =>
      putInt(T.LoadOp); putType(ty); putVal(ptr)
    case Op.Store(ty, value, ptr) =>
      putInt(T.StoreOp); putType(ty); putVal(value); putVal(ptr)
    case Op.Elem(v, indexes) =>
      putInt(T.ElemOp); putVal(v); putVals(indexes)
    case Op.Extract(v, index) =>
      putInt(T.ExtractOp); putVal(v); putVal(index)
    case Op.Insert(v, value, index) =>
      putInt(T.InsertOp); putVal(v); putVal(value); putVal(index)
    case Op.Alloc(ty) =>
      putInt(T.AllocOp); putType(ty)
    case Op.Alloca(ty) =>
      putInt(T.AllocaOp); putType(ty)
    case Op.Size(ty) =>
      putInt(T.SizeOp); putType(ty)
    case Op.Builtin(builtin, tys, args) =>
      putInt(T.BuiltinOp); putBuiltin(builtin); putTypes(tys); putVals(args)
    case Op.FieldElem(name, v) =>
      putInt(T.FieldElemOp); putName(name); putVal(v)
    case Op.MethodElem(name, v) =>
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
    case Op.ArrayElem(v, index) =>
      putInt(T.ArrayElemOp); putVal(v); putVal(index)
    case Op.Box(v, ty) =>
      putInt(T.BoxOp); putVal(v); putType(ty)
    case Op.Unbox(v, ty) =>
      putInt(T.UnboxOp); putVal(v); putType(ty)
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
