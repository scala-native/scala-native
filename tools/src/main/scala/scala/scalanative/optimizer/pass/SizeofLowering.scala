package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import analysis.ClassHierarchy.Top
import util.ScopedVar, ScopedVar.scoped
import nir._

/** Maps sizeof computation to pointer arithmetics over null pointer. */
class SizeofLowering(implicit fresh: Fresh) extends Pass {
  override def onInsts(insts: Seq[Inst]): Seq[Inst] = {
    val buf = mutable.UnrolledBuffer.empty[Inst]

    insts.foreach {
      case Inst.Let(n, Op.Sizeof(ty)) =>
        val elem = Val.Local(fresh(), Type.Ptr)
        buf += Inst.Let(elem.name, Op.Elem(ty, Val.Null, Seq(Val.I32(1))))
        buf += Inst.Let(n, Op.Conv(Conv.Ptrtoint, Type.I64, elem))

      case inst =>
        buf += inst
    }

    buf
  }
}

object SizeofLowering extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new SizeofLowering()(top.fresh)
}
