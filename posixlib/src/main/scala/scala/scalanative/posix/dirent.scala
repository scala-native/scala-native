package scala.scalanative
package posix

import scala.scalanative.unsafe._, Nat._

@extern
object dirent {

  type _256 = Digit3[_2, _5, _6]
  type DIR = CStruct0
  type dirent =
    CStruct3[CUnsignedLongLong, CArray[CChar, _256], CShort]

  @name("scalanative_opendir")
  def opendir(name: CString): Ptr[DIR] = extern

  @name("scalanative_readdir")
  def readdir(dirp: Ptr[DIR], buf: Ptr[dirent]): CInt = extern

  @name("scalanative_closedir")
  def closedir(dirp: Ptr[DIR]): CInt = extern

  @name("scalanative_dt_unknown")
  def DT_UNKNOWN: CInt = extern
  @name("scalanative_dt_fifo")
  def DT_FIFO: CInt = extern
  @name("scalanative_dt_chr")
  def DT_CHR: CInt = extern
  @name("scalanative_dt_dir")
  def DT_DIR: CInt = extern
  @name("scalanative_dt_blk")
  def DT_BLK: CInt = extern
  @name("scalanative_dt_reg")
  def DT_REG: CInt = extern
  @name("scalanative_dt_lnk")
  def DT_LNK: CInt = extern
  @name("scalanative_dt_sock")
  def DT_SOCK: CInt = extern
  @name("scalanative_dt_wht")
  def DT_WHT: CInt = extern
}
