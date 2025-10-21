package org.scalanative.testsuite.posixlib

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix
import scala.scalanative.posix.{errno => posixErrno}
import scala.scalanative.unsafe._

import posixErrno._

class StringTest {
  /* This class tests only strtok_r(). This to exercise the declaration
   * and use of its complex third argument.
   *
   * Tests for other methods can be added incrementally over time.
   */

  /* Use the longer 'posix.string.foo' form of the methods under test
   * to ensure that the POSIX variant is used and that the libc version
   * did not sneak in.
   */

  @Test def strtok_rShouldNotFindToken(): Unit =
    if (!isWindows) {
      val str = c"Now is the time"
      val delim = c"&"
      val saveptr: Ptr[Ptr[Byte]] = stackalloc[Ptr[Byte]]()

      val rtn_1 = posix.string.strtok_r(str, delim, saveptr)
      assertEquals("strtok_1", fromCString(str), fromCString(rtn_1))

      val rtn_2 = posix.string.strtok_r(null, delim, saveptr)
      assertNull(
        s"strtok should not have found token: '${fromCString(rtn_2)}'",
        rtn_2
      )
    }

  @Test def strtok_rShouldFindTokens(): Unit =
    if (!isWindows) Zone.acquire { implicit z =>
      /* On this happy path, strtok_r() will attempt to write NULs into
       * the string, so DO NOT USE c"" interpolator.
       * "Segmentation fault caught" will remind you
       */
      val str = toCString("Now is the time")
      val delim = c" "
      val saveptr = stackalloc[Ptr[Byte]]()

      val rtn_1 = posix.string.strtok_r(str, delim, saveptr)
      assertEquals("strtok_1", "Now", fromCString(rtn_1))

      val rtn_2 = posix.string.strtok_r(null, delim, saveptr)
      assertEquals("strtok_2", "is", fromCString(rtn_2))

      val rtn_3 = posix.string.strtok_r(null, delim, saveptr)
      assertEquals("strtok_3", "the", fromCString(rtn_3))

      val rtn_4 = posix.string.strtok_r(null, delim, saveptr)
      assertEquals("strtok_4", "time", fromCString(rtn_4))

      // End of parse
      val rtn_5 = posix.string.strtok_r(null, delim, saveptr)
      assertNull(
        s"strtok should not have found token: '${fromCString(rtn_5)}'",
        rtn_5
      )
    }

}
