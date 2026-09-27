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

import java.io.{ObjectInputStream, ObjectOutputStream, Serializable}
import java.lang.invoke.VarHandle
import java.util._
import java.util.concurrent.atomic.LongAdder
import java.util.function._

import scala.scalanative.annotation.{alwaysinline, safePublish}
import scala.scalanative.libc.stdatomic._
import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.runtime.fromRawPtr

// scalafmt: { maxColumn = 120}

object ConcurrentSkipListMap {
  private[concurrent] final class Node[K, V](
      @safePublish private[concurrent] val key: K,
      private[concurrent] var `val`: V,
      private[concurrent] var next: Node[K, V]
  ) {
    @alwaysinline private[concurrent] def NEXT: AtomicRef[Node[K, V]] =
      fromRawPtr[Node[K, V]](classFieldRawPtr(this, "next")).atomic
    @alwaysinline private[concurrent] def VAL: AtomicRef[AnyRef] =
      fromRawPtr[AnyRef](classFieldRawPtr(this, "val")).atomic
  }

  private[concurrent] final class Index[K, V](
      @safePublish private[concurrent] val node: Node[K, V],
      @safePublish private[concurrent] val down: Index[K, V],
      private[concurrent] var right: Index[K, V]
  ) {
    @alwaysinline private[concurrent] def RIGHT: AtomicRef[Index[K, V]] =
      fromRawPtr[Index[K, V]](classFieldRawPtr(this, "right")).atomic
  }

  private[concurrent] def cpr(c: Comparator[_], x: Any, y: Any): Int = {
    if (c != null) c.asInstanceOf[Comparator[Any]].compare(x, y)
    else x.asInstanceOf[Comparable[Any]].compareTo(y)
  }

  private[concurrent] def unlinkNode[K, V](b: Node[K, V], n: Node[K, V]): Unit = {
    if (b != null && n != null) {
      var f: Node[K, V] = null.asInstanceOf[Node[K, V]]
      var p: Node[K, V] = null.asInstanceOf[Node[K, V]]
      var breakInner = false
      while (!breakInner) {
        if ({ f = n.next; f } != null && f.key == null) {
          p = f.next
          // already marked
          breakInner = true
        } else if (n.NEXT.compareExchangeStrong(f, new Node[K, V](null.asInstanceOf[K], null.asInstanceOf[V], f))) {
          p = f
          // add marker
          breakInner = true
        }
      }
      b.NEXT.compareExchangeStrong(n, p)
    }
  }

  private[concurrent] def addIndices[K, V](
      q0: Index[K, V],
      skips0: Int,
      x: Index[K, V],
      cmp: Comparator[_]
  ): Boolean = {
    var q = q0; var skips = skips0

    var z: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var key: K = null.asInstanceOf[K]
    if (x != null && { z = x.node; z } != null && { key = z.key; key } != null &&
        q != null) { // hoist checks
      var retrying: Boolean = false
      var breakInner = false
      while (!breakInner) {
        // find splice point
        var r: Index[K, V] = null.asInstanceOf[Index[K, V]]
        var d: Index[K, V] = null.asInstanceOf[Index[K, V]]
        var c: Int = 0
        if ({ r = q.right; r } != null) {
          var p: Node[K, V] = null.asInstanceOf[Node[K, V]]
          var k: K = null.asInstanceOf[K]
          if ({ p = r.node; p } == null || { k = p.key; k } == null ||
              p.`val` == null) {
            q.RIGHT.compareExchangeStrong(r, r.right)
            c = 0
          } else if ({ c = cpr(cmp, key, k); c } > 0)
            q = r
          else if (c == 0)
            breakInner = true // stale
        } else
          c = -1

        if (c < 0) {
          if ({ d = q.down; d } != null && skips > 0) {
            {
              skips -= 1
              skips
            }
            q = d
          } else if (d != null && !retrying &&
              !addIndices(d, 0, x.down, cmp))
            breakInner = true
          else {
            x.right = r
            if (q.RIGHT.compareExchangeStrong(r, x))
              return true
            else
              retrying = true
            // re-find splice point
          }
        }
      }
    }
    false
  }

  private[concurrent] final val EQ: Int = 1

  private[concurrent] final val LT: Int = 2

  private[concurrent] final val GT: Int = 0

  private[concurrent] def toList[E](c: Collection[E]): List[E] = {
    // Using size() here would be a pessimization.
    var list: ArrayList[E] = new ArrayList[E]()
    val _it_e = c.iterator()
    while (_it_e.hasNext()) {
      var e: E = _it_e.next()
      list.add(e)
    }
    list
  }

  private[concurrent] final class KeySet[K, V](private[concurrent] final val m: ConcurrentNavigableMap[K, V])
      extends AbstractSet[K]
      with NavigableSet[K] {
    override def size(): Int = {
      m.size()
    }

    override def isEmpty(): Boolean = {
      m.isEmpty()
    }

    override def contains(o: Any): Boolean = {
      m.containsKey(o)
    }

    override def remove(o: Any): Boolean = {
      m.remove(o) != null
    }

    override def clear(): Unit = {
      m.clear()
    }

    def lower(e: K): K = {
      m.lowerKey(e)
    }

    def floor(e: K): K = {
      m.floorKey(e)
    }

    def ceiling(e: K): K = {
      m.ceilingKey(e)
    }

    def higher(e: K): K = {
      m.higherKey(e)
    }

    override def comparator(): Comparator[_ >: K] = {
      m.comparator()
    }

    def first(): K = {
      m.firstKey()
    }

    def last(): K = {
      m.lastKey()
    }

    def pollFirst(): K = {
      var e: Map.Entry[K, V] = m.pollFirstEntry()
      if (e == null) null.asInstanceOf[K] else e.getKey()
    }

    def pollLast(): K = {
      var e: Map.Entry[K, V] = m.pollLastEntry()
      if (e == null) null.asInstanceOf[K] else e.getKey()
    }

    override def iterator(): Iterator[K] = {
      if (m.isInstanceOf[ConcurrentSkipListMap[_, _]]) {
        val _o = m.asInstanceOf[ConcurrentSkipListMap[K, V]]; new _o.KeyIterator()
      } else { val _o = m.asInstanceOf[SubMap[K, V]]; new _o.SubMapKeyIterator() }
    }

    override def equals(o: Any): Boolean = {
      if (o.asInstanceOf[AnyRef] eq this)
        return true
      if (!o.isInstanceOf[Set[_]])
        return false
      var c: Collection[_] = o.asInstanceOf[Collection[_]]
      try {
        containsAll(c) && c.containsAll(this)
      } catch {
        case _: ClassCastException | _: NullPointerException => false
      }
    }

    override def toArray(): Array[AnyRef] = toList(this).toArray().asInstanceOf[Array[AnyRef]]

    override def toArray[T <: AnyRef](a: Array[T]): Array[T] = toList(this).toArray(a)

    def descendingIterator(): Iterator[K] = {
      descendingSet().iterator()
    }

    def subSet(fromElement: K, fromInclusive: Boolean, toElement: K, toInclusive: Boolean): NavigableSet[K] = {
      new KeySet(m.subMap(fromElement, fromInclusive, toElement, toInclusive))
    }

    def headSet(toElement: K, inclusive: Boolean): NavigableSet[K] = {
      new KeySet(m.headMap(toElement, inclusive))
    }

    def tailSet(fromElement: K, inclusive: Boolean): NavigableSet[K] = {
      new KeySet(m.tailMap(fromElement, inclusive))
    }

    def subSet(fromElement: K, toElement: K): NavigableSet[K] = {
      subSet(fromElement, true, toElement, false)
    }

    def headSet(toElement: K): NavigableSet[K] = {
      headSet(toElement, false)
    }

    def tailSet(fromElement: K): NavigableSet[K] = {
      tailSet(fromElement, true)
    }

    def descendingSet(): NavigableSet[K] = {
      new KeySet(m.descendingMap())
    }

    override def spliterator(): Spliterator[K] = {
      if (m.isInstanceOf[ConcurrentSkipListMap[_, _]])
        (m.asInstanceOf[ConcurrentSkipListMap[K, V]]).keySpliterator()
      else { val _o = m.asInstanceOf[SubMap[K, V]]; new _o.SubMapKeyIterator() }
    }
  }

  private[concurrent] final class Values[K, V](private[concurrent] final val m: ConcurrentNavigableMap[K, V])
      extends AbstractCollection[V] {
    override def iterator(): Iterator[V] = {
      if (m.isInstanceOf[ConcurrentSkipListMap[_, _]]) {
        val _o = m.asInstanceOf[ConcurrentSkipListMap[K, V]]; new _o.ValueIterator()
      } else { val _o = m.asInstanceOf[SubMap[K, V]]; new _o.SubMapValueIterator() }
    }

    override def size(): Int = {
      m.size()
    }

    override def isEmpty(): Boolean = {
      m.isEmpty()
    }

    override def contains(o: Any): Boolean = {
      m.containsValue(o)
    }

    override def clear(): Unit = {
      m.clear()
    }

    override def toArray(): Array[AnyRef] = toList(this).toArray().asInstanceOf[Array[AnyRef]]

    override def toArray[T <: AnyRef](a: Array[T]): Array[T] = toList(this).toArray(a)

    override def spliterator(): Spliterator[V] = {
      if (m.isInstanceOf[ConcurrentSkipListMap[_, _]])
        (m.asInstanceOf[ConcurrentSkipListMap[K, V]]).valueSpliterator()
      else { val _o = m.asInstanceOf[SubMap[K, V]]; new _o.SubMapValueIterator() }
    }

    override def removeIf(filter: Predicate[_ >: V]): Boolean = {
      if (filter == null) throw new NullPointerException();
      if (m.isInstanceOf[ConcurrentSkipListMap[_, _]])
        return (m.asInstanceOf[ConcurrentSkipListMap[K, V]]).removeValueIf(filter)
      // else use iterator
      var it: Iterator[Map.Entry[K, V]] = { val _o = m.asInstanceOf[SubMap[K, V]]; new _o.SubMapEntryIterator() }
      var removed: Boolean = false
      while (it.hasNext()) {
        var e: Map.Entry[K, V] = it.next()
        var v: V = e.getValue()
        if (filter.test(v) && m.remove(e.getKey(), v))
          removed = true
      }
      removed
    }
  }

