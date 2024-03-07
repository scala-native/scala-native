package scala.scalanative
package interflow

import scalanative.linker._

private[interflow] trait NoOpt { self: Interflow =>

  def noOpt(defn: nir.Defn.Define): Unit =
    noOptInsts(defn.insts)

  def noOptInsts(insts: Seq[nir.Inst]): Unit =
    insts.foreach(noOptInst)

  def noOptInst(inst: nir.Inst): Unit = inst match {
    case _: nir.Inst.Label =>
      ()
    case nir.Inst.Let(n, op, unwind) =>
      noOptOp(op)
      noOptNext(unwind)
    case nir.Inst.Ret(v) =>
      noOptVal(v)
    case nir.Inst.Jump(next) =>
      noOptNext(next)
    case nir.Inst.If(v, thenp, elsep) =>
      noOptVal(v)
      noOptNext(thenp)
      noOptNext(elsep)
    case nir.Inst.Switch(v, default, cases) =>
      noOptVal(v)
      noOptNext(default)
      cases.foreach(noOptNext)
    case nir.Inst.Throw(v, unwind) =>
      noOptVal(v)
      noOptNext(unwind)
    case nir.Inst.Unreachable(unwind) =>
      noOptNext(unwind)
    case _: nir.Inst.LinktimeCf =>
      util.unreachable
  }

  def noOptNext(next: nir.Next): Unit = next match {
    case nir.Next.Label(_, args) =>
      args.foreach(noOptVal)
    case _ =>
      ()
  }

  def noOptOp(op: nir.Op): Unit = op match {
    case nir.Op.Call(_, ptrv, argvs) =>
      noOptVal(ptrv)
      argvs.foreach(noOptVal)
    case nir.Op.Load(_, ptrv, _) =>
      noOptVal(ptrv)
    case nir.Op.Store(_, ptrv, v, _) =>
      noOptVal(ptrv)
      noOptVal(v)
    case nir.Op.Elem(_, ptrv, indexvs) =>
      noOptVal(ptrv)
      indexvs.foreach(noOptVal)
    case nir.Op.Extract(aggrv, indexvs) =>
      noOptVal(aggrv)
    case nir.Op.Insert(aggrv, v, indexvs) =>
      noOptVal(aggrv)
      noOptVal(v)
    case nir.Op.Stackalloc(_, v) =>
      noOptVal(v)
    case nir.Op.Bin(bin, _, lv, rv) =>
      noOptVal(lv)
      noOptVal(rv)
    case nir.Op.Comp(comp, _, lv, rv) =>
      noOptVal(lv)
      noOptVal(rv)
    case nir.Op.Conv(conv, _, v) =>
      noOptVal(v)
    case nir.Op.Fence(_) => ()

    case nir.Op.Classalloc(n, zone) =>
      noOptGlobal(n)
      zone.foreach(noOptVal)
    case nir.Op.Fieldload(_, v, n) =>
      noOptVal(v)
      noOptGlobal(n)
    case nir.Op.Fieldstore(_, v1, n, v2) =>
      noOptVal(v1)
      noOptGlobal(n)
      noOptVal(v2)
    case nir.Op.Field(obj, n) =>
      noOptVal(obj)
      noOptGlobal(n)
    case nir.Op.Method(obj, sig) =>
      noOptVal(obj)
      obj.ty match {
        case refty: nir.Type.RefKind =>
          val name = refty.className
          val scope = analysis.infos(name).asInstanceOf[ScopeInfo]
          scope.targets(sig).foreach(visitEntry)
        case _ =>
          ()
      }
    case nir.Op.Dynmethod(obj, dynsig) =>
      analysis.dynimpls.foreach {
        case impl @ nir.Global.Member(_, sig) if sig.toProxy == dynsig =>
          visitEntry(impl)
        case _ =>
          ()
      }
    case nir.Op.Module(n) =>
      visitEntry(n)
    case nir.Op.As(_, v) =>
      noOptVal(v)
    case nir.Op.Is(_, v) =>
      noOptVal(v)
    case nir.Op.Copy(v) =>
      noOptVal(v)
    case _: nir.Op.SizeOf      => ()
    case _: nir.Op.AlignmentOf => ()
    case nir.Op.Box(code, obj) =>
      noOptVal(obj)
    case nir.Op.Unbox(code, obj) =>
      noOptVal(obj)
    case _: nir.Op.Var =>
      ()
    case nir.Op.Varload(slot) =>
      noOptVal(slot)
    case nir.Op.Varstore(slot, value) =>
      noOptVal(slot)
      noOptVal(value)
    case nir.Op.Arrayalloc(_, init, zone) =>
      noOptVal(init)
      zone.foreach(noOptVal)
    case nir.Op.Arrayload(_, arr, idx) =>
      noOptVal(arr)
      noOptVal(idx)
    case nir.Op.Arraystore(_, arr, idx, value) =>
      noOptVal(arr)
      noOptVal(idx)
      noOptVal(value)
    case nir.Op.Arraylength(arr) =>
      noOptVal(arr)
  }

  def noOptVal(value: nir.Val): Unit = value match {
    case _: nir.Val.Zero =>
      ()
    case nir.Val.StructValue(values) =>
      values.foreach(noOptVal)
    case nir.Val.ArrayValue(ty, values) =>
      values.foreach(noOptVal)
    case _: nir.Val.Local =>
      ()
    case nir.Val.Global(n, _) =>
      noOptGlobal(n)
    case nir.Val.Const(v) =>
      noOptVal(v)
    case _ =>
      ()
  }

  def noOptGlobal(name: nir.Global): Unit =
    visitEntry(name)

}
