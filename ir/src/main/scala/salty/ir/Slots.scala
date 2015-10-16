package salty.ir

// TODO: compute schema dynamically
final class Slot private[ir] (
                val schema: Schema,
                val node:   Node,
  private[this] var next:   Node
) {
  def isVal: Boolean  = schema == Schema.Val
  def isCf: Boolean   = schema == Schema.Cf
  def isEf: Boolean   = schema == Schema.Ef
  def isRef: Boolean  = schema == Schema.Ref

  next.uses += this

  def isEmpty: Boolean = false
  def get: Node        = next

  def :=(value: Node): Unit = {
    next.uses -= this
    next = value
    next.uses += this
  }
}
object Slot {
  def unapply(slot: Slot): Slot = slot
  implicit def unwrap(slot: Slot): Node = slot.get
}

final class MultiSlot(val node: Node, val length: Int, val offset: Int) {
  def apply(index: Int): Slot = node.slots(offset + index)

  def toSeq: Seq[Slot] = {
    var i = offset
    Seq.fill(length) {
      val slot = node.slots(i)
      i += 1
      slot
    }
  }
}
object MultiSlot {
  def unapplySeq(mslot: MultiSlot): Seq[Slot] = mslot
  implicit def unwrap1(mslot: MultiSlot): Seq[Slot] = mslot.toSeq
  implicit def unwrap2(mslot: MultiSlot): Seq[Node] = mslot.toSeq.map(_.get)
}


