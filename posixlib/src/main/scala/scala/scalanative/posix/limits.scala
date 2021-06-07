package scala.scalanative
package posix

import scalanative.unsafe.{CSize, extern, name}

@extern
object limits {
  @name("scalanative_path_max")
  def PATH_MAX: CSize = extern
}
