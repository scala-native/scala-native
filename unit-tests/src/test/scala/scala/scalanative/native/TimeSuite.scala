package scala.scalanative.native

import scala.scalanative.native.time.{
  asctime,
  daylight,
  difftime,
  gmtime,
  localtime,
  mktime,
  strftime,
  strptime,
  time_t,
  timezone,
  tm,
  tzname,
  tzset,
  wcsftime
}

import scala.scalanative.native.timeOps.tmOps

import scala.scalanative.native.stdlib.{getenv, setenv, unsetenv}
import scala.scalanative.native.string.{memset, strcmp}

object TimeSuite extends tests.Suite {

  val now_ptr: Ptr[time_t] = stackalloc[time_t]
  val now_time_t: time_t   = time.time(now_ptr)

  var tzEnvVarName     = c"TZ"
  var savedTZ: CString = _

  /* TimeSuite must execute in a single threaded environment. The only
   * sure way to meet this requirement is for the enclosing testing
   * environment to guarantee it.
   *
   * The four functions in the suite (localtime & friends) are defined
   * as returning pointers to static buffers and THREAD_UNSAFE!.  In
   * addition, this code possibly/probably changes the environment variable
   * TZ for the duration of the suite run. Any other thread running in
   * the same process may pick up a bogus, to it, value for TZ.
   *
   * A runtime test is possible here (stat /proc/self/task. If it
   * is successful and nlinks == 3, then process is single threaded, at
   * moment of testing). This test however is a weak one, because without
   * a guarantee from the testing environment, a new thread can be created
   * at any time after the initial test.
   */

  test("save unknown current TZ, set to known Central European Time (CET)") {

    savedTZ = getenv(tzEnvVarName)

    val result = setenv(tzEnvVarName, c"CET-1CEST,M3.5.0,M10.5.0", 1)

    assert(result == 0)

    tzset()
  }

  test("tzname(0) should now be CET & tzname(1) should be CEST") {
    // must be early in file but after tzset() had been called, either
    // directly or indirectly (mktime).
    assert(strcmp(tzname(0), c"CET") == 0)
    assert(strcmp(tzname(1), c"CEST") == 0)
  }

  test("timezone & daylight variables for CEST should be valid") {
    // must be after tzname test, preferably as immediately next test.
    val cetOffset  = -1 * (60 * 60)
    val cestOffset = cetOffset * 2

    assert((timezone == cetOffset) || (timezone == cestOffset))
    assert((daylight == 0) || (daylight == 1))
  }

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

  test("localtime() should convert Unix Epoch to expected") {

    val unix_epoch: time_t = 0

    val time_ptr = stackalloc[time_t]

    !time_ptr = unix_epoch

    // TZ set to known "CET" (Central European Time) on entry to Suite above.

    val time: Ptr[tm] = localtime(time_ptr)
    val cstr: CString = asctime(time)
    val str: String   = fromCString(cstr)

    assert("Thu Jan  1 01:00:00 1970\n".equals(str))
  }

  test("strptime() should convert 1969-07-21T02:56:15 UTC to expected") {

    // A human, Neil Armstrong, first stepped onto the Moon at
    // 1969-07-21T02:56:15 UTC.

    val tmPtr: Ptr[tm] = stackalloc[tm]

    memset(tmPtr.asInstanceOf[Ptr[Byte]], 0, sizeof[tm])

    val result =
      strptime(c"1969-07-21T02:56:15 UTC", c"%Y-%m-%dT%H:%M:%S", tmPtr);

    assertNotNull(result)

    assert(tmPtr.tm_sec == 15)
    assert(tmPtr.tm_min == 56)
    assert(tmPtr.tm_hour == 2)
    assert(tmPtr.tm_mday == 21)
    assert(tmPtr.tm_mon == 6)
    assert(tmPtr.tm_year == (1969 - 1900))
    assert(tmPtr.tm_wday == 1)
    assert(tmPtr.tm_yday == 201)
    assert(tmPtr.tm_isdst == 0) // Never dst in UTC
  }

