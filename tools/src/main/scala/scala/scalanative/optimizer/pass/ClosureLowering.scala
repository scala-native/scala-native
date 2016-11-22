package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import analysis.ClassHierarchy.Top
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
  override def apply(config: tools.Config, top: Top) =
    new ClosureLowering
}
