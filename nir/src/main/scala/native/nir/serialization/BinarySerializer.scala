package native
package nir
package serialization

import java.nio.ByteBuffer
import native.nir.{Tags => T}

final class BinarySerializer(buffer: ByteBuffer) {
  import buffer._

  final def serialize(defns: Seq[Defn]) = putDefns(defns)

  private def putSeq[T](putT: T => Unit)(seq: Seq[T]) = {
    putInt(seq.length)
    seq.foreach(putT)
  }

  private def putOpt[T](putT: T => Unit)(opt: Option[T]) = opt match {
    case None    => put(0.toByte)
    case Some(v) => put(1.toByte); putT(v)
  }

  private def putString(v: String) = {
    val bytes = v.getBytes
    putInt(bytes.length); put(bytes)
  }

  private def putAdvice(adv: Advice) = adv match {
    case Advice.No   => T.NoAdvice
    case Advice.Hint => T.HintAdvice
    case Advice.Must => T.MustAdvice
  }

  private def putAttrs(attrs: Seq[Attr]) = putSeq(putAttr)(attrs)
  private def putAttr(attr: Attr) = attr match {
    case Attr.Usgn => putInt(T.UsgnAttr)

    case Attr.Inline(adv)  => putInt(T.InlineAttr); putAdvice(adv)
    case Attr.Overrides(n) => putInt(T.OverridesAttr); putGlobal(n)
  }

  private def putBin(bin: Bin) = bin match {
    case Bin.Add  => putInt(T.AddBin)
    case Bin.Sub  => putInt(T.SubBin)
    case Bin.Mul  => putInt(T.MulBin)
    case Bin.Div  => putInt(T.DivBin)
    case Bin.Mod  => putInt(T.ModBin)
    case Bin.Shl  => putInt(T.ShlBin)
    case Bin.Lshr => putInt(T.LshrBin)
    case Bin.Ashr => putInt(T.AshrBin)
    case Bin.And  => putInt(T.AndBin)
    case Bin.Or   => putInt(T.OrBin)
    case Bin.Xor  => putInt(T.XorBin)
  }

  private def putBlocks(blocks: Seq[Block]) = putSeq(putBlock)(blocks)
  private def putBlock(block: Block) = {
    putLocal(block.name)
    putParams(block.params)
    putInstrs(block.instrs)
  }

  private def putCases(kases: Seq[Case]) = putSeq(putCase)(kases)
  private def putCase(kase: Case) = {
    putVal(kase.value)
    putNext(kase.next)
  }

  private def putComp(comp: Comp) = comp match {
    case Comp.Eq  => putInt(T.EqComp)
    case Comp.Neq => putInt(T.NeqComp)
    case Comp.Lt  => putInt(T.LtComp)
    case Comp.Lte => putInt(T.LteComp)
    case Comp.Gt  => putInt(T.GtComp)
    case Comp.Gte => putInt(T.GteComp)
  }

  private def putConv(conv: Conv) = conv match {
    case Conv.Trunc    => putInt(T.TruncConv)
    case Conv.Zext     => putInt(T.ZextConv)
    case Conv.Sext     => putInt(T.SextConv)
    case Conv.Fptrunc  => putInt(T.FptruncConv)
    case Conv.Fpext    => putInt(T.FpextConv)
    case Conv.Fptoui   => putInt(T.FptouiConv)
    case Conv.Fptosi   => putInt(T.FptosiConv)
    case Conv.Uitofp   => putInt(T.UitofpConv)
    case Conv.Sitofp   => putInt(T.SitofpConv)
    case Conv.Ptrtoint => putInt(T.PtrtointConv)
    case Conv.Inttoptr => putInt(T.InttoptrConv)
    case Conv.Bitcast  => putInt(T.BitcastConv)
  }

