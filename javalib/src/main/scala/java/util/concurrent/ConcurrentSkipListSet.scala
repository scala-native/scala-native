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
import java.util._

import scala.scalanative.annotation.safePublish

@SerialVersionUID(-2479143111061671589L)
class ConcurrentSkipListSet[E] private[concurrent] (
    @safePublish private val m: ConcurrentNavigableMap[E, AnyRef]
) extends AbstractSet[E]
    with NavigableSet[E]
    with Cloneable
    with Serializable {

  def this() = {
    this(new ConcurrentSkipListMap[E, AnyRef]())
  }

  def this(comparator: Comparator[_ >: E]) = {
    this(new ConcurrentSkipListMap[E, AnyRef](comparator))
  }

  def this(c: Collection[_ <: E]) = {
    this(new ConcurrentSkipListMap[E, AnyRef]())
    addAll(c)
  }

  def this(s: SortedSet[E]) = {
    this(new ConcurrentSkipListMap[E, AnyRef](s.comparator()))
    addAll(s)
  }

  // Java clone() uses reflection to overwrite the final m after super.clone().
  // SN does not allow mutating vals; construct a fresh set with a map copy.
  override def clone(): ConcurrentSkipListSet[E] = {
    new ConcurrentSkipListSet[E](new ConcurrentSkipListMap[E, AnyRef](m))
  }

  /* ---------------- Set operations -------------- */

  def size(): Int = {
    m.size()
  }

  override def isEmpty(): Boolean = {
    m.isEmpty()
  }

  override def contains(o: Any): Boolean = {
    m.containsKey(o)
  }

  override def add(e: E): Boolean = {
    m.putIfAbsent(e, java.lang.Boolean.TRUE) == null
  }

  override def remove(o: Any): Boolean = {
    m.remove(o, java.lang.Boolean.TRUE)
  }

  override def clear(): Unit = {
    m.clear()
  }

  def iterator(): Iterator[E] = {
    m.navigableKeySet().iterator()
  }

  def descendingIterator(): Iterator[E] = {
    m.descendingKeySet().iterator()
  }

  /* ---------------- AbstractSet Overrides -------------- */

  override def equals(o: Any): Boolean = {
    // Override AbstractSet version to avoid calling size()
    // Use eq: Scala == would recurse into equals (same as ConcurrentSkipListMap)
    if (o.asInstanceOf[AnyRef] eq this)
      return true
    if (!o.isInstanceOf[Set[_]])
      return false
    val c: Collection[_] = o.asInstanceOf[Collection[_]]
    try {
      containsAll(c) && c.containsAll(this)
    } catch {
      case _: ClassCastException | _: NullPointerException =>
        false
    }
  }

  override def removeAll(c: Collection[_]): Boolean = {
    // Override AbstractSet version to avoid unnecessary call to size()
    var modified = false
    val _it_e = c.iterator()
    while (_it_e.hasNext()) {
      if (remove(_it_e.next()))
        modified = true
    }
    modified
  }

  /* ---------------- Relational operations -------------- */

  def lower(e: E): E = {
    m.lowerKey(e)
  }

  def floor(e: E): E = {
    m.floorKey(e)
  }

  def ceiling(e: E): E = {
    m.ceilingKey(e)
  }

  def higher(e: E): E = {
    m.higherKey(e)
  }

  def pollFirst(): E = {
    val e: Map.Entry[E, AnyRef] = m.pollFirstEntry()
    if (e == null) null.asInstanceOf[E] else e.getKey()
  }

  def pollLast(): E = {
    val e: Map.Entry[E, AnyRef] = m.pollLastEntry()
    if (e == null) null.asInstanceOf[E] else e.getKey()
  }

  /* ---------------- SortedSet operations -------------- */

  def comparator(): Comparator[_ >: E] = {
    m.comparator()
  }

  def first(): E = {
    m.firstKey()
  }

  def last(): E = {
    m.lastKey()
  }

  def subSet(
      fromElement: E,
      fromInclusive: Boolean,
      toElement: E,
      toInclusive: Boolean
  ): NavigableSet[E] = {
    new ConcurrentSkipListSet[E](
      m.subMap(fromElement, fromInclusive, toElement, toInclusive)
    )
  }

  def headSet(toElement: E, inclusive: Boolean): NavigableSet[E] = {
    new ConcurrentSkipListSet[E](m.headMap(toElement, inclusive))
  }

  def tailSet(fromElement: E, inclusive: Boolean): NavigableSet[E] = {
    new ConcurrentSkipListSet[E](m.tailMap(fromElement, inclusive))
  }

  def subSet(fromElement: E, toElement: E): NavigableSet[E] = {
    subSet(fromElement, true, toElement, false)
  }

  def headSet(toElement: E): NavigableSet[E] = {
    headSet(toElement, false)
  }

  def tailSet(fromElement: E): NavigableSet[E] = {
    tailSet(fromElement, true)
  }

  def descendingSet(): NavigableSet[E] = {
    new ConcurrentSkipListSet[E](m.descendingMap())
  }

  override def spliterator(): Spliterator[E] = {
    if (m.isInstanceOf[ConcurrentSkipListMap[_, _]])
      m.asInstanceOf[ConcurrentSkipListMap[E, _]].keySpliterator()
    else {
      val _o = m.asInstanceOf[ConcurrentSkipListMap.SubMap[E, _]]
      new _o.SubMapKeyIterator()
    }
  }
}
