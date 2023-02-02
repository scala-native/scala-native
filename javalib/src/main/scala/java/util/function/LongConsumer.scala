// Ported from Scala.js, commit SHA: 7b4e8a80b dated: 2022-12-06
package java.util.function

@FunctionalInterface
trait LongConsumer {
  def accept(value: Long): Unit

  def andThen(after: LongConsumer): LongConsumer = { (value: Long) =>
    this.accept(value)
    after.accept(value)
  }
}
