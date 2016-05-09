package scala.scalanative

import scala.reflect.ClassTag
import runtime.undefined

package object native {
  /** Int on 32-bit architectures and Long on 64-bit ones. */
  type Word = Long

  /** UInt on 32-bit architectures and ULong on 64-bit ones. */
  type UWord = ULong

  /** The C 'char' type. */
  type CChar = Byte

  /** The C 'unsigned char' type. */
  type CUnsignedChar = UByte

  /** The C 'unsigned short' type. */
  type CUnsignedShort = UShort

  /** The C 'unsigned int' type. */
  type CUnsignedInt = UInt

  /** The C 'unsigned long' type. */
  type CUnsignedLong = UWord

  /** The C 'unsigned long long' type. */
  type CUnsignedLongLong = ULong

  /** The C 'signed char' type. */
  type CSignedChar = Byte

  /** The C 'short' type. */
  type CShort = Short

  /** The C 'int' type. */
  type CInt = Int

  /** The C 'long' type. */
  type CLong = Word

  /** The C 'long long' type. */
  type CLongLong = Long

  /** The C 'float' type. */
  type CFloat = Float

  /** The C 'double' type. */
  type CDouble = Double

  /** The C++ 'wchar_t' type. */
  type CWideChar = UInt

  /** The C++ 'char16_t' type. */
  type CChar16 = UShort

  /** The C++ 'char32_t' type. */
  type CChar32 = UInt

  /** The C '_Bool' and C++ 'bool' type. */
  type CBool = Boolean

  /** The C/C++ 'size_t' type. */
  // TODO: this should really be UWord, but uints are quite crippled atm
  type CSize = Word

  /** C-style string with trailing 0. */
  type CString = Ptr[CChar]

  /** The C 'sizeof' operator. */
  def sizeof[T](implicit ct: ClassTag[T]): CSize = undefined

  /** Stack allocate value. */
  def stackalloc[T](implicit ct: ClassTag[T]): Ptr[T] = undefined

  /** Used as right hand side of external method and field declarations. */
  def extern: Nothing = undefined

  /** C-style string literal. */
  implicit class CQuote(val ctx: StringContext) {
    def c(): CString = undefined
  }

  /** C-style unchecked cast. */
  implicit class CCast(val any: Any) {
    def cast[T](implicit ct: ClassTag[T]): T = undefined
  }
}
