package scala.scalanative
package posix

import scala.scalanative.unsafe._
import scala.scalanative.posix.sys.types, types._
import scala.scalanative.posix.signal.sigevent

// XSI comment before method indicates it is defined in
// extended POSIX X/Open System Interfaces, not base POSIX.

@extern
object time {

  type clock_t = types.clock_t
  type clockid_t = types.clockid_t

  /* locale_t is required by POSIX stanard but otherwise unused in this file.
   * C (void *) which can be cast if/when posixlib locale.h is implemented.
   */

  type locale_t = Ptr[Byte]

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

  type tm = CStruct9[
    CInt, // tm_sec
    CInt, // tm_min
    CInt, // tm_hour
    CInt, // tm_mday
    CInt, // tm_mon
    CInt, // tm_year
    CInt, // tm_wday
    CInt, // tm_yday
    CInt // tm_isdst
  ]

  type itimerspec = CStruct2[
    timespec, // it_interval
    timespec // it_value
  ]

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
   * "clock_*" & "timer_*" use this assurance to avoid "glue".
   *
   * Some methods which now do not strictly need "@name" annotations
   * retain them for historical reasons.
   */

  @name("scalanative_asctime")
  def asctime(time_ptr: Ptr[tm]): CString = extern

  @name("scalanative_asctime_r")
  def asctime_r(time_ptr: Ptr[tm], buf: Ptr[CChar]): CString = extern

  def clock(): clock_t = extern

  def clock_getres(clockid: clockid_t, res: Ptr[timespec]): CInt = extern

  def clock_gettime(clockid: clockid_t, tp: Ptr[timespec]): CInt = extern

  @name("scalanative_clock_nanosleep") // glue handles clock_nanosleep on macOS
  def clock_nanosleep(
      clockid: clockid_t,
      flags: CInt,
      request: Ptr[timespec],
      remain: Ptr[timespec]
  ): CInt = extern

  def clock_settime(clockid: clockid_t, tp: Ptr[timespec]): CInt = extern

  def ctime(time: Ptr[time_t]): CString = extern
  def ctime_r(time: Ptr[time_t], buf: Ptr[CChar]): CString = extern

  def difftime(time_end: CLong, time_beg: CLong): CDouble = extern

  @name("scalanative_gmtime")
  def gmtime(time: Ptr[time_t]): Ptr[tm] = extern

  @name("scalanative_gmtime_r")
  def gmtime_r(time: Ptr[time_t], tm: Ptr[tm]): Ptr[tm] = extern

  @name("scalanative_localtime")
  def localtime(time: Ptr[time_t]): Ptr[tm] = extern

  @name("scalanative_localtime_r")
  def localtime_r(time: Ptr[time_t], tm: Ptr[tm]): Ptr[tm] = extern

  @name("scalanative_mktime")
  def mktime(time: Ptr[tm]): time_t = extern

  def nanosleep(requested: Ptr[timespec], remaining: Ptr[timespec]): CInt =
    extern

  @name("scalanative_strftime")
  def strftime(
      str: Ptr[CChar],
      count: CSize,
      format: CString,
      time: Ptr[tm]
  ): CSize = extern

  // XSI
  @name("scalanative_strptime")
  def strptime(str: Ptr[CChar], format: CString, time: Ptr[tm]): CString =
    extern

  def time(arg: Ptr[time_t]): time_t = extern

  /* Users of the five timer_() methods on operating systems other than
   * macOS need to provide the "-lrt" link option so that the real time
   * library "librt" is available.
   */

  /* The five timer_() methods need "@name" to provide "always -1"
   * stubs on macOS. Unfortunate overhead in execution time sensitive methods.
   */

  @name("scalanative_timer_create")
  def timer_create(
      clockid: clockid_t,
      sevp: sigevent,
      timerid: Ptr[timer_t]
  ): CInt = extern

  @name("scalanative_timer_delete")
  def timer_delete(timerid: timer_t): CInt = extern

  @name("scalanative_timer_getoverrun")
  def timer_getoverrun(timerid: timer_t): CInt = extern

  @name("scalanative_timer_gettime")
  def timer_gettime(timerid: timer_t, curr_value: Ptr[itimerspec]): CInt =
    extern

  @name("scalanative_timer_settime")
  def timer_settime(
      timerid: timer_t,
      flags: CInt,
      new_value: Ptr[itimerspec],
      old_value: Ptr[itimerspec]
  ): CInt = extern

  def tzset(): Unit = extern

// POSIX variables (vals, not vars)

  @name("scalanative_daylight")
  def daylight(): CInt = extern

  // XSI
  @name("scalanative_timezone")
  def timezone(): CLong = extern

  // XSI
  @name("scalanative_tzname")
  def tzname(): Ptr[CStruct2[CString, CString]] = extern
// Macros
  @name("scalanative_clocks_per_sec")
  def CLOCKS_PER_SEC: CInt = extern

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
    def tm_sec_=(v: CInt): Unit = ptr._1 = v
    def tm_min_=(v: CInt): Unit = ptr._2 = v
    def tm_hour_=(v: CInt): Unit = ptr._3 = v
    def tm_mday_=(v: CInt): Unit = ptr._4 = v
    def tm_mon_=(v: CInt): Unit = ptr._5 = v
    def tm_year_=(v: CInt): Unit = ptr._6 = v
    def tm_wday_=(v: CInt): Unit = ptr._7 = v
    def tm_yday_=(v: CInt): Unit = ptr._8 = v
    def tm_isdst_=(v: CInt): Unit = ptr._9 = v
  }
}
