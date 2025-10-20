package org.scalanative.testsuite.posixlib

import org.junit.Test
import org.junit.Assert.*
import org.junit.Assume.*
import org.junit.BeforeClass

import java.lang as jl
import java.io.IOException

import org.scalanative.testsuite.utils.Platform
import scala.scalanative.meta.LinktimeInfo.{isWindows, is32BitPlatform}
import scala.scalanative.runtime.PlatformExt

// libc.string is hidden by posix.string
import scala.scalanative.libc.string as libcString

/* Scala 2.11.n & 2.12.n complain about import of posixErrno.errno.
 * To span many Scala versions with same code used as
 * qualified posixErrno.errno below.
 */
import scala.scalanative.posix.errno as posixErrno, posixErrno.*

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

import scala.scalanative.posix.time, time.*
import scala.scalanative.posix.timeOps.{timespecOps, tmOps}

object TimeTest {

  @BeforeClass
  def beforeClass(): Unit = {
    tzset()
  }
}

class TimeTest {

  /* Many tests below use the "if (!isWindows)" idiom rather than one
   * obvious and simpler, but wrong, assumption here:
   *     assumeFalse("POSIX tests are not run on Windows", isWindows)
   *
   * The reason is that "isWindows" is a link time option which avoids
   * linking "!isWindows" code. "assumeFalse()" is executed at runtime,
   * after the link on Windows fails from missing symbols.
   *
   * A motivated developer could arrange for POSIX tests never to be
   * compiled at all on Windows. A bigger task than today allows.
   */

  // Note: both alloc & stackalloc clear allocated memory.

  // In 2.11/2.12 time was resolved to posix.time.type, in 2.13 to
  // posix.time.time method.

  val now_time: time_t = scala.scalanative.posix.time.time(null)
  val epoch: time_t = 0

  @Test def asctimeWithGivenKnownStateShouldMatchItsRepresentation(): Unit =
    if (!isWindows) {
      val anno_zero_ptr = stackalloc[tm]()
      anno_zero_ptr.tm_mday = 1
      anno_zero_ptr.tm_wday = 1
      val cstr: CString = asctime(anno_zero_ptr)
      val str: String = fromCString(cstr)
      assertEquals("Mon Jan  1 00:00:00 1900\n", str)
    }

  @Test def asctime_rWithGivenKnownStateShouldMatchItsRepresentation(): Unit =
    if (!isWindows) {
      val anno_zero_ptr = stackalloc[tm]()
      anno_zero_ptr.tm_mday = 1
      anno_zero_ptr.tm_wday = 1
      val cstr: CString = asctime_r(anno_zero_ptr, stackalloc[Byte](26))
      val str: String = fromCString(cstr)
      assertEquals("Mon Jan  1 00:00:00 1900\n", str)
    }

  @Test def localtimeShouldHandleEpochPlusTimezone(): Unit =
    if (!isWindows) {
      assumeFalse(
        "Skipping localtime test since FreeBSD hasn't the 'timezone' variable",
        Platform.isFreeBSD
      )

      val haveCI =
        java.lang.Boolean.parseBoolean(System.getenv("GITHUB_ACTIONS"))

      // Test has proven too fragile to run outside known environments.
      assumeTrue(
        "Tested only by GitHub continuous integration or developer bypass.",
        haveCI
      )

      /* unix epoch is defined as 0 seconds UTC (Universal Time).
       * 'timezone' is defined in Posix as seconds WEST of UTC. Yes WEST.
       * At 'epoch + timezone seconds' it will be 0 seconds local time.
       * That local time should display as the expected "Thu Jan etc.".
       *
       * The logic here is the inverse of what one would expect. This
       * is to avoid having to deal with daylight saving issues. We
       * know the standard timezone but the 'is_dst' field is documented
       * as unreliable.
       */

      val time_ptr = stackalloc[time_t]()
      !time_ptr = epoch + timezone
      val time: Ptr[tm] = localtime(time_ptr)
      val cstr: CString = asctime(time)
      val str: String = fromCString(cstr)

      assertEquals("Thu Jan  1 00:00:00 1970\n", str)
    }

