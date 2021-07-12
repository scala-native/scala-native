package scala.scalanative.libc

import scala.scalanative.unsafe._
import stdlib.size_t

@extern
object time {
  //todo: maybe use trickery here since time_t is unspecified in the standard
  type time_t   = CLong
  type clock_t  = CLong
  type timespec = CStruct2[CLong, CLong]
  type tm       = CStruct8[CInt, CInt, CInt, CInt, CInt, CInt, CInt, CInt]
  @name("scalanative_time_utc")
  val TIME_UTC: CInt = extern

  @name("scalanative_clocks_per_sec")
  val CLOCKS_PER_SEC: CInt = extern

  def difftime(time_end: time_t, time_beg: time_t): CDouble = extern
  def time(arg: Ptr[time_t]): time_t                        = extern
  def clock(): clock_t                                      = extern
  def timespec_get(ts: Ptr[timespec], base: CInt): CInt     = extern
  def asctime(time_ptr: Ptr[tm]): CString                   = extern
  def ctime(time: Ptr[time_t]): CString                     = extern
  def strftime(str: CString,
               count: size_t,
               format: CString,
               time: Ptr[tm]): size_t       = extern
  def gmtime(time: Ptr[time_t]): Ptr[tm]    = extern
  def mktime(time: Ptr[tm]): time_t         = extern
  def localtime(time: Ptr[time_t]): Ptr[tm] = extern
}
