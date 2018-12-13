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
    val fresh  = Fresh(insts)
    val cfg    = ControlFlow.Graph(insts)
    val usedef = UseDef(cfg)
    val buf    = new nir.Buffer()(fresh)

    cfg.all.foreach { block =>
      if (usedef(block.name).alive) {
        buf += block.label
        block.insts.foreach {
          case inst @ Inst.Let(n, _, _) =>
            if (usedef(n).alive) buf += inst
          case inst: Inst.Cf =>
            buf += inst
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
}
