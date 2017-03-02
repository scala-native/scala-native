package scala.scalanative.posix.sys

import scala.scalanative.native._
import scala.scalanative.posix.sys.types.suseconds_t
import scala.scalanative.runtime.time.time_t

/**
 * Created by remi on 01/03/17.
 */
@extern
object timex {

  def adjtimex(buf: Ptr[timex]): CInt    = extern
  def ntp_adjtime(buf: Ptr[timex]): CInt = extern

  // Types
  type timeval = CStruct2[time_t, suseconds_t]
  type timex = CStruct20[CInt, CLong, CLong, CLong, CLong, CInt, CLong, CLong, CLong, timeval, CLong, CLong, CLong, CInt, CLong, CLong, CLong, CLong, CLong, CInt]

  // Macros
  @name("scalamative_adj_offset")
  def ADJ_OFFSET = extern
  @name("scalamative_adj_frequency")
  def ADJ_FREQUENCY = extern
  @name("scalamative_adj_maxerror")
  def ADJ_MAXERROR = extern
  @name("scalamative_adj_esterror")
  def ADJ_ESTERROR = extern
  @name("scalamative_adj_status")
  def ADJ_STATUS = extern
  @name("scalamative_adj_timeconst")
  def ADJ_TIMECONST = extern
  @name("scalamative_adj_setoffset")
  def ADJ_SETOFFSET = extern
  @name("scalamative_adj_micro")
  def ADJ_MICRO = extern
  @name("scalamative_adj_nano")
  def ADJ_NANO = extern
  @name("scalamative_adj_tai")
  def ADJ_TAI = extern
  @name("scalamative_adj_tick")
  def ADJ_TICK = extern
  @name("scalamative_adj_offset_singleshot")
  def ADJ_OFFSET_SINGLESHOT = extern
  @name("scalamative_adj_offset_ss_read")
  def ADJ_OFFSET_SS_READ = extern
  @name("scalamative_sta_ppl")
  def STA_PPL = extern
  @name("scalamative_sta_ppsfrq")
  def STA_PPSFREQ = extern
  @name("scalamative_sta_ppstime")
  def STA_PPSTIME = extern
  @name("scalamative_sta_fll")
  def STA_FLL = extern
  @name("scalamative_sta_ins")
  def STA_INS = extern
  @name("scalamative_sta_del")
  def STA_DEL = extern
  @name("scalamative_sta_unsync")
  def STA_UNSYNC = extern
  @name("scalamative_sta_freqhold")
  def STA_FREQHOLD = extern
  @name("scalamative_sta_ppssignal")
  def STA_PPSSIGNAL = extern
  @name("scalamative_sta_ppsjitter")
  def STA_PPSJITTER = extern
  @name("scalamative_sta_ppswander")
  def STA_PPSWANDER = extern
  @name("scalamative_sta_ppserror")
  def STA_PPSERROR = extern
  @name("scalamative_sta_clockerr")
  def STA_CLOCKERR = extern
  @name("scalamative_sta_nano")
  def STA_NANO = extern
  @name("scalamative_sta_mode")
  def STA_MODE = extern
  @name("scalamative_sta_clk")
  def STA_CLK = extern
  @name("scalamative_time_ok")
  def TIME_OK = extern
  @name("scalamative_time_ins")
  def TIME_INS = extern
  @name("scalamative_time_del")
  def TIME_DEL = extern
  @name("scalamative_time_oop")
  def TIME_OOP = extern
  @name("scalamative_time_wait")
  def TIME_WAIT = extern
  @name("scalamative_time_error")
  def TIME_ERROR = extern
}
