package scala.scalanative
package interflow

import scala.collection.mutable
import scala.annotation.tailrec

final class MergeBlock(val label: nir.Inst.Label, val id: nir.Local) {

  var incoming = mutable.Map.empty[nir.Local, (Seq[nir.Val], State)]
  var outgoing = mutable.Map.empty[nir.Local, MergeBlock]
  var phis: Seq[MergePhi] = _
  var start: State = _
  var end: State = _
  var cf: nir.Inst.Cf = _
  var invalidations: Int = 0
  implicit def cfPos: nir.Position = {
    if (cf != null) cf.pos
    else label.pos
  }

  private var stackSavePtr: nir.Val.Local = _
  private[interflow] var emitStackSaveOp = false
  private[interflow] var emitStackRestoreFor: List[nir.Local] = Nil

  def toInsts(): Seq[nir.Inst] = {
    import Interflow.LLVMIntrinsics._
    val block = this
    val result = new nir.Buffer()(nir.Fresh(0))

    def mergeNext(next: nir.Next.Label): nir.Next.Label = {
      val nextBlock = outgoing(next.id)

      if (nextBlock.stackSavePtr != null &&
          emitStackRestoreFor.contains(next.id)) {
        emitIfMissing(
          end.fresh(),
          nir.Op
            .Call(StackRestoreSig, StackRestore, Seq(nextBlock.stackSavePtr))
        )(result, block)
      }
      val mergeValues = nextBlock.phis.flatMap {
        case MergePhi(_, incoming) =>
          incoming.collect {
            case (id, value) if id == block.label.id => value
          }
      }
      nir.Next.Label(nextBlock.id, mergeValues)
    }
    def mergeUnwind(next: nir.Next): nir.Next = next match {
      case nir.Next.None =>
        next
      case nir.Next.Unwind(exc, next: nir.Next.Label) =>
        nir.Next.Unwind(exc, mergeNext(next))
      case _ =>
        util.unreachable
    }

    val params = block.phis.map(_.param)
    result.label(block.id, params)
    if (emitStackSaveOp) {
      val id = block.end.fresh()
      val emmited = emitIfMissing(
        id = id,
        op = nir.Op.Call(StackSaveSig, StackSave, Nil)
      )(result, block)
      if (emmited) block.stackSavePtr = nir.Val.Local(id, nir.Type.Ptr)
    }
    result ++= block.end.emit
    block.cf match {
      case ret: nir.Inst.Ret =>
        result += ret
      case nir.Inst.Jump(next: nir.Next.Label) =>
        result.jump(mergeNext(next))
      case nir.Inst.If(
            cond,
            thenNext: nir.Next.Label,
            elseNext: nir.Next.Label
          ) =>
        result.branch(cond, mergeNext(thenNext), mergeNext(elseNext))
      case nir.Inst.Switch(scrut, defaultNext: nir.Next.Label, cases) =>
        val mergeCases = cases.map {
          case nir.Next.Case(v, next: nir.Next.Label) =>
            nir.Next.Case(v, mergeNext(next))
          case _ =>
            util.unreachable
        }
        result.switch(scrut, mergeNext(defaultNext), mergeCases)
      case nir.Inst.Throw(v, unwind) =>
        result.raise(v, mergeUnwind(unwind))
      case nir.Inst.Unreachable(unwind) =>
        result.unreachable(mergeUnwind(unwind))
      case unknown =>
        throw BailOut(s"MergeUnwind unknown Inst: ${unknown.show}")
    }
    result.toSeq
  }

  private def emitIfMissing(
      id: => nir.Local,
      op: nir.Op.Call
  )(result: nir.Buffer, block: MergeBlock): Boolean = {
    // Check if original defn already contains this op
    val alreadyEmmited = block.end.emit.exists {
      case nir.Inst.Let(_, `op`, _) =>
        true
      case _ =>
        false
    }
    if (alreadyEmmited) false
    else {
      // TODO: resolving actual scopeId. Currently not really important becouse used only to introduce stack guard intrinsics
      implicit def scopeId: nir.ScopeId = nir.ScopeId.TopLevel
      result.let(id, op, nir.Next.None)
      true
    }
  }

}
