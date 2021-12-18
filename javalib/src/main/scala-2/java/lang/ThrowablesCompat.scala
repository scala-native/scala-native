// Classes in this file need special handling in Scala 3, we need to make sure
// that they would not be compiled with Scala 3 compiler

package java.lang

class NullPointerException(s: String) extends RuntimeException(s) {
  def this() = this(null)
}
