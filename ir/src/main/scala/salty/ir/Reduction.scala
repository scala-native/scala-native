package salty.ir

import scala.collection.mutable

abstract class Reduction {
  def reduce: PartialFunction[Node, Reduction.Change]
}
object Reduction {
  sealed abstract class Change
  final case object NoChange extends Change
  // TODO: split into Replace & ReplaceAll ?
  final case class Replace(f: Slot => Node) extends Change
  final case class After(slot: Slot)(val f: Node => Change) extends Change
  object Replace { def all(n: Node) = Replace(_ => n) }

  def alive(node: Node): Boolean = node.desc match {
    case Desc.Dead => false
    case _         => node.deps.forall(alive)
  }

  sealed abstract class Action
  final case class Visit(node: Node) extends Action
  final case class Revisit(node: Node, after: After) extends Action

  def run(reduction: Reduction, entry: Node): Unit = {
    val epoch = Node.nextEpoch
    val stack = mutable.Stack[Action](Visit(entry))
    def handle(node: Node, change: Change): Unit = change match {
      case NoChange =>
        node._epoch = epoch
        node.deps.foreach(n => stack.push(Visit(n)))
      case Replace(f) =>
        node.uses.foreach { s =>
          val newnode = f(s)
          stack.push(Visit(newnode))
          s := newnode
        }
        node._desc = Desc.Dead
        node._epoch = epoch
      case after: After =>
        if (after.slot.get._epoch == epoch) {
          handle(node, after.f(after.slot.get))
        } else {
          stack.push(Revisit(node, after))
          stack.push(Visit(after.slot.get))
        }
    }
    while (stack.nonEmpty) {
      stack.pop() match {
        case Visit(node) =>
          if (node._epoch < epoch)
            handle(node, reduction.reduce.applyOrElse(node, (_: Node) => NoChange))
        case Revisit(node, after) =>
          assert(after.slot.get._epoch == epoch)
          handle(node, after.f(after.slot.get))
      }
    }
  }
}
