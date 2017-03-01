package scala.scalanative.posix.sys

import scala.scalanative.native.{CInt, extern}
import scala.scalanative.posix.sys.types.{gid_t, uid_t}

/**
  * Created by remi on 01/03/17.
  */
@extern
object fsuid {

  def setfsuid(fsuid: uid_t): CInt = extern
  def setfsgid(fsgid: gid_t): CInt = extern

}
