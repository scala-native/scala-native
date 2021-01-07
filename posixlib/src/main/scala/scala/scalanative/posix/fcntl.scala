package scala.scalanative
package posix

import scalanative.unsafe._
import scalanative.posix.sys.stat.mode_t

@extern
object fcntl {

  def open(pathname: CString, flags: CInt): CInt = extern

  def open(pathname: CString, flags: CInt, mode: mode_t): CInt = extern

  def close(fd: CInt): CInt = extern

  def fcntl(fd: CInt, cmd: CInt, flags: CInt): CInt = extern

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
