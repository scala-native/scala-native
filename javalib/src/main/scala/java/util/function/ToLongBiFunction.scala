// Ported from Scala.js, commit sha:cfb4888a6 dated:2021-01-07
package java.util.function

@FunctionalInterface
trait ToLongBiFunction[T, U] {
  def applyAsLong(t: T, u: U): Long
}