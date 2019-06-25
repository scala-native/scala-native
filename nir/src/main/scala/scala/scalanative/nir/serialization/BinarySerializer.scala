package scala.scalanative
package nir
package serialization

import java.nio.ByteBuffer
import scala.collection.mutable
import nir.serialization.{Tags => T}

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
      positions += buffer.position()
      putInt(0)
    }

    val offsets = defns.map { defn =>
      val pos: Int = buffer.position()
      putDefn(defn)
      pos
    }
    val end = buffer.position()

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
    val bytes = v.getBytes("UTF-8")
    putInt(bytes.length); put(bytes)
  }

  private def putBool(v: Boolean) = put((if (v) 1 else 0).toByte)

  private def putAttrs(attrs: Attrs) = putSeq(attrs.toSeq)(putAttr)
  private def putAttr(attr: Attr) = attr match {
    case Attr.MayInline    => putInt(T.MayInlineAttr)
    case Attr.InlineHint   => putInt(T.InlineHintAttr)
    case Attr.NoInline     => putInt(T.NoInlineAttr)
    case Attr.AlwaysInline => putInt(T.AlwaysInlineAttr)

    case Attr.MaySpecialize => putInt(T.MaySpecialize)
    case Attr.NoSpecialize  => putInt(T.NoSpecialize)

    case Attr.UnOpt        => putInt(T.UnOptAttr)
    case Attr.NoOpt        => putInt(T.NoOptAttr)
    case Attr.DidOpt       => putInt(T.DidOptAttr)
    case Attr.BailOpt(msg) => putInt(T.BailOptAttr); putString(msg)

    case Attr.Dyn      => putInt(T.DynAttr)
    case Attr.Stub     => putInt(T.StubAttr)
    case Attr.Extern   => putInt(T.ExternAttr)
    case Attr.Link(s)  => putInt(T.LinkAttr); putString(s)
    case Attr.Abstract => putInt(T.AbstractAttr)
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
    case Inst.Label(name, params) =>
      putInt(T.LabelInst)
      putLocal(name)
      putParams(params)

    case Inst.Let(name, op, Next.None) =>
      putInt(T.LetInst)
      putLocal(name)
      putOp(op)

    case Inst.Let(name, op, unwind) =>
      putInt(T.LetUnwindInst)
      putLocal(name)
      putOp(op)
      putNext(unwind)

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

    case Inst.Throw(v, unwind) =>
      putInt(T.ThrowInst)
      putVal(v)
      putNext(unwind)

    case Inst.Unreachable(unwind) =>
      putInt(T.UnreachableInst)
      putNext(unwind)
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
    case Global.None =>
      putInt(T.NoneGlobal)
    case Global.Top(id) =>
      putInt(T.TopGlobal)
      putString(id)
    case Global.Member(Global.Top(owner), sig) =>
      putInt(T.MemberGlobal)
      putString(owner)
      putSig(sig)
    case _ =>
      util.unreachable
  }

  private def putSig(sig: Sig): Unit =
    putString(sig.mangle)

  private def putLocal(local: Local): Unit =
    putLong(local.id)

  private def putNexts(nexts: Seq[Next]) = putSeq(nexts)(putNext)
  private def putNext(next: Next): Unit = next match {
    case Next.None         => putInt(T.NoneNext)
    case Next.Unwind(e, n) => putInt(T.UnwindNext); putParam(e); putNext(n)
    case Next.Case(v, n)   => putInt(T.CaseNext); putVal(v); putNext(n)
    case Next.Label(n, vs) => putInt(T.LabelNext); putLocal(n); putVals(vs)
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

    case Op.Classalloc(n) =>
      putInt(T.ClassallocOp)
      putGlobal(n)

    case Op.Fieldload(ty, obj, name) =>
      putInt(T.FieldloadOp)
      putType(ty)
      putVal(obj)
      putGlobal(name)

    case Op.Fieldstore(ty, obj, name, value) =>
      putInt(T.FieldstoreOp)
      putType(ty)
      putVal(obj)
      putGlobal(name)
      putVal(value)

    case Op.Method(v, sig) =>
      putInt(T.MethodOp)
      putVal(v)
      putSig(sig)

    case Op.Dynmethod(obj, sig) =>
      putInt(T.DynmethodOp)
      putVal(obj)
      putSig(sig)

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

    case Op.Box(ty, obj) =>
      putInt(T.BoxOp)
      putType(ty)
      putVal(obj)

    case Op.Unbox(ty, obj) =>
      putInt(T.UnboxOp)
      putType(ty)
      putVal(obj)

    case Op.Var(ty) =>
      putInt(T.VarOp)
      putType(ty)

    case Op.Varload(slot) =>
      putInt(T.VarloadOp)
      putVal(slot)

    case Op.Varstore(slot, value) =>
      putInt(T.VarstoreOp)
      putVal(slot)
      putVal(value)

    case Op.Arrayalloc(ty, init) =>
      putInt(T.ArrayallocOp)
      putType(ty)
      putVal(init)

    case Op.Arrayload(ty, arr, idx) =>
      putInt(T.ArrayloadOp)
      putType(ty)
      putVal(arr)
      putVal(idx)

    case Op.Arraystore(ty, arr, idx, value) =>
      putInt(T.ArraystoreOp)
      putType(ty)
      putVal(arr)
      putVal(idx)
      putVal(value)

    case Op.Arraylength(arr) =>
      putInt(T.ArraylengthOp)
      putVal(arr)
  }

  private def putParams(params: Seq[Val.Local]) = putSeq(params)(putParam)
  private def putParam(param: Val.Local) = {
    putLocal(param.name)
    putType(param.ty)
  }

  private def putTypes(tys: Seq[Type]): Unit = putSeq(tys)(putType)
  private def putType(ty: Type): Unit = ty match {
    case Type.Vararg => putInt(T.VarargType)
    case Type.Ptr    => putInt(T.PtrType)
    case Type.Bool   => putInt(T.BoolType)
    case Type.Char   => putInt(T.CharType)
    case Type.Byte   => putInt(T.ByteType)
    case Type.Short  => putInt(T.ShortType)
    case Type.Int    => putInt(T.IntType)
    case Type.Long   => putInt(T.LongType)
    case Type.Float  => putInt(T.FloatType)
    case Type.Double => putInt(T.DoubleType)
    case Type.ArrayValue(ty, n) =>
      putInt(T.ArrayValueType); putType(ty); putInt(n)
    case Type.StructValue(tys) =>
      putInt(T.StructValueType); putTypes(tys)
    case Type.Function(args, ret) =>
      putInt(T.FunctionType); putTypes(args); putType(ret)

    case Type.Null    => putInt(T.NullType)
    case Type.Nothing => putInt(T.NothingType)
    case Type.Virtual => putInt(T.VirtualType)
    case Type.Var(ty) => putInt(T.VarType); putType(ty)
    case Type.Unit    => putInt(T.UnitType)
    case Type.Array(ty, nullable) =>
      putInt(T.ArrayType)
      putType(ty)
      putBool(nullable)
    case Type.Ref(n, exact, nullable) =>
      putInt(T.RefType)
      putGlobal(n)
      putBool(exact)
      putBool(nullable)
  }

  private def putVals(values: Seq[Val]): Unit = putSeq(values)(putVal)
  private def putVal(value: Val): Unit = value match {
    case Val.True            => putInt(T.TrueVal)
    case Val.False           => putInt(T.FalseVal)
    case Val.Null            => putInt(T.NullVal)
    case Val.Zero(ty)        => putInt(T.ZeroVal); putType(ty)
    case Val.Char(v)         => putInt(T.CharVal); putShort(v.toShort)
    case Val.Byte(v)         => putInt(T.ByteVal); put(v)
    case Val.Short(v)        => putInt(T.ShortVal); putShort(v)
    case Val.Int(v)          => putInt(T.IntVal); putInt(v)
    case Val.Long(v)         => putInt(T.LongVal); putLong(v)
    case Val.Float(v)        => putInt(T.FloatVal); putFloat(v)
    case Val.Double(v)       => putInt(T.DoubleVal); putDouble(v)
    case Val.StructValue(vs) => putInt(T.StructValueVal); putVals(vs)
    case Val.ArrayValue(ty, vs) =>
      putInt(T.ArrayValueVal); putType(ty); putVals(vs)
    case Val.Chars(s)      => putInt(T.CharsVal); putString(s)
    case Val.Local(n, ty)  => putInt(T.LocalVal); putLocal(n); putType(ty)
    case Val.Global(n, ty) => putInt(T.GlobalVal); putGlobal(n); putType(ty)

    case Val.Unit       => putInt(T.UnitVal)
    case Val.Const(v)   => putInt(T.ConstVal); putVal(v)
    case Val.String(v)  => putInt(T.StringVal); putString(v)
    case Val.Virtual(v) => putInt(T.VirtualVal); putLong(v)
  }
}
