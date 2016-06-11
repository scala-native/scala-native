package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy
import compiler.analysis.ClassHierarchyExtractors._
import nir._

/** Lowers traits and operations on them. */
class TraitLowering(implicit chg: ClassHierarchy.Graph, fresh: Fresh)
    extends Pass {
  override def preDefn = {
    case defn: Defn.Trait =>
      Seq()
    // TODO: filter out abstract trait methods
  }

  override def preInst = {
    case Inst(n, Op.Method(sig, obj, MethodRef(Some(TraitRef(_)), _))) =>
      ???

    case inst @ Inst(n, Op.Is(TraitRef(_), _)) =>
      ???
  }
}

object TraitLowering extends PassCompanion {
  def apply(ctx: Ctx) = new TraitLowering()(ctx.chg, ctx.fresh)
}
