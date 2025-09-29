package scala.scalanative
package posix

import scalanative.posix.sys.types.{size_t, ssize_t}
import scalanative.unsafe._

/** POSIX monetary.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 */

@extern
object monetary {
  type locale_t = locale.locale_t

  def strfmon(
      str: CString,
      max: size_t,
      format: CString,
      vargs: Double*
  ): ssize_t = extern

  def strfmon_l(
      str: CString,
      max: size_t,
      locale: locale_t,
      format: CString,
      vargs: Double*
  ): ssize_t = extern
}
