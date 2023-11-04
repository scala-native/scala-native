package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.util.unreachable
import scalanative.linker._
import scalanative.codegen.Lower

final class State(block: nir.Local)(preserveDebugInfo: Boolean) {

  var fresh = nir.Fresh(block.id)
  /* Performance Note: nir.OpenHashMap/LongMap/AnyRefMap have a faster clone()
   * operation. This really makes a difference on fullClone() */
  var heap = mutable.LongMap.empty[Instance]
  var locals = mutable.OpenHashMap.empty[nir.Local, nir.Val]
  var delayed = mutable.AnyRefMap.empty[nir.Op, nir.Val]
  var emitted = mutable.AnyRefMap.empty[nir.Op, nir.Val.Local]
  var emit = new nir.Buffer()(fresh)
  var inlineDepth = 0

  // Delayed init
  var localNames: mutable.OpenHashMap[nir.Local, String] = _
  var virtualNames: mutable.LongMap[String] = _

  if (preserveDebugInfo) {
    localNames = mutable.OpenHashMap.empty[nir.Local, String]
    virtualNames = mutable.LongMap.empty[String]
  }

  private def alloc(
      kind: Kind,
      cls: Class,
      values: Array[nir.Val],
      zone: Option[nir.Val]
  )(implicit srcPosition: nir.Position, scopeId: nir.ScopeId): Addr = {
    val addr = fresh().id
    heap(addr) = VirtualInstance(kind, cls, values, zone)
    addr
  }
  def allocClass(cls: Class, zone: Option[nir.Val])(implicit
      srcPosition: nir.Position,
      scopeId: nir.ScopeId
  ): Addr = {
    val fields = cls.fields.map(fld => nir.Val.Zero(fld.ty).canonicalize)
    alloc(ClassKind, cls, fields.toArray[nir.Val], zone)
  }
  def allocArray(elemty: nir.Type, count: Int, zone: Option[nir.Val])(implicit
      analysis: ReachabilityAnalysis.Result,
      srcPosition: nir.Position,
      scopeId: nir.ScopeId
  ): Addr = {
    val zero = nir.Val.Zero(elemty).canonicalize
    val values = Array.fill[nir.Val](count)(zero)
    val cls = analysis.infos(nir.Type.toArrayClass(elemty)).asInstanceOf[Class]
    alloc(ArrayKind, cls, values, zone)
  }
  def allocBox(boxname: nir.Global, value: nir.Val)(implicit
      analysis: ReachabilityAnalysis.Result,
      srcPosition: nir.Position,
      scopeId: nir.ScopeId
  ): Addr = {
    val boxcls = analysis.infos(boxname).asInstanceOf[Class]
    alloc(BoxKind, boxcls, Array(value), zone = None)
  }
  def allocString(value: String)(implicit
      analysis: ReachabilityAnalysis.Result,
      srcPosition: nir.Position,
      scopeId: nir.ScopeId
  ): Addr = {
    val charsArray = value.toArray
    val charsAddr = allocArray(nir.Type.Char, charsArray.length, zone = None)
    val chars = derefVirtual(charsAddr)
    charsArray.zipWithIndex.foreach {
      case (value, idx) =>
        chars.values(idx) = nir.Val.Char(value)
    }
    val values = new Array[nir.Val](4)
    values(analysis.StringValueField.index) = nir.Val.Virtual(charsAddr)
    values(analysis.StringOffsetField.index) = nir.Val.Int(0)
    values(analysis.StringCountField.index) = nir.Val.Int(charsArray.length)
    values(analysis.StringCachedHashCodeField.index) =
      nir.Val.Int(Lower.stringHashCode(value))
    alloc(StringKind, analysis.StringClass, values, zone = None)
  }
  def delay(
      op: nir.Op
  )(implicit srcPosition: nir.Position, scopeId: nir.ScopeId): nir.Val = {
    delayed.getOrElseUpdate(
      op, {
        val addr = fresh().id
        heap(addr) = DelayedInstance(op)
        nir.Val.Virtual(addr)
      }
    )
  }
  def emit(op: nir.Op, idempotent: Boolean = false)(implicit
      srcPosition: nir.Position,
      scopeId: nir.ScopeId
  ): nir.Val.Local = {
    if (op.isIdempotent || idempotent) {
      if (emitted.contains(op)) {
        emitted(op)
      } else {
        val value = emit.let(op, nir.Next.None)
        emitted(op) = value
        value
      }
    } else emit.let(op, nir.Next.None)
  }

