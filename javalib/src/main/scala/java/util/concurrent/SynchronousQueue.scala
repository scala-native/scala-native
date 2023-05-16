/*
 * Written by Doug Lea, Bill Scherer, and Michael Scott with
 * assistance from members of JCP JSR-166 Expert Group and released to
 * the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent

import java.util
import java.util._
import java.util.concurrent.locks._
import scala.scalanative.libc.atomic.CAtomicRef
import scala.scalanative.libc.atomic.memory_order._
import scala.scalanative.runtime.{fromRawPtr, Intrinsics}

/** A {@linkplain BlockingQueue blocking queue} in which each insert operation
 *  must wait for a corresponding remove operation by another thread, and vice
 *  versa. A synchronous queue does not have any internal capacity, not even a
 *  capacity of one. You cannot {@code peek} at a synchronous queue because an
 *  element is only present when you try to remove it; you cannot insert an
 *  element (using any method) unless another thread is trying to remove it; you
 *  cannot iterate as there is nothing to iterate. The <em>head</em> of the
 *  queue is the element that the first queued inserting thread is trying to add
 *  to the queue; if there is no such queued thread then no element is available
 *  for removal and {@code poll()} will return {@code null}. For purposes of
 *  other {@code Collection} methods (for example {@code contains}), a {@code
 *  SynchronousQueue} acts as an empty collection. This queue does not permit
 *  {@code null} elements.
 *
 *  <p>Synchronous queues are similar to rendezvous channels used in CSP and
 *  Ada. They are well suited for handoff designs, in which an object running in
 *  one thread must sync up with an object running in another thread in order to
 *  hand it some information, event, or task.
 *
 *  <p>This class supports an optional fairness policy for ordering waiting
 *  producer and consumer threads. By default, this ordering is not guaranteed.
 *  However, a queue constructed with fairness set to {@code true} grants
 *  threads access in FIFO order.
 *
 *  <p>This class and its iterator implement all of the <em>optional</em>
 *  methods of the {@link Collection} and {@link Iterator} interfaces.
 *
 *  <p>This class is a member of the <a
 *  href="{@docRoot}/java.base/java/util/package-summary.html#CollectionsFramework">
 *  Java Collections Framework</a>.
 *
 *  @since 1.5
 *  @author
 *    Doug Lea and Bill Scherer and Michael Scott
 *  @param <E>
 *    the type of elements held in this queue
 */
@SerialVersionUID(-3223113410248163686L)
object SynchronousQueue {

  abstract private[concurrent] class Transferer[E] {

    private[concurrent] def transfer(e: E, timed: Boolean, nanos: Long): E
  }

  private[concurrent] val SPIN_FOR_TIMEOUT_THRESHOLD = 1023L

  private[concurrent] object TransferStack {

    private[concurrent] val REQUEST = 0

    private[concurrent] val DATA = 1

    private[concurrent] val FULFILLING = 2

    private[concurrent] def isFulfilling(m: Int): Boolean =
      (m & FULFILLING) != 0

    final private[concurrent] class SNode private[concurrent] (
        var item: Any // data; or null for REQUESTs
    ) extends ForkJoinPool.ManagedBlocker {

      @volatile var next: SNode = _ // next node in stack
      @volatile var `match`: SNode = _ // the node matched to this
      @volatile var waiter: Thread = _ // to control park/unpark

      val atomicMatch = new CAtomicRef[SNode](
        fromRawPtr(Intrinsics.classFieldRawPtr(this, "match"))
      )
      val atomicNext = new CAtomicRef[SNode](
        fromRawPtr(Intrinsics.classFieldRawPtr(this, "next"))
      )
      val atomicWaiter = new CAtomicRef[Thread](
        fromRawPtr(Intrinsics.classFieldRawPtr(this, "waiter"))
      )

      private[concurrent] var mode = 0
      private[concurrent] def casNext(
          cmp: TransferStack.SNode,
          `val`: TransferStack.SNode
      ): Boolean = (cmp eq next) && atomicNext.compareExchangeStrong(cmp, `val`)

      private[concurrent] def tryMatch(s: TransferStack.SNode): Boolean = {
        val m = `match`
        if (m == null)
          if (atomicMatch.compareExchangeStrong(null: SNode, s)) {
            val w = waiter
            if (w != null) LockSupport.unpark(w)
            true
          } else `match` eq s
        else m eq s
      }

      private[concurrent] def tryCancel() =
        atomicMatch.compareExchangeStrong(null: SNode, this)

      private[concurrent] def isCancelled() = `match` eq this

      override final def isReleasable(): Boolean =
        `match` != null || Thread.currentThread().isInterrupted()

      override final def block(): Boolean = {
        while (!isReleasable()) LockSupport.park()
        true
      }

      private[concurrent] def forgetWaiter(): Unit =
        atomicWaiter.store(null: Thread, memory_order_relaxed)
    }

    private[concurrent] def snode(
        _s: TransferStack.SNode,
        e: Any,
        next: TransferStack.SNode,
        mode: Int
    ) = {
      val s =
        if (_s != null) _s
        else new TransferStack.SNode(e)
      s.mode = mode
      s.next = next
      s
    }
  }

