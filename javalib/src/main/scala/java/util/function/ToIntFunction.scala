// Ported from Scala.js commit 00e462d dated: 2023-01-22

package java.util.function

@FunctionalInterface
trait ToIntFunction[T] {
  def applyAsInt(t: T): Int
}
