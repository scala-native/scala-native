package scala.scalanative
package runtime

import native._

// Minimal bindings for the subset of libc used by the nativelib.
// This is done purely to avoid circular dependency between clib
// and nativelib. The actual bindings should go to clib namespace.
@extern
object libc {
  def malloc(size: CSize): RawPtr                              = extern
  def free(ptr: RawPtr): Unit                                  = extern
  def strlen(str: CString): CSize                              = extern
  def memcpy(dst: RawPtr, src: RawPtr, count: CSize): RawPtr   = extern
  def memcmp(lhs: RawPtr, rhs: RawPtr, count: CSize): CInt     = extern
  def memset(dest: RawPtr, ch: CInt, count: CSize): RawPtr     = extern
  def memmove(dest: RawPtr, src: RawPtr, count: CSize): RawPtr = extern
  def remove(fname: CString): CInt                             = extern
}
