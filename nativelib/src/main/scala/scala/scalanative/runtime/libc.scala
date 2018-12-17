package scala.scalanative
package runtime

import native._

// Minimal bindings for the subset of libc used by the nativelib.
// This is done purely to avoid circular dependency between clib
// and nativelib. The actual bindings should go to clib namespace.
@extern
object libc {
  def malloc(size: CSize): Ptr[Byte]                                    = extern
  def free(ptr: Ptr[Byte]): Unit                                        = extern
  def strlen(str: CString): CSize                                       = extern
  def memcpy(dst: Ptr[Byte], src: Ptr[Byte], count: CSize): Ptr[Byte]   = extern
  def memcmp(lhs: Ptr[Byte], rhs: Ptr[Byte], count: CSize): CInt        = extern
  def memset(dest: Ptr[Byte], ch: CInt, count: CSize): Ptr[Byte]        = extern
  def memmove(dest: Ptr[Byte], src: Ptr[Byte], count: CSize): Ptr[Byte] = extern
  def remove(fname: CString): CInt                                      = extern
}
