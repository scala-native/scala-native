/* Ported from JSR-166 Expert Group main CVS (Concurrent Versions System)
 * repository as described at Doug Lea "Concurrency JSR-166 Interest Site"
 *    https://gee.cs.oswego.edu/dl/concurrency-interest/.
 *
 *  file: src/main/java/util/concurrent/DelayQueue.java
 *  revision 1.78, dated: 2018-09-30
 */

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util.concurrent.TimeUnit.NANOSECONDS
import java.util.concurrent.locks.ReentrantLock
import java.util.{
  AbstractQueue, Collection, Iterator, NoSuchElementException, Objects,
  PriorityQueue
}

/*
 * An unbounded {@linkplain BlockingQueue blocking queue} of
 * {@code Delayed} elements, in which an element can only be taken
 * when its delay has expired.  The <em>head</em> of the queue is that
 * {@code Delayed} element whose delay expired furthest in the
 * past.  If no delay has expired there is no head and {@code poll}
 * will return {@code null}. Expiration occurs when an element's
 * {@code getDelay(TimeUnit.NANOSECONDS)} method returns a value less
 * than or equal to zero.  Even though unexpired elements cannot be
 * removed using {@code take} or {@code poll}, they are otherwise
 * treated as normal elements. For example, the {@code size} method
 * returns the count of both expired and unexpired elements.
 * This queue does not permit null elements.
 *
 * <p>This class and its iterator implement all of the <em>optional</em>
 * methods of the {@link Collection} and {@link Iterator} interfaces.
 * The Iterator provided in method {@link #iterator()} is <em>not</em>
 * guaranteed to traverse the elements of the DelayQueue in any
 * particular order.
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/java.base/java/util/package-summary.html#CollectionsFramework">
 * Java Collections Framework</a>.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <E> the type of elements held in this queue
 */

class DelayQueue[E <: Delayed] extends AbstractQueue[E] with BlockingQueue[E] {
  private val lock = new ReentrantLock()
  private val q = new PriorityQueue[E]()

  // A new DelayQueue is initially empty

  /*
   * Thread designated to wait for the element at the head of
   * the queue.  This variant of the Leader-Follower pattern
   * (http://www.cs.wustl.edu/~schmidt/POSA/POSA2/) serves to
   * minimize unnecessary timed waiting.  When a thread becomes
   * the leader, it waits only for the next delay to elapse, but
   * other threads await indefinitely.  The leader thread must
   * signal some other thread before returning from take() or
   * poll(...), unless some other thread becomes leader in the
   * interim.  Whenever the head of the queue is replaced with
   * an element with an earlier expiration time, the leader
   * field is invalidated by being reset to null, and some
   * waiting thread, but not necessarily the current leader, is
   * signalled.  So waiting threads must be prepared to acquire
   * and lose leadership while waiting.
   */
  private var leader: Thread = _

  /*
   * Condition signalled when a newer element becomes available
   * at the head of the queue or a new thread may need to
   * become leader.
   */
  private val available = lock.newCondition()

  /*
   * Creates a {@code DelayQueue} initially containing the elements of the
   * given collection of {@link Delayed} instances.
   *
   * @param c the collection of elements to initially contain
   * @throws NullPointerException if the specified collection or any
   *         of its elements are null
   */
  def this(c: Collection[_ <: E]) = {
    this()
    this.addAll(c)
  }

  /*
   * Inserts the specified element into this delay queue.
   *
   * @param e the element to add
   * @return {@code true} (as specified by {@link Collection#add})
   * @throws NullPointerException if the specified element is null
   */
  override def add(e: E): Boolean =
    offer(e)

  /*
   * Inserts the specified element into this delay queue.
   *
   * @param e the element to add
   * @return {@code true}
   * @throws NullPointerException if the specified element is null
   */
  def offer(e: E): Boolean = {
    val lock = this.lock
    lock.lock()
    try {
      q.offer(e)
      if (q.peek().eq(e)) { // reference quality: exact element just inserted
        leader = null
        available.signal()
      }
      true
    } finally {
      lock.unlock()
    }
  }

  /*
   * Inserts the specified element into this delay queue. As the queue is
   * unbounded this method will never block.
   *
   * @param e the element to add
   * @throws NullPointerException {@inheritDoc}
   */
  def put(e: E): Unit =
    offer(e)
  /*
   * Inserts the specified element into this delay queue. As the queue is
   * unbounded this method will never block.
   *
   * @param e the element to add
   * @param timeout This parameter is ignored as the method never blocks
   * @param unit This parameter is ignored as the method never blocks
   * @return {@code true}
   * @throws NullPointerException {@inheritDoc}
   */
  def offer(e: E, timeout: Long, unit: TimeUnit): Boolean =
    offer(e)

