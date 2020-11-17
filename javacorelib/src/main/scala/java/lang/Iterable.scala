// Influenced by and corresponds to Scala.js commit SHA: f9fc1ae

package java.lang

import java.util.Iterator
import java.util.function.Consumer

trait Iterable[T] {
  def iterator(): Iterator[T]

  def forEach(action: Consumer[_ >: T]): Unit = {
    val iter = iterator()
    while (iter.hasNext())
      action.accept(iter.next())
  }
}
