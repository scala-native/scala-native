package salty.ir

import scala.collection.mutable

abstract class Reduction {
  def reduce: PartialFunction[Node, Reduction.Change]
}
object Reduction {
  sealed abstract class Change
  final case object NoChange extends Change
  final case class Replace(f: Slot => Node) extends Change
  object Replace { def all(n: Node) = Replace(_ => n) }

  def run(reduction: Reduction, entry: Node): Unit = {
    val epoch = Node.nextEpoch
    val stack = mutable.Stack(entry)
    while (stack.nonEmpty) {
      val node = stack.pop()
      if (node.epoch < epoch)
        reduction.reduce.applyOrElse(node, (_: Node) => NoChange) match {
          case NoChange =>
            node.epoch = epoch
            node.deps.foreach(stack.push)
          // TODO: split into Replace & ReplaceAll ?
          case Replace(f) =>
            node.uses.foreach { s =>
              val newnode = f(s)
              stack.push(newnode)
              s := newnode
            }
        }
    }
  }
}
