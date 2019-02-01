package scala.scalanative
package nir

import scala.collection.mutable

class Buffer(implicit fresh: Fresh) {
  private val buffer = mutable.UnrolledBuffer.empty[Inst]
  def +=(inst: Inst): Unit =
    buffer += inst
  def ++=(insts: Seq[Inst]): Unit =
    buffer ++= insts
  def ++=(other: Buffer): Unit =
    buffer ++= other.buffer
  def toSeq: Seq[Inst] =
    buffer
  def size: Int =
    buffer.size

  // Control-flow ops
  def label(name: Local): Unit =
    this += Inst.Label(name, Seq.empty)
  def label(name: Local, params: Seq[Val.Local]): Unit =
    this += Inst.Label(name, params)
  def unreachable(unwind: Next): Unit =
    this += Inst.Unreachable(unwind)
  def ret(value: Val): Unit =
    this += Inst.Ret(value)
  def jump(next: Next): Unit =
    this += Inst.Jump(next)
  def jump(to: Local, args: Seq[Val]): Unit =
    this += Inst.Jump(Next.Label(to, args))
  def branch(value: Val, thenp: Next, elsep: Next): Unit =
    this += Inst.If(value, thenp, elsep)
  def switch(value: Val, default: Next, cases: Seq[Next]): Unit =
    this += Inst.Switch(value, default, cases)
  def raise(value: Val, unwind: Next): Unit =
    this += Inst.Throw(value, unwind)

  // Compute ops
  def let(name: Local, op: Op, unwind: Next): Val = {
    this += Inst.Let(name, op, unwind)
    Val.Local(name, op.resty)
  }
  def let(op: Op, unwind: Next): Val =
    let(fresh(), op, unwind)
  def call(ty: Type, ptr: Val, args: Seq[Val], unwind: Next): Val =
    let(Op.Call(ty, ptr, args), unwind)
  def load(ty: Type, ptr: Val, unwind: Next): Val =
    let(Op.Load(ty, ptr), unwind)
  def store(ty: Type, ptr: Val, value: Val, unwind: Next): Val =
    let(Op.Store(ty, ptr, value), unwind)
  def elem(ty: Type, ptr: Val, indexes: Seq[Val], unwind: Next): Val =
    let(Op.Elem(ty, ptr, indexes), unwind)
  def extract(aggr: Val, indexes: Seq[Int], unwind: Next): Val =
    let(Op.Extract(aggr, indexes), unwind)
  def insert(aggr: Val, value: Val, indexes: Seq[Int], unwind: Next): Val =
    let(Op.Insert(aggr, value, indexes), unwind)
  def stackalloc(ty: Type, n: Val, unwind: Next): Val =
    let(Op.Stackalloc(ty, n), unwind)
  def bin(bin: nir.Bin, ty: Type, l: Val, r: Val, unwind: Next): Val =
    let(Op.Bin(bin, ty, l, r), unwind)
  def comp(comp: nir.Comp, ty: Type, l: Val, r: Val, unwind: Next): Val =
    let(Op.Comp(comp, ty, l, r), unwind)
  def conv(conv: nir.Conv, ty: Type, value: Val, unwind: Next): Val =
    let(Op.Conv(conv, ty, value), unwind)
  def classalloc(name: Global, unwind: Next): Val =
    let(Op.Classalloc(name), unwind)
  def fieldload(ty: Type, obj: Val, name: Global, unwind: Next): Val =
    let(Op.Fieldload(ty, obj, name), unwind)
  def fieldstore(ty: Type,
                 obj: Val,
                 name: Global,
                 value: Val,
                 unwind: Next): Val =
    let(Op.Fieldstore(ty, obj, name, value), unwind)
  def method(obj: Val, sig: Sig, unwind: Next): Val =
    let(Op.Method(obj, sig), unwind)
  def dynmethod(obj: Val, sig: Sig, unwind: Next): Val =
    let(Op.Dynmethod(obj, sig), unwind)
  def module(name: Global, unwind: Next): Val =
    let(Op.Module(name), unwind)
  def as(ty: Type, obj: Val, unwind: Next): Val =
    let(Op.As(ty, obj), unwind)
  def is(ty: Type, obj: Val, unwind: Next): Val =
    let(Op.Is(ty, obj), unwind)
  def copy(value: Val, unwind: Next): Val =
    let(Op.Copy(value), unwind)
  def sizeof(ty: Type, unwind: Next): Val =
    let(Op.Sizeof(ty), unwind)
  def box(ty: Type, obj: Val, unwind: Next): Val =
    let(Op.Box(ty, obj), unwind)
  def unbox(ty: Type, obj: Val, unwind: Next): Val =
    let(Op.Unbox(ty, obj), unwind)
  def var_(ty: Type, unwind: Next): Val =
    let(Op.Var(ty), unwind)
  def varload(slot: Val, unwind: Next): Val =
    let(Op.Varload(slot), unwind)
  def varstore(slot: Val, value: Val, unwind: Next): Val =
    let(Op.Varstore(slot, value), unwind)
  def arrayalloc(ty: Type, init: Val, unwind: Next): Val =
    let(Op.Arrayalloc(ty, init), unwind)
  def arrayload(ty: Type, arr: Val, idx: Val, unwind: Next): Val =
    let(Op.Arrayload(ty, arr, idx), unwind)
  def arraystore(ty: Type, arr: Val, idx: Val, value: Val, unwind: Next): Val =
    let(Op.Arraystore(ty, arr, idx, value), unwind)
  def arraylength(arr: Val, unwind: Next): Val =
    let(Op.Arraylength(arr), unwind)
}
