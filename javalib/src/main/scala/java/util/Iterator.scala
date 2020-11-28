// Influenced by and corresponds to Scala.js commit SHA: f86ed65

package java.util

import scala.scalanative.annotation.JavaDefaultMethod

import java.util.function.Consumer

trait Iterator[E] {
  def hasNext(): Boolean
  def next(): E

  @JavaDefaultMethod
  def remove(): Unit =
    throw new UnsupportedOperationException("remove")

  @JavaDefaultMethod
  def forEachRemaining(action: Consumer[_ >: E]): Unit = {
    while (hasNext())
      action.accept(next())
  }
}
