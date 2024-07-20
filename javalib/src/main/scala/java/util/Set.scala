package java.util

trait Set[E] extends Collection[E] {
  def size(): scala.Int
  def add(obj: E): scala.Boolean
  def contains(obj: Any): scala.Boolean
  def containsAll(c: Collection[_]): Boolean
  def isEmpty(): scala.Boolean
  def iterator(): Iterator[E]
  def clear(): Unit
  def remove(obj: Any): scala.Boolean
  def removeAll(c: Collection[_]): Boolean
  def retainAll(c: Collection[_]): Boolean

  // TODO:
  // def addAll(coll: Collection[_ <: E]): scala.Boolean
  // def hashCode(): scala.Int
  // def toArray(): Array[Any]
  // def toArray[T](array: Array[T]): Array[T]
  // def contains(coll: Collection[_]): scala.Boolean
  // def equals(obj: Any): scala.Boolean

  override def spliterator(): Spliterator[E] = {
    Spliterators.spliterator[E](
      this,
      Spliterator.SIZED |
        Spliterator.SUBSIZED |
        Spliterator.DISTINCT
    )
  }
}
