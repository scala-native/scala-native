package scala.scalanative
package posix

import scalanative.unsafe._

@deprecated(
  "getopt is no longer part of POSIX 2018 and will be removed. Use unistd instead.",
  "0.4.10"
)
@extern
object getopt {
  var optarg: CString = extern
  var opterr: CInt = extern
  var optind: CInt = extern
  var optopt: CInt = extern

  def getopt(argc: CInt, argv: Ptr[CString], optstring: CString): CInt = extern
}
