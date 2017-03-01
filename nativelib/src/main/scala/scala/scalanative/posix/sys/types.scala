package scala.scalanative.posix.sys

import scala.scalanative.native.{CUnsignedInt, extern, CInt}

/**
  * Created by remi on 28/02/17.
  */
@extern
object types {

  // Types

  type off_t = CInt
  type ssize_t = CInt
  type uid_t = CInt
  type gid_t = CInt
  type pid_t = CInt
  type key_t = CInt
  type mode_t = CUnsignedInt
  type dev_t = CInt
  type clockid_t = CInt
  type timer_t = CInt

}
