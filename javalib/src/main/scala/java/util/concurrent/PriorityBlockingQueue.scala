/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent
import java.util
import java.util._
import java.util.concurrent.locks._
import java.util.function._
import scala.annotation.tailrec
import scala.scalanative.runtime.{fromRawPtr, Intrinsics}
import scala.scalanative.libc.atomic.CAtomicInt

@SerialVersionUID(5595510919245408276L)
object PriorityBlockingQueue {

  private final val DEFAULT_INITIAL_CAPACITY = 11

  private def ensureNonEmpty[E](es: Array[E]) =
    if (es.length > 0) es
    else new Array[AnyRef](1).asInstanceOf[Array[E]]

  private def siftUpComparable[T](_k: Int, x: T, es: Array[T]): Unit = {
    var k = _k
    val key = x.asInstanceOf[Comparable[_ >: T]]
    @tailrec def loop(): Unit = {
      if (k > 0) {
        val parent = (k - 1) >>> 1
        val e = es(parent)
        if (key.compareTo(e.asInstanceOf[T]) >= 0) () // break
        else {
          es(k) = e
          k = parent
          loop()
        }
      }
    }
    loop()
    es(k) = key.asInstanceOf[T]
  }

  private def siftUpUsingComparator[T](
      _k: Int,
      x: T,
      es: Array[T],
      cmp: Comparator[_ >: T]
  ): Unit = {
    var k = _k
    @tailrec def loop(): Unit = {
      if (k > 0) {
        val parent = (k - 1) >>> 1
        val e = es(parent)
        if (cmp.compare(x, e.asInstanceOf[T]) >= 0) () // break
        else {
          es(k) = e
          k = parent
          loop()
        }
      }
    }
    loop()
    es(k) = x.asInstanceOf[T]
  }

  private def siftDownComparable[T](
      _k: Int,
      x: T,
      es: Array[T],
      n: Int
  ): Unit = { // assert n > 0;
    val key = x.asInstanceOf[Comparable[_ >: T]]
    val half = n >>> 1 // loop while a non-leaf
    var k = _k
    @tailrec def loop(): Unit = {
      if (k < half) {
        var child = (k << 1) + 1
        var c = es(child)
        val right = child + 1
        if (right < n && c
              .asInstanceOf[Comparable[_ >: T]]
              .compareTo(es(right).asInstanceOf[T]) > 0) {
          child = right
          c = es(child)
        }
        if (key.compareTo(c.asInstanceOf[T]) <= 0) () // break
        else {
          es(k) = c
          k = child
          loop()
        }
      }
    }
    loop()
    es(k) = key.asInstanceOf[T]
  }

  private def siftDownUsingComparator[T](
      _k: Int,
      x: T,
      es: Array[T],
      n: Int,
      cmp: Comparator[_ >: T]
  ): Unit = {
    val half = n >>> 1
    var k = _k
    @tailrec def loop(): Unit = {
      if (k < half) {
        var child = (k << 1) + 1
        var c = es(child)
        val right = child + 1
        if (right < n && cmp.compare(
              c.asInstanceOf[T],
              es(right).asInstanceOf[T]
            ) > 0) {
          child = right
          c = es(child)
        }
        if (cmp.compare(x, c.asInstanceOf[T]) <= 0) () // break
        else {
          es(k) = c
          k = child
          loop()
        }
      }
    }
    loop()
    es(k) = x.asInstanceOf[T]
  }

  private def nBits(n: Int) = new Array[Long](((n - 1) >> 6) + 1)
  private def setBit(bits: Array[Long], i: Int): Unit = {
    bits(i >> 6) |= 1L << i
  }
  private def isClear(bits: Array[Long], i: Int) =
    (bits(i >> 6) & (1L << i)) == 0
}

