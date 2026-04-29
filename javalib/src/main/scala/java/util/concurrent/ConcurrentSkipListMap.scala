/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util._
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.function.{BiConsumer, BiFunction, Function}

import scala.language.existentials

@SerialVersionUID(-8627078645895051609L)
class ConcurrentSkipListMap[K, V] private (
    private val backing: ConcurrentSkipListMap.Backing[K, V],
    private val viewSelector: TreeMap[K, V] => NavigableMap[K, V]
) extends AbstractMap[K, V]
    with ConcurrentNavigableMap[K, V]
    with Cloneable
    with Serializable {

  import ConcurrentSkipListMap._

  def this() =
    this(
      new ConcurrentSkipListMap.Backing[K, V](new TreeMap[K, V]()),
      (map: TreeMap[K, V]) => map
    )

  def this(comparator: Comparator[_ >: K]) =
    this(
      new ConcurrentSkipListMap.Backing[K, V](
        new TreeMap[K, V](comparator)
      ),
      (map: TreeMap[K, V]) => map
    )

  def this(m: Map[_ <: K, _ <: V]) = {
    this()
    putAll(m)
  }

  def this(m: SortedMap[K, _ <: V]) = {
    this(m.comparator())
    putAll(m)
  }

  private def readOp[T](body: => T): T = {
    val lock = backing.lock.readLock()
    lock.lock()
    try body
    finally lock.unlock()
  }

  private def writeOp[T](body: => T): T = {
    val lock = backing.lock.writeLock()
    lock.lock()
    try body
    finally lock.unlock()
  }

  private def view(): NavigableMap[K, V] = viewSelector(backing.map)

  private def requireKey(key: Any): K = {
    if (key == null) throw new NullPointerException
    key.asInstanceOf[K]
  }

  private def requireValue(value: Any): V = {
    if (value == null) throw new NullPointerException
    value.asInstanceOf[V]
  }

  private def immutableEntry(entry: Map.Entry[K, V]): Map.Entry[K, V] =
    if (entry == null) null
    else new AbstractMap.SimpleImmutableEntry[K, V](entry)

  private def withSubView(
      selector: NavigableMap[K, V] => NavigableMap[K, V]
  ): ConcurrentSkipListMap[K, V] = {
    readOp { selector(view()); () } // validate bounds and null handling now
    new ConcurrentSkipListMap[K, V](
      backing,
      (root: TreeMap[K, V]) => selector(viewSelector(root))
    )
  }

  private[concurrent] def snapshotEntries(): ArrayList[Map.Entry[K, V]] =
    readOp {
      val v = view()
      val entries = new ArrayList[Map.Entry[K, V]]()
      val it = v.navigableKeySet().iterator()
      while (it.hasNext()) {
        val key = it.next()
        entries.add(new AbstractMap.SimpleImmutableEntry[K, V](key, v.get(key)))
      }
      entries
    }

  private[concurrent] def removeSnapshotEntry(
      entry: Map.Entry[K, V]
  ): Boolean =
    remove(entry.getKey(), entry.getValue())

  override def size(): Int =
    readOp(view().size())

  override def isEmpty(): Boolean =
    readOp(view().isEmpty())

  override def containsKey(key: Any): Boolean = {
    val k = requireKey(key)
    readOp(view().containsKey(k))
  }

  override def containsValue(value: Any): Boolean = {
    requireValue(value)
    readOp(view().containsValue(value))
  }

  override def get(key: Any): V = {
    val k = requireKey(key)
    readOp(view().get(k))
  }

  override def getOrDefault(key: Any, defaultValue: V): V = {
    val value = get(key)
    if (value == null) defaultValue else value
  }

  override def put(key: K, value: V): V = {
    requireKey(key)
    requireValue(value)
    writeOp(view().put(key, value))
  }

  override def putAll(m: Map[_ <: K, _ <: V]): Unit = {
    Objects.requireNonNull(m)
    val it = m.entrySet().iterator()
    while (it.hasNext()) {
      val e = it.next()
      requireKey(e.getKey())
      requireValue(e.getValue())
    }
    val it2 = m.entrySet().iterator()
    writeOp {
      val v = view()
      while (it2.hasNext()) {
        val e = it2.next()
        v.put(e.getKey(), e.getValue())
      }
    }
  }

  override def remove(key: Any): V = {
    val k = requireKey(key)
    writeOp(view().remove(k))
  }

  override def clear(): Unit =
    writeOp(view().clear())

  override def putIfAbsent(key: K, value: V): V = {
    requireKey(key)
    requireValue(value)
    writeOp {
      val v = view()
      val oldValue = v.get(key)
      if (oldValue == null) v.put(key, value)
      oldValue
    }
  }

  override def remove(key: Any, value: Any): Boolean = {
    val k = requireKey(key)
    if (value == null) false
    else
      writeOp {
        val v = view()
        val oldValue = v.get(k)
        if (oldValue != null && Objects.equals(oldValue, value)) {
          v.remove(k)
          true
        } else false
      }
  }

  override def replace(key: K, oldValue: V, newValue: V): Boolean = {
    requireKey(key)
    requireValue(oldValue)
    requireValue(newValue)
    writeOp {
      val v = view()
      val current = v.get(key)
      if (current != null && Objects.equals(current, oldValue)) {
        v.put(key, newValue)
        true
      } else false
    }
  }

  override def replace(key: K, value: V): V = {
    requireKey(key)
    requireValue(value)
    writeOp {
      val v = view()
      if (v.containsKey(key)) v.put(key, value)
      else null.asInstanceOf[V]
    }
  }

  override def computeIfAbsent(
      key: K,
      mappingFunction: Function[_ >: K, _ <: V]
  ): V = {
    requireKey(key)
    Objects.requireNonNull(mappingFunction)
    writeOp {
      val v = view()
      val oldValue = v.get(key)
      if (oldValue != null) oldValue
      else {
        val newValue = mappingFunction.apply(key)
        if (newValue != null) v.put(key, newValue)
        newValue
      }
    }
  }

  override def computeIfPresent(
      key: K,
      remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]
  ): V = {
    requireKey(key)
    Objects.requireNonNull(remappingFunction)
    writeOp {
      val v = view()
      val oldValue = v.get(key)
      if (oldValue == null) null.asInstanceOf[V]
      else {
        val newValue = remappingFunction.apply(key, oldValue)
        if (newValue == null) v.remove(key)
        else v.put(key, newValue)
        newValue
      }
    }
  }

  override def compute(
      key: K,
      remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]
  ): V = {
    requireKey(key)
    Objects.requireNonNull(remappingFunction)
    writeOp {
      val v = view()
      val oldValue = v.get(key)
      val newValue = remappingFunction.apply(key, oldValue)
      if (newValue == null) v.remove(key)
      else v.put(key, newValue)
      newValue
    }
  }

  override def merge(
      key: K,
      value: V,
      remappingFunction: BiFunction[_ >: V, _ >: V, _ <: V]
  ): V = {
    requireKey(key)
    requireValue(value)
    Objects.requireNonNull(remappingFunction)
    writeOp {
      val v = view()
      val oldValue = v.get(key)
      val newValue =
        if (oldValue == null) value
        else remappingFunction.apply(oldValue, value)
      if (newValue == null) v.remove(key)
      else v.put(key, newValue)
      newValue
    }
  }

  override def replaceAll(
      function: BiFunction[_ >: K, _ >: V, _ <: V]
  ): Unit = {
    Objects.requireNonNull(function)
    writeOp {
      val v = view()
      val it = v.entrySet().iterator()
      while (it.hasNext()) {
        val e = it.next()
        val newValue = function.apply(e.getKey(), e.getValue())
        if (newValue == null) throw new NullPointerException
        e.setValue(newValue)
      }
    }
  }

  override def forEach(action: BiConsumer[_ >: K, _ >: V]): Unit = {
    Objects.requireNonNull(action)
    val entries = snapshotEntries()
    val it = entries.iterator()
    while (it.hasNext()) {
      val e = it.next()
      action.accept(e.getKey(), e.getValue())
    }
  }

  override def entrySet(): Set[Map.Entry[K, V]] =
    new EntrySet[K, V](this)

  override def keySet(): NavigableSet[K] =
    navigableKeySet()

  override def navigableKeySet(): NavigableSet[K] =
    new KeySet[K, V](this)

  override def descendingKeySet(): NavigableSet[K] =
    descendingMap().navigableKeySet()

  override def values(): Collection[V] =
    new Values[K, V](this)

  override def comparator(): Comparator[_ >: K] =
    readOp(view().comparator())

  override def firstKey(): K =
    readOp(view().firstKey())

  override def lastKey(): K =
    readOp(view().lastKey())

  override def firstEntry(): Map.Entry[K, V] =
    readOp(immutableEntry(view().firstEntry()))

  override def lastEntry(): Map.Entry[K, V] =
    readOp(immutableEntry(view().lastEntry()))

  override def pollFirstEntry(): Map.Entry[K, V] =
    writeOp(immutableEntry(view().pollFirstEntry()))

  override def pollLastEntry(): Map.Entry[K, V] =
    writeOp(immutableEntry(view().pollLastEntry()))

  override def lowerEntry(key: K): Map.Entry[K, V] = {
    requireKey(key)
    readOp(immutableEntry(view().lowerEntry(key)))
  }

  override def lowerKey(key: K): K = {
    requireKey(key)
    readOp(view().lowerKey(key))
  }

  override def floorEntry(key: K): Map.Entry[K, V] = {
    requireKey(key)
    readOp(immutableEntry(view().floorEntry(key)))
  }

  override def floorKey(key: K): K = {
    requireKey(key)
    readOp(view().floorKey(key))
  }

  override def ceilingEntry(key: K): Map.Entry[K, V] = {
    requireKey(key)
    readOp(immutableEntry(view().ceilingEntry(key)))
  }

  override def ceilingKey(key: K): K = {
    requireKey(key)
    readOp(view().ceilingKey(key))
  }

  override def higherEntry(key: K): Map.Entry[K, V] = {
    requireKey(key)
    readOp(immutableEntry(view().higherEntry(key)))
  }

  override def higherKey(key: K): K = {
    requireKey(key)
    readOp(view().higherKey(key))
  }

  override def subMap(
      fromKey: K,
      fromInclusive: Boolean,
      toKey: K,
      toInclusive: Boolean
  ): ConcurrentNavigableMap[K, V] = {
    requireKey(fromKey)
    requireKey(toKey)
    withSubView(_.subMap(fromKey, fromInclusive, toKey, toInclusive))
  }

  override def headMap(
      toKey: K,
      inclusive: Boolean
  ): ConcurrentNavigableMap[K, V] = {
    requireKey(toKey)
    withSubView(_.headMap(toKey, inclusive))
  }

  override def tailMap(
      fromKey: K,
      inclusive: Boolean
  ): ConcurrentNavigableMap[K, V] = {
    requireKey(fromKey)
    withSubView(_.tailMap(fromKey, inclusive))
  }

  override def subMap(fromKey: K, toKey: K): ConcurrentNavigableMap[K, V] =
    subMap(fromKey, true, toKey, false)

  override def headMap(toKey: K): ConcurrentNavigableMap[K, V] =
    headMap(toKey, false)

  override def tailMap(fromKey: K): ConcurrentNavigableMap[K, V] =
    tailMap(fromKey, true)

  override def descendingMap(): ConcurrentNavigableMap[K, V] =
    withSubView(_.descendingMap())

  override def clone(): ConcurrentSkipListMap[K, V] =
    readOp {
      val cloned = new ConcurrentSkipListMap[K, V](comparator())
      cloned.putAll(view())
      cloned
    }
}

