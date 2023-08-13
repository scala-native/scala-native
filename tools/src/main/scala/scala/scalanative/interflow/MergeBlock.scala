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
  implicit def cfPos: Position = {
    if (cf != null) cf.pos
    else label.pos
  }

  private var stackSavePtr: Val.Local = _
  private[interflow] var emitStackSaveOp = false
  private[interflow] var emitStackRestoreFor: List[Local] = Nil

  def toInsts(): Seq[Inst] = {
    import Interflow.LLVMIntrinsics._
    val block = this
    val result = new nir.Buffer()(Fresh(0))
    def mergeNext(next: Next.Label): Next.Label = {
      val nextBlock = outgoing(next.name)

      if (nextBlock.stackSavePtr != null &&
          emitStackRestoreFor.contains(next.name)) {
        emitIfMissing(
          end.fresh(),
          Op.Call(StackRestoreSig, StackRestore, Seq(nextBlock.stackSavePtr))
        )(result, block)
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
    if (emitStackSaveOp) {
      val id = block.end.fresh()
      val emmited = emitIfMissing(
        id = id,
        op = Op.Call(StackSaveSig, StackSave, Nil)
      )(result, block)
      if (emmited) block.stackSavePtr = Val.Local(id, Type.Ptr)
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

  private def emitIfMissing(
      id: => Local,
      op: Op.Call
  )(result: nir.Buffer, block: MergeBlock): Boolean = {
    // Check if original defn already contains this op
    val alreadyEmmited = block.end.emit.exists {
      case Inst.Let(_, `op`, _) => true
      case _                    => false
    }
    if (alreadyEmmited) false
    else {
      result.let(id, op, Next.None)
      true
    }
  }
}
