package scala.scalanative
package posix

import scalanative.unsafe._
import scalanative.posix.sys.stat.{uid_t, gid_t}

@extern
object unistd {

  type off_t = CLongLong

  @name("scalanative__xopen_version")
  def _XOPEN_VERSION: CInt = extern

  var environ: Ptr[CString] = extern

  // optarg, opterr, optopt, and optopt are used by getopt().

  var optarg: CString = extern

  var opterr: CInt = extern

  var optind: CInt = extern

  var optopt: CInt = extern

// Methods/functions
  def _exit(status: CInt): Unit = extern
  def access(pathname: CString, mode: CInt): CInt = extern
  def chdir(path: CString): CInt = extern
  def close(fildes: CInt): CInt = extern
  def dup(fildes: CInt): CInt = extern
  def dup2(fildes: CInt, fildesnew: CInt): CInt = extern
  def execve(path: CString, argv: Ptr[CString], envp: Ptr[CString]): CInt =
    extern
  def fork(): CInt = extern
  def fsync(fildes: CInt): CInt = extern
  def ftruncate(fildes: CInt, length: off_t): CInt = extern
  def getcwd(buf: CString, size: CSize): CString = extern
  def gethostname(name: CString, len: CSize): CInt = extern
  def getopt(argc: CInt, argv: Ptr[CString], optstring: CString): CInt = extern

  def getpid(): CInt = extern
  def getppid(): CInt = extern
  def getuid(): uid_t = extern
  def lseek(fildes: CInt, offset: off_t, whence: CInt): off_t = extern
  def pipe(fildes: Ptr[CInt]): CInt = extern
  def read(fildes: CInt, buf: Ptr[_], nbyte: CSize): CInt = extern
  def readlink(path: CString, buf: CString, bufsize: CSize): CInt = extern
  def sethostname(name: CString, len: CSize): CInt = extern
  def sleep(seconds: CUnsignedInt): CUnsignedInt = extern
  def truncate(path: CString, length: off_t): CInt = extern
  def unlink(path: CString): CInt = extern

  // Maintainer: See 'Developer Note' in Issue #2395 about complete removal.
  @deprecated(
    "Removed in POSIX.1-2008. Use POSIX time.h nanosleep().",
    "posixlib 0.4.5"
  )
  def usleep(usecs: CUnsignedInt): CInt = extern

  def vfork(): CInt = extern
  def write(fildes: CInt, buf: Ptr[_], nbyte: CSize): CInt = extern

  @name("scalanative_chown")
  def chown(path: CString, owner: uid_t, group: gid_t): CInt = extern

  @name("scalanative_link")
  def link(path1: CString, path2: CString): CInt = extern

  @name("scalanative_linkat")
  def linkat(
      fd1: CInt,
      path1: CString,
      fd2: CInt,
      path2: CString,
      flag: CInt
  ): CInt = extern

  // NULL, see POSIX stddef

  @name("scalanative_f_ok")
  def F_OK: CInt = extern

  @name("scalanative_r_ok")
  def R_OK: CInt = extern

  @name("scalanative_w_ok")
  def W_OK: CInt = extern

  @name("scalanative_x_ok")
  def X_OK: CInt = extern

  // SEEK_CUR, SEEK_END, SEEK_SET, see clib stdio

  @name("scalanative_f_lock")
  def F_LOCK: CInt = extern

  @name("scalanative_f_test")
  def F_TEST: CInt = extern

  @name("scalanative_f_tlock")
  def F_TLOCK: CInt = extern

  @name("scalanative_f_ulock")
  def F_ULOCK: CInt = extern

  @name("scalanative_stderr_fileno")
  def STDERR_FILENO: CInt = extern

  @name("scalanative_stdin_fileno")
  def STDIN_FILENO: CInt = extern

  @name("scalanative_stdout_fileno")
  def STDOUT_FILENO: CInt = extern

  @name("scalanative_symlink")
  def symlink(path1: CString, path2: CString): CInt = extern

  @name("scalanative_symlinkat")
  def symlinkat(path1: CString, fd: CInt, path2: CString): CInt = extern

}
