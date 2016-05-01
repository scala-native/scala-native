package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy
import compiler.analysis.ClassHierarchyExtractors._
import nir._

/** Generates type instances for all classes, modules,
  * traits and structs. Lowers typeof and instance checks
  * to operations based on runtime types.
  *
  * Eliminates:
  * - Op.{As, Is, Typeof}
  */
class TypeLowering(implicit chg: ClassHierarchy.Graph, fresh: Fresh)
    extends Pass {
  private def typeName(node: ClassHierarchy.Node): Global = node match {
    case node: ClassHierarchy.Class if node.isModule =>
      node.name.tag("module").tag("type")
    case node: ClassHierarchy.Class if !node.isModule =>
      node.name.tag("class").tag("type")
    case node: ClassHierarchy.Trait =>
      node.name.tag("trait").tag("type")
    case node: ClassHierarchy.Struct =>
      node.name.tag("struct").tag("type")
  }

  private def typeDefn(id: Int, str: String, name: Global) = {
    val typeId   = Val.I32(id)
    val typeStr  = Val.String(str)
    val typeVal  = Val.Struct(Rt.Type.name, Seq(typeId, typeStr))

    Defn.Const(Seq(), name, Rt.Type, typeVal)
  }

  private val noTypeDefn =
    typeDefn(0, "notype", Global.Val("scalanative_notype"))
  private val noType =
    Val.Global(noTypeDefn.name, Type.Ptr)

  override def preAssembly = { case defns =>
    noTypeDefn +: defns
  }

  override def preDefn = {
    case defn @ (_: Defn.Module | _: Defn.Class | _: Defn.Trait | _: Defn.Struct) =>
      val node = chg.nodes(defn.name)
      val id   = node.id
      val str  = node.name.id
      val name = typeName(node)

      Seq(defn, typeDefn(id, str, name))
  }

  override def preInst = {
    case Inst(n, Op.As(_, v)) =>
      Seq(
          Inst(n, Op.Copy(v))
      )

    case Inst(n, Op.Is(ClassRef(cls), v)) =>
      val ty = Val.Local(fresh(), Type.Ptr)
      val id = Val.Local(fresh(), Type.I32)
      val cond =
        if (cls.range.length == 1)
          Seq(
              Inst(n, Op.Comp(Comp.Ieq, Type.I32, id, Val.I32(cls.id)))
          )
        else {
          val ge = Val.Local(fresh(), Type.Bool)
          val le = Val.Local(fresh(), Type.Bool)

          Seq(
              Inst(ge.name,
                   Op.Comp(Comp.Sge, Type.I32, Val.I32(cls.range.start), id)),
              Inst(le.name,
                   Op.Comp(Comp.Sle, Type.I32, id, Val.I32(cls.range.end))),
              Inst(n, Op.Bin(Bin.And, Type.Bool, ge, le))
          )
        }

      // Seq(
      //   Inst(ty.name, Rt.call(Rt.Object_getType, v)),
      //   Inst(id.name, Rt.call(Rt.Type_getId, ty))
      // ) ++ cond
      ???

    // TODO: is trait/module/struct
    case Inst(n, Op.Is(_, obj)) =>
      ???

    case Inst(n, Op.Typeof(ty)) =>
      val value = ty match {
        case Ref(node) => Val.Global(typeName(node), Type.Ptr)
        case ty        => noType
      }

      Seq(Inst(n, Op.Copy(value)))
  }
}
