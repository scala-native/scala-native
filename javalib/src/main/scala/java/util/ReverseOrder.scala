package java.util

import java.{lang => jl}

import java.{util => ju}
import java.util.function.UnaryOperator

// ReverseOrderDequeView ------------------------------------------------

private[util] trait ReverseOrderDequeViewTrait[E] extends Deque[E] {

  val underlying: Deque[E]

  override def add(e: E): Boolean =
    underlying.offerFirst(e)

  override def addFirst(e: E): Unit =
    underlying.addLast(e)

  override def addLast(e: E): Unit =
    underlying.addFirst(e)

  override def contains(o: Any): Boolean =
    underlying.contains(o)

  def descendingIterator(): java.util.Iterator[E] =
    underlying.iterator()

  override def element(): E =
    underlying.getLast()

  override def getFirst(): E =
    underlying.getLast()

  override def getLast(): E =
    underlying.getFirst()

  override def iterator(): java.util.Iterator[E] =
    underlying.descendingIterator()

  override def offer(e: E): Boolean =
    underlying.offerFirst(e)

  override def offerFirst(e: E): Boolean =
    underlying.offerLast(e)

  override def offerLast(e: E): Boolean =
    underlying.offerFirst(e)

  override def peek(): E =
    underlying.peekLast()

  override def peekFirst(): E =
    underlying.peekLast()

  override def peekLast(): E =
    underlying.peekFirst()

  override def poll(): E =
    underlying.pollLast()

  override def pollFirst(): E =
    underlying.pollLast()

  override def pollLast(): E =
    underlying.pollFirst()

  override def pop(): E =
    underlying.removeLast()

  override def push(e: E): Unit =
    underlying.addLast(e)

  override def remove(): E =
    underlying.removeLast()

  override def removeFirst(): E =
    underlying.removeLast()

  override def removeFirstOccurrence(o: Any): Boolean =
    underlying.removeLastOccurrence(o)

  override def removeLast(): E =
    underlying.removeFirst()

  override def removeLastOccurrence(o: Any): Boolean =
    underlying.removeFirstOccurrence(o)

  def size(): Int =
    underlying.size()
}

private[util] class ReverseOrderDequeView[E](forward: Deque[E])
    extends AbstractCollection[E] // not AbstractQueue[E] of it overriden
    with ReverseOrderDequeViewTrait[E] {

  val underlying = forward

  override def reversed(): Deque[E] =
    underlying
}

