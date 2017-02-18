package scala.scalanative
package nir

import java.util.concurrent.atomic.AtomicInteger

final class Fresh(scope: String) {
  private var i = new AtomicInteger(0)
  def apply() = {
    val value = i.getAndIncrement()
    Local(scope, value)
  }
}
object Fresh {
  def apply(scope: String) = new Fresh(scope)
}
