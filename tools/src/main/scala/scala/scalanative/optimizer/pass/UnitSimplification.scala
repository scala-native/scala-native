package scala.scalanative
package optimizer
package pass

import nir.Val
import nir.Type
import analysis.ClassHierarchy.Top

class UnitSimplification extends Pass {

  override def onVal(value: Val): Val = value match {
    case v if (v.ty == Type.Unit) =>
      Val.Unit

    case _ =>
      super.onVal(value)
  }
}

object UnitSimplification extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new UnitSimplification
}
