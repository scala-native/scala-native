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
    val Block(n, params, insts) = block
    val ninsts = mutable.UnrolledBuffer.empty[Inst]
    def result() = super.onBlock(Block(n, params, ninsts.toSeq))
    insts.foreach {
      case inst if inst.op.resty != Type.Nothing =>
        ninsts += inst
      case Inst(_, attrs, call: Op.Call) if call.resty == Type.Nothing =>
        ninsts += Inst(None, attrs, call)
        ninsts += Inst(Op.Unreachable)
        return result()
      case inst @ Inst(_, _, termn: Op.Cf) =>
        ninsts += inst
        return result()
    }
    unreachable
  }

  override def onType(ty: Type): Type = super.onType(ty match {
    case Type.Nothing => Type.Void
    case _            => ty
  })
}