  @Test def localtime_rShouldHandleEpochPlusTimezone(): Unit =
    if (!isWindows) {
      Zone.acquire { implicit z =>
        assumeFalse(
          "Skipping localtime_r test since FreeBSD hasn't the 'timezone' variable",
          Platform.isFreeBSD
        )

        // See _essential_ comment in corresponding localtime test about logic.

        val time_ptr = stackalloc[time_t]()
        !time_ptr = epoch + timezone
        val time: Ptr[tm] = localtime_r(time_ptr, alloc[tm]())
        val cstr: CString = asctime_r(time, alloc[Byte](26))
        val str: String = fromCString(cstr)

        assertEquals("Thu Jan  1 00:00:00 1970\n", str)
      }
    }

  @Test def difftimeBetweenEpochAndNowGreaterThanTimestampWhenCodeWasWritten()
      : Unit = {
    assertTrue(difftime(now_time, epoch) > 1502752688)
  }

  @Test def timeNowGreaterThanTimestampWhenCodeWasWritten(): Unit =
    if (!isWindows) {
      // arbitrary date set at the time when I was writing this.
      assertTrue(now_time > 1502752688)
    }

  @Test def strftimeForJanOne1900ZeroZulu(): Unit = if (!isWindows) {
    Zone.acquire { implicit z =>
      val isoDatePtr: Ptr[CChar] = alloc[CChar](70)
      val timePtr = alloc[tm]()

      timePtr.tm_mday = 1

      strftime(isoDatePtr, 70.toUSize, c"%FT%TZ", timePtr)

      val isoDateString: String = fromCString(isoDatePtr)

      assertEquals("1900-01-01T00:00:00Z", isoDateString)
    }
  }

  @Test def strftimeForMondayJanOne1990ZeroTime(): Unit = if (!isWindows) {
    Zone.acquire { implicit z =>
      val timePtr = alloc[tm]()
      val datePtr: Ptr[CChar] = alloc[CChar](70)

      timePtr.tm_mday = 1
      timePtr.tm_wday = 1

      strftime(datePtr, 70.toUSize, c"%A %c", timePtr)

      val dateString: String = fromCString(datePtr)
      assertEquals("Monday Mon Jan  1 00:00:00 1900", dateString)
    }
  }

  @Test def strptimeDetectsGrosslyInvalidFormat(): Unit = if (!isWindows) {
    val tmPtr = stackalloc[tm]()

    // As described in the Scala Native time.c implementation,
    // the format string is passed, unchecked, to the underlying
    // libc. All(?) will reject %Q in format.
    //
    // Gnu, macOS, and possibly other libc implementations parse
    // strftime specifiers such as %Z. As described in time.c, the
    // implementation under test is slightly non-conforming because
    // it does not reject specifiers accepted by the underlying libc.

    val result =
      strptime(c"December 31, 2016 23:59:60", c"%B %d, %Y %Q", tmPtr)

    assertTrue(s"expected null result, got pointer", result == null)
  }

  @Test def strptimeDetectsInvalidString(): Unit = if (!isWindows) {
    val tmPtr = stackalloc[tm]()

    // 32 in string is invalid
    val result =
      strptime(c"December 32, 2016 23:59:60", c"%B %d, %Y %T", tmPtr)

    assertTrue(s"expected null result, got pointer", result == null)
  }

  @Test def strptimeDetectsStringShorterThanFormat(): Unit = if (!isWindows) {
    val tmPtr = stackalloc[tm]()

    val result =
      strptime(c"December 32, 2016 23:59", c"%B %d, %Y %T", tmPtr)

    assertTrue(s"expected null result, got pointer", result == null)
  }

  @Test def strptimeFor31December2016Time235960(): Unit = if (!isWindows) {
    val tmPtr = stackalloc[tm]()

    // A leap second was added at this time
    val result =
      strptime(c"December 31, 2016 23:59:60", c"%B %d, %Y %T", tmPtr)

    assertNotEquals(
      "unexpected null return from strptime() call",
      null,
      result
    )

    val expectedYear = 116
    assertEquals("tm_year", expectedYear, tmPtr.tm_year)

    val expectedMonth = 11
    assertTrue(
      s"tm_mon: ${tmPtr.tm_mon} != expected: ${expectedMonth}",
      tmPtr.tm_mon == expectedMonth
    )

    val expectedMday = 31
    assertEquals("tm_mday", expectedMday, tmPtr.tm_mday)

    val expectedHour = 23
    assertEquals("tm_hour", expectedHour, tmPtr.tm_hour)

    val expectedMin = 59
    assertEquals("tm_min", expectedMin, tmPtr.tm_min)

    val expectedSec = 60
    assertEquals("tm_sec", expectedSec, tmPtr.tm_sec)

    // Per posix specification, content of field tm_isdst is not reliable.
  }

