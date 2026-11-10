package scala.scalanative
package codegen

import scalanative.linker.{Class, Field}

private[codegen] class FieldLayout(cls: Class)(implicit meta: Metadata) {

  import meta.layouts.{ArrayHeader, Object, ObjectHeader}
  import meta.platform

  def index(fld: Field) = entries.indexOf(fld) + Object.ValuesOffset
  // Proxy fields due to cyclic dependency
  def entries: Seq[Field] = entries0
  def layout: MemoryLayout = layout0

  private lazy val (entries0, layout0) = {
    val entries: Seq[Field] = {
      val base = cls.parent.fold {
        Seq.empty[Field]
      } { parent => meta.layout(parent).entries }
      base ++ cls.members.collect { case f: Field => f }
    }
    val usesCustomAlignment = entries.exists(_.attrs.align.isDefined)
    val isArray = nir.Type.isArray(cls.name)
    if (usesCustomAlignment) {
      assert(!isArray) // Only regular object can have custom alignmet
      val fields = entries.sortBy(_.attrs.align.flatMap(_.group))
      val layout = MemoryLayout.ofAlignedFields(fields)
      (fields, layout)
    } else {
      val rttiHeader = if (isArray) ArrayHeader else ObjectHeader
      val layout = MemoryLayout(rttiHeader.layout +: entries.map(_.ty))
      (entries, layout)
    }
  }

  val struct = nir.Type.StructValue(layout.tys.map(_.ty))
  val size = layout.size
  /* Reference map used by the GC: offsets of reference fields, -1, then
   * offsets of raw pointer fields to scan conservatively, -1. Only boxed
   * pointers (unsafe.Ptr) scan raw pointers: one can point into the heap, for
   * example to an array element, and be the only thing keeping it alive.
   */
  val referenceOffsetsValue = {
    val conservative =
      if (cls.name == nir.Rt.BoxedPtr.name) layout.rawPointerFieldsOffsets
      else Nil
    val offsets = layout.referenceFieldsOffsets ++ (-1 +: conservative) :+ -1
    nir.Val.Const(nir.Val.ArrayValue(nir.Type.Int, offsets.map(nir.Val.Int(_))))
  }

}
