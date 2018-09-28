package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import nir._
import sema._

/** Eliminates pure computations that are not being used, as well as unused block parameters. */
class DeadCodeElimination extends Pass {
  import DeadCodeElimination._

  override def onInsts(insts: Seq[Inst]): Seq[Inst] = {
    val fresh      = Fresh(insts)
    val cfg        = ControlFlow.Graph(insts)
    val usedef     = UseDef(cfg)
    val removeArgs = new ArgRemover(usedef, cfg.entry.name)
    val buf        = new nir.Buffer()(fresh)

    cfg.all.foreach { block =>
      if (usedef(block.name).alive) {
        val newParams = block.params.filter { p =>
          (block.name == cfg.entry.name) || usedef(p.name).alive
        }
        buf += block.label.copy(params = newParams)
        block.insts.foreach {
          case inst @ Inst.Let(n, _, _) =>
            if (usedef(n).alive) buf += inst
          case inst: Inst.Cf =>
            buf += removeArgs.onInst(inst)
          case _ =>
            ()
        }
      }
    }

    buf.toSeq
  }
}

object DeadCodeElimination extends PassCompanion {
  def apply(insts: Seq[Inst]): Seq[Inst] =
    (new DeadCodeElimination()).onInsts(insts)

  override def apply(config: build.Config, linked: linker.Result): Pass =
    new DeadCodeElimination()

  class ArgRemover(usedef: Map[Local, UseDef.Def], entryName: Local)
      extends Transform {
    override def onNext(next: Next) = next match {
      case Next.Label(name, args) if (name != entryName) =>
        usedef(name) match {
          case UseDef.BlockDef(_, _, _, params) =>
            val newArgs = args.zip(params).collect {
              case (arg, param) if param.alive => arg
            }
            Next.Label(name, newArgs)

          case _ =>
            throw new IllegalStateException(
              s"Expected a BlockDef in usedef for ${name.show}")
        }

      case _ =>
        next
    }
  }
}
