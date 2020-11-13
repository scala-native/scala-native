// Ported from Scala.js commit SHA1: 9dc4d5b dated: 2018-10-11

package java.util

import scala.scalanative.annotation.JavaDefaultMethod

trait Comparator[A] {
  def compare(o1: A, o2: A): Int
  def equals(obj: Any): Boolean

  @JavaDefaultMethod
  def reversed(): Comparator[A] =
    Collections.reverseOrder(this)
}
