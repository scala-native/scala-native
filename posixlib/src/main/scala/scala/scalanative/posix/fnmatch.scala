package scala.scalanative
package posix

import scalanative.unsafe._

/** POSIX fnmatch.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 */

@extern
object fnmatch {
  // Symbolic constants

  @name("scalanative_fnm_nomatch")
  def FNM_NOMATCH: CInt = extern

  @name("scalanative_fnm_pathname")
  def FNM_PATHNAME: CInt = extern

  @name("scalanative_fnm_period")
  def FNM_PERIOD: CInt = extern

  @name("scalanative_fnm_noescape")
  def FNM_NOESCAPE: CInt = extern

  // Method

  def fnmatch(pattern: CString, string: CString, flags: CInt): CInt = extern
}
