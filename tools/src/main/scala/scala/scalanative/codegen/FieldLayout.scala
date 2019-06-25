package scala.scalanative
package codegen

import scalanative.nir._
import scalanative.linker.{Class, Field}

class FieldLayout(meta: Metadata, cls: Class) {
  def index(fld: Field) =
    entries.indexOf(fld) + 1
  val entries: Seq[Field] = {
    val base = cls.parent.fold {
      Seq.empty[Field]
    } { parent =>
      meta.layout(parent).entries
    }
    base ++ cls.members.collect { case f: Field => f }
  }
  val struct: Type.StructValue = {
    val data = entries.map(_.ty)
    val body = Type.Ptr +: data
    Type.StructValue(body)
  }
  val layout = MemoryLayout(struct.tys)
  val size   = layout.size
  val referenceOffsetsTy =
    Type.StructValue(Seq(Type.Ptr))
  val referenceOffsetsValue =
    Val.StructValue(
      Seq(Val.Const(Val.ArrayValue(Type.Long, layout.offsetArray))))
}
