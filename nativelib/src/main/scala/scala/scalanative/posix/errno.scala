package scala.scalanative
package posix

import native.{CInt, extern, name}

object errno {
  @name("scalanative_eintr")
  def EINTR: CInt = extern
  @name("scalanative_eexist")
  def EEXIST: CInt = extern
}
