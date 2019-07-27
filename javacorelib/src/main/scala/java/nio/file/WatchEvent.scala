package java.nio.file

trait WatchEvent[T] {
  def context(): T
  def count(): Int
  def kind(): WatchEvent.Kind[T]
}

object WatchEvent {
  trait Kind[T] {
    def name(): String
    def `type`(): Class[T]
  }

  trait Modifier {
    def name(): String
  }
}
