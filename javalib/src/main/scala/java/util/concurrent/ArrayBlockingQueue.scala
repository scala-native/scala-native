/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent

import java.lang.ref.WeakReference
import java.util
import java.util._
import java.util.concurrent.locks._
import java.util.function._
import scala.annotation.tailrec

@SerialVersionUID(-817911632652898426L)
object ArrayBlockingQueue {

  private[concurrent] def inc(i: Int, modulus: Int) = {
    val j = i + 1
    if (j >= modulus) 0
    else j
  }

  private[concurrent] def dec(i: Int, modulus: Int) = {
    val j = i - 1
    if (j < 0) modulus - 1
    else j
  }

  private[concurrent] def itemAt[E](items: Array[AnyRef], i: Int) =
    items(i).asInstanceOf[E]

  /** Nulls out slots starting at array index i, upto index end. Condition i ==
   *  end means "full" - the entire array is cleared.
   */
  private def circularClear(items: Array[AnyRef], i: Int, end: Int): Unit = {
    // assert 0 <= i && i < items.length;
    // assert 0 <= end && end < items.length;
    val to = if (i < end) end else items.length
    for (i <- i until to) items(i) = null
    if (to != end)
      for (i <- 0 until end) items(i) = null
  }

  private def nBits(n: Int) =
    new Array[Long](((n - 1) >> 6) + 1)

  private def setBit(bits: Array[Long], i: Int): Unit = {
    bits(i >> 6) |= 1L << i
  }

  private def isClear(bits: Array[Long], i: Int) =
    (bits(i >> 6) & (1L << i)) == 0
}

