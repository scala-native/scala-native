package scala.scalanative.native

import scala.scalanative.native.time.{
  asctime,
  difftime,
  localtime,
  mktime,
  strftime,
  wcsftime,
  gmtime,
  time_t,
  tm
}
import scala.scalanative.native.timeOps.tmOps

object TimeSuite extends tests.Suite {

  val now_ptr: Ptr[time_t] = stackalloc[time_t]
  val now_time_t: time_t   = time.time(now_ptr)

  test("asctime() with a given known state should match its representation") {

    val anno_zero_ptr: Ptr[tm] = stackalloc[tm]
    anno_zero_ptr.tm_sec = 0
    anno_zero_ptr.tm_min = 0
    anno_zero_ptr.tm_hour = 0
    anno_zero_ptr.tm_mday = 1
    anno_zero_ptr.tm_mon = 0
    anno_zero_ptr.tm_year = 0
    anno_zero_ptr.tm_wday = 0
    anno_zero_ptr.tm_yday = 0
    anno_zero_ptr.tm_isdst = 0

    val cstr: CString = asctime(anno_zero_ptr)
    val str: String   = fromCString(cstr)
    assert("Sun Jan  1 00:00:00 1900\n".equals(str))
  }

  test("localtime() should transform day zero time to localtime (+0100=CET)") {

    val anno_zero_ptr: Ptr[tm] = stackalloc[tm]
    anno_zero_ptr.tm_sec = 0
    anno_zero_ptr.tm_min = 0
    anno_zero_ptr.tm_hour = 0
    anno_zero_ptr.tm_mday = 1
    anno_zero_ptr.tm_mon = 0
    anno_zero_ptr.tm_year = 0
    anno_zero_ptr.tm_wday = 0
    anno_zero_ptr.tm_yday = 0
    anno_zero_ptr.tm_isdst = 0

    val theTime: time_t = mktime(anno_zero_ptr)
    val time_ptr        = stackalloc[time_t]
    !time_ptr = theTime
    val time: Ptr[tm] = localtime(time_ptr)
    val cstr: CString = asctime(time)
    val str: String   = fromCString(cstr)

    assert("Thu Jan  1 00:59:59 1970\n".equals(str))
  }

  test(
    "difftime() between epoch and now should be bigger than the timestamp when I wrote this code") {

    val anno_zero_ptr: Ptr[tm] = stackalloc[tm]
    anno_zero_ptr.tm_sec = 0
    anno_zero_ptr.tm_min = 0
    anno_zero_ptr.tm_hour = 0
    anno_zero_ptr.tm_mday = 1
    anno_zero_ptr.tm_mon = 0
    anno_zero_ptr.tm_year = 0
    anno_zero_ptr.tm_wday = 0
    anno_zero_ptr.tm_yday = 0
    anno_zero_ptr.tm_isdst = 0
    val anno_zero_time_t: time_t = mktime(anno_zero_ptr)

    val diff: time_t = difftime(now_time_t, anno_zero_time_t)
    assert(diff > 1502752688)
  }

  test("time() should be bigger than the timestamp when I wrote this code") {
    // arbitrary date set at the time when I was writing this.
    assert(now_time_t > 1502752688)
  }

  test("strftime() for 1900-01-00T00:00:00Z") {
    val isoDatePtr: Ptr[CChar] = stackalloc[CChar](70)
    val timePtr: Ptr[tm]       = stackalloc[tm]
    timePtr.tm_sec = 0
    timePtr.tm_min = 0
    timePtr.tm_hour = 0
    timePtr.tm_mday = 0
    timePtr.tm_mon = 0
    timePtr.tm_year = 0
    timePtr.tm_wday = 0
    timePtr.tm_yday = 0
    timePtr.tm_isdst = 0

    strftime(isoDatePtr, 70, c"%FT%TZ", timePtr)

    val isoDateString: String = fromCString(isoDatePtr)

    assert("1900-01-00T00:00:00Z".equals(isoDateString))
  }

  test("wcsftime() not implemented yet. Waiting for fromWideChar") {
    val isoDatePtr: Ptr[CWideChar] = stackalloc[CWideChar](70)
    val timePtr: Ptr[tm]           = stackalloc[tm]
    timePtr.tm_sec = 0
    timePtr.tm_min = 0
    timePtr.tm_hour = 0
    timePtr.tm_mday = 0
    timePtr.tm_mon = 0
    timePtr.tm_year = 0
    timePtr.tm_wday = 0
    timePtr.tm_yday = 0
    timePtr.tm_isdst = 0
  }

  test("gmtime()") {
    val datePtr: Ptr[CChar] = stackalloc[CChar](70)

    val timePtr: Ptr[tm] = stackalloc[tm]
    timePtr.tm_sec = 0
    timePtr.tm_min = 0
    timePtr.tm_hour = 0
    timePtr.tm_mday = 0
    timePtr.tm_mon = 0
    timePtr.tm_year = 0
    timePtr.tm_wday = 0
    timePtr.tm_yday = 0
    timePtr.tm_isdst = 0

    strftime(datePtr, 70, c"%A %c", timePtr)

    val dateString: String = fromCString(datePtr)
    assert("Sunday Sun Jan  0 00:00:00 1900".equals(dateString))
  }

}
