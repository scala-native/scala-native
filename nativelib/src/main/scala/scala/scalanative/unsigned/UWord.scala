package scala.scalanative
package unsigned

import scala.language.implicitConversions

import scala.runtime.BoxesRunTime._
import scala.reflect.ClassTag

import scalanative.runtime._
import scalanative.runtime.Intrinsics._
import scalanative.runtime.Boxes._
import unsafe._

import java.lang.{Long => JLong}

final class UWord(private[scalanative] val rawWord: RawWord) {
  @inline def toByte: Byte        = castRawWordToLongUnsigned(rawWord).toByte
  @inline def toChar: Char        = castRawWordToLongUnsigned(rawWord).toChar
  @inline def toShort: Short      = castRawWordToLongUnsigned(rawWord).toShort
  @inline def toInt: Int          = castRawWordToLongUnsigned(rawWord).toInt
  @inline def toLong: Long        = castRawWordToLongUnsigned(rawWord)
  @inline def toWord: unsafe.Word = new unsafe.Word(rawWord)

  @inline def toUByte: UByte   = toByte.toUByte
  @inline def toUShort: UShort = toShort.toUShort
  @inline def toUInt: UInt     = toInt.toUInt
  @inline def toULong: ULong =
    new ULong(castRawWordToLongUnsigned(rawWord))

  @inline override def hashCode: Int = toULong.hashCode

  @inline override def equals(obj: Any): Boolean = obj match {
    case that: UWord => this.rawWord == that.rawWord
    case _           => false
  }

  @inline override def toString(): String = JLong.toUnsignedString(toLong)

