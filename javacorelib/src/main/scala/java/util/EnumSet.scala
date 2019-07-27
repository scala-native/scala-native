package java.util

import java.lang.Enum

final class EnumSet[E <: Enum[E]] private (values: Array[E])
    extends AbstractSet[E]
    with Cloneable
    with Serializable {
  def iterator(): Iterator[E] =
    new Iterator[E] {
      private var i                   = 0
      override def hasNext(): Boolean = i < values.length
      override def next(): E = {
        val r = values(i)
        i += 1
        r
      }
      override def remove(): Unit = throw new UnsupportedOperationException()
    }
  def size(): Int = values.length
}

object EnumSet {
  def noneOf[E <: Enum[E]: scala.reflect.ClassTag](
      elementType: Class[E]): EnumSet[E] =
    new EnumSet[E](Array.empty[E])
}
