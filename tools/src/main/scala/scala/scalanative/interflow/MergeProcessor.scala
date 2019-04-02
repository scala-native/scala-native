package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.util.unreachable
import scalanative.nir._
import scalanative.linker._

final class MergeProcessor(insts: Array[Inst],
                           blockFresh: Fresh,
                           inline: Boolean,
                           eval: Eval)(implicit linked: linker.Result) {
  val offsets =
    insts.zipWithIndex.collect {
      case (Inst.Label(local, _), offset) =>
        local -> offset
    }.toMap
  val blocks = mutable.Map.empty[Local, MergeBlock]
  var todo   = mutable.Set.empty[Local]

  def currentSize(): Int =
    blocks.values.map { b =>
      if (b.end == null) 0 else b.end.emit.size
    }.sum

  def findMergeBlock(name: Local): MergeBlock = {
    def newMergeBlock = {
      val label = insts(offsets(name)).asInstanceOf[Inst.Label]
      new MergeBlock(label, Local(blockFresh().id * 10000))
    }
    blocks.getOrElseUpdate(name, newMergeBlock)
  }

  def merge(block: MergeBlock)(
      implicit linked: linker.Result): (Seq[MergePhi], State) =
    merge(block.name, block.label.params, block.incoming.toSeq.sortBy(_._1.id))

  def merge(merge: Local,
            params: Seq[Val.Local],
            incoming: Seq[(Local, (Seq[Val], State))])(
      implicit linked: linker.Result): (Seq[MergePhi], State) = {
    val names  = incoming.map { case (n, (_, _)) => n }
    val states = incoming.map { case (_, (_, s)) => s }

    incoming match {
      case Seq() =>
        unreachable
      case Seq((Local(id), (values, state))) =>
        val newstate = state.fullClone(merge)
        params.zip(values).foreach {
          case (param, value) =>
            newstate.storeLocal(param.name, value)
        }
        val phis =
          if (id == -1 && !inline) {
            values.zipWithIndex.map {
              case (param: Val.Local, i) =>
                MergePhi(param, Seq.empty[(Local, Val)])
              case _ =>
                unreachable
            }
          } else {
            Seq.empty
          }
        (phis, newstate)
      case _ =>
        val headState = states.head

        var mergeFresh   = Fresh(merge.id)
        val mergeLocals  = mutable.Map.empty[Local, Val]
        val mergeHeap    = mutable.Map.empty[Addr, Instance]
        val mergePhis    = mutable.UnrolledBuffer.empty[MergePhi]
        val mergeDelayed = mutable.Map.empty[Op, Val]
        val mergeEmitted = mutable.Map.empty[Op, Val]
        val newEscapes   = mutable.Set.empty[Addr]

        def mergePhi(values: Seq[Val]): Val = {
          if (values.distinct.size == 1) {
            values.head
          } else {
            val materialized = states.zip(values).map {
              case (s, v @ Val.Virtual(addr)) if !s.hasEscaped(addr) =>
                newEscapes += addr
                s.materialize(v)
              case (s, v) =>
                s.materialize(v)
            }
            val name    = mergeFresh()
            val paramty = Sub.lub(materialized.map(_.ty))
            val param   = Val.Local(name, paramty)
            mergePhis += MergePhi(param, names.zip(materialized))
            param
          }
        }

        def computeMerge(): Unit = {

          // 1. Merge locals

          def mergeLocal(local: Local): Unit = {
            val values = mutable.UnrolledBuffer.empty[Val]
            states.foreach { s =>
              if (s.locals.contains(local)) {
                values += s.locals(local)
              }
            }
            if (states.size == values.size) {
              mergeLocals(local) = mergePhi(values)
            }
          }
          headState.locals.keys.foreach(mergeLocal)

          // 2. Merge heap

          def includeAddr(addr: Addr): Boolean =
            states.forall { state =>
              state.heap.contains(addr)
            }
          def escapes(addr: Addr): Boolean =
            states.exists(_.hasEscaped(addr))
          val addrs = {
            val out =
              states.head.heap.keys.filter(includeAddr).toArray.sorted
            out.foreach { addr =>
              val headInstance = states.head.deref(addr)
              headInstance match {
                case _ if escapes(addr) =>
                  val values = states.map { s =>
                    s.deref(addr) match {
                      case EscapedInstance(value) => value
                      case _                      => Val.Virtual(addr)
                    }
                  }
                  mergeHeap(addr) = EscapedInstance(mergePhi(values))
                case VirtualInstance(headKind, headCls, headValues) =>
                  val mergeValues = headValues.zipWithIndex.map {
                    case (_, idx) =>
                      val values = states.map { state =>
                        if (state.hasEscaped(addr)) restart()
                        state.derefVirtual(addr).values(idx)
                      }
                      mergePhi(values)
                  }
                  mergeHeap(addr) =
                    VirtualInstance(headKind, headCls, mergeValues)
                case DelayedInstance(op) =>
                  assert(
                    states.forall(s => s.derefDelayed(addr).delayedOp == op))
                  mergeHeap(addr) = DelayedInstance(op)
              }
            }
            out
          }

          // 3. Merge params

          params.zipWithIndex.foreach {
            case (param, idx) =>
              val values = incoming.map {
                case (_, (values, _)) =>
                  values(idx)
              }
              mergeLocals(param.name) = mergePhi(values)
          }

          // 4. Merge delayed ops

          def includeDelayedOp(op: Op, v: Val): Boolean = {
            states.forall { s =>
              s.delayed.contains(op) && s.delayed(op) == v
            }
          }
          states.head.delayed.foreach {
            case (op, v) =>
              if (includeDelayedOp(op, v)) {
                mergeDelayed(op) = v
              }
          }

          // 4. Merge emitted ops

          def includeEmittedOp(op: Op, v: Val): Boolean = {
            states.forall { s =>
              s.emitted.contains(op) && s.emitted(op) == v
            }
          }
          states.head.emitted.foreach {
            case (op, v) =>
              if (includeEmittedOp(op, v)) {
                mergeEmitted(op) = v
              }
          }
        }

        def restart(): Nothing =
          throw MergeProcessor.Restart

        // Retry until no new escapes are found

        var retries = 0
        do {
          retries += 1
          mergeFresh = Fresh(merge.id)
          mergeLocals.clear()
          mergeHeap.clear()
          mergePhis.clear()
          mergeDelayed.clear()
          mergeEmitted.clear()
          newEscapes.clear()
          try {
            computeMerge()
          } catch {
            case MergeProcessor.Restart =>
              ()
          }
          if (retries > 128) {
            throw BailOut("too many state merge retries")
          }
        } while (newEscapes.nonEmpty)

        // Wrap up anre rturn a new merge state

        val mergeState = new State(merge)
        mergeState.emit = new nir.Buffer()(mergeFresh)
        mergeState.fresh = mergeFresh
        mergeState.locals = mergeLocals
        mergeState.heap = mergeHeap
        mergeState.delayed = mergeDelayed
        mergeState.emitted = mergeEmitted

        (mergePhis, mergeState)
    }
  }

  def done(): Boolean =
    todo.isEmpty

  def invalidate(rootBlock: MergeBlock): Unit = {
    val invalid = mutable.Map.empty[Local, MergeBlock]

    def visitBlock(from: MergeBlock, block: MergeBlock): Unit = {
      val fromName = from.label.name
      val name     = block.label.name
      if (!invalid.contains(name)) {
        if (offsets(name) > offsets(fromName)) {
          invalid(name) = block
          if (block.cf != null) {
            visitCf(from, block.cf)
          }
        }
      }
    }

    def visitLabel(from: MergeBlock, next: Next.Label): Unit =
      visitBlock(from, findMergeBlock(next.name))

    def visitUnwind(from: MergeBlock, next: Next): Unit = next match {
      case Next.None =>
        ()
      case Next.Unwind(_, next: Next.Label) =>
        visitLabel(from, next)
      case _ =>
        util.unreachable
    }

    def visitCf(from: MergeBlock, cf: Inst.Cf): Unit = {
      cf match {
        case _: Inst.Ret =>
          ()
        case Inst.Jump(next: Next.Label) =>
          visitLabel(from, next)
        case Inst.If(_, thenNext: Next.Label, elseNext: Next.Label) =>
          visitLabel(from, thenNext)
          visitLabel(from, elseNext)
        case Inst.Switch(_, defaultNext: Next.Label, cases) =>
          visitLabel(from, defaultNext)
          cases.foreach {
            case Next.Case(_, caseNext: Next.Label) =>
              visitLabel(from, caseNext)
            case _ =>
              unreachable
          }
        case Inst.Throw(_, next) =>
          visitUnwind(from, next)
        case Inst.Unreachable(next) =>
          visitUnwind(from, next)
        case _ =>
          unreachable
      }
    }

    if (rootBlock.cf != null) {
      visitCf(rootBlock, rootBlock.cf)
    }

    invalid.values.foreach { block =>
      block.incoming = block.incoming.filter {
        case (name, _) =>
          !invalid.contains(name)
      }
      block.outgoing.clear()
      block.phis = null
      block.start = null
      block.end = null
      block.cf = null
    }

    todo = todo.filterNot(n => invalid.contains(n))
  }

  def updateDirectSuccessors(block: MergeBlock): Unit = {
    def nextLabel(next: Next.Label): Unit = {
      val nextMergeBlock = findMergeBlock(next.name)
      block.outgoing(next.name) = nextMergeBlock
      nextMergeBlock.incoming(block.label.name) = ((next.args, block.end))
      todo += next.name
    }
    def nextUnwind(next: Next): Unit = next match {
      case Next.None =>
        ()
      case Next.Unwind(_, next: Next.Label) =>
        nextLabel(next)
      case _ =>
        util.unreachable
    }

    block.cf match {
      case _: Inst.Ret =>
        ()
      case Inst.Jump(next: Next.Label) =>
        nextLabel(next)
      case Inst.If(_, thenNext: Next.Label, elseNext: Next.Label) =>
        nextLabel(thenNext)
        nextLabel(elseNext)
      case Inst.Switch(_, defaultNext: Next.Label, cases) =>
        nextLabel(defaultNext)
        cases.foreach {
          case Next.Case(_, caseNext: Next.Label) =>
            nextLabel(caseNext)
          case _ =>
            unreachable
        }
      case Inst.Throw(_, next) =>
        nextUnwind(next)
      case Inst.Unreachable(next) =>
        nextUnwind(next)
      case _ =>
        unreachable
    }
  }

  def visit(block: MergeBlock,
            newPhis: Seq[MergePhi],
            newState: State): Unit = {
    if (block.invalidations > 128) {
      throw BailOut("too many block invalidations")
    } else {
      if (block.invalidations > 0) {
        invalidate(block)
      }
      block.invalidations += 1
    }

    block.start = newState.fullClone(block.name)
    block.end = newState
    block.cf = eval.run(insts, offsets, block.label.name)(block.end)
    block.outgoing.clear()
    updateDirectSuccessors(block)

    todo = todo.filter(n => findMergeBlock(n).incoming.nonEmpty)
  }

  def advance(): Unit = {
    val sortedTodo = todo.toArray.sortBy(n => offsets(n))
    val block      = findMergeBlock(sortedTodo.head)
    todo.clear()
    todo ++= sortedTodo.tail

    val (newPhis, newState) = merge(block)
    block.phis = newPhis

    if (newState != block.start) {
      visit(block, newPhis, newState)
    }
  }

  def toSeq(): Seq[MergeBlock] = {
    val sortedBlocks = blocks.values.toSeq
      .sortBy { block =>
        offsets(block.label.name)
      }
      .filter(_.cf != null)

    val retMergeBlocks = sortedBlocks.collect {
      case block if block.cf.isInstanceOf[Inst.Ret] =>
        block
    }.toSeq

    def isExceptional(block: MergeBlock): Boolean = {
      val cf = block.cf
      cf.isInstanceOf[Inst.Unreachable] || cf.isInstanceOf[Inst.Throw]
    }

    val orderedBlocks = mutable.UnrolledBuffer.empty[MergeBlock]
    orderedBlocks ++= sortedBlocks.filterNot(isExceptional)

    // Inlining expects at most one block that returns.
    // If the discovered blocks contain more than one,
    // we must merge them together using a synthetic block.
    if (inline && retMergeBlocks.size > 1) {
      val retTy = Sub.lub(retMergeBlocks.map { block =>
        val Inst.Ret(v)    = block.cf
        implicit val state = block.end
        v match {
          case InstanceRef(ty) => ty
          case _               => v.ty
        }
      })

      // Create synthetic label and block where all returning blocks
      // are going tojump to. Synthetics names must be fresh relative
      // to the source instructions, not relative to generated ones.
      val syntheticFresh = Fresh(insts)
      val syntheticParam = Val.Local(syntheticFresh(), retTy)
      val syntheticLabel = Inst.Label(syntheticFresh(), Seq(syntheticParam))
      val resultMergeBlock =
        new MergeBlock(syntheticLabel, Local(blockFresh().id * 10000))
      blocks(syntheticLabel.name) = resultMergeBlock
      orderedBlocks += resultMergeBlock

      // Update all returning blocks to jump to result block,
      // and update incoming/outgoing edges to include result block.
      retMergeBlocks.foreach { block =>
        val Inst.Ret(v) = block.cf
        block.cf = Inst.Jump(Next.Label(syntheticLabel.name, Seq(v)))
        block.outgoing(syntheticLabel.name) = resultMergeBlock
        resultMergeBlock.incoming(block.label.name) = ((Seq(v), block.end))
      }

      // Perform merge of all incoming edges to compute
      // state and phis in the resulting block. Synthetic
      // param value must be evaluated in end state as it
      // might be eliminated after merge processing.
      val (phis, state) = merge(resultMergeBlock)
      resultMergeBlock.phis = phis
      resultMergeBlock.start = state
      resultMergeBlock.end = state
      resultMergeBlock.cf = Inst.Ret(eval.eval(syntheticParam)(state))
    }

    orderedBlocks ++= sortedBlocks.filter(isExceptional)
    orderedBlocks
  }
}

object MergeProcessor {
  final case object Restart
      extends Exception
      with scala.util.control.NoStackTrace

  def fromEntry(insts: Array[Inst],
                args: Seq[Val],
                state: State,
                inline: Boolean,
                blockFresh: Fresh,
                eval: Eval)(implicit linked: linker.Result): MergeProcessor = {
    val builder         = new MergeProcessor(insts, blockFresh, inline, eval)
    val entryName       = insts.head.asInstanceOf[Inst.Label].name
    val entryMergeBlock = builder.findMergeBlock(entryName)
    val entryState      = new State(entryMergeBlock.name)
    entryState.inherit(state, args)
    entryMergeBlock.incoming(Local(-1)) = ((args, entryState))
    builder.todo += entryName
    builder
  }
}
