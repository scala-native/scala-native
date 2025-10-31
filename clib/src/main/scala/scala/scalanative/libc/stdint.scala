package scala.scalanative.libc

import scala.scalanative.unsafe._

@extern object stdint extends stdint

// A very partial implementation. May it grow to completeness with time.

@extern private[scalanative] trait stdint {
  // intmax_t and uintmax_t are not always equivalent to `long long`,
  // but they are usually `long long` in common data models.
  type intmax_t = CLongLong
  type uintmax_t = CUnsignedLongLong

  type intptr_t = CLongLong
  type uintptr_t = CUnsignedLongLong
}
