package scala.scalanative
package interflow

import scalanative.nir._
import scalanative.linker._

trait NoOpt { self: Interflow =>
  def noOpt(defn: Defn.Define): Unit =
    noOptInsts(defn.insts)

  def noOptInsts(insts: Seq[Inst]): Unit =
    insts.foreach(noOptInst)

  def noOptInst(inst: Inst): Unit = inst match {
    case _: Inst.Label =>
      ()
    case Inst.Let(n, op, unwind) =>
      noOptOp(op)
      noOptNext(unwind)
    case Inst.Ret(v) =>
      noOptVal(v)
    case Inst.Jump(next) =>
      noOptNext(next)
    case Inst.If(v, thenp, elsep) =>
      noOptVal(v)
      noOptNext(thenp)
      noOptNext(elsep)
    case Inst.Switch(v, default, cases) =>
      noOptVal(v)
      noOptNext(default)
      cases.foreach(noOptNext)
    case Inst.Throw(v, unwind) =>
      noOptVal(v)
      noOptNext(unwind)
    case Inst.Unreachable(unwind) =>
      noOptNext(unwind)
    case _: Inst.LinktimeCf =>
      util.unreachable
  }

  def noOptNext(next: Next): Unit = next match {
    case Next.Label(_, args) =>
      args.foreach(noOptVal)
    case _ =>
      ()
  }

  def noOptOp(op: Op): Unit = op match {
    case Op.Call(_, ptrv, argvs) =>
      noOptVal(ptrv)
      argvs.foreach(noOptVal)
    case Op.Load(_, ptrv, _) =>
      noOptVal(ptrv)
    case Op.Store(_, ptrv, v, _) =>
      noOptVal(ptrv)
      noOptVal(v)
    case Op.Elem(_, ptrv, indexvs) =>
      noOptVal(ptrv)
      indexvs.foreach(noOptVal)
    case Op.Extract(aggrv, indexvs) =>
      noOptVal(aggrv)
    case Op.Insert(aggrv, v, indexvs) =>
      noOptVal(aggrv)
      noOptVal(v)
    case Op.Stackalloc(_, v) =>
      noOptVal(v)
    case Op.Bin(bin, _, lv, rv) =>
      noOptVal(lv)
      noOptVal(rv)
    case Op.Comp(comp, _, lv, rv) =>
      noOptVal(lv)
      noOptVal(rv)
    case Op.Conv(conv, _, v) =>
      noOptVal(v)
    case Op.Fence(_) => ()

    case Op.Classalloc(n) =>
      noOptGlobal(n)
    case Op.Fieldload(_, v, n) =>
      noOptVal(v)
      noOptGlobal(n)
    case Op.Fieldstore(_, v1, n, v2) =>
      noOptVal(v1)
      noOptGlobal(n)
      noOptVal(v2)
    case Op.Field(obj, n) =>
      noOptVal(obj)
      noOptGlobal(n)
    case Op.Method(obj, sig) =>
      noOptVal(obj)
      obj.ty match {
        case refty: Type.RefKind =>
          val name = refty.className
          val scope = linked.infos(name).asInstanceOf[ScopeInfo]
          scope.targets(sig).foreach(visitEntry)
        case _ =>
          ()
      }
    case Op.Dynmethod(obj, dynsig) =>
      linked.dynimpls.foreach {
        case impl @ Global.Member(_, sig) if sig.toProxy == dynsig =>
          visitEntry(impl)
        case _ =>
          ()
      }
    case Op.Module(n) =>
      visitEntry(n)
    case Op.As(_, v) =>
      noOptVal(v)
    case Op.Is(_, v) =>
      noOptVal(v)
    case Op.Copy(v) =>
      noOptVal(v)
    case _: Op.SizeOf      => ()
    case _: Op.AlignmentOf => ()
    case Op.Box(code, obj) =>
      noOptVal(obj)
    case Op.Unbox(code, obj) =>
      noOptVal(obj)
    case _: Op.Var =>
      ()
    case Op.Varload(slot) =>
      noOptVal(slot)
    case Op.Varstore(slot, value) =>
      noOptVal(slot)
      noOptVal(value)
    case Op.Arrayalloc(_, init) =>
      noOptVal(init)
    case Op.Arrayload(_, arr, idx) =>
      noOptVal(arr)
      noOptVal(idx)
    case Op.Arraystore(_, arr, idx, value) =>
      noOptVal(arr)
      noOptVal(idx)
      noOptVal(value)
    case Op.Arraylength(arr) =>
      noOptVal(arr)
  }

  def noOptVal(value: Val): Unit = value match {
    case _: Val.Zero =>
      ()
    case Val.StructValue(values) =>
      values.foreach(noOptVal)
    case Val.ArrayValue(ty, values) =>
      values.foreach(noOptVal)
    case _: Val.Local =>
      ()
    case Val.Global(n, _) =>
      noOptGlobal(n)
    case Val.Const(v) =>
      noOptVal(v)
    case _ =>
      ()
  }

  def noOptGlobal(name: Global): Unit =
    visitEntry(name)
}
