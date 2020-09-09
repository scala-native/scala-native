package scala.scalanative
package posix
package sys

import scalanative.unsafe._
import scalanative.posix.timeOps._

@extern
object stat {

  type timespec = posix.time.timespec

  type blkcnt_t  = types.blkcnt_t
  type blksize_t = types.blksize_t
  type dev_t     = types.dev_t
  type gid_t     = types.gid_t
  type ino_t     = types.ino_t
  type mode_t    = types.mode_t
  type nlink_t   = types.nlink_t
  type off_t     = types.off_t
  type uid_t     = types.uid_t

  type stat = CStruct13[dev_t, // st_dev
                        dev_t,     // st_rdev
                        ino_t,     // st_ino
                        uid_t,     // st_uid
                        gid_t,     // st_gid
                        off_t,     // st_size
                        timespec,  // st_atim
                        timespec,  // st_mtim
                        timespec,  // st_ctim
                        blkcnt_t,  // st_blocks
                        blksize_t, // st_blksize
                        nlink_t,   // st_nlink
                        mode_t]    // st_mode

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

  def futimens(fd: CInt, times: Ptr[timespec]): CInt = extern

  def utimensat(dirfd: CInt,
                pathname: CString,
                times: Ptr[timespec],
                flags: CInt): CInt = extern

  // Constants

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

  @name("scalanative_utime_now")
  def UTIME_NOW: CLong = extern

  @name("scalanative_utime_omit")
  def UTIME_OMIT: CLong = extern
}

object statOps {

  import posix.sys.stat._
  import posix.time.time_t

  implicit class statOps(val ptr: Ptr[sys.stat.stat]) extends AnyVal {

    def st_dev: dev_t         = ptr._1
    def st_rdev: dev_t        = ptr._2
    def st_ino: ino_t         = ptr._3
    def st_uid: uid_t         = ptr._4
    def st_gid: gid_t         = ptr._5
    def st_size: off_t        = ptr._6
    def st_atim: timespec     = ptr.at7 // seconds & nanoseconds
    def st_atime: time_t      = ptr.at7.tv_sec // seconds
    def st_mtim: timespec     = ptr.at8 // seconds & nanoseconds
    def st_mtime: time_t      = ptr.at8.tv_sec // seconds
    def st_ctim: timespec     = ptr.at9 // seconds & nanoseconds
    def st_ctime: time_t      = ptr.at9.tv_sec // seconds
    def st_blocks: blkcnt_t   = ptr._10
    def st_blksize: blksize_t = ptr._11
    def st_nlink: nlink_t     = ptr._12
    def st_mode: mode_t       = ptr._13

    def st_dev_(v: dev_t): Unit         = ptr._1 = v
    def st_rdev_(v: dev_t): Unit        = ptr._2 = v
    def st_ino_(v: ino_t): Unit         = ptr._3 = v
    def st_uid_(v: uid_t): Unit         = ptr._4 = v
    def st_gid_(v: gid_t): Unit         = ptr._5 = v
    def st_size_(v: off_t): Unit        = ptr._6 = v
    def st_atim_(v: timespec): Unit     = ptr._7 = v
    def st_atime_(v: time_t): Unit      = ptr._7._1 = v
    def st_mtim_(v: timespec): Unit     = ptr._8 = v
    def st_mtime_(v: time_t): Unit      = ptr._8._1 = v
    def st_ctim_(v: timespec): Unit     = ptr._9 = v
    def st_ctime_(v: time_t): Unit      = ptr._9._1 = v
    def st_blocks_(v: blkcnt_t): Unit   = ptr._10 = v
    def st_blksize_(v: blksize_t): Unit = ptr._11 = v
    def st_nlink_(v: nlink_t): Unit     = ptr._12 = v
    def st_mode_(v: mode_t): Unit       = ptr._13 = v
  }
}
