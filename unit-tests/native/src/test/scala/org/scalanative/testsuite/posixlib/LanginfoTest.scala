package org.scalanative.testsuite.posixlib

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.junit.{BeforeClass, AfterClass}

import scala.scalanative.meta.LinktimeInfo.{
  isLinux,
  isMac,
  isWindows,
  isOpenBSD
}

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import scala.scalanative.posix.langinfo._
import scala.scalanative.posix.locale.{setlocale, LC_ALL}
import scala.scalanative.posix.stdlib
import scala.scalanative.posix.string

object LanginfoTest {
  private var savedLocale: Option[CString] = None

  @BeforeClass
  def beforeClass(): Unit = {
    if (!isWindows) {
      val entryLocale = setlocale(LC_ALL, null)
      assertNotNull(
        "setlocale() could not determine locale at start of test.",
        entryLocale
      )

      // Save before setlocale() calls overwrite static buffer returned.
      savedLocale = Some(string.strdup(entryLocale)) // note: no CString

      /* Require a known locale in order to simplify CI testing.
       * "soft fail" is the locale is not available, such as in the wild.
       */

      val requiredLocale = if (isLinux) c"en_US.utf8" else c"en_US.UTF-8"

      if (setlocale(LC_ALL, requiredLocale) == null)
        savedLocale = None // Oops, no change! Nothing to restore. Fail later.
    }
  }

  @AfterClass
  def afterClass(): Unit = {
    if (!isWindows) {
      savedLocale.map { sl =>
        // Restore Locale as recorded on entry.
        val restoredLocale = setlocale(LC_ALL, sl)

        stdlib.free(sl)

        if (restoredLocale == null)
          fail("setlocale() was unable to restore the locale.")
      }
    }
  }
}

/* Can't tell the players without a program or this legend.
 * The testing matrix is complex because locales and their configurations are
 * highly variable.
 *
 * Testing is done on Linux & macOS when the required en_US.utf8 (Linux) or
 * en_US.UTF-8 locale is available.
 *
 * - No testing at all is done on Windows (because not implemented).
 * - The testing on CI multiarch machines "soft fails" because the
 *   required locale is not available.
 * - No os specific testing is done on FreeBSD, for want of a suitable
 *   validation environment.
 */

class LanginfoTest {

  case class LanginfoItem(name: String, code: nl_item, expected: String)

  def verify(item: LanginfoItem): Unit = {
    assertEquals(
      s"${item.name}",
      item.expected,
      fromCString(nl_langinfo(item.code))
    )
  }

