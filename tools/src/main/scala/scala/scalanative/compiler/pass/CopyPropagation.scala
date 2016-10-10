package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import scala.annotation.tailrec
import util.ScopedVar, ScopedVar.scoped
import nir._

/** Propagates all copies down the use chain.
 *
 *  For example:
 *
 *      %foo = copy 1i32
 *      %bar = copy %foo
 *      %baz = iadd[i32] %bar, 2i32
 *
 *  Becomes:
 *
 *      %baz = iadd[i32] 1i32, 2i32
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
    case value =>
      @tailrec
      def loop(value: Val): Val =
        value match {
          case Val.Local(local, _) if locals.contains(local) =>
            loop(locals(local))

          case value =>
            value
        }

      loop(value)
  }
}

object CopyPropagation extends PassCompanion {
  def apply(ctx: Ctx) = new CopyPropagation
}
