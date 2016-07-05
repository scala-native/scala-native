package scala.scalanative
package native

@extern
object time {

  type time_t = CLong

  @struct
  class timespec(
      val tv_sec: time_t = 0L, /* seconds */
      val tv_nsec: CLong = 0L /* nanoseconds */
  )

  def clock_getres(clk_id: Clock.clockid_t, res: Ptr[timespec]): CInt  = extern
  def clock_gettime(clk_id: Clock.clockid_t, res: Ptr[timespec]): CInt = extern
  def clock_settime(clk_id: Clock.clockid_t, res: Ptr[timespec]): CInt = extern
}

object Clock {
  type clockid_t = CInt

  final val CLOCK_REALTIME: clockid_t           = 0
  final val CLOCK_MONOTONIC: clockid_t          = 1
  final val CLOCK_PROCESS_CPUTIME_ID: clockid_t = 2
  final val CLOCK_THREAD_CPUTIME_ID: clockid_t  = 3
}
