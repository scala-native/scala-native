package scala.scalanative.posix

import scala.scalanative.unsafe.*
import scala.scalanative.posix.sys.types

/** POSIX strings.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 *
 *  A method with an XSI comment indicates it is defined in extended POSIX
 *  X/Open System Interfaces, not base POSIX.
 */

@extern
object strings {

  type size_t = types.size_t
  type locale_t = locale.locale_t

  /** XSI */
  def ffs(i: CInt): CInt = extern

  def strcasecmp(s1: CString, s2: CString): CInt = extern
  def strcasecmp_l(s1: CString, s2: CString, locale: locale_t): CInt = extern
  def strncasecmp(s1: CString, s2: CString, n: size_t): CInt = extern
  def strncasecmp_l(
      s1: CString,
      s2: CString,
      n: size_t,
      locale: locale_t
  ): CInt = extern

}
