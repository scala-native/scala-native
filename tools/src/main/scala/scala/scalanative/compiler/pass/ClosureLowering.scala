package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import util.unsupported
import nir._

/** Translates high-level closures to corresponding class allocation. */
class ClosureLowering extends Pass {
  override def preInst = {
    case inst @ Inst.Let(_, _: Op.Closure) =>
      unsupported(inst)
  }
}

object ClosureLowering extends PassCompanion {
  def apply(ctx: Ctx) = new ClosureLowering
}
