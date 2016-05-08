package scala.scalanative
package compiler
package pass

import nir._

/** Eliminates:
  * - Op.As
  */
class AsLowering extends Pass {
  override def preInst = {
    case Inst(n, Op.As(_, v)) =>
      Seq(Inst(n, Op.Copy(v)))
  }
}
