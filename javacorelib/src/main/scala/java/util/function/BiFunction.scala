package java.util.function

trait BiFunction[T, U, R] { self =>
  def andThen[V](after: Function[_ >: R, _ <: V]): BiFunction[T, U, V] = {
    new BiFunction[T, U, V] {
      override def apply(t: T, u: U): V = after.apply(self.apply(t, u))
    }
  }

  def apply(t: T, u: U): R
}
