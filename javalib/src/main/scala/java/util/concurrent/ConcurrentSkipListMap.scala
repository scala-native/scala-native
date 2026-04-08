/* Ported from JSR-166. Modified for Scala Native.
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util._
import java.util.function.{BiConsumer, BiFunction, Function}

import scala.scalanative.libc.stdatomic._
import scala.scalanative.libc.stdatomic.memory_order._
import scala.scalanative.runtime.{Intrinsics, fromRawPtr}
import scala.scalanative.unsafe._

@SerialVersionUID(-8627078664224814049L)
class ConcurrentSkipListMap[K, V] private (
    comparator0: Comparator[_ >: K],
    randomSeed: Long
) extends AbstractMap[K, V]
    with ConcurrentNavigableMap[K, V]
    with Cloneable
    with Serializable {

  import ConcurrentSkipListMap._

  /** The comparator used when constructor supplied one. */
  private final val comparator: Comparator[_ >: K] = comparator0

  /** The topmost head index node. */
  @volatile private var head: HeadIndex[K, V] = _

  private def headAtomic: AtomicRef[HeadIndex[K, V]] =
    new AtomicRef[HeadIndex[K, V]](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "head"))
    )

  /** Number of key-value mappings in this map. */
  @volatile private var nitems: Long = 0

  private def nitemsAtomic: AtomicLongLong =
    new AtomicLongLong(fromRawPtr(Intrinsics.classFieldRawPtr(this, "nitems")))

  @volatile private var seed: Long = if (randomSeed != 0) randomSeed else {
    var s = System.nanoTime() ^ Thread.currentThread().threadId()
    s ^= (s >>> 17)
    s ^= (s << 7)
    s ^= (s >>> 13)
    if (s == 0) 1 else s
  }

  private def initialize(): Unit = {
    val h = new Node[K, V](null, null.asInstanceOf[V], null)
    head = new HeadIndex[K, V](h, null, 1)
  }

  private def casHead(h: HeadIndex[K, V], nh: HeadIndex[K, V]): Boolean =
    headAtomic.compareExchangeStrong(h, nh)

  private def randomLevel(): Int = {
    var x: Long = 0
    var updated = false
    while (!updated) {
      x = seed
      var nx = x
      nx ^= nx << 13
      nx ^= nx >>> 7
      nx ^= nx << 17
      val atomic = new AtomicLongLong(fromRawPtr(Intrinsics.classFieldRawPtr(this, "seed")))
      updated = atomic.compareExchangeStrong(x, nx)
    }
    var level = 1
    if ((x & 0x80000001L) == 0L) {
      if ((x & 0x20000000L) == 0L) level = 2
      else if ((x & 0x4000000L) == 0L) level = 3
      else if ((x & 0x800000L) == 0L) level = 4
      else if ((x & 0x200000L) == 0L) level = 5
      else if ((x & 0x100000L) == 0L) level = 6
      else level = 7
    }
    level
  }

  private def compare(k1: K, k2: K): Int = {
    if (comparator != null)
      comparator.asInstanceOf[Comparator[K]].compare(k1, k2)
    else
      k1.asInstanceOf[Comparable[K]].compareTo(k2)
  }

  /** Returns the base-level predecessor of the given key, or null if none. */
  private def findPredecessor(key: K): Node[K, V] = {
    if (key == null) throw new NullPointerException
    var q: Index[K, V] = head
    var r: Index[K, V] = null
    while (q != null) {
      var p = q.right
      var desc = true
      while (desc) {
        if (p == null) {
          r = q
          q = q.down
          p = if (q != null) q.right else null
          desc = false
        } else {
          val nd = p.node
          if (nd == null) {
            q.rightAtomic.compareExchangeStrong(p, null)
            p = q.right
          } else {
            val c = compare(key, nd.key)
            if (c > 0) {
              p = p.right
            } else if (c == 0) {
              return nd
            } else {
              r = q
              q = q.down
              p = if (q != null) q.right else null
              desc = false
            }
          }
        }
      }
    }
    if (r != null) r.node else null
  }

  /** Returns a base-level node matching the given key, or null if not found. */
  private def findNode(key: K): Node[K, V] = {
    if (key == null) throw new NullPointerException
    var b = findPredecessor(key)
    if (b == null) return null
    var n = b.next
    while (n != null) {
      if (n.value == null) {
        n = unlinkNode(n)
        if (n == null) {
          b = findPredecessor(key)
          if (b == null) return null
          n = b.next
        }
      } else {
        val c = compare(key, n.key)
        if (c == 0) return n
        if (c < 0) return null
        b = n
        n = n.next
      }
    }
    null
  }

  def get(key: K): V = {
    if (key == null) throw new NullPointerException
    var n = findNode(key)
    while (n != null) {
      val v = n.value
      if (v != null) return v
      n = findNode(key)
    }
    null.asInstanceOf[V]
  }

  def put(key: K, value: V): V = {
    if (key == null || value == null) throw new NullPointerException
    doPut(key, value, onlyIfAbsent = false)
  }

  private def doPut(key: K, value: V, onlyIfAbsent: Boolean): V = {
    var done = false
    var result: V = null.asInstanceOf[V]
    while (!done) {
      var n = findPredecessor(key)
      if (n == null) {
        if (head == null) initialize()
        n = findPredecessor(key)
        if (n == null) { /* retry */ }
        else {
          val r = tryInsertAt(n, key, value, onlyIfAbsent)
          if (r._1) { done = true; result = r._2 }
        }
      } else {
        val r = tryInsertAt(n, key, value, onlyIfAbsent)
        if (r._1) { done = true; result = r._2 }
      }
    }
    result
  }

  private def tryInsertAt(curr0: Node[K, V], key: K, value: V, onlyIfAbsent: Boolean): (Boolean, V) = {
    var curr = curr0
    while (curr != null) {
      val f = curr.next
      if (f == null) {
        val z = new Node[K, V](key, value, null)
        if (curr.casNext(null, z)) {
          val level = randomLevel()
          if (level > 0) insertIndex(z, level)
          nitemsAtomic.addAndGet(1L)
          return (true, null.asInstanceOf[V])
        }
        return (false, null.asInstanceOf[V])
      } else {
        val c = compare(key, f.key)
        if (c > 0) {
          curr = f
        } else if (c == 0) {
          if (f.value == null) {
            unlinkNode(f)
            return (false, null.asInstanceOf[V])
          }
          val v = f.value
          if (!onlyIfAbsent) f.casValue(v, value)
          return (true, v)
        } else {
          val z = new Node[K, V](key, value, f)
          if (curr.casNext(f, z)) {
            val level = randomLevel()
            if (level > 0) insertIndex(z, level)
            nitemsAtomic.addAndGet(1L)
            return (true, null.asInstanceOf[V])
          }
          return (false, null.asInstanceOf[V])
        }
      }
    }
    (false, null.asInstanceOf[V])
  }

  /**
   * Inserts index nodes for the given base node at correct sorted positions.
   */
  private def insertIndex(z: Node[K, V], level: Int): Unit = {
    val key = z.key
    var h = head
    var maxLevel = h.level

    if (level > maxLevel) {
      var newIdx: Index[K, V] = null
      var lv = level
      while (lv > maxLevel) {
        newIdx = new Index[K, V](z, newIdx, lv)
        lv -= 1
      }
      val newHead = new HeadIndex[K, V](h.node, newIdx, level)
      if (casHead(h, newHead)) {
        var idx = newIdx
        while (idx != null) {
          linkIndex(idx)
          idx = idx.down
        }
        return
      }
      h = head
      maxLevel = h.level
      if (level > maxLevel) return // another thread grew, give up this time
    }

    var idx: Index[K, V] = null
    var lv = level
    while (lv > 0) {
      idx = new Index[K, V](z, idx, lv)
      lv -= 1
    }
    var currentIdx = idx
    while (currentIdx != null) {
      linkIndex(currentIdx)
      currentIdx = currentIdx.down
    }
  }

  private def linkIndex(idx: Index[K, V]): Unit = {
    val key = idx.node.key
    var q: Index[K, V] = head
    var d = head.level
    val lvl = idx.level

    while (d > lvl && q != null) {
      var r = q.right
      var done = false
      while (!done) {
        if (r == null) {
          done = true
        } else {
          val rn = r.node
          if (rn == null) {
            q.rightAtomic.compareExchangeStrong(r, null)
            r = q.right
          } else if (compare(key, rn.key) > 0) {
            q = r
            r = q.right
          } else {
            done = true
          }
        }
      }
      q = q.down
      d -= 1
    }

    if (q != null) {
      var retry = true
      while (retry) {
        retry = false
        var r = q.right
        var done = false
        while (!done) {
          if (r == null) {
            done = true
          } else {
            val rn = r.node
            if (rn == null) {
              q.rightAtomic.compareExchangeStrong(r, null)
              r = q.right
            } else if (compare(key, rn.key) > 0) {
              q = r
              r = q.right
            } else {
              done = true
            }
          }
        }
        idx.right = q.right
        if (!q.rightAtomic.compareExchangeStrong(idx.right, idx)) {
          retry = true
        }
      }
    }
  }

  def remove(key: K): V = {
    if (key == null) throw new NullPointerException
    doGetRemove(key)
  }

  private def doGetRemove(key: K): V = {
    var b = findPredecessor(key)
    while (b != null) {
      val f = b.next
      if (f == null) return null.asInstanceOf[V]
      val c = compare(key, f.key)
      if (c > 0) {
        b = f
      } else if (c == 0) {
        val v = f.value
        if (v == null) {
          unlinkNode(f)
          return null.asInstanceOf[V]
        }
        if (f.casValue(v, null.asInstanceOf[V])) {
          unlinkNode(f)
          nitemsAtomic.addAndGet(-1L)
          return v
        }
        b = findPredecessor(key)
      } else {
        return null.asInstanceOf[V]
      }
    }
    null.asInstanceOf[V]
  }

  private def unlinkNode(n: Node[K, V]): Node[K, V] = {
    var b: Node[K, V] = head.node
    var f = b.next
    while (f != null) {
      if (f eq n) {
        val fn = f.next
        if (b.casNext(f, fn)) return fn
        b = f
        f = b.next
      } else if (f.value == null) {
        val fn = f.next
        if (fn != null) { b.casNext(f, fn); f = fn }
        else { b = f; f = b.next }
      } else {
        b = f
        f = b.next
      }
    }
    null
  }

  def containsKey(key: K): Boolean = {
    if (key == null) throw new NullPointerException
    findNode(key) != null
  }

  def containsValue(value: V): Boolean = {
    if (value == null) throw new NullPointerException
    var n = head.node.next
    while (n != null) {
      val v = n.value
      if (v != null && v.equals(value)) return true
      n = n.next
    }
    false
  }

  def isEmpty(): Boolean = {
    var n = head.node.next
    while (n != null) {
      if (n.value != null) return false
      n = n.next
    }
    true
  }

  def size(): Int = {
    val n = nitems
    if (n > Int.MaxValue) Int.MaxValue else n.toInt
  }

  def clear(): Unit = {
    if (head != null) {
      var n = head.node.next
      while (n != null) {
        val next = n.next
        if (n.value != null) n.casValue(n.value, null.asInstanceOf[V])
        n = next
      }
    }
    nitemsAtomic.store(0L)
  }

  def remove(key: K, value: V): Boolean = {
    if (key == null || value == null) throw new NullPointerException
    var b = findPredecessor(key)
    while (b != null) {
      val f = b.next
      if (f == null) return false
      val c = compare(key, f.key)
      if (c > 0) { b = f }
      else if (c == 0) {
        val v = f.value
        if (v == null) { unlinkNode(f); return false }
        if (v.equals(value)) {
          if (f.casValue(v, null.asInstanceOf[V])) {
            unlinkNode(f)
            nitemsAtomic.addAndGet(-1L)
            return true
          }
        }
        return false
      } else return false
    }
    false
  }

  def replace(key: K, value: V): V = {
    if (key == null || value == null) throw new NullPointerException
    var n = findNode(key)
    while (n != null) {
      val v = n.value
      if (v != null && n.casValue(v, value)) return v
      n = findNode(key)
    }
    null.asInstanceOf[V]
  }

  def replace(key: K, oldVal: V, newVal: V): Boolean = {
    if (key == null || oldVal == null || newVal == null) throw new NullPointerException
    var n = findNode(key)
    while (n != null) {
      val v = n.value
      if (v == null) return false
      if (v.equals(oldVal)) {
        if (n.casValue(v, newVal)) return true
      } else return false
      n = findNode(key)
    }
    false
  }

  def putIfAbsent(key: K, value: V): V = {
    if (key == null || value == null) throw new NullPointerException
    doPut(key, value, onlyIfAbsent = true)
  }

  def compute(key: K, remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]): V = {
    if (key == null || remappingFunction == null) throw new NullPointerException
    doCompute(key, remappingFunction, onlyIfPresent = false)
  }

  private def doCompute(key: K, f: BiFunction[_ >: K, _ >: V, _ <: V], onlyIfPresent: Boolean): V = {
    var done = false
    var result: V = null.asInstanceOf[V]
    while (!done) {
      var b = findPredecessor(key)
      if (b == null) {
        if (head == null) initialize()
        b = findPredecessor(key)
        if (b == null) { /* retry */ }
        else {
          val r = tryComputeAt(b, key, f, onlyIfPresent)
          if (r._1) { done = true; result = r._2 }
        }
      } else {
        val r = tryComputeAt(b, key, f, onlyIfPresent)
        if (r._1) { done = true; result = r._2 }
      }
    }
    result
  }

  private def tryComputeAt(b: Node[K, V], key: K, f: BiFunction[_ >: K, _ >: V, _ <: V], onlyIfPresent: Boolean): (Boolean, V) = {
    var curr = b
    while (curr != null) {
      val n = curr.next
      if (n == null) {
        if (onlyIfPresent) return (true, null.asInstanceOf[V])
        val nv = f(key.asInstanceOf[K], null.asInstanceOf[V])
        if (nv != null) {
          val z = new Node[K, V](key, nv.asInstanceOf[V], null)
          if (curr.casNext(null, z)) {
            val level = randomLevel()
            if (level > 0) insertIndex(z, level)
            nitemsAtomic.addAndGet(1L)
            return (true, nv.asInstanceOf[V])
          }
        }
        return (false, null.asInstanceOf[V])
      } else if (n.value == null) {
        unlinkNode(n)
        return (false, null.asInstanceOf[V])
      } else {
        val c = compare(key, n.key)
        if (c > 0) { curr = n }
        else if (c == 0) {
          val oldValue = n.value
          val newValue = f(key.asInstanceOf[K], n.value.asInstanceOf[V])
          if (newValue == null) {
            if (oldValue != null && n.casValue(oldValue, null.asInstanceOf[V])) {
              unlinkNode(n)
              nitemsAtomic.addAndGet(-1L)
            }
            return (true, null.asInstanceOf[V])
          } else {
            if (n.casValue(oldValue, newValue.asInstanceOf[V])) return (true, newValue.asInstanceOf[V])
            return (false, null.asInstanceOf[V])
          }
        } else {
          if (onlyIfPresent) return (true, null.asInstanceOf[V])
          val nv = f(key.asInstanceOf[K], null.asInstanceOf[V])
          if (nv != null) {
            val z = new Node[K, V](key, nv.asInstanceOf[V], n)
            if (curr.casNext(n, z)) {
              val level = randomLevel()
              if (level > 0) insertIndex(z, level)
              nitemsAtomic.addAndGet(1L)
              return (true, nv.asInstanceOf[V])
            }
          }
          return (false, null.asInstanceOf[V])
        }
      }
    }
    (false, null.asInstanceOf[V])
  }

  def computeIfAbsent(key: K, mappingFunction: Function[_ >: K, _ <: V]): V = {
    if (key == null || mappingFunction == null) throw new NullPointerException
    doComputeIfAbsent(key, mappingFunction)
  }

  private def doComputeIfAbsent(key: K, f: Function[_ >: K, _ <: V]): V = {
    var done = false
    var result: V = null.asInstanceOf[V]
    while (!done) {
      var b = findPredecessor(key)
      if (b == null) {
        if (head == null) initialize()
        b = findPredecessor(key)
        if (b == null) { /* retry */ }
        else {
          val r = tryComputeIfAbsentAt(b, key, f)
          if (r._1) { done = true; result = r._2 }
        }
      } else {
        val r = tryComputeIfAbsentAt(b, key, f)
        if (r._1) { done = true; result = r._2 }
      }
    }
    result
  }

  private def tryComputeIfAbsentAt(b: Node[K, V], key: K, f: Function[_ >: K, _ <: V]): (Boolean, V) = {
    var curr = b
    while (curr != null) {
      val n = curr.next
      if (n == null) {
        val nv = f(key.asInstanceOf[K])
        if (nv != null) {
          val z = new Node[K, V](key, nv.asInstanceOf[V], null)
          if (curr.casNext(null, z)) {
            val level = randomLevel()
            if (level > 0) insertIndex(z, level)
            nitemsAtomic.addAndGet(1L)
            return (true, nv.asInstanceOf[V])
          }
        }
        return (true, null.asInstanceOf[V])
      } else if (n.value == null) {
        unlinkNode(n)
        return (false, null.asInstanceOf[V])
      } else {
        val c = compare(key, n.key)
        if (c > 0) { curr = n }
        else if (c == 0) {
          val v = n.value
          if (v != null) return (true, v)
          return (false, null.asInstanceOf[V])
        } else {
          val nv = f(key.asInstanceOf[K])
          if (nv != null) {
            val z = new Node[K, V](key, nv.asInstanceOf[V], n)
            if (curr.casNext(n, z)) {
              val level = randomLevel()
              if (level > 0) insertIndex(z, level)
              nitemsAtomic.addAndGet(1L)
              return (true, nv.asInstanceOf[V])
            }
          }
          return (true, null.asInstanceOf[V])
        }
      }
    }
    (false, null.asInstanceOf[V])
  }

  def computeIfPresent(key: K, remappingFunction: BiFunction[_ >: K, _ >: V, _ <: V]): V = {
    if (key == null || remappingFunction == null) throw new NullPointerException
    doCompute(key, remappingFunction, onlyIfPresent = true)
  }

  def merge(key: K, value: V, remappingFunction: BiFunction[_ >: V, _ >: V, _ <: V]): V = {
    if (key == null || value == null || remappingFunction == null) throw new NullPointerException
    doMerge(key, value, remappingFunction)
  }

  private def doMerge(key: K, value: V, f: BiFunction[_ >: V, _ >: V, _ <: V]): V = {
    var done = false
    var result: V = null.asInstanceOf[V]
    while (!done) {
      var b = findPredecessor(key)
      if (b == null) {
        if (head == null) initialize()
        b = findPredecessor(key)
        if (b == null) { /* retry */ }
        else {
          val r = tryMergeAt(b, key, value, f)
          if (r._1) { done = true; result = r._2 }
        }
      } else {
        val r = tryMergeAt(b, key, value, f)
        if (r._1) { done = true; result = r._2 }
      }
    }
    result
  }

  private def tryMergeAt(b: Node[K, V], key: K, value: V, f: BiFunction[_ >: V, _ >: V, _ <: V]): (Boolean, V) = {
    var curr = b
    while (curr != null) {
      val n = curr.next
      if (n == null) {
        val z = new Node[K, V](key, value.asInstanceOf[V], null)
        if (curr.casNext(null, z)) {
          val level = randomLevel()
          if (level > 0) insertIndex(z, level)
          nitemsAtomic.addAndGet(1L)
          return (true, value.asInstanceOf[V])
        }
        return (false, null.asInstanceOf[V])
      } else if (n.value == null) {
        unlinkNode(n)
        return (false, null.asInstanceOf[V])
      } else {
        val c = compare(key, n.key)
        if (c > 0) { curr = n }
        else if (c == 0) {
          val oldValue = n.value
          if (oldValue == null) return (false, null.asInstanceOf[V])
          val newValue = f(oldValue.asInstanceOf[V], value.asInstanceOf[V])
          if (newValue == null) {
            if (n.casValue(oldValue, null.asInstanceOf[V])) {
              unlinkNode(n)
              nitemsAtomic.addAndGet(-1L)
            }
            return (true, null.asInstanceOf[V])
          } else {
            if (n.casValue(oldValue, newValue.asInstanceOf[V])) return (true, newValue.asInstanceOf[V])
            return (false, null.asInstanceOf[V])
          }
        } else {
          val z = new Node[K, V](key, value.asInstanceOf[V], n)
          if (curr.casNext(n, z)) {
            val level = randomLevel()
            if (level > 0) insertIndex(z, level)
            nitemsAtomic.addAndGet(1L)
            return (true, value.asInstanceOf[V])
          }
          return (false, null.asInstanceOf[V])
        }
      }
    }
    (false, null.asInstanceOf[V])
  }

  def getOrDefault(key: K, defaultValue: V): V = {
    val v = get(key)
    if (v != null) v else defaultValue
  }

  def putAll(m: Map[_ <: K, _ <: V]): Unit = {
    val iter = m.entrySet().iterator()
    while (iter.hasNext()) {
      val e = iter.next()
      put(e.getKey(), e.getValue())
    }
  }

  override def clone(): ConcurrentSkipListMap[K, V] = {
    val cloned = new ConcurrentSkipListMap[K, V](comparator, 0L)
    var n = head.node.next
    while (n != null) {
      val v = n.value
      if (v != null) cloned.put(n.key, v)
      n = n.next
    }
    cloned
  }

  // ---- Views ----

  override def entrySet(): AbstractSet[Map.Entry[K, V]] = {
    if (_entrySet == null) _entrySet = new EntrySetView[K, V](this)
    _entrySet
  }
  @volatile private var _entrySet: AbstractSet[Map.Entry[K, V]] = _

  override def keySet(): NavigableSet[K] = {
    if (_keySet == null) _keySet = new KeySetView[K, V](this)
    _keySet
  }
  @volatile private var _keySet: NavigableSet[K] = _

  def navigableKeySet(): NavigableSet[K] = keySet()
  def descendingKeySet(): NavigableSet[K] = new KeySetView[K, V](this, lo = null, hi = null, descending = true)
  def descendingMap(): ConcurrentNavigableMap[K, V] = new SubMapView[K, V](this, lo = null, hi = null, descending = true)

  def subMap(fromKey: K, fromInclusive: Boolean, toKey: K, toInclusive: Boolean): ConcurrentNavigableMap[K, V] =
    new SubMapView[K, V](this, lo = fromKey, loIncl = fromInclusive, hi = toKey, hiIncl = toInclusive, descending = false)

  def headMap(toKey: K, inclusive: Boolean): ConcurrentNavigableMap[K, V] =
    new SubMapView[K, V](this, lo = null, hi = toKey, hiIncl = inclusive, descending = false)

  def tailMap(fromKey: K, inclusive: Boolean): ConcurrentNavigableMap[K, V] =
    new SubMapView[K, V](this, lo = fromKey, loIncl = inclusive, hi = null, descending = false)

  def subMap(fromKey: K, toKey: K): ConcurrentNavigableMap[K, V] = subMap(fromKey, true, toKey, false)
  def headMap(toKey: K): ConcurrentNavigableMap[K, V] = headMap(toKey, false)
  def tailMap(fromKey: K): ConcurrentNavigableMap[K, V] = tailMap(fromKey, true)

  // ---- Navigation ----

  private def findFirst(): Node[K, V] = {
    var n = head.node
    var f = n.next
    while (f != null) {
      if (f.value != null) return f
      n.casNext(f, f.next)
      f = n.next
    }
    null
  }

  private def findLast(): Node[K, V] = {
    var n: Node[K, V] = null
    var q: Index[K, V] = head
    while (q != null) {
      var p = q.right
      var goRight = true
      while (goRight) {
        if (p == null) goRight = false
        else {
          val pn = p.node
          if (pn == null) {
            q.rightAtomic.compareExchangeStrong(p, null)
            p = q.right
          } else {
            n = pn
            q = p
            p = q.right
          }
        }
      }
      if (p == null) q = q.down
    }
    if (n == null) return null
    var f = n.next
    while (f != null) {
      if (f.value != null) n = f
      else n.casNext(f, f.next)
      f = n.next
    }
    n
  }

  def firstKey(): K = {
    val n = findFirst()
    if (n != null) n.key else throw new NoSuchElementException
  }

  def lastKey(): K = {
    val n = findLast()
    if (n != null) n.key else throw new NoSuchElementException
  }

  def firstEntry(): Map.Entry[K, V] = {
    val n = findFirst()
    if (n != null) new SimpleImmutableEntry(n.key, n.value) else null
  }

  def lastEntry(): Map.Entry[K, V] = {
    val n = findLast()
    if (n != null) new SimpleImmutableEntry(n.key, n.value) else null
  }

  def pollFirstEntry(): Map.Entry[K, V] = {
    var b = head.node
    var n = b.next
    while (n != null) {
      if (n.value != null) {
        val v = n.value
        if (n.casValue(v, null.asInstanceOf[V])) {
          unlinkNode(n)
          nitemsAtomic.addAndGet(-1L)
          return new SimpleImmutableEntry(n.key, v)
        }
      }
      b = n
      n = b.next
    }
    null
  }

  def pollLastEntry(): Map.Entry[K, V] = {
    var done = false
    var result: Map.Entry[K, V] = null
    while (!done) {
      val n = findLast()
      if (n == null) { done = true; result = null }
      else {
        val v = n.value
        if (v == null) { done = true; result = null }
        else if (n.casValue(v, null.asInstanceOf[V])) {
          unlinkNode(n)
          nitemsAtomic.addAndGet(-1L)
          done = true
          result = new SimpleImmutableEntry(n.key, v)
        }
        // else CAS failed, retry
      }
    }
    result
  }

  def lowerEntry(key: K): Map.Entry[K, V] = {
    if (key == null) throw new NullPointerException
    var b = findPredecessor(key)
    if (b == null) return null
    var n = b.next
    while (n != null) {
      if (n.value != null) {
        val c = compare(key, n.key)
        if (c > 0) return new SimpleImmutableEntry(n.key, n.value)
        if (c == 0) return null
        return null
      }
      b = n; n = n.next
    }
    null
  }

  def lowerKey(key: K): K = { val e = lowerEntry(key); if (e != null) e.getKey() else null.asInstanceOf[K] }

  def floorEntry(key: K): Map.Entry[K, V] = {
    if (key == null) throw new NullPointerException
    var n = findNode(key)
    if (n != null) return new SimpleImmutableEntry(n.key, n.value)
    var b = findPredecessor(key)
    if (b == null) return null
    n = b.next
    if (n != null && n.value != null && compare(key, n.key) < 0) {
      return new SimpleImmutableEntry(n.key, n.value)
    }
    // b is predecessor - if b is a real node (not sentinel), it's < key
    if (b.key != null && b.value != null) return new SimpleImmutableEntry(b.key, b.value)
    null
  }

  def floorKey(key: K): K = { val e = floorEntry(key); if (e != null) e.getKey() else null.asInstanceOf[K] }

  def ceilingEntry(key: K): Map.Entry[K, V] = {
    if (key == null) throw new NullPointerException
    var n = findNode(key)
    if (n != null) return new SimpleImmutableEntry(n.key, n.value)
    var b = findPredecessor(key)
    if (b == null) return null
    n = b.next
    while (n != null) {
      if (n.value != null) return new SimpleImmutableEntry(n.key, n.value)
      b = n; n = n.next
    }
    null
  }

  def ceilingKey(key: K): K = { val e = ceilingEntry(key); if (e != null) e.getKey() else null.asInstanceOf[K] }

  def higherEntry(key: K): Map.Entry[K, V] = {
    if (key == null) throw new NullPointerException
    var b = findPredecessor(key)
    if (b == null) return null
    var n = b.next
    var found = false
    while (n != null) {
      if (n.value != null) {
        val c = compare(key, n.key)
        if (c == 0) { found = true; b = n; n = n.next }
        else if (c < 0 && !found) return new SimpleImmutableEntry(n.key, n.value)
        else if (found) return new SimpleImmutableEntry(n.key, n.value)
        else { b = n; n = n.next }
      } else { b = n; n = n.next }
    }
    null
  }

  def higherKey(key: K): K = { val e = higherEntry(key); if (e != null) e.getKey() else null.asInstanceOf[K] }

  def comparator(): Comparator[_ >: K] = comparator0

  override def equals(o: Any): Boolean = {
    if (o.asInstanceOf[AnyRef] eq this) return true
    o match {
      case m: Map[_, _] =>
        try {
          val iter = entrySet().iterator()
          while (iter.hasNext()) {
            val e = iter.next()
            val k = e.getKey()
            val v = e.getValue()
            if (v == null && !(m.containsKey(k) && m.get(k) == null)) return false
            if (v != null && !v.equals(m.get(k))) return false
          }
          true
        } catch {
          case _: ClassCastException => false
          case _: NullPointerException => false
        }
      case _ => false
    }
  }

  override def forEach(action: BiConsumer[_ >: K, _ >: V]): Unit = {
    if (action == null) throw new NullPointerException
    var n = head.node.next
    while (n != null) {
      val v = n.value
      if (v != null) action(n.key, v)
      n = n.next
    }
  }

  override def replaceAll(function: BiFunction[_ >: K, _ >: V, _ <: V]): Unit = {
    if (function == null) throw new NullPointerException
    var n = head.node.next
    while (n != null) {
      val v = n.value
      if (v != null) {
        val newVal = function(n.key, v)
        if (newVal != null) n.casValue(v, newVal.asInstanceOf[V])
      }
      n = n.next
    }
  }

  override def hashCode(): Int = {
    var h = 0
    var n = head.node.next
    while (n != null) {
      if (n.value != null) h += n.key.hashCode() ^ n.value.hashCode()
      n = n.next
    }
    h
  }

  override def toString(): String = {
    val sb = new StringBuilder
    sb.append('{')
    var first = true
    var n = head.node.next
    while (n != null) {
      if (n.value != null) {
        if (!first) sb.append(", ")
        sb.append(n.key).append('=').append(n.value)
        first = false
      }
      n = n.next
    }
    sb.append('}')
    sb.toString()
  }
}

