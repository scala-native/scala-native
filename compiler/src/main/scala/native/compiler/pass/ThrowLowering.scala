package native
package compiler
package pass

import scala.collection.mutable
import native.nir._
import native.compiler.analysis.ControlFlow
import native.util.unreachable

/** Eliminates:
 *  - Cf.Throw
 */
class ThrowLowering extends Pass {
  override def preBlock = {
    case block @ Block(_, _, insts, Cf.Throw(v)) =>
      Seq(block.copy(insts = insts :+ Inst(Intr.call(Intr.throw_, v)),
                     cf    = Cf.Unreachable))
  }
}
