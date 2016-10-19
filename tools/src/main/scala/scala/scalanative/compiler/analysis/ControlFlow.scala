package scala.scalanative
package compiler
package analysis

import scala.collection.mutable
import util.unsupported
import nir._

/** Analysis that's used to answer following questions:
 *
 *  * What are the predecessors of given block?
 *
 *  * What are the successors of given block?
 */
object ControlFlow {
  final case class Edge(val from: Block, val to: Block, val next: Next)

  final case class Block(name: Local, params: Seq[Val.Local], insts: Seq[Inst]) {
    val pred = mutable.UnrolledBuffer.empty[Edge]
    val succ = mutable.UnrolledBuffer.empty[Edge]
    def label = Inst.Label(name, params)
  }

  final class Graph(val entry: Block,
                    val all: Seq[Block],
                    val find: Map[Local, Block]) {
    def foreach(f: Block => Unit): Unit = {
      val visited  = mutable.Set.empty[Block]
      val worklist = mutable.Stack.empty[Block]

      worklist.push(entry)
      while (worklist.nonEmpty) {
        val node = worklist.pop()
        if (!visited.contains(node)) {
          visited += node
          node.succ.foreach(e => worklist.push(e.to))
          f(node)
        }
      }
    }

    def map[T: reflect.ClassTag](f: Block => T): Seq[T] = {
      val result = mutable.UnrolledBuffer.empty[T]

      foreach { block =>
        result += f(block)
      }

      result
    }

    lazy val eh: Map[Local, Option[Local]] = {
      val handlers = mutable.Map.empty[Local, Option[Local]]
      var current: Option[Local] = None

      foreach { block =>
        handlers.get(block.name).foreach { handler =>
          current = handler
        }

        block.insts.last match {
          case Inst.Try(succ, fail: Next.Fail) =>
            handlers(succ.name) = Some(fail.name)
            handlers(fail.name) = current
          case _ =>
            ()
        }

        handlers(block.name) = current
      }

      handlers.toMap
    }
  }

  object Graph {
    def apply(insts: Seq[Inst]): Graph = {
      assert(insts.nonEmpty)

      def edge(from: Block, to: Block, next: Next) = {
        val e = new Edge(from, to, next)
        from.succ += e
        to.pred += e
      }

      val blocks: Seq[Block] = insts.zipWithIndex.collect {
        case (Inst.Label(n, params), k) =>
          // copy all instruction up until and including
          // first control-flow instruction after the label
          val body = mutable.UnrolledBuffer.empty[Inst]
          var i = k
          do {
            i += 1
            body += insts(i)
          } while (!insts(i).isInstanceOf[Inst.Cf])
          new Block(n, params, body)
      }

      val nodes = blocks.map { b =>
        b.name -> b
      }.toMap

      blocks.foreach {
        case node @ Block(n, _, _ :+ cf) =>
          cf match {
            case Inst.Unreachable | _: Inst.Ret | _: Inst.Throw =>
              ()
            case Inst.Jump(next) =>
              edge(node, nodes(next.name), next)
            case Inst.If(_, next1, next2) =>
              edge(node, nodes(next1.name), next1)
              edge(node, nodes(next2.name), next2)
            case Inst.Switch(_, default, cases) =>
              edge(node, nodes(default.name), default)
              cases.foreach { case_ =>
                edge(node, nodes(case_.name), case_)
              }
            case Inst.Invoke(_, _, _, succ, fail) =>
              edge(node, nodes(succ.name), succ)
              edge(node, nodes(fail.name), fail)
            case Inst.Try(next1, next2) =>
              edge(node, nodes(next1.name), next1)
              edge(node, nodes(next2.name), next2)
            case inst =>
              unsupported(inst)
          }
      }

      new Graph(nodes(blocks.head.name), blocks, nodes)
    }
  }
}
