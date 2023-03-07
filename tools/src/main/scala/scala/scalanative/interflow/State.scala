package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.util.unreachable
import scalanative.nir._
import scalanative.linker._
import scalanative.codegen.Lower

final class State(block: Local) {
  var fresh = Fresh(block.id)
  /* Performance Note: OpenHashMap/LongMap/AnyRefMap have a faster clone()
   * operation. This really makes a difference on fullClone() */
  var heap = mutable.LongMap.empty[Instance]
  var locals = mutable.OpenHashMap.empty[Local, Val]
  var delayed = mutable.AnyRefMap.empty[Op, Val]
  var emitted = mutable.AnyRefMap.empty[Op, Val]
  var emit = new nir.Buffer()(fresh)
  var inlineDepth = 0

  private def alloc(kind: Kind, cls: Class, values: Array[Val]): Addr = {
    val addr = fresh().id
    heap(addr) = VirtualInstance(kind, cls, values)
    addr
  }
  def allocClass(cls: Class): Addr = {
    val fields = cls.fields.map(fld => Val.Zero(fld.ty).canonicalize)
    alloc(ClassKind, cls, fields.toArray[Val])
  }
  def allocArray(elemty: Type, count: Int)(implicit
      linked: linker.Result
  ): Addr = {
    val zero = Val.Zero(elemty).canonicalize
    val values = Array.fill[Val](count)(zero)
    val cls = linked.infos(Type.toArrayClass(elemty)).asInstanceOf[Class]
    alloc(ArrayKind, cls, values)
  }
  def allocBox(boxname: Global, value: Val)(implicit
      linked: linker.Result
  ): Addr = {
    val boxcls = linked.infos(boxname).asInstanceOf[Class]
    alloc(BoxKind, boxcls, Array(value))
  }
  def allocString(value: String)(implicit linked: linker.Result): Addr = {
    val charsArray = value.toArray
    val charsAddr = allocArray(Type.Char, charsArray.length)
    val chars = derefVirtual(charsAddr)
    charsArray.zipWithIndex.foreach {
      case (value, idx) =>
        chars.values(idx) = Val.Char(value)
    }
    val values = new Array[Val](4)
    values(linked.StringValueField.index) = Val.Virtual(charsAddr)
    values(linked.StringOffsetField.index) = Val.Int(0)
    values(linked.StringCountField.index) = Val.Int(charsArray.length)
    values(linked.StringCachedHashCodeField.index) =
      Val.Int(Lower.stringHashCode(value))
    alloc(StringKind, linked.StringClass, values)
  }
  def delay(op: Op): Val = {
    if (delayed.contains(op)) {
      delayed(op)
    } else {
      val addr = fresh().id
      val value = Val.Virtual(addr)
      heap(addr) = DelayedInstance(op)
      delayed(op) = value
      value
    }
  }
  def emit(op: Op, idempotent: Boolean = false)(implicit
      position: Position
  ): Val = {
    if (op.isIdempotent || idempotent) {
      if (emitted.contains(op)) {
        emitted(op)
      } else {
        val value = emit.let(op, Next.None)
        emitted(op) = value
        value
      }
    } else {
      emit.let(op, Next.None)
    }
  }
  def deref(addr: Addr): Instance = {
    heap(addr)
  }
  def derefVirtual(addr: Addr): VirtualInstance = {
    heap(addr).asInstanceOf[VirtualInstance]
  }
  def derefDelayed(addr: Addr): DelayedInstance = {
    heap(addr).asInstanceOf[DelayedInstance]
  }
  def derefEscaped(addr: Addr): EscapedInstance = {
    heap(addr).asInstanceOf[EscapedInstance]
  }
  def isVirtual(addr: Addr): Boolean = {
    heap(addr).isInstanceOf[VirtualInstance]
  }
  def isVirtual(value: Val): Boolean = value match {
    case Val.Virtual(addr) => isVirtual(addr)
    case _                 => false
  }
  def isDelayed(addr: Addr): Boolean = {
    heap(addr).isInstanceOf[DelayedInstance]
  }
  def isDelayed(value: Val): Boolean = value match {
    case Val.Virtual(addr) => isDelayed(addr)
    case _                 => false
  }
  def hasEscaped(addr: Addr): Boolean = {
    heap(addr).isInstanceOf[EscapedInstance]
  }
  def hasEscaped(value: Val): Boolean = value match {
    case Val.Virtual(addr) => hasEscaped(addr)
    case _                 => false
  }
  def loadLocal(local: Local): Val = {
    locals(local)
  }
  def storeLocal(local: Local, value: Val): Unit = {
    locals(local) = value
  }
  def newVar(ty: Type): Local = {
    val local = Local(-fresh().id)
    locals(local) = Val.Zero(ty).canonicalize
    local
  }
  def loadVar(local: Local): Val = {
    assert(local.id < 0)
    locals(local)
  }
  def storeVar(local: Local, value: Val): Unit = {
    assert(local.id < 0)
    locals(local) = value
  }
  def inherit(other: State, roots: Seq[Val]): Unit = {
    val closure = heapClosure(roots) ++ other.heapClosure(roots)

    for {
      addr <- closure
      // Ignore keys no longer existing in other state
      obj <- other.heap.get(addr)
    } {
      val clone = obj.clone()
      clone match {
        case DelayedInstance(op) =>
          delayed(op) = Val.Virtual(addr)
        case _ =>
          ()
      }
      heap(addr) = clone
    }

    emitted ++= other.emitted
  }
  def heapClosure(roots: Seq[Val]): mutable.Set[Addr] = {
    val reachable = mutable.Set.empty[Addr]

    def reachAddr(addr: Addr): Unit = {
      if (heap.contains(addr) && !reachable.contains(addr)) {
        reachable += addr
        heap(addr) match {
          case VirtualInstance(_, _, vals) =>
            vals.foreach(reachVal)
          case EscapedInstance(value) =>
            reachVal(value)
          case DelayedInstance(op) =>
            reachOp(op)
        }
      }
    }

    def reachVal(v: Val): Unit = v match {
      case Val.Virtual(addr)       => reachAddr(addr)
      case Val.ArrayValue(_, vals) => vals.foreach(reachVal)
      case Val.StructValue(vals)   => vals.foreach(reachVal)
      case _                       => ()
    }

    def reachOp(op: Op): Unit = op match {
      case Op.Call(_, v, vs)      => reachVal(v); vs.foreach(reachVal)
      case Op.Load(_, v, _)       => reachVal(v)
      case Op.Store(_, v1, v2, _) => reachVal(v1); reachVal(v2)
      case Op.Elem(_, v, vs)      => reachVal(v); vs.foreach(reachVal)
      case Op.Extract(v, _)       => reachVal(v)
      case Op.Insert(v1, v2, _)   => reachVal(v1); reachVal(v2)
      case Op.Stackalloc(_, v)    => reachVal(v)
      case Op.Bin(_, _, v1, v2)   => reachVal(v1); reachVal(v2)
      case Op.Comp(_, _, v1, v2)  => reachVal(v1); reachVal(v2)
      case Op.Conv(_, _, v)       => reachVal(v)
      case Op.Fence(_)            => ()

      case _: Op.Classalloc            => ()
      case Op.Fieldload(_, v, _)       => reachVal(v)
      case Op.Fieldstore(_, v1, _, v2) => reachVal(v1); reachVal(v2)
      case Op.Field(v, _)              => reachVal(v)
      case Op.Method(v, _)             => reachVal(v)
      case Op.Dynmethod(v, _)          => reachVal(v)
      case _: Op.Module                => ()
      case Op.As(_, v)                 => reachVal(v)
      case Op.Is(_, v)                 => reachVal(v)
      case Op.Copy(v)                  => reachVal(v)
      case _: Op.SizeOf                => ()
      case _: Op.AlignmentOf           => ()
      case Op.Box(_, v)                => reachVal(v)
      case Op.Unbox(_, v)              => reachVal(v)
      case _: Op.Var                   => ()
      case Op.Varload(v)               => reachVal(v)
      case Op.Varstore(v1, v2)         => reachVal(v1); reachVal(v2)
      case Op.Arrayalloc(_, v)         => reachVal(v)
      case Op.Arrayload(_, v1, v2)     => reachVal(v1); reachVal(v2)
      case Op.Arraystore(_, v1, v2, v3) =>
        reachVal(v1); reachVal(v2); reachVal(v3)
      case Op.Arraylength(v) => reachVal(v)
    }

    roots.foreach(reachVal)

    reachable
  }
  def fullClone(block: Local): State = {
    val newstate = new State(block)
    newstate.heap = heap.mapValuesNow(_.clone())
    newstate.locals = locals.clone()
    newstate.delayed = delayed.clone()
    newstate.emitted = emitted.clone()
    newstate.inlineDepth = inlineDepth
    newstate
  }
  override def equals(other: Any): Boolean = other match {
    case other: State =>
      other.heap == this.heap && other.locals == this.locals
    case _ =>
      false
  }
  def materialize(
      rootValue: Val
  )(implicit linked: linker.Result, origPos: Position): Val = {
    val locals = mutable.Map.empty[Addr, Val]

    def reachAddr(addr: Addr): Unit = {
      if (!locals.contains(addr)) {
        val local = reachAlloc(addr)
        locals(addr) = local
        reachInit(local, addr)
        heap(addr) = EscapedInstance(local)
      }
    }

    def reachAlloc(addr: Addr): Val = heap(addr) match {
      case VirtualInstance(ArrayKind, cls, values) =>
        val ArrayRef(elemty, _) = cls.ty: @unchecked
        val canConstantInit =
          (!elemty.isInstanceOf[Type.RefKind]
            && values.forall(_.isCanonical)
            && values.exists(v => !v.isZero))
        val init =
          if (canConstantInit) {
            Val.ArrayValue(elemty, values.toSeq)
          } else {
            Val.Int(values.length)
          }
        emit.arrayalloc(elemty, init, Next.None)
      case VirtualInstance(BoxKind, cls, Array(value)) =>
        reachVal(value)
        emit(Op.Box(Type.Ref(cls.name), escapedVal(value)))
      case VirtualInstance(StringKind, _, values)
          if !hasEscaped(values(linked.StringValueField.index)) =>
        val Val.Virtual(charsAddr) = values(
          linked.StringValueField.index
        ): @unchecked
        val chars = derefVirtual(charsAddr).values
          .map {
            case Val.Char(v) =>
              v
            case _ =>
              unreachable
          }
          .toArray[Char]
        Val.String(new java.lang.String(chars))
      case VirtualInstance(_, cls, values) =>
        emit.classalloc(cls.name, Next.None)
      case DelayedInstance(op) =>
        reachOp(op)
        emit(escapedOp(op), idempotent = true)
      case EscapedInstance(value) =>
        reachVal(value)
        escapedVal(value)
    }

    def reachInit(local: Val, addr: Addr): Unit = heap(addr) match {
      case VirtualInstance(ArrayKind, cls, values) =>
        val ArrayRef(elemty, _) = cls.ty: @unchecked
        val canConstantInit =
          (!elemty.isInstanceOf[Type.RefKind]
            && values.forall(_.isCanonical)
            && values.exists(v => !v.isZero))
        if (!canConstantInit) {
          values.zipWithIndex.foreach {
            case (value, idx) =>
              if (!value.isZero) {
                reachVal(value)
                emit.arraystore(
                  elemty,
                  local,
                  Val.Int(idx),
                  escapedVal(value),
                  Next.None
                )
              }
          }
        }
      case VirtualInstance(BoxKind, cls, Array(value)) =>
        ()
      case VirtualInstance(StringKind, _, values)
          if !hasEscaped(values(linked.StringValueField.index)) =>
        ()
      case VirtualInstance(_, cls, vals) =>
        cls.fields.zip(vals).foreach {
          case (fld, value) =>
            if (!value.isZero) {
              reachVal(value)
              emit.fieldstore(
                fld.ty,
                local,
                fld.name,
                escapedVal(value),
                Next.None
              )
            }
        }
      case DelayedInstance(op) =>
        ()
      case EscapedInstance(value) =>
        ()
    }

    def reachVal(v: Val): Unit = v match {
      case Val.Virtual(addr)       => reachAddr(addr)
      case Val.ArrayValue(_, vals) => vals.foreach(reachVal)
      case Val.StructValue(vals)   => vals.foreach(reachVal)
      case _                       => ()
    }

    def reachOp(op: Op): Unit = op match {
      case Op.Call(_, v, vs)      => reachVal(v); vs.foreach(reachVal)
      case Op.Load(_, v, _)       => reachVal(v)
      case Op.Store(_, v1, v2, _) => reachVal(v1); reachVal(v2)
      case Op.Elem(_, v, vs)      => reachVal(v); vs.foreach(reachVal)
      case Op.Extract(v, _)       => reachVal(v)
      case Op.Insert(v1, v2, _)   => reachVal(v1); reachVal(v2)
      case Op.Stackalloc(_, v)    => reachVal(v)
      case Op.Bin(_, _, v1, v2)   => reachVal(v1); reachVal(v2)
      case Op.Comp(_, _, v1, v2)  => reachVal(v1); reachVal(v2)
      case Op.Conv(_, _, v)       => reachVal(v)
      case Op.Fence(_)            => ()

      case _: Op.Classalloc            => ()
      case Op.Fieldload(_, v, _)       => reachVal(v)
      case Op.Fieldstore(_, v1, _, v2) => reachVal(v1); reachVal(v2)
      case Op.Field(v, _)              => reachVal(v)
      case Op.Method(v, _)             => reachVal(v)
      case Op.Dynmethod(v, _)          => reachVal(v)
      case _: Op.Module                => ()
      case Op.As(_, v)                 => reachVal(v)
      case Op.Is(_, v)                 => reachVal(v)
      case Op.Copy(v)                  => reachVal(v)
      case _: Op.SizeOf                => ()
      case _: Op.AlignmentOf           => ()
      case Op.Box(_, v)                => reachVal(v)
      case Op.Unbox(_, v)              => reachVal(v)
      case _: Op.Var                   => ()
      case Op.Varload(v)               => reachVal(v)
      case Op.Varstore(v1, v2)         => reachVal(v1); reachVal(v2)
      case Op.Arrayalloc(_, v)         => reachVal(v)
      case Op.Arrayload(_, v1, v2)     => reachVal(v1); reachVal(v2)
      case Op.Arraystore(_, v1, v2, v3) =>
        reachVal(v1); reachVal(v2); reachVal(v3)
      case Op.Arraylength(v) => reachVal(v)
    }

    def escapedVal(v: Val): Val = v match {
      case Val.Virtual(addr) =>
        locals(addr)
      case _ =>
        v
    }

    def escapedOp(op: Op): Op = op match {
      case Op.Call(ty, v, vs) =>
        Op.Call(ty, escapedVal(v), vs.map(escapedVal))
      case op @ Op.Load(_, v, _) =>
        op.copy(ptr = escapedVal(v))
      case op @ Op.Store(_, v1, v2, _) =>
        op.copy(ptr = escapedVal(v1), value = escapedVal(v2))
      case Op.Elem(ty, v, vs) =>
        Op.Elem(ty, escapedVal(v), vs.map(escapedVal))
      case Op.Extract(v, idxs) =>
        Op.Extract(escapedVal(v), idxs)
      case Op.Insert(v1, v2, idxs) =>
        Op.Insert(escapedVal(v1), escapedVal(v2), idxs)
      case Op.Stackalloc(ty, v) =>
        Op.Stackalloc(ty, escapedVal(v))
      case Op.Bin(bin, ty, v1, v2) =>
        Op.Bin(bin, ty, escapedVal(v1), escapedVal(v2))
      case Op.Comp(comp, ty, v1, v2) =>
        Op.Comp(comp, ty, escapedVal(v1), escapedVal(v2))
      case Op.Conv(conv, ty, v) =>
        Op.Conv(conv, ty, escapedVal(v))
      case Op.Fence(_) => op

      case op: Op.Classalloc =>
        op
      case Op.Fieldload(ty, v, n) =>
        Op.Fieldload(ty, escapedVal(v), n)
      case Op.Fieldstore(ty, v1, n, v2) =>
        Op.Fieldstore(ty, escapedVal(v1), n, escapedVal(v2))
      case Op.Field(v, n) =>
        Op.Field(escapedVal(v), n)
      case Op.Method(v, n) =>
        Op.Method(escapedVal(v), n)
      case Op.Dynmethod(v, n) =>
        Op.Dynmethod(escapedVal(v), n)
      case op: Op.Module =>
        op
      case Op.As(ty, v) =>
        Op.As(ty, escapedVal(v))
      case Op.Is(ty, v) =>
        Op.Is(ty, escapedVal(v))
      case Op.Copy(v) =>
        Op.Copy(escapedVal(v))
      case op: Op.SizeOf      => op
      case op: Op.AlignmentOf => op
      case Op.Box(ty, v) =>
        Op.Box(ty, escapedVal(v))
      case Op.Unbox(ty, v) =>
        Op.Unbox(ty, escapedVal(v))
      case op: Op.Var =>
        op
      case Op.Varload(v) =>
        Op.Varload(escapedVal(v))
      case Op.Varstore(v1, v2) =>
        Op.Varstore(escapedVal(v1), escapedVal(v2))
      case Op.Arrayalloc(ty, v) => Op.Arrayalloc(ty, escapedVal(v))
      case Op.Arrayload(ty, v1, v2) =>
        Op.Arrayload(ty, escapedVal(v1), escapedVal(v2))
      case Op.Arraystore(ty, v1, v2, v3) =>
        Op.Arraystore(ty, escapedVal(v1), escapedVal(v2), escapedVal(v3))
      case Op.Arraylength(v) =>
        Op.Arraylength(escapedVal(v))
    }

    reachVal(rootValue)
    escapedVal(rootValue)
  }
}
