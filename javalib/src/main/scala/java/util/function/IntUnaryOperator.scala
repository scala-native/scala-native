// Ported from Scala.js, commit SHA: 7b4e8a80b dated: 2022-12-06
package java.util.function

@FunctionalInterface
trait IntUnaryOperator {
  def applyAsInt(operand: Int): Int

  def andThen(after: IntUnaryOperator): IntUnaryOperator = { (i: Int) =>
    after.applyAsInt(applyAsInt(i))
  }

  def compose(before: IntUnaryOperator): IntUnaryOperator = { (i: Int) =>
    applyAsInt(before.applyAsInt(i))
  }
}

object IntUnaryOperator {
  def identity(): IntUnaryOperator = (i: Int) => i
}
