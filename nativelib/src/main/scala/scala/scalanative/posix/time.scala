package scala.scalanative.native

import scala.scalanative.native.signal.sigevent
import scala.scalanative.posix.sys.types.{timer_t, pid_t, clockid_t}

// http://man7.org/linux/man-pages/man7/time.7.html

@extern
object time {
  def scalanative_nano_time: CLong = extern

  def clock(): clock_t                                = extern
  def time(tloc: Ptr[time_t]): time_t                 = extern
  def difftime(time1: time_t, time0: time_t): CDouble = extern
  def mktime(tm: Ptr[tm]): time_t                     = extern
  def strftime(s: CString, max: CSize, format: CString, tm: Ptr[tm]): CSize =
    extern
  def strptime(s: CString, format: CString, tm: Ptr[tm]): CString =
    extern
  def gmtime(timep: Ptr[time_t]): Ptr[tm]    = extern
  def localtime(timep: Ptr[time_t]): Ptr[tm] = extern
  def gmtime_r(timep: Ptr[time_t], result: Ptr[tm]): Ptr[tm] =
    extern
  def localtime_r(timep: Ptr[time_t], result: Ptr[tm]): Ptr[tm] =
    extern
  def asctime(tm: Ptr[tm]): CString                             = extern
  def asctime_r(tm: Ptr[tm], buf: CString): CString             = extern
  def ctime(timep: Ptr[time_t]): CString                        = extern
  def ctime_r(timep: Ptr[time_t], buf: CString): CString        = extern
  def stime(t: Ptr[time_t]): CInt                               = extern
  def timegm(tm: Ptr[tm]): time_t                               = extern
  def timelocal(tm: Ptr[tm]): time_t                            = extern
  def dysize(year: CInt): CInt                                  = extern
  def nanosleep(req: Ptr[timespec], rem: Ptr[timespec]): CInt   = extern
  def clock_getres(clk_id: clockid_t, res: Ptr[timespec]): CInt = extern
  def clock_gettime(clk_id: clockid_t, tp: Ptr[timespec]): CInt = extern
  def clock_settime(clk_id: clockid_t, tp: Ptr[timespec]): CInt = extern
  def clock_nanosleep(clock_id: clockid_t,
                      flags: CInt,
                      request: Ptr[timespec],
                      remain: Ptr[timespec]): CInt                    = extern
  def clock_getcpuclockid(pid: pid_t, clock_id: Ptr[clockid_t]): CInt = extern
  def timer_create(clockid: clockid_t,
                   sevp: Ptr[sigevent],
                   timerid: Ptr[timer_t]): CInt = extern
  def timer_delete(timerid: timer_t): CInt      = extern
  def timer_settime(timerid: timer_t,
                    flags: CInt,
                    new_value: Ptr[timespec],
                    old_value: Ptr[timespec]): CInt                    = extern
  def timer_gettime(timerid: timer_t, curr_value: Ptr[timespec]): CInt = extern
  def timer_getoverrun(timerid: timer_t): CInt                         = extern
  def getdate(string: CString): Ptr[tm]                                = extern
  def getdate_r(string: CString, res: Ptr[tm]): CInt                   = extern

  // Types
  type clock_t  = CLong
  type time_t   = CInt
  type timespec = CStruct2[time_t, CUnsignedLong]
  type tm       = CStruct9[CInt, CInt, CInt, CInt, CInt, CInt, CInt, CInt, CInt]

  // Macros
  @name("scalanative_clock_realtime")
  def CLOCK_REALTIME = extern
  @name("scalanative_clock_realtime_coarse")
  def CLOCK_REALTIME_COARSE = extern
  @name("scalanative_clock_monotonic")
  def CLOCK_MONOTONIC = extern
  @name("scalanative_clock_monotonic_coarse")
  def CLOCK_MONOTONIC_COARSE = extern
  @name("scalanative_clock_monotonic_raw")
  def CLOCK_MONOTONIC_RAW = extern
  @name("scalanative_clock_boottime")
  def CLOCK_BOOTTIME = extern
  @name("scalanative_clock_process_cputime_id")
  def CLOCK_PROCESS_CPUTIME_ID = extern
  @name("scalanative_clock_thread_cputime_id")
  def CLOCK_THREAD_CPUTIME_ID = extern