  private def putDefns(defns: Seq[Defn]): Unit = putSeq(putDefn)(defns)
  private def putDefn(value: Defn): Unit = value match {
    case Defn.Var(attrs, name, ty, value) =>
      putInt(T.VarDefn); putAttrs(attrs); putGlobal(name); putType(ty); putVal(value)
    case Defn.Declare(attrs, name, ty) =>
      putInt(T.DeclareDefn); putAttrs(attrs); putGlobal(name); putType(ty)
    case Defn.Define(attrs, name, ty, blocks) =>
      putInt(T.DefineDefn); putAttrs(attrs); putGlobal(name); putType(ty); putBlocks(blocks)
    case Defn.Struct(attrs, name, members) =>
      putInt(T.StructDefn); putAttrs(attrs); putGlobal(name); putTypes(members)
    case Defn.Interface(attrs, name, ifaces, members) =>
      putInt(T.IntefaceDefn); putAttrs(attrs); putGlobal(name); putGlobals(ifaces); putDefns(members)
    case Defn.Class(attrs, name, parent, ifaces, members) =>
      putInt(T.ClassDefn); putAttrs(attrs); putGlobal(name); putGlobal(parent); putGlobals(ifaces); putDefns(members)
    case Defn.Module(attrs, name, parent, ifaces, members) =>
      putInt(T.ModuleDefn); putAttrs(attrs); putGlobal(name); putGlobal(parent); putGlobals(ifaces); putDefns(members)
  }

  private def putGlobals(globals: Seq[Global]): Unit = putSeq(putGlobal)(globals)
  private def putGlobalOpt(globalopt: Option[Global]): Unit = putOpt(putGlobal)(globalopt)
  private def putGlobal(global: Global): Unit = global match {
    case Global.Atom(id)              => putInt(T.AtomGlobal); putString(id)
    case Global.Nested(owner, member) => putInt(T.NestedGlobal); putGlobal(owner); putGlobal(member)
    case Global.Tagged(n, tag)        => putInt(T.TaggedGlobal); putGlobal(n); putGlobal(tag)
    case Global.Intrinsic(id)         => putInt(T.IntrinsicGlobal); putString(id)
  }

  private def putInstrs(instrs: Seq[Instr]) = putSeq(putInstr)(instrs)
  private def putInstr(instr: Instr) = {
    putLocalOpt(instr.name)
    putAttrs(instr.attrs)
    putOp(instr.op)
  }

  private def putLocalOpt(opt: Option[Local]): Unit = putOpt(putLocal)(opt)
  private def putLocal(local: Local): Unit = { putString(local.scope); putInt(local.id) }

  private def putNexts(nexts: Seq[Next]) = putSeq(putNext)(nexts)
  private def putNext(next: Next) = {
    putLocal(next.name)
    putVals(next.args)
  }

  private def putOp(op: Op) = op match {
    case Op.Unreachable =>
      putInt(T.UnreachableOp)
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
    case Op.Invoke(ty, f, args, succ, fail) =>
      putInt(T.InvokeOp); putType(ty); putVal(f); putVals(args); putNext(succ); putNext(fail)

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
    case Op.Alloca(ty) =>
      putInt(T.AllocaOp); putType(ty)
    case Op.Bin(bin, ty, l, r) =>
      putInt(T.BinOp); putBin(bin); putType(ty); putVal(l); putVal(r)
    case Op.Comp(comp, ty, l, r) =>
      putInt(T.CompOp); putComp(comp); putType(ty); putVal(l); putVal(r)
    case Op.Conv(conv, ty, v) =>
      putInt(T.ConvOp); putConv(conv); putType(ty); putVal(v)

    case Op.ObjAlloc(ty) =>
      putInt(T.ObjAllocOp); putType(ty)
    case Op.ObjFieldElem(ty, v, name) =>
      putInt(T.ObjFieldElemOp); putType(ty); putVal(v); putGlobal(name)
    case Op.ObjMethodElem(ty, v, name) =>
      putInt(T.ObjMethodElemOp); putType(ty); putVal(v); putGlobal(name)
    case Op.ObjAs(ty, v) =>
      putInt(T.ObjAsOp); putType(ty); putVal(v)
    case Op.ObjIs(ty, v) =>
      putInt(T.ObjIsOp); putType(ty); putVal(v)
    case Op.ArrAlloc(ty, v) =>
      putInt(T.ArrAllocOp); putType(ty); putVal(v)
    case Op.ArrLength(v) =>
      putInt(T.ArrLengthOp); putVal(v)
    case Op.ArrElem(ty, v, index) =>
      putInt(T.ArrElemOp); putType(ty); putVal(v); putVal(index)
    case Op.Copy(v) =>
      putInt(T.CopyOp); putVal(v)
  }

