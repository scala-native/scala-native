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
  final class Edge(val from: Node, val to: Node, val values: Seq[Val])
  final class Node(val block: Block, var pred: Seq[Edge], var succ: Seq[Edge])
  type Result = Map[Local, Node]

  def apply(blocks: Seq[Block]): Result = {
    val nodes = mutable.Map.empty[Local, Node]
    def edge(from: Node, to: Node, params: Seq[Val]) ={
      val e = new Edge(from, to, params)
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
          case Cf.Jump(Next(n, args)) =>
            edge(node, nodes(n), args)
          case Cf.If(_, Next(n1, args1), Next(n2, args2)) =>
            edge(node, nodes(n1), args1)
            edge(node, nodes(n2), args2)
          case Cf.Switch(_, default, cases) =>
            cases.map {
              case Case(_, Next(n, args)) =>
                edge(node, nodes(n), args)
            }
            edge(node, nodes(default.name), default.args)
          case Cf.Invoke(_, _, _, succ, fail) =>
            edge(node, nodes(succ.name), succ.args)
            edge(node, nodes(fail.name), succ.args)
          case Cf.Throw(_) =>
            ()
          case Cf.Try(Next(n1, args1), Next(n2, args2)) =>
            edge(node, nodes(n1), args1)
            edge(node, nodes(n2), args2)
          case _ =>
            unreachable
        }
    }

    nodes.toMap
  }
}
