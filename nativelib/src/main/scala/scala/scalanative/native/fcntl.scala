package scala.scalanative
package native

@extern
object fcntl {

  def open(pathname: CString, flags: CInt): CInt = extern

  def close(fd: CInt): CInt = extern

  @name("scalanative_o_rdonly")
  def O_RDONLY: CInt = extern

  @name("scalanative_o_wronly")
  def O_WRONLY: CInt = extern

  @name("scalanative_o_rdwr")
  def O_RDWR: CInt = extern

  @name("scalanative_o_append")
  def O_APPEND: CInt = extern

  @name("scalanative_w_ok")
  def W_OK: CInt = extern

  @name("scalanative_r_ok")
  def R_OK: CInt = extern

  @name("scalanative_x_ok")
  def X_OK: CInt = extern

  @name("scalanative_f_ok")
  def F_OK: CInt = extern
}
