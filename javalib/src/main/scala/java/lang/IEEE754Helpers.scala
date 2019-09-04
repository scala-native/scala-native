package java.lang

import scalanative.unsafe._
import scalanative.libc.errno

import scala.annotation.{switch, tailrec}
import scalanative.posix.errno.ERANGE

private [java] object  IEEE754Helpers {

  // Java parseDouble() and parseFloat() allow characters at & after
  // after where C strtod() or strtof() stops. Continuous trailing whitespace
  // with or without an initial 'D', 'd', 'F', 'f' size indicator
  // is allowed.
  // The whitespace can include tabs ('\t') and even newlines! ('\n').
  // Perhaps even Unicode....
  //
  // The Java trim method is used to ensure that Java rules are followed.
  // It might not be the most memory & runtime efficient, but if execution
  // is in this routine, it is already on a slow path.

  @tailrec
  def vetIEEE754Tail(tail: String): Boolean = {

    if (tail.length <= 0) {
      true
    } else {
      val rest = tail.tail.trim
      (tail(0): @scala.annotation.switch) match {
        case 'D' | 'd' => vetIEEE754Tail(rest)
        case 'F' | 'f' => vetIEEE754Tail(rest)
        case _         => (tail.trim.length == 0)
      }
    }
  }


  def parseIEEE754[T](s: String, f: (CString, Ptr[CString]) => T): T = {
    Zone { implicit z =>
      val cstr = toCString(s)
      val end  = stackalloc[CString]

      errno.errno = 0
      var res = f(cstr, end)

      def exceptionMsg = s"For input string \042${s}\042"

      if (errno.errno != 0) {
        if (errno.errno != ERANGE) {
        // The need to use \042 for double quote seems to be a Scala 2.11 bug.
        // Uglier workarounds exist.
          throw new NumberFormatException(exceptionMsg)
        }
        // Else strtod() or strtof() will have returned the proper type for
        // 0.0 (too close to zero) or +/- infinity. Slick C lib design!
      } else if (!end == cstr) {
          throw new NumberFormatException(exceptionMsg)
      } else if ((!end(0) == 0) ||
          vetIEEE754Tail(s.slice((!end - cstr).toInt, s.length))) {
         res
      } else {
          throw new NumberFormatException(exceptionMsg)
      }

      res
    }
  }

}
