package scala.scalanative
package native

@extern
object time {

  type time_t   = CLong
  type clock_t  = CLong
  type timespec = CStruct2[time_t, CLong]
  type tm       = CStruct9[CInt, CInt, CInt, CInt, CInt, CInt, CInt, CInt, CInt]

  def asctime(time_ptr: Ptr[tm]): CString                               = extern
  def asctime_s(buf: Ptr[CChar], bufsz: CSize, time_ptr: Ptr[tm]): CInt = extern
  def clock(): clock_t                                                  = extern
  def ctime(time: Ptr[time_t]): CString                                 = extern
  def ctime_s(buffer: Ptr[CChar], bufsz: CSize, time: Ptr[time_t]): CInt =
    extern
  def difftime(time_end: CLong, time_beg: CLong): CDouble      = extern
  def gmtime(time: Ptr[time_t]): Ptr[tm]                       = extern
  def gmtime_s(time: Ptr[time_t], result: Ptr[tm]): Ptr[tm]    = extern
  def localtime(time: Ptr[time_t]): Ptr[tm]                    = extern
  def localtime_s(time: Ptr[time_t], result: Ptr[tm]): Ptr[tm] = extern
  def mktime(time: Ptr[tm]): time_t                            = extern
  def strftime(str: Ptr[CChar],
               count: CSize,
               format: CString,
               time: Ptr[tm]): CSize                    = extern
  def time(arg: Ptr[time_t]): time_t                    = extern
  def timespec_get(ts: Ptr[timespec], base: CInt): CInt = extern
  def tzset(): Unit                                     = extern
  def wcsftime(str: CWideChar,
               count: CSize,
               format: Ptr[CWideChar],
               time: Ptr[tm]): CSize = extern

  @name("scalanative_daylight")
  def daylight(): CInt = extern
  @name("scalanative_timezone")
  def timezone(): CLong = extern
  @name("scalanative_tzname")
  def tzname(): Ptr[CStruct2[CString, CString]] = extern
}

object timeOps {

  import time._
  implicit class timespecOps(val ptr: Ptr[timespec]) extends AnyVal {
    def tv_sec: time_t = !ptr._1

    def tv_nsec: CLong = !ptr._2

    def tv_sec_=(v: time_t): Unit = !ptr._1 = v

    def tv_nsec_=(v: CLong): Unit = !ptr._2 = v

  }

  implicit class tmOps(val ptr: Ptr[tm]) extends AnyVal {

    def tm_sec: CInt = !ptr._1

    def tm_min: CInt = !ptr._2

    def tm_hour: CInt = !ptr._3

    def tm_mday: CInt = !ptr._4

    def tm_mon: CInt = !ptr._5

    def tm_year: CInt = !ptr._6

    def tm_wday: CInt = !ptr._7

    def tm_yday: CInt = !ptr._8

    def tm_isdst: CInt = !ptr._9

    def tm_sec_=(v: CInt): Unit = !ptr._1 = v

    def tm_min_=(v: CInt): Unit = !ptr._2 = v

    def tm_hour_=(v: CInt): Unit = !ptr._3 = v

    def tm_mday_=(v: CInt): Unit = !ptr._4 = v

    def tm_mon_=(v: CInt): Unit = !ptr._5 = v

    def tm_year_=(v: CInt): Unit = !ptr._6 = v

    def tm_wday_=(v: CInt): Unit = !ptr._7 = v

    def tm_yday_=(v: CInt): Unit = !ptr._8 = v

    def tm_isdst_=(v: CInt): Unit = !ptr._9 = v

  }

}
