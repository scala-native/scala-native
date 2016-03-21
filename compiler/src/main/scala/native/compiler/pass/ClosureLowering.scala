package native
package compiler
package pass

import scala.collection.mutable
import native.nir._
import native.util.ScopedVar, ScopedVar.scoped

/** Eliminates:
 *  - Op.Closure
 */
class ClosureLowering extends Pass {
  override def preInst = {
    case Inst(_, _: Op.Closure) =>
      ???
  }
}
