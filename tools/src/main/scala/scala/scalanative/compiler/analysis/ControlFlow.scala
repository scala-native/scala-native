package scala.scalanative
package compiler
package analysis

import scala.collection.mutable
import util.unreachable
import nir._

/** Analysis that's used to answer following questions:
 *
 *  * What are the predecessors of given block?
 *
 *  * What are the successors of given block?
 */
object ControlFlow {
  final case class Edge(val from: Node, val to: Node, val next: Next)
  final class Node(val block: Block, var pred: Seq[Edge], var succ: Seq[Edge])
  final class Graph(val entry: Node, val nodes: Map[Local, Node]) {
    def map[T: reflect.ClassTag](f: Node => T): Seq[T] = {
      val visited  = mutable.Set.empty[Node]
      val worklist = mutable.Stack.empty[Node]
      val result   = mutable.UnrolledBuffer.empty[T]

      worklist.push(entry)
      while (worklist.nonEmpty) {
        val node = worklist.pop()
        if (!visited.contains(node)) {
          visited += node
          node.succ.foreach(e => worklist.push(e.to))
          result += f(node)
        }
      }

      result.toSeq
    }
  }

  def apply(blocks: Seq[Block]): Graph = {
    val nodes = mutable.Map.empty[Local, Node]
    def edge(from: Node, to: Node, next: Next) = {
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
          case Cf.Unreachable | _: Cf.Ret | _: Cf.Resume | _: Cf.Throw =>
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
            edge(node, nodes(fail.name), fail)
          case Cf.Try(next1, next2) =>
            edge(node, nodes(next1.name), next1)
            edge(node, nodes(next2.name), next2)
          case _ =>
            unreachable
        }
    }

    new Graph(nodes(blocks.head.name), nodes.toMap)
  }
}
