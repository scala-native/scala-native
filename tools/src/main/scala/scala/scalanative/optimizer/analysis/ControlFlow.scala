package scala.scalanative
package optimizer
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

  final case class Block(name: Local,
                         params: Seq[Val.Local],
                         insts: Seq[Inst],
                         isEntry: Boolean) {
    val inEdges  = mutable.UnrolledBuffer.empty[Edge]
    val outEdges = mutable.UnrolledBuffer.empty[Edge]

    lazy val splitCount: Int = {
      var count = 0
      insts.foreach {
        case Inst.Let(_, call: Op.Call) if call.unwind ne Next.None =>
          count += 1
        case _ =>
          ()
      }
      count
    }

    def pred  = inEdges.map(_.from)
    def succ  = outEdges.map(_.to)
    def label = Inst.Label(name, params)
    def show  = name.show

    def isRegular: Boolean =
      inEdges.forall {
        case Edge(_, _, _: Next.Case)  => true
        case Edge(_, _, _: Next.Label) => true
        case _                         => false
      }

    def isExceptionHandler: Boolean =
      inEdges.forall {
        case Edge(_, _, _: Next.Unwind) => true
        case _                          => false
      }
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
          node.outEdges.foreach(e => worklist.push(e.to))
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
  }

  object Graph {
    def apply(insts: Seq[Inst]): Graph = {
      assert(insts.nonEmpty)

      def edge(from: Block, to: Block, next: Next) = {
        val e = new Edge(from, to, next)
        from.outEdges += e
        to.inEdges += e
      }

      val blocks: Seq[Block] = insts.zipWithIndex.collect {
        case (Inst.Label(n, params), k) =>
          // copy all instruction up until and including
          // first control-flow instruction after the label
          val body = mutable.UnrolledBuffer.empty[Inst]
          var i    = k
          do {
            i += 1
            body += insts(i)
          } while (!insts(i).isInstanceOf[Inst.Cf])
          new Block(n, params, body, isEntry = k == 0)
      }

      val nodes = blocks.map { b =>
        b.name -> b
      }.toMap

      blocks.foreach {
        case node @ Block(n, _, insts :+ cf, _) =>
          insts.foreach {
            case Inst.Let(_, op: Op.Unwind) if op.unwind ne Next.None =>
              edge(node, nodes(op.unwind.name), op.unwind)
            case _ =>
              ()
          }
          cf match {
            case Inst.Unreachable | _: Inst.Ret =>
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
            case Inst.Throw(_, next) =>
              if (next ne Next.None) {
                edge(node, nodes(next.name), next)
              }
            case inst =>
              unsupported(inst)
          }
      }

      new Graph(nodes(blocks.head.name), blocks, nodes)
    }
  }
}
