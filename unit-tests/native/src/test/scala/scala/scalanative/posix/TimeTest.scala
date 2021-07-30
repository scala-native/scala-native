package scala.scalanative.posix

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import java.io.IOException

import org.scalanative.testsuite.utils.Platform

import scalanative.libc.{errno => libcErrno, string}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import time._
import timeOps.tmOps

class TimeTest {
  tzset()

  // Note: alloc clears memory

  // In 2.11/2.12 time was resolved to posix.time.type, in 2.13 to
  // posix.time.time method.
  val now_time_t: time_t = scala.scalanative.posix.time.time(null)
  val epoch: time_t = 0L

  // Some of the tests (the ones that call localtime) need
  // for the standard time to be in effect. This is because
  // depending on the timezone and or the underlying C stdlib
  // (we observed differences in tm_isdt output when dst was
  // in effect between macOS, Arch Linux and Ubuntu), if
  // daylight saving time is in effect, we can get skewed time
  // results. This is a best effort to make the tests more portable
  //
  // See discussion in https://github.com/scala-native/scala-native/issues/2237
  val timeIsStandard: Boolean = {
    Zone { implicit z =>
      val time_ptr = stackalloc[time_t]
      !time_ptr = now_time_t
      val localtime: Ptr[tm] = localtime_r(time_ptr, alloc[tm])

      localtime.tm_isdst == 0
    }
  }

  @Test def asctimeWithGivenKnownStateShouldMatchItsRepresentation(): Unit = {
    Zone { implicit z =>
      val anno_zero_ptr = alloc[tm]
      anno_zero_ptr.tm_mday = 1
      anno_zero_ptr.tm_wday = 1
      val cstr: CString = asctime(anno_zero_ptr)
      val str: String = fromCString(cstr)
      assertEquals("Mon Jan  1 00:00:00 1900\n", str)
    }
  }

  @Test def asctime_rWithGivenKnownStateShouldMatchItsRepresentation(): Unit = {
    Zone { implicit z =>
      val anno_zero_ptr = alloc[tm]
      anno_zero_ptr.tm_mday = 1
      anno_zero_ptr.tm_wday = 1
      val cstr: CString = asctime_r(anno_zero_ptr, alloc[Byte](26))
      val str: String = fromCString(cstr)
      assertEquals("Mon Jan  1 00:00:00 1900\n", str)
    }
  }
  @Test def localtimeShouldTransformTheEpochToLocaltime(): Unit = {
    assumeTrue("time is not standard, test will not execute", timeIsStandard)
    assumeFalse(
      "Skipping localtime test since FreeBSD hasn't the 'timezone' variable",
      Platform.isFreeBSD
    )
    val time_ptr = stackalloc[time_t]
    !time_ptr = epoch + timezone
    val time: Ptr[tm] = localtime(time_ptr)
    val cstr: CString = asctime(time)
    val str: String = fromCString(cstr)

    assertEquals("Thu Jan  1 00:00:00 1970\n", str)
  }

  @Test def localtime_rShouldTransformTheEpochToLocaltime(): Unit = {
    Zone { implicit z =>
      assumeTrue("time is not standard, test will not execute", timeIsStandard)
      assumeFalse(
        "Skipping localtime_r test since FreeBSD hasn't the 'timezone' variable",
        Platform.isFreeBSD
      )
      val time_ptr = stackalloc[time_t]
      !time_ptr = epoch + timezone
      val time: Ptr[tm] = localtime_r(time_ptr, alloc[tm])
      val cstr: CString = asctime_r(time, alloc[Byte](26))
      val str: String = fromCString(cstr)

      assertEquals("Thu Jan  1 00:00:00 1970\n", str)
    }
  }

  @Test def difftimeBetweenEpochAndNowGreaterThanTimestampWhenCodeWasWritten()
      : Unit = {
    assertTrue(difftime(now_time_t, epoch) > 1502752688)
  }

  @Test def timeNowGreaterThanTimestampWhenCodeWasWritten(): Unit = {
    // arbitrary date set at the time when I was writing this.
    assertTrue(now_time_t > 1502752688)
  }

