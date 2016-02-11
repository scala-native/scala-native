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
object CFG {
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
      case Block(n, _, _ :+ Instr(_, _, op)) =>
        val node = nodes(n)
        op match {
          case Op.Unreachable =>
            ()
          case Op.Ret(_) =>
            ()
          case Op.Throw(_) =>
            ()
          case Op.Jump(Next(n, args)) =>
            edge(node, nodes(n), args)
          case Op.If(_, Next(n1, args1), Next(n2, args2)) =>
            edge(node, nodes(n1), args1)
            edge(node, nodes(n2), args2)
          case Op.Switch(_, default, cases) =>
            cases.map {
              case Case(_, Next(n, args)) =>
                edge(node, nodes(n), args)
            }
            edge(node, nodes(default.name), default.args)
          case Op.Invoke(_, _, _, succ, fail) =>
            edge(node, nodes(succ.name), succ.args)
            edge(node, nodes(fail.name), succ.args)
          case _ =>
            unreachable
        }
    }

    nodes.toMap
  }
}
