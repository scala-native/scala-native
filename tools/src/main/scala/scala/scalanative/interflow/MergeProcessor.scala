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

  def findMergeBlock(name: Local): MergeBlock = {
    def newMergeBlock = {
      val label = insts(offsets(name)).asInstanceOf[Inst.Label]
      new MergeBlock(label, Local(blockFresh().id * 10000))
    }
    blocks.getOrElseUpdate(name, newMergeBlock)
  }

  def merge(block: MergeBlock)(
      implicit linked: linker.Result): (Seq[MergePhi], State) = {
    val params   = block.label.params
    val incoming = block.incoming.toSeq.sortBy(_._1.id)

    incoming match {
      case Seq() =>
        unreachable
      case Seq((Local(id), (values, state))) =>
        val newstate = state.fullClone(block.name)
        params.zip(values).foreach {
          case (param, value) =>
            newstate.storeLocal(param.name, value)
        }
        val phis =
          if (id == -1 && !inline) {
            values.zipWithIndex.map {
              case (param: Val.Local, i) =>
                MergePhi(param, Seq.empty[(Local, Val)])
            }
          } else {
            Seq.empty
          }
        (phis, newstate)
      case _ =>
        val names     = incoming.map { case (n, (_, _)) => n }
        val states    = incoming.map { case (_, (_, s)) => s }
        val headState = states.head

        var mergeFresh  = Fresh(block.name.id)
        val mergeLocals = mutable.Map.empty[Local, Val]
        val mergeHeap   = mutable.Map.empty[Addr, Instance]
        val mergePhis   = mutable.UnrolledBuffer.empty[MergePhi]
        val newEscapes  = mutable.Set.empty[Addr]

        def mergePhi(values: Seq[Val]): Val = {
          if (values.distinct.size == 1) {
            values.head
          } else {
            val materialized = states.zip(values).map {
              case (s, v @ Val.Virtual(addr)) if !s.escaped(addr) =>
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
              state.heap.contains(addr) &&
              (state.deref(addr).cls eq states.head.deref(addr).cls)
            }
          def escapes(addr: Addr): Boolean =
            states.exists(_.escaped(addr))
          val addrs = {
            val out =
              states.head.heap.keys.filter(includeAddr).toArray.sorted
            out.foreach { addr =>
              val headInstance = states.head.deref(addr)
              if (escapes(addr)) {
                val values = states.map { s =>
                  s.deref(addr) match {
                    case _: VirtualInstance        => Val.Virtual(addr)
                    case EscapedInstance(_, value) => value
                  }
                }
                val param = mergePhi(values)
                mergeHeap(addr) = EscapedInstance(headInstance.cls, param)
              } else {
                val VirtualInstance(headKind, _, headValues) = headInstance
                val mergeValues = headValues.zipWithIndex.map {
                  case (_, idx) =>
                    val values = states.map { state =>
                      if (state.escaped(addr)) restart()
                      state.derefVirtual(addr).values(idx)
                    }
                    mergePhi(values)
                }
                mergeHeap(addr) =
                  VirtualInstance(headKind, headInstance.cls, mergeValues)
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
        }

        def restart(): Nothing =
          throw MergeProcessor.Restart

        // Retry until no new escapes are found

        var retries = 0
        do {
          retries += 1
          mergeFresh = Fresh(block.name.id)
          mergeLocals.clear()
          mergeHeap.clear()
          mergePhis.clear()
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

        val mergeState = new State(block.name)
        mergeState.emit = new nir.Buffer()(mergeFresh)
        mergeState.fresh = mergeFresh
        mergeState.locals = mergeLocals
        mergeState.heap = mergeHeap

        (mergePhis, mergeState)
    }
  }

  def done(): Boolean =
    todo.isEmpty

  def snapshot(): String = {
    val sb = new StringBuilder
    def println(msg: String) = {
      sb.append(msg)
      sb.append('\n')
    }
    blocks.values.toSeq.sortBy(_.name.id).foreach { block =>
      println("-- block " + block.label.show)
      println("name " + block.name.show)
      if (block.phis != null) {
        block.phis.foreach {
          case MergePhi(name, incoming) =>
            println(
              s"phi " + name.show + " = " + incoming
                .map {
                  case (from, value) => s"(${from.show}, ${value.show})"
                }
                .mkString("{ ", ", ", "}"))
        }
      } else {
        println("phis = null")
      }
      if (block.end != null) {
        block.end.emit.toSeq.foreach(i => println(i.show))
      } else {
        println("insts = null")
      }
      if (block.cf != null) {
        println(block.cf.show)
      } else {
        println("cf = null")
      }
    }
    sb.toString
  }

  def advance(): Unit = {
    val sortedTodo = todo.toArray.sortBy(_.id)
    val block      = findMergeBlock(sortedTodo.head)
    todo.clear()
    todo ++= sortedTodo.tail

    val (newPhis, newState) = merge(block)
    block.phis = newPhis

    if (newState != block.start) {
      if (block.invalidations > 128) {
        throw BailOut("too many block invalidations")
      } else {
        block.invalidations += 1
      }

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

      block.start = newState.fullClone(block.name)
      block.end = newState
      block.cf =
        eval.run(insts, offsets, block.label.name, blockFresh)(block.end)
      block.outgoing.clear()
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
          }
        case Inst.Throw(_, next) =>
          nextUnwind(next)
        case Inst.Unreachable(next) =>
          nextUnwind(next)
        case _ =>
          unreachable
      }
    }
  }

  def toSeq(): Seq[MergeBlock] = {
    val retMergeBlocks = blocks.values.collect {
      case block if block.cf.isInstanceOf[Inst.Ret] =>
        block
    }.toSeq

    // Inlining expects at most one block that returns.
    // If the discovered blocks contain more than one,
    // we must merge them together using a synthetic block.
    if (inline && retMergeBlocks.size > 1) {
      val retTy = Sub.lub(retMergeBlocks.map { block =>
        val Inst.Ret(v) = block.cf
        v match {
          case Val.Virtual(addr) => block.end.deref(addr).cls.ty
          case _                 => v.ty
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

    blocks.values.toSeq.sortBy(_.label.name.id)
  }
}

object MergeProcessor {
  final case object Restart
      extends Exception
      with scala.util.control.NoStackTrace

  def fromEntry(insts: Array[Inst],
                args: Seq[Val],
                state: State,
                blockFresh: Fresh,
                inline: Boolean,
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

  def process(insts: Array[Inst],
              args: Seq[Val],
              state: State,
              blockFresh: Fresh,
              inline: Boolean,
              eval: Eval)(implicit linked: linker.Result): Seq[MergeBlock] = {
    val builder =
      MergeProcessor.fromEntry(insts, args, state, blockFresh, inline, eval)

    while (!builder.done()) {
      builder.advance()
    }

    builder.toSeq()
  }
}
