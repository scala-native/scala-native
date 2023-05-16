package scala.scalanative
package posix
package sys

import scalanative.unsafe._
import scalanative.posix.sys.types.{suseconds_t, time_t}

@extern
object time {

  type timeval = CStruct2[time_t, suseconds_t]

  def gettimeofday(tv: Ptr[timeval], tz: Ptr[scala.Byte]): CInt = extern
}

object timeOps {
  import time.timeval

  implicit class timevalOps(val ptr: Ptr[timeval]) extends AnyVal {
    def tv_sec: time_t = ptr._1
    def tv_usec: suseconds_t = ptr._2
    def tv_sec_=(v: time_t): Unit = ptr._1 = v
    def tv_usec_=(v: suseconds_t): Unit = ptr._2 = v
  }

  implicit class timevalValOps(val tv: timeval) extends AnyVal {
    def tv_sec: time_t = tv._1
    def tv_usec: suseconds_t = tv._2
    def tv_sec_=(v: time_t): Unit = tv._1 = v
    def tv_usec_=(v: suseconds_t): Unit = tv._2 = v
  }

}
