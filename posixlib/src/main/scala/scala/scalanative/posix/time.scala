package scala.scalanative
package posix

import scala.scalanative.unsafe._
import scala.scalanative.posix.sys.types, types._

@extern
object time {

  type time_t   = types.time_t
  type clock_t  = types.clock_t
  type timespec = CStruct2[time_t, CLong]

  // Keep 'type tm' manually synchronized with 'struct scalanative_tm'
  // in nativelib/src/main/resources/time.c.
  //
  // This is must be the linux 'long' 56 byte 'struct tm' so that
  // a localtime_r() strftime() using %Z or %z timezone information
  // will work in Scala Native as it does in C.
  // It is OK to declare more memory that some J-random OS may not
  // use in order to keep the common linux & BSD cases friendly.

  type tm = CStruct12[CInt, // tm_sec
                      CInt, // tm_min
                      CInt, // tm_hour
                      CInt, // tm_mday
                      CInt, // tm_mon
                      CInt, // tm_year
                      CInt, // tm_wday
                      CInt, // tm_yday
                      CInt, // tm_isdst
                      CInt, // pad to int64_t boundary
                      CLongLong, // tm_gmtoff
                      CString] // tm_gmtoff

  @name("scalanative_asctime")
  def asctime(time_ptr: Ptr[tm]): CString = extern
  @name("scalanative_asctime_r")
  def asctime_r(time_ptr: Ptr[tm], buf: Ptr[CChar]): CString = extern
  def clock(): clock_t                                       = extern
  def ctime(time: Ptr[time_t]): CString                      = extern
  def ctime_r(time: Ptr[time_t], buf: Ptr[CChar]): CString   = extern
  def difftime(time_end: CLong, time_beg: CLong): CDouble    = extern
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
//  @name("scalanative_strftime")
  def strftime(str: Ptr[CChar],
               count: CSize,
               format: CString,
               time: Ptr[tm]): CSize = extern
  @name("scalanative_strptime")
  def strptime(str: Ptr[CChar], format: CString, time: Ptr[tm]): CString =
    extern
  def time(arg: Ptr[time_t]): time_t = extern
  def tzset(): Unit                  = extern
  @name("scalanative_daylight")
  def daylight(): CInt = extern
  @name("scalanative_timezone")
  def timezone(): CLong = extern
  @name("scalanative_tzname")
  def tzname(): Ptr[CStruct2[CString, CString]] = extern
}

object timeOps {
  import time.{time_t, timespec, tm}

  implicit class timespecOps(val ptr: Ptr[timespec]) extends AnyVal {
    def tv_sec: time_t            = ptr._1
    def tv_nsec: CLong            = ptr._2
    def tv_sec_=(v: time_t): Unit = ptr._1 = v
    def tv_nsec_=(v: CLong): Unit = ptr._2 = v
  }

  implicit class tmOps(val ptr: Ptr[tm]) extends AnyVal {
    def tm_sec: CInt   = ptr._1
    def tm_min: CInt   = ptr._2
    def tm_hour: CInt  = ptr._3
    def tm_mday: CInt  = ptr._4
    def tm_mon: CInt   = ptr._5
    def tm_year: CInt  = ptr._6
    def tm_wday: CInt  = ptr._7
    def tm_yday: CInt  = ptr._8
    def tm_isdst: CInt = ptr._9
    // ptr._10 is alignment padding, skip it
    def tm_gmtoff: CLongLong = ptr._11
    def tm_zone: CString     = ptr._12

    def tm_sec_=(v: CInt): Unit   = ptr._1 = v
    def tm_min_=(v: CInt): Unit   = ptr._2 = v
    def tm_hour_=(v: CInt): Unit  = ptr._3 = v
    def tm_mday_=(v: CInt): Unit  = ptr._4 = v
    def tm_mon_=(v: CInt): Unit   = ptr._5 = v
    def tm_year_=(v: CInt): Unit  = ptr._6 = v
    def tm_wday_=(v: CInt): Unit  = ptr._7 = v
    def tm_yday_=(v: CInt): Unit  = ptr._8 = v
    def tm_isdst_=(v: CInt): Unit = ptr._9 = v
    // ptr._10 is alignment padding, skip it
    def tm_gmtoff_=(v: CLongLong): Unit = ptr._11 = v
    def tm_zone_=(v: CString): Unit     = ptr._12 = v
  }
}
