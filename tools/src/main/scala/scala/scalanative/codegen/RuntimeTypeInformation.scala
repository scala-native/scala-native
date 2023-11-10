package scala.scalanative
package codegen

import scalanative.util.unreachable
import scalanative.linker.{ScopeInfo, Class, Trait}

class RuntimeTypeInformation(info: ScopeInfo)(implicit meta: Metadata) {

  import RuntimeTypeInformation._

  val name: nir.Global.Member = info.name.member(nir.Sig.Generated("type"))
  val const: nir.Val.Global = nir.Val.Global(name, nir.Type.Ptr)
  val struct: nir.Type.StructValue = info match {
    case cls: Class =>
      meta.layouts.ClassRtti.genLayout(
        vtable = meta.vtable(cls).ty
      )
    case _ => meta.layouts.Rtti.layout
  }
  val value: nir.Val.StructValue = {
    val typeId = nir.Val.Int(info match {
      case _: Class => meta.ids(info)
      case _: Trait => -(meta.ids(info) + 1)
    })
    val typeStr = nir.Val.String(info.name.asInstanceOf[nir.Global.Top].id)
    val traitId = nir.Val.Int(info match {
      case info: Class => meta.dispatchTable.traitClassIds.getOrElse(info, -1)
      case _           => -1
    })
    val base = nir.Val.StructValue(
      classConst :: meta.lockWordVals ::: typeId :: traitId :: typeStr :: Nil
    )
    info match {
      case cls: Class =>
        val dynmap =
          if (!meta.layouts.ClassRtti.usesDynMap) Nil
          else List(meta.dynmap(cls).value)
        val range = meta.ranges(cls)
        nir.Val.StructValue(
          base ::
            nir.Val.Int(meta.layout(cls).size.toInt) ::
            nir.Val.Int(range.last) ::
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
    nir.Val.Global(
      nir.Rt.Class.name.member(nir.Sig.Generated("type")),
      nir.Type.Ptr
    )

}