  private[concurrent] final class EntrySet[K, V](private[concurrent] final val m: ConcurrentNavigableMap[K, V])
      extends AbstractSet[Map.Entry[K, V]] {
    override def iterator(): Iterator[Map.Entry[K, V]] = {
      if (m.isInstanceOf[ConcurrentSkipListMap[_, _]]) {
        val _o = m.asInstanceOf[ConcurrentSkipListMap[K, V]]; new _o.EntryIterator()
      } else { val _o = m.asInstanceOf[SubMap[K, V]]; new _o.SubMapEntryIterator() }
    }

    override def contains(o: Any): Boolean = {
      if (!o.isInstanceOf[Map.Entry[_, _]])
        return false
      var e: Map.Entry[_, _] = o.asInstanceOf[Map.Entry[_, _]]
      var v: V = m.get(e.getKey())
      v != null && v.asInstanceOf[AnyRef].equals(e.getValue())
    }

    override def remove(o: Any): Boolean = {
      if (!o.isInstanceOf[Map.Entry[_, _]])
        return false
      var e: Map.Entry[_, _] = o.asInstanceOf[Map.Entry[_, _]]
      m.remove(e.getKey(), e.getValue())
    }

    override def isEmpty(): Boolean = {
      m.isEmpty()
    }

    override def size(): Int = {
      m.size()
    }

    override def clear(): Unit = {
      m.clear()
    }

    override def equals(o: Any): Boolean = {
      if (o.asInstanceOf[AnyRef] eq this)
        return true
      if (!o.isInstanceOf[Set[_]])
        return false
      var c: Collection[_] = o.asInstanceOf[Collection[_]]
      try {
        containsAll(c) && c.containsAll(this)
      } catch {
        case _: ClassCastException | _: NullPointerException => false
      }
    }

    override def toArray(): Array[AnyRef] = toList(this).toArray().asInstanceOf[Array[AnyRef]]

    override def toArray[T <: AnyRef](a: Array[T]): Array[T] = toList(this).toArray(a)

    override def spliterator(): Spliterator[Map.Entry[K, V]] = {
      if (m.isInstanceOf[ConcurrentSkipListMap[_, _]])
        (m.asInstanceOf[ConcurrentSkipListMap[K, V]]).entrySpliterator()
      else { val _o = m.asInstanceOf[SubMap[K, V]]; new _o.SubMapEntryIterator() }
    }

    override def removeIf(filter: Predicate[_ >: Map.Entry[K, V]]): Boolean = {
      if (filter == null) throw new NullPointerException();
      if (m.isInstanceOf[ConcurrentSkipListMap[_, _]])
        return (m.asInstanceOf[ConcurrentSkipListMap[K, V]]).removeEntryIf(filter)
      // else use iterator
      var it: Iterator[Map.Entry[K, V]] = { val _o = m.asInstanceOf[SubMap[K, V]]; new _o.SubMapEntryIterator() }
      var removed: Boolean = false
      while (it.hasNext()) {
        var e: Map.Entry[K, V] = it.next()
        if (filter.test(e) && m.remove(e.getKey(), e.getValue()))
          removed = true
      }
      removed
    }
  }

