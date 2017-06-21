package scala.scalanative
package posix

import scalanative.native._
import scalanative.posix.sys.stat.mode_t
import scalanative.posix.sys.stat.mode_t

@extern
object fcntl {

  @name("scalanative_fcntl_open")
  def open(pathname: CString, flags: CInt): CInt = extern

  @name("scalanative_fcntl_open_with_mode")
  def open(pathname: CString, flags: CInt, mode: UInt): CInt = extern

  @name("scalanative_fcntl_close")
  def close(fd: CInt): CInt = extern

  @name("scalanative_fcntl_fcntl")
  def fcntl(fd: CInt, cmd: CInt, args: CVararg*): CInt = extern

  @name("scalanative_o_rdonly")
  def O_RDONLY: CInt = extern

  @name("scalanative_o_wronly")
  def O_WRONLY: CInt = extern

  @name("scalanative_o_rdwr")
  def O_RDWR: CInt = extern

  @name("scalanative_o_append")
  def O_APPEND: CInt = extern

  @name("scalanative_o_creat")
  def O_CREAT: CInt = extern

  @name("scalanative_o_trunc")
  def O_TRUNC: CInt = extern

  @name("scalanative_w_ok")
  def W_OK: CInt = extern

  @name("scalanative_r_ok")
  def R_OK: CInt = extern

  @name("scalanative_x_ok")
  def X_OK: CInt = extern

  @name("scalanative_f_ok")
  def F_OK: CInt = extern

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

  @name("scalanative_f_getown")
  def F_GETOWN: CInt = extern

  @name("scalanative_f_setown")
  def F_SETOWN: CInt = extern

  @name("scalanative_f_getlk")
  def F_GETLK: CInt = extern

  @name("scalanative_f_setlk")
  def F_SETLK: CInt = extern

  @name("scalanative_f_setlkw")
  def F_SETLKW: CInt = extern

}
