package scala.scalanative
package runtime

<<<<<<< b36f7b54fd7e0fc330d2480ea5a7d0bb8e2b0bee
import scala.scalanative.native.{CLongLong, extern}

@extern
object time {
  def scalanative_nano_time: CLongLong           = extern
  def scalanative_current_time_millis: CLongLong = extern
=======
import scala.scalanative.native._
import scala.scalanative.posix.sys.types.{timer_t, pid_t, clockid_t}

@extern
object time {
  def scalanative_nano_time: CLong = extern

  def clock(): clock_t = extern
  def time(tloc: Ptr[time_t]): time_t = extern
  def difftime(time1: time_t, time0: time_t): CDouble = extern
  def mktime(tm: Ptr[CStruct9]): time_t = extern
  def strftime(s: CString, max: CSize, format: CString, tm: Ptr[CStruct9]): CSize = extern
  def strptime(s: CString, format: CString, tm: Ptr[CStruct9]): CString = extern
  def gmtime(timep: Ptr[time_t]): Ptr[CStruct9] = extern
  def localtime(timep: Ptr[time_t]): Ptr[CStruct9] = extern
  def gmtime_r(timep: Ptr[time_t], result: Ptr[CStruct9]): Ptr[CStruct9] = extern
  def localtime_r(timep: Ptr[time_t], result: Ptr[CStruct9]): Ptr[CStruct9] = extern
  def asctime(tm: Ptr[CStruct9]): CString = extern
  def asctime_r(tm: Ptr[CStruct9], buf: CString): CString = extern
  def ctime(timep: Ptr[time_t]): CString = extern
  def ctime_r(timep: Ptr[time_t], buf: CString): CString = extern
  def stime(t: Ptr[time_t]): CInt = extern
  def timegm(tm: Ptr[CStruct9]): time_t = extern
  def timelocal(tm: Ptr[CStruct9]): time_t = extern
  def dysize(year: CInt): CInt = extern
  def nanosleep(req: Ptr[CStruct2], rem: Ptr[CStruct2]): CInt = extern
  def clock_getres(clk_id: clockid_t, res: Ptr[CStruct2]): CInt = extern
  def clock_gettime(clk_id: clockid_t, tp: Ptr[CStruct2]): CInt = extern
  def clock_settime(clk_id: clockid_t, tp: Ptr[CStruct2]): CInt = extern
  def clock_nanosleep(clock_id: clockid_t, flags: CInt, request: Ptr[CStruct2], remain: Ptr[CStruct2]): CInt = extern
  def clock_getcpuclockid(pid: pid_t, clock_id: Ptr[clockid_t]): CInt = extern
  def timer_create(clockid: clockid_t, sevp: Ptr[CStruct6], timerid: Ptr[timer_t]): CInt = extern
  def timer_delete(timerid: timer_t): CInt = extern
  def timer_settime(timerid: timer_t, flags: CInt, new_value: Ptr[CStruct2], old_value: Ptr[CStruct2]): CInt = extern
  def timer_gettime(timerid: timer_t, curr_value: Ptr[CStruct2]): CInt = extern
  def timer_getoverrun(timerid: timer_t): CInt = extern
  def getdate(string: CString): Ptr[CStruct9] = extern
  def getdate_r(string: CString, res: Ptr[CStruct9]): CInt = extern

  // Types
  type clock_t = CLong
  type time_t = CInt

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

>>>>>>> some more functions
}
