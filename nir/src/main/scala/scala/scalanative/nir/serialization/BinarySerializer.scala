package scala.scalanative
package nir
package serialization

import java.nio.ByteBuffer
import scala.collection.mutable
import nir.{Tags => T}

final class BinarySerializer(buffer: ByteBuffer) {
  import buffer._

  final def serialize(defns: Seq[Defn]): Unit = {
    val names     = defns.map(_.name)
    val positions = mutable.UnrolledBuffer.empty[Int]

    putInt(Versions.magic)
    putInt(Versions.compat)
    putInt(Versions.revision)

    putSeq(names) { n =>
      putGlobal(n)
      positions += buffer.position
      putInt(0)
    }

    val offsets = defns.map { defn =>
      val pos: Int = buffer.position
      putDefn(defn)
      pos
    }
    val end = buffer.position

    positions.zip(offsets).map {
      case (pos, offset) =>
        buffer.position(pos)
        putInt(offset)
    }
    buffer.position(end)
  }

  private def putSeq[T](seq: Seq[T])(putT: T => Unit) = {
    putInt(seq.length)
    seq.foreach(putT)
  }

  private def putOpt[T](opt: Option[T])(putT: T => Unit) = opt match {
    case None    => put(0.toByte)
    case Some(t) => put(1.toByte); putT(t)
  }

  private def putInts(ints: Seq[Int]) = putSeq[Int](ints)(putInt(_))

  private def putStrings(vs: Seq[String]) = putSeq(vs)(putString)
  private def putString(v: String) = {
    val bytes = v.getBytes
    putInt(bytes.length); put(bytes)
  }

  private def putBool(v: Boolean) = put((if (v) 1 else 0).toByte)

  private def putAttrs(attrs: Attrs) = putSeq(attrs.toSeq)(putAttr)
  private def putAttr(attr: Attr) = attr match {
    case Attr.MayInline    => putInt(T.MayInlineAttr)
    case Attr.InlineHint   => putInt(T.InlineHintAttr)
    case Attr.NoInline     => putInt(T.NoInlineAttr)
    case Attr.AlwaysInline => putInt(T.AlwaysInlineAttr)

    case Attr.Pure        => putInt(T.PureAttr)
    case Attr.Extern      => putInt(T.ExternAttr)
    case Attr.Override(n) => putInt(T.OverrideAttr); putGlobal(n)

    case Attr.Link(s)      => putInt(T.LinkAttr); putString(s)
    case Attr.PinAlways(n) => putInt(T.PinAlwaysAttr); putGlobal(n)
    case Attr.PinIf(n, cond) =>
      putInt(T.PinIfAttr); putGlobal(n); putGlobal(cond)
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
    case Bin.Shl  => putInt(T.ShlBin)
    case Bin.Lshr => putInt(T.LshrBin)
    case Bin.Ashr => putInt(T.AshrBin)
    case Bin.And  => putInt(T.AndBin)
    case Bin.Or   => putInt(T.OrBin)
    case Bin.Xor  => putInt(T.XorBin)
  }

