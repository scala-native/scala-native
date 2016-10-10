package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import compiler.analysis.ClassHierarchy._
import compiler.analysis.ClassHierarchyExtractors._
import nir._

/** Eliminates traits and injects trait method
 *  and instance dispatch tables into the assembly.
 */
class TraitLowering(implicit top: Top, fresh: Fresh) extends Pass {
  import TraitLowering._

  override def preAssembly = {
    case assembly =>
      assembly :+ top.dispatchDefn :+ top.instanceDefn
  }

  override def preDefn = {
    case _: Defn.Trait =>
      Seq()

    case Defn.Declare(_, MethodRef(_: Trait, _), _) =>
      Seq()
  }
}

object TraitLowering extends PassCompanion {
  def apply(ctx: Ctx) = new TraitLowering()(ctx.top, ctx.fresh)
}
