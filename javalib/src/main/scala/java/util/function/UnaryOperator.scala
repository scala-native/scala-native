// Ported from Scala.js, commit SHA: 4a394815e dated: 2020-09-06
package java.util.function

trait UnaryOperator[T] extends Function[T, T]

object UnaryOperator {
  def identity[T](): UnaryOperator[T] = (t: T) => t
}
