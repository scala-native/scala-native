package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy
import compiler.analysis.ClassHierarchyExtractors._
import nir._

/** Hoists external members from external modules to top-level scope. */
class ExternalHoisting(implicit chg: ClassHierarchy.Graph) extends Pass {
  private def stripName(n: Global): Global = {
    val Global.Member(_, id) = n
    Global.Val(id)
  }

  private def hoist(defns: Seq[Defn]): (Seq[Defn], Seq[Defn]) = {
    def isExternal(defn: Defn): Boolean =
      defn.attrs.exists(_ == Attr.External)
    val hoisted = defns.collect {
      case defn: Defn.Declare if isExternal(defn) =>
        defn.copy(name = stripName(defn.name),
                  attrs = defn.attrs.filterNot(_ == Attr.External))
      case defn: Defn.Const if isExternal(defn) =>
        defn.copy(name = stripName(defn.name))
      case defn: Defn.Var if isExternal(defn) =>
        defn.copy(name = stripName(defn.name))
    }
    val rest = defns.filterNot(isExternal)

    (hoisted, rest)
  }

  override def preVal = {
    case Val.Global(n @ ClassRef(cls), ty) if cls.isExternal =>
      Val.Global(stripName(n), ty)
  }

  def isExternalModule(name: Global): Boolean =
    chg.nodes.get(name) match {
      case Some(cls: ClassHierarchy.Class)
          if cls.attrs.exists(_ == Attr.External) =>
        true
      case _ =>
        false
    }
}
