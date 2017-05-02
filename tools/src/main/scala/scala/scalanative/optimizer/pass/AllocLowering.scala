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
    val buf = new nir.Buffer
    import buf._

    insts.foreach {
      case Let(n, Op.Classalloc(ClassRef(cls))) =>
        val size = let(Op.Sizeof(cls.layout.struct))
        let(n, Op.Call(allocSig, alloc, Seq(cls.rtti.const, size), Next.None))

      case inst =>
        buf += inst
    }

    buf.toSeq
  }
}

object AllocLowering extends PassCompanion {
  val allocName = Global.Top("scalanative_alloc")
  val allocSig  = Type.Function(Seq(Type.Ptr, Type.Long), Type.Ptr)
  val alloc     = Val.Global(allocName, allocSig)

  override val injects =
    Seq(Defn.Declare(Attrs.None, allocName, allocSig))

  override def apply(config: tools.Config, top: Top) =
    new AllocLowering()(top.fresh, top)
}
