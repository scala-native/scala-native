// Ported from Scala.js, commit sha:cfb4888a6 dated:2021-01-07
package java.util.function

@FunctionalInterface
trait IntToDoubleFunction {
  def applyAsDouble(value: Int): Double
}