package native
package compiler
package pass

import native.nir._
import native.util.unreachable

/** Eliminates:
 *  - Val.Unit
 *  - Type.Unit
 */
class UnitLowering extends Pass {
  override def preInst = {
    case Inst(n, op) if n != Local.None && op.resty == Type.Unit =>
      Seq(Inst(Local.None, op))
    case Inst(_, Op.Ret(v)) if v.ty == Type.Unit =>
      Seq(Inst(Local.None, Op.Ret(Val.None)))
  }

  override def preNext = { case Next(label, args) =>
    Next(label, args.filter(_.ty != Type.Unit))
  }

  override def preBlock = { case Block(n, params, insts) =>
    Seq(Block(n, params.filter(_.ty != Type.Unit), insts))
  }

  override def preType = {
    case Type.Unit =>
      unreachable
    case Type.Function(params, Type.Unit) =>
      Type.Function(params, Type.Void)
  }
}
