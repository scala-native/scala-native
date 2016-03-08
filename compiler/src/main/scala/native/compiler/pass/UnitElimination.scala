package native
package compiler
package pass

import native.nir._
import native.util.unreachable

/** Eliminates returns of Unit values and replaces
 *  them with void.
 */
class UnitElimination extends Pass {
  override def preInst = {
    case Inst(n, op) if op.resty == Nrt.Unit =>
      Seq(
        Inst(op),
        Inst(n, Op.Copy(Nrt.Unit_value))
      )
  }

  override def preCf = {
    case Cf.Ret(v) if v.ty == Nrt.Unit =>
      Cf.Ret(Val.None)
  }

  override def preType = {
    case Type.Function(params, Nrt.Unit) =>
      Type.Function(params, Type.Void)
  }
}
