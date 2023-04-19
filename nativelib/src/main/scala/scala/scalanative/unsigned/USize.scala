// format: off

// BEWARE: This file is generated - direct edits will be lost.
// Do not edit this it directly other than to remove
// personally identifiable information in sourceLocation lines.
// All direct edits to this file will be lost the next time it
// is generated.
//
// See nativelib runtime/Arrays.scala.gyb for details.

package scala.scalanative
package unsigned

import scala.language.implicitConversions

import scalanative.runtime._
import scalanative.runtime.Intrinsics._
import unsafe._

import java.lang.{Long => JLong}

final class USize(private[scalanative] val rawSize: RawSize) {
  @inline def toByte: Byte        = castRawSizeToInt(rawSize).toByte
  @inline def toChar: Char        = castRawSizeToInt(rawSize).toChar
  @inline def toShort: Short      = castRawSizeToInt(rawSize).toShort
  @inline def toInt: Int          = castRawSizeToInt(rawSize)
  @inline def toLong: Long        = castRawSizeToLongUnsigned(rawSize)
  @inline def toSize: unsafe.Size = new unsafe.Size(rawSize)

  @inline def toUByte: UByte   = toByte.toUByte
  @inline def toUShort: UShort = toShort.toUShort
  @inline def toUInt: UInt     = new UInt(castRawSizeToInt(rawSize))
  @inline def toULong: ULong   = new ULong(castRawSizeToLongUnsigned(rawSize))

  @inline def toPtr[T]: Ptr[T] =
    if (is32BitPlatform) fromRawPtr[T](castIntToRawPtr(toInt))
    else fromRawPtr[T](castLongToRawPtr(toLong))

  @inline override def hashCode: Int = toULong.hashCode

  @inline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: USize =>
        other.rawSize == rawSize
      case _ =>
        false
    })

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
    if (is32BitPlatform) (~toInt).toUSize
    else (~toLong).toUSize

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the new right bits with zeroes.
   * @example {{{ 6 << 3 == 48 // in binary: 0110 << 3 == 0110000 }}}
   */
  @inline def <<(x: Int): USize =
    if (is32BitPlatform) (toInt << x).toUSize
    else (toLong << x).toUSize

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the new right bits with zeroes.
   * @example {{{ 6 << 3 == 48 // in binary: 0110 << 3 == 0110000 }}}
   */
  @inline def <<(x: Long): USize =
    if (is32BitPlatform) (toInt << x.toInt).toUSize
    else (toLong << x).toUSize

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
    if (is32BitPlatform) (toInt >>> x).toUSize
    else (toLong >>> x).toUSize

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
    if (is32BitPlatform) (toInt >>> x.toInt).toUSize
    else (toLong >>> x).toUSize

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the right bits with the same value as the left-most bit of this.
   * @example {{{
   * 4294967275 >> 3 == 4294967293
   * // in binary: 11111111 11111111 11111111 11101011 >> 3 ==
   * //            11111111 11111111 11111111 11111101
   * }}}
   */
  @inline final def >>(x: Int): USize =
    if (is32BitPlatform) (toInt >> x).toUSize
    else (toLong >> x).toUSize

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the right bits with the same value as the left-most bit of this.
   * @example {{{
   * 4294967275 >> 3 == 4294967293
   * // in binary: 11111111 11111111 11111111 11101011 >> 3 ==
   * //            11111111 11111111 11111111 11111101
   * }}}
   */
  @inline final def >>(x: Long): USize =
    if (is32BitPlatform) (toInt >> x.toInt).toUSize
    else (toLong >> x).toUSize

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
    if (is32BitPlatform) this.toUInt == other.toUInt
    else this.toULong == other.toULong

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
    if (is32BitPlatform) this.toUInt != other.toUInt
    else this.toULong != other.toULong

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
    if (is32BitPlatform) this.toUInt < other.toUInt
    else this.toULong < other.toULong

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
    if (is32BitPlatform) this.toUInt <= other.toUInt
    else this.toULong <= other.toULong

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
    if (is32BitPlatform) this.toUInt > other.toUInt
    else this.toULong > other.toULong

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
    if (is32BitPlatform) this.toUInt >= other.toUInt
    else this.toULong >= other.toULong

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
    if (is32BitPlatform) new USize(castIntToRawSizeUnsigned(castRawSizeToInt(rawSize) & castRawSizeToInt(other.rawSize)))
    else new USize(castLongToRawSize(castRawSizeToLongUnsigned(rawSize) & castRawSizeToLongUnsigned(other.rawSize)))

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
    if (is32BitPlatform) new USize(castIntToRawSizeUnsigned(castRawSizeToInt(rawSize) | castRawSizeToInt(other.rawSize)))
    else new USize(castLongToRawSize(castRawSizeToLongUnsigned(rawSize) | castRawSizeToLongUnsigned(other.rawSize)))

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
    if (is32BitPlatform) new USize(castIntToRawSizeUnsigned(castRawSizeToInt(rawSize) ^ castRawSizeToInt(other.rawSize)))
    else new USize(castLongToRawSize(castRawSizeToLongUnsigned(rawSize) ^ castRawSizeToLongUnsigned(other.rawSize)))

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
    if (is32BitPlatform) new USize(castIntToRawSizeUnsigned(castRawSizeToInt(rawSize) + castRawSizeToInt(other.rawSize)))
    else new USize(castLongToRawSize(castRawSizeToLongUnsigned(rawSize) + castRawSizeToLongUnsigned(other.rawSize)))

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
    if (is32BitPlatform) new USize(castIntToRawSizeUnsigned(castRawSizeToInt(rawSize) - castRawSizeToInt(other.rawSize)))
    else new USize(castLongToRawSize(castRawSizeToLongUnsigned(rawSize) - castRawSizeToLongUnsigned(other.rawSize)))

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
    if (is32BitPlatform) new USize(castIntToRawSizeUnsigned(castRawSizeToInt(rawSize) * castRawSizeToInt(other.rawSize)))
    else new USize(castLongToRawSize(castRawSizeToLongUnsigned(rawSize) * castRawSizeToLongUnsigned(other.rawSize)))


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
    if (is32BitPlatform) new USize(castIntToRawSizeUnsigned(divUInt(castRawSizeToInt(rawSize), castRawSizeToInt(other.rawSize))))
    else new USize(castLongToRawSize(divULong(castRawSizeToLongUnsigned(rawSize), castRawSizeToLongUnsigned(other.rawSize))))

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
    if (is32BitPlatform) new USize(castIntToRawSizeUnsigned(remUInt(castRawSizeToInt(rawSize), castRawSizeToInt(other.rawSize))))
    else new USize(castLongToRawSize(remULong(castRawSizeToLongUnsigned(rawSize), castRawSizeToLongUnsigned(other.rawSize))))


  // "Rich" API

  @inline final def max(that: USize): USize =
    if (this >= that) this else that
  @inline final def min(that: USize): USize =
    if (this <= that) this else that
}

object USize {
  @inline implicit def ubyteToUSize(x: UByte): USize = x.toUSize
  @inline implicit def ushortToUSize(x: UShort): USize = x.toUSize
  @inline implicit def uintToUSize(x: UInt): USize = x.toUSize
}
