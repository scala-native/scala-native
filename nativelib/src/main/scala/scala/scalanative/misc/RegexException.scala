package scala.scalanative.misc
package regex

class RegexException(s: String, e: Throwable)
    extends RuntimeException(s, e) {
  def this(e: Throwable) = this(if (e == null) null else e.toString, e)
  def this(s: String) = this(s, null)
  def this() = this(null, null)
}