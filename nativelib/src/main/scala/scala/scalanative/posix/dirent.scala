package scala.scalanative
package posix

import scala.scalanative.native._, Nat._

@extern
object dirent {

  type _256   = Digit[_2, Digit[_5, _6]]
  type DIR    = Void
  type dirent = CStruct2[CUnsignedLongLong, CArray[CChar, _256]]

  @name("scalanative_opendir")
  def opendir(name: CString): Ptr[DIR] = extern

  @name("scalanative_readdir")
  def readdir(dirp: Ptr[DIR], buf: Ptr[dirent]): CInt = extern

  @name("scalanative_closedir")
  def closedir(dirp: Ptr[DIR]): CInt = extern

  @name("scalanative_gettempdir")
  def gettempdir(buffer: CString, length: CSize): CInt = extern
}
