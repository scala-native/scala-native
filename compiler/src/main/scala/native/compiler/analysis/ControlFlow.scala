package native
package compiler
package analysis

import scala.collection.mutable
import native.util.unreachable
import native.nir._

/** Analysis that's used to answer following questions:
 *
 *  * What are the predecessors of given block?
 *
 *  * What are the successors of given block?
 */
object ControlFlow {
  final class Edge(val from: Node, val to: Node, val next: Next)
  final class Node(val block: Block, var pred: Seq[Edge], var succ: Seq[Edge])
  type Result = Map[Local, Node]

  def apply(blocks: Seq[Block]): Result = {
    val nodes = mutable.Map.empty[Local, Node]
    def edge(from: Node, to: Node, next: Next) ={
      val e = new Edge(from, to, next)
      from.succ = from.succ :+ e
      to.pred = to.pred :+ e
    }

    blocks.foreach { b =>
      nodes(b.name) = new Node(b, Seq(), Seq())
    }
    blocks.foreach {
      case Block(n, _, _, cf) =>
        val node = nodes(n)
        cf match {
          case Cf.Unreachable =>
            ()
          case Cf.Ret(_) =>
            ()
          case Cf.Jump(next) =>
            edge(node, nodes(next.name), next)
          case Cf.If(_, next1, next2) =>
            edge(node, nodes(next1.name), next1)
            edge(node, nodes(next2.name), next2)
          case Cf.Switch(_, default, cases) =>
            edge(node, nodes(default.name), default)
            cases.foreach { case_ =>
              edge(node, nodes(case_.name), case_)
            }
          case Cf.Invoke(_, _, _, succ, fail) =>
            edge(node, nodes(succ.name), succ)
            edge(node, nodes(fail.name), succ)
          case Cf.Throw(_) =>
            ()
          case Cf.Try(next1, next2) =>
            edge(node, nodes(next1.name), next1)
            edge(node, nodes(next2.name), next2)
          case _ =>
            unreachable
        }
    }

    nodes.toMap
  }
}