object ConcurrentSkipListMap {

  /** Factory methods. */
  def apply[K <: Comparable[K], V](): ConcurrentSkipListMap[K, V] =
    new ConcurrentSkipListMap[K, V](null, 0)

  def apply[K, V](comparator: Comparator[_ >: K]): ConcurrentSkipListMap[K, V] =
    new ConcurrentSkipListMap[K, V](comparator, 0)

  def apply[K, V](m: Map[_ <: K, _ <: V]): ConcurrentSkipListMap[K, V] = {
    val map = new ConcurrentSkipListMap[K, V](null, 0)
    val iter = m.entrySet().iterator()
    while (iter.hasNext()) {
      val e = iter.next()
      map.put(e.getKey(), e.getValue())
    }
    map
  }

  def apply[K, V](m: SortedMap[K, _ <: V]): ConcurrentSkipListMap[K, V] = {
    val map = new ConcurrentSkipListMap[K, V](m.comparator(), 0)
    val iter = m.entrySet().iterator()
    while (iter.hasNext()) {
      val e = iter.next()
      map.put(e.getKey(), e.getValue())
    }
    map
  }

  /** Base node class. */
  private[concurrent] final class Node[K, V](val key: K, _value: V, next0: Node[K, V]) {
    @volatile var value: V = _value
    @volatile var next: Node[K, V] = next0

    def casValue(cmp: V, vl: V): Boolean = {
      val atomic = new AtomicRef[V](fromRawPtr(Intrinsics.classFieldRawPtr(this, "value")))
      atomic.compareExchangeStrong(cmp, vl)
    }

    def casNext(cmp: Node[K, V], vl: Node[K, V]): Boolean = {
      val atomic = new AtomicRef[Node[K, V]](fromRawPtr(Intrinsics.classFieldRawPtr(this, "next")))
      atomic.compareExchangeStrong(cmp, vl)
    }

    def isDeleted: Boolean = value == null
  }

