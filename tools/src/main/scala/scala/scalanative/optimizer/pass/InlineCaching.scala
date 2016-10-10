package scala.scalanative
package optimizer
package pass

import scala.io.Source

import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import analysis.ControlFlow, ControlFlow.Block
import nir._, Inst.Let

/**
 * Inline caching based on information gathered at runtime.
 */
class InlineCaching(dispatchInfo: Map[String, Seq[String]], maxCandidates: Int)(implicit fresh: Fresh, top: Top) extends Pass {
  import InlineCaching._

  println("#" * 181)
  println("Dispatch info:")
  println(dispatchInfo)
  println("#" * 181)

  private def splitAfter[T](fn: T => Boolean)(elems: Seq[T]): (Seq[T], Seq[T]) =
    elems span fn match {
      case (l0, x +: rest) => (l0 :+ x, rest)
      case other           => other
    }

  private def isVirtualCall(inst: Inst): Boolean =
    inst match {
      case Let(_, Op.Method(_, MethodRef(_: Class, meth)))
        if meth.isVirtual =>
        true
      case _ =>
        false
    }

  private def blockToInsts(block: Block): Seq[Inst] =
    block.label +: block.insts

  private def splitBlock(block: Block): Seq[Block] =
    if (block.insts exists isVirtualCall) {
      val (b0Insts, b1Insts) = splitAfter(isVirtualCall)(block.insts)
      val b1Name = fresh()
      val b0 = block.copy(
        insts = b0Insts :+ Inst.Jump(Next.Label(b1Name, Nil))
      )
      val b1 = block.copy(name = b1Name, params = Nil, insts = b1Insts)
      b0 +: splitBlock(b1)
    } else Seq(block)

  override def preDefn = {
    case define: Defn.Define =>
      val graph = ControlFlow.Graph(define.insts)
      val newBlocks = graph.all flatMap splitBlock
      Seq(define.copy(insts = newBlocks flatMap blockToInsts))
  }


}

object InlineCaching extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    config.profileDispatchInfo match {
      case Some(info) if info.exists =>
        val dispatchInfo =
          analysis.DispatchInfoParser(Source.fromFile(info).mkString)
        new InlineCaching(dispatchInfo, 5)(top.fresh, top)

      case _ =>
        EmptyPass
    }
}
