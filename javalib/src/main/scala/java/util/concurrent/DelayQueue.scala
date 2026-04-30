/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util._
import java.util.concurrent.locks.ReentrantLock

import scala.scalanative.annotation.safePublish

@SerialVersionUID(-4611681480180855872L)
class DelayQueue[E <: Delayed]()
    extends AbstractQueue[E]
    with BlockingQueue[E]
    with Serializable {

  @safePublish
  private final val lock = new ReentrantLock()

  private final val available = lock.newCondition()

  private final val q = new PriorityQueue[E]()

  private def signalAvailableIfNonEmpty(): Unit =
    if (q.peek() != null) available.signal()

  def this(c: Collection[_ <: E]) = {
    this()
    addAll(c)
  }

  override def add(e: E): Boolean = offer(e)

  override def offer(e: E): Boolean = {
    Objects.requireNonNull(e)
    val lock = this.lock
    lock.lock()
    try {
      q.offer(e)
      if (q.peek() eq e)
        available.signal()
      true
    } finally lock.unlock()
  }

  override def put(e: E): Unit = {
    offer(e)
    ()
  }

  override def offer(e: E, timeout: Long, unit: TimeUnit): Boolean = {
    Objects.requireNonNull(unit)
    offer(e)
  }

  override def poll(): E = {
    val lock = this.lock
    lock.lock()
    try {
      val first = q.peek()
      if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0L)
        null.asInstanceOf[E]
      else {
        val result = q.poll()
        signalAvailableIfNonEmpty()
        result
      }
    } finally lock.unlock()
  }

  @throws[InterruptedException]
  override def take(): E = {
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      var result: E = null.asInstanceOf[E]
      while (result == null) {
        val first = q.peek()
        if (first == null) available.await()
        else {
          val delay = first.getDelay(TimeUnit.NANOSECONDS)
          if (delay <= 0L) {
            result = q.poll()
            signalAvailableIfNonEmpty()
          } else available.awaitNanos(delay)
        }
      }
      result
    } finally lock.unlock()
  }

  @throws[InterruptedException]
  override def poll(timeout: Long, unit: TimeUnit): E = {
    var nanos = unit.toNanos(timeout)
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      var result: E = null.asInstanceOf[E]
      var done = false
      while (!done) {
        val first = q.peek()
        if (first == null) {
          if (nanos <= 0L) done = true
          else nanos = available.awaitNanos(nanos)
        } else {
          val delay = first.getDelay(TimeUnit.NANOSECONDS)
          if (delay <= 0L) {
            result = q.poll()
            signalAvailableIfNonEmpty()
            done = true
          } else if (nanos <= 0L) done = true
          else nanos = available.awaitNanos(nanos.min(delay))
        }
      }
      result
    } finally lock.unlock()
  }

  override def peek(): E = {
    val lock = this.lock
    lock.lock()
    try q.peek()
    finally lock.unlock()
  }

  override def remove(): E = super.remove()

  override def element(): E = super.element()

  override def size(): Int = {
    val lock = this.lock
    lock.lock()
    try q.size()
    finally lock.unlock()
  }

  override def remainingCapacity(): Int = Int.MaxValue

  override def clear(): Unit = {
    val lock = this.lock
    lock.lock()
    try q.clear()
    finally lock.unlock()
  }

  override def remove(o: Any): Boolean = {
    val lock = this.lock
    lock.lock()
    try q.remove(o)
    finally lock.unlock()
  }

  override def contains(o: Any): Boolean = {
    val lock = this.lock
    lock.lock()
    try q.contains(o)
    finally lock.unlock()
  }

  override def toArray(): Array[AnyRef] = {
    val lock = this.lock
    lock.lock()
    try q.toArray()
    finally lock.unlock()
  }

  override def toArray[T <: AnyRef](a: Array[T]): Array[T] = {
    val lock = this.lock
    lock.lock()
    try q.toArray(a)
    finally lock.unlock()
  }

  override def iterator(): Iterator[E] = {
    val snapshot = toArray()
    new Iterator[E] {
      private var cursor = 0
      private var last: AnyRef = _

      override def hasNext(): Boolean = cursor < snapshot.length

      override def next(): E = {
        if (!hasNext()) throw new NoSuchElementException()
        val e = snapshot(cursor)
        cursor += 1
        last = e
        e.asInstanceOf[E]
      }

      override def remove(): Unit = {
        if (last == null) throw new IllegalStateException()
        DelayQueue.this.remove(last)
        last = null
      }
    }
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
      var n = 0
      var done = false
      while (!done && n < maxElements) {
        val first = q.peek()
        if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0L)
          done = true
        else {
          c.add(q.poll())
          n += 1
        }
      }
      if (n != 0) signalAvailableIfNonEmpty()
      n
    } finally lock.unlock()
  }
}
