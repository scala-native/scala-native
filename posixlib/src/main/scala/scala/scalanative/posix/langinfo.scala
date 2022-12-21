package scala.scalanative
package posix

import scala.scalanative.unsafe._

/** POSIX langinfo.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 */

@extern object langinfo {

  type locale_t = locale.locale_t

  type nl_item = nl_types.nl_item

// Symbolic constants

  @name("scalanative_codeset")
  def CODESET: CInt = extern

  @name("scalanative_d_t_fmt")
  def D_T_FMT: CInt = extern

  @name("scalanative_d_fmt")
  def D_FMT: CInt = extern

  @name("scalanative_t_fmt")
  def T_FMT: CInt = extern

  @name("scalanative_t_fmt_ampm")
  def T_FMT_AMPM: CInt = extern

  @name("scalanative_am_str")
  def AM_STR: CInt = extern

  @name("scalanative_pm_str")
  def PM_STR: CInt = extern

  @name("scalanative_day_1")
  def DAY_1: CInt = extern

  @name("scalanative_day_2")
  def DAY_2: CInt = extern

  @name("scalanative_day_3")
  def DAY_3: CInt = extern

  @name("scalanative_day_4")
  def DAY_4: CInt = extern

  @name("scalanative_day_5")
  def DAY_5: CInt = extern

  @name("scalanative_day_6")
  def DAY_6: CInt = extern

  @name("scalanative_day_7")
  def DAY_7: CInt = extern

  @name("scalanative_abday_1")
  def ABDAY_1: CInt = extern

  @name("scalanative_abday_2")
  def ABDAY_2: CInt = extern

  @name("scalanative_abday_3")
  def ABDAY_3: CInt = extern

  @name("scalanative_abday_4")
  def ABDAY_4: CInt = extern

  @name("scalanative_abday_5")
  def ABDAY_5: CInt = extern

  @name("scalanative_abday_6")
  def ABDAY_6: CInt = extern

  @name("scalanative_abday_7")
  def ABDAY_7: CInt = extern

  @name("scalanative_mon_1")
  def MON_1: CInt = extern

  @name("scalanative_mon_2")
  def MON_2: CInt = extern

  @name("scalanative_mon_3")
  def MON_3: CInt = extern

  @name("scalanative_mon_4")
  def MON_4: CInt = extern

  @name("scalanative_mon_5")
  def MON_5: CInt = extern

  @name("scalanative_mon_6")
  def MON_6: CInt = extern

  @name("scalanative_mon_7")
  def MON_7: CInt = extern

  @name("scalanative_mon_8")
  def MON_8: CInt = extern

  @name("scalanative_mon_9")
  def MON_9: CInt = extern

  @name("scalanative_mon_10")
  def MON_10: CInt = extern

  @name("scalanative_mon_11")
  def MON_11: CInt = extern

  @name("scalanative_mon_12")
  def MON_12: CInt = extern

  @name("scalanative_abmon_1")
  def ABMON_1: CInt = extern

  @name("scalanative_abmon_2")
  def ABMON_2: CInt = extern

  @name("scalanative_abmon_3")
  def ABMON_3: CInt = extern

  @name("scalanative_abmon_4")
  def ABMON_4: CInt = extern

  @name("scalanative_abmon_5")
  def ABMON_5: CInt = extern

  @name("scalanative_abmon_6")
  def ABMON_6: CInt = extern

  @name("scalanative_abmon_7")
  def ABMON_7: CInt = extern

  @name("scalanative_abmon_8")
  def ABMON_8: CInt = extern

  @name("scalanative_abmon_9")
  def ABMON_9: CInt = extern

  @name("scalanative_abmon_10")
  def ABMON_10: CInt = extern

  @name("scalanative_abmon_11")
  def ABMON_11: CInt = extern

  @name("scalanative_abmon_12")
  def ABMON_12: CInt = extern

  @name("scalanative_era")
  def ERA: CInt = extern

  @name("scalanative_era_d_fmt")
  def ERA_D_FMT: CInt = extern

  @name("scalanative_era_d_t_fmt")
  def ERA_D_T_FMT: CInt = extern

  @name("scalanative_era_t_fmt")
  def ERA_T_FMT: CInt = extern

  @name("scalanative_alt_digits")
  def ALT_DIGITS: CInt = extern

  @name("scalanative_radixchar")
  def RADIXCHAR: CInt = extern

  @name("scalanative_thousep")
  def THOUSEP: CInt = extern

  @name("scalanative_yesexpr")
  def YESEXPR: CInt = extern

  @name("scalanative_noexpr")
  def NOEXPR: CInt = extern

  @name("scalanative_crncystr")
  def CRNCYSTR: CInt = extern

// Methods

  def nl_langinfo(item: nl_item): CString = extern

  def nl_langinfo_l(item: nl_item, locale: locale_t): CString = extern
}
