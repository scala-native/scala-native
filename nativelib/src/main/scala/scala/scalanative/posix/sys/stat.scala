package scala.scalanative
package posix
package sys

import scalanative.native._
import scalanative.posix.unistd.off_t

@extern
object stat {
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

  @name("scalanative_stat")
  def stat(path: CString, buf: Ptr[stat]): CInt = extern

  @name("scalanative_fstat")
  def fstat(fildes: CInt, buf: Ptr[stat]): CInt = extern

  @name("scalanative_lstat")
  def lstat(path: CString, buf: Ptr[stat]): CInt = extern

  @name("scalanative_mkdir")
  def mkdir(path: CString, mode: mode_t): CInt = extern

  @name("scalanative_chmod")
  def chmod(pathname: CString, mode: mode_t): CInt = extern

  @name("scalanative_fchmod")
  def fchmod(fd: CInt, mode: mode_t): CInt = extern

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
