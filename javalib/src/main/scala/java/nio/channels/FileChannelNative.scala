package java.nio.channels

import scala.scalanative.posix.sys.types.{size_t, ssize_t}
import scala.scalanative.unsafe._

// pread/pwrite with 64-bit offsets, also on 32-bit targets.
@define("__SCALANATIVE_JAVALIB_NIO_FILE_CHANNEL")
@extern
private[channels] object FileChannelNative {
  @blocking
  @name("scalanative_javalib_pread")
  def pread(
      fd: CInt,
      buf: CVoidPtr,
      count: size_t,
      offset: CLongLong
  ): ssize_t = extern

  @blocking
  @name("scalanative_javalib_pwrite")
  def pwrite(
      fd: CInt,
      buf: CVoidPtr,
      count: size_t,
      offset: CLongLong
  ): ssize_t = extern
}
