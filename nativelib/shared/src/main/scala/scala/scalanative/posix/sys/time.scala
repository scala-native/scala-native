package scala.scalanative
package posix
package sys

import scalanative.native.{CInt, CLong, CLongInt, CStruct2, Ptr, extern}
import scalanative.posix.time._

@extern
object time {
  type timeval = CStruct2[time_t, CLong]

  def gettimeofday(tv: Ptr[timeval], tz: Ptr[scala.Byte]): CInt = extern
}
