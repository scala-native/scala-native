package scala.scalanative
package testinterface
package serialization

trait Serializable[T] {
  def serialize(v: T): Iterator[String]
  def deserialize(in: Iterator[String]): T
  def name: String
}

object Serializable {
  def apply[T](_name: String,
               s: T => Iterator[String],
               d: Iterator[String] => T): Serializable[T] =
    new Serializable[T] {
      override def name: String                         = _name
      override def serialize(v: T): Iterator[String]    = s(v)
      override def deserialize(in: Iterator[String]): T = d(in)
    }
}