  @SerialVersionUID(-7647078645895051609L)
  private[concurrent] final class SubMap[K, V](
      private[concurrent] final val m: ConcurrentSkipListMap[K, V],
      private[concurrent] final val lo: K,
      private[concurrent] final val loInclusive: Boolean,
      private[concurrent] final val hi: K,
      private[concurrent] final val hiInclusive: Boolean,
      private[concurrent] final val isDescending: Boolean
  ) extends AbstractMap[K, V]
      with ConcurrentNavigableMap[K, V]
      with Serializable {
    // Lazily initialized view holders
    @transient private var keySetView: KeySet[K, V] = null.asInstanceOf[KeySet[K, V]]
    @transient private var valuesView: Values[K, V] = null.asInstanceOf[Values[K, V]]
    @transient private var entrySetView: EntrySet[K, V] = null.asInstanceOf[EntrySet[K, V]]

    // validate range (from Java ctor)
    {
      val cmp = m._comparator
      if (lo != null && hi != null && cpr(cmp, lo, hi) > 0)
        throw new IllegalArgumentException("inconsistent range")
    }

    /* ----------------  Utilities -------------- */

    private[concurrent] def tooLow(key: Any, cmp: Comparator[_ >: K]): Boolean = {
      var c: Int = 0
      lo != null && ({ c = cpr(cmp, key, lo); c } < 0 ||
      (c == 0 && !loInclusive))
    }

    private[concurrent] def tooHigh(key: Any, cmp: Comparator[_ >: K]): Boolean = {
      var c: Int = 0
      hi != null && ({ c = cpr(cmp, key, hi); c } > 0 ||
      (c == 0 && !hiInclusive))
    }

    private[concurrent] def inBounds(key: Any, cmp: Comparator[_ >: K]): Boolean = {
      !tooLow(key, cmp) && !tooHigh(key, cmp)
    }

    private[concurrent] def checkKeyBounds(key: K, cmp: Comparator[_ >: K]): Unit = {
      if (key == null)
        throw new NullPointerException()
      if (!inBounds(key, cmp))
        throw new IllegalArgumentException("key out of range")
    }

    private[concurrent] def isBeforeEnd(n: Node[K, V], cmp: Comparator[_ >: K]): Boolean = {
      if (n == null)
        return false
      if (hi == null)
        return true
      var k: K = n.key
      if (k == null) // pass by markers and headers
        return true
      var c: Int = cpr(cmp, k, hi)
      c < 0 || (c == 0 && hiInclusive)
    }

    private[concurrent] def loNode(cmp: Comparator[_ >: K]): Node[K, V] = {
      if (lo == null)
        m.findFirst()
      else if (loInclusive)
        m.findNear(lo, GT | EQ, cmp)
      else
        m.findNear(lo, GT, cmp)
    }

    private[concurrent] def hiNode(cmp: Comparator[_ >: K]): Node[K, V] = {
      if (hi == null)
        m.findLast()
      else if (hiInclusive)
        m.findNear(hi, LT | EQ, cmp)
      else
        m.findNear(hi, LT, cmp)
    }

    private[concurrent] def lowestKey(): K = {
      var cmp: Comparator[_ >: K] = m._comparator
      var n: Node[K, V] = loNode(cmp)
      if (isBeforeEnd(n, cmp))
        n.key
      else
        throw new NoSuchElementException()
    }

    private[concurrent] def highestKey(): K = {
      var cmp: Comparator[_ >: K] = m._comparator
      var n: Node[K, V] = hiNode(cmp)
      if (n != null) {
        var last: K = n.key
        if (inBounds(last, cmp))
          return last
      }
      throw new NoSuchElementException()
    }

    private[concurrent] def lowestEntry(): Map.Entry[K, V] = {
      var cmp: Comparator[_ >: K] = m._comparator
      while (true) {
        var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
        var v: V = null.asInstanceOf[V]
        if ({ n = loNode(cmp); n } == null || !isBeforeEnd(n, cmp))
          return null.asInstanceOf[Map.Entry[K, V]]
        else if ({ v = n.`val`; v } != null)
          return new AbstractMap.SimpleImmutableEntry[K, V](n.key, v)
      }
      null.asInstanceOf[Map.Entry[K, V]] // unreachable
    }

    private[concurrent] def highestEntry(): Map.Entry[K, V] = {
      var cmp: Comparator[_ >: K] = m._comparator
      while (true) {
        var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
        var v: V = null.asInstanceOf[V]
        if ({ n = hiNode(cmp); n } == null || !inBounds(n.key, cmp))
          return null.asInstanceOf[Map.Entry[K, V]]
        else if ({ v = n.`val`; v } != null)
          return new AbstractMap.SimpleImmutableEntry[K, V](n.key, v)
      }
      null.asInstanceOf[Map.Entry[K, V]] // unreachable
    }

    private[concurrent] def removeLowest(): Map.Entry[K, V] = {
      var cmp: Comparator[_ >: K] = m._comparator
      while (true) {
        var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
        var k: K = null.asInstanceOf[K]
        var v: V = null.asInstanceOf[V]
        if ({ n = loNode(cmp); n } == null)
          return null.asInstanceOf[Map.Entry[K, V]]
        else if (!inBounds({ k = n.key; k }, cmp))
          return null.asInstanceOf[Map.Entry[K, V]]
        else if ({ v = m.doRemove(k, null.asInstanceOf[V]); v } != null)
          return new AbstractMap.SimpleImmutableEntry[K, V](k, v)
      }
      null.asInstanceOf[Map.Entry[K, V]] // unreachable
    }

    private[concurrent] def removeHighest(): Map.Entry[K, V] = {
      var cmp: Comparator[_ >: K] = m._comparator
      while (true) {
        var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
        var k: K = null.asInstanceOf[K]
        var v: V = null.asInstanceOf[V]
        if ({ n = hiNode(cmp); n } == null)
          return null.asInstanceOf[Map.Entry[K, V]]
        else if (!inBounds({ k = n.key; k }, cmp))
          return null.asInstanceOf[Map.Entry[K, V]]
        else if ({ v = m.doRemove(k, null.asInstanceOf[V]); v } != null)
          return new AbstractMap.SimpleImmutableEntry[K, V](k, v)
      }
      null.asInstanceOf[Map.Entry[K, V]] // unreachable
    }

    private[concurrent] def getNearEntry(key: K, rel0: Int): Map.Entry[K, V] = {
      var rel = rel0
      var cmp: Comparator[_ >: K] = m._comparator
      if (isDescending) { // adjust relation for direction
        if ((rel & LT) == 0)
          rel = rel | LT
        else
          rel = rel & ~LT
      }
      if (tooLow(key, cmp))
        return if ((rel & LT) != 0) null.asInstanceOf[Map.Entry[K, V]] else lowestEntry()
      if (tooHigh(key, cmp))
        return if ((rel & LT) != 0) highestEntry() else null.asInstanceOf[Map.Entry[K, V]]
      var e: AbstractMap.SimpleImmutableEntry[K, V] =
        m.findNearEntry(key, rel, cmp)
      if (e == null || !inBounds(e.getKey(), cmp))
        null.asInstanceOf[Map.Entry[K, V]]
      else
        e
    }

    // Almost the same as getNearEntry, except for keys

    private[concurrent] def getNearKey(key: K, rel0: Int): K = {
      var rel = rel0
      var cmp: Comparator[_ >: K] = m._comparator
      if (isDescending) { // adjust relation for direction
        if ((rel & LT) == 0)
          rel = rel | LT
        else
          rel = rel & ~LT
      }
      if (tooLow(key, cmp)) {
        if ((rel & LT) == 0) {
          var n: Node[K, V] = loNode(cmp)
          if (isBeforeEnd(n, cmp))
            return n.key
        }
        return null.asInstanceOf[K]
      }
      if (tooHigh(key, cmp)) {
        if ((rel & LT) != 0) {
          var n: Node[K, V] = hiNode(cmp)
          if (n != null) {
            var last: K = n.key
            if (inBounds(last, cmp))
              return last
          }
        }
        return null.asInstanceOf[K]
      }
      while (true) {
        var n: Node[K, V] = m.findNear(key, rel, cmp)
        if (n == null || !inBounds(n.key, cmp))
          return null.asInstanceOf[K]
        if (n.`val` != null)
          return n.key
      }
      null.asInstanceOf[K] // unreachable
    }

    /* ----------------  Map API methods -------------- */

    override def containsKey(key: Any): Boolean = {
      if (key == null) throw new NullPointerException();
      inBounds(key, m._comparator) && m.containsKey(key)
    }

    override def get(key: Any): V = {
      if (key == null) throw new NullPointerException();
      if (!inBounds(key, m._comparator)) null.asInstanceOf[V] else m.get(key)
    }

    override def put(key: K, value: V): V = {
      checkKeyBounds(key, m._comparator)
      m.put(key, value)
    }

    override def remove(key: Any): V = {
      if (!inBounds(key, m._comparator)) null.asInstanceOf[V] else m.remove(key)
    }

    override def size(): Int = {
      var cmp: Comparator[_ >: K] = m._comparator
      var count: Long = 0
      var n: Node[K, V] = loNode(cmp)
      while (isBeforeEnd(n, cmp)) {
        if (n.`val` != null) {
          count += 1
          count
        }

        n = n.next
      }
      if (count >= java.lang.Integer.MAX_VALUE) java.lang.Integer.MAX_VALUE else count.toInt
    }

    override def isEmpty(): Boolean = {
      var cmp: Comparator[_ >: K] = m._comparator
      !isBeforeEnd(loNode(cmp), cmp)
    }

    override def containsValue(value: Any): Boolean = {
      if (value == null)
        throw new NullPointerException()
      var cmp: Comparator[_ >: K] = m._comparator
      var n: Node[K, V] = loNode(cmp)
      while (isBeforeEnd(n, cmp)) {
        var v: V = n.`val`
        if (v != null && value.asInstanceOf[AnyRef].equals(v.asInstanceOf[AnyRef]))
          return true

        n = n.next
      }
      false
    }

    override def clear(): Unit = {
      var cmp: Comparator[_ >: K] = m._comparator
      var n: Node[K, V] = loNode(cmp)
      while (isBeforeEnd(n, cmp)) {
        if (n.`val` != null)
          m.remove(n.key)
        n = n.next
      }
    }

    /* ----------------  ConcurrentMap API methods -------------- */

    override def putIfAbsent(key: K, value: V): V = {
      checkKeyBounds(key, m._comparator)
      m.putIfAbsent(key, value)
    }

    override def remove(key: Any, value: Any): Boolean = {
      inBounds(key, m._comparator) && m.remove(key, value)
    }

    override def replace(key: K, oldValue: V, newValue: V): Boolean = {
      checkKeyBounds(key, m._comparator)
      m.replace(key, oldValue, newValue)
    }

    override def replace(key: K, value: V): V = {
      checkKeyBounds(key, m._comparator)
      m.replace(key, value)
    }

    /* ----------------  SortedMap API methods -------------- */

    override def comparator(): Comparator[_ >: K] = {
      var cmp: Comparator[_ >: K] = m.comparator()
      if (isDescending)
        Collections.reverseOrder(cmp)
      else
        cmp
    }

    private[concurrent] def newSubMap(
        fromKey0: K,
        fromInclusive0: Boolean,
        toKey0: K,
        toInclusive0: Boolean
    ): SubMap[K, V] = {
      var fromKey = fromKey0
      var fromInclusive = fromInclusive0
      var toKey = toKey0
      var toInclusive = toInclusive0
      var cmp: Comparator[_ >: K] = m._comparator
      if (isDescending) { // flip senses
        val tk: K = fromKey
        fromKey = toKey
        toKey = tk
        val ti: Boolean = fromInclusive
        fromInclusive = toInclusive
        toInclusive = ti
      }
      if (lo != null) {
        if (fromKey == null) {
          fromKey = lo
          fromInclusive = loInclusive
        } else {
          var c: Int = cpr(cmp, fromKey, lo)
          if (c < 0 || (c == 0 && !loInclusive && fromInclusive))
            throw new IllegalArgumentException("key out of range")
        }
      }
      if (hi != null) {
        if (toKey == null) {
          toKey = hi
          toInclusive = hiInclusive
        } else {
          var c: Int = cpr(cmp, toKey, hi)
          if (c > 0 || (c == 0 && !hiInclusive && toInclusive))
            throw new IllegalArgumentException("key out of range")
        }
      }
      new SubMap[K, V](m, fromKey, fromInclusive, toKey, toInclusive, isDescending)
    }

    override def subMap(fromKey: K, fromInclusive: Boolean, toKey: K, toInclusive: Boolean): SubMap[K, V] = {
      if (fromKey == null || toKey == null)
        throw new NullPointerException()
      newSubMap(fromKey, fromInclusive, toKey, toInclusive)
    }

    override def headMap(toKey: K, inclusive: Boolean): SubMap[K, V] = {
      if (toKey == null)
        throw new NullPointerException()
      newSubMap(null.asInstanceOf[K], false, toKey, inclusive)
    }

    override def tailMap(fromKey: K, inclusive: Boolean): SubMap[K, V] = {
      if (fromKey == null)
        throw new NullPointerException()
      newSubMap(fromKey, inclusive, null.asInstanceOf[K], false)
    }

    override def subMap(fromKey: K, toKey: K): SubMap[K, V] = {
      subMap(fromKey, true, toKey, false)
    }

    override def headMap(toKey: K): SubMap[K, V] = {
      headMap(toKey, false)
    }

    override def tailMap(fromKey: K): SubMap[K, V] = {
      tailMap(fromKey, true)
    }

    override def descendingMap(): SubMap[K, V] = {
      new SubMap[K, V](m, lo, loInclusive, hi, hiInclusive, !isDescending)
    }

    /* ----------------  Relational methods -------------- */

    override def ceilingEntry(key: K): Map.Entry[K, V] = {
      getNearEntry(key, GT | EQ)
    }

    override def ceilingKey(key: K): K = {
      getNearKey(key, GT | EQ)
    }

    override def lowerEntry(key: K): Map.Entry[K, V] = {
      getNearEntry(key, LT)
    }

    override def lowerKey(key: K): K = {
      getNearKey(key, LT)
    }

    override def floorEntry(key: K): Map.Entry[K, V] = {
      getNearEntry(key, LT | EQ)
    }

    override def floorKey(key: K): K = {
      getNearKey(key, LT | EQ)
    }

    override def higherEntry(key: K): Map.Entry[K, V] = {
      getNearEntry(key, GT)
    }

    override def higherKey(key: K): K = {
      getNearKey(key, GT)
    }

    override def firstKey(): K = {
      if (isDescending) highestKey() else lowestKey()
    }

    override def lastKey(): K = {
      if (isDescending) lowestKey() else highestKey()
    }

    override def firstEntry(): Map.Entry[K, V] = {
      if (isDescending) highestEntry() else lowestEntry()
    }

    override def lastEntry(): Map.Entry[K, V] = {
      if (isDescending) lowestEntry() else highestEntry()
    }

    override def pollFirstEntry(): Map.Entry[K, V] = {
      if (isDescending) removeHighest() else removeLowest()
    }

    override def pollLastEntry(): Map.Entry[K, V] = {
      if (isDescending) removeLowest() else removeHighest()
    }

    /* ---------------- Submap Views -------------- */

    override def keySet(): NavigableSet[K] = {
      var ks: KeySet[K, V] = null.asInstanceOf[KeySet[K, V]]
      if ({ ks = keySetView; ks } != null) return ks;
      { keySetView = new KeySet(this); keySetView }
    }

    override def navigableKeySet(): NavigableSet[K] = {
      var ks: KeySet[K, V] = null.asInstanceOf[KeySet[K, V]]
      if ({ ks = keySetView; ks } != null) return ks;
      { keySetView = new KeySet(this); keySetView }
    }

    override def values(): Collection[V] = {
      var vs: Values[K, V] = null.asInstanceOf[Values[K, V]]
      if ({ vs = valuesView; vs } != null) return vs;
      { valuesView = new Values(this); valuesView }
    }

    override def entrySet(): Set[Map.Entry[K, V]] = {
      var es: EntrySet[K, V] = null.asInstanceOf[EntrySet[K, V]]
      if ({ es = entrySetView; es } != null) return es;
      { entrySetView = new EntrySet[K, V](this); entrySetView }
    }

    override def descendingKeySet(): NavigableSet[K] = {
      descendingMap().navigableKeySet()
    }

    private[concurrent] abstract class SubMapIter[T] extends Iterator[T] with Spliterator[T] {
      // Java: lastReturned, next, nextValue — next -> nextNode (conflicts with next())
      protected var lastReturned: Node[K, V] = _
      protected var nextNode: Node[K, V] = _
      protected var nextValue: V = _

      {
        VarHandle.acquireFence()
        val cmp = m._comparator
        var done = false
        while (!done) {
          nextNode = if (isDescending) hiNode(cmp) else loNode(cmp)
          if (nextNode == null) done = true
          else {
            val x = nextNode.`val`
            if (x != null) {
              if (!inBounds(nextNode.key, cmp)) nextNode = null
              else nextValue = x
              done = true
            }
          }
        }
      }

      final def hasNext(): Boolean = nextNode != null

      final def advance(): Unit = {
        if (nextNode == null) throw new NoSuchElementException()
        lastReturned = nextNode
        if (isDescending) descend()
        else ascend()
      }

      private def ascend(): Unit = {
        val cmp = m._comparator
        var done = false
        while (!done) {
          nextNode = nextNode.next
          if (nextNode == null) done = true
          else {
            val x = nextNode.`val`
            if (x != null) {
              if (tooHigh(nextNode.key, cmp)) nextNode = null
              else nextValue = x
              done = true
            }
          }
        }
      }

      private def descend(): Unit = {
        val cmp = m._comparator
        var done = false
        while (!done) {
          nextNode = m.findNear(lastReturned.key, LT, cmp)
          if (nextNode == null) done = true
          else {
            val x = nextNode.`val`
            if (x != null) {
              if (tooLow(nextNode.key, cmp)) nextNode = null
              else nextValue = x
              done = true
            }
          }
        }
      }

      override def remove(): Unit = {
        val l = lastReturned
        if (l == null) throw new IllegalStateException()
        m.remove(l.key)
        lastReturned = null
      }

      def trySplit(): Spliterator[T] = null

      def tryAdvance(action: Consumer[_ >: T]): Boolean = {
        if (hasNext()) {
          action.accept(next())
          true
        } else false
      }

      override def forEachRemaining(action: Consumer[_ >: T]): Unit = {
        while (hasNext()) action.accept(next())
      }

      def estimateSize(): Long = java.lang.Long.MAX_VALUE
    }

    final class SubMapValueIterator extends SubMapIter[V] {
      def next(): V = {
        val v = nextValue
        advance()
        v
      }
      def characteristics(): Int = 0
    }

    final class SubMapKeyIterator extends SubMapIter[K] {
      def next(): K = {
        val n = nextNode
        advance()
        n.key
      }
      def characteristics(): Int =
        Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SORTED
      override def getComparator(): Comparator[_ >: K] = SubMap.this.comparator()
    }

    final class SubMapEntryIterator extends SubMapIter[Map.Entry[K, V]] {
      def next(): Map.Entry[K, V] = {
        val n = nextNode
        val v = nextValue
        advance()
        new AbstractMap.SimpleImmutableEntry[K, V](n.key, v)
      }
      def characteristics(): Int = Spliterator.DISTINCT
    }
  } // end SubMap

  private[concurrent] abstract class CSLMSpliterator[K, V](
      private[concurrent] final val comparator: Comparator[_ >: K],
      private[concurrent] var row: Index[K, V],
      private[concurrent] var current: Node[K, V],
      private[concurrent] final val fence: K,
      private[concurrent] var est: Long
  ) {
    // exclusive upper bound for keys, or null if to end
    // the level to split out
    // current traversal node; initialize at origin
    // size estimate

    def estimateSize(): Long = est
  }

