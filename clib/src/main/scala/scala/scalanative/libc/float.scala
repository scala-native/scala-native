package scala.scalanative
package libc

import scalanative.unsafe._

/**
 * Bindings for float.h
 */
@extern
object float {

  // Macros

  /** Minimal floating point value. */
  @name("scalanative_float_flt_min")
  def FLT_MIN: CFloat = extern

  /** Minimal floating point value. */
  @name("scalanative_float_dbl_min")
  def DBL_MIN: CDouble = extern

  /** Maximum finite floating-point value. */
  @name("scalanative_float_flt_max")
  def FLT_MAX: CFloat = extern

  /** Maximum finite floating-point value. */
  @name("scalanative_float_dbl_max")
  def DBL_MAX: CDouble = extern

  /** The smallest x for which 1.0 + x != 1.0. */
  @name("scalanative_float_flt_epsilon")
  def FLT_EPSILON: CFloat = extern

  /** The smallest x for which 1.0 + x != 1.0. */
  @name("scalanative_float_dbl_epsilon")
  def DBL_EPSILON: CDouble = extern

  /** Rounding mode for floating point addition.
   *
   * -1 indeterminable
   * 0 towards zero
   * 1 to nearest
   * 2 towards positive infinity
   * 3 towards negative infinity
   */
  @name("scalanative_float_flt_rounds")
  def FLT_ROUNDS: CInt = extern

  /** The base radix representation of the exponent.
   *
   * A base-2 is binary,
   * base-10 is the normal decimal representation
   * base-16 is Hex.
   */
  @name("scalanative_float_flt_radix")
  def FLT_RADIX: CInt = extern

  /** Number of [[FLT_RADIX]] digits in the mantissa. */
  @name("scalanative_float_flt_mant_dig")
  def FLT_MANT_DIG: CInt = extern

  /** Number of [[FLT_RADIX]] digits in the mantissa. */
  @name("scalanative_float_flt_mant_dig")
  def DBL_MANT_DIG: CInt = extern

  /** Number of significant digits in a floating point number. */
  @name("scalanative_float_flt_dig")
  def FLT_DIG: CInt = extern

  /** Number of significant digits in a floating point number. */
  @name("scalanative_float_dbl_dig")
  def DBL_DIG: CInt = extern

  /** The minimal exponent of a floating point value expressed in base [[FLT_RADIX]]. */
  @name("scalanative_float_flt_min_exp")
  def FLT_MIN_EXP: CInt = extern

  /** The minimal exponent of a floating point value expressed in base [[FLT_RADIX]]. */
  @name("scalanative_float_dbl_min_exp")
  def DBL_MIN_EXP: CInt = extern

  /** The minimal exponent of a floating point value expressed in base 10. */
  @name("scalanative_float_flt_min_10_exp")
  def FLT_MIN_10_EXP: CInt = extern

  /** The minimal exponent of a floating point value expressed in base 10. */
  @name("scalanative_float_dbl_min_10_exp")
  def DBL_MIN_10_EXP: CInt = extern

  /** The maximal exponent of a floating point value expressed in base [[FLT_RADIX]]. */
  @name("scalanative_float_flt_min_exp")
  def FLT_MAX_EXP: CInt = extern

  /** The maximal exponent of a floating point value expressed in base [[FLT_RADIX]]. */
  @name("scalanative_float_dbl_min_exp")
  def DBL_MAX_EXP: CInt = extern

  /** The maximal exponent of a floating point value expressed in base 10. */
  @name("scalanative_float_flt_min_10_exp")
  def FLT_MAX_10_EXP: CInt = extern

  /** The maximal exponent of a floating point value expressed in base 10. */
  @name("scalanative_float_dbl_min_10_exp")
  def DBL_MAX_10_EXP: CInt = extern
}
