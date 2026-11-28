package scala.scalanative
package interflow

import scala.collection.mutable

/** Liveness of the locals of a method: the locals that may still be read once
 *  the control reaches the given block.
 */
private[interflow] object Liveness {

  def liveIn(insts: Array[nir.Inst]): Map[nir.Local, Set[nir.Local]] = {
    final class Block(val id: nir.Local) extends nir.Traverse {
      val defs = mutable.Set.empty[nir.Local]
      // Read before being defined in the block
      val uses = mutable.Set.empty[nir.Local]
      val succ = mutable.Set.empty[nir.Local]
      var liveIn = Set.empty[nir.Local]

      override def onVal(value: nir.Val): Unit = value match {
        case nir.Val.Local(id, _) => if (!defs.contains(id)) uses += id
        case _                    => super.onVal(value)
      }
      override def onNext(next: nir.Next): Unit = {
        next match {
          case nir.Next.Label(id, _)                    => succ += id
          case nir.Next.Unwind(nir.Val.Local(id, _), _) => defs += id
          case _                                        => ()
        }
        super.onNext(next)
      }
      override def onInst(inst: nir.Inst): Unit = inst match {
        case nir.Inst.Label(_, params) =>
          params.foreach(defs += _.id)
        case nir.Inst.Let(id, _, _) =>
          super.onInst(inst)
          defs += id
        case nir.Inst.LinktimeIf(_, thenp, elsep) =>
          onNext(thenp)
          onNext(elsep)
        case _ =>
          super.onInst(inst)
      }
    }

    val blocks = mutable.LinkedHashMap.empty[nir.Local, Block]
    var current: Block = null
    insts.foreach { inst =>
      inst match {
        case nir.Inst.Label(id, _) =>
          current = new Block(id)
          blocks(id) = current
        case _ => ()
      }
      current.onInst(inst)
    }

    val order = blocks.values.toArray.reverse
    var changed = true
    while (changed) {
      changed = false
      order.foreach { block =>
        var live = block.uses.toSet
        block.succ.foreach { id =>
          blocks.get(id).foreach { succ =>
            succ.liveIn.foreach { local =>
              if (!block.defs.contains(local)) live += local
            }
          }
        }
        if (live.size != block.liveIn.size) {
          block.liveIn = live
          changed = true
        }
      }
    }
    blocks.iterator.map { case (id, block) => id -> block.liveIn }.toMap
  }
}
