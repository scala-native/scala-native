package scala.scalanative
package posix

import scala.scalanative.posix.signal.sigevent
import scala.scalanative.posix.sys.types
import scala.scalanative.unsafe._

import types._

// XSI comment before method indicates it is defined in
// extended POSIX X/Open System Interfaces, not base POSIX.

@extern object time extends time

@extern
@define("__SCALANATIVE_POSIX_TIME")
trait time extends libc.time {

  type clock_t = types.clock_t
  type clockid_t = types.clockid_t

  type locale_t = locale.locale_t

  /* NULL is required by the POSIX standard but is not directly implemented
   * here. It is implemented in posix/stddef.scala.
   *
   * There is no good way to import stddef.scala NULL in an @extern
   * object, such as this.
   *
   * The idiomatic scala 'null' is more likely to be used in scala files.
   */

  type pid_t = types.pid_t
  type size_t = types.size_t

  type time_t = types.time_t
  type timer_t = types.timer_t

  type timespec = CStruct2[
    time_t, // tv_sec
    CLong // tv_nsec
  ]

  // Open Group Issue 8, 2024 definition
  // Keep in sync with posix/time.scala
  type tm = CStruct11[
    CInt, // tm_sec
    CInt, // tm_min
    CInt, // tm_hour
    CInt, // tm_mday
    CInt, // tm_mon
    CInt, // tm_year
    CInt, // tm_wday
    CInt, // tm_yday
    CInt, // tm_isdst
    CLongLong, // tm_gmtoff  // Open Group Issue 8, 2024, "seconds EAST of UTC"
    CString // tm_zone // Open Group Issue 8, 2024, "Timezone abbreviation"
  ]

  // See separate timer object below for itimerspec type and timer_*() methods.

  /* Some methods here have a @name annotation and some do not.
   * Methods where a @name extern "glue" layer would simply pass through
   * the arguments or return value do not need that layer & its
   * annotation.
   *
   * time_t is a simple type, not a structure, so it does not need to be
   * transformed. Ptr also does not need to be transformed.
   *
   * _Static_assert code now in time.c checks the match of scalanative
   * structures such as timespec and tm with the operating system definition.
   * "timer_*" use this assurance to avoid "glue".
   *
   * Methods taking or returning time_t, directly or through timespec, go
   * through "glue" anyway: NetBSD headers rename them to their 64-bit
   * time_t versions (e.g. time -> __time50). Calling the plain symbol from
   * Scala would bind to the compatibility version using 32-bit time_t.
   */

  def asctime(time_ptr: Ptr[tm]): CString = extern
  def asctime_r(time_ptr: Ptr[tm], buf: Ptr[CChar]): CString = extern

  def clock(): clock_t = extern
  @name("scalanative_clock_getres")
  def clock_getres(clockid: clockid_t, res: Ptr[timespec]): CInt = extern

  @deprecated(
    "Binary compatibility: code compiled against 0.5.12 links to this extern name.",
    "0.5.13"
  )
  @name("clock_getres")
  private[posix] def _clock_getres(
      clockid: clockid_t,
      res: Ptr[timespec]
  ): CInt =
    externRedirect(clock_getres(clockid, res))

  @name("scalanative_clock_gettime")
  def clock_gettime(clockid: clockid_t, tp: Ptr[timespec]): CInt = extern

  @deprecated(
    "Binary compatibility: code compiled against 0.5.12 links to this extern name.",
    "0.5.13"
  )
  @name("clock_gettime")
  private[posix] def _clock_gettime(
      clockid: clockid_t,
      tp: Ptr[timespec]
  ): CInt = externRedirect(clock_gettime(clockid, tp))

  // No clock_nanosleep on macOS. time.c provides a stub always returning -1.
  @name("scalanative_clock_nanosleep")
  @blocking
  def clock_nanosleep(
      clockid: clockid_t,
      flags: CInt,
      request: Ptr[timespec],
      remain: Ptr[timespec]
  ): CInt = extern

  @name("scalanative_clock_settime")
  def clock_settime(clockid: clockid_t, tp: Ptr[timespec]): CInt = extern

  @deprecated(
    "Binary compatibility: code compiled against 0.5.12 links to this extern name.",
    "0.5.13"
  )
  @name("clock_settime")
  private[posix] def _clock_settime(
      clockid: clockid_t,
      tp: Ptr[timespec]
  ): CInt =
    externRedirect(clock_settime(clockid, tp))

  @name("scalanative_ctime")
  def ctime(time: Ptr[time_t]): CString = extern

  @deprecated(
    "Binary compatibility: code compiled against 0.5.12 links to this extern name.",
    "0.5.13"
  )
  @name("ctime")
  private[posix] def _ctime(time: Ptr[time_t]): CString =
    externRedirect(ctime(time))

  @name("scalanative_ctime_r")
  def ctime_r(time: Ptr[time_t], buf: Ptr[CChar]): CString = extern

