package java.nio.channels

import scala.scalanative.posix.sys.types.{size_t, ssize_t}
import scala.scalanative.unsafe._

// File offsets are 64 bits, also on 32-bit targets, where posixlib's off_t
// (CLong) is 32 bits.
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

  @name("scalanative_javalib_lseek")
  def lseek(fd: CInt, offset: CLongLong, whence: CInt): CLongLong = extern

  // F_SETLK; mode 0 unlock, 1 shared, 2 exclusive. Returns 0 or errno.
  @name("scalanative_javalib_lock")
  def lock(fd: CInt, mode: CInt, start: CLongLong, len: CLongLong): CInt =
    extern

  @name("scalanative_javalib_ftruncate")
  def ftruncate(fd: CInt, length: CLongLong): CInt = extern
}