  @Test def strptimeExtraTextAfterDateStringIsOK(): Unit = if (!isWindows) {
    val tmPtr = stackalloc[tm]()

    val result =
      strptime(c"December 31, 2016 23:59:60 UTC", c"%B %d, %Y %T ", tmPtr)

    assertTrue(s"error: null returned", result != null)

    val expected = 'U'
    assertTrue(
      s"character: ${!result} != expected: ${expected}",
      !result == expected
    )
  }

  @Test def clockGetresReturnsBelievableResults(): Unit = if (!isWindows) {
    val timespecP = stackalloc[timespec]()
    timespecP.tv_sec = Int.MinValue // initialize with known bad values
    timespecP.tv_nsec = Int.MinValue

    val result = clock_getres(CLOCK_REALTIME, timespecP)

    assertEquals(
      s"clock_getres failed with errno: ${posixErrno.errno}",
      0,
      result
    )

    assertEquals(
      s"clock_getres tv_sec ${timespecP.tv_sec} != 0",
      0,
      timespecP.tv_sec.toLong
    )

    // Apparently silly test ensures CLOCKS_PER_SEC is exercised.
    assertTrue(
      s"clock_getres tv_nsec ${timespecP.tv_nsec} not in interval" +
        s" [0, ${CLOCKS_PER_SEC})",
      (timespecP.tv_nsec > 0) && (timespecP.tv_nsec <= CLOCKS_PER_SEC)
    )

    assertTrue(
      s"clock_getres tv_nsec ${timespecP.tv_nsec} is greater than millisecond",
      timespecP.tv_nsec <= (1 * 1000 * 1000)
    )

  }

  @Test def clockGettimeReturnsBelievableResults(): Unit = if (!isWindows) {
    val timespecP = stackalloc[timespec]()
    timespecP.tv_nsec = Int.MinValue // initialize with known bad value

    val now = scala.scalanative.posix.time.time(null) // Seconds since Epoch

    val result = clock_gettime(CLOCK_REALTIME, timespecP)

    assertEquals(
      s"clock_gettime failed with errno: ${posixErrno.errno}",
      0,
      result
    )

    /* The two time fetches were not done as one atomic transaction so
     * the times can and do validly vary by a "small" amount.
     *
     * Leap seconds, double leap seconds, process scheduling, VM machine
     * swapouts, and a number of factors can cause the difference.
     *
     * The challenge of defining "small" becomes an exercise in balancing
     * the reporting of false positives vs false negatives, the concept of
     * "Receiver Operating Characteristics". False positives in CI waste
     * a __lot__ of time, so err on the high side.
     *
     * Normally, the two times would be withing microseconds of each other,
     * well less than a second. Leap seconds, double leap seconds can add
     * a second or more, slow machines, etc.
     * 5 is a generous guess. Let's see if time proves it a good trade off.
     * The basic idea is to detect wildly wrong results from the unit under
     * test, not to stress either race conditions or developers.
     */

    val acceptableDiff = 5L
    val secondsDiff = Math.abs((timespecP.tv_sec - now).toLong)

    assertTrue(
      s"clock_gettime seconds ${secondsDiff} not within ${acceptableDiff}",
      secondsDiff <= acceptableDiff
    )

    assertTrue(
      s"clock_gettime nanoseconds ${timespecP.tv_nsec} not in " +
        s"interval [0, 999999999]",
      (timespecP.tv_nsec >= 0L) && (timespecP.tv_nsec <= 999999999L)
    )
  }

