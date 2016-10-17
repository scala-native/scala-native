package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import compiler.analysis.ControlFlow
import util.unreachable
import nir._

/** Lowers throw terminator into calls to runtime's throw. */
class ThrowLowering(implicit fresh: Fresh) extends Pass {
  import ThrowLowering._

  override def preInst = {
    case Inst.Throw(v) =>
      Seq(
          Inst.Let(Op.Call(throwSig, throw_, Seq(v))),
          Inst.Unreachable
      )
  }
}

object ThrowLowering extends PassCompanion {
  def apply(ctx: Ctx) = new ThrowLowering()(ctx.fresh)

  val throwName = Global.Top("scalanative_throw")
  val throwSig  = Type.Function(Seq(Arg(Type.Ptr)), Type.Void)
  val throw_    = Val.Global(throwName, Type.Ptr)

  override val injects = Seq(Defn.Declare(Attrs.None, throwName, throwSig))
}
