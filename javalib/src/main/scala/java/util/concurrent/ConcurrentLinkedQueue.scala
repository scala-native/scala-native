/*
 * Written by Doug Lea and Martin Buchholz with assistance from members of
 * JCP JSR-166 Expert Group and released to the public domain, as explained
 * at http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import java.util._
import java.util.NoSuchElementException
import java.util.Objects
import java.util.Spliterator
import java.util.Spliterators
import java.util.function.Consumer
import java.util.function.Predicate
import java.io.{ObjectInputStream, ObjectOutputStream}

import scala.scalanative.unsafe._
import scala.scalanative.runtime.fromRawPtr
import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.libc.stdatomic._
import scala.scalanative.libc.stdatomic.memory_order.memory_order_release
import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic.memory_order.memory_order_relaxed

@SerialVersionUID(196745693267521676L)
object ConcurrentLinkedQueue {

  private[concurrent] final class Node[E <: AnyRef] private[concurrent]
  /** Constructs a dead dummy node. */
  {
    @volatile private[concurrent] var item: E = _
    @volatile private[concurrent] var next: Node[E] = _

    @alwaysinline private[ConcurrentLinkedQueue] def ITEM: AtomicRef[E] =
      fromRawPtr[E](classFieldRawPtr(this, "item")).atomic
    @alwaysinline private[ConcurrentLinkedQueue] def NEXT: AtomicRef[Node[E]] =
      fromRawPtr[Node[E]](classFieldRawPtr(this, "next")).atomic

    def this(item: E) = {
      this()
      ITEM.store(item, memory_order_relaxed)
    }

    private[concurrent] def appendRelaxed(next: Node[E]): Unit = {
      // assert next != null;
      // assert this.next == null;
      NEXT.store(next, memory_order_relaxed)
    }

    private[concurrent] def casItem(cmp: E, `val`: E) = {
      // assert item == cmp || item == null;
      // assert cmp != null;
      // assert val == null;
      ITEM.compareExchangeStrong(cmp, `val`)
    }
  }

  /** Tolerate this many consecutive dead nodes before CAS-collapsing. Amortized
   *  cost of clear() is (1 + 1/MAX_HOPS) CASes per element.
   */
  private final val MAX_HOPS = 8
}

