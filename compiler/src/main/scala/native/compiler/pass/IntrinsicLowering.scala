package native
package compiler
package pass

import scala.collection.mutable
import native.nir._

/** Re-wires intrinsic references to corresponding runtime functions and values.
 */
class IntrinsicLowering extends Pass {
  private val added   = mutable.Set.empty[Global]
  private val externs = mutable.UnrolledBuffer.empty[Defn]

  override def preAssembly = { case defns =>
    defns ++ Nrt.layouts.toSeq.map {
      case (g, fields) =>
        Defn.Struct(Seq(), g, fields)
    }
  }

  override def postAssembly = { case defns =>
    defns ++ externs
  }

  override def postVal = {
    case value @ Val.Global(g, ptrty @ Type.Ptr(ty)) if g.isIntrinsic =>
      if (!added.contains(g)) {
        ty match {
          case _: Type.Function =>
            externs += Defn.Declare(Seq(), g, ty)
          case _ =>
            externs += Defn.Var(Seq(Attr.External), g, ty, Val.None)
        }
        added += g
      }
      value
  }
}