  /** Index node class. */
  private[concurrent] class Index[K, V](val node: Node[K, V], val down: Index[K, V], val level: Int) {
    @volatile var right: Index[K, V] = _

    def rightAtomic: AtomicRef[Index[K, V]] =
      new AtomicRef[Index[K, V]](fromRawPtr(Intrinsics.classFieldRawPtr(this, "right")))
  }

  /** Head index node class. */
  private[concurrent] final class HeadIndex[K, V](node: Node[K, V], down: Index[K, V], level: Int)
    extends Index[K, V](node, down, level)

  // ---- View Classes ----

  /** KeySet view. */
  private[concurrent] final class KeySetView[K, V](
      map: ConcurrentSkipListMap[K, V],
      lo: K = null,
      loIncl: Boolean = true,
      hi: K = null,
      hiIncl: Boolean = false,
      descending: Boolean = false
  ) extends AbstractSet[K] with NavigableSet[K] {

    import map._

    private def inRange(key: K): Boolean = {
      if (lo != null) {
        val c = compare(key, lo)
        if (c < 0 || (c == 0 && !loIncl)) return false
      }
      if (hi != null) {
        val c = compare(key, hi)
        if (c > 0 || (c == 0 && !hiIncl)) return false
      }
      true
    }

    private def tooHigh(key: K): Boolean = {
      if (hi == null) false
      else { val c = compare(key, hi); c > 0 || (c == 0 && !hiIncl) }
    }

    def size(): Int = { var c = 0; val it = iterator(); while (it.hasNext) { it.next(); c += 1 }; c }
    override def isEmpty(): Boolean = !iterator().hasNext()

    override def contains(o: Any): Boolean = o match {
      case null => false
      case k: K @unchecked => inRange(k) && map.containsKey(k)
      case _ => false
    }

    override def add(e: K): Boolean = {
      if (e == null) throw new NullPointerException
      map.put(e, null.asInstanceOf[V])
      true
    }

    override def remove(o: Any): Boolean = o match {
      case null => throw new NullPointerException
      case k: K @unchecked =>
        if (!inRange(k)) false
        else {
          val b = findPredecessor(k)
          if (b == null) false
          else {
            val f = b.next
            if (f != null && compare(f.key, k) == 0) {
              if (f.casValue(f.value, null.asInstanceOf[V])) { unlinkNode(f); nitemsAtomic.addAndGet(-1L); true }
              else false
            } else false
          }
        }
      case _ => false
    }

    override def clear(): Unit = { val it = iterator(); while (it.hasNext) { it.next(); it.remove() } }

    def iterator(): Iterator[K] = new KeyIter[K, V](map, lo, loIncl, hi, hiIncl, descending)
    def descendingIterator(): Iterator[K] = new KeyIter[K, V](map, lo, loIncl, hi, hiIncl, !descending)
    def descendingSet(): NavigableSet[K] = new KeySetView[K, V](map, lo, loIncl, hi, hiIncl, !descending)

    def comparator(): Comparator[_ >: K] = map.comparator()

    def first(): K = {
      if (descending) {
        if (hi != null) { val e = map.floorEntry(hi); if (e != null && inRange(e.getKey)) e.getKey else throw new NoSuchElementException }
        else map.lastKey()
      } else {
        if (lo != null) { val e = map.ceilingEntry(lo); if (e != null && inRange(e.getKey)) e.getKey else throw new NoSuchElementException }
        else map.firstKey()
      }
    }

    def last(): K = {
      if (descending) {
        if (lo != null) { val e = map.ceilingEntry(lo); if (e != null && inRange(e.getKey)) e.getKey else throw new NoSuchElementException }
        else map.firstKey()
      } else {
        if (hi != null) { val e = map.floorEntry(hi); if (e != null && inRange(e.getKey)) e.getKey else throw new NoSuchElementException }
        else map.lastKey()
      }
    }

    def lower(e: K): K = { val en = map.lowerEntry(e); if (en != null && inRange(en.getKey)) en.getKey else null.asInstanceOf[K] }
    def floor(e: K): K = { val en = map.floorEntry(e); if (en != null && inRange(en.getKey)) en.getKey else null.asInstanceOf[K] }
    def ceiling(e: K): K = { val en = map.ceilingEntry(e); if (en != null && inRange(en.getKey)) en.getKey else null.asInstanceOf[K] }
    def higher(e: K): K = { val en = map.higherEntry(e); if (en != null && inRange(en.getKey)) en.getKey else null.asInstanceOf[K] }

    def pollFirst(): K = {
      if (descending) pollLast()
      else { val e = map.pollFirstEntry(); if (e != null && inRange(e.getKey)) e.getKey else null.asInstanceOf[K] }
    }

    def pollLast(): K = {
      if (descending) pollFirst()
      else { val e = map.pollLastEntry(); if (e != null && inRange(e.getKey)) e.getKey else null.asInstanceOf[K] }
    }

    def subSet(fromElement: K, fromInclusive: Boolean, toElement: K, toInclusive: Boolean): NavigableSet[K] =
      new KeySetView[K, V](map, fromElement, fromInclusive, toElement, toInclusive, descending)

    def headSet(toElement: K, inclusive: Boolean): NavigableSet[K] =
      new KeySetView[K, V](map, lo, loIncl, toElement, inclusive, descending)

    def tailSet(fromElement: K, inclusive: Boolean): NavigableSet[K] =
      new KeySetView[K, V](map, fromElement, inclusive, hi, hiIncl, descending)

    def subSet(fromElement: K, toElement: K): NavigableSet[K] = subSet(fromElement, true, toElement, false)
    def headSet(toElement: K): NavigableSet[K] = headSet(toElement, false)
    def tailSet(fromElement: K): NavigableSet[K] = tailSet(fromElement, true)
  }

