// Ported from Scala.js commit: f9fc1a dated: 2020-03-06
// default spliterator method added for Scala Native.

package java.lang

import java.util.Iterator
import java.util.function.Consumer
import java.util.{Spliterator, Spliterators}

trait Iterable[T] {
  def iterator(): Iterator[T]

  def forEach(action: Consumer[_ >: T]): Unit = {
    val iter = iterator()
    while (iter.hasNext())
      action.accept(iter.next())
  }

  /** From the Java 8 documentation: The default implementation should usually
   *  be overridden. The spliterator returned by the default implementation has
   *  poor splitting capabilities, is unsized, and does not report any
   *  spliterator characteristics. Implementing classes can nearly always
   *  provide a better implementation.
   */
  def spliterator(): Spliterator[T] = {
    Spliterators.spliteratorUnknownSize[T](this.iterator(), 0)
  }
}
