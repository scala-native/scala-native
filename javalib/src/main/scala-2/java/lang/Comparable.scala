// Classes in this file need special handling in Scala 3, we need to make sure
// that they would not be compiled with Scala 3 compiler

package java.lang

trait Comparable[A] {
  def compareTo(o: A): scala.Int
}
