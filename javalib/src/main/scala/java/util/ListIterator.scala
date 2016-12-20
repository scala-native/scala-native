package java.util

trait ListIterator[E] extends Iterator[E] {
  def add(e: E): Unit
  def hasPrevious(): Boolean
  def previous(): E
  def previousIndex(): Int
  def nextIndex(): Int
  def set(e: E): Unit

  // Workaround for #375
  def next(): E
  def hasNext(): Boolean
}
