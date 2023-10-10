/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util.AbstractQueue
import java.util.Arrays
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.LockSupport
import scala.scalanative.libc.atomic.CAtomicRef
import scala.scalanative.libc.atomic.memory_order.{
  memory_order_relaxed,
  memory_order_release
}
import scala.scalanative.runtime.{fromRawPtr, Intrinsics}
import java.{util => ju}

@SerialVersionUID(-3223113410248163686L) class LinkedTransferQueue[E <: AnyRef]
    extends AbstractQueue[E]
    with TransferQueue[E]
    with Serializable {
  import LinkedTransferQueue._

  @volatile private[concurrent] var head: Node = _
  @volatile private[concurrent] var tail: Node = _
  @volatile private[concurrent] var needSweep: Boolean = _

  private val tailAtomic = new CAtomicRef[Node](
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "tail"))
  )
  private val headAtomic = new CAtomicRef[Node](
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "head"))
  )

  private def casTail(cmp: Node, `val`: Node) =
    tailAtomic.compareExchangeStrong(cmp, `val`)
  private def casHead(cmp: Node, `val`: Node) =
    headAtomic.compareExchangeStrong(cmp, `val`)

  private def tryCasSuccessor(pred: Node, c: Node, p: Node) = {
    if (pred != null) {
      pred.casNext(c, p)
    } else if (casHead(c, p)) {
      c.selfLink()
      true
    } else {
      false
    }
  }

  private def skipDeadNodes(pred: Node, c: Node, p: Node, q: Node): Node = {
    var _q = q
    if (_q == null) {
      // Never unlink trailing node.
      if (c == p) return pred
      _q = p;
    }
    if (tryCasSuccessor(pred, c, _q) && (pred == null || !pred.isMatched()))
      pred
    else p
  }

  private def skipDeadNodesNearHead(h: Node, p: Node): Unit = {
    var _p = p
    var continueLoop = true
    while (continueLoop) {
      val q = _p.next
      if (q == null) continueLoop = false
      else if (!q.isMatched()) { _p = q; continueLoop = false }
      else {
        if (_p == q) return
        _p = q
      }
    }
    if (casHead(h, _p))
      h.selfLink()
  }

  private def xfer(e: E, haveData: Boolean, how: Int, nanos: Long): E = {
    if (haveData && (e == null))
      throw new NullPointerException()

    var restart = true
    var s: Node = null
    var t: Node = null
    var h: Node = null
    while (true) {
      val old_t = t
      t = tail
      var innerBreak = false
      var p = if (old_t != t && t.isData == haveData) t else { h = head; h }
      while (!innerBreak) {
        var skipRest = false
        var item: Object = null
        if (p.isData != haveData && haveData == {
              item = p.item; item == null
            }) {
          if (h == null) h = head
          if (p.tryMatch(item, e)) {
            if (h != p) skipDeadNodesNearHead(h, p)
            return item.asInstanceOf[E]
          }
        }
        val q = p.next
        if (q == null) {
          if (how == NOW) return e
          if (s == null) s = new Node(e)
          if (!p.casNext(null, s)) skipRest = true
          if (!skipRest) {
            if (p != t) casTail(t, s)
            if (how == ASYNC) return e
            return awaitMatch(s, p, e, (how == TIMED), nanos)
          }
        }
        if (!skipRest) {
          val old_p = p
          p = q
          if (old_p == p) innerBreak = true
        }
      }
    }
    ???
  }

  private def awaitMatch(
      s: Node,
      pred: Node,
      e: E,
      timed: Boolean,
      nanos: Long
  ): E = {
    var _nanos = nanos
    val isData = s.isData
    val deadline = if (timed) System.nanoTime() + _nanos else 0
    val w = Thread.currentThread()
    var stat = -1
    var item: Object = s.item
    var continueLoop = true
    while (item == e && continueLoop) {
      if (needSweep) sweep()
      else if ((timed && _nanos <= 0) || w.isInterrupted()) {
        if (s.casItem(e, if (e == null) s else null)) {
          unsplice(pred, s) // cancelled
          return e
        }
      } else if (stat <= 0) {
        if (pred != null && pred.next == s) {
          if (stat < 0 &&
              (pred.isData != isData || pred.isMatched())) {
            stat = 0 // yield once if first
            Thread.`yield`()
          } else {
            stat = 1
            s.waiter = w // enable unpark
          }
        } // else signal in progress
      } else if ({ item = s.item; item != e }) {
        continueLoop = false
      } else if (!timed) {
        LockSupport.setCurrentBlocker(this)
        try {
          ForkJoinPool.managedBlock(s)
        } catch {
          case cannotHappen: InterruptedException => {}
        }
        LockSupport.setCurrentBlocker(null)
      } else {
        _nanos = deadline - System.nanoTime()
        if (_nanos > SPIN_FOR_TIMEOUT_THRESHOLD)
          LockSupport.parkNanos(this, _nanos)
      }

      item = s.item
    }
    item.asInstanceOf[E]
  }

  /* -------------- Traversal methods -------------- */

  final def firstDataNode(): Node = {
    var first: Node = null
    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false

      var h = head
      var p = h
      var innerBreak = false
      while (p != null && !innerBreak) {
        if (p.item != null) {
          if (p.isData) {
            first = p
            innerBreak = true
          }
        } else if (!p.isData) {
          innerBreak = true
        }
        if (!innerBreak) {
          val q = p.next
          if (q == null) innerBreak = true
          if (!innerBreak && p == q) {
            restartFromHead = true; innerBreak = true
          }
          if (!innerBreak) p = q
        }
      }
    }
    first
  }

  private def countOfMode(data: Boolean): Int = {
    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false
      var count = 0
      var p = head
      var innerBreak = false
      while (p != null && !innerBreak) {
        if (!p.isMatched()) {
          if (p.isData != data)
            return 0
          count += 1
          if (count == Integer.MAX_VALUE)
            innerBreak = true // @see Collection.size()
        }
        if (!innerBreak) {
          val q = p.next
          if (p == q) {
            innerBreak = true
            restartFromHead = true
          } else
            p = q
        }
      }
      if (!restartFromHead) return count
    }
    ???
  }

  override def toString(): String = {
    var a: Array[String] = null

    var restartFromHead = true;
    while (restartFromHead) {
      restartFromHead = false

      var charLength = 0
      var size = 0

      var p = head
      var innerBreak = false
      while (p != null && !innerBreak) {
        val item = p.item
        if (p.isData) {
          if (item != null) {
            if (a == null)
              a = Array.fill(4)("")
            else if (size == a.length)
              a = Arrays.copyOf(a, 2 * size)
            val s = item.toString()
            a(size) = s
            size += 1
            charLength += s.length()
          }
        } else if (item == null) {
          innerBreak = true
        }
        if (!innerBreak) {
          val q = p.next
          if (p == q) {
            restartFromHead = true
            innerBreak = true
          } else p = q
        }
      }

      if (!restartFromHead) {
        if (size == 0)
          return "[]"

        return Helpers.toString(a.asInstanceOf[Array[AnyRef]], size, charLength)
      }
    }
    ???
  }

  private def toArrayInternal(a: Array[Object]): Array[Object] = {
    var x = a

    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false

      var size = 0

      var p = head
      var innerBreak = false
      while (p != null && !innerBreak) {
        val item = p.item
        if (p.isData) {
          if (item != null) {
            if (x == null)
              x = Array.fill[Object](4)(null)
            else if (size == x.length)
              x = Arrays.copyOf(x, 2 * (size + 4))
            x(size) = item
            size += 1
          }
        } else if (item == null) {
          innerBreak = true
        }
        if (!innerBreak) {
          val q = p.next
          if (p == q) {
            restartFromHead = true
            innerBreak = true
          }
          p = q
        }
      }
      if (!restartFromHead) {
        if (x == null)
          return Array()
        else if (a != null && size <= a.length) {
          if (a != x)
            System.arraycopy(x, 0, a, 0, size)
          if (size < a.length)
            a(size) = null
          return a
        }
        return (if (size == x.length) x else Arrays.copyOf(x, size))
      }
    }
    ???
  }

  override def toArray(): Array[Object] = toArrayInternal(null)

  override def toArray[T <: AnyRef](
      a: Array[T]
  ): Array[T] = {
    java.util.Objects.requireNonNull(a)
    toArrayInternal(a.asInstanceOf[Array[Object]])
      .asInstanceOf[Array[T with Object]]
  }

  final class Itr extends ju.Iterator[E] {
    private var nextNode: Node = null
    private var nextItem: E = null.asInstanceOf[E]
    private var lastRet: Node = null
    private var ancestor: Node = null

    private def advance(pred: Node): Unit = {
      var _pred = pred
      var p = if (_pred == null) head else _pred.next
      var c = p
      var innerBreak = false
      while (p != null && !innerBreak) {
        val item = p.item
        if (item != null && p.isData) {
          nextNode = p
          nextItem = item.asInstanceOf[E]
          if (c != p)
            tryCasSuccessor(_pred, c, p)
          return
        } else if (!p.isData && item == null) {
          innerBreak = true
        }
        if (!innerBreak) {
          if (c != p && {
                val old_c = c
                c = p
                !tryCasSuccessor(_pred, old_c, c)
              }) {
            _pred = p
            p = p.next
            c = p
          } else {
            val q = p.next
            if (p == q) {
              _pred = null
              p = head
              c = p
            } else {
              p = q
            }
          }
        }
      }
      nextItem = null.asInstanceOf[E]
      nextNode = null
    }

    advance(null)

    final override def hasNext(): Boolean = nextNode != null

    final override def next() = {
      var p = nextNode
      if (p == null)
        throw new ju.NoSuchElementException()
      val e = nextItem
      lastRet = p
      advance(lastRet)
      e
    }

    override def forEachRemaining(
        action: ju.function.Consumer[_ >: E]
    ): Unit = {
      ju.Objects.requireNonNull(action)
      var q: Node = null
      var p = nextNode
      while (p != null) {
        action.accept(nextItem)
        q = p
        advance(q)
        p = nextNode
      }
      if (q != null)
        lastRet = q
    }

    override def remove(): Unit = {
      val lastRet = this.lastRet
      if (lastRet == null)
        throw new IllegalStateException()
      this.lastRet = null
      if (lastRet.item == null)
        return

      var pred = ancestor
      var p = if (pred == null) head else pred.next
      var c = p
      var q: Node = null
      var innerBreak = false
      while (p != null && !innerBreak) {
        if (p == lastRet) {
          val item = p.item
          if (item != null)
            p.tryMatch(item, null)
          q = p.next
          if (q == null) q = p
          if (c != q)
            tryCasSuccessor(pred, c, q)
          ancestor = pred
          return
        }
        val item = p.item
        val pAlive = item != null && p.isData
        if (pAlive) {
          // exceptionally, nothing to do
        } else if (!p.isData && item == null) {
          innerBreak = true
        }
        if (!innerBreak) {
          if ((c != p && {
                val old_c = c
                c = p
                !tryCasSuccessor(pred, old_c, c)
              }) || pAlive) {
            pred = p
            p = p.next
            c = p
          } else {
            val q = p.next
            if (p == q) {
              pred = null
              p = head
              c = p
            } else {
              p = q
            }
          }
        }
      }
    }
  }

  final class LTQSpliterator extends ju.Spliterator[E] {
    var _current: Node = null
    var batch = 0
    var exhausted = false

    def trySplit(): ju.Spliterator[E] = {
      var p = current()
      if (p == null) return null
      var q = p.next
      if (q == null) return null

      var i = 0
      batch = Math.min(batch + 1, LTQSpliterator.MAX_BATCH)
      var n = batch
      var a: Array[Object] = null
      var continueLoop = true
      while (continueLoop) {
        val item = p.item
        if (p.isData) {
          if (item != null) {
            if (a == null)
              a = Array.fill[Object](n)(null)
            a(i) = item
            i += 1
          }
        } else if (item == null) {
          p = null
          continueLoop = false
        }
        if (continueLoop) {
          if (p == q)
            p = firstDataNode()
          else
            p = q
        }
        if (p == null) continueLoop = false
        if (continueLoop) {
          q = p.next
          if (q == null) continueLoop = false
        }
        if (i >= n) continueLoop = false
      }
      setCurrent(p)
      if (i == 0)
        null
      else
        ju.Spliterators.spliterator(
          a,
          0,
          i,
          (ju.Spliterator.ORDERED | ju.Spliterator.NONNULL | ju.Spliterator.CONCURRENT)
        )
    }

    override def forEachRemaining(
        action: ju.function.Consumer[_ >: E]
    ): Unit = {
      ju.Objects.requireNonNull(action)
      val p = current()
      if (p != null) {
        _current = null
        exhausted = true
        forEachFrom(action, p)
      }
    }

    override def tryAdvance(action: ju.function.Consumer[_ >: E]): Boolean = {
      ju.Objects.requireNonNull(action)
      var p = current()
      while (p != null) {
        var e: E = null.asInstanceOf[E]
        var continueLoop = true
        while (continueLoop) {
          val item = p.item
          val isData = p.isData
          val q = p.next
          p = if (p == q) head else q
          if (isData) {
            if (item != null) {
              e = item.asInstanceOf[E]
              continueLoop = false
            }
          } else if (item == null) {
            p = null
          }
          if (p == null) continueLoop = false
        }
        setCurrent(p)
        if (e != null) {
          action.accept(e)
          return true
        }
      }
      false
    }

    private def setCurrent(p: Node): Unit = {
      _current = p
      if (_current == null)
        exhausted = true
    }

    private def current(): Node = {
      var p = _current
      if (p == null && !exhausted) {
        p = firstDataNode()
        setCurrent(p)
      }
      p
    }

    override def estimateSize() = Long.MaxValue

    override def characteristics(): Int =
      ju.Spliterator.ORDERED | ju.Spliterator.NONNULL | ju.Spliterator.CONCURRENT
  }

  object LTQSpliterator {
    val MAX_BATCH = 1 << 25
  }

  override def spliterator(): ju.Spliterator[E] = new LTQSpliterator()

  /* -------------- Removal methods -------------- */

  def unsplice(pred: Node, s: Node): Unit = {
    s.waiter = null // disable signals
    if (pred != null && pred.next == s) {
      val n = s.next
      if (n == null || (n != s && pred.casNext(s, n) && pred.isMatched())) {
        var continueLoop = true
        while (continueLoop) {
          val h = head
          if (h == pred || h == s)
            return
          if (!h.isMatched())
            continueLoop = false
          if (continueLoop) {
            val hn = h.next
            if (hn == null)
              return
            if (hn != h && casHead(h, hn))
              h.selfLink()
          }
        }
        if (pred.next != pred && s.next != s)
          needSweep = true
      }
    }
  }

  private def sweep(): Unit = {
    needSweep = false
    var p = head
    var continueLoop = true
    while (p != null && continueLoop) {
      val s = p.next
      if (s == null) continueLoop = false
      if (continueLoop) {
        if (!s.isMatched())
          // Unmatched nodes are never self-linked
          p = s
        else {
          val n = s.next
          if (n == null) // trailing node is pinned
            continueLoop = false
          else if (s == n) // stale
            // No need to also check for p == s, since that implies s == n
            p = head
          else p.casNext(s, n)
        }
      }
    }
  }

  /* -------------- Constructors -------------- */

  def this(c: ju.Collection[_ <: E]) = {
    this()
    var h: Node = null
    var t: Node = null
    val it = c.iterator()
    while (it.hasNext()) {
      val e = it.next()
      val newNode = new Node(ju.Objects.requireNonNull(e))
      if (h == null) {
        t = newNode
        h = t
      } else {
        t.appendRelaxed(newNode)
        t = newNode
      }
    }
    if (h == null) {
      t = new Node()
      h = t
    }
    head = h
    tail = t
  }

  head = new Node()
  tail = head

  /* -------------------- Other ------------------- */

  override def put(e: E): Unit = xfer(e, true, ASYNC, 0)

  override def offer(e: E, timeout: Long, unit: TimeUnit): Boolean = {
    xfer(e, true, ASYNC, 0)
    true
  }

  override def offer(e: E) = {
    xfer(e, true, ASYNC, 0)
    true
  }

  override def add(e: E): Boolean = {
    xfer(e, true, ASYNC, 0)
    true
  }

  override def tryTransfer(e: E): Boolean = {
    return xfer(e, true, NOW, 0L) == null
  }

  override def transfer(e: E): Unit = {
    if (xfer(e, true, SYNC, 0L) != null) {
      Thread.interrupted() // failure possible only due to interrupt
      throw new InterruptedException()
    }
  }

  override def tryTransfer(e: E, timeout: Long, unit: TimeUnit): Boolean = {
    if (xfer(e, true, TIMED, unit.toNanos(timeout)) == null)
      true
    else if (!Thread.interrupted())
      false
    else throw new InterruptedException()
  }

  override def take(): E = {
    val e = xfer(null.asInstanceOf[E], false, SYNC, 0L)
    if (e != null)
      e
    else {
      Thread.interrupted()
      throw new InterruptedException()
    }
  }

  override def poll(timeout: Long, unit: TimeUnit): E = {
    val e = xfer(null.asInstanceOf[E], false, TIMED, unit.toNanos(timeout))
    if (e != null || !Thread.interrupted())
      e
    else throw new InterruptedException()
  }

  override def poll(): E = xfer(null.asInstanceOf[E], false, NOW, 0L)

  override def drainTo(c: ju.Collection[_ >: E]): Int = {
    ju.Objects.requireNonNull(c)
    if (c == this)
      throw new IllegalArgumentException()
    var n = 0
    var e = poll()
    while (e != null) {
      c.add(e)
      n += 1
      e = poll()
    }
    n
  }

  override def drainTo(c: ju.Collection[_ >: E], maxElements: Int): Int = {
    ju.Objects.requireNonNull(c)
    if (c == this)
      throw new IllegalArgumentException()
    var n = 0
    var innerBreak = false
    while (n < maxElements && !innerBreak) {
      val e = poll()
      if (e == null)
        innerBreak = true
      else {
        c.add(e)
        n += 1
      }
    }
    n
  }

  override def iterator(): ju.Iterator[E] = new Itr()

  override def peek(): E = {
    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false
      var p = head
      var innerBreak = false
      while (p != null && !innerBreak) {
        val item = p.item
        if (p.isData) {
          if (item != null) {
            return item.asInstanceOf[E]
          }
        } else if (item == null) {
          innerBreak = true
        }
        if (!innerBreak) {
          val q = p.next
          if (p == q) {
            restartFromHead = true
            innerBreak = true
          } else p = q
        }
      }
      if (!restartFromHead) return null.asInstanceOf[E]
    }
    ???
  }

  override def isEmpty(): Boolean = firstDataNode() == null

  override def hasWaitingConsumer(): Boolean = {
    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false
      var p = head
      var innerBreak = false
      while (p != null && !innerBreak) {
        val item = p.item
        if (p.isData) {
          if (item != null) {
            innerBreak = true
          }
        } else if (item == null) {
          return true
        }
        if (!innerBreak) {
          val q = p.next
          if (p == q) {
            restartFromHead = true
            innerBreak = true
          } else p = q
        }
      }
      if (!restartFromHead) return false
    }
    ???
  }

  override def size(): Int = countOfMode(true)

  override def getWaitingConsumerCount(): Int = countOfMode(false)

  override def remove(o: Any): Boolean = {
    if (o == null) return false
    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false

      var p = head
      var pred: Node = null
      var innerBreak = false
      while (p != null && !innerBreak) {
        var q = p.next
        val item = p.item
        var skipRest = false
        if (item != null) {
          if (p.isData) {
            if (item.equals(o) && p.tryMatch(item, null)) {
              skipDeadNodes(pred, p, p, q)
              return true
            }
            pred = p
            p = q
            skipRest = true
          }
        } else if (!p.isData) innerBreak = true
        if (!skipRest && !innerBreak) {
          var c = p
          var cBreak = false
          while (!cBreak) {
            if (q == null || !q.isMatched()) {
              pred = skipDeadNodes(pred, c, p, q)
              p = q
              cBreak = true
            } else {
              val old_p = p
              p = q
              if (old_p == p) {
                innerBreak = true
                cBreak = true
                restartFromHead = true
              }
            }
            q = p.next
          }
        }
      }
    }
    false
  }

  override def contains(o: Any): Boolean = {
    if (o == null) return false

    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false

      var p = head
      var pred: Node = null
      var pLoopBreak = false
      while (p != null && !pLoopBreak) {
        var q = p.next
        val item = p.item
        var pLoopSkip = false
        if (item != null) {
          if (p.isData) {
            if (o.equals(item))
              return true
            pred = p
            p = q
            pLoopSkip = true
          }
        } else if (!p.isData) pLoopBreak = true
        if (!pLoopSkip && !pLoopBreak) {
          val c = p
          var qLoopBreak = false
          while (!qLoopBreak) {
            if (q == null || !q.isMatched()) {
              pred = skipDeadNodes(pred, c, p, q)
              p = q
              qLoopBreak = true
            }
            if (!qLoopBreak) {
              val old_p = p
              p = q
              if (old_p == p) {
                pLoopBreak = true
                qLoopBreak = true
                restartFromHead = true
              }
              q = p.next
            }
          }
        }
      }
      if (!restartFromHead) return false
    }
    ???
  }

  override def remainingCapacity(): Int = Integer.MAX_VALUE

  // No ObjectInputStream in ScalaNative
  // private def writeObject(s: java.io.ObjectOutputStream): Unit
  // private def readObject(s: java.io.ObjectInputStream): Unit

  override def removeIf(filter: ju.function.Predicate[_ >: E]): Boolean = {
    ju.Objects.requireNonNull(filter)
    bulkRemove(filter)
  }

  override def removeAll(c: ju.Collection[_]): Boolean = {
    ju.Objects.requireNonNull(c)
    bulkRemove(e => c.contains(e))
  }

  override def retainAll(c: ju.Collection[_]): Boolean = {
    ju.Objects.requireNonNull(c)
    bulkRemove(e => !c.contains(e))
  }

  override def clear(): Unit = bulkRemove(_ => true)

  private def bulkRemove(filter: ju.function.Predicate[_ >: E]): Boolean = {
    var removed = false

    var restartFromHead = true
    while (restartFromHead) {
      restartFromHead = false

      var hops = MAX_HOPS
      // c will be CASed to collapse intervening dead nodes between
      // pred (or head if null) and p.
      var p = head
      var c = p
      var pred: Node = null
      var innerBreak = false
      while (p != null && !innerBreak) {
        val q = p.next
        val item = p.item
        var pAlive = item != null && p.isData
        if (pAlive) {
          if (filter.test(item.asInstanceOf[E])) {
            if (p.tryMatch(item, null))
              removed = true
            pAlive = false
          }
        } else if (!p.isData && item == null)
          innerBreak = true
        if (!innerBreak) {
          if (pAlive || q == null || { hops -= 1; hops } == 0) {
            // p might already be self-linked here, but if so:
            // - CASing head will surely fail
            // - CASing pred's next will be useless but harmless.
            val old_c = c
            if ((c != p && {
                  c = p; !tryCasSuccessor(pred, old_c, c)
                }) || pAlive) {
              // if CAS failed or alive, abandon old pred
              hops = MAX_HOPS
              pred = p
              c = q
            }
          } else if (p == q) {
            innerBreak = true
            restartFromHead = true
          }
          p = q
        }
      }
    }
    removed
  }

  def forEachFrom(action: ju.function.Consumer[_ >: E], p: Node): Unit = {
    var _p = p
    var pred: Node = null
    var continueLoop = true
    while (_p != null && continueLoop) {
      var q = _p.next
      val item = _p.item
      var continueRest = true
      if (item != null) {
        if (_p.isData) {
          action.accept(item.asInstanceOf[E])
          pred = _p
          _p = q
          continueRest = false
        }
      } else if (!_p.isData)
        continueLoop = false
      if (continueLoop && continueRest) {
        var c = _p
        var continueInner = true
        while (continueInner) {
          if (q == null || !q.isMatched()) {
            pred = skipDeadNodes(pred, c, _p, q)
            _p = q
            continueInner = false
          }
          if (continueInner) {
            val old_p = _p
            _p = q
            if (_p == old_p) { pred = null; _p = head; continueInner = false }
            q = _p.next
          }
        }
      }
    }
  }

  override def forEach(action: ju.function.Consumer[_ >: E]): Unit = {
    ju.Objects.requireNonNull(action)
    forEachFrom(action, head)
  }
}

