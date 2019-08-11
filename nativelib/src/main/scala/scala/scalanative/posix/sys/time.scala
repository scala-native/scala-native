package scala.scalanative
package posix
package sys

import scala.scalanative.native.{
  CInt,
  CLong,
  CLongInt,
  CStruct2,
  Ptr,
  extern,
  name
}

@extern
object time {
  type time_t    = CLongInt
  type timespec  = CStruct2[time_t, CLong]
  type clockid_t = CInt

  def clock_gettime(clk_id: clockid_t, ts: Ptr[timespec]): CInt = extern

  @name("scalanative_clock_realtime")
  def CLOCK_REALTIME: clockid_t = extern
}
