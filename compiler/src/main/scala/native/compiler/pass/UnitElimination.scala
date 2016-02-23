package native
package compiler
package pass

import native.nir._
import native.util.unreachable

/** Eliminates returns of unit values and replaces
 *  them with void.
 */
class UnitElimination extends Pass {
  override def preInst = {
    case Inst(n, op) if op.resty == Intr.unit =>
      Seq(
        Inst(op),
        Inst(n, Op.Copy(Intr.unit_value))
      )
  }

  override def preCf = {
    case Cf.Ret(v) if v.ty == Intr.unit =>
      Cf.Ret(Val.None)
  }

  override def preType = {
    case Type.Function(params, Intr.unit) =>
      Type.Function(params, Type.Void)
  }
}