@SerialVersionUID(196745693267521676L)
class ConcurrentLinkedQueue[E <: AnyRef]
    extends AbstractQueue[E]
    with Queue[E]
    with Serializable {
  import ConcurrentLinkedQueue._

  @volatile
  @transient private[concurrent] var head: Node[E] = new Node[E]

  @volatile
  @transient private var tail: Node[E] = head

  @alwaysinline private def HEAD: AtomicRef[Node[E]] =
    fromRawPtr[Node[E]](classFieldRawPtr(this, "head")).atomic
  @alwaysinline private def TAIL: AtomicRef[Node[E]] =
    fromRawPtr[Node[E]](classFieldRawPtr(this, "tail")).atomic

  def this(c: Collection[_ <: E]) = {
    this()
    var h, t: Node[E] = head
    c.forEach { e =>
      val newNode = new Node[E](Objects.requireNonNull(e))
      t.appendRelaxed(newNode)
      t = newNode
    }
    if (h == null) {
      h = new Node[E]();
      t = h
    }
    head = h;
    tail = t;
  }

  override def add(e: E): Boolean = offer(e)

  private[concurrent] final def updateHead(h: Node[E], p: Node[E]): Unit = {
    // assert h != null && p != null && (h == p || h.item == null);
    if ((h ne p) && this.HEAD.compareExchangeStrong(h, p))
      h.NEXT.store(h, memory_order_release)
  }

  private[concurrent] final def succ(p: Node[E]) = {
    p.next match {
      case `p`  => head
      case next => next
    }
  }

  private def tryCasSuccessor(
      pred: Node[E],
      c: Node[E],
      p: Node[E]
  ): Boolean = {
    // assert p != null;
    // assert c.item == null;
    // assert c != p;
    if (pred != null) pred.NEXT.compareExchangeStrong(c, p)
    else if (this.HEAD.compareExchangeStrong(c, p)) {
      c.NEXT.store(c, memory_order_release)
      true
    } else false
  }

  private def skipDeadNodes(
      pred: Node[E],
      c: Node[E],
      p: Node[E],
      _q: Node[E]
  ): Node[E] = {
    // assert pred != c;
    // assert p != q;
    // assert c.item == null;
    // assert p.item == null;
    var q = _q
    if (q == null) {
      // Never unlink trailing node.
      if (c eq p) return pred
      q = p
    }
    if (tryCasSuccessor(pred, c, q) && (pred == null || pred.ITEM.load(
          memory_order_relaxed
        ) != null)) pred
    else p
  }

  override def offer(e: E): Boolean = {
    val newNode = new Node[E](Objects.requireNonNull(e))
    var t = tail
    var p = t
    while (true) {
      val q = p.next
      if (q == null) {
        // p is last node
        if (p.NEXT.compareExchangeStrong(null: Node[E], newNode)) {
          // Successful CAS is the linearization point
          // for e to become an element of this queue,
          // and for newNode to become "live".
          if (p ne t)
            // hop two nodes at a time; failure is OK
            this.TAIL.compareExchangeWeak(t, newNode)
          return true
        }
      } else if (p eq q) // We have fallen off list.  If tail is unchanged, it
        // will also be off-list, in which case we need to
        // jump to head, from which all live nodes are always
        // reachable.  Else the new tail is a better bet.
        p =
          if (t ne { t = tail; t }) t
          else head
      else
        p =
          if ((p ne t) && (t ne { t = tail; t })) t
          else q // Check for tail updates after two hops.
    }
    // unreachable
    false
  }

  override def poll(): E = {
    while (true) {
      val h = head
      var p = h
      var q: Node[E] = null.asInstanceOf[Node[E]]
      var restart = false
      while (!restart) {
        val item: E = p.item
        if (item != null && p.casItem(item, null.asInstanceOf[E])) {
          // Successful CAS is the linearization point
          // for item to be removed from this queue.
          if (p ne h)
            updateHead(
              h,
              if ({ q = p.next; q } != null) q
              else p
            ) // hop two nodes at a time

          return item
        } else if ({ q = p.next; q } == null) {
          updateHead(h, p)
          return null.asInstanceOf[E]
        } else if (p eq q) restart = true
        else p = q
      }
    }
    // unreachable
    null.asInstanceOf[E]
  }

  override def peek(): E = {
    while (true) {
      val h = head
      var p = h
      var q: Node[E] = null
      var restart = false
      while (!restart) {
        val item: E = p.item
        if (item != null || { q = p.next; q } == null) {
          updateHead(h, p)
          return item
        } else if (p eq q) restart = true
        else p = q
      }
    }
    // unreachable
    null.asInstanceOf[E]
  }

  private[concurrent] def first(): Node[E] = {
    while (true) {
      val h = head
      var p = h
      var q: Node[E] = null
      var restart = false
      while (!restart) {
        val hasItem = p.item != null
        if (hasItem || { q = p.next; q } == null) {
          updateHead(h, p)
          return if (hasItem) p
          else null
        } else if (p eq q) restart = true
        else p = q
      }
    }
    // unreachable
    null
  }

  override def isEmpty(): Boolean = first() == null

  override def size(): Int = {
    while (true) {
      var count = 0
      var p = first()
      var restart = false
      while (p != null && !restart) {
        if (p.item != null) {
          count += 1
          // @see Collection.size()
          if (count == Integer.MAX_VALUE) return count
        }
        if (p eq { p = p.next; p }) restart = true
      }
      if (!restart) return count
    }
    // unreachable
    -1
  }

  override def contains(_o: Any): Boolean = {
    if (_o == null) return false
    val o = _o.asInstanceOf[AnyRef]
    var p = head
    var pred: Node[E] = null
    while (p != null) {
      var q = p.next
      val item: E = p.item
      if (item != null) {
        if (o.equals(item)) return true
        pred = p
        p = q
      } else {
        var c = p
        var break = false
        while (!break) {
          if (q == null || q.item != null) {
            pred = skipDeadNodes(pred, c, p, q)
            p = q
            break = true
          }
          if (!break && (p eq { p = q; q })) break = true
        }
      }
    }
    false
  }

  override def remove(_o: Any): Boolean = {
    if (_o == null || !_o.isInstanceOf[AnyRef]) return false
    val o = _o.asInstanceOf[AnyRef]

    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false
      var p = head
      var pred: Node[E] = null
      while (p != null && !restartFromHead) {
        var q = p.next
        val item: E = p.item
        if (item != null) {
          if (o.equals(item) && p.casItem(item, null.asInstanceOf[E])) {
            skipDeadNodes(pred, p, p, q)
            return true
          }
          pred = p
          p = q
        } else {
          var c = p
          var break = false
          while (!break) {
            if (q == null || q.item != null) {
              pred = skipDeadNodes(pred, c, p, q)
              p = q
              break = true
            }
            if (!break && { p eq { p = q; q } }) {
              break = true
              restartFromHead = true
            }
          }
        }
      }
    }
    false
  }

  override def addAll(c: Collection[_ <: E]): Boolean = {
    if (c eq this)
      throw new IllegalArgumentException // As historically specified in AbstractQueue#addAll

    // Copy c into a private chain of Nodes
    var beginningOfTheEnd: Node[E] = null
    var last: Node[E] = null
    c.forEach { e =>
      val newNode = new Node[E](Objects.requireNonNull(e))
      if (beginningOfTheEnd == null) beginningOfTheEnd = {
        last = newNode; last
      }
      else last.appendRelaxed({ last = newNode; last })
    }
    if (beginningOfTheEnd == null) return false
    // Atomically append the chain at the tail of this collection
    var t = tail
    var p = t
    while (true) {
      val q = p.next
      if (q == null) {
        // p is last node
        if (p.NEXT.compareExchangeStrong(null: Node[E], beginningOfTheEnd)) {
          // Successful CAS is the linearization point
          // for all elements to be added to this queue.
          if (!this.TAIL.compareExchangeWeak(t, last)) {
            // Try a little harder to update tail,
            // since we may be adding many elements.
            t = tail
            if (last.next == null) this.TAIL.compareExchangeWeak(t, last)
          }
          return true
        }
      } else if (p eq q) // We have fallen off list.  If tail is unchanged, it
        // will also be off-list, in which case we need to
        // jump to head, from which all live nodes are always
        // reachable.  Else the new tail is a better bet.
        p =
          if (t ne { t = tail; t }) t
          else head
      else
        p =
          if ((p ne t) && (t ne { t = tail; t })) t
          else q // Check for tail updates after two hops.
    }
    // unreachable
    false
  }

  override def toString: String = {
    var a: Array[String] = null
    while (true) {
      var charLength = 0
      var size = 0
      var p = first()
      var restart = false
      while (p != null && !restart) {
        val item: E = p.item
        if (item != null) {
          if (a == null) a = new Array[String](4)
          else if (size == a.length) a = Arrays.copyOf(a, 2 * size)
          val s = item.toString
          a(size) = s
          size += 1
          charLength += s.length
        }
        if (p eq { p = p.next; p }) restart = true
      }
      if (!restart) {
        if (size == 0) return "[]"
        return Helpers.toString(a.asInstanceOf[Array[AnyRef]], size, charLength)
      }
    }
    // unreachable
    null
  }

  private def toArrayInternal(a: Array[AnyRef]): Array[AnyRef] = {
    var x = a
    var restartFromHead = true
    while (true) {
      var size = 0
      var p = first()
      restartFromHead = false
      while (p != null && !restartFromHead) {
        val item: E = p.item
        if (item != null) {
          if (x == null) x = new Array[AnyRef](4)
          else if (size == x.length) x = Arrays.copyOf(x, 2 * (size + 4))
          x(size) = item
          size += 1
        }
        if (p eq { p = p.next; p }) restartFromHead = true
      }
      if (!restartFromHead) {
        if (x == null) return new Array[AnyRef](0)
        else if (a != null && size <= a.length) {
          if (a ne x) System.arraycopy(x, 0, a, 0, size)
          if (size < a.length) a(size) = null
          return a
        }
        return if (size == x.length) x
        else Arrays.copyOf(x, size)
      }
    }
    a // unreachable
  }

  override def toArray(): Array[AnyRef] = toArrayInternal(null)

  override def toArray[T <: AnyRef](a: Array[T]): Array[T] = {
    Objects.requireNonNull(a)
    toArrayInternal(a.asInstanceOf[Array[AnyRef]]).asInstanceOf[Array[T]]
  }

  override def iterator(): Iterator[E] = new Itr()

  private class Itr() extends Iterator[E] {
    private var nextNode: Node[E] = _
    private var nextItem: E = _
    private var lastRet: Node[E] = _

    private def _init(): Unit = {
      var restartFromHead = true
      while (restartFromHead) {
        restartFromHead = false
        var h: Node[E] = head
        var p: Node[E] = h
        var q: Node[E] = null
        var break = false
        while (!break) {
          val item: E = p.item
          if (item != null) {
            nextNode = p
            nextItem = item
            break = true
          } else if ({ q = p.next; q == null }) {
            break = true
          } else if (p eq q) {
            restartFromHead = true
            break = true
          }
          if (!break) p = q
        }
        if (!restartFromHead) {
          updateHead(h, p)
          return
        }
      }
    }
    _init()

    override def hasNext(): Boolean = nextItem != null

    override def next(): E = {
      val pred = nextNode
      if (pred == null) throw new NoSuchElementException()
      lastRet = pred
      var item: E = null.asInstanceOf[E]

      var p = succ(pred)
      var q: Node[E] = null
      while (true) {
        if (p == null || { item = p.item; item != null }) {
          nextNode = p
          val x = nextItem
          nextItem = item
          return x
        }
        // unlink deleted nodes
        if ({ q = succ(p); q != null })
          pred.NEXT.compareExchangeStrong(p, q)
        p = q
      }
      // unreachable
      null.asInstanceOf[E]
    }

    // Default implementation of forEachRemaining is "good enough".
    override def remove(): Unit = {
      val l = lastRet
      if (l == null) throw new IllegalStateException()
      // rely on a future traversal to relink.
      l.item = null.asInstanceOf[E]
      lastRet = null
    }
  }

  @throws[java.io.IOException]
  private def writeObject(s: ObjectOutputStream): Unit = {
    // Write out any hidden stuff
    s.defaultWriteObject()
    // Write out all elements in the proper order.
    var p = first()
    while (p != null) {
      val item: E = p.item
      if (item != null) s.writeObject(item)
      p = succ(p)
    }
    // Use trailing null as sentinel
    s.writeObject(null)
  }

  private def readObject(s: ObjectInputStream): Unit = {
    s.defaultReadObject()
    // Read in elements until trailing null sentinel found
    var h: Node[E] = null
    var t: Node[E] = null
    var item: AnyRef = null
    while ({ item = s.readObject; item } != null) {
      @SuppressWarnings(Array("unchecked")) val newNode =
        new Node[E](item.asInstanceOf[E])
      if (h == null) h = { t = newNode; t }
      else t.appendRelaxed({ t = newNode; t })
    }
    if (h == null) h = { t = new Node[E]; t }
    head = h
    tail = t
  }

  private[concurrent] object CLQSpliterator {
    private[concurrent] val MAX_BATCH = 1 << 25 // max batch array size;

  }

  private[concurrent] final class CLQSpliterator extends Spliterator[E] {
    private[concurrent] var current: Node[E] = _ // current node
    private[concurrent] var batch = 0 // batch size for splits
    private[concurrent] var exhausted = false // true when no more nodes

    override def trySplit(): Spliterator[E] = {
      var p: Node[E] = null
      var q: Node[E] = null
      if ({ p = getCurrent(); p } == null || { q = p.next; q } == null)
        return null
      var i = 0
      batch = Math.min(batch + 1, CLQSpliterator.MAX_BATCH)
      val n = batch
      var a: Array[AnyRef] = null
      while ({
        val e: E = p.item
        if (e != null) {
          if (a == null) a = new Array[AnyRef](n)
          a(i) = e
          i += 1
        }
        if (p eq { p = q; p }) p = first()
        p != null && { q = p.next; q } != null && i < n
      }) ()
      setCurrent(p)
      if (i == 0) null
      else
        Spliterators.spliterator(
          a,
          0,
          i,
          Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.CONCURRENT
        )
    }

    override def forEachRemaining(action: Consumer[_ >: E]): Unit = {
      Objects.requireNonNull(action)
      val p: Node[E] = getCurrent()
      if (p != null) {
        current = null
        exhausted = true
        forEachFrom(action, p)
      }
    }

    override def tryAdvance(action: Consumer[_ >: E]): Boolean = {
      Objects.requireNonNull(action)
      var p: Node[E] = getCurrent()
      if (p != null) {
        var e: E = null.asInstanceOf[E]
        while ({
          e = p.item
          if (p eq { p = p.next; p }) p = first()
          e == null && p != null
        }) ()
        setCurrent(p)
        if (e != null) {
          action.accept(e)
          return true
        }
      }
      false
    }

    private def setCurrent(p: Node[E]): Unit = {
      if ({ current = p; current } == null) exhausted = true
    }

    private def getCurrent() = {
      var p: Node[E] = current
      if (p == null && !exhausted) setCurrent({ p = first(); p })
      p
    }

    override def estimateSize(): Long = java.lang.Long.MAX_VALUE

    override def characteristics(): Int =
      Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.CONCURRENT
  }

  override def spliterator(): Spliterator[E] = new CLQSpliterator

  override def removeIf(filter: Predicate[_ >: E]): Boolean = {
    Objects.requireNonNull(filter)
    bulkRemove(filter)
  }

  override def removeAll(c: Collection[_]): Boolean = {
    Objects.requireNonNull(c)
    bulkRemove((e: E) => c.contains(e))
  }

  override def retainAll(c: Collection[_]): Boolean = {
    Objects.requireNonNull(c)
    bulkRemove((e: E) => !c.contains(e))
  }

  override def clear(): Unit = {
    bulkRemove((e: E) => true)
  }

  private def bulkRemove(filter: Predicate[_ >: E]): Boolean = {
    var removed = false
    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false
      var hops = MAX_HOPS
      var p, c: Node[E] = head
      var pred, q: Node[E] = null
      // c will be CASed to collapse intervening dead nodes between
      // pred (or head if null) and p.
      while (p != null && !restartFromHead) {
        q = p.next
        val item = p.item
        var pAlive = item != null
        if (pAlive) {
          if (filter.test(item)) {
            if (p.casItem(item, null.asInstanceOf[E])) removed = true
            // Set pAlive to false to signify that the node is no longer alive
            pAlive = false
          }
        }
        if (pAlive || q == null || { hops -= 1; hops == 0 }) {
          // p might already be self-linked here, but if so:
          // - CASing head will surely fail
          // - CASing pred's next will be useless but harmless.
          if (((c ne p) && !tryCasSuccessor(pred, c, { c = p; p })) || pAlive) {
            // if CAS failed or alive, abandon old pred
            hops = MAX_HOPS
            pred = p
            c = q
          }
        } else if (p eq q) restartFromHead = true
        p = q
      }
    }
    return removed
  }

  /** Runs action on each element found during a traversal starting at p. If p
   *  is null, the action is not run.
   */
  private[concurrent] def forEachFrom(
      action: Consumer[_ >: E],
      _p: Node[E]
  ): Unit = {
    var p = _p
    var pred: Node[E] = null
    while (p != null) {
      var q = p.next
      val item: E = p.item
      if (item != null) {
        action.accept(item)
        pred = p
        p = q
        // continue
      } else {
        val c = p
        var break = false
        while (!break) {
          if (q == null || q.item != null) {
            pred = skipDeadNodes(pred, c, p, q)
            p = q
            break = true
          } else if (p eq ({ p = q; p })) {
            pred = null
            p = head
            break = true
          } else q = p.next
        }
      }
    }
  }

  override def forEach(action: Consumer[_ >: E]): Unit = {
    Objects.requireNonNull(action)
    forEachFrom(action, head)
  }
}
