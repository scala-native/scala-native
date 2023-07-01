package scala.scalanative
package unsigned

import scalanative.runtime.Intrinsics.{
  divULong,
  remULong,
  ulongToFloat,
  ulongToDouble,
  castLongToRawSize
}
import java.lang.{Long => JLong}

/** `ULong`, a 64-bit unsigned integer. */
final class ULong private[scalanative] (
    private[scalanative] val underlying: Long
) extends java.io.Serializable
    with Comparable[ULong] {

  @inline final def toByte: Byte = underlying.toByte
  @inline final def toShort: Short = underlying.toShort
  @inline final def toChar: Char = underlying.toChar
  @inline final def toInt: Int = underlying.toInt
  @inline final def toLong: Long = underlying
  @inline final def toFloat: Float = ulongToFloat(underlying)
  @inline final def toDouble: Double = ulongToDouble(underlying)

  @inline final def toUByte: UByte = UByte.valueOf(toByte)
  @inline final def toUShort: UShort = UShort.valueOf(toShort)
  @inline final def toUInt: UInt = UInt.valueOf(toInt)
  @inline final def toULong: ULong = this
  @inline final def toUSize: USize =
    USize.valueOf(castLongToRawSize(underlying))

  /** Returns the bitwise negation of this value.
   */
  @inline final def unary_~ : ULong = ULong.valueOf(~underlying)

  /** Returns this value bit-shifted left by the specified number of bits,
   *  filling in the new right bits with zeroes.
   *  @example
   *
   *  {{{
   *  6 << 3 == 48 // in binary: 0110 << 3 == 0110000
   *  }}}
   */
  @inline final def <<(x: Int): ULong = ULong.valueOf(underlying << x)

  /** Returns this value bit-shifted left by the specified number of bits,
   *  filling in the new right bits with zeroes.
   *  @example
   *
   *  {{{
   *  6 << 3 == 48 // in binary: 0110 << 3 == 0110000
   *  }}}
   */
  @inline final def <<(x: Long): ULong = ULong.valueOf(underlying << x)

  /** Returns this value bit-shifted right by the specified number of bits,
   *  filling the new left bits with zeroes.
   *  @example
   *
   *  {{{
   *  21 >>> 3 == 2 // in binary: 010101 >>> 3 == 010
   *
   *  4294967275 >>> 3 == 536870909
   *  // in binary: 11111111 11111111 11111111 11101011 >>> 3 ==
   *  //            00011111 11111111 11111111 11111101
   *  }}}
   */
  @inline final def >>>(x: Int): ULong = ULong.valueOf(underlying >>> x)

  /** Returns this value bit-shifted right by the specified number of bits,
   *  filling the new left bits with zeroes.
   *  @example
   *
   *  {{{
   *  21 >>> 3 == 2 // in binary: 010101 >>> 3 == 010
   *
   *  4294967275 >>> 3 == 536870909
   *  // in binary: 11111111 11111111 11111111 11101011 >>> 3 ==
   *  //            00011111 11111111 11111111 11111101
   *  }}}
   */
  @inline final def >>>(x: Long): ULong = ULong.valueOf(underlying >>> x)

  /** Returns this value bit-shifted left by the specified number of bits,
   *  filling in the right bits with the same value as the left-most bit of
   *  this.
   *  @example
   *
   *  {{{
   *  4294967275 >> 3 == 4294967293
   *  // in binary: 11111111 11111111 11111111 11101011 >> 3 ==
   *  //            11111111 11111111 11111111 11111101
   *  }}}
   */
  @inline final def >>(x: Int): ULong = ULong.valueOf(underlying >> x)

  /** Returns this value bit-shifted left by the specified number of bits,
   *  filling in the right bits with the same value as the left-most bit of
   *  this.
   *  @example
   *
   *  {{{
   *  4294967275 >> 3 == 4294967293
   *  // in binary: 11111111 11111111 11111111 11101011 >> 3 ==
   *  //            11111111 11111111 11111111 11111101
   *  }}}
   */
  @inline final def >>(x: Long): ULong = ULong.valueOf(underlying >> x)

  @inline final override def compareTo(x: ULong): Int =
    JLong.compareUnsigned(underlying, x.underlying)

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline final def ==(x: UByte): Boolean = this == x.toULong

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline final def ==(x: UShort): Boolean = this == x.toULong

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline final def ==(x: UInt): Boolean = this == x.toULong

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline final def ==(x: ULong): Boolean = underlying == x.underlying

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline final def !=(x: UByte): Boolean = this != x.toULong

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline final def !=(x: UShort): Boolean = this != x.toULong

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline final def !=(x: UInt): Boolean = this != x.toULong

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline final def !=(x: ULong): Boolean = underlying != x.underlying

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline final def <(x: UByte): Boolean = this < x.toULong

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline final def <(x: UShort): Boolean = this < x.toULong

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline final def <(x: UInt): Boolean = this < x.toULong

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline final def <(x: ULong): Boolean = compareTo(x) < 0

  /** Returns `true` if this value is less than or equal to x, `false`
   *  otherwise.
   */
  @inline final def <=(x: UByte): Boolean = this <= x.toULong

  /** Returns `true` if this value is less than or equal to x, `false`
   *  otherwise.
   */
  @inline final def <=(x: UShort): Boolean = this <= x.toULong

  /** Returns `true` if this value is less than or equal to x, `false`
   *  otherwise.
   */
  @inline final def <=(x: UInt): Boolean = this <= x.toULong

  /** Returns `true` if this value is less than or equal to x, `false`
   *  otherwise.
   */
  @inline final def <=(x: ULong): Boolean = compareTo(x) <= 0

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline final def >(x: UByte): Boolean = this > x.toULong

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline final def >(x: UShort): Boolean = this > x.toULong

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline final def >(x: UInt): Boolean = this > x.toULong

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline final def >(x: ULong): Boolean = compareTo(x) > 0

  /** Returns `true` if this value is greater than or equal to x, `false`
   *  otherwise.
   */
  @inline final def >=(x: UByte): Boolean = this >= x.toULong

  /** Returns `true` if this value is greater than or equal to x, `false`
   *  otherwise.
   */
  @inline final def >=(x: UShort): Boolean = this >= x.toULong

  /** Returns `true` if this value is greater than or equal to x, `false`
   *  otherwise.
   */
  @inline final def >=(x: UInt): Boolean = this >= x.toULong

  /** Returns `true` if this value is greater than or equal to x, `false`
   *  otherwise.
   */
  @inline final def >=(x: ULong): Boolean = compareTo(x) >= 0

  /** Returns the bitwise OR of this value and `x`. */
  @inline final def |(x: UByte): ULong = this | x.toULong

  /** Returns the bitwise OR of this value and `x`. */
  @inline final def |(x: UShort): ULong = this | x.toULong

  /** Returns the bitwise OR of this value and `x`. */
  @inline final def |(x: UInt): ULong = this | x.toULong

  /** Returns the bitwise OR of this value and `x`. */
  @inline final def |(x: ULong): ULong =
    ULong.valueOf(underlying | x.underlying)

  /** Returns the bitwise AND of this value and `x`. */
  @inline final def &(x: UByte): ULong = this & x.toULong

  /** Returns the bitwise AND of this value and `x`. */
  @inline final def &(x: UShort): ULong = this & x.toULong

  /** Returns the bitwise AND of this value and `x`. */
  @inline final def &(x: UInt): ULong = this & x.toULong

  /** Returns the bitwise AND of this value and `x`. */
  @inline final def &(x: ULong): ULong =
    ULong.valueOf(underlying & x.underlying)

  /** Returns the bitwise XOR of this value and `x`. */
  @inline final def ^(x: UByte): ULong = this ^ x.toULong

  /** Returns the bitwise XOR of this value and `x`. */
  @inline final def ^(x: UShort): ULong = this ^ x.toULong

  /** Returns the bitwise XOR of this value and `x`. */
  @inline final def ^(x: UInt): ULong = this ^ x.toULong

  /** Returns the bitwise XOR of this value and `x`. */
  @inline final def ^(x: ULong): ULong =
    ULong.valueOf(underlying ^ x.underlying)

  /** Returns the sum of this value and `x`. */
  @inline final def +(x: UByte): ULong = this + x.toULong

  /** Returns the sum of this value and `x`. */
  @inline final def +(x: UShort): ULong = this + x.toULong

  /** Returns the sum of this value and `x`. */
  @inline final def +(x: UInt): ULong = this + x.toULong

  /** Returns the sum of this value and `x`. */
  @inline final def +(x: ULong): ULong =
    ULong.valueOf(underlying + x.underlying)

  /** Returns the difference of this value and `x`. */
  @inline final def -(x: UByte): ULong = this - x.toULong

  /** Returns the difference of this value and `x`. */
  @inline final def -(x: UShort): ULong = this - x.toULong

  /** Returns the difference of this value and `x`. */
  @inline final def -(x: UInt): ULong = this - x.toULong

  /** Returns the difference of this value and `x`. */
  @inline final def -(x: ULong): ULong =
    ULong.valueOf(underlying - x.underlying)

  /** Returns the product of this value and `x`. */
  @inline final def *(x: UByte): ULong = this * x.toULong

  /** Returns the product of this value and `x`. */
  @inline final def *(x: UShort): ULong = this * x.toULong

  /** Returns the product of this value and `x`. */
  @inline final def *(x: UInt): ULong = this * x.toULong

  /** Returns the product of this value and `x`. */
  @inline final def *(x: ULong): ULong =
    ULong.valueOf(underlying * x.underlying)

  /** Returns the quotient of this value and `x`. */
  @inline final def /(x: UByte): ULong = this / x.toULong

  /** Returns the quotient of this value and `x`. */
  @inline final def /(x: UShort): ULong = this / x.toULong

  /** Returns the quotient of this value and `x`. */
  @inline final def /(x: UInt): ULong = this / x.toULong

  /** Returns the quotient of this value and `x`. */
  @inline final def /(x: ULong): ULong =
    ULong.valueOf(divULong(underlying, x.underlying))

  /** Returns the remainder of the division of this value by `x`. */
  @inline final def %(x: UByte): ULong = this % x.toULong

  /** Returns the remainder of the division of this value by `x`. */
  @inline final def %(x: UShort): ULong = this % x.toULong

  /** Returns the remainder of the division of this value by `x`. */
  @inline final def %(x: UInt): ULong = this % x.toULong

  /** Returns the remainder of the division of this value by `x`. */
  @inline final def %(x: ULong): ULong =
    ULong.valueOf(remULong(underlying, x.underlying))

  @inline final override def toString(): String =
    JLong.toUnsignedString(underlying)

  @inline override def hashCode(): Int = underlying.##

  @inline override def equals(obj: Any): Boolean = obj match {
    case that: ULong => this.underlying == that.underlying
    case _           => false
  }

  // "Rich" API

  @inline final def max(that: ULong): ULong = if (this >= that) this else that
  @inline final def min(that: ULong): ULong = if (this <= that) this else that

  @inline final def toBinaryString: String = toLong.toBinaryString
  @inline final def toHexString: String = toLong.toHexString
  @inline final def toOctalString: String = toLong.toOctalString
}

object ULong {

  /** The smallest value representable as a ULong. */
  final val MinValue = ULong.valueOf(0L)

  /** The largest value representable as a ULong. */
  final val MaxValue = ULong.valueOf(-1L)

  /** The String representation of the scala.ULong companion object. */
  override def toString(): String = "object scala.ULong"

  @inline def valueOf(longValue: scala.Long): ULong = {
    import ULongCache.cache
    val byteValue = longValue.toByte
    if (byteValue.toLong != longValue) {
      new ULong(longValue)
    } else {
      val idx = byteValue + 128
      val cached = cache(idx)
      if (cached ne null) cached
      else {
        val newBox = new ULong(longValue)
        cache(idx) = newBox
        newBox
      }
    }
  }
}

private[unsigned] object ULongCache {
  private[unsigned] val cache = new Array[ULong](256)
}
