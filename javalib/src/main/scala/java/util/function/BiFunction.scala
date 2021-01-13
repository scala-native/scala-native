// Corresponds to Scala.js commit:  d3a9711 dated: 2020-09-06
// Design note: Do not use lambdas with Scala Native and Scala 2.11

package java.util.function

import scala.scalanative.annotation.JavaDefaultMethod

trait BiFunction[T, U, R] { self =>
  def apply(t: T, u: U): R

  @JavaDefaultMethod
  def andThen[V](after: Function[_ >: R, _ <: V]): BiFunction[T, U, V] = {
    new BiFunction[T, U, V] {
      def apply(t: T, u: U): V = after.apply(self.apply(t, u))
    }
  }
}
