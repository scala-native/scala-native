// Ported from Scala.js, commit SHA: 7b4e8a80b dated: 2022-12-06
package java.util.function

@FunctionalInterface
trait DoubleConsumer {
  def accept(value: Double): Unit

  def andThen(after: DoubleConsumer): DoubleConsumer = { (value: Double) =>
    this.accept(value)
    after.accept(value)
  }
}
