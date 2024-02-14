package scala.scalanative
package posix

import scala.scalanative.unsafe._

/** POSIX locale.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 *
 *  All declarations which do not have a more specific extension specifier are
 *  described by POSIX as being a CX extension.
 */

@extern object locale extends libc.locale {

  type locale_t = Ptr[_] // CX, so can get no simpler.

// Symbolic constants

  /** CX */
  @name("scalanative_lc_global_locale")
  def LC_GLOBAL_LOCALE: locale_t = extern

  /** CX */
  @name("scalanative_lc_messages")
  def LC_MESSAGES: CInt = extern

  @name("scalanative_lc_all_mask")
  def LC_ALL_MASK: CInt = extern

  @name("scalanative_lc_collate_mask")
  def LC_COLLATE_MASK: CInt = extern

  @name("scalanative_lc_ctype_mask")
  def LC_CTYPE_MASK: CInt = extern

  @name("scalanative_lc_monetary_mask")
  def LC_MONETARY_MASK: CInt = extern

  @name("scalanative_lc_messages_mask")
  def LC_MESSAGES_MASK: CInt = extern

  @name("scalanative_lc_numeric_mask")
  def LC_NUMERIC_MASK: CInt = extern

  @name("scalanative_lc_time_mask")
  def LC_TIME_MASK: CInt = extern

// Methods

  def duplocale(locobj: locale_t): locale_t = extern

  def freelocale(locobj: locale_t): CInt = extern

  def newlocale(categoryMask: CInt, locale: CString, base: locale_t): locale_t =
    extern

  def uselocale(newloc: locale_t): locale_t = extern
}

object localeOps {
  import locale.lconv
  import scalanative.libc.localeOpsImpl

  implicit class lconvOps(val ptr: Ptr[lconv]) extends AnyVal {
    def decimal_point: CString = localeOpsImpl.decimal_point(ptr)
    def thousands_sep: CString = localeOpsImpl.thousands_sep(ptr)
    def grouping: CString = localeOpsImpl.grouping(ptr)
    def int_curr_symbol: CString = localeOpsImpl.int_curr_symbol(ptr)
    def currency_symbol: CString = localeOpsImpl.currency_symbol(ptr)

    def mon_decimal_point: CString = localeOpsImpl.mon_decimal_point(ptr)
    def mon_thousands_sep: CString = localeOpsImpl.mon_thousands_sep(ptr)
    def mon_grouping: CString = localeOpsImpl.mon_grouping(ptr)
    def positive_sign: CString = localeOpsImpl.positive_sign(ptr)
    def negative_sign: CString = localeOpsImpl.negative_sign(ptr)

    def int_frac_digits: CChar = localeOpsImpl.int_frac_digits(ptr)
    def frac_digits: CChar = localeOpsImpl.frac_digits(ptr)
    def p_cs_precedes: CChar = localeOpsImpl.p_cs_precedes(ptr)
    def p_sep_by_space: CChar = localeOpsImpl.p_sep_by_space(ptr)
    def n_cs_precedes: CChar = localeOpsImpl.n_cs_precedes(ptr)

    def n_sep_by_space: CChar = localeOpsImpl.n_sep_by_space(ptr)
    def p_sign_posn: CChar = localeOpsImpl.p_sign_posn(ptr)
    def n_sign_posn: CChar = localeOpsImpl.n_sign_posn(ptr)
    def int_p_cs_precedes: CChar = localeOpsImpl.int_p_cs_precedes(ptr)
    def int_p_sep_by_space: CChar = localeOpsImpl.int_p_sep_by_space(ptr)

    def int_n_cs_precedes: CChar = localeOpsImpl.int_n_cs_precedes(ptr)
    def int_n_sep_by_space: CChar = localeOpsImpl.int_n_sep_by_space(ptr)
    def int_p_sign_posn: CChar = localeOpsImpl.int_p_sign_posn(ptr)
    def int_n_sign_posn: CChar = localeOpsImpl.int_n_sign_posn(ptr)

    /* Linux 'man localeconv' documents lconv not to be modified,
     * so no corresponding 'set' Ops.
     */
  }
}
