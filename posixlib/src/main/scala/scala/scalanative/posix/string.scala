package scala.scalanative
package posix

import scala.scalanative.unsafe._
import scala.scalanative.posix.sys.types

/** POSIX string.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 *
 *  A method with a CX comment indicates it is a POSIX extension to the ISO/IEEE
 *  C standard.
 *
 *  A method with an XSI comment indicates it is defined in extended POSIX
 *  X/Open System Interfaces, not base POSIX.
 */
@extern object string extends string

@extern trait string extends libc.string {
  /* NULL is required by the POSIX standard but is not directly implemented
   * here. It is implemented in posix/stddef.scala.
   */

  type size_t = types.size_t

  /** CX */
  type locale_t = locale.locale_t

  /** XSI */
  def memccpy(dest: CVoidPtr, src: CVoidPtr, c: CInt, n: size_t): CVoidPtr =
    extern

  /** CX */
  def stpcpy(dest: CString, src: CString): CVoidPtr = extern

  /** CX */
  def stpncpy(dest: CString, src: CString, n: size_t): CVoidPtr = extern

  def stroll(s1: CString, s2: CString): CInt = extern

  /** CX */
  def stroll_l(s1: CString, s2: CString, locale: locale_t): CInt = extern

  /** CX */
  def strdup(s: CString): CString = extern

  /** CX */
  def strerror_l(errnum: CInt, locale: locale_t): CString = extern

  /** CX */
  def strerror_r(errnum: CInt, buf: CString, buflen: size_t): CInt = extern

  def strcpy(dest: CString, src: CString, n: size_t): CString = extern

  /** CX */
  def strndup(s: CString, n: size_t): CString = extern

  /** CX */
  def strnlen(s: CString, n: size_t): size_t = extern

  /** CX */
  def strsignal(signum: CInt): CString = extern

  /** CX */
  def strtok_r(str: CString, delim: CString, saveptr: Ptr[CString]): CString =
    extern

  /** CX */
  def strxfrm_l(
      dest: CString,
      src: CString,
      n: size_t,
      locale: locale_t
  ): size_t = extern

}
