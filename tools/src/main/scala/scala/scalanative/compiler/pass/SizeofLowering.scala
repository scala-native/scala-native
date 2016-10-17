package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import util.ScopedVar, ScopedVar.scoped
import nir._, Inst.Let

/** Maps sizeof computation to pointer arithmetics over null pointer. */
class SizeofLowering(implicit fresh: Fresh) extends Pass {
  override def preInst = {
    case Let(n, Op.Sizeof(ty)) =>
      val elem = Val.Local(fresh(), Type.Ptr)
      Seq(
          Let(elem.name, Op.Elem(ty, Val.Null, Seq(Val.I32(1)))),
          Let(n, Op.Conv(Conv.Ptrtoint, Type.I64, elem))
      )
  }
}

object SizeofLowering extends PassCompanion {
  def apply(ctx: Ctx) = new SizeofLowering()(ctx.fresh)
}
