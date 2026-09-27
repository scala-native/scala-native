/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

/*
 * Mostly a mechanical migration, on purpose. Structure and control flow
 * follow the original JSR-166 source as closely as possible.
 */

package java.util.concurrent

import java.io.Serializable
import java.util.function.{Consumer, Predicate}
import java.util.{
  AbstractSet, Collection, Iterator, Objects, Set, Spliterator, Spliterators
}

import scala.scalanative.annotation.safePublish

// scalafmt: { maxColumn = 120}

object CopyOnWriteArraySet {

  /** Tells whether the objects in snapshot (regarded as a set) are a superset of the given set.
   *
   *  @return
   *    -1 if snapshot is not a superset, 0 if the two sets contain precisely the same elements, and 1 if snapshot is a
   *    proper superset of the given set
   */
  private def compareSets(snapshot: Array[Object], set: Set[_]): Int = {
    // Uses O(n^2) algorithm, that is only appropriate for small
    // sets, which CopyOnWriteArraySets should be.
    //
    // Optimize up to O(n) if the two sets share a long common prefix,
    // as might happen if one set was created as a copy of the other set.

    val len = snapshot.length
    // Mark matched elements to avoid re-checking
    val matched = new Array[Boolean](len)

    // j is the largest int with matched[i] true for { i | 0 <= i < j }
    var j = 0
    val it = set.iterator()
    while (it.hasNext()) {
      val x = it.next()
      var continueOuter = false
      var i = j
      while (i < len && !continueOuter) {
        if (!matched(i) && Objects.equals(x, snapshot(i))) {
          matched(i) = true
          if (i == j) {
            j += 1
            while (j < len && matched(j)) {
              j += 1
            }
          }
          continueOuter = true
        }
        i += 1
      }
      if (!continueOuter)
        return -1
    }
    if (j == len) 0 else 1
  }
}

@SerialVersionUID(5457747651344034263L)
class CopyOnWriteArraySet[E <: AnyRef] private (
    @safePublish private val al: CopyOnWriteArrayList[E]
) extends AbstractSet[E]
    with Serializable {
  import CopyOnWriteArraySet._

  def this() = {
    this(new CopyOnWriteArrayList[E]())
  }

  // Scala auxiliary constructors must call this() / primary first.
  def this(c: Collection[_ <: E]) = {
    this(
      if (c.getClass() == classOf[CopyOnWriteArraySet[_]])
        new CopyOnWriteArrayList[E](c.asInstanceOf[CopyOnWriteArraySet[E]].al)
      else {
        val list = new CopyOnWriteArrayList[E]()
        list.addAllAbsent(c)
        list
      }
    )
  }

  def size(): Int = {
    al.size()
  }

  override def isEmpty(): Boolean = {
    al.isEmpty()
  }

  override def contains(o: Any): Boolean = {
    al.contains(o)
  }

  override def toArray(): Array[Object] = {
    al.toArray()
  }

  override def toArray[T <: AnyRef](a: Array[T]): Array[T] = {
    al.toArray(a)
  }

  override def clear(): Unit = {
    al.clear()
  }

  override def remove(o: Any): Boolean = {
    al.remove(o)
  }

  override def add(e: E): Boolean = {
    al.addIfAbsent(e)
  }

  override def containsAll(c: Collection[_]): Boolean = {
    if (c.isInstanceOf[Set[_]])
      compareSets(al.getArray(), c.asInstanceOf[Set[_]]) >= 0
    else
      al.containsAll(c)
  }

  override def addAll(c: Collection[_ <: E]): Boolean = {
    al.addAllAbsent(c) > 0
  }

  override def removeAll(c: Collection[_]): Boolean = {
    al.removeAll(c)
  }

  override def retainAll(c: Collection[_]): Boolean = {
    al.retainAll(c)
  }

  def iterator(): Iterator[E] = {
    al.iterator()
  }

  override def equals(o: Any): Boolean = {
    (o.asInstanceOf[AnyRef] eq this) ||
    (o.isInstanceOf[Set[_]] && compareSets(al.getArray(), o.asInstanceOf[Set[_]]) == 0)
  }

  override def removeIf(filter: Predicate[_ >: E]): Boolean = {
    al.removeIf(filter)
  }

  override def forEach(action: Consumer[_ >: E]): Unit = {
    al.forEach(action)
  }

  override def spliterator(): Spliterator[E] = {
    Spliterators.spliterator(al.getArray(), Spliterator.IMMUTABLE | Spliterator.DISTINCT)
  }
}
