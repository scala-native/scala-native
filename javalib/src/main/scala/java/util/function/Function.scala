// Ported from Scala.js commit: eb637e3 dated: 2020-09-06

package java.util.function

import scala.scalanative.annotation.JavaDefaultMethod

trait Function[T, R] {
  def apply(t: T): R

  @JavaDefaultMethod
  def andThen[V](after: Function[_ >: R, _ <: V]): Function[T, V] = { (t: T) =>
    after.apply(apply(t))
  }

  @JavaDefaultMethod
  def compose[V](before: Function[_ >: V, _ <: T]): Function[V, R] = { (v: V) =>
    apply(before.apply(v))
  }
}

object Function {
  def identity[T](): Function[T, T] = (t: T) => t
}
