package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import scala.util.control.Breaks._
import compiler.analysis.ClassHierarchy._
import compiler.analysis.ClassHierarchyExtractors._
import util.unsupported
import nir._

/** Hoists all stack allocations to the entry basic block and
 *  maps class allocations to calls to the gc allocator.
 */
class AllocLowering(implicit fresh: Fresh, top: Top) extends Pass {
  import AllocLowering._

  private val stackallocs = mutable.UnrolledBuffer.empty[Inst]

  override def preDefn = {
    case defn: Defn.Define =>
      stackallocs.clear
      Seq(defn)
  }

  override def preInst = {
    case inst @ Inst(_, alloc: Op.Stackalloc) =>
      stackallocs += inst
      Seq()

    case Inst(n, Op.Classalloc(ClassRef(cls))) =>
      val size = Val.Local(fresh(), Type.I64)

      Seq(
          Inst(size.name, Op.Sizeof(cls.classStruct)),
          Inst(n, Op.Call(allocSig, alloc, Seq(cls.typeConst, size)))
      )
  }

  override def postDefn = {
    case defn: Defn.Define =>
      val Block(n, params, insts, cf) +: rest = defn.blocks

      val newBlocks = Block(n, params, stackallocs ++: insts, cf) +: rest

      Seq(defn.copy(blocks = newBlocks))
  }
}

object AllocLowering extends PassCompanion {
  def apply(ctx: Ctx) = new AllocLowering()(ctx.fresh, ctx.top)

  val allocName = Global.Top("scalanative_alloc")
  val allocSig  = Type.Function(Seq(Arg(Type.Ptr), Arg(Type.I64)), Type.Ptr)
  val alloc     = Val.Global(allocName, allocSig)

  override val injects = Seq(Defn.Declare(Attrs.None, allocName, allocSig))
}
