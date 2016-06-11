package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy
import compiler.analysis.ClassHierarchyExtractors._
import nir._

/** Generates type instances for all classes, modules,
 *  traits and structs.
 */
class RuntimeTypeInfoInjection(
    implicit chg: ClassHierarchy.Graph, fresh: Fresh)
    extends Pass {
  private def typeName(node: ClassHierarchy.Scope): Global = node match {
    case node: ClassHierarchy.Class =>
      node.name tag "class" tag "type"
    case node: ClassHierarchy.Trait =>
      node.name tag "trait" tag "type"
    case node: ClassHierarchy.Struct =>
      node.name tag "struct" tag "type"
  }

  override def preDefn = {
    case classDefn @ Defn.Class(_, name @ ClassRef(cls), _, _) =>
      val typeDefn =
        Defn.Const(Attrs.None, typeName(cls), cls.typeStruct, cls.typeValue)

      Seq(classDefn, typeDefn)

    case defn @ (_: Defn.Module | _: Defn.Trait | _: Defn.Struct) =>
      val node = chg.nodes(defn.name).asInstanceOf[ClassHierarchy.Scope]

      val typeId   = Val.I32(node.id)
      val typeStr  = Val.String(node.name.id)
      val typeVal  = Val.Struct(Rt.Type.name, Seq(typeId, typeStr))
      val typeDefn = Defn.Const(Attrs.None, typeName(node), Rt.Type, typeVal)

      Seq(defn, typeDefn)
  }

  override def preVal = {
    case Val.Global(ScopeRef(node), _) =>
      Val.Global(typeName(node), Type.Ptr)
  }
}

object RuntimeTypeInfoInjection extends PassCompanion {
  def apply(ctx: Ctx) = new RuntimeTypeInfoInjection()(ctx.chg, ctx.fresh)

  override val depends = Seq(Rt.Type.name)
}
