package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import analysis.ClassHierarchy.Top
import nir._, Shows._
import util.sh

/** Eliminates pure computations that are not being used. */
class DeadCodeElimination(implicit top: Top) extends Pass {
  override def preDefn = {
    case defn: Defn.Define =>
      val insts    = defn.insts
      val cfg      = analysis.ControlFlow.Graph(insts)
      val usedef   = analysis.UseDef(cfg)
      val newinsts = mutable.UnrolledBuffer.empty[Inst]

      cfg.all.foreach { block =>
        if (usedef(block.name).alive) {
          newinsts += block.label
          block.insts.foreach {
            case inst @ Inst.Let(n, op) =>
              if (usedef(n).alive) newinsts += inst
            case inst: Inst.Cf =>
              newinsts += inst
            case _ =>
              ()
          }
        }
      }

      Seq(defn.copy(insts = newinsts))
  }
}

object DeadCodeElimination extends PassCompanion {
  def apply(ctx: Ctx) = new DeadCodeElimination()(ctx.top)
}
