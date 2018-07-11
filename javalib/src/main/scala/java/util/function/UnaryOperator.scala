package java.util.function

trait UnaryOperator[T] extends Function[T, T] { self =>

}

object UnaryOperator {
  def identity[T](): UnaryOperator[T] =
    new UnaryOperator[T] {
      override def apply(t: T): T = t
    }
}
