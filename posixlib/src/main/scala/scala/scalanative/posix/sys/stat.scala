package scala.scalanative
package posix
package sys

import scalanative.unsafe._
import scalanative.posix.time._
import scalanative.posix.sys.types._

@extern
object stat {

  /* This file is incomplete and DOES NOT comply with POSIX 2018.
   * It is useful in the time before it can be brought into compliance.
   */

  /* POSIX states that these types be declared "as described in <sys/types.h>".
   *
   * Although not face evident, that requirement is met here on 64 bit systems.
   * The various C*Long fields here and in types.h all describe 64 bits.
   *
   * 32 bit systems meet the requirement except for 4 types:
   * dev_t, ino_t, off_t, and blkcnt_t.
   *
   * Socket.c uses the variant types on 32 bit systems to adapt to differences
   * in how operating systems declare the type. That code is hard to follow
   * but seems to work; do not disturb its tranquility in the search for
   * purity or yours may be disturbed as a consequence.
   *
   * Because this is an "@extern" object, LinktimeInfo can not be used.
   * expressions, including necessary "if", are not allowed in such objects.
   */

  // Declare in the order they are used in 'struct stat'
  type dev_t = CUnsignedLong
  type ino_t = CUnsignedLongLong
  type uid_t = types.uid_t
  type gid_t = types.gid_t
  type off_t = CLongLong
  type blksize_t = types.blksize_t
  type blkcnt_t = CLongLong
  type nlink_t = types.nlink_t
  type mode_t = types.mode_t

  // This structure is _not_ a candidate for direct pass-thru to OS.
  type stat = CStruct13[
    dev_t, // st_dev
    dev_t, // st_rdev
    ino_t, // st_ino
    uid_t, // st_uid
    gid_t, // st_gid
    off_t, // st_size
    timespec, // st_atim or st_atimespec
    timespec, // st_mtim or st_mtimespec
    timespec, // st_ctim or st_ctimespec
    blkcnt_t, // st_blocks
    blksize_t, // st_blksize
    nlink_t, // st_nlink
    mode_t // st_mode
  ]

  /** stat gets file metadata from path
   *  @param path
   *    path to file/directory
   *  @param buf
   *    pointer to buffer into which stat struct is written.
   *  @return
   *    Return `0` on success. Otherwise return `-1` with `errno` being set.
   *    `errno` can be the followings:
   *    - EACCES(permission denied)
   *    - EBADF(invalid filedes)
   *    - EFAULT(wrong address)
   *    - ELOOP(too many symbolic links)
   *    - ENAMETOOLONG(too long name)
   *    - ENOENT(path component not found or path is empty string)
   *    - ENOMEM(kernel out of memory)
   *    - ENOTDIR(path component is not a directory)
   *  @example
   *    {{{
   *    import scala.scalanative.unsafe._
   *    import scala.scalanative.posix.sys.stat
   *    Zone { implicit z =>
   *      val s = alloc[stat.stat]()
   *      val code = stat.stat(filename,s)
   *      if (code == 0) {
   *        ???
   *      }
   *    }
   *    }}}
   */
  @name("scalanative_stat")
  def stat(path: CString, buf: Ptr[stat]): CInt = extern

  /** similar to [[stat]], but different in that `fstat` uses fd instead of path
   *  string.
   */
  @name("scalanative_fstat")
  def fstat(fildes: CInt, buf: Ptr[stat]): CInt = extern

  /** similar to [[stat]], but different in that `lstat` gets stat of the link
   *  itself instead of that of file the link refers to when path points to
   *  link.
   */
  @name("scalanative_lstat")
  def lstat(path: CString, buf: Ptr[stat]): CInt = extern

  // mkdir(), chmod(), & fchmod() are straight passthrough; "glue" needed.
  def mkdir(path: CString, mode: mode_t): CInt = extern
  def chmod(pathname: CString, mode: mode_t): CInt = extern
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

object statOps {
  implicit class statOps(val c: Ptr[stat.stat]) extends AnyVal {
    def st_dev: stat.dev_t = c._1
    def st_dev_=(dev_t: stat.dev_t): Unit = c._1 = dev_t
    def st_rdev: stat.dev_t = c._2
    def st_rdev_=(dev_t: stat.dev_t): Unit = c._2 = dev_t
    def st_ino: stat.ino_t = c._3
    def st_ino_=(ino_t: stat.ino_t): Unit = c._3 = ino_t
    def st_uid: uid_t = c._4
    def st_uid_=(uid: uid_t): Unit = c._4 = uid
    def st_gid: gid_t = c._5
    def st_gid_=(gid: gid_t): Unit = c._5 = gid
    def st_size: stat.off_t = c._6
    def st_size_=(size: stat.off_t): Unit = c._6 = size
    def st_atim: timespec = c._7
    def st_atim_=(t: timespec): Unit = c._7 = t
    def st_atime: time_t = c._7._1
    def st_atime_=(t: time_t): Unit = c._7._1 = t
    def st_mtim: timespec = c._8
    def st_mtim_=(t: timespec): Unit = c._8 = t
    def st_mtime_=(t: time_t): Unit = c._8._1 = t
    def st_mtime: time_t = c._8._1
    def st_ctim: timespec = c._9
    def st_ctim_=(t: timespec): Unit = c._9 = t
    def st_ctime: time_t = c._9._1
    def st_ctime_=(t: time_t): Unit = c._9._1 = t
    def st_blocks: stat.blkcnt_t = c._10
    def st_blocks_=(blc: stat.blkcnt_t): Unit = c._10 = blc
    def st_blksize: blksize_t = c._11
    def st_blksize_=(blcsize: blksize_t): Unit = c._11 = blcsize
    def st_nlink: nlink_t = c._12
    def st_nlink_=(nlink: nlink_t): Unit = c._12 = nlink
    def st_mode: mode_t = c._13
    def st_mode_=(mode: mode_t): Unit = c._13 = mode

    // helpers for Non POSIX(most likely Apple) st_* equivalents
    def st_atimespec: timespec = c._7
    def st_atimespec_=(t: timespec): Unit = c._7 = t
    def st_atimensec: time_t = c._7._1
    def st_atimensec_=(t: time_t): Unit = c._7._1 = t
    def st_mtimespec: timespec = c._8
    def st_mtimespec_=(t: timespec): Unit = c._8 = t
    def st_mtimensec: time_t = c._8._1
    def st_mtimensec_=(t: time_t): Unit = c._8._1 = t
    def st_ctimespec: timespec = c._9
    def st_ctimespec_=(t: timespec): Unit = c._9 = t
    def st_ctimensec: time_t = c._9._1
    def st_ctimensec_=(t: time_t): Unit = c._9._1 = t
  }
}
