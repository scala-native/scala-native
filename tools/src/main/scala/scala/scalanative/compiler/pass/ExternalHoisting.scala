package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy
import compiler.analysis.ClassHierarchyExtractors._
import nir._

/** Hoists external members from external modules to top-level scope. */
class ExternalHoisting(implicit chg: ClassHierarchy.Graph) extends Pass {
  private def stripName(n: Global): Global = {
    val Global.Member(Global.Member(_, id), "ext") = n
    Global.Val(id)
  }

  override def preDefn = {
    case defn @ Defn.Declare(_, name @ Ref(node), _) if node.isExternal =>
      Seq(defn.copy(name = stripName(name),
                    attrs = defn.attrs.filterNot(_ == Attr.External)))
    case defn @ Defn.Const(_, name @ Ref(node), _, _) if node.isExternal =>
      Seq(defn.copy(name = stripName(name)))
    case defn @ Defn.Var(_, name @ Ref(node), _, _) if node.isExternal =>
      Seq(defn.copy(name = stripName(name)))
  }

  override def preVal = {
    case Val.Global(n @ Ref(node), ty) if node.isExternal =>
      Val.Global(stripName(n), ty)
  }
}