  /** EntrySet view. */
  private[concurrent] final class EntrySetView[K, V](map: ConcurrentSkipListMap[K, V])
    extends AbstractSet[Map.Entry[K, V]] with Serializable {

    def size(): Int = map.size()
    override def isEmpty(): Boolean = map.isEmpty()

    override def contains(o: Any): Boolean = o match {
      case e: Map.Entry[_, _] =>
        val v = map.get(e.getKey())
        v != null && v.equals(e.getValue())
      case _ => false
    }

    override def remove(o: Any): Boolean = o match {
      case e: Map.Entry[_, _] => map.remove(e.getKey(), e.getValue())
      case _ => false
    }

    def iterator(): Iterator[Map.Entry[K, V]] = new CSLMIter[K, V](map)
    override def clear(): Unit = map.clear()
  }

  /** SubMap view. */
  private[concurrent] final class SubMapView[K, V](
      map: ConcurrentSkipListMap[K, V],
      lo: K = null,
      loIncl: Boolean = true,
      hi: K = null,
      hiIncl: Boolean = false,
      descending: Boolean = false
  ) extends AbstractMap[K, V] with ConcurrentNavigableMap[K, V] {

    import map._

    private def inRange(key: K): Boolean = {
      if (lo != null) {
        val c = compare(key, lo)
        if (c < 0 || (c == 0 && !loIncl)) return false
      }
      if (hi != null) {
        val c = compare(key, hi)
        if (c > 0 || (c == 0 && !hiIncl)) return false
      }
      true
    }

    private def tooHigh(key: K): Boolean = {
      if (hi == null) false
      else { val c = compare(key, hi); c > 0 || (c == 0 && !hiIncl) }
    }

    override def comparator(): Comparator[_ >: K] = map.comparator()
    override def size(): Int = { var c = 0; val it = entrySet().iterator(); while (it.hasNext) { it.next(); c += 1 }; c }
    override def isEmpty(): Boolean = !entrySet().iterator().hasNext()
    override def containsKey(key: K): Boolean = { if (key == null) throw new NullPointerException; inRange(key) && map.containsKey(key) }

    def get(key: K): V = {
      if (key == null) throw new NullPointerException
      if (!inRange(key)) null.asInstanceOf[V] else map.get(key)
    }

    def put(key: K, value: V): V = {
      if (key == null || value == null) throw new NullPointerException
      if (!inRange(key)) throw new IllegalArgumentException("key out of range")
      map.put(key, value)
    }

    def remove(key: K): V = {
      if (key == null) throw new NullPointerException
      if (!inRange(key)) null.asInstanceOf[V] else map.remove(key)
    }

    def containsValue(value: V): Boolean = {
      val it = values().iterator()
      while (it.hasNext) if (it.next().equals(value)) return true
      false
    }

    override def clear(): Unit = { val it = keySet().iterator(); while (it.hasNext) { it.next(); it.remove() } }

    private def findFirstInRange(): Node[K, V] = {
      if (lo == null) {
        var n = map.findFirst()
        while (n != null && tooHigh(n.key)) n = n.next
        n
      } else {
        var b = findPredecessor(lo)
        if (b == null) b = map.head.node
        var n = b.next
        while (n != null) {
          if (n.value != null && inRange(n.key)) return n
          if (tooHigh(n.key)) return null
          n = n.next
        }
        null
      }
    }

    private def findLastInRange(): Node[K, V] = {
      if (hi == null) {
        var n = map.findLast()
        while (n != null && (n.value == null || !inRange(n.key))) n = { if (n.value == null) { map.head.node.casNext(n, n.next); map.findLast() } else null }
        // simpler: walk from start
        var curr = map.findFirst()
        var result: Node[K, V] = null
        while (curr != null) {
          if (curr.value != null && inRange(curr.key)) result = curr
          if (tooHigh(curr.key)) return result
          curr = curr.next
        }
        result
      } else {
        var curr = map.findFirst()
        var result: Node[K, V] = null
        while (curr != null) {
          if (curr.value != null && inRange(curr.key)) result = curr
          if (tooHigh(curr.key)) return result
          curr = curr.next
        }
        result
      }
    }

    def firstKey(): K = { val n = findFirstInRange(); if (n != null) n.key else throw new NoSuchElementException }
    def lastKey(): K = { val n = findLastInRange(); if (n != null) n.key else throw new NoSuchElementException }

    def firstEntry(): Map.Entry[K, V] = { val n = findFirstInRange(); if (n != null) new SimpleImmutableEntry(n.key, n.value) else null }
    def lastEntry(): Map.Entry[K, V] = { val n = findLastInRange(); if (n != null) new SimpleImmutableEntry(n.key, n.value) else null }

    def lowerEntry(key: K): Map.Entry[K, V] = {
      if (!inRange(key)) { if (tooHigh(key)) lastEntry() else null }
      else { val e = map.lowerEntry(key); if (e != null && inRange(e.getKey)) e else null }
    }
    def floorEntry(key: K): Map.Entry[K, V] = {
      if (!inRange(key)) { if (tooHigh(key)) lastEntry() else null }
      else { val e = map.floorEntry(key); if (e != null && inRange(e.getKey)) e else null }
    }
    def ceilingEntry(key: K): Map.Entry[K, V] = {
      if (!inRange(key)) { if (tooHigh(key)) null else firstEntry() }
      else { val e = map.ceilingEntry(key); if (e != null && inRange(e.getKey)) e else null }
    }
    def higherEntry(key: K): Map.Entry[K, V] = {
      if (!inRange(key)) { if (tooHigh(key)) null else firstEntry() }
      else { val e = map.higherEntry(key); if (e != null && inRange(e.getKey)) e else null }
    }

    def lowerKey(key: K): K = { val e = lowerEntry(key); if (e != null) e.getKey() else null.asInstanceOf[K] }
    def floorKey(key: K): K = { val e = floorEntry(key); if (e != null) e.getKey() else null.asInstanceOf[K] }
    def ceilingKey(key: K): K = { val e = ceilingEntry(key); if (e != null) e.getKey() else null.asInstanceOf[K] }
    def higherKey(key: K): K = { val e = higherEntry(key); if (e != null) e.getKey() else null.asInstanceOf[K] }

    def subMap(fromKey: K, fromInclusive: Boolean, toKey: K, toInclusive: Boolean): ConcurrentNavigableMap[K, V] =
      new SubMapView[K, V](map, fromKey, fromInclusive, toKey, toInclusive, descending)
    def headMap(toKey: K, inclusive: Boolean): ConcurrentNavigableMap[K, V] =
      new SubMapView[K, V](map, lo, loIncl, toKey, inclusive, descending)
    def tailMap(fromKey: K, inclusive: Boolean): ConcurrentNavigableMap[K, V] =
      new SubMapView[K, V](map, fromKey, inclusive, hi, hiIncl, descending)

    def subMap(fromKey: K, toKey: K): ConcurrentNavigableMap[K, V] = subMap(fromKey, true, toKey, false)
    def headMap(toKey: K): ConcurrentNavigableMap[K, V] = headMap(toKey, false)
    def tailMap(fromKey: K): ConcurrentNavigableMap[K, V] = tailMap(fromKey, true)

    def descendingMap(): ConcurrentNavigableMap[K, V] = new SubMapView[K, V](map, lo, loIncl, hi, hiIncl, !descending)
    def navigableKeySet(): NavigableSet[K] = new KeySetView[K, V](map, lo, loIncl, hi, hiIncl, descending)
    def keySet(): NavigableSet[K] = navigableKeySet()
    def descendingKeySet(): NavigableSet[K] = new KeySetView[K, V](map, lo, loIncl, hi, hiIncl, !descending)

    override def entrySet(): AbstractSet[Map.Entry[K, V]] = new SubMapEntrySet[K, V](this)
    override def values(): Collection[V] = new SubMapViewVals[K, V](this)
  }

