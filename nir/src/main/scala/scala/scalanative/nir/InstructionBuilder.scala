package scala.scalanative
package nir

import scala.collection.mutable

/** A class to build sequences of NIR instructions. */
class InstructionBuilder(implicit fresh: Fresh) {

  private val buffer = mutable.UnrolledBuffer.empty[Inst]
  def +=(inst: Inst): Unit = buffer += inst
  def ++=(insts: Seq[Inst]): Unit = buffer ++= insts
  def ++=(other: InstructionBuilder): Unit = buffer ++= other.buffer

  def toSeq: Seq[Inst] = buffer.toSeq
  def size: Int = buffer.size
  def foreach(fn: Inst => Unit) = buffer.foreach(fn)
  def exists(pred: Inst => Boolean) = buffer.exists(pred)

  private[scalanative] def updateLetInst(
      expectedId: Local
  )(fn: Inst.Let => Inst.Let): Option[Inst.Let] = {
    val idx = buffer.indexWhere {
      case Inst.Let(id, _, _) => expectedId == id
      case _                  => false
    }
    if (idx < 0) None
    else {
      val inst = buffer(idx).asInstanceOf[Inst.Let]
      val patched = fn(inst)
      buffer.update(idx, patched)
      Some(patched)
    }
  }

  // Control-flow ops
  def label(id: Local)(implicit pos: SourcePosition): Unit =
    this += Inst.Label(id, Seq.empty)
  def label(id: Local, params: Seq[Val.Local])(implicit
      pos: SourcePosition
  ): Unit =
    this += Inst.Label(id, params)
  def unreachable(unwind: Next)(implicit pos: SourcePosition): Unit =
    this += Inst.Unreachable(unwind)
  def ret(value: Val)(implicit pos: SourcePosition): Unit =
    this += Inst.Ret(value)
  def jump(next: Next)(implicit pos: SourcePosition): Unit =
    this += Inst.Jump(next)
  def jump(to: Local, args: Seq[Val])(implicit pos: SourcePosition): Unit =
    this += Inst.Jump(Next.Label(to, args))
  def branch(value: Val, thenp: Next, elsep: Next)(implicit
      pos: SourcePosition
  ): Unit =
    this += Inst.If(value, thenp, elsep)
  def branchLinktime(condition: LinktimeCondition, thenp: Next, elsep: Next)(
      implicit pos: SourcePosition
  ): Unit =
    this += Inst.LinktimeIf(condition, thenp, elsep)
  def switch(value: Val, default: Next, cases: Seq[Next])(implicit
      pos: SourcePosition
  ): Unit =
    this += Inst.Switch(value, default, cases)
  def raise(value: Val, unwind: Next)(implicit pos: SourcePosition): Unit =
    this += Inst.Throw(value, unwind)

  // Compute ops
  def let(id: Local, op: Op, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = {
    this += Inst.Let(id, op, unwind)
    Val.Local(id, op.resty)
  }
  def let(op: Op, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(fresh(), op, unwind)

  def call(ty: Type.Function, ptr: Val, args: Seq[Val], unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Call(ty, ptr, args), unwind)

  def load(
      ty: Type,
      ptr: Val,
      unwind: Next,
      memoryOrder: Option[MemoryOrder] = None
  )(implicit pos: SourcePosition, scope: ScopeId): Val.Local =
    let(Op.Load(ty, ptr, memoryOrder), unwind)

  def store(
      ty: Type,
      ptr: Val,
      value: Val,
      unwind: Next,
      memoryOrder: Option[MemoryOrder] = None
  )(implicit pos: SourcePosition, scope: ScopeId): Val.Local =
    let(Op.Store(ty, ptr, value, memoryOrder), unwind)

  def elem(ty: Type, ptr: Val, indexes: Seq[Val], unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Elem(ty, ptr, indexes), unwind)

  def extract(aggr: Val, indexes: Seq[Int], unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Extract(aggr, indexes), unwind)

  def insert(aggr: Val, value: Val, indexes: Seq[Int], unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local =
    let(Op.Insert(aggr, value, indexes), unwind)

  def stackalloc(ty: Type, n: Val, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Stackalloc(ty, n), unwind)

  def bin(bin: nir.Bin, ty: Type, l: Val, r: Val, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Bin(bin, ty, l, r), unwind)

  def comp(comp: nir.Comp, ty: Type, l: Val, r: Val, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Comp(comp, ty, l, r), unwind)

  def conv(conv: nir.Conv, ty: Type, value: Val, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Conv(conv, ty, value), unwind)

  def classalloc(name: Global.Top, unwind: Next, zone: Option[Val] = None)(
      implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Classalloc(name, zone), unwind)

  def fieldload(ty: Type, obj: Val, name: Global.Member, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Fieldload(ty, obj, name), unwind)

  def fieldstore(
      ty: Type,
      obj: Val,
      name: Global.Member,
      value: Val,
      unwind: Next
  )(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Fieldstore(ty, obj, name, value), unwind)

  def field(obj: Val, name: Global.Member, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ) =
    let(Op.Field(obj, name), unwind)

  def method(obj: Val, sig: Sig, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Method(obj, sig), unwind)

  def dynmethod(obj: Val, sig: Sig, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Dynmethod(obj, sig), unwind)

  def module(name: Global.Top, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Module(name), unwind)

  def as(ty: Type, obj: Val, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.As(ty, obj), unwind)

  def is(ty: Type, obj: Val, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Is(ty, obj), unwind)

  def copy(value: Val, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Copy(value), unwind)

  def sizeOf(ty: Type, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.SizeOf(ty), unwind)

  def alignmentOf(ty: Type, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.AlignmentOf(ty), unwind)

  def box(ty: Type, obj: Val, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Box(ty, obj), unwind)

  def unbox(ty: Type, obj: Val, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Unbox(ty, obj), unwind)

  def var_(ty: Type, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Var(ty), unwind)

  def varload(slot: Val, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Varload(slot), unwind)

  def varstore(slot: Val, value: Val, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Varstore(slot, value), unwind)

  def arrayalloc(
      ty: Type,
      init: Val,
      unwind: Next,
      zone: Option[Val] = None
  )(implicit pos: SourcePosition, scope: ScopeId): Val.Local =
    let(Op.Arrayalloc(ty, init, zone), unwind)

  def arrayload(ty: Type, arr: Val, idx: Val, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Arrayload(ty, arr, idx), unwind)

  def arraystore(ty: Type, arr: Val, idx: Val, value: Val, unwind: Next)(
      implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Arraystore(ty, arr, idx, value), unwind)

  def arraylength(arr: Val, unwind: Next)(implicit
      pos: SourcePosition,
      scope: ScopeId
  ): Val.Local = let(Op.Arraylength(arr), unwind)

  def fence(
      memoryOrder: MemoryOrder
  )(implicit pos: SourcePosition, scope: ScopeId): Val.Local =
    let(
      Op.Fence(memoryOrder = memoryOrder),
      Next.None
    )
}
