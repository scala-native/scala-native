package native
package compiler
package pass

import scala.collection.mutable
import native.nir._
import native.util.unreachable

/** Eliminates:
 *  - Op.{Throw, Try}
 */
class ExceptionLowering extends Pass {
  override def preBlock = { case Block(n, params, init :+ last) =>
    val nlast = last.op match {
      case Op.Try(n1, n2) =>
        Seq(Inst(Op.Jump(n1)))
      case Op.Throw(v) =>
        Seq(Inst(Intr.call(Intr.throw_, v)),
            Inst(Op.Unreachable))
      case _ =>
        Seq(last)
    }

    Seq(Block(n, params, init ++ nlast))
  }
}
