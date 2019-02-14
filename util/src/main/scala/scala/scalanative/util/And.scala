package scala.scalanative
package util

object And {
  def unapply[T](x: T): Some[(T, T)] =
    Some((x, x))
}
