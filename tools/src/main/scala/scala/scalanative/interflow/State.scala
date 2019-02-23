package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.util.unreachable
import scalanative.nir._
import scalanative.linker._
import scalanative.codegen.Lower

final class State(block: Local) {
  var fresh  = Fresh(block.id)
  var heap   = mutable.Map.empty[Addr, Instance]
  var locals = mutable.Map.empty[Local, Val]
  var emit   = new nir.Buffer()(fresh)

  private def alloc(kind: Kind, cls: Class, values: Array[Val]): Addr = {
    val addr = fresh().id
    heap(addr) = VirtualInstance(kind, cls, values)
    addr
  }
  def allocClass(cls: Class): Addr = {
    val fields = cls.fields.map(fld => Val.Zero(fld.ty).canonicalize)
    alloc(ClassKind, cls, fields.toArray[Val])
  }
  def allocArray(elemty: Type, count: Int)(
      implicit linked: linker.Result): Addr = {
    val zero   = Val.Zero(elemty).canonicalize
    val values = Array.fill[Val](count)(zero)
    val cls    = linked.infos(Type.toArrayClass(elemty)).asInstanceOf[Class]
    alloc(ArrayKind, cls, values)
  }
  def allocBox(boxname: Global, value: Val)(
      implicit linked: linker.Result): Addr = {
    val boxcls = linked.infos(boxname).asInstanceOf[Class]
    alloc(BoxKind, boxcls, Array(value))
  }
  def allocString(value: String)(implicit linked: linker.Result): Addr = {
    val charsArray = value.toArray
    val charsAddr  = allocArray(Type.Char, charsArray.length)
    val chars      = derefVirtual(charsAddr)
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
  def deref(addr: Addr): Instance = {
    heap(addr)
  }
  def derefVirtual(addr: Addr): VirtualInstance = {
    heap(addr).asInstanceOf[VirtualInstance]
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

    closure.foreach { addr =>
      heap(addr) = other.heap(addr).clone()
    }
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
        }
      }
    }

    def reachVal(v: Val): Unit = v match {
      case Val.Virtual(addr)       => reachAddr(addr)
      case Val.ArrayValue(_, vals) => vals.foreach(reachVal)
      case Val.StructValue(vals)   => vals.foreach(reachVal)
      case _                       => ()
    }

    roots.foreach(reachVal)

    reachable
  }
  def fullClone(block: Local): State = {
    val newstate = new State(block)
    newstate.heap = heap.map { case (k, v) => (k, v.clone()) }
    newstate.locals = locals.clone()
    newstate
  }
  override def equals(other: Any): Boolean = other match {
    case other: State =>
      other.heap == this.heap && other.locals == this.locals
    case _ =>
      false
  }
  def materialize(rootValue: Val)(implicit linked: linker.Result): Val = {
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
        val ArrayRef(elemty, _) = cls.ty
        val canConstantInit =
          (!elemty.isInstanceOf[Type.RefKind]
            && values.forall(_.isCanonical)
            && values.exists(v => !v.isDefault))
        val init =
          if (canConstantInit) {
            Val.ArrayValue(elemty, values)
          } else {
            Val.Int(values.length)
          }
        emit.arrayalloc(elemty, init, Next.None)
      case VirtualInstance(BoxKind, cls, Array(value)) =>
        reachVal(value)
        emit.let(Op.Box(Type.Ref(cls.name), escapedVal(value)), Next.None)
      case VirtualInstance(StringKind, _, values)
          if !hasEscaped(values(linked.StringValueField.index)) =>
        val Val.Virtual(charsAddr) = values(linked.StringValueField.index)
        val chars = derefVirtual(charsAddr).values
          .map {
            case Val.Char(v) => v
          }
          .toArray[Char]
        Val.String(new java.lang.String(chars))
      case VirtualInstance(_, cls, values) =>
        emit.classalloc(cls.name, Next.None)
      case EscapedInstance(value) =>
        reachVal(value)
        escapedVal(value)
    }

    def reachInit(local: Val, addr: Addr): Unit = heap(addr) match {
      case VirtualInstance(ArrayKind, cls, values) =>
        val ArrayRef(elemty, _) = cls.ty
        val canConstantInit =
          (!elemty.isInstanceOf[Type.RefKind]
            && values.forall(_.isCanonical)
            && values.exists(v => !v.isDefault))
        if (!canConstantInit) {
          values.zipWithIndex.foreach {
            case (value, idx) =>
              if (!value.isDefault) {
                reachVal(value)
                emit.arraystore(elemty,
                                local,
                                Val.Int(idx),
                                escapedVal(value),
                                Next.None)
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
            if (!value.isDefault) {
              reachVal(value)
              emit.fieldstore(fld.ty,
                              local,
                              fld.name,
                              escapedVal(value),
                              Next.None)
            }
        }
      case EscapedInstance(value) =>
        ()
    }

    def reachVal(v: Val): Unit = v match {
      case Val.Virtual(addr)       => reachAddr(addr)
      case Val.ArrayValue(_, vals) => vals.foreach(reachVal)
      case Val.StructValue(vals)   => vals.foreach(reachVal)
      case _                       => ()
    }

    def escapedVal(v: Val): Val = v match {
      case Val.Virtual(addr) =>
        locals(addr)
      case _ =>
        v
    }

    reachVal(rootValue)
    escapedVal(rootValue)
  }
}
