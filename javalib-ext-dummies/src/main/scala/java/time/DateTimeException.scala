// Ported from Scala.js, commit: 54648372, dated: 2020-09-24
package java.time

class DateTimeException(message: String, cause: Throwable)
    extends RuntimeException(message, cause) {

  def this(message: String) = this(message, null)
}
