package scala.scalanative
package posix

import scalanative.native.{extern, name, CInt}

@extern
object limits {
  @name("scalanative_path_max")
  def PATH_MAX: CInt = extern
}
