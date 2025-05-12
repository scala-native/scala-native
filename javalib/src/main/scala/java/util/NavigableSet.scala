package java.util

trait NavigableSet[E] extends SortedSet[E] {

  def ceiling(e: E): E

  def descendingIterator(): Iterator[E]

  def descendingSet(): NavigableSet[E]

  def floor(e: E): E

  def headSet(toElement: E): SortedSet[E]

  def headSet(toElement: E, inclusive: Boolean): NavigableSet[E]

  def higher(e: E): E

  def iterator(): Iterator[E]

  def lower(e: E): E

  def pollFirst(): E

  def pollLast(): E

  override def removeFirst(): E = {
    if (isEmpty())
      throw new NoSuchElementException

    pollFirst()
  }

  override def removeLast(): E = {
    if (isEmpty())
      throw new NoSuchElementException

    pollLast()
  }

  // Not the obvious underlying.descendingSet(); See Scala Native Issue #4321.
  override def reversed(): NavigableSet[E] =
    new ReverseOrderNavigableSetView[E](this)

  def subSet(fromElement: E, toElement: E): SortedSet[E]

  def subSet(
      fromElement: E,
      fromInclusive: Boolean,
      toElement: E,
      toInclusive: Boolean
  ): NavigableSet[E]

  def tailSet(fromElement: E): SortedSet[E]

  def tailSet(fromElement: E, inclusive: Boolean): NavigableSet[E]

}
