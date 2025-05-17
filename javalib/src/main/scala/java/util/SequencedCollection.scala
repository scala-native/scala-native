package java.util

trait SequencedCollection[E /* <: AnyRef */ ] extends Collection[E] {

  def reversed(): SequencedCollection[E]

  def addFirst(elem: E): Unit = throw new UnsupportedOperationException()
  def addLast(elem: E): Unit = throw new UnsupportedOperationException()

  def getFirst(): E = {
    val it = this.iterator()

    if (!it.hasNext())
      throw new NoSuchElementException()

    it.next()
  }

  def getLast(): E = {
    val it = this.reversed().iterator()

    if (!it.hasNext())
      throw new NoSuchElementException()

    it.next()
  }

  def removeFirst(): E = {
    val it = this.iterator()

    if (!it.hasNext())
      throw new NoSuchElementException()

    val elem = it.next()
    it.remove()
    elem
  }

  def removeLast(): E = {
    val it = this.reversed().iterator()

    if (!it.hasNext())
      throw new NoSuchElementException()

    val elem = it.next()
    it.remove()
    elem
  }
}
