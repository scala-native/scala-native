package scala.scalanative
package optimizer
package analysis

import scalanative.optimizer.analysis.ClassHierarchy._
import scalanative.nir._
import scalanative.util.unreachable

class RuntimeTypeInformation(node: Scope) {
  val name: Global      = node.name member "type"
  val const: Val.Global = Val.Global(name, Type.Ptr)
  val struct: Type.Struct = node match {
    case cls: Class =>
      Type.Struct(
        Global.None,
        Seq(Rt.Type,
            Type.Long, // size
            Type.Struct(Global.None, Seq(Type.Int, Type.Int)), // range
            cls.dynmap.ty,
            cls.layout.referenceOffsetsTy,
            cls.vtable.ty)
      )
    case _ =>
      Rt.Type
  }
  val value: Val.Struct = {
    val typeId  = Val.Int(node.id)
    val typeStr = Val.String(node.name.id)
    val typeKind = Val.Byte(node match {
      case _: Class  => 0
      case _: Trait  => 1
      case _: Struct => 2
      case _         => unreachable
    })
    val base = Val.Struct(Rt.Type.name, Seq(typeId, typeStr, typeKind))
    node match {
      case cls: Class =>
        Val.Struct(
          Global.None,
          Seq(
            base,
            Val.Long(cls.layout.size),
            Val.Struct(Global.None,
                       Seq(Val.Int(cls.range.head), Val.Int(cls.range.last))),
            cls.dynmap.value,
            cls.layout.referenceOffsetsValue,
            cls.vtable.value
          )
        )
      case _ =>
        base
    }
  }
}
