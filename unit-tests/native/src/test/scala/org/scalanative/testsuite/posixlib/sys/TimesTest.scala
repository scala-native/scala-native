package org.scalanative.testsuite.posixlib
package sys

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo.{
  is32BitPlatform, isFreeBSD, isNetBSD, isWindows
}
import scala.scalanative.posix.stdlib
import scala.scalanative.posix.string.memset
import scala.scalanative.posix.sys.times._
import scala.scalanative.posix.sys.timesOps._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

class TimesTest {

  @Test def timesSucceeds(): Unit = {
    assumeTrue(
      "times.scala is not implemented on Windows",
      !isWindows
    )
    if (!isWindows) {
      val timesBuf = stackalloc[tms]()

      /* Modify the buffer so we can tell that subsequent values are
       * set by the operating system, not unmodified original values.
       * It is highly unlikely, but not impossible, that a busted times()
       * call would set the field to _exactly_ the poisoned value.
       */
      val poisonByte = -86 // -86 == bits 10101010
      val poisonedClock_t = stackalloc[clock_t]()

      memset(
        poisonedClock_t.asInstanceOf[Ptr[Byte]],
        poisonByte,
        sizeof[clock_t]
      )
      memset(timesBuf.asInstanceOf[Ptr[Byte]], poisonByte, sizeof[tms])

      val status = times(timesBuf)

      assertNotEquals("times() failed: $strerror(errno):", -1, status)

      /* _very_rough_ checks for sensible values follow.
       */

      /* One would believe that by the time execution reached this point
       * at least one clock tick of user CPU time (tms_utime) had elapsed
       * since the process executing this test started. A similar but weaker
       * expectation holds for system CPU time (tms_stime).
       *
       * Continuous Integration (CI) experience has shown that in
       * unknown situations either or both tms_utime & tms_stime can be zero,
       * even in the middle of what appears to be a long-running process.
       * This appears to be something the operating systems, plural, are
       * doing, rather than a misunderstanding of times(), a broken SN
       * implementation, or a blatantly bad test here. Subtly bad
       * perhaps, or blatantly bad to other eyes.
       *
       * This test is intentionally designed to not waste CI time by
       * burning CPU cycles doing busy work trying to force at least one
       * clock tick.
       *
       * Test below that times() has changed the field and then
       * accept any unexpected zeros returned by the operating system.
       * That is, test for non-zero rather than strictly positive.
       *
       * As experience is gained or the cause of the zeros is better
       * understood, this test should be updated.
       *
       * In the meantime, do not inject intermittent failures into
       * the CI builds. They annoy the residents.
       */

      assertNotEquals(
        s"tms_utime should not be poisoned:",
        !poisonedClock_t,
        timesBuf.tms_utime
      )
      assertTrue(
        s"tms_utime ${timesBuf.tms_utime} should be non-negative:",
        timesBuf.tms_utime >= 0L
      )

      assertNotEquals(
        s"tms_stime should not be poisoned:",
        !poisonedClock_t,
        timesBuf.tms_stime
      )
      assertTrue(
        s"tms_stime ${timesBuf.tms_stime} should be non-negative:",
        timesBuf.tms_stime >= 0L
      )

      /* This Test does nothing to change child process times but
       * some Tests which executed before it may have. So one must
       * test for non-negative instead of the obvious zero.
       * Joys of executing in a ~~poluted~~ shared execution environment.
       */
      assertNotEquals(
        s"tms_cutime should not be poisoned:",
        !poisonedClock_t,
        timesBuf.tms_cutime
      )
      assertTrue(
        s"tms_cutime ${timesBuf.tms_cutime} should be non-negative:",
        timesBuf.tms_utime >= 0L
      )

      assertNotEquals(
        s"tms_cstime should not be poisoned:",
        !poisonedClock_t,
        timesBuf.tms_cstime
      )
      assertTrue(
        s"tms_cstime ${timesBuf.tms_cstime} should be non-negative:",
        timesBuf.tms_cstime >= 0L
      )
    }
  }

  @Test def naturalTimesOpsShouldGetAndSetFields(): Unit = {
    assumeTrue(
      "times.scala is not implemented on Windows",
      !isWindows
    )

    if (!isWindows && !isFreeBSD && !isNetBSD) {
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

  @Test def use32BitOn64BitTimesOpsShouldGetAndSetFields(): Unit = {
    if ((isFreeBSD || isNetBSD) && !is32BitPlatform) {
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
