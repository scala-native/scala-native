package scala.scalanative.libc

import scala.scalanative.unsafe.*

@extern object inttypes extends inttypes

@extern private[scalanative] trait inttypes {
  import scala.scalanative.libc.stdint.*

  /** See also https://en.cppreference.com/w/cpp/numeric/math/abs */
  type imaxdiv_t = CStruct2[intmax_t, intmax_t]

  /** Calculates the absolute value of an integer of any size. The imaxabs
   *  function is similar to llabs() and labs(). The only difference being that
   *  the return value and the argument passed in are of type intmax_t. (since
   *  C99)
   *
   *  @return
   *    The imaxabs function returns the absolute value of the argument. There's
   *    no error return.
   *
   *  See also https://en.cppreference.com/w/cpp/numeric/math/abs
   */
  def imaxabs(j: intmax_t): intmax_t = extern

  /** Computes the quotient and the remainder of two integer values of any size
   *  as a single operation.
   *  ==Example==
   *  ```
   *  val res = stackalloc[imaxdiv_t]()
   *  imaxdiv(45, 7, res)
   *  res._1 // 6
   *  res._2 // 3
   *  ```
   *  @param numer
   *    The numerator.
   *  @param denom
   *    The denominator.
   *  @param result
   *    The pointer to struct of type imaxdiv_t to store quotient and remainder.
   *    C spec defines `imaxdiv` as `(intmax_t,intmax_t) => imaxdiv_t`, but due
   *    to the limitation of scala native (scala native not supporting passing
   *    struct between scalanative and c), this function takes result struct as
   *    workaround.
   *
   *  See also https://en.cppreference.com/w/c/numeric/math/div#imaxdiv_t
   */
  @name("scalanative_inttypes_imaxdiv")
  def imaxdiv(
      numer: intmax_t,
      denom: intmax_t,
      result: Ptr[imaxdiv_t]
  ): Unit = extern

  /** The strtoimax function is equivalent to the strtol, strtoll functions,
   *  except that the initial portion of the string is converted to intmax_t
   *  representation.(since C99)
   *
   *  @param nptr
   *    to the null-terminated byte string to be interpreted
   *  @param endptr
   *    pointer to a pointer to character.
   *  @param base
   *    base of the interpreted integer value
   *  @return
   *    the converted value, if any.If no conversion could be performed, zero is
   *    returned. If the correct value is outside the range of representable
   *    values, INTMAX_MAX or INTMAX_MIN is returned
   *
   *  See also https://en.cppreference.com/w/c/string/byte/strtoimax
   */
  def strtoimax(
      nptr: CString,
      endptr: Ptr[Ptr[CChar]],
      base: CInt
  ): intmax_t =
    extern

  /** The strtoumax function is equivalent to the strtoul, and strtoull
   *  functions, except that the initial portion of the string is converted to
   *  uintmax_t representation. (since C99)
   *
   *  @param nptr
   *    to the null-terminated byte string to be interpreted
   *  @param endptr
   *    pointer to a pointer to character.
   *  @param base
   *    of the interpreted integer value
   *  @return
   *    the converted value, if any.If no conversion could be performed, zero is
   *    returned. If the correct value is outside the range of representable
   *    values,INTMAX_MAX, INTMAX_MIN, or UINTMAX_MAX is returned.
   *
   *  See also https://en.cppreference.com/w/c/string/byte/strtoimax
   */
  def strtoumax(
      nptr: CString,
      endptr: Ptr[Ptr[CChar]],
      base: CInt
  ): intmax_t =
    extern
}
