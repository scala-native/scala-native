package java.util.function

import java.util.{Comparator, Objects}

trait BinaryOperator[T] extends BiFunction[T, T, T] { self =>
}

object BinaryOperator {

  def minBy[T](comparator: Comparator[_ >: T]): BinaryOperator[T] = {
    Objects.requireNonNull(comparator)
    new BinaryOperator[T] {
      override def apply(a: T, b: T): T =
        if (comparator.compare(a, b) <= 0) a else b
    }
  }

  def maxBy[T](comparator: Comparator[_ >: T]): BinaryOperator[T] = {
    Objects.requireNonNull(comparator)
    new BinaryOperator[T] {
      override def apply(a: T, b: T): T =
        if (comparator.compare(a, b) >= 0) a else b
    }
  }
}
