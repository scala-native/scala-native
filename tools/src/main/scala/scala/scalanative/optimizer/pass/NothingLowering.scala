package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import scala.util.control.Breaks._
import analysis.ClassHierarchy.Top
import util.unsupported
import nir._

/** Short-circuits method calls that return nothing. */
class NothingLowering(implicit fresh: Fresh) extends Pass {
  import NothingLowering._

  override def onInsts(insts: Seq[Inst]): Seq[Inst] = {
    val buf = new nir.Buffer

    insts.foreach {
      case Inst.Throw(v, unwind) =>
        buf += super.onInst(
          Inst.Let(Op.Call(throwSig, throw_, Seq(v), unwind)))
        buf.unreachable

      case inst @ Inst.Let(n, op) if op.resty == Type.Nothing =>
        buf += super.onInst(inst)
        buf.unreachable

      case inst =>
        buf += super.onInst(inst)
    }

    buf.toSeq
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
  val throwName = Global.Top("scalanative_throw")
  val throwSig  = Type.Function(Seq(Type.Ptr), Type.Void)
  val throw_    = Val.Global(throwName, Type.Ptr)

  override val injects =
    Seq(Defn.Declare(Attrs.None, throwName, throwSig))

  override def apply(config: tools.Config, top: Top) =
    new NothingLowering()(top.fresh)
}
