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
  def deref(addr: Addr): Instance =
    heap(addr)
  def derefVirtual(addr: Addr): VirtualInstance =
    heap(addr).asInstanceOf[VirtualInstance]
  def derefEscaped(addr: Addr): EscapedInstance =
    heap(addr).asInstanceOf[EscapedInstance]
  def escaped(addr: Addr): Boolean =
    deref(addr).isInstanceOf[EscapedInstance]
  def escaped(value: Val): Boolean = value match {
    case Val.Virtual(addr) => escaped(addr)
    case _                 => false
  }
  def loadLocal(local: Local): Val =
    locals(local)
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
  def materialize(value: Val)(implicit linked: linker.Result): Val =
    value match {
      case Val.Virtual(addr) =>
        materialize(addr)
      case _ =>
        value
    }
  def materialize(addr: Long)(implicit linked: linker.Result): Val = {
    val reachable = mutable.Set.empty[Addr]
    val addrs     = mutable.UnrolledBuffer.empty[Addr]
    def markValue(v: Val): Unit = v match {
      case Val.Virtual(addr) =>
        markAddr(addr)
      case _ =>
        ()
    }
    def markAddr(addr: Addr): Unit = {
      if (!reachable.contains(addr)) {
        def markCurrentAddr() = {
          reachable += addr
          addrs += addr
        }
        deref(addr) match {
          case VirtualInstance(BoxKind, _, _) =>
            markCurrentAddr()
          case VirtualInstance(StringKind, _, values) =>
            markCurrentAddr()
            if (escaped(values(linked.StringValueField.index))) {
              values.foreach(markValue)
            }
          case VirtualInstance(_, _, values) =>
            markCurrentAddr()
            values.foreach(markValue)
          case _: EscapedInstance =>
            ()
        }
      }
    }
    markAddr(addr)

    val locals = addrs.map { addr =>
      val local = deref(addr) match {
        case VirtualInstance(ArrayKind, cls, values) =>
          val ArrayRef(elemty, _) = cls.ty
          val init =
            if (!elemty.isInstanceOf[Type.RefKind]
                && values.forall(_.isCanonical)
                && values.exists(v => !v.isDefault)) {
              Val.ArrayValue(elemty, values)
            } else {
              Val.Int(values.length)
            }
          emit.arrayalloc(elemty, init, Next.None)
        case VirtualInstance(BoxKind, cls, Array(value)) =>
          emit.box(Type.Ref(cls.name), value, Next.None)
        case VirtualInstance(StringKind, _, values)
            if !escaped(values(linked.StringValueField.index)) =>
          val Val.Virtual(charsAddr) = values(linked.StringValueField.index)
          val chars = derefVirtual(charsAddr).values
            .map {
              case Val.Char(v) => v
            }
            .toArray[Char]
          Val.String(new java.lang.String(chars))
        case VirtualInstance(_, cls, _) =>
          emit.classalloc(cls.name, Next.None)
        case _: EscapedInstance =>
          unreachable
      }
      addr -> local
    }.toMap

    def escapedValueOf(addr: Addr): Val =
      if (locals.contains(addr)) {
        locals(addr)
      } else {
        derefEscaped(addr).escapedValue
      }

    addrs.foreach { addr =>
      val local = locals(addr)
      deref(addr) match {
        case VirtualInstance(ArrayKind, cls, values) =>
          val ArrayRef(elemty, _) = cls.ty
          if (!elemty.isInstanceOf[Type.RefKind]
              && values.forall(_.isCanonical)
              && values.exists(v => !v.isDefault)) {
            ()
          } else {
            values.zipWithIndex.foreach {
              case (value, idx) if value.isDefault =>
                // fields are initialied to default value
                ()
              case (Val.Virtual(addr), idx) =>
                val value = escapedValueOf(addr)
                assert(!value.isVirtual)
                emit.arraystore(elemty, local, Val.Int(idx), value, Next.None)
              case (value, idx) =>
                emit.arraystore(elemty, local, Val.Int(idx), value, Next.None)
            }
          }
        case VirtualInstance(BoxKind, _, _) =>
          ()
        case VirtualInstance(StringKind, _, values)
            if !escaped(values(linked.StringValueField.index)) =>
          ()
        case VirtualInstance(_, cls, values) =>
          cls.fields.zip(values).foreach {
            case (fld, value) if value.isDefault =>
              // fields are initialied to default value
              ()
            case (fld, Val.Virtual(addr)) =>
              val value = escapedValueOf(addr)
              assert(!value.isVirtual)
              emit.fieldstore(fld.ty, local, fld.name, value, Next.None)
            case (fld, value) =>
              emit.fieldstore(fld.ty, local, fld.name, value, Next.None)
          }
        case _: EscapedInstance =>
          unreachable
      }
      heap(addr) = new EscapedInstance(heap(addr).cls, local)
    }

    escapedValueOf(addr)
  }
  def inherit(other: State): Unit = {
    this.heap = other.heap.map { case (k, v) => (k, v.clone()) }
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
  def diff(other: State): String = {
    val builder = new StringBuilder
    def println(msg: String): Unit = {
      builder.append(msg)
      builder.append("\n")
    }
    if (other == null) {
      println("right is null")
    } else {
      heap.foreach {
        case (addr, instance) =>
          if (!other.heap.contains(addr)) {
            println(s"addr $addr is only present in left")
          } else if (other.heap(addr) != instance) {
            println(
              s"different value for $addr: left = $instance, right = ${other.heap(addr)}")
          }
      }
      other.heap.foreach {
        case (addr, instance) =>
          if (!heap.contains(addr)) {
            println(s"addr $addr is only present in right")
          }
      }
      locals.foreach {
        case (id, value) =>
          if (!other.locals.contains(id)) {
            println(s"local ${id.show} is only present in left")
          } else if (other.locals(id) != value) {
            println(
              s"different value for local ${id.show}: left = ${value.show}, right = ${other.locals(id).show}")
          }
      }
      other.locals.foreach {
        case (id, value) =>
          if (!locals.contains(id)) {
            println(s"local ${id.show} is only present in right")
          }
      }
    }
    builder.toString
  }
}
