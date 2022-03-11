package scala.scalanative.libc

import scala.scalanative.unsafe.extern
import scala.scalanative.unsafe.name
import scala.scalanative.unsafe.{
  CArray,
  CInt,
  CString,
  CChar,
  Ptr,
  toCString,
  alloc,
  Zone,
  Nat
}

@extern
object locale {
  type LConv = CArray[Byte, Nat.Digit2[Nat._9, Nat._5]]
  def setlocale(category: CInt, locale: CString): CString = extern
  def localeconv(): Ptr[LConv] = extern

  // categories
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
}

object LConv {
  import locale.LConv

  def apply()(implicit z: Zone): Ptr[LConv] = alloc[LConv]()
  def apply(
      decimal_point: CString,
      thousands_sep: CString,
      grouping: CString,
      int_curr_symbol: CString,
      currency_symbol: CString,
      mon_decimal_point: CString,
      mon_thousands_sep: CString,
      mon_grouping: CString,
      positive_sign: CString,
      negative_sign: CString,
      int_frac_digits: CChar,
      frac_digits: CChar,
      p_cs_precedes: CChar,
      p_sep_by_space: CChar,
      n_cs_precedes: CChar,
      n_sep_by_space: CChar,
      p_sign_posn: CChar,
      n_sign_posn: CChar,
      int_p_cs_precedes: CChar,
      int_p_sep_by_space: CChar,
      int_n_cs_precedes: CChar,
      int_n_sep_by_space: CChar,
      int_p_sign_posn: CChar,
      int_n_sign_posn: CChar
  )(implicit z: Zone): Ptr[LConv] = {
    val ptr = alloc[LConv]()
    ptr.decimal_point = decimal_point
    ptr.thousands_sep = thousands_sep
    ptr.grouping = grouping
    ptr.mon_decimal_point = mon_decimal_point
    ptr.mon_thousands_sep = mon_thousands_sep
    ptr.mon_grouping = mon_grouping
    ptr.positive_sign = positive_sign
    ptr.negative_sign = negative_sign
    ptr.currency_symbol = currency_symbol
    ptr.frac_digits = frac_digits
    ptr.p_cs_precedes = p_cs_precedes
    ptr.n_cs_precedes = n_cs_precedes
    ptr.p_sep_by_space = p_sep_by_space
    ptr.n_sep_by_space = n_sep_by_space
    ptr.p_sign_posn = p_sign_posn
    ptr.n_sign_posn = n_sign_posn
    ptr.int_curr_symbol = int_curr_symbol
    ptr.int_frac_digits = int_frac_digits
    ptr.int_p_cs_precedes = int_p_cs_precedes
    ptr.int_n_cs_precedes = int_n_cs_precedes
    ptr.int_p_sep_by_space = int_p_sep_by_space
    ptr.int_n_sep_by_space = int_n_sep_by_space
    ptr.int_p_sign_posn = int_p_sign_posn
    ptr.int_n_sign_posn = int_n_sign_posn
    ptr
  }

