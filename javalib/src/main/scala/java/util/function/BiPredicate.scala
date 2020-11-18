// Influenced by Scala.js commit: 0c27b64 dated: 2020-09-06

package java.util.function

import scala.scalanative.annotation.JavaDefaultMethod

trait BiPredicate[T, U] { self =>
  def test(t: T, u: U): Boolean

  @JavaDefaultMethod
  def and(other: BiPredicate[_ >: T, _ >: U]): BiPredicate[T, U] =
    new BiPredicate[T, U] {
      override def test(t: T, u: U): Boolean =
        self.test(t, u) && other.test(t, u)
    }

  @JavaDefaultMethod
  def negate(): BiPredicate[T, U] =
    new BiPredicate[T, U] {
      override def test(t: T, u: U): Boolean =
        !self.test(t, u)
    }

  @JavaDefaultMethod
  def or(other: BiPredicate[_ >: T, _ >: U]): BiPredicate[T, U] =
    new BiPredicate[T, U] {
      override def test(t: T, u: U): Boolean =
        self.test(t, u) || other.test(t, u)
    }
}
