package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import nir._

/** Eliminates:
 *  - Val.Const
 */
class ConstLowering extends Pass {
  private val consts = mutable.UnrolledBuffer.empty[Val]
  private def constName(idx: Int): Global =
    Global.Val("__const." + idx.toString)
  private def constFor(v: Val): Int =
    if (consts.contains(v)) consts.indexOf(v)
    else {
      consts += v
      consts.length - 1
    }

  override def postAssembly = {
    case defns =>
      defns ++ consts.zipWithIndex.map {
        case (v, idx) =>
          Defn.Const(Attrs.None, constName(idx), v.ty, v)
      }
  }

  override def preVal = {
    case Val.Const(v) =>
      Val.Global(constName(constFor(v)), Type.Ptr)
  }
}
