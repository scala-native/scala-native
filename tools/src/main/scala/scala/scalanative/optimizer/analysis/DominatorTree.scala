package scala.scalanative
package optimizer
package analysis

import scala.collection.immutable
import scala.collection.mutable
import ControlFlow.Block

object DominatorTree {

  /** Fixpoint-based method to build the dominator tree
   *  from the CFG. The dominator tree is simply represented
   *  as a Map from a CFG block to the set of blocks dominating
   *  this block.
   */
  def build(cfg: ControlFlow.Graph): Map[Block, Set[Block]] = {
    val domination = mutable.HashMap.empty[Block, Set[Block]]
    var workList   = immutable.Queue(cfg.entry)

    while (workList.nonEmpty) {
      val (block, dequeued) = workList.dequeue
      workList = dequeued.filterNot(_ == block) // remove duplicates

      val visitedPreds = block.pred.filter(domination.contains)
      val predDomination =
        visitedPreds.toList.map(pred => domination.getOrElse(pred, Set.empty))
      val correctPredDomination = predDomination.filterNot(
        predDominators =>
          predDominators
            .contains(block)) // removes the potential dominators that are dominated by this block (counters loop problems)

      val blockDomination =
        if (correctPredDomination.isEmpty)
          Set.empty[Block]
        else
          correctPredDomination.tail.foldLeft(correctPredDomination.head)(
            (a, b) => a.intersect(b))

      val oldDomination = domination.getOrElse(block, Set.empty)
      val newDomination = blockDomination + block // a block dominates itself

      if (oldDomination != newDomination) {
        domination += (block -> newDomination)
        workList ++= block.succ
      }
    }

    domination.toMap
  }

}
