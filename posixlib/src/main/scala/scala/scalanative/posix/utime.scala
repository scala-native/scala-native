package scala.scalanative
package posix

import scalanative.unsafe.*

@extern
object utime {
  type utimbuf = CStruct2[
    time.time_t, // actime
    time.time_t // modtime
  ]

  def utime(path: CString, times: Ptr[utimbuf]): CInt = extern
}