  @deprecated(
    "Binary compatibility: code compiled against 0.5.12 links to this extern name.",
    "0.5.13"
  )
  @name("ctime_r")
  private[posix] def _ctime_r(time: Ptr[time_t], buf: Ptr[CChar]): CString =
    externRedirect(ctime_r(time, buf))

  @name("scalanative_difftime")
  def difftime(time_end: CLong, time_beg: CLong): CDouble = extern

  @deprecated(
    "Binary compatibility: code compiled against 0.5.12 links to this extern name.",
    "0.5.13"
  )
  @name("difftime")
  private[posix] def _difftime(time_end: CLong, time_beg: CLong): CDouble =
    externRedirect(difftime(time_end, time_beg))

  @name("scalanative_gmtime")
  def gmtime(time: Ptr[time_t]): Ptr[tm] = extern

  @deprecated(
    "Binary compatibility: code compiled against 0.5.12 links to this extern name.",
    "0.5.13"
  )
  @name("gmtime")
  private[posix] def _gmtime(time: Ptr[time_t]): Ptr[tm] =
    externRedirect(gmtime(time))

  @name("scalanative_gmtime_r")
  def gmtime_r(time: Ptr[time_t], tm: Ptr[tm]): Ptr[tm] = extern

  @deprecated(
    "Binary compatibility: code compiled against 0.5.12 links to this extern name.",
    "0.5.13"
  )
  @name("gmtime_r")
  private[posix] def _gmtime_r(time: Ptr[time_t], tm: Ptr[tm]): Ptr[tm] =
    externRedirect(gmtime_r(time, tm))

  @name("scalanative_localtime")
  def localtime(time: Ptr[time_t]): Ptr[tm] = extern

  @deprecated(
    "Binary compatibility: code compiled against 0.5.12 links to this extern name.",
    "0.5.13"
  )
  @name("localtime")
  private[posix] def _localtime(time: Ptr[time_t]): Ptr[tm] =
    externRedirect(localtime(time))

  @name("scalanative_localtime_r")
  def localtime_r(time: Ptr[time_t], tm: Ptr[tm]): Ptr[tm] = extern

  @deprecated(
    "Binary compatibility: code compiled against 0.5.12 links to this extern name.",
    "0.5.13"
  )
  @name("localtime_r")
  private[posix] def _localtime_r(time: Ptr[time_t], tm: Ptr[tm]): Ptr[tm] =
    externRedirect(localtime_r(time, tm))

  @name("scalanative_mktime")
  def mktime(time: Ptr[tm]): time_t = extern

  @deprecated(
    "Binary compatibility: code compiled against 0.5.12 links to this extern name.",
    "0.5.13"
  )
  @name("mktime")
  private[posix] def _mktime(time: Ptr[tm]): time_t =
    externRedirect(mktime(time))

  @name("scalanative_nanosleep")
  @blocking
  def nanosleep(
      requested: Ptr[timespec],
      remaining: Ptr[timespec]
  ): CInt = extern

  @deprecated(
    "Binary compatibility: code compiled against 0.5.12 links to this extern name.",
    "0.5.13"
  )
  @name("nanosleep")
  private[posix] def _nanosleep(
      requested: Ptr[timespec],
      remaining: Ptr[timespec]
  ): CInt = externRedirect(nanosleep(requested, remaining))

//  @name("scalanative_strftime")
  def strftime(
      str: Ptr[CChar],
      count: CSize,
      format: CString,
      time: Ptr[tm]
  ): CSize = extern

  // XSI
//  @name("scalanative_strptime")
  def strptime(str: Ptr[CChar], format: CString, time: Ptr[tm]): CString =
    extern

  @name("scalanative_time")
  def time(arg: Ptr[time_t]): time_t = extern

  @deprecated(
    "Binary compatibility: code compiled against 0.5.12 links to this extern name.",
    "0.5.13"
  )
  @name("time")
  private[posix] def _time(arg: Ptr[time_t]): time_t =
    externRedirect(time(arg))

  // See separate timer object below for timer_*() methods.

  @name("scalanative_tzset")
  def tzset(): Unit = extern

  @deprecated(
    "Binary compatibility: code compiled against 0.5.12 links to this extern name.",
    "0.5.13"
  )
  @name("tzset")
  private[posix] def _tzset(): Unit =
    externRedirect(tzset())

// POSIX variables (vals, not vars)

  @name("scalanative_daylight")
  def daylight: CInt = extern

  // XSI
  @name("scalanative_timezone")
  def timezone: CLong = extern

  // XSI
  @name("scalanative_tzname")
  def tzname: Ptr[CStruct2[CString, CString]] = extern

// Macros

// Symbolic constants

  @name("scalanative_clock_monotonic")
  def CLOCK_MONOTONIC: clockid_t = extern

  @name("scalanative_clock_process_cputime_id")
  def CLOCK_PROCESS_CPUTIME_ID: clockid_t = extern

  @name("scalanative_clock_realtime")
  def CLOCK_REALTIME: clockid_t = extern

  @name("scalanative_clock_thread_cputime_id")
  def CLOCK_THREAD_CPUTIME_ID: clockid_t = extern

