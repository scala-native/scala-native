package java.util

trait SortedSet[E] extends Set[E] with SequencedSet[E] {

  override def addFirst(e: E): Unit =
    throw new UnsupportedOperationException()

  override def addLast(e: E): Unit =
    throw new UnsupportedOperationException()

  def comparator(): Comparator[_ >: E]

  def first(): E

  override def getFirst(): E =
    first()

  override def getLast(): E =
    last()

  def headSet(toElement: E): SortedSet[E]

  def last(): E

  override def removeFirst(): E = {
    val e = first()
    remove(e)
    e
  }

  override def removeLast(): E = {
    val e = last()
    remove(e)
    e
  }

  override def reversed(): SortedSet[E] =
    new ReverseOrderSortedSetView[E](this)

  override def spliterator(): Spliterator[E] =
    Spliterators.spliterator(
      this,
      Spliterator.SIZED |
        Spliterator.SUBSIZED |
        Spliterator.DISTINCT
    )

  def subSet(fromElement: E, toElement: E): SortedSet[E]

  def tailSet(fromElement: E): SortedSet[E]
}
