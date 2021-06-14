package scala.scalanative
package unsafe

import scala.language.implicitConversions

import scala.runtime.BoxesRunTime._
import scala.reflect.ClassTag

import scalanative.runtime._
import scalanative.runtime.Intrinsics._
import scalanative.runtime.Boxes._

import scalanative.unsigned._

final class Size(private[scalanative] val rawSize: RawSize) {
  @inline def toByte: Byte   = castRawSizeToInt(rawSize).toByte
  @inline def toChar: Char   = castRawSizeToInt(rawSize).toChar
  @inline def toShort: Short = castRawSizeToInt(rawSize).toShort
  @inline def toInt: Int     = castRawSizeToInt(rawSize)
  @inline def toLong: Long   = castRawSizeToLong(rawSize)

  @inline def toUByte: UByte   = toUSize.toUByte
  @inline def toUShort: UShort = toUSize.toUShort
  @inline def toUInt: UInt     = toUSize.toUInt
  @inline def toULong: ULong   = toUSize.toULong
  @inline def toUSize: USize   = new USize(rawSize)

  // TODO(shadaj): intrinsify
  @inline def toPtr[T]: Ptr[T] = fromRawPtr[T](castLongToRawPtr(toLong))

  @inline override def hashCode: Int = toLong.hashCode

  @inline override def equals(other: Any): Boolean =
    (this eq other.asInstanceOf[AnyRef]) || (other match {
      case other: Size =>
        other.rawSize == rawSize
      case _ =>
        false
    })

  @inline override def toString(): String = toLong.toString

  /**
   * Returns the bitwise negation of this value.
   * @example {{{
   * ~5 == 4294967290
   * // in binary: ~00000101 ==
   * //             11111010
   * }}}
   */
  @inline def unary_~ : Size =
    (~toLong).toSize // TODO(shadaj): intrinsify

