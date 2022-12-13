// Ported from Scala.js commit: 137c11d dated: 2019-07-03

package java.util.function

import java.{util => ju}

@FunctionalInterface
trait Predicate[T] { self =>
  def test(t: T): Boolean

  def and(other: Predicate[_ >: T]): Predicate[T] = {
    new Predicate[T] {
      def test(t: T): Boolean =
        self.test(t) && other.test(t) // the order and short-circuit are by-spec
    }
  }

  def negate(): Predicate[T] = {
    new Predicate[T] {
      def test(t: T): Boolean =
        !self.test(t)
    }
  }

  def or(other: Predicate[_ >: T]): Predicate[T] = {
    new Predicate[T] {
      def test(t: T): Boolean =
        self.test(t) || other.test(t) // the order and short-circuit are by-spec
    }
  }
}

object Predicate {
  def isEqual[T](targetRef: Any): Predicate[T] = {
    new Predicate[T] {
      def test(t: T): Boolean =
        ju.Objects.equals(targetRef, t)
    }
  }
}