@SerialVersionUID(-817911632652898426L)
class ArrayBlockingQueue[E <: AnyRef](val capacity: Int, val fair: Boolean)
    extends util.AbstractQueue[E]
    with BlockingQueue[E]
    with Serializable {
  import ArrayBlockingQueue._

  if (capacity <= 0) throw new IllegalArgumentException

  private[concurrent] final val items = new Array[AnyRef](capacity)

  private[concurrent] var takeIndex = 0

  private[concurrent] var putIndex = 0

  private[concurrent] var count = 0

  private[concurrent] var itrs: Itrs = _

  final private[concurrent] val lock: ReentrantLock = new ReentrantLock(fair)

  final private val notEmpty: Condition = lock.newCondition()

  final private val notFull: Condition = lock.newCondition()

  final private[concurrent] def itemAt(i: Int) = items(i).asInstanceOf[E]

  private def enqueue(e: E): Unit = {
    // assert lock.isHeldByCurrentThread();
    // assert lock.getHoldCount() == 1;
    // assert items[putIndex] == null;
    val items = this.items
    items(putIndex) = e
    if ({ putIndex += 1; putIndex } == items.length) putIndex = 0
    count += 1
    notEmpty.signal()
    // checkInvariants();
  }

  private def dequeue() = {
    val items = this.items
    val e = items(takeIndex).asInstanceOf[E]
    items(takeIndex) = null
    takeIndex += 1
    if (takeIndex == items.length) takeIndex = 0
    count -= 1
    if (itrs != null) itrs.elementDequeued()
    notFull.signal()
    e
  }

  private[concurrent] def removeAt(removeIndex: Int): Unit = {
    // assert items[removeIndex] != null;
    // assert removeIndex >= 0 && removeIndex < items.length;
    val items = this.items
    if (removeIndex == takeIndex) { // removing front item; just advance
      items(takeIndex) = null
      takeIndex += 1
      if (takeIndex == items.length) takeIndex = 0
      count -= 1
      if (itrs != null) itrs.elementDequeued()
    } else { // an "interior" remove
      // slide over all others up through putIndex.
      var i = removeIndex
      val putIndex = this.putIndex
      var break = false
      while (!break) {
        val pred = i
        i += 1
        if (i == items.length) i = 0
        if (i == putIndex) {
          items(pred) = null
          this.putIndex = pred
          break = true
        } else items(pred) = items(i)
      }
      count -= 1
      if (itrs != null) itrs.removedAt(removeIndex)
    }
    notFull.signal()
  }

  def this(capacity: Int) = this(capacity, false)

  def this(capacity: Int, fair: Boolean, c: util.Collection[_ <: E]) = {
    this(capacity, fair)
    val lock = this.lock
    lock.lock() // Lock only for visibility, not mutual exclusion

    try {
      val items = this.items
      var i = 0
      val it = c.iterator()
      try
        while (it.hasNext()) {
          val e = it.next()
          items(i) = Objects.requireNonNull(e)
          i += 1
        }
      catch {
        case ex: ArrayIndexOutOfBoundsException =>
          throw new IllegalArgumentException
      }
      count = i
      putIndex =
        if (i == capacity) 0
        else i
    } finally lock.unlock()
  }

  override def add(e: E): Boolean = super.add(e)

  override def offer(e: E): Boolean = {
    Objects.requireNonNull(e)
    val lock = this.lock
    lock.lock()
    try
      if (count == items.length) false
      else {
        enqueue(e)
        true
      }
    finally lock.unlock()
  }

  @throws[InterruptedException]
  override def put(e: E): Unit = {
    Objects.requireNonNull(e)
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      while (count == items.length) notFull.await()
      enqueue(e)
    } finally lock.unlock()
  }

  @throws[InterruptedException]
  override def offer(e: E, timeout: Long, unit: TimeUnit): Boolean = {
    Objects.requireNonNull(e)
    var nanos = unit.toNanos(timeout)
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      while (count == items.length) {
        if (nanos <= 0L) return false
        nanos = notFull.awaitNanos(nanos)
      }
      enqueue(e)
      true
    } finally lock.unlock()
  }

  override def poll(): E = {
    val lock = this.lock
    lock.lock()
    try
      if (count == 0) null.asInstanceOf[E]
      else dequeue()
    finally lock.unlock()
  }

  @throws[InterruptedException]
  override def take(): E = {
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      while (count == 0) notEmpty.await()
      dequeue()
    } finally lock.unlock()
  }

  @throws[InterruptedException]
  override def poll(timeout: Long, unit: TimeUnit): E = {
    var nanos = unit.toNanos(timeout)
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      while ({ count == 0 }) {
        if (nanos <= 0L) return null.asInstanceOf[E]
        nanos = notEmpty.awaitNanos(nanos)
      }
      dequeue()
    } finally lock.unlock()
  }

  override def peek(): E = {
    val lock = this.lock
    lock.lock()
    try itemAt(takeIndex) // null when queue is empty

    finally lock.unlock()
  }

  override def size(): Int = {
    val lock = this.lock
    lock.lock()
    try count
    finally lock.unlock()
  }

  override def remainingCapacity(): Int = {
    val lock = this.lock
    lock.lock()
    try items.length - count
    finally lock.unlock()
  }

  override def remove(o: Any): Boolean = {
    if (o == null) return false
    val lock = this.lock
    lock.lock()
    try {
      if (count > 0) {
        val items = this.items
        var i = takeIndex
        val end = putIndex
        var to =
          if (i < end) end
          else items.length
        while (true) {
          while ({ i < to }) {
            if (o == items(i)) {
              removeAt(i)
              return true
            }
            i += 1
          }
          if (to == end) return false

          i = 0
          to = end
        }
      }
      false
    } finally lock.unlock()
  }

  override def contains(o: Any): Boolean = {
    if (o == null) return false
    val lock = this.lock
    lock.lock()
    try {
      if (count > 0) {
        val items = this.items
        var i = takeIndex
        val end = putIndex
        var to =
          if (i < end) end
          else items.length
        while ({ true }) {
          while ({ i < to }) {
            if (o == items(i)) return true
            i += 1
          }
          if (to == end) return false

          i = 0
          to = end
        }
      }
      false
    } finally lock.unlock()
  }

  /** Returns an array containing all of the elements in this queue, in proper
   *  sequence.
   *
   *  <p>The returned array will be "safe" in that no references to it are
   *  maintained by this queue. (In other words, this method must allocate a new
   *  array). The caller is thus free to modify the returned array.
   *
   *  <p>This method acts as bridge between array-based and collection-based
   *  APIs.
   *
   *  @return
   *    an array containing all of the elements in this queue
   */
  override def toArray(): Array[AnyRef] = {
    val lock = this.lock
    lock.lock()
    try {
      val items = this.items
      val end = takeIndex + count
      val a = util.Arrays.copyOfRange(items, takeIndex, end)
      if (end != putIndex)
        System.arraycopy(items, 0, a, items.length - takeIndex, putIndex)
      a
    } finally lock.unlock()
  }

  override def toArray[T <: AnyRef](_a: Array[T]): Array[T] = {
    var a: Array[T] = _a
    val lock = this.lock
    lock.lock()
    try {
      val items = this.items
      val count = this.count
      val firstLeg = Math.min(items.length - takeIndex, count)
      if (a.length < count)
        a = util.Arrays
          .copyOfRange(items, takeIndex, takeIndex + count, a.getClass())
          .asInstanceOf[Array[T]]
      else {
        System.arraycopy(items, takeIndex, a, 0, firstLeg)
        if (a.length > count) a(count) = null.asInstanceOf[T]
      }
      if (firstLeg < count) System.arraycopy(items, 0, a, firstLeg, putIndex)
      a
    } finally lock.unlock()
  }

  override def toString(): String = Helpers.collectionToString(this)

  override def clear(): Unit = {
    val lock = this.lock
    lock.lock()
    try {
      var k = count
      if (k > 0) {
        circularClear(items, takeIndex, putIndex)
        takeIndex = putIndex
        count = 0
        if (itrs != null) itrs.queueIsEmpty()
        while (k > 0 && lock.hasWaiters(notFull)) {
          notFull.signal()
          k -= 1
        }
      }
    } finally lock.unlock()
  }

  override def drainTo(c: util.Collection[_ >: E]): Int =
    drainTo(c, Integer.MAX_VALUE)

  override def drainTo(c: util.Collection[_ >: E], maxElements: Int): Int = {
    Objects.requireNonNull(c)
    if (c eq this) throw new IllegalArgumentException
    if (maxElements <= 0) return 0
    val items = this.items
    val lock = this.lock
    lock.lock()
    try {
      val n = Math.min(maxElements, count)
      var take = takeIndex
      var i = 0
      try {
        while (i < n) {
          val e = items(take).asInstanceOf[E]
          c.add(e)
          items(take) = null
          if ({ take += 1; take } == items.length) take = 0
          i += 1
        }
        n
      } finally {
        // Restore invariants even if c.add() threw
        if (i > 0) {
          count -= i
          takeIndex = take
          if (itrs != null)
            if (count == 0) itrs.queueIsEmpty()
            else if (i > take) itrs.takeIndexWrapped()

          while ({ i > 0 && lock.hasWaiters(notFull) }) {
            notFull.signal()
            i -= 1
          }
        }
      }
    } finally lock.unlock()
  }

  /** Returns an iterator over the elements in this queue in proper sequence.
   *  The elements will be returned in order from first (head) to last (tail).
   *
   *  <p>The returned iterator is <a
   *  href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
   *
   *  @return
   *    an iterator over the elements in this queue in proper sequence
   */
  override def iterator(): Iterator[E] = new Itr

  /** Shared data between iterators and their queue, allowing queue
   *  modifications to update iterators when elements are removed.
   *
   *  This adds a lot of complexity for the sake of correctly handling some
   *  uncommon operations, but the combination of circular-arrays and supporting
   *  interior removes (i.e., those not at head) would cause iterators to
   *  sometimes lose their places and/or (re)report elements they shouldn't. To
   *  avoid this, when a queue has one or more iterators, it keeps iterator
   *  state consistent by:
   *
   *  (1) keeping track of the number of "cycles", that is, the number of times
   *  takeIndex has wrapped around to 0. (2) notifying all iterators via the
   *  callback removedAt whenever an interior element is removed (and thus other
   *  elements may be shifted).
   *
   *  These suffice to eliminate iterator inconsistencies, but unfortunately add
   *  the secondary responsibility of maintaining the list of iterators. We
   *  track all active iterators in a simple linked list (accessed only when the
   *  queue's lock is held) of weak references to The list is cleaned up using 3
   *  different mechanisms:
   *
   *  (1) Whenever a new iterator is created, do some O(1) checking for stale
   *  list elements.
   *
   *  (2) Whenever takeIndex wraps around to 0, check for iterators that have
   *  been unused for more than one wrap-around cycle.
   *
   *  (3) Whenever the queue becomes empty, all iterators are notified and this
   *  entire data structure is discarded.
   *
   *  So in addition to the removedAt callback that is necessary for
   *  correctness, iterators have the shutdown and takeIndexWrapped callbacks
   *  that help remove stale iterators from the list.
   *
   *  Whenever a list element is examined, it is expunged if either the GC has
   *  determined that the iterator is discarded, or if the iterator reports that
   *  it is "detached" (does not need any further state updates). Overhead is
   *  maximal when takeIndex never advances, iterators are discarded before they
   *  are exhausted, and all removals are interior removes, in which case all
   *  stale iterators are discovered by the GC. But even in this case we don't
   *  increase the amortized complexity.
   *
   *  Care must be taken to keep list sweeping methods from reentrantly invoking
   *  another such method, causing subtle corruption bugs.
   */
  private[concurrent] object Itrs {
    private val SHORT_SWEEP_PROBES = 4
    private val LONG_SWEEP_PROBES = 16
  }
  private[concurrent] class Itrs private[concurrent] (initial: Itr) {
    register(initial)

    private var head: Node = _

    private var sweeper: Node = _

    private[concurrent] var cycles = 0

    private[concurrent] class Node private[concurrent] (
        val iterator: Itr,
        var next: Node
    ) extends WeakReference[Itr](iterator) {}

    private[concurrent] def doSomeSweeping(tryHarder: Boolean): Unit = {
      // assert head != null;
      val probes =
        if (tryHarder) Itrs.LONG_SWEEP_PROBES
        else Itrs.SHORT_SWEEP_PROBES
      var o: Node = null
      var p: Node = null
      val sweeper = this.sweeper
      var passedGo = false // to limit search to one full sweep
      if (sweeper == null) {
        o = null
        p = head
        passedGo = true
      } else {
        o = sweeper
        p = o.next
        passedGo = false
      }

      @annotation.tailrec
      def loop(probes: Int): Unit = {
        if (probes > 0) {
          if (p == null && passedGo) ()
          else {
            if (p == null) {
              o = null
              p = head
              passedGo = true
            }
            val it = p.get()
            val next = p.next
            val nextProbes =
              if (it == null || it.isDetached) {
                // found a discarded/exhausted iterator
                // unlink p
                p.clear()
                p.next = null
                if (o == null) {
                  head = next
                  if (next == null) {
                    // We've run out of iterators to track; retire
                    itrs = null
                    return ()
                  }
                } else o.next = next
                Itrs.LONG_SWEEP_PROBES // "try harder"
              } else {
                o = p
                probes - 1
              }
            p = next
            loop(nextProbes)
          }
        }
      }

      loop(probes)
      this.sweeper =
        if (p == null) null
        else o
    }

    private[concurrent] def register(itr: Itr): Unit = {
      head = new Node(itr, head)
    }

    private[concurrent] def takeIndexWrapped(): Unit = {
      cycles += 1
      var o: Node = null
      var p: Node = head
      while (p != null) {
        val it = p.get()
        val next = p.next
        if (it == null || it.takeIndexWrapped) {
          // assert it == null || it.isDetached();
          p.clear()
          p.next = null
          if (o == null) head = next
          else o.next = next
        } else o = p
        p = next
      }
      if (head == null) { // no more iterators to track
        itrs = null
      }
    }

    private[concurrent] def removedAt(removedIndex: Int): Unit = {
      var o: Node = null
      var p: Node = head
      while (p != null) {
        val it = p.get()
        val next = p.next
        if (it == null || it.removedAt(removedIndex)) {
          p.clear()
          p.next = null
          if (o == null) head = next
          else o.next = next
        } else o = p
        p = next
      }
      if (head == null) {
        itrs = null
      }
    }

    private[concurrent] def queueIsEmpty(): Unit = {
      var p = head
      while (p != null) {
        val it = p.get()
        if (it != null) {
          p.clear()
          it.shutdown()
        }

        p = p.next
      }
      head = null
      itrs = null
    }

    private[concurrent] def elementDequeued(): Unit = {
      if (count == 0) queueIsEmpty()
      else if (takeIndex == 0) takeIndexWrapped()
    }
  }

  /** Iterator for
   *
   *  To maintain weak consistency with respect to puts and takes, we read ahead
   *  one slot, so as to not report hasNext true but then not have an element to
   *  return.
   *
   *  We switch into "detached" mode (allowing prompt unlinking from itrs
   *  without help from the GC) when all indices are negative, or when hasNext
   *  returns false for the first time. This allows the iterator to track
   *  concurrent updates completely accurately, except for the corner case of
   *  the user calling Iterator.remove() after hasNext() returned false. Even in
   *  this case, we ensure that we don't remove the wrong element by keeping
   *  track of the expected element to remove, in lastItem. Yes, we may fail to
   *  remove lastItem from the queue if it moved due to an interleaved interior
   *  remove while in detached mode.
   *
   *  Method forEachRemaining, added in Java 8, is treated similarly to hasNext
   *  returning false, in that we switch to detached mode, but we regard it as
   *  an even stronger request to "close" this iteration, and don't bother
   *  supporting subsequent remove().
   */
  private object Itr {

    /** Special index value indicating "not available" or "undefined" */
    private val NONE = -1

    /** Special index value indicating "removed elsewhere", that is, removed by
     *  some operation other than a call to this.remove().
     */
    private val REMOVED = -2

    /** Special value for prevTakeIndex indicating "detached mode" */
    private val DETACHED = -3
  }

  private[concurrent] class Itr private[concurrent] ()
      extends util.Iterator[E] {
    import Itr._

    private var cursor: Int = 0

    private var nextItem: E = _

    private var nextIndex: Int = 0

    private var lastItem: E = _

    private var lastRet: Int = NONE

    private var prevTakeIndex: Int = 0

    private var prevCycles: Int = 0

    locally {
      val lock = ArrayBlockingQueue.this.lock
      lock.lock()
      try
        if (count == 0) {
          // assert itrs == null;
          cursor = NONE
          nextIndex = NONE
          prevTakeIndex = DETACHED
        } else {
          val takeIndex = ArrayBlockingQueue.this.takeIndex
          prevTakeIndex = takeIndex
          nextIndex = takeIndex
          nextItem = itemAt(nextIndex)
          cursor = incCursor(takeIndex)
          if (itrs == null)
            itrs = new Itrs(this)
          else {
            itrs.register(this) // in this order
            itrs.doSomeSweeping(false)
          }
          prevCycles = itrs.cycles
          // assert takeIndex >= 0;
          // assert prevTakeIndex == takeIndex;
          // assert nextIndex >= 0;
          // assert nextItem != null;
        }
      finally lock.unlock()
    }

    private[concurrent] def isDetached = prevTakeIndex < 0
    private def incCursor(index: Int) = {
      var idx = index + 1
      if (idx == items.length) idx = 0
      if (idx == putIndex) idx = NONE
      idx
    }

    private def invalidated(
        index: Int,
        prevTakeIndex: Int,
        dequeues: Long,
        length: Int
    ): Boolean = {
      if (index < 0) return false
      var distance = index - prevTakeIndex
      if (distance < 0) distance += length
      dequeues > distance
    }

    private def incorporateDequeues(): Unit = {
      // assert lock.isHeldByCurrentThread();
      // assert itrs != null;
      // assert !isDetached();
      // assert count > 0;
      val cycles = itrs.cycles
      val takeIndex = ArrayBlockingQueue.this.takeIndex
      val prevCycles = this.prevCycles
      val prevTakeIndex = this.prevTakeIndex
      if (cycles != prevCycles || takeIndex != prevTakeIndex) {
        val len = items.length
        // how far takeIndex has advanced since the previous
        // operation of this iterator
        val dequeues =
          (cycles - prevCycles).toLong * len + (takeIndex - prevTakeIndex)
        // Check indices for invalidation
        if (invalidated(lastRet, prevTakeIndex, dequeues, len))
          lastRet = REMOVED
        if (invalidated(nextIndex, prevTakeIndex, dequeues, len))
          nextIndex = REMOVED
        if (invalidated(cursor, prevTakeIndex, dequeues, len))
          cursor = takeIndex
        if (cursor < 0 && nextIndex < 0 && lastRet < 0) detach()
        else {
          this.prevCycles = cycles
          this.prevTakeIndex = takeIndex
        }
      }
    }

    /** Called when itrs should stop tracking this iterator, either because
     *  there are no more indices to update (cursor < 0 && nextIndex < 0 &&
     *  lastRet < 0) or as a special exception, when lastRet >= 0, because
     *  hasNext() is about to return false for the first time. Call only from
     *  iterating thread.
     */
    private def detach(): Unit = {
      // Switch to detached mode
      // assert cursor == NONE;
      // assert nextIndex < 0;
      // assert lastRet < 0 || nextItem == null;
      // assert lastRet < 0 ^ lastItem != null;
      if (prevTakeIndex >= 0) {
        prevTakeIndex = DETACHED
        // try to unlink from itrs (but not too hard)
        itrs.doSomeSweeping(true)
      }
    }

    override def hasNext(): Boolean = {
      nextItem != null || {
        noNext()
        false
      }
    }

    private def noNext(): Unit = {
      val lock = ArrayBlockingQueue.this.lock
      lock.lock()
      // assert nextIndex == NONE;
      try
        if (!isDetached) {
          // assert lastRet >= 0;
          incorporateDequeues() // might update lastRet

          if (lastRet >= 0) {
            lastItem = itemAt(lastRet)
            // assert lastItem != null;
            detach()
          }
        }
      finally lock.unlock()
    }

    override def next(): E = {
      val e = nextItem
      if (e == null) throw new NoSuchElementException
      val lock = ArrayBlockingQueue.this.lock
      lock.lock()
      try {
        if (!isDetached) incorporateDequeues()
        // assert nextIndex != NONE;
        // assert lastItem == null;
        lastRet = nextIndex
        val cursor = this.cursor
        if (cursor >= 0) {
          nextIndex = cursor
          nextItem = itemAt(cursor)
          this.cursor = incCursor(cursor)
        } else {
          nextIndex = NONE
          nextItem = null.asInstanceOf[E]
          if (lastRet == REMOVED) detach()
        }
      } finally lock.unlock()
      e
    }

    override def forEachRemaining(action: Consumer[_ >: E]): Unit = {
      Objects.requireNonNull(action)
      val lock = ArrayBlockingQueue.this.lock
      lock.lock()
      try {
        val e = nextItem
        if (e == null) return
        if (!isDetached) incorporateDequeues()
        action.accept(e)
        if (isDetached || cursor < 0) return
        val items = ArrayBlockingQueue.this.items
        var i = cursor
        val end = putIndex
        var to =
          if (i < end) end
          else items.length
        @annotation.tailrec
        def loop(): Unit = {
          while (i < to) {
            action.accept(ArrayBlockingQueue.itemAt(items, i))
            i += 1
          }
          if (to != end) {
            i = 0
            to = end
            loop()
          }
        }
        loop()
      } finally {
        // Calling forEachRemaining is a strong hint that this
        // iteration is surely over; supporting remove() after
        // forEachRemaining() is more trouble than it's worth
        lastRet = NONE
        nextIndex = lastRet
        cursor = lastRet
        lastItem = null.asInstanceOf[E]
        nextItem = lastItem
        detach()
        lock.unlock()
      }
    }
    override def remove(): Unit = {
      val lock = ArrayBlockingQueue.this.lock
      lock.lock()
      try {
        if (!isDetached)
          incorporateDequeues() // might update lastRet or detach
        val lastRet = this.lastRet
        this.lastRet = NONE
        if (lastRet >= 0) {
          if (!isDetached) removeAt(lastRet)
          else {
            val lastItem = this.lastItem
            this.lastItem = null.asInstanceOf[E]
            if (itemAt(lastRet) eq lastItem)
              removeAt(lastRet)
          }
        } else if (lastRet == NONE) throw new IllegalStateException
        // else lastRet == REMOVED and the last returned element was
        // previously asynchronously removed via an operation other
        // than this.remove(), so nothing to do.
        if (cursor < 0 && nextIndex < 0)
          detach()
      } finally lock.unlock()
    }

    private[concurrent] def shutdown(): Unit = {
      cursor = NONE
      if (nextIndex >= 0) nextIndex = REMOVED
      if (lastRet >= 0) {
        lastRet = REMOVED
        lastItem = null.asInstanceOf[E]
      }
      prevTakeIndex = DETACHED
      // Don't set nextItem to null because we must continue to be
      // able to return it on next().
      //
      // Caller will unlink from itrs when convenient.
    }
    private def distance(index: Int, prevTakeIndex: Int, length: Int) = {
      var distance = index - prevTakeIndex
      if (distance < 0) distance += length
      distance
    }

    private[concurrent] def removedAt(removedIndex: Int): Boolean = {
      if (isDetached) return true
      val takeIndex = ArrayBlockingQueue.this.takeIndex
      val prevTakeIndex = this.prevTakeIndex
      val len = items.length
      // distance from prevTakeIndex to removedIndex
      val removedDistance =
        len * (itrs.cycles - this.prevCycles + {
          if (removedIndex < takeIndex) 1 else 0
        }) + (removedIndex - prevTakeIndex)
      // assert itrs.cycles - this.prevCycles >= 0;
      // assert itrs.cycles - this.prevCycles <= 1;
      // assert removedDistance > 0;
      // assert removedIndex != takeIndex;
      var cursor = this.cursor
      if (cursor >= 0) {
        val x = distance(cursor, prevTakeIndex, len)
        if (x == removedDistance) {
          if (cursor == putIndex) {
            cursor = NONE
            this.cursor = NONE
          }
        } else if (x > removedDistance) {
          // assert cursor != prevTakeIndex;
          this.cursor = dec(cursor, len)
          cursor = this.cursor
        }
      }
      var lastRet = this.lastRet
      if (lastRet >= 0) {
        val x = distance(lastRet, prevTakeIndex, len)
        if (x == removedDistance) {
          lastRet = REMOVED
          this.lastRet = lastRet
        } else if (x > removedDistance) {
          lastRet = dec(lastRet, len)
          this.lastRet = lastRet
        }
      }
      var nextIndex = this.nextIndex
      if (nextIndex >= 0) {
        val x = distance(nextIndex, prevTakeIndex, len)
        if (x == removedDistance) {
          nextIndex = REMOVED
          this.nextIndex = nextIndex
        } else if (x > removedDistance) {
          nextIndex = dec(nextIndex, len)
          this.nextIndex = nextIndex
        }
      }
      if (cursor < 0 && nextIndex < 0 && lastRet < 0) {
        this.prevTakeIndex = DETACHED
        true
      } else false
    }

    private[concurrent] def takeIndexWrapped: Boolean = {
      if (isDetached) return true
      if (itrs.cycles - prevCycles > 1) { // All the elements that existed at the time of the last
        // operation are gone, so abandon further iteration.
        shutdown()
        return true
      }
      false
    }

    //
    // override def toString(): String = {
    //   "cursor=" + cursor + " " +
    //     "nextIndex=" + nextIndex + " " +
    //     "lastRet=" + lastRet + " " +
    //     "nextItem=" + nextItem + " " +
    //     "lastItem=" + lastItem + " " +
    //     "prevCycles=" + prevCycles + " " +
    //     "prevTakeIndex=" + prevTakeIndex + " " +
    //     "size()=" + size() + " " +
    //     "remainingCapacity()=" + remainingCapacity()
    // }
  }

  override def spliterator(): Spliterator[E] = Spliterators.spliterator(
    this,
    Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.CONCURRENT
  )

  override def forEach(action: Consumer[_ >: E]): Unit = {
    Objects.requireNonNull(action)
    val lock = this.lock
    lock.lock()
    try
      if (count > 0) {
        val items = this.items
        var i = takeIndex
        val end = putIndex
        var to =
          if (i < end) end
          else items.length

        @tailrec def loop(): Unit = {
          while (i < to) {
            action.accept(ArrayBlockingQueue.itemAt(items, i))
            i += 1
          }
          if (to == end) ()
          else {
            i = 0
            to = end
            loop()
          }
        }
        loop()
      }
    finally lock.unlock()
  }

  override def removeIf(filter: Predicate[_ >: E]): Boolean = {
    Objects.requireNonNull(filter)
    bulkRemove(filter)
  }

  override def removeAll(c: util.Collection[_]): Boolean = {
    Objects.requireNonNull(c)
    bulkRemove((e: E) => c.contains(e))
  }

  override def retainAll(c: util.Collection[_]): Boolean = {
    Objects.requireNonNull(c)
    bulkRemove((e: E) => !c.contains(e))
  }

  private def bulkRemove(filter: Predicate[_ >: E]): Boolean = {
    val lock = this.lock
    lock.lock()
    try
      if (itrs == null) return { // check for active iterators
        if (count <= 0) false
        else {
          val items = this.items
          // Optimize for initial run of survivors
          val start = takeIndex
          val end = putIndex
          val to =
            if (start < end) end
            else items.length
          def findInRange(range: Range) = range.find { i =>
            filter.test(ArrayBlockingQueue.itemAt(items, i))
          }
          findInRange(start until to)
            .orElse(if (to == end) None else findInRange(0 until end))
            .map(bulkRemoveModified(filter, _))
            .getOrElse(false)
        }
      }
    finally lock.unlock()
    // Active iterators are too hairy!
    // Punting (for now) to the slow n^2 algorithm ...
    super.removeIf(filter)
  }

  private def distanceNonEmpty(i: Int, j: Int) = (j - i) match {
    case n if n <= 0 => n + items.length
    case n           => n
  }

  private def bulkRemoveModified(filter: Predicate[_ >: E], beg: Int) = {
    val es = items
    val capacity = items.length
    val end = putIndex
    val deathRow = nBits(distanceNonEmpty(beg, putIndex))
    deathRow(0) = 1L // set bit 0

    val from = beg + 1
    val to = if (from <= end) end else es.length

    setBits(from, to, beg)
    def setBits(from: Int, to: Int, k: Int): Unit = {
      for (i <- from until to) {
        if (filter.test(ArrayBlockingQueue.itemAt(es, i))) {
          setBit(deathRow, i - k)
        }
      }
      if (to != end) {
        setBits(from = 0, to = end, k = k - capacity)
      }
    }

    // a two-finger traversal, with hare i reading, tortoise w writing
    var w = beg
    traverse(from, to, beg)
    def traverse(from: Int, to: Int, k: Int): Unit = {
      // In this loop, i and w are on the same leg, with i > w
      for (i <- from until to)
        if (isClear(deathRow, i - k)) {
          es(w) = es(i)
          w += 1
        }

      if (to != end) {
        var i = 0
        val to = end
        val cap = k - capacity
        while (i < to && w < capacity) {
          if (isClear(deathRow, i - cap)) {
            es(w) = es(i)
            w += 1
          }
          i += 1
        }
        if (i >= to) {
          if (w == capacity) w = 0
          // break
        } else {
          // w rejoins i on second leg
          w = 0
          traverse(from = i, to = to, k = cap)
        }
      }
    }
    count -= distanceNonEmpty(w, end)
    putIndex = w
    circularClear(es, putIndex, end)
    true
  }

  private[concurrent] def checkInvariants(): Unit = { // meta-assertions
    if (!invariantsSatisfied) {
      val detail = String.format(
        "takeIndex=%d putIndex=%d count=%d capacity=%d items=%s",
        takeIndex: Integer,
        putIndex: Integer,
        count: Integer,
        items.length: Integer,
        util.Arrays.toString(items)
      )
      System.err.println(detail)
      throw new AssertionError(detail)
    }
  }
  private def invariantsSatisfied = {
    // Unlike ArrayDeque, we have a count field but no spare slot.
    // We prefer ArrayDeque's strategy (and the names of its fields!),
    // but our field layout is baked into the serial form, and so is
    // too annoying to change.
    // putIndex == takeIndex must be disambiguated by checking count.
    val capacity = items.length
    capacity > 0 &&
      (items.getClass == classOf[Array[AnyRef]]) &&
      (takeIndex | putIndex | count) >= 0 &&
      takeIndex < capacity &&
      putIndex < capacity &&
      count <= capacity &&
      (putIndex - takeIndex - count) % capacity == 0 &&
      (count == 0 || items(takeIndex) != null) &&
      (count == capacity || items(putIndex) == null) &&
      (count == 0 || items(ArrayBlockingQueue.dec(putIndex, capacity)) != null)
  }

  //
  // @throws[java.io.IOException]
  // @throws[ClassNotFoundException]
  // private def readObject(s: ObjectInputStream): Unit = { // Read in items array and various fields
  //   s.defaultReadObject()
  //   if (!invariantsSatisfied)
  //     throw new InvalidObjectException("invariants violated")
  // }
}
