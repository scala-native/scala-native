package java.util.concurrent

// Ported from Harmony

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import java.util
import java.util.{NoSuchElementException, concurrent}

import sun.reflect.generics.reflectiveObjects.NotImplementedException

class LinkedBlockingQueue[E](
    private final val capacity: Int = Integer.MAX_VALUE)
    extends java.util.AbstractQueue[E]
    with BlockingQueue[E]
    with java.io.Serializable {

  import LinkedBlockingQueue._

  if (capacity <= 0) throw new IllegalArgumentException()

  /** Current number of elements */
  private final val count: AtomicInteger = new AtomicInteger(0)

  /**
   * Head of linked list.
   * Invariant: head.item == null
   */
  //transient
  private var head: Node[E] = new Node[E](null.asInstanceOf[E])

  /**
   * Tail of linked list.
   * Invariant: last.next == null
   */
  //transient
  private var last: Node[E] = new Node[E](null.asInstanceOf[E])

  /** Lock held by take, poll, etc */
  private final val takeLock: ReentrantLock = new ReentrantLock() {}

  /** Wait queue for waiting takes */
  private final val notEmpty: Condition = takeLock.newCondition()

  /** Lock held by put, offer, etc */
  private final val putLock: ReentrantLock = new ReentrantLock() {}

  /** Wait queue for waiting puts */
  private final val notFull: Condition = putLock.newCondition()

  def this(c: util.Collection[_ <: E]) = {
    this()
    val putLock: ReentrantLock = this.putLock
    putLock.lock() // Never contended, but necessary for visibility
    try {
      var n: Int = 0
      val it     = c.iterator()
      while (it.hasNext) {
        val e: E = it.next()
        if (e == null)
          throw new NullPointerException()
        if (n == capacity)
          throw new IllegalStateException("Queue full")
        enqueue(e)
        n += 1
      }
      count.set(n)
    } finally {
      putLock.unlock()
    }
  }

  private def signalNotEmpty(): Unit = {
    val takeLock: ReentrantLock = this.takeLock
    takeLock.lock()
    try {
      notEmpty.signal()
    } finally {
      takeLock.unlock()
    }
  }

  private def signalNotFull(): Unit = {
    val putLock: ReentrantLock = this.putLock
    putLock.lock()
    try {
      notFull.signal()
    } finally {
      putLock.unlock()
    }
  }

  private def enqueue(x: E): Unit = {
    last.next = new Node[E](x)
    last = last.next
  }

  private def dequeue: E = {
    val h: Node[E]     = head
    val first: Node[E] = h.next
    h.next = h // help GC
    head = first
    val x: E = first.item
    first.item = null.asInstanceOf[E]
    x
  }

  def fullyLock(): Unit = {
    putLock.lock()
    takeLock.lock()
  }

  def fullyUnlock(): Unit = {
    takeLock.unlock()
    putLock.unlock()
  }

  override def size(): Int = count.get()

  override def remainingCapacity(): Int = capacity - count.get()

  override def put(e: E): Unit = {
    if (e == null) throw new NullPointerException()
    // Note: convention in all put/take/etc is to preset local var
    // holding count negative to indicate failure unless set.
    var c: Int                 = -1
    val putLock: ReentrantLock = this.putLock
    val count: AtomicInteger   = this.count
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
      enqueue(e)
      c = count.getAndIncrement()
      if (c + 1 < capacity)
        notFull.signal()
    } finally {
      putLock.unlock()
    }
    if (c == 0)
      signalNotEmpty()
  }

  override def offer(e: E, timeout: Long, unit: TimeUnit): Boolean = {
    if (e == null) throw new NullPointerException()
    var nanos: Long            = unit.toNanos(timeout)
    var c: Int                 = -1
    val putLock: ReentrantLock = this.putLock
    val count: AtomicInteger   = this.count
    putLock.lockInterruptibly()
    try {
      while (count.get() == capacity) {
        if (nanos <= 0)
          return false
        nanos = notFull.awaitNanos(nanos)
      }
      enqueue(e)
      c = count.getAndIncrement()
      if (c + 1 < capacity)
        notFull.signal()
    } finally {
      putLock.unlock()
    }
    if (c == 0)
      signalNotEmpty()
    true
  }

  override def offer(e: E): Boolean = {
    if (e == null) throw new NullPointerException()
    val count: AtomicInteger = this.count
    if (count.get() == capacity)
      return false
    var c: Int                 = -1
    val putLock: ReentrantLock = this.putLock
    putLock.lock()
    try {
      if (count.get() < capacity) {
        enqueue(e)
        c = count.getAndIncrement()
        if (c + 1 < capacity)
          notFull.signal()
      }
    } finally {
      putLock.unlock()
    }
    if (c == 0)
      signalNotEmpty()
    c >= 0
  }

  override def take(): E = {
    var x: E                    = null.asInstanceOf[E]
    var c: Int                  = -1
    val count: AtomicInteger    = this.count
    val takeLock: ReentrantLock = this.takeLock
    takeLock.lockInterruptibly()
    try {
      while (count.get() == 0) notEmpty.await()
      x = dequeue
      c = count.getAndDecrement()
      if (c > 1)
        notEmpty.signal()
    } finally {
      takeLock.unlock()
    }
    if (c == capacity)
      signalNotFull()
    x
  }

  override def poll(timeout: Long, unit: TimeUnit): E = {
    var x: E                    = null.asInstanceOf[E]
    var c: Int                  = -1
    var nanos: Long             = unit.toNanos(timeout)
    val count: AtomicInteger    = this.count
    val takeLock: ReentrantLock = this.takeLock
    takeLock.lockInterruptibly()
    try {
      while (count.get() == 0) {
        if (nanos <= 0)
          return null.asInstanceOf[E]
        nanos = notEmpty.awaitNanos(nanos)
      }
      x = dequeue
      c = count.getAndDecrement()
      if (c > 1)
        notEmpty.signal()
    } finally {
      takeLock.unlock()
    }
    if (c == capacity)
      signalNotFull()
    x
  }

  override def poll(): E = {
    val count: AtomicInteger = this.count
    if (count.get() == 0)
      return null.asInstanceOf[E]
    var x: E                    = null.asInstanceOf[E]
    var c: Int                  = -1
    val takeLock: ReentrantLock = this.takeLock
    takeLock.lock()
    try {
      if (count.get() > 0) {
        x = dequeue
        c = count.getAndDecrement()
        if (c > 1)
          notEmpty.signal()
      }
    } finally {
      takeLock.unlock()
    }
    if (c == capacity)
      signalNotFull()
    x
  }

  override def peek(): E = {
    if (count.get() == 0)
      return null.asInstanceOf[E]
    val takeLock: ReentrantLock = this.takeLock
    takeLock.lock()
    try {
      val first: Node[E] = head.next
      if (first == null)
        null.asInstanceOf[E]
      else
        first.item
    } finally {
      takeLock.unlock()
    }
  }

  def unlink(p: Node[E], trail: Node[E]): Unit = {
    // p.next is not changed, to allow iterators that are
    // traversing p to maintain their weak-consistency guarantee.
    p.item = null.asInstanceOf[E]
    trail.next = p.next
    if (last == p)
      last = trail
    if (count.getAndDecrement() == capacity)
      notFull.signal()
  }

  override def remove(o: Any): Boolean = {
    if (o == null) return false
    fullyLock()
    try {
      var trail: Node[E] = head
      var p: Node[E]     = trail.next
      while (p != null) {
        if (o.equals(p.item)) {
          unlink(p, trail)
          return true
        }

        trail = p
        p = p.next
      }
      false
    } finally {
      fullyUnlock()
    }
  }

  override def contains(o: Any): Boolean = throw new NotImplementedException()

  override def toArray: Array[Object] = {
    fullyLock()
    try {
      val size: Int        = count.get()
      val a: Array[Object] = new Array[Object](size)
      var k: Int           = 0
      var p: Node[E]       = head.next
      while (p != null) {
        a(k) = p.item.asInstanceOf[Object]

        k += 1
        p = p.next
      }
      a
    } finally {
      fullyUnlock()
    }
  }

  override def toArray[T](a: Array[T]): Array[T] = {
    fullyLock()
    try {
      val size: Int = count.get()
      if (a.length < size) {}
      //TODO grow input array

      var k: Int     = 0
      var p: Node[E] = head.next
      while (p != null) {
        a(k) = p.item.asInstanceOf[T]

        k += 1
        p = p.next
      }
      if (a.length > k)
        a(k) = null.asInstanceOf[T]
      a
    } finally {
      fullyUnlock()
    }
  }

  override def toString: String = {
    fullyLock()
    try {
      super.toString
    } finally {
      fullyUnlock()
    }
  }

  override def clear(): Unit = {
    fullyLock()
    try {
      var h          = head
      var p: Node[E] = h.next
      while (p != null) {
        h.next = h
        p.item = null.asInstanceOf[E]

        h = p
        p = h.next
      }
      head = last
      if (count.getAndSet(0) == capacity)
        notFull.signal()
    } finally {
      fullyUnlock()
    }
  }

  override def drainTo(c: util.Collection[_ >: E]): Int =
    drainTo(c, Integer.MAX_VALUE)

  override def drainTo(c: util.Collection[_ >: E], maxElements: Int): Int = {
    if (c == null)
      throw new NullPointerException()
    if (c == this)
      throw new IllegalArgumentException()
    var signalNotFull: Boolean  = false
    val takeLock: ReentrantLock = this.takeLock
    takeLock.lock()
    try {
      val n: Int = Math.min(maxElements, count.get())
      // count.get provides visibility to first n Nodes
      var h: Node[E] = head
      var i: Int     = 0
      try {
        while (i < n) {
          var p: Node[E] = h.next
          c.add(p.item)
          p.item = null.asInstanceOf[E]
          h.next = h
          h = p
          i += 1
        }
        n
      } finally {
        // Restore invariants even if c.add() threw
        if (i > 0) {
          head = h
          signalNotFull = count.getAndAdd(-i) == capacity
        }
      }
    } finally {
      takeLock.unlock()
      if (signalNotFull)
        this.signalNotFull()
    }
  }

  override def iterator(): util.Iterator[E] = new Itr()

  private class Itr extends util.Iterator[E] {
    /*
     * Basic weakly-consistent iterator.  At all times hold the next
     * item to hand out so that if hasNext() reports true, we will
     * still have it to return even if lost race with a take etc.
     */
    private var current: Node[E]  = null.asInstanceOf[Node[E]]
    private var lastRet: Node[E]  = null.asInstanceOf[Node[E]]
    private var currentElement: E = null.asInstanceOf[E]

    fullyLock()
    try {
      current = head.next
      if (current != null)
        currentElement = current.item
    } finally {
      fullyUnlock()
    }

    override def hasNext: Boolean = current != null

    private def nextNode(p: Node[E]): Node[E] = {
      var s: Node[E] = p.next
      if (p == s)
        head.next
      // Skip over removed nodes.
      // May be necessary if multiple interior Nodes are removed.
      while (s != null && s.item == null) s = s.next
      s
    }

    override def next(): E = {
      fullyLock()
      try {
        if (current == null)
          throw new util.NoSuchElementException()
        val x: E = currentElement
        lastRet = current
        current = nextNode(current)
        currentElement =
          if (current == null) null.asInstanceOf[E] else current.item
        x
      } finally {
        fullyUnlock()
      }
    }

    override def remove(): Unit = {
      if (lastRet == null)
        throw new IllegalStateException()
      fullyLock()
      try {
        val node: Node[E]  = lastRet
        var trail: Node[E] = head
        var break: Boolean = false
        var p: Node[E]     = trail.next
        while (p != null) {
          if (p == node) {
            unlink(p, trail)
            break = true
          }

          trail = p
          p = p.next
        }
      } finally {
        fullyUnlock()
      }
    }

    private def writeObject(s: java.io.ObjectOutputStream): Unit = {
      fullyLock()
      try {
        // Write out any hidden stuff, plus capacity// Write out any hidden stuff, plus capacity
        s.defaultWriteObject()

        // Write out all elements in the proper order.
        var p: Node[E] = head.next
        while (p != null) {
          s.writeObject(p.item)

          p = p.next
        }

        // Use trailing null as sentinel
        s.writeObject(null)
      } finally {
        fullyUnlock()
      }
    }

    private def readObject(s: java.io.ObjectInputStream): Unit = {
      // Read in capacity, and any hidden stuff
      s.defaultReadObject()

      count.set(0)
      head = new Node[E](null.asInstanceOf[E])
      last = head

      // Read in all elements and place in queue
      var break: Boolean = false
      while (!break) {
        val item: E = s.readObject().asInstanceOf[E]
        if (item == null)
          break = true
        if (!break) add(item)
      }
    }

  }

}

object LinkedBlockingQueue {

  private final val serialVersionUID: Long = -6903933977591709194L

  class Node[E](var item: E) {

    /**
     * One of:
     * - the real successor Node
     * - this Node, meaning the successor is head.next
     * - null, meaning there is no successor (this is the last node)
     */
    var next: Node[E] = _

  }

}
