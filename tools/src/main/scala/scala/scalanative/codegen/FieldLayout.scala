package scala.scalanative
package codegen

import scalanative.linker.{Class, Field}

class FieldLayout(cls: Class)(implicit meta: Metadata) {

  import meta.layouts.{Object, ObjectHeader}
  import meta.platform

  def index(fld: Field) = entries.indexOf(fld) + Object.ValuesOffset
  // Proxy fields due to cyclic dependency
  def entries: Seq[Field] = entries0
  def layout: MemoryLayout = layout0

  private lazy val (entries0, layout0): (Seq[Field], MemoryLayout) = {
    val entries: Seq[Field] = {
      val base = cls.parent.fold {
        Seq.empty[Field]
      } { parent => meta.layout(parent).entries }
      base ++ cls.members.collect { case f: Field => f }
    }
    val usesCustomAlignment = entries.exists(_.attrs.align.isDefined)
    if (usesCustomAlignment) {
      val fields = entries.sortBy(_.attrs.align.flatMap(_.group))
      val layout = MemoryLayout.ofAlignedFields(fields)
      (fields, layout)
    } else {
      val layout = MemoryLayout(ObjectHeader.layout +: entries.map(_.ty))
      (entries, layout)
    }
  }

  val struct = nir.Type.StructValue(layout.tys.map(_.ty))
  val size = layout.size
  val referenceOffsetsValue = nir.Val.Const(
    nir.Val.ArrayValue(nir.Type.Int, layout.referenceFieldsOffsets)
  )

}
