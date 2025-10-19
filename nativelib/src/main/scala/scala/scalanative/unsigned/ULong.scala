package scala.scalanative
package unsigned

import scalanative.runtime.Intrinsics.{
  divULong,
  remULong,
  ulongToFloat,
  ulongToDouble,
  castLongToRawSize,
  unsignedOf
}
import java.lang.{Long => JLong}

/** `ULong`, a 64-bit unsigned integer. */
final class ULong private[scalanative] (
    private[scalanative] val underlyingValue: Long
) extends scala.math.ScalaNumber
    with java.io.Serializable
    with Comparable[ULong] {

  @inline final def toByte: Byte = underlyingValue.toByte
  @inline final def toShort: Short = underlyingValue.toShort
  @inline final def toChar: Char = underlyingValue.toChar
  @inline final def toInt: Int = underlyingValue.toInt
  @inline final def toLong: Long = underlyingValue
  @inline final def toFloat: Float = ulongToFloat(underlyingValue)
  @inline final def toDouble: Double = ulongToDouble(underlyingValue)

  @inline final def toUByte: UByte = unsignedOf(toByte)
  @inline final def toUShort: UShort = unsignedOf(toShort)
  @inline final def toUInt: UInt = unsignedOf(toInt)
  @inline final def toULong: ULong = this
  @inline final def toUSize: USize =
    unsignedOf(castLongToRawSize(underlyingValue))

  @inline override def doubleValue(): Double = toDouble
  @inline override def floatValue(): Float = toFloat
  @inline override def intValue(): Int = toInt
  @inline override def longValue(): Long = toLong
  @inline override protected def isWhole(): Boolean = true
  @inline override def underlying(): Object = this

  /** Returns the bitwise negation of this value.
   */
  @inline final def unary_~ : ULong = unsignedOf(~underlyingValue)

  /** Returns this value bit-shifted left by the specified number of bits,
   *  filling in the new right bits with zeroes.
   *  @example
   *
   *  {{{
   *  6 << 3 == 48 // in binary: 0110 << 3 == 0110000
   *  }}}
   */
  @inline final def <<(x: Int): ULong = unsignedOf(underlyingValue << x)

  /** Returns this value bit-shifted left by the specified number of bits,
   *  filling in the new right bits with zeroes.
   *  @example
   *
   *  {{{
   *  6 << 3 == 48 // in binary: 0110 << 3 == 0110000
   *  }}}
   */
  @inline final def <<(x: Long): ULong = unsignedOf(underlyingValue << x)

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
  @inline final def >>>(x: Int): ULong = unsignedOf(underlyingValue >>> x)

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
  @inline final def >>>(x: Long): ULong = unsignedOf(underlyingValue >>> x)

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
  @inline final def >>(x: Int): ULong = unsignedOf(underlyingValue >> x)

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
  @inline final def >>(x: Long): ULong = unsignedOf(underlyingValue >> x)

  @inline override final def compareTo(x: ULong): Int =
    JLong.compareUnsigned(underlyingValue, x.underlyingValue)

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline final def ==(x: UByte): Boolean = this == x.toULong

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline final def ==(x: UShort): Boolean = this == x.toULong

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline final def ==(x: UInt): Boolean = this == x.toULong

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline final def ==(x: ULong): Boolean = underlyingValue == x.underlyingValue

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline final def !=(x: UByte): Boolean = this != x.toULong

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline final def !=(x: UShort): Boolean = this != x.toULong

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline final def !=(x: UInt): Boolean = this != x.toULong

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline final def !=(x: ULong): Boolean = underlyingValue != x.underlyingValue

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
    unsignedOf(underlyingValue | x.underlyingValue)

  /** Returns the bitwise AND of this value and `x`. */
  @inline final def &(x: UByte): ULong = this & x.toULong

  /** Returns the bitwise AND of this value and `x`. */
  @inline final def &(x: UShort): ULong = this & x.toULong

  /** Returns the bitwise AND of this value and `x`. */
  @inline final def &(x: UInt): ULong = this & x.toULong

  /** Returns the bitwise AND of this value and `x`. */
  @inline final def &(x: ULong): ULong =
    unsignedOf(underlyingValue & x.underlyingValue)

  /** Returns the bitwise XOR of this value and `x`. */
  @inline final def ^(x: UByte): ULong = this ^ x.toULong

  /** Returns the bitwise XOR of this value and `x`. */
  @inline final def ^(x: UShort): ULong = this ^ x.toULong

  /** Returns the bitwise XOR of this value and `x`. */
  @inline final def ^(x: UInt): ULong = this ^ x.toULong

  /** Returns the bitwise XOR of this value and `x`. */
  @inline final def ^(x: ULong): ULong =
    unsignedOf(underlyingValue ^ x.underlyingValue)

  /** Returns the sum of this value and `x`. */
  @inline final def +(x: UByte): ULong = this + x.toULong

  /** Returns the sum of this value and `x`. */
  @inline final def +(x: UShort): ULong = this + x.toULong

  /** Returns the sum of this value and `x`. */
  @inline final def +(x: UInt): ULong = this + x.toULong

  /** Returns the sum of this value and `x`. */
  @inline final def +(x: ULong): ULong =
    unsignedOf(underlyingValue + x.underlyingValue)

  /** Returns the difference of this value and `x`. */
  @inline final def -(x: UByte): ULong = this - x.toULong

  /** Returns the difference of this value and `x`. */
  @inline final def -(x: UShort): ULong = this - x.toULong

  /** Returns the difference of this value and `x`. */
  @inline final def -(x: UInt): ULong = this - x.toULong

  /** Returns the difference of this value and `x`. */
  @inline final def -(x: ULong): ULong =
    unsignedOf(underlyingValue - x.underlyingValue)

  /** Returns the product of this value and `x`. */
  @inline final def *(x: UByte): ULong = this * x.toULong

  /** Returns the product of this value and `x`. */
  @inline final def *(x: UShort): ULong = this * x.toULong

  /** Returns the product of this value and `x`. */
  @inline final def *(x: UInt): ULong = this * x.toULong

  /** Returns the product of this value and `x`. */
  @inline final def *(x: ULong): ULong =
    unsignedOf(underlyingValue * x.underlyingValue)

  /** Returns the quotient of this value and `x`. */
  @inline final def /(x: UByte): ULong = this / x.toULong

  /** Returns the quotient of this value and `x`. */
  @inline final def /(x: UShort): ULong = this / x.toULong

  /** Returns the quotient of this value and `x`. */
  @inline final def /(x: UInt): ULong = this / x.toULong

  /** Returns the quotient of this value and `x`. */
  @inline final def /(x: ULong): ULong =
    unsignedOf(divULong(underlyingValue, x.underlyingValue))

  /** Returns the remainder of the division of this value by `x`. */
  @inline final def %(x: UByte): ULong = this % x.toULong

  /** Returns the remainder of the division of this value by `x`. */
  @inline final def %(x: UShort): ULong = this % x.toULong

  /** Returns the remainder of the division of this value by `x`. */
  @inline final def %(x: UInt): ULong = this % x.toULong

  /** Returns the remainder of the division of this value by `x`. */
  @inline final def %(x: ULong): ULong =
    unsignedOf(remULong(underlyingValue, x.underlyingValue))

  @inline override final def toString(): String =
    JLong.toUnsignedString(underlyingValue)

  @inline override def hashCode(): Int = underlyingValue.##

  @inline override def equals(obj: Any): Boolean = obj match {
    case that: ULong => this.underlyingValue == that.underlyingValue
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
  final val MinValue = unsignedOf(0L)

  /** The largest value representable as a ULong. */
  final val MaxValue = unsignedOf(-1L)

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
