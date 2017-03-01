package scala.scalanative.posix

import scala.scalanative.native.Nat._2
import scala.scalanative.native.{CArray, CInt, CLongLong, CSize, CString, CUnsignedInt, Ptr, extern, name}
import scala.scalanative.posix.sys.types.{gid_t, pid_t, ssize_t, uid_t}

@extern
object unistd {

  type off_t = CLongLong

  def sleep(seconds: CUnsignedInt): Int = extern

  def usleep(usecs: CUnsignedInt): Int  = extern

  def alarm(seconds: CUnsignedInt): CUnsignedInt = extern

  def access(pathname: CString, mode: CInt): CInt = extern

  def faccessat(dirfd: CInt, pathname: CString, mode: CInt, flags: CInt): CInt = extern

  def lseek(fd: CInt, offset: off_t, whence: CInt): off_t = extern

  def close(fd: CInt): CInt = extern

  def read(fd: CInt, buf: Ptr[Byte], count: CSize): CSize = extern

  def write(fd: CInt, buf: Ptr[Byte], count: CSize): ssize_t = extern

  def pipe(pipefd: CArray[CInt, _2]): CInt = extern

  def pipe2(pipefd: CArray[CInt, _2], flags: CInt): CInt = extern

  def pause(): CInt = extern

  def chown(pathname: CString, owner: uid_t, group: gid_t): CInt = extern

  def fchown(fd: CInt, owner: uid_t, group: gid_t): CInt = extern

  def lchown(pathmame: CString, owner: uid_t, group: gid_t): CInt = extern

  def fchownat(dirfd: CInt, pathname: CString, owner: uid_t, group: gid_t, flags: CInt): CInt = extern

  def chdir(path: CString): CInt = extern

  def fchdir(fd: CInt): CInt = extern

  def getcwd(buf: CString, size: CSize): CString = extern

  def getwd(buf: CString): CString = extern

  def get_current_dir_name(): CString = extern

  def dup(oldfd: CInt): CInt = extern

  def dup2(olfd: CInt, newfd: CInt): CInt = extern

  def dup3(oldfd: CInt, newfd: CInt, flags: CInt): CInt = extern

  def execve(filename: CString, argv: Ptr[CString], envp: Ptr[CString]): CInt = extern

  def nice(inc: CInt): CInt = extern

  def _exit(status: CInt): Unit = extern

  def getpid(): pid_t = extern

  def getppid(): pid_t = extern

  def setpgid(pid: pid_t, pgid: pid_t): CInt = extern

  def getpgid(pid: pid_t): pid_t = extern

  def getpgrp(): pid_t = extern

  def setsid(): pid_t = extern

  def getsid(pid: pid_t): pid_t = extern

  def getuid(): uid_t = extern

  def geteuid(): uid_t = extern

  def getgid(): gid_t = extern

  def getegid(): gid_t = extern

  def getgroups(size: CInt, list: Ptr[gid_t]): CInt = extern

  def setuid(uid: uid_t): CInt = extern

  def setreuid(ruid: uid_t, euid: uid_t): CInt = extern

  def setregid(rgid: gid_t, egid: gid_t): CInt = extern

  def setgid(gid: gid_t): CInt = extern

  def getresuid(ruid: Ptr[uid_t], euid: Ptr[uid_t], suid: Ptr[uid_t]): CInt = extern

  def getresgid(rgid: Ptr[gid_t], egid: Ptr[gid_t], sgid: Ptr[gid_t]): CInt = extern

  def setresuid(ruid: uid_t, euid: uid_t, suid: uid_t): CInt = extern

  def setresgid(rgid: gid_t, egid: gid_t, sgid: gid_t): CInt = extern

  def fork(): pid_t = extern

  def vfork(): pid_t = extern

  def link(oldpath: CString, newpath: CString): CInt = extern

  def linkat(olddirfd: CInt, oldpath: CString, newdirfd: CInt, newpath: CString, flags: CInt): CInt = extern

  def symlink(target: CString, linkpath: CString): CInt = extern

  def symlinkat(target: CString, newdirfd: CInt, linkpath: CString): CInt = extern

  def readlink(pathname: CString, buf: CString, bufsize: CSize): ssize_t = extern

  def readlinkat(dirfd: CInt, pathname: CString, bug: CString, bufsize: CSize): ssize_t = extern

  def unlink(pathname: CString): CInt = extern

  def unlinkat(dirfd: CInt, pathname: CString, flags: CInt): CInt = extern

  def rmdir(pathname: CString): CInt = extern

  def vhangup(): CInt = extern

  def acct(filename: CString): CInt = extern

  def chroot(path: CString): CInt = extern

  def fsync(fd: CInt): CInt = extern

  def fdatasync(fd: CInt): CInt = extern

  def sync(): Unit = extern

  def syncfs(fd: CInt): CInt = extern

  def getpagesize(): CInt = extern

  def truncate(path: CString, length: off_t): CInt = extern

  def ftruncate(fd: CInt, length: off_t): CInt = extern

  def brk(addr: Ptr[Byte]): CInt = extern

  //def sbrk(increment: intptr_t): Unit = extern


  // Macros
  @name("scalanative_stdin_fileno")
  def STDIN_FILENO: CInt = extern

  @name("scalanative_stdout_fileno")
  def STDOUT_FILENO: CInt = extern

  @name("scalanative_stderr_fileno")
  def STDERR_FILENO: CInt = extern

  @name("scalanative_environ")
  def environ: Ptr[CString] = extern

  @name("scalanative_r_ok")
  def R_OK: CInt = extern

  @name("scalanative_w_ok")
  def W_OK: CInt = extern

  @name("scalanative_x_ok")
  def X_OK: CInt = extern

  @name("scalanative_f_ok")
  def F_OK: CInt = extern

  @name("scalanative__gnu_source")
  def _GNU_SOURCE: CInt = extern

}
