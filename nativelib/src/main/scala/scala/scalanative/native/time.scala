package scala.scalanative
package native

@extern
object time {

  // Time manipulation

  def difftime(time_end: time_t, time_beg: time_t): CDouble = extern
  def time(arg: Ptr[time_t]): time_t                        = extern
  def clock(): clock_t                                      = extern
  def timespec_get(ts: Ptr[timespec], base: CInt): CInt     = extern

  // Format conversions

  def asctime(time_ptr: Ptr[tm]): CString = extern
  def ctime(time: Ptr[time_t]): CString   = extern
  def strftime(str: CString, count: CSize, format: CString, time: Ptr[tm]) =
    extern
  def gmtime(time: Ptr[time_t]): Ptr[tm]    = extern
  def localtime(time: Ptr[time_t]): Ptr[tm] = extern
  def mktime(time: Ptr[tm]): time_t         = extern

  // Types

  type tm       = CStruct9[CInt, CInt, CInt, CInt, CInt, CInt, CInt, CInt, CInt]
  type time_t   = CStruct0
  type clock_t  = CStruct0
  type timespec = CStruct2[time_t, CLong]

  // Macros

  @name("scalanative_libc_clocks_per_sec")
  def CLOCKS_PER_SEC: clock_t = extern
  @name("scalanative_libc_time_utc")
  def TIME_UTC: CInt = extern
}
