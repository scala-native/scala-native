package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import util.ScopedVar, ScopedVar.scoped
import nir._

/** Eliminates:
 *  - Op.Copy
 */
class CopyPropagation extends Pass {
  private var locals: mutable.Map[Local, Val] = _

  private def collect(blocks: Seq[Block]): mutable.Map[Local, Val] = {
    val copies = mutable.Map.empty[Local, Val]

    blocks.foreach { b =>
      b.insts.foreach {
        case Inst(n, Op.Copy(v)) =>
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

object CopyPropagation extends PassCompanion {
  def apply(ctx: Ctx) = new CopyPropagation
}