// ReverseOrderListView -------------------------------------------------
private[util] trait ReverseOrderListViewTrait[E] extends AbstractList[E] {
  import ReverseOrderListView._

  val underlying: List[E]

  private def fromUnderlyingIndex(underlyingIndex: Int): Int = {
    /* Presumably the underlying method returned a valid index.
     *  Trust, but verify, at least for early development.
     */
    val underlyingSize = underlying.size()
    Objects.checkIndex(underlyingIndex, underlyingSize)

    (underlyingSize - underlyingIndex) - 1
  }

  /* This is the essense of 'reversed' and heavily used, so get it right.
   * Trust, but verify, at least for early development.
   */
  private def toUnderlyingIndex(index: Int): Int = {
    // Follows the usual Java practice: the upper bound is excluded.
    val underlyingSize = underlying.size()
    val highestValidUnderlyingIndex = underlyingSize - 1
    Objects.checkIndex(index, highestValidUnderlyingIndex + 1)

    highestValidUnderlyingIndex - index
  }

  private def toUnderlyingIndexInclusive(index: Int): Int = {
    val underlyingSize = underlying.size()
    val highestValidUnderlyingIndex = underlyingSize
    Objects.checkIndex(index, highestValidUnderlyingIndex + 1)

    highestValidUnderlyingIndex - index
  }

  override def add(index: Int, element: E): Unit =
    underlying.add(toUnderlyingIndexInclusive(index), element)

  // implementation has a more accurate "changed" condition than AbstractList
  override def add(e: E): Boolean = {
    val sizeAtEntry = underlying.size()
    underlying.addFirst(e)
    underlying.size() > sizeAtEntry
  }

  override def addAll(index: Int, c: java.util.Collection[? <: E]): Boolean = {
    val startSize = this.size()
    val colIterator = c.iterator()

    val underIterator =
      underlying.listIterator(toUnderlyingIndexInclusive(index))

    while (colIterator.hasNext()) {
      underIterator.add(colIterator.next()) // add thows if nothing added.
      underIterator.previous()
    }

    this.size() > startSize
  }

  override def addAll(c: java.util.Collection[? <: E]): Boolean =
    this.addAll(size(), c)

  override def addFirst(e: E): Unit =
    underlying.addLast(e)

  override def addLast(e: E): Unit =
    underlying.addFirst(e)

  override def clear(): Unit =
    underlying.clear()

  override def contains(o: Any): Boolean =
    underlying.contains(o)

  override def containsAll(c: java.util.Collection[?]): Boolean =
    underlying.containsAll(c)

  override def get(index: Int): E =
    underlying.get(toUnderlyingIndex(index))

  override def getFirst(): E = {
    if (underlying.isEmpty())
      throw new NoSuchElementException()
    else
      underlying.getLast()
  }

  override def getLast(): E = {
    if (underlying.isEmpty())
      throw new NoSuchElementException()
    else
      underlying.getFirst()
  }

  override def indexOf(o: Any): Int = {
    val ulIndex = underlying.lastIndexOf(o)
    if (ulIndex < 0) ulIndex
    else fromUnderlyingIndex(ulIndex)
  }

  override def isEmpty(): Boolean =
    underlying.isEmpty()

  override def iterator(): java.util.Iterator[E] =
    new ReverseOrderIterator(underlying)

  override def lastIndexOf(o: Any): Int = {
    val ulIndex = underlying.indexOf(o)
    if (ulIndex < 0) ulIndex
    else fromUnderlyingIndex(ulIndex)
  }

  override def listIterator(): java.util.ListIterator[E] =
    this.listIterator(0)

  override def listIterator(index: Int): java.util.ListIterator[E] =
    new ReversedListIterator(underlying, toUnderlyingIndexInclusive(index))

  override def remove(index: Int): E =
    underlying.remove(toUnderlyingIndex(index))

  override def remove(o: Any): Boolean = {
    val lstIter = this.listIterator()
    var removed = false

    while (lstIter.hasNext() && !removed) {
      if (lstIter.next() == o) {
        lstIter.remove()
        removed = true
      }
    }

    removed
  }

  override def removeAll(c: java.util.Collection[?]): Boolean =
    underlying.removeAll(c)

  override def removeFirst(): E = {
    if (underlying.isEmpty())
      throw new NoSuchElementException()
    else
      underlying.removeLast()
  }

  override def removeLast(): E = {
    if (underlying.isEmpty())
      throw new NoSuchElementException()
    else
      underlying.removeFirst()
  }

  override def replaceAll(operator: UnaryOperator[E]): Unit =
    underlying.replaceAll(operator)

  override def retainAll(c: java.util.Collection[?]): Boolean =
    underlying.retainAll(c)

  override def set(index: Int, element: E): E =
    underlying.set(toUnderlyingIndex(index), element)

  def size(): Int =
    underlying.size()

  override def sort(c: Comparator[_ >: E]): Unit =
    underlying.sort(c.reversed())

  // Use default implementation in Collection.scala
  // def spliterator(): Spliterator[E]

  override def subList(fromIndex: Int, toIndex: Int): java.util.List[E] = {
    Objects.checkFromIndexSize(fromIndex, toIndex - fromIndex, this.size())

    /* Minus & plus 1 account for swapping of inclusive & exclusive bounds.
     * After bounds check above, this will always be within the underlying
     * bounds. A zero fromIndex here becomes the index of the last element
     * of underlying. A zero toIndex here will never be less than a zero
     * fromIndex here, so it too will pass the underlying bounds check.
     */

    new ReverseOrderListView(
      underlying
        .subList(
          toUnderlyingIndex(toIndex - 1),
          toUnderlyingIndex(fromIndex - 1)
        )
    )
  }

  override def toArray(): Array[AnyRef] =
    toArray(new Array[AnyRef](underlying.size()))

  override def toArray[T <: AnyRef](a: Array[T]): Array[T] = {
    Objects.requireNonNull(a, "a")

    val underlyingSize = underlying.size()
    val toFill: Array[T] =
      if (a.length >= underlyingSize) a
      else
        jl.reflect.Array
          .newInstance(a.getClass.getComponentType, underlyingSize)
          .asInstanceOf[Array[T]]

    val iter = underlying.iterator()

    for (i <- (underlyingSize - 1) to 0 by -1)
      toFill(i) = iter.next().asInstanceOf[T]

    if (toFill.length > underlyingSize)
      toFill(size()) = null.asInstanceOf[T]

    toFill
  }
}

