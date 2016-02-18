package native
package compiler
package pass

import scala.collection.mutable
import native.nir._

/** Re-wires intrinsic references to corresponding runtime functions and values.
 */
trait IntrinsicLowering extends Pass {
  private val added   = mutable.Set.empty[Global]
  private val externs = mutable.UnrolledBuffer.empty[Defn]

  override def onPostCompilationUnit(defns: Seq[Defn]): Seq[Defn] = {
    val structs = Intr.structs.toSeq.map {
      case (g, fields) => Defn.Struct(Seq(), Global("nrt_" + g.parts.mkString("_")), fields)
    }
    val extras = structs ++ externs
    super.onPostCompilationUnit(onScope(extras) ++ defns)
  }

  private def linkify(g: Global) =
    Global("nrt_" + g.parts.mkString("_"))

  override def onVal(value: Val) = super.onVal(value match {
    case Val.Global(g, ptrty @ Type.Ptr(ty)) if g.isIntrinsic =>
      val n = linkify(g)
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
      Val.Struct(linkify(g), vals)
    case _ =>
      value
  })

  override def onType(ty: Type) = super.onType(ty match {
    case Type.Struct(g) if g.isIntrinsic =>
      Type.Struct(linkify(g))
    case _ =>
      ty
  })
}
