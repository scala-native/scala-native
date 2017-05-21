package scala.scalanative
package posix

import scala.scalanative.native._
import scala.scalanative.posix.sys.types.{off_t, pid_t}
import stat.mode_t

// http://pubs.opengroup.org/onlinepubs/9699919799/

@extern
object fcntl {

  def creat(pathname: CString, mode: mode_t): CInt = extern

  def fcntl(fd: CInt, cmd: CInt, args: CVararg*): CInt = extern

  def open(pathname: CString, flags: CInt, mode: CVararg*): CInt = extern

  def openat(dirfd: CInt,
             pathname: CString,
             flags: CInt,
             args: CVararg*): CInt = extern

  def posix_fadvise(fd: CInt, offset: off_t, len: off_t, advice: CInt): CInt =
    extern

  def posix_fallocate(fd: CInt, offset: off_t, len: off_t): CInt = extern

  def close(fd: CInt): CInt = extern

  // Types
  type flock = CStruct5[CShort, CShort, off_t, off_t, pid_t]

  // Macros

  @name("scalanative_f_dupfd")
  def F_DUPFD: CInt = extern

  @name("scalanative_f_dupfd_cloexec")
  def F_DUPFD_CLOEXEC: CInt = extern

  @name("scalanative_f_getfd")
  def F_GETFD: CInt = extern

  @name("scalanative_f_setfd")
  def F_SETFD: CInt = extern

  @name("scalanative_f_getfl")
  def F_GETFL: CInt = extern

  @name("scalanative_f_setfl")
  def F_SETFL: CInt = extern

  @name("scalanative_f_getlk")
  def F_GETLK: CInt = extern

  @name("scalanative_f_setlk")
  def F_SETLK: CInt = extern

  @name("scalanative_f_setlkw")
  def F_SETLKW: CInt = extern

  @name("scalanative_f_getown")
  def F_GETOWN: CInt = extern

  @name("scalanative_f_setown")
  def F_SETOWN: CInt = extern

  @name("scalanative_fd_cloexec")
  def FD_CLOEXEC: CInt = extern

  @name("scalanative_f_rdlck")
  def F_RDCLK: CInt = extern

  @name("scalanative_f_unlck")
  def F_UNLCK: CInt = extern

  @name("scalanative_f_wrlck")
  def F_WRLCK: CInt = extern

  @name("scalanative_o_cloexec")
  def O_CLOEXEC: CInt = extern

  @name("scalanative_o_creat")
  def O_CREAT: CInt = extern

  @name("scalanative_o_directory")
  def O_DIRECTORY: CInt = extern

  @name("scalanative_o_excl")
  def O_EXCL: CInt = extern

  @name("scalanative_o_noctty")
  def O_NOCTTY: CInt = extern

  @name("scalanative_o_nofollow")
  def O_NOFOLLOW: CInt = extern

  @name("scalanative_o_trunc")
  def O_TRUNC: CInt = extern

  @name("scalanative_o_tty_init")
  def O_TTY_INIT: CInt = extern

  @name("scalanative_o_append")
  def O_APPEND: CInt = extern

  @name("scalanative_o_dysinc")
  def O_DYSINC: CInt = extern

  @name("scalanative_o_rsync")
  def O_RSYNC: CInt = extern

  @name("scalanative_o_sync")
  def O_SYNC: CInt = extern

  @name("scalanative_o_accmode")
  def O_ACCMODE: CInt = extern

  @name("scalanative_o_exec")
  def O_EXEC: CInt = extern

  @name("scalanative_o_rdonly")
  def O_RDONLY: CInt = extern

  @name("scalanative_o_wronly")
  def O_WRONLY: CInt = extern

  @name("scalanative_o_rdwr")
  def O_RDWR: CInt = extern

  @name("scalanative_o_search")
  def O_SEARCH: CInt = extern

  @name("scalanative_at_fdcwd")
  def AT_FDCWD: CInt = extern

  @name("scalanative_at_eacces")
  def AT_EACCES: CInt = extern

  @name("scalanative_at_symlink_nofollow")
  def AT_SYMLINK_NOFOLLOW: CInt = extern

  @name("scalanative_at_symlink_follow")
  def AT_SYMLINK_FOLLOW: CInt = extern

  @name("scalanative_at_removedir")
  def AT_REMOVEDIR: CInt = extern

  @name("scalanative_posix_fadv_dontneed")
  def POSIX_FADV_DONTNEED: CInt = extern

  @name("scalanative_posix_fadv_noreuse")
  def POSIX_FADV_NOREUSE: CInt = extern

  @name("scalanative_posix_fadv_normal")
  def POSIX_FADV_NORMAL: CInt = extern

  @name("scalanative_posix_fadv_random")
  def POSIX_FADV_RANDOM: CInt = extern

  @name("scalanative_posix_fadv_sequential")
  def POSIX_FADV_SEQUENTIAL: CInt = extern

  @name("scalanative_posix_fadv_willneed")
  def POSIX_FADV_WILLNEED: CInt = extern

  @name("scalanative_w_ok")
  def W_OK: CInt = extern

  @name("scalanative_r_ok")
  def R_OK: CInt = extern

  @name("scalanative_x_ok")
  def X_OK: CInt = extern

  @name("scalanative_f_ok")
  def F_OK: CInt = extern
}
