package native
package compiler
package pass

import scala.collection.mutable
import native.nir._
import native.util.ScopedVar, ScopedVar.scoped

/** Eliminates:
 *  - Op.Copy
 */
trait CopyLowering extends Pass {
  private val locals = new ScopedVar[mutable.Map[Local, Val]]

  private def collect(blocks: Seq[Block]): mutable.Map[Local, Val] = {
    val copies = mutable.Map.empty[Local, Val]

    blocks.foreach { b =>
      b.insts.foreach {
        case Inst(Some(n), Op.Copy(v)) =>
          copies(n) = v
        case inst =>
          ()
      }
    }

    copies
  }

  override def onBlocks(blocks: Seq[Block]): Seq[Block] =
    scoped (
      locals := collect(blocks)
    ) {
      super.onBlocks(blocks)
    }

  override def onInst(inst: Inst): Seq[Inst] = inst match {
    case Inst(_, _: Op.Copy) =>
      Seq()
    case _ =>
      super.onInst(inst)
  }

  override def onVal(value: Val): Val = super.onVal(value match {
    case Val.Local(loc, _) if locals.contains(loc) =>
      locals(loc)
    case _ =>
      value
  })
}
