package scala.scalanative
package nir

import java.util.concurrent.atomic.AtomicInteger

final class Fresh private (private var start: Int) {
  def apply(): Local = {
    start += 1
    val value = start
    Local(value)
  }
}

object Fresh {
  def apply(start: Int = 0): Fresh =
    new Fresh(start)

  def apply(insts: Seq[Inst]): Fresh = {
    var max = -1
    insts.foreach {
      case Inst.Let(local, _, _) =>
        max = Math.max(max, local.id)
      case Inst.Label(local, params) =>
        max = Math.max(max, local.id)
        params.foreach { param =>
          max = Math.max(max, param.name.id)
        }
      case _ =>
        ()
    }
    new Fresh(max)
  }
}
