package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import nir._

/** Generates type instances for all classes, modules, traits and structs. */
class TypeValueLowering(implicit top: Top) extends Pass {

  override def onVal(value: Val) = value match {
    case Val.Global(ScopeRef(node), _) =>
      Val.Global(node.rtti.name, Type.Ptr)
    case _ =>
      super.onVal(value)
  }
}

object TypeValueLowering extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new TypeValueLowering()(top)
}