private object ConcurrentSkipListMap {
  private final class Backing[K, V](
      val map: TreeMap[K, V],
      val lock: ReentrantReadWriteLock = new ReentrantReadWriteLock()
  )

  private final class SnapshotIterator[A](
      snapshot: ArrayList[A],
      removeLast: A => Unit
  ) extends Iterator[A] {
    private val it = snapshot.iterator()
    private var last: A = _
    private var canRemove = false

    override def hasNext(): Boolean = it.hasNext()

    override def next(): A = {
      if (!it.hasNext())
        throw new NoSuchElementException
      last = it.next()
      canRemove = true
      last
    }

    override def remove(): Unit = {
      if (!canRemove) throw new IllegalStateException
      removeLast(last)
      canRemove = false
    }
  }

  private final class SnapshotKeyIterator[K, V](
      snapshot: ArrayList[Map.Entry[K, V]],
      map: ConcurrentSkipListMap[K, V]
  ) extends Iterator[K] {
    private val it = snapshot.iterator()
    private var last: Map.Entry[K, V] = _
    private var canRemove = false

    override def hasNext(): Boolean = it.hasNext()

    override def next(): K = {
      if (!it.hasNext())
        throw new NoSuchElementException
      last = it.next()
      canRemove = true
      last.getKey()
    }

    override def remove(): Unit = {
      if (!canRemove) throw new IllegalStateException
      map.remove(last.getKey())
      canRemove = false
    }
  }