  /** EntrySet for SubMap. */
  private[concurrent] final class SubMapEntrySet[K, V](sub: SubMapView[K, V])
    extends AbstractSet[Map.Entry[K, V]] with Serializable {
    import sub.map
    import sub.inRange
    override def size(): Int = sub.size()
    override def isEmpty(): Boolean = sub.isEmpty()
    override def contains(o: Any): Boolean = o match {
      case e: Map.Entry[_, _] =>
        val k = e.getKey().asInstanceOf[K]
        inRange(k) && { val v = map.get(k); v != null && v.equals(e.getValue()) }
      case _ => false
    }
    override def remove(o: Any): Boolean = o match {
      case e: Map.Entry[_, _] =>
        val k = e.getKey().asInstanceOf[K]
        if (!inRange(k)) false else map.remove(k, e.getValue())
      case _ => false
    }
    def iterator(): Iterator[Map.Entry[K, V]] = new SubMapIter[K, V](sub.map, sub.lo, sub.loIncl, sub.hi, sub.hiIncl, sub.descending)
    override def clear(): Unit = sub.clear()
  }

  /** Values view for SubMap. */
  private[concurrent] final class SubMapViewVals[K, V](sub: SubMapView[K, V])
    extends AbstractCollection[V] with Serializable {
    override def size(): Int = sub.size()
    override def isEmpty(): Boolean = sub.isEmpty()
    override def contains(o: Any): Boolean = { val it = iterator(); while (it.hasNext) if (it.next().equals(o)) return true; false }
    def iterator(): Iterator[V] = new SubMapValIter[K, V](sub.map, sub.lo, sub.loIncl, sub.hi, sub.hiIncl, sub.descending)
    override def clear(): Unit = sub.clear()
  }

