// Ported from Scala.js, commit sha:7b4e8a80b dated:2022-12-06
package java.util.function

@FunctionalInterface
trait LongUnaryOperator {
  def applyAsLong(operand: Long): Long

  def andThen(after: LongUnaryOperator): LongUnaryOperator = { (l: Long) =>
    after.applyAsLong(applyAsLong(l))
  }

  def compose(before: LongUnaryOperator): LongUnaryOperator = { (l: Long) =>
    applyAsLong(before.applyAsLong(l))
  }
}

object LongUnaryOperator {
  def identity(): LongUnaryOperator = (l: Long) => l
}