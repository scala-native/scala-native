package scala.scalanative
package interflow

import scalanative.nir._
import scalanative.linker._
import scala.collection.mutable
import scala.annotation.tailrec

trait Opt { self: Interflow =>

  def shallOpt(name: Global): Boolean = {
    val defn =
      getOriginal(originalName(name))
    val noUnwind = defn.insts.forall {
      case Inst.Let(_, _, unwind)   => unwind == Next.None
      case Inst.Throw(_, unwind)    => unwind == Next.None
      case Inst.Unreachable(unwind) => unwind == Next.None
      case _                        => true
    }

    defn.attrs.opt != Attr.NoOpt && noUnwind
  }

  def opt(name: Global): Defn.Define = in(s"visit ${name.show}") {
    val orig = originalName(name)
    val origtys = argumentTypes(orig)
    val origdefn = getOriginal(orig)
    val argtys = argumentTypes(name)
    implicit val pos = origdefn.pos
    // Wrap up the result.
    def result(retty: Type, rawInsts: Seq[Inst]) =
      origdefn.copy(
        name = name,
        attrs = origdefn.attrs.copy(opt = Attr.DidOpt),
        ty = Type.Function(argtys, retty),
        insts = ControlFlow.removeDeadBlocks(rawInsts)
      )(origdefn.pos)

    // Create new fresh and state for the first basic block.
    val fresh = Fresh(0)
    val state = new State(Local(0))

    // Interflow usually infers better types on our erased type system
    // than scalac, yet we live it as a benefit of the doubt and make sure
    // that if original return type is more specific, we keep it as is.
    val Type.Function(_, origRetTy) = origdefn.ty: @unchecked

    // Compute opaque fresh locals for the arguments. Argument types
    // are always a subtype of the original declared type, but in
    // some cases they might not be obviously related, despite
    // having the same concrete allocated class inhabitants.
    val args = argtys.zip(origtys).map {
      case (argty, origty) =>
        val ty = if (!Sub.is(argty, origty)) {
          log(
            s"using original argument type ${origty.show} instead of ${argty.show}"
          )
          origty
        } else {
          argty
        }
        Val.Local(fresh(), ty)
    }

    // If any of the argument types is nothing, this method
    // is never going to be called, so we don't have to visit it.
    if (args.exists(_.ty == Type.Nothing)) {
      val insts = Seq(Inst.Label(Local(0), args), Inst.Unreachable(Next.None))
      result(Type.Nothing, insts)
    } else {
      // Run a merge processor starting from the entry basic block.
      val blocks =
        try {
          pushBlockFresh(fresh)
          process(
            origdefn.insts.toArray,
            args,
            state,
            doInline = false,
            origRetTy
          )
        } finally {
          popBlockFresh()
        }

      // Collect instructions, materialize all returned values
      // and compute the result type.
      val insts = blocks.flatMap { block =>
        block.cf = block.cf match {
          case inst @ Inst.Ret(retv) =>
            Inst.Ret(block.end.materialize(retv))(inst.pos)
          case inst @ Inst.Throw(excv, unwind) =>
            Inst.Throw(block.end.materialize(excv), unwind)(inst.pos)
          case cf =>
            cf
        }
        block.toInsts()
      }
      val rets = insts.collect {
        case Inst.Ret(v) => v.ty
      }

      val retty0 = rets match {
        case Seq()   => Type.Nothing
        case Seq(ty) => ty
        case tys     => Sub.lub(tys, Some(origRetTy))
      }
      // Make sure to not override expected BoxedUnit with primitive Unit
      val retty =
        if (retty0 == Type.Unit && origRetTy.isInstanceOf[Type.Ref]) origRetTy
        else retty0

      result(retty, insts)
    }
  }

  def process(
      insts: Array[Inst],
      args: Seq[Val],
      state: State,
      doInline: Boolean,
      retTy: Type
  )(implicit
      originDefnPos: nir.Position
  ): Seq[MergeBlock] = {
    val processor =
      MergeProcessor.fromEntry(insts, args, state, doInline, blockFresh, this)

    try {
      pushMergeProcessor(processor)

      while (!processor.done()) {
        processor.advance()
      }
    } finally {
      popMergeProcessor()
    }

    val blocks = processor.toSeq(retTy)
    postProcess(blocks)
  }

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
