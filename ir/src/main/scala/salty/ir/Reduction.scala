package salty.ir

import scala.collection.mutable

abstract class Reduction {
  def reduce: PartialFunction[Node, Reduction.Change]
}
object Reduction {
  sealed abstract class Change
  final case object NoChange extends Change
  final case class Replace(node: Node) extends Change

  def run(reduction: Reduction, entry: Node): Unit = {
    val epoch = Node.nextEpoch
    val stack = mutable.Stack(entry)
    while (stack.nonEmpty) {
      val node = stack.pop()
      node.deps.foreach { n =>
        if (n.epoch < epoch) stack.push(n)
      }
      reduction.reduce.applyOrElse(node, (_: Node) => NoChange) match {
        case NoChange =>
          ()
        case Replace(newnode) =>
          node.uses.foreach(_ := newnode)
      }
      node.epoch = epoch
    }
  }
}
