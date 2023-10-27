package scala.scalanative
package unsigned

import scalanative.runtime.Intrinsics.{
  divUInt,
  remUInt,
  uintToFloat,
  uintToDouble,
  castIntToRawSizeUnsigned,
  unsignedOf
}
import java.lang.{Integer => JInteger}

/** `UInt`, a 32-bit unsigned integer. */
final class UInt private[scalanative] (override val underlying: Int)
    extends scala.math.ScalaNumber
    with java.io.Serializable
    with Comparable[UInt] {

  @inline final def toByte: Byte = underlying.toByte
  @inline final def toShort: Short = underlying.toShort
  @inline final def toChar: Char = underlying.toChar
  @inline final def toInt: Int = underlying
  @inline final def toLong: Long = JInteger.toUnsignedLong(underlying)
  @inline final def toFloat: Float = uintToFloat(underlying)
  @inline final def toDouble: Double = uintToDouble(underlying)

  @inline final def toUByte: UByte = unsignedOf(toByte)
  @inline final def toUShort: UShort = unsignedOf(toShort)
  @inline final def toUInt: UInt = this
  @inline final def toULong: ULong = unsignedOf(toLong)
  @inline final def toUSize: USize = unsignedOf(
    castIntToRawSizeUnsigned(underlying)
  )

  @inline override def doubleValue(): Double = toDouble
  @inline override def floatValue(): Float = toFloat
  @inline override def intValue(): Int = toInt
  @inline override def longValue(): Long = toLong
  @inline override protected def isWhole(): Boolean = true

  /** Returns the bitwise negation of this value.
   *  @example
   *    {{{~5 == 4294967290 // in binary: ~00000101 == // 11111010}}}
   */
  @inline final def unary_~ : UInt = unsignedOf(~underlying)

  /** Returns this value bit-shifted left by the specified number of bits,
   *  filling in the new right bits with zeroes.
   *  @example
   *    {{{6 << 3 == 48 // in binary: 0110 << 3 == 0110000}}}
   */
  @inline final def <<(x: Int): UInt = unsignedOf(underlying << x)

  /** Returns this value bit-shifted left by the specified number of bits,
   *  filling in the new right bits with zeroes.
   *  @example
   *    {{{6 << 3 == 48 // in binary: 0110 << 3 == 0110000}}}
   */
  @inline final def <<(x: Long): UInt = unsignedOf(underlying << x.toInt)

  /** Returns this value bit-shifted right by the specified number of bits,
   *  filling the new left bits with zeroes.
   *  @example
   *    {{{21 >>> 3 == 2 // in binary: 010101 >>> 3 == 010}}}
   *  @example
   *    {{{ 4294967275 >>> 3 == 536870909 // in binary: 11111111 11111111
   *    11111111 11101011 >>> 3 == // 00011111 11111111 11111111 11111101 }}}
   */
  @inline final def >>>(x: Int): UInt = unsignedOf(underlying >>> x)

  /** Returns this value bit-shifted right by the specified number of bits,
   *  filling the new left bits with zeroes.
   *  @example
   *    {{{21 >>> 3 == 2 // in binary: 010101 >>> 3 == 010}}}
   *  @example
   *    {{{ 4294967275 >>> 3 == 536870909 // in binary: 11111111 11111111
   *    11111111 11101011 >>> 3 == // 00011111 11111111 11111111 11111101 }}}
   */
  @inline final def >>>(x: Long): UInt = unsignedOf(underlying >>> x.toInt)

  /** Returns this value bit-shifted left by the specified number of bits,
   *  filling in the right bits with the same value as the left-most bit of
   *  this.
   *  @example
   *    {{{ 4294967275 >> 3 == 4294967293 // in binary: 11111111 11111111
   *    11111111 11101011 >> 3 == // 11111111 11111111 11111111 11111101 }}}
   */
  @inline final def >>(x: Int): UInt = unsignedOf(underlying >> x)

  /** Returns this value bit-shifted left by the specified number of bits,
   *  filling in the right bits with the same value as the left-most bit of
   *  this.
   *  @example
   *    {{{ 4294967275 >> 3 == 4294967293 // in binary: 11111111 11111111
   *    11111111 11101011 >> 3 == // 11111111 11111111 11111111 11111101 }}}
   */
  @inline final def >>(x: Long): UInt = unsignedOf(underlying >> x.toInt)

  @inline final override def compareTo(x: UInt): Int =
    JInteger.compareUnsigned(underlying, x.underlying)

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline final def ==(x: UByte): Boolean = this == x.toUInt

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline final def ==(x: UShort): Boolean = this == x.toUInt

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline final def ==(x: UInt): Boolean = underlying == x.underlying

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline final def ==(x: ULong): Boolean = this.toULong == x

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline final def !=(x: UByte): Boolean = this != x.toUInt

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline final def !=(x: UShort): Boolean = this != x.toUInt

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline final def !=(x: UInt): Boolean = underlying != x.underlying

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline final def !=(x: ULong): Boolean = this.toULong != x

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline final def <(x: UByte): Boolean = this < x.toUInt

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline final def <(x: UShort): Boolean = this < x.toUInt

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline final def <(x: UInt): Boolean = compareTo(x) < 0

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline final def <(x: ULong): Boolean = this.toULong < x

  /** Returns `true` if this value is less than or equal to x, `false`
   *  otherwise.
   */
  @inline final def <=(x: UByte): Boolean = this <= x.toUInt

  /** Returns `true` if this value is less than or equal to x, `false`
   *  otherwise.
   */
  @inline final def <=(x: UShort): Boolean = this <= x.toUInt

  /** Returns `true` if this value is less than or equal to x, `false`
   *  otherwise.
   */
  @inline final def <=(x: UInt): Boolean = compareTo(x) <= 0

  /** Returns `true` if this value is less than or equal to x, `false`
   *  otherwise.
   */
  @inline final def <=(x: ULong): Boolean = this.toULong <= x

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline final def >(x: UByte): Boolean = this > x.toUInt

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline final def >(x: UShort): Boolean = this > x.toUInt

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline final def >(x: UInt): Boolean = compareTo(x) > 0

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline final def >(x: ULong): Boolean = this.toULong > x

  /** Returns `true` if this value is greater than or equal to x, `false`
   *  otherwise.
   */
  @inline final def >=(x: UByte): Boolean = this >= x.toUInt

  /** Returns `true` if this value is greater than or equal to x, `false`
   *  otherwise.
   */
  @inline final def >=(x: UShort): Boolean = this >= x.toUInt

  /** Returns `true` if this value is greater than or equal to x, `false`
   *  otherwise.
   */
  @inline final def >=(x: UInt): Boolean = compareTo(x) >= 0

  /** Returns `true` if this value is greater than or equal to x, `false`
   *  otherwise.
   */
  @inline final def >=(x: ULong): Boolean = this.toULong >= x

  /** Returns the bitwise OR of this value and `x`. */
  @inline final def |(x: UByte): UInt = this | x.toUInt

  /** Returns the bitwise OR of this value and `x`. */
  @inline final def |(x: UShort): UInt = this | x.toUInt

  /** Returns the bitwise OR of this value and `x`. */
  @inline final def |(x: UInt): UInt = unsignedOf(underlying | x.underlying)

  /** Returns the bitwise OR of this value and `x`. */
  @inline final def |(x: ULong): ULong = this.toULong | x

  /** Returns the bitwise AND of this value and `x`. */
  @inline final def &(x: UByte): UInt = this & x.toUInt

  /** Returns the bitwise AND of this value and `x`. */
  @inline final def &(x: UShort): UInt = this & x.toUInt

  /** Returns the bitwise AND of this value and `x`. */
  @inline final def &(x: UInt): UInt = unsignedOf(underlying & x.underlying)

  /** Returns the bitwise AND of this value and `x`. */
  @inline final def &(x: ULong): ULong = this.toULong & x

  /** Returns the bitwise XOR of this value and `x`. */
  @inline final def ^(x: UByte): UInt = this ^ x.toUInt

  /** Returns the bitwise XOR of this value and `x`. */
  @inline final def ^(x: UShort): UInt = this ^ x.toUInt

  /** Returns the bitwise XOR of this value and `x`. */
  @inline final def ^(x: UInt): UInt = unsignedOf(underlying ^ x.underlying)

  /** Returns the bitwise XOR of this value and `x`. */
  @inline final def ^(x: ULong): ULong = this.toULong ^ x

  /** Returns the sum of this value and `x`. */
  @inline final def +(x: UByte): UInt = this + x.toUInt

  /** Returns the sum of this value and `x`. */
  @inline final def +(x: UShort): UInt = this + x.toUInt

  /** Returns the sum of this value and `x`. */
  @inline final def +(x: UInt): UInt = unsignedOf(underlying + x.underlying)

  /** Returns the sum of this value and `x`. */
  @inline final def +(x: ULong): ULong = this.toULong + x

  /** Returns the difference of this value and `x`. */
  @inline final def -(x: UByte): UInt = this - x.toUInt

  /** Returns the difference of this value and `x`. */
  @inline final def -(x: UShort): UInt = this - x.toUInt

  /** Returns the difference of this value and `x`. */
  @inline final def -(x: UInt): UInt = unsignedOf(underlying - x.underlying)

  /** Returns the difference of this value and `x`. */
  @inline final def -(x: ULong): ULong = this.toULong - x

  /** Returns the product of this value and `x`. */
  @inline final def *(x: UByte): UInt = this * x.toUInt

  /** Returns the product of this value and `x`. */
  @inline final def *(x: UShort): UInt = this * x.toUInt

  /** Returns the product of this value and `x`. */
  @inline final def *(x: UInt): UInt = unsignedOf(underlying * x.underlying)

  /** Returns the product of this value and `x`. */
  @inline final def *(x: ULong): ULong = this.toULong * x

  /** Returns the quotient of this value and `x`. */
  @inline final def /(x: UByte): UInt = this / x.toUInt

  /** Returns the quotient of this value and `x`. */
  @inline final def /(x: UShort): UInt = this / x.toUInt

  /** Returns the quotient of this value and `x`. */
  @inline final def /(x: UInt): UInt =
    unsignedOf(divUInt(underlying, x.underlying))

  /** Returns the quotient of this value and `x`. */
  @inline final def /(x: ULong): ULong = this.toULong / x

  /** Returns the remainder of the division of this value by `x`. */
  @inline final def %(x: UByte): UInt = this % x.toUInt

  /** Returns the remainder of the division of this value by `x`. */
  @inline final def %(x: UShort): UInt = this % x.toUInt

  /** Returns the remainder of the division of this value by `x`. */
  @inline final def %(x: UInt): UInt =
    unsignedOf(remUInt(underlying, x.underlying))

  /** Returns the remainder of the division of this value by `x`. */
  @inline final def %(x: ULong): ULong = this.toULong % x

  @inline final override def toString(): String =
    JInteger.toUnsignedString(underlying)

  @inline override def hashCode(): Int = underlying.##

  @inline override def equals(obj: Any): Boolean = obj match {
    case that: UInt => this.underlying == that.underlying
    case _          => false
  }

  // "Rich" API

  @inline final def max(that: UInt): UInt = if (this >= that) this else that
  @inline final def min(that: UInt): UInt = if (this <= that) this else that

  @inline final def toBinaryString: String = toInt.toBinaryString
  @inline final def toHexString: String = toInt.toHexString
  @inline final def toOctalString: String = toInt.toOctalString
}

object UInt {

  /** The smallest value representable as a UInt. */
  final val MinValue = unsignedOf(0)

  /** The largest value representable as a UInt. */
  final val MaxValue = unsignedOf(-1)

  /** The String representation of the scala.UInt companion object. */
  override def toString(): String = "object scala.UInt"

  /** Language mandated coercions from UInt to "wider" types. */
  import scala.language.implicitConversions
  implicit def uint2ulong(x: UInt): ULong = x.toULong

  @inline def valueOf(intValue: scala.Int): UInt = {
    import UIntCache.cache
    val byteValue = intValue.toByte
    if (byteValue.toInt != intValue) {
      new UInt(intValue)
    } else {
      val idx = intValue + 128
      val cached = cache(idx)
      if (cached ne null) cached
      else {
        val newBox = new UInt(intValue)
        cache(idx) = newBox
        newBox
      }
    }
  }

}

private[unsigned] object UIntCache {
  private[unsigned] val cache = new Array[UInt](256)
}