  private[concurrent] final class KeySpliterator[K, V](
      comparator: Comparator[_ >: K],
      _row: Index[K, V],
      origin: Node[K, V],
      fence: K,
      _est: Long
  ) extends CSLMSpliterator[K, V](comparator, _row, origin, fence, _est)
      with Spliterator[K] {
    def trySplit(): KeySpliterator[K, V] = {
      var e: Node[K, V] = null.asInstanceOf[Node[K, V]]
      var ek: K = null.asInstanceOf[K]
      var cmp: Comparator[_ >: K] = comparator
      var f: K = fence
      if ({ e = current; e } != null && { ek = e.key; ek } != null) {
        var q: Index[K, V] = row
        while (q != null) {
          var s: Index[K, V] = null.asInstanceOf[Index[K, V]]
          var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
          var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
          var sk: K = null.asInstanceOf[K]
          if ({ s = q.right; s } != null && { b = s.node; b } != null &&
              {
                n = b.next
                n
              } != null && n.`val` != null &&
              {
                sk = n.key
                sk
              } != null && cpr(cmp, sk, ek) > 0 &&
              (f == null || cpr(cmp, sk, f) < 0)) {
            current = n
            var r: Index[K, V] = q.down
            row = if (s.right != null) s else s.down
            est -= est >>> 2;
            return new KeySpliterator[K, V](cmp, r, e, sk, est)
          }

          row = q.down; q = row
        }
      }
      null.asInstanceOf[KeySpliterator[K, V]]
    }

    override def forEachRemaining(action: Consumer[_ >: K]): Unit = {
      if (action == null) throw new NullPointerException();
      var cmp: Comparator[_ >: K] = comparator
      var f: K = fence
      var e: Node[K, V] = current
      current = null
      var breakInner = false
      while (e != null && !breakInner) {
        var k: K = null.asInstanceOf[K]
        if ({ k = e.key; k } != null && f != null && cpr(cmp, f, k) <= 0)
          breakInner = true // break
        else {
          if (e.`val` != null)
            action.accept(k)
          e = e.next
        }
      }
    }

    def tryAdvance(action: Consumer[_ >: K]): Boolean = {
      if (action == null) throw new NullPointerException();
      var cmp: Comparator[_ >: K] = comparator
      var f: K = fence
      var e: Node[K, V] = current
      var breakInner = false
      while (e != null && !breakInner) {
        var k: K = null.asInstanceOf[K]
        if ({ k = e.key; k } != null && f != null && cpr(cmp, f, k) <= 0) {
          e = null
          breakInner = true // break
        } else if (e.`val` != null) {
          current = e.next
          action.accept(k)
          return true
        } else
          e = e.next
      }
      current = e
      false
    }

    def characteristics(): Int = {
      Spliterator.DISTINCT | Spliterator.SORTED |
        Spliterator.ORDERED | Spliterator.CONCURRENT |
        Spliterator.NONNULL
    }

    override def getComparator(): Comparator[_ >: K] = {
      comparator
    }
  }

  private[concurrent] final class ValueSpliterator[K, V](
      comparator: Comparator[_ >: K],
      _row: Index[K, V],
      origin: Node[K, V],
      fence: K,
      _est: Long
  ) extends CSLMSpliterator[K, V](comparator, _row, origin, fence, _est)
      with Spliterator[V] {
    def trySplit(): ValueSpliterator[K, V] = {
      var e: Node[K, V] = null.asInstanceOf[Node[K, V]]
      var ek: K = null.asInstanceOf[K]
      var cmp: Comparator[_ >: K] = comparator
      var f: K = fence
      if ({ e = current; e } != null && { ek = e.key; ek } != null) {
        var q: Index[K, V] = row
        while (q != null) {
          var s: Index[K, V] = null.asInstanceOf[Index[K, V]]
          var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
          var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
          var sk: K = null.asInstanceOf[K]
          if ({ s = q.right; s } != null && { b = s.node; b } != null &&
              {
                n = b.next
                n
              } != null && n.`val` != null &&
              {
                sk = n.key
                sk
              } != null && cpr(cmp, sk, ek) > 0 &&
              (f == null || cpr(cmp, sk, f) < 0)) {
            current = n
            var r: Index[K, V] = q.down
            row = if (s.right != null) s else s.down
            est -= est >>> 2;
            return new ValueSpliterator[K, V](cmp, r, e, sk, est)
          }

          row = q.down; q = row
        }
      }
      null.asInstanceOf[ValueSpliterator[K, V]]
    }

    override def forEachRemaining(action: Consumer[_ >: V]): Unit = {
      if (action == null) throw new NullPointerException();
      var cmp: Comparator[_ >: K] = comparator
      var f: K = fence
      var e: Node[K, V] = current
      current = null
      var breakInner = false
      while (e != null && !breakInner) {
        var k: K = null.asInstanceOf[K]
        var v: V = null.asInstanceOf[V]
        if ({ k = e.key; k } != null && f != null && cpr(cmp, f, k) <= 0)
          breakInner = true // break
        else {
          if ({ v = e.`val`; v } != null)
            action.accept(v)
          e = e.next
        }
      }
    }

    def tryAdvance(action: Consumer[_ >: V]): Boolean = {
      if (action == null) throw new NullPointerException();
      var cmp: Comparator[_ >: K] = comparator
      var f: K = fence
      var e: Node[K, V] = current
      var breakInner = false
      while (e != null && !breakInner) {
        var k: K = null.asInstanceOf[K]
        var v: V = null.asInstanceOf[V]
        if ({ k = e.key; k } != null && f != null && cpr(cmp, f, k) <= 0) {
          e = null
          breakInner = true // break
        } else if ({ v = e.`val`; v } != null) {
          current = e.next
          action.accept(v)
          return true
        } else
          e = e.next
      }
      current = e
      false
    }

    def characteristics(): Int = {
      Spliterator.CONCURRENT | Spliterator.ORDERED |
        Spliterator.NONNULL
    }
  }

  private[concurrent] final class EntrySpliterator[K, V](
      comparator: Comparator[_ >: K],
      _row: Index[K, V],
      origin: Node[K, V],
      fence: K,
      _est: Long
  ) extends CSLMSpliterator[K, V](comparator, _row, origin, fence, _est)
      with Spliterator[Map.Entry[K, V]] {
    def trySplit(): EntrySpliterator[K, V] = {
      var e: Node[K, V] = null.asInstanceOf[Node[K, V]]
      var ek: K = null.asInstanceOf[K]
      var cmp: Comparator[_ >: K] = comparator
      var f: K = fence
      if ({ e = current; e } != null && { ek = e.key; ek } != null) {
        var q: Index[K, V] = row
        while (q != null) {
          var s: Index[K, V] = null.asInstanceOf[Index[K, V]]
          var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
          var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
          var sk: K = null.asInstanceOf[K]
          if ({ s = q.right; s } != null && { b = s.node; b } != null &&
              {
                n = b.next
                n
              } != null && n.`val` != null &&
              {
                sk = n.key
                sk
              } != null && cpr(cmp, sk, ek) > 0 &&
              (f == null || cpr(cmp, sk, f) < 0)) {
            current = n
            var r: Index[K, V] = q.down
            row = if (s.right != null) s else s.down
            est -= est >>> 2;
            return new EntrySpliterator[K, V](cmp, r, e, sk, est)
          }

          row = q.down; q = row
        }
      }
      null.asInstanceOf[EntrySpliterator[K, V]]
    }

    override def forEachRemaining(action: Consumer[_ >: Map.Entry[K, V]]): Unit = {
      if (action == null) throw new NullPointerException();
      var cmp: Comparator[_ >: K] = comparator
      var f: K = fence
      var e: Node[K, V] = current
      current = null
      var breakInner = false
      while (e != null && !breakInner) {
        var k: K = null.asInstanceOf[K]
        var v: V = null.asInstanceOf[V]
        if ({ k = e.key; k } != null && f != null && cpr(cmp, f, k) <= 0)
          breakInner = true // break
        else {
          if ({ v = e.`val`; v } != null) {
            action.accept(new AbstractMap.SimpleImmutableEntry[K, V](k, v))
          }
          e = e.next
        }
      }
    }

    def tryAdvance(action: Consumer[_ >: Map.Entry[K, V]]): Boolean = {
      if (action == null) throw new NullPointerException();
      var cmp: Comparator[_ >: K] = comparator
      var f: K = fence
      var e: Node[K, V] = current
      var breakInner = false
      while (e != null && !breakInner) {
        var k: K = null.asInstanceOf[K]
        var v: V = null.asInstanceOf[V]
        if ({ k = e.key; k } != null && f != null && cpr(cmp, f, k) <= 0) {
          e = null
          breakInner = true // break
        } else if ({ v = e.`val`; v } != null) {
          current = e.next
          action.accept(new AbstractMap.SimpleImmutableEntry[K, V](k, v))
          return true
        } else
          e = e.next
      }
      current = e
      false
    }

    def characteristics(): Int = {
      Spliterator.DISTINCT | Spliterator.SORTED |
        Spliterator.ORDERED | Spliterator.CONCURRENT |
        Spliterator.NONNULL
    }

    override def getComparator(): Comparator[Map.Entry[K, V]] = {
      // Adapt or create a key-based comparator
      if (comparator != null) {
        new Comparator[Map.Entry[K, V]] with Serializable {
          def compare(a: Map.Entry[K, V], b: Map.Entry[K, V]): Int =
            comparator.compare(a.getKey(), b.getKey())
        }
      } else {
        new Comparator[Map.Entry[K, V]] with Serializable {
          def compare(e1: Map.Entry[K, V], e2: Map.Entry[K, V]): Int =
            e1.getKey().asInstanceOf[Comparable[Any]].compareTo(e2.getKey())
        }
      }
    }
  } // end EntrySpliterator
} // end object ConcurrentSkipListMap