  @Test def langinfo_Using_en_US_UTF8(): Unit = {
    assumeTrue(
      "langinfo.scala is not implemented on Windows",
      !isWindows
    )

    /* Warn here instead of doing a hard fail.
     *   Multi-arch CI tests do not have an en_US locale.
     *   This may also be true of non-CI systems in the wild.
     */
    assumeTrue(
      "setlocale() failed to set an en_US.[utf8|UTF-8] test locale",
      LanginfoTest.savedLocale.isDefined
    )

    if (!isWindows) {
      val osSharedItems = Array(
        LanginfoItem("CODESET", CODESET, "UTF-8"),
        LanginfoItem("D_FMT", D_FMT, if (isOpenBSD) "%m/%d/%y" else "%m/%d/%Y"),
        LanginfoItem("T_FMT_AMPM", T_FMT_AMPM, "%I:%M:%S %p"),
        LanginfoItem("AM_STR", AM_STR, "AM"),
        LanginfoItem("PM_STR", PM_STR, "PM"),
        LanginfoItem("DAY_1", DAY_1, "Sunday"),
        LanginfoItem("DAY_2", DAY_2, "Monday"),
        LanginfoItem("DAY_3", DAY_3, "Tuesday"),
        LanginfoItem("DAY_4", DAY_4, "Wednesday"),
        LanginfoItem("DAY_5", DAY_5, "Thursday"),
        LanginfoItem("DAY_6", DAY_6, "Friday"),
        LanginfoItem("DAY_7", DAY_7, "Saturday"),
        LanginfoItem("ABDAY_1", ABDAY_1, "Sun"),
        LanginfoItem("ABDAY_2", ABDAY_2, "Mon"),
        LanginfoItem("ABDAY_3", ABDAY_3, "Tue"),
        LanginfoItem("ABDAY_4", ABDAY_4, "Wed"),
        LanginfoItem("ABDAY_5", ABDAY_5, "Thu"),
        LanginfoItem("ABDAY_6", ABDAY_6, "Fri"),
        LanginfoItem("ABDAY_7", ABDAY_7, "Sat"),
        LanginfoItem("MON_1", MON_1, "January"),
        LanginfoItem("MON_2", MON_2, "February"),
        LanginfoItem("MON_3", MON_3, "March"),
        LanginfoItem("MON_4", MON_4, "April"),
        LanginfoItem("MON_5", MON_5, "May"),
        LanginfoItem("MON_6", MON_6, "June"),
        LanginfoItem("MON_7", MON_7, "July"),
        LanginfoItem("MON_8", MON_8, "August"),
        LanginfoItem("MON_9", MON_9, "September"),
        LanginfoItem("MON_10", MON_10, "October"),
        LanginfoItem("MON_11", MON_11, "November"),
        LanginfoItem("MON_12", MON_12, "December"),
        LanginfoItem("ABMON_1", ABMON_1, "Jan"),
        LanginfoItem("ABMON_2", ABMON_2, "Feb"),
        LanginfoItem("ABMON_3", ABMON_3, "Mar"),
        LanginfoItem("ABMON_4", ABMON_4, "Apr"),
        LanginfoItem("ABMON_5", ABMON_5, "May"),
        LanginfoItem("ABMON_6", ABMON_6, "Jun"),
        LanginfoItem("ABMON_7", ABMON_7, "Jul"),
        LanginfoItem("ABMON_8", ABMON_8, "Aug"),
        LanginfoItem("ABMON_9", ABMON_9, "Sep"),
        LanginfoItem("ABMON_10", ABMON_10, "Oct"),
        LanginfoItem("ABMON_11", ABMON_11, "Nov"),
        LanginfoItem("ABMON_12", ABMON_12, "Dec"),
        LanginfoItem("ALT_DIGITS", ALT_DIGITS, ""),
        LanginfoItem("RADIXCHAR", RADIXCHAR, ".")
      )

      osSharedItems.foreach(verify(_))

      if (!isWindows && !isOpenBSD)
        Array(
          LanginfoItem("ERA", ERA, ""),
          LanginfoItem("ERA_D_FMT", ERA_D_FMT, ""),
          LanginfoItem("ERA_D_T_FMT", ERA_D_T_FMT, ""),
          LanginfoItem("ERA_T_FMT", ERA_T_FMT, ""),
          LanginfoItem("THOUSEP", THOUSEP, ","), // linux
          LanginfoItem("CRNCYSTR", CRNCYSTR, "-$")
        ).foreach(verify(_))

      if (isLinux) {
        Array(
          LanginfoItem("D_T_FMT", D_T_FMT, "%a %d %b %Y %r %Z"),
          LanginfoItem("T_FMT", T_FMT, "%r"),
          LanginfoItem("YESEXPR", YESEXPR, "^[+1yY]"),
          LanginfoItem("NOEXPR", NOEXPR, "^[-0nN]")
        ).foreach(verify(_))
      } else if (isMac) {
        Array(
          LanginfoItem("D_T_FMT", D_T_FMT, "%a %b %e %X %Y"),
          LanginfoItem("T_FMT", T_FMT, "%H:%M:%S"),
          LanginfoItem("YESEXPR", YESEXPR, "^[yYsS].*"),
          LanginfoItem("NOEXPR", NOEXPR, "^[nN].*")
        ).foreach(verify(_))
      }
    }
  }
}