  /**
   * Returns the bitwise negation of this value.
   * @example {{{
   * ~5 == 4294967290
   * // in binary: ~00000101 ==
   * //             11111010
   * }}}
   */
  @inline def unary_~ : UWord =
    (~toLong).toUWord // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the new right bits with zeroes.
   * @example {{{ 6 << 3 == 48 // in binary: 0110 << 3 == 0110000 }}}
   */
  @inline def <<(x: Int): UWord =
    (toLong << x).toUWord // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the new right bits with zeroes.
   * @example {{{ 6 << 3 == 48 // in binary: 0110 << 3 == 0110000 }}}
   */
  @inline def <<(x: Long): UWord =
    (toLong << x).toUWord // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted right by the specified number of bits,
   *         filling the new left bits with zeroes.
   * @example {{{ 21 >>> 3 == 2 // in binary: 010101 >>> 3 == 010 }}}
   * @example {{{
   * 4294967275 >>> 3 == 536870909
   * // in binary: 11111111 11111111 11111111 11101011 >>> 3 ==
   * //            00011111 11111111 11111111 11111101
   * }}}
   */
  @inline def >>>(x: Int): UWord =
    (toLong >>> x).toUWord // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted right by the specified number of bits,
   *         filling the new left bits with zeroes.
   * @example {{{ 21 >>> 3 == 2 // in binary: 010101 >>> 3 == 010 }}}
   * @example {{{
   * 4294967275 >>> 3 == 536870909
   * // in binary: 11111111 11111111 11111111 11101011 >>> 3 ==
   * //            00011111 11111111 11111111 11111101
   * }}}
   */
  @inline def >>>(x: Long): UWord =
    (toLong >>> x).toUWord // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the right bits with the same value as the left-most bit of this.
   * @example {{{
   * 4294967275 >> 3 == 4294967293
   * // in binary: 11111111 11111111 11111111 11101011 >> 3 ==
   * //            11111111 11111111 11111111 11111101
   * }}}
   */
  @inline final def >>(x: Int): UWord = (toLong >> x).toUWord

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the right bits with the same value as the left-most bit of this.
   * @example {{{
   * 4294967275 >> 3 == 4294967293
   * // in binary: 11111111 11111111 11111111 11101011 >> 3 ==
   * //            11111111 11111111 11111111 11111101
   * }}}
   */
  @inline final def >>(x: Long): UWord = (toLong >> x).toUWord

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(x: UByte): Boolean = this == x.toUWord

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(x: UShort): Boolean = this == x.toUWord

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(x: UInt): Boolean = this == x.toUWord

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(x: ULong): Boolean = this.toULong == x

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(other: UWord): Boolean =
    this.rawWord == other.rawWord

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(x: UByte): Boolean = this != x.toUWord

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(x: UShort): Boolean = this != x.toUWord

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(x: UInt): Boolean = this != x.toUWord

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(x: ULong): Boolean = this.toULong != x

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(other: UWord): Boolean =
    this.toULong != other.toULong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(x: UByte): Boolean = this < x.toUWord

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(x: UShort): Boolean = this < x.toUWord

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(x: UInt): Boolean = this < x.toUWord

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(x: ULong): Boolean = this.toULong < x

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(other: UWord): Boolean =
    this.toULong < other.toULong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(x: UByte): Boolean = this <= x.toUWord

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(x: UShort): Boolean = this <= x.toUWord

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(x: UInt): Boolean = this <= x.toUWord

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(x: ULong): Boolean = this.toULong <= x

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(other: UWord): Boolean =
    this.toULong <= other.toULong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(x: UByte): Boolean = this > x.toUWord

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(x: UShort): Boolean = this > x.toUWord

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(x: UInt): Boolean = this > x.toUWord

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(x: ULong): Boolean = this.toULong > x

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(other: UWord): Boolean =
    this.toULong > other.toULong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(x: UByte): Boolean = this >= x.toUWord

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(x: UShort): Boolean = this >= x.toUWord

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(x: UInt): Boolean = this >= x.toUWord

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(x: ULong): Boolean = this.toULong >= x

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(other: UWord): Boolean =
    this.toULong >= other.toULong // TODO(shadaj): intrinsify

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(x: UByte): UWord = this & x.toUWord

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(x: UShort): UWord = this & x.toUWord

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(x: UInt): UWord = this & x.toUWord

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(x: ULong): ULong = this.toULong & x

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(other: UWord): UWord =
    new UWord(andRawWords(rawWord, other.rawWord))

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(x: UByte): UWord = this | x.toUWord

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(x: UShort): UWord = this | x.toUWord

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(x: UInt): UWord = this | x.toUWord

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(x: ULong): ULong = this.toULong | x

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(other: UWord): UWord =
    new UWord(orRawWords(rawWord, other.rawWord))

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(x: UByte): UWord = this ^ x.toUWord

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(x: UShort): UWord = this ^ x.toUWord

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(x: UInt): UWord = this ^ x.toUWord

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(x: ULong): ULong = this.toULong ^ x

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(other: UWord): UWord =
    new UWord(xorRawWords(rawWord, other.rawWord))

  /** Returns the sum of this value and `x`. */
  @inline def +(x: UByte): UWord = this + x.toUWord

  /** Returns the sum of this value and `x`. */
  @inline def +(x: UShort): UWord = this + x.toUWord

  /** Returns the sum of this value and `x`. */
  @inline def +(x: UInt): UWord = this + x.toUWord

  /** Returns the sum of this value and `x`. */
  @inline def +(x: ULong): ULong = this.toULong + x

  /** Returns the sum of this value and `x`. */
  @inline def +(other: UWord): UWord =
    new UWord(addRawWords(rawWord, other.rawWord))

  /** Returns the difference of this value and `x`. */
  @inline def -(x: UByte): UWord = this - x.toUWord

  /** Returns the difference of this value and `x`. */
  @inline def -(x: UShort): UWord = this - x.toUWord

  /** Returns the difference of this value and `x`. */
  @inline def -(x: UInt): UWord = this - x.toUWord

  /** Returns the difference of this value and `x`. */
  @inline def -(x: ULong): ULong = this.toULong - x

  /** Returns the difference of this value and `x`. */
  @inline def -(other: UWord): UWord =
    new UWord(subRawWords(rawWord, other.rawWord))

  /** Returns the product of this value and `x`. */
  @inline def *(x: UByte): UWord = this * x.toUWord

  /** Returns the product of this value and `x`. */
  @inline def *(x: UShort): UWord = this * x.toUWord

  /** Returns the product of this value and `x`. */
  @inline def *(x: UInt): UWord = this * x.toUWord

  /** Returns the product of this value and `x`. */
  @inline def *(x: ULong): ULong = this.toULong * x

  /** Returns the product of this value and `x`. */
  @inline def *(other: UWord): UWord =
    new UWord(multRawWords(rawWord, other.rawWord))

  /** Returns the quotient of this value and `x`. */
  @inline def /(x: UByte): UWord = this / x.toUWord

  /** Returns the quotient of this value and `x`. */
  @inline def /(x: UShort): UWord = this / x.toUWord

  /** Returns the quotient of this value and `x`. */
  @inline def /(x: UInt): UWord = this / x.toUWord

  /** Returns the quotient of this value and `x`. */
  @inline def /(x: ULong): ULong = this.toULong / x

  /** Returns the quotient of this value and `x`. */
  @inline def /(other: UWord): UWord =
    new UWord(divRawWordsUnsigned(rawWord, other.rawWord))

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(x: UByte): UWord = this % x.toUWord

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(x: UShort): UWord = this % x.toUWord

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(x: UInt): UWord = this % x.toUWord

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(x: ULong): ULong = this.toULong % x

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(other: UWord): UWord =
    new UWord(modRawWordsUnsigned(rawWord, other.rawWord))

  // TODO(shadaj): intrinsify
  @inline final def max(that: UWord): UWord =
    this.toULong.max(that.toULong).toUWord
  @inline final def min(that: UWord): UWord =
    this.toULong.min(that.toULong).toUWord
}

object UWord {
  @inline implicit def byteToUWord(x: Byte): UWord =
    new UWord(castIntToRawWord(x.toInt))
  @inline implicit def charToUWord(x: Char): UWord =
    new UWord(castIntToRawWord(x.toInt))
  @inline implicit def shortToUWord(x: Short): UWord =
    new UWord(castIntToRawWord(x.toInt))
  @inline implicit def intToUWord(x: Int): UWord =
    new UWord(castIntToRawWord(x))
  @inline implicit def wordToUWord(x: Word): UWord =
    new UWord(x.rawWord)

  @inline implicit def ubyteToUWord(x: UByte): UWord =
    x.toUWord
  @inline implicit def ushortToUWord(x: UShort): UWord =
    x.toUWord
  @inline implicit def uintToUWord(x: UInt): UWord =
    x.toUWord

  @inline implicit def uwordToULong(x: UWord): ULong =
    x.toULong
}
