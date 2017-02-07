package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy.Top
import analysis.ControlFlow
import analysis.ControlFlow.Block

import nir._
import Inst._
import scala.collection.mutable

class DeadBlockElimination extends Pass {
  override def onInsts(insts: Seq[Inst]) = {
    val cfg = ControlFlow.Graph(insts)
    val buf = mutable.UnrolledBuffer.empty[Inst]

    cfg.foreach { b =>
      buf += b.label
      buf ++= b.insts
    }

    buf
  }
}

object DeadBlockElimination extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new DeadBlockElimination()
}
