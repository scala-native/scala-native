package scala.scalanative
package posix

import scalanative.unsafe._

@extern
@define("__SCALANATIVE_POSIX_UTIME")
object utime {
  type utimbuf = CStruct2[
    time.time_t, // actime
    time.time_t // modtime
  ]

  @name("scalanative_utime")
  def utime(path: CString, times: Ptr[utimbuf]): CInt = extern

  @deprecated(
    "Binary compatibility: code compiled against 0.5.13 links to this extern name.",
    "0.5.14"
  )
  @name("utime")
  private[posix] def _utime(path: CString, times: Ptr[utimbuf]): CInt =
    externRedirect(utime(path, times))
}
