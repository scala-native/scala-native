package scala.scalanative
package interflow

import scalanative.nir._
import scalanative.linker._

object InstanceRef {
  def unapply(addr: Addr)(implicit state: State): Option[Type] =
    unapply(Val.Virtual(addr))
  def unapply(value: Val)(implicit state: State): Option[Type] = value match {
    case Val.Virtual(addr) =>
      Some(state.deref(addr).ty)
    case _ =>
      None
  }
}

object VirtualRef {
  def unapply(addr: Addr)(
      implicit state: State): Option[(Kind, Class, Array[Val])] =
    unapply(Val.Virtual(addr))
  def unapply(value: Val)(
      implicit state: State): Option[(Kind, Class, Array[Val])] = value match {
    case Val.Virtual(addr) =>
      state.deref(addr) match {
        case VirtualInstance(kind, cls, values) =>
          Some((kind, cls, values))
        case _ =>
          None
      }
    case _ =>
      None
  }
}

object DelayedRef {
  def unapply(addr: Addr)(implicit state: State): Option[Op] =
    unapply(Val.Virtual(addr))
  def unapply(value: Val)(implicit state: State): Option[Op] = value match {
    case Val.Virtual(addr) =>
      state.deref(addr) match {
        case DelayedInstance(op) =>
          Some(op)
        case _ =>
          None
      }
    case _ =>
      None
  }
}

object BinRef {
  def unapply(addr: Addr)(implicit state: State): Option[(Bin, Val, Val)] =
    unapply(Val.Virtual(addr))
  def unapply(value: Val)(implicit state: State): Option[(Bin, Val, Val)] =
    value match {
      case Val.Virtual(addr) =>
        state.deref(addr) match {
          case DelayedInstance(Op.Bin(bin, _, l, r)) =>
            Some((bin, l, r))
          case _ =>
            None
        }
      case _ =>
        None
    }
}

object ConvRef {
  def unapply(addr: Addr)(implicit state: State): Option[(Conv, Type, Val)] =
    unapply(Val.Virtual(addr))
  def unapply(value: Val)(implicit state: State): Option[(Conv, Type, Val)] =
    value match {
      case Val.Virtual(addr) =>
        state.deref(addr) match {
          case DelayedInstance(Op.Conv(conv, ty, v)) =>
            Some((conv, ty, v))
          case _ =>
            None
        }
      case _ =>
        None
    }
}

object CompRef {
  def unapply(addr: Addr)(
      implicit state: State): Option[(Comp, Type, Val, Val)] =
    unapply(Val.Virtual(addr))
  def unapply(value: Val)(
      implicit state: State): Option[(Comp, Type, Val, Val)] =
    value match {
      case Val.Virtual(addr) =>
        state.deref(addr) match {
          case DelayedInstance(Op.Comp(comp, ty, v1, v2)) =>
            Some((comp, ty, v1, v2))
          case _ =>
            None
        }
      case _ =>
        None
    }
}

object EscapedRef {
  def unapply(addr: Addr)(implicit state: State): Option[Val] =
    unapply(Val.Virtual(addr))
  def unapply(value: Val)(implicit state: State): Option[Val] = value match {
    case Val.Virtual(addr) =>
      state.deref(addr) match {
        case EscapedInstance(value) =>
          Some(value)
        case _ =>
          None
      }
    case _ =>
      None
  }
}
