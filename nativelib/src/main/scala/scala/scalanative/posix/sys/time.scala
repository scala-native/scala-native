package scala.scalanative
package posix
package sys

import scalanative.native.{CInt, CLong, CLongInt, CStruct2, Ptr, extern}

@extern
object time {
  type time_t   = CLongInt
  type timespec = CStruct2[time_t, CLong]
  type timeval  = CStruct2[time_t, CLong]

  def gettimeofday(tv: Ptr[timeval], tz: Ptr[scala.Byte]): CInt = extern
}