  def emitVirtual(
      addr: Addr
  )(op: nir.Op, idempotent: Boolean = false): nir.Val.Local = {
    val instance = heap(addr)
    import instance.{srcPosition, scopeId}

    val value = emit(op, idempotent)
    // there might cases when virtualName for given addres might be assigned to two different instances
    // It can happend when we deal with partially-evaluated instances, eg. arrayalloc + arraystore
    // Don't emit local names for ops returing unit value
    if (preserveDebugInfo && op.resty != nir.Type.Unit) {
      virtualNames.get(addr).foreach { name =>
        this.localNames += value.id -> name
      }
    }
    value
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
  def isVirtual(value: nir.Val): Boolean = value match {
    case nir.Val.Virtual(addr) =>
      isVirtual(addr)
    case _ =>
      false
  }
  def isDelayed(addr: Addr): Boolean = {
    heap(addr).isInstanceOf[DelayedInstance]
  }
  def isDelayed(value: nir.Val): Boolean = value match {
    case nir.Val.Virtual(addr) =>
      isDelayed(addr)
    case _ =>
      false
  }
  def hasEscaped(addr: Addr): Boolean = {
    heap(addr).isInstanceOf[EscapedInstance]
  }
  def hasEscaped(value: nir.Val): Boolean = value match {
    case nir.Val.Virtual(addr) =>
      hasEscaped(addr)
    case _ =>
      false
  }
  def loadLocal(local: nir.Local): nir.Val = {
    locals(local)
  }
  def storeLocal(local: nir.Local, value: nir.Val): Unit = {
    locals(local) = value
  }
  def newVar(ty: nir.Type): nir.Local = {
    val local = nir.Local(-fresh().id)
    locals(local) = nir.Val.Zero(ty).canonicalize
    local
  }
  def loadVar(local: nir.Local): nir.Val = {
    assert(local.id < 0)
    locals(local)
  }
  def storeVar(local: nir.Local, value: nir.Val): Unit = {
    assert(local.id < 0)
    locals(local) = value
  }
  def inherit(other: State, roots: Seq[nir.Val]): Unit = {
    val closure = heapClosure(roots) ++ other.heapClosure(roots)

    for {
      addr <- closure
      // Ignore keys no longer existing in other state
      obj <- other.heap.get(addr)
    } {
      val clone = obj.clone()
      clone match {
        case DelayedInstance(op) =>
          delayed(op) = nir.Val.Virtual(addr)
        case _ =>
          ()
      }
      heap(addr) = clone
    }

    emitted ++= other.emitted
    if (preserveDebugInfo) {
      localNames.addMissing(other.localNames)
      virtualNames.addMissing(other.virtualNames)
    }
  }
  def heapClosure(roots: Seq[nir.Val]): mutable.Set[Addr] = {
    val reachable = mutable.Set.empty[Addr]

    def reachAddr(addr: Addr): Unit = {
      if (heap.contains(addr) && !reachable.contains(addr)) {
        reachable += addr
        heap(addr) match {
          case VirtualInstance(_, _, vals, zone) =>
            vals.foreach(reachVal)
            zone.foreach(reachVal)
          case EscapedInstance(value) =>
            reachVal(value)
          case DelayedInstance(op) =>
            reachOp(op)
        }
      }
    }

    def reachVal(v: nir.Val): Unit = v match {
      case nir.Val.Virtual(addr) =>
        reachAddr(addr)
      case nir.Val.ArrayValue(_, vals) =>
        vals.foreach(reachVal)
      case nir.Val.StructValue(vals) =>
        vals.foreach(reachVal)
      case _ =>
        ()
    }

    def reachOp(op: nir.Op): Unit = op match {
      case nir.Op.Call(_, v, vs)      => reachVal(v); vs.foreach(reachVal)
      case nir.Op.Load(_, v, _)       => reachVal(v)
      case nir.Op.Store(_, v1, v2, _) => reachVal(v1); reachVal(v2)
      case nir.Op.Elem(_, v, vs)      => reachVal(v); vs.foreach(reachVal)
      case nir.Op.Extract(v, _)       => reachVal(v)
      case nir.Op.Insert(v1, v2, _)   => reachVal(v1); reachVal(v2)
      case nir.Op.Stackalloc(_, v)    => reachVal(v)
      case nir.Op.Bin(_, _, v1, v2)   => reachVal(v1); reachVal(v2)
      case nir.Op.Comp(_, _, v1, v2)  => reachVal(v1); reachVal(v2)
      case nir.Op.Conv(_, _, v)       => reachVal(v)
      case nir.Op.Fence(_)            => ()

      case nir.Op.Classalloc(_, zh)        => zh.foreach(reachVal)
      case nir.Op.Fieldload(_, v, _)       => reachVal(v)
      case nir.Op.Fieldstore(_, v1, _, v2) => reachVal(v1); reachVal(v2)
      case nir.Op.Field(v, _)              => reachVal(v)
      case nir.Op.Method(v, _)             => reachVal(v)
      case nir.Op.Dynmethod(v, _)          => reachVal(v)
      case _: nir.Op.Module                => ()
      case nir.Op.As(_, v)                 => reachVal(v)
      case nir.Op.Is(_, v)                 => reachVal(v)
      case nir.Op.Copy(v)                  => reachVal(v)
      case _: nir.Op.SizeOf                => ()
      case _: nir.Op.AlignmentOf           => ()
      case nir.Op.Box(_, v)                => reachVal(v)
      case nir.Op.Unbox(_, v)              => reachVal(v)
      case _: nir.Op.Var                   => ()
      case nir.Op.Varload(v)               => reachVal(v)
      case nir.Op.Varstore(v1, v2)         => reachVal(v1); reachVal(v2)
      case nir.Op.Arrayalloc(_, v1, zh)    => reachVal(v1); zh.foreach(reachVal)
      case nir.Op.Arrayload(_, v1, v2)     => reachVal(v1); reachVal(v2)
      case nir.Op.Arraystore(_, v1, v2, v3) =>
        reachVal(v1); reachVal(v2); reachVal(v3)
      case nir.Op.Arraylength(v) => reachVal(v)
    }

    roots.foreach(reachVal)

    reachable
  }
  def fullClone(block: nir.Local): State = {
    val newstate = new State(block)(preserveDebugInfo)
    newstate.heap = heap.mapValuesNow(_.clone())
    newstate.locals = locals.clone()
    newstate.delayed = delayed.clone()
    newstate.emitted = emitted.clone()
    newstate.inlineDepth = inlineDepth
    if (preserveDebugInfo) {
      newstate.virtualNames = virtualNames.mapValuesNow(identity)
      newstate.localNames = localNames.clone()
    }
    newstate
  }
  override def equals(other: Any): Boolean = other match {
    case other: State =>
      other.heap == this.heap && other.locals == this.locals
    case _ =>
      false
  }
  def materialize(
      rootValue: nir.Val
  )(implicit analysis: ReachabilityAnalysis.Result): nir.Val = {
    val locals = mutable.Map.empty[Addr, nir.Val]
    def reachAddr(addr: Addr): Unit = {
      if (!locals.contains(addr)) {
        val local = reachAlloc(addr)
        val instance = heap(addr)
        locals(addr) = local
        reachInit(local, addr)
        heap(addr) = new EscapedInstance(local, instance)
      }
    }

    def reachAlloc(addr: Addr): nir.Val = heap(addr) match {
      case VirtualInstance(ArrayKind, cls, values, zone) =>
        val ArrayRef(elemty, _) = cls.ty: @unchecked
        val canConstantInit =
          (!elemty.isInstanceOf[nir.Type.RefKind]
            && values.forall(_.isCanonical)
            && values.exists(v => !v.isZero))
        val init =
          if (canConstantInit) {
            nir.Val.ArrayValue(elemty, values.toSeq)
          } else {
            nir.Val.Int(values.length)
          }
        emitVirtual(addr)(
          nir.Op.Arrayalloc(elemty, init, zone.map(escapedVal))
        )
      case VirtualInstance(BoxKind, cls, Array(value), zone) =>
        reachVal(value)
        zone.foreach(reachVal)
        emitVirtual(addr)(
          nir.Op.Box(nir.Type.Ref(cls.name), escapedVal(value))
        )
      case VirtualInstance(StringKind, _, values, zone)
          if !hasEscaped(values(analysis.StringValueField.index)) =>
        val nir.Val.Virtual(charsAddr) = values(
          analysis.StringValueField.index
        ): @unchecked
        val chars = derefVirtual(charsAddr).values
          .map {
            case nir.Val.Char(v) => v
            case _               => unreachable
          }
          .toArray[Char]
        nir.Val.String(new java.lang.String(chars))
      case VirtualInstance(_, cls, values, zone) =>
        emitVirtual(addr)(
          nir.Op.Classalloc(cls.name, zone.map(escapedVal))
        )
      case DelayedInstance(op) =>
        reachOp(op)
        emitVirtual(addr)(
          escapedOp(op),
          idempotent = true
        )
      case EscapedInstance(value) =>
        reachVal(value)
        escapedVal(value)
    }

    def reachInit(local: nir.Val, addr: Addr): Unit = heap(addr) match {
      case VirtualInstance(ArrayKind, cls, values, zone) =>
        val ArrayRef(elemty, _) = cls.ty: @unchecked
        val canConstantInit =
          (!elemty.isInstanceOf[nir.Type.RefKind]
            && values.forall(_.isCanonical)
            && values.exists(v => !v.isZero))
        if (!canConstantInit) {
          values.zipWithIndex.foreach {
            case (value, idx) =>
              if (!value.isZero) {
                reachVal(value)
                zone.foreach(reachVal)
                emitVirtual(addr)(
                  nir.Op.Arraystore(
                    ty = elemty,
                    arr = local,
                    idx = nir.Val.Int(idx),
                    value = escapedVal(value)
                  )
                )
              }
          }
        }
      case VirtualInstance(BoxKind, cls, Array(value), _) =>
        ()
      case VirtualInstance(StringKind, _, values, _)
          if !hasEscaped(values(analysis.StringValueField.index)) =>
        ()
      case VirtualInstance(_, cls, vals, zone) =>
        cls.fields.zip(vals).foreach {
          case (fld, value) =>
            if (!value.isZero) {
              reachVal(value)
              zone.foreach(reachVal)
              emitVirtual(addr)(
                nir.Op.Fieldstore(
                  ty = fld.ty,
                  obj = local,
                  name = fld.name,
                  value = escapedVal(value)
                )
              )
            }
        }
      case DelayedInstance(op)    => ()
      case EscapedInstance(value) => ()
    }

    def reachVal(v: nir.Val): Unit = v match {
      case nir.Val.Virtual(addr) =>
        reachAddr(addr)
      case nir.Val.ArrayValue(_, vals) =>
        vals.foreach(reachVal)
      case nir.Val.StructValue(vals) =>
        vals.foreach(reachVal)
      case _ =>
        ()
    }

    def reachOp(op: nir.Op): Unit = op match {
      case nir.Op.Call(_, v, vs)      => reachVal(v); vs.foreach(reachVal)
      case nir.Op.Load(_, v, _)       => reachVal(v)
      case nir.Op.Store(_, v1, v2, _) => reachVal(v1); reachVal(v2)
      case nir.Op.Elem(_, v, vs)      => reachVal(v); vs.foreach(reachVal)
      case nir.Op.Extract(v, _)       => reachVal(v)
      case nir.Op.Insert(v1, v2, _)   => reachVal(v1); reachVal(v2)
      case nir.Op.Stackalloc(_, v)    => reachVal(v)
      case nir.Op.Bin(_, _, v1, v2)   => reachVal(v1); reachVal(v2)
      case nir.Op.Comp(_, _, v1, v2)  => reachVal(v1); reachVal(v2)
      case nir.Op.Conv(_, _, v)       => reachVal(v)
      case nir.Op.Fence(_)            => ()

      case nir.Op.Classalloc(_, zh)        => zh.foreach(reachVal)
      case nir.Op.Fieldload(_, v, _)       => reachVal(v)
      case nir.Op.Fieldstore(_, v1, _, v2) => reachVal(v1); reachVal(v2)
      case nir.Op.Field(v, _)              => reachVal(v)
      case nir.Op.Method(v, _)             => reachVal(v)
      case nir.Op.Dynmethod(v, _)          => reachVal(v)
      case _: nir.Op.Module                => ()
      case nir.Op.As(_, v)                 => reachVal(v)
      case nir.Op.Is(_, v)                 => reachVal(v)
      case nir.Op.Copy(v)                  => reachVal(v)
      case _: nir.Op.SizeOf                => ()
      case _: nir.Op.AlignmentOf           => ()
      case nir.Op.Box(_, v)                => reachVal(v)
      case nir.Op.Unbox(_, v)              => reachVal(v)
      case _: nir.Op.Var                   => ()
      case nir.Op.Varload(v)               => reachVal(v)
      case nir.Op.Varstore(v1, v2)         => reachVal(v1); reachVal(v2)
      case nir.Op.Arrayalloc(_, v1, zh)    => reachVal(v1); zh.foreach(reachVal)
      case nir.Op.Arrayload(_, v1, v2)     => reachVal(v1); reachVal(v2)
      case nir.Op.Arraystore(_, v1, v2, v3) =>
        reachVal(v1); reachVal(v2); reachVal(v3)
      case nir.Op.Arraylength(v) => reachVal(v)
    }

    def escapedVal(v: nir.Val): nir.Val = v match {
      case nir.Val.Virtual(addr) =>
        locals(addr)
      case _ =>
        v
    }

    def escapedOp(op: nir.Op): nir.Op = op match {
      case nir.Op.Call(ty, v, vs) =>
        nir.Op.Call(ty, escapedVal(v), vs.map(escapedVal))
      case op @ nir.Op.Load(_, v, _) =>
        op.copy(ptr = escapedVal(v))
      case op @ nir.Op.Store(_, v1, v2, _) =>
        op.copy(ptr = escapedVal(v1), value = escapedVal(v2))
      case nir.Op.Elem(ty, v, vs) =>
        nir.Op.Elem(ty, escapedVal(v), vs.map(escapedVal))
      case nir.Op.Extract(v, idxs) =>
        nir.Op.Extract(escapedVal(v), idxs)
      case nir.Op.Insert(v1, v2, idxs) =>
        nir.Op.Insert(escapedVal(v1), escapedVal(v2), idxs)
      case nir.Op.Stackalloc(ty, v) =>
        nir.Op.Stackalloc(ty, escapedVal(v))
      case nir.Op.Bin(bin, ty, v1, v2) =>
        nir.Op.Bin(bin, ty, escapedVal(v1), escapedVal(v2))
      case nir.Op.Comp(comp, ty, v1, v2) =>
        nir.Op.Comp(comp, ty, escapedVal(v1), escapedVal(v2))
      case nir.Op.Conv(conv, ty, v) =>
        nir.Op.Conv(conv, ty, escapedVal(v))
      case nir.Op.Fence(_) => op

      case op: nir.Op.Classalloc =>
        op
      case nir.Op.Fieldload(ty, v, n) =>
        nir.Op.Fieldload(ty, escapedVal(v), n)
      case nir.Op.Fieldstore(ty, v1, n, v2) =>
        nir.Op.Fieldstore(ty, escapedVal(v1), n, escapedVal(v2))
      case nir.Op.Field(v, n) =>
        nir.Op.Field(escapedVal(v), n)
      case nir.Op.Method(v, n) =>
        nir.Op.Method(escapedVal(v), n)
      case nir.Op.Dynmethod(v, n) =>
        nir.Op.Dynmethod(escapedVal(v), n)
      case op: nir.Op.Module =>
        op
      case nir.Op.As(ty, v) =>
        nir.Op.As(ty, escapedVal(v))
      case nir.Op.Is(ty, v) =>
        nir.Op.Is(ty, escapedVal(v))
      case nir.Op.Copy(v) =>
        nir.Op.Copy(escapedVal(v))
      case op: nir.Op.SizeOf      => op
      case op: nir.Op.AlignmentOf => op
      case nir.Op.Box(ty, v) =>
        nir.Op.Box(ty, escapedVal(v))
      case nir.Op.Unbox(ty, v) =>
        nir.Op.Unbox(ty, escapedVal(v))
      case op: nir.Op.Var =>
        op
      case nir.Op.Varload(v) =>
        nir.Op.Varload(escapedVal(v))
      case nir.Op.Varstore(v1, v2) =>
        nir.Op.Varstore(escapedVal(v1), escapedVal(v2))
      case nir.Op.Arrayalloc(ty, v1, zh) =>
        nir.Op.Arrayalloc(ty, escapedVal(v1), zh.map(escapedVal))
      case nir.Op.Arrayload(ty, v1, v2) =>
        nir.Op.Arrayload(ty, escapedVal(v1), escapedVal(v2))
      case nir.Op.Arraystore(ty, v1, v2, v3) =>
        nir.Op.Arraystore(ty, escapedVal(v1), escapedVal(v2), escapedVal(v3))
      case nir.Op.Arraylength(v) =>
        nir.Op.Arraylength(escapedVal(v))
    }

    reachVal(rootValue)
    escapedVal(rootValue)
  }

}
