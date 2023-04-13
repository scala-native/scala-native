package scala.scalanative
package nir

import scala.collection.mutable

class Buffer(implicit fresh: Fresh) {
  private val buffer = mutable.UnrolledBuffer.empty[Inst]
  def +=(inst: Inst): Unit = buffer += inst
  def ++=(insts: Seq[Inst]): Unit = buffer ++= insts
  def ++=(other: Buffer): Unit = buffer ++= other.buffer

  def toSeq: Seq[Inst] = buffer.toSeq
  def size: Int = buffer.size
  def exists(pred: Inst => Boolean) = buffer.exists(pred)

  // Control-flow ops
  def label(name: Local)(implicit pos: Position): Unit =
    this += Inst.Label(name, Seq.empty)
  def label(name: Local, params: Seq[Val.Local])(implicit pos: Position): Unit =
    this += Inst.Label(name, params)
  def unreachable(unwind: Next)(implicit pos: Position): Unit =
    this += Inst.Unreachable(unwind)
  def ret(value: Val)(implicit pos: Position): Unit =
    this += Inst.Ret(value)
  def jump(next: Next)(implicit pos: Position): Unit =
    this += Inst.Jump(next)
  def jump(to: Local, args: Seq[Val])(implicit pos: Position): Unit =
    this += Inst.Jump(Next.Label(to, args))
  def branch(value: Val, thenp: Next, elsep: Next)(implicit
      pos: Position
  ): Unit =
    this += Inst.If(value, thenp, elsep)
  def branchLinktime(condition: LinktimeCondition, thenp: Next, elsep: Next)(
      implicit pos: Position
  ): Unit =
    this += Inst.LinktimeIf(condition, thenp, elsep)
  def switch(value: Val, default: Next, cases: Seq[Next])(implicit
      pos: Position
  ): Unit =
    this += Inst.Switch(value, default, cases)
  def raise(value: Val, unwind: Next)(implicit pos: Position): Unit =
    this += Inst.Throw(value, unwind)

