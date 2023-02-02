// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package java.util.function

@FunctionalInterface
trait ToIntBiFunction[T, U] {
  def applyAsInt(t: T, u: U): Int
}
