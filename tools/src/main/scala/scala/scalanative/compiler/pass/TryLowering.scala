package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import compiler.analysis.ControlFlow
import util.unreachable
import nir._

/** Lowers try-catch with nested function calls into LLVM-style invokes. */
class TryLowering(implicit fresh: Fresh) extends Pass {
  private def transformCf(block: Block) = block.cf match {
    case Cf.Try(Next.Succ(n), fail) =>
      block.copy(cf = Cf.Jump(Next.Label(n, Seq())))
    case _ =>
      block
  }

  private def transformBlock(block: Block,
                             handler: Option[Next.Fail]): Seq[Block] =
    handler.fold(Seq(transformCf(block))) { fail =>
      val blocks = mutable.UnrolledBuffer.empty[Block]
      var name   = block.name
      var params = block.params
      var insts  = mutable.UnrolledBuffer.empty[Inst]

      def push(inst: Inst): Unit =
        insts += inst

      def invoke(n: Local, call: Op.Call): Unit = {
        val succ = Next.Succ(fresh())
        val inv  = Cf.Invoke(call.ty, call.ptr, call.args, succ, fail)
        blocks += Block(name, params, insts.toSeq, inv)
        name = succ.name
        params =
          if (call.resty != Type.Void) Seq(Val.Local(n, call.resty))
          else Seq()
        insts = mutable.UnrolledBuffer.empty[Inst]
      }

      def finish() = {
        blocks += transformCf(Block(name, params, insts.toSeq, block.cf))
        blocks.toSeq
      }

      block.insts.foreach {
        case Inst(n, call: Op.Call) =>
          invoke(n, call)
        case inst =>
          push(inst)
      }

      finish()
    }

  override def preDefn = {
    case defn @ Defn.Define(_, _, _, blocks) =>
      val cfg        = ControlFlow(blocks)
      val handlerfor = mutable.Map.empty[Local, Option[Next.Fail]]
      var curhandler: Option[Next.Fail] = None

      val nblocks = cfg.map { node =>
        val block = node.block

        handlerfor.get(block.name).foreach { h =>
          curhandler = h
        }

        block.cf match {
          case Cf.Try(succ, fail: Next.Fail) =>
            handlerfor(succ.name) = Some(fail)
            handlerfor(fail.name) = curhandler
          case _ =>
            ()
        }

        transformBlock(block, curhandler)
      }.flatten

      Seq(defn.copy(blocks = nblocks))
  }
}

object TryLowering extends PassCompanion {
  def apply(ctx: Ctx) = new TryLowering()(ctx.fresh)
}
