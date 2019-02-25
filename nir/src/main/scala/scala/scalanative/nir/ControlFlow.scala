package scala.scalanative
package nir

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
        case Inst.Let(_, call: Op.Call, unwind) if unwind ne Next.None =>
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
  }

  final class Graph(val entry: Block,
                    val all: Seq[Block],
                    val find: mutable.Map[Local, Block])

  object Graph {
    def apply(insts: Seq[Inst]): Graph = {
      assert(insts.nonEmpty)

      val locations = {
        val entries = mutable.Map.empty[Local, Int]
        var i       = 0

        insts.foreach { inst =>
          inst match {
            case inst: Inst.Label =>
              entries(inst.name) = i
            case _ =>
              ()
          }
          i += 1
        }

        entries
      }

      val blocks = mutable.Map.empty[Local, Block]
      val todo   = mutable.Stack.empty[Block]

      def edge(from: Block, to: Block, next: Next) = {
        val e = new Edge(from, to, next)
        from.outEdges += e
        to.inEdges += e
      }

      def block(local: Local): Block =
        blocks.get(local).getOrElse {
          val k                     = locations(local)
          val Inst.Label(n, params) = insts(k)

          // copy all instruction up until and including
          // first control-flow instruction after the label
          val body = mutable.UnrolledBuffer.empty[Inst]
          var i    = k
          do {
            i += 1
            body += insts(i)
          } while (!insts(i).isInstanceOf[Inst.Cf])

          val block = new Block(n, params, body, isEntry = k == 0)
          blocks(local) = block
          todo.push(block)
          block
        }

      def visit(node: Block): Unit = {
        val insts :+ cf = node.insts
        insts.foreach {
          case Inst.Let(_, op, unwind) if unwind ne Next.None =>
            edge(node, block(unwind.name), unwind)
          case _ =>
            ()
        }
        cf match {
          case _: Inst.Ret =>
            ()
          case Inst.Jump(next) =>
            edge(node, block(next.name), next)
          case Inst.If(_, next1, next2) =>
            edge(node, block(next1.name), next1)
            edge(node, block(next2.name), next2)
          case Inst.Switch(_, default, cases) =>
            edge(node, block(default.name), default)
            cases.foreach { case_ =>
              edge(node, block(case_.name), case_)
            }
          case Inst.Throw(_, next) =>
            if (next ne Next.None) {
              edge(node, block(next.name), next)
            }
          case Inst.Unreachable(next) =>
            if (next ne Next.None) {
              edge(node, block(next.name), next)
            }
          case inst =>
            unsupported(inst)
        }
      }

      val entry   = block(insts.head.asInstanceOf[Inst.Label].name)
      val visited = mutable.Set.empty[Local]

      while (todo.nonEmpty) {
        val block = todo.pop()
        val name  = block.name
        if (!visited(name)) {
          visited += name
          visit(block)
        }
      }

      val all = insts.collect {
        case Inst.Label(name, _) if visited.contains(name) =>
          blocks(name)
      }

      new Graph(entry, all, blocks)
    }
  }

  def removeDeadBlocks(insts: Seq[Inst]): Seq[Inst] = {
    val cfg = ControlFlow.Graph(insts)
    val buf = new nir.Buffer()(Fresh(insts))

    cfg.all.foreach { b =>
      buf += b.label
      buf ++= b.insts
    }

    buf.toSeq
  }
}
