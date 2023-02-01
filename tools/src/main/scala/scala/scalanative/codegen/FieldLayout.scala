package scala.scalanative
package codegen

import scalanative.nir._
import scalanative.linker.{Class, Field}

object FieldLayout {
  val referenceOffsetsTy = Type.StructValue(Seq(Type.Ptr))
}

class FieldLayout(cls: Class)(implicit meta: Metadata) {
  import meta.layouts.{Object, ObjectHeader}
  import meta.platform

  def index(fld: Field) = entries.indexOf(fld) + Object.ValuesOffset
  val entries: Seq[Field] = {
    val base = cls.parent.fold {
      Seq.empty[Field]
    } { parent => meta.layout(parent).entries }
    base ++ cls.members.collect { case f: Field => f }
  }
  val struct: Type.StructValue = {
    val data = entries.map(_.ty)
    Type.StructValue(ObjectHeader.layout +: data)
  }
  val layout = MemoryLayout(struct.tys)
  val size = layout.size
  val referenceOffsetsValue = Val.StructValue(
    Seq(Val.Const(Val.ArrayValue(Type.Long, layout.offsetArray)))
  )
}
