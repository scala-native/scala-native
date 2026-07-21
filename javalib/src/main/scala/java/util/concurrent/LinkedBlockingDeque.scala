/* Ported from JSR-166 Expert Group main CVS (Concurrent Versions System)
 * repository as described at Doug Lea "Concurrency JSR-166 Interest Site"
 *    https://gee.cs.oswego.edu/dl/concurrency-interest/.
 *
 *  file: src/main/java/util/concurrent/LinkedBlockingDeque.java
 *  revision 1.83, dated: 2019-10-16
 */

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

import java.util._
import java.util.concurrent.locks.ReentrantLock
import java.util.function.{Consumer, Predicate}

import scala.scalanative.annotation.safePublish

/*
 * An optionally-bounded {@linkplain BlockingDeque blocking deque} based on
 * linked nodes.
 *
 * <p>The optional capacity bound constructor argument serves as a
 * way to prevent excessive expansion. The capacity, if unspecified,
 * is equal to {@link Integer#MAX_VALUE}.  Linked nodes are
 * dynamically created upon each insertion unless this would bring the
 * deque above capacity.
 *
 * <p>Most operations run in constant time (ignoring time spent
 * blocking).  Exceptions include {@link #remove(Object) remove},
 * {@link #removeFirstOccurrence removeFirstOccurrence}, {@link
 * #removeLastOccurrence removeLastOccurrence}, {@link #contains
 * contains}, {@link #iterator iterator.remove()}, and the bulk
 * operations, all of which run in linear time.
 *
 * <p>This class and its iterator implement all of the <em>optional</em>
 * methods of the {@link Collection} and {@link Iterator} interfaces.
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/java.base/java/util/package-summary.html#CollectionsFramework">
 * Java Collections Framework</a>.
 *
 * @since 1.6
 * @author  Doug Lea
 * @param <E> the type of elements held in this deque
 */

@SerialVersionUID(-387911632671998426L)
object LinkedBlockingDeque {
  /*
   * Implemented as a simple doubly-linked list protected by a
   * single lock and using conditions to manage blocking.
   *
   * To implement weakly consistent iterators, it appears we need to
   * keep all Nodes GC-reachable from a predecessor dequeued Node.
   * That would cause two problems:
   * - allow a rogue Iterator to cause unbounded memory retention
   * - cause cross-generational linking of old Nodes to new Nodes if
   *   a Node was tenured while live, which generational GCs have a
   *   hard time dealing with, causing repeated major collections.
   * However, only non-deleted Nodes need to be reachable from
   * dequeued Nodes, and reachability does not necessarily have to
   * be of the kind understood by the GC.  We use the trick of
   * linking a Node that has just been dequeued to itself.  Such a
   * self-link implicitly means to jump to "first" (for next links)
   * or "last" (for prev links).
   */

  /*
   * We have "diamond" multiple interface/abstract class inheritance
   * here, and that introduces ambiguities. Often we want the
   * BlockingDeque javadoc combined with the AbstractQueue
   * implementation, so a lot of method specs are duplicated here.
   */

  /* Doubly-linked list node class */
  private[concurrent] final class Node[E] private[concurrent] (var item: E) {
    /*
     * The item, or null if this node has been removed.
     */

    /*
     * One of:
     * - the real predecessor Node
     * - this Node, meaning the predecessor is tail
     * - null, meaning there is no predecessor
     */
    var prev: Node[E] = _

    /*
     * One of:
     * - the real successor Node
     * - this Node, meaning the successor is head
     * - null, meaning there is no successor
     */
    var next: Node[E] = _
  }
}

