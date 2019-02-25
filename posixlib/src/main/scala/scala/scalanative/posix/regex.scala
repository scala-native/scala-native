package scala.scalanative
package posix

import scalanative.native._

@extern
object regex {

  @name("regcomp")
  def compile(regex: Ptr[CInt], str: CString, num: CInt): CInt = extern

  @name("regexec")
  def execute(regex: Ptr[CInt],
              str: CString,
              num: CInt,
              ptr: Ptr[CInt],
              num2: CInt): CInt = extern
}
