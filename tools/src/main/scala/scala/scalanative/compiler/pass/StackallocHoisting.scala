package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import scala.util.control.Breaks._
import util.unsupported
import nir._

/** Hoists all stack allocations to the entry basic block. */
class StackallocHoisting extends Pass {
  private var allocs = mutable.UnrolledBuffer.empty[Inst]

  override def preDefn = {
    case defn: Defn.Define =>
      allocs.clear
      Seq(defn)
  }

  override def preInst = {
    case inst @ Inst(_, alloc: Op.Stackalloc) =>
      allocs += inst
      Seq()
  }

  override def postDefn = {
    case defn: Defn.Define =>
      val Block(n, params, insts, cf) +: rest = defn.blocks
      val newBlocks                           = Block(n, params, allocs ++: insts, cf) +: rest
      Seq(defn.copy(blocks = newBlocks))
  }
}

object StackallocHoisting extends PassCompanion {
  def apply(ctx: Ctx) = new StackallocHoisting
}
