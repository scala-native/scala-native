package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import nir._, Inst.Let
import analysis.ClassHierarchy.Top
import analysis.DominatorTree
import analysis.ControlFlow

/** Eliminates redundant box/unbox operations within
 *  a single method definition. This is quite simplistic approach
 *  but we need this to remove boxing around pointer operations
 *  that happen to have generic signatures.
 */
class GlobalBoxingElimination extends Pass {
  import GlobalBoxingElimination._

  override def onInsts(insts: Seq[Inst]): Seq[Inst] = {
    val records = mutable.UnrolledBuffer.empty[Record]

    // Setup the dominator tree checks
    val cfg             = ControlFlow.Graph(insts)
    val blockDomination = DominatorTree.build(cfg)

    val localToBlock =
      cfg.all.flatMap { block =>
        val params = block.params.map { local =>
          (local.name, block)
        }
        val insts = block.insts.collect {
          case Let(name, _) => (name, block)
        }
        params ++ insts
      }.toMap

    def isDominatedBy(dominated: Local, dominating: Local): Boolean = {
      val dominatedBlock  = localToBlock(dominated)
      val dominatingBlock = localToBlock(dominating)
      blockDomination(dominatedBlock).contains(dominatingBlock)
    }

    def canReuse(target: Local, reusedVal: Val): Boolean = {
      reusedVal match {
        case Val.Local(reusedLocal, _) => isDominatedBy(target, reusedLocal)
        case _                         => false
      }
    }

    // Original box elimination code
    insts.map {
      case inst @ Let(to, op @ Op.Box(ty, from)) =>
        records
          .collectFirst {
            // if a box for given value already exists, re-use the box
            case Box(rty, rfrom, rto)
                if rty == ty && from == rfrom && canReuse(to, rto) =>
              Let(to, Op.Copy(rto))

            // if we re-box previously unboxed value, re-use the original box
            case Unbox(rty, rfrom, rto)
                if rty == ty && from == rto && canReuse(to, rfrom) =>
              Let(to, Op.Copy(rfrom))
          }
          .getOrElse {
            // otherwise do actual boxing
            records += Box(ty, from, Val.Local(to, op.resty))
            inst
          }

      case inst @ Let(to, op @ Op.Unbox(ty, from)) =>
        records
          .collectFirst {
            // if we unbox previously boxed value, return original value
            case Box(rty, rfrom, rto)
                if rty == ty && from == rto && canReuse(to, rfrom) =>
              Let(to, Op.Copy(rfrom))

            // if an unbox for this value already exists, re-use unbox
            case Unbox(rty, rfrom, rto)
                if rty == ty && from == rfrom && canReuse(to, rto) =>
              Let(to, Op.Copy(rto))
          }
          .getOrElse {
            // otherwise do actual unboxing
            records += Unbox(ty, from, Val.Local(to, op.resty))
            inst
          }

      case inst =>
        inst
    }
  }
}

object GlobalBoxingElimination extends PassCompanion {
  private sealed abstract class Record
  private final case class Box(ty: Type, from: nir.Val, to: nir.Val)
      extends Record
  private final case class Unbox(ty: Type, from: nir.Val, to: nir.Val)
      extends Record

  override def apply(config: tools.Config, top: Top) =
    new GlobalBoxingElimination
}
