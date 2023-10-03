/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util.AbstractQueue
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.LockSupport
import scala.scalanative.libc.atomic.CAtomicRef
import scala.scalanative.libc.atomic.memory_order._
import scala.scalanative.runtime.{fromRawPtr, Intrinsics}
import scala.annotation.tailrec

@SerialVersionUID(-3223113410248163686L) class LinkedTransferQueue[E <: AnyRef]
    extends AbstractQueue[E]
    with TransferQueue[E]
    with Serializable {
  import LinkedTransferQueue._

  @transient @volatile private var head: Node = _
  @transient @volatile private var tail: Node = _
  @transient @volatile private var needSweep: Boolean = _

  val tailAtomic = CAtomicRef[Node](
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "tail"))
  )
  val headAtomic = CAtomicRef[Node](
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

  private def skipDeadNodes(pred: Node, c: Node, p: Node, q: Node) = {
    var _q = q
    if (q == null && c == p) pred
    else {
      if (q == null) {
        // Never unlink trailing node.
        _q = p;
      }
      if (tryCasSuccessor(pred, c, _q) && (pred == null || !pred.isMatched()))
        pred
      else p;
    }
  }

  private def skipDeadNodesNearHead(h: Node, p: Node) = {
    @tailrec def go(p: Node): Node =
      val q = p.next
      if (q == null) p
      else if (!q.isMatched()) q
      else if (p == q) null /* do not casHead */
      else go(q)
    // while (true) {
    //   val q: Node = null;
    //   if ((q = p.next) == null) break;
    //   else if (!q.isMatched()) { p = q; break; }
    //   else if (p == (p = q)) return;
    // }
    val _p = go(p)
    if (_p != null && casHead(h, _p))
      h.selfLink()
  }

  private def xfer(e: E, haveData: Boolean, how: Int, nanos: Long) = {
    if (haveData && (e == null))
      throw NullPointerException()

    @tailrec def restart(s: Node, t: Node, h: Node): E = {
      var _s: Node = s
      var _t: Node = t
      var _h: Node = h
      @tailrec def inner(p: Node): E = {
        var item: Object = _
        if (p.isData != haveData && haveData == ({
              item = p.item; item
            } == null)) {
          if (_h == null) { _h = head; }
          if (p.tryMatch(item, e)) {
            if (_h != p) skipDeadNodesNearHead(_h, p)
            return item.asInstanceOf[E]
          }
        }
        val q = p.next
        if (q == null) {
          if (how == NOW) return e
          if (_s == null) _s = new Node(e)
          if (!p.casNext(null, s)) return inner(p)
          if (p != _t) casTail(_t, _s)
          if (how == ASYNC) return e
          return awaitMatch(_s, p, e, (how == TIMED), nanos)
        }

        if (p == q) restart(_s, _t, _h)
        else inner(q)
      }

      inner(
        if (t != { _t = tail; _t } && _t.isData == haveData) _t
        else { _h = head; _h }
      )
    }

    restart(null, null, null)
  }

  private def awaitMatch(
      s: Node,
      pred: Node,
      e: E,
      timed: Boolean,
      nanos: Long
  ): E = {
    val isData = s.isData
    val deadline = if (timed) System.nanoTime() + nanos else 0L
    val w = Thread.currentThread()

    var stat = -1 // -1: may yield, +1: park, else 0
    var item: Object = _
    var _nanos = nanos

    @tailrec def loop: Option[E] = {
      item = s.item
      if (item != e) return None
      if (needSweep) sweep()
      else if ((timed && _nanos <= 0) || w.isInterrupted()) {
        if (s.casItem(e, if (e == null) s else null)) {
          unsplice(pred, s)
          return Some(e)
        }
      } else if (stat <= 0) {
        if (pred != null && pred.next == s) {
          if (stat < 0 &&
              (pred.isData != isData || pred.isMatched())) {
            stat = 0
            Thread.`yield`()
          } else {
            stat = 1
            s.waiter = w
          }
        }
      } else if ({ item = s.item; item } != e) return None
      else if (!timed) {
        LockSupport.setCurrentBlocker(this)
        try {
          ForkJoinPool.managedBlock(s)
        } catch {
          case cannotHappen: InterruptedException => {}
        }
      } else {
        _nanos = deadline - System.nanoTime()
        if (_nanos > SPIN_FOR_TIMEOUT_THRESHOLD)
          LockSupport.parkNanos(this, _nanos)
      }

      loop
    }

    loop.match
      case None =>
        if (stat == 1) s.waiterAtomic.store(null)
        if (!isData) s.itemAtomic.store(s)
        item.asInstanceOf[E]
      case Some(value) => value
  }

  /* -------------- Traversal methods -------------- */

  final def firstDataNode(): Node = {
    var first: Node = null
    @tailrec def restartFromHead: (Node, Node) = {
      val h = head
      @tailrec def inner(p: Node): (Node, Node) = {
        if (p == null) return (h, p)
        if (p.item != null) {
          if (p.isData) {
            first = p
            return (h, p)
          }
        } else if (!p.isData) return (h, p)
        val q = p.next
        if (q == null) (h, p)
        else if (p == q) restartFromHead
        else inner(q)
      }
      inner(h)
    }
    val (h, p) = restartFromHead
    if (p != h && casHead(h, p)) h.selfLink()
    first
  }

  private def countOfMode(data: Boolean): Int = {
    @tailrec def inner(p: Node, count: Int): Int = {
      if (p == null) return count
      val newCount = if (!p.isMatched()) {
        if (p.isData != data) return 0
        if (count + 1 == Integer.MAX_VALUE) return count
        count + 1
      } else count
      val q = p.next
      if (p == q) inner(head, 0)
      else inner(q, newCount)
    }
    inner(head, 0)
  }

  override def toString(): String = {
    var a: Array[String] = null

    while (true) {
      var charLength = 0
      var size = 0
      var p: Node = head

      while (p != null) {
        val item = p.item

        if (p.isData) {
          if (item != null) {
            if (a == null)
              a = new Array[String](4)
            else if (size == a.length)
              a = java.util.Arrays.copyOf(a, 2 * size)

            val s = item.toString
            a(size) = s
            size += 1
            charLength += s.length
          }
        } else if (item == null) {
          return "[]"
        }

        if (p == p.next) {
          // Continue loop from the head
          p = head
        } else {
          p = p.next
        }
      }

      if (size == 0)
        return "[]"

      return Helpers.toString(a.asInstanceOf[Array[AnyRef]], size, charLength)
    }
    ???
  }

  def unsplice(pred: Node, s: Node): Unit = ???

  private def sweep(): Unit = ???

  // private def xfer(e: E, haveData: Boolean, how: Int, nanos: Long) {
  //       if (haveData && (e == null))
  //           throw NullPointerException()

  //       var s: Node = null
  //       var t: Node = null
  //       var h: Node = null
  //       @tailrec def restart = {
  //         @tailrec def go = {
  //           val p = if (t == tail) { h = head; h } else {

  //           }
  //         }
  //       }

  //     restart: for (Node s = null, t = null, h = null;;) {
  //         for (Node p = (t != (t = tail) && t.isData == haveData) ? t
  //                  : (h = head);; ) {
  //             final Node q; final Object item;
  //             if (p.isData != haveData
  //                 && haveData == ((item = p.item) == null)) {
  //                 if (h == null) h = head;
  //                 if (p.tryMatch(item, e)) {
  //                     if (h != p) skipDeadNodesNearHead(h, p);
  //                     return (E) item;
  //                 }
  //             }
  //             if ((q = p.next) == null) {
  //                 if (how == NOW) return e;
  //                 if (s == null) s = new Node(e);
  //                 if (!p.casNext(null, s)) continue;
  //                 if (p != t) casTail(t, s);
  //                 if (how == ASYNC) return e;
  //                 return awaitMatch(s, p, e, (how == TIMED), nanos);
  //             }
  //             if (p == (p = q)) continue restart;
  //         }
  //     }
  // }
}

@SerialVersionUID(-3223113410248163686L) object LinkedTransferQueue {
  final val SPIN_FOR_TIMEOUT_THRESHOLD = 1023L
  final val SWEEP_THRESHOLD = 32

  @SerialVersionUID(-3223113410248163686L) final class Node private (
      val isData: Boolean,
      @volatile var item: Object
  ) extends ForkJoinPool.ManagedBlocker {
    @volatile var next: Node = null
    @volatile var waiter: Thread = _

    val nextAtomic = CAtomicRef[Node](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "next"))
    )
    val itemAtomic = CAtomicRef[Object](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "item"))
    )
    val waiterAtomic = CAtomicRef[Object](
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
}
