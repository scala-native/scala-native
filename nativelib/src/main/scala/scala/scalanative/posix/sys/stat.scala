package scala.scalanative.posix.sys

import scala.scalanative.native.Nat._2
import scala.scalanative.native._
import scala.scalanative.posix.sys.types._
import scala.scalanative.runtime.time.{timespec, time_t}

/**
 * Created by remi on 01/03/17.
 */
@extern
object stat {

  def stat(pathname: CString, buf: Ptr[stat]): CInt  = extern
  def fstat(fd: CInt, buf: Ptr[stat]): CInt          = extern
  def lstat(pathname: CString, bug: Ptr[stat]): CInt = extern
  def fstatat(dirfd: CInt,
              pathname: CString,
              buf: Ptr[stat],
              flags: CInt): CInt                   = extern
  def chmod(pathname: CString, mode: mode_t): CInt = extern
  def fchmod(fd: CInt, mode: mode_t): CInt         = extern
  def fchmodat(dirfd: CInt,
               pathname: CString,
               mode: mode_t,
               flags: CInt): CInt                                 = extern
  def umask(mask: mode_t): mode_t                                 = extern
  def getumask(): mode_t                                          = extern
  def mkdir(pathname: CString, mode: mode_t): CInt                = extern
  def mkdirat(dirfd: CInt, pathname: CString, mode: mode_t): CInt = extern
  def mknod(pathname: CString, mode: mode_t, dev: dev_t): CInt    = extern
  def mknodat(dirfd: CInt, pathname: CString, mode: mode_t, dev: dev_t): CInt =
    extern
  def mkfifo(pathname: CString, mode: mode_t): CInt                = extern
  def mkfifoat(dirfd: CInt, pathname: CString, mode: mode_t): CInt = extern
  def utimensat(dirfd: CInt,
                pathname: CString,
                times: CArray[timespec, _2],
                flags: CInt): CInt                          = extern
  def futimens(fd: CInt, times: CArray[timespec, _2]): CInt = extern

  // Types
  type stat = CStruct13[dev_t,
                        ino_t,
                        mode_t,
                        nlink_t,
                        uid_t,
                        gid_t,
                        dev_t,
                        off_t,
                        time_t,
                        time_t,
                        time_t,
                        blksize_t,
                        blkcnt_t]

  // Macros
  @name("scalanative_s_isuid")
  def S_ISUID = extern
  @name("scalanative_s_isgid")
  def S_ISGID = extern
  @name("scalanative_s_isvtx")
  def S_ISVTX = extern
  @name("scalanative_s_irusr")
  def S_IRUSR = extern
  @name("scalanative_s_iwusr")
  def S_IWUSR = extern
  @name("scalanative_s_ixusr")
  def S_IXUSR = extern
  @name("scalanative_s_irgrp")
  def S_IRGRP = extern
  @name("scalanative_s_iwgrp")
  def S_IWGRP = extern
  @name("scalanative_s_ixgrp")
  def S_IXGRP = extern
  @name("scalanative_s_iroth")
  def S_IROTH = extern
  @name("scalanative_s_iwoth")
  def S_WOTH = extern
  @name("scalanative_s_ixoth")
  def S_IXOTH = extern

}
