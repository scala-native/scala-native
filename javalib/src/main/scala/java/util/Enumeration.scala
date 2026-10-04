package java.util

trait Enumeration[E] {
  def hasMoreElements(): Boolean
  def nextElement(): E

  def asIterator(): Iterator[E] = new Iterator[E] {
    def hasNext(): Boolean = hasMoreElements()

    def next(): E = nextElement()
  }
}
