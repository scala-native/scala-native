package native
package compiler
package pass

import scala.collection.mutable
import native.nir._
import native.util.unreachable

/** Eliminates:
 *  - Type.Nothing
 */
trait NothingLowering extends Pass {
  override def onBlock(block: Block): Seq[Block] = {
    val Block(n, params, instrs) = block
    val ninstrs = mutable.UnrolledBuffer.empty[Instr]
    def result() = super.onBlock(Block(n, params, ninstrs.toSeq))
    instrs.foreach {
      case instr if instr.op.resty != Type.Nothing =>
        ninstrs += instr
      case Instr(_, attrs, call: Op.Call) if call.resty == Type.Nothing =>
        ninstrs += Instr(None, attrs, call)
        ninstrs += Instr(Op.Unreachable)
        return result()
      case instr @ Instr(_, _, termn: Op.Cf) =>
        ninstrs += instr
        return result()
    }
    unreachable
  }

  override def onType(ty: Type): Type = super.onType(ty match {
    case Type.Nothing => Type.Void
    case _            => ty
  })
}
