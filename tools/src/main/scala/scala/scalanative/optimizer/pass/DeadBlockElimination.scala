package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import nir._, Inst._
import sema._, ControlFlow.Block

class DeadBlockElimination extends Pass {
  override def onInsts(insts: Seq[Inst]) = {
    val cfg = ControlFlow.Graph(insts)
    val buf = new nir.Buffer()(Fresh(insts))

    cfg.all.foreach { b =>
      buf += b.label
      buf ++= b.insts
    }

    buf.toSeq
  }
}

object DeadBlockElimination extends PassCompanion {
  override def apply(config: build.Config, linked: linker.Result) =
    new DeadBlockElimination()
}
