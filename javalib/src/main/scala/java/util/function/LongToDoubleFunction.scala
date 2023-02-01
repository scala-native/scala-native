// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package java.util.function

@FunctionalInterface
trait LongToDoubleFunction {
  def applyAsDouble(value: Long): Double
}
