package native
package compiler
package pass

import scala.collection.mutable
import native.nir._
import native.compiler.analysis.ControlFlow
import native.util.unreachable

/** Eliminates:
 *  - Cf.Try
 */
class TryLowering extends Pass {
  override def preDefn = {
    case Defn.Define(_, _, _, blocks) =>
      val cfg = ControlFlow(blocks)
      ???
  }
}
