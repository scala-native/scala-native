package org.scalanative.testsuite.posixlib

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.junit.Before

import scala.scalanative.meta.LinktimeInfo.isWindows

/* Using both LinktimeInfo & runtime.Platform looks strange.
 * It is a workaround to let this test run whilst I a suspected bug
 * in LinktimeInfo is tracked down.
 */
import scalanative.runtime.Platform

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import scala.scalanative.libc.errno.errno
import scala.scalanative.libc.locale._
import scala.scalanative.posix.locale._
import scala.scalanative.posix.monetary._

class MonetaryTest {

  @Before
  def before(): Unit = {
    assumeTrue(
      "monetary.scala is not implemented on Windows",
      !isWindows
    )
  }

  @Test def strfmon_l_Using_en_US(): Unit =
    if (!isWindows) {
      errno = 0

      val locale = {
        val nl = newlocale(LC_MONETARY_MASK, c"en_US", null)
        if (errno == 0) nl
        else {
          errno = 0
          val unixNl = newlocale(LC_MONETARY_MASK, c"en_US.utf8", null)
          if (errno == 0) unixNl
          else {
            errno = 0
            newlocale(LC_MONETARY_MASK, c"en_US.UTF-8", null) // macOS
          }
        }
      }

      // multi-arch CI appears not to have any of these locales
      assumeTrue(
        "newlocale() failed to use one of en_US, en_US.utf8, " +
          "or en_US.UTF-8.",
        locale != null
      )

      try {
        val max = 128.toUInt
        val buf = stackalloc[Byte](max)

        // format arg adapted from Linux "man strfmon"

        val n = strfmon_l(
          buf,
          max,
          locale,
          c"[%^=*#6n] [%=*#6i]",
          1234.567,
          1234.567
        )

        assertNotEquals(s"strfmon_l() failed with errno: ${errno}\n", -1, n)

        val expected = if (Platform.isLinux()) {
          "[ $**1234.57] [ USD **1,234.57]"
        } else {
          "[ $**1234.57] [ USD**1,234.57]"
        }

        assertEquals(
          "Unexpected strfmon_l() result",
          expected,
          fromCString(buf)
        )

      } finally {
        freelocale(locale)
      }
    }
}
