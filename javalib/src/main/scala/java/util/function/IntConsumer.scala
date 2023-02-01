// Ported from Scala.js, commit sha:7b4e8a80b dated:2022-12-06
package java.util.function

@FunctionalInterface
trait IntConsumer {
  def accept(value: Int): Unit

  def andThen(after: IntConsumer): IntConsumer = { (value: Int) =>
    this.accept(value)
    after.accept(value)
  }
}