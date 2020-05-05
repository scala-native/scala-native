package scala.scalanative.posix

import scala.scalanative.unsafe._

import time._
import timeOps.tmOps

object TimeSuite extends tests.Suite {
  tzset()
  val now_time_t: time_t = time.time(null)
  val epoch: time_t      = 0

  test("asctime() with a given known state should match its representation") {
    Zone { implicit z =>
      val anno_zero_ptr = alloc[tm]
      anno_zero_ptr.tm_mday = 1
      anno_zero_ptr.tm_wday = 1
      val cstr: CString = asctime(anno_zero_ptr)
      val str: String   = fromCString(cstr)
      assert("Mon Jan  1 00:00:00 1900\n".equals(str))
    }
  }

  test("asctime_r() with a given known state should match its representation") {
    Zone { implicit z =>
      val anno_zero_ptr = alloc[tm]
      anno_zero_ptr.tm_mday = 1
      anno_zero_ptr.tm_wday = 1
      val cstr: CString = asctime_r(anno_zero_ptr, alloc[Byte](26))
      val str: String   = fromCString(cstr)
      assert("Mon Jan  1 00:00:00 1900\n".equals(str))
    }
  }

  test("localtime() should transform the epoch to localtime") {
    val time_ptr = stackalloc[time_t]
    !time_ptr = epoch + timezone
    val time: Ptr[tm] = localtime(time_ptr)
    val cstr: CString = asctime(time)
    val str: String   = fromCString(cstr)

    assert("Thu Jan  1 00:00:00 1970\n".equals(str))
  }

  test("localtime_r() should transform the epoch to localtime") {
    Zone { implicit z =>
      val time_ptr = stackalloc[time_t]
      !time_ptr = epoch + timezone
      val time: Ptr[tm] = localtime_r(time_ptr, alloc[tm])
      val cstr: CString = asctime_r(time, alloc[Byte](26))
      val str: String   = fromCString(cstr)

      assert("Thu Jan  1 00:00:00 1970\n".equals(str))
    }
  }

  test(
    "difftime() between epoch and now should be bigger than the timestamp when I wrote this code") {
    assert(difftime(now_time_t, epoch) > 1502752688)
  }

  test("time() should be bigger than the timestamp when I wrote this code") {
    // arbitrary date set at the time when I was writing this.
    assert(now_time_t > 1502752688)
  }

  test("strftime() for 1900-01-01T00:00:00Z") {
    Zone { implicit z =>
      val isoDatePtr: Ptr[CChar] = alloc[CChar](70)
      val timePtr                = alloc[tm]

      timePtr.tm_mday = 1

      strftime(isoDatePtr, 70, c"%FT%TZ", timePtr)

      val isoDateString: String = fromCString(isoDatePtr)

      assert("1900-01-01T00:00:00Z".equals(isoDateString))
    }
  }

  test("strftime() for Monday Mon Jan  1 00:00:00 1900") {
    Zone { implicit z =>
      val timePtr             = alloc[tm]
      val datePtr: Ptr[CChar] = alloc[CChar](70)

      timePtr.tm_mday = 1
      timePtr.tm_wday = 1

      strftime(datePtr, 70, c"%A %c", timePtr)

      val dateString: String = fromCString(datePtr)
      assert("Monday Mon Jan  1 00:00:00 1900".equals(dateString))
    }
  }

  test(s"strptime() - detect invalid format") {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

      // %Q in format is invalid
      val result =
        strptime(c"December 31, 2016 23:59:60", c"%B %d, %Y %Q", tmPtr)

      assert(result == null, s"expected null result, got pointer")
    }
  }

  test(s"strptime() - detect invalid string") {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

      // 32 in string is invalid
      val result =
        strptime(c"December 32, 2016 23:59:60", c"%B %d, %Y %T", tmPtr)

      assert(result == null, s"expected null result, got pointer")
    }
  }

  test(s"strptime() - detect string shorter than format") {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

      val result =
        strptime(c"December 32, 2016 23:59", c"%B %d, %Y %T", tmPtr)

      assert(result == null, s"expected null result, got pointer")
    }
  }

  test("strptime() for December 31, 2016 23:59:60") {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

      // A leap second was added at this time
      val result =
        strptime(c"December 31, 2016 23:59:60", c"%B %d, %Y %T", tmPtr)

      assert(result != null, s"error: unexpected null returned")

      val expectedYear = 116
      assert(tmPtr.tm_year == expectedYear,
             s"tm_year: ${tmPtr.tm_year} != expected: ${expectedYear}")

      val expectedMonth = 11
      assert(tmPtr.tm_mon == expectedMonth,
             s"tm_mon: ${tmPtr.tm_mon} != expected: ${expectedMonth}")

      val expectedMday = 31
      assert(tmPtr.tm_mday == expectedMday,
             s"tm_mon: ${tmPtr.tm_mday} != expected: ${expectedMday}")

      val expectedHour = 23
      assert(tmPtr.tm_hour == expectedHour,
             s"tm_mon: ${tmPtr.tm_hour} != expected: ${expectedHour}")

      val expectedMin = 59
      assert(tmPtr.tm_min == expectedMin,
             s"tm_min: ${tmPtr.tm_min} != expected: ${expectedMin}")

      val expectedSec = 60
      assert(tmPtr.tm_sec == expectedSec,
             s"tm_sec: ${tmPtr.tm_sec} != expected: ${expectedSec}")

      val expectedIsdst = 0
      assert(tmPtr.tm_isdst == expectedIsdst,
             s"tm_isdst: ${tmPtr.tm_isdst} != expected: ${expectedIsdst}")
    }
  }

  test("strptime() -- extra text after date string is OK") {
    Zone { implicit z =>
      val tmPtr = alloc[tm]

      val result =
        strptime(c"December 31, 2016 23:59:60 UTC", c"%B %d, %Y %T ", tmPtr)

      assert(result != null, s"error: null returned")

      val expected = 'U'
      assert(!result == expected,
             s"character: ${!result} != expected: ${expected}")
    }
  }

}
