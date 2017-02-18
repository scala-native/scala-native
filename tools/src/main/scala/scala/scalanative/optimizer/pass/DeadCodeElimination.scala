package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import analysis.ClassHierarchy.Top
import analysis.UseDef
import analysis.ControlFlow
import nir._

/** Eliminates pure computations that are not being used, as well as unused block parameters. */
class DeadCodeElimination(implicit top: Top) extends Pass {
  import DeadCodeElimination._

  override def onInsts(insts: Seq[Inst]): Seq[Inst] = {
    val cfg        = ControlFlow.Graph(insts)
    val usedef     = UseDef(cfg)
    val removeArgs = new ArgRemover(usedef, cfg.entry.name)
    val buf        = new nir.Buffer

    cfg.all.foreach { block =>
      if (usedef(block.name).alive) {
        val newParams = block.params.filter { p =>
          (block.name == cfg.entry.name) || usedef(p.name).alive
        }
        buf += block.label.copy(params = newParams)
        block.insts.foreach {
          case inst @ Inst.Let(n, op) =>
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
  override def apply(config: tools.Config, top: Top) =
    new DeadCodeElimination()(top)

  class ArgRemover(usedef: Map[Local, UseDef.Def], entryName: Local)
      extends Pass {
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
