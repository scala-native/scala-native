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
  override def preBlock = {
    case block @ Block(_, _, _, Cf.Try(n1, n2)) =>
      Seq(block.copy(cf = Cf.Jump(n1)))
    case block @ Block(_, _, insts, Cf.Throw(v)) =>
      Seq(block.copy(insts = insts :+ Inst(Intr.call(Intr.throw_, v)),
                     cf    = Cf.Unreachable))
  }
}
