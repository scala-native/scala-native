// Ported from Scala.js, commit SHA: 1ef4c4e0f dated: 2020-09-06
package java.util.function

import java.util.Comparator

trait BinaryOperator[T] extends BiFunction[T, T, T]

object BinaryOperator {
  def minBy[T](comparator: Comparator[_ >: T]): BinaryOperator[T] = {
    (a: T, b: T) =>
      if (comparator.compare(a, b) <= 0) a
      else b
  }

  def maxBy[T](comparator: Comparator[_ >: T]): BinaryOperator[T] = {
    (a: T, b: T) =>
      if (comparator.compare(a, b) >= 0) a
      else b
  }
}
