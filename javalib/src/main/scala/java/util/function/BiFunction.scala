// Ported from Scala.js commit:  d3a9711 dated: 2020-09-06

package java.util.function

trait BiFunction[T, U, R] { self =>
  def apply(t: T, u: U): R

  def andThen[V](after: Function[_ >: R, _ <: V]): BiFunction[T, U, V] = {
    new BiFunction[T, U, V] {
      def apply(t: T, u: U): V = after.apply(self.apply(t, u))
    }
  }
}
