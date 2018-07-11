package scala.scalanative
package posix
package sys

import scalanative.native.{CInt, CLong, CLongInt, CStruct2, Ptr, extern}
import scalanative.posix.time._

@extern
object time {

  type useconds_t = CLongInt

  type timeval = CStruct2[time_t, useconds_t]

  def gettimeofday(tv: Ptr[timeval], tz: Ptr[scala.Byte]): CInt = extern
}

object timeOps {
  import time._

  implicit class timevalOps(val ptr: Ptr[time.timeval]) extends AnyVal {
    def tv_sec: time_t      = !(ptr._1)
    def tv_usec: useconds_t = !(ptr._2)

    def tv_sec_=(v: time_t): Unit      = !ptr._1 = v
    def tv_usec_=(v: useconds_t): Unit = !ptr._2 = v
  }
}
