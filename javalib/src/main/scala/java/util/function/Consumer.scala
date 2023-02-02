// Ported from Scala.js, commit SHA: 7b4e8a80b dated: 2022-12-06
package java.util.function

@FunctionalInterface
trait Consumer[T] { self =>
  def accept(t: T): Unit

  def andThen(after: Consumer[_ >: T]): Consumer[T] = {
    new Consumer[T] {
      def accept(t: T): Unit = {
        self.accept(t)
        after.accept(t)
      }
    }
  }
}
