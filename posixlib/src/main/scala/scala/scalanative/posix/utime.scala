package scala.scalanative
package posix

import scalanative.unsafe._

@extern
object utime {
  type utimbuf = CStruct2[time.time_t, // actime
                          time.time_t] // modtime

  @name("scalanative_utime")
  def utime(path: CString, times: Ptr[utimbuf]): CInt = extern
}