  private def putInsts(insts: Seq[Inst]) = putSeq(insts)(putInst)
  private def putInst(cf: Inst) = cf match {
    case Inst.None =>
      putInt(T.NoneInst)

    case Inst.Label(name, params) =>
      putInt(T.LabelInst)
      putLocal(name)
      putParams(params)

    case Inst.Let(name, op) =>
      putInt(T.LetInst)
      putLocal(name)
      putOp(op)

    case Inst.Unreachable =>
      putInt(T.UnreachableInst)

    case Inst.Ret(v) =>
      putInt(T.RetInst)
      putVal(v)

    case Inst.Jump(next) =>
      putInt(T.JumpInst)
      putNext(next)

    case Inst.If(v, thenp, elsep) =>
      putInt(T.IfInst)
      putVal(v)
      putNext(thenp)
      putNext(elsep)

    case Inst.Switch(v, default, cases) =>
      putInt(T.SwitchInst)
      putVal(v)
      putNext(default)
      putNexts(cases)

    case Inst.Invoke(ty, f, args, succ, fail) =>
      putInt(T.InvokeInst)
      putType(ty)
      putVal(f)
      putVals(args)
      putNext(succ)
      putNext(fail)

    case Inst.Throw(v) =>
      putInt(T.ThrowInst)
      putVal(v)

    case Inst.Try(norm, exc) =>
      putInt(T.TryInst)
      putNext(norm)
      putNext(exc)
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

  private def putDefn(value: Defn): Unit = value match {
    case Defn.Var(attrs, name, ty, value) =>
      putInt(T.VarDefn)
      putAttrs(attrs)
      putGlobal(name)
      putType(ty)
      putVal(value)

    case Defn.Const(attrs, name, ty, value) =>
      putInt(T.ConstDefn)
      putAttrs(attrs)
      putGlobal(name)
      putType(ty)
      putVal(value)

    case Defn.Declare(attrs, name, ty) =>
      putInt(T.DeclareDefn)
      putAttrs(attrs)
      putGlobal(name)
      putType(ty)

    case Defn.Define(attrs, name, ty, insts) =>
      putInt(T.DefineDefn)
      putAttrs(attrs)
      putGlobal(name)
      putType(ty)
      putInsts(insts)

    case Defn.Struct(attrs, name, members) =>
      putInt(T.StructDefn)
      putAttrs(attrs)
      putGlobal(name)
      putTypes(members)

    case Defn.Trait(attrs, name, ifaces) =>
      putInt(T.TraitDefn)
      putAttrs(attrs)
      putGlobal(name)
      putGlobals(ifaces)

    case Defn.Class(attrs, name, parent, ifaces) =>
      putInt(T.ClassDefn)
      putAttrs(attrs)
      putGlobal(name)
      putGlobalOpt(parent)
      putGlobals(ifaces)

    case Defn.Module(attrs, name, parent, ifaces) =>
      putInt(T.ModuleDefn)
      putAttrs(attrs)
      putGlobal(name)
      putGlobalOpt(parent)
      putGlobals(ifaces)
  }

  private def putGlobals(globals: Seq[Global]): Unit =
    putSeq(globals)(putGlobal)
  private def putGlobalOpt(globalopt: Option[Global]): Unit =
    putOpt(globalopt)(putGlobal)
  private def putGlobal(global: Global): Unit = global match {
    case Global.None    => putInt(T.NoneGlobal)
    case Global.Top(id) => putInt(T.TopGlobal); putString(id)
    case Global.Member(n, id) =>
      putInt(T.MemberGlobal); putGlobal(n); putString(id)
  }

  private def putLocal(local: Local): Unit = {
    putString(local.scope); putInt(local.id)
  }

  private def putNexts(nexts: Seq[Next]) = putSeq(nexts)(putNext)
  private def putNext(next: Next) = next match {
    case Next.Succ(n)      => putInt(T.SuccNext); putLocal(n)
    case Next.Fail(n)      => putInt(T.FailNext); putLocal(n)
    case Next.Label(n, vs) => putInt(T.LabelNext); putLocal(n); putVals(vs)
    case Next.Case(v, n)   => putInt(T.CaseNext); putVal(v); putLocal(n)
  }

  private def putOp(op: Op) = op match {
    case Op.Call(ty, v, args) =>
      putInt(T.CallOp)
      putType(ty)
      putVal(v)
      putVals(args)

    case Op.Load(ty, ptr) =>
      putInt(T.LoadOp)
      putType(ty)
      putVal(ptr)

    case Op.Store(ty, value, ptr) =>
      putInt(T.StoreOp)
      putType(ty)
      putVal(value)
      putVal(ptr)

    case Op.Elem(ty, v, indexes) =>
      putInt(T.ElemOp)
      putType(ty)
      putVal(v)
      putVals(indexes)

    case Op.Extract(v, indexes) =>
      putInt(T.ExtractOp)
      putVal(v)
      putInts(indexes)

    case Op.Insert(v, value, indexes) =>
      putInt(T.InsertOp)
      putVal(v)
      putVal(value)
      putInts(indexes)

    case Op.Stackalloc(ty, n) =>
      putInt(T.StackallocOp)
      putType(ty)
      putVal(n)

    case Op.Bin(bin, ty, l, r) =>
      putInt(T.BinOp)
      putBin(bin)
      putType(ty)
      putVal(l)
      putVal(r)

    case Op.Comp(comp, ty, l, r) =>
      putInt(T.CompOp)
      putComp(comp)
      putType(ty)
      putVal(l)
      putVal(r)

    case Op.Conv(conv, ty, v) =>
      putInt(T.ConvOp)
      putConv(conv)
      putType(ty)
      putVal(v)

    case Op.Select(cond, thenv, elsev) =>
      putInt(T.SelectOp)
      putVal(cond)
      putVal(thenv)
      putVal(elsev)

    case Op.Classalloc(n) =>
      putInt(T.ClassallocOp)
      putGlobal(n)

    case Op.Field(ty, v, name) =>
      putInt(T.FieldOp)
      putType(ty)
      putVal(v)
      putGlobal(name)

    case Op.Method(ty, v, name) =>
      putInt(T.MethodOp)
      putType(ty)
      putVal(v)
      putGlobal(name)

    case Op.Module(name) =>
      putInt(T.ModuleOp)
      putGlobal(name)

    case Op.As(ty, v) =>
      putInt(T.AsOp)
      putType(ty)
      putVal(v)

    case Op.Is(ty, v) =>
      putInt(T.IsOp)
      putType(ty)
      putVal(v)

    case Op.Copy(v) =>
      putInt(T.CopyOp)
      putVal(v)

    case Op.Sizeof(ty) =>
      putInt(T.SizeofOp)
      putType(ty)

    case Op.Closure(ty, fun, captures) =>
      putInt(T.ClosureOp)
      putType(ty)
      putVal(fun)
      putVals(captures)
  }

  private def putParams(params: Seq[Val.Local]) = putSeq(params)(putParam)
  private def putParam(param: Val.Local) = {
    putLocal(param.name)
    putType(param.ty)
  }

  private def putTypes(tys: Seq[Type]): Unit = putSeq(tys)(putType)
  private def putType(ty: Type): Unit = ty match {
    case Type.None         => putInt(T.NoneType)
    case Type.Void         => putInt(T.VoidType)
    case Type.Vararg       => putInt(T.VarargType)
    case Type.Ptr          => putInt(T.PtrType)
    case Type.Bool         => putInt(T.BoolType)
    case Type.I8           => putInt(T.I8Type)
    case Type.I16          => putInt(T.I16Type)
    case Type.I32          => putInt(T.I32Type)
    case Type.I64          => putInt(T.I64Type)
    case Type.F32          => putInt(T.F32Type)
    case Type.F64          => putInt(T.F64Type)
    case Type.Array(ty, n) => putInt(T.ArrayType); putType(ty); putInt(n)
    case Type.Function(args, ret) =>
      putInt(T.FunctionType); putArgs(args); putType(ret)
    case Type.Struct(n, tys) =>
      putInt(T.StructType); putGlobal(n); putTypes(tys)

    case Type.Unit      => putInt(T.UnitType)
    case Type.Nothing   => putInt(T.NothingType)
    case Type.Class(n)  => putInt(T.ClassType); putGlobal(n)
    case Type.Trait(n)  => putInt(T.TraitType); putGlobal(n)
    case Type.Module(n) => putInt(T.ModuleType); putGlobal(n)
  }

  private def putArgs(args: Seq[Arg]): Unit = putSeq(args)(putArg)
  private def putArg(arg: Arg): Unit = {
    putType(arg.ty)
    putOpt(arg.passConvention)(putPassConvention)
  }
  private def putPassConvention(attr: PassConv): Unit = attr match {
    case PassConv.Byval(ty) => putInt(T.Byval); putType(ty)
    case PassConv.Sret(ty)  => putInt(T.Sret); putType(ty)
  }

  private def putVals(values: Seq[Val]): Unit = putSeq(values)(putVal)
  private def putVal(value: Val): Unit = value match {
    case Val.None          => putInt(T.NoneVal)
    case Val.True          => putInt(T.TrueVal)
    case Val.False         => putInt(T.FalseVal)
    case Val.Zero(ty)      => putInt(T.ZeroVal); putType(ty)
    case Val.Undef(ty)     => putInt(T.UndefVal); putType(ty)
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

    case Val.Unit      => putInt(T.UnitVal)
    case Val.Const(v)  => putInt(T.ConstVal); putVal(v)
    case Val.String(v) => putInt(T.StringVal); putString(v)
  }
}