  // ---- Iterators ----

  /** Base entry iterator. */
  private[concurrent] class CSLMIter[K, V](map: ConcurrentSkipListMap[K, V])
    extends Iterator[Map.Entry[K, V]] {
    import map._
    private var lastReturned: Node[K, V] = _
    private var nextNode: Node[K, V] = _
    {
      var n = head.node.next
      while (n != null && n.value == null) n = n.next
      nextNode = n
    }
    def hasNext: Boolean = nextNode != null
    def next(): Map.Entry[K, V] = {
      val n = nextNode
      if (n == null) throw new NoSuchElementException
      lastReturned = n
      var f = n.next
      while (f != null && f.value == null) f = f.next
      nextNode = f
      new SimpleImmutableEntry(n.key, n.value)
    }
    override def remove(): Unit = {
      val n = lastReturned
      if (n == null) throw new IllegalStateException
      if (n.value != null) {
        n.casValue(n.value, null.asInstanceOf[V])
        unlinkNode(n)
        nitemsAtomic.addAndGet(-1L)
      }
      lastReturned = null
    }
  }

  /** Key iterator with range limits. */
  private[concurrent] class KeyIter[K, V](
      map: ConcurrentSkipListMap[K, V],
      lo: K, loIncl: Boolean, hi: K, hiIncl: Boolean, descending: Boolean
  ) extends Iterator[K] {
    import map._
    private var lastReturned: Node[K, V] = _
    private var nextNode: Node[K, V] = _

    private def inRange(key: K): Boolean = {
      if (lo != null) { val c = compare(key, lo); if (c < 0 || (c == 0 && !loIncl)) return false }
      if (hi != null) { val c = compare(key, hi); if (c > 0 || (c == 0 && !hiIncl)) return false }
      true
    }
    private def tooHigh(key: K): Boolean = {
      if (hi == null) false else { val c = compare(key, hi); c > 0 || (c == 0 && !hiIncl) }
    }

    {
      var n = head.node.next
      while (n != null && (n.value == null || !inRange(n.key))) {
        if (tooHigh(n.key)) { n = null } else n = n.next
      }
      nextNode = n
    }

    def hasNext: Boolean = nextNode != null
    def next(): K = {
      val n = nextNode
      if (n == null) throw new NoSuchElementException
      lastReturned = n
      var f = n.next
      while (f != null && (f.value == null || !inRange(f.key))) {
        if (tooHigh(f.key)) { f = null; nextNode = null } else f = f.next
      }
      nextNode = f
      n.key
    }
    override def remove(): Unit = {
      val n = lastReturned
      if (n == null) throw new IllegalStateException
      if (n.value != null) {
        n.casValue(n.value, null.asInstanceOf[V])
        unlinkNode(n)
        nitemsAtomic.addAndGet(-1L)
      }
      lastReturned = null
    }
  }

