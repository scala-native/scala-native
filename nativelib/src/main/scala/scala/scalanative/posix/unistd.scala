package scala.scalanative.posix

import scala.scalanative.native.{CUnsignedInt, CString, Ptr, extern, name}

@extern
object unistd {
  def sleep(seconds: CUnsignedInt): Int = extern
  def usleep(usecs: CUnsignedInt): Int  = extern

  // Macros

  @name("scalanative_environ")
  def environ: Ptr[CString] = extern
}
