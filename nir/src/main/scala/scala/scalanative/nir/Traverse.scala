package scala.scalanative.nir

trait Traverse {
  def onDefns(defns: Iterable[Defn]): Unit = defns.foreach(onDefn)

  def onDefn(defn: Defn): Unit = {
    defn match {
      case Defn.Var(_, _, ty, value) =>
        onType(ty)
        onVal(value)
      case Defn.Const(_, _, ty, value) =>
        onType(ty)
        onVal(value)
      case Defn.Declare(_, _, ty) =>
        onType(ty)
      case Defn.Define(_, _, ty, insts) =>
        onInsts(insts)
      case Defn.Trait(_, _, _)     => ()
      case Defn.Class(_, _, _, _)  => ()
      case Defn.Module(_, _, _, _) => ()
    }
  }

  def onInsts(insts: Iterable[Inst]): Unit =
    insts.foreach(onInst)

  def onInst(inst: Inst): Unit = {
    inst match {
      case Inst.Label(n, params) =>
        params.foreach { param =>
          onType(param.ty)
        }
      case Inst.Let(n, op, unwind) =>
        onOp(op)
        onNext(unwind)
      case Inst.Ret(v)     => onVal(v)
      case Inst.Jump(next) => onNext(next)
      case Inst.If(v, thenp, elsep) =>
        onVal(v)
        onNext(thenp)
        onNext(elsep)
      case Inst.Switch(v, default, cases) =>
        onVal(v)
        onNext(default)
        cases.foreach(onNext)
      case Inst.Throw(v, unwind) =>
        onVal(v)
        onNext(unwind)
      case Inst.Unreachable(unwind) =>
        onNext(unwind)
      case _: Inst.LinktimeCf =>
        ()
    }
  }

  def onOp(op: Op): Unit = op match {
    case Op.Call(ty, ptrv, argvs) =>
      onType(ty)
      onVal(ptrv)
      argvs.foreach(onVal)
    case Op.Load(ty, ptrv, sync) =>
      onType(ty)
      onVal(ptrv)
    case Op.Store(ty, ptrv, v, sync) =>
      onType(ty)
      onVal(ptrv)
      onVal(v)
    case Op.Elem(ty, ptrv, indexvs) =>
      onType(ty)
      onVal(ptrv)
      indexvs.foreach(onVal)
    case Op.Extract(aggrv, indexvs) =>
      onVal(aggrv)
    case Op.Insert(aggrv, v, indexvs) =>
      onVal(aggrv)
      onVal(v)
    case Op.Stackalloc(ty, v) =>
      onType(ty)
      onVal(v)
    case Op.Bin(bin, ty, lv, rv) =>
      onType(ty)
      onVal(lv)
      onVal(rv)
    case Op.Comp(comp, ty, lv, rv) =>
      onType(ty)
      onVal(lv)
      onVal(rv)
    case Op.Conv(conv, ty, v) =>
      onType(ty)
      onVal(v)

    case Op.Classalloc(n) => ()
    case Op.Fieldload(ty, v, n) =>
      onType(ty)
      onVal(v)
    case Op.Fieldstore(ty, v1, n, v2) =>
      onType(ty)
      onVal(v1)
      onVal(v2)
    case Op.Field(v, n) =>
      onVal(v)
    case Op.Method(v, n) =>
      onVal(v)
    case Op.Dynmethod(obj, signature) =>
      onVal(obj)
    case Op.Module(n) => ()
    case Op.As(ty, v) =>
      onType(ty)
      onVal(v)
    case Op.Is(ty, v) =>
      onType(ty)
      onVal(v)
    case Op.Copy(v) =>
      onVal(v)
    case Op.SizeOf(ty)      => onType(ty)
    case Op.AlignmentOf(ty) => onType(ty)
    case Op.Box(code, obj) =>
      onVal(obj)
    case Op.Unbox(code, obj) =>
      onVal(obj)
    case Op.Var(ty) =>
      onType(ty)
    case Op.Varload(elem) =>
      onVal(elem)
    case Op.Varstore(elem, value) =>
      onVal(elem)
      onVal(value)
    case Op.Arrayalloc(ty, init) =>
      onType(ty)
      onVal(init)
    case Op.Arrayload(ty, arr, idx) =>
      onType(ty)
      onVal(arr)
      onVal(idx)
    case Op.Arraystore(ty, arr, idx, value) =>
      onType(ty)
      onVal(arr)
      onVal(idx)
      onVal(value)
    case Op.Arraylength(arr) =>
      onVal(arr)
    case Op.Fence(_) =>
      ()
  }

  def onVal(value: Val): Unit = value match {
    case Val.Zero(ty)            => onType(ty)
    case Val.StructValue(values) => values.foreach(onVal)
    case Val.ArrayValue(ty, values) =>
      onType(ty)
      values.foreach(onVal)
    case Val.Local(n, ty)  => onType(ty)
    case Val.Global(n, ty) => onType(ty)
    case Val.Const(v)      => onVal(v)
    case _                 => ()
  }

  def onType(ty: Type): Unit = ty match {
    case Type.ArrayValue(ty, n) =>
      onType(ty)
    case Type.Function(args, ty) =>
      args.foreach(onType)
      onType(ty)
    case Type.StructValue(tys) =>
      tys.foreach(onType)
    case Type.Var(ty) =>
      onType(ty)
    case Type.Array(ty, nullable) =>
      onType(ty)
    case _ =>
      ()
  }

  def onNext(next: Next): Unit = next match {
    case Next.None => ()
    case Next.Case(v, n) =>
      onVal(v)
      onNext(n)
    case Next.Unwind(n, next) => onNext(next)
    case Next.Label(n, args)  => args.foreach(onVal)
  }
}
