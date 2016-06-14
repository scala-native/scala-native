package scala.scalanative
package libc

import native._

@extern
object stdlib {
  @name("scalanative_stderr")
  def stderr: Ptr[_] = extern
  @name("scalanative_stdout")
  def stdout: Ptr[_] = extern

  def fopen(filename: CString, mode: CString): Ptr[_]               = extern
  def fprintf(stream: Ptr[_], format: CString, args: Vararg*): CInt = extern
  def malloc(size: Word): Ptr[_]                                    = extern
  def getenv(name: CString): CString                                = extern
}