  /*
   * Retrieves and removes the head of this queue, or returns {@code null}
   * if this queue has no elements with an expired delay.
   *
   * @return the head of this queue, or {@code null} if this
   *         queue has no elements with an expired delay
   */
  def poll(): E = {
    val lock = this.lock
    lock.lock()
    try {
      val first = q.peek()
      if (first == null || first.getDelay(NANOSECONDS) > 0)
        null.asInstanceOf[E]
      else
        q.poll()
    } finally {
      lock.unlock()
    }
  }

  /*
   * Retrieves and removes the head of this queue, waiting if necessary
   * until an element with an expired delay is available on this queue.
   *
   * @return the head of this queue
   * @throws InterruptedException {@inheritDoc}
   */
  def take(): E = {
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      while (true) {
        var first = q.peek()
        if (first == null)
          available.await()
        else {
          val delay = first.getDelay(NANOSECONDS)
          if (delay <= 0L)
            return q.poll()
          // don't retain ref while waiting
          first = null.asInstanceOf[E]
          if (leader != null)
            available.await()
          else {
            val thisThread = Thread.currentThread()
            leader = thisThread
            try {
              available.awaitNanos(delay)
            } finally {
              if (leader == thisThread)
                leader = null
            }
          }
        }
      }
      null.asInstanceOf[E] // should never get here
    } finally {
      if (leader == null && q.peek() != null)
        available.signal()
      lock.unlock()
    }
  }

  /*
   * Retrieves and removes the head of this queue, waiting if necessary
   * until an element with an expired delay is available on this queue,
   * or the specified wait time expires.
   *
   * @return the head of this queue, or {@code null} if the
   *         specified waiting time elapses before an element with
   *         an expired delay becomes available
   * @throws InterruptedException {@inheritDoc}
   */
  def poll(timeout: Long, unit: TimeUnit): E = {
    var nanos = unit.toNanos(timeout)
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      while (true) {
        var first = q.peek()
        if (first == null) {
          if (nanos <= 0L)
            return null.asInstanceOf[E]
          else
            nanos = available.awaitNanos(nanos)
        } else {
          val delay = first.getDelay(NANOSECONDS)
          if (delay <= 0L)
            return q.poll()
          if (nanos <= 0L)
            return null.asInstanceOf[E]
          // don't retain ref while waiting
          first = null.asInstanceOf[E]
          if (nanos < delay || leader != null)
            nanos = available.awaitNanos(nanos);
          else {
            val thisThread = Thread.currentThread();
            leader = thisThread
            try {
              val timeLeft = available.awaitNanos(delay)
              nanos -= delay - timeLeft
            } finally {
              if (leader == thisThread)
                leader = null
            }
          }
        }
      }
      null.asInstanceOf[E] // should never get here
    } finally {
      if (leader == null && q.peek() != null)
        available.signal();
      lock.unlock();
    }
  }

  /*
   * Retrieves, but does not remove, the head of this queue, or
   * returns {@code null} if this queue is empty.  Unlike
   * {@code poll}, if no expired elements are available in the queue,
   * this method returns the element that will expire next,
   * if one exists.
   *
   * @return the head of this queue, or {@code null} if this
   *         queue is empty
   */
  def peek(): E = {
    val lock = this.lock
    lock.lock()
    try {
      return q.peek()
    } finally {
      lock.unlock()
    }
  }

  def size(): Int = {
    val lock = this.lock
    lock.lock()
    try {
      return q.size()
    } finally {
      lock.unlock()
    }
  }

  /*
   * @throws UnsupportedOperationException {@inheritDoc}
   * @throws ClassCastException            {@inheritDoc}
   * @throws NullPointerException          {@inheritDoc}
   * @throws IllegalArgumentException      {@inheritDoc}
   */
  def drainTo(c: Collection[_ >: E]): Int =
    return drainTo(c, Integer.MAX_VALUE)

  /*
   * @throws UnsupportedOperationException {@inheritDoc}
   * @throws ClassCastException            {@inheritDoc}
   * @throws NullPointerException          {@inheritDoc}
   * @throws IllegalArgumentException      {@inheritDoc}
   */
  def drainTo(c: Collection[? >: E], maxElements: Int): Int = {
    Objects.requireNonNull(c)
    if (c == this)
      throw new IllegalArgumentException()
    if (maxElements <= 0)
      return 0
    val lock = this.lock
    lock.lock()
    try {
      var n = 0
      var first: E = null.asInstanceOf[E]

      while (n < maxElements
          && ({ first = q.peek(); first }) != null
          && first.getDelay(NANOSECONDS) <= 0) {
        c.add(first) // In this order, in case add() throws.
        q.poll()
        n += 1
      }
      return n
    } finally {
      lock.unlock()
    }
  }

  /*
   * Atomically removes all of the elements from this delay queue.
   * The queue will be empty after this call returns.
   * Elements with an unexpired delay are not waited for; they are
   * simply discarded from the queue.
   */
  override def clear(): Unit = {
    val lock = this.lock
    lock.lock()
    try {
      q.clear()
    } finally {
      lock.unlock()
    }
  }

  /*
   * Always returns {@code Integer.MAX_VALUE} because
   * a {@code DelayQueue} is not capacity constrained.
   *
   * @return {@code Integer.MAX_VALUE}
   */
  def remainingCapacity(): Int =
    Integer.MAX_VALUE

  /*
   * Returns an array containing all of the elements in this queue.
   * The returned array elements are in no particular order.
   *
   * <p>The returned array will be "safe" in that no references to it are
   * maintained by this queue.  (In other words, this method must allocate
   * a new array).  The caller is thus free to modify the returned array.
   *
   * <p>This method acts as bridge between array-based and collection-based
   * APIs.
   *
   * @return an array containing all of the elements in this queue
   */
  override def toArray(): Array[Object] = {
    val lock = this.lock
    lock.lock()
    try {
      return q.toArray()
    } finally {
      lock.unlock()
    }
  }

  /*
   * Returns an array containing all of the elements in this queue; the
   * runtime type of the returned array is that of the specified array.
   * The returned array elements are in no particular order.
   * If the queue fits in the specified array, it is returned therein.
   * Otherwise, a new array is allocated with the runtime type of the
   * specified array and the size of this queue.
   *
   * <p>If this queue fits in the specified array with room to spare
   * (i.e., the array has more elements than this queue), the element in
   * the array immediately following the end of the queue is set to
   * {@code null}.
   *
   * <p>Like the {@link #toArray()} method, this method acts as bridge between
   * array-based and collection-based APIs.  Further, this method allows
   * precise control over the runtime type of the output array, and may,
   * under certain circumstances, be used to save allocation costs.
   *
   * <p>The following code can be used to dump a delay queue into a newly
   * allocated array of {@code Delayed}:
   *
   * <pre> {@code Delayed[] a = q.toArray(new Delayed[0]);}</pre>
   *
   * Note that {@code toArray(new Object[0])} is identical in function to
   * {@code toArray()}.
   *
   * @param a the array into which the elements of the queue are to
   *          be stored, if it is big enough; otherwise, a new array of the
   *          same runtime type is allocated for this purpose
   * @return an array containing all of the elements in this queue
   * @throws ArrayStoreException if the runtime type of the specified array
   *         is not a supertype of the runtime type of every element in
   *         this queue
   * @throws NullPointerException if the specified array is null
   */
  override def toArray[T <: AnyRef](a: Array[T]): Array[T] = {
    val lock = this.lock
    lock.lock()
    try {
      return q.toArray(a)
    } finally {
      lock.unlock()
    }
  }

  /* @since JDK 21 */
  override def remove(): E = {
    val h = poll()

    if (h == null.asInstanceOf[E])
      throw new NoSuchElementException()
    else
      h
  }

  /*
   * Removes a single instance of the specified element from this
   * queue, if it is present, whether or not it has expired.
   */
  override def remove(o: Any): Boolean = {
    val lock = this.lock
    lock.lock()
    try {
      return q.remove(o)
    } finally {
      lock.unlock()
    }
  }

  /*
   * Identity-based version for use in Itr.remove.
   */
  private def removeEQ(o: AnyRef): Unit = {
    val lock = this.lock
    lock.lock()
    try {
      val it = q.iterator()
      var breakSeen = false
      while (it.hasNext() && !breakSeen) {
        if (o.eq(it.next())) {
          it.remove()
          breakSeen = true
        }
      }
    } finally {
      lock.unlock()
    }
  }

  /*
   * Returns an iterator over all the elements (both expired and
   * unexpired) in this queue. The iterator does not return the
   * elements in any particular order.
   *
   * <p>The returned iterator is
   * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
   *
   * @return an iterator over the elements in this queue
   */
  def iterator(): Iterator[E] =
    new Itr(toArray())

  /*
   * Snapshot iterator that works off copy of underlying q array.
   */
  private class Itr(array: Array[Object]) extends Iterator[E] {
    // Array of all elements
    var cursor = 0 // index of next element to return
    var lastRet = -1 // index of last element, or -1 if no such

    def hasNext(): Boolean = {
      cursor < array.length
    }

    def next(): E = {
      if (cursor >= array.length)
        throw new NoSuchElementException()
      lastRet = cursor
      cursor += 1
      array(lastRet).asInstanceOf[E]
    }

    override def remove(): Unit = {
      if (lastRet < 0)
        throw new IllegalStateException()
      removeEQ(array(lastRet))
      lastRet = -1
    }
  }
}
