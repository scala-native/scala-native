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

  // Control-flow ops
  def label(name: Local): Unit =
    this += Inst.Label(name, Seq.empty)
  def label(name: Local, params: Seq[Val.Local]): Unit =
    this += Inst.Label(name, params)
  def unreachable: Unit =
    this += Inst.Unreachable
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
  def let(name: Local, op: Op): Val = {
    this += Inst.Let(name, op)
    Val.Local(name, op.resty)
  }
  def let(op: Op): Val =
    let(fresh(), op)
  def call(ty: Type, ptr: Val, args: Seq[Val], unwind: Next): Val =
    let(Op.Call(ty, ptr, args, unwind))
  def load(ty: Type, ptr: Val, isVolatile: Boolean = false): Val =
    let(Op.Load(ty, ptr, isVolatile))
  def store(ty: Type, ptr: Val, value: Val, isVolatile: Boolean = false): Val =
    let(Op.Store(ty, ptr, value, isVolatile))
  def elem(ty: Type, ptr: Val, indexes: Seq[Val]): Val =
    let(Op.Elem(ty, ptr, indexes))
  def extract(aggr: Val, indexes: Seq[Int]): Val =
    let(Op.Extract(aggr, indexes))
  def insert(aggr: Val, value: Val, indexes: Seq[Int]): Val =
    let(Op.Insert(aggr, value, indexes))
  def stackalloc(ty: Type, n: Val): Val =
    let(Op.Stackalloc(ty, n))
  def bin(bin: nir.Bin, ty: Type, l: Val, r: Val): Val =
    let(Op.Bin(bin, ty, l, r))
  def comp(comp: nir.Comp, ty: Type, l: Val, r: Val): Val =
    let(Op.Comp(comp, ty, l, r))
  def conv(conv: nir.Conv, ty: Type, value: Val): Val =
    let(Op.Conv(conv, ty, value))
  def select(cond: Val, thenv: Val, elsev: Val): Val =
    let(Op.Select(cond, thenv, elsev))
  def classalloc(name: Global): Val =
    let(Op.Classalloc(name))
  def field(obj: Val, name: Global): Val =
    let(Op.Field(obj, name))
  def method(obj: Val, name: Global): Val =
    let(Op.Method(obj, name))
  def dynmethod(obj: Val, signature: String): Val =
    let(Op.Dynmethod(obj, signature))
  def module(name: Global, unwind: Next): Val =
    let(Op.Module(name, unwind))
  def as(ty: Type, obj: Val): Val =
    let(Op.As(ty, obj))
  def is(ty: Type, obj: Val): Val =
    let(Op.Is(ty, obj))
  def copy(value: Val): Val =
    let(Op.Copy(value))
  def sizeof(ty: Type): Val =
    let(Op.Sizeof(ty))
  def closure(ty: Type, fun: Val, captures: Seq[Val]): Val =
    let(Op.Closure(ty, fun, captures))
  def box(ty: Type, obj: Val): Val =
    let(Op.Box(ty, obj))
  def unbox(ty: Type, obj: Val): Val =
    let(Op.Unbox(ty, obj))
}
