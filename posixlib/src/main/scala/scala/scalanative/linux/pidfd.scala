package scala.scalanative
package linux

import posix.sys.types._
import unsafe._

@extern
@define("__SCALANATIVE_POSIX_PIDFD")
object pidfd {

  @name("scalanative_has_pidfd_open")
  def has_pidfd_open(): CBool = extern

  @name("scalanative_pidfd_open")
  def pidfd_open(pid: pid_t, flags: CUnsignedInt): CInt = extern

}
