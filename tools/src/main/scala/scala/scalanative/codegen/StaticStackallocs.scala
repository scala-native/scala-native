package scala.scalanative
package codegen

import scala.collection.mutable

import scalanative.interflow.Interflow.LLVMIntrinsics
import scalanative.nir.ControlFlow.{Block, Graph}

/** Fixed-size stack allocations placed outside of the entry block of a
 *  function, which can be allocated in its entry block instead.
 *
 *  LLVM treats an alloca outside of the entry block as a dynamic one: it is
 *  never promoted to registers and it forces the frame to be addressed (and the
 *  stack pointer to be restored) through the frame pointer. Such frames do not
 *  survive the relocation of the stack done when resuming a continuation (see
 *  delimcc.c): the frame pointers saved by the callees and the stack pointers
 *  saved by `llvm.stacksave` are not rebased.
 *
 *  An allocation is hoisted when a single slot is enough for all of its
 *  executions:
 *    - it is not a part of a CFG cycle, so it is executed at most once, or
 *    - every two of its executions are separated by a `llvm.stackrestore`
 *      releasing the memory of the previous one. Interflow emits such stack
 *      guards around the loops which allocate memory not escaping a single
 *      iteration (see interflow.MergePostProcessor)
 *
 *  @param hoisted
 *    allocations to be done in the entry block
 *  @param dropStackRestores
 *    hoisting leaves the function without any dynamic stack allocation, there
 *    is no stack state to restore
 */
private[codegen] final class StaticStackallocs(
    val hoisted: Seq[nir.Inst.Let],
    val dropStackRestores: Boolean
) {
  private val hoistedIds = hoisted.map(_.id).toSet
  def isHoisted(id: nir.Local): Boolean = hoistedIds.contains(id)
}

private[codegen] object StaticStackallocs {
  private val None = new StaticStackallocs(Nil, dropStackRestores = false)

  def apply(cfg: Graph): StaticStackallocs = {
    // Stack allocations outside of the entry block with their positions
    val dynamic = mutable.ArrayBuffer.empty[(Block, Int, nir.Inst.Let)]
    cfg.all.foreach { block =>
      if (!block.isEntry) {
        var idx = 0
        block.insts.foreach { inst =>
          inst match {
            case inst @ nir.Inst.Let(_, _: nir.Op.Stackalloc, _) =>
              dynamic += ((block, idx, inst))
            case _ => ()
          }
          idx += 1
        }
      }
    }
    if (dynamic.isEmpty) None
    else {
      val guards = new StackGuards(cfg)
      val hoisted = dynamic.collect {
        case (block, idx, inst @ nir.Inst.Let(_, nir.Op.Stackalloc(_, size), _))
            if isFixedSize(size) && idx >= guards.releasedFrom(block) =>
          inst
      }.toSeq
      new StaticStackallocs(
        hoisted,
        dropStackRestores = hoisted.size == dynamic.size
      )
    }
  }

  private def isFixedSize(size: nir.Val): Boolean = size match {
    case _: nir.Val.Size | _: nir.Val.Int | _: nir.Val.Long | _: nir.Val.Short |
        _: nir.Val.Byte =>
      true
    case _ => false
  }

  private class StackGuards(cfg: Graph) {
    // Saved stack state -> block and position of the save
    private val saves = mutable.Map.empty[nir.Local, (Block, Int)]
    // Saved stack state -> blocks restoring it
    private val restores = mutable.Map.empty[nir.Local, List[Block]]
    cfg.all.foreach { block =>
      var idx = 0
      block.insts.foreach { inst =>
        inst match {
          case nir.Inst.Let(
                id,
                nir.Op.Call(_, LLVMIntrinsics.StackSave, _),
                _
              ) =>
            saves(id) = (block, idx)
          case nir.Inst.Let(
                _,
                nir.Op.Call(
                  _,
                  LLVMIntrinsics.StackRestore,
                  Seq(nir.Val.Local(state, _))
                ),
                _
              ) =>
            restores(state) = block :: restores.getOrElse(state, Nil)
          case _ => ()
        }
        idx += 1
      }
    }

    private val releasedFromCache = mutable.Map.empty[nir.Local, Int]

    /** Position in the block, starting from which the memory of a stack
     *  allocation is always released before its next execution.
     */
    def releasedFrom(block: Block): Int =
      releasedFromCache.getOrElseUpdate(block.id, releasedFromImpl(block))

    private def releasedFromImpl(block: Block): Int =
      if (!reachableFrom(block, Nil).contains(block.id)) 0 // not in a cycle
      else {
        var position = Int.MaxValue
        restores.foreach {
          case (state, restoringBlocks) =>
            saves.get(state).foreach {
              case (savingBlock, saveIdx) =>
                val isRestoring = restoringBlocks.exists(_ eq block)
                val reachable = reachableFrom(block, restoringBlocks)
                // Stack is always restored before the next allocation
                val alwaysRestores =
                  isRestoring || !reachable.contains(block.id)
                if (alwaysRestores) {
                  // Saved state has to exclude the allocation
                  if (savingBlock eq block) {
                    if (!isRestoring) position = position.min(saveIdx + 1)
                  } else if (!reachable.contains(savingBlock.id)) position = 0
                }
            }
        }
        position
      }

    // Blocks reachable from the given one, not following the edges going out
    // of the barrier blocks
    private def reachableFrom(
        block: Block,
        barriers: List[Block]
    ): mutable.Set[nir.Local] = {
      val visited = mutable.Set.empty[nir.Local]
      val todo = mutable.Stack.empty[Block]
      def visitSuccessors(block: Block) = block.outEdges.foreach(todo push _.to)
      visitSuccessors(block)
      while (todo.nonEmpty) {
        val next = todo.pop()
        if (visited.add(next.id) && !barriers.exists(_ eq next))
          visitSuccessors(next)
      }
      visited
    }
  }
}
