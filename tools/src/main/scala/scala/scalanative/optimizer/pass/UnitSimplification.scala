package scala.scalanative
package optimizer
package pass

import nir._

class UnitSimplification extends Pass {
  def onInsts(insts: Seq[Inst]): Seq[Inst] =
    UnitSimplification.RemoveUnitDependency.onInsts(insts)
}

object UnitSimplification extends PassCompanion {

  private object RemoveUnitDependency extends Transform {
    override def onVal(value: Val): Val = value match {
      case v if (v.ty == Type.Unit) =>
        Val.Unit

      case _ =>
        super.onVal(value)
    }
  }

  override def apply(config: build.Config, top: sema.Top) =
    new UnitSimplification
}
