package scala.scalanative
package codegen

import scala.collection.mutable
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
  val value: nir.Val =
    nir.Val.ArrayValue(
      nir.Type.Ptr,
      modules.toSeq.map { cls =>
        if (cls.isConstantModule(meta.analysis))
          nir.Val.Global(cls.name.member(nir.Sig.Generated("instance")), nir.Type.Ptr)
        else
          nir.Val.Null
      }
    )

}
