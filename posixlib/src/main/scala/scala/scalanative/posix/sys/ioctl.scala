package scala.scalanative.posix.sys

import scalanative.unsafe.*

@extern
@define("__SCALANATIVE_POSIX_SYS_IOCTL")
object ioctl {

  @name("scalanative_ioctl")
  @blocking
  def ioctl(fd: CInt, request: CLongInt, argp: Ptr[Byte]): CInt = extern

  @name("scalanative_fionread")
  def FIONREAD: CLongInt = extern
}
