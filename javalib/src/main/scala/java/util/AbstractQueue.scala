package java.util

abstract class AbstractQueue[E] protected ()
    extends AbstractCollection[E]
    with Queue[E] {

  override def add(e: E): Boolean =
    if (offer(e)) true
    else throw new IllegalStateException()

  def remove(): E = {
    val e = poll()
    if (e != null) e
    else throw new NoSuchElementException()
  }

  def element(): E = {
    val e = peek()
    if (e != null) e
    else throw new NoSuchElementException()
  }

  override def clear(): Unit = {
    while (poll() != null) {}
  }

  override def addAll(c: Collection[_ <: E]): Boolean = {
    if (c == null) throw new NullPointerException()
    if (c == this) throw new IllegalArgumentException()
    val iter = c.iterator()
    var changed = false
    while (iter.hasNext()) changed = add(iter.next()) || changed
    changed
  }
}