  @Test def clockNanosleepShouldExecute(): Unit = if (!isWindows) {
    val requestP = stackalloc[timespec]()
    requestP.tv_sec = 0
    requestP.tv_nsec = 1 // will be rounded up to minimum clock resolution.

    val result = clock_nanosleep(CLOCK_MONOTONIC, flags = 0, requestP, null)

    if (result == 0) {
      /* Full sleep should have happened.
       * Hard to test/verify. Nanosecond delays,
       * even rounded up to clock granularity, are exceedingly small
       * compared to background of OS & hardware, especially VM, noise.
       */
    } else if (result == EINTR) {
      // EINTR means sleep was interrupted, clock_nanosleep() executed.
    } else if (Platform.isMacOs || Platform.isOpenBSD) {
      // No macOS and OpenBSD clock_nanosleep().
      // time.c stub should always return ENOTSUP.
      assumeTrue(
        s"macOS or OpenBSD clock_nanosleep failed with return code: ${result}",
        result == ENOTSUP
      )
    } else {
      assertTrue(
        s"clock_nanosleep failed with return code: ${result}",
        false
      )
    }
  }

  @Test def clockSettimeShouldExecute(): Unit = if (!isWindows) {
    val timespecP = stackalloc[timespec]()

    // CLOCK_MONOTONIC is defined as un-settable, use to cause error result.
    val result = clock_settime(CLOCK_MONOTONIC, timespecP)

    assertEquals(
      s"clock_settime should have failed setting CLOCK_MONOTONIC",
      -1,
      result
    )

    /* Testing for a specific errno tends to quickly run into OS differences.
     * EINVAL is the "expected" result.
     * aarch64-linux-gnu and probably others return EPERM.
     */

    assertTrue(
      s"clock_settime failed with errno: ${posixErrno.errno}",
      posixErrno.errno == (EINVAL) || posixErrno.errno == (EPERM)
    )
  }

  // Issue 4454, Posix Issue 8 increased size of 'struct tm'
  @Test def structTmSize(): Unit = if (!isWindows) {
    def localTime(hours: Int, minutes: Int, seconds: Int): Long = {
      val out = stackalloc[time.tm]()
      val timePtr = stackalloc[time.time_t]()
      !timePtr = time.time(null)
      val gmTime: Ptr[time.tm] = time.gmtime_r(timePtr, out)

      gmTime.tm_hour = hours
      gmTime.tm_min = minutes
      gmTime.tm_sec = seconds
      gmTime.tm_isdst = -1; // Is DST on? 1 = yes, 0 = no, -1 = unknown
      time.mktime(gmTime).longValue()
    }

    /* In Issue 4454, localTime() would crash upon return.
     *
     * assertion is to force optimizer to use the return value of localTime().
     * 1756526400 approximately equals the date 2025-08-30 04:00:00 UTC/GMT.
     * Arguments supplied to localTime() are after that (by about 90 minutes).
     * Useful URL: https://www.unixtimestamp.com/
     */

    val expected = 1756526400L
    val got = localTime(5, 30, 15)
    assertTrue(s"expected: ${expected} < got, ${got} seconds", expected < got)
  }

  // Issue 4454, Posix Issue 8 increased size of 'struct tm'
  @Test def posixIssue8FieldsArePlausible(): Unit = if (!isWindows) {
    if (!is32BitPlatform) {
      // 32 bit platforms probably do not have fields under test.
      val out = stackalloc[time.tm]()
      val timePtr = stackalloc[time.time_t]()
      !timePtr = time.time(null)
      val localTime: Ptr[time.tm] = time.localtime_r(timePtr, out)

      /* CI can check if values returned by OS are reasonable.
       * 
       * Developers should manually & visually check for values being
       * correct for the timezone in which the test is being run.
       */

      // Theoretical range is +/- 86_400 seconds. Range in practice differs

      val gmtoffValue = localTime.tm_gmtoff
      val gmtoffLowBound = -43200
      val gmtoffHighBound = 50400
      val gmtoffRange = "(${gmtoffLowBound}, ${gmtoffHighBound})"

      val gmtoffMsg =
        s"local tm_gmtoff expected in range: ${gmtoffRange} got: ${gmtoffValue}"

      if (gmtoffValue < 0)
        assertTrue(gmtoffMsg, gmtoffValue >= gmtoffLowBound)
      else
        assertTrue(gmtoffMsg, gmtoffValue <= gmtoffHighBound)

      // Developers: uncomment to check against value expected for your timezone
      // fail(s"visual check of tm_gmtoff ${gmtoffValue}")

      val zoneValue = localTime.tm_zone

      assertNotNull("tm_zone", zoneValue) // check null vs. "", expect latter

      // Developers: uncomment to check against value expected for your timezone
      // fail(s"visual check of tm_zone '${fromCString(zoneValue)}'")
    }
  }
}
