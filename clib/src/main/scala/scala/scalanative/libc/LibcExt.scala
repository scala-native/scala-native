package scala.scalanative.libc

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.unsafe._

object LibcExt {

  /** Obtain an error message for a given error code.
   *  @param errnum
   *    error code
   *  @return
   *    error message
   */
  @alwaysinline
  def strError(errnum: CInt): String = fromCString(string.strerror(errnum))

  /** Obtain an error message for the current [[errno.errno errno]] code.
   *  @return
   *    error message
   */
  @alwaysinline
  def strError(): String = strError(errno.errno)

}
