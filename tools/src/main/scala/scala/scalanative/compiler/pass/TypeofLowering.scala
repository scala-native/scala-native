package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy
import compiler.analysis.ClassHierarchyExtractors._
import nir._

/** Generates type instances for all classes, modules,
 *  traits and structs. Lowers typeof and instance checks
 *  to operations based on runtime types.
 *
 *  Eliminates:
 *  - Op.{As, Is, Typeof}
 */
class TypeofLowering(implicit chg: ClassHierarchy.Graph, fresh: Fresh)
    extends Pass {
  private def typeName(node: ClassHierarchy.Node): Global = node match {
    case node: ClassHierarchy.Class =>
      node.name.tag("class").tag("type")
    case node: ClassHierarchy.Trait =>
      node.name.tag("trait").tag("type")
    case node: ClassHierarchy.Struct =>
      node.name.tag("struct").tag("type")
    case _ =>
      util.unreachable
  }

  private def typeDefn(id: Int, str: String, name: Global) = {
    val typeId  = Val.I32(id)
    val typeStr = Val.String(str)
    val typeVal = Val.Struct(Rt.Type.name, Seq(typeId, typeStr))

    Defn.Const(Attrs.None, name, Rt.Type, typeVal)
  }

  private val noTypeDefn = typeDefn(
      0, "notype", Global.Val("scalanative_notype"))
  private val noType = Val.Global(noTypeDefn.name, Type.Ptr)

  override def preAssembly = {
    case defns =>
      noTypeDefn +: defns
  }

  override def preDefn = {
    case defn @ (_: Defn.Module | _: Defn.Class | _: Defn.Trait |
        _: Defn.Struct) =>
      val node = chg.nodes(defn.name)
      val id   = node.id
      val str  = node.name.id
      val name = typeName(node)

      Seq(defn, typeDefn(id, str, name))
  }

  override def preInst = {
    case Inst(n, Op.Typeof(ty)) =>
      val value = ty match {
        case Ref(node) => Val.Global(typeName(node), Type.Ptr)
        case ty        => noType
      }

      Seq(Inst(n, Op.Copy(value)))
  }
}
