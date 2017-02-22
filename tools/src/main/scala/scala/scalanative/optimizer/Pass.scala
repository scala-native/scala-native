package scala.scalanative
package optimizer

import analysis.ClassHierarchy.Top
import tools.Config
import scala.collection.mutable
import nir._

sealed trait AnyPass

object NoPass extends AnyPass

trait Inject extends AnyPass {
  def apply(buffer: mutable.Buffer[Defn]): Unit
}

trait Pass extends AnyPass {
  def onDefns(assembly: Seq[Defn]): Seq[Defn] =
    assembly.map(onDefn)

  def onDefn(defn: Defn): Defn = defn match {
    case defn @ Defn.Var(_, _, ty, value) =>
      defn.copy(ty = onType(ty), rhs = onVal(value))
    case defn @ Defn.Const(_, _, ty, value) =>
      defn.copy(ty = onType(ty), rhs = onVal(value))
    case defn @ Defn.Declare(_, _, ty) =>
      defn.copy(ty = onType(ty))
    case defn @ Defn.Define(_, _, ty, insts) =>
      defn.copy(ty = onType(ty), insts = onInsts(insts))
    case defn @ Defn.Struct(_, _, tys) =>
      defn.copy(tys = tys.map(onType))
    case defn @ Defn.Trait(_, _, _) =>
      defn
    case defn @ Defn.Class(_, _, _, _) =>
      defn
    case defn @ Defn.Module(_, _, _, _) =>
      defn
  }

  def onInsts(insts: Seq[Inst]): Seq[Inst] =
    insts.map(onInst)

  def onInst(inst: Inst): Inst = inst match {
    case Inst.None =>
      inst
    case Inst.Label(n, params) =>
      val newparams = params.map { param =>
        Val.Local(param.name, onType(param.ty))
      }
      Inst.Label(n, newparams)
    case Inst.Let(n, op) =>
      Inst.Let(n, onOp(op))

    case Inst.Unreachable =>
      Inst.Unreachable
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
  }

  def onOp(op: Op): Op = op match {
    case Op.Call(ty, ptrv, argvs, unwind) =>
      Op.Call(onType(ty), onVal(ptrv), argvs.map(onVal), onNext(unwind))
    case Op.Load(ty, ptrv) =>
      Op.Load(onType(ty), onVal(ptrv))
    case Op.Store(ty, ptrv, v) =>
      Op.Store(onType(ty), onVal(ptrv), onVal(v))
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
    case Op.Select(v1, v2, v3) =>
      Op.Select(onVal(v1), onVal(v2), onVal(v3))

    case Op.Classalloc(n) =>
      Op.Classalloc(n)
    case Op.Field(v, n) =>
      Op.Field(onVal(v), n)
    case Op.Method(v, n) =>
      Op.Method(onVal(v), n)
    case Op.Dynmethod(obj, signature) =>
      Op.Dynmethod(onVal(obj), signature)
    case Op.Module(n, unwind) =>
      Op.Module(n, onNext(unwind))
    case Op.As(ty, v) =>
      Op.As(onType(ty), onVal(v))
    case Op.Is(ty, v) =>
      Op.Is(onType(ty), onVal(v))
    case Op.Copy(v) =>
      Op.Copy(onVal(v))
    case Op.Sizeof(ty) =>
      Op.Sizeof(onType(ty))
    case Op.Closure(ty, fun, captures) =>
      Op.Closure(onType(ty), onVal(fun), captures.map(onVal))
    case Op.Box(code, obj) =>
      Op.Box(code, onVal(obj))
    case Op.Unbox(code, obj) =>
      Op.Unbox(code, onVal(obj))
  }

  def onVal(value: Val): Val = value match {
    case Val.Zero(ty)          => Val.Zero(onType(ty))
    case Val.Undef(ty)         => Val.Undef(onType(ty))
    case Val.Struct(n, values) => Val.Struct(n, values.map(onVal))
    case Val.Array(ty, values) => Val.Array(onType(ty), values.map(onVal))
    case Val.Local(n, ty)      => Val.Local(n, onType(ty))
    case Val.Global(n, ty)     => Val.Global(n, onType(ty))
    case Val.Const(v)          => Val.Const(onVal(v))
    case _                     => value
  }

  def onType(ty: Type): Type = ty match {
    case Type.Array(ty, n) =>
      Type.Array(onType(ty), n)
    case Type.Function(args, ty) =>
      Type.Function(args.map(onType), onType(ty))
    case Type.Struct(n, tys) =>
      Type.Struct(n, tys.map(onType))
    case _ =>
      ty
  }

  def onNext(next: Next): Next = next match {
    case Next.None           => Next.None
    case unwind: Next.Unwind => unwind
    case Next.Label(n, args) => Next.Label(n, args.map(onVal))
    case Next.Case(v, n)     => Next.Case(onVal(v), n)
  }
}
