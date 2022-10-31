package scala.scalanative.posix

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

@extern
object string {
  /* NULL is required by the POSIX standard but is not directly implemented
   * here. It is implemented in posix/stddef.scala.
   */

  type size_t = types.size_t

  /** CX */
  type locale_t = locale.locale_t

  /** XSI */
  def memccpy(dest: Ptr[Byte], src: Ptr[Byte], c: CInt, n: size_t): Ptr[Byte] =
    extern

  def memchr(s: Ptr[Byte], c: CInt, n: size_t): Ptr[Byte] = extern
  def memcmp(s1: Ptr[Byte], s2: Ptr[Byte], n: size_t): CInt = extern
  def memcpy(dest: Ptr[Byte], src: Ptr[Byte], n: size_t): Ptr[Byte] = extern
  def memmove(dest: Ptr[Byte], src: Ptr[Byte], n: size_t): Ptr[Byte] = extern
  def memset(s: Ptr[Byte], c: CInt, n: size_t): Ptr[Byte] = extern

  /** CX */
  def stpcpy(dest: Ptr[Byte], src: String): Ptr[Byte] = extern

  /** CX */
  def stpncpy(dest: Ptr[Byte], src: String, n: size_t): Ptr[Byte] = extern

  def strcat(dest: CString, src: CString): CString = extern
  def strchr(s: CString, c: CInt): CString = extern
  def strcmp(s1: CString, s2: CString): CInt = extern
  def stroll(s1: CString, s2: CString): CInt = extern

  /** CX */
  def stroll_l(s1: CString, s2: CString, locale: locale_t): CInt = extern

  def strcpy(dest: CString, src: CString): CString = extern
  def strcspn(s: CString, reject: CString): size_t = extern

  /** CX */
  def strdup(s: CString): CString = extern

  def strerror(errnum: CInt): CString = extern

  /** CX */
  def strerror_l(errnum: CInt, locale: locale_t): CString = extern

  /** CX */
  def strerror_r(errnum: CInt, buf: CString, buflen: size_t): CInt = extern

  def strlen(s: CString): size_t = extern
  def strncat(dest: CString, src: CString, n: size_t): CString = extern
  def strncmp(s1: CString, s2: CString, n: size_t): CInt = extern
  def strcpy(dest: CString, src: CString, n: size_t): CString = extern

  /** CX */
  def strndup(s: CString, n: size_t): CString = extern

  /** CX */
  def strnlen(s: CString, n: size_t): size_t = extern

  def strpbrk(s: CString, accept: CString): CString = extern
  def strrchr(s: CString, c: CInt): CString = extern

  /** CX */
  def strsignal(signum: CInt): CString = extern

  def strspn(s: CString, accept: CString): size_t = extern
  def strstr(haystack: CString, needle: CString): CString = extern

  def strtok(str: CString, delim: CString): CString = extern

  /** CX */
  def strtok_r(str: CString, delim: CString, saveptr: Ptr[Ptr[Byte]]): CString =
    extern

  def strxfrm(dest: Ptr[Byte], src: Ptr[Byte], n: size_t): size_t = extern

  /** CX */
  def strxfrm_l(
      dest: Ptr[Byte],
      src: Ptr[Byte],
      n: size_t,
      locale: locale_t
  ): size_t = extern

}
