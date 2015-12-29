package native
package compiler

import scala.collection.mutable
import scala.reflect.ClassTag
import native.nir._

abstract class Pass {
  def onDefn(defn: Defn): Seq[Defn] = Seq(defn)
  def onType(ty: Type): Type = ty
  def onVal(value: Val) = value
  def onOp(op: Op, focus: Focus): Focus = focus withOp op

  final def apply(scope: Seq[Defn]) = runDefns(scope)

  private def runDefns(scope: Seq[Defn]): Seq[Defn] =
    scope.flatMap(runDefn(_))

  private def runDefn(defn: Defn): Seq[Defn] =
    onDefn(defn match {
      case Defn.Var(_, _, _) =>
        defn
      case Defn.Declare(_, _) =>
        defn
      case defn @ Defn.Define(_, _, blocks) =>
        defn.copy(blocks = runBlocks(blocks))
      case defn @ Defn.Struct(_, members) =>
        defn.copy(members = runDefns(members))
      case defn @ Defn.Interface(_, _, members) =>
        defn.copy(members = runDefns(members))
      case defn @ Defn.Class(_, _, _, members) =>
        defn.copy(members = runDefns(members))
      case defn @ Defn.Module(_, _, _, members) =>
        defn.copy(members = runDefns(members))
    })

  private def runBlocks(blocks: Seq[Block]): Seq[Block] =
    blocks.flatMap(runBlock)

  private def runBlock(block: Block): Seq[Block] = {
    var focus = Focus.entry(block.name, block.params)(new Fresh)
    val corr = mutable.Map.empty[Name, Val]
    val instrs :+ termn = block.instrs
    def nameSubs(value: Val): Val = value match {
      case Val.Name(name, _)             => corr(name)
      case value @ Val.Struct(_, values) => value.copy(values = values.map(nameSubs))
      case value @ Val.Array(_, values)  => value.copy(values = values.map(nameSubs))
      case _                             => value
    }
    instrs.foreach {
      case Instr(name, attrs, op) =>
        val newop = mapOpValues(op, nameSubs)
        val newfocus = onOp(newop, focus)
        corr += (name -> newfocus.value)
        focus = newfocus
    }
    focus.finish(mapOpValues(termn.op, nameSubs)).blocks
  }

  private def mapOpValues(op: Op, f: Val => Val): Op = op match {
    case Op.Undefined                       => Op.Undefined
    case Op.Ret(v)                          => Op.Ret(f(v))
    case Op.Throw(v)                        => Op.Throw(f(v))
    case Op.Jump(next)                      => Op.Jump(next)
    case Op.If(v, thenp, elsep)             => Op.If(f(v), thenp, elsep)
    case Op.Switch(v, default, cases)       => Op.Switch(f(v), default, cases)
    case Op.Invoke(ptrv, argvs, succ, fail) => Op.Invoke(f(ptrv), argvs.map(f), succ, fail)

    case Op.Call(ty, ptrv, argvs)        => Op.Call(ty, f(ptrv), argvs.map(f))
    case Op.Load(ty, ptrv)               => Op.Load(ty, f(ptrv))
    case Op.Store(ty, ptrv, v)           => Op.Store(ty, f(ptrv), f(v))
    case Op.Elem(ty, ptrv, indexvs)      => Op.Elem(ty, f(ptrv), indexvs.map(f))
    case Op.Extract(ty, aggrv, indexv)   => Op.Extract(ty, f(aggrv), f(indexv))
    case Op.Insert(ty, aggrv, v, indexv) => Op.Insert(ty, f(aggrv), f(v), f(indexv))
    case Op.Alloc(ty)                    => Op.Alloc(ty)
    case Op.Alloca(ty)                   => Op.Alloca(ty)
    case Op.Size(ty)                     => Op.Size(ty)
    case Op.Bin(bin, ty, lv, rv)         => Op.Bin(bin, ty, f(lv), f(rv))
    case Op.Comp(comp, ty, lv, rv)       => Op.Comp(comp, ty, f(lv), f(rv))
    case Op.Conv(conv, ty, v)            => Op.Conv(conv, ty, f(v))

    case Op.FieldElem(ty, n, v)       => Op.FieldElem(ty, n, f(v))
    case Op.MethodElem(ty, n, v)      => Op.MethodElem(ty, n, f(v))
    case Op.AllocClass(ty)            => Op.AllocClass(ty)
    case Op.AllocArray(ty, v)         => Op.AllocArray(ty, f(v))
    case Op.Equals(lv, rv)            => Op.Equals(f(lv), f(rv))
    case Op.HashCode(v)               => Op.HashCode(f(v))
    case Op.GetClass(v)               => Op.GetClass(f(v))
    case Op.AsInstanceOf(ty, v)       => Op.AsInstanceOf(ty, f(v))
    case Op.IsInstanceOf(ty, v)       => Op.IsInstanceOf(ty, f(v))
    case Op.ArrayLength(v)            => Op.ArrayLength(f(v))
    case Op.ArrayElem(ty, v, indexv)  => Op.ArrayElem(ty, f(v), f(indexv))
    case Op.Box(ty, v)                => Op.Box(ty, f(v))
    case Op.Unbox(ty, v)              => Op.Unbox(ty, f(v))
    case Op.MonitorEnter(v)           => Op.MonitorEnter(f(v))
    case Op.MonitorExit(v)            => Op.MonitorExit(f(v))
    case Op.StringConcat(lv, rv)      => Op.StringConcat(f(lv), f(rv))
    case Op.ToString(v, radixv)       => Op.ToString(f(v), f(radixv))
    case Op.FromString(ty, v, radixv) => Op.FromString(ty, f(v), f(radixv))
  }
}
