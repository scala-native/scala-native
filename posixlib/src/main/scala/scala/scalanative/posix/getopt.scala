package scala.scalanative
package posix

import scalanative.unsafe._

@extern
object getopt {
  var optarg: CString = extern
  var opterr: CInt    = extern
  var optind: CInt    = extern
  var optopt: CInt    = extern

  def getopt(argc: CInt, argv: Ptr[CString], optstring: CString): CInt = extern
}
