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
    case defn @ Defn.Define(_, _, _, blocks) =>
      val cfg = ControlFlow(blocks)
      var stack = List.empty[Next.Fail]
      cfg.map { node =>
        println(s"${node.block.name} ${stack}")
        node.block.cf match {
          case Cf.Try(succ, fail: Next.Fail) =>
            stack = fail :: stack
          case _ =>
            ()
        }
        ()
      }
      Seq(defn)
  }
}