private[util] class ReverseOrderListView[E](forward: List[E])
    extends ReverseOrderListViewTrait[E] {

  val underlying = forward

  override def reversed(): List[E] =
    underlying
}

private[util] object ReverseOrderListView {

  class ReverseOrderIterator[E](underlying: List[E]) extends Iterator[E] {
    val uli = underlying.listIterator(underlying.size())

    def hasNext(): Boolean =
      uli.hasPrevious()

    def next(): E =
      uli.previous()

    override def remove(): Unit =
      uli.remove()
  }

  class ReversedListIterator[E](underlying: List[E], underlyingIndex: Int)
      extends ListIterator[E] {

    val uli = underlying.listIterator(underlyingIndex)

    def add(e: E): Unit =
      uli.add(e)

    def hasNext(): Boolean =
      uli.hasPrevious()

    def hasPrevious(): Boolean =
      uli.hasNext()

    def next(): E =
      uli.previous()

    def nextIndex(): Int =
      uli.previousIndex()

    def previous(): E =
      uli.next()

    def previousIndex(): Int =
      uli.nextIndex()

    // Iterator interface default method always throws, we can do better.
    override def remove(): Unit =
      uli.remove()

    def set(e: E): Unit =
      uli.set(e)
  }
}

// ReverseOrderLinkedListView -------------------------------------------

private[util] class ReverseOrderLinkedListView[E](forward: LinkedList[E])
    extends LinkedList[E]
    with ReverseOrderDequeViewTrait[E]
    with ReverseOrderListViewTrait[E] {

  val underlying = forward

  override def reversed(): LinkedList[E] =
    forward

  override def size(): Int =
    underlying.size()

  override def descendingIterator(): java.util.Iterator[E] =
    underlying.iterator()
}

// ReverseOrderNavigableSetView --------------------------------------------

