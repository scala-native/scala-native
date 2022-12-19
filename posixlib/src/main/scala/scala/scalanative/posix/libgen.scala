package scala.scalanative
package posix

import scalanative.unsafe._

/** POSIX libgen.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 *
 *  A method with an XSI comment indicates it is defined in extended POSIX
 *  X/Open System Interfaces, not base POSIX.
 */

@extern
object libgen {

  /** XSI */
  def basename(path: CString): CString = extern

  /** XSI */
  def dirname(path: CString): CString = extern
}
