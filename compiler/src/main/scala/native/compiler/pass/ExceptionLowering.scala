package native
package compiler
package pass

import scala.collection.mutable
import native.nir._
import native.util.unreachable

/** Eliminates:
 *  - Op.{Throw, Try}
 */
trait ExceptionLowering extends Pass {
  override def onBlock(block: Block): Seq[Block] = {
    val Block(n, params, init :+ last) = block
    val nlast = last.op match {
      case Op.Try(n1, n2) =>
        Seq(Instr(Op.Jump(n1)))
      case Op.Throw(v) =>
        Seq(Instr(Intrinsic.call(Intrinsic.throw_, v)),
            Instr(Op.Unreachable))
      case _ =>
        Seq(last)
    }

    super.onBlock(Block(n, params, init ++ nlast))
  }
}
