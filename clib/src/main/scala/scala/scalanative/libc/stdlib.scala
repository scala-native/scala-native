package scala.scalanative
package libc

import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

@extern object stdlib extends stdlib

@extern private[scalanative] trait stdlib {

  // Memory management

  def malloc(size: CSize): Ptr[Byte] = extern
  def calloc(num: CSize, size: CSize): Ptr[Byte] = extern
  def realloc[T](ptr: Ptr[T], newSize: CSize): Ptr[T] = extern
  def free(ptr: CVoidPtr): Unit = extern
  def aligned_alloc(alignment: CSize, size: CSize): Unit = extern

  def malloc(size: Int): Ptr[Byte] = malloc(size.toCSize)
  def malloc(size: Long): Ptr[Byte] = malloc(size.toCSize)
  def calloc(num: Int, size: Int): Ptr[Byte] = calloc(num.toCSize, size.toCSize)
  def calloc(num: Long, size: Long): Ptr[Byte] =
    calloc(num.toCSize, size.toCSize)
  def realloc[T](ptr: Ptr[T], newSize: Int): Ptr[T] =
    realloc(ptr, newSize.toCSize)
  def realloc[T](ptr: Ptr[T], newSize: Long): Ptr[T] =
    realloc(ptr, newSize.toCSize)
  def aligned_alloc(alignment: Int, size: Int): Unit =
    aligned_alloc(alignment.toCSize, size.toCSize)
  def aligned_alloc(alignment: Long, size: Long): Unit =
    aligned_alloc(alignment.toCSize, size.toCSize)

  // Program utilities

  def abort(): Unit = extern
  def exit(exitCode: CInt): Unit = extern
  def quick_exit(exitCode: CInt): Unit = extern
  def _Exit(exitCode: CInt): Unit = extern
  def atexit(func: CFuncPtr0[Unit]): CInt = extern
  def at_quick_exit(func: CFuncPtr0[Unit]): CInt = extern

  // Communicating with the environment

  def system(command: CString): CInt = extern
  def getenv(name: CString): CString = extern

  // Pseudo-random number generation

  def rand(): CInt = extern
  def srand(seed: CUnsignedInt): Unit = extern
  def srand(seed: Int): Unit = srand(seed.toUInt)

  // Conversions to numeric formats

  def atof(str: CString): CDouble = extern
  def atoi(str: CString): CInt = extern
  def atol(str: CString): CLong = extern
  def atoll(str: CString): CLongLong = extern
  def strtol(str: CString, str_end: Ptr[CString], base: CInt): CLong = extern
  def strtoll(str: CString, str_end: Ptr[CString], base: CInt): CLongLong =
    extern
  def strtoul(
      str: CString,
      str_end: Ptr[CString],
      base: CInt
  ): CUnsignedLong =
    extern
  def strtoull(
      str: CString,
      str_end: Ptr[CString],
      base: CInt
  ): CUnsignedLongLong =
    extern
  def strtof(str: CString, str_end: Ptr[CString]): CFloat = extern
  def strtod(str: CString, str_end: Ptr[CString]): CDouble = extern

  // Searching and sorting

  def bsearch(
      key: CVoidPtr,
      data: CVoidPtr,
      num: CSize,
      size: CSize,
      comparator: CFuncPtr2[CVoidPtr, CVoidPtr, CInt]
  ): Unit = extern

  def bsearch(
      key: CVoidPtr,
      data: CVoidPtr,
      num: Int,
      size: Int,
      comparator: CFuncPtr2[CVoidPtr, CVoidPtr, CInt]
  ): Unit = bsearch(key, data, num.toCSize, size.toCSize, comparator)

  def qsort[T](
      data: Ptr[T],
      num: CSize,
      size: CSize,
      comparator: CFuncPtr2[CVoidPtr, CVoidPtr, CInt]
  ): Unit = extern

  def qsort[T](
      data: Ptr[T],
      num: Int,
      size: Int,
      comparator: CFuncPtr2[CVoidPtr, CVoidPtr, CInt]
  ): Unit = qsort(data, num.toCSize, size.toCSize, comparator)

  // Macros

  @name("scalanative_exit_success")
  def EXIT_SUCCESS: CInt = extern
  @name("scalanative_exit_failure")
  def EXIT_FAILURE: CInt = extern
  @name("scalanative_rand_max")
  def RAND_MAX: CInt = extern
}
