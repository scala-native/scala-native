// Ported from Scala.js commit: f122aa5 dated: 2019-07-03
// Additional Spliterator code implemented for Scala Native
// Additional Stream code implemented for Scala Native
package java.util

import java.util.function.Predicate
import java.util.stream.{Stream, StreamSupport}

trait Collection[E] extends java.lang.Iterable[E] {
  def size(): Int
  def isEmpty(): Boolean
  def contains(o: Any): Boolean
  def iterator(): Iterator[E]
  def toArray(): Array[AnyRef]
  def toArray[T <: AnyRef](a: Array[T]): Array[T]
  def add(e: E): Boolean
  def remove(o: Any): Boolean
  def containsAll(c: Collection[_]): Boolean
  def addAll(c: Collection[_ <: E]): Boolean
  def removeAll(c: Collection[_]): Boolean

  def removeIf(filter: Predicate[_ >: E]): Boolean = {
    var result = false
    val iter = iterator()
    while (iter.hasNext()) {
      if (filter.test(iter.next())) {
        iter.remove()
        result = true
      }
    }
    result
  }

  def retainAll(c: Collection[_]): Boolean
  def clear(): Unit
  def equals(o: Any): Boolean
  def hashCode(): Int

  /* From the Java documentation:
   *   "The default implementation should be overridden by subclasses that
   *    can return a more efficient spliterator."
   */
  override def spliterator(): Spliterator[E] = {
    Spliterators.spliterator[E](this, Spliterator.SIZED | Spliterator.SUBSIZED)
  }

  /* From the Java documentation:
   *   "The default implementation should be overridden by subclasses that
   *   "This method should be overridden when the spliterator() method cannot
   *    return a spliterator that is IMMUTABLE, CONCURRENT, or late-binding.
   *    (See spliterator() for details.)""
   */
  def stream(): Stream[E] =
    StreamSupport.stream(this.spliterator(), parallel = false)

  def parallelStream(): Stream[E] =
    StreamSupport.stream(this.spliterator(), parallel = true)
}
