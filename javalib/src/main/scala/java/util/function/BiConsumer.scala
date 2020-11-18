// Corresponds to Scala.js commit: f86ed6 c2f5a43 dated: 2020-09-06
// Design note: Do not use lambdas with Scala Native and Scala 2.11

package java.util.function

import scala.scalanative.annotation.JavaDefaultMethod

trait BiConsumer[T, U] {
  self =>

  def accept(t: T, u: U): Unit

  @JavaDefaultMethod
  def andThen(after: BiConsumer[T, U]): BiConsumer[T, U] =
    new BiConsumer[T, U]() {
      override def accept(t: T, u: U): Unit = {
        self.accept(t, u)
        after.accept(t, u)
      }
    }
}
