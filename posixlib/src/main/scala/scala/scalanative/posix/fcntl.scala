package scala.scalanative
package posix

import scalanative.unsafe._
import scalanative.posix.sys.stat.mode_t
import scala.scalanative.posix.unistd.off_t
import scala.scalanative.posix.sys.types.pid_t

@extern
@define("__SCALANATIVE_POSIX_FCNTL")
object fcntl {

  def open(pathname: CString, flags: CInt): CInt = extern

  @name("scalanative_open_m")
  def open(pathname: CString, flags: CInt, mode: mode_t): CInt = extern

  @name("scalanative_fcntl_i")
  def fcntl(fd: CInt, cmd: CInt, flags: CInt): CInt = extern

  @name("scalanative_fcntl")
  def fcntl(fd: CInt, cmd: CInt, flock_struct: Ptr[flock]): CInt = extern

  type flock = CStruct5[
    off_t, // l_start starting offset
    off_t, // l_len len = 0 means until end of file
    pid_t, // l_pid lock owner
    CInt, // l_type lock type: read/write, etc.
    CInt // l_whence type of l_start
  ]

  @name("scalanative_f_dupfd")
  def F_DUPFD: CInt = extern

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
  def F_RDLCK: CInt = extern

  @name("scalanative_f_unlck")
  def F_UNLCK: CInt = extern

  @name("scalanative_f_wrlck")
  def F_WRLCK: CInt = extern

  @name("scalanative_o_creat")
  def O_CREAT: CInt = extern

  @name("scalanative_o_excl")
  def O_EXCL: CInt = extern

  @name("scalanative_o_noctty")
  def O_NOCTTY: CInt = extern

  @name("scalanative_o_trunc")
  def O_TRUNC: CInt = extern

  @name("scalanative_o_append")
  def O_APPEND: CInt = extern

  @name("scalanative_o_nonblock")
  def O_NONBLOCK: CInt = extern

  @name("scalanative_o_sync")
  def O_SYNC: CInt = extern

  @name("scalanative_o_accmode")
  def O_ACCMODE: CInt = extern

  @name("scalanative_o_rdonly")
  def O_RDONLY: CInt = extern

  @name("scalanative_o_rdwr")
  def O_RDWR: CInt = extern

  @name("scalanative_o_wronly")
  def O_WRONLY: CInt = extern
}

object fcntlOps {
  import fcntl._

  implicit class flockOps(val ptr: Ptr[flock]) extends AnyVal {
    def l_start: off_t = ptr._1
    def l_start_=(value: off_t): Unit = ptr._1 = value
    def l_len: off_t = ptr._2
    def l_len_=(value: off_t): Unit = ptr._2 = value
    def l_pid: pid_t = ptr._3
    def l_pid_=(value: pid_t): Unit = ptr._3 = value
    def l_type: CInt = ptr._4
    def l_type_=(value: CInt): Unit = ptr._4 = value
    def l_whence: CInt = ptr._5
    def l_whence_=(value: CInt): Unit = ptr._5 = value
  }

}