  @Test def strftimeDoesNotReadMemoryOutsideStructTm(): Unit = {
    Zone { implicit z =>
      // The purpose of this test is to check two closely related conditions.
      // These conditions not a concern when the size of the C structure
      // is the same as the Scala Native structure and the order of the
      // fields match. They are necessary on BSD or glibc derived systems
      // where the Operating System libc uses 56 bytes, where the "extra"
      // have a time-honored, specified meaning.
      //
      //   1) Did time.scala strftime() have "@name" to ensure that structure
      //      copy-in/copy-out happened? Failure case is if 36 byte
      //      Scala Native tm got passed as-is to C strftime on a BSD/glibc
      //      system.
      //
      //   2) Did time.c strftime() zero any "excess" bytes if the C structure
      //      is larger than the Scala Native one? Failure case is that the
      //      timezone name in the output fails to match the expected regex.
      //      Often the mismatch consists of invisible, non-printing
      //      characters.
      //
      // Review the logic of this test thoroughly if size of "tm" changes.
      // This test may no longer be needed or need updating.
      assertEquals(
        "Review test! sizeof[Scala Native struct tm] changed",
        sizeof[tm],
        36.toULong
      )

      val ttPtr = alloc[time_t]
      !ttPtr = 1490986064740L / 1000L // Fri Mar 31 14:47:44 EDT 2017

      // This code is testing for reading past the end of a "short"
      // Scala Native tm, so the linux 56 byte form is necessary here.
      val tmBufCount = 7.toULong

      val tmBuf = alloc[Ptr[Byte]](tmBufCount)

      val tmPtr = tmBuf.asInstanceOf[Ptr[tm]]

      if (localtime_r(ttPtr, tmPtr) == null) {
        throw new IOException(fromCString(string.strerror(libcErrno.errno)))
      } else {
        val unexpected = "BOGUS"

        // With the "short" 36 byte SN struct tm tmBuf(6) is
        // BSD linux tm_zone, and outside the posix minimal required
        // range. strftime() should not read it.
        tmBuf(6) = toCString(unexpected)

        // grossly over-provision rather than chase fencepost bugs.
        val bufSize = 70.toULong
        val buf = alloc[Byte](bufSize)

        val n = strftime(buf, bufSize, c"%a %b %d %T %Z %Y", tmPtr)

        // strftime does not set errno on error
        assertNotEquals("unexpected zero from strftime", n, 0)

        val result = fromCString(buf)
        val len = "Fri Mar 31 14:47:44 ".length

        // time.scala @name caused structure copy-in/copy-out.
        assertEquals("strftime failed", result.indexOf(unexpected, len), -1)

        val regex = "[A-Z][a-z]{2} [A-Z][a-z]{2} " +
          "\\d\\d \\d{2}:\\d{2}:\\d{2} [A-Z]{2,5} 2017"

        // time.c strftime() zeroed excess bytes in BSD/glibc struct tm.
        assertTrue(
          s"result: '${result}' does not match regex: '${regex}'",
          result.matches(regex)
        )
      }
    }
  }

  @Test def strftimeForJanOne1900ZeroZulu(): Unit = {
    Zone { implicit z =>
      val isoDatePtr: Ptr[CChar] = alloc[CChar](70)
      val timePtr = alloc[tm]

      timePtr.tm_mday = 1

      strftime(isoDatePtr, 70.toULong, c"%FT%TZ", timePtr)

      val isoDateString: String = fromCString(isoDatePtr)

      assertEquals("1900-01-01T00:00:00Z", isoDateString)
    }
  }

  @Test def strftimeForMondayJanOne1990ZeroTime(): Unit = {
    Zone { implicit z =>
      val timePtr = alloc[tm]
      val datePtr: Ptr[CChar] = alloc[CChar](70)

      timePtr.tm_mday = 1
      timePtr.tm_wday = 1

      strftime(datePtr, 70.toULong, c"%A %c", timePtr)

      val dateString: String = fromCString(datePtr)
      assertEquals("Monday Mon Jan  1 00:00:00 1900", dateString)
    }
  }

  @Test def strptimeDetectsGrosslyInvalidFormat(): Unit = {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

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
  }

