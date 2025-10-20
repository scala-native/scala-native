// Ported from Scala.js, commit SHA: 7b4e8a80b dated: 2022-12-06
package java.util.function

import java.util as ju

@FunctionalInterface
trait Predicate[T] { self =>
  def test(t: T): Boolean

  def and(other: Predicate[? >: T]): Predicate[T] = {
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

  def or(other: Predicate[? >: T]): Predicate[T] = {
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

  def not[T](target: Predicate[? >: T]): Predicate[T] = {
    new Predicate[T] {
      def test(t: T): Boolean =
        !target.test(t)
    }
  }
}
