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
      if (node.epoch < epoch)
        reduction.reduce.applyOrElse(node, (_: Node) => NoChange) match {
          case NoChange =>
            println(s"no change $node")
            node.epoch = epoch
            node.deps.foreach(stack.push)
          case Replace(newnode) =>
            println(s"replace $node with $newnode")
            node.uses.foreach(_ := newnode)
            stack.push(newnode)
        }
    }
  }
}
