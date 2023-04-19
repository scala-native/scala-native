/*
 * Written by Josh Bloch of Google Inc. and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/.
 */

/*
 * Ported from JSR 166 revision 1.138
 * https://gee.cs.oswego.edu/dl/concurrency-interest/index.html
 */

package java.util

import java.io.Serializable
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.UnaryOperator

import ArrayDeque._

object ArrayDeque {

  /** The maximum size of array to allocate. Some VMs reserve some header words
   *  in an array. Attempts to allocate larger arrays may result in
   *  OutOfMemoryError: Requested array size exceeds VM limit
   */
  private val MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8

}

/** Resizable-array implementation of the {@link Deque} interface. Array deques
 *  have no capacity restrictions; they grow as necessary to support usage. They
 *  are not thread-safe; in the absence of external synchronization, they do not
 *  support concurrent access by multiple threads. Null elements are prohibited.
 *  This class is likely to be faster than java.util.Stack when used as a stack,
 *  and faster than {@link LinkedList} when used as a queue.
 *
 *  Exceptions include remove, {@link #removeFirstOccurrence
 *  removeFirstOccurrence}, {@link #removeLastOccurrence removeLastOccurrence},
 *  {@link #contains contains}, {@link #iterator iterator.remove()}, and the
 *  bulk operations, all of which run in linear time.
 *
 *  <p>The iterators returned by this class's {@link #iterator iterator} method
 *  are <em>fail-fast</em>: If the deque is modified at any time after the
 *  iterator is created, in any way except through the iterator's own {@code
 *  remove} method, the iterator will generally throw a
 *  ConcurrentModificationException. Thus, in the face of concurrent
 *  modification, the iterator fails quickly and cleanly, rather than risking
 *  arbitrary, non-deterministic behavior at an undetermined time in the future.
 *
 *  <p>Note that the fail-fast behavior of an iterator cannot be guaranteed as
 *  it is, generally speaking, impossible to make any hard guarantees in the
 *  presence of unsynchronized concurrent modification. Fail-fast iterators
 *  throw ConcurrentModificationException on a best-effort basis. Therefore, it
 *  would be wrong to write a program that depended on this exception for its
 *  correctness: <i>the fail-fast behavior of iterators should be used only to
 *  detect bugs.</i>
 *
 *  <p>This class and its iterator implement all of the <em>optional</em>
 *  methods of the {@link Collection} and {@link Iterator} interfaces.
 *
 *  <p>This class is a member of the <a
 *  href="{@docRoot}/java.base/java/util/package-summary.html#CollectionsFramework">
 *  Java Collections Framework</a>.
 *
 *  @author
 *    Josh Bloch and Doug Lea
 *  @param <E>
 *    the type of elements held in this deque
 *  @since 1.6
 */