  // Compute ops
  def let(name: Local, op: Op, unwind: Next)(implicit pos: Position): Val = {
    this += Inst.Let(name, op, unwind)
    Val.Local(name, op.resty)
  }
  def let(op: Op, unwind: Next)(implicit pos: Position): Val =
    let(fresh(), op, unwind)
  def call(ty: Type, ptr: Val, args: Seq[Val], unwind: Next)(implicit
      pos: Position
  ): Val =
    let(Op.Call(ty, ptr, args), unwind)
  def load(
      ty: Type,
      ptr: Val,
      unwind: Next,
      syncAttrs: Option[SyncAttrs] = None
  )(implicit
      pos: Position
  ): Val =
    let(Op.Load(ty, ptr, syncAttrs), unwind)
  def store(
      ty: Type,
      ptr: Val,
      value: Val,
      unwind: Next,
      syncAttrs: Option[SyncAttrs] = None
  )(implicit
      pos: Position
  ): Val =
    let(Op.Store(ty, ptr, value, syncAttrs), unwind)
  def elem(ty: Type, ptr: Val, indexes: Seq[Val], unwind: Next)(implicit
      pos: Position
  ): Val =
    let(Op.Elem(ty, ptr, indexes), unwind)
  def extract(aggr: Val, indexes: Seq[Int], unwind: Next)(implicit
      pos: Position
  ): Val =
    let(Op.Extract(aggr, indexes), unwind)
  def insert(aggr: Val, value: Val, indexes: Seq[Int], unwind: Next)(implicit
      pos: Position
  ): Val =
    let(Op.Insert(aggr, value, indexes), unwind)
  def stackalloc(ty: Type, n: Val, unwind: Next)(implicit pos: Position): Val =
    let(Op.Stackalloc(ty, n), unwind)
  def bin(bin: nir.Bin, ty: Type, l: Val, r: Val, unwind: Next)(implicit
      pos: Position
  ): Val =
    let(Op.Bin(bin, ty, l, r), unwind)
  def comp(comp: nir.Comp, ty: Type, l: Val, r: Val, unwind: Next)(implicit
      pos: Position
  ): Val =
    let(Op.Comp(comp, ty, l, r), unwind)
  def conv(conv: nir.Conv, ty: Type, value: Val, unwind: Next)(implicit
      pos: Position
  ): Val =
    let(Op.Conv(conv, ty, value), unwind)
  def classalloc(name: Global, unwind: Next)(implicit pos: Position): Val =
    let(Op.Classalloc(name), unwind)
  def fieldload(ty: Type, obj: Val, name: Global, unwind: Next)(implicit
      pos: Position
  ): Val =
    let(Op.Fieldload(ty, obj, name), unwind)
  def fieldstore(ty: Type, obj: Val, name: Global, value: Val, unwind: Next)(
      implicit pos: Position
  ): Val =
    let(Op.Fieldstore(ty, obj, name, value), unwind)
  def field(obj: Val, name: Global, unwind: Next)(implicit pos: Position) =
    let(Op.Field(obj, name), unwind)
  def method(obj: Val, sig: Sig, unwind: Next)(implicit pos: Position): Val =
    let(Op.Method(obj, sig), unwind)
  def dynmethod(obj: Val, sig: Sig, unwind: Next)(implicit pos: Position): Val =
    let(Op.Dynmethod(obj, sig), unwind)
  def module(name: Global, unwind: Next)(implicit pos: Position): Val =
    let(Op.Module(name), unwind)
  def as(ty: Type, obj: Val, unwind: Next)(implicit pos: Position): Val =
    let(Op.As(ty, obj), unwind)
  def is(ty: Type, obj: Val, unwind: Next)(implicit pos: Position): Val =
    let(Op.Is(ty, obj), unwind)
  def copy(value: Val, unwind: Next)(implicit pos: Position): Val =
    let(Op.Copy(value), unwind)
  def sizeOf(ty: Type, unwind: Next)(implicit pos: Position): Val =
    let(Op.SizeOf(ty), unwind)
  def alignmentOf(ty: Type, unwind: Next)(implicit pos: Position): Val =
    let(Op.AlignmentOf(ty), unwind)
  def box(ty: Type, obj: Val, unwind: Next)(implicit pos: Position): Val =
    let(Op.Box(ty, obj), unwind)
  def unbox(ty: Type, obj: Val, unwind: Next)(implicit pos: Position): Val =
    let(Op.Unbox(ty, obj), unwind)
  def var_(ty: Type, unwind: Next)(implicit pos: Position): Val =
    let(Op.Var(ty), unwind)
  def varload(slot: Val, unwind: Next)(implicit pos: Position): Val =
    let(Op.Varload(slot), unwind)
  def varstore(slot: Val, value: Val, unwind: Next)(implicit
      pos: Position
  ): Val =
    let(Op.Varstore(slot, value), unwind)
  def arrayalloc(ty: Type, init: Val, unwind: Next)(implicit
      pos: Position
  ): Val =
    let(Op.Arrayalloc(ty, init), unwind)
  def arrayload(ty: Type, arr: Val, idx: Val, unwind: Next)(implicit
      pos: Position
  ): Val =
    let(Op.Arrayload(ty, arr, idx), unwind)
  def arraystore(ty: Type, arr: Val, idx: Val, value: Val, unwind: Next)(
      implicit pos: Position
  ): Val =
    let(Op.Arraystore(ty, arr, idx, value), unwind)
  def arraylength(arr: Val, unwind: Next)(implicit pos: Position): Val =
    let(Op.Arraylength(arr), unwind)

  def fence(memoryOrder: MemoryOrder)(implicit pos: Position): Val =
    let(
      Op.Fence(SyncAttrs(memoryOrder = memoryOrder, isVolatile = false)),
      Next.None
    )
}
