package scala.scalanative
package libc

import scala.scalanative.unsafe._

@extern
object stddef {
  type ptrdiff_t = CLong
  type wchar_t = CInt
  type size_t = CSize

// Macros

  // Ptr[Byte] is Scala Native convention for C (void *).
  @name("scalanative_clib_null")
  def NULL: Ptr[Byte] = extern

  // offsetof() is not implemented in Scala Native.
}
