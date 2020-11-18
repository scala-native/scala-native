// Influenced Scala.js commit: eb637e3 dated: 2020-09-06
//
// Design Note: Once Scala Native no longer supports Scala 2.11,
//              OK to use Scala.js code with lambdas.

package java.util.function

import scala.scalanative.annotation.JavaDefaultMethod

trait Function[T, R] { self =>
  def apply(t: T): R

  @JavaDefaultMethod
  def andThen[V](after: Function[_ >: R, _ <: V]): Function[T, V] =
    new Function[T, V] {
      override def apply(t: T): V = after.apply(self.apply(t))
    }

  @JavaDefaultMethod
  def compose[V](before: Function[_ >: V, _ <: T]): Function[V, R] =
    new Function[V, R] {
      override def apply(v: V): R = self.apply(before.apply(v))
    }
}

object Function {
  def identity[T](): Function[T, T] =
    new Function[T, T] {
      override def apply(t: T): T = t
    }
}
