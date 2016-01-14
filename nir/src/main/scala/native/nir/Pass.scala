package native
package nir

import scala.collection.mutable

trait Pass {
  val fresh = new Fresh("tx")

  def onCompilationUnit(defns: Seq[Defn]): Seq[Defn] =
    onScope(defns)

  def onScope(defns: Seq[Defn]): Seq[Defn] =
    defns.flatMap(onDefn)

  def onDefn(defn: Defn): Seq[Defn] =
    Seq(defn match {
      case defn @ Defn.Var(_, _, ty, value) =>
        defn.copy(ty = onType(ty), value = onVal(value))
      case defn @ Defn.Declare(_, _, ty) =>
        defn.copy(ty = onType(ty))
      case defn @ Defn.Define(_, _, ty, blocks) =>
        defn.copy(ty = onType(ty), blocks = onBlocks(blocks))
      case defn @ Defn.Struct(_, _, fieldtys) =>
        defn.copy(fieldtys = fieldtys.map(onType))
      case defn @ Defn.Interface(_, _, _, members) =>
        defn.copy(members = onScope(members))
      case defn @ Defn.Class(_, _, _, _, members) =>
        defn.copy(members = onScope(members))
      case defn @ Defn.Module(_, _, _, _, members) =>
        defn.copy(members = onScope(members))
    })

  def onBlocks(blocks: Seq[Block]): Seq[Block] =
    blocks.flatMap(onBlock)

  def onBlock(block: Block): Seq[Block] =
    Seq(Block(onLocal(block.name), block.params.flatMap(onParam), block.instrs.flatMap(onInstr)))

  def onParam(param: Param): Seq[Param] =
    Seq(Param(param.name, onType(param.ty)))

  def onInstr(instr: Instr): Seq[Instr] =
    Seq(Instr(instr.name, instr.attrs, onOp(instr.op)))

  def onOp(op: Op): Op = op match {
    case Op.Undefined                           => Op.Undefined
    case Op.Ret(v)                              => Op.Ret(onVal(v))
    case Op.Throw(v)                            => Op.Throw(onVal(v))
    case Op.Jump(next)                          => Op.Jump(onNext(next))
    case Op.If(v, thenp, elsep)                 => Op.If(onVal(v), onNext(thenp), onNext(elsep))
    case Op.Switch(v, default, cases)           => Op.Switch(onVal(v), onNext(default), cases.map(onCast))
    case Op.Invoke(ty, ptrv, argvs, succ, fail) => Op.Invoke(onType(ty), onVal(ptrv), argvs.map(onVal), onNext(succ), onNext(fail))

    case Op.Call(ty, ptrv, argvs)        => Op.Call(onType(ty), onVal(ptrv), argvs.map(onVal))
    case Op.Load(ty, ptrv)               => Op.Load(onType(ty), onVal(ptrv))
    case Op.Store(ty, ptrv, v)           => Op.Store(onType(ty), onVal(ptrv), onVal(v))
    case Op.Elem(ty, ptrv, indexvs)      => Op.Elem(onType(ty), onVal(ptrv), indexvs.map(onVal))
    case Op.Extract(ty, aggrv, indexv)   => Op.Extract(onType(ty), onVal(aggrv), onVal(indexv))
    case Op.Insert(ty, aggrv, v, indexv) => Op.Insert(onType(ty), onVal(aggrv), onVal(v), onVal(indexv))
    case Op.Alloca(ty)                   => Op.Alloca(onType(ty))
    case Op.Size(ty)                     => Op.Size(onType(ty))
    case Op.Bin(bin, ty, lv, rv)         => Op.Bin(bin, onType(ty), onVal(lv), onVal(rv))
    case Op.Comp(comp, ty, lv, rv)       => Op.Comp(comp, onType(ty), onVal(lv), onVal(rv))
    case Op.Conv(conv, ty, v)            => Op.Conv(conv, onType(ty), onVal(v))

    case Op.ObjAlloc(ty)            => Op.ObjAlloc(onType(ty))
    case Op.ObjFieldElem(ty, n, v)  => Op.ObjFieldElem(onType(ty), n, onVal(v))
    case Op.ObjMethodElem(ty, n, v) => Op.ObjMethodElem(onType(ty), n, onVal(v))
    case Op.ObjEquals(l, r)         => Op.ObjEquals(onVal(l), onVal(r))
    case Op.ObjHashCode(v)          => Op.ObjHashCode(onVal(v))
    case Op.ObjToString(v)          => Op.ObjToString(onVal(v))
    case Op.ObjGetClass(v)          => Op.ObjGetClass(onVal(v))
    case Op.ObjAs(ty, v)            => Op.ObjAs(onType(ty), onVal(v))
    case Op.ObjIs(ty, v)            => Op.ObjIs(onType(ty), onVal(v))
    case Op.ArrAlloc(ty, v)         => Op.ArrAlloc(onType(ty), onVal(v))
    case Op.ArrLength(v)            => Op.ArrLength(onVal(v))
    case Op.ArrElem(ty, v, i)       => Op.ArrElem(onType(ty), onVal(v), onVal(i))
    case Op.PrimBox(ty, v)          => Op.PrimBox(onType(ty), onVal(v))
    case Op.PrimUnbox(ty, v)        => Op.PrimUnbox(onType(ty), onVal(v))
    case Op.PrimParse(ty, v, r)     => Op.PrimParse(onType(ty), onVal(v), onVal(r))
    case Op.PrimToString(ty, v, r)  => Op.PrimToString(onType(ty), onVal(v), onVal(r))
    case Op.MonitorEnter(v)         => Op.MonitorEnter(onVal(v))
    case Op.MonitorExit(v)          => Op.MonitorExit(onVal(v))
    case Op.MonitorNotify(v)        => Op.MonitorNotify(onVal(v))
    case Op.MonitorNotifyAll(v)     => Op.MonitorNotifyAll(onVal(v))
    case Op.MonitorWait(v, t, n)    => Op.MonitorWait(onVal(v), onVal(t), onVal(n))
    case Op.StringConcat(l, r)      => Op.StringConcat(onVal(l), onVal(r))
  }

  def onVal(value: Val): Val = value match {
    case Val.Zero(ty)          => Val.Zero(onType(ty))
    case Val.Struct(n, values) => Val.Struct(n, values.map(onVal))
    case Val.Array(ty, values) => Val.Array(onType(ty), values.map(onVal))
    case Val.Local(n, ty)      => Val.Local(onLocal(n), onType(ty))
    case Val.Global(n, ty)     => Val.Global(n, onType(ty))
    case Val.Class(ty)         => Val.Class(onType(ty))
    case _                     => value
  }

  def onType(ty: Type): Type = ty match {
    case Type.Array(ty, n)      => Type.Array(onType(ty), n)
    case Type.Ptr(ty)           => Type.Ptr(onType(ty))
    case Type.Function(tys, ty) => Type.Function(tys.map(onType), onType(ty))
    case Type.ArrayClass(ty)    => Type.ArrayClass(onType(ty))
    case _                      => ty
  }

  def onCast(c: Case): Case =
    Case(onVal(c.value), onNext(c.next))

  def onNext(next: Next): Next =
    Next(onLocal(next.name), next.args.map(onVal))

  def onLocal(local: Local): Local = local
}
