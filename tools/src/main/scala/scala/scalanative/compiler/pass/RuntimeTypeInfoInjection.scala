package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy._
import compiler.analysis.ClassHierarchyExtractors._
import nir._

/** Generates type instances for all classes, modules, traits and structs. */
class RuntimeTypeInfoInjection(implicit top: Top, fresh: Fresh) extends Pass {
  private def typeName(node: Scope): Global = node match {
    case node: Class =>
      node.name tag "class" tag "type"
    case node: Trait =>
      node.name tag "trait" tag "type"
    case node: Struct =>
      node.name tag "struct" tag "type"
    case _ =>
      util.unreachable
  }

  override def preDefn = {
    case classDefn @ Defn.Class(_, name @ ClassRef(cls), _, _) =>
      val typeDefn =
        Defn.Const(Attrs.None, typeName(cls), cls.typeStruct, cls.typeValue)

      Seq(classDefn, typeDefn)

    case defn @ (_: Defn.Module | _: Defn.Trait | _: Defn.Struct) =>
      val node = top.nodes(defn.name).asInstanceOf[Scope]

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
  def apply(ctx: Ctx) = new RuntimeTypeInfoInjection()(ctx.top, ctx.fresh)

  override val depends = Seq(Rt.Type.name)
}
