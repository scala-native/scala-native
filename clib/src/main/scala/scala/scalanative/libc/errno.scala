package scala.scalanative
package libc

import scalanative.unsafe._

@extern object errno extends errno

@extern private[scalanative] trait errno {
  var errno: CInt = extern
  @name("scalanative_edom")
  def EDOM: CInt = extern
  @name("scalanative_eilseq")
  def EILSEQ: CInt = extern
  @name("scalanative_erange")
  def ERANGE: CInt = extern
}
