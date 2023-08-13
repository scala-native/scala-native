package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.util.unreachable
import scalanative.nir._
import scalanative.linker._

private[interflow] object MergePostProcessor {
  def postProcess(blocks: Seq[MergeBlock]): Seq[MergeBlock] = {
    lazy val blockIndices = blocks.zipWithIndex.toMap
    lazy val blockCyclesFinder = new BlockCycleFinder(blocks)

    blocks.foreach { block =>
      emitStackStateResetForCycles(
        block = block,
        blocks = blocks,
        blockIndices = blockIndices,
        cyclesFinder = blockCyclesFinder
      )
    }

    blocks
  }

  private def emitStackStateResetForCycles(
      block: MergeBlock,
      blocks: Seq[MergeBlock],
      blockIndices: => Map[MergeBlock, Int],
      cyclesFinder: => BlockCycleFinder
  ): Unit = {
    // Detect cycles involving stackalloc memory
    // Insert StackSave/StackRestore instructions at its first/last block
    val allocatesOnStackCache = mutable.Map.empty[MergeBlock, Boolean]
    def allocatesOnStack(block: MergeBlock) =
      allocatesOnStackCache.getOrElseUpdate(
        block,
        block.end.emit.exists {
          case Inst.Let(_, _: Op.Stackalloc, _) => true
          case _                                => false
        }
      )

    val shouldCheck = allocatesOnStack(block) &&
      cyclesFinder.canHaveCycles(block, blocks.head)
    if (shouldCheck) {
      val allocationEscapeCheck = new TrackStackallocEscape()
      def tryEmit(
          block: MergeBlock,
          innerCycle: BlocksCycle,
          innerCycleStart: Option[MergeBlock]
      ): Unit = {
        cyclesFinder
          .cyclesOf(block)
          .filter { cycle =>
            val isDirectLoop = innerCycle.isEmpty
            def isEnclosingLoop = !cyclesFinder.isRotationOf(innerCycle, cycle)
            isDirectLoop || // 1st run
              isEnclosingLoop // 2nd run
          }
          .foreach { cycle =>
            val startIdx = cycle.map(blockIndices(_)).min
            val start = blocks(startIdx)
            val startName = start.label.name
            val end = cycle((cycle.indexOf(start) + 1) % cycle.size)
            assert(
              end.outgoing.contains(start.label.name),
              "Invalid cycle, last block does not point to cycle start"
            )

            def canEscapeAlloc = allocationEscapeCheck(
              allocatingBlock = block,
              entryBlock = start,
              cycle = cycle
            )
            // If memory escapes current loop we cannot create stack stage guards
            // Instead try to insert guard in outer loop
            if (!canEscapeAlloc || innerCycleStart.exists(cycle.contains)) {
              start.emitStackSaveOp = true
              end.emitStackRestoreFor ::= startName
            } else if (innerCycleStart.isEmpty) {
              // If allocation escapes direct loop try to create state restore in outer loop
              // Outer loop is a while loop which does not perform stack allocation, but is a cycle
              // containing entry to inner loop
              tryEmit(
                start,
                innerCycle = cycle,
                innerCycleStart = Some(start)
              )
            }
          }
      }
      tryEmit(block, innerCycle = Nil, innerCycleStart = None)
    }
  }

  private type BlocksCycle = List[MergeBlock]
  // NIR traversal used to check if stackallocated memory might escape the cycle
  // meaning it might be referenced in next loop runs
  private class TrackStackallocEscape() extends nir.Traverse {
    private var tracked = mutable.Set.empty[Local]
    private var curInst: Inst = _

    // thread-unsafe
    def apply(
        allocatingBlock: MergeBlock,
        entryBlock: MergeBlock,
        cycle: Seq[MergeBlock]
    ): Boolean = {
      val loopStateVals = mutable.Set.empty[Local]
      entryBlock.phis.foreach {
        case MergePhi(_, values) =>
          values.foreach {
            case (_, v: Val.Local) =>
              if (Type.isPtrType(v.ty)) loopStateVals += v.name
            case _ => ()
          }
      }
      if (loopStateVals.isEmpty) false
      else {
        tracked.clear()
        def visit(blocks: Seq[MergeBlock]) =
          blocks.foreach(_.end.emit.foreach(onInst))
        cycle.view
          .dropWhile(_ ne allocatingBlock)
          .takeWhile(_ ne entryBlock)
          .foreach(_.end.emit.foreach(onInst))
        tracked.intersect(loopStateVals).nonEmpty
      }
    }

    override def onInst(inst: Inst): Unit = {
      curInst = inst
      inst match {
        case Inst.Let(name, _: Op.Stackalloc, _) => tracked += name
        case _                                   => ()
      }
      super.onInst(inst)
    }

    override def onVal(value: Val): Unit = value match {
      case Val.Local(valName, _) =>
        curInst match {
          case Inst.Let(instName, op, _) if Type.isPtrType(op.resty) =>
            if (tracked.contains(valName)) tracked += instName
          case _ => ()
        }
      case _ => ()
    }
  }

  private class BlockCycleFinder(blocks: Seq[MergeBlock]) {
    def isRotationOf(expected: BlocksCycle, rotation: BlocksCycle): Boolean = {
      if (expected.size != rotation.size) false
      else {
        val concat = expected ::: expected
        concat.containsSlice(rotation)
      }
    }

    private val blocksById = blocks.map(b => b.label.name -> b).toMap
    private val canHaveCyclesCache = mutable.Map.empty[MergeBlock, Boolean]
    private def canHaveCyclesImpl(
        block: MergeBlock,
        entryBlock: MergeBlock
    ): Boolean = {
      if (block eq entryBlock) false
      else if (block.incoming.size > 1) true
      else canHaveCycles(blocksById(block.incoming.head._1), entryBlock)
    }
    def canHaveCycles(block: MergeBlock, entryBlock: MergeBlock): Boolean =
      canHaveCyclesCache.getOrElseUpdate(
        block,
        canHaveCyclesImpl(block, entryBlock)
      )

    private val cyclesOfCache = mutable.Map.empty[MergeBlock, List[BlocksCycle]]
    private def cyclesOfImpl(block: MergeBlock) = {
      val cycles = mutable.ListBuffer.empty[BlocksCycle]
      def shortestPath(
          from: MergeBlock,
          to: MergeBlock
      ): Option[BlocksCycle] = {
        val visited = mutable.Set.empty[MergeBlock]
        def loop(queue: List[(MergeBlock, BlocksCycle)]): Option[BlocksCycle] =
          queue match {
            case Nil               => None
            case (`to`, path) :: _ => Some(path)
            case (current, path) :: tail =>
              if (visited.contains(current)) loop(tail)
              else {
                visited.add(current)
                val todo = current.outgoing.map {
                  case (_, node) => (node, node :: path)
                }.toList
                loop(todo ::: tail)
              }
          }
        loop((from, from :: Nil) :: Nil)
      }

      block.outgoing
        .foreach {
          case (_, next) =>
            shortestPath(next, block).foreach { cycle =>
              def isDuplciate = cycles.exists(isRotationOf(_, cycle))
              if (cycle.contains(block) && !isDuplciate)
                cycles += cycle
            }
        }
      cycles.toList
    }

    def cyclesOf(block: MergeBlock): List[BlocksCycle] =
      cyclesOfCache.getOrElseUpdate(block, cyclesOfImpl(block))
  }
}