  private def putParams(params: Seq[Param]) = putSeq(putParam)(params)
  private def putParam(param: Param) = {
    putLocal(param.name)
    putType(param.ty)
  }

  private def putTypes(tys: Seq[Type]): Unit = putSeq(putType)(tys)
  private def putType(ty: Type): Unit = ty match {
    case Type.None                => putInt(T.NoneType)
    case Type.Void                => putInt(T.VoidType)
    case Type.Size                => putInt(T.SizeType)
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
    case Type.Struct(n)           => putInt(T.StructType); putGlobal(n)

    case Type.Unit                => putInt(T.UnitType)
    case Type.Nothing             => putInt(T.NothingType)
    case Type.NullClass           => putInt(T.NullClassType)
    case Type.ObjectClass         => putInt(T.ObjectClassType)
    case Type.ClassClass          => putInt(T.ClassClassType)
    case Type.StringClass         => putInt(T.StringClassType)
    case Type.CharacterClass      => putInt(T.CharacterClassType)
    case Type.BooleanClass        => putInt(T.BooleanClassType)
    case Type.ByteClass           => putInt(T.ByteClassType)
    case Type.ShortClass          => putInt(T.ShortClassType)
    case Type.IntegerClass        => putInt(T.IntegerClassType)
    case Type.LongClass           => putInt(T.LongClassType)
    case Type.FloatClass          => putInt(T.FloatClassType)
    case Type.DoubleClass         => putInt(T.DoubleClassType)
    case Type.Class(n)            => putInt(T.ClassType); putGlobal(n)
    case Type.InterfaceClass(n)   => putInt(T.InterfaceClassType); putGlobal(n)
    case Type.ModuleClass(n)      => putInt(T.ModuleClassType); putGlobal(n)
    case Type.ArrayClass(ty)      => putInt(T.ArrayClassType); putType(ty)
  }

  private def putVals(values: Seq[Val]): Unit = putSeq(putVal)(values)
  private def putVal(value: Val): Unit = value match {
    case Val.None          => putInt(T.NoneVal)
    case Val.True          => putInt(T.TrueVal)
    case Val.False         => putInt(T.FalseVal)
    case Val.Zero(ty)      => putInt(T.ZeroVal); putType(ty)
    case Val.I8(v)         => putInt(T.I8Val); put(v)
    case Val.I16(v)        => putInt(T.I16Val); putShort(v)
    case Val.I32(v)        => putInt(T.I32Val); putInt(v)
    case Val.I64(v)        => putInt(T.I64Val); putLong(v)
    case Val.F32(v)        => putInt(T.F32Val); putFloat(v)
    case Val.F64(v)        => putInt(T.F64Val); putDouble(v)
    case Val.Struct(n, vs) => putInt(T.StructVal); putGlobal(n); putVals(vs)
    case Val.Array(ty, vs) => putInt(T.ArrayVal); putType(ty); putVals(vs)
    case Val.Chars(s)      => putInt(T.CharsVal); putString(s)
    case Val.Local(n, ty)  => putInt(T.LocalVal); putLocal(n); putType(ty)
    case Val.Global(n, ty) => putInt(T.GlobalVal); putGlobal(n); putType(ty)

    case Val.Unit             => putInt(T.UnitVal)
    case Val.Null             => putInt(T.NullVal)
    case Val.String(v)        => putInt(T.StringVal); putString(v)
    case Val.Size(ty)         => putInt(T.SizeVal); putType(ty)
    case Val.Class(ty)        => putInt(T.ClassVal); putType(ty)
  }
}
