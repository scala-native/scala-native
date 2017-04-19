package scala.scalanative.posix.sys

import scala.scalanative.native.Nat._2
import scala.scalanative.native.time.timespec
import scala.scalanative.native.{
  CArray,
  CInt,
  CLong,
  CLongLong,
  CString,
  CStruct13,
  CUnsignedInt,
  CUnsignedLong,
  CUnsignedLongLong,
  Ptr,
  extern,
  name,
  time
}
import scala.scalanative.posix.unistd.off_t

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
  type dev_t     = CUnsignedLong
  type ino_t     = CUnsignedLongLong
  type mode_t    = CUnsignedInt
  type nlink_t   = CUnsignedLong
  type uid_t     = CUnsignedInt
  type gid_t     = CUnsignedInt
  type blksize_t = CLong
  type blkcnt_t  = CLongLong
  type stat = CStruct13[dev_t, // st_dev
                        dev_t, // st_rdev
                        ino_t, // st_ino
                        uid_t, // st_uid
                        gid_t, // st_gid
                        off_t, // st_size
                        time.time_t, // st_atime
                        time.time_t, // st_mtime
                        time.time_t, // st_ctime
                        blkcnt_t, // st_blocks
                        blksize_t, // st_blksize
                        nlink_t, // st_nlink
                        mode_t] // st_mode

  // Macros
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

}
