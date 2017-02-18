package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import nir._

/** Hoists external members from external modules to top-level scope. */
class ExternHoisting(implicit top: Top) extends Pass {
  private def stripName(n: Global): Global = {
    val id = n.id
    assert(id.startsWith("extern."))
    Global.Member(Global.Top("__extern"), id.substring(7)) // strip extern. prefix
  }

  override def onDefn(defn: Defn): Defn = super.onDefn {
    defn match {
      case defn @ Defn.Declare(attrs, name, _) if attrs.isExtern =>
        defn.copy(name = stripName(name))
      case defn @ Defn.Define(attrs, name, _, _) if attrs.isExtern =>
        defn.copy(name = stripName(name))
      case defn @ Defn.Const(attrs, name, _, _) if attrs.isExtern =>
        defn.copy(name = stripName(name))
      case defn @ Defn.Var(attrs, name, _, _) if attrs.isExtern =>
        defn.copy(name = stripName(name))
      case _ =>
        defn
    }
  }

  override def onVal(value: Val) = value match {
    case Val.Global(n @ Ref(node), ty) if node.attrs.isExtern =>
      Val.Global(stripName(n), ty)
    case _ =>
      super.onVal(value)
  }
}

object ExternHoisting extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new ExternHoisting()(top)
}