  test("mktime() should convert 1969-07-21T04:56:15 CEST to expected") {

    val tmPtr: Ptr[tm] = stackalloc[tm]

    memset(tmPtr.asInstanceOf[Ptr[Byte]], 0, sizeof[tm])

    val result =
      strptime(c"1969-07-21T02:56:15 UTC", c"%Y-%m-%dT%H:%M:%S", tmPtr);

    assertNotNull(result)

    tmPtr.tm_hour += 2 // Convert from UTC to CEST

    val utcOffset = mktime(tmPtr)

    assert(utcOffset == -14155425) // negative because tmPtr earlier than Epoch
  }

  test(
    "difftime(1900-01-01, now) should be > time this test was written") {

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

    val anno_zero_time_t = mktime(anno_zero_ptr)

    val diff = difftime(now_time_t, anno_zero_time_t)

    assert(diff.toLong > 1502752688L) // Use Long to prevent sign flip
  }

  test("time() should be bigger than the timestamp when I wrote this code") {
    // arbitrary date set at the time when I was writing this.
    assert(now_time_t > 1502752688)
  }

  test("strftime() should convert 1900-01-01T00:00:00Z to expected") {

    val isoDateBuf: Ptr[CChar] = stackalloc[CChar](70)
    val timePtr: Ptr[tm]       = stackalloc[tm]
    timePtr.tm_sec = 0
    timePtr.tm_min = 0
    timePtr.tm_hour = 0
    timePtr.tm_mday = 1
    timePtr.tm_mon = 0
    timePtr.tm_year = 0
    timePtr.tm_wday = 0
    timePtr.tm_yday = 0
    timePtr.tm_isdst = 0

    strftime(isoDateBuf, 70, c"%FT%TZ", timePtr)

    val isoDateString = fromCString(isoDateBuf)

    assert("1900-01-01T00:00:00Z".equals(isoDateString))
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

  test("gmtime() should convert 2000-01-01T00:01:02 CET to expected") {

    // mktime() using TZ of CET set on entry to suite.
    // so tm_hour for Month 0 (January) is +1.
    // No CEST in January.

    // Chose a year, day, & hour, 2000-01-01T00:01:02 CET, which will
    // trigger plenty of change/havoc.
    // tm_hour, tm_mday, tm_mon, tm_year, tm_wday, & tm_yday all change.
    // Non-zero tm_min and tm_sec values are used to exercise something other
    // than zero, which has previously been used.

    val tmPtr: Ptr[tm] = stackalloc[tm]

    // Single point of truth
    val tm_sec   = 2
    val tm_min   = 1
    val tm_hour  = 0 // so -1 for UTC will be in previous day, month, year
    val tm_mday  = 1
    val tm_mon   = 0
    val tm_year  = 2000 - 1900
    val tm_wday  = 6 // Saturday
    val tm_yday  = 0
    val tm_isdst = 0

    tmPtr.tm_sec = tm_sec
    tmPtr.tm_min = tm_min
    tmPtr.tm_hour = tm_hour
    tmPtr.tm_mday = tm_mday
    tmPtr.tm_mon = tm_mon
    tmPtr.tm_year = tm_year
    tmPtr.tm_wday = tm_wday
    tmPtr.tm_yday = tm_yday
    tmPtr.tm_isdst = tm_isdst

    val havocTime = mktime(tmPtr)

    val havocPtr = stackalloc[time_t]

    !havocPtr = havocTime

    val gmtTmPtr = gmtime(havocPtr)

    // These stay the same
    assert(gmtTmPtr.tm_sec == tm_sec)
    assert(gmtTmPtr.tm_min == tm_min)
    assert(gmtTmPtr.tm_isdst == tm_isdst)

    // These change
    assert(gmtTmPtr.tm_hour == 23)
    assert(gmtTmPtr.tm_mday == 31)
    assert(gmtTmPtr.tm_mon == 11)

    assert(gmtTmPtr.tm_year == (tm_year - 1))
    assert(gmtTmPtr.tm_wday == 5)
    assert(gmtTmPtr.tm_yday == 364)
  }

  test("restore saved TZ") {

    // This test must come last.

    val result = if (savedTZ == null) {
      unsetenv(tzEnvVarName)
    } else {
      setenv(tzEnvVarName, savedTZ, 1)
    }

    assert(result == 0)

    tzset();
  }

}
