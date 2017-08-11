package scala.scalanative
package optimizer
package pass

import scala.collection.mutable
import scala.util.control.Breaks._
import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import util.unsupported
import nir._
import Inst.Let
import scala.scalanative.optimizer.analysis.MemoryLayout

/** Hoists all stack allocations to the entry basic block and
 *  maps class allocations to calls to the gc allocator.
 */
class AllocLowering(implicit fresh: Fresh, top: Top) extends Pass {
  import AllocLowering._

  override def onInsts(insts: Seq[Inst]) = {
    val buf = new nir.Buffer
    import buf._

    insts.foreach {
      case Let(n, Op.Classalloc(ClassRef(cls))) =>
        val size = MemoryLayout.sizeOf(cls.layout.struct)
        val allocMethod =
          if (size < LARGE_OBJECT_MIN_SIZE) alloc else largeAlloc

        let(n,
            Op.Call(allocSig,
                    allocMethod,
                    Seq(cls.rtti.const, Val.Long(size)),
                    Next.None))

      case inst =>
        buf += inst
    }

    buf.toSeq
  }
}

object AllocLowering extends PassCompanion {

  val LARGE_OBJECT_MIN_SIZE = 8192

  val allocSig = Type.Function(Seq(Type.Ptr, Type.Long), Type.Ptr)

  val allocSmallName = Global.Top("scalanative_alloc_small")
  val alloc          = Val.Global(allocSmallName, allocSig)

  val largeAllocName = Global.Top("scalanative_alloc_large")
  val largeAlloc     = Val.Global(largeAllocName, allocSig)

  override val injects =
    Seq(Defn.Declare(Attrs.None, allocSmallName, allocSig),
        Defn.Declare(Attrs.None, largeAllocName, allocSig))

  override def apply(config: tools.Config, top: Top) =
    new AllocLowering()(top.fresh, top)
}
