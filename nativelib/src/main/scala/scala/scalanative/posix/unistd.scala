package scala.scalanative
package posix

import scalanative.native._
import scalanative.posix.sys.stat.{uid_t, gid_t}

@extern
object unistd {

  type off_t = CLongLong

  @name("scalanative_unistd_sleep")
  def sleep(seconds: CUnsignedInt): CInt = extern
  @name("scalanative_unistd_usleep")
  def usleep(usecs: CUnsignedInt): CInt = extern
  @name("scalanative_unistd_unlink")
  def unlink(path: CString): CInt = extern
  @name("scalanative_unistd_access")
  def access(pathname: CString, mode: CInt): CInt = extern
  @name("scalanative_unistd_readlink")
  def readlink(path: CString, buf: CString, bufsize: CSize): CInt = extern
  @name("scalanative_unistd_getcwd")
  def getcwd(buf: CString, size: CSize): CString = extern
  @name("scalanative_unistd_write")
  def write(fildes: CInt, buf: Ptr[_], nbyte: CSize): CInt = extern
  @name("scalanative_unistd_read")
  def read(fildes: CInt, buf: Ptr[_], nbyte: CSize): CInt = extern
  @name("scalanative_unistd_close")
  def close(fildes: CInt): CInt = extern
  @name("scalanative_unistd_fsync")
  def fsync(fildes: CInt): CInt = extern
  @name("scalanative_unistd_lseek")
  def lseek(fildes: CInt, offset: off_t, whence: CInt): off_t = extern
  @name("scalanative_unistd_ftruncate")
  def ftruncate(fildes: CInt, length: off_t): CInt = extern
  @name("scalanative_unistd_truncate")
  def truncate(path: CString, length: off_t): CInt = extern

  @name("scalanative_stdin_fileno")
  def STDIN_FILENO: CInt = extern

  @name("scalanative_stdout_fileno")
  def STDOUT_FILENO: CInt = extern

  @name("scalanative_stderr_fileno")
  def STDERR_FILENO: CInt = extern

  @name("scalanative_symlink")
  def symlink(path1: CString, path2: CString): CInt = extern

  @name("scalanative_symlinkat")
  def symlinkat(path1: CString, fd: CInt, path2: CString): CInt = extern

  @name("scalanative_link")
  def link(path1: CString, path2: CString): CInt = extern

  @name("scalanative_linkat")
  def linkat(fd1: CInt,
             path1: CString,
             fd2: CInt,
             path2: CString,
             flag: CInt): CInt = extern

  @name("scalanative_chown")
  def chown(path: CString, owner: uid_t, group: gid_t): CInt = extern

  // Macros

  @name("scalanative_environ")
  def environ: Ptr[CString] = extern

}
