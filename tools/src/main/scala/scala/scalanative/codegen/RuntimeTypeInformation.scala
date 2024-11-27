package scala.scalanative
package codegen

import scalanative.util.unreachable
import scalanative.linker.{ScopeInfo, Class, Trait}

private[codegen] class RuntimeTypeInformation(info: ScopeInfo)(implicit
    meta: Metadata
) {

  import RuntimeTypeInformation._

  val name: nir.Global.Member = info.name.member(nir.Sig.Generated("type"))
  val const: nir.Val.Global = nir.Val.Global(name, nir.Type.Ptr)
  val struct: nir.Type.StructValue = info match {
    case cls: Class =>
      meta.layouts.ClassRtti
        .genLayout(vtable = meta.vtable(cls).ty)
    case _ => meta.layouts.Rtti.layout
  }
  lazy val value: nir.Val.StructValue = {
    val typeId = nir.Val.Int(meta.ids(info))
    val typeStr = nir.Val.String(info.name.asInstanceOf[nir.Global.Top].id)
    val traits = info.linearized
      .collect { case cls: Trait if cls != info => cls }
      .sortBy(meta.ids(_))
    val interfacesCount = nir.Val.Int(traits.size)
    val interfaces = nir.Val.Const(
      nir.Val.ArrayValue(nir.Type.Ptr, traits.map(meta.rtti(_).const))
    )

    val base = nir.Val.StructValue(
      classConst :: meta.lockWordVals :::
        typeId ::
        interfacesCount ::
        interfaces ::
        typeStr :: Nil
    )
    info match {
      case cls: Class =>
        val dynmap =
          if (!meta.layouts.ClassRtti.usesDynMap) Nil
          else List(meta.dynmap(cls).value)
        val range = meta.ranges(cls)
        val itable = meta.itable(cls)
        val itablesSize = {
          // we want to have compile-time computed mask for fast selction
          // negative size to mark requirement of using slow-path binary search
          if (itable.useFastITables) (itable.size - 1).max(0)
          else -itable.size
        }
        val superClass =
          cls.parent.map(meta.rtti(_).const).getOrElse(nir.Val.Null)
        nir.Val.StructValue(
          base ::
            nir.Val.Int(meta.layout(cls).size.toInt) :: // size
            nir.Val.Int(range.last) :: // idRangeUntil
            meta.layout(cls).referenceOffsetsValue :: // refFieldOffsets
            // Free slot for additional Int32 to be used in the future
            nir.Val.Int(itablesSize) ::
            itable.const ::
            superClass ::
            dynmap :::
            meta.vtable(cls).value ::
            Nil
        )
      case _ =>
        base
    }
  }

  lazy val canUseFastITables: Boolean = info match {
    case cls: Class => meta.itable(cls).useFastITables
    case cls: Trait => cls.implementors.forall(meta.rtti(_).canUseFastITables)
  }
}

private[codegen] object RuntimeTypeInformation {

  private val classConst =
    nir.Val.Global(
      nir.Rt.Class.name.member(nir.Sig.Generated("type")),
      nir.Type.Ptr
    )

}
