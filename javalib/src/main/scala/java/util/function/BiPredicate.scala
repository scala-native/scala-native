// Ported from Scala.js, commit SHA: 7b4e8a80b dated: 2022-12-06
package java.util.function

@FunctionalInterface
trait BiPredicate[T, U] {
  def test(t: T, u: U): Boolean

  def and(other: BiPredicate[? >: T, ? >: U]): BiPredicate[T, U] = {
    (t: T, u: U) =>
      test(t, u) && other.test(t, u)
  }

  def negate(): BiPredicate[T, U] = (t: T, u: U) => !test(t, u)

  def or(other: BiPredicate[? >: T, ? >: U]): BiPredicate[T, U] = {
    (t: T, u: U) =>
      test(t, u) || other.test(t, u)
  }
}