  implicit class LConvOps(val p: Ptr[LConv]) extends AnyVal {
    def decimal_point: CString = !p.at(0).asInstanceOf[Ptr[CString]]
    def decimal_point_=(value: CString): Unit =
      !p.at(0).asInstanceOf[Ptr[CString]] = value
    def thousands_sep: CString = !p.at(8).asInstanceOf[Ptr[CString]]
    def thousands_sep_=(value: CString): Unit =
      !p.at(8).asInstanceOf[Ptr[CString]] = value
    def grouping: CString = !p.at(16).asInstanceOf[Ptr[CString]]
    def grouping_=(value: CString): Unit =
      !p.at(16).asInstanceOf[Ptr[CString]] = value
    def int_curr_symbol: CString =
      !p.at(24).asInstanceOf[Ptr[CString]]
    def int_curr_symbol_=(value: CString): Unit =
      !p.at(24).asInstanceOf[Ptr[CString]] = value
    def currency_symbol: CString =
      !p.at(32).asInstanceOf[Ptr[CString]]
    def currency_symbol_=(value: CString): Unit =
      !p.at(32).asInstanceOf[Ptr[CString]] = value
    def mon_decimal_point: CString =
      !p.at(40).asInstanceOf[Ptr[CString]]
    def mon_decimal_point_=(value: CString): Unit =
      !p.at(40).asInstanceOf[Ptr[CString]] = value
    def mon_thousands_sep: CString =
      !p.at(48).asInstanceOf[Ptr[CString]]
    def mon_thousands_sep_=(value: CString): Unit =
      !p.at(48).asInstanceOf[Ptr[CString]] = value
    def mon_grouping: CString = !p.at(56).asInstanceOf[Ptr[CString]]
    def mon_grouping_=(value: CString): Unit =
      !p.at(56).asInstanceOf[Ptr[CString]] = value
    def positive_sign: CString = !p.at(64).asInstanceOf[Ptr[CString]]
    def positive_sign_=(value: CString): Unit =
      !p.at(64).asInstanceOf[Ptr[CString]] = value
    def negative_sign: CString = !p.at(72).asInstanceOf[Ptr[CString]]
    def negative_sign_=(value: CString): Unit =
      !p.at(72).asInstanceOf[Ptr[CString]] = value
    def int_frac_digits: CChar = !p.at(80).asInstanceOf[Ptr[CChar]]
    def int_frac_digits_=(value: CChar): Unit =
      !p.at(80).asInstanceOf[Ptr[CChar]] = value
    def frac_digits: CChar = !p.at(81).asInstanceOf[Ptr[CChar]]
    def frac_digits_=(value: CChar): Unit =
      !p.at(81).asInstanceOf[Ptr[CChar]] = value
    def p_cs_precedes: CChar = !p.at(82).asInstanceOf[Ptr[CChar]]
    def p_cs_precedes_=(value: CChar): Unit =
      !p.at(82).asInstanceOf[Ptr[CChar]] = value
    def p_sep_by_space: CChar = !p.at(83).asInstanceOf[Ptr[CChar]]
    def p_sep_by_space_=(value: CChar): Unit =
      !p.at(83).asInstanceOf[Ptr[CChar]] = value
    def n_cs_precedes: CChar = !p.at(84).asInstanceOf[Ptr[CChar]]
    def n_cs_precedes_=(value: CChar): Unit =
      !p.at(84).asInstanceOf[Ptr[CChar]] = value
    def n_sep_by_space: CChar = !p.at(85).asInstanceOf[Ptr[CChar]]
    def n_sep_by_space_=(value: CChar): Unit =
      !p.at(85).asInstanceOf[Ptr[CChar]] = value
    def p_sign_posn: CChar = !p.at(86).asInstanceOf[Ptr[CChar]]
    def p_sign_posn_=(value: CChar): Unit =
      !p.at(86).asInstanceOf[Ptr[CChar]] = value
    def n_sign_posn: CChar = !p.at(87).asInstanceOf[Ptr[CChar]]
    def n_sign_posn_=(value: CChar): Unit =
      !p.at(87).asInstanceOf[Ptr[CChar]] = value
    def int_p_cs_precedes: CChar = !p.at(88).asInstanceOf[Ptr[CChar]]
    def int_p_cs_precedes_=(value: CChar): Unit =
      !p.at(88).asInstanceOf[Ptr[CChar]] = value
    def int_p_sep_by_space: CChar =
      !p.at(89).asInstanceOf[Ptr[CChar]]
    def int_p_sep_by_space_=(value: CChar): Unit =
      !p.at(89).asInstanceOf[Ptr[CChar]] = value
    def int_n_cs_precedes: CChar = !p.at(90).asInstanceOf[Ptr[CChar]]
    def int_n_cs_precedes_=(value: CChar): Unit =
      !p.at(90).asInstanceOf[Ptr[CChar]] = value
    def int_n_sep_by_space: CChar =
      !p.at(91).asInstanceOf[Ptr[CChar]]
    def int_n_sep_by_space_=(value: CChar): Unit =
      !p.at(91).asInstanceOf[Ptr[CChar]] = value
    def int_p_sign_posn: CChar = !p.at(92).asInstanceOf[Ptr[CChar]]
    def int_p_sign_posn_=(value: CChar): Unit =
      !p.at(92).asInstanceOf[Ptr[CChar]] = value
    def int_n_sign_posn: CChar = !p.at(93).asInstanceOf[Ptr[CChar]]
    def int_n_sign_posn_=(value: CChar): Unit =
      !p.at(93).asInstanceOf[Ptr[CChar]] = value
  }
}
