package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import util.ScopedVar, ScopedVar.scoped
import nir._

/** Translates high-level closures to corresponding class allocation. */
class ClosureLowering extends Pass {
  override def preInst = {
    case Inst(_, _: Op.Closure) =>
      ???
  }
}

object ClosureLowering extends PassCompanion {
  def apply(ctx: Ctx) = new ClosureLowering
}
