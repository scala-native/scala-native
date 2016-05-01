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
  private def defnType(tag: String, name: Global): Defn = {
    val typeName = name.tag(tag).tag("type")
    val typeId   = Val.I32(chg.nodes(name).id)
    val typeStr  = Val.String(name.id)
    val typeVal  = Val.Struct(Rt.Type.name, Seq(typeId, typeStr))

    Defn.Const(Seq(), typeName, Rt.Type, typeVal)
  }

  override def preDefn = {
    case defn: Defn.Module => Seq(defn, defnType("module", defn.name))
    case defn: Defn.Class  => Seq(defn, defnType("class", defn.name))
    case defn: Defn.Trait  => Seq(defn, defnType("trait", defn.name))
    case defn: Defn.Struct => Seq(defn, defnType("struct", defn.name))
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

    case Inst(n, Op.TypeOf(_)) =>
      ???
  }
}