  private final class SnapshotValueIterator[K, V](
      snapshot: ArrayList[Map.Entry[K, V]],
      map: ConcurrentSkipListMap[K, V]
  ) extends Iterator[V] {
    private val it = snapshot.iterator()
    private var last: Map.Entry[K, V] = _
    private var canRemove = false

    override def hasNext(): Boolean = it.hasNext()

    override def next(): V = {
      if (!it.hasNext())
        throw new NoSuchElementException
      last = it.next()
      canRemove = true
      last.getValue()
    }

    override def remove(): Unit = {
      if (!canRemove) throw new IllegalStateException
      map.removeSnapshotEntry(last)
      canRemove = false
    }
  }

  private final class EntrySet[K, V](map: ConcurrentSkipListMap[K, V])
      extends AbstractSet[Map.Entry[K, V]]
      with Serializable {

    override def size(): Int = map.size()

    override def isEmpty(): Boolean = map.isEmpty()

    override def clear(): Unit = map.clear()

    override def contains(o: Any): Boolean = o match {
      case e: Map.Entry[_, _] =>
        val value = map.get(e.getKey())
        value != null && Objects.equals(value, e.getValue())
      case _ => false
    }

    override def remove(o: Any): Boolean = o match {
      case e: Map.Entry[_, _] => map.remove(e.getKey(), e.getValue())
      case _                  => false
    }

    override def iterator(): Iterator[Map.Entry[K, V]] =
      new SnapshotIterator[Map.Entry[K, V]](
        map.snapshotEntries(),
        e => map.removeSnapshotEntry(e)
      )
  }

