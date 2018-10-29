package scala.scalanative
package codegen

import scalanative.util.unreachable
import scalanative.nir._
import scalanative.linker.{ScopeInfo, Class, Trait}

class RuntimeTypeInformation(meta: Metadata, info: ScopeInfo) {
  val name: Global      = info.name.member(Sig.Generated("type"))
  val const: Val.Global = Val.Global(name, Type.Ptr)
  val struct: Type.StructValue = info match {
    case cls: Class =>
      Type.StructValue(
        Seq(
          Rt.Type,
          Type.Long, // size
          Type.StructValue(Seq(Type.Int, Type.Int)), // range
          meta.dynmap(cls).ty,
          meta.layout(cls).referenceOffsetsTy,
          meta.vtable(cls).ty
        )
      )
    case _ =>
      Rt.Type
  }
  val value: Val.StructValue = {
    val typeId  = Val.Int(meta.ids(info))
    val typeStr = Val.String(info.name.asInstanceOf[Global.Top].id)
    val typeKind = Val.Byte(info match {
      case _: Class => 0
      case _: Trait => 1
      case _        => unreachable
    })
    val traitId = Val.Int(info match {
      case info: Class =>
        meta.dispatchTable.traitClassIds.get(info).getOrElse(-1)
      case _ =>
        -1
    })
    val base =
      Val.StructValue(Seq(typeId, traitId, typeStr, typeKind))
    info match {
      case cls: Class =>
        val range = meta.ranges(cls)
        Val.StructValue(
          Seq(
            base,
            Val.Long(meta.layout(cls).size),
            Val.StructValue(Seq(Val.Int(range.head), Val.Int(range.last))),
            meta.dynmap(cls).value,
            meta.layout(cls).referenceOffsetsValue,
            meta.vtable(cls).value
          )
        )
      case _ =>
        base
    }
  }
}
