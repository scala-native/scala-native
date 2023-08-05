package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.nir._
import scala.annotation.tailrec

final class MergeBlock(val label: Inst.Label, val name: Local) {
  var incoming = mutable.Map.empty[Local, (Seq[Val], State)]
  var outgoing = mutable.Map.empty[Local, MergeBlock]
  var phis: Seq[MergePhi] = _
  var start: State = _
  var end: State = _
  var cf: Inst.Cf = _
  var invalidations: Int = 0

  // Check if inlined function performed stack allocation, if so add
  // insert stacksave/stackrestore LLVM Intrinsics to prevent affecting.
  // By definition every stack allocation of inlined function is only needed within it's body
  lazy val allocatesOnStack = end.emit.exists {
    case Inst.Let(_, _: Op.Stackalloc, _) => true
    case _                                => false
  }
  var stackStatePtr: Val = Val.Null
  var emitStackSaveOp: Option[Local] = None

  implicit def cfPos: Position = {
    if (cf != null) cf.pos
    else label.pos
  }

  lazy val isPartOfCycle: Boolean = {
    val visited = mutable.Set.empty[MergeBlock]
    def outgoingList(block: MergeBlock): List[MergeBlock] =
      block.outgoing.foldRight[List[MergeBlock]](Nil) {
        case ((_, outgoing), acc) =>
          if (visited.contains(outgoing)) acc
          else outgoing :: acc
      }
    @tailrec def loop(todo: List[MergeBlock]): Boolean = todo match {
      case Nil => false
      case head :: tail =>
        if (head eq this) true
        else if (visited.add(head)) {
          visited += head
          loop(outgoingList(head) ::: tail)
        } else loop(tail)
    }
    loop(todo = outgoingList(this))
  }

  def toInsts(): Seq[Inst] = {
    import Interflow.LLVMIntrinsics._
    val block = this
    val result = new nir.Buffer()(Fresh(0))
    def mergeNext(next: Next.Label): Next.Label = {
      val nextBlock = outgoing(next.name)
      if (nextBlock.stackStatePtr != Val.Null && this.isPartOfCycle) {
        result.call(
          StackRestoreSig,
          StackRestore,
          Seq(nextBlock.stackStatePtr),
          Next.None
        )
      }
      val mergeValues = nextBlock.phis.flatMap {
        case MergePhi(_, incoming) =>
          incoming.collect {
            case (name, value) if name == block.label.name =>
              value
          }
      }
      Next.Label(nextBlock.name, mergeValues)
    }
    def mergeUnwind(next: Next): Next = next match {
      case Next.None =>
        next
      case Next.Unwind(exc, next: Next.Label) =>
        Next.Unwind(exc, mergeNext(next))
      case _ =>
        util.unreachable
    }
    val params = block.phis.map(_.param)
    result.label(block.name, params)
    // Ooptionally emit StackSave op
    // It is required when block is a start of cycle involving stackalloc op
    emitStackSaveOp.foreach { n =>
      val alreadyEmmited = block.end.emit.exists {
        case Inst.Let(_, Op.Call(_, StackSave, _), _) => true
        case _                                        => false
      }
      if (!alreadyEmmited) {
        stackStatePtr = result.let(
          name = n,
          op = Op.Call(StackSaveSig, StackSave, Nil),
          unwind = Next.None
        )
      }
    }
    result ++= block.end.emit
    block.cf match {
      case ret: Inst.Ret =>
        result += ret
      case Inst.Jump(next: Next.Label) =>
        result.jump(mergeNext(next))
      case Inst.If(cond, thenNext: Next.Label, elseNext: Next.Label) =>
        result.branch(cond, mergeNext(thenNext), mergeNext(elseNext))
      case Inst.Switch(scrut, defaultNext: Next.Label, cases) =>
        val mergeCases = cases.map {
          case Next.Case(v, next: Next.Label) =>
            Next.Case(v, mergeNext(next))
          case _ =>
            util.unreachable
        }
        result.switch(scrut, mergeNext(defaultNext), mergeCases)
      case Inst.Throw(v, unwind) =>
        result.raise(v, mergeUnwind(unwind))
      case Inst.Unreachable(unwind) =>
        result.unreachable(mergeUnwind(unwind))
      case unknown =>
        throw BailOut(s"MergeUnwind unknown Inst: ${unknown.show}")
    }
    result.toSeq
  }
}
