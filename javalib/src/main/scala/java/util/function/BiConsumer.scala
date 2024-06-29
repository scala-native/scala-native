// Ported from Scala.js, commit SHA: 7b4e8a80b dated: 2022-12-06
package java.util.function

@FunctionalInterface
trait BiConsumer[T, U] {
  def accept(t: T, u: U): Unit

  def andThen(after: BiConsumer[T, U]): BiConsumer[T, U] = { (t: T, u: U) =>
    accept(t, u)
    after.accept(t, u)
  }
}
