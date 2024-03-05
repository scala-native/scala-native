package scala.scalanative
package runtime

import scalanative.unsafe._

// Minimal bindings for the subset of libc/Posix/WindowsAPI used by the nativelib.
// This is done purely to avoid circular dependency between clib
// and nativelib. The actual bindings should go to clib namespace.
@extern
object ffi {
  def malloc(size: CSize): RawPtr = extern
  def malloc(size: RawSize): RawPtr = extern
  def realloc(ptr: RawPtr, size: RawSize): RawPtr = extern
  def calloc(elems: RawSize, size: RawSize): RawPtr = extern
  def free(ptr: RawPtr): Unit = extern
  def free(ptr: CVoidPtr): Unit = extern
  def strlen(str: CString): CSize = extern
  def strchr(str: CString, ch: CInt): CString = extern
  def strrchr(str: CString, ch: CInt): CString = extern
  def wcslen(str: CWideString): CSize = extern
  def strncpy(dest: CString, src: CString, count: RawSize): CString = extern
  def strcpy(dest: CString, src: CString): CString = extern
  def strcat(dest: CString, src: CString): CString = extern
  def memcpy(dst: CVoidPtr, src: CVoidPtr, count: CSize): RawPtr = extern
  def memcpy(dst: RawPtr, src: RawPtr, count: RawSize): RawPtr = extern
  def memcmp(lhs: RawPtr, rhs: RawPtr, count: RawSize): CInt = extern
  def memset(dest: RawPtr, ch: CInt, count: RawSize): RawPtr = extern
  def memset(dest: CVoidPtr, ch: CInt, count: CSize): RawPtr = extern
  def memmove(dest: RawPtr, src: RawPtr, count: RawSize): RawPtr = extern
  def remove(fname: CString): CInt = extern
  def atexit(func: CFuncPtr0[Unit]): CInt = extern

  // Glue layer defined in libc
  @name("scalanative_atomic_compare_exchange_strong_byte")
  private[runtime] def atomic_compare_exchange_byte(
      ptr: RawPtr,
      expected: RawPtr,
      desired: Byte
  ): CBool = extern

  @name("scalanative_atomic_compare_exchange_strong_llong")
  private[runtime] def atomic_compare_exchange_llong(
      ptr: RawPtr,
      expected: RawPtr,
      desired: Long
  ): CBool = extern

  @name("scalanative_atomic_compare_exchange_strong_intptr")
  private[runtime] def atomic_compare_exchange_intptr(
      ptr: RawPtr,
      expected: RawPtr,
      desired: RawPtr
  ): CBool = extern

  @name("scalanative_atomic_load_explicit_llong")
  private[runtime] def atomic_load_llong(
      ptr: RawPtr,
      memoryOrder: memory_order
  ): Long = extern

  @name("scalanative_atomic_load_explicit_intptr")
  private[runtime] def atomic_load_intptr(
      ptr: RawPtr,
      memoryOrder: memory_order
  ): RawPtr = extern

  @name("scalanative_atomic_store_explicit_intptr")
  private[runtime] def atomic_store_intptr(
      ptr: RawPtr,
      v: RawPtr,
      memoryOrder: memory_order
  ): Unit = extern

  @name("scalanative_atomic_thread_fence")
  private[runtime] final def atomic_thread_fence(order: memory_order): Unit =
    extern

  private[runtime] type memory_order = Int
  @extern
  private[runtime] object memory_order {
    @name("scalanative_atomic_memory_order_acquire")
    final def memory_order_acquire: memory_order = extern
    @name("scalanative_atomic_memory_order_release")
    final def memory_order_release: memory_order = extern
    @name("scalanative_atomic_memory_order_seq_cst")
    final def memory_order_seq_cst: memory_order = extern
  }
}
