package java.util.function

trait Consumer[T] {
  self =>

  def accept(t: T): Unit

  def andThen(after: Consumer[T]): Consumer[T] = new Consumer[T]() {
    override def accept(t: T): Unit = {
      self.accept(t)
      after.accept(t)
    }
  }

}
