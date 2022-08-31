package scala.scalanative
package posix

import scala.scalanative.libc
import scala.scalanative.posix.sys.types
import scala.scalanative.unsafe._

object stddef {
  type ptrdiff_t = libc.stddef.ptrdiff_t
  type wchar_t = libc.stddef.ptrdiff_t
  type size_t = types.size_t

// Macros

  // Ptr[Byte] is Scala Native convention for C (void *).
  def NULL: Ptr[Byte] = libc.stddef.NULL

  // offsetof() is not implemented in Scala Native.
}
