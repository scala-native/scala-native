package scala.scalanative
package lower

import scalanative.nir._
import scalanative.sema._

class FieldLayout(meta: Metadata, cls: Class) {
  def index(fld: Field) =
    entries.indexOf(fld) + 1
  val entries: Seq[Field] = {
    val base = cls.parent.fold {
      Seq.empty[Field]
    } { parent =>
      meta.layout(parent).entries
    }
    base ++ cls.fields
  }
  val name = cls.name member "layout"
  val struct: Type.Struct = {
    val data = entries.map(_.ty)
    val body = Type.Ptr +: data
    val ty   = Type.Struct(name, body)
    Type.Struct(name, body)
  }
  val layout = MemoryLayout(struct.tys)
  val size   = layout.size
  val referenceOffsetsTy =
    Type.Struct(Global.None, Seq(Type.Ptr))
  val referenceOffsetsValue =
    Val.Struct(Global.None,
               Seq(Val.Const(Val.Array(Type.Long, layout.offsetArray))))
}
