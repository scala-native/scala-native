package scala.scalanative
package optimizer
package pass

import scala.io.Source

import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import analysis.ControlFlow, ControlFlow.Block
import scalanative.util.unreachable
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

  def splitAtVirtualCall(block: Block): (Block, Let, Block) = {
    assert(block.insts exists isVirtualCall)
    block.insts span (!isVirtualCall(_)) match {
      case (l0, (x @ Let(_, Op.Method(_, _))) +: rest) =>
        val merge = fresh()
        val b0 = block.copy(insts = l0)
        val b1 = Block(merge, Seq(Val.Local(x.name, Type.Ptr)), rest :+ block.insts.last)
        (b0, x, b1)
      case _ =>
        unreachable
    }
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
      val (b0, inst, b1) = splitAtVirtualCall(block)
      val instName = fresh()
      val b01 = b0.copy(
        insts = b0.insts :+ inst.copy(name = instName) :+ Inst.Jump(Next.Label(b1.name, Seq(Val.Local(instName, Type.Ptr)))))

      b01 +: splitBlock(b1)
    } else {
      Seq(block)
    }

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
