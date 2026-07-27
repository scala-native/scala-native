package scala.scalanative.windows.crt

import scala.scalanative.unsafe._

@extern
object time {
  /* Bindings for time.h which are not part of POSIX standard.
   * We assume the same structure as in the POSIX, however on Windows
   * we don't need to introduce tm coping glue layer
   * For compat with existing POSIX structures we use 32bit variant of methods whenever possible
   */
  type clock_t = CLong
  type time_t = CLong
  type uid_t = CUnsignedInt

// format: off

  // Keep in sync with scalanative/posix/time.scala
  type tm = CStruct11[
    CInt, CInt, CInt,
    CInt, CInt, CInt,
    CInt, CInt, CInt,
    CLongLong,
    CString
  ]

// format: on

  // 64-bit time_t. Prefer this for any value that may exceed the
  // 2038-01-19 ceiling of `__time32_t` (e.g. ZIP DOS dates extend to
  // 2107).
  type time64_t = CLongLong

  type errno_t = CInt

  def asctime(time_ptr: Ptr[tm]): CString = extern
  def asctime_s(time_ptr: Ptr[tm], size: CSize, buf: Ptr[CChar]): errno_t =
    extern
  @name("_gmtime32")
  def gmtime(time: Ptr[time_t]): Ptr[tm] = extern
  @name("_gmtime32_s")
  def gmtime_s(tm: Ptr[tm], time: Ptr[time_t]): errno_t = extern
  @name("_localtime32")
  def localtime(time: Ptr[time_t]): Ptr[tm] = extern
  @name("_localtime32_s")
  def localtime_s(tm: Ptr[tm], time: Ptr[time_t]): errno_t = extern
  @name("_localtime64_s")
  def localtime64_s(tm: Ptr[tm], time: Ptr[time64_t]): errno_t = extern
  @name("_mktime64")
  def mktime64(time: Ptr[tm]): time64_t = extern

  def strftime(
      str: Ptr[CChar],
      count: CSize,
      format: CString,
      time: Ptr[tm]
  ): CSize = extern

  @name("_time32")
  def time(arg: Ptr[time_t]): time_t = extern
  @name("_tzset")
  def tzset(): Unit = extern

  @name("_daylight")
  def daylight: CInt = extern

  @name("_timezone")
  def timezone: CLong = extern

  @name("_tzname")
  def tzname: Ptr[CStruct2[CString, CString]] = extern
}
