package native
package compiler
package pass

import scala.collection.mutable
import native.nir._
import native.util.ScopedVar, ScopedVar.scoped

/** Eliminates:
 *  - Op.Copy
 */
class CopyLowering extends Pass {
  private var locals: mutable.Map[Local, Val] = _

  private def collect(blocks: Seq[Block]): mutable.Map[Local, Val] = {
    val copies = mutable.Map.empty[Local, Val]

    blocks.foreach { b =>
      b.insts.foreach {
        case Inst(n, Op.Copy(v)) if n.nonEmpty =>
          copies(n) = v
        case inst =>
          ()
      }
    }

    copies
  }

  override def preDefn = {
    case defn: Defn.Define =>
      locals = collect(defn.blocks)
      Seq(defn)
  }

  override def preInst = {
    case Inst(_, _: Op.Copy) =>
      Seq()
  }

  override def preVal = {
    case Val.Local(loc, _) if locals.contains(loc) =>
      locals(loc)
  }
}
