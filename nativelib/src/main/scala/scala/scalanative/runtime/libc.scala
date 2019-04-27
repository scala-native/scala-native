package scala.scalanative
package runtime

import scalanative.unsafe._

// Minimal bindings for the subset of libc used by the nativelib.
// This is done purely to avoid circular dependency between clib
// and nativelib. The actual bindings should go to clib namespace.
@extern
object libc {
  def malloc(size: Word): RawPtr                              = extern
  def free(ptr: RawPtr): Unit                                 = extern
  def strlen(str: CString): Word                              = extern
  def memcpy(dst: RawPtr, src: RawPtr, count: Word): RawPtr   = extern
  def memcmp(lhs: RawPtr, rhs: RawPtr, count: Word): CInt     = extern
  def memset(dest: RawPtr, ch: CInt, count: Word): RawPtr     = extern
  def memmove(dest: RawPtr, src: RawPtr, count: Word): RawPtr = extern
  def remove(fname: CString): CInt                            = extern
}
