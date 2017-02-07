package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import scala.util.control.Breaks._
import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import util.unsupported
import nir._, Inst.Let

/** Hoists all stack allocations to the entry basic block and
 *  maps class allocations to calls to the gc allocator.
 */
class AllocLowering(implicit fresh: Fresh, top: Top) extends Pass {
  import AllocLowering._

  override def onInsts(insts: Seq[Inst]) = {
    val entry = mutable.UnrolledBuffer.empty[Inst]
    val buf   = mutable.UnrolledBuffer.empty[Inst]

    val label +: rest = insts
    entry += label

    val newinsts = rest.foreach {
      case inst @ Let(_, alloc: Op.Stackalloc) =>
        entry += inst

      case Let(n, Op.Classalloc(ClassRef(cls))) =>
        val size = Val.Local(fresh(), Type.I64)

        buf += Let(size.name, Op.Sizeof(cls.classStruct))
        buf += Let(
          n,
          Op.Call(allocSig, alloc, Seq(cls.typeConst, size), Next.None))

      case inst =>
        buf += inst
    }

    entry ++= buf
    entry
  }
}

object AllocLowering extends PassCompanion {
  val allocName = Global.Top("scalanative_alloc")
  val allocSig  = Type.Function(Seq(Type.Ptr, Type.I64), Type.Ptr)
  val alloc     = Val.Global(allocName, allocSig)

  override val injects =
    Seq(Defn.Declare(Attrs.None, allocName, allocSig))

  override def apply(config: tools.Config, top: Top) =
    new AllocLowering()(top.fresh, top)
}
