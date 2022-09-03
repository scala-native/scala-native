package scala.scalanative
package posix
package sys

import scalanative.unsafe._
import scalanative.posix.time._
import scalanative.posix.sys.types._

import scalanative.meta.LinktimeInfo.is32BitPlatform

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
    time_t, // st_atime
    time_t, // st_mtime
    time_t, // st_ctime
    blkcnt_t, // st_blocks
    blksize_t, // st_blksize
    nlink_t, // st_nlink
    mode_t // st_mode
  ]

  @name("scalanative_stat")
  def stat(path: CString, buf: Ptr[stat]): CInt = extern

  @name("scalanative_fstat")
  def fstat(fildes: CInt, buf: Ptr[stat]): CInt = extern

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
