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
  override def preInst = {
    case inst @ Inst.Let(n, call: Op.Call) if call.resty == Type.Nothing =>
      Seq(inst, Inst.Unreachable)
  }

  override def preType = {
    case Type.Nothing =>
      Type.Ptr

    case Type.Function(params, Type.Nothing) =>
      Type.Function(params, Type.Void)
  }
}

object NothingLowering extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new NothingLowering
}
