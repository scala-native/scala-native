package scala.scalanative.posix

import org.junit.Test
import org.junit.Assert._

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import time._
import timeOps.tmOps

class TimeTest {
  tzset()
  //In 2.11/2.12 time was resolved to posix.time.type, in 2.13 to posix.time.time method
  val now_time_t: time_t = scala.scalanative.posix.time.time(null)
  val epoch: time_t      = 0L

  @Test def asctimeWithGivenKnownStateShouldMatchItsRepresentation(): Unit = {
    Zone { implicit z =>
      val anno_zero_ptr = alloc[tm]
      anno_zero_ptr.tm_mday = 1
      anno_zero_ptr.tm_wday = 1
      val cstr: CString = asctime(anno_zero_ptr)
      val str: String   = fromCString(cstr)
      assertTrue("Mon Jan  1 00:00:00 1900\n".equals(str))
    }
  }

  @Test def asctime_rWithGivenKnownStateShouldMatchItsRepresentation(): Unit = {
    Zone { implicit z =>
      val anno_zero_ptr = alloc[tm]
      anno_zero_ptr.tm_mday = 1
      anno_zero_ptr.tm_wday = 1
      val cstr: CString = asctime_r(anno_zero_ptr, alloc[Byte](26))
      val str: String   = fromCString(cstr)
      assertTrue("Mon Jan  1 00:00:00 1900\n".equals(str))
    }
  }

  @Test def localtimeShouldTransformTheEpochToLocaltime(): Unit = {
    val time_ptr = stackalloc[time_t]
    !time_ptr = epoch + timezone
    val time: Ptr[tm] = localtime(time_ptr)
    val cstr: CString = asctime(time)
    val str: String   = fromCString(cstr)

    assertTrue("Thu Jan  1 00:00:00 1970\n".equals(str))
  }

  @Test def localtime_rShouldTransformTheEpochToLocaltime(): Unit = {
    Zone { implicit z =>
      val time_ptr = stackalloc[time_t]
      !time_ptr = epoch + timezone
      val time: Ptr[tm] = localtime_r(time_ptr, alloc[tm])
      val cstr: CString = asctime_r(time, alloc[Byte](26))
      val str: String   = fromCString(cstr)

      assertTrue("Thu Jan  1 00:00:00 1970\n".equals(str))
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

  @Test def strftimeForJanOne1900ZeroZulu(): Unit = {
    Zone { implicit z =>
      val isoDatePtr: Ptr[CChar] = alloc[CChar](70)
      val timePtr                = alloc[tm]

      timePtr.tm_mday = 1

      strftime(isoDatePtr, 70.toULong, c"%FT%TZ", timePtr)

      val isoDateString: String = fromCString(isoDatePtr)

      assertTrue("1900-01-01T00:00:00Z".equals(isoDateString))
    }
  }

  @Test def strftimeForMondayJanOne1990ZeroTime(): Unit = {
    Zone { implicit z =>
      val timePtr             = alloc[tm]
      val datePtr: Ptr[CChar] = alloc[CChar](70)

      timePtr.tm_mday = 1
      timePtr.tm_wday = 1

      strftime(datePtr, 70.toULong, c"%A %c", timePtr)

      val dateString: String = fromCString(datePtr)
      assertTrue("Monday Mon Jan  1 00:00:00 1900".equals(dateString))
    }
  }

  @Test def strptimeDetectsInvalidFormat(): Unit = {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

      // %Q in format is invalid
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

  @Test def strptimeFor31December2016Time235960(): Unit = {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

      // A leap second was added at this time
      val result =
        strptime(c"December 31, 2016 23:59:60", c"%B %d, %Y %T", tmPtr)

      assertTrue(s"error: unexpected null returned", result != null)

      val expectedYear = 116
      assertTrue(s"tm_year: ${tmPtr.tm_year} != expected: ${expectedYear}",
                 tmPtr.tm_year == expectedYear)

      val expectedMonth = 11
      assertTrue(s"tm_mon: ${tmPtr.tm_mon} != expected: ${expectedMonth}",
                 tmPtr.tm_mon == expectedMonth)

      val expectedMday = 31
      assertTrue(s"tm_mon: ${tmPtr.tm_mday} != expected: ${expectedMday}",
                 tmPtr.tm_mday == expectedMday)

      val expectedHour = 23
      assertTrue(s"tm_mon: ${tmPtr.tm_hour} != expected: ${expectedHour}",
                 tmPtr.tm_hour == expectedHour)

      val expectedMin = 59
      assertTrue(s"tm_min: ${tmPtr.tm_min} != expected: ${expectedMin}",
                 tmPtr.tm_min == expectedMin)

      val expectedSec = 60
      assertTrue(s"tm_sec: ${tmPtr.tm_sec} != expected: ${expectedSec}",
                 tmPtr.tm_sec == expectedSec)

      val expectedIsdst = 0
      assertTrue(s"tm_isdst: ${tmPtr.tm_isdst} != expected: ${expectedIsdst}",
                 tmPtr.tm_isdst == expectedIsdst)
    }
  }

  @Test def strptimeExtraTextAfterDateStringIsOK(): Unit = {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

      val result =
        strptime(c"December 31, 2016 23:59:60 UTC", c"%B %d, %Y %T ", tmPtr)

      assertTrue(s"error: null returned", result != null)

      val expected = 'U'
      assertTrue(s"character: ${!result} != expected: ${expected}",
                 !result == expected)
    }
  }

}