private[util] class ReverseOrderNavigableSetView[E](underlying: NavigableSet[E])
    extends ReverseOrderSortedSetView[E](underlying)
    with NavigableSet[E] {

  def ceiling(e: E): E =
    underlying.floor(e)

  def descendingIterator(): Iterator[E] =
    underlying.iterator()

  def descendingSet(): NavigableSet[E] =
    underlying

  def floor(e: E): E =
    underlying.ceiling(e)

  override def headSet(toElement: E): SortedSet[E] =
    new ReverseOrderSortedSetView(underlying.tailSet(toElement, false))

  def headSet(toElement: E, inclusive: Boolean): NavigableSet[E] =
    new ReverseOrderNavigableSetView(underlying.tailSet(toElement, inclusive))

  def higher(e: E): E =
    underlying.lower(e)

  override def iterator(): Iterator[E] =
    underlying.descendingIterator()

  def lower(e: E): E =
    underlying.higher(e)

  def pollFirst(): E =
    underlying.pollLast()

  def pollLast(): E =
    underlying.pollFirst()

  def subSet(
      fromElement: E,
      fromInclusive: Boolean,
      toElement: E,
      toInclusive: Boolean
  ): java.util.NavigableSet[E] = {

    /* compare arguments in Reversed domain, where comparator will never be
     * null. underlying.comparator() may be null if it defauls to NaturalOrder.
     */

    if (comparator().compare(fromElement, toElement) > 0)
      throw new IllegalArgumentException(s"fromKey > toKey")

    new ReverseOrderNavigableSetView(
      underlying.subSet(
        toElement,
        toInclusive,
        fromElement,
        fromInclusive
      )
    )
  }

  override def tailSet(fromElement: E): java.util.SortedSet[E] =
    new ReverseOrderSortedSetView(underlying.headSet(fromElement, true))

  def tailSet(fromElement: E, inclusive: Boolean): java.util.NavigableSet[E] =
    new ReverseOrderNavigableSetView(underlying.headSet(fromElement, inclusive))
}

// ReverseOrderSequencedSetView --------------------------------------------

private[util] class ReverseOrderSequencedSetView[E](underlying: SequencedSet[E])
    extends AbstractSet[E]
    with SequencedSet[E] {

  override def add(e: E): Boolean = {
    val sizeAtEntry = underlying.size()
    underlying.addFirst(e)
    underlying.size() > sizeAtEntry
  }

  override def addFirst(e: E): Unit =
    underlying.addLast(e)

  override def addLast(e: E): Unit =
    underlying.addFirst(e)

  override def getFirst(): E =
    underlying.getLast()

  override def getLast(): E =
    underlying.getFirst()

  def iterator(): java.util.Iterator[E] = {
    /* See comment about _hideously_ inefficient reversed iterator in
     * ReverseOrderSortedSetView.
     *
     * The same concerns apply here.
     */
    var remaining = underlying.size()

    new Iterator[E] {
      def hasNext(): Boolean =
        remaining > 0

      var uli = null.asInstanceOf[Iterator[E]]

      def next(): E = {
        if (remaining <= 0)
          throw new NoSuchElementException()

        uli = underlying.iterator()
        for (j <- 1 until remaining)
          uli.next()

        remaining -= 1
        uli.next()
      }

      override def remove(): Unit =
        uli.remove()
    }
  }

  override def removeFirst(): E =
    underlying.removeLast()

  override def removeLast(): E =
    underlying.removeFirst()

  override def reversed(): SequencedSet[E] =
    underlying

  def size(): Int =
    underlying.size()
}

// ReverseOrderSortedSetView --------------------------------------------

