package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import scala.util.control.Breaks._
import util.unsupported
import nir._

/** Eliminates:
 *  - Type.Nothing
 */
class NothingLowering extends Pass {
  override def preBlock = {
    case Block(n, params, insts, cf) =>
      val ninsts = mutable.UnrolledBuffer.empty[Inst]
      var ncf = cf

      breakable {
        insts.foreach {
          case inst @ Inst(n, call: Op.Call) if call.resty == Type.Nothing =>
            ninsts += inst
            ncf = Cf.Unreachable
            break
          case inst if inst.op.resty == Type.Nothing =>
            unsupported("only calls can return nothing")
          case inst =>
            ninsts += inst
        }
      }

      Seq(Block(n, params, ninsts.toSeq, ncf))
  }

  override def preType = {
    case Type.Nothing =>
      Type.Ptr

    case Type.Function(params, Type.Nothing) =>
      Type.Function(params, Type.Void)
  }
}

object NothingLowering extends PassCompanion {
  def apply(ctx: Ctx) = new NothingLowering
}
