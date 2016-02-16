package native
package compiler
package pass

import native.nir._
import native.util.unreachable

/** Eliminates:
 *  - Val.Unit
 *  - Type.Unit
 */
trait UnitLowering extends Pass {
  override def onInst(inst: Inst): Seq[Inst] = inst match {
    case Inst(Some(_), op) if op.resty == Type.Unit =>
      onInst(Inst(None, op))
    case Inst(_, Op.Ret(v)) if v.ty == Type.Unit =>
      onInst(Inst(None, Op.Ret(Val.None)))
    case _ =>
      super.onInst(inst)
  }

  override def onNext(n: Next) = super.onNext {
    val Next(label, args) = n
    Next(label, args.filter(_.ty != Type.Unit))
  }

  override def onBlock(b: Block) = super.onBlock {
    val Block(n, params, insts) = b
    Block(n, params.filter(_.ty != Type.Unit), insts)
  }

  override def onVal(value: Val) = value match {
    case Val.Unit => unreachable
    case _        => super.onVal(value)
  }

  override def onType(ty: Type) = ty match {
    case Type.Unit =>
      unreachable
    case Type.Function(params, Type.Unit) =>
      Type.Function(params.map(onType), Type.Void)
    case _ =>
      super.onType(ty)
  }
}
