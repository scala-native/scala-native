/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util
import java.util._
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks._
import java.util.function._

@SerialVersionUID(-6903933977591709194L)
object LinkedBlockingQueue {

  private[concurrent] class Node[E] private[concurrent] (var item: E) {

    private[concurrent] var next: Node[E] = _
  }
}
@SerialVersionUID(-6903933977591709194L)
class LinkedBlockingQueue[E <: AnyRef](
    val capacity: Int
) extends util.AbstractQueue[E]
    with BlockingQueue[E]
    with Serializable {
  import LinkedBlockingQueue._

  if (capacity <= 0) throw new IllegalArgumentException

  private[concurrent] var head = new Node[E](null.asInstanceOf[E])

  private var last = head

  final private val count = new AtomicInteger()

  final private val takeLock = new ReentrantLock()

  final private val notEmpty: Condition = takeLock.newCondition()

  final private val putLock = new ReentrantLock()

  final private val notFull = putLock.newCondition()

  private def signalNotEmpty(): Unit = {
    val takeLock = this.takeLock
    takeLock.lock()
    try notEmpty.signal()
    finally takeLock.unlock()
  }

  private def signalNotFull(): Unit = {
    val putLock = this.putLock
    putLock.lock()
    try notFull.signal()
    finally putLock.unlock()
  }

  private def enqueue(node: Node[E]): Unit = {
    // assert putLock.isHeldByCurrentThread();
    // assert last.next == null;
    last.next = node
    last = node
  }

  private def dequeue() = {
    // assert takeLock.isHeldByCurrentThread();
    // assert head.item == null;
    val h = head
    val first = h.next
    h.next = h // help GC

    head = first
    val x = first.item
    first.item = null.asInstanceOf[E]
    x
  }

  private[concurrent] def fullyLock(): Unit = {
    putLock.lock()
    takeLock.lock()
  }

  private[concurrent] def fullyUnlock(): Unit = {
    takeLock.unlock()
    putLock.unlock()
  }

  def this() = this(Integer.MAX_VALUE)

  def this(c: util.Collection[_ <: E]) = {
    this(Integer.MAX_VALUE)
    val putLock = this.putLock
    putLock.lock() // Never contended, but necessary for visibility

    try {
      var n = 0
      val it = c.iterator()
      while (it.hasNext()) {
        val e = it.next()
        if (e == null) throw new NullPointerException
        if (n == capacity) throw new IllegalStateException("Queue full")
        enqueue(new Node[E](e))
        n += 1
      }
      count.set(n)
    } finally putLock.unlock()
  }

  override def size(): Int = count.get()

  override def remainingCapacity(): Int = capacity - count.get()

  @throws[InterruptedException]
  override def put(e: E): Unit = {
    if (e == null) throw new NullPointerException
    var c = 0
    val node = new Node[E](e)
    val putLock = this.putLock
    val count = this.count
    putLock.lockInterruptibly()
    try {
      /*
       * Note that count is used in wait guard even though it is
       * not protected by lock. This works because count can
       * only decrease at this point (all other puts are shut
       * out by lock), and we (or some other waiting put) are
       * signalled if it ever changes from capacity. Similarly
       * for all other uses of count in other wait guards.
       */
      while (count.get() == capacity) notFull.await()
      enqueue(node)
      c = count.getAndIncrement()
      if (c + 1 < capacity) notFull.signal()
    } finally putLock.unlock()
    if (c == 0) signalNotEmpty()
  }

  @throws[InterruptedException]
  override def offer(e: E, timeout: Long, unit: TimeUnit): Boolean = {
    if (e == null) throw new NullPointerException
    var nanos = unit.toNanos(timeout)
    var c = 0
    val putLock = this.putLock
    val count = this.count
    putLock.lockInterruptibly()
    try {
      while (count.get() == capacity) {
        if (nanos <= 0L) return false
        nanos = notFull.awaitNanos(nanos)
      }
      enqueue(new Node[E](e))
      c = count.getAndIncrement()
      if (c + 1 < capacity) notFull.signal()
    } finally putLock.unlock()
    if (c == 0) signalNotEmpty()
    true
  }

  override def offer(e: E): Boolean = {
    if (e == null) throw new NullPointerException
    val count = this.count
    if (count.get() == capacity) return false
    var c = 0
    val node = new Node[E](e)
    val putLock = this.putLock
    putLock.lock()
    try {
      if (count.get() == capacity) return false
      enqueue(node)
      c = count.getAndIncrement()
      if (c + 1 < capacity) notFull.signal()
    } finally putLock.unlock()
    if (c == 0) signalNotEmpty()
    true
  }
  @throws[InterruptedException]
  override def take(): E = {
    var x: E = null.asInstanceOf[E]
    var c = 0
    val count = this.count
    val takeLock = this.takeLock
    takeLock.lockInterruptibly()
    try {
      while (count.get() == 0) notEmpty.await()
      x = dequeue()
      c = count.getAndDecrement()
      if (c > 1) notEmpty.signal()
    } finally takeLock.unlock()
    if (c == capacity) signalNotFull()
    x
  }

  @throws[InterruptedException]
  override def poll(timeout: Long, unit: TimeUnit): E = {
    var x = null.asInstanceOf[E]
    var c = 0
    var nanos = unit.toNanos(timeout)
    val count = this.count
    val takeLock = this.takeLock
    takeLock.lockInterruptibly()
    try {
      while (count.get() == 0) {
        if (nanos <= 0L) return null.asInstanceOf[E]
        nanos = notEmpty.awaitNanos(nanos)
      }
      x = dequeue()
      c = count.getAndDecrement()
      if (c > 1) notEmpty.signal()
    } finally takeLock.unlock()
    if (c == capacity) signalNotFull()
    x
  }

  override def poll(): E = {
    val count = this.count
    if (count.get() == 0) return null.asInstanceOf[E]
    var x = null.asInstanceOf[E]
    var c = 0
    val takeLock = this.takeLock
    takeLock.lock()
    try {
      if (count.get() == 0) return null.asInstanceOf[E]
      x = dequeue()
      c = count.getAndDecrement()
      if (c > 1) notEmpty.signal()
    } finally takeLock.unlock()
    if (c == capacity) signalNotFull()
    x
  }
  override def peek(): E = {
    val count = this.count
    if (count.get() == 0) return null.asInstanceOf[E]
    val takeLock = this.takeLock
    takeLock.lock()
    try
      if (count.get() > 0) head.next.item
      else null.asInstanceOf[E]
    finally takeLock.unlock()
  }

  private[concurrent] def unlink(
      p: Node[E],
      pred: Node[E]
  ): Unit = { // p.next is not changed, to allow iterators that are
    // traversing p to maintain their weak-consistency guarantee.
    p.item = null.asInstanceOf[E]
    pred.next = p.next
    if (last eq p) last = pred
    if (count.getAndDecrement() == capacity) notFull.signal()
  }

  override def remove(o: Any): Boolean = {
    if (o == null) return false
    fullyLock()
    try {
      var pred = head
      var p = pred.next
      while ({ p != null }) {
        if (o == p.item) {
          unlink(p, pred)
          return true
        }

        pred = p
        p = p.next
      }
      false
    } finally fullyUnlock()
  }

  override def contains(o: Any): Boolean = {
    if (o == null) return false
    fullyLock()
    try {
      var p = head.next
      while ({ p != null }) {
        if (o == p.item) return true
        p = p.next
      }
      false
    } finally fullyUnlock()
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
    fullyLock()
    try {
      val size = count.get()
      val a = new Array[AnyRef](size)
      var k = 0
      var p = head.next
      while (p != null) {
        val idx = k
        k += 1
        a(idx) = p.item
        p = p.next
      }
      a
    } finally fullyUnlock()
  }

  override def toArray[T <: AnyRef](_a: Array[T]): Array[T] = {
    var a = _a
    fullyLock()
    try {
      val size = count.get()
      if (a.length < size)
        a = java.lang.reflect.Array
          .newInstance(a.getClass.getComponentType, size)
          .asInstanceOf[Array[T]]
      var k = 0
      var p = head.next
      while (p != null) {
        val idx = k
        k += 1
        a(idx) = p.item.asInstanceOf[T]
        p = p.next
      }
      if (a.length > k) a(k) = null.asInstanceOf[T]
      a
    } finally fullyUnlock()
  }
  override def toString: String = Helpers.collectionToString(this)

  override def clear(): Unit = {
    fullyLock()
    try {
      var p: Node[E] = null.asInstanceOf[Node[E]]
      var h = head
      while ({ p = h.next; p != null }) {
        h.next = h
        p.item = null.asInstanceOf[E]
        h = p
      }
      head = last
      // assert head.item == null && head.next == null;
      if (count.getAndSet(0) == capacity) notFull.signal()
    } finally fullyUnlock()
  }

  override def drainTo(c: util.Collection[_ >: E]): Int =
    drainTo(c, Integer.MAX_VALUE)
  override def drainTo(c: util.Collection[_ >: E], maxElements: Int): Int = {
    Objects.requireNonNull(c)
    if (c eq this) throw new IllegalArgumentException
    if (maxElements <= 0) return 0
    var signalNotFull = false
    val takeLock = this.takeLock
    takeLock.lock()
    try {
      val n = Math.min(maxElements, count.get())
      // count.get() provides visibility to first n Nodes
      var h = head
      var i = 0
      try {
        while (i < n) {
          val p = h.next
          c.add(p.item)
          p.item = null.asInstanceOf[E]
          h.next = h
          h = p
          i += 1
        }
        n
      } finally {
        // Restore invariants even if c.add() threw
        if (i > 0) { // assert h.item == null;
          head = h
          signalNotFull = count.getAndAdd(-i) == capacity
        }
      }
    } finally {
      takeLock.unlock()
      if (signalNotFull) this.signalNotFull()
    }
  }

  private[concurrent] def succ(p: Node[E]) = {
    p.next match {
      case `p`  => head.next
      case next => next
    }
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
  override def iterator(): Iterator[E] = new Itr()

  private[concurrent] class Itr private[concurrent] ()
      extends util.Iterator[E] {
    private var nextNode: Node[E] = _
    private var nextItem: E = _
    private var lastRet: Node[E] = _
    private var ancestor: Node[E] = _ // Helps unlink lastRet on remove()

    fullyLock()
    try if ({ nextNode = head.next; nextNode != null }) nextItem = nextNode.item
    finally fullyUnlock()

    override def hasNext(): Boolean = nextNode != null
    override def next(): E = {
      var p: Node[E] = null
      if ({ p = nextNode; p == null }) throw new NoSuchElementException
      lastRet = p
      val x = nextItem
      fullyLock()
      try {
        var e: E = null.asInstanceOf[E]
        p = p.next
        while (p != null && { e = p.item; e == null }) p = succ(p)
        nextNode = p
        nextItem = e
      } finally fullyUnlock()
      x
    }

    override def forEachRemaining(action: Consumer[_ >: E]): Unit = {
      // A variant of forEachFrom
      Objects.requireNonNull(action)
      var p: Node[E] = nextNode
      if (p == null) return ()
      lastRet = p
      nextNode = null.asInstanceOf[Node[E]]
      val batchSize = 64
      var es: Array[AnyRef] = null
      var n = 0
      var len = 1
      while ({
        fullyLock()
        try {
          if (es == null) {
            p = p.next
            var q = p
            var break = false
            while (q != null && !break) {
              if (q.item != null && { len += 1; len } == batchSize)
                break = true
              else
                q = succ(q)
            }
            es = new Array[AnyRef](len)
            es(0) = nextItem
            nextItem = null.asInstanceOf[E]
            n = 1
          } else n = 0

          while (p != null && n < len) {
            es(n) = p.item
            if (es(n) != null) {
              lastRet = p
              n += 1
            }
            p = succ(p)
          }
        } finally fullyUnlock()
        for (i <- 0 until n) {
          val e = es(i).asInstanceOf[E]
          action.accept(e)
        }
        n > 0 && p != null
      }) ()
    }

    override def remove(): Unit = {
      val p = lastRet
      if (p == null) throw new IllegalStateException
      lastRet = null
      fullyLock()
      try
        if (p.item != null) {
          if (ancestor == null) ancestor = head
          ancestor = findPred(p, ancestor)
          unlink(p, ancestor)
        }
      finally fullyUnlock()
    }
  }

  private object LBQSpliterator {
    private[concurrent] val MAX_BATCH = 1 << 25 // max batch array size;

  }
  final private[concurrent] class LBQSpliterator private[concurrent] ()
      extends Spliterator[E] {
    private[concurrent] var current: Node[E] = _
    private[concurrent] var batch = 0 // batch size for splits
    private[concurrent] var exhausted = false // true when no more nodes
    private[concurrent] var est: Long = size() // size estimate

    override def estimateSize(): Long = est
    override def trySplit(): Spliterator[E] = {
      var h: Node[E] = null.asInstanceOf[Node[E]]
      if (!exhausted &&
          ({ h = current; h != null } || { h = head.next; h != null }) &&
          h.next != null) {
        batch = Math.min(batch + 1, LBQSpliterator.MAX_BATCH)
        val n = batch
        val a = new Array[AnyRef](n)
        var i = 0
        var p: Node[E] = current
        fullyLock()
        try
          if (p != null || { p = head.next; p != null })
            while (p != null && i < n) {
              if ({ a(i) = p.item; a(i) != null }) i += 1
              p = succ(p)
            }
        finally fullyUnlock()
        if ({ current = p; current == null }) {
          est = 0L
          exhausted = true
        } else if ({ est -= i; est < 0L }) est = 0L
        if (i > 0)
          return Spliterators.spliterator(
            a,
            0,
            i,
            Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.CONCURRENT
          )
      }
      null
    }

    override def tryAdvance(action: Consumer[_ >: E]): Boolean = {
      Objects.requireNonNull(action)
      if (!exhausted) {
        var e: E = null.asInstanceOf[E]
        fullyLock()
        try {
          var p: Node[E] = current
          if (p != null || { p = head.next; p != null }) while ({
            e = p.item
            p = succ(p)
            e == null && p != null
          }) ()
          current = p
          if (current == null) exhausted = true
        } finally fullyUnlock()
        if (e != null) {
          action.accept(e)
          return true
        }
      }
      false
    }

    override def forEachRemaining(action: Consumer[_ >: E]): Unit = {
      Objects.requireNonNull(action)
      if (!exhausted) {
        exhausted = true
        val p = current
        current = null
        forEachFrom(action, p)
      }
    }
    override def characteristics(): Int =
      Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.CONCURRENT
  }

  override def spliterator(): Spliterator[E] = new LBQSpliterator

  override def forEach(action: Consumer[_ >: E]): Unit = {
    Objects.requireNonNull(action)
    forEachFrom(action, null)
  }

  private[concurrent] def forEachFrom(
      action: Consumer[_ >: E],
      _p: Node[E]
  ): Unit = {
    // Extract batches of elements while holding the lock; then
    // run the action on the elements while not
    var p = _p
    val batchSize = 64 // max number of elements per batch
    var es = null.asInstanceOf[Array[AnyRef]] // container for batch of elements
    var n = 0
    var len = 0
    while ({
      fullyLock()
      try {
        if (es == null) {
          if (p == null) p = head.next
          var q = p
          var break = false
          while (q != null && !break) {
            if (q.item != null && { len += 1; len } == batchSize)
              break = true
            else q = succ(q)
          }
          es = new Array[AnyRef](len)
        }

        n = 0
        while (p != null && n < len) {
          es(n) = p.item
          if (es(n) != null) n += 1
          p = succ(p)
        }
      } finally fullyUnlock()

      for (i <- 0 until n) {
        val e = es(i).asInstanceOf[E]
        action.accept(e)
      }
      n > 0 && p != null
    }) ()
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

  private[concurrent] def findPred(
      p: Node[E],
      _ancestor: Node[E]
  ) = {
    // assert p.item != null;
    var ancestor = _ancestor
    if (ancestor.item == null) ancestor = head
    // Fails with NPE if precondition not satisfied
    var q = ancestor.next
    while ({ q = ancestor.next; q ne p }) ancestor = q
    ancestor
  }

  private def bulkRemove(
      filter: Predicate[_ >: E]
  ) = {
    var removed = false
    var p = null: Node[E]
    var ancestor = head
    var nodes = null: Array[Node[E]]
    var n = 0
    var len = 0
    while ({ // 1. Extract batch of up to 64 elements while holding the lock.
      fullyLock()
      try {
        if (nodes == null) { // first batch; initialize
          p = head.next
          var q = p
          var break = false
          while (!break && q != null) {
            if (q.item != null && { len += 1; len } == 64)
              break = true
            else
              q = succ(q)
          }
          nodes = new Array[Node[AnyRef]](len).asInstanceOf[Array[Node[E]]]
        }
        n = 0
        while (p != null && n < len) {
          val idx = n
          n += 1
          nodes(idx) = p
          p = succ(p)
        }
      } finally fullyUnlock()
      // 2. Run the filter on the elements while lock is free.
      var deathRow = 0L // "bitset" of size 64
      for (i <- 0 until n) {
        val e = nodes(i).item
        if (e != null && filter.test(e)) deathRow |= 1L << i
      }
      // 3. Remove any filtered elements while holding the lock.
      if (deathRow != 0) {
        fullyLock()
        try
          for (i <- 0 until n) {
            var q = null: Node[E]
            if ((deathRow & (1L << i)) != 0L && {
                  q = nodes(i); q.item != null
                }) {
              ancestor = findPred(q, ancestor)
              unlink(q, ancestor)
              removed = true
            }
            nodes(i) = null
          }
        finally fullyUnlock()
      }
      n > 0 && p != null
    }) ()
    removed
  }

  // No ObjectInputStream in ScalaNative
  // private def writeObject(s: ObjectOutputStream): Unit
  // private def readObject(s: ObjectInputStream): Unit
}
