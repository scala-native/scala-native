package scala.scalanative
package posix
package sys

import scala.scalanative.native.{CLong, CLongInt, CStruct2}

object time {
  type time_t   = CLongInt
  type timespec = CStruct2[time_t, CLong]
}
