package scala.scalanative
package optimizer
package pass

import nir.Val
import nir.Type
import analysis.ClassHierarchy.Top

class UnitSimplification extends Pass {

  override def preVal = {
    case v if (v.ty == Type.Unit) => Val.Unit
  }

}

object UnitSimplification extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new UnitSimplification
}
