package scala.scalanative
package nir

import scala.annotation.switch

case class SyncAttrs(
    memoryOrder: MemoryOrder,
    isVolatile: Boolean = true,
    scope: Option[Global] = None
)

sealed abstract class MemoryOrder(private[nir] val tag: Int) {
  final def show: String = nir.Show(this)
}

object MemoryOrder {
  case object Unordered extends MemoryOrder(0)
  case object Monotonic extends MemoryOrder(1)
  case object Acquire extends MemoryOrder(2)
  case object Release extends MemoryOrder(3)
  case object AcqRel extends MemoryOrder(4)
  case object SeqCst extends MemoryOrder(5)
}
