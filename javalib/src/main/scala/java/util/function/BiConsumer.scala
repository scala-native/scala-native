package java.util.function

trait BiConsumer[T, U] {
  self =>

  def accept(t: T, u: U): Unit

  def andThen(after: BiConsumer[T, U]): BiConsumer[T, U] =
    new BiConsumer[T, U]() {
      override def accept(t: T, u: U): Unit = {
        self.accept(t, u)
        after.accept(t, u)
      }
    }

}
