package scala.scalanative.posix

import scala.scalanative.unsafe._
import scala.scalanative.posix.sys.types

// The Open Group Base Specifications Issue 7, 2018 edition

/* An XSI comment before method indicates it is defined in
 * extended POSIX X/Open System Interfaces, not base POSIX.
 */

@extern
object strings {

  type size_t = types.size_t
  type locale_t = locale.locale_t

// XSI - Begin
  def ffs(i: CInt): CInt = extern
// XSI - End

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
