package native
package gir

import scala.collection.mutable

abstract class Reduction {
  def reduce: PartialFunction[Node, Reduction.Change]
}
object Reduction {
  def replace(f: Use => Node): Change       = Replace(f)
  def replaceAll(node: Node): Change        = Replace(_ => node)
  def after(dep: Dep)(f: => Change): Change = After(dep, () => f)

  // TODO: separate ReplaceAll node
  sealed abstract class Change
  final case object NoChange extends Change
  final case class Replace(f: Use => Node) extends Change
  final case class After(dep: Dep, f: () => Change) extends Change

  sealed abstract class Action
  final case class Visit(node: Node) extends Action
  final case class Revisit(node: Node, f: () => Change) extends Action

  def run(reduction: Reduction, entry: Node): Unit = try {
    val epoch = Node.nextEpoch
    val stack = mutable.Stack[Action](Visit(entry))
    def handle(node: Node, change: Change): Unit = change match {
      case NoChange =>
        node._epoch = epoch
        node.deps.foreach(d => stack.push(Visit(d.dep)))
      case Replace(f) =>
        node._uses.toList.foreach { slot =>
          val newnode = f(slot)
          stack.push(Visit(newnode))
          slot := newnode
        }
        assert(node._uses.isEmpty)
        node._desc = Desc.Dead
        node._epoch = epoch
      case after: After =>
        val dep = after.dep.dep
        if (dep._epoch == epoch) {
          handle(node, after.f())
        } else {
          stack.push(Revisit(node, after.f))
          stack.push(Visit(dep))
        }
    }
    while (stack.nonEmpty) {
      stack.pop() match {
        case Visit(node) =>
          if (node._epoch < epoch)
            handle(node, reduction.reduce.applyOrElse(node, (_: Node) => NoChange))
        case Revisit(node, f) =>
          handle(node, f())
      }
    }
  } catch { case e: Exception =>
    e.printStackTrace()
  }
}