  /** Returns the negated version of this value. */
  @inline def unary_- : Size = 0 - this // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the new right bits with zeroes.
   * @example {{{ 6 << 3 == 48 // in binary: 0110 << 3 == 0110000 }}}
   */
  @inline def <<(x: Int): Size =
    (toLong << x).toSize // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the new right bits with zeroes.
   * @example {{{ 6 << 3 == 48 // in binary: 0110 << 3 == 0110000 }}}
   */
  @inline def <<(x: Long): Size =
    (toLong << x).toSize // TODO(shadaj): intrinsify

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
  @inline def >>>(x: Int): Size =
    (toLong >>> x).toSize // TODO(shadaj): intrinsify

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
  @inline def >>>(x: Long): Size =
    (toLong >>> x).toSize // TODO(shadaj): intrinsify

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the right bits with the same value as the left-most bit of this.
   * @example {{{
   * 4294967275 >> 3 == 4294967293
   * // in binary: 11111111 11111111 11111111 11101011 >> 3 ==
   * //            11111111 11111111 11111111 11111101
   * }}}
   */
  @inline final def >>(x: Int): Size = (toLong >> x).toSize

  /**
   * Returns this value bit-shifted left by the specified number of bits,
   *         filling in the right bits with the same value as the left-most bit of this.
   * @example {{{
   * 4294967275 >> 3 == 4294967293
   * // in binary: 11111111 11111111 11111111 11101011 >> 3 ==
   * //            11111111 11111111 11111111 11111101
   * }}}
   */
  @inline final def >>(x: Long): Size = (toLong >> x).toSize

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(x: Byte): Boolean = this == x.toSize

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(x: Short): Boolean = this == x.toSize

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(x: Int): Boolean = this == x.toSize

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(x: Long): Boolean = this.toLong == x

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline def ==(other: Size): Boolean =
    this.toLong == other.toLong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(x: Byte): Boolean = this != x.toSize

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(x: Short): Boolean = this != x.toSize

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(x: Int): Boolean = this != x.toSize

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(x: Long): Boolean = this.toLong != x

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline def !=(other: Size): Boolean =
    this.toLong != other.toLong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(x: Byte): Boolean = this < x.toSize

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(x: Short): Boolean = this < x.toSize

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(x: Int): Boolean = this < x.toSize

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(x: Long): Boolean = this.toLong < x

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline def <(other: Size): Boolean =
    this.toLong < other.toLong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(x: Byte): Boolean = this <= x.toSize

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(x: Short): Boolean = this <= x.toSize

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(x: Int): Boolean = this <= x.toSize

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(x: Long): Boolean = this.toLong <= x

  /** Returns `true` if this value is less than or equal to x, `false` otherwise. */
  @inline def <=(other: Size): Boolean =
    this.toLong <= other.toLong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(x: Byte): Boolean = this > x.toSize

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(x: Short): Boolean = this > x.toSize

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(x: Int): Boolean = this > x.toSize

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(x: Long): Boolean = this.toLong > x

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline def >(other: Size): Boolean =
    this.toLong > other.toLong // TODO(shadaj): intrinsify

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(x: Byte): Boolean = this >= x.toSize

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(x: Short): Boolean = this >= x.toSize

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(x: Int): Boolean = this >= x.toSize

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(x: Long): Boolean = this.toLong >= x

  /** Returns `true` if this value is greater than or equal to x, `false` otherwise. */
  @inline def >=(other: Size): Boolean =
    this.toLong >= other.toLong // TODO(shadaj): intrinsify

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(x: Byte): Size = this & x.toSize

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(x: Short): Size = this & x.toSize

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(x: Int): Size = this & x.toSize

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(x: Long): Long = this.toLong & x

  /** Returns the bitwise AND of this value and `x`. */
  @inline def &(other: Size): Size =
    new Size(andRawSizes(rawSize, other.rawSize))

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(x: Byte): Size = this | x.toSize

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(x: Short): Size = this | x.toSize

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(x: Int): Size = this | x.toSize

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(x: Long): Long = this.toLong | x

  /** Returns the bitwise OR of this value and `x`. */
  @inline def |(other: Size): Size =
    new Size(orRawSizes(rawSize, other.rawSize))

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(x: Byte): Size = this ^ x.toSize

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(x: Short): Size = this ^ x.toSize

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(x: Int): Size = this ^ x.toSize

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(x: Long): Long = this.toLong ^ x

  /** Returns the bitwise XOR of this value and `x`. */
  @inline def ^(other: Size): Size =
    new Size(xorRawSizes(rawSize, other.rawSize))

  /** Returns the sum of this value and `x`. */
  @inline def +(x: Byte): Size = this + x.toSize

  /** Returns the sum of this value and `x`. */
  @inline def +(x: Short): Size = this + x.toSize

  /** Returns the sum of this value and `x`. */
  @inline def +(x: Int): Size = this + x.toSize

  /** Returns the sum of this value and `x`. */
  @inline def +(x: Long): Long = this.toLong + x

  /** Returns the sum of this value and `x`. */
  @inline def +(other: Size): Size =
    new Size(addRawSizes(rawSize, other.rawSize))

  /** Returns the difference of this value and `x`. */
  @inline def -(x: Byte): Size = this - x.toSize

  /** Returns the difference of this value and `x`. */
  @inline def -(x: Short): Size = this - x.toSize

  /** Returns the difference of this value and `x`. */
  @inline def -(x: Int): Size = this - x.toSize

  /** Returns the difference of this value and `x`. */
  @inline def -(x: Long): Long = this.toLong - x

  /** Returns the difference of this value and `x`. */
  @inline def -(other: Size): Size =
    new Size(subRawSizes(rawSize, other.rawSize))

  /** Returns the product of this value and `x`. */
  @inline def *(x: Byte): Size = this * x.toSize

  /** Returns the product of this value and `x`. */
  @inline def *(x: Short): Size = this * x.toSize

  /** Returns the product of this value and `x`. */
  @inline def *(x: Int): Size = this * x.toSize

  /** Returns the product of this value and `x`. */
  @inline def *(x: Long): Long = this.toLong * x

  /** Returns the product of this value and `x`. */
  @inline def *(other: Size): Size =
    new Size(multRawSizes(rawSize, other.rawSize))

  /** Returns the quotient of this value and `x`. */
  @inline def /(x: Byte): Size = this / x.toSize

  /** Returns the quotient of this value and `x`. */
  @inline def /(x: Short): Size = this / x.toSize

  /** Returns the quotient of this value and `x`. */
  @inline def /(x: Int): Size = this / x.toSize

  /** Returns the quotient of this value and `x`. */
  @inline def /(x: Long): Long = this.toLong / x

  /** Returns the quotient of this value and `x`. */
  @inline def /(other: Size): Size =
    new Size(divRawSizes(rawSize, other.rawSize))

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(x: Byte): Size = this % x.toSize

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(x: Short): Size = this % x.toSize

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(x: Int): Size = this % x.toSize

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(x: Long): Long = this.toLong % x

  /** Returns the remainder of the division of this value by `x`. */
  @inline def %(other: Size): Size =
    new Size(modRawSizes(rawSize, other.rawSize))

  // "Rich" API

  @inline final def max(that: Size): Size =
    if (this >= that) this else that
  @inline final def min(that: Size): Size =
    if (this <= that) this else that
}

object Size {
  @inline implicit def byteToSize(x: Byte): Size =
    new Size(castIntToRawSize(x))
  @inline implicit def shortToSize(x: Short): Size =
    new Size(castIntToRawSize(x))
  @inline implicit def intToSize(x: Int): Size =
    new Size(castIntToRawSize(x))
}
