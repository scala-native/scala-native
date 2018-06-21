package scala.scalanative
package posix

import scalanative.native._
// import scalanative.native.stdint._ // as soon as it is implemented.
import scalanative.native.stdio._
import scalanative.native.stdlib._
import scalanative.posix.sys.types

@extern
object unistd {

  type gid_t      = types.gid_t
  type size_t     = types.size_t
  type ssize_t    = types.ssize_t
  type off_t      = types.off_t
  type pid_t      = types.pid_t
  type uid_t      = types.uid_t
  type useconds_t = CUnsignedInt // primary definition here, not types.scala

  type intptr_t = Ptr[CInt] // Explicit because no stdint.scala yet

  def access(pathname: CString, mode: CInt): CInt = extern
  def alarm(seconds: CUnsignedInt): CUnsignedInt  = extern

  def chdir(path: CString): CInt                             = extern
  def chown(path: CString, owner: uid_t, group: gid_t): CInt = extern
  def close(fildes: CInt): CInt                              = extern
  def confstr(name: CInt, buf: Ptr[_], len: size_t): size_t  = extern
  def crypt(key: Ptr[CChar], salt: Ptr[CChar]): Ptr[CChar]   = extern
  // crypt_r is GNU. data should be Ptr[struct crypt_data].
  def crypt_r(key: Ptr[CChar], salt: Ptr[CChar], data: Ptr[CChar]): Ptr[CChar] =
    extern

  def dup(fildes: CInt): CInt                   = extern
  def dup2(fildes: CInt, fildesnew: CInt): CInt = extern

  def _exit(status: CInt): Unit                      = extern
  def encrypt(block: Ptr[CChar], edflag: CInt): Unit = extern
  // encrypt_r is GNU. data should be Ptr[struct crypt_data].
  def encrypt_r(block: Ptr[CChar], edflag: CInt, data: Ptr[CChar]): Unit =
    extern
  def execl(path: CString, arg: Ptr[CString], args: CVararg*): CInt =
    extern
  def execle(path: CString, arg: Ptr[CString], args: CVararg*): CInt =
    extern
  def execlp(file: CString, arg: Ptr[CString], args: CVararg*): CInt =
    extern
  def execv(path: CString, argv: Ptr[CString]): CInt =
    extern
  def execve(path: CString, argv: Ptr[CString], envp: Ptr[CString]): CInt =
    extern
  def execvp(file: CString, argv: Ptr[CString]): CInt =
    extern
  // execvpe() is GNU
  def execvpe(file: CString, argv: Ptr[CString], envp: Ptr[CString]): CInt =
    extern

  def faccessat(dirfd: CInt, pathname: CString, mode: CInt, flags: CInt): CInt =
    extern
  def fchdir(fd: CInt): CInt                         = extern
  def fchown(fd: CInt, uid: uid_t, gid: gid_t): CInt = extern
  def fchownat(dirfd: CInt, pathname: CString, uid: uid_t, gid: gid_t): CInt =
    extern
  def fexecve(fd: CInt, argv: Ptr[CString], envp: Ptr[CString]): CInt =
    extern
  def fork(): CInt                                 = extern
  def fpathconf(fd: CInt, name: CInt): CLong       = extern
  def fsync(fildes: CInt): CInt                    = extern
  def ftruncate(fildes: CInt, length: off_t): CInt = extern

  def getcwd(buf: Ptr[CChar], size: size_t): CString                    = extern
  def getegid(): gid_t                                                  = extern
  def geteuid(): uid_t                                                  = extern
  def getgid(): gid_t                                                   = extern
  def getgroups(size: CInt, list: Ptr[gid_t]): CInt                     = extern
  def gethostid(): CLong                                                = extern
  def gethostname(name: CString, len: size_t): CInt                     = extern
  def getlogin(): CString                                               = extern
  def getlogin_r(buf: Ptr[CChar], bufsize: size_t): CInt                = extern
  def getopt(argc: CInt, argv: Ptr[CChar], optstring: Ptr[CChar]): CInt = extern
  def getpgid(pid: pid_t): pid_t                                        = extern
  def getpgrp(): pid_t                                                  = extern
  def getpid(): pid_t                                                   = extern
  def getppid(): pid_t                                                  = extern
  def getsid(pid: pid_t): pid_t                                         = extern
  def getuid(): uid_t                                                   = extern

