package scala.scalanative
package native

final class Ptr[T] private () {
  def apply(): T
  def update(value: T): Unit
}
