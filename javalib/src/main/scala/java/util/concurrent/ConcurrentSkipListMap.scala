/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.util._
import java.util.concurrent.atomic.LongAdder
import java.util.function.{
  BiConsumer, BiFunction, Consumer, Function, Predicate
}

import scala.annotation.tailrec
import scala.language.existentials

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic.memory_order._
import scala.scalanative.libc.stdatomic.{AtomicRef, atomic_thread_fence}
import scala.scalanative.runtime.{Intrinsics, fromRawPtr}
import scala.scalanative.unsafe._

@SerialVersionUID(-8627078645895051609L)
class ConcurrentSkipListMap[K <: AnyRef, V <: AnyRef](
    private[concurrent] val ordering: Comparator[_ >: K]
) extends AbstractMap[K, V]
    with ConcurrentNavigableMap[K, V]
    with Cloneable
    with Serializable {

  import ConcurrentSkipListMap._

  def this() =
    this(null.asInstanceOf[Comparator[_ >: K]])

  def this(m: Map[_ <: K, _ <: V]) = {
    this()
    putAll(m)
  }

  def this(m: SortedMap[K, _ <: V]) = {
    this(m.comparator())
    putAll(m)
  }

  @transient private var baseHead =
    new Node[K, V](null.asInstanceOf[K], null.asInstanceOf[V], null)
  @transient private var adder = new LongAdder
  @transient private[concurrent] var headIndex =
    new Index[K, V](baseHead, null, null)

  @alwaysinline
  private def headRef: AtomicRef[Index[K, V]] =
    new AtomicRef[Index[K, V]](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "headIndex"))
    )

  @alwaysinline
  private def casHead(expect: Index[K, V], update: Index[K, V]): Boolean =
    headRef.compareExchangeStrong(expect, update)

  @alwaysinline
  private def addCount(c: Long): Unit =
    adder.add(c)

  private[concurrent] def count(): Long =
    math.max(0L, adder.sum)

  private[concurrent] def doClear(): Unit = {
    var done = false
    while (!done) {
      val h = headIndex
      val r = h.right
      if (r != null) h.casRight(r, null)
      else {
        val d = h.down
        if (d != null) casHead(h, d)
        else {
          var count = 0L
          val b = h.node
          if (b != null) {
            var n = b.next
            while (n != null) {
              var v = n.value
              if (v != null && n.casValue(v, null.asInstanceOf[V])) {
                count -= 1L
                v = null.asInstanceOf[V]
              }
              if (v == null) unlinkNode(b, n)
              n = b.next
            }
          }
          if (count != 0L) addCount(count)
          else done = true
        }
      }
    }
  }

  private def unlinkNode(b: Node[K, V], n: Node[K, V]): Unit = {
    if (b != null && n != null) {
      var p: Node[K, V] = null
      var done = false
      while (!done) {
        val f = n.next
        if (f != null && f.key == null) {
          p = f.next
          done = true
        } else {
          val marker =
            new Node[K, V](null.asInstanceOf[K], null.asInstanceOf[V], f)
          if (n.casNext(f, marker)) {
            p = f
            done = true
          }
        }
      }
      b.casNext(n, p)
    }
  }

  private[concurrent] def compareAny(a: Any, b: Any): Int =
    compare(a.asInstanceOf[K], b.asInstanceOf[K])

  private[concurrent] def compare(a: K, b: K): Int =
    if (ordering != null) ordering.compare(a, b)
    else a.asInstanceOf[Comparable[Any]].compareTo(b)

  private[concurrent] def doGet(key: K): V = {
    compare(key, key)
    val n = findNode(key)
    if (n == null) null.asInstanceOf[V] else n.value
  }

  private[concurrent] def doPut(
      key: K,
      value: V,
      onlyIfAbsent: Boolean
  ): V = {
    compare(key, key)
    while (true) {
      val h = headIndex
      var q = h
      var b: Node[K, V] = null
      var levels = 0
      var descended = false
      while (!descended) {
        var r = q.right
        var scanning = true
        while (scanning && r != null) {
          val n = r.node
          val k = if (n == null) null.asInstanceOf[K] else n.key
          val v = if (n == null) null.asInstanceOf[V] else n.value
          if (k == null || v == null) {
            q.casRight(r, r.right)
            r = q.right
          } else if (compare(key, k) > 0) {
            q = r
            r = q.right
          } else scanning = false
        }
        val d = q.down
        if (d != null) {
          levels += 1
          q = d
        } else {
          b = q.node
          descended = true
        }
      }

      if (b != null) {
        var z: Node[K, V] = null
        var inserted = false
        while (!inserted) {
          val n = b.next
          var c = 0
          if (n == null) c = -1
          else {
            val k = n.key
            if (k == null) inserted = true
            else {
              val v = n.value
              if (v == null) {
                unlinkNode(b, n)
                c = 1
              } else {
                c = compare(key, k)
                if (c > 0) b = n
                else if (c == 0 && (onlyIfAbsent || n.casValue(v, value)))
                  return v
              }
            }
          }

          if (!inserted && c < 0) {
            val p = new Node[K, V](key, value, n)
            if (b.casNext(n, p)) {
              z = p
              inserted = true
            }
          }
        }

        if (z != null) {
          val lr = ThreadLocalRandom.nextSecondarySeed()
          if ((lr & 0x3) == 0) {
            val hr = ThreadLocalRandom.nextSecondarySeed()
            var rnd = (hr.toLong << 32) | (lr.toLong & 0xffffffffL)
            var skips = levels
            var x: Index[K, V] = null
            var done = false
            while (!done) {
              x = new Index[K, V](z, x, null)
              if (rnd >= 0L) done = true
              else {
                skips -= 1
                if (skips < 0) done = true
                else rnd <<= 1
              }
            }
            if (addIndices(h, skips, x) && skips < 0 && (headIndex eq h)) {
              val hx = new Index[K, V](z, x, null)
              val nh = new Index[K, V](h.node, h, hx)
              casHead(h, nh)
            }
            if (z.value == null)
              findPredecessor(key)
          }
          addCount(1L)
          return null.asInstanceOf[V]
        }
      }
    }
    null.asInstanceOf[V]
  }

  private[concurrent] def doRemove(key: K, expectedValue: Any): V = {
    Objects.requireNonNull(key)
    var result: V = null.asInstanceOf[V]
    var done = false
    while (!done && result == null) {
      var b = findPredecessor(key)
      while (b != null && !done && result == null) {
        val n = b.next
        if (n == null) done = true
        else {
          val k = n.key
          if (k == null) b = null
          else {
            val v = n.value
            if (v == null) unlinkNode(b, n)
            else {
              val c = compare(key, k)
              if (c > 0) b = n
              else if (c < 0) done = true
              else if (expectedValue != null && !Objects.equals(
                    expectedValue,
                    v
                  )) done = true
              else if (n.casValue(v, null.asInstanceOf[V])) {
                result = v
                unlinkNode(b, n)
              }
            }
          }
        }
      }
    }
    if (result != null) {
      tryReduceLevel()
      addCount(-1L)
    }
    result
  }

  private def addIndices(
      q0: Index[K, V],
      skips0: Int,
      x: Index[K, V]
  ): Boolean = {
    if (x != null) {
      val z = x.node
      if (z != null) {
        val key = z.key
        if (key != null && q0 != null) {
          var q = q0
          var skips = skips0
          var retrying = false
          while (true) {
            val r = q.right
            var c = 0
            if (r != null) {
              val p = r.node
              val k = if (p == null) null.asInstanceOf[K] else p.key
              val v = if (p == null) null.asInstanceOf[V] else p.value
              if (k == null || v == null) {
                q.casRight(r, r.right)
              } else {
                c = compare(key, k)
                if (c > 0) q = r
                else if (c == 0) return false
              }
            } else {
              c = -1
            }

            if (c < 0) {
              val d = q.down
              if (d != null && skips > 0) {
                skips -= 1
                q = d
              } else if (d != null && !retrying && !addIndices(d, 0, x.down))
                return false
              else {
                x.right = r
                if (q.casRight(r, x)) return true
                retrying = true
              }
            }
          }
        }
      }
    }
    false
  }

  private[concurrent] def doReplace(
      key: K,
      oldValue: V,
      newValue: V
  ): Boolean = {
    compare(key, key)
    while (true) {
      val n = findNode(key)
      if (n == null) return false
      val v = n.value
      if (v == null) ()
      else if (!Objects.equals(v, oldValue)) return false
      else if (n.casValue(v, newValue)) return true
    }
    false
  }

  private[concurrent] def doReplace(key: K, value: V): V = {
    compare(key, key)
    while (true) {
      val n = findNode(key)
      if (n == null) return null.asInstanceOf[V]
      val v = n.value
      if (v == null) ()
      else if (n.casValue(v, value)) return v
    }
    null.asInstanceOf[V]
  }

  private[concurrent] def firstNode(): Node[K, V] =
    liveNext(baseHead)

  private[concurrent] def lastNode(): Node[K, V] = {
    var last: Node[K, V] = null
    var n = firstNode()
    while (n != null) {
      last = n
      n = liveNext(n)
    }
    last
  }

  private[concurrent] def findGreaterOrEqual(key: K): Node[K, V] = {
    compare(key, key)
    val pred = findPredecessor(key)
    var n = liveNext(pred)
    while (n != null && compare(n.key, key) < 0) n = liveNext(n)
    n
  }

  private[concurrent] def findGreaterThan(key: K): Node[K, V] = {
    var n = findGreaterOrEqual(key)
    if (n != null && compare(n.key, key) == 0) n = liveNext(n)
    n
  }

  private[concurrent] def findLessThan(key: K): Node[K, V] = {
    compare(key, key)
    val pred = findPredecessor(key)
    if (pred eq baseHead) null else pred
  }

  private[concurrent] def findLessOrEqual(key: K): Node[K, V] = {
    compare(key, key)
    val pred = findPredecessor(key)
    val n = liveNext(pred)
    if (n != null && compare(n.key, key) == 0) n
    else if (pred eq baseHead) null
    else pred
  }

  private def findNode(key: K): Node[K, V] = {
    var n = findGreaterOrEqual(key)
    if (n != null && compare(n.key, key) == 0 && n.value != null) n
    else null
  }

  private[concurrent] def liveNext(pred: Node[K, V]): Node[K, V] = {
    var n = pred.next
    while (n != null) {
      val k = n.key
      val v = n.value
      if (k == null) {
        pred.casNext(n, n.next)
        n = pred.next
      } else if (v != null) return n
      else {
        unlinkNode(pred, n)
        n = pred.next
      }
    }
    null
  }

  private def findPredecessor(key: K): Node[K, V] = {
    var q = headIndex
    while (true) {
      var r = q.right
      while (r != null) {
        val n = r.node
        val k = n.key
        val v = n.value
        if (k == null || v == null) {
          q.casRight(r, r.right)
          r = q.right
        } else if (compare(k, key) < 0) {
          q = r
          r = q.right
        } else r = null
      }
      if (q.down == null) {
        var b = q.node
        if ((b ne baseHead) && b.value == null)
          return findPredecessor(key)
        var n = b.next
        while (n != null) {
          val k = n.key
          val v = n.value
          if (k == null) {
            return findPredecessor(key)
          } else if (v == null) {
            unlinkNode(b, n)
            n = b.next
          } else if (compare(k, key) < 0) {
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

  private def tryReduceLevel(): Unit = {
    val h = headIndex
    if (h.right == null) {
      val d = h.down
      if (d != null && d.right == null) {
        val e = d.down
        if (e != null && e.right == null && casHead(h, d) && h.right != null)
          casHead(d, h)
      }
    }
  }

  private def requireKey(key: Any): K = {
    if (key == null) throw new NullPointerException
    key.asInstanceOf[K]
  }

  private def requireValue(value: Any): V = {
    if (value == null) throw new NullPointerException
    value.asInstanceOf[V]
  }

  private def checkKey(key: K): Unit =
    requireKey(key)

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

  private def firstNodeInView(): Node[K, V] =
    firstNode()

  private def lastNodeInView(): Node[K, V] =
    lastNode()

  private def nextNodeInView(node: Node[K, V]): Node[K, V] =
    liveNext(node)

  private def lowerNodeInView(key: K): Node[K, V] =
    findLessThan(key)

  private def floorNodeInView(key: K): Node[K, V] =
    findLessOrEqual(key)

  private def ceilingNodeInView(key: K): Node[K, V] =
    findGreaterOrEqual(key)

  private def higherNodeInView(key: K): Node[K, V] =
    findGreaterThan(key)

  private def newSubMap(
      fromKey0: K,
      fromSet0: Boolean,
      fromInclusive0: Boolean,
      toKey0: K,
      toSet0: Boolean,
      toInclusive0: Boolean
  ): ConcurrentNavigableMap[K, V] = {
    new SubMap[K, V](
      this,
      if (fromSet0) fromKey0 else null.asInstanceOf[K],
      fromInclusive0,
      if (toSet0) toKey0 else null.asInstanceOf[K],
      toInclusive0,
      isDescending = false
    )
  }

  @throws[java.io.IOException]
  private def writeObject(s: ObjectOutputStream): Unit = {
    s.defaultWriteObject()
    var b = baseHead
    while (b != null) {
      val n = b.next
      if (n == null) b = null
      else {
        val v = n.value
        if (v != null) {
          s.writeObject(n.key)
          s.writeObject(v)
        }
        b = n
      }
    }
    s.writeObject(null)
  }

  @throws[java.io.IOException]
  @throws[ClassNotFoundException]
  private def readObject(s: ObjectInputStream): Unit = {
    s.defaultReadObject()

    val preds = new Array[Index[K, V]](64)
    var bp = new Node[K, V](null.asInstanceOf[K], null.asInstanceOf[V], null)
    var h = new Index[K, V](bp, null, null)
    preds(0) = h

    var prevKey = null.asInstanceOf[K]
    var count = 0L
    var done = false
    while (!done) {
      val k = s.readObject().asInstanceOf[K]
      if (k == null) done = true
      else {
        val v = s.readObject().asInstanceOf[V]
        if (v == null) throw new NullPointerException
        if (prevKey != null && compare(prevKey, k) > 0)
          throw new IllegalStateException("out of order")
        prevKey = k

        val z = new Node[K, V](k, v, null)
        bp.next = z
        bp = z
        count += 1L

        if ((count & 3L) == 0L) {
          var m = count >>> 2
          var i = 0
          var idx: Index[K, V] = null
          while (i < preds.length && (m & 1L) != 0L) {
            idx = new Index[K, V](z, idx, null)
            val q = preds(i)
            if (q == null) {
              h = new Index[K, V](h.node, h, idx)
              preds(i) = h
            } else {
              q.right = idx
              preds(i) = idx
            }
            i += 1
            m >>>= 1
          }
        }
      }
    }

    baseHead = h.node
    adder = new LongAdder
    if (count != 0L) {
      atomic_thread_fence(memory_order_release)
      addCount(count)
    }
    headIndex = h
    atomic_thread_fence(memory_order_seq_cst)
  }

  override def size(): Int = {
    val count = this.count()
    if (count >= Int.MaxValue) Int.MaxValue else count.toInt
  }

  override def isEmpty(): Boolean =
    firstNode() == null

  override def containsKey(key: Any): Boolean = {
    val k = requireKey(key)
    doGet(k) != null
  }

  override def containsValue(value: Any): Boolean = {
    requireValue(value)
    var n = firstNode()
    while (n != null) {
      val v = n.value
      if (v != null && Objects.equals(v, value)) return true
      n = liveNext(n)
    }
    false
  }

  override def get(key: Any): V = {
    val k = requireKey(key)
    doGet(k)
  }

  override def getOrDefault(key: Any, defaultValue: V): V = {
    val value = get(key)
    if (value == null) defaultValue else value
  }

  override def put(key: K, value: V): V = {
    checkKey(key)
    requireValue(value)
    doPut(key, value, onlyIfAbsent = false)
  }

  override def putAll(m: Map[_ <: K, _ <: V]): Unit = {
    Objects.requireNonNull(m)
    val it = m.entrySet().iterator()
    while (it.hasNext()) {
      val e = it.next()
      checkKey(e.getKey())
      requireValue(e.getValue())
    }
    val it2 = m.entrySet().iterator()
    while (it2.hasNext()) {
      val e = it2.next()
      doPut(e.getKey(), e.getValue(), onlyIfAbsent = false)
    }
  }

  override def remove(key: Any): V = {
    val k = requireKey(key)
    doRemove(k, null)
  }

  override def clear(): Unit = {
    doClear()
  }

  override def putIfAbsent(key: K, value: V): V = {
    checkKey(key)
    requireValue(value)
    doPut(key, value, onlyIfAbsent = true)
  }

  override def remove(key: Any, value: Any): Boolean = {
    val k = requireKey(key)
    value != null && doRemove(k, value) != null
  }

  override def replace(key: K, oldValue: V, newValue: V): Boolean = {
    checkKey(key)
    requireValue(oldValue)
    requireValue(newValue)
    doReplace(key, oldValue, newValue)
  }

  override def replace(key: K, value: V): V = {
    checkKey(key)
    requireValue(value)
    doReplace(key, value)
  }

  override def computeIfAbsent(
      key: K,
      mappingFunction: Function[_ >: K, _ <: V]
  ): V = {
    checkKey(key)
    Objects.requireNonNull(mappingFunction)
    var current = doGet(key)
    if (current != null) current
    else {
      val newValue = mappingFunction.apply(key)
      if (newValue == null) null.asInstanceOf[V]
      else {
        current = doPut(key, newValue, onlyIfAbsent = true)
        if (current == null) newValue else current
      }
    }
  }

  override def computeIfPresent(
      key: K,
      remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]
  ): V = {
    checkKey(key)
    Objects.requireNonNull(remappingFunction)
    while (true) {
      val oldValue = doGet(key)
      if (oldValue == null) return null.asInstanceOf[V]
      val newValue = remappingFunction.apply(key, oldValue)
      if (newValue == null) {
        if (doRemove(key, oldValue) != null) return null.asInstanceOf[V]
      } else if (doReplace(key, oldValue, newValue)) return newValue
    }
    null.asInstanceOf[V]
  }

  override def compute(
      key: K,
      remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]
  ): V = {
    checkKey(key)
    Objects.requireNonNull(remappingFunction)
    while (true) {
      val oldValue = doGet(key)
      val newValue = remappingFunction.apply(key, oldValue)
      if (oldValue == null) {
        if (newValue == null) return null.asInstanceOf[V]
        if (doPut(key, newValue, onlyIfAbsent = true) == null)
          return newValue
      } else if (newValue == null) {
        if (doRemove(key, oldValue) != null) return null.asInstanceOf[V]
      } else if (doReplace(key, oldValue, newValue)) return newValue
    }
    null.asInstanceOf[V]
  }

  override def merge(
      key: K,
      value: V,
      remappingFunction: BiFunction[_ >: V, _ >: V, _ <: V]
  ): V = {
    checkKey(key)
    requireValue(value)
    Objects.requireNonNull(remappingFunction)
    while (true) {
      val oldValue = doGet(key)
      if (oldValue == null) {
        if (doPut(key, value, onlyIfAbsent = true) == null) return value
      } else {
        val newValue = remappingFunction.apply(oldValue, value)
        if (newValue == null) {
          if (doRemove(key, oldValue) != null) return null.asInstanceOf[V]
        } else if (doReplace(key, oldValue, newValue)) return newValue
      }
    }
    null.asInstanceOf[V]
  }

  override def replaceAll(
      function: BiFunction[_ >: K, _ >: V, _ <: V]
  ): Unit = {
    Objects.requireNonNull(function)
    var n = firstNodeInView()
    while (n != null) {
      var done = false
      while (!done) {
        val v = n.value
        if (v == null) done = true
        else {
          val newValue = function.apply(n.key, v)
          if (newValue == null) throw new NullPointerException
          done = n.casValue(v, newValue)
        }
      }
      n = nextNodeInView(n)
    }
  }

  override def forEach(action: BiConsumer[_ >: K, _ >: V]): Unit = {
    Objects.requireNonNull(action)
    var n = firstNodeInView()
    while (n != null) {
      val v = n.value
      if (v != null) action.accept(n.key, v)
      n = nextNodeInView(n)
    }
  }

  override def entrySet(): Set[Map.Entry[K, V]] =
    new EntrySet[K, V](
      this,
      () =>
        new LiveEntryIterator[K, V](
          () => firstNodeInView(),
          n => nextNodeInView(n),
          k => { remove(k); () }
        ),
      () => entrySpliterator(),
      () => descendingMap().sequencedEntrySet()
    )

  override def keySet(): NavigableSet[K] =
    navigableKeySet()

  override def navigableKeySet(): NavigableSet[K] =
    new KeySet[K, V](
      this,
      () =>
        new LiveKeyIterator[K, V](
          () => firstNodeInView(),
          n => nextNodeInView(n),
          k => { remove(k); () },
          () => comparator()
        ),
      () => keySpliterator()
    )

  override def descendingKeySet(): NavigableSet[K] =
    descendingMap().navigableKeySet()

  override def values(): Collection[V] =
    new Values[K, V](
      this,
      () =>
        new LiveValueIterator[K, V](
          () => firstNodeInView(),
          n => nextNodeInView(n),
          k => { remove(k); () }
        ),
      () =>
        new LiveEntryIterator[K, V](
          () => firstNodeInView(),
          n => nextNodeInView(n),
          k => { remove(k); () }
        ),
      () => valueSpliterator(),
      () => descendingMap().sequencedValues()
    )

  override def comparator(): Comparator[_ >: K] =
    ordering

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
      if (value != null && doRemove(n.key, value) != null)
        return immutableEntry(n.key, value)
    }
    null
  }

  override def pollLastEntry(): Map.Entry[K, V] = {
    while (true) {
      val n = lastNodeInView()
      if (n == null) return null
      val value = n.value
      if (value != null && doRemove(n.key, value) != null)
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
    new SubMap[K, V](
      this,
      null.asInstanceOf[K],
      loInclusive = false,
      null.asInstanceOf[K],
      hiInclusive = false,
      isDescending = true
    )

  override def reversed(): ConcurrentNavigableMap[K, V] =
    descendingMap()

  override def putFirst(key: K, value: V): V =
    throw new UnsupportedOperationException

  override def putLast(key: K, value: V): V =
    throw new UnsupportedOperationException

  override def sequencedKeySet(): SequencedSet[K] =
    navigableKeySet()

  override def sequencedValues(): SequencedCollection[V] =
    values().asInstanceOf[SequencedCollection[V]]

  override def sequencedEntrySet(): SequencedSet[Map.Entry[K, V]] =
    entrySet().asInstanceOf[SequencedSet[Map.Entry[K, V]]]

  private def keySpliterator(): Spliterator[K] = {
    atomic_thread_fence(memory_order_acquire)
    val h = headIndex
    new KeySpliterator[K, V](ordering, h, h.node, null.asInstanceOf[K], count())
  }

  private def valueSpliterator(): Spliterator[V] = {
    atomic_thread_fence(memory_order_acquire)
    val h = headIndex
    new ValueSpliterator[K, V](
      ordering,
      h,
      h.node,
      null.asInstanceOf[K],
      count()
    )
  }

  private def entrySpliterator(): Spliterator[Map.Entry[K, V]] = {
    atomic_thread_fence(memory_order_acquire)
    val h = headIndex
    new EntrySpliterator[K, V](
      ordering,
      h,
      h.node,
      null.asInstanceOf[K],
      count()
    )
  }

  override def clone(): ConcurrentSkipListMap[K, V] = {
    val cloned = new ConcurrentSkipListMap[K, V](ordering)
    var n = firstNodeInView()
    while (n != null) {
      val v = n.value
      if (v != null)
        cloned.put(n.key, v)
      n = nextNodeInView(n)
    }
    cloned
  }
}

private object ConcurrentSkipListMap {
  private[concurrent] final class Node[K <: AnyRef, V <: AnyRef](
      val key: K,
      private[concurrent] var value: V,
      private[concurrent] var next: Node[K, V]
  ) {
    @alwaysinline
    private def valueRef: AtomicRef[V] =
      new AtomicRef[V](
        fromRawPtr(Intrinsics.classFieldRawPtr(this, "value"))
      )

    @alwaysinline
    private def nextRef: AtomicRef[Node[K, V]] =
      new AtomicRef[Node[K, V]](
        fromRawPtr(Intrinsics.classFieldRawPtr(this, "next"))
      )

    @alwaysinline
    def casValue(expect: V, update: V): Boolean =
      valueRef.compareExchangeStrong(expect, update)

    @alwaysinline
    def casNext(expect: Node[K, V], update: Node[K, V]): Boolean =
      nextRef.compareExchangeStrong(expect, update)
  }

  private[concurrent] final class Index[K <: AnyRef, V <: AnyRef](
      val node: Node[K, V],
      val down: Index[K, V],
      private[concurrent] var right: Index[K, V]
  ) {
    @alwaysinline
    private def rightRef: AtomicRef[Index[K, V]] =
      new AtomicRef[Index[K, V]](
        fromRawPtr(Intrinsics.classFieldRawPtr(this, "right"))
      )

    @alwaysinline
    def casRight(expect: Index[K, V], update: Index[K, V]): Boolean =
      rightRef.compareExchangeStrong(expect, update)
  }

  private def compare[K <: AnyRef](
      comparator: Comparator[_ >: K],
      a: K,
      b: K
  ): Int =
    if (comparator != null) comparator.compare(a, b)
    else a.asInstanceOf[Comparable[Any]].compareTo(b)

  private abstract class CSLMSpliterator[K <: AnyRef, V <: AnyRef, A](
      val comparator: Comparator[_ >: K],
      var row: Index[K, V],
      var current: Node[K, V],
      val fence: K,
      var est: Long
  ) extends Spliterator[A] {
    override final def estimateSize(): Long = est
  }

  private final class KeySpliterator[K <: AnyRef, V <: AnyRef](
      comparator: Comparator[_ >: K],
      row: Index[K, V],
      origin: Node[K, V],
      fence: K,
      est: Long
  ) extends CSLMSpliterator[K, V, K](comparator, row, origin, fence, est) {

    override def trySplit(): Spliterator[K] = {
      val e = current
      if (e != null && e.key != null) {
        var q = row
        while (q != null) {
          val s = q.right
          if (s != null) {
            val b = s.node
            val n = if (b == null) null else b.next
            if (n != null && n.value != null && n.key != null &&
                compare(comparator, n.key, e.key) > 0 &&
                (fence == null || compare(comparator, n.key, fence) < 0)) {
              current = n
              val r = q.down
              row = if (s.right != null) s else s.down
              this.est = this.est - (this.est >>> 2)
              return new KeySpliterator[K, V](
                comparator,
                r,
                e,
                n.key,
                this.est
              )
            }
          }
          q = q.down
          row = q
        }
      }
      null
    }

    override def forEachRemaining(action: Consumer[_ >: K]): Unit = {
      Objects.requireNonNull(action)
      var e = current
      current = null
      while (e != null) {
        val k = e.key
        if (k != null && fence != null && compare(comparator, fence, k) <= 0)
          return
        if (e.value != null)
          action.accept(k)
        e = e.next
      }
    }

    override def tryAdvance(action: Consumer[_ >: K]): Boolean = {
      Objects.requireNonNull(action)
      var e = current
      while (e != null) {
        val k = e.key
        if (k != null && fence != null && compare(comparator, fence, k) <= 0) {
          e = null
        } else if (e.value != null) {
          current = e.next
          action.accept(k)
          return true
        } else e = e.next
      }
      current = e
      false
    }

    override def characteristics(): Int =
      Spliterator.DISTINCT | Spliterator.SORTED |
        Spliterator.ORDERED | Spliterator.CONCURRENT |
        Spliterator.NONNULL

    override def getComparator(): Comparator[_ >: K] =
      comparator
  }

  private final class ValueSpliterator[K <: AnyRef, V <: AnyRef](
      comparator: Comparator[_ >: K],
      row: Index[K, V],
      origin: Node[K, V],
      fence: K,
      est: Long
  ) extends CSLMSpliterator[K, V, V](comparator, row, origin, fence, est) {

    override def trySplit(): Spliterator[V] = {
      val e = current
      if (e != null && e.key != null) {
        var q = row
        while (q != null) {
          val s = q.right
          if (s != null) {
            val b = s.node
            val n = if (b == null) null else b.next
            if (n != null && n.value != null && n.key != null &&
                compare(comparator, n.key, e.key) > 0 &&
                (fence == null || compare(comparator, n.key, fence) < 0)) {
              current = n
              val r = q.down
              row = if (s.right != null) s else s.down
              this.est = this.est - (this.est >>> 2)
              return new ValueSpliterator[K, V](
                comparator,
                r,
                e,
                n.key,
                this.est
              )
            }
          }
          q = q.down
          row = q
        }
      }
      null
    }

    override def forEachRemaining(action: Consumer[_ >: V]): Unit = {
      Objects.requireNonNull(action)
      var e = current
      current = null
      while (e != null) {
        val k = e.key
        if (k != null && fence != null && compare(comparator, fence, k) <= 0)
          return
        val v = e.value
        if (v != null)
          action.accept(v)
        e = e.next
      }
    }

    override def tryAdvance(action: Consumer[_ >: V]): Boolean = {
      Objects.requireNonNull(action)
      var e = current
      while (e != null) {
        val k = e.key
        if (k != null && fence != null && compare(comparator, fence, k) <= 0) {
          e = null
        } else {
          val v = e.value
          if (v != null) {
            current = e.next
            action.accept(v)
            return true
          }
          e = e.next
        }
      }
      current = e
      false
    }

    override def characteristics(): Int =
      Spliterator.CONCURRENT | Spliterator.ORDERED | Spliterator.NONNULL
  }

  private final class EntrySpliterator[K <: AnyRef, V <: AnyRef](
      comparator: Comparator[_ >: K],
      row: Index[K, V],
      origin: Node[K, V],
      fence: K,
      est: Long
  ) extends CSLMSpliterator[K, V, Map.Entry[K, V]](
        comparator,
        row,
        origin,
        fence,
        est
      ) {

    override def trySplit(): Spliterator[Map.Entry[K, V]] = {
      val e = current
      if (e != null && e.key != null) {
        var q = row
        while (q != null) {
          val s = q.right
          if (s != null) {
            val b = s.node
            val n = if (b == null) null else b.next
            if (n != null && n.value != null && n.key != null &&
                compare(comparator, n.key, e.key) > 0 &&
                (fence == null || compare(comparator, n.key, fence) < 0)) {
              current = n
              val r = q.down
              row = if (s.right != null) s else s.down
              this.est = this.est - (this.est >>> 2)
              return new EntrySpliterator[K, V](
                comparator,
                r,
                e,
                n.key,
                this.est
              )
            }
          }
          q = q.down
          row = q
        }
      }
      null
    }

    override def forEachRemaining(
        action: Consumer[_ >: Map.Entry[K, V]]
    ): Unit = {
      Objects.requireNonNull(action)
      var e = current
      current = null
      while (e != null) {
        val k = e.key
        if (k != null && fence != null && compare(comparator, fence, k) <= 0)
          return
        val v = e.value
        if (v != null)
          action.accept(new AbstractMap.SimpleImmutableEntry[K, V](k, v))
        e = e.next
      }
    }

    override def tryAdvance(
        action: Consumer[_ >: Map.Entry[K, V]]
    ): Boolean = {
      Objects.requireNonNull(action)
      var e = current
      while (e != null) {
        val k = e.key
        if (k != null && fence != null && compare(comparator, fence, k) <= 0) {
          e = null
        } else {
          val v = e.value
          if (v != null) {
            current = e.next
            action.accept(new AbstractMap.SimpleImmutableEntry[K, V](k, v))
            return true
          }
          e = e.next
        }
      }
      current = e
      false
    }

    override def characteristics(): Int =
      Spliterator.DISTINCT | Spliterator.SORTED |
        Spliterator.ORDERED | Spliterator.CONCURRENT |
        Spliterator.NONNULL

    override def getComparator(): Comparator[_ >: Map.Entry[K, V]] =
      if (comparator != null)
        new Comparator[Map.Entry[K, V]] {
          override def compare(
              e1: Map.Entry[K, V],
              e2: Map.Entry[K, V]
          ): Int =
            comparator.compare(e1.getKey(), e2.getKey())
        }
      else
        new Comparator[Map.Entry[K, V]] {
          override def compare(
              e1: Map.Entry[K, V],
              e2: Map.Entry[K, V]
          ): Int =
            e1.getKey().asInstanceOf[Comparable[Any]].compareTo(e2.getKey())
        }
  }

  private final class SubMap[K <: AnyRef, V <: AnyRef](
      private val m: ConcurrentSkipListMap[K, V],
      private val lo: K,
      private val loInclusive: Boolean,
      private val hi: K,
      private val hiInclusive: Boolean,
      private val isDescending: Boolean
  ) extends AbstractMap[K, V]
      with ConcurrentNavigableMap[K, V]
      with Serializable {

    if (lo != null && hi != null && m.compare(lo, hi) > 0)
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
      lo != null && {
        val c = m.compareAny(key, lo)
        c < 0 || (c == 0 && !loInclusive)
      }

    private def tooHigh(key: Any): Boolean =
      hi != null && {
        val c = m.compareAny(key, hi)
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
        if (lo == null) m.firstNode()
        else if (loInclusive) m.findGreaterOrEqual(lo)
        else m.findGreaterThan(lo)
      if (n != null && !tooHigh(n.key)) n else null
    }

    private def highestNodeAscending(): Node[K, V] = {
      val n =
        if (hi == null) m.lastNode()
        else if (hiInclusive) m.findLessOrEqual(hi)
        else m.findLessThan(hi)
      if (n != null && !tooLow(n.key)) n else null
    }

    private def firstNodeInView(): Node[K, V] =
      if (isDescending) highestNodeAscending()
      else lowestNodeAscending()

    private def lastNodeInView(): Node[K, V] =
      if (isDescending) lowestNodeAscending()
      else highestNodeAscending()

    private def nextNodeInView(node: Node[K, V]): Node[K, V] = {
      val n =
        if (isDescending) m.findLessThan(node.key)
        else m.liveNext(node)
      if (n != null && inBounds(n.key)) n else null
    }

    private def ascLowerNode(key: K): Node[K, V] =
      if (tooLow(key)) null
      else if (tooHigh(key)) highestNodeAscending()
      else {
        val n = m.findLessThan(key)
        if (n != null && inBounds(n.key)) n else null
      }

    private def ascFloorNode(key: K): Node[K, V] =
      if (tooLow(key)) null
      else if (tooHigh(key)) highestNodeAscending()
      else {
        val n = m.findLessOrEqual(key)
        if (n != null && inBounds(n.key)) n else null
      }

    private def ascCeilingNode(key: K): Node[K, V] =
      if (tooLow(key)) lowestNodeAscending()
      else if (tooHigh(key)) null
      else {
        val n = m.findGreaterOrEqual(key)
        if (n != null && inBounds(n.key)) n else null
      }

    private def ascHigherNode(key: K): Node[K, V] =
      if (tooLow(key)) lowestNodeAscending()
      else if (tooHigh(key)) null
      else {
        val n = m.findGreaterThan(key)
        if (n != null && inBounds(n.key)) n else null
      }

    private def lowerNodeInView(key: K): Node[K, V] =
      if (isDescending) ascHigherNode(key) else ascLowerNode(key)

    private def floorNodeInView(key: K): Node[K, V] =
      if (isDescending) ascCeilingNode(key) else ascFloorNode(key)

    private def ceilingNodeInView(key: K): Node[K, V] =
      if (isDescending) ascFloorNode(key) else ascCeilingNode(key)

    private def higherNodeInView(key: K): Node[K, V] =
      if (isDescending) ascLowerNode(key) else ascHigherNode(key)

    private def newSubMap(
        fromKey0: K,
        fromSet0: Boolean,
        fromInclusive0: Boolean,
        toKey0: K,
        toSet0: Boolean,
        toInclusive0: Boolean
    ): ConcurrentNavigableMap[K, V] = {
      var fromKey = fromKey0
      var fromSet = fromSet0
      var fromInclusive = fromInclusive0
      var toKey = toKey0
      var toSet = toSet0
      var toInclusive = toInclusive0

      if (isDescending) {
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

      if (lo != null) {
        if (!fromSet) {
          fromKey = lo
          fromSet = true
          fromInclusive = loInclusive
        } else {
          val c = m.compare(fromKey, lo)
          if (c < 0 || (c == 0 && !loInclusive && fromInclusive))
            throw new IllegalArgumentException("key out of range")
        }
      }

      if (hi != null) {
        if (!toSet) {
          toKey = hi
          toSet = true
          toInclusive = hiInclusive
        } else {
          val c = m.compare(toKey, hi)
          if (c > 0 || (c == 0 && !hiInclusive && toInclusive))
            throw new IllegalArgumentException("key out of range")
        }
      }

      new SubMap[K, V](
        m,
        if (fromSet) fromKey else null.asInstanceOf[K],
        fromInclusive,
        if (toSet) toKey else null.asInstanceOf[K],
        toInclusive,
        isDescending
      )
    }

    override def size(): Int = {
      var count = 0L
      var n = lowestNodeAscending()
      while (n != null) {
        if (n.value != null) count += 1L
        n = m.liveNext(n)
        if (n != null && tooHigh(n.key)) n = null
      }
      if (count >= Int.MaxValue) Int.MaxValue else count.toInt
    }

    override def isEmpty(): Boolean =
      lowestNodeAscending() == null

    override def containsKey(key: Any): Boolean = {
      val k = requireKey(key)
      inBounds(k) && m.doGet(k) != null
    }

    override def containsValue(value: Any): Boolean = {
      requireValue(value)
      var n = lowestNodeAscending()
      while (n != null) {
        val v = n.value
        if (v != null && Objects.equals(v, value)) return true
        n = m.liveNext(n)
        if (n != null && tooHigh(n.key)) n = null
      }
      false
    }

    override def get(key: Any): V = {
      val k = requireKey(key)
      if (!inBounds(k)) null.asInstanceOf[V]
      else m.doGet(k)
    }

    override def getOrDefault(key: Any, defaultValue: V): V = {
      val value = get(key)
      if (value == null) defaultValue else value
    }

    override def put(key: K, value: V): V = {
      checkKeyBounds(key)
      requireValue(value)
      m.doPut(key, value, onlyIfAbsent = false)
    }

    override def putAll(map: Map[_ <: K, _ <: V]): Unit = {
      Objects.requireNonNull(map)
      val it = map.entrySet().iterator()
      while (it.hasNext()) {
        val e = it.next()
        checkKeyBounds(e.getKey())
        requireValue(e.getValue())
      }
      val it2 = map.entrySet().iterator()
      while (it2.hasNext()) {
        val e = it2.next()
        m.doPut(e.getKey(), e.getValue(), onlyIfAbsent = false)
      }
    }

    override def remove(key: Any): V = {
      val k = requireKey(key)
      if (!inBounds(k)) null.asInstanceOf[V]
      else m.doRemove(k, null)
    }

    override def clear(): Unit = {
      var n = firstNodeInView()
      while (n != null) {
        val next = nextNodeInView(n)
        if (n.value != null)
          m.doRemove(n.key, null)
        n = next
      }
    }

    override def putIfAbsent(key: K, value: V): V = {
      checkKeyBounds(key)
      requireValue(value)
      m.doPut(key, value, onlyIfAbsent = true)
    }

    override def remove(key: Any, value: Any): Boolean = {
      val k = requireKey(key)
      value != null && inBounds(k) && m.doRemove(k, value) != null
    }

    override def replace(key: K, oldValue: V, newValue: V): Boolean = {
      checkKeyBounds(key)
      requireValue(oldValue)
      requireValue(newValue)
      m.doReplace(key, oldValue, newValue)
    }

    override def replace(key: K, value: V): V = {
      checkKeyBounds(key)
      requireValue(value)
      m.doReplace(key, value)
    }

    override def computeIfAbsent(
        key: K,
        mappingFunction: Function[_ >: K, _ <: V]
    ): V = {
      checkKeyBounds(key)
      Objects.requireNonNull(mappingFunction)
      var current = m.doGet(key)
      if (current != null) current
      else {
        val newValue = mappingFunction.apply(key)
        if (newValue == null) null.asInstanceOf[V]
        else {
          current = m.doPut(key, newValue, onlyIfAbsent = true)
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
        val oldValue = m.doGet(key)
        if (oldValue == null) return null.asInstanceOf[V]
        val newValue = remappingFunction.apply(key, oldValue)
        if (newValue == null) {
          if (m.doRemove(key, oldValue) != null) return null.asInstanceOf[V]
        } else if (m.doReplace(key, oldValue, newValue)) return newValue
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
        val oldValue = m.doGet(key)
        val newValue = remappingFunction.apply(key, oldValue)
        if (oldValue == null) {
          if (newValue == null) return null.asInstanceOf[V]
          if (m.doPut(key, newValue, onlyIfAbsent = true) == null)
            return newValue
        } else if (newValue == null) {
          if (m.doRemove(key, oldValue) != null) return null.asInstanceOf[V]
        } else if (m.doReplace(key, oldValue, newValue)) return newValue
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
        val oldValue = m.doGet(key)
        if (oldValue == null) {
          if (m.doPut(key, value, onlyIfAbsent = true) == null) return value
        } else {
          val newValue = remappingFunction.apply(oldValue, value)
          if (newValue == null) {
            if (m.doRemove(key, oldValue) != null) return null.asInstanceOf[V]
          } else if (m.doReplace(key, oldValue, newValue)) return newValue
        }
      }
      null.asInstanceOf[V]
    }

    override def replaceAll(
        function: BiFunction[_ >: K, _ >: V, _ <: V]
    ): Unit = {
      Objects.requireNonNull(function)
      var n = firstNodeInView()
      while (n != null) {
        var done = false
        while (!done) {
          val v = n.value
          if (v == null) done = true
          else {
            val newValue = function.apply(n.key, v)
            if (newValue == null) throw new NullPointerException
            done = n.casValue(v, newValue)
          }
        }
        n = nextNodeInView(n)
      }
    }

    override def forEach(action: BiConsumer[_ >: K, _ >: V]): Unit = {
      Objects.requireNonNull(action)
      var n = firstNodeInView()
      while (n != null) {
        val v = n.value
        if (v != null) action.accept(n.key, v)
        n = nextNodeInView(n)
      }
    }

    override def entrySet(): Set[Map.Entry[K, V]] =
      new EntrySet[K, V](
        this,
        () =>
          new LiveEntryIterator[K, V](
            () => firstNodeInView(),
            n => nextNodeInView(n),
            k => { remove(k); () }
          ),
        () =>
          new LiveEntryIterator[K, V](
            () => firstNodeInView(),
            n => nextNodeInView(n),
            k => { remove(k); () }
          ),
        () => descendingMap().sequencedEntrySet()
      )

    override def keySet(): NavigableSet[K] =
      navigableKeySet()

    override def navigableKeySet(): NavigableSet[K] =
      new KeySet[K, V](
        this,
        () =>
          new LiveKeyIterator[K, V](
            () => firstNodeInView(),
            n => nextNodeInView(n),
            k => { remove(k); () },
            () => comparator()
          ),
        () =>
          new LiveKeyIterator[K, V](
            () => firstNodeInView(),
            n => nextNodeInView(n),
            k => { remove(k); () },
            () => comparator()
          )
      )

    override def descendingKeySet(): NavigableSet[K] =
      descendingMap().navigableKeySet()

    override def values(): Collection[V] =
      new Values[K, V](
        this,
        () =>
          new LiveValueIterator[K, V](
            () => firstNodeInView(),
            n => nextNodeInView(n),
            k => { remove(k); () }
          ),
        () =>
          new LiveEntryIterator[K, V](
            () => firstNodeInView(),
            n => nextNodeInView(n),
            k => { remove(k); () }
          ),
        () =>
          new LiveValueIterator[K, V](
            () => firstNodeInView(),
            n => nextNodeInView(n),
            k => { remove(k); () }
          ),
        () => descendingMap().sequencedValues()
      )

    override def comparator(): Comparator[_ >: K] =
      if (!isDescending) m.ordering
      else
        Collections.reverseOrder(m.ordering.asInstanceOf[Comparator[K]])

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
        if (value != null && m.doRemove(n.key, value) != null)
          return immutableEntry(n.key, value)
      }
      null
    }

    override def pollLastEntry(): Map.Entry[K, V] = {
      while (true) {
        val n = lastNodeInView()
        if (n == null) return null
        val value = n.value
        if (value != null && m.doRemove(n.key, value) != null)
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
      new SubMap[K, V](
        m,
        lo,
        loInclusive,
        hi,
        hiInclusive,
        !isDescending
      )

    override def reversed(): ConcurrentNavigableMap[K, V] =
      descendingMap()

    override def putFirst(key: K, value: V): V =
      throw new UnsupportedOperationException

    override def putLast(key: K, value: V): V =
      throw new UnsupportedOperationException

    override def sequencedKeySet(): SequencedSet[K] =
      navigableKeySet()

    override def sequencedValues(): SequencedCollection[V] =
      values().asInstanceOf[SequencedCollection[V]]

    override def sequencedEntrySet(): SequencedSet[Map.Entry[K, V]] =
      entrySet().asInstanceOf[SequencedSet[Map.Entry[K, V]]]
  }

  private abstract class LiveIterator[K <: AnyRef, V <: AnyRef, A](
      firstNode: () => Node[K, V],
      nextNodeOf: Node[K, V] => Node[K, V],
      removeKey: K => Unit
  ) extends Iterator[A]
      with Spliterator[A] {
    private var nextNode: Node[K, V] = _
    private var nextValue: V = _
    private var lastReturned: Node[K, V] = _
    protected var currentValue: V = _

    atomic_thread_fence(memory_order_acquire)
    nextNode = firstNode()
    skipDeleted()

    private def skipDeleted(): Unit = {
      while (nextNode != null) {
        nextValue = nextNode.value
        if (nextValue != null) return
        nextNode = nextNodeOf(nextNode)
      }
    }

    override final def hasNext(): Boolean = nextNode != null

    protected final def nextLiveNode(): Node[K, V] = {
      val n = nextNode
      if (n == null)
        throw new NoSuchElementException
      currentValue = nextValue
      lastReturned = n
      nextNode = nextNodeOf(n)
      skipDeleted()
      n
    }

    override final def remove(): Unit = {
      val n = lastReturned
      if (n == null) throw new IllegalStateException
      removeKey(n.key)
      lastReturned = null
    }

    override final def trySplit(): Spliterator[A] = null

    override final def tryAdvance(action: Consumer[_ >: A]): Boolean = {
      Objects.requireNonNull(action)
      if (!hasNext()) false
      else {
        action.accept(next())
        true
      }
    }

    override final def forEachRemaining(action: Consumer[_ >: A]): Unit = {
      Objects.requireNonNull(action)
      while (hasNext())
        action.accept(next())
    }

    override final def estimateSize(): Long = java.lang.Long.MAX_VALUE
  }

  private final class LiveKeyIterator[K <: AnyRef, V <: AnyRef](
      firstNode: () => Node[K, V],
      nextNodeOf: Node[K, V] => Node[K, V],
      removeKey: K => Unit,
      comparatorFactory: () => Comparator[_ >: K]
  ) extends LiveIterator[K, V, K](firstNode, nextNodeOf, removeKey) {
    override def next(): K = nextLiveNode().key
    override def characteristics(): Int =
      Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SORTED
    override def getComparator(): Comparator[_ >: K] =
      comparatorFactory()
  }

  private final class LiveValueIterator[K <: AnyRef, V <: AnyRef](
      firstNode: () => Node[K, V],
      nextNodeOf: Node[K, V] => Node[K, V],
      removeKey: K => Unit
  ) extends LiveIterator[K, V, V](firstNode, nextNodeOf, removeKey) {
    override def next(): V = {
      nextLiveNode()
      currentValue
    }
    override def characteristics(): Int = 0
  }

  private final class LiveEntryIterator[K <: AnyRef, V <: AnyRef](
      firstNode: () => Node[K, V],
      nextNodeOf: Node[K, V] => Node[K, V],
      removeKey: K => Unit
  ) extends LiveIterator[K, V, Map.Entry[K, V]](
        firstNode,
        nextNodeOf,
        removeKey
      ) {
    override def next(): Map.Entry[K, V] = {
      val n = nextLiveNode()
      new AbstractMap.SimpleImmutableEntry[K, V](n.key, currentValue)
    }
    override def characteristics(): Int =
      Spliterator.DISTINCT
  }

  private final class EntrySet[K <: AnyRef, V <: AnyRef](
      map: ConcurrentNavigableMap[K, V],
      iteratorFactory: () => Iterator[Map.Entry[K, V]],
      spliteratorFactory: () => Spliterator[Map.Entry[K, V]],
      reversedFactory: () => SequencedSet[Map.Entry[K, V]]
  ) extends AbstractSet[Map.Entry[K, V]]
      with SequencedSet[Map.Entry[K, V]]
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
      iteratorFactory()

    override def spliterator(): Spliterator[Map.Entry[K, V]] =
      spliteratorFactory()

    override def removeIf(filter: Predicate[_ >: Map.Entry[K, V]]): Boolean = {
      Objects.requireNonNull(filter)
      val it = iteratorFactory()
      var removed = false
      while (it.hasNext()) {
        val e = it.next()
        if (filter.test(e) && map.remove(e.getKey(), e.getValue()))
          removed = true
      }
      removed
    }

    override def reversed(): SequencedSet[Map.Entry[K, V]] =
      reversedFactory()
  }

  private final class Values[K <: AnyRef, V <: AnyRef](
      map: ConcurrentNavigableMap[K, V],
      iteratorFactory: () => Iterator[V],
      entryIteratorFactory: () => Iterator[Map.Entry[K, V]],
      spliteratorFactory: () => Spliterator[V],
      reversedFactory: () => SequencedCollection[V]
  ) extends AbstractCollection[V]
      with SequencedCollection[V]
      with Serializable {

    override def size(): Int = map.size()

    override def isEmpty(): Boolean = map.isEmpty()

    override def clear(): Unit = map.clear()

    override def contains(o: Any): Boolean = map.containsValue(o)

    override def iterator(): Iterator[V] =
      iteratorFactory()

    override def spliterator(): Spliterator[V] =
      spliteratorFactory()

    override def removeIf(filter: Predicate[_ >: V]): Boolean = {
      Objects.requireNonNull(filter)
      val it = entryIteratorFactory()
      var removed = false
      while (it.hasNext()) {
        val e = it.next()
        val v = e.getValue()
        if (filter.test(v) && map.remove(e.getKey(), v))
          removed = true
      }
      removed
    }

    override def reversed(): SequencedCollection[V] =
      reversedFactory()
  }

  private final class KeySet[K <: AnyRef, V <: AnyRef](
      map: ConcurrentNavigableMap[K, V],
      iteratorFactory: () => Iterator[K],
      spliteratorFactory: () => Spliterator[K]
  ) extends AbstractSet[K]
      with NavigableSet[K]
      with Serializable {

    override def size(): Int = map.size()

    override def isEmpty(): Boolean = map.isEmpty()

    override def clear(): Unit = map.clear()

    override def contains(o: Any): Boolean = map.containsKey(o)

    override def remove(o: Any): Boolean = map.remove(o) != null

    override def add(e: K): Boolean =
      throw new UnsupportedOperationException

    override def iterator(): Iterator[K] =
      iteratorFactory()

    override def spliterator(): Spliterator[K] =
      spliteratorFactory()

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