@SerialVersionUID(-8627078645895051609L)
class ConcurrentSkipListMap[K, V](
    @safePublish private[concurrent] final val _comparator: Comparator[_ >: K]
) extends AbstractMap[K, V]
    with ConcurrentNavigableMap[K, V]
    with Cloneable
    with Serializable {
  import ConcurrentSkipListMap._

  @transient private[concurrent] var head: Index[K, V] = _
  @transient private var adder: LongAdder = _
  @transient private var _keySet: KeySet[K, V] = _
  @transient private var _values: Values[K, V] = _
  @transient private var _entrySet: EntrySet[K, V] = _
  @transient private var _descendingMap: SubMap[K, V] = _

  @alwaysinline private def HEAD: AtomicRef[Index[K, V]] =
    fromRawPtr[Index[K, V]](classFieldRawPtr(this, "head")).atomic
  @alwaysinline private def ADDER: AtomicRef[LongAdder] =
    fromRawPtr[LongAdder](classFieldRawPtr(this, "adder")).atomic

  /* ----------------  Utilities -------------- */

  private[concurrent] def baseHead(): Node[K, V] = {
    var h: Index[K, V] = null.asInstanceOf[Index[K, V]]
    VarHandle.acquireFence()
    if ({ h = head; h } == null) null.asInstanceOf[Node[K, V]] else h.node
  }

  private def addCount(c: Long): Unit = {
    var a: LongAdder = null.asInstanceOf[LongAdder]
    while ({
      a = adder
      if (a == null) {
        a = new LongAdder()
        if (!ADDER.compareExchangeStrong(null.asInstanceOf[LongAdder], a)) {
          a = null
        }
      }
      a == null
    }) {}
    a.add(c)
  }

  private[concurrent] def getAdderCount(): Long = {
    var a: LongAdder = null.asInstanceOf[LongAdder]
    var c: Long = 0L
    while ({
      a = adder
      if (a == null) {
        a = new LongAdder()
        if (!ADDER.compareExchangeStrong(null.asInstanceOf[LongAdder], a)) {
          a = null
        }
      }
      a == null
    }) {}
    c = a.sum; if (c <= 0L) 0L else c // ignore transient negatives
  }

  /* ---------------- Traversal -------------- */

  private def findPredecessor(key: Any, cmp: Comparator[_ >: K]): Node[K, V] = {
    var q: Index[K, V] = null.asInstanceOf[Index[K, V]]
    VarHandle.acquireFence()
    if ({ q = head; q } == null || key == null)
      null.asInstanceOf[Node[K, V]]
    else {
      var r: Index[K, V] = null.asInstanceOf[Index[K, V]]
      var d: Index[K, V] = null.asInstanceOf[Index[K, V]]
      while (true) {
        var breakInner = false
        while (!breakInner && { r = q.right; r } != null) {
          var p: Node[K, V] = null.asInstanceOf[Node[K, V]]
          var k: K = null.asInstanceOf[K]
          if ({ p = r.node; p } == null || { k = p.key; k } == null ||
              p.`val` == null) // unlink index to deleted node
            q.RIGHT.compareExchangeStrong(r, r.right)
          else if (cpr(cmp, key, k) > 0)
            q = r
          else
            breakInner = true // break
        }
        if ({ d = q.down; d } != null)
          q = d
        else
          return q.node
      }
      null.asInstanceOf[Node[K, V]] // unreachable
    }
  }

  private def findNode(key: Any): Node[K, V] = {
    if (key == null)
      throw new NullPointerException()
    // don't postpone errors
    var cmp: Comparator[_ >: K] = _comparator
    var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var breakOuter = false
    while (!breakOuter && ({ b = findPredecessor(key, cmp); b } != null)) {
      var breakInner = false
      while (!breakInner && !breakOuter) {
        var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
        var k: K = null.asInstanceOf[K]
        var v: V = null.asInstanceOf[V]
        var c: Int = 0
        if ({ n = b.next; n } == null)
          breakOuter = true // empty
        else if ({ k = n.key; k } == null)
          breakInner = true // b is deleted
        else if ({ v = n.`val`; v } == null)
          unlinkNode(b, n)
        // n is deleted
        else if ({ c = cpr(cmp, key, k); c } > 0)
          b = n
        else if (c == 0)
          return n
        else
          breakOuter = true
      }
    }
    null.asInstanceOf[Node[K, V]]
  }

  private def doGet(key: Any): V = {
    var q: Index[K, V] = null.asInstanceOf[Index[K, V]]
    VarHandle.acquireFence()
    if (key == null)
      throw new NullPointerException()
    var cmp: Comparator[_ >: K] = _comparator
    var result: V = null.asInstanceOf[V]
    if ({ q = head; q } != null) {
      var r: Index[K, V] = null.asInstanceOf[Index[K, V]]
      var d: Index[K, V] = null.asInstanceOf[Index[K, V]]
      var breakOuter = false
      while (!breakOuter) {
        var breakRight = false
        while (!breakRight && !breakOuter && ({ r = q.right; r } != null)) {
          var p: Node[K, V] = null.asInstanceOf[Node[K, V]]
          var k: K = null.asInstanceOf[K]
          var v: V = null.asInstanceOf[V]
          var c: Int = 0
          if ({ p = r.node; p } == null || { k = p.key; k } == null ||
              {
                v = p.`val`
                v
              } == null)
            q.RIGHT.compareExchangeStrong(r, r.right)
          else if ({ c = cpr(cmp, key, k); c } > 0)
            q = r
          else if (c == 0) {
            result = v
            breakOuter = true
          } else
            breakRight = true // break
        }
        if (!breakOuter) {
          if ({ d = q.down; d } != null)
            q = d
          else {
            var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
            var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
            if ({ b = q.node; b } != null) {
              var breakBase = false
              while (!breakBase && { n = b.next; n } != null) {
                var v: V = null.asInstanceOf[V]
                var c: Int = 0
                var k: K = n.key
                if ({ v = n.`val`; v } == null || k == null ||
                    {
                      c = cpr(cmp, key, k)
                      c
                    } > 0)
                  b = n
                else {
                  if (c == 0)
                    result = v
                  breakBase = true // break
                }
              }
            }
            breakOuter = true // break
          }
        }
      }
    }
    result
  }

  /* ---------------- Insertion -------------- */

  private def doPut(key: K, value: V, onlyIfAbsent: Boolean): V = {
    if (key == null)
      throw new NullPointerException()
    var cmp: Comparator[_ >: K] = _comparator
    while (true) {
      var h: Index[K, V] = null.asInstanceOf[Index[K, V]]
      var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
      VarHandle.acquireFence()
      var levels: Int = 0
      // number of levels descended
      if ({ h = head; h } == null) { // try to initialize
        var base: Node[K, V] = new Node[K, V](null.asInstanceOf[K], null.asInstanceOf[V], null)
        h = new Index[K, V](base, null, null)
        b = if (HEAD.compareExchangeStrong(null.asInstanceOf[Index[K, V]], h)) base else null
      } else {
        var q: Index[K, V] = h
        var r: Index[K, V] = null.asInstanceOf[Index[K, V]]
        var d: Index[K, V] = null.asInstanceOf[Index[K, V]]
        var breakDescend = false
        while (!breakDescend) {
          // count while descending
          var breakRight = false
          while (!breakRight && { r = q.right; r } != null) {
            var p: Node[K, V] = null.asInstanceOf[Node[K, V]]
            var k: K = null.asInstanceOf[K]
            if ({ p = r.node; p } == null || { k = p.key; k } == null ||
                p.`val` == null)
              q.RIGHT.compareExchangeStrong(r, r.right)
            else if (cpr(cmp, key, k) > 0)
              q = r
            else
              breakRight = true
          }
          if ({ d = q.down; d } != null) {
            levels += 1
            q = d
          } else {
            b = q.node
            breakDescend = true
          }
        }
      }
      if (b != null) {
        var z: Node[K, V] = null
        // new node, if inserted
        var breakInsert = false
        while (!breakInsert) {
          // find insertion point
          var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
          var p: Node[K, V] = null.asInstanceOf[Node[K, V]]
          var k: K = null.asInstanceOf[K]
          var v: V = null.asInstanceOf[V]
          var c: Int = 0
          if ({ n = b.next; n } == null) {
            if (b.key == null) // if empty, type check key now
              cpr(cmp, key, key)
            c = -1
          } else if ({ k = n.key; k } == null)
            breakInsert = true // can't append; restart
          else if ({ v = n.`val`; v } == null) {
            unlinkNode(b, n)
            c = 1
          } else if ({ c = cpr(cmp, key, k); c } > 0)
            b = n
          else if (c == 0 &&
              (onlyIfAbsent || n.VAL.compareExchangeStrong(v.asInstanceOf[AnyRef], value.asInstanceOf[AnyRef])))
            return v

          if (c < 0 &&
              { p = new Node[K, V](key, value, n); b.NEXT.compareExchangeStrong(n, p) }) {
            z = p
            breakInsert = true
          }
        }

        if (z != null) {
          var lr: Int = ThreadLocalRandom.nextSecondarySeed()
          if ((lr & 0x3) == 0) { // add indices with 1/4 prob
            var hr: Int = ThreadLocalRandom.nextSecondarySeed()
            var rnd: Long = (hr.toLong << 32) | (lr.toLong & 0xffffffffL)
            var skips: Int = levels
            // levels to descend before add
            var x: Index[K, V] = null
            var breakIndices = false
            while (!breakIndices) {
              // create at most 62 indices
              x = new Index[K, V](z, x, null)
              if (rnd >= 0L || { skips -= 1; skips } < 0)
                breakIndices = true
              else
                rnd <<= 1;
            }
            if (addIndices(h, skips, x, cmp) && skips < 0 &&
                head == h) { // try to add new level
              var hx: Index[K, V] = new Index[K, V](z, x, null)
              var nh: Index[K, V] = new Index[K, V](h.node, h, hx)
              HEAD.compareExchangeStrong(h, nh)
            }
            if (z.`val` == null) // deleted while adding indices
              findPredecessor(key, cmp)
            // clean
          }
          addCount(1L)
          return null.asInstanceOf[V]
        }
      }
    }
    null.asInstanceOf[V] // unreachable
  }

  /* ---------------- Deletion -------------- */

  private[concurrent] def doRemove(key: Any, value: Any): V = {
    if (key == null)
      throw new NullPointerException()
    var cmp: Comparator[_ >: K] = _comparator
    var result: V = null.asInstanceOf[V]
    var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var breakOuter = false
    while (!breakOuter && ({ b = findPredecessor(key, cmp); b } != null &&
        result == null)) {
      var breakInner = false
      while (!breakInner && !breakOuter) {
        var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
        var k: K = null.asInstanceOf[K]
        var v: V = null.asInstanceOf[V]
        var c: Int = 0
        if ({ n = b.next; n } == null)
          breakOuter = true
        else if ({ k = n.key; k } == null)
          breakInner = true
        else if ({ v = n.`val`; v } == null)
          unlinkNode(b, n)
        else if ({ c = cpr(cmp, key, k); c } > 0)
          b = n
        else if (c < 0)
          breakOuter = true
        else if (value != null && !value.asInstanceOf[AnyRef].equals(v.asInstanceOf[AnyRef]))
          breakOuter = true
        else if (n.VAL.compareExchangeStrong(v.asInstanceOf[AnyRef], null.asInstanceOf[AnyRef])) {
          result = v
          unlinkNode(b, n)
          breakInner = true // loop to clean up
        }
      }
    }
    if (result != null) {
      tryReduceLevel();
      addCount(-1L)
    }
    result
  }

  private def tryReduceLevel(): Unit = {
    var h: Index[K, V] = null.asInstanceOf[Index[K, V]]
    var d: Index[K, V] = null.asInstanceOf[Index[K, V]]
    var e: Index[K, V] = null.asInstanceOf[Index[K, V]]
    if ({ h = head; h } != null && h.right == null &&
        {
          d = h.down
          d
        } != null && d.right == null &&
        {
          e = d.down
          e
        } != null && e.right == null &&
        HEAD.compareExchangeStrong(h, d) &&
        h.right != null) // recheck
      HEAD.compareExchangeStrong(d, h)
    // try to backout
  }

  /* ---------------- Finding and removing first element -------------- */

  private[concurrent] def findFirst(): Node[K, V] = {
    var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
    if ({ b = baseHead(); b } != null) {
      while ({ n = b.next; n } != null) {
        if (n.`val` == null)
          unlinkNode(b, n)
        else
          return n
      }
    }
    null.asInstanceOf[Node[K, V]]
  }

  private[concurrent] def findFirstEntry(): AbstractMap.SimpleImmutableEntry[K, V] = {
    var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var v: V = null.asInstanceOf[V]
    if ({ b = baseHead(); b } != null) {
      while ({ n = b.next; n } != null) {
        if ({ v = n.`val`; v } == null)
          unlinkNode(b, n)
        else
          return new AbstractMap.SimpleImmutableEntry[K, V](n.key, v)
      }
    }
    null.asInstanceOf[AbstractMap.SimpleImmutableEntry[K, V]]
  }

  private def doRemoveFirstEntry(): AbstractMap.SimpleImmutableEntry[K, V] = {
    var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var v: V = null.asInstanceOf[V]
    if ({ b = baseHead(); b } != null) {
      while ({ n = b.next; n } != null) {
        if ({ v = n.`val`; v } == null || n.VAL.compareExchangeStrong(
              v.asInstanceOf[AnyRef],
              null.asInstanceOf[AnyRef]
            )) {
          var k: K = n.key
          unlinkNode(b, n)
          if (v != null) {
            tryReduceLevel();
            findPredecessor(k, _comparator)
            // clean index
            addCount(-1L)
            return new AbstractMap.SimpleImmutableEntry[K, V](k, v)
          }
        }
      }
    }
    null.asInstanceOf[AbstractMap.SimpleImmutableEntry[K, V]]
  }

  /* ---------------- Finding and removing last element -------------- */

  private[concurrent] def findLast(): Node[K, V] = {
    var breakOuter = false
    while (!breakOuter) {
      var q: Index[K, V] = null.asInstanceOf[Index[K, V]]
      var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
      VarHandle.acquireFence()
      if ({ q = head; q } == null)
        breakOuter = true
      else {
        var breakMid = false
        while (!breakMid) {
          var r: Index[K, V] = null.asInstanceOf[Index[K, V]]
          var d: Index[K, V] = null.asInstanceOf[Index[K, V]]
          while ({ r = q.right; r } != null) {
            var p: Node[K, V] = null.asInstanceOf[Node[K, V]]
            if ({ p = r.node; p } == null || p.`val` == null)
              q.RIGHT.compareExchangeStrong(r, r.right)
            else
              q = r
          }
          if ({ d = q.down; d } != null)
            q = d
          else {
            b = q.node
            breakMid = true
          }
        }
        if (b != null) {
          var breakInner = false
          while (!breakInner && !breakOuter) {
            var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
            if ({ n = b.next; n } == null) {
              if (b.key == null) // empty
                breakOuter = true
              else
                return b
            } else if (n.key == null)
              breakInner = true
            else if (n.`val` == null)
              unlinkNode(b, n)
            else
              b = n
          }
        }
      }
    }
    null.asInstanceOf[Node[K, V]]
  }

  private[concurrent] def findLastEntry(): AbstractMap.SimpleImmutableEntry[K, V] = {
    while (true) {
      var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
      var v: V = null.asInstanceOf[V]
      if ({ n = findLast(); n } == null)
        return null.asInstanceOf[AbstractMap.SimpleImmutableEntry[K, V]]
      if ({ v = n.`val`; v } != null)
        return new AbstractMap.SimpleImmutableEntry[K, V](n.key, v)
    }
    null.asInstanceOf[AbstractMap.SimpleImmutableEntry[K, V]] // unreachable
  }

  private def doRemoveLastEntry(): Map.Entry[K, V] = {
    var breakOuter = false
    while (!breakOuter) {
      var q: Index[K, V] = null.asInstanceOf[Index[K, V]]
      var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
      VarHandle.acquireFence()
      if ({ q = head; q } == null)
        breakOuter = true
      else {
        var breakMid = false
        while (!breakMid) {
          var d: Index[K, V] = null.asInstanceOf[Index[K, V]]
          var r: Index[K, V] = null.asInstanceOf[Index[K, V]]
          var p: Node[K, V] = null.asInstanceOf[Node[K, V]]
          var breakRight = false
          while (!breakRight && { r = q.right; r } != null) {
            if ({ p = r.node; p } == null || p.`val` == null)
              q.RIGHT.compareExchangeStrong(r, r.right)
            else if (p.next != null)
              q = r // continue only if a successor
            else
              breakRight = true
          }
          if ({ d = q.down; d } != null)
            q = d
          else {
            b = q.node
            breakMid = true
          }
        }
        if (b != null) {
          var breakInner = false
          while (!breakInner && !breakOuter) {
            var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
            var k: K = null.asInstanceOf[K]
            var v: V = null.asInstanceOf[V]
            if ({ n = b.next; n } == null) {
              if (b.key == null) // empty
                breakOuter = true
              else
                breakInner = true // retry
            } else if ({ k = n.key; k } == null)
              breakInner = true
            else if ({ v = n.`val`; v } == null)
              unlinkNode(b, n)
            else if (n.next != null)
              b = n
            else if (n.VAL.compareExchangeStrong(v.asInstanceOf[AnyRef], null.asInstanceOf[AnyRef])) {
              unlinkNode(b, n)
              tryReduceLevel()
              findPredecessor(k, _comparator) // clean index
              addCount(-1L)
              return new AbstractMap.SimpleImmutableEntry[K, V](k, v)
            }
          }
        }
      }
    }
    null.asInstanceOf[Map.Entry[K, V]]
  }

  /* ---------------- Relational operations -------------- */

  // Control values OR'ed as arguments to findNear

  // Actually checked as !LT

  private[concurrent] def findNear(key: K, rel: Int, cmp: Comparator[_ >: K]): Node[K, V] = {
    if (key == null)
      throw new NullPointerException()
    var result: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var breakOuter = false
    while (!breakOuter) {
      var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
      if ({ b = findPredecessor(key, cmp); b } == null) {
        result = null
        breakOuter = true // empty
      } else {
        var breakInner = false
        while (!breakInner && !breakOuter) {
          var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
          var k: K = null.asInstanceOf[K]
          var c: Int = 0
          if ({ n = b.next; n } == null) {
            result = if ((rel & LT) != 0 && b.key != null) b else null.asInstanceOf[Node[K, V]]
            breakOuter = true
          } else if ({ k = n.key; k } == null)
            breakInner = true
          else if (n.`val` == null)
            unlinkNode(b, n)
          else if (({ c = cpr(cmp, key, k); c } == 0 && (rel & EQ) != 0) ||
              (c < 0 && (rel & LT) == 0)) {
            result = n
            breakOuter = true
          } else if (c <= 0 && (rel & LT) != 0) {
            result = if (b.key != null) b else null.asInstanceOf[Node[K, V]]
            breakOuter = true
          } else
            b = n
        }
      }
    }
    result
  }

  private[concurrent] def findNearEntry(
      key: K,
      rel: Int,
      cmp: Comparator[_ >: K]
  ): AbstractMap.SimpleImmutableEntry[K, V] = {
    while (true) {
      var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
      var v: V = null.asInstanceOf[V]
      if ({ n = findNear(key, rel, cmp); n } == null)
        return null.asInstanceOf[AbstractMap.SimpleImmutableEntry[K, V]]
      if ({ v = n.`val`; v } != null)
        return new AbstractMap.SimpleImmutableEntry[K, V](n.key, v)
    }
    null.asInstanceOf[AbstractMap.SimpleImmutableEntry[K, V]] // unreachable
  }

  /* ---------------- Constructors -------------- */

  def this() = this(null.asInstanceOf[Comparator[_ >: K]])

  def this(m: Map[_ <: K, _ <: V]) = {
    this(null.asInstanceOf[Comparator[_ >: K]])
    putAll(m)
  }

  def this(m: SortedMap[K, _ <: V]) = {
    this(m.comparator())
    buildFromSorted(m)
  }

  override def clone(): ConcurrentSkipListMap[K, V] = {
    try {
      val clone = super.clone().asInstanceOf[ConcurrentSkipListMap[K, V]]
      clone._keySet = null
      clone._entrySet = null
      clone._values = null
      clone._descendingMap = null
      clone.adder = null
      clone.buildFromSorted(this)
      clone
    } catch {
      case _: CloneNotSupportedException => throw new InternalError()
    }
  }

  private def buildFromSorted(map: SortedMap[K, _ <: V]): Unit = {
    if (map == null)
      throw new NullPointerException()
    var it: Iterator[_ <: Map.Entry[_ <: K, _ <: V]] =
      map.entrySet().iterator()

    /*
     * Add equally spaced indices at log intervals, using the bits
     * of count during insertion. The maximum possible resulting
     * level is less than the number of bits in a long (64). The
     * preds array tracks the current rightmost node at each
     * level.
     */
    var preds: Array[Index[K, V]] = new Array[Index[K, V]](64)
    var bp: Node[K, V] = new Node[K, V](null.asInstanceOf[K], null.asInstanceOf[V], null)
    val _idx0 = new Index[K, V](bp, null, null); preds(0) = _idx0; var h: Index[K, V] = _idx0
    var count: Long = 0

    while (it.hasNext()) {
      var e: Map.Entry[_ <: K, _ <: V] = it.next()
      var k: K = e.getKey()
      var v: V = e.getValue()
      if (k == null || v == null)
        throw new NullPointerException()
      var z: Node[K, V] = new Node[K, V](k, v, null)
      bp.next = z; bp = z
      if (({ count += 1; count } & 3L) == 0L) {
        var m: Long = count >>> 2
        var i: Int = 0
        var idx: Index[K, V] = null.asInstanceOf[Index[K, V]]; var q: Index[K, V] = null.asInstanceOf[Index[K, V]]
        var _doWhile = true
        while (_doWhile) {
          idx = new Index[K, V](z, idx, null)
          if ({ q = preds(i); q } == null) {
            h = new Index[K, V](h.node, h, idx); preds(i) = h
          } else {
            q.right = idx; preds(i) = idx
          }

          _doWhile = ({ i += 1; i } < preds.length && { m = m >>> 1; (m & 1L) != 0L })
        }
      }
    }
    if (count != 0L) {
      VarHandle.releaseFence()
      // emulate volatile stores
      addCount(count)
      head = h
      VarHandle.fullFence()
    }
  }

  /* ---------------- Serialization -------------- */

  private def writeObject(s: java.io.ObjectOutputStream): Unit = {
    // Write out the Comparator and any hidden stuff
    s.defaultWriteObject()

    // Write out keys and values (alternating)
    var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var v: V = null.asInstanceOf[V]
    if ({ b = baseHead(); b } != null) {
      while ({ n = b.next; n } != null) {
        if ({ v = n.`val`; v } != null) {
          s.writeObject(n.key)
          s.writeObject(v)
        }
        b = n
      }
    }
    s.writeObject(null)
  }

  private def readObject(s: java.io.ObjectInputStream): Unit = {
    // Read in the Comparator and any hidden stuff
    s.defaultReadObject()

    // Same idea as buildFromSorted
    var preds: Array[Index[K, V]] = new Array[Index[K, V]](64)
    var bp: Node[K, V] = new Node[K, V](null.asInstanceOf[K], null.asInstanceOf[V], null)
    val _idx0 = new Index[K, V](bp, null, null); preds(0) = _idx0; var h: Index[K, V] = _idx0
    var cmp: Comparator[_ >: K] = _comparator
    var prevKey: K = null.asInstanceOf[K]
    var count: Long = 0

    var breakOuter = false
    while (!breakOuter) {
      var k: K = s.readObject().asInstanceOf[K]
      if (k == null)
        breakOuter = true
      else {
        var v: V = s.readObject().asInstanceOf[V]
        if (v == null)
          throw new NullPointerException()
        if (prevKey != null && cpr(cmp, prevKey, k) > 0)
          throw new IllegalStateException("out of order")
        prevKey = k
        var z: Node[K, V] = new Node[K, V](k, v, null)
        bp.next = z; bp = z
        if (({ count += 1; count } & 3L) == 0L) {
          var m: Long = count >>> 2
          var i: Int = 0
          var idx: Index[K, V] = null.asInstanceOf[Index[K, V]]; var q: Index[K, V] = null.asInstanceOf[Index[K, V]]
          var _doWhile = true
          while (_doWhile) {
            idx = new Index[K, V](z, idx, null)
            if ({ q = preds(i); q } == null) {
              h = new Index[K, V](h.node, h, idx); preds(i) = h
            } else {
              q.right = idx; preds(i) = idx
            }

            _doWhile = ({ i += 1; i } < preds.length && { m = m >>> 1; (m & 1L) != 0L })
          }
        }
      }
    }
    if (count != 0L) {
      VarHandle.releaseFence()
      addCount(count)
      head = h
      VarHandle.fullFence()
    }
  }

  /* ------ Map API methods ------ */

  override def containsKey(key: Any): Boolean = {
    doGet(key) != null
  }

  override def get(key: Any): V = {
    doGet(key)
  }

  override def getOrDefault(key: Any, defaultValue: V): V = {
    var v: V = null.asInstanceOf[V]
    if ({ v = doGet(key); v } == null) defaultValue else v
  }

  override def put(key: K, value: V): V = {
    if (value == null)
      throw new NullPointerException()
    doPut(key, value, false)
  }

  override def remove(key: Any): V = {
    doRemove(key, null.asInstanceOf[V])
  }

  override def containsValue(value: Any): Boolean = {
    if (value == null)
      throw new NullPointerException()
    var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var v: V = null.asInstanceOf[V]
    if ({ b = baseHead(); b } != null) {
      while ({ n = b.next; n } != null) {
        if ({ v = n.`val`; v } != null && value.asInstanceOf[AnyRef].equals(v.asInstanceOf[AnyRef]))
          return true
        else
          b = n
      }
    }
    false
  }

  override def size(): Int = {
    var c: Long = 0L
    if (baseHead() == null) 0
    else {
      c = getAdderCount()
      if (c >= java.lang.Integer.MAX_VALUE) java.lang.Integer.MAX_VALUE else c.toInt
    }
  }

  override def isEmpty(): Boolean = {
    findFirst() == null
  }

  override def clear(): Unit = {
    var h: Index[K, V] = null.asInstanceOf[Index[K, V]]
    var r: Index[K, V] = null.asInstanceOf[Index[K, V]]
    var d: Index[K, V] = null.asInstanceOf[Index[K, V]]
    var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
    VarHandle.acquireFence()
    var breakOuter = false
    while (!breakOuter && { h = head; h } != null) {
      if ({ r = h.right; r } != null) // remove indices
        h.RIGHT.compareExchangeStrong(r, null.asInstanceOf[Index[K, V]])
      else if ({ d = h.down; d } != null) // remove levels
        HEAD.compareExchangeStrong(h, d)
      else {
        var count: Long = 0L
        if ({ b = h.node; b } != null) { // remove nodes
          var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
          var v: V = null.asInstanceOf[V]
          while ({ n = b.next; n } != null) {
            if ({ v = n.`val`; v } != null &&
                n.VAL.compareExchangeStrong(v.asInstanceOf[AnyRef], null.asInstanceOf[AnyRef])) {
              count -= 1
              v = null.asInstanceOf[V]
            }
            if (v == null)
              unlinkNode(b, n)
          }
        }
        if (count != 0L)
          addCount(count)
        else
          breakOuter = true // break
      }
    }
  }

  override def computeIfAbsent(key: K, mappingFunction: Function[_ >: K, _ <: V]): V = {
    if (key == null || mappingFunction == null)
      throw new NullPointerException()
    var v: V = null.asInstanceOf[V]
    var p: V = null.asInstanceOf[V]
    var r: V = null.asInstanceOf[V]
    if ({ v = doGet(key); v } == null &&
        { r = mappingFunction.apply(key); r } != null)
      v = if ({ p = doPut(key, r, true); p } == null) r else p
    v
  }

  override def computeIfPresent(key: K, remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]): V = {
    if (key == null || remappingFunction == null)
      throw new NullPointerException()
    var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var v: V = null.asInstanceOf[V]
    while ({ n = findNode(key); n } != null) {
      if ({ v = n.`val`; v } != null) {
        var r: V = remappingFunction.apply(key, v)
        if (r != null) {
          if (n.VAL.compareExchangeStrong(v.asInstanceOf[AnyRef], r.asInstanceOf[AnyRef]))
            return r
        } else if (doRemove(key, v) != null)
          return null.asInstanceOf[V]
      }
    }
    null.asInstanceOf[V]
  }

  override def compute(key: K, remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]): V = {
    if (key == null || remappingFunction == null)
      throw new NullPointerException()
    while (true) {
      var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
      var v: V = null.asInstanceOf[V]
      var r: V = null.asInstanceOf[V]
      if ({ n = findNode(key); n } == null) {
        if ({ r = remappingFunction.apply(key, null.asInstanceOf[V]); r } == null)
          return null.asInstanceOf[V]
        if (doPut(key, r, true) == null)
          return r
      } else if ({ v = n.`val`; v } != null) {
        if ({ r = remappingFunction.apply(key, v); r } != null) {
          if (n.VAL.compareExchangeStrong(v.asInstanceOf[AnyRef], r.asInstanceOf[AnyRef]))
            return r
        } else if (doRemove(key, v) != null)
          return null.asInstanceOf[V]
      }
    }
    null.asInstanceOf[V] // unreachable
  }

  override def merge(key: K, value: V, remappingFunction: BiFunction[_ >: V, _ >: V, _ <: V]): V = {
    if (key == null || value == null || remappingFunction == null)
      throw new NullPointerException()
    while (true) {
      var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
      var v: V = null.asInstanceOf[V]
      var r: V = null.asInstanceOf[V]
      if ({ n = findNode(key); n } == null) {
        if (doPut(key, value, true) == null)
          return value
      } else if ({ v = n.`val`; v } != null) {
        if ({ r = remappingFunction.apply(v, value); r } != null) {
          if (n.VAL.compareExchangeStrong(v.asInstanceOf[AnyRef], r.asInstanceOf[AnyRef]))
            return r
        } else if (doRemove(key, v) != null)
          return null.asInstanceOf[V]
      }
    }
    null.asInstanceOf[V] // unreachable
  }

  /* ---------------- View methods -------------- */

  override def keySet(): NavigableSet[K] = {
    var ks: KeySet[K, V] = null.asInstanceOf[KeySet[K, V]]
    if ({ ks = _keySet; ks } != null) return ks;
    { _keySet = new KeySet(this); _keySet }
  }

  override def navigableKeySet(): NavigableSet[K] = {
    var ks: KeySet[K, V] = null.asInstanceOf[KeySet[K, V]]
    if ({ ks = _keySet; ks } != null) return ks;
    { _keySet = new KeySet(this); _keySet }
  }

  override def values(): Collection[V] = {
    var vs: Values[K, V] = null.asInstanceOf[Values[K, V]]
    if ({ vs = _values; vs } != null) return vs;
    { _values = new Values(this); _values }
  }

  override def entrySet(): Set[Map.Entry[K, V]] = {
    var es: EntrySet[K, V] = null.asInstanceOf[EntrySet[K, V]]
    if ({ es = _entrySet; es } != null) return es;
    { _entrySet = new EntrySet[K, V](this); _entrySet }
  }

  override def descendingMap(): ConcurrentNavigableMap[K, V] = {
    var dm: ConcurrentNavigableMap[K, V] = null.asInstanceOf[ConcurrentNavigableMap[K, V]]
    if ({ dm = _descendingMap; dm } != null) return dm
    _descendingMap = new SubMap[K, V](this, null.asInstanceOf[K], false, null.asInstanceOf[K], false, true)
    _descendingMap
  }

  override def descendingKeySet(): NavigableSet[K] = {
    descendingMap().navigableKeySet()
  }

  /* ---------------- AbstractMap Overrides -------------- */

  override def equals(o: Any): Boolean = {
    if (o.asInstanceOf[AnyRef] eq this)
      return true
    if (!o.isInstanceOf[Map[_, _]])
      return false
    var m: Map[_, _] = o.asInstanceOf[Map[_, _]]
    try {
      var cmp: Comparator[_ >: K] = _comparator
      // See JDK-8223553 for Iterator type wildcard rationale
      var it: Iterator[_ <: Map.Entry[_, _]] = m.entrySet().iterator()
      if (m.isInstanceOf[SortedMap[_, _]] &&
          (m
            .asInstanceOf[SortedMap[_, _]]
            .comparator()
            .asInstanceOf[AnyRef] eq cmp.asInstanceOf[AnyRef])) {
        var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
        var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
        if ({ b = baseHead(); b } != null) {
          while ({ n = b.next; n } != null) {
            var k: K = null.asInstanceOf[K]
            var v: V = null.asInstanceOf[V]
            if ({ v = n.`val`; v } != null && { k = n.key; k } != null) {
              if (!it.hasNext())
                return false
              var e: Map.Entry[_, _] = it.next()
              var mk: Any = e.getKey()
              var mv: Any = e.getValue()
              if (mk == null || mv == null)
                return false
              try {
                if (cpr(cmp, k, mk) != 0)
                  return false
              } catch {
                case _: ClassCastException =>
                  return false
              }
              if (!mv.asInstanceOf[AnyRef].equals(v.asInstanceOf[AnyRef]))
                return false
            }
            b = n
          }
        }
        !it.hasNext()
      } else {
        while (it.hasNext()) {
          var v: V = null.asInstanceOf[V]
          var e: Map.Entry[_, _] = it.next()
          var mk: Any = e.getKey()
          var mv: Any = e.getValue()
          if (mk == null || mv == null ||
              {
                v = get(mk)
                v
              } == null || !v.asInstanceOf[AnyRef].equals(mv.asInstanceOf[AnyRef]))
            return false
        }
        var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
        var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
        if ({ b = baseHead(); b } != null) {
          var k: K = null.asInstanceOf[K]
          var v: V = null.asInstanceOf[V]
          var mv: Any = null.asInstanceOf[Any]
          while ({ n = b.next; n } != null) {
            if ({ v = n.`val`; v } != null && { k = n.key; k } != null &&
                ({ mv = m.get(k); mv } == null || !mv.asInstanceOf[AnyRef].equals(v.asInstanceOf[AnyRef])))
              return false
            b = n
          }
        }
        true
      }
    } catch {
      case _: ClassCastException | _: NullPointerException => false
    }
  }

  /* ------ ConcurrentMap API methods ------ */

  override def putIfAbsent(key: K, value: V): V = {
    if (value == null)
      throw new NullPointerException()
    doPut(key, value, true)
  }

  override def remove(key: Any, value: Any): Boolean = {
    if (key == null)
      throw new NullPointerException()
    value != null && doRemove(key, value) != null
  }

  override def replace(key: K, oldValue: V, newValue: V): Boolean = {
    if (key == null || oldValue == null || newValue == null)
      throw new NullPointerException()
    while (true) {
      var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
      var v: V = null.asInstanceOf[V]
      if ({ n = findNode(key); n } == null)
        return false
      if ({ v = n.`val`; v } != null) {
        if (!oldValue.asInstanceOf[AnyRef].equals(v.asInstanceOf[AnyRef]))
          return false
        if (n.VAL.compareExchangeStrong(v.asInstanceOf[AnyRef], newValue.asInstanceOf[AnyRef]))
          return true
      }
    }
    false // unreachable
  }

  override def replace(key: K, value: V): V = {
    if (key == null || value == null)
      throw new NullPointerException()
    while (true) {
      var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
      var v: V = null.asInstanceOf[V]
      if ({ n = findNode(key); n } == null)
        return null.asInstanceOf[V]
      if ({ v = n.`val`; v } != null &&
          n.VAL.compareExchangeStrong(v.asInstanceOf[AnyRef], value.asInstanceOf[AnyRef]))
        return v
    }
    null.asInstanceOf[V] // unreachable
  }

  /* ------ SortedMap API methods ------ */

  override def comparator(): Comparator[_ >: K] = {
    _comparator
  }

  override def firstKey(): K = {
    var n: Node[K, V] = findFirst()
    if (n == null)
      throw new NoSuchElementException()
    n.key
  }

  override def lastKey(): K = {
    var n: Node[K, V] = findLast()
    if (n == null)
      throw new NoSuchElementException()
    n.key
  }

  override def subMap(
      fromKey: K,
      fromInclusive: Boolean,
      toKey: K,
      toInclusive: Boolean
  ): ConcurrentNavigableMap[K, V] = {
    if (fromKey == null || toKey == null)
      throw new NullPointerException()
    new SubMap[K, V](this, fromKey, fromInclusive, toKey, toInclusive, false)
  }

  override def headMap(toKey: K, inclusive: Boolean): ConcurrentNavigableMap[K, V] = {
    if (toKey == null)
      throw new NullPointerException()
    new SubMap[K, V](this, null.asInstanceOf[K], false, toKey, inclusive, false)
  }

  override def tailMap(fromKey: K, inclusive: Boolean): ConcurrentNavigableMap[K, V] = {
    if (fromKey == null)
      throw new NullPointerException()
    new SubMap[K, V](this, fromKey, inclusive, null.asInstanceOf[K], false, false)
  }

  override def subMap(fromKey: K, toKey: K): ConcurrentNavigableMap[K, V] = {
    subMap(fromKey, true, toKey, false)
  }

  override def headMap(toKey: K): ConcurrentNavigableMap[K, V] = {
    headMap(toKey, false)
  }

  override def tailMap(fromKey: K): ConcurrentNavigableMap[K, V] = {
    tailMap(fromKey, true)
  }

  /* ---------------- Relational operations -------------- */

  override def lowerEntry(key: K): Map.Entry[K, V] = {
    findNearEntry(key, LT, _comparator)
  }

  override def lowerKey(key: K): K = {
    var n: Node[K, V] = findNear(key, LT, _comparator)
    if (n == null) null.asInstanceOf[K] else n.key
  }

  override def floorEntry(key: K): Map.Entry[K, V] = {
    findNearEntry(key, LT | EQ, _comparator)
  }

  override def floorKey(key: K): K = {
    var n: Node[K, V] = findNear(key, LT | EQ, _comparator)
    if (n == null) null.asInstanceOf[K] else n.key
  }

  override def ceilingEntry(key: K): Map.Entry[K, V] = {
    findNearEntry(key, GT | EQ, _comparator)
  }

  override def ceilingKey(key: K): K = {
    var n: Node[K, V] = findNear(key, GT | EQ, _comparator)
    if (n == null) null.asInstanceOf[K] else n.key
  }

  override def higherEntry(key: K): Map.Entry[K, V] = {
    findNearEntry(key, GT, _comparator)
  }

  override def higherKey(key: K): K = {
    var n: Node[K, V] = findNear(key, GT, _comparator)
    if (n == null) null.asInstanceOf[K] else n.key
  }

  override def firstEntry(): Map.Entry[K, V] = {
    findFirstEntry()
  }

  override def lastEntry(): Map.Entry[K, V] = {
    findLastEntry()
  }

  override def pollFirstEntry(): Map.Entry[K, V] = {
    doRemoveFirstEntry()
  }

  override def pollLastEntry(): Map.Entry[K, V] = {
    doRemoveLastEntry()
  }

  /* ---------------- Iterators -------------- */

  private[concurrent] abstract class Iter[T] extends Iterator[T] {
    // Java fields: lastReturned, next, nextValue — `next` renamed nextNode (conflicts with next())
    protected var lastReturned: Node[K, V] = _
    protected var nextNode: Node[K, V] = _
    protected var nextValue: V = _

    // Iter() { advance(baseHead()); }
    advance(baseHead())

    def hasNext(): Boolean = nextNode != null

    private[concurrent] def advance(b: Node[K, V]): Unit = {
      var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
      var v: V = null.asInstanceOf[V]
      var bb = b
      if ({ lastReturned = bb; lastReturned } != null) {
        while ({ n = bb.next; n } != null && { v = n.`val`; v } == null)
          bb = n
      }
      nextValue = v
      nextNode = n
    }

    override def remove(): Unit = {
      var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
      var k: K = null.asInstanceOf[K]
      if ({ n = lastReturned; n } == null || { k = n.key; k } == null)
        throw new IllegalStateException()
      ConcurrentSkipListMap.this.remove(k)
      lastReturned = null
    }
  }

  private[concurrent] final class ValueIterator extends Iter[V] {
    def next(): V = {
      var v: V = null.asInstanceOf[V]
      if ({ v = nextValue; v } == null)
        throw new NoSuchElementException()
      advance(nextNode)
      v
    }
  }

  private[concurrent] final class KeyIterator extends Iter[K] {
    def next(): K = {
      var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
      if ({ n = nextNode; n } == null)
        throw new NoSuchElementException()
      var k: K = n.key
      advance(n)
      k
    }
  }

  private[concurrent] final class EntryIterator extends Iter[Map.Entry[K, V]] {
    def next(): Map.Entry[K, V] = {
      var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
      if ({ n = nextNode; n } == null)
        throw new NoSuchElementException()
      var k: K = n.key
      var v: V = nextValue
      advance(n)
      new AbstractMap.SimpleImmutableEntry[K, V](k, v)
    }
  }

  /* ---------------- View Classes -------------- */

  /*
   * View classes are static, delegating to a ConcurrentNavigableMap
   * to allow use by SubMaps, which outweighs the ugliness of
   * needing type-tests for Iterator methods.
   */

  // default Map method overrides

  override def forEach(action: BiConsumer[_ >: K, _ >: V]): Unit = {
    if (action == null) throw new NullPointerException();
    var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var v: V = null.asInstanceOf[V]
    if ({ b = baseHead(); b } != null) {
      while ({ n = b.next; n } != null) {
        if ({ v = n.`val`; v } != null)
          action.accept(n.key, v)
        b = n
      }
    }
  }

  override def replaceAll(function: BiFunction[_ >: K, _ >: V, _ <: V]): Unit = {
    if (function == null) throw new NullPointerException();
    var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var v: V = null.asInstanceOf[V]
    if ({ b = baseHead(); b } != null) {
      while ({ n = b.next; n } != null) {
        var breakInner = false
        while (!breakInner && ({ v = n.`val`; v } != null)) {
          var r: V = function.apply(n.key, v)
          if (r == null) throw new NullPointerException();
          if (n.VAL.compareExchangeStrong(v.asInstanceOf[AnyRef], r.asInstanceOf[AnyRef]))
            breakInner = true // break
        }
        b = n
      }
    }
  }

  private[concurrent] def removeEntryIf(function: Predicate[_ >: Map.Entry[K, V]]): Boolean = {
    if (function == null) throw new NullPointerException();
    var removed: Boolean = false
    var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var v: V = null.asInstanceOf[V]
    if ({ b = baseHead(); b } != null) {
      while ({ n = b.next; n } != null) {
        if ({ v = n.`val`; v } != null) {
          var k: K = n.key
          var e: Map.Entry[K, V] = new AbstractMap.SimpleImmutableEntry(k, v)
          if (function.test(e) && remove(k, v))
            removed = true
        }
        b = n
      }
    }
    removed
  }

  private[concurrent] def removeValueIf(function: Predicate[_ >: V]): Boolean = {
    if (function == null) throw new NullPointerException();
    var removed: Boolean = false
    var b: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var v: V = null.asInstanceOf[V]
    if ({ b = baseHead(); b } != null) {
      while ({ n = b.next; n } != null) {
        if ({ v = n.`val`; v } != null && function.test(v) && remove(n.key, v))
          removed = true
        b = n
      }
    }
    removed
  }

  // factory method for KeySpliterator

  private[concurrent] def keySpliterator(): KeySpliterator[K, V] = {
    var h: Index[K, V] = null.asInstanceOf[Index[K, V]]
    var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var est: Long = 0L
    VarHandle.acquireFence()
    if ({ h = head; h } == null) {
      n = null
      est = 0L
    } else {
      n = h.node
      est = getAdderCount()
    }
    new KeySpliterator[K, V](_comparator, h, n, null.asInstanceOf[K], est)
  }

  // Almost the same as keySpliterator()

  private[concurrent] def valueSpliterator(): ValueSpliterator[K, V] = {
    var h: Index[K, V] = null.asInstanceOf[Index[K, V]]
    var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var est: Long = 0L
    VarHandle.acquireFence()
    if ({ h = head; h } == null) {
      n = null
      est = 0L
    } else {
      n = h.node
      est = getAdderCount()
    }
    new ValueSpliterator[K, V](_comparator, h, n, null.asInstanceOf[K], est)
  }

  // Almost the same as keySpliterator()

  private[concurrent] def entrySpliterator(): EntrySpliterator[K, V] = {
    var h: Index[K, V] = null.asInstanceOf[Index[K, V]]
    var n: Node[K, V] = null.asInstanceOf[Node[K, V]]
    var est: Long = 0L
    VarHandle.acquireFence()
    if ({ h = head; h } == null) {
      n = null
      est = 0L
    } else {
      n = h.node
      est = getAdderCount()
    }
    new EntrySpliterator[K, V](_comparator, h, n, null.asInstanceOf[K], est)
  }
}
