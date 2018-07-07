package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy.Top
import nir._
import scala.scalanative.optimizer.analysis.MemoryLayout

/** Maps sizeof computation to pointer arithmetics over null pointer. */
class SizeofLowering(top: Top, config: build.Config) extends Pass {
  override def onInsts(insts: Seq[Inst]): Seq[Inst] = {
    val buf = new nir.Buffer
    import buf._

    insts.foreach {

      case Inst.Let(n, Op.Sizeof(ty)) =>
        if (config.targetArchitecture.is32) {
          let(n,
              Op.Copy(
                Val.Int(
                  MemoryLayout.sizeOf(ty, config.targetArchitecture).toInt)))
        } else {
          let(n,
              Op.Copy(
                Val.Long(MemoryLayout.sizeOf(ty, config.targetArchitecture))))
        }

      case inst =>
        buf += inst
    }

    buf.toSeq
  }
}

object SizeofLowering extends PassCompanion {
  override def apply(config: build.Config, top: Top) =
    new SizeofLowering(top, config)
}
