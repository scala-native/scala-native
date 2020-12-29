package scala.scalanative.posix

import org.junit.Test
import org.junit.Assert._
import org.junit.BeforeClass

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import time._
import timeOps.tmOps

object TimeTest {
  private def validateStructTmDeclaredSize(): Unit = {
    // See comments at declaration site in time.scala for explanation
    // of magic number 56.
    val EXPECTED_SIZE = 56.toULong
    assertEquals("sizeof(struct tm)", EXPECTED_SIZE, sizeof[tm])
  }

  @BeforeClass def initialize(): Unit = {
    // Initialize C time environment before any calls to it.
    // localtime_r and other _r calls assume this has happened.
    tzset()

    // Test that the Scala Native size is large enough for C libs
    // which expect memory for the 11 field _BSD_SOURCE definition.
    //
    // Check before using to avoid painful and hard to debug cases of
    // reading and/or writing unallocated memory beyond the end of the struct.
    //
    // This validation could/should be a @Test but those seem to get run
    // in reverse order of declaration. Executing the code here removes
    // a sensitivity to execution order and makes evident both that it
    // exists and when it should be run.

    validateStructTmDeclaredSize()
  }
}

class TimeTest {
  // In 2.11/2.12 time was resolved to posix.time.type, in 2.13 to
  // posix.time.time method
  val now_time_t: time_t = scala.scalanative.posix.time.time(null)
  val epoch: time_t      = 0L

  @Test def asctimeWithGivenKnownStateShouldMatchItsRepresentation(): Unit = {
    Zone { implicit z =>
      val anno_zero_ptr = alloc[tm]
      anno_zero_ptr.tm_mday = 1
      anno_zero_ptr.tm_wday = 1
      val cstr: CString = asctime(anno_zero_ptr)
      val str: String   = fromCString(cstr)
      assertEquals("Mon Jan  1 00:00:00 1900\n", str)
    }
  }

  @Test def asctime_rWithGivenKnownStateShouldMatchItsRepresentation(): Unit = {
    Zone { implicit z =>
      val anno_zero_ptr = alloc[tm]
      anno_zero_ptr.tm_mday = 1
      anno_zero_ptr.tm_wday = 1
      val cstr: CString = asctime_r(anno_zero_ptr, alloc[Byte](26))
      val str: String   = fromCString(cstr)
      assertEquals("Mon Jan  1 00:00:00 1900\n", str)
    }
  }

  @Test def localtimeShouldTransformTheEpochToLocaltime(): Unit = {
    val time_ptr = stackalloc[time_t]
    !time_ptr = epoch + timezone
    val time: Ptr[tm] = localtime(time_ptr)
    val cstr: CString = asctime(time)
    val str: String   = fromCString(cstr)

    assertEquals("Thu Jan  1 00:00:00 1970\n", str)
  }

  @Test def localtime_rShouldTransformTheEpochToLocaltime(): Unit = {
    Zone { implicit z =>
      val time_ptr = stackalloc[time_t]
      !time_ptr = epoch + timezone
      val time: Ptr[tm] = localtime_r(time_ptr, alloc[tm])
      val cstr: CString = asctime_r(time, alloc[Byte](26))
      val str: String   = fromCString(cstr)

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

  @Test def strftimeForJanOne1900ZeroZulu(): Unit = {
    Zone { implicit z =>
      val isoDatePtr: Ptr[CChar] = alloc[CChar](70)
      val timePtr                = alloc[tm]

      timePtr.tm_mday = 1

      strftime(isoDatePtr, 70.toULong, c"%FT%TZ", timePtr)

      val isoDateString: String = fromCString(isoDatePtr)
      assertEquals("1900-01-01T00:00:00Z", isoDateString)
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
      assertEquals("Monday Mon Jan  1 00:00:00 1900", dateString)
    }
  }

  @Test def strptimeDetectsInvalidFormat(): Unit = {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

      // %Q in format is invalid
      val result =
        strptime(c"December 31, 2016 23:59:60", c"%B %d, %Y %Q", tmPtr)

      assertNull(result)
    }
  }

  @Test def strptimeDetectsInvalidString(): Unit = {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

      // 32 in string is invalid
      val result =
        strptime(c"December 32, 2016 23:59:60", c"%B %d, %Y %T", tmPtr)

      assertNull(result)
    }
  }

  @Test def strptimeDetectsStringShorterThanFormat(): Unit = {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

      val result =
        strptime(c"December 32, 2016 23:59", c"%B %d, %Y %T", tmPtr)

      assertNull(result)
    }
  }

  @Test def strptimeFor31December2016Time235960(): Unit = {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

      // A leap second was added at this time
      val result =
        strptime(c"December 31, 2016 23:59:60", c"%B %d, %Y %T", tmPtr)

      assertNotNull(s"error: unexpected null returned", result)

      val expectedYear = 116
      assertEquals("tm_year", tmPtr.tm_year, expectedYear)

      val expectedMonth = 11
      assertEquals("tm_mon", tmPtr.tm_mon, expectedMonth)

      val expectedMday = 31
      assertEquals(s"tm_mday", tmPtr.tm_mday, expectedMday)

      val expectedHour = 23
      assertEquals(s"tm_hour", tmPtr.tm_hour, expectedHour)

      val expectedMin = 59
      assertEquals(s"tm_min", tmPtr.tm_min, expectedMin)

      val expectedSec = 60
      assertEquals(s"tm_sec", tmPtr.tm_sec, expectedSec)

      val expectedIsdst = 0
      assertEquals(s"tm_isdst", tmPtr.tm_isdst, expectedIsdst)
    }
  }

  @Test def strptimeExtraTextAfterDateStringIsOK(): Unit = {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

      val result =
        strptime(c"December 31, 2016 23:59:60 UTC", c"%B %d, %Y %T ", tmPtr)

      assertNotNull("strptime", result)

      val expected = 'U'
      assertNotEquals("result", result, expected)
    }
  }
}
