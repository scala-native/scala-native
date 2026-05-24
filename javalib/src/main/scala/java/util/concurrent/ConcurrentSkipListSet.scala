/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.lang.Cloneable
import java.util._

@SerialVersionUID(-2479143111061671589L)
class ConcurrentSkipListSet[E] private (
    private val map: ConcurrentNavigableMap[AnyRef, java.lang.Boolean]
) extends AbstractSet[E]
    with NavigableSet[E]
    with Cloneable
    with Serializable {

  import ConcurrentSkipListSet._

  def this() =
    this(new ConcurrentSkipListMap[AnyRef, java.lang.Boolean]())

  def this(comparator: Comparator[_ >: E]) =
    this(
      new ConcurrentSkipListMap[AnyRef, java.lang.Boolean](
        comparator.asInstanceOf[Comparator[_ >: AnyRef]]
      )
    )

  def this(collection: Collection[_ <: E]) = {
    this()
    addAll(collection)
  }

  def this(sortedSet: SortedSet[E]) = {
    this(sortedSet.comparator())
    addAll(sortedSet)
  }

  override def clone(): ConcurrentSkipListSet[E] = {
    val cloned = new ConcurrentSkipListSet[E](comparator())
    cloned.addAll(this)
    cloned
  }

  override def size(): Int =
    map.size()

  override def isEmpty(): Boolean =
    map.isEmpty()

  override def contains(o: Any): Boolean =
    map.containsKey(o)

  override def add(e: E): Boolean =
    map.putIfAbsent(e.asInstanceOf[AnyRef], PRESENT) == null

  override def remove(o: Any): Boolean =
    map.remove(o) != null

  override def clear(): Unit =
    map.clear()

  override def iterator(): Iterator[E] =
    map.navigableKeySet().iterator().asInstanceOf[Iterator[E]]

  override def descendingIterator(): Iterator[E] =
    map.descendingKeySet().iterator().asInstanceOf[Iterator[E]]

  override def lower(e: E): E =
    map.lowerKey(e.asInstanceOf[AnyRef]).asInstanceOf[E]

  override def floor(e: E): E =
    map.floorKey(e.asInstanceOf[AnyRef]).asInstanceOf[E]

  override def ceiling(e: E): E =
    map.ceilingKey(e.asInstanceOf[AnyRef]).asInstanceOf[E]

  override def higher(e: E): E =
    map.higherKey(e.asInstanceOf[AnyRef]).asInstanceOf[E]

  override def pollFirst(): E = {
    val e = map.pollFirstEntry()
    if (e == null) null.asInstanceOf[E] else e.getKey().asInstanceOf[E]
  }

  override def pollLast(): E = {
    val e = map.pollLastEntry()
    if (e == null) null.asInstanceOf[E] else e.getKey().asInstanceOf[E]
  }

  override def comparator(): Comparator[_ >: E] =
    map.comparator().asInstanceOf[Comparator[_ >: E]]

  override def first(): E =
    map.firstKey().asInstanceOf[E]

  override def last(): E =
    map.lastKey().asInstanceOf[E]

  override def subSet(
      fromElement: E,
      fromInclusive: Boolean,
      toElement: E,
      toInclusive: Boolean
  ): NavigableSet[E] =
    new ConcurrentSkipListSet[E](
      map.subMap(
        fromElement.asInstanceOf[AnyRef],
        fromInclusive,
        toElement.asInstanceOf[AnyRef],
        toInclusive
      )
    )

  override def headSet(toElement: E, inclusive: Boolean): NavigableSet[E] =
    new ConcurrentSkipListSet[E](
      map.headMap(toElement.asInstanceOf[AnyRef], inclusive)
    )

  override def tailSet(fromElement: E, inclusive: Boolean): NavigableSet[E] =
    new ConcurrentSkipListSet[E](
      map.tailMap(fromElement.asInstanceOf[AnyRef], inclusive)
    )

  override def subSet(fromElement: E, toElement: E): NavigableSet[E] =
    subSet(fromElement, true, toElement, false)

  override def headSet(toElement: E): NavigableSet[E] =
    headSet(toElement, false)

  override def tailSet(fromElement: E): NavigableSet[E] =
    tailSet(fromElement, true)

  override def descendingSet(): NavigableSet[E] =
    new ConcurrentSkipListSet[E](map.descendingMap())
}

private object ConcurrentSkipListSet {
  private val PRESENT = java.lang.Boolean.TRUE
}
