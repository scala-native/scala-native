package native
package compiler
package pass

import scala.collection.mutable
import native.nir._

/** Eliminates:
 *  - Val.Intrinsic
 */
trait IntrinsicLowering extends Pass {
  private val extras = mutable.UnrolledBuffer.empty[Defn.Declare]

  override def onPostCompilationUnit(defns: Seq[Defn]): Seq[Defn] =
    super.onPostCompilationUnit(defns ++ onScope(extras))

  override def onVal(value: Val) = super.onVal(value match {
    case Val.Intrinsic(Global.Atom(id), ptrty @ Type.Ptr(ty)) =>
      val n    = Global.Atom("nrt_" + id)
      val decl = Defn.Declare(Seq(), n, ty)
      extras += decl
      Val.Global(n, ptrty)
    case _ =>
      value
  })
}
