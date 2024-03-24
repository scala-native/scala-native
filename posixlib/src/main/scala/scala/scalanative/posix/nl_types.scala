package scala.scalanative
package posix

import scala.scalanative.unsafe._

/** POSIX nl_types.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 */

@extern
@define("__SCALANATIVE_POSIX_NL_TYPES")
object nl_types {

  type nl_catd = CVoidPtr // Scala Native idiom for void *.

  type nl_item = CInt

// Symbolic constants

  @name("scalanative_nl_setd")
  def NL_SETD: CInt = extern

  @name("scalanative_nl_cat_locale")
  def NL_CAT_LOCALE: CInt = extern

// Methods

  def catclose(catalog: nl_catd): CInt = extern

  def catgets(
      catalog: nl_catd,
      setNumber: CInt,
      messageNumber: CInt,
      message: CString
  ): CString = extern

  def catopen(name: CString, flag: CInt): nl_catd = extern
}
