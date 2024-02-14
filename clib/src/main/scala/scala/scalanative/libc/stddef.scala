package scala.scalanative
package libc

import scala.scalanative.unsafe._

@extern object stddef extends stddef

@extern private[scalanative] trait stddef {
  type ptrdiff_t = CLong
  type wchar_t = CInt
  type size_t = CSize

  // Macros

  // Ptr[_] is Scala Native convention for C (void *).
  @name("scalanative_clib_null")
  def NULL: Ptr[_] = extern

  // offsetof() is not implemented in Scala Native.
}
