package scala.scalanative.posix.sys

import scalanative.native._

@extern
object ioctl {

  @name("scalanative_ioctl")
  def ioctl(fd: CInt, request: CLongInt, argp: Ptr[Byte]): CInt = extern

  @name("scalanative_FIONREAD")
  def FIONREAD: CLongInt = extern

  @name("scalanative_SIOCGIFCONF")
  def SIOCGIFCONF: CLongInt = extern

  @name("scalanative_SIOCGIFFLAGS")
  def SIOCGIFFLAGS: CLongInt = extern

  @name("scalanative_SIOCGIFHWADDR")
  def SIOCGIFHWADDR: CLongInt = extern

  @name("scalanative_SIOCGIFMTU")
  def SIOCGIFMTU: CLongInt = extern
}