@SerialVersionUID(-3223113410248163686L) object LinkedTransferQueue {
  final val SPIN_FOR_TIMEOUT_THRESHOLD = 1023L
  final val SWEEP_THRESHOLD = 32

  /** Tolerate this many consecutive dead nodes before CAS-collapsing. Amortized
   *  cost of clear() is (1 + 1/MAX_HOPS) CASes per element.
   */
  private final val MAX_HOPS = 8

  @SerialVersionUID(-3223113410248163686L) final class Node private (
      val isData: Boolean,
      @volatile var item: Object
  ) extends ForkJoinPool.ManagedBlocker {
    @volatile var next: Node = null
    @volatile var waiter: Thread = _

    val nextAtomic = new CAtomicRef[Node](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "next"))
    )
    val itemAtomic = new CAtomicRef[Object](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "item"))
    )
    val waiterAtomic = new CAtomicRef[Object](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "waiter"))
    )

    def this(item: Object) = {
      this(item != null, item)
    }

    def this() = {
      this(true, null)
    }

    def casNext(cmp: Node, `val`: Node) =
      nextAtomic.compareExchangeStrong(cmp, `val`)
    def casItem(cmp: Object, `val`: Object) =
      itemAtomic.compareExchangeStrong(cmp, `val`)

    def selfLink(): Unit =
      nextAtomic.store(this, memory_order_release)

    def appendRelaxed(next: Node): Unit =
      nextAtomic.store(next, memory_order_relaxed)

    def isMatched(): Boolean = isData == (item == null)

    def tryMatch(cmp: Object, `val`: Object) = {
      if (casItem(cmp, `val`)) {
        LockSupport.unpark(waiter)
        true
      } else { false }
    }

    def cannotPrecede(haveData: Boolean) = {
      val d = isData
      d != haveData && d != (item == null)
    }

    def isReleasable() =
      (isData == (item == null)) || Thread.currentThread().isInterrupted()

    def block() = {
      while (!isReleasable()) LockSupport.park()
      true
    }
  }

  /* Possible values for "how" argument in xfer method. */
  private final val NOW: Int = 0; // for untimed poll, tryTransfer
  private final val ASYNC: Int = 1; // for offer, put, add
  private final val SYNC: Int = 2; // for transfer, take
  private final val TIMED: Int = 3; // for timed poll, tryTransfer

  // Reduce the risk of rare disastrous classloading in first call to
  // LockSupport.park: https://bugs.openjdk.java.net/browse/JDK-8074773
  locally { val _ = LockSupport.getClass }
}
