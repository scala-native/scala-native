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

final class USize(private[scalanative] val rawSize: RawSize) {
  @inline def toByte: Byte        = castRawSizeToLongUnsigned(rawSize).toByte
  @inline def toChar: Char        = castRawSizeToLongUnsigned(rawSize).toChar
  @inline def toShort: Short      = castRawSizeToLongUnsigned(rawSize).toShort
  @inline def toInt: Int          = castRawSizeToLongUnsigned(rawSize).toInt
  @inline def toLong: Long        = castRawSizeToLongUnsigned(rawSize)
  @inline def toSize: unsafe.Size = new unsafe.Size(rawSize)

  @inline def toUByte: UByte   = toByte.toUByte
  @inline def toUShort: UShort = toShort.toUShort
  @inline def toUInt: UInt     = toInt.toUInt
  @inline def toULong: ULong =
    new ULong(castRawSizeToLongUnsigned(rawSize))

  @inline override def hashCode: Int = toULong.hashCode

  @inline override def equals(obj: Any): Boolean = obj match {
    case that: USize => this.rawSize == that.rawSize
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
  @inline def unary_~ : USize =
    (~toLong).toUSize // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the new right bits with zeroes.
   * @example {{{ 6 << 3 == 48 // in binary: 0110 << 3 == 0110000 }}}
   */
  @inline def <<(x: Int): USize =
    (toLong << x).toUSize // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the new right bits with zeroes.
   * @example {{{ 6 << 3 == 48 // in binary: 0110 << 3 == 0110000 }}}
   */
  @inline def <<(x: Long): USize =
    (toLong << x).toUSize // TODO(shadaj): intrinsify

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
  @inline def >>>(x: Int): USize =
    (toLong >>> x).toUSize // TODO(shadaj): intrinsify

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
  @inline def >>>(x: Long): USize =
    (toLong >>> x).toUSize // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the right bits with the same value as the left-most bit of this.
   * @example {{{
   * 4294967275 >> 3 == 4294967293
   * // in binary: 11111111 11111111 11111111 11101011 >> 3 ==
   * //            11111111 11111111 11111111 11111101
   * }}}
   */
  @inline final def >>(x: Int): USize = (toLong >> x).toUSize

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the right bits with the same value as the left-most bit of this.
   * @example {{{
   * 4294967275 >> 3 == 4294967293
   * // in binary: 11111111 11111111 11111111 11101011 >> 3 ==
   * //            11111111 11111111 11111111 11111101
   * }}}
   */
  @inline final def >>(x: Long): USize = (toLong >> x).toUSize

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(x: UByte): Boolean = this == x.toUSize

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(x: UShort): Boolean = this == x.toUSize

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(x: UInt): Boolean = this == x.toUSize

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(x: ULong): Boolean = this.toULong == x

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(other: USize): Boolean =
    this.rawSize == other.rawSize

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(x: UByte): Boolean = this != x.toUSize

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(x: UShort): Boolean = this != x.toUSize

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(x: UInt): Boolean = this != x.toUSize

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(x: ULong): Boolean = this.toULong != x

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(other: USize): Boolean =
    this.toULong != other.toULong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(x: UByte): Boolean = this < x.toUSize

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(x: UShort): Boolean = this < x.toUSize

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(x: UInt): Boolean = this < x.toUSize

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(x: ULong): Boolean = this.toULong < x

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(other: USize): Boolean =
    this.toULong < other.toULong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(x: UByte): Boolean = this <= x.toUSize

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(x: UShort): Boolean = this <= x.toUSize

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(x: UInt): Boolean = this <= x.toUSize

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(x: ULong): Boolean = this.toULong <= x

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(other: USize): Boolean =
    this.toULong <= other.toULong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(x: UByte): Boolean = this > x.toUSize

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(x: UShort): Boolean = this > x.toUSize

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(x: UInt): Boolean = this > x.toUSize

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(x: ULong): Boolean = this.toULong > x

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(other: USize): Boolean =
    this.toULong > other.toULong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(x: UByte): Boolean = this >= x.toUSize

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(x: UShort): Boolean = this >= x.toUSize

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(x: UInt): Boolean = this >= x.toUSize

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(x: ULong): Boolean = this.toULong >= x

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(other: USize): Boolean =
    this.toULong >= other.toULong // TODO(shadaj): intrinsify

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(x: UByte): USize = this & x.toUSize

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(x: UShort): USize = this & x.toUSize

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(x: UInt): USize = this & x.toUSize

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(x: ULong): ULong = this.toULong & x

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(other: USize): USize =
    new USize(andRawSizes(rawSize, other.rawSize))

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(x: UByte): USize = this | x.toUSize

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(x: UShort): USize = this | x.toUSize

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(x: UInt): USize = this | x.toUSize

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(x: ULong): ULong = this.toULong | x

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(other: USize): USize =
    new USize(orRawSizes(rawSize, other.rawSize))

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(x: UByte): USize = this ^ x.toUSize

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(x: UShort): USize = this ^ x.toUSize

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(x: UInt): USize = this ^ x.toUSize

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(x: ULong): ULong = this.toULong ^ x

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(other: USize): USize =
    new USize(xorRawSizes(rawSize, other.rawSize))

  /** Returns the sum of this value and `x`. */
  @inline def +(x: UByte): USize = this + x.toUSize

  /** Returns the sum of this value and `x`. */
  @inline def +(x: UShort): USize = this + x.toUSize

  /** Returns the sum of this value and `x`. */
  @inline def +(x: UInt): USize = this + x.toUSize

  /** Returns the sum of this value and `x`. */
  @inline def +(x: ULong): ULong = this.toULong + x

  /** Returns the sum of this value and `x`. */
  @inline def +(other: USize): USize =
    new USize(addRawSizes(rawSize, other.rawSize))

  /** Returns the difference of this value and `x`. */
  @inline def -(x: UByte): USize = this - x.toUSize

  /** Returns the difference of this value and `x`. */
  @inline def -(x: UShort): USize = this - x.toUSize

  /** Returns the difference of this value and `x`. */
  @inline def -(x: UInt): USize = this - x.toUSize

  /** Returns the difference of this value and `x`. */
  @inline def -(x: ULong): ULong = this.toULong - x

  /** Returns the difference of this value and `x`. */
  @inline def -(other: USize): USize =
    new USize(subRawSizes(rawSize, other.rawSize))

  /** Returns the product of this value and `x`. */
  @inline def *(x: UByte): USize = this * x.toUSize

  /** Returns the product of this value and `x`. */
  @inline def *(x: UShort): USize = this * x.toUSize

  /** Returns the product of this value and `x`. */
  @inline def *(x: UInt): USize = this * x.toUSize

  /** Returns the product of this value and `x`. */
  @inline def *(x: ULong): ULong = this.toULong * x

  /** Returns the product of this value and `x`. */
  @inline def *(other: USize): USize =
    new USize(multRawSizes(rawSize, other.rawSize))

  /** Returns the quotient of this value and `x`. */
  @inline def /(x: UByte): USize = this / x.toUSize

  /** Returns the quotient of this value and `x`. */
  @inline def /(x: UShort): USize = this / x.toUSize

  /** Returns the quotient of this value and `x`. */
  @inline def /(x: UInt): USize = this / x.toUSize

  /** Returns the quotient of this value and `x`. */
  @inline def /(x: ULong): ULong = this.toULong / x

  /** Returns the quotient of this value and `x`. */
  @inline def /(other: USize): USize =
    new USize(divRawSizesUnsigned(rawSize, other.rawSize))

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(x: UByte): USize = this % x.toUSize

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(x: UShort): USize = this % x.toUSize

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(x: UInt): USize = this % x.toUSize

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(x: ULong): ULong = this.toULong % x

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(other: USize): USize =
    new USize(modRawSizesUnsigned(rawSize, other.rawSize))

  // TODO(shadaj): intrinsify
  @inline final def max(that: USize): USize =
    this.toULong.max(that.toULong).toUSize
  @inline final def min(that: USize): USize =
    this.toULong.min(that.toULong).toUSize
}

object USize {
  @inline implicit def byteToUSize(x: Byte): USize =
    new USize(castIntToRawSize(x.toInt))
  @inline implicit def charToUSize(x: Char): USize =
    new USize(castIntToRawSize(x.toInt))
  @inline implicit def shortToUSize(x: Short): USize =
    new USize(castIntToRawSize(x.toInt))
  @inline implicit def intToUSize(x: Int): USize =
    new USize(castIntToRawSize(x))
  @inline implicit def wordToUSize(x: Size): USize =
    new USize(x.rawSize)

  @inline implicit def ubyteToUSize(x: UByte): USize =
    x.toUSize
  @inline implicit def ushortToUSize(x: UShort): USize =
    x.toUSize
  @inline implicit def uintToUSize(x: UInt): USize =
    x.toUSize

  @inline implicit def uwordToULong(x: USize): ULong =
    x.toULong
}
