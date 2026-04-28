/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util._
import java.util.function.Predicate

@SerialVersionUID(5457747651344034263L)
class CopyOnWriteArraySet[E <: AnyRef]()
    extends AbstractSet[E]
    with Serializable {

  private final val al = new CopyOnWriteArrayList[E]()

  def this(c: Collection[_ <: E]) = {
    this()
    c match {
      case s: CopyOnWriteArraySet[E] @unchecked =>
        al.addAllAbsent(s.al)
      case _ =>
        al.addAllAbsent(c)
    }
  }

  override def size(): Int = al.size()

  override def isEmpty(): Boolean = al.isEmpty()

  override def contains(o: Any): Boolean = al.contains(o)

  override def toArray(): Array[AnyRef] = al.toArray()

  override def toArray[T <: AnyRef](a: Array[T]): Array[T] = al.toArray(a)

  override def clear(): Unit = al.clear()

  override def remove(o: Any): Boolean = al.remove(o)

  override def add(e: E): Boolean = al.addIfAbsent(e)

  override def containsAll(c: Collection[_]): Boolean = al.containsAll(c)

  override def addAll(c: Collection[_ <: E]): Boolean =
    al.addAllAbsent(c) > 0

  override def removeAll(c: Collection[_]): Boolean = al.removeAll(c)

  override def retainAll(c: Collection[_]): Boolean = al.retainAll(c)

  override def removeIf(filter: Predicate[_ >: E]): Boolean =
    al.removeIf(filter)

  override def iterator(): Iterator[E] = al.iterator()

  override def equals(o: Any): Boolean = {
    if (o.asInstanceOf[AnyRef] eq this) true
    else
      o match {
        case set: Set[_] =>
          set.size() == size() && containsAll(set)
        case _ => false
      }
  }

  override def spliterator(): Spliterator[E] =
    Spliterators.spliterator(
      al,
      Spliterator.IMMUTABLE | Spliterator.DISTINCT
    )
}
