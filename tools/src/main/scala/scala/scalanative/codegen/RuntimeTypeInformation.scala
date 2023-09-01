package scala.scalanative
package codegen

import scalanative.util.unreachable
import scalanative.nir._
import scalanative.linker.{ScopeInfo, Class, Trait}

class RuntimeTypeInformation(info: ScopeInfo)(implicit meta: Metadata) {
  import RuntimeTypeInformation._
  val name: Global.Member = info.name.member(Sig.Generated("type"))
  val const: Val.Global = Val.Global(name, Type.Ptr)
  val struct: Type.StructValue = info match {
    case cls: Class =>
      meta.layouts.ClassRtti.genLayout(
        vtable = meta.vtable(cls).ty
      )
    case _ => meta.layouts.Rtti.layout
  }
  val value: Val.StructValue = {
    val typeId = Val.Int(info match {
      case _: Class => meta.ids(info)
      case _: Trait => -(meta.ids(info) + 1)
    })
    val typeStr = Val.String(info.name.asInstanceOf[Global.Top].id)
    val traitId = Val.Int(info match {
      case info: Class => meta.dispatchTable.traitClassIds.getOrElse(info, -1)
      case _           => -1
    })
    val base = Val.StructValue(
      classConst :: meta.lockWordVals ::: typeId :: traitId :: typeStr :: Nil
    )
    info match {
      case cls: Class =>
        val dynmap =
          if (!meta.layouts.ClassRtti.usesDynMap) Nil
          else List(meta.dynmap(cls).value)
        val range = meta.ranges(cls)
        Val.StructValue(
          base ::
            Val.Int(meta.layout(cls).size.toInt) ::
            Val.Int(range.last) ::
            meta.layout(cls).referenceOffsetsValue ::
            dynmap :::
            meta.vtable(cls).value ::
            Nil
        )
      case _ =>
        base
    }
  }
}
object RuntimeTypeInformation {
  private val classConst =
    Val.Global(Rt.Class.name.member(Sig.Generated("type")), Type.Ptr)
}
