package java.util.function

trait Predicate[T] { self =>
  def test(t: T): Boolean

  def negate(): Predicate[T] = new Predicate[T] {
    override def test(t: T): Boolean =
      !self.test(t)
  }

  def and(other: Predicate[_ >: T]): Predicate[T] = new Predicate[T] {
    override def test(t: T): Boolean =
      test(t) && other.test(t)
  }

  def or(other: Predicate[_ >: T]): Predicate[T] = new Predicate[T] {
    override def test(t: T): Boolean =
      test(t) || other.test(t)
  }
}
