package scala.scalanative
package compiler
package pass

import nir._
import util._

/** Eliminates:
 *  - Op.DynMethod
 */
class DynMethodLowering extends Pass {
  override def preInst = {
    case Inst(n, dyn @ Op.DynMethod(_, _)) =>
      unsupported(s"DynMethod lowering missing ($dyn)")
  }
}

object DynMethodLowering extends PassCompanion {
  def apply(ctx: Ctx) = new DynMethodLowering
}
