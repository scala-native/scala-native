/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util._
import java.util.concurrent.atomic.LongAdder
import java.util.function.{BiConsumer, BiFunction, Function}

import scala.annotation.tailrec
import scala.language.existentials

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic.AtomicRef
import scala.scalanative.runtime.{Intrinsics, fromRawPtr}
import scala.scalanative.unsafe._

@SerialVersionUID(-8627078645895051609L)
class ConcurrentSkipListMap[K <: AnyRef, V <: AnyRef] private (
    private val root: ConcurrentSkipListMap[K, V],
    private[concurrent] val ordering: Comparator[_ >: K],
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
      null.asInstanceOf[ConcurrentSkipListMap[K, V]],
      null,
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
      null.asInstanceOf[ConcurrentSkipListMap[K, V]],
      comparator,
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

  if (loSet && hiSet && m.compare(lo, hi) > 0)
    throw new IllegalArgumentException("inconsistent range")

  private[concurrent] def m: ConcurrentSkipListMap[K, V] =
    if (root == null) this else root

  private val baseHead =
    new Node[K, V](null.asInstanceOf[K], null.asInstanceOf[V], null)
  private val adder = new LongAdder
  private[concurrent] var headIndex =
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
      n = findGreaterThan(n.key)
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

  private def liveNext(pred: Node[K, V]): Node[K, V] = {
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

  private def tooLow(key: Any): Boolean =
    loSet && {
      val c = m.compareAny(key, lo)
      c < 0 || (c == 0 && !loInclusive)
    }

  private def tooHigh(key: Any): Boolean =
    hiSet && {
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
      if (!loSet) m.firstNode()
      else if (loInclusive) m.findGreaterOrEqual(lo)
      else m.findGreaterThan(lo)
    if (n != null && !tooHigh(n.key)) n else null
  }

  private def highestNodeAscending(): Node[K, V] = {
    val n =
      if (!hiSet) m.lastNode()
      else if (hiInclusive) m.findLessOrEqual(hi)
      else m.findLessThan(hi)
    if (n != null && !tooLow(n.key)) n else null
  }

  private def firstNodeInView(): Node[K, V] =
    if (descending) highestNodeAscending()
    else lowestNodeAscending()

  private def lastNodeInView(): Node[K, V] =
    if (descending) lowestNodeAscending()
    else highestNodeAscending()

  private def nextNodeInView(node: Node[K, V]): Node[K, V] = {
    val n =
      if (descending) m.findLessThan(node.key)
      else m.findGreaterThan(node.key)
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
        val c = m.compare(fromKey, lo)
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
        val c = m.compare(toKey, hi)
        if (c > 0 || (c == 0 && !hiInclusive && toInclusive))
          throw new IllegalArgumentException("key out of range")
      }
    }

    new ConcurrentSkipListMap[K, V](
      m,
      m.ordering,
      fromKey,
      fromInclusive,
      fromSet,
      toKey,
      toInclusive,
      toSet,
      descending
    )
  }

  override def size(): Int = {
    val count =
      if (!loSet && !hiSet) m.count()
      else {
        var count = 0L
        var n = lowestNodeAscending()
        while (n != null) {
          if (n.value != null) count += 1L
          n = m.findGreaterThan(n.key)
          if (n != null && tooHigh(n.key)) n = null
        }
        count
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
      n = m.findGreaterThan(n.key)
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
      this.m.doPut(e.getKey(), e.getValue(), onlyIfAbsent = false)
    }
  }

  override def remove(key: Any): V = {
    val k = requireKey(key)
    if (!inBounds(k)) null.asInstanceOf[V]
    else m.doRemove(k, null)
  }

  override def clear(): Unit = {
    if (!loSet && !hiSet) m.doClear()
    else {
      var n = firstNodeInView()
      while (n != null) {
        val next = nextNodeInView(n)
        if (n.value != null)
          m.doRemove(n.key, null)
        n = next
      }
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
    if (!descending) m.ordering
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
    new ConcurrentSkipListMap[K, V](
      m,
      m.ordering,
      lo,
      loInclusive,
      loSet,
      hi,
      hiInclusive,
      hiSet,
      !descending
    )

  override def clone(): ConcurrentSkipListMap[K, V] = {
    val cloned = new ConcurrentSkipListMap[K, V](m.ordering)
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

  private abstract class LiveIterator[K <: AnyRef, V <: AnyRef, A](
      map: ConcurrentSkipListMap[K, V]
  ) extends Iterator[A] {
    private var nextNode = map.firstNodeInView()
    private var nextValue: V = _
    private var lastReturned: Node[K, V] = _
    protected var currentValue: V = _

    skipDeleted()

    private def skipDeleted(): Unit = {
      while (nextNode != null) {
        nextValue = nextNode.value
        if (nextValue != null) return
        nextNode = map.nextNodeInView(nextNode)
      }
    }

    override final def hasNext(): Boolean = nextNode != null

    protected final def nextLiveNode(): Node[K, V] = {
      val n = nextNode
      if (n == null)
        throw new NoSuchElementException
      currentValue = nextValue
      lastReturned = n
      nextNode = map.nextNodeInView(n)
      skipDeleted()
      n
    }

    override final def remove(): Unit = {
      val n = lastReturned
      if (n == null) throw new IllegalStateException
      map.remove(n.key)
      lastReturned = null
    }
  }

  private final class LiveKeyIterator[K <: AnyRef, V <: AnyRef](
      map: ConcurrentSkipListMap[K, V]
  ) extends LiveIterator[K, V, K](map) {
    override def next(): K = nextLiveNode().key
  }

  private final class LiveValueIterator[K <: AnyRef, V <: AnyRef](
      map: ConcurrentSkipListMap[K, V]
  ) extends LiveIterator[K, V, V](map) {
    override def next(): V = {
      nextLiveNode()
      currentValue
    }
  }

  private final class LiveEntryIterator[K <: AnyRef, V <: AnyRef](
      map: ConcurrentSkipListMap[K, V]
  ) extends LiveIterator[K, V, Map.Entry[K, V]](map) {
    override def next(): Map.Entry[K, V] = {
      val n = nextLiveNode()
      new AbstractMap.SimpleImmutableEntry[K, V](n.key, currentValue)
    }
  }

  private final class EntrySet[K <: AnyRef, V <: AnyRef](
      map: ConcurrentSkipListMap[K, V]
  ) extends AbstractSet[Map.Entry[K, V]]
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
      new LiveEntryIterator[K, V](map)
  }

  private final class Values[K <: AnyRef, V <: AnyRef](
      map: ConcurrentSkipListMap[K, V]
  ) extends AbstractCollection[V]
      with Serializable {

    override def size(): Int = map.size()

    override def isEmpty(): Boolean = map.isEmpty()

    override def clear(): Unit = map.clear()

    override def contains(o: Any): Boolean = map.containsValue(o)

    override def iterator(): Iterator[V] =
      new LiveValueIterator[K, V](map)
  }

  private final class KeySet[K <: AnyRef, V <: AnyRef](
      map: ConcurrentSkipListMap[K, V]
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
      new LiveKeyIterator[K, V](map)

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
