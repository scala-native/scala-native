package scala.scalanative
package interflow

import scalanative.linker.*

private[interflow] object InstanceRef {

  def unapply(addr: Addr)(implicit state: State): Option[nir.Type] =
    unapply(nir.Val.Virtual(addr))

  def unapply(value: nir.Val)(implicit state: State): Option[nir.Type] =
    value match {
      case nir.Val.Virtual(addr) =>
        Some(state.deref(addr).ty)
      case _ =>
        None
    }

}

private[interflow] object VirtualRef {

  type Extract = (Kind, Class, Array[nir.Val])

  def unapply(addr: Addr)(implicit
      state: State
  ): Option[Extract] =
    unapply(nir.Val.Virtual(addr))

  def unapply(
      value: nir.Val
  )(implicit state: State): Option[Extract] = value match {
    case nir.Val.Virtual(addr) =>
      state.deref(addr) match {
        case VirtualInstance(kind, cls, values, _) =>
          Some((kind, cls, values))
        case _ =>
          None
      }
    case _ =>
      None
  }

}

private[interflow] object DelayedRef {

  def unapply(addr: Addr)(implicit state: State): Option[nir.Op] =
    unapply(nir.Val.Virtual(addr))

  def unapply(value: nir.Val)(implicit state: State): Option[nir.Op] =
    value match {
      case nir.Val.Virtual(addr) =>
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

private[interflow] object BinRef {

  type Extract = (nir.Bin, nir.Val, nir.Val)

  def unapply(addr: Addr)(implicit state: State): Option[Extract] =
    unapply(nir.Val.Virtual(addr))

  def unapply(value: nir.Val)(implicit state: State): Option[Extract] =
    value match {
      case nir.Val.Virtual(addr) =>
        state.deref(addr) match {
          case DelayedInstance(nir.Op.Bin(bin, _, l, r)) =>
            Some((bin, l, r))
          case _ =>
            None
        }
      case _ =>
        None
    }

}

private[interflow] object ConvRef {

  type Extract = (nir.Conv, nir.Type, nir.Val)

  def unapply(addr: Addr)(implicit state: State): Option[Extract] =
    unapply(nir.Val.Virtual(addr))

  def unapply(value: nir.Val)(implicit state: State): Option[Extract] =
    value match {
      case nir.Val.Virtual(addr) =>
        state.deref(addr) match {
          case DelayedInstance(nir.Op.Conv(conv, ty, v)) =>
            Some((conv, ty, v))
          case _ =>
            None
        }
      case _ =>
        None
    }

}

private[interflow] object CompRef {

  type Extract = (nir.Comp, nir.Type, nir.Val, nir.Val)

  def unapply(addr: Addr)(implicit
      state: State
  ): Option[Extract] =
    unapply(nir.Val.Virtual(addr))

  def unapply(
      value: nir.Val
  )(implicit state: State): Option[Extract] =
    value match {
      case nir.Val.Virtual(addr) =>
        state.deref(addr) match {
          case DelayedInstance(nir.Op.Comp(comp, ty, v1, v2)) =>
            Some((comp, ty, v1, v2))
          case _ =>
            None
        }
      case _ =>
        None
    }

}

private[interflow] object EscapedRef {

  def unapply(addr: Addr)(implicit state: State): Option[nir.Val] =
    unapply(nir.Val.Virtual(addr))

  def unapply(value: nir.Val)(implicit state: State): Option[nir.Val] =
    value match {
      case nir.Val.Virtual(addr) =>
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
