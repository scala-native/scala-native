package scala.scalanative
package posix

import scala.scalanative.unsafe._
import scala.scalanative.posix.sys.types

@extern
object stddef {
  type ptrdiff_t = CLong
  type wchar_t = CInt
  type size_t = types.size_t

// Macros

  // Ptr[Byte] is Scala Native convention for C (void *).
  @name("scalanative_posix_null")
  def NULL: Ptr[Byte] = extern

  // offsetof() is not implemented in Scala Native.
}