  final private[concurrent] class TransferStack[E]
      extends SynchronousQueue.Transferer[E] {
    import TransferStack._

    @volatile private[concurrent] var head: SNode = _
    private val atomicHead = new CAtomicRef[SNode](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "head"))
    )

    private[concurrent] def casHead(
        h: TransferStack.SNode,
        nh: TransferStack.SNode
    ): Boolean = (h eq head) && atomicHead.compareExchangeStrong(h, nh)

    override private[concurrent] def transfer(
        e: E,
        timed: Boolean,
        _nanos: Long
    ): E = {
      /*
       * Basic algorithm is to loop trying one of three actions:
       *
       * 1. If apparently empty or already containing nodes of same
       *    mode, try to push node on stack and wait for a match,
       *    returning it, or null if cancelled.
       *
       * 2. If apparently containing node of complementary mode,
       *    try to push a fulfilling node on to stack, match
       *    with corresponding waiting node, pop both from
       *    stack, and return matched item. The matching or
       *    unlinking might not actually be necessary because of
       *    other threads performing action 3:
       *
       * 3. If top of stack already holds another fulfilling node,
       *    help it out by doing its match and/or pop
       *    operations, and then continue. The code for helping
       *    is essentially the same as for fulfilling, except
       *    that it doesn't return the item.
       */
      var nanos = _nanos
      var s: SNode = null // constructed/reused as needed
      val mode =
        if (e == null) TransferStack.REQUEST
        else TransferStack.DATA

      while (true) {
        val h = head
        if (h == null || h.mode == mode) { // empty or same-mode
          if (timed && nanos <= 0L) { // can't wait
            if (h != null && h.isCancelled())
              casHead(h, h.next) // pop cancelled node
            else
              return null.asInstanceOf[E]
          } else if (casHead(h, { s = snode(s, e, h, mode); s })) {
            val deadline =
              if (timed) System.nanoTime() + nanos
              else 0L
            val w = Thread.currentThread()
            var stat = -1 // -1: may yield, +1: park, else 0
            var m: SNode = null // await fulfill or cancel
            var break = false
            while (!break && { m = s.`match`; m == null }) {
              if ((timed && {
                    nanos = deadline - System.nanoTime()
                    nanos <= 0
                  }) || w.isInterrupted()) {
                if (s.tryCancel()) {
                  clean(s) // wait cancelled
                  return null.asInstanceOf[E]
                }
              } else if ({ m = s.`match`; m != null }) break = true // recheck
              else if (stat <= 0) {
                if (stat < 0 && h == null && (head eq s)) {
                  stat = 0 // yield once if was empty
                  Thread.`yield`()
                } else {
                  stat = 1
                  s.waiter = w // enable signal
                }
              } else if (!timed) {
                LockSupport.setCurrentBlocker(this)
                try ForkJoinPool.managedBlock(s)
                catch { case _: InterruptedException => () }
                LockSupport.setCurrentBlocker(null)
              } else if (nanos > SPIN_FOR_TIMEOUT_THRESHOLD)
                LockSupport.parkNanos(this, nanos)
            }

            if (stat == 1) s.forgetWaiter()
            val result =
              if (mode == TransferStack.REQUEST) m.item
              else s.item
            if (h != null && (h.next eq s)) casHead(h, s.next) // help fulfiller
            return result.asInstanceOf[E]
          }
        } else if (!TransferStack.isFulfilling(h.mode)) { // try to fulfill
          if (h.isCancelled()) // already cancelled
            casHead(h, h.next) // pop and retry
          else if ({
            s = TransferStack.snode(s, e, h, TransferStack.FULFILLING | mode)
            casHead(h, s)
          }) {
            var break = false
            while (!break) { // loop until matched or waiters disappear
              val m = s.next // m is s's match
              if (m == null) { // all waiters are gone
                casHead(s, null) // pop fulfill node
                s = null // use new node next time
                break = true // restart main loop
              }

              val mn = m.next
              if (m.tryMatch(s)) {
                casHead(s, mn) // pop both s and m
                return (if (mode == TransferStack.REQUEST) m.item
                        else s.item).asInstanceOf[E]
              } else { // lost match
                s.casNext(m, mn) // help unlink
              }
            }
          }
        } else { // help a fulfiller
          val m = h.next // m is h's match
          if (m == null) { // waiter is gone
            casHead(h, null) // pop fulfilling node
          } else {
            val mn = m.next
            if (m.tryMatch(h)) // help match
              casHead(h, mn) // pop both h and m
            else
              h.casNext(m, mn)
          }
        }
      }
      null.asInstanceOf[E] // unreachable
    }

    private[concurrent] def clean(s: TransferStack.SNode): Unit = {
      s.item = null // forget item

      s.forgetWaiter()
      /*
       * At worst we may need to traverse entire stack to unlink
       * s. If there are multiple concurrent calls to clean, we
       * might not see s if another thread has already removed
       * it. But we can stop when we see any node known to
       * follow s. We use s.next unless it too is cancelled, in
       * which case we try the node one past. We don't check any
       * further because we don't want to doubly traverse just to
       * find sentinel.
       */
      var past = s.next
      if (past != null && past.isCancelled()) past = past.next
      // Absorb cancelled nodes at head
      var p: SNode = null
      while ({ p = head; p != null } && (p ne past) && p.isCancelled())
        casHead(p, p.next)
      // Unsplice embedded nodes
      while (p != null && (p ne past)) {
        val n = p.next
        if (n != null && n.isCancelled()) p.casNext(n, n.next)
        else p = n
      }
    }
  }

  private[concurrent] object TransferQueue {

    final private[concurrent] class QNode private[concurrent] (
        @volatile var item: Object, // CAS'ed to or from null
        val isData: Boolean
    ) extends ForkJoinPool.ManagedBlocker {
      @volatile private[concurrent] var next: QNode = _ // next node in queue
      @volatile private[concurrent] var waiter: Thread = _

      private val atomicItem = new CAtomicRef[Object](
        fromRawPtr(Intrinsics.classFieldRawPtr(this, "item"))
      )
      private val atomicNext = new CAtomicRef[QNode](
        fromRawPtr(Intrinsics.classFieldRawPtr(this, "next"))
      )
      private val atomicWaiter = new CAtomicRef[Thread](
        fromRawPtr(Intrinsics.classFieldRawPtr(this, "waiter"))
      )

      private[concurrent] def casNext(cmp: QNode, `val`: QNode): Boolean =
        (next eq cmp) && atomicNext.compareExchangeStrong(cmp, `val`)

      private[concurrent] def casItem(cmp: Object, `val`: Object): Boolean =
        (item eq cmp) && atomicItem.compareExchangeStrong(cmp, `val`)

      private[concurrent] def tryCancel(cmp: Object): Boolean =
        atomicItem.compareExchangeStrong(cmp, this)
      private[concurrent] def isCancelled() = item eq this

      private[concurrent] def isOffList = next eq this
      private[concurrent] def forgetWaiter(): Unit =
        atomicWaiter.store(null: Thread, memory_order_relaxed)
      private[concurrent] def isFulfilled() = {
        val x = item
        isData == (x == null) || (x eq this)
      }
      override final def isReleasable(): Boolean = {
        val x = item
        isData == (item == null) || (x eq this) || Thread
          .currentThread()
          .isInterrupted()
      }

      override final def block(): Boolean = {
        while (!isReleasable()) LockSupport.park()
        true
      }
    }
  }
  final private[concurrent] class TransferQueue[
      E <: AnyRef
  ] private[concurrent] ()
      extends SynchronousQueue.Transferer[E] {
    import TransferQueue.QNode

    @volatile private[concurrent] var head: QNode = new QNode(null, false)

    @volatile private[concurrent] var tail: QNode = head

    @volatile private[concurrent] var cleanMe: QNode = _

    private val atomicHead = new CAtomicRef[QNode](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "head"))
    )
    private val atomicTail = new CAtomicRef[QNode](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "tail"))
    )
    private val atomicCleanMe = new CAtomicRef[QNode](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "cleanMe"))
    )

    private[concurrent] def advanceHead(h: QNode, nh: QNode): Unit =
      if ((h eq head) && atomicHead.compareExchangeStrong(h, nh)) {
        h.next = h // forget old next
      }

    private[concurrent] def advanceTail(t: QNode, nt: QNode): Unit =
      if (tail eq t) atomicTail.compareExchangeStrong(t, nt)

    private[concurrent] def casCleanMe(cmp: QNode, `val`: QNode) =
      (cleanMe eq cmp) && atomicCleanMe.compareExchangeStrong(cmp, `val`)

    override private[concurrent] def transfer(
        e: E,
        timed: Boolean,
        _nanos: Long
    ): E = {
      var nanos = _nanos
      /* Basic algorithm is to loop trying to take either of
       * two actions:
       *
       * 1. If queue apparently empty or holding same-mode nodes,
       *    try to add node to queue of waiters, wait to be
       *    fulfilled (or cancelled) and return matching item.
       *
       * 2. If queue apparently contains waiting items, and this
       *    call is of complementary mode, try to fulfill by CAS'ing
       *    item field of waiting node and dequeuing it, and then
       *    returning matching item.
       *
       * In each case, along the way, check for and try to help
       * advance head and tail on behalf of other stalled/slow
       * threads.
       *
       * The loop starts off with a null check guarding against
       * seeing uninitialized head or tail values. This never
       * happens in current SynchronousQueue, but could if
       * callers held non-volatile/final ref to the
       * transferer. The check is here anyway because it places
       * null checks at top of loop, which is usually faster
       * than having them implicitly interspersed.
       */
      var s: QNode = null
      val isData = e != null

      while (true) {
        val t = tail
        val h = head
        var m: QNode = null
        var tn: QNode = null // m is node to fulfill
        if (t == null || h == null) () // inconsistent
        else if ((h eq t) || t.isData == isData) {
          if (t ne tail) () // no-op
          else if ({ tn = t.next; tn != null }) advanceTail(t, tn)
          else if (timed && nanos <= 0L) return null.asInstanceOf[E]
          else if ({
            if (s == null) s = new QNode(e, isData)
            t.casNext(null, s)
          }) {
            advanceTail(t, s)
            val deadline =
              if (timed) System.nanoTime() + nanos
              else 0L
            val w = Thread.currentThread()
            var stat = -1 // same idea as TransferStack
            var item: AnyRef = null
            var break = false
            while (!break && { item = s.item; item eq e }) {
              if ((timed && {
                    nanos = deadline - System.nanoTime()
                    nanos <= 0
                  }) || w.isInterrupted()) {
                if (s.tryCancel(e)) {
                  clean(t, s)
                  return null.asInstanceOf[E]
                }
              } else if ({ item = s.item; item ne e }) break = true
              else if (stat <= 0) {
                if (t.next eq s) {
                  if (stat < 0 && t.isFulfilled()) {
                    stat = 0 // yield once if first
                    Thread.`yield`()
                  } else {
                    stat = 1
                    s.waiter = w
                  }
                }
              } else if (!timed) {
                LockSupport.setCurrentBlocker(this)
                try ForkJoinPool.managedBlock(s)
                catch { case _: InterruptedException => () }
                LockSupport.setCurrentBlocker(null)
              } else if (nanos > SPIN_FOR_TIMEOUT_THRESHOLD)
                LockSupport.parkNanos(this, nanos)
            }

            if (stat == 1) s.forgetWaiter()
            if (!s.isOffList) { // not already unlinked
              advanceHead(t, s) // unlink if head
              if (item != null) // and forget fields
                s.item = s
            }
            return {
              if (item != null) item.asInstanceOf[E]
              else e
            }
          }
        } else if ({ m = h.next; m != null } && (t eq tail) && (h eq head)) {
          var waiter: Thread = null
          val x = m.item
          val fulfilled =
            isData == (x == null) &&
              (x ne m) &&
              m.casItem(x, e)
          advanceHead(h, m) // (help) dequeue

          if (fulfilled) {
            if ({ waiter = m.waiter; waiter != null })
              LockSupport.unpark(waiter)
            return {
              if (x != null) x.asInstanceOf[E]
              else e
            }
          }
        }
      }
      null.asInstanceOf[E] // unreachable
    }

    private[concurrent] def clean(
        pred: QNode,
        s: QNode
    ): Unit = {
      s.forgetWaiter()
      /*
       * At any given time, exactly one node on list cannot be
       * deleted -- the last inserted node. To accommodate this,
       * if we cannot delete s, we save its predecessor as
       * "cleanMe", deleting the previously saved version
       * first. At least one of node s or the node previously
       * saved can always be deleted, so this always terminates.
       */
      var break = false
      while (!break && (pred.next eq s)) {
        // Return early if already unlinked
        val h = head
        val hn = h.next // Absorb cancelled first node as head
        if (hn != null && hn.isCancelled()) {
          advanceHead(h, hn)
          break = true
        } else {
          val t = tail // Ensure consistent read for tail
          if (t eq h) return ()
          val tn = t.next
          if (t ne tail) break = true
          else if (tn != null) {
            advanceTail(t, tn)
            break = true
          } else {
            if (s ne t) { // If not tail, try to unsplice
              val sn = s.next
              if ((sn eq s) || pred.casNext(s, sn)) return ()
            }
            val dp = cleanMe
            if (dp != null) { // Try unlinking previous cancelled node
              val d = dp.next
              lazy val dn = d.next
              if (d == null || // d is gone or
                  (d eq dp) || // d is off list or
                  !d.isCancelled() || // d not cancelled or
                  ((d ne t) && // d not tail and
                    dn != null && //   has successor
                    (dn ne d) && //   that is on list
                    dp.casNext(d, dn))) { // d unspliced
                casCleanMe(dp, null)
              }
              if (dp eq pred) return // s is already saved node
            } else if (casCleanMe(null, pred)) return // Postpone cleaning s
          }
        }
      }
    }
  }

  private[concurrent] class WaitQueue extends Serializable {}
  private[concurrent] class LifoWaitQueue extends SynchronousQueue.WaitQueue {}
  private[concurrent] class FifoWaitQueue extends SynchronousQueue.WaitQueue {}
}