  def isatty(fd: CInt): CInt = extern

  def lchown(pathname: CString, uid: uid_t, gid: gid_t): CInt = extern
  def link(path1: CString, path2: CString): CInt              = extern
  def linkat(fd1: CInt,
             path1: CString,
             fd2: CInt,
             path2: CString,
             flag: CInt): CInt                                = extern
  def lockf(fd: CInt, cmd: CInt, len: off_t): CInt            = extern
  def lseek(fildes: CInt, offset: off_t, whence: CInt): off_t = extern

  def nice(inc: CInt): CInt = extern

  def pathconf(path: CString, name: CInt): CLong = extern
  def pause(): CInt                              = extern
  def pipe(fildes: Ptr[CInt]): CInt              = extern
  def pread(fd: CInt, buf: Ptr[_], count: size_t, offset: off_t): ssize_t =
    extern
  def pwrite(fd: CInt, buf: Ptr[_], count: size_t, offset: off_t): ssize_t =
    extern

  def read(fildes: CInt, buf: Ptr[_], nbyte: size_t): ssize_t = extern
  def readlink(path: CString, buf: Ptr[CChar], bufsize: size_t): ssize_t =
    extern
  def readlinkat(dirfd: CInt,
                 pathname: CString,
                 buf: Ptr[CChar],
                 bufsize: size_t): ssize_t = extern
  def rmdir(pathname: CString): CInt       = extern

  def setegid(egid: gid_t): CInt                                = extern
  def seteuid(uid: uid_t): CInt                                 = extern
  def setgid(gid: gid_t): CInt                                  = extern
  def sethostname(name: CString, len: size_t): CInt             = extern
  def setpgid(pid: pid_t, pgid: pid_t): CInt                    = extern
  def setsid(): pid_t                                           = extern
  def setuid(uid: uid_t): pid_t                                 = extern
  def sleep(seconds: CUnsignedInt): CUnsignedInt                = extern
  def swab(from: Ptr[_], to: Ptr[_], n: ssize_t): Unit          = extern
  def symlink(path1: CString, path2: CString): CInt             = extern
  def symlinkat(path1: CString, fd: CInt, path2: CString): CInt = extern
  def sync(): Unit                                              = extern
  def sysconf(name: CInt): CLongInt                             = extern

  def tcgetpgrp(fd: CInt): pid_t                                 = extern
  def tcsetgrp(fd: CInt, pgrp: pid_t): CInt                      = extern
  def truncate(path: CString, length: off_t): CInt               = extern
  def ttyname(fd: CInt): CString                                 = extern
  def ttyname_r(fd: CInt, buf: Ptr[CChar], buflen: size_t): CInt = extern

  def unlink(path: CString): CInt                                 = extern
  def unlinkat(dirfd: CInt, pathname: CString, flags: CInt): CInt = extern
  def usleep(usec: useconds_t): CInt                              = extern

  def vfork(): pid_t = extern

  def write(fildes: CInt, buf: Ptr[_], nbyte: size_t): ssize_t = extern

  // "Constants" for Functions

  // access()
  @name("scalanative_f_ok")
  def F_OK: CInt = extern

  @name("scalanative_r_ok")
  def R_OK: CInt = extern

  @name("scalanative_w_ok")
  def W_OK: CInt = extern

  @name("scalanative_x_ok")
  def X_OK: CInt = extern

  // file streams
  @name("scalanative_stderr_fileno")
  def STDERR_FILENO: CInt = extern

  @name("scalanative_stdin_fileno")
  def STDIN_FILENO: CInt = extern

  @name("scalanative_stdout_fileno")
  def STDOUT_FILENO: CInt = extern

  // SEEK_CUR, SEEK_END, & SEEK_SET are visible because of
  // "import scalanative.native.stdio._" above.

  // Macros

  @name("scalanative_environ")
  def environ: Ptr[CString] = extern // GNU extension

}
