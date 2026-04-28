/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util._
import java.util.concurrent.locks.ReentrantLock

import scala.scalanative.annotation.safePublish

@SerialVersionUID(-387911632671998426L)
class LinkedBlockingDeque[E <: AnyRef](val capacity: Int)
    extends AbstractQueue[E]
    with BlockingDeque[E]
    with Serializable {

  private final class Node(var item: E) {
    var prev: Node = _
    var next: Node = _
  }

  if (capacity <= 0) throw new IllegalArgumentException()

  @safePublish
  private final val lock = new ReentrantLock()

  private final val notEmpty = lock.newCondition()
  private final val notFull = lock.newCondition()

  private var first: Node = _
  private var last: Node = _
  private var count = 0

  def this() = this(Int.MaxValue)

  def this(c: Collection[_ <: E]) = {
    this(Int.MaxValue)
    addAll(c)
  }

  private def linkFirst(node: Node): Boolean = {
    if (count >= capacity) false
    else {
      val f = first
      node.next = f
      first = node
      if (last == null) last = node
      else f.prev = node
      count += 1
      notEmpty.signal()
      true
    }
  }

  private def linkLast(node: Node): Boolean = {
    if (count >= capacity) false
    else {
      val l = last
      node.prev = l
      last = node
      if (first == null) first = node
      else l.next = node
      count += 1
      notEmpty.signal()
      true
    }
  }

  private def unlinkFirst(): E = {
    val f = first
    if (f == null) null.asInstanceOf[E]
    else {
      val n = f.next
      val item = f.item
      f.item = null.asInstanceOf[E]
      f.next = f
      first = n
      if (n == null) last = null
      else n.prev = null
      count -= 1
      notFull.signal()
      item
    }
  }

  private def unlinkLast(): E = {
    val l = last
    if (l == null) null.asInstanceOf[E]
    else {
      val p = l.prev
      val item = l.item
      l.item = null.asInstanceOf[E]
      l.prev = l
      last = p
      if (p == null) first = null
      else p.next = null
      count -= 1
      notFull.signal()
      item
    }
  }

  private def unlink(node: Node): Unit = {
    val p = node.prev
    val n = node.next
    if (p == null) unlinkFirst()
    else if (n == null) unlinkLast()
    else {
      p.next = n
      n.prev = p
      node.item = null.asInstanceOf[E]
      count -= 1
      notFull.signal()
    }
  }

  override def addFirst(e: E): Unit =
    if (!offerFirst(e)) throw new IllegalStateException("Deque full")

  override def addLast(e: E): Unit =
    if (!offerLast(e)) throw new IllegalStateException("Deque full")

  override def offerFirst(e: E): Boolean = {
    Objects.requireNonNull(e)
    val lock = this.lock
    lock.lock()
    try linkFirst(new Node(e))
    finally lock.unlock()
  }

  override def offerLast(e: E): Boolean = {
    Objects.requireNonNull(e)
    val lock = this.lock
    lock.lock()
    try linkLast(new Node(e))
    finally lock.unlock()
  }

  @throws[InterruptedException]
  override def putFirst(e: E): Unit = {
    Objects.requireNonNull(e)
    val node = new Node(e)
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      while (!linkFirst(node)) notFull.await()
    } finally lock.unlock()
  }

  @throws[InterruptedException]
  override def putLast(e: E): Unit = {
    Objects.requireNonNull(e)
    val node = new Node(e)
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      while (!linkLast(node)) notFull.await()
    } finally lock.unlock()
  }

  @throws[InterruptedException]
  override def offerFirst(e: E, timeout: Long, unit: TimeUnit): Boolean = {
    Objects.requireNonNull(e)
    var nanos = unit.toNanos(timeout)
    val node = new Node(e)
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      while (!linkFirst(node)) {
        if (nanos <= 0L) return false
        nanos = notFull.awaitNanos(nanos)
      }
      true
    } finally lock.unlock()
  }

  @throws[InterruptedException]
  override def offerLast(e: E, timeout: Long, unit: TimeUnit): Boolean = {
    Objects.requireNonNull(e)
    var nanos = unit.toNanos(timeout)
    val node = new Node(e)
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      while (!linkLast(node)) {
        if (nanos <= 0L) return false
        nanos = notFull.awaitNanos(nanos)
      }
      true
    } finally lock.unlock()
  }

  override def removeFirst(): E = {
    val x = pollFirst()
    if (x == null) throw new NoSuchElementException()
    x
  }

  override def removeLast(): E = {
    val x = pollLast()
    if (x == null) throw new NoSuchElementException()
    x
  }

  override def pollFirst(): E = {
    val lock = this.lock
    lock.lock()
    try unlinkFirst()
    finally lock.unlock()
  }

  override def pollLast(): E = {
    val lock = this.lock
    lock.lock()
    try unlinkLast()
    finally lock.unlock()
  }

  @throws[InterruptedException]
  override def takeFirst(): E = {
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      var x = unlinkFirst()
      while (x == null) {
        notEmpty.await()
        x = unlinkFirst()
      }
      x
    } finally lock.unlock()
  }

  @throws[InterruptedException]
  override def takeLast(): E = {
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      var x = unlinkLast()
      while (x == null) {
        notEmpty.await()
        x = unlinkLast()
      }
      x
    } finally lock.unlock()
  }

  @throws[InterruptedException]
  override def pollFirst(timeout: Long, unit: TimeUnit): E = {
    var nanos = unit.toNanos(timeout)
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      var x = unlinkFirst()
      while (x == null) {
        if (nanos <= 0L) return null.asInstanceOf[E]
        nanos = notEmpty.awaitNanos(nanos)
        x = unlinkFirst()
      }
      x
    } finally lock.unlock()
  }

  @throws[InterruptedException]
  override def pollLast(timeout: Long, unit: TimeUnit): E = {
    var nanos = unit.toNanos(timeout)
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      var x = unlinkLast()
      while (x == null) {
        if (nanos <= 0L) return null.asInstanceOf[E]
        nanos = notEmpty.awaitNanos(nanos)
        x = unlinkLast()
      }
      x
    } finally lock.unlock()
  }

  override def getFirst(): E = {
    val x = peekFirst()
    if (x == null) throw new NoSuchElementException()
    x
  }

  override def getLast(): E = {
    val x = peekLast()
    if (x == null) throw new NoSuchElementException()
    x
  }

  override def peekFirst(): E = {
    val lock = this.lock
    lock.lock()
    try if (first == null) null.asInstanceOf[E] else first.item
    finally lock.unlock()
  }

  override def peekLast(): E = {
    val lock = this.lock
    lock.lock()
    try if (last == null) null.asInstanceOf[E] else last.item
    finally lock.unlock()
  }

  override def removeFirstOccurrence(o: Any): Boolean = {
    if (o == null) return false
    val lock = this.lock
    lock.lock()
    try {
      var p = first
      while (p != null) {
        if (o == p.item) {
          unlink(p)
          return true
        }
        p = p.next
      }
      false
    } finally lock.unlock()
  }

  override def removeLastOccurrence(o: Any): Boolean = {
    if (o == null) return false
    val lock = this.lock
    lock.lock()
    try {
      var p = last
      while (p != null) {
        if (o == p.item) {
          unlink(p)
          return true
        }
        p = p.prev
      }
      false
    } finally lock.unlock()
  }

  override def add(e: E): Boolean = {
    addLast(e)
    true
  }

  override def offer(e: E): Boolean = offerLast(e)

  override def put(e: E): Unit = putLast(e)

  override def offer(e: E, timeout: Long, unit: TimeUnit): Boolean =
    offerLast(e, timeout, unit)

  override def remove(): E = removeFirst()

  override def poll(): E = pollFirst()

  override def take(): E = takeFirst()

  override def poll(timeout: Long, unit: TimeUnit): E =
    pollFirst(timeout, unit)

  override def element(): E = getFirst()

  override def peek(): E = peekFirst()

  override def push(e: E): Unit = addFirst(e)

  override def pop(): E = removeFirst()

  override def remove(o: Any): Boolean = removeFirstOccurrence(o)

  override def contains(o: Any): Boolean = {
    if (o == null) return false
    val lock = this.lock
    lock.lock()
    try {
      var p = first
      while (p != null) {
        if (o == p.item) return true
        p = p.next
      }
      false
    } finally lock.unlock()
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
    try capacity - count
    finally lock.unlock()
  }

  override def clear(): Unit = {
    val lock = this.lock
    lock.lock()
    try {
      var p = first
      while (p != null) {
        val n = p.next
        p.item = null.asInstanceOf[E]
        p.prev = null
        p.next = null
        p = n
      }
      first = null
      last = null
      count = 0
      notFull.signalAll()
    } finally lock.unlock()
  }

  private def snapshot(reverse: Boolean): Array[AnyRef] = {
    val lock = this.lock
    lock.lock()
    try {
      val a = new Array[AnyRef](count)
      var i = 0
      var p = if (reverse) last else first
      while (p != null) {
        a(i) = p.item
        i += 1
        p = if (reverse) p.prev else p.next
      }
      a
    } finally lock.unlock()
  }

  private final class SnapshotIterator(reverse: Boolean) extends Iterator[E] {
    private val elements = snapshot(reverse)
    private var cursor = 0
    private var lastRet: AnyRef = _

    override def hasNext(): Boolean = cursor < elements.length

    override def next(): E = {
      if (!hasNext()) throw new NoSuchElementException()
      val e = elements(cursor)
      cursor += 1
      lastRet = e
      e.asInstanceOf[E]
    }

    override def remove(): Unit = {
      if (lastRet == null) throw new IllegalStateException()
      if (reverse) removeLastOccurrence(lastRet)
      else removeFirstOccurrence(lastRet)
      lastRet = null
    }
  }

  override def iterator(): Iterator[E] = new SnapshotIterator(false)

  override def descendingIterator(): Iterator[E] = new SnapshotIterator(true)

  override def spliterator(): Spliterator[E] =
    Spliterators.spliterator(
      iterator(),
      size().toLong,
      Spliterator.CONCURRENT | Spliterator.NONNULL | Spliterator.ORDERED
    )

  override def toArray(): Array[AnyRef] = snapshot(false)

  override def toArray[T <: AnyRef](a: Array[T]): Array[T] = {
    val elements = snapshot(false)
    val result =
      if (a.length >= elements.length) a
      else
        java.lang.reflect.Array
          .newInstance(a.getClass.getComponentType, elements.length)
          .asInstanceOf[Array[T]]
    System.arraycopy(elements, 0, result, 0, elements.length)
    if (result.length > elements.length)
      result(elements.length) = null.asInstanceOf[T]
    result
  }

  override def drainTo(c: Collection[_ >: E]): Int =
    drainTo(c, Int.MaxValue)

  override def drainTo(c: Collection[_ >: E], maxElements: Int): Int = {
    Objects.requireNonNull(c)
    if (c.asInstanceOf[AnyRef] eq this) throw new IllegalArgumentException()
    if (maxElements <= 0) return 0

    val lock = this.lock
    lock.lock()
    try {
      val n = maxElements.min(count)
      var i = 0
      while (i < n) {
        c.add(unlinkFirst())
        i += 1
      }
      i
    } finally lock.unlock()
  }
}
