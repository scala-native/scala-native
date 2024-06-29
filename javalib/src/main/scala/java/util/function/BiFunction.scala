// Ported from Scala.js, commit SHA: 7b4e8a80b dated: 2022-12-06
package java.util.function

@FunctionalInterface
trait BiFunction[T, U, R] {
  def apply(t: T, u: U): R

  def andThen[V](after: Function[_ >: R, _ <: V]): BiFunction[T, U, V] = {
    (t: T, u: U) =>
      after.apply(this.apply(t, u))
  }
}
