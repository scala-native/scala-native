package scala.scalanative.posix.sys

import scala.scalanative.native.CInt

/**
  * Created by remi on 28/02/17.
  */
object types {

  // Types

  type off_t = CInt
  type ssize_t = CInt
  type uid_t = CInt
  type gid_t = CInt
  type pid_t = CInt

}