@SuppressWarnings(Array("unchecked"))
@SerialVersionUID(5595510919245408276L)
class PriorityBlockingQueue[E <: AnyRef] private (
    /** Priority queue represented as a balanced binary heap: the two children
     *  of queue[n] are queue[2*n+1] and queue[2*(n+1)]. The priority queue is
     *  ordered by comparator, or by the elements' natural ordering, if
     *  comparator is null: For each node n in the heap and each descendant d of
     *  n, n <= d. The element with the lowest value is in queue[0], assuming
     *  the queue is nonempty.
     */
    private var queue: Array[E],
    elemComparator: Comparator[_ >: E]
) extends util.AbstractQueue[E]
    with BlockingQueue[E]
    with Serializable {
  import PriorityBlockingQueue._

  private var curSize = 0

  final private val lock = new ReentrantLock

  final private val notEmpty: Condition = lock.newCondition()

  @volatile private var allocationSpinLock = 0

  private val atomicAllocationSpinLock = new CAtomicInt(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "allocationSpinLock"))
  )

  def this(initialCapacity: Int, comparator: Comparator[_ >: E]) = {
    this(
      queue = {
        if (initialCapacity < 1)
          throw new IllegalArgumentException()
        new Array[AnyRef](initialCapacity.max(1)).asInstanceOf[Array[E]]
      },
      comparator
    )
  }
  def this(initialCapacity: Int) = this(initialCapacity, null)
  def this() = this(PriorityBlockingQueue.DEFAULT_INITIAL_CAPACITY, null)
  def this(c: util.Collection[_ <: E]) = {
    this(
      elemComparator = c match {
        case s: SortedSet[E] @unchecked =>
          s.comparator()
        case p: PriorityBlockingQueue[E] @unchecked => p.comparator()
        case _                                      => null
      },
      queue = {
        var screen = true // true if must screen for nulls
        val hasComparator = c match {
          case s: SortedSet[_] =>
            s.comparator() != null
          case p: PriorityBlockingQueue[_] =>
            screen = false
            p.comparator() != null
          case _ => false
        }
        var es = c.toArray()
        val n = es.length
        if (c.getClass() != classOf[java.util.ArrayList[_]])
          es = Arrays.copyOf(es, n)
        if (screen && (n == 1 || hasComparator)) {
          if (es.contains(null)) throw new NullPointerException()
        }
        PriorityBlockingQueue.ensureNonEmpty(es.asInstanceOf[Array[E]])
      }
    )

    this.curSize = this.queue.length
    val heapify = c match {
      case s: SortedSet[_] => false
      case p: PriorityBlockingQueue[_] =>
        p.getClass() != classOf[PriorityBlockingQueue[_]]
      case _ => true
    }
    if (heapify) this.heapify()
  }

  private def tryGrow(array: Array[E], oldCap: Int): Unit = {
    lock.unlock() // must release and then re-acquire main lock

    var newArray: Array[E] = null
    if (allocationSpinLock == 0 && atomicAllocationSpinLock
          .compareExchangeStrong(0, 1)) {
      try {
        val growth =
          if (oldCap < 64) oldCap + 2 // grow faster if small}
          else oldCap >> 1
        val newCap = oldCap + growth
        if (newCap < 0)
          throw new OutOfMemoryError(
            s"Required array length $oldCap + $growth is too large"
          )
        if (queue eq array)
          newArray = new Array[AnyRef](newCap).asInstanceOf[Array[E]]
      } finally allocationSpinLock = 0
    }
    if (newArray == null) { // back off if another thread is allocating
      Thread.`yield`()
    }
    lock.lock()
    if (newArray != null && (queue eq array)) {
      queue = newArray.asInstanceOf[Array[E]]
      System.arraycopy(array, 0, newArray, 0, oldCap)
    }
  }

  private def dequeue() = {
    // assert lock.isHeldByCurrentThread();
    val es = queue
    val result = es(0).asInstanceOf[E]
    if (result != null) {
      curSize -= 1
      val n = curSize
      val x = es(n).asInstanceOf[E]
      es(n) = null.asInstanceOf[E]
      if (n > 0) {
        val cmp = comparator()
        if (cmp == null)
          PriorityBlockingQueue.siftDownComparable(0, x, es, n)
        else
          PriorityBlockingQueue.siftDownUsingComparator[E](0, x, es, n, cmp)
      }
    }
    result
  }

  private def heapify(): Unit = {
    val es = queue
    val n = curSize
    var i = (n >>> 1) - 1
    val cmp = comparator()
    if (cmp == null) while (i >= 0) {
      PriorityBlockingQueue.siftDownComparable(i, es(i).asInstanceOf[E], es, n)
      i -= 1
    }
    else
      while (i >= 0) {
        PriorityBlockingQueue.siftDownUsingComparator[E](
          i,
          es(i).asInstanceOf[E],
          es,
          n,
          cmp
        )
        i -= 1
      }
  }

  override def add(e: E) = offer(e)

  override def offer(e: E) = {
    if (e == null) throw new NullPointerException
    val lock = this.lock
    lock.lock()
    var n: Int = 0
    var cap: Int = 0
    var es = queue
    while ({
      n = curSize
      es = queue
      cap = es.length
      n >= cap
    }) tryGrow(es, cap)
    try {
      val cmp = comparator()
      if (cmp == null)
        PriorityBlockingQueue.siftUpComparable(n, e, es)
      else
        PriorityBlockingQueue.siftUpUsingComparator[E](n, e, es, cmp)
      curSize = n + 1
      notEmpty.signal()
    } finally lock.unlock()
    true
  }

  override def put(e: E): Unit = {
    offer(e) // never need to block

  }

  override def offer(e: E, timeout: Long, unit: TimeUnit) = offer(e)
  override def poll() = {
    val lock = this.lock
    lock.lock()
    try dequeue()
    finally lock.unlock()
  }

  @throws[InterruptedException]
  override def take() = {
    val lock = this.lock
    lock.lockInterruptibly()
    var result: E = null.asInstanceOf[E]
    try while ({ result = dequeue(); result == null }) notEmpty.await()
    finally lock.unlock()
    result
  }
  @throws[InterruptedException]
  override def poll(timeout: Long, unit: TimeUnit) = {
    var nanos = unit.toNanos(timeout)
    val lock = this.lock
    lock.lockInterruptibly()
    var result: E = null.asInstanceOf[E]
    try
      while ({ result = dequeue(); result == null && nanos > 0 })
        nanos = notEmpty.awaitNanos(nanos)
    finally lock.unlock()
    result
  }

  override def peek() = {
    val lock = this.lock
    lock.lock()
    try queue(0).asInstanceOf[E]
    finally lock.unlock()
  }

  def comparator(): Comparator[_ >: E] = this.elemComparator

  override def size(): Int = {
    val lock = this.lock
    lock.lock()
    try curSize
    finally lock.unlock()
  }

  override def remainingCapacity() = Integer.MAX_VALUE
  private def indexOf(o: Any): Int = {
    if (o != null) {
      val es = queue
      var i = 0
      val n = curSize
      while ({ i < n }) {
        if (o == es(i)) return i
        i += 1
      }
    }
    -1
  }

  private def removeAt(i: Int): Unit = {
    val es = queue
    val n = curSize - 1
    if (n == i) { // removed last element
      es(i) = null.asInstanceOf[E]
    } else {
      val moved = es(n)
      es(n) = null.asInstanceOf[E]
      val cmp = comparator()
      if (cmp == null)
        PriorityBlockingQueue.siftDownComparable(i, moved, es, n)
      else
        PriorityBlockingQueue.siftDownUsingComparator[E](i, moved, es, n, cmp)
      if (es(i) eq moved)
        if (cmp == null) PriorityBlockingQueue.siftUpComparable(i, moved, es)
        else PriorityBlockingQueue.siftUpUsingComparator[E](i, moved, es, cmp)
    }
    curSize = n
  }

  override def remove(o: Any): Boolean = {
    val lock = this.lock
    lock.lock()
    try {
      val i = indexOf(o)
      if (i == -1) return false
      removeAt(i)
      true
    } finally lock.unlock()
  }

  private[concurrent] def removeEq(o: AnyRef): Unit = {
    val lock = this.lock
    lock.lock()
    try {
      val es = queue
      var i = 0
      val n = curSize
      var break = false
      while (i < n && !break) {
        if (o eq es(i)) {
          removeAt(i)
          break = true
        }
        i += 1
      }
    } finally lock.unlock()
  }

  override def contains(o: Any) = {
    val lock = this.lock
    lock.lock()
    try indexOf(o) != -1
    finally lock.unlock()
  }
  override def toString = Helpers.collectionToString(this)

  override def drainTo(c: util.Collection[_ >: E]) =
    drainTo(c, Integer.MAX_VALUE)
  override def drainTo(c: util.Collection[_ >: E], maxElements: Int): Int = {
    Objects.requireNonNull(c)
    if (c eq this) throw new IllegalArgumentException
    if (maxElements <= 0) return 0
    val lock = this.lock
    lock.lock()
    try {
      val n = Math.min(curSize, maxElements)
      for (i <- 0 until n) {
        c.add(queue(0).asInstanceOf[E]) // In this order, in case add() throws.
        dequeue()
      }
      n
    } finally lock.unlock()
  }

  override def clear(): Unit = {
    val lock = this.lock
    lock.lock()
    try {
      val es = queue
      var i = 0
      val n = curSize
      while ({ i < n }) {
        es(i) = null.asInstanceOf[E]
        i += 1
      }
      curSize = 0
    } finally lock.unlock()
  }

  override def toArray() = {
    val lock = this.lock
    lock.lock()
    try util.Arrays.copyOf(queue.asInstanceOf[Array[AnyRef]], curSize)
    finally lock.unlock()
  }

  override def toArray[T <: AnyRef](a: Array[T]): Array[T] = {
    val lock = this.lock
    lock.lock()
    try {
      val n = curSize
      if (a.length < n) { // Make a new array of a's runtime type, but my contents:
        util.Arrays
          .copyOf(queue, curSize)
          .asInstanceOf[Array[T]]
      } else {
        System.arraycopy(queue, 0, a, 0, n)
        if (a.length > n) a(n) = null.asInstanceOf[T]
        a
      }
    } finally lock.unlock()
  }

  override def iterator() = new Itr(toArray())

  final private[concurrent] class Itr private[concurrent] (
      val array: Array[AnyRef] // Array of all elements
  ) extends util.Iterator[E] {
    private[concurrent] var cursor = 0 // index of next element to return

    private[concurrent] var lastRet =
      -1 // index of last element, or -1 if no such

    override def hasNext(): Boolean = cursor < array.length
    override def next(): E = {
      if (cursor >= array.length) throw new NoSuchElementException
      lastRet = cursor
      cursor += 1
      array(lastRet).asInstanceOf[E]
    }
    override def remove(): Unit = {
      if (lastRet < 0) throw new IllegalStateException
      removeEq(array(lastRet))
      lastRet = -1
    }
    override def forEachRemaining(action: Consumer[_ >: E]): Unit = {
      Objects.requireNonNull(action)
      val es = array
      var i = cursor
      if (i < es.length) {
        lastRet = -1
        cursor = es.length

        while (i < es.length) {
          action.accept(es(i).asInstanceOf[E])
          i += 1
        }
        lastRet = es.length - 1
      }
    }
  }

  /** Immutable snapshot spliterator that binds to elements "late".
   */
  final private[concurrent] class PBQSpliterator private[concurrent] (
      array: Array[AnyRef],
      var index: Int,
      var fence: Int
  ) extends Spliterator[E] {
    private[concurrent] def this(array: Array[AnyRef]) =
      this(array, 0, array.length)
    private[concurrent] def this() = this(toArray())

    override def trySplit(): PBQSpliterator = {
      val hi = fence
      val lo = index
      val mid = (lo + hi) >>> 1
      if (lo >= mid) null
      else {
        index = mid
        new PBQSpliterator(array, lo, index)
      }
    }
    override def forEachRemaining(action: Consumer[_ >: E]): Unit = {
      Objects.requireNonNull(action)
      val hi = fence
      val lo = index
      val es = array
      index = hi // ensure exhaustion

      for (i <- lo until hi) { action.accept(es(i).asInstanceOf[E]) }
    }

    override def tryAdvance(action: Consumer[_ >: E]): Boolean = {
      Objects.requireNonNull(action)
      if (fence > index && index >= 0) {
        val idx = index
        index += 1
        action.accept(array(idx).asInstanceOf[E])
        return true
      }
      false
    }
    override def estimateSize(): Long = fence - index
    override def characteristics(): Int =
      Spliterator.NONNULL | Spliterator.SIZED | Spliterator.SUBSIZED
  }

  override def spliterator(): Spliterator[E] = new PBQSpliterator

  override def removeIf(filter: Predicate[_ >: E]) = {
    Objects.requireNonNull(filter)
    bulkRemove(filter)
  }
  override def removeAll(c: util.Collection[_]) = {
    Objects.requireNonNull(c)
    bulkRemove((e: E) => c.contains(e))
  }
  override def retainAll(c: util.Collection[_]) = {
    Objects.requireNonNull(c)
    bulkRemove((e: E) => !c.contains(e))
  }

  private def bulkRemove(filter: Predicate[_ >: E]): Boolean = {
    val lock = this.lock
    lock.lock()
    try {
      val es = queue
      val end = curSize
      var i = 0
      // Optimize for initial run of survivors
      i = 0
      while ({ i < end && !filter.test(es(i).asInstanceOf[E]) }) i += 1
      if (i >= end) return false
      // Tolerate predicates that reentrantly access the
      // collection for read, so traverse once to find elements
      // to delete, a second pass to physically expunge.
      val beg = i
      val deathRow = PriorityBlockingQueue.nBits(end - beg)
      deathRow(0) = 1L // set bit 0

      i = beg + 1
      while (i < end) {
        if (filter.test(es(i).asInstanceOf[E]))
          PriorityBlockingQueue.setBit(deathRow, i - beg)
        i += 1
      }
      var w = beg
      i = beg
      while (i < end) {
        if (PriorityBlockingQueue.isClear(deathRow, i - beg)) es({
          w += 1; w - 1
        }) = es(i)
        i += 1
      }
      curSize = w
      i = curSize
      while (i < end) {
        es(i) = null.asInstanceOf[E]
        i += 1
      }
      heapify()
      true
    } finally lock.unlock()
  }
  override def forEach(action: Consumer[_ >: E]): Unit = {
    Objects.requireNonNull(action)
    val lock = this.lock
    lock.lock()
    try {
      val es = queue
      var i = 0
      val n = curSize
      while ({ i < n }) {
        action.accept(es(i).asInstanceOf[E])
        i += 1
      }
    } finally lock.unlock()
  }
}
