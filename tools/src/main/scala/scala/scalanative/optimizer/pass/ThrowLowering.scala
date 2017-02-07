package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import analysis.ClassHierarchy.Top
import analysis.ControlFlow
import util.unreachable
import nir._

/** Lowers throw terminator into calls to runtime's throw. */
class ThrowLowering(implicit fresh: Fresh) extends Pass {
  import ThrowLowering._

  override def preInst = {
    case Inst.Let(_, Op.Throw(v, unwind)) =>
      Seq(
        Inst.Let(Op.Call(throwSig, throw_, Seq(v), unwind))
      )
  }
}

object ThrowLowering extends PassCompanion {
  val throwName = Global.Top("scalanative_throw")
  val throwSig  = Type.Function(Seq(Type.Ptr), Type.Void)
  val throw_    = Val.Global(throwName, Type.Ptr)

  override val injects =
    Seq(Defn.Declare(Attrs.None, throwName, throwSig))

  override def apply(config: tools.Config, top: Top) =
    new ThrowLowering()(top.fresh)
}
