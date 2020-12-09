package scala.scalanative
package posix

import scalanative.unsafe.{extern, name, CInt}

object limits {
  @name("scalanative_path_max")
  def PATH_MAX: CInt = extern
}
