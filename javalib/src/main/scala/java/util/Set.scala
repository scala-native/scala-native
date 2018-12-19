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
  // TODO: remove forwarders - 0.3.8 workaround
  def addAll(coll: Collection[_ <: E]): Boolean
  //TODO:
  //def hashCode(): scala.Int
  //def toArray(): Array[Any]
  //def toArray[T](array: Array[T]): Array[T]
  //def contains(coll: Collection[_]): scala.Boolean
  //def equals(obj: Any): scala.Boolean
}
