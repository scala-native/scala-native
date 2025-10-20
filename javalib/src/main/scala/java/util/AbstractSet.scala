package java.util

import scala.annotation.tailrec
import ScalaOps.*

abstract class AbstractSet[E] protected ()
    extends AbstractCollection[E]
    with Set[E] {
  override def equals(that: Any): Boolean = {
    if (that.asInstanceOf[AnyRef] eq this) true
    else {
      that match {
        case that: Set[?] =>
          that.size() == this.size() && containsAll(that)
        case _ => false
      }
    }
  }

  override def hashCode(): Int =
    iterator().scalaOps.foldLeft(0)((prev, item) => item.hashCode + prev)

  override def removeAll(c: Collection[?]): Boolean = {
    if (size() > c.size())
      c.scalaOps.foldLeft(false)((prev, elem) => this.remove(elem) || prev)
    else {
      @tailrec
      def removeAll(iter: Iterator[E], modified: Boolean): Boolean = {
        if (iter.hasNext()) {
          if (c.contains(iter.next())) {
            iter.remove()
            removeAll(iter, true)
          } else
            removeAll(iter, modified)
        } else
          modified
      }
      removeAll(this.iterator(), false)
    }
  }
}
