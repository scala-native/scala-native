package scala.scalanative
package posix
package sys

import scalanative.native._
import scalanative.posix.sys.types._

@extern
object stat {

  type blkcnt_t  = types.blkcnt_t
  type blksize_t = types.blksize_t
  type dev_t     = types.dev_t
  type gid_t     = types.gid_t
  type ino_t     = types.ino_t
  type mode_t    = types.mode_t
  type nlink_t   = types.nlink_t
  type off_t     = types.off_t
  type uid_t     = types.uid_t

  type timespec = posix.time.timespec

  // fstat(), fstatat(), lstat(), and fstat() all use this CStruct because
  // the order of fields can vary across operating systems.
  // This use necessitates translation code for those methods/functions in
  // resources/stat.c. Other methods can be straight passthrough.

  type stat = CStruct13[dev_t, // st_dev
                        dev_t, // st_rdev
                        ino_t, // st_ino
                        uid_t, // st_uid
                        gid_t, // st_gid
                        off_t, // st_size
                        time_t, // st_atime
                        time_t, // st_mtime
                        time_t, // st_ctime
                        blkcnt_t, // st_blocks
                        blksize_t, // st_blksize
                        nlink_t, // st_nlink
                        mode_t] // st_mode

  def chmod(pathname: CString, mode: mode_t): CInt = extern
  def fchmod(fd: CInt, mode: mode_t): CInt         = extern
  def fchmodat(dirfd: CInt,
               pathname: CString,
               mode: mode_t,
               flags: CInt): CInt = extern

  @name("scalanative_fstat")
  def fstat(fildes: CInt, buf: Ptr[stat]): CInt = extern

  @name("scalanative_fstatat")
  def fstatat(dirfd: CInt,
              pathname: CString,
              statbuf: Ptr[stat],
              flags: CInt): CInt = extern

  def futimesns(fd: CInt, times: Ptr[timespec]): CInt = extern

  @name("scalanative_lstat")
  def lstat(path: CString, buf: Ptr[stat]): CInt = extern

  def mkdir(pathname: CString, mode: mode_t): CInt                 = extern
  def mkdirat(dirfd: CInt, pathname: CString, mode: mode_t): CInt  = extern
  def mkfifo(pathname: CString, mode: mode_t): CInt                = extern
  def mkfifoat(dirfd: CInt, pathname: CString, mode: mode_t): CInt = extern
  def mknod(pathname: CString, mode: mode_t, dev: dev_t): CInt     = extern
  def mknodat(dirfd: CInt, pathname: CString, mode: mode_t, dev: dev_t): CInt =
    extern

  @name("scalanative_stat")
  def stat(path: CString, buf: Ptr[stat]): CInt = extern

  def umask(mode: mode_t): mode_t = extern
  def utimesns(dirfd: CInt,
               pathname: CString,
               times: Ptr[timespec],
               flags: CInt): CInt = extern

  @name("scalanative_s_isdir")
  def S_ISDIR(mode: mode_t): CInt = extern

  @name("scalanative_s_isreg")
  def S_ISREG(mode: mode_t): CInt = extern

  @name("scalanative_s_ischr")
  def S_ISCHR(mode: mode_t): CInt = extern

  @name("scalanative_s_isblk")
  def S_ISBLK(mode: mode_t): CInt = extern

  @name("scalanative_s_isfifo")
  def S_ISFIFO(mode: mode_t): CInt = extern

  @name("scalanative_s_islnk")
  def S_ISLNK(mode: mode_t): CInt = extern

  @name("scalanative_s_issock")
  def S_ISSOCK(mode: mode_t): CInt = extern

  @name("scalanative_s_isuid")
  def S_ISUID: mode_t = extern

  @name("scalanative_s_isgid")
  def S_ISGID: mode_t = extern

  @name("scalanative_s_isvtx")
  def S_ISVTX: mode_t = extern

  @name("scalanative_s_irusr")
  def S_IRUSR: mode_t = extern

  @name("scalanative_s_iwusr")
  def S_IWUSR: mode_t = extern

  @name("scalanative_s_ixusr")
  def S_IXUSR: mode_t = extern

  @name("scalanative_s_irgrp")
  def S_IRGRP: mode_t = extern

  @name("scalanative_s_iwgrp")
  def S_IWGRP: mode_t = extern

  @name("scalanative_s_ixgrp")
  def S_IXGRP: mode_t = extern

  @name("scalanative_s_iroth")
  def S_IROTH: mode_t = extern

  @name("scalanative_s_iwoth")
  def S_IWOTH: mode_t = extern

  @name("scalanative_s_ixoth")
  def S_IXOTH: mode_t = extern

}
