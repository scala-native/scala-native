/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util._
import java.util.function.{BiConsumer, BiFunction, Function}

import scala.annotation.tailrec
import scala.language.existentials

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic.AtomicRef
import scala.scalanative.runtime.{Intrinsics, fromRawPtr}
import scala.scalanative.unsafe._

@SerialVersionUID(-8627078645895051609L)
class ConcurrentSkipListMap[K, V] private (
    private val backing: ConcurrentSkipListMap.Backing[K, V],
    private val lo: K,
    private val loInclusive: Boolean,
    private val loSet: Boolean,
    private val hi: K,
    private val hiInclusive: Boolean,
    private val hiSet: Boolean,
    private val descending: Boolean
) extends AbstractMap[K, V]
    with ConcurrentNavigableMap[K, V]
    with Cloneable
    with Serializable {

  import ConcurrentSkipListMap._

  def this() =
    this(
      new ConcurrentSkipListMap.Backing[K, V](null),
      null.asInstanceOf[K],
      false,
      false,
      null.asInstanceOf[K],
      false,
      false,
      false
    )

  def this(comparator: Comparator[_ >: K]) =
    this(
      new ConcurrentSkipListMap.Backing[K, V](comparator),
      null.asInstanceOf[K],
      false,
      false,
      null.asInstanceOf[K],
      false,
      false,
      false
    )

  def this(m: Map[_ <: K, _ <: V]) = {
    this()
    putAll(m)
  }

  def this(m: SortedMap[K, _ <: V]) = {
    this(m.comparator())
    putAll(m)
  }

  if (loSet && hiSet && backing.compare(lo, hi) > 0)
    throw new IllegalArgumentException("inconsistent range")

  private def requireKey(key: Any): K = {
    if (key == null) throw new NullPointerException
    key.asInstanceOf[K]
  }

  private def requireValue(value: Any): V = {
    if (value == null) throw new NullPointerException
    value.asInstanceOf[V]
  }

  private def tooLow(key: Any): Boolean =
    loSet && {
      val c = backing.compareAny(key, lo)
      c < 0 || (c == 0 && !loInclusive)
    }

  private def tooHigh(key: Any): Boolean =
    hiSet && {
      val c = backing.compareAny(key, hi)
      c > 0 || (c == 0 && !hiInclusive)
    }

  private def inBounds(key: Any): Boolean =
    !tooLow(key) && !tooHigh(key)

  private def checkKeyBounds(key: K): Unit = {
    requireKey(key)
    if (!inBounds(key)) throw new IllegalArgumentException("key out of range")
  }

  private def immutableEntry(
      key: K,
      value: V
  ): Map.Entry[K, V] =
    if (value == null) null
    else new AbstractMap.SimpleImmutableEntry[K, V](key, value)

  private def immutableEntry(
      node: Node[K, V]
  ): Map.Entry[K, V] =
    if (node == null) null
    else immutableEntry(node.key, node.value)

  private def lowestNodeAscending(): Node[K, V] = {
    val n =
      if (!loSet) backing.firstNode()
      else if (loInclusive) backing.findGreaterOrEqual(lo)
      else backing.findGreaterThan(lo)
    if (n != null && !tooHigh(n.key)) n else null
  }

  private def highestNodeAscending(): Node[K, V] = {
    val n =
      if (!hiSet) backing.lastNode()
      else if (hiInclusive) backing.findLessOrEqual(hi)
      else backing.findLessThan(hi)
    if (n != null && !tooLow(n.key)) n else null
  }

  private def firstNodeInView(): Node[K, V] =
    if (descending) highestNodeAscending()
    else lowestNodeAscending()

  private def lastNodeInView(): Node[K, V] =
    if (descending) lowestNodeAscending()
    else highestNodeAscending()

  private def ascLowerNode(key: K): Node[K, V] =
    if (tooLow(key)) null
    else if (tooHigh(key)) highestNodeAscending()
    else {
      val n = backing.findLessThan(key)
      if (n != null && inBounds(n.key)) n else null
    }

  private def ascFloorNode(key: K): Node[K, V] =
    if (tooLow(key)) null
    else if (tooHigh(key)) highestNodeAscending()
    else {
      val n = backing.findLessOrEqual(key)
      if (n != null && inBounds(n.key)) n else null
    }

  private def ascCeilingNode(key: K): Node[K, V] =
    if (tooLow(key)) lowestNodeAscending()
    else if (tooHigh(key)) null
    else {
      val n = backing.findGreaterOrEqual(key)
      if (n != null && inBounds(n.key)) n else null
    }

  private def ascHigherNode(key: K): Node[K, V] =
    if (tooLow(key)) lowestNodeAscending()
    else if (tooHigh(key)) null
    else {
      val n = backing.findGreaterThan(key)
      if (n != null && inBounds(n.key)) n else null
    }

  private def lowerNodeInView(key: K): Node[K, V] =
    if (descending) ascHigherNode(key) else ascLowerNode(key)

  private def floorNodeInView(key: K): Node[K, V] =
    if (descending) ascCeilingNode(key) else ascFloorNode(key)

  private def ceilingNodeInView(key: K): Node[K, V] =
    if (descending) ascFloorNode(key) else ascCeilingNode(key)

  private def higherNodeInView(key: K): Node[K, V] =
    if (descending) ascLowerNode(key) else ascHigherNode(key)

  private def newSubMap(
      fromKey0: K,
      fromSet0: Boolean,
      fromInclusive0: Boolean,
      toKey0: K,
      toSet0: Boolean,
      toInclusive0: Boolean
  ): ConcurrentSkipListMap[K, V] = {
    var fromKey = fromKey0
    var fromSet = fromSet0
    var fromInclusive = fromInclusive0
    var toKey = toKey0
    var toSet = toSet0
    var toInclusive = toInclusive0

    if (descending) {
      val k = fromKey
      val ks = fromSet
      val ki = fromInclusive
      fromKey = toKey
      fromSet = toSet
      fromInclusive = toInclusive
      toKey = k
      toSet = ks
      toInclusive = ki
    }

    if (loSet) {
      if (!fromSet) {
        fromKey = lo
        fromSet = true
        fromInclusive = loInclusive
      } else {
        val c = backing.compare(fromKey, lo)
        if (c < 0 || (c == 0 && !loInclusive && fromInclusive))
          throw new IllegalArgumentException("key out of range")
      }
    }

    if (hiSet) {
      if (!toSet) {
        toKey = hi
        toSet = true
        toInclusive = hiInclusive
      } else {
        val c = backing.compare(toKey, hi)
        if (c > 0 || (c == 0 && !hiInclusive && toInclusive))
          throw new IllegalArgumentException("key out of range")
      }
    }

    new ConcurrentSkipListMap[K, V](
      backing,
      fromKey,
      fromInclusive,
      fromSet,
      toKey,
      toInclusive,
      toSet,
      descending
    )
  }

  private[concurrent] def snapshotEntries(): ArrayList[Map.Entry[K, V]] = {
    val entries = new ArrayList[Map.Entry[K, V]]()
    var n = lowestNodeAscending()
    while (n != null) {
      val value = n.value
      if (value != null)
        entries.add(new AbstractMap.SimpleImmutableEntry[K, V](n.key, value))
      n = backing.findGreaterThan(n.key)
      if (n != null && tooHigh(n.key)) n = null
    }
    if (!descending) entries
    else {
      val reversed = new ArrayList[Map.Entry[K, V]](entries.size())
      var i = entries.size() - 1
      while (i >= 0) {
        reversed.add(entries.get(i))
        i -= 1
      }
      reversed
    }
  }

  private[concurrent] def removeSnapshotEntry(
      entry: Map.Entry[K, V]
  ): Boolean =
    remove(entry.getKey(), entry.getValue())

  override def size(): Int = {
    var count = 0L
    var n = lowestNodeAscending()
    while (n != null) {
      if (n.value != null) count += 1L
      n = backing.findGreaterThan(n.key)
      if (n != null && tooHigh(n.key)) n = null
    }
    if (count >= Int.MaxValue) Int.MaxValue else count.toInt
  }

  override def isEmpty(): Boolean =
    lowestNodeAscending() == null

  override def containsKey(key: Any): Boolean = {
    val k = requireKey(key)
    inBounds(k) && backing.get(k) != null
  }

  override def containsValue(value: Any): Boolean = {
    requireValue(value)
    var n = lowestNodeAscending()
    while (n != null) {
      val v = n.value
      if (v != null && Objects.equals(v, value)) return true
      n = backing.findGreaterThan(n.key)
      if (n != null && tooHigh(n.key)) n = null
    }
    false
  }

  override def get(key: Any): V = {
    val k = requireKey(key)
    if (!inBounds(k)) null.asInstanceOf[V]
    else backing.get(k)
  }

  override def getOrDefault(key: Any, defaultValue: V): V = {
    val value = get(key)
    if (value == null) defaultValue else value
  }

  override def put(key: K, value: V): V = {
    checkKeyBounds(key)
    requireValue(value)
    backing.put(key, value, onlyIfAbsent = false)
  }

  override def putAll(m: Map[_ <: K, _ <: V]): Unit = {
    Objects.requireNonNull(m)
    val it = m.entrySet().iterator()
    while (it.hasNext()) {
      val e = it.next()
      checkKeyBounds(e.getKey())
      requireValue(e.getValue())
    }
    val it2 = m.entrySet().iterator()
    while (it2.hasNext()) {
      val e = it2.next()
      backing.put(e.getKey(), e.getValue(), onlyIfAbsent = false)
    }
  }

  override def remove(key: Any): V = {
    val k = requireKey(key)
    if (!inBounds(k)) null.asInstanceOf[V]
    else backing.remove(k, null)
  }

  override def clear(): Unit = {
    val entries = snapshotEntries()
    val it = entries.iterator()
    while (it.hasNext()) {
      val e = it.next()
      backing.remove(e.getKey(), null)
    }
  }

  override def putIfAbsent(key: K, value: V): V = {
    checkKeyBounds(key)
    requireValue(value)
    backing.put(key, value, onlyIfAbsent = true)
  }

  override def remove(key: Any, value: Any): Boolean = {
    val k = requireKey(key)
    value != null && inBounds(k) && backing.remove(k, value) != null
  }

  override def replace(key: K, oldValue: V, newValue: V): Boolean = {
    checkKeyBounds(key)
    requireValue(oldValue)
    requireValue(newValue)
    backing.replace(key, oldValue, newValue)
  }

  override def replace(key: K, value: V): V = {
    checkKeyBounds(key)
    requireValue(value)
    backing.replace(key, value)
  }

  override def computeIfAbsent(
      key: K,
      mappingFunction: Function[_ >: K, _ <: V]
  ): V = {
    checkKeyBounds(key)
    Objects.requireNonNull(mappingFunction)
    var current = backing.get(key)
    if (current != null) current
    else {
      val newValue = mappingFunction.apply(key)
      if (newValue == null) null.asInstanceOf[V]
      else {
        current = backing.put(key, newValue, onlyIfAbsent = true)
        if (current == null) newValue else current
      }
    }
  }

  override def computeIfPresent(
      key: K,
      remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]
  ): V = {
    checkKeyBounds(key)
    Objects.requireNonNull(remappingFunction)
    while (true) {
      val oldValue = backing.get(key)
      if (oldValue == null) return null.asInstanceOf[V]
      val newValue = remappingFunction.apply(key, oldValue)
      if (newValue == null) {
        if (backing.remove(key, oldValue) != null) return null.asInstanceOf[V]
      } else if (backing.replace(key, oldValue, newValue)) return newValue
    }
    null.asInstanceOf[V]
  }

  override def compute(
      key: K,
      remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]
  ): V = {
    checkKeyBounds(key)
    Objects.requireNonNull(remappingFunction)
    while (true) {
      val oldValue = backing.get(key)
      val newValue = remappingFunction.apply(key, oldValue)
      if (oldValue == null) {
        if (newValue == null) return null.asInstanceOf[V]
        if (backing.put(key, newValue, onlyIfAbsent = true) == null)
          return newValue
      } else if (newValue == null) {
        if (backing.remove(key, oldValue) != null) return null.asInstanceOf[V]
      } else if (backing.replace(key, oldValue, newValue)) return newValue
    }
    null.asInstanceOf[V]
  }

  override def merge(
      key: K,
      value: V,
      remappingFunction: BiFunction[_ >: V, _ >: V, _ <: V]
  ): V = {
    checkKeyBounds(key)
    requireValue(value)
    Objects.requireNonNull(remappingFunction)
    while (true) {
      val oldValue = backing.get(key)
      if (oldValue == null) {
        if (backing.put(key, value, onlyIfAbsent = true) == null) return value
      } else {
        val newValue = remappingFunction.apply(oldValue, value)
        if (newValue == null) {
          if (backing.remove(key, oldValue) != null) return null.asInstanceOf[V]
        } else if (backing.replace(key, oldValue, newValue)) return newValue
      }
    }
    null.asInstanceOf[V]
  }

  override def replaceAll(
      function: BiFunction[_ >: K, _ >: V, _ <: V]
  ): Unit = {
    Objects.requireNonNull(function)
    val entries = snapshotEntries()
    val it = entries.iterator()
    while (it.hasNext()) {
      val e = it.next()
      val newValue = function.apply(e.getKey(), e.getValue())
      if (newValue == null) throw new NullPointerException
      backing.replace(e.getKey(), e.getValue(), newValue)
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
    if (!descending) backing.comparator
    else
      Collections.reverseOrder(backing.comparator.asInstanceOf[Comparator[K]])

  override def firstKey(): K = {
    val n = firstNodeInView()
    if (n == null) throw new NoSuchElementException
    n.key
  }

  override def lastKey(): K = {
    val n = lastNodeInView()
    if (n == null) throw new NoSuchElementException
    n.key
  }

  override def firstEntry(): Map.Entry[K, V] =
    immutableEntry(firstNodeInView())

  override def lastEntry(): Map.Entry[K, V] =
    immutableEntry(lastNodeInView())

  override def pollFirstEntry(): Map.Entry[K, V] = {
    while (true) {
      val n = firstNodeInView()
      if (n == null) return null
      val value = n.value
      if (value != null && backing.remove(n.key, value) != null)
        return immutableEntry(n.key, value)
    }
    null
  }

  override def pollLastEntry(): Map.Entry[K, V] = {
    while (true) {
      val n = lastNodeInView()
      if (n == null) return null
      val value = n.value
      if (value != null && backing.remove(n.key, value) != null)
        return immutableEntry(n.key, value)
    }
    null
  }

  override def lowerEntry(key: K): Map.Entry[K, V] = {
    requireKey(key)
    immutableEntry(lowerNodeInView(key))
  }

  override def lowerKey(key: K): K = {
    val e = lowerEntry(key)
    if (e == null) null.asInstanceOf[K] else e.getKey()
  }

  override def floorEntry(key: K): Map.Entry[K, V] = {
    requireKey(key)
    immutableEntry(floorNodeInView(key))
  }

  override def floorKey(key: K): K = {
    val e = floorEntry(key)
    if (e == null) null.asInstanceOf[K] else e.getKey()
  }

  override def ceilingEntry(key: K): Map.Entry[K, V] = {
    requireKey(key)
    immutableEntry(ceilingNodeInView(key))
  }

  override def ceilingKey(key: K): K = {
    val e = ceilingEntry(key)
    if (e == null) null.asInstanceOf[K] else e.getKey()
  }

  override def higherEntry(key: K): Map.Entry[K, V] = {
    requireKey(key)
    immutableEntry(higherNodeInView(key))
  }

  override def higherKey(key: K): K = {
    val e = higherEntry(key)
    if (e == null) null.asInstanceOf[K] else e.getKey()
  }

  override def subMap(
      fromKey: K,
      fromInclusive: Boolean,
      toKey: K,
      toInclusive: Boolean
  ): ConcurrentNavigableMap[K, V] = {
    requireKey(fromKey)
    requireKey(toKey)
    newSubMap(
      fromKey,
      fromSet0 = true,
      fromInclusive,
      toKey,
      toSet0 = true,
      toInclusive
    )
  }

  override def headMap(
      toKey: K,
      inclusive: Boolean
  ): ConcurrentNavigableMap[K, V] = {
    requireKey(toKey)
    newSubMap(
      null.asInstanceOf[K],
      fromSet0 = false,
      fromInclusive0 = false,
      toKey,
      toSet0 = true,
      inclusive
    )
  }

  override def tailMap(
      fromKey: K,
      inclusive: Boolean
  ): ConcurrentNavigableMap[K, V] = {
    requireKey(fromKey)
    newSubMap(
      fromKey,
      fromSet0 = true,
      inclusive,
      null.asInstanceOf[K],
      toSet0 = false,
      toInclusive0 = false
    )
  }

  override def subMap(fromKey: K, toKey: K): ConcurrentNavigableMap[K, V] =
    subMap(fromKey, true, toKey, false)

  override def headMap(toKey: K): ConcurrentNavigableMap[K, V] =
    headMap(toKey, false)

  override def tailMap(fromKey: K): ConcurrentNavigableMap[K, V] =
    tailMap(fromKey, true)

  override def descendingMap(): ConcurrentNavigableMap[K, V] =
    new ConcurrentSkipListMap[K, V](
      backing,
      lo,
      loInclusive,
      loSet,
      hi,
      hiInclusive,
      hiSet,
      !descending
    )

  override def clone(): ConcurrentSkipListMap[K, V] = {
    val cloned = new ConcurrentSkipListMap[K, V](backing.comparator)
    val entries = snapshotEntries()
    val it = entries.iterator()
    while (it.hasNext()) {
      val e = it.next()
      cloned.put(e.getKey(), e.getValue())
    }
    cloned
  }
}

private object ConcurrentSkipListMap {
  private final val MaxIndexLevel = 32

  private[concurrent] final class Node[K, V](
      val key: K,
      @volatile private[concurrent] var rawValue: AnyRef,
      @volatile private[concurrent] var nextNode: Node[K, V]
  ) {
    @alwaysinline
    private def valueRef: AtomicRef[AnyRef] =
      new AtomicRef[AnyRef](
        fromRawPtr(Intrinsics.classFieldRawPtr(this, "rawValue"))
      )

    @alwaysinline
    private def nextRef: AtomicRef[Node[K, V]] =
      new AtomicRef[Node[K, V]](
        fromRawPtr(Intrinsics.classFieldRawPtr(this, "nextNode"))
      )

    @alwaysinline
    def valueAny: AnyRef = valueRef.load()

    @alwaysinline
    def value: V = valueAny.asInstanceOf[V]

    @alwaysinline
    def next: Node[K, V] = nextRef.load()

    @alwaysinline
    def casValue(expect: AnyRef, update: AnyRef): Boolean =
      valueRef.compareExchangeStrong(expect, update)

    @alwaysinline
    def casNext(expect: Node[K, V], update: Node[K, V]): Boolean =
      nextRef.compareExchangeStrong(expect, update)
  }

  private[concurrent] final class Index[K, V](
      val node: Node[K, V],
      val down: Index[K, V],
      @volatile private[concurrent] var rightNode: Index[K, V]
  ) {
    @alwaysinline
    private def rightRef: AtomicRef[Index[K, V]] =
      new AtomicRef[Index[K, V]](
        fromRawPtr(Intrinsics.classFieldRawPtr(this, "rightNode"))
      )

    @alwaysinline
    def right: Index[K, V] = rightRef.load()

    @alwaysinline
    def setRight(right: Index[K, V]): Unit = rightRef.store(right)

    @alwaysinline
    def casRight(expect: Index[K, V], update: Index[K, V]): Boolean =
      rightRef.compareExchangeStrong(expect, update)
  }

  private[concurrent] final class Backing[K, V](
      val comparator: Comparator[_ >: K]
  ) {
    private val baseHead = new Node[K, V](null.asInstanceOf[K], null, null)
    @volatile private[concurrent] var headIndex =
      new Index[K, V](baseHead, null, null)

    @alwaysinline
    private def headRef: AtomicRef[Index[K, V]] =
      new AtomicRef[Index[K, V]](
        fromRawPtr(Intrinsics.classFieldRawPtr(this, "headIndex"))
      )

    @alwaysinline
    private def head: Index[K, V] = headRef.load()

    @alwaysinline
    private def casHead(expect: Index[K, V], update: Index[K, V]): Boolean =
      headRef.compareExchangeStrong(expect, update)

    def compareAny(a: Any, b: Any): Int =
      compare(a.asInstanceOf[K], b.asInstanceOf[K])

    def compare(a: K, b: K): Int =
      if (comparator != null) comparator.compare(a, b)
      else a.asInstanceOf[Comparable[Any]].compareTo(b)

    def get(key: K): V = {
      compare(key, key)
      val n = findNode(key)
      if (n == null) null.asInstanceOf[V] else n.value
    }

    def put(key: K, value: V, onlyIfAbsent: Boolean): V = {
      compare(key, key)
      val newValue = value.asInstanceOf[AnyRef]
      while (true) {
        val b = findPredecessor(key)
        var n = b.next
        var pred = b
        while (n != null) {
          val v = n.valueAny
          if (v == null) {
            pred.casNext(n, n.next)
            n = pred.next
          } else {
            val c = compare(n.key, key)
            if (c < 0) {
              pred = n
              n = n.next
            } else if (c == 0) {
              if (onlyIfAbsent) return v.asInstanceOf[V]
              if (n.casValue(v, newValue)) return v.asInstanceOf[V]
              n = pred.next
            } else {
              val z = new Node[K, V](key, newValue, n)
              if (pred.casNext(n, z)) {
                addIndices(z)
                return null.asInstanceOf[V]
              }
              n = pred.next
            }
          }
        }
        val z = new Node[K, V](key, newValue, null)
        if (pred.casNext(null, z)) {
          addIndices(z)
          return null.asInstanceOf[V]
        }
      }
      null.asInstanceOf[V]
    }

    def remove(key: K, expectedValue: Any): V = {
      compare(key, key)
      while (true) {
        val b = findPredecessor(key)
        var n = b.next
        var pred = b
        while (n != null) {
          val v = n.valueAny
          if (v == null) {
            pred.casNext(n, n.next)
            n = pred.next
          } else {
            val c = compare(n.key, key)
            if (c < 0) {
              pred = n
              n = n.next
            } else if (c > 0) return null.asInstanceOf[V]
            else {
              if (expectedValue != null && !Objects.equals(v, expectedValue))
                return null.asInstanceOf[V]
              if (n.casValue(v, null)) {
                pred.casNext(n, n.next)
                findPredecessor(key)
                return v.asInstanceOf[V]
              }
              n = pred.next
            }
          }
        }
        return null.asInstanceOf[V]
      }
      null.asInstanceOf[V]
    }

    def replace(key: K, oldValue: V, newValue: V): Boolean = {
      compare(key, key)
      val newRef = newValue.asInstanceOf[AnyRef]
      while (true) {
        val n = findNode(key)
        if (n == null) return false
        val v = n.valueAny
        if (v == null) ()
        else if (!Objects.equals(v, oldValue)) return false
        else if (n.casValue(v, newRef)) return true
      }
      false
    }

    def replace(key: K, value: V): V = {
      compare(key, key)
      val newRef = value.asInstanceOf[AnyRef]
      while (true) {
        val n = findNode(key)
        if (n == null) return null.asInstanceOf[V]
        val v = n.valueAny
        if (v == null) ()
        else if (n.casValue(v, newRef)) return v.asInstanceOf[V]
      }
      null.asInstanceOf[V]
    }

    def firstNode(): Node[K, V] =
      liveNext(baseHead)

    def lastNode(): Node[K, V] = {
      var last: Node[K, V] = null
      var n = firstNode()
      while (n != null) {
        last = n
        n = findGreaterThan(n.key)
      }
      last
    }

    def findGreaterOrEqual(key: K): Node[K, V] = {
      compare(key, key)
      val pred = findPredecessor(key)
      var n = liveNext(pred)
      while (n != null && compare(n.key, key) < 0) n = liveNext(n)
      n
    }

    def findGreaterThan(key: K): Node[K, V] = {
      var n = findGreaterOrEqual(key)
      if (n != null && compare(n.key, key) == 0) n = liveNext(n)
      n
    }

    def findLessThan(key: K): Node[K, V] = {
      compare(key, key)
      val pred = findPredecessor(key)
      if (pred eq baseHead) null else pred
    }

    def findLessOrEqual(key: K): Node[K, V] = {
      compare(key, key)
      val pred = findPredecessor(key)
      val n = liveNext(pred)
      if (n != null && compare(n.key, key) == 0) n
      else if (pred eq baseHead) null
      else pred
    }

    private def findNode(key: K): Node[K, V] = {
      var n = findGreaterOrEqual(key)
      if (n != null && compare(n.key, key) == 0 && n.valueAny != null) n
      else null
    }

    private def liveNext(pred: Node[K, V]): Node[K, V] = {
      var n = pred.next
      while (n != null) {
        if (n.valueAny != null) return n
        pred.casNext(n, n.next)
        n = pred.next
      }
      null
    }

    private def findPredecessor(key: K): Node[K, V] = {
      var q = head
      while (true) {
        var r = q.right
        while (r != null) {
          val n = r.node
          val v = n.valueAny
          if (v == null) {
            q.casRight(r, r.right)
            r = q.right
          } else if (compare(n.key, key) < 0) {
            q = r
            r = q.right
          } else r = null
        }
        if (q.down == null) {
          var b = q.node
          if ((b ne baseHead) && b.valueAny == null)
            return findPredecessor(key)
          var n = b.next
          while (n != null) {
            val v = n.valueAny
            if (v == null) {
              b.casNext(n, n.next)
              n = b.next
            } else if (compare(n.key, key) < 0) {
              b = n
              n = n.next
            } else return b
          }
          return b
        }
        q = q.down
      }
      baseHead
    }

    private def randomLevel(): Int = {
      var x = ThreadLocalRandom.current().nextInt()
      var level = 0
      while ((x & 1) != 0 && level < MaxIndexLevel) {
        level += 1
        x = x >>> 1
      }
      level
    }

    private def headHeight(): Int = {
      var h = head
      var height = 1
      while (h.down != null) {
        height += 1
        h = h.down
      }
      height
    }

    private def ensureHeadHeight(level: Int): Unit = {
      var done = false
      while (!done) {
        val h = head
        val height = headHeight()
        if (height >= level) done = true
        else {
          var newHead = h
          var i = height
          while (i < level) {
            newHead = new Index[K, V](baseHead, newHead, null)
            i += 1
          }
          done = casHead(h, newHead)
        }
      }
    }

    private def predecessorIndex(key: K, targetLevel: Int): Index[K, V] = {
      var q = head
      var level = headHeight()
      while (true) {
        var r = q.right
        while (r != null) {
          val n = r.node
          val v = n.valueAny
          if (v == null) {
            q.casRight(r, r.right)
            r = q.right
          } else if (compare(n.key, key) < 0) {
            q = r
            r = q.right
          } else r = null
        }
        if (level == targetLevel || q.down == null) return q
        q = q.down
        level -= 1
      }
      q
    }

    private def addIndices(node: Node[K, V]): Unit = {
      val level = randomLevel()
      if (level == 0) return
      ensureHeadHeight(level)

      val indices = new Array[Index[K, V]](level)
      var i = 0
      var down: Index[K, V] = null
      while (i < level) {
        val idx = new Index[K, V](node, down, null)
        indices(i) = idx
        down = idx
        i += 1
      }

      i = 1
      while (i <= level && node.valueAny != null) {
        var inserted = false
        val idx = indices(i - 1)
        while (!inserted && node.valueAny != null) {
          val pred = predecessorIndex(node.key, i)
          val right = pred.right
          idx.setRight(right)
          inserted = pred.casRight(right, idx)
        }
        i += 1
      }
    }
  }

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
