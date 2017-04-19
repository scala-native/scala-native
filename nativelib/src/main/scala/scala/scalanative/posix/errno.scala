package scala.scalanative.posix

import scala.scalanative.native.{CInt, extern, name}

object errno {
  @name("scalanative_eintr")
  def EINTR: CInt = extern
  @name("scalanative_eexist")
  def EEXIST: CInt = extern
}
