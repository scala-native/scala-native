package salty.ir

import scala.collection.{ mutable => mut }
import salty.util.sh
import salty.ir.Combinators._

abstract class Pass[T] {
  def onBackReference(b: Block): Unit = ()
  def onForwardReference(b: Block): Unit = ()
  def onBlock(b: Block): Unit = ()
  def result: T
  def run(b: Block): T
}

abstract class DFPass[T] extends Pass[T] {
  def run(b: Block): T = {
    var visited = List.empty[Block]
    def loop(block: Block): Unit ={
      onBlock(block)
      visited = block :: visited
      block.instrs.foreach {
        case Instr.Assign(_, Expr.Phi(branches)) =>
          branches.foreach { br =>
            onBackReference(br.block)
          }
        case _ =>
          ()
      }
      block.foreachNext { next =>
        if (visited.contains(next)) {
          onBackReference(next)
        } else {
          onForwardReference(next)
          loop(next)
        }
      }
    }
    loop(b)
    result
  }
}

final class SimplifyCFG extends Pass[Unit] {
  type UseCount = mut.Map[Val, Int]

  private class CollectUseCount extends DFPass[UseCount]{
    val result = mut.Map.empty[Val, Int].withDefault(_ => 0)
    override def onBackReference(b: Block) = result(b.name) += 1
    override def onForwardReference(b: Block) = result(b.name) += 1
  }

  private class MergeFallthroughs(usecount: UseCount) extends DFPass[Unit] {
    val result = ()
    override def onBlock(block: Block) = {
      def loop() = block match {
        case curr @ Block(_, instrs1, Termn.Jump(Block(next, instrs2, termn)))
             if usecount(next) == 1 =>
          block.instrs = instrs1 ++ instrs2
          block.termn = termn
          true
        case _ =>
          false
      }
      while (loop()) ()
    }
  }

  val result = ()
  def run(b: Block): Unit = {
    val usecount = (new CollectUseCount).run(b)
    (new MergeFallthroughs(usecount)).run(b)
  }
}
