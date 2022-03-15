package scala.scalanative

package libc
import scala.scalanative.unsafe._

@extern
object limits {

  /** Defines the number of bits in a byte. */
  @name("scalanative_limits_char_bit")
  def CHAR_BIT: CInt = extern

  /** Defines the value for type char and its value will be equal to SCHAR_MAX
   *  if char represents negative values, otherwise UCHAR_MAX.
   */
  @name("scalanative_limits_char_max")
  def CHAR_MAX: CInt = extern

  /** Defines the minimum value for type char and its value will be equal to
   *  SCHAR_MIN if char represents negative values, otherwise zero.
   */
  @name("scalanative_limits_char_min")
  def CHAR_MIN: CInt = extern

  /** Defines the maximum value for an int.
   */
  @name("scalanative_limits_int_max")
  def INT_MAX: CInt = extern

  /** Defines the minimum value for an int.
   */
  @name("scalanative_limits_int_min")
  def INT_MIN: CInt = extern

  /** Defines the maximum value for a long int.
   */
  @name("scalanative_limits_long_max")
  def LONG_MAX: CLong = extern

  /** Defines the minimum value for a long int.
   */
  @name("scalanative_limits_long_min")
  def LONG_MIN: CLong = extern

  /** Defines the maximum number of bytes in a multi-byte character.
   */
  @name("scalanative_limits_mb_len_max")
  def MB_LEN_MAX: CInt = extern

  /** Defines the maximum value for a signed char.
   */
  @name("scalanative_limits_schar_max")
  def SCHAR_MAX: CInt = extern

  /** Defines the minimum value for a signed char.
   */
  @name("scalanative_limits_schar_min")
  def SCHAR_MIN: CInt = extern

  /** Defines the maximum value for a short int.
   */
  @name("scalanative_limits_shrt_max")
  def SHRT_MAX: CShort = extern

  /** Defines the minimum value for a short int.
   */
  @name("scalanative_limits_shrt_min")
  def SHRT_MIN: CShort = extern

  /** Defines the maximum value for an unsigned char.
   */
  @name("scalanative_limits_uchar_max")
  def UCHAR_MAX: CUnsignedChar = extern

  /** Defines the maximum value for an unsigned int.
   */
  @name("scalanative_limits_uint_max")
  def UINT_MAX: CUnsignedInt = extern

  /** Defines the maximum value for an unsigned long int.
   */
  @name("scalanative_limits_ulong_max")
  def ULONG_MAX: CUnsignedLong = extern

  /** Defines the maximum value for an unsigned short int.
   */
  @name("scalanative_limits_ushrt_max")
  def USHRT_MAX: CUnsignedShort = extern
}
