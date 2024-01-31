package org.scalanative.testsuite.posixlib

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.junit.{BeforeClass, AfterClass}

import scala.scalanative.meta.LinktimeInfo

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import scala.scalanative.posix.errno.errno
import scala.scalanative.posix.locale._
import scala.scalanative.posix.localeOps._
import scala.scalanative.posix.stdlib
import scala.scalanative.posix.string

object LocaleTest {
  var savedLocale: Option[CString] = None

  @BeforeClass
  def beforeClass(): Unit = {
    assumeTrue(
      "locale.scala is not implemented on Windows",
      !LinktimeInfo.isWindows
    )

    if (!LinktimeInfo.isWindows) {
      val entryLocale = setlocale(LC_ALL, null)
      assertNotNull(
        "setlocale() could not determine locale at start of test.",
        entryLocale
      )

      // Save before setlocale() calls overwrite static buffer returned.
      savedLocale = Some(string.strdup(entryLocale)) // note: no CString

      val currentLocale = {
        val en_US = setlocale(LC_ALL, c"en_US")
        if (en_US != null) en_US
        else {
          val en_USutf8 = setlocale(LC_ALL, c"en_US.utf8") // Linux
          if (en_USutf8 != null) en_USutf8
          else setlocale(LC_ALL, c"en_US.UTF-8") // macOS
        }
      }

      if (currentLocale == null)
        savedLocale = None // Oops, no change! Nothing to restore.
    }
  }

  @AfterClass
  def afterClass(): Unit = {
    if (!LinktimeInfo.isWindows) {
      savedLocale.map { sl =>
        errno = 0
        // restore Locale as recorded on entry
        val restoredLocale = setlocale(LC_ALL, sl)

        stdlib.free(sl)

        if (restoredLocale == null)
          fail("setlocale() was unable to restore the locale.")
      }
    }
  }
}

// See also MonetaryTest.scala where number of locale methods are exercised.

class LocaleTest {

  import LocaleTest.savedLocale

  @Test def localeconv_Using_en_US(): Unit = {

    // Multi-arch CI tests do not have an en_US locale; warn not fail
    assumeTrue(
      "setlocale() failed to set an en_US test locale",
      savedLocale.isDefined
    )

    if (!LinktimeInfo.isWindows) {
      val currentLconv = localeconv() // documented as always succeeds.

      assertEquals(
        "US decimal_point",
        ".",
        fromCString(currentLconv.decimal_point)
      )

      assertEquals(
        "US thousands_sep",
        ",",
        fromCString(currentLconv.thousands_sep)
      )

      /* Skip grouping testing on FreeBSD. There is some long standing
       * discussion that FreeBSD does not use POSIX compliant values.
       * Do not test for an exact value that is known to be buggy.
       * The is to reduce them maintenance headache & cost if that
       * bug ever gets fixed.
       */

      if (!LinktimeInfo.isFreeBSD) {
        // Expect three byte-integers 3, 3, 0  meaning infinite group-by-three
        assertEquals(
          "US grouping",
          "\u0003\u0003",
          fromCString(currentLconv.grouping)
        )
      }

      assertEquals(
        "US int_curr_symbol",
        "USD ",
        fromCString(currentLconv.int_curr_symbol)
      )

      assertEquals(
        "US currency_symbol",
        "$",
        fromCString(currentLconv.currency_symbol)
      )

      assertEquals(
        "US mon_decimal_point",
        ".",
        fromCString(currentLconv.mon_decimal_point)
      )

      assertEquals(
        "US mon_thousands_sep",
        ",",
        fromCString(currentLconv.mon_thousands_sep)
      )

      // See "skip "FreeBSD"" comment before US grouping check above.

      if (!LinktimeInfo.isFreeBSD) {
        // Expect three byte-integers 3, 3, 0, meaning infinite group-by-3
        assertEquals(
          "US mon_grouping",
          "\u0003\u0003",
          fromCString(currentLconv.mon_grouping)
        )
      }

      assertEquals(
        "US positive_sign",
        "",
        fromCString(currentLconv.positive_sign)
      )

      assertEquals(
        "US negative_sign",
        "-",
        fromCString(currentLconv.negative_sign)
      )

      assertEquals("US int_frac_digits", 2, currentLconv.int_frac_digits)

      assertEquals("US frac_digits", 2, currentLconv.frac_digits)

      assertEquals("US p_cs_precedes", 1, currentLconv.p_cs_precedes)

      assertEquals("US p_sep_by_space", 0, currentLconv.p_sep_by_space)

      assertEquals("US n_cs_precedes", 1, currentLconv.n_cs_precedes)

      assertEquals("US n_sep_by_space", 0, currentLconv.n_sep_by_space)

      assertEquals("US p_sign_posn", 1, currentLconv.p_sign_posn)

      assertEquals("US n_sign_posn", 1, currentLconv.n_sign_posn)

      assertEquals("US int_p_cs_precedes", 1, currentLconv.int_p_cs_precedes)

      assertEquals("US int_n_cs_precedes", 1, currentLconv.int_n_cs_precedes)

      if (LinktimeInfo.isLinux) {
        assertEquals(
          "US int_p_sep_by_space",
          1,
          currentLconv.int_p_sep_by_space
        )

        assertEquals(
          "US int_n_sep_by_space",
          1,
          currentLconv.int_n_sep_by_space
        )
      } else {
        assertEquals(
          "US int_p_sep_by_space",
          0,
          currentLconv.int_p_sep_by_space
        )
        assertEquals(
          "US int_n_sep_by_space",
          0,
          currentLconv.int_n_sep_by_space
        )
      }

      assertEquals("US int_p_sign_posn", 1, currentLconv.int_p_sign_posn)

      assertEquals("US int_n_sign_posn", 1, currentLconv.int_n_sign_posn)
    }
  }
}