private[util] class ReverseOrderSortedSetView[E](underlying: SortedSet[E])
    extends AbstractSet[E]
    with SortedSet[E] {

  override def add(e: E): Boolean =
    underlying.add(e)

  def comparator(): java.util.Comparator[? >: E] = {
    val ulCmp = underlying.comparator()

    // explicit type required by at least Scala 2.12.20, Scala 3 does not need.
    val cmp: ju.Comparator[? >: E] =
      if (ulCmp != null) ulCmp
      else NaturalComparator

    cmp.reversed()
  }

  def first(): E =
    underlying.last()

  def headSet(toElement: E): java.util.SortedSet[E] =
    throw new UnsupportedOperationException("Not Yet Implemented")

  /* This algorithm is sometimes called a "road painter" algorithm
   * (https://www.joelonsoftware.com/2001/12/11/back-to-basics/).
   * 
   * It is not especially efficient; worst case, n-squared. It also does
   * a lot of object creations.
   * 
   * Given the methods available to SortedSet, its virtue is that it does not
   * retain, say double, the memory of the original set for long periods of
   * time, potentially even after use.
   * 
   * Improvements to this code are welcome.
   * 
   * Classes extending SortedSet(), such as NavigableSet, may be able to
   * provide a more pleasing implementation.
   */

  def iterator(): java.util.Iterator[E] = {
    var remaining = underlying.size()

    new Iterator[E] {
      def hasNext(): Boolean =
        remaining > 0

      var uli = null.asInstanceOf[Iterator[E]]

      def next(): E = {
        if (remaining <= 0)
          throw new NoSuchElementException()

        uli = underlying.iterator()

        for (j <- 1 until remaining)
          uli.next()

        remaining -= 1
        uli.next()
      }

      override def remove(): Unit =
        uli.remove()
    }
  }

  def last(): E =
    underlying.first()

  override def reversed(): SortedSet[E] =
    underlying.asInstanceOf[SortedSet[E]]

  def size(): Int =
    underlying.size()

  def subSet(fromElement: E, toElement: E): java.util.SortedSet[E] =
    throw new UnsupportedOperationException("Not Yet Implemented")

  def tailSet(fromElement: E): java.util.SortedSet[E] =
    throw new UnsupportedOperationException("Not Yet Implemented")
}

// ReverseOrderUnmodifiableSequencedCollectionView ----------------------

private[util] class ReverseOrderUnmodifiableSequencedCollectionView[E](
    underlying: SequencedCollection[E]
) extends AbstractCollection[E]
    with SequencedCollection[E] {

  override def iterator(): java.util.Iterator[E] =
    new Iterator[E] {
      // No 'override remove()' so this Iterator is immutable.

      var remaining = underlying.size()

      def hasNext(): Boolean =
        remaining > 0

      def next(): E = {
        val uli = underlying.iterator()

        if (remaining <= 0)
          throw new NoSuchElementException()

        for (j <- 1 until remaining)
          uli.next()

        remaining -= 1
        uli.next()
      }
    }

  override def reversed(): SequencedCollection[E] = {
    /* You know you are in deep cess when it takes more than a dozen lines
     * of comments to explain one line of code.
     *
     * Usually on JVM, reversing an already reversed instance will
     * return the 'forward' object provided to the original 'reversed()'.
     * The results are both reference (.eq) and content (.equals) equal.
     *
     * For unmodifiableSequencedCollection(), the JVM returns a value
     * which is neither reference nor content equal to the originally
     * supplied collection.  This is somewhat astonishing as it is
     * inconsistent, but there it is.
     *
     * Follow the JVM practice and return a different object, which
     * uses Objects.equals() to give the required, if unsettling, semantics.
     */

    Collections.unmodifiableSequencedCollection(underlying)
  }

  def size(): Int =
    underlying.size()
}

// ReverseOrderUnmodifiableSequencedSetView -----------------------------

private[util] class ReverseOrderUnmodifiableSequencedSetView[E](
    underlying: SequencedSet[E]
) extends AbstractSet[E]
    with SequencedSet[E] {

  private def painterIterator(): java.util.Iterator[E] =
    new Iterator[E] {
      // No 'override remove()' so this Iterator is immutable.

      var remaining = underlying.size()

      def hasNext(): Boolean =
        remaining > 0

      def next(): E = {
        val uli = underlying.iterator()

        if (remaining <= 0)
          throw new NoSuchElementException()

        for (j <- 1 until remaining)
          uli.next()

        remaining -= 1
        uli.next()
      }
    }

  def iterator(): java.util.Iterator[E] =
    underlying match {
      case ns: NavigableSet[E] =>
        new Iterator[E] {
          // No 'override remove()' so this Iterator is immutable.
          val uli = ns.descendingIterator()

          def hasNext(): Boolean =
            uli.hasNext()

          def next(): E =
            uli.next()
        }

      case _ => painterIterator()
    }

  override def reversed(): SequencedSet[E] =
    underlying

  def size(): Int =
    underlying.size()
}
