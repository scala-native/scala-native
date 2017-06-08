package scala.scalanative
package testinterface
package serialization

trait Serializable[T] {
  def serialize(v: T): Iterator[String]
  def deserialize(in: Iterator[String]): T
}

object Serializable {
  def apply[T](s: T => Iterator[String],
               d: Iterator[String] => T): Serializable[T] =
    new Serializable[T] {
      override def serialize(v: T): Iterator[String]    = s(v)
      override def deserialize(in: Iterator[String]): T = d(in)
    }
}
