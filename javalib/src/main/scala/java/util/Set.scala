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
  def addAll(coll: Collection[_ <: E]): Boolean
}
