package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import analysis.ClassHierarchy.Top
import nir._

/** Maps const values to top-level deduplicated constants.
 */
class ConstLowering extends Pass {
  private val consts   = mutable.UnrolledBuffer.empty[Val]
  private val constfor = mutable.Map.empty[Val, Int]
  private def constName(idx: Int): Global =
    Global.Member(Global.Top("__const"), " " + this.## + "." + idx.toString)
  private def constFor(v: Val): Int =
    if (constfor.contains(v)) {
      constfor(v)
    } else {
      consts += v
      constfor(v) = consts.length - 1
      consts.length - 1
    }

  override def onDefns(defns: Seq[Defn]): Seq[Defn] =
    super.onDefns(defns) ++ consts.zipWithIndex.map {
      case (v, idx) =>
        Defn.Const(Attrs.None, constName(idx), v.ty, v)
    }

  override def onVal(value: Val): Val = value match {
    case Val.Const(v) =>
      Val.Global(constName(constFor(onVal(v))), Type.Ptr)
    case _ =>
      super.onVal(value)
  }
}

object ConstLowering extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    new ConstLowering
}
