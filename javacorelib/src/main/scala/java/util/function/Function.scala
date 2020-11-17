package java.util.function

trait Function[T, R] { self =>
  def andThen[V](after: Function[_ >: R, _ <: V]): Function[T, V] =
    new Function[T, V] {
      override def apply(t: T): V = after.apply(self.apply(t))
    }

  def compose[V](before: Function[_ >: V, _ <: T]): Function[V, R] =
    new Function[V, R] {
      override def apply(v: V): R = self.apply(before.apply(v))
    }

  def apply(t: T): R
}

object Function {
  def identity[T](): Function[T, T] =
    new Function[T, T] {
      override def apply(t: T): T = t
    }
}
