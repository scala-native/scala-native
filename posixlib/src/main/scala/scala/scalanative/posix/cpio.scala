package scala.scalanative
package posix

import scalanative.unsafe._

@extern
object cpio {
  @name("scalanative_c_issock")
  def C_ISSOCK: CUnsignedShort = extern

  @name("scalanative_c_islnk")
  def C_ISLNK: CUnsignedShort = extern

  @name("scalanative_c_isctg")
  def C_ISCTG: CUnsignedShort = extern

  @name("scalanative_c_isreg")
  def C_ISREG: CUnsignedShort = extern

  @name("scalanative_c_isblk")
  def C_ISBLK: CUnsignedShort = extern

  @name("scalanative_c_isdir")
  def C_ISDIR: CUnsignedShort = extern

  @name("scalanative_c_ischr")
  def C_ISCHR: CUnsignedShort = extern

  @name("scalanative_c_isfifo")
  def C_ISFIFO: CUnsignedShort = extern

  @name("scalanative_c_isuid")
  def C_ISUID: CUnsignedShort = extern

  @name("scalanative_c_isgid")
  def C_ISGID: CUnsignedShort = extern

  @name("scalanative_c_isvtx")
  def C_ISVTX: CUnsignedShort = extern

  @name("scalanative_c_irusr")
  def C_IRUSR: CUnsignedShort = extern

  @name("scalanative_c_iwusr")
  def C_IWUSR: CUnsignedShort = extern

  @name("scalanative_c_ixusr")
  def C_IXUSR: CUnsignedShort = extern

  @name("scalanative_c_irgrp")
  def C_IRGRP: CUnsignedShort = extern

  @name("scalanative_c_iwgrp")
  def C_IWGRP: CUnsignedShort = extern

  @name("scalanative_c_ixgrp")
  def C_IXGRP: CUnsignedShort = extern

  @name("scalanative_c_iroth")
  def C_IROTH: CUnsignedShort = extern

  @name("scalanative_c_iwoth")
  def C_IWOTH: CUnsignedShort = extern

  @name("scalanative_c_ixoth")
  def C_IXOTH: CUnsignedShort = extern

  @name("scalanative_magic")
  def MAGIC: CString = extern
}
