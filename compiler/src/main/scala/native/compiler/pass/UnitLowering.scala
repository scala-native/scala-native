package native
package compiler
package pass

import native.nir._
import native.util.unreachable

/** Eliminates returns of Unit values and replaces
 *  them with void.
 */
class UnitLowering extends Pass {

  override def preInst = {
    case Inst(n, op) if op.resty == Type.Unit =>
      Seq(
        Inst(op),
        Inst(n, Op.Copy(Val.Unit))
      )
  }

  override def preCf = {
    case Cf.Ret(v) if v.ty == Type.Unit =>
      Cf.Ret(Val.None)
  }

  override def preVal = {
    case Val.Unit =>
      Nrt.BoxedUnit_value
  }

  override def preType = {
    case Type.Unit =>
      Nrt.BoxedUnit

    case Type.Function(params, Type.Unit) =>
      Type.Function(params, Type.Void)
  }
}
