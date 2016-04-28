package scala.scalanative
package native

import runtime.undefined

final class Ptr[T] private () {
  def apply(): T = undefined
  def update(value: T): Unit = undefined
}
