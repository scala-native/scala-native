package scala.scalanative
package posix

import scala.scalanative.unsafe.{CInt, CString, extern}

@extern
object stdlib {
  def setenv(name: CString, value: CString, overwrite: CInt): CInt = extern
  def unsetenv(name: CString): CInt                                = extern
}
