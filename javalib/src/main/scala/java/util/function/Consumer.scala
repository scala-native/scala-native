package java.util.function

import scala.scalanative.annotation.JavaDefaultMethod

trait Consumer[T] { self =>
  def accept(t: T): Unit

  @JavaDefaultMethod
  def andThen(after: Consumer[T]): Consumer[T] = new Consumer[T]() {
    def accept(t: T): Unit = {
      self.accept(t)
      after.accept(t)
    }
  }
}
