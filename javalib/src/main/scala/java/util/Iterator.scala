// Influenced by and corresponds to Scala.js commit SHA: f86ed65

package java.util

import java.util.function.Consumer

trait Iterator[E] {
  def hasNext(): Boolean
  def next(): E

  def remove(): Unit =
    throw new UnsupportedOperationException("remove")

  def forEachRemaining(action: Consumer[? >: E]): Unit = {
    while (hasNext())
      action.accept(next())
  }
}
