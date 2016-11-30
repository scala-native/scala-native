package scala.scalanative
package optimizer
package pass

import scala.scalanative.nir.{Inst, Op}
import scala.scalanative.optimizer.analysis.ClassHierarchy.Top
import scala.scalanative.tools.Config
import scala.scalanative.util.unsupported

/** Eliminates:
 *  - Op.Dynmethod
 */
class DynmethodLowering extends Pass {
  override def preInst = {
    case Inst.Let(_, Op.Dynmethod(_, _)) =>
      unsupported(s"Dynmethod is not supported.")
  }
}

object DynmethodLowering extends PassCompanion {
  def apply(config: Config, top: Top): Pass = new DynmethodLowering
}