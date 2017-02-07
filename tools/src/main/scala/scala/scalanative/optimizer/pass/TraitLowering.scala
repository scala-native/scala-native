package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import nir._

/** Eliminates traits and injects trait method
 *  and instance dispatch tables into the assembly.
 */
class TraitLowering(implicit top: Top, fresh: Fresh) extends Pass {
  import TraitLowering._

  override def onDefns(defns: Seq[Defn]): Seq[Defn] =
    defns.filter {
      case _: Defn.Trait                              => false
      case Defn.Declare(_, MethodRef(_: Trait, _), _) => false
      case _                                          => true
    }
}

object TraitLowering extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new TraitLowering()(top, top.fresh)
}
