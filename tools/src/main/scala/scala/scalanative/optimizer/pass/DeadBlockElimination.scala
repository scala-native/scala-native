package scala.scalanative
package optimizer
package pass

import scalanative.nir.{Inst, ControlFlow}

class DeadBlockElimination extends Pass {
  override def onInsts(insts: Seq[Inst]) =
    ControlFlow.removeDeadBlocks(insts)
}

object DeadBlockElimination extends PassCompanion {
  override def apply(config: build.Config, linked: linker.Result) =
    new DeadBlockElimination()
}
