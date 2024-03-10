package scala.scalanative
package libc

import scalanative.unsafe._
import scalanative.meta.LinktimeInfo.{isLinux, isOpenBSD}

/** ISO/IEC C definitions for locale.h
 *
 *  See https://en.cppreference.com/w/c/numeric/locale
 */
@extern object locale extends locale

/** Definitions shared with POSIX */
@extern private[scalanative] trait locale {

  // CStruct is limited to 22 fields, lconv wants 24, so group int_* & use Ops

  /* Be careful here!
   * This is the Linux layout. localeOps handles the fact that macOS
   * swaps/echanges the  int_p_sep_by_space & int_n_cs_precedes fields.
   */

  type lconv = CStruct19[
    CString, // decimal_point
    CString, // thousands_sep
    CString, // grouping
    CString, // int_curr_symbol
    CString, // currency_symbol

    CString, // mon_decimal_point
    CString, // mon_thousands_sep
    CString, // mon_grouping
    CString, // positive_sign
    CString, // negative_sign

    Byte, // int_frac_digits
    Byte, // frac_digits
    Byte, // p_cs_precedes
    Byte, // p_sep_by_space
    Byte, // n_cs_precedes

    Byte, // n_sep_by_space
    Byte, // p_sign_posn
    Byte, // n_sign_posn

    CStruct6[
      Byte, // int_p_cs_precedes
      Byte, // Linux int_p_sep_by_space, macOS int_n_cs_precedes
      Byte, // Linux int_n_cs_precedes,  macOS int_p_sep_by_space
      Byte, // int_n_sep_by_space
      Byte, // int_p_sign_posn
      Byte // int_n_sign_posn
    ]
  ]

  // Macros

  @name("scalanative_lc_all")
  def LC_ALL: CInt = extern

  @name("scalanative_lc_collate")
  def LC_COLLATE: CInt = extern

  @name("scalanative_lc_ctype")
  def LC_CTYPE: CInt = extern

  @name("scalanative_lc_monetary")
  def LC_MONETARY: CInt = extern

  @name("scalanative_lc_numeric")
  def LC_NUMERIC: CInt = extern

  @name("scalanative_lc_time")
  def LC_TIME: CInt = extern

// Methods

  def localeconv(): Ptr[lconv] = extern

  def setlocale(category: CInt, locale: CString): CString = extern
}

object localeOpsImpl {
  import locale.lconv
  def decimal_point(ptr: Ptr[lconv]): CString = ptr._1
  def thousands_sep(ptr: Ptr[lconv]): CString = ptr._2
  def grouping(ptr: Ptr[lconv]): CString = ptr._3
  def int_curr_symbol(ptr: Ptr[lconv]): CString = ptr._4
  def currency_symbol(ptr: Ptr[lconv]): CString = ptr._5

  def mon_decimal_point(ptr: Ptr[lconv]): CString = ptr._6
  def mon_thousands_sep(ptr: Ptr[lconv]): CString = ptr._7
  def mon_grouping(ptr: Ptr[lconv]): CString = ptr._8
  def positive_sign(ptr: Ptr[lconv]): CString = ptr._9
  def negative_sign(ptr: Ptr[lconv]): CString = ptr._10

  def int_frac_digits(ptr: Ptr[lconv]): CChar = ptr._11
  def frac_digits(ptr: Ptr[lconv]): CChar = ptr._12
  def p_cs_precedes(ptr: Ptr[lconv]): CChar = ptr._13
  def p_sep_by_space(ptr: Ptr[lconv]): CChar = ptr._14
  def n_cs_precedes(ptr: Ptr[lconv]): CChar = ptr._15

  def n_sep_by_space(ptr: Ptr[lconv]): CChar = ptr._16
  def p_sign_posn(ptr: Ptr[lconv]): CChar = ptr._17
  def n_sign_posn(ptr: Ptr[lconv]): CChar = ptr._18
  def int_p_cs_precedes(ptr: Ptr[lconv]): CChar = ptr._19._1
  def int_p_sep_by_space(ptr: Ptr[lconv]): CChar =
    if (isLinux || isOpenBSD) ptr._19._2
    else ptr._19._3 // macOS & probably BSDs

  def int_n_cs_precedes(ptr: Ptr[lconv]): CChar =
    if (isLinux || isOpenBSD) ptr._19._3
    else ptr._19._2 // macOS & probably BSDs

  def int_n_sep_by_space(ptr: Ptr[lconv]): CChar = ptr._19._4
  def int_p_sign_posn(ptr: Ptr[lconv]): CChar = ptr._19._5
  def int_n_sign_posn(ptr: Ptr[lconv]): CChar = ptr._19._6

  /* Linux 'man localeconv' documents lconv not to be modified,
   * so no corresponding 'set' Ops.
   */
}

object localeOps {
  import locale.lconv

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
    def int_n_cs_precedes: CChar = localeOpsImpl.int_n_cs_precedes(ptr)
    def int_p_sep_by_space: CChar = localeOpsImpl.int_p_sep_by_space(ptr)
    def int_n_sep_by_space: CChar = localeOpsImpl.int_n_sep_by_space(ptr)
    def int_p_sign_posn: CChar = localeOpsImpl.int_p_sign_posn(ptr)
    def int_n_sign_posn: CChar = localeOpsImpl.int_n_sign_posn(ptr)

    /* Linux 'man localeconv' documents lconv not to be modified,
     * so no corresponding 'set' Ops.
     */
  }
}
