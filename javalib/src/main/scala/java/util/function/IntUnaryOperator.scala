// Ported from Scala.js commit: d028054 dated: 2022-05-16

package java.util.function

import scala.scalanative.annotation.JavaDefaultMethod

@FunctionalInterface
trait IntUnaryOperator {
  def applyAsInt(operand: Int): Int

  @JavaDefaultMethod
  def andThen(after: IntUnaryOperator): IntUnaryOperator = { (i: Int) =>
    after.applyAsInt(applyAsInt(i))
  }

  @JavaDefaultMethod
  def compose(before: IntUnaryOperator): IntUnaryOperator = { (i: Int) =>
    applyAsInt(before.applyAsInt(i))
  }
}

object IntUnaryOperator {
  def identity(): IntUnaryOperator = (i: Int) => i
}
