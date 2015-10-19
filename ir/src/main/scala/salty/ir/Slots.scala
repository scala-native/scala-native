package salty.ir

// TODO: compute schema dynamically
final class Slot private[ir] (
                val schema: Schema,
                val node:   Node,
                var next:   Node
) {
  def isVal: Boolean  = schema == Schema.Val
  def isCf: Boolean   = schema == Schema.Cf
  def isEf: Boolean   = schema == Schema.Ef
  def isRef: Boolean  = schema == Schema.Ref

  next._uses += this
  assert(next ne node)

  def isEmpty: Boolean = false
  def get: Node        = next

  def :=(value: Node): Unit = {
    assert(value ne node)
    next._uses -= this
    next = value
    next._uses += this
  }

  override def toString = s"Slot($next)"
}
object Slot {
  def unapply(slot: Slot): Slot = slot
  implicit def unwrap(slot: Slot): Node = slot.get
}

object Use {
  def unapply(slot: Slot): Some[Node] = Some(slot.node)
}

final class MultiSlot(val node: Node, val length: Int, val offset: Int) {
  def apply(index: Int): Slot = node._slots(offset + index)

  def nodes: Seq[Node] = {
    var i = offset
    Seq.fill(length) {
      val n = node._slots(i).get
      i += 1
      n
    }
  }
  def slots: Seq[Slot] = {
    var i = offset
    Seq.fill(length) {
      val slot = node._slots(i)
      i += 1
      slot
    }
  }
}
object MultiSlot {
  //def unapplySeq(mslot: MultiSlot): Seq[Slot] = mslot.slots
  //implicit def unwrap(mslot: MultiSlot): Seq[Slot] = mslot.toSeq
}