  @Test def strptimeDetectsInvalidString(): Unit = {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

      // 32 in string is invalid
      val result =
        strptime(c"December 32, 2016 23:59:60", c"%B %d, %Y %T", tmPtr)

      assertTrue(s"expected null result, got pointer", result == null)
    }
  }

  @Test def strptimeDetectsStringShorterThanFormat(): Unit = {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

      val result =
        strptime(c"December 32, 2016 23:59", c"%B %d, %Y %T", tmPtr)

      assertTrue(s"expected null result, got pointer", result == null)
    }
  }

  @Test def strptimeDoesNotWriteMemoryOutsideStructTm(): Unit = {
    Zone { implicit z =>
      assumeTrue("time is not standard, test will not execute", timeIsStandard)
      // The purpose of this test is to check that time.scala method
      // declaration had an "@name" annotation, so that structure
      // copy-in/copy-out happened? Failure case is if 36 byte
      // Scala Native tm got passed as-is to C strptime on a BSD/glibc
      // or macOS system; see the tm_gmtoff & tm_zone handling below.

      // This is not a concern when the size of the C structure
      // is the same as the Scala Native structure and the order of the
      // fields match. They are necessary on BSD, glibc derived, macOS,
      // and possibly other systems where the Operating System libc
      // uses 56 bytes, where the "extra" have a time-honored, specified
      // meaning.
      //
      // Key to magic numbers 56 & 36.
      // Linux _BSD_Source and macOS use at least 56 Bytes.
      // Posix specifies 36 but allows more.

      // Review logic of this test thoroughly if size of "tm" changes.
      // This test may no longer be needed or need updating.
      assertEquals(
        "Review test! sizeof[Scala Native struct tm] changed",
        sizeof[tm],
        36.toULong
      )

      val tmBufSize = 56.toULong
      val tmBuf = alloc[Byte](tmBufSize)

      val tmPtr = tmBuf.asInstanceOf[Ptr[tm]]

      val gmtIndex = 36.toULong

      // To detect the case where SN strptime() is writing tm_gmtoff
      // use a value outside the known range of valid values.
      // This can happen if "@name" annotation has gone missing.

      val expectedGmtOff = Long.MaxValue
      (tmBuf + gmtIndex).asInstanceOf[Ptr[CLong]](0) = expectedGmtOff

      // %Z is not a supported posix conversion specification, but
      // is useful here to detect a defect in the method-under-test.
      //
      // %Z is parsed by many/most libc. The Scala Native implementation
      // of strptime() passes the format argument to libc without parsing &
      // rejecting it for containing a non-posix conversion.
      // Gnu libc will parse the specifier and set no field in the C struct.
      // macOS will parse and accept "GMT" or the local timezone name
      // and write to the corresponding fields in the C struct.
      // "GMT" is used here to avoid local timezone handling.
      // FreeBSD fills the structure with values relative to the local
      // time zone, so the check would fail if we parse a date with a
      // different time zone.

      val cp =
        if (Platform.isFreeBSD)
          strptime(c"Fri Mar 31 14:47:44 2017", c"%a %b %d %T %Y", tmPtr)
        else
          strptime(c"Fri Mar 31 14:47:44 GMT 2017", c"%a %b %d %T %Z %Y", tmPtr)

      assertNotNull(s"strptime() returned unexpected null pointer", cp)

      val ch = cp(0) // last character not processed by strptime().
      assertEquals("strptime() result is not NUL terminated", ch, '\u0000')

      // tm_gmtoff & tm_zone are outside the posix defined range.
      // Scala Native strftime() should never write to them.
      //
      // Assume no leading or interior padding.

      val tm_gmtoff = (tmBuf + gmtIndex).asInstanceOf[Ptr[CLong]](0)
      assertEquals("tm_gmtoff", expectedGmtOff, tm_gmtoff)

      val tmZoneIndex = (gmtIndex + sizeof[CLong])
      val tm_zone = (tmBuf + tmZoneIndex).asInstanceOf[CString]
      assertNull("tm_zone", null)

      // Major concerning conditions passed. Sanity check the tm proper.

      val expectedSec = 44
      assertEquals("tm_sec", expectedSec, tmPtr.tm_sec)

      val expectedMin = 47
      assertEquals("tm_min", expectedMin, tmPtr.tm_min)

      val expectedHour = 14
      assertEquals("tm_hour", expectedHour, tmPtr.tm_hour)

      val expectedMday = 31
      assertEquals("tm_mday", expectedMday, tmPtr.tm_mday)

      val expectedMonth = 2
      assertEquals("tm_mon", expectedMonth, tmPtr.tm_mon)

      val expectedYear = 117
      assertEquals("tm_year", expectedYear, tmPtr.tm_year)

      val expectedWday = 5
      assertEquals("tm_wday", expectedWday, tmPtr.tm_wday)

      val expectedYday = 89
      assertEquals("tm_yday", expectedYday, tmPtr.tm_yday)

    // Per posix specification, contents of tm_isdst are not reliable.
    }
  }

  @Test def strptimeFor31December2016Time235960(): Unit = {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

      // A leap second was added at this time
      val result =
        strptime(c"December 31, 2016 23:59:60", c"%B %d, %Y %T", tmPtr)

      assertTrue(s"error: unexpected null returned", result != null)

      val expectedYear = 116
      assertTrue(
        s"tm_year: ${tmPtr.tm_year} != expected: ${expectedYear}",
        tmPtr.tm_year == expectedYear
      )

      val expectedMonth = 11
      assertTrue(
        s"tm_mon: ${tmPtr.tm_mon} != expected: ${expectedMonth}",
        tmPtr.tm_mon == expectedMonth
      )

      val expectedMday = 31
      assertTrue(
        s"tm_mon: ${tmPtr.tm_mday} != expected: ${expectedMday}",
        tmPtr.tm_mday == expectedMday
      )

      val expectedHour = 23
      assertTrue(
        s"tm_mon: ${tmPtr.tm_hour} != expected: ${expectedHour}",
        tmPtr.tm_hour == expectedHour
      )

      val expectedMin = 59
      assertTrue(
        s"tm_min: ${tmPtr.tm_min} != expected: ${expectedMin}",
        tmPtr.tm_min == expectedMin
      )

      val expectedSec = 60
      assertTrue(
        s"tm_sec: ${tmPtr.tm_sec} != expected: ${expectedSec}",
        tmPtr.tm_sec == expectedSec
      )

    // Per posix specification, contents of tm_isdst are not reliable.
    }
  }

  @Test def strptimeExtraTextAfterDateStringIsOK(): Unit = {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

      val result =
        strptime(c"December 31, 2016 23:59:60 UTC", c"%B %d, %Y %T ", tmPtr)

      assertTrue(s"error: null returned", result != null)

      val expected = 'U'
      assertTrue(
        s"character: ${!result} != expected: ${expected}",
        !result == expected
      )
    }
  }

}
