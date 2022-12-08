/*
 * Written by Josh Bloch of Google Inc. and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/.
 */

/*
 * Ported from JSR 166 revision 1.138
 * https://gee.cs.oswego.edu/dl/concurrency-interest/index.html
 */

package java.util;

import java.io.Serializable
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.UnaryOperator

import ArrayDeque._

object ArrayDeque {

  private val MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8

}

class ArrayDeque[E](
    var elements: Array[Object]
) extends AbstractCollection[E]
    with Deque[E]
    with Cloneable
    with Serializable {

  var head: Int = _

  var tail: Int = _

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
    // checkInvariants();
  }

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

  def ensureCapacity(minCapacity: Int): Unit = {
    val needed = minCapacity + 1 - elements.length
    if (needed > 0)
      grow(needed)
    // checkInvariants();
  }

  def trimToSize(): Unit = {
    val size = this.size()
    if (size + 1 < elements.length) {
      elements = toArray(new Array[Object](size + 1))
      head = 0
      tail = size
    }
    // checkInvariants();
  }

  def this() = {
    this(new Array[Object](16 + 1))
  }

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

  def this(c: Collection[_ <: E]) = {
    this(c.size())
    copyElements(c)
  }

  private def inc(_i: Int, modulus: Int): Int = {
    var i = _i + 1
    if (i >= modulus) i = 0
    return i
  }

  private def dec(_i: Int, modulus: Int): Int = {
    var i = _i - 1
    if (i < 0) i = modulus - 1
    return i
  }

  private def inc(_i: Int, distance: Int, modulus: Int): Int = {
    var i = _i + distance
    if (i - modulus >= 0) i -= modulus
    return i
  }

  private def sub(_i: Int, j: Int, modulus: Int): Int = {
    var i = _i - j
    if (i < 0) i += modulus
    return i
  }

  private def elementAt(es: Array[Object], i: Int): E = {
    return es(i).asInstanceOf[E]
  }

  private def nonNullElementAt(es: Array[Object], i: Int): E = {
    val e = es(i).asInstanceOf[E]
    if (e == null)
      throw new ConcurrentModificationException()
    return e
  }

  def addFirst(e: E): Unit = {
    if (e == null)
      throw new NullPointerException()
    val es = elements
    head = dec(head, es.length)
    es(head) = e.asInstanceOf[Object]
    if (head == tail)
      grow(1)
    // checkInvariants();
  }

  def addLast(e: E): Unit = {
    if (e == null)
      throw new NullPointerException()
    val es = elements
    es(tail) = e.asInstanceOf[Object]
    tail = inc(tail, es.length)
    if (head == tail)
      grow(1)
    // checkInvariants();
  }

  override def addAll(c: Collection[_ <: E]): Boolean = {
    val s = size()
    val needed = s + c.size() + 1 - elements.length
    if (needed > 0)
      grow(needed)
    copyElements(c)
    // checkInvariants();
    return size() > s
  }

  private def copyElements(c: Collection[_ <: E]): Unit = {
    c.forEach(addLast(_))
  }

  def offerFirst(e: E): Boolean = {
    addFirst(e)
    return true
  }

  def offerLast(e: E): Boolean = {
    addLast(e)
    return true
  }

  def removeFirst(): E = {
    val e = pollFirst()
    if (e == null)
      throw new NoSuchElementException()
    // checkInvariants();
    return e
  }

  def removeLast(): E = {
    val e = pollLast()
    if (e == null)
      throw new NoSuchElementException()
    // checkInvariants();
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
    // checkInvariants();
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
    // checkInvariants();
    return e
  }

  def getFirst(): E = {
    val e = elementAt(elements, head)
    if (e == null)
      throw new NoSuchElementException()
    // checkInvariants();
    return e
  }

  def getLast(): E = {
    val es = elements
    val e = elementAt(es, dec(tail, es.length))
    if (e == null)
      throw new NoSuchElementException()
    // checkInvariants();
    return e
  }

  def peekFirst(): E = {
    // checkInvariants();
    return elementAt(elements, head)
  }

  def peekLast(): E = {
    // checkInvariants();
    val es = elements
    return elementAt(es, dec(tail, es.length))
  }

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

  override def add(e: E): Boolean = {
    addLast(e)
    return true
  }

  def offer(e: E): Boolean = {
    return offerLast(e)
  }

  def remove(): E = {
    return removeFirst()
  }

  def poll(): E = {
    return pollFirst()
  }

  def element(): E = {
    return getFirst()
  }

  def peek(): E = {
    return peekFirst()
  }

  // *** Stack methods ***

  def push(e: E): Unit = {
    addFirst(e)
  }

  def pop(): E = {
    return removeFirst()
  }

  private def delete(i: Int): Boolean = {
    // checkInvariants();
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
      // checkInvariants();
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
      // checkInvariants();
      return true
    }
  }

  // *** Collection Methods ***

  def size(): Int = {
    return sub(tail, head, elements.length)
  }

  override def isEmpty(): Boolean = {
    return head == tail;
  }

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

  def spliterator(): Spliterator[E] = {
    return new DeqSpliterator()
  }

  final class DeqSpliterator extends Spliterator[E] {

    private var fence: Int = -1 // -1 until first use
    private var cursor: Int = _ // current index, modified on traverse/split

    def this(origin: Int, fence: Int) = {
      this()
      // assert 0 <= origin && origin < elements.length;
      // assert 0 <= fence && fence < elements.length;
      this.cursor = origin
      this.fence = fence
    }

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
    // checkInvariants();
  }

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
    // checkInvariants();
  }

  override def removeIf(filter: Predicate[_ >: E]): Boolean = {
    Objects.requireNonNull(filter)
    return bulkRemove(filter)
  }

  override def removeAll(c: Collection[_]): Boolean = {
    Objects.requireNonNull(c)
    return bulkRemove(c.contains(_))
  }

  override def retainAll(c: Collection[_]): Boolean = {
    Objects.requireNonNull(c)
    return bulkRemove(!c.contains(_))
  }

  def bulkRemove(filter: Predicate[_ >: E]): Boolean = {
    // checkInvariants();
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
    // checkInvariants();
    return true;
  }

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

  override def remove(o: Any): Boolean = {
    return removeFirstOccurrence(o)
  }

  override def clear(): Unit = {
    circularClear(elements, head, tail)
    head = 0
    tail = 0
    // checkInvariants();
  }

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

  override def clone(): ArrayDeque[E] = {
    val result = new ArrayDeque[E](Arrays.copyOf(elements, elements.length))
    result.head = this.head
    result.tail = this.tail
    result
  }

  private def checkInvariants(): Unit = {
    // Use head and tail fields with empty slot at tail strategy.
    // head == tail disambiguates to "empty".
    try {
      val capacity = elements.length
      // assert 0 <= head && head < capacity;
      // assert 0 <= tail && tail < capacity;
      // assert capacity > 0;
      // assert size() < capacity;
      // assert head == tail || elements[head] != null;
      // assert elements[tail] == null;
      // assert head == tail || elements[dec(tail, capacity)] != null;
    } catch {
      case t: Throwable =>
        System.err.printf(
          "head=%d tail=%d capacity=%d%n",
          Array[Object](
            Integer.valueOf(head),
            Integer.valueOf(tail),
            Integer.valueOf(elements.length)
          )
        )
        System.err.printf(
          "elements=%s%n",
          Array[Object](Arrays.toString(elements))
        )
        throw t
    }
  }

}