class LinkedBlockingDeque[E <: AnyRef](
    val capacity: Int // Maximum number of items in the deque
) extends AbstractQueue[E]
    with BlockingQueue[E]
    with Serializable {

  import LinkedBlockingDeque._

  /*
   * Creates a {@code LinkedBlockingDeque} with the given (fixed) capacity.
   *
   * @param capacity the capacity of this deque
   * @throws IllegalArgumentException if {@code capacity} is less than 1
   */
  // SN: see primary constructor

  if (capacity <= 0)
    throw new IllegalArgumentException

  /*
   * Creates a {@code LinkedBlockingDeque} with a capacity of
   * {@link Integer#MAX_VALUE}.
   */
  def this() = this(Integer.MAX_VALUE)

  /*
   * Creates a {@code LinkedBlockingDeque} with a capacity of
   * {@link Integer#MAX_VALUE}, initially containing the elements of
   * the given collection, added in traversal order of the
   * collection's iterator.
   *
   * @param c the collection of elements to initially contain
   * @throws NullPointerException if the specified collection or any
   *         of its elements are null
   */
  def this(c: Collection[_ <: E]) = {
    this(Integer.MAX_VALUE)
    addAll(c)
  }

  /*
   * Pointer to first node.
   * Invariant: (first == null && last == null) ||
   *            (first.prev == null && first.item != null)
   */
  var first: Node[E] = null

  /*
   * Pointer to last node.
   * Invariant: (first == null && last == null) ||
   *            (last.next == null && last.item != null)
   */
  var last: Node[E] = null

  /* SN: simple volatile is sufficient. 'count' is always accessed under
   * this.lock and never used for CAS (CompareAndSet). Reduce allocations.
   *
   * This introduces a slight non-parallelism with the implementation of
   * close sibling LinkedBlockingQueue. That implementation uses an
   * AtomicInteger. It could almost certainly benefit from using 'volatile'
   * as it also never uses CAS.  An exercise for the devoted, no time
   * or heart for yak shaving now.
   */
  /* Number of items in the deque */
  @volatile private var count = 0

  /* Main lock guarding all access */
  @safePublish
  private final val lock = new ReentrantLock()

  /* Condition for waiting takes */
  @safePublish
  private final val notEmpty = lock.newCondition()

  /* Condition for waiting puts */
  @safePublish
  private final val notFull = lock.newCondition()

  // Basic linking and unlinking operations, called only while holding lock

  /*
   * Links node as first element, or returns false if full.
   */
  private def linkFirst(node: Node[E]): Boolean = {
    // assert lock.isHeldByCurrentThread()
    if (count >= capacity)
      return false
    val f = first
    node.next = f
    first = node
    if (last == null)
      last = node
    else
      f.prev = node

    count += 1
    notEmpty.signal()
    true
  }

  /*
   * Links node as last element, or returns false if full.
   */
  private def linkLast(node: Node[E]): Boolean = {
    // assert lock.isHeldByCurrentThread()
    if (count >= capacity)
      return false
    val l = last
    node.prev = l
    last = node
    if (first == null)
      first = node
    else
      l.next = node

    count += 1
    notEmpty.signal()
    true
  }

  /*
   * Removes and returns first element, or null if empty.
   */
  private def unlinkFirst(): E = {
    // assert lock.isHeldByCurrentThread()
    val f = first
    if (f == null)
      return null.asInstanceOf[E]
    val n = f.next
    val item = f.item
    f.item = null.asInstanceOf[E]
    f.next = f // help GC
    first = n
    if (n == null)
      last = null.asInstanceOf[Node[E]]
    else
      n.prev = null.asInstanceOf[Node[E]]

    count -= 1
    notFull.signal()
    item
  }

  /*
   * Removes and returns last element, or null if empty.
   */
  private def unlinkLast(): E = {
    // assert lock.isHeldByCurrentThread()
    val l = last
    if (l == null)
      return null.asInstanceOf[E]

    val p = l.prev
    val item = l.item
    l.item = null.asInstanceOf[E]
    l.prev = l // help GC
    last = p
    if (p == null)
      first = null.asInstanceOf[Node[E]]
    else
      p.next = null.asInstanceOf[Node[E]]

    count -= 1
    notFull.signal()
    item
  }

  /*
   * Unlinks x.
   */
  def unlink(x: Node[E]): Unit = {
    // assert lock.isHeldByCurrentThread()
    // assert x.item != null
    val p = x.prev
    val n = x.next
    if (p == null) {
      unlinkFirst()
    } else if (n == null) {
      unlinkLast()
    } else {
      p.next = n
      n.prev = p
      x.item = null.asInstanceOf[E]

      // Don't mess with x's links.  They may still be in use by
      // an iterator.
      count -= 1
      notFull.signal()
    }
  }

  // BlockingDeque methods

  /*
   * @throws IllegalStateException if this deque is full
   * @throws NullPointerException {@inheritDoc}
   */
  def addFirst(e: E): Unit = {
    if (!offerFirst(e))
      throw new IllegalStateException("Deque full")
  }

  /*
   * @throws IllegalStateException if this deque is full
   * @throws NullPointerException  {@inheritDoc}
   */
  def addLast(e: E): Unit = {
    if (!offerLast(e))
      throw new IllegalStateException("Deque full")
  }

  /*
   * @throws NullPointerException {@inheritDoc}
   */
  def offerFirst(e: E): Boolean = {
    if (e == null)
      throw new NullPointerException()
    val node = new Node[E](e)
    val lock = this.lock
    lock.lock()
    try {
      return linkFirst(node)
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  /*
   * @throws NullPointerException {@inheritDoc}
   */
  def offerLast(e: E): Boolean = {
    if (e == null)
      throw new NullPointerException()
    val node = new Node[E](e)
    val lock = this.lock
    lock.lock()
    try {
      return linkLast(node)
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  /*
   * @throws NullPointerException {@inheritDoc}
   * @throws InterruptedException {@inheritDoc}
   */
  def putFirst(e: E): Unit = {
    if (e == null)
      throw new NullPointerException()
    val node = new Node[E](e)
    val lock = this.lock
    lock.lock()
    try {
      while (!linkFirst(node))
        notFull.await()
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  /*
   * @throws NullPointerException {@inheritDoc}
   * @throws InterruptedException {@inheritDoc}
   */
  def putLast(e: E): Unit = {
    if (e == null)
      throw new NullPointerException()
    val node = new Node[E](e)
    val lock = this.lock
    lock.lock()
    try {
      while (!linkLast(node))
        notFull.await()
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  /*
   * @throws NullPointerException {@inheritDoc}
   * @throws InterruptedException {@inheritDoc}
   */
  def offerFirst(e: E, timeout: Long, unit: TimeUnit): Boolean = {
    if (e == null)
      throw new NullPointerException()
    val node = new Node[E](e)
    var nanos = unit.toNanos(timeout)
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      while (!linkFirst(node)) {
        if (nanos <= 0L)
          return false
        nanos = notFull.awaitNanos(nanos)
      }
      return true
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  /*
   * @throws NullPointerException {@inheritDoc}
   * @throws InterruptedException {@inheritDoc}
   */
  def offerLast(e: E, timeout: Long, unit: TimeUnit): Boolean = {
    if (e == null)
      throw new NullPointerException()
    val node = new Node[E](e)
    var nanos = unit.toNanos(timeout)
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      while (!linkLast(node)) {
        if (nanos <= 0L)
          return false
        nanos = notFull.awaitNanos(nanos)
      }
      return true
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  /*
   * @throws NoSuchElementException {@inheritDoc}
   */
  def removeFirst(): E = {
    val x = pollFirst()
    if (x == null)
      throw new NoSuchElementException()
    x
  }

  /*
   * @throws NoSuchElementException {@inheritDoc}
   */
  def removeLast(): E = {
    val x = pollLast()
    if (x == null)
      throw new NoSuchElementException()
    x
  }

  def pollFirst(): E = {
    val lock = this.lock
    lock.lock()
    try {
      return unlinkFirst()
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  def pollLast(): E = {
    val lock = this.lock
    lock.lock()
    try {
      return unlinkLast()
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  def takeFirst(): E = {
    val lock = this.lock
    lock.lock()
    try {
      var x = null.asInstanceOf[E]
      while (({ x = unlinkFirst(); x }) == null)
        notEmpty.await()
      return x
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  def takeLast(): E = {
    val lock = this.lock
    lock.lock()
    try {
      var x = null.asInstanceOf[E]
      while (({ x = unlinkLast(); x }) == null)
        notEmpty.await()
      return x
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  def pollFirst(timeout: Long, unit: TimeUnit): E = {
    var nanos = unit.toNanos(timeout)
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      var x = null.asInstanceOf[E]
      while (({ x = unlinkFirst(); x }) == null) {
        if (nanos <= 0L)
          return null.asInstanceOf[E]
        nanos = notEmpty.awaitNanos(nanos)
      }
      return x
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  def pollLast(timeout: Long, unit: TimeUnit): E = {
    var nanos = unit.toNanos(timeout)
    val lock = this.lock
    lock.lockInterruptibly()
    try {
      var x = null.asInstanceOf[E]
      while (({ x = unlinkLast(); x }) == null) {
        if (nanos <= 0L)
          return null.asInstanceOf[E]
        nanos = notEmpty.awaitNanos(nanos)
      }
      return x
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  /*
   * @throws NoSuchElementException {@inheritDoc}
   */
  def getFirst(): E = {
    val x = peekFirst()
    if (x == null)
      throw new NoSuchElementException()
    x
  }

  /*
   * @throws NoSuchElementException {@inheritDoc}
   */
  def getLast(): E = {
    val x = peekLast()
    if (x == null)
      throw new NoSuchElementException()
    x
  }

  def peekFirst(): E = {
    val lock = this.lock
    lock.lock()
    try {
      return if (first == null) null.asInstanceOf[E]
      else first.item
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  def peekLast(): E = {
    val lock = this.lock
    lock.lock()
    try {
      return if (last == null) null.asInstanceOf[E]
      else last.item
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  def removeFirstOccurrence(o: Object): Boolean = {
    if (o == null)
      return false

    val lock = this.lock
    lock.lock()
    try {
      var p = first
      while (p != null) {
        if (o.equals(p.item)) {
          unlink(p)
          return true
        }
        p = p.next
      }
      return false
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  def removeLastOccurrence(o: Object): Boolean = {
    if (o == null)
      return false

    val lock = this.lock
    lock.lock()
    try {
      var p = last
      while (p != null) {
        if (o.equals(p.item)) {
          unlink(p)
          return true
        }
        p = p.prev
      }
      return false
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  // BlockingQueue methods

  /*
   * Inserts the specified element at the end of this deque unless it would
   * violate capacity restrictions.  When using a capacity-restricted deque,
   * it is generally preferable to use method {@link #offer(Object) offer}.
   *
   * <p>This method is equivalent to {@link #addLast}.
   *
   * @throws IllegalStateException if this deque is full
   * @throws NullPointerException if the specified element is null
   */
  override def add(e: E): Boolean = {
    addLast(e)
    true
  }

  /*
   * @throws NullPointerException if the specified element is null
   */
  def offer(e: E): Boolean =
    offerLast(e)

  /*
   * @throws NullPointerException {@inheritDoc}
   * @throws InterruptedException {@inheritDoc}
   */
  def put(e: E): Unit =
    putLast(e)

  /*
   * @throws NullPointerException {@inheritDoc}
   * @throws InterruptedException {@inheritDoc}
   */
  def offer(e: E, timeout: Long, unit: TimeUnit): Boolean =
    offerLast(e, timeout, unit)

  /*
   * Retrieves and removes the head of the queue represented by this deque.
   * This method differs from {@link #poll() poll()} only in that it throws an
   * exception if this deque is empty.
   *
   * <p>This method is equivalent to {@link #removeFirst() removeFirst}.
   *
   * @return the head of the queue represented by this deque
   * @throws NoSuchElementException if this deque is empty
   */
  override def remove(): E =
    removeFirst()

  def poll(): E =
    pollFirst()

  def take(): E =
    takeFirst()

  def poll(timeout: Long, unit: TimeUnit): E =
    pollFirst(timeout, unit)

  /*
   * Retrieves, but does not remove, the head of the queue represented by
   * this deque.  This method differs from {@link #peek() peek()} only in that
   * it throws an exception if this deque is empty.
   *
   * <p>This method is equivalent to {@link #getFirst() getFirst}.
   *
   * @return the head of the queue represented by this deque
   * @throws NoSuchElementException if this deque is empty
   */
  override def element(): E =
    getFirst()

  def peek(): E =
    peekFirst()

  /*
   * Returns the number of additional elements that this deque can ideally
   * (in the absence of memory or resource constraints) accept without
   * blocking. This is always equal to the initial capacity of this deque
   * less the current {@code size} of this deque.
   *
   * <p>Note that you <em>cannot</em> always tell if an attempt to insert
   * an element will succeed by inspecting {@code remainingCapacity}
   * because it may be the case that another thread is about to
   * insert or remove an element.
   */
  def remainingCapacity(): Int = {
    val lock = this.lock
    lock.lock()
    try {
      return capacity - count
    } finally {
      // checkInvariants()
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
    drainTo(c, Integer.MAX_VALUE)

  /*
   * @throws UnsupportedOperationException {@inheritDoc}
   * @throws ClassCastException            {@inheritDoc}
   * @throws NullPointerException          {@inheritDoc}
   * @throws IllegalArgumentException      {@inheritDoc}
   */
  def drainTo(c: Collection[_ >: E], maxElements: Int): Int = {
    Objects.requireNonNull(c)
    if (c == this)
      throw new IllegalArgumentException()
    if (maxElements <= 0)
      return 0
    val lock = this.lock
    lock.lock()
    try {
      val n = Math.min(maxElements, count)
      for (i <- 0 until n) {
        c.add(first.item) // In this order, in case add() throws.
        unlinkFirst()
      }
      return n
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  // Stack methods

  /*
   * @throws IllegalStateException if this deque is full
   * @throws NullPointerException {@inheritDoc}
   */
  def push(e: E): Unit =
    addFirst(e)

  /*
   * @throws NoSuchElementException {@inheritDoc}
   */
  def pop(): E =
    removeFirst()

  // Collection methods

  /*
   * Removes the first occurrence of the specified element from this deque.
   * If the deque does not contain the element, it is unchanged.
   * More formally, removes the first element {@code e} such that
   * {@code o.equals(e)} (if such an element exists).
   * Returns {@code true} if this deque contained the specified element
   * (or equivalently, if this deque changed as a result of the call).
   *
   * <p>This method is equivalent to
   * {@link #removeFirstOccurrence(Object) removeFirstOccurrence}.
   *
   * @param o element to be removed from this deque, if present
   * @return {@code true} if this deque changed as a result of the call
   */
  override def remove(o: Any): Boolean =
    removeFirstOccurrence(o.asInstanceOf[E])

  /*
   * Returns the number of elements in this deque.
   *
   * @return the number of elements in this deque
   */
  def size(): Int = {
    val lock = this.lock
    lock.lock()
    try {
      return count
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  /*
   * Returns {@code true} if this deque contains the specified element.
   * More formally, returns {@code true} if and only if this deque contains
   * at least one element {@code e} such that {@code o.equals(e)}.
   *
   * @param o object to be checked for containment in this deque
   * @return {@code true} if this deque contains the specified element
   */
  override def contains(o: Any): Boolean = {
    if (o == null)
      return false
    val lock = this.lock
    lock.lock()
    try {
      var p = first

      while (p != null) {
        if (o.equals(p.item))
          return true
        p = p.next
      }
      false
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  /*
   * Appends all of the elements in the specified collection to the end of
   * this deque, in the order that they are returned by the specified
   * collection's iterator.  Attempts to {@code addAll} of a deque to
   * itself result in {@code IllegalArgumentException}.
   *
   * @param c the elements to be inserted into this deque
   * @return {@code true} if this deque changed as a result of the call
   * @throws NullPointerException if the specified collection or any
   *         of its elements are null
   * @throws IllegalArgumentException if the collection is this deque
   * @throws IllegalStateException if this deque is full
   * @see #add(Object)
   */
  override def addAll(c: Collection[_ <: E]): Boolean = {
    Objects.requireNonNull(c, "c")

    if (c == this)
      // As historically specified in AbstractQueue#addAll
      throw new IllegalArgumentException()

    // Copy c into a private chain of Nodes
    var beg = null.asInstanceOf[Node[E]]
    var end = beg
    var n = 0

    c.forEach(e => {
      Objects.requireNonNull(e)
      n += 1
      val newNode = new Node[E](e)
      if (beg == null) {
        beg = newNode
        end = newNode
      } else {
        end.next = newNode
        newNode.prev = end
        end = newNode
      }
    })

    if (beg == null)
      return false

    // Atomically append the chain at the end
    val lock = this.lock
    lock.lock()
    try {
      if (count + n <= capacity) {
        beg.prev = last
        if (first == null)
          first = beg
        else
          last.next = beg
        last = end
        count += n
        notEmpty.signalAll()
        return true
      }
    } finally {
      // checkInvariants()
      lock.unlock()
    }
    // Fall back to historic non-atomic implementation, failing
    // with IllegalStateException when the capacity is exceeded.
    super.addAll(c)
  }

  /*
   * Returns an array containing all of the elements in this deque, in
   * proper sequence (from first to last element).
   *
   * <p>The returned array will be "safe" in that no references to it are
   * maintained by this deque.  (In other words, this method must allocate
   * a new array).  The caller is thus free to modify the returned array.
   *
   * <p>This method acts as bridge between array-based and collection-based
   * APIs.
   *
   * @return an array containing all of the elements in this deque
   */
  override def toArray(): Array[AnyRef] = {
    val lock = this.lock
    lock.lock()
    try {
      val a = new Array[AnyRef](count)

      var k = 0
      var p = first
      while (p != null) {
        val idx = k
        k += 1
        a(idx) = p.item
        p = p.next
      }
      a
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  /*
   * Returns an array containing all of the elements in this deque, in
   * proper sequence; the runtime type of the returned array is that of
   * the specified array.  If the deque fits in the specified array, it
   * is returned therein.  Otherwise, a new array is allocated with the
   * runtime type of the specified array and the size of this deque.
   *
   * <p>If this deque fits in the specified array with room to spare
   * (i.e., the array has more elements than this deque), the element in
   * the array immediately following the end of the deque is set to
   * {@code null}.
   *
   * <p>Like the {@link #toArray()} method, this method acts as bridge between
   * array-based and collection-based APIs.  Further, this method allows
   * precise control over the runtime type of the output array, and may,
   * under certain circumstances, be used to save allocation costs.
   *
   * <p>Suppose {@code x} is a deque known to contain only strings.
   * The following code can be used to dump the deque into a newly
   * allocated array of {@code String}:
   *
   * <pre> {@code String[] y = x.toArray(new String[0])}</pre>
   *
   * Note that {@code toArray(new Object[0])} is identical in function to
   * {@code toArray()}.
   *
   * @param a the array into which the elements of the deque are to
   *          be stored, if it is big enough; otherwise, a new array of the
   *          same runtime type is allocated for this purpose
   * @return an array containing all of the elements in this deque
   * @throws ArrayStoreException if the runtime type of the specified array
   *         is not a supertype of the runtime type of every element in
   *         this deque
   * @throws NullPointerException if the specified array is null
   */
  override def toArray[T <: AnyRef](_a: Array[T]): Array[T] = {
    var a = _a

    val lock = this.lock
    lock.lock()
    try {
      val size = count
      if (a.length < size)
        a = java.lang.reflect.Array
          .newInstance(a.getClass.getComponentType, size)
          .asInstanceOf[Array[T]]

      var k = 0
      var p = first
      while (p != null) {
        val idx = k
        k += 1
        a(idx) = p.item.asInstanceOf[T]
        p = p.next
      }
      if (a.length > k)
        a(k) = null.asInstanceOf[T]
      return a
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  override def toString(): String =
    Helpers.collectionToString(this)

  /*
   * Atomically removes all of the elements from this deque.
   * The deque will be empty after this call returns.
   */
  override def clear(): Unit = {
    val lock = this.lock
    lock.lock()
    try {
      var f = first
      while (f != null) {
        f.item = null.asInstanceOf[E]
        val n = f.next
        f.prev = null.asInstanceOf[Node[E]]
        f.next = null.asInstanceOf[Node[E]]
        f = n
      }
      first = null.asInstanceOf[Node[E]]
      last = null.asInstanceOf[Node[E]]
      count = 0
      notFull.signalAll()
    } finally {
      // checkInvariants()
      lock.unlock()
    }
  }

  /*
   * Used for any element traversal that is not entirely under lock.
   * Such traversals must handle both:
   * - dequeued nodes (p.next == p)
   * - (possibly multiple) interior removed nodes (p.item == null)
   */
  def succ(_p: Node[E]): Node[E] = {
    var p = _p
    if (p == ({ p = p.next; p }))
      p = first
    p
  }

  /*
   * Returns an iterator over the elements in this deque in proper sequence.
   * The elements will be returned in order from first (head) to last (tail).
   *
   * <p>The returned iterator is
   * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
   *
   * @return an iterator over the elements in this deque in proper sequence
   */
  def iterator(): Iterator[E] =
    new Itr()

  /*
   * Returns an iterator over the elements in this deque in reverse
   * sequential order.  The elements will be returned in order from
   * last (tail) to first (head).
   *
   * <p>The returned iterator is
   * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
   *
   * @return an iterator over the elements in this deque in reverse order
   */
  def descendingIterator(): Iterator[E] =
    new DescendingItr()

  /*
   * Base class for LinkedBlockingDeque iterators.
   */
  private abstract class AbstractItr extends Iterator[E] {
    /*
     * The next node to return in next().
     */
    var nextVar = null.asInstanceOf[Node[E]]

    /*
     * nextItem holds on to item fields because once we claim that
     * an element exists in hasNext(), we must return item read
     * under lock even if it was in the process of being removed
     * when hasNext() was called.
     */
    var nextItem = null.asInstanceOf[E]

    /*
     * Node returned by most recent call to next. Needed by remove.
     * Reset to null if this element is deleted by a call to remove.
     */
    private var lastRet = null.asInstanceOf[Node[E]]

    def firstNode(): Node[E]
    def nextNode(n: Node[E]): Node[E]

    private def succ(_p: Node[E]): Node[E] = {
      var p = _p
      if (p == { p = nextNode(p); p })
        p = firstNode()
      p
    }

    // set to initial position
    val lock = LinkedBlockingDeque.this.lock
    lock.lock()
    try {
      if ({ nextVar = firstNode(); nextVar } != null)
        nextItem = nextVar.item
    } finally {
      // checkInvariants()
      lock.unlock()
    }

    def hasNext(): Boolean =
      nextVar != null

    def next(): E = {
      var p = null.asInstanceOf[Node[E]]
      if ({ p = nextVar; p } == null)
        throw new NoSuchElementException()
      lastRet = p
      val x = nextItem
      val lock = LinkedBlockingDeque.this.lock
      lock.lock()
      try {
        var e = null.asInstanceOf[E]

        p = nextNode(p)
        while (p != null && { e = p.item; e } == null)
          p = succ(p)

        nextVar = p
        nextItem = e
      } finally {
        // checkInvariants()
        lock.unlock()
      }

      x
    }

    override def forEachRemaining(action: Consumer[_ >: E]): Unit = {
      // A variant of forEachFrom
      Objects.requireNonNull(action, "action")
      var p = null.asInstanceOf[Node[E]]

      if ({ p = nextVar; p } == null)
        return

      lastRet = p
      nextVar = null.asInstanceOf[Node[E]]
      val lock = LinkedBlockingDeque.this.lock
      val batchSize = 64

      var es = null.asInstanceOf[Array[Object]]
      var len = 1
      var n = 0

      var breakSeen = false

      while ({
        lock.lock()
        try {
          if (es == null) {
            p = nextNode(p)
            var q = p
            var breakSeen = false

            while (!breakSeen && (q != null)) {
              if (q.item != null && { len += 1; len } == batchSize)
                breakSeen = true
              if (!breakSeen)
                q = succ(q)
            }
            es = new Array[Object](len)
            es(0) = nextItem
            nextItem = null.asInstanceOf[E]
            n = 1
          } else
            n = 0
          while (p != null && n < len) {
            if ({ es(n) = p.item; es(n) } != null) {
              lastRet = p
              n += 1
            }
            p = succ(p)
          }
        } finally {
          // checkInvariants()
          lock.unlock()
        }

        for (i <- 0 until n)
          action.accept(es(i).asInstanceOf[E])

        n > 0 && p != null
      }) ()

    }

    override def remove(): Unit = {
      val n = lastRet
      if (n == null)
        throw new IllegalStateException()
      lastRet = null
      val lock = LinkedBlockingDeque.this.lock
      lock.lock()
      try {
        if (n.item != null)
          unlink(n)
      } finally {
        // checkInvariants()
        lock.unlock()
      }
    }
  }

  /* Forward iterator */
  private class Itr() extends AbstractItr {
    def firstNode(): Node[E] =
      first

    def nextNode(n: Node[E]): Node[E] =
      n.next
  }

  /* Descending iterator */
  private class DescendingItr() extends AbstractItr {
    def firstNode(): Node[E] =
      last

    def nextNode(n: Node[E]): Node[E] =
      n.prev
  }

  /*
   * A customized variant of Spliterators.IteratorSpliterator.
   * Keep this class in sync with (very similar) LBQSpliterator.
   */
  private final class LBDSpliterator extends Spliterator[E] {
    final val MAX_BATCH = 1 << 25 // max batch array size
    // current node; null until initialized
    var current = null.asInstanceOf[Node[E]]

    /* SN: Editorial comment.
     *   The implementation of this 'batch' variable is faithful to both
     *   the .java original and the sibling LinkedBlockingQueue.scala.
     *
     *   That said, from tracing, the logic appears somewhat strange.
     *   Batch does get larger the more times trySplit() is called on
     *   the same class instance. However, it starts at 0 and grows to
     *   MAX_BATCH. One would expect the progression to be descending,
     *   so that the split had enough work to make parallelism worthwhile.
     *   Probably some subtle behavior designed by the original JSR-166
     *   authors, but astonishing enough to note here.
     */
    var batch = 0 // batch size for splits

    var exhausted = true // true when no more nodes

    var est: Long = size() // size estimate

    def estimateSize(): Long = est

    def trySplit(): Spliterator[E] = {
      var h = null.asInstanceOf[Node[E]]

      if (!exhausted &&
          ({ h = current; h } != null || { h = first; h } != null)
          && h.next != null) {
        batch = Math.min(batch + 1, MAX_BATCH)
        val n = batch
        val a = new Array[Object](n)

        val lock = LinkedBlockingDeque.this.lock
        var i = 0
        var p = current
        lock.lock()
        try {
          if (p != null || { p = first; p } != null)
            while (p != null && i < n) {
              if ({ a(i) = p.item; a(i) } != null)
                i += 1
              p = succ(p)
            }
        } finally {
          // checkInvariants()
          lock.unlock()
        }
        if ({ current = p; current } == null) {
          est = 0L
          exhausted = true
        } else if ({ est -= i; est } < 0L)
          est = 0L
        if (i > 0)
          return Spliterators.spliterator[E](
            a,
            0,
            i,
            Spliterator.ORDERED | Spliterator.NONNULL |
              Spliterator.CONCURRENT
          )
      }
      return null
    }

    def tryAdvance(action: Consumer[_ >: E]): Boolean = {
      Objects.requireNonNull(action)
      if (!exhausted) {
        var e = null.asInstanceOf[E]
        val lock = LinkedBlockingDeque.this.lock
        lock.lock()
        try {
          var p = null.asInstanceOf[Node[E]]
          if ({ p = current; p } != null || { p = first; p } != null)
            while ({
              e = p.item
              p = succ(p)
              (e == null && p != null)
            }) ()

          if ({ current = p; p } == null)
            exhausted = true
        } finally {
          // checkInvariants()
          lock.unlock()
        }
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
        current = null.asInstanceOf[Node[E]]
        forEachFrom(action, p)
      }
    }

    def characteristics(): Int =
      (Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.CONCURRENT)
  }

  /*
   * Returns a {@link Spliterator} over the elements in this deque.
   *
   * <p>The returned spliterator is
   * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
   *
   * <p>The {@code Spliterator} reports {@link Spliterator#CONCURRENT},
   * {@link Spliterator#ORDERED}, and {@link Spliterator#NONNULL}.
   *
   * @implNote
   * The {@code Spliterator} implements {@code trySplit} to permit limited
   * parallelism.
   *
   * @return a {@code Spliterator} over the elements in this deque
   * @since 1.8
   */
  override def spliterator(): Spliterator[E] =
    new LBDSpliterator()

  /*
   * @throws NullPointerException {@inheritDoc}
   */
  override def forEach(action: Consumer[_ >: E]): Unit = {
    Objects.requireNonNull(action, "action")
    forEachFrom(action, null)
  }

  /*
   * Runs action on each element found during a traversal starting at p.
   * If p is null, traversal starts at head.
   */
  def forEachFrom(action: Consumer[_ >: E], _p: Node[E]): Unit = {
    var p = _p

    // Extract batches of elements while holding the lock; then
    // run the action on the elements while not
    val lock = this.lock
    val batchSize = 64 // max number of elements per batch
    // container for batch of elements
    var es = null.asInstanceOf[Array[Object]]
    var len = 0
    var n = 0

    while ({
      lock.lock()
      try {
        if (es == null) {
          if (p == null)
            p = first

          var breakSeen = false
          var q = p

          while (!breakSeen && (q != null)) {
            if (q.item != null && { len += 1; len } == batchSize)
              breakSeen = true
            if (!breakSeen)
              q = succ(q)
          }
          es = new Array[Object](len)
        }

        n = 0

        while ((p != null) && (n < len)) {
          if ({ es(n) = p.item; es(n) } != null)
            n += 1
          p = succ(p)
        }
      } finally {
        // checkInvariants()
        lock.unlock()
      }

      for (i <- 0 until n)
        action.accept(es(i).asInstanceOf[E])

      (n > 0) && (p != null)
    }) ()
  }

  /*
   * @throws NullPointerException {@inheritDoc}
   */
  override def removeIf(filter: Predicate[_ >: E]): Boolean = {
    Objects.requireNonNull(filter, "filter")
    bulkRemove(filter)
  }

  /*
   * @throws NullPointerException {@inheritDoc}
   */
  override def removeAll(c: Collection[_]): Boolean = {
    Objects.requireNonNull(c, "c")
    bulkRemove(e => c.contains(e))
  }

  /*
   * @throws NullPointerException {@inheritDoc}
   */
  override def retainAll(c: Collection[_]): Boolean = {
    Objects.requireNonNull(c, "c")
    bulkRemove(e => !c.contains(e))
  }

  /* Implementation of bulk remove methods. */
  private def bulkRemove(filter: Predicate[_ >: E]): Boolean = {
    var removed = false
    val lock = this.lock
    var p = null.asInstanceOf[Node[E]]
    var nodes = null.asInstanceOf[Array[Node[E]]]
    var len = 0
    var n = 0

    while ({
      // 1. Extract batch of up to 64 elements while holding the lock.
      lock.lock()
      try {
        if (nodes == null) { // first batch; initialize
          p = first

          var breakSeen = false
          var q = p

          while (!breakSeen && (q != null)) {
            if (q.item != null && { len += 1; len } == 64)
              breakSeen = true
            if (!breakSeen)
              q = succ(q)
          }
          nodes = new Array[Node[E]](len)
        }

        n = 0

        while (p != null && n < len) {
          nodes(n) = p
          p = succ(p)

          n += 1
        }
      } finally {
        // checkInvariants()
        lock.unlock()
      }

      // 2. Run the filter on the elements while lock is free.
      var deathRow = 0L // "bitset" of size 64

      for (i <- 0 until n) {
        val e = nodes(i).item
        if ((e != null) && filter.test(e))
          deathRow |= 1L << i
      }

      // 3. Remove any filtered elements while holding the lock.
      if (deathRow != 0) {
        lock.lock()
        try {
          for (i <- 0 until n) {
            val q = nodes(i)
            if ((deathRow & (1L << i)) != 0L
                && (q.item != null)) {
              unlink(q)
              removed = true
            }
            nodes(i) = null.asInstanceOf[Node[E]] // help GC
          }
        } finally {
          // checkInvariants()
          lock.unlock()
        }
      }
      ((n > 0) && (p != null))
    }) ()

    removed
  }

  /*
   * Saves this deque to a stream (that is, serializes it).
   *
   * @param s the stream
   * @throws java.io.IOException if an I/O error occurs
   * @serialData The capacity (int), followed by elements (each an
   * {@code Object}) in the proper order, followed by a null
   */
  // SN: Not implemented, Scala Native does not support serialization
  // private void writeObject(java.io.ObjectOutputStream s)

  /*
   * Reconstitutes this deque from a stream (that is, deserializes it).
   * @param s the stream
   * @throws ClassNotFoundException if the class of a serialized object
   *         could not be found
   * @throws java.io.IOException if an I/O error occurs
   */
  // SN: Not implemented, Scala Native does not support serialization
  // private void readObject(java.io.ObjectInputStream s)

  def checkInvariants(): Unit = {
    // assert lock.isHeldByCurrentThread()
    // Nodes may get self-linked or lose their item, but only
    // after being unlinked and becoming unreachable from first.
    var p = first
    while (p != null) {
      // assert p.next != p
      // assert p.item != null
      p = p.next
    }
  }
}
