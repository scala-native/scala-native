package scala.scalanative
package compiler
package pass

import scala.collection.mutable
import scala.util.control.Breaks._
import compiler.analysis.ClassHierarchy._
import compiler.analysis.ClassHierarchyExtractors._
import util.unsupported
import nir._, Inst.Let

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
    case inst @ Let(_, alloc: Op.Stackalloc) =>
      stackallocs += inst
      Seq()

    case Let(n, Op.Classalloc(ClassRef(cls))) =>
      val size = Val.Local(fresh(), Type.I64)

      Seq(
          Let(size.name, Op.Sizeof(cls.classStruct)),
          Let(n, Op.Call(allocSig, alloc, Seq(cls.typeConst, size)))
      )
  }

  override def postDefn = {
    case defn: Defn.Define =>
      val label +: rest = defn.insts
      val newinsts      = label +: (stackallocs ++: rest)

      Seq(defn.copy(insts = newinsts))
  }
}

object AllocLowering extends PassCompanion {
  def apply(ctx: Ctx) = new AllocLowering()(ctx.fresh, ctx.top)

  val allocName = Global.Top("scalanative_alloc")
  val allocSig  = Type.Function(Seq(Arg(Type.Ptr), Arg(Type.I64)), Type.Ptr)
  val alloc     = Val.Global(allocName, allocSig)

  override val injects = Seq(Defn.Declare(Attrs.None, allocName, allocSig))
}
