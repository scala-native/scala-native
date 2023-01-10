package org.scalanative.testsuite.posixlib
package sys

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import scala.scalanative.meta.LinktimeInfo.{
  is32BitPlatform,
  isFreeBSD,
  isWindows
}

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import scala.scalanative.posix.stdlib

import scala.scalanative.posix.sys.times._
import scala.scalanative.posix.sys.timesOps._

class TimesTest {

  @Test def timesSucceeds(): Unit = {
    assumeTrue(
      "times.scala is not implemented on Windows",
      !isWindows
    )
    if (!isWindows) {
      val timesBuf = stackalloc[tms]()

      // Modify the buffer so we can tell that OS changed values. Expect Zero.
      timesBuf.tms_cutime = -1
      timesBuf.tms_cstime = -1

      val status = times(timesBuf)

      assertNotEquals("times() failed: $strerror(errno):", -1, status)

      // A _very_rough_ check for sensible values follows.

      assertNotEquals("tms_utime should be non-zero:", 0L, timesBuf.tms_utime)
      assertTrue(
        s"tms_utime ${timesBuf.tms_utime} should be positive:",
        timesBuf.tms_utime > 0L
      )

      /* If this test is the first or only test being run, the system
       * time can be zero.
       */
      if (timesBuf.tms_stime != 0L)
        assertTrue(
          s"tms_stime ${timesBuf.tms_stime} should be positive:",
          timesBuf.tms_stime > 0L
        )

      assertNotEquals("tms_cutime should be zero:", 0L, timesBuf.tms_cutime)

      assertNotEquals("tms_cstime should be zero:", 0L, timesBuf.tms_cstime)
    }
  }

  @Test def naturalTimesOpsShouldGetAndSetFields(): Unit = {
    assumeTrue(
      "times.scala is not implemented on Windows",
      !isWindows
    )

    if (!isWindows && !isFreeBSD) {
      /* Test the 'natural' cases where there is no FreeBSD64 overlay
       * of tms fields. Here each of those fields is a Scala Size, 64
       * or 32 bits as appropriate to the architecture.
       */

      val timesBuf = stackalloc[tms]()

      val expectedUTime = 123L.toSize

      timesBuf.tms_utime = expectedUTime
      assertEquals("Unexpected tms_utime:", expectedUTime, timesBuf.tms_utime)

      val expectedSTime = 456L.toSize

      timesBuf.tms_stime = expectedSTime
      assertEquals("Unexpected tms_stime:", expectedSTime, timesBuf.tms_stime)

      val expectedCUTime = 333L.toSize
      timesBuf.tms_cutime = expectedCUTime
      assertEquals(
        "Unexpected tms_cutime:",
        expectedCUTime,
        timesBuf.tms_cutime
      )

      val expectedCSTime = 789L.toSize
      timesBuf.tms_cstime = expectedCSTime
      assertEquals(
        "Unexpected tms_cstime:",
        expectedCSTime,
        (timesBuf.tms_cstime)
      )
    }
  }

  @Test def freeBSD64TimesOpsShouldGetAndSetFields(): Unit = {
    if (isFreeBSD && !is32BitPlatform) {
      val timesBuf = stackalloc[tms]()

      val expectedUTime = 222L.toSize

      timesBuf.tms_utime = expectedUTime

      // Was the tmsOps 'set' done correctly?
      assertEquals(
        "Unexpected timesBuf._1 low bits:",
        expectedUTime,
        timesBuf._1
      )

      // Does the tmsOp 'get' retrieve correctly?
      assertEquals("Unexpected tms_utime:", expectedUTime, timesBuf.tms_utime)

      val expectedSTime = 666L.toSize

      timesBuf.tms_stime = expectedSTime

      assertEquals(
        "Unexpected timesBuf._1 high bits:",
        expectedSTime,
        (timesBuf._1 >>> 32)
      )

      assertEquals(
        "Unexpected tms_stime:",
        expectedSTime,
        (timesBuf.tms_stime >>> 32)
      )

      val expectedCUTime = 333L.toSize
      timesBuf.tms_cutime = expectedCUTime

      assertEquals(
        "Unexpected timesBuf._2 low bits:",
        expectedCUTime,
        timesBuf._2
      )

      assertEquals(
        "Unexpected tms_cutime:",
        expectedCUTime,
        timesBuf.tms_cutime
      )

      val expectedCSTime = 667L.toSize

      timesBuf.tms_cstime = expectedCSTime

      assertEquals(
        "Unexpected timesBuf._2 high bits:",
        expectedCSTime,
        (timesBuf._2 >>> 32)
      )

      assertEquals(
        "Unexpected tms_cstime:",
        expectedCSTime,
        (timesBuf.tms_cstime >>> 32)
      )
    }
  }
}
