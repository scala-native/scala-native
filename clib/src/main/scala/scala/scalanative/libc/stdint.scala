package scala.scalanative.libc

import scala.scalanative.unsafe._

@extern object stdint extends stdint

// A very partial implementation. May it grow to completeness with time.

@extern private[scalanative] trait stdint {

  type int16_t = CShort
  type uint16_t = CUnsignedShort

  type int32_t = CInt
  type uint32_t = CUnsignedInt

  type int64_t = CLongLong
  type uint64_t = CUnsignedLongLong

  // intmax_t and uintmax_t are not always equivalent to `long long`,
  // but they are usually `long long` in common data models.
  type intmax_t = CLongLong
  type uintmax_t = CUnsignedLongLong

  /* Assume & implement a data model where the size of a C/C++ pointer is the
   * same as the maximum size of a C/C++ data object.
   * 
   * C and C++ do not __require__ this match but it is empirically true
   * for the most common data models. This is similar to the intmax_t/uintmax_t
   * assumption above.
   *
   * Reference:
   *   https://en.wikipedia.org/wiki/64-bit_computing#64-bit_data_models
   *   Retrieved 2025-10-31
   */

  type intptr_t = CSSize
  type uintptr_t = CSize
}
