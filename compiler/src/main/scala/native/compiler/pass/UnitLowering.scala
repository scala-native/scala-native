package native
package compiler
package pass

import native.nir._

/** Eliminates unit type and unit value.
 *
 *  Eliminates:
 *  - Val.Unit
 *  - Type.Unit
 */
trait UnitLowering extends Pass {
  override def onInstr(instr: Instr): Seq[Instr] = instr match {
    case Instr(Some(_), attrs, op) if op.resty == Type.Unit =>
      onInstr(Instr(None, attrs, op))
    case Instr(_, attrs, Op.Ret(v)) if v.ty == Type.Unit =>
      onInstr(Instr(None, attrs, Op.Ret(Val.None)))
    case _ =>
      super.onInstr(instr)
  }

  override def onNext(n: Next) = super.onNext {
    val Next(label, args) = n
    Next(label, args.filter(_ != Val.Unit))
  }

  override def onBlock(b: Block) = super.onBlock {
    val Block(n, params, instrs) = b
    Block(n, params.filter(_.ty != Type.Unit), instrs)
  }

  override def onType(ty: Type) = super.onType(ty match {
    case Type.Unit => Type.Void
    case ty        => ty
  })
}
