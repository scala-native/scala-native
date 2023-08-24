package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.nir._
import scalanative.linker.Class

class ModuleArray(meta: Metadata) {
  val index = mutable.Map.empty[Class, Int]
  val modules = mutable.UnrolledBuffer.empty[Class]
  meta.classes.foreach { cls =>
    if (cls.isModule && cls.allocated) {
      index(cls) = modules.size
      modules += cls
    }
  }
  val size: Int = modules.size
  val value: Val =
    Val.ArrayValue(
      Type.Ptr,
      modules.toSeq.map { cls =>
        if (cls.isConstantModule(meta.analysis))
          Val.Global(cls.name.member(Sig.Generated("instance")), Type.Ptr)
        else
          Val.Null
      }
    )
}
