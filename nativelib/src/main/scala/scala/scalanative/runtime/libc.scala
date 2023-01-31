package scala.scalanative
package runtime

import scalanative.unsafe._

// Minimal bindings for the subset of libc used by the nativelib.
// This is done purely to avoid circular dependency between clib
// and nativelib. The actual bindings should go to clib namespace.
@extern
object libc {
  def malloc(size: CSize): RawPtr = extern
  def realloc(ptr: RawPtr, size: CSize): RawPtr = extern
  def free(ptr: RawPtr): Unit = extern
  def strlen(str: CString): CSize = extern
  def wcslen(str: CWideString): CSize = extern
  def strcpy(dest: CString, src: CString): CString = extern
  def strcat(dest: CString, src: CString): CString = extern
  def memcpy(dst: Ptr[Byte], src: Ptr[Byte], count: CSize): RawPtr = extern
  def memcpy(dst: RawPtr, src: RawPtr, count: CSize): RawPtr = extern
  def memcmp(lhs: RawPtr, rhs: RawPtr, count: CSize): CInt = extern
  def memset(dest: RawPtr, ch: CInt, count: CSize): RawPtr = extern
  def memmove(dest: RawPtr, src: RawPtr, count: CSize): RawPtr = extern
  def remove(fname: CString): CInt = extern

  // Glue layer defined in libc
  @name("scalanative_atomic_thread_fence")
  private[runtime] final def atomic_thread_fence(order: memory_order): Unit =
    extern

  private[runtime] type memory_order = Int
  @extern
  private[runtime] object memory_order {
    @name("scalanative_atomic_memory_order_seq_cst")
    final def memory_order_seq_cst: memory_order = extern
  }
}
