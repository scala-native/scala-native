package scala.scalanative
package nir

import scala.scalanative.util.TypeOps.TypeNarrowing

trait Transform {
  def onDefns(assembly: Seq[Defn]): Seq[Defn] =
    assembly.map(onDefn)

  def onDefn(defn: Defn): Defn = {
    implicit val rootPos: SourcePosition = defn.pos
    defn match {
      case defn @ Defn.Var(_, _, ty, value) =>
        defn.copy(ty = onType(ty), rhs = onVal(value))
      case defn @ Defn.Const(_, _, ty, value) =>
        defn.copy(ty = onType(ty), rhs = onVal(value))
      case defn @ Defn.Declare(_, _, ty) =>
        defn.copy(ty = onType(ty).narrow[Type.Function])
      case defn @ Defn.Define(_, _, ty, insts, _) =>
        defn.copy(ty = onType(ty).narrow[Type.Function], insts = onInsts(insts))
      case defn @ Defn.Trait(_, _, _) =>
        defn
      case defn @ Defn.Class(_, _, _, _) =>
        defn
      case defn @ Defn.Module(_, _, _, _) =>
        defn
    }
  }

  def onInsts(insts: Seq[Inst]): Seq[Inst] =
    insts.map(onInst)

  def onInst(inst: Inst): Inst = {
    implicit val pos = inst.pos
    inst match {
      case Inst.Label(n, params) =>
        val newparams = params.map { param =>
          param.copy(valty = onType(param.ty))
        }
        Inst.Label(n, newparams)
      case inst @ Inst.Let(_, op, unwind) =>
        implicit val scopeId: ScopeId = inst.scopeId
        inst.copy(op = onOp(op), unwind = onNext(unwind))
      case Inst.Ret(v) =>
        Inst.Ret(onVal(v))
      case Inst.Jump(next) =>
        Inst.Jump(onNext(next))
      case Inst.If(v, thenp, elsep) =>
        Inst.If(onVal(v), onNext(thenp), onNext(elsep))
      case Inst.Switch(v, default, cases) =>
        Inst.Switch(onVal(v), onNext(default), cases.map(onNext))
      case Inst.Throw(v, unwind) =>
        Inst.Throw(onVal(v), onNext(unwind))
      case Inst.Unreachable(unwind) =>
        Inst.Unreachable(onNext(unwind))
      case _: Inst.LinktimeCf => util.unreachable
    }
  }

  def onOp(op: Op): Op = op match {
    case Op.Call(ty, ptrv, argvs) =>
      Op.Call(onType(ty).narrow[Type.Function], onVal(ptrv), argvs.map(onVal))
    case Op.Load(ty, ptrv, memoryOrder) =>
      Op.Load(onType(ty), onVal(ptrv), memoryOrder)
    case Op.Store(ty, ptrv, v, memoryOrder) =>
      Op.Store(onType(ty), onVal(ptrv), onVal(v), memoryOrder)
    case Op.Elem(ty, ptrv, indexvs) =>
      Op.Elem(onType(ty), onVal(ptrv), indexvs.map(onVal))
    case Op.Extract(aggrv, indexvs) =>
      Op.Extract(onVal(aggrv), indexvs)
    case Op.Insert(aggrv, v, indexvs) =>
      Op.Insert(onVal(aggrv), onVal(v), indexvs)
    case Op.Stackalloc(ty, v) =>
      Op.Stackalloc(onType(ty), onVal(v))
    case Op.Bin(bin, ty, lv, rv) =>
      Op.Bin(bin, onType(ty), onVal(lv), onVal(rv))
    case Op.Comp(comp, ty, lv, rv) =>
      Op.Comp(comp, onType(ty), onVal(lv), onVal(rv))
    case Op.Conv(conv, ty, v) =>
      Op.Conv(conv, onType(ty), onVal(v))
    case Op.Fence(_) => op

    case Op.Classalloc(n, zone) =>
      Op.Classalloc(n, zone.map(onVal))
    case Op.Fieldload(ty, v, n) =>
      Op.Fieldload(onType(ty), onVal(v), n)
    case Op.Fieldstore(ty, v1, n, v2) =>
      Op.Fieldstore(onType(ty), onVal(v1), n, onVal(v2))
    case Op.Field(v, n) =>
      Op.Field(onVal(v), n)
    case Op.Method(v, n) =>
      Op.Method(onVal(v), n)
    case Op.Dynmethod(obj, signature) =>
      Op.Dynmethod(onVal(obj), signature)
    case Op.Module(n) =>
      Op.Module(n)
    case Op.As(ty, v) =>
      Op.As(onType(ty).narrow[Type.RefKind], onVal(v))
    case Op.Is(ty, v) =>
      Op.Is(onType(ty).narrow[Type.RefKind], onVal(v))
    case Op.Copy(v) =>
      Op.Copy(onVal(v))
    case Op.SizeOf(ty)      => Op.SizeOf(onType(ty))
    case Op.AlignmentOf(ty) => Op.AlignmentOf(onType(ty))
    case Op.Box(code, obj)  =>
      Op.Box(code, onVal(obj))
    case Op.Unbox(code, obj) =>
      Op.Unbox(code, onVal(obj))
    case Op.Var(ty) =>
      Op.Var(onType(ty))
    case Op.Varload(elem) =>
      Op.Varload(onVal(elem))
    case Op.Varstore(elem, value) =>
      Op.Varstore(onVal(elem), onVal(value))
    case Op.Arrayalloc(ty, init, zone) =>
      Op.Arrayalloc(onType(ty), onVal(init), zone.map(onVal))
    case Op.Arrayload(ty, arr, idx) =>
      Op.Arrayload(onType(ty), onVal(arr), onVal(idx))
    case Op.Arraystore(ty, arr, idx, value) =>
      Op.Arraystore(onType(ty), onVal(arr), onVal(idx), onVal(value))
    case Op.Arraylength(arr) =>
      Op.Arraylength(onVal(arr))
  }

  def onVal(value: Val): Val = value match {
    case Val.Zero(ty)               => Val.Zero(onType(ty))
    case Val.StructValue(values)    => Val.StructValue(values.map(onVal))
    case Val.ArrayValue(ty, values) =>
      Val.ArrayValue(onType(ty), values.map(onVal))
    case v @ Val.Local(_, ty) => v.copy(valty = onType(ty))
    case Val.Global(n, ty)    => Val.Global(n, onType(ty))
    case Val.Const(v)         => Val.Const(onVal(v))
    case _                    => value
  }

  def onType(ty: Type): Type = ty match {
    case Type.ArrayValue(ty, n) =>
      Type.ArrayValue(onType(ty), n)
    case Type.Function(args, ty) =>
      Type.Function(args.map(onType), onType(ty))
    case Type.StructValue(tys) =>
      Type.StructValue(tys.map(onType))
    case Type.Var(ty) =>
      Type.Var(onType(ty))
    case Type.Array(ty, nullable) =>
      Type.Array(onType(ty), nullable)
    case _ =>
      ty
  }

  def onNext(next: Next): Next = next match {
    case Next.None            => Next.None
    case Next.Case(v, n)      => Next.Case(onVal(v), onNext(n))
    case Next.Unwind(n, next) => Next.Unwind(n, onNext(next))
    case Next.Label(n, args)  => Next.Label(n, args.map(onVal))
  }
}
