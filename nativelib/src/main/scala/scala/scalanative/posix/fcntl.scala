package scala.scalanative
package posix

import native._

@extern
object fcntl {

  def open(pathname: CString, flags: CInt, mode: CVararg*): CInt = extern

  def close(fd: CInt): CInt = extern

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

  @name("scalanative_o_cloexec")
  def O_CLOEXEC: CInt = extern

  @name("scalanative_o_direct")
  def O_DIRECT: CInt = extern

  @name("scalanative_o_excl")
  def O_EXCL: CInt = extern

  @name("scalanative_o_nonblock")
  def O_NONBLOCK: CInt = extern

  @name("scalanative_w_ok")
  def W_OK: CInt = extern

  @name("scalanative_r_ok")
  def R_OK: CInt = extern

  @name("scalanative_x_ok")
  def X_OK: CInt = extern

  @name("scalanative_f_ok")
  def F_OK: CInt = extern

  @name("scalanative_at_fdcw")
  def AT_FDCW: CInt = extern

  @name("scalanative_at_symlink_nofollow")
  def AT_SYMLINK_NOFOLLOW: CInt = extern

  @name("scalanative_at_removedir")
  def AT_REMOVEDIR: CInt = extern

  @name("scalanative_at_symlink_follow")
  def AT_SYMLINK_FOLLOW: CInt = extern

  @name("scalanative_at_no_automount")
  def AT_NO_AUTOMOUNT: CInt = extern

  @name("scalanative_at_empty_path")
  def AT_EMPTY_PATH: CInt = extern

  @name("scalanative_at_fdcwd")
  def AT_FDCWD: CInt = extern

}
