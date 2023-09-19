package java.util

trait SequencedCollection[E /* <: AnyRef */ ] extends Collection[E] {
/* Commented out until we're able to provide reversed views for collections
  def reversed(): SequencedCollection[E]

  def addFirst(elem: E): Unit = throw new UnsupportedOperationException()
  def addLast(elem: E): Unit = throw new UnsupportedOperationException()

  def getFirst(): E = this.iterator().next()
  def getLast(): E = this.reversed().iterator().next()

  def removeFirst(): E = {
    val it = this.iterator()
    val elem = it.next()
    it.remove()
    elem
  }

  def removeLast(): E = {
    val it = this.reversed().iterator()
    val elem = it.next()
    it.remove()
    elem
  }
  */
}