  @name("scalanative_timer_abstime")
  def TIMER_ABSTIME: CInt = extern
}

object timeOps {
  import time.{time_t, timespec, tm}

  implicit class timespecOps(val ptr: Ptr[timespec]) extends AnyVal {
    def tv_sec: time_t = ptr._1
    def tv_nsec: CLong = ptr._2
    def tv_sec_=(v: time_t): Unit = ptr._1 = v
    def tv_nsec_=(v: CLong): Unit = ptr._2 = v
  }

  implicit class timespecValueOps(val ts: timespec) extends AnyVal {
    def tv_sec: time_t = ts._1
    def tv_nsec: CLong = ts._2
    def tv_sec_=(v: time_t): Unit = ts._1 = v
    def tv_nsec_=(v: CLong): Unit = ts._2 = v
  }

  implicit class tmOps(val ptr: Ptr[tm]) extends AnyVal {
    def tm_sec: CInt = ptr._1
    def tm_min: CInt = ptr._2
    def tm_hour: CInt = ptr._3
    def tm_mday: CInt = ptr._4
    def tm_mon: CInt = ptr._5
    def tm_year: CInt = ptr._6
    def tm_wday: CInt = ptr._7
    def tm_yday: CInt = ptr._8
    def tm_isdst: CInt = ptr._9

    def tm_gmtoff: CLongLong = ptr._10

    def tm_zone: CString = ptr._11

    def tm_sec_=(v: CInt): Unit = ptr._1 = v
    def tm_min_=(v: CInt): Unit = ptr._2 = v
    def tm_hour_=(v: CInt): Unit = ptr._3 = v
    def tm_mday_=(v: CInt): Unit = ptr._4 = v
    def tm_mon_=(v: CInt): Unit = ptr._5 = v
    def tm_year_=(v: CInt): Unit = ptr._6 = v
    def tm_wday_=(v: CInt): Unit = ptr._7 = v
    def tm_yday_=(v: CInt): Unit = ptr._8 = v
    def tm_isdst_=(v: CInt): Unit = ptr._9 = v

    def tm_gmtoff_=(v: CLongLong): Unit = ptr._10 = v

    def tm_zone_=(v: CString): Unit = ptr._11 = v
  }
}

@extern
@define("__SCALANATIVE_POSIX_TIMER")
object timer {
  /* The five timer_*() methods are in this separate object to simplify
   * the use of the more frequently used methods in time.h. Yes, at the
   * cost of having to import this separate, not-described-by-POSIX object.
   *
   * 1) Some operating systems provide the timer_* symbols in a way that
   *    no special linking is needed.  Include this object and link away.
   *
   * 2) Many/most operating systems require that the linker "-lrt" be specified
   *    so that the real time library, librt, is used to resolve the
   *    timer_* symbols.
   *
   * 3) macOS does not provide librt and has it own, entirely different, way of
   *    handling timers.
   */

  import time.timespec

  type itimerspec = CStruct2[
    timespec, // it_interval
    timespec // it_value
  ]

  def timer_create(
      clockid: clockid_t,
      sevp: sigevent,
      timerid: Ptr[timer_t]
  ): CInt = extern

  def timer_delete(timerid: timer_t): CInt = extern

  def timer_getoverrun(timerid: timer_t): CInt = extern

  @name("scalanative_timer_gettime")
  def timer_gettime(timerid: timer_t, curr_value: Ptr[itimerspec]): CInt =
    extern

  @deprecated(
    "Binary compatibility: code compiled against 0.5.13 links to this extern name.",
    "0.5.14"
  )
  @name("timer_gettime")
  private[posix] def _timer_gettime(
      timerid: timer_t,
      curr_value: Ptr[itimerspec]
  ): CInt =
    externRedirect(timer_gettime(timerid, curr_value))

  @name("scalanative_timer_settime")
  def timer_settime(
      timerid: timer_t,
      flags: CInt,
      new_value: Ptr[itimerspec],
      old_value: Ptr[itimerspec]
  ): CInt = extern

  @deprecated(
    "Binary compatibility: code compiled against 0.5.13 links to this extern name.",
    "0.5.14"
  )
  @name("timer_settime")
  private[posix] def _timer_settime(
      timerid: timer_t,
      flags: CInt,
      new_value: Ptr[itimerspec],
      old_value: Ptr[itimerspec]
  ): CInt =
    externRedirect(timer_settime(timerid, flags, new_value, old_value))

// Symbolic constants

  @name("scalanative_timer_abstime")
  def TIMER_ABSTIME: CInt = extern
}

object timerOps {
  import time.timespec
  import timer.itimerspec

  implicit class itimerspecOps(val ptr: Ptr[itimerspec]) extends AnyVal {
    def it_interval: timespec = ptr._1
    def it_value: timespec = ptr._2
    def it_interval_=(v: timespec): Unit = ptr._1 = v
    def it_value_=(v: timespec): Unit = ptr._2 = v
  }
}
