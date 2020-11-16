// Ported from Scala.js commit: f9fc1a dated: 2020-03-06

package java.lang

import java.util.Iterator
import java.util.function.Consumer

import scala.scalanative.annotation.JavaDefaultMethod

trait Iterable[T] {
  def iterator(): Iterator[T]

  @JavaDefaultMethod
  def forEach(action: Consumer[_ >: T]): Unit = {
    val iter = iterator()
    while (iter.hasNext())
      action.accept(iter.next())
  }
}
