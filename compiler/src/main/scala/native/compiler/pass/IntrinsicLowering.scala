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

  private def nrt(g: Global) =
    Global(("nrt" +: g.parts): _*)

  override def preAssembly = { case defns =>
    defns ++ Intr.layout.toSeq.map {
      case (g, fields) =>
        Defn.Struct(Seq(), Global(("nrt" +: g.parts): _*), fields)
    }
  }

  override def postAssembly = { case defns =>
    defns ++ externs
  }

  override def postVal = {
    case Val.Global(g, ptrty @ Type.Ptr(ty)) if g.isIntrinsic =>
      val n = nrt(g)
      if (!added.contains(g)) {
        ty match {
          case _: Type.Function =>
            externs += Defn.Declare(Seq(), n, ty)
          case _ =>
            externs += Defn.Var(Seq(Attr.External), n, ty, Val.None)
        }
        added += g
      }
      Val.Global(n, ptrty)

    case Val.Struct(g, vals) if g.isIntrinsic =>
      Val.Struct(nrt(g), vals)
  }

  override def preType = {
    case ty @ Type.Struct(g) if g.isIntrinsic =>
      Type.Struct(nrt(g))
  }
}