  private final class Values[K, V](map: ConcurrentSkipListMap[K, V])
      extends AbstractCollection[V]
      with Serializable {

    override def size(): Int = map.size()

    override def isEmpty(): Boolean = map.isEmpty()

    override def clear(): Unit = map.clear()

    override def contains(o: Any): Boolean = map.containsValue(o)

    override def iterator(): Iterator[V] = {
      val entries = map.snapshotEntries()
      new SnapshotValueIterator[K, V](entries, map)
    }
  }

  private final class KeySet[K, V](map: ConcurrentSkipListMap[K, V])
      extends AbstractSet[K]
      with NavigableSet[K]
      with Serializable {

    override def size(): Int = map.size()

    override def isEmpty(): Boolean = map.isEmpty()

    override def clear(): Unit = map.clear()

    override def contains(o: Any): Boolean = map.containsKey(o)

    override def remove(o: Any): Boolean = map.remove(o) != null

    override def add(e: K): Boolean =
      throw new UnsupportedOperationException

    override def iterator(): Iterator[K] = {
      val entries = map.snapshotEntries()
      new SnapshotKeyIterator[K, V](entries, map)
    }

    override def descendingIterator(): Iterator[K] =
      descendingSet().iterator()

    override def comparator(): Comparator[_ >: K] =
      map.comparator()

    override def first(): K = map.firstKey()

    override def last(): K = map.lastKey()

    override def lower(e: K): K = map.lowerKey(e)

    override def floor(e: K): K = map.floorKey(e)

    override def ceiling(e: K): K = map.ceilingKey(e)

    override def higher(e: K): K = map.higherKey(e)

    override def pollFirst(): K = {
      val e = map.pollFirstEntry()
      if (e == null) null.asInstanceOf[K] else e.getKey()
    }

    override def pollLast(): K = {
      val e = map.pollLastEntry()
      if (e == null) null.asInstanceOf[K] else e.getKey()
    }

    override def subSet(
        fromElement: K,
        fromInclusive: Boolean,
        toElement: K,
        toInclusive: Boolean
    ): NavigableSet[K] =
      map.subMap(fromElement, fromInclusive, toElement, toInclusive).keySet()

    override def headSet(toElement: K, inclusive: Boolean): NavigableSet[K] =
      map.headMap(toElement, inclusive).keySet()

    override def tailSet(fromElement: K, inclusive: Boolean): NavigableSet[K] =
      map.tailMap(fromElement, inclusive).keySet()

    override def subSet(fromElement: K, toElement: K): SortedSet[K] =
      subSet(fromElement, true, toElement, false)

    override def headSet(toElement: K): SortedSet[K] =
      headSet(toElement, false)

    override def tailSet(fromElement: K): SortedSet[K] =
      tailSet(fromElement, true)

    override def descendingSet(): NavigableSet[K] =
      map.descendingMap().navigableKeySet()
  }
}
