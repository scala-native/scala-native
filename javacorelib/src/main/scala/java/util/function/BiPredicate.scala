package java.util.function

trait BiPredicate[T, U] { self =>
  def and(other: BiPredicate[_ >: T, _ >: U]): BiPredicate[T, U] =
    new BiPredicate[T, U] {
      override def test(t: T, u: U): Boolean =
        self.test(t, u) && other.test(t, u)
    }

  def negate(): BiPredicate[T, U] =
    new BiPredicate[T, U] {
      override def test(t: T, u: U): Boolean =
        !self.test(t, u)
    }

  def or(other: BiPredicate[_ >: T, _ >: U]): BiPredicate[T, U] =
    new BiPredicate[T, U] {
      override def test(t: T, u: U): Boolean =
        self.test(t, u) || other.test(t, u)
    }

  def test(t: T, u: U): Boolean
}
