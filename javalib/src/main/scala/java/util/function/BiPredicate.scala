// Ported from Scala.js commit: 0c27b64 dated: 2020-09-06

package java.util.function

trait BiPredicate[T, U] { self =>
  def test(t: T, u: U): Boolean

  def and(other: BiPredicate[_ >: T, _ >: U]): BiPredicate[T, U] =
    new BiPredicate[T, U] {
      override def test(t: T, u: U): Boolean =
        self.test(t, u) && other.test(t, u)
    }

  def negate(): BiPredicate[T, U] =
    new BiPredicate[T, U] {
      override def test(t: T, u: U): Boolean =
        !self.test(t, u)
    }

  def or(other: BiPredicate[_ >: T, _ >: U]): BiPredicate[T, U] =
    new BiPredicate[T, U] {
      override def test(t: T, u: U): Boolean =
        self.test(t, u) || other.test(t, u)
    }
}
