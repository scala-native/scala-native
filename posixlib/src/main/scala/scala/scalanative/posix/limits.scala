package scala.scalanative
package posix

import scalanative.unsafe._

@extern
@define("__SCALANATIVE_POSIX_LIMITS")
object limits {
  @name("scalanative_path_max")
  def PATH_MAX: CSize = extern
}
