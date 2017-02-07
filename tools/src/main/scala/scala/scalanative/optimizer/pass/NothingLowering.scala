package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import scala.util.control.Breaks._
import analysis.ClassHierarchy.Top
import util.unsupported
import nir._

/** Short-circuits method calls that return nothing. */
class NothingLowering extends Pass {
  override def onInsts(insts: Seq[Inst]): Seq[Inst] = {
    val buf = mutable.UnrolledBuffer.empty[Inst]

    insts.foreach {
      case inst @ Inst.Let(n, call: Op.Call) if call.resty == Type.Nothing =>
        buf += super.onInst(inst)
        buf += Inst.Unreachable

      case inst =>
        buf += super.onInst(inst)
    }

    buf
  }

  override def onType(ty: Type): Type = ty match {
    case Type.Nothing =>
      Type.Ptr

    case Type.Function(params, Type.Nothing) =>
      Type.Function(params, Type.Void)

    case _ =>
      super.onType(ty)
  }
}

object NothingLowering extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new NothingLowering
}
