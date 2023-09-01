package org.scalanative.testsuite.javalib.util

import java.util._

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.scalanative.testsuite.utils.Platform._

class DateTest {
  // now : java.util.Date = Fri Mar 31 14:47:44 EDT 2017
  val nowUt = 1490986064740L
  val beforeUt = 1490986059300L
  val afterUt = 1490986090620L
  val now = new Date(nowUt)
  val before = new Date(beforeUt)
  val after = new Date(afterUt)
  val now2 = new Date(nowUt)

  @Test def testAfter(): Unit = {
    assertTrue(after.after(now))
  }

  @Test def testBefore(): Unit = {
    assertTrue(before.before(now))
  }

  @Test def testClone(): Unit = {
    val clone = now.clone().asInstanceOf[Date]
    assertTrue(clone.getTime equals now.getTime)
  }

  @Test def testCompareTo(): Unit = {
    assertTrue(now.compareTo(now2) == 0)
    assertTrue(before.compareTo(now) == -1)
    assertTrue(after.compareTo(now) == 1)
  }

  @Test def testEquals(): Unit = {
    assertTrue(now.equals(now2))
  }

  @Test def testGetTime(): Unit = {
    assertTrue(now.getTime == nowUt)
  }

  @Test def testHashCode(): Unit = {
    assertTrue(now.hashCode == nowUt.hashCode())
  }

  @Test def testSetTime(): Unit = {
    val nowBefore = new Date(nowUt)
    nowBefore.setTime(afterUt)
    assertTrue(nowBefore equals after)
  }

  @Test def testToString(): Unit = {
    // Due to problems with timezone abbreviation on Windows
    assumeFalse(
      "SN Windows implementation does not contain timezone",
      executingInScalaNative && isWindows
    )

    /*
     * The JDK Date.toString() description defines the format for most of
     * the fields returned by toString(). One can expect "Mon" instead of, say
     * "Lundi".
     *
     * The timezone name, "zzz" in the description, can be any name in the
     * IANA (Internet Assigned Numbers Authority) Time Zone Database
     * URL: https://www.iana.org/time-zones.
     *
     * The timezone name is controlled/known in the GitHub Continuous
     * Integration (CI) environment as is the matching regex
     * (regular expression)
     *
     * Use a wildcard regex outside the CI environment to avoid having to
     * parse the whole Time Zone Database.
     */

    val haveCI =
      java.lang.Boolean.parseBoolean(System.getenv("GITHUB_ACTIONS"))

    val tzRegex =
      if (haveCI) "[A-Z]{2,5} "
      else ".*"

    /* regex should match, but not be: "Fri Mar 31 14:47:44 EDT 2020"
     * Two decade year range in regex is coarse sanity check.
     */
    val expected = "[A-Z][a-z]{2} [A-Z][a-z]{2} " +
      s"\\d\\d \\d{2}:\\d{2}:\\d{2} ${tzRegex}20[2-3]\\d"

    val result = new Date().toString // actual time this test is run.

    assertTrue(
      s"""Result "${result}" does not match regex "${expected}"""",
      result.matches(expected)
    )
  }
}
