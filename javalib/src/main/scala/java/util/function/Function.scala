// Ported from Scala.js, commit SHA: 7b4e8a80b dated: 2022-12-06
package java.util.function

@FunctionalInterface
trait Function[T, R] {
  def apply(t: T): R

  def andThen[V](after: Function[? >: R, ? <: V]): Function[T, V] = { (t: T) =>
    after.apply(apply(t))
  }

  def compose[V](before: Function[? >: V, ? <: T]): Function[V, R] = { (v: V) =>
    apply(before.apply(v))
  }
}

object Function {
  def identity[T](): Function[T, T] = (t: T) => t
}