class ArrayDeque[E](
    /** The array in which the elements of the deque are stored. All array cells
     *  not holding deque elements are always null. The array always has at
     *  least one null slot (at tail).
     */
    var elements: Array[Object]
) extends AbstractCollection[E]
    with Deque[E]
    with Cloneable
    with Serializable {
  /*
   * VMs excel at optimizing simple array loops where indices are
   * incrementing or decrementing over a valid slice, e.g.
   *
   * for (int i = start; i < end; i++) ... elements[i]
   *
   * Because in a circular array, elements are in general stored in
   * two disjoint such slices, we help the VM by writing unusual
   * nested loops for all traversals over the elements.  Having only
   * one hot inner loop body instead of two or three eases human
   * maintenance and encourages VM loop inlining into the caller.
   */

  /** The index of the element at the head of the deque (which is the element
   *  that would be removed by remove() or pop()); or an arbitrary number 0 <=
   *  head < elements.length equal to tail if the deque is empty.
   */
  var head: Int = _

  /** The index at which the next element would be added to the tail of the
   *  deque (via addLast(E), add(E), or push(E)); elements[tail] is always null.
   */
  var tail: Int = _

  /** Increases the capacity of this deque by at least the given amount.
   *
   *  @param needed
   *    the required minimum extra capacity; must be positive
   */
  private def grow(needed: Int): Unit = {
    // overflow-conscious code
    val oldCapacity = elements.length
    var newCapacity = 0
    // Double capacity if small; else grow by 50%
    val jump = if (oldCapacity < 64) (oldCapacity + 2) else (oldCapacity >> 1)
    if (jump < needed
        || {
          newCapacity = (oldCapacity + jump); newCapacity
        } - MAX_ARRAY_SIZE > 0)
      newCapacity = this.newCapacity(needed, jump)
    elements = Arrays.copyOf(elements, newCapacity)
    val es = elements
    // Exceptionally, here tail == head needs to be disambiguated
    if (tail < head || (tail == head && es(head) != null)) {
      // wrap around; slide first leg forward to end of array
      val newSpace = newCapacity - oldCapacity
      System.arraycopy(es, head, es, head + newSpace, oldCapacity - head)
      var i = head
      head += newSpace
      val to = head
      while (i < to) {
        es(i) = null
        i += 1
      }
    }
  }

  /** Capacity calculation for edge conditions, especially overflow. */
  private def newCapacity(needed: Int, jump: Int): Int = {
    val oldCapacity = elements.length
    val minCapacity = oldCapacity + needed
    if (minCapacity - MAX_ARRAY_SIZE > 0) {
      if (minCapacity < 0)
        throw new IllegalStateException("Sorry, deque too big")
      return Integer.MAX_VALUE
    }
    if (needed > jump)
      return minCapacity
    return if (oldCapacity + jump - MAX_ARRAY_SIZE < 0)
      oldCapacity + jump
    else MAX_ARRAY_SIZE
  }

  /** Increases the internal storage of this collection, if necessary, to ensure
   *  that it can hold at least the given number of elements.
   *
   *  @param minCapacity
   *    the desired minimum capacity
   *  @since TBD
   */
  /* public */
  def ensureCapacity(minCapacity: Int): Unit = {
    val needed = minCapacity + 1 - elements.length
    if (needed > 0)
      grow(needed)
  }

  /** Minimizes the internal storage of this collection.
   *
   *  @since TBD
   */
  /* public */
  def trimToSize(): Unit = {
    val size = this.size()
    if (size + 1 < elements.length) {
      elements = toArray(new Array[Object](size + 1))
      head = 0
      tail = size
    }
  }

  /** Constructs an empty array deque with an initial capacity sufficient to
   *  hold 16 elements.
   */
  def this() = {
    this(new Array[Object](16 + 1))
  }

  /** Constructs an empty array deque with an initial capacity sufficient to
   *  hold the specified number of elements.
   *
   *  @param numElements
   *    lower bound on initial capacity of the deque
   */
  def this(numElements: Int) = {
    this(
      new Array[Object](
        if (numElements < 1) 1
        else if (numElements == Integer.MAX_VALUE) Integer.MAX_VALUE
        else
          numElements + 1
      )
    )
  }

  /** Constructs a deque containing the elements of the specified collection, in
   *  the order they are returned by the collection's iterator. (The first
   *  element returned by the collection's iterator becomes the first element,
   *  or <i>front</i> of the deque.)
   *
   *  @param c
   *    the collection whose elements are to be placed into the deque
   *  @throws java.lang.NullPointerException
   *    if the specified collection is null
   */
  def this(c: Collection[_ <: E]) = {
    this(c.size())
    copyElements(c)
  }

  /** Circularly increments i, mod modulus. Precondition and postcondition: 0 <=
   *  i < modulus.
   */
  private def inc(_i: Int, modulus: Int): Int = {
    var i = _i + 1
    if (i >= modulus) i = 0
    return i
  }

  /** Circularly decrements i, mod modulus. Precondition and postcondition: 0 <=
   *  i < modulus.
   */
  private def dec(_i: Int, modulus: Int): Int = {
    var i = _i - 1
    if (i < 0) i = modulus - 1
    return i
  }

  /** Circularly adds the given distance to index i, mod modulus. Precondition:
   *  0 <= i < modulus, 0 <= distance <= modulus.
   *  @return
   *    index 0 <= i < modulus
   */
  private def inc(_i: Int, distance: Int, modulus: Int): Int = {
    var i = _i + distance
    if (i - modulus >= 0) i -= modulus
    return i
  }

  /** Subtracts j from i, mod modulus. Index i must be logically ahead of index
   *  j. Precondition: 0 <= i < modulus, 0 <= j < modulus.
   *  @return
   *    the "circular distance" from j to i; corner case i == j is disambiguated
   *    to "empty", returning 0.
   */
  private def sub(_i: Int, j: Int, modulus: Int): Int = {
    var i = _i - j
    if (i < 0) i += modulus
    return i
  }

  /** Returns element at array index i. This is a slight abuse of generics,
   *  accepted by javac.
   */
  private def elementAt(es: Array[Object], i: Int): E = {
    return es(i).asInstanceOf[E]
  }

  /** A version of elementAt that checks for null elements. This check doesn't
   *  catch all possible comodifications, but does catch ones that corrupt
   *  traversal.
   */
  private def nonNullElementAt(es: Array[Object], i: Int): E = {
    val e = es(i).asInstanceOf[E]
    if (e == null)
      throw new ConcurrentModificationException()
    return e
  }

  // The main insertion and extraction methods are addFirst,
  // addLast, pollFirst, pollLast. The other methods are defined in
  // terms of these.

  /** Inserts the specified element at the front of this deque.
   *
   *  @param e
   *    the element to add
   *  @throws java.lang.NullPointerException
   *    if the specified element is null
   */
  def addFirst(e: E): Unit = {
    if (e == null)
      throw new NullPointerException()
    val es = elements
    head = dec(head, es.length)
    es(head) = e.asInstanceOf[Object]
    if (head == tail)
      grow(1)
  }

  /** Inserts the specified element at the end of this deque.
   *
   *  <p>This method is equivalent to {@link #add}.
   *
   *  @param e
   *    the element to add
   *  @throws java.lang.NullPointerException
   *    if the specified element is null
   */
  def addLast(e: E): Unit = {
    if (e == null)
      throw new NullPointerException()
    val es = elements
    es(tail) = e.asInstanceOf[Object]
    tail = inc(tail, es.length)
    if (head == tail)
      grow(1)
  }

  /** Adds all of the elements in the specified collection at the end of this
   *  deque, as if by calling {@link #addLast} on each one, in the order that
   *  they are returned by the collection's iterator.
   *
   *  @param c
   *    the elements to be inserted into this deque
   *  @return
   *    {@code true} if this deque changed as a result of the call
   *  @throws java.lang.NullPointerException
   *    if the specified collection or any of its elements are null
   */
  override def addAll(c: Collection[_ <: E]): Boolean = {
    val s = size()
    val needed = s + c.size() + 1 - elements.length
    if (needed > 0)
      grow(needed)
    copyElements(c)
    return size() > s
  }

  private def copyElements(c: Collection[_ <: E]): Unit = {
    c.forEach(addLast(_))
  }

  /** Inserts the specified element at the front of this deque.
   *
   *  @param e
   *    the element to add
   *  @return
   *    {@code true} (as specified by {@link Deque#offerFirst})
   *  @throws java.lang.NullPointerException
   *    if the specified element is null
   */
  def offerFirst(e: E): Boolean = {
    addFirst(e)
    return true
  }

  /** Inserts the specified element at the end of this deque.
   *
   *  @param e
   *    the element to add
   *  @return
   *    {@code true} (as specified by {@link Deque#offerLast})
   *  @throws java.lang.NullPointerException
   *    if the specified element is null
   */
  def offerLast(e: E): Boolean = {
    addLast(e)
    return true
  }

  /** @throws NoSuchElementException */
  def removeFirst(): E = {
    val e = pollFirst()
    if (e == null)
      throw new NoSuchElementException()
    return e
  }

  /** @throws NoSuchElementException */
  def removeLast(): E = {
    val e = pollLast()
    if (e == null)
      throw new NoSuchElementException()
    return e
  }

  def pollFirst(): E = {
    val es = elements
    val h = head
    val e = elementAt(es, h)
    if (e != null) {
      es(h) = null
      head = inc(h, es.length)
    }
    return e
  }

  def pollLast(): E = {
    val es = elements
    val t = dec(tail, es.length)
    val e = elementAt(es, t)
    if (e != null) {
      tail = t
      es(t) = null
    }
    return e
  }

  /** @throws NoSuchElementException */
  def getFirst(): E = {
    val e = elementAt(elements, head)
    if (e == null)
      throw new NoSuchElementException()
    return e
  }

  /** @throws NoSuchElementException */
  def getLast(): E = {
    val es = elements
    val e = elementAt(es, dec(tail, es.length))
    if (e == null)
      throw new NoSuchElementException()
    return e
  }

  def peekFirst(): E = {
    return elementAt(elements, head)
  }

  def peekLast(): E = {
    val es = elements
    return elementAt(es, dec(tail, es.length))
  }

  /** Removes the first occurrence of the specified element in this deque (when
   *  traversing the deque from head to tail). If the deque does not contain the
   *  element, it is unchanged. More formally, removes the first element {@code
   *  e} such that {@code o.equals(e)} (if such an element exists). Returns
   *  {@code true} if this deque contained the specified element (or
   *  equivalently, if this deque changed as a result of the call).
   *
   *  @param o
   *    element to be removed from this deque, if present
   *  @return
   *    {@code true} if the deque contained the specified element
   */
  def removeFirstOccurrence(o: Any): Boolean = {
    if (o != null) {
      val es = elements
      var i = head
      val end = tail
      var to = if (i <= end) end else es.length
      while (true) {
        while (i < to) {
          if (o.equals(es(i))) {
            delete(i)
            return true
          }
          i += 1
        }
        if (to == end) return false
        i = 0
        to = end
      }
    }
    return false
  }

  /** Removes the last occurrence of the specified element in this deque (when
   *  traversing the deque from head to tail). If the deque does not contain the
   *  element, it is unchanged.
   *
   *  More formally, removes the last element such that {@code o.equals(e)} (if
   *  such an element exists). Returns {@code true} if this deque contained the
   *  specified element (or equivalently, if this deque changed as a result of
   *  the call).
   *
   *  @param o
   *    element to be removed from this deque, if present
   *  @return
   *    {@code true} if the deque contained the specified element
   */
  def removeLastOccurrence(o: Any): Boolean = {
    if (o != null) {
      val es = elements
      var i = tail
      val end = head
      var to = if (i >= end) end else 0
      while (true) {
        i -= 1
        while (i > to - 1) {
          if (o.equals(es(i))) {
            delete(i)
            return true
          }
          i -= 1
        }
        if (to == end) return false
        i = es.length
        to = end
      }
    }
    return false;
  }

  // *** Queue methods ***

  /** Inserts the specified element at the end of this deque.
   *
   *  <p>This method is equivalent to {@link #addLast}.
   *
   *  @param e
   *    the element to add
   *  @return
   *    {@code true} (as specified by {@link Collection#add})
   *  @throws java.lang.NullPointerException
   *    if the specified element is null
   */
  override def add(e: E): Boolean = {
    addLast(e)
    return true
  }

  /** Inserts the specified element at the end of this deque.
   *
   *  <p>This method is equivalent to {@link #offerLast}.
   *
   *  @param e
   *    the element to add
   *  @return
   *    {@code true} (as specified by {@link Queue#offer})
   *  @throws java.lang.NullPointerException
   *    if the specified element is null
   */
  def offer(e: E): Boolean = {
    return offerLast(e)
  }

  /** Retrieves and removes the head of the queue represented by this deque.
   *
   *  This method differs from {@link #poll poll()} only in that it throws an
   *  exception if this deque is empty.
   *
   *  <p>This method is equivalent to {@link #removeFirst}.
   *
   *  @return
   *    the head of the queue represented by this deque
   *  @throws NoSuchElementException
   */
  def remove(): E = {
    return removeFirst()
  }

  /** Retrieves and removes the head of the queue represented by this deque (in
   *  other words, the first element of this deque), or returns {@code null} if
   *  this deque is empty.
   *
   *  <p>This method is equivalent to {@link #pollFirst}.
   *
   *  @return
   *    the head of the queue represented by this deque, or {@code null} if this
   *    deque is empty
   */
  def poll(): E = {
    return pollFirst()
  }

  /** Retrieves, but does not remove, the head of the queue represented by this
   *  deque. This method differs from {@link #peek peek} only in that it throws
   *  an exception if this deque is empty.
   *
   *  <p>This method is equivalent to {@link #getFirst}.
   *
   *  @return
   *    the head of the queue represented by this deque
   *  @throws NoSuchElementException
   */
  def element(): E = {
    return getFirst()
  }

  /** Retrieves, but does not remove, the head of the queue represented by this
   *  deque, or returns {@code null} if this deque is empty.
   *
   *  <p>This method is equivalent to {@link #peekFirst}.
   *
   *  @return
   *    the head of the queue represented by this deque, or {@code null} if this
   *    deque is empty
   */
  def peek(): E = {
    return peekFirst()
  }

  // *** Stack methods ***

  /** Pushes an element onto the stack represented by this deque. In other
   *  words, inserts the element at the front of this deque.
   *
   *  <p>This method is equivalent to {@link #addFirst}.
   *
   *  @param e
   *    the element to push
   *  @throws java.lang.NullPointerException
   *    if the specified element is null
   */
  def push(e: E): Unit = {
    addFirst(e)
  }

  /** Pops an element from the stack represented by this deque. In other words,
   *  removes and returns the first element of this deque.
   *
   *  <p>This method is equivalent to {@link #removeFirst}.
   *
   *  @return
   *    the element at the front of this deque (which is the top of the stack
   *    represented by this deque)
   *  @throws NoSuchElementException
   */
  def pop(): E = {
    return removeFirst()
  }

  /** Removes the element at the specified position in the elements array. This
   *  can result in forward or backwards motion of array elements. We optimize
   *  for least element motion.
   *
   *  <p>This method is called delete rather than remove to emphasize that its
   *  semantics differ from those of {@link List#remove(int)}.
   *
   *  @return
   *    true if elements near tail moved backwards
   */
  private def delete(i: Int): Boolean = {
    val es = elements
    val capacity = es.length
    val h = head
    val t = tail
    // number of elements before to-be-deleted elt
    val front = sub(i, h, capacity)
    // number of elements after to-be-deleted elt
    val back = sub(t, i, capacity) - 1
    if (front < back) {
      // move front elements forwards
      if (h <= i) {
        System.arraycopy(es, h, es, h + 1, front)
      } else { // Wrap around
        System.arraycopy(es, 0, es, 1, i)
        es(0) = es(capacity - 1)
        System.arraycopy(es, h, es, h + 1, front - (i + 1))
      }
      es(h) = null
      head = inc(h, capacity)
      return false
    } else {
      // move back elements backwards
      tail = dec(t, capacity)
      if (i <= tail) {
        System.arraycopy(es, i + 1, es, i, back)
      } else { // Wrap around
        System.arraycopy(es, i + 1, es, i, capacity - (i + 1))
        es(capacity - 1) = es(0)
        System.arraycopy(es, 1, es, 0, t - 1)
      }
      es(tail) = null
      return true
    }
  }

  // *** Collection Methods ***

  /** Returns the number of elements in this deque.
   *
   *  @return
   *    the number of elements in this deque
   */
  def size(): Int = {
    return sub(tail, head, elements.length)
  }

  /** Returns {@code true} if this deque contains no elements.
   *
   *  @return
   *    {@code true} if this deque contains no elements
   */
  override def isEmpty(): Boolean = {
    return head == tail;
  }

  /** Returns an iterator over the elements in this deque. The elements will be
   *  ordered from first (head) to last (tail). This is the same order that
   *  elements would be dequeued (via successive calls to remove or popped (via
   *  successive calls to {@link #pop}).
   *
   *  @return
   *    an iterator over the elements in this deque
   */
  def iterator(): Iterator[E] = {
    return new DeqIterator()
  }

  def descendingIterator(): Iterator[E] = {
    return new DescendingIterator();
  }

  private class DeqIterator(
      /** Index of element to be returned by subsequent call to next. */
      var cursor: Int = head
  ) extends Iterator[E] {

    /** Number of elements yet to be returned. */
    var remaining = size()

    /** Index of element returned by most recent call to next. Reset to -1 if
     *  element is deleted by a call to remove.
     */
    var lastRet = -1;

    def hasNext(): Boolean = {
      return remaining > 0
    }

    def next(): E = {
      if (remaining <= 0)
        throw new NoSuchElementException()
      val es = elements
      val e = nonNullElementAt(es, cursor)
      lastRet = cursor
      cursor = inc(cursor, es.length)
      remaining -= 1
      return e
    }

    def postDelete(leftShifted: Boolean): Unit = {
      if (leftShifted)
        cursor = dec(cursor, elements.length)
    }

    override def remove(): Unit = {
      if (lastRet < 0)
        throw new IllegalStateException()
      postDelete(delete(lastRet))
      lastRet = -1
    }

    override def forEachRemaining(action: Consumer[_ >: E]): Unit = {
      Objects.requireNonNull(action)
      val r = remaining
      if (r <= 0)
        return ()
      remaining = 0
      val es = elements;
      if (es(cursor) == null || sub(tail, cursor, es.length) != r)
        throw new ConcurrentModificationException()
      var i = cursor
      val end = tail
      var to = if (i <= end) end else es.length
      while (true) {
        while (i < to) {
          action.accept(elementAt(es, i))
          i += 1
        }
        if (to == end) {
          if (end != tail)
            throw new ConcurrentModificationException();
          lastRet = dec(end, es.length)
          return ()
        }
        i = 0
        to = end
      }
    }
  }

  private class DescendingIterator
      extends DeqIterator(dec(tail, elements.length)) {

    final override def next(): E = {
      if (remaining <= 0)
        throw new NoSuchElementException()
      val es = elements
      val e = nonNullElementAt(es, cursor)
      lastRet = cursor
      cursor = dec(cursor, es.length)
      remaining -= 1
      return e
    }

    override def postDelete(leftShifted: Boolean): Unit = {
      if (!leftShifted)
        cursor = inc(cursor, elements.length)
    }

    final override def forEachRemaining(action: Consumer[_ >: E]): Unit = {
      Objects.requireNonNull(action)
      val r = remaining
      if (r <= 0)
        return ()
      remaining = 0
      val es = elements
      if (es(cursor) == null || sub(cursor, head, es.length) + 1 != r)
        throw new ConcurrentModificationException()
      var i = cursor
      val end = head
      var to = if (i >= end) end else 0
      while (true) {
        while (i > to - 1) {
          action.accept(elementAt(es, i))
          i -= 1
        }
        if (to == end) {
          if (end != head)
            throw new ConcurrentModificationException()
          lastRet = end
          return ()
        }
        i = es.length - 1
        to = end
      }
    }
  }

  /** Creates a <em><a href="Spliterator.html#binding">late-binding</a></em> and
   *  <em>fail-fast</em> {@link Spliterator} over the elements in this deque.
   *
   *  <p>The {@code Spliterator} reports [[Spliterator.SIZED]],
   *  [[Spliterator.SUBSIZED]], [[Spliterator.ORDERED]], and
   *  [[Spliterator.NONNULL]]. Overriding implementations should document the
   *  reporting of additional characteristic values.
   *
   *  @return
   *    a {@code Spliterator} over the elements in this deque
   *  @since 1.8
   */
  override def spliterator(): Spliterator[E] = {
    return new DeqSpliterator()
  }

  final class DeqSpliterator extends Spliterator[E] {

    /** Constructs late-binding spliterator over all elements. */
    private var fence: Int = -1 // -1 until first use
    private var cursor: Int = _ // current index, modified on traverse/split

    /** Constructs spliterator over the given range. */
    def this(origin: Int, fence: Int) = {
      this()
      // assert 0 <= origin && origin < elements.length;
      // assert 0 <= fence && fence < elements.length;
      this.cursor = origin
      this.fence = fence
    }

    /** Ensures late-binding initialization; then returns fence. */
    private def getFence(): Int = { // force initialization
      var t = fence
      if (t < 0) {
        fence = tail
        t = fence
        cursor = head
      }
      return t
    }

    def trySplit(): DeqSpliterator = {
      val es = elements
      val i = cursor
      val n = sub(getFence(), i, es.length) >> 1
      return if (n <= 0)
        null
      else {
        cursor = inc(i, n, es.length)
        new DeqSpliterator(i, cursor)
      }
    }

    override def forEachRemaining(action: Consumer[_ >: E]): Unit = {
      if (action == null)
        throw new NullPointerException()
      val end = getFence()
      val cursor = this.cursor
      val es = elements
      if (cursor != end) {
        this.cursor = end
        // null check at both ends of range is sufficient
        if (es(cursor) == null || es(dec(end, es.length)) == null)
          throw new ConcurrentModificationException()
        var i = cursor
        var to = if (i <= end) end else es.length
        while (true) {
          while (i < to) {
            action.accept(elementAt(es, i))
            i += 1
          }
          if (to == end) return ()
          i = 0
          to = end
        }
      }
    }

    def tryAdvance(action: Consumer[_ >: E]): Boolean = {
      Objects.requireNonNull(action)
      val es = elements
      if (fence < 0) { fence = tail; cursor = head; } // late-binding
      var i = cursor
      if (i == fence)
        return false
      val e = nonNullElementAt(es, i)
      cursor = inc(i, es.length)
      action.accept(e)
      return true
    }

    def estimateSize(): Long = {
      return sub(getFence(), cursor, elements.length)
    }

    def characteristics(): Int = {
      return Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED
    }
  }

  /** @throws java.lang.NullPointerException */
  override def forEach(action: Consumer[_ >: E]): Unit = {
    Objects.requireNonNull(action)
    val es = elements
    var i = head
    val end = tail
    var to = if (i <= end) end else es.length
    while (true) {
      while (i < to) {
        action.accept(elementAt(es, i))
        i += 1
      }
      if (to == end) {
        if (end != tail) throw new ConcurrentModificationException()
        return ()
      }
      i = 0
      to = end
    }
  }

  /** Replaces each element of this deque with the result of applying the
   *  operator to that element, as specified by {@link List#replaceAll}.
   *
   *  @param operator
   *    the operator to apply to each element
   *  @since TBD
   */
  def replaceAll(operator: UnaryOperator[E]): Unit = {
    Objects.requireNonNull(operator)
    val es = elements
    var i = head
    val end = tail
    var to = if (i <= end) end else es.length
    while (true) {
      while (i < to) {
        es(i) = operator.apply(elementAt(es, i)).asInstanceOf[Object]
        i += 1
      }
      if (to == end) {
        if (end != tail) throw new ConcurrentModificationException()
        return ()
      }
      i = 0
      to = end
    }
  }

  /** @throws java.lang.NullPointerException */
  override def removeIf(filter: Predicate[_ >: E]): Boolean = {
    Objects.requireNonNull(filter)
    return bulkRemove(filter)
  }

  /** @throws java.lang.NullPointerException */
  override def removeAll(c: Collection[_]): Boolean = {
    Objects.requireNonNull(c)
    return bulkRemove(c.contains(_))
  }

  /** @throws java.lang.NullPointerException */
  override def retainAll(c: Collection[_]): Boolean = {
    Objects.requireNonNull(c)
    return bulkRemove(!c.contains(_))
  }

  /** Implementation of bulk remove methods. */
  def bulkRemove(filter: Predicate[_ >: E]): Boolean = {
    val es = elements
    // Optimize for initial run of survivors
    var i = head
    val end = tail
    var to = if (i <= end) end else es.length
    while (true) {
      while (i < to) {
        if (filter.test(elementAt(es, i)))
          return bulkRemoveModified(filter, i);
        i += 1
      }
      if (to == end) {
        if (end != tail) throw new ConcurrentModificationException()
        return false
      }
      i = 0
      to = end
    }
    return false
  }

  // A tiny bit set implementation

  private def nBits(n: Int): Array[Long] = {
    return new Array[Long](((n - 1) >> 6) + 1)
  }
  private def setBit(bits: Array[Long], i: Int): Unit = {
    bits(i >> 6) |= 1L << i
  }
  private def isClear(bits: Array[Long], i: Int): Boolean = {
    return (bits(i >> 6) & (1L << i)) == 0
  }

  /** Helper for bulkRemove, in case of at least one deletion. Tolerate
   *  predicates that reentrantly access the collection for read (but writers
   *  still get CME), so traverse once to find elements to delete, a second pass
   *  to physically expunge.
   *
   *  @param beg
   *    valid index of first element to be deleted
   */
  private def bulkRemoveModified(
      filter: Predicate[_ >: E],
      beg: Int
  ): Boolean = {
    val es = elements
    val capacity = es.length
    val end = tail
    val doRemove = nBits(sub(end, beg, capacity))
    doRemove(0) = 1L // set bit 0
    var i = beg + 1
    var to = if (i <= end) end else es.length
    var k = beg
    var continue = true
    while (continue) {
      while (i < to) {
        if (filter.test(elementAt(es, i)))
          setBit(doRemove, i - k)
        i += 1
      }
      if (to == end) continue = false
      else {
        i = 0
        to = end
        k -= capacity
      }
    }
    // a two-finger traversal, with hare i reading, tortoise w writing
    var w = beg
    i = beg + 1
    to = if (i <= end) end else es.length
    k = beg
    continue = true
    while (continue) {
      // In this loop, i and w are on the same leg, with i > w
      while (i < to) {
        if (isClear(doRemove, i - k)) {
          es(w) = es(i)
          w += 1
        }
        i += 1
      }
      if (to == end) {
        continue = false
      } else {
        // In this loop, w is on the first leg, i on the second
        i = 0
        to = end
        k -= capacity
        while (i < to && w < capacity) {
          if (isClear(doRemove, i - k)) {
            es(w) = es(i)
            w += 1
          }
          i += 1
        }
        if (i >= to) {
          if (w == capacity) w = 0 // "corner" case
          continue = false
        } else {
          w = 0 // w rejoins i on second leg
        }
      }
    }
    if (end != tail) throw new ConcurrentModificationException()
    tail = w
    circularClear(es, tail, end)
    return true;
  }

  /** Returns {@code true} if this deque contains the specified element. More
   *  formally, returns {@code true} if and only if this deque contains at least
   *  one element {@code e} such that {@code o.equals(e)}.
   *
   *  @param o
   *    object to be checked for containment in this deque
   *  @return
   *    {@code true} if this deque contains the specified element
   */
  override def contains(o: Any): Boolean = {
    if (o != null) {
      val es = elements
      var i = head
      val end = tail
      var to = if (i <= end) end else es.length
      while (true) {
        while (i < to) {
          if (o.equals(es(i)))
            return true
          i += 1
        }
        if (to == end) return false
        i = 0
        to = end
      }
    }
    return false
  }

  /** Removes a single instance of the specified element from this deque. If the
   *  deque does not contain the element, it is unchanged. More formally,
   *  removes the first element {@code e} such that {@code o.equals(e)} (if such
   *  an element exists). Returns {@code true} if this deque contained the
   *  specified element (or equivalently, if this deque changed as a result of
   *  the call).
   *
   *  <p>This method is equivalent to [[removeFirstOccurrence]].
   *
   *  @param o
   *    element to be removed from this deque, if present
   *  @return
   *    {@code true} if this deque contained the specified element
   */
  override def remove(o: Any): Boolean = {
    return removeFirstOccurrence(o)
  }

  /** Removes all of the elements from this deque. The deque will be empty after
   *  this call returns.
   */
  override def clear(): Unit = {
    circularClear(elements, head, tail)
    head = 0
    tail = 0
  }

  /** Nulls out slots starting at array index i, upto index end. Condition i ==
   *  end means "empty" - nothing to do.
   */
  private def circularClear(es: Array[Object], _i: Int, end: Int): Unit = {
    var i = _i
    var to = if (i <= end) end else es.length
    // assert 0 <= i && i < es.length;
    // assert 0 <= end && end < es.length;
    while (true) {
      while (i < to) {
        es(i) = null
        i += 1
      }
      if (to == end) return ()
      i = 0
      to = end
    }
  }

  /** Returns an array containing all of the elements in this deque in proper
   *  sequence (from first to last element).
   *
   *  <p>The returned array will be "safe" in that no references to it are
   *  maintained by this deque. (In other words, this method must allocate a new
   *  array). The caller is thus free to modify the returned array.
   *
   *  <p>This method acts as bridge between array-based and collection-based
   *  APIs.
   *
   *  @return
   *    an array containing all of the elements in this deque
   */
  override def toArray(): Array[Object] = {
    return toArrayImpl(classOf[Array[Object]])
  }

  private def toArrayImpl[T <: AnyRef](klazz: Class[Array[T]]): Array[T] = {
    val es = elements;
    var a: Array[T] = null
    val head = this.head
    val tail = this.tail
    val end = tail + (if ((head <= tail)) 0 else es.length)
    if (end >= 0) {
      // Uses null extension feature of copyOfRange
      a = Arrays.copyOfRange(es, head, end, klazz)
    } else {
      // integer overflow!
      a = Arrays.copyOfRange[T, Object](es, 0, end - head, klazz)
      System.arraycopy(es, head, a, 0, es.length - head)
    }
    if (end != tail)
      System.arraycopy(es, 0, a, es.length - head, tail)
    return a
  }

  /** Returns an array containing all of the elements in this deque in proper
   *  sequence (from first to last element); the runtime type of the returned
   *  array is that of the specified array. If the deque fits in the specified
   *  array, it is returned therein. Otherwise, a new array is allocated with
   *  the runtime type of the specified array and the size of this deque.
   *
   *  <p>If this deque fits in the specified array with room to spare (i.e., the
   *  array has more elements than this deque), the element in the array
   *  immediately following the end of the deque is set to {@code null}.
   *
   *  <p>Like the [[toArray()*]] method, this method acts as bridge between
   *  array-based and collection-based APIs. Further, this method allows precise
   *  control over the runtime type of the output array, and may, under certain
   *  circumstances, be used to save allocation costs.
   *
   *  <p>Suppose {@code x} is a deque known to contain only strings. The
   *  following code can be used to dump the deque into a newly allocated array
   *  of {@code String}:
   *
   *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
   *
   *  Note that {@code toArray(new Object[0])} is identical in function to
   *  {@code toArray()}.
   *
   *  @param a
   *    the array into which the elements of the deque are to be stored, if it
   *    is big enough; otherwise, a new array of the same runtime type is
   *    allocated for this purpose
   *  @return
   *    an array containing all of the elements in this deque
   *  @throws java.lang.ArrayStoreException
   *    if the runtime type of the specified array is not a supertype of the
   *    runtime type of every element in this deque
   *  @throws java.lang.NullPointerException
   *    if the specified array is null
   */
  override def toArray[T <: AnyRef](a: Array[T]): Array[T] = {
    val size = this.size()
    if (size > a.length)
      return toArrayImpl(a.getClass().asInstanceOf[Class[Array[T]]])
    val es = elements
    var i = head
    var j = 0
    var len = Math.min(size, es.length - i)
    var continue = true
    while (continue) {
      System.arraycopy(es, i, a, j, len)
      j += len
      if (j == size) continue = false
      else {
        i = 0
        len = tail
      }
    }
    if (size < a.length)
      a(size) = null.asInstanceOf[T]
    return a
  }

  // *** Object methods ***

  /** Returns a copy of this deque.
   *
   *  @return
   *    a copy of this deque
   */
  override def clone(): ArrayDeque[E] = {
    val result = new ArrayDeque[E](Arrays.copyOf(elements, elements.length))
    result.head = this.head
    result.tail = this.tail
    result
  }

}
