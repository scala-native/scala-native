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

  private def putStrings(vs: Seq[String]) = putSeq(putString)(vs)
  private def putString(v: String) = {
    val bytes = v.getBytes
    putInt(bytes.length); put(bytes)
  }

  private def putBool(v: Boolean) = put((if (v) 1 else 0).toByte)

  private def putAttrs(attrs: Seq[Attr]) = putSeq(putAttr)(attrs)
  private def putAttr(attr: Attr) = attr match {
    case Attr.InlineHint => putInt(T.InlineHintAttr)
    case Attr.NoInline   => putInt(T.NoInlineAttr)
    case Attr.MustInline => putInt(T.MustInlineAttr)

    case Attr.Private             => putInt(T.PrivateAttr)
    case Attr.Internal            => putInt(T.InternalAttr)
    case Attr.AvailableExternally => putInt(T.AvailableExternallyAttr)
    case Attr.LinkOnce            => putInt(T.LinkOnceAttr)
    case Attr.Weak                => putInt(T.WeakAttr)
    case Attr.Common              => putInt(T.CommonAttr)
    case Attr.Appending           => putInt(T.AppendingAttr)
    case Attr.ExternWeak          => putInt(T.ExternWeakAttr)
    case Attr.LinkOnceODR         => putInt(T.LinkOnceODRAttr)
    case Attr.WeakODR             => putInt(T.WeakODRAttr)
    case Attr.External            => putInt(T.ExternalAttr)

    case Attr.Override(n) => putInt(T.OverrideAttr); putGlobal(n)
  }

  private def putBin(bin: Bin) = bin match {
    case Bin.Iadd => putInt(T.IaddBin)
    case Bin.Fadd => putInt(T.FaddBin)
    case Bin.Isub => putInt(T.IsubBin)
    case Bin.Fsub => putInt(T.FsubBin)
    case Bin.Imul => putInt(T.ImulBin)
    case Bin.Fmul => putInt(T.FmulBin)
    case Bin.Sdiv => putInt(T.SdivBin)
    case Bin.Udiv => putInt(T.UdivBin)
    case Bin.Fdiv => putInt(T.FdivBin)
    case Bin.Srem => putInt(T.SremBin)
    case Bin.Urem => putInt(T.UremBin)
    case Bin.Frem => putInt(T.FremBin)
    case Bin.Shl  => putInt(T.ShlBin )
    case Bin.Lshr => putInt(T.LshrBin)
    case Bin.Ashr => putInt(T.AshrBin)
    case Bin.And  => putInt(T.AndBin )
    case Bin.Or   => putInt(T.OrBin  )
    case Bin.Xor  => putInt(T.XorBin )
  }

  private def putBlocks(blocks: Seq[Block]) = putSeq(putBlock)(blocks)
  private def putBlock(block: Block) = {
    putLocal(block.name)
    putParams(block.params)
    putInsts(block.insts)
  }

  private def putCases(kases: Seq[Case]) = putSeq(putCase)(kases)
  private def putCase(kase: Case) = {
    putVal(kase.value)
    putNext(kase.next)
  }

  private def putComp(comp: Comp) = comp match {
    case Comp.Ieq => putInt(T.IeqComp)
    case Comp.Ine => putInt(T.IneComp)
    case Comp.Ugt => putInt(T.UgtComp)
    case Comp.Uge => putInt(T.UgeComp)
    case Comp.Ult => putInt(T.UltComp)
    case Comp.Ule => putInt(T.UleComp)
    case Comp.Sgt => putInt(T.SgtComp)
    case Comp.Sge => putInt(T.SgeComp)
    case Comp.Slt => putInt(T.SltComp)
    case Comp.Sle => putInt(T.SleComp)

    case Comp.Feq => putInt(T.FeqComp)
    case Comp.Fne => putInt(T.FneComp)
    case Comp.Fgt => putInt(T.FgtComp)
    case Comp.Fge => putInt(T.FgeComp)
    case Comp.Flt => putInt(T.FltComp)
    case Comp.Fle => putInt(T.FleComp)
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
    case Defn.Const(attrs, name, ty, value) =>
      putInt(T.ConstDefn); putAttrs(attrs); putGlobal(name); putType(ty); putVal(value)
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
  private def putGlobal(global: Global): Unit = {
    putStrings(global.parts)
    putBool(global.isIntrinsic)
  }

  private def putInsts(insts: Seq[Inst]) = putSeq(putInst)(insts)
  private def putInst(inst: Inst) = {
    putLocal(inst.name)
    putOp(inst.op)
  }

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
    case Op.Jump(next) =>
      putInt(T.JumpOp); putNext(next)
    case Op.If(v, thenp, elsep) =>
      putInt(T.IfOp); putVal(v); putNext(thenp); putNext(elsep)
    case Op.Switch(v, default, cases) =>
      putInt(T.SwitchOp); putVal(v); putNext(default); putCases(cases)
    case Op.Invoke(ty, f, args, succ, fail) =>
      putInt(T.InvokeOp); putType(ty); putVal(f); putVals(args); putNext(succ); putNext(fail)

    case Op.Throw(v) =>
      putInt(T.ThrowOp); putVal(v)
    case Op.Try(norm, exc) =>
      putInt(T.TryOp); putNext(norm); putNext(exc)

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

    case Op.Alloc(ty) =>
      putInt(T.AllocOp); putType(ty)
    case Op.Field(ty, v, name) =>
      putInt(T.FieldOp); putType(ty); putVal(v); putGlobal(name)
    case Op.Method(ty, v, name) =>
      putInt(T.MethodOp); putType(ty); putVal(v); putGlobal(name)
    case Op.Module(name) =>
      putInt(T.ModuleOp); putGlobal(name)
    case Op.As(ty, v) =>
      putInt(T.AsOp); putType(ty); putVal(v)
    case Op.Is(ty, v) =>
      putInt(T.IsOp); putType(ty); putVal(v)
    case Op.ArrAlloc(ty, v) =>
      putInt(T.ArrAllocOp); putType(ty); putVal(v)
    case Op.ArrLength(v) =>
      putInt(T.ArrLengthOp); putVal(v)
    case Op.ArrElem(ty, v, index) =>
      putInt(T.ArrElemOp); putType(ty); putVal(v); putVal(index)
    case Op.Copy(v) =>
      putInt(T.CopyOp); putVal(v)
    case Op.SizeOf(ty) =>
      putInt(T.SizeOfOp); putType(ty)
    case Op.ArrSizeOf(ty, v) =>
      putInt(T.ArrSizeOfOp); putType(ty); putVal(v)
    case Op.TypeOf(ty) =>
      putInt(T.TypeOfOp); putType(ty)
    case Op.StringOf(s) =>
      putInt(T.StringOfOp); putString(s)
  }

  private def putParams(params: Seq[Val.Local]) = putSeq(putParam)(params)
  private def putParam(param: Val.Local) = {
    putLocal(param.name)
    putType(param.ty)
  }

  private def putTypes(tys: Seq[Type]): Unit = putSeq(putType)(tys)
  private def putType(ty: Type): Unit = ty match {
    case Type.None                => putInt(T.NoneType)
    case Type.Void                => putInt(T.VoidType)
    case Type.Bool                => putInt(T.BoolType)
    case Type.Label               => putInt(T.LabelType)
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

    case Type.Size              => putInt(T.SizeType)
    case Type.Unit              => putInt(T.UnitType)
    case Type.Nothing           => putInt(T.NothingType)
    case Type.Null              => putInt(T.NullType)
    case Type.Class(n)          => putInt(T.ClassType); putGlobal(n)
    case Type.InterfaceClass(n) => putInt(T.InterfaceClassType); putGlobal(n)
    case Type.ModuleClass(n)    => putInt(T.ModuleClassType); putGlobal(n)
    case Type.ArrayClass(ty)    => putInt(T.ArrayClassType); putType(ty)
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
  }
}
