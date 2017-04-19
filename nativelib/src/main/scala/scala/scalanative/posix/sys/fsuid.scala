package scala.scalanative
package posix.sys

import native.{CInt, extern}
import types.{gid_t, uid_t}

// part of http://man7.org/linux/man-pages/man7/credentials.7.html

@extern
object fsuid {

  def setfsuid(fsuid: uid_t): CInt = extern
  def setfsgid(fsgid: gid_t): CInt = extern

}
