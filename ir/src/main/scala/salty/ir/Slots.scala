package salty.ir

final class Slot private[ir] (
                val node:   Node,
  private[this] var next:   Node
) {
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


