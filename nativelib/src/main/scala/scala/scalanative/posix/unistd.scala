package scala.scalanative.posix

import scala.scalanative.native.{CUnsignedInt, extern}

/**
 * Created by marius on 27.10.16.
 */
@extern
object unistd {
  def sleep(seconds: CUnsignedInt): Int = extern
  def usleep(usecs: CUnsignedInt): Int  = extern
}
