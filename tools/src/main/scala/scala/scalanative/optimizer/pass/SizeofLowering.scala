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
    val buf = new nir.Buffer
    import buf._

    insts.foreach {
      case Inst.Let(n, Op.Sizeof(ty)) =>
        val elem = let(Op.Elem(ty, Val.Null, Seq(Val.Int(1))))
        let(n, Op.Conv(Conv.Ptrtoint, Type.Long, elem))

      case inst =>
        buf += inst
    }

    buf.toSeq
  }
}

object SizeofLowering extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new SizeofLowering()(top.fresh)
}
