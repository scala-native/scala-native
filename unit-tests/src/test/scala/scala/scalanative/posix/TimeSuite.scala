package scala.scalanative.posix

import scalanative.libc.{errno => libcErrno, string}
import scalanative.unsafe._
import java.io.IOException

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

  test("strftime() should not read memory outside struct tm") {
    Zone { implicit z =>
      if (sizeof[tm] < 56) {

        val ttPtr = alloc[time_t]
        !ttPtr = 1490986064740L / 1000L // Fri Mar 31 14:47:44 EDT 2017

        // This code is testing for reading past the end of a "short"
        // Scala Native tm, so the linux 56 byte form is necessary here.
        val tmBufSize = 7
        val tmBuf     = alloc[Ptr[Byte]](tmBufSize) // will zero/clear all bytes
        val tmPtr     = tmBuf.asInstanceOf[Ptr[tm]]

        if (localtime_r(ttPtr, tmPtr) == null) {
          throw new IOException(fromCString(string.strerror(libcErrno.errno)))
        } else {
          val unexpected = "BOGUS"

          // With the "short" 36 byte SN struct tm tmBuf(6) is
          // is linux tm_zone, and outside the posix minimal required range.
          // strftime() should not read it.

          tmBuf(6) = toCString(unexpected)

          val bufSize = 70 // easier to grossly overprovision than to chase bugs
          val buf     = alloc[Byte](bufSize) // will zero/clear all bytes
          val n       = strftime(buf, bufSize, c"%a %b %d %T %Z %Y", tmPtr)

          // strftime does not set errno on error
          assert(n != 0, s"unexpected zero from strftime")

          val result = fromCString(buf)

          assert(
            result.indexOf(unexpected, "Fri Mar 31 14:47:44 ".length) == -1,
            s"result: '${result}' contains unexpected ${unexpected}")

          val regex = "[A-Z][a-z]{2} [A-Z][a-z]{2} " +
            "\\d\\d \\d{2}:\\d{2}:\\d{2} [A-Z]{2,5} 20[1-3]\\d"

          assert(
            result.matches(regex),
            s"result: '${result}' does not match expected regex: '${regex}'")
          assert(false, s"result is : '${result}'")
        }
      }
    }
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

  test("strftime() for Monday Jan  1 00:00:00 1900") {
    Zone { implicit z =>
      val timePtr             = alloc[tm]
      val datePtr: Ptr[CChar] = alloc[CChar](70)

      timePtr.tm_mday = 1
      timePtr.tm_wday = 1

      strftime(datePtr, 70, c"%A %b %e %T %Y", timePtr)

      val expected           = "Monday Jan  1 00:00:00 1900"
      val dateString: String = fromCString(datePtr)
      assert(expected.equals(dateString),
             s"result: ${dateString} != expected: ${expected}")
    }
  }

  test("strftime() for Fri Mar 31 14:47:44 EDT 2017") {
    Zone { implicit z =>
      val ttPtr = alloc[time_t]
      !ttPtr = 1490986064740L / 1000L
      val tmPtr = alloc[tm]

      if (localtime_r(ttPtr, tmPtr) == null) {
        throw new IOException(fromCString(string.strerror(libcErrno.errno)))
      } else {
        val bufSize = 70 // easier to grossly overprovision than to chase bugs
        val buf     = alloc[Byte](bufSize)

        val n = strftime(buf, bufSize, c"%a %b %d %T %Z %Y", tmPtr)

        // strftime does not set errno on error					      assert(n != 0, s"unexpected zero from strftime")

        val result = fromCString(buf)

        // Travis CI currently reports its timezone name as the
        // apparently bogus "db", which is supposed to force UTC.
        // Somebody apparently forgot to tell strftime %Z that.
        // It reports "db".

        // JVM Date.toString day-of-month always has two digits [01,31].

        val expected = "[A-Z][a-z]{2} [A-Z][a-z]{2} " +
          "\\d\\d \\d{2}:\\d{2}:\\d{2} [A-Z]{2,5} 20[1-3]\\d"

        assert(
          result.matches(expected),
          s"result: '${result}' does not match expected regex: '${expected}'")
      }
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

  test("strptime() should not write memory outside struct tm") {
    Zone { implicit z =>
      // Linux 56 Bytes, Posix specifies 36 but allows more.
      val tmBufSize = 7
      val tmBuf     = alloc[Ptr[Byte]](tmBufSize) // will zero/clear all bytes
      val tmPtr     = tmBuf.asInstanceOf[Ptr[tm]]

      val cp =
        strptime(c"Fri Mar 31 14:47:44 EDT 2017", c"%a %b %d %T %Z %Y", tmPtr)

      assert(cp != null, s"strptime() returned unexpected null pointer")

      val ch = cp(0)
      assert(ch == '\0',
             s"strptime() returned unexpected non-null character: ${ch}")

      val tm_gmtoff = tmBuf(5) // tm_gmtoff is outside posix minimal range.
      assert(tm_gmtoff == null, s"tm_gmtoff: ${tm_gmtoff} != expected: 0")

      val tm_zone = tmBuf(6) // tm_zone is outside posix minimal range.
      assert(tm_zone == null, s"tm_zone: ${tm_zone} != expected: null")

      // Major concerning conditions passed. Sanity check the tm proper.

      val expectedSec = 44
      assert(tmPtr.tm_sec == expectedSec,
             s"tm_sec: ${tmPtr.tm_sec} != expected: ${expectedSec}")

      val expectedMin = 47
      assert(tmPtr.tm_min == expectedMin,
             s"tm_min: ${tmPtr.tm_min} != expected: ${expectedMin}")

      val expectedHour = 14
      assert(tmPtr.tm_hour == expectedHour,
             s"tm_mon: ${tmPtr.tm_hour} != expected: ${expectedHour}")

      val expectedMday = 31
      assert(tmPtr.tm_mday == expectedMday,
             s"tm_mon: ${tmPtr.tm_mday} != expected: ${expectedMday}")

      val expectedMonth = 2
      assert(tmPtr.tm_mon == expectedMonth,
             s"tm_mon: ${tmPtr.tm_mon} != expected: ${expectedMonth}")

      val expectedYear = 117
      assert(tmPtr.tm_year == expectedYear,
             s"tm_year: ${tmPtr.tm_year} != expected: ${expectedYear}")

      val expectedWday = 5
      assert(tmPtr.tm_wday == expectedWday,
             s"tm_wday: ${tmPtr.tm_wday} != expected: ${expectedWday}")

      val expectedYday = 89
      assert(tmPtr.tm_yday == expectedYday,
             s"tm_yday: ${tmPtr.tm_yday} != expected: ${expectedYday}")

      // strptime() parses %Z but does not set corresponding field.
      // Daylight saving time in most of the USA started March 12, 2017,
      // so this would be a 1 if the field were being set.
      val expectedIsdst = 0
      assert(tmPtr.tm_isdst == expectedIsdst,
             s"tm_isdst: ${tmPtr.tm_isdst} != expected: ${expectedIsdst}")
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