  /** SubMap entry iterator. */
  private[concurrent] class SubMapIter[K, V](
      map: ConcurrentSkipListMap[K, V],
      lo: K, loIncl: Boolean, hi: K, hiIncl: Boolean, descending: Boolean
  ) extends Iterator[Map.Entry[K, V]] {
    import map._
    private var lastReturned: Node[K, V] = _
    private var nextNode: Node[K, V] = _

    private def inRange(key: K): Boolean = {
      if (lo != null) { val c = compare(key, lo); if (c < 0 || (c == 0 && !loIncl)) return false }
      if (hi != null) { val c = compare(key, hi); if (c > 0 || (c == 0 && !hiIncl)) return false }
      true
    }
    private def tooHigh(key: K): Boolean = {
      if (hi == null) false else { val c = compare(key, hi); c > 0 || (c == 0 && !hiIncl) }
    }

    {
      var n = head.node.next
      while (n != null && (n.value == null || !inRange(n.key))) {
        if (tooHigh(n.key)) { n = null } else n = n.next
      }
      nextNode = n
    }

    def hasNext: Boolean = nextNode != null
    def next(): Map.Entry[K, V] = {
      val n = nextNode
      if (n == null) throw new NoSuchElementException
      lastReturned = n
      var f = n.next
      while (f != null && (f.value == null || !inRange(f.key))) {
        if (tooHigh(f.key)) { f = null; nextNode = null } else f = f.next
      }
      nextNode = f
      new SimpleImmutableEntry(n.key, n.value)
    }
    override def remove(): Unit = {
      val n = lastReturned
      if (n == null) throw new IllegalStateException
      if (n.value != null) {
        n.casValue(n.value, null.asInstanceOf[V])
        unlinkNode(n)
        nitemsAtomic.addAndGet(-1L)
      }
      lastReturned = null
    }
  }

  /** SubMap value iterator. */
  private[concurrent] class SubMapValIter[K, V](
      map: ConcurrentSkipListMap[K, V],
      lo: K, loIncl: Boolean, hi: K, hiIncl: Boolean, descending: Boolean
  ) extends Iterator[V] {
    private val entryIter = new SubMapIter[K, V](map, lo, loIncl, hi, hiIncl, descending)
    def hasNext: Boolean = entryIter.hasNext
    def next(): V = entryIter.next().getValue()
    override def remove(): Unit = entryIter.remove()
  }
}
