package scala.scalanative
package codegen

import scalanative.nir._
import scalanative.linker.{Class, Field}
import scala.scalanative.build.TargetArchitecture

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
  val name = cls.name member "layout"
  val struct: Type.StructValue = {
    val data = entries.map(_.ty)
    val body = Type.Ptr +: data
    val ty   = Type.StructValue(name, body)
    Type.StructValue(name, body)
  }
  val layout = MemoryLayout(struct.tys, meta.targetArchitecture)
  val size   = layout.size
  val referenceOffsetsTy =
    Type.StructValue(Global.None, Seq(Type.Ptr))
  val referenceOffsetsValue =
    Val.StructValue(
      Global.None,
      Seq(Val.Const(Val.ArrayValue(Type.Long, layout.offsetArray))))
}
