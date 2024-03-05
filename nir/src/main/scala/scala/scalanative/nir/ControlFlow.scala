package scala.scalanative
package nir

import scala.collection.mutable
import util.unsupported

/** Analysis that's used to answer following questions:
 *
 *  * What are the predecessors of given block?
 *
 *  * What are the successors of given block?
 */
private[scalanative] object ControlFlow {
  final case class Edge(from: Block, to: Block, next: Next)

  final case class Block(
      id: Local,
      params: Seq[Val.Local],
      insts: Seq[Inst],
      isEntry: Boolean
  )(implicit val pos: SourcePosition) {
    val inEdges = mutable.UnrolledBuffer.empty[Edge]
    val outEdges = mutable.UnrolledBuffer.empty[Edge]

    lazy val splitCount: Int = {
      var count = 0
      insts.foreach {
        case Inst.Let(_, _: Op.Call, unwind) if unwind ne Next.None =>
          count += 1
        case _ =>
          ()
      }
      count
    }

    def pred = inEdges.map(_.from)
    def succ = outEdges.map(_.to)
    def label = Inst.Label(id, params)
    def show = id.show
  }

  final class Graph(
      val entry: Block,
      val all: Seq[Block],
      val find: mutable.Map[Local, Block]
  )

  object Graph {
    def apply(insts: Seq[Inst]): Graph = {
      assert(insts.nonEmpty)

      val locations = {
        val entries = mutable.Map.empty[Local, Int]
        var i = 0

        insts.foreach { inst =>
          inst match {
            case inst: Inst.Label =>
              entries(inst.id) = i
            case _ =>
              ()
          }
          i += 1
        }

        entries
      }

      val blocks = mutable.Map.empty[Local, Block]
      var todo = List.empty[Block]

      def edge(from: Block, to: Block, next: Next) = {
        val e = Edge(from, to, next)
        from.outEdges += e
        to.inEdges += e
      }

      def block(local: Local)(implicit pos: SourcePosition): Block =
        blocks.getOrElse(
          local, {
            val k = locations(local)
            val Inst.Label(n, params) = insts(k): @unchecked

            // copy all instruction up until and including
            // first control-flow instruction after the label
            val body = mutable.UnrolledBuffer.empty[Inst]
            var i = k
            while ({
              i += 1
              body += insts(i)
              !insts(i).isInstanceOf[Inst.Cf]
            }) ()

            val block = Block(n, params, body.toSeq, isEntry = k == 0)
            blocks(local) = block
            todo ::= block
            block
          }
        )

      def visit(node: Block): Unit = {
        val insts :+ cf = node.insts: @unchecked
        insts.foreach {
          case inst @ Inst.Let(_, op, unwind) if unwind ne Next.None =>
            edge(node, block(unwind.id)(inst.pos), unwind)
          case _ =>
            ()
        }
        implicit val pos: SourcePosition = cf.pos

        cf match {
          case _: Inst.Ret =>
            ()
          case Inst.Jump(next) =>
            edge(node, block(next.id), next)
          case Inst.If(_, next1, next2) =>
            edge(node, block(next1.id), next1)
            edge(node, block(next2.id), next2)
          case Inst.LinktimeIf(_, next1, next2) =>
            edge(node, block(next1.id), next1)
            edge(node, block(next2.id), next2)
          case Inst.Switch(_, default, cases) =>
            edge(node, block(default.id), default)
            cases.foreach { case_ => edge(node, block(case_.id), case_) }
          case Inst.Throw(_, next) =>
            if (next ne Next.None) {
              edge(node, block(next.id), next)
            }
          case Inst.Unreachable(next) =>
            if (next ne Next.None) {
              edge(node, block(next.id), next)
            }
          case inst =>
            unsupported(inst)
        }
      }

      val entryInst = insts.head.asInstanceOf[Inst.Label]
      val entry = block(entryInst.id)(entryInst.pos)
      val visited = mutable.Set.empty[Local]

      while (todo.nonEmpty) {
        val block = todo.head
        todo = todo.tail
        val id = block.id
        if (!visited(id)) {
          visited += id
          visit(block)
        }
      }

      val all = insts.collect {
        case Inst.Label(id, _) if visited.contains(id) =>
          blocks(id)
      }

      new Graph(entry, all, blocks)
    }
  }

  def removeDeadBlocks(insts: Seq[Inst]): Seq[Inst] = {
    val cfg = ControlFlow.Graph(insts)
    val buf = new nir.InstructionBuilder()(Fresh(insts))

    cfg.all.foreach { b =>
      buf += b.label
      buf ++= b.insts
    }

    buf.toSeq
  }
}
