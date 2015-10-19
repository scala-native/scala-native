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
  final case class After(node: Node)(val f: Node => Change) extends Change
  object Replace { def all(n: Node) = Replace(_ => n) }

  def alive(node: Node): Boolean = node.desc match {
    case Desc.Dead => false
    case _         => node.deps.forall(alive)
  }

  def run(reduction: Reduction, entry: Node): Unit = {
    println(s"--- running $reduction")
    val epoch = Node.nextEpoch
    val stack = mutable.Stack(entry)
    def handle(node: Node, change: Change): Unit = change match {
      case NoChange =>
        println(s"no change for ${node.desc} ${node.name}")
        node._epoch = epoch
        node.deps.foreach(stack.push)
      case Replace(f) =>
        println(s"replacing ${node.desc} ${node.name}")
        node.uses.foreach { s =>
          val newnode = f(s)
          stack.push(newnode)
          s := newnode
        }
        node.desc = Desc.Dead
      case after: After =>
        if (after.node._epoch == epoch)
          handle(node, after.f(after.node))
        else {
          println(s"putting off ${node.desc} ${node.name} until ${after.node.desc} ${after.node.name} is done")
          stack.push(node)
          stack.push(after.node)
        }
    }
    while (stack.nonEmpty) {
      val node = stack.pop()
      if (node._epoch < epoch)
        handle(node, reduction.reduce.applyOrElse(node, (_: Node) => NoChange))
    }
  }
}