class SynchronousQueue[E <: AnyRef](val fair: Boolean)
    extends util.AbstractQueue[E]
    with BlockingQueue[E]
    with Serializable {
  import SynchronousQueue._
  @volatile private var transferer =
    if (fair) new SynchronousQueue.TransferQueue[E]
    else new SynchronousQueue.TransferStack[E]

  def this() = this(false)

  @throws[InterruptedException]
  override def put(e: E): Unit = {
    if (e == null) throw new NullPointerException
    if (transferer.transfer(e, false, 0) == null) {
      Thread.interrupted()
      throw new InterruptedException
    }
  }

  @throws[InterruptedException]
  override def offer(e: E, timeout: Long, unit: TimeUnit): Boolean = {
    if (e == null) throw new NullPointerException
    else if (transferer.transfer(e, true, unit.toNanos(timeout)) != null) true
    else if (!Thread.interrupted()) false
    else throw new InterruptedException
  }

  override def offer(e: E): Boolean = {
    if (e == null) throw new NullPointerException
    transferer.transfer(e, true, 0) != null
  }

  @throws[InterruptedException]
  override def take(): E = {
    val e = transferer.transfer(null.asInstanceOf[E], false, 0)
    if (e != null) e
    else {
      Thread.interrupted()
      throw new InterruptedException
    }
  }

  @throws[InterruptedException]
  override def poll(timeout: Long, unit: TimeUnit): E = {
    val e =
      transferer.transfer(null.asInstanceOf[E], true, unit.toNanos(timeout))
    if (e != null || !Thread.interrupted()) e
    else throw new InterruptedException
  }

  override def poll(): E = transferer.transfer(null.asInstanceOf[E], true, 0)

  override def isEmpty() = true

  override def size() = 0
  override def remainingCapacity() = 0

  override def clear(): Unit = {}

  override def contains(o: Any) = false

  override def remove(o: Any) = false

  override def containsAll(c: util.Collection[_]): Boolean = c.isEmpty()

  override def removeAll(c: util.Collection[_]) = false
  override def retainAll(c: util.Collection[_]) = false

  override def peek(): E = null.asInstanceOf[E]

  override def iterator(): util.Iterator[E] = Collections.emptyIterator()

  override def spliterator(): Spliterator[E] = Spliterators.emptySpliterator()

  override def toArray() = new Array[AnyRef](0)

  override def toArray[T <: AnyRef](a: Array[T]): Array[T] = {
    if (a.length > 0) a(0) = null.asInstanceOf[T]
    a
  }

  /** Always returns {@code "[]"}.
   *  @return
   *    {@code "[]"}
   */
  override def toString = "[]"

  override def drainTo(c: util.Collection[_ >: E]): Int = {
    Objects.requireNonNull(c)
    if (c eq this) throw new IllegalArgumentException
    var n = 0
    var e: E = null.asInstanceOf[E]
    while ({ e = poll(); e != null }) {
      c.add(e)
      n += 1
    }
    n
  }

  override def drainTo(c: util.Collection[_ >: E], maxElements: Int): Int = {
    Objects.requireNonNull(c)
    if (c eq this) throw new IllegalArgumentException
    var n = 0
    var e: E = null.asInstanceOf[E]
    while (n < maxElements && { e = poll(); e != null }) {
      c.add(e)
      n += 1
    }
    n
  }

  // No support for ObjectInputStream in Scala Native
  // @throws[java.io.IOException]
  // private def writeObject(s: ObjectOutputStream): Unit = {
  //   val fair = transferer.isInstanceOf[SynchronousQueue.TransferQueue[_]]
  //   if (fair) {
  //     qlock = new ReentrantLock(true)
  //     waitingProducers = new SynchronousQueue.FifoWaitQueue
  //     waitingConsumers = new SynchronousQueue.FifoWaitQueue
  //   } else {
  //     qlock = new ReentrantLock
  //     waitingProducers = new SynchronousQueue.LifoWaitQueue
  //     waitingConsumers = new SynchronousQueue.LifoWaitQueue
  //   }
  //   s.defaultWriteObject()
  // }

  //
  // @throws[java.io.IOException]
  // @throws[ClassNotFoundException]
  // private def readObject(s: ObjectInputStream): Unit = {
  //   s.defaultReadObject()
  //   if (waitingProducers.isInstanceOf[SynchronousQueue.FifoWaitQueue])
  //     transferer = new SynchronousQueue.TransferQueue[E]
  //   else transferer = new SynchronousQueue.TransferStack[E]
  // }
}
