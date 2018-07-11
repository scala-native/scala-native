package scala.scalanative.posix

import scala.scalanative.native._

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
}
