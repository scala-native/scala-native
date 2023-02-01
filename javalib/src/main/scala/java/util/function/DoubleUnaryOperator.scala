// Ported from Scala.js, commit SHA: 7b4e8a80b dated: 2022-12-06
package java.util.function

@FunctionalInterface
trait DoubleUnaryOperator {
  def applyAsDouble(operand: Double): Double

  def andThen(after: DoubleUnaryOperator): DoubleUnaryOperator = {
    (d: Double) =>
      after.applyAsDouble(applyAsDouble(d))
  }

  def compose(before: DoubleUnaryOperator): DoubleUnaryOperator = {
    (d: Double) =>
      applyAsDouble(before.applyAsDouble(d))
  }
}

object DoubleUnaryOperator {
  def identity(): DoubleUnaryOperator = (d: Double) => d
}
