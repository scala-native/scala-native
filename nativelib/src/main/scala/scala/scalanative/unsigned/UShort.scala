package scala.scalanative
package unsigned

import scalanative.runtime.Intrinsics.{castIntToRawSize, unsignedOf}

/** `UShort`, a 16-bit unsigned integer. */
final class UShort private[scalanative] (
    private[scalanative] val underlyingValue: Short
) extends scala.math.ScalaNumber
    with java.io.Serializable
    with Comparable[UShort] {

  @inline final def toByte: Byte = underlyingValue.toByte
  @inline final def toShort: Short = underlyingValue
  @inline final def toChar: Char = underlyingValue.toChar
  @inline final def toInt: Int = underlyingValue & 0xffff
  @inline final def toLong: Long = toInt.toLong
  @inline final def toFloat: Float = toInt.toFloat
  @inline final def toDouble: Double = toInt.toDouble

  @inline final def toUByte: UByte = unsignedOf(toByte)
  @inline final def toUShort: UShort = this
  @inline final def toUInt: UInt = unsignedOf(toInt)
  @inline final def toULong: ULong = unsignedOf(toLong)
  @inline final def toUSize: USize = unsignedOf(castIntToRawSize(toInt))

  @inline override def doubleValue(): Double = toDouble
  @inline override def floatValue(): Float = toFloat
  @inline override def intValue(): Int = toInt
  @inline override def longValue(): Long = toLong
  @inline override protected def isWhole(): Boolean = true
  @inline override def underlying(): Object = this

  /** Returns the bitwise negation of this value.
   *  @example
   *    {{{~5 == -6 // in binary: ~00000101 == // 11111010}}}
   */
  @inline final def unary_~ : UInt = ~toUInt

  /** Returns this value bit-shifted left by the specified number of bits,
   *  filling in the new right bits with zeroes.
   *  @example
   *    {{{6 << 3 == 48 // in binary: 0110 << 3 == 0110000}}}
   */
  @inline final def <<(x: Int): UInt = toUInt << x

  /** Returns this value bit-shifted left by the specified number of bits,
   *  filling in the new right bits with zeroes.
   *  @example
   *    {{{6 << 3 == 48 // in binary: 0110 << 3 == 0110000}}}
   */
  @inline final def <<(x: Long): UInt = toUInt << x

  /** Returns this value bit-shifted right by the specified number of bits,
   *  filling the new left bits with zeroes.
   *  @example
   *    {{{21 >>> 3 == 2 // in binary: 010101 >>> 3 == 010}}}
   *  @example
   *    {{{ 4294967275 >>> 3 == 536870909 // in binary: 11111111 11111111
   *    11111111 11101011 >>> 3 == // 00011111 11111111 11111111 11111101 }}}
   */
  @inline final def >>>(x: Int): UInt = toUInt >>> x

  /** Returns this value bit-shifted right by the specified number of bits,
   *  filling the new left bits with zeroes.
   *  @example
   *    {{{21 >>> 3 == 2 // in binary: 010101 >>> 3 == 010}}}
   *  @example
   *    {{{ 4294967275 >>> 3 == 536870909 // in binary: 11111111 11111111
   *    11111111 11101011 >>> 3 == // 00011111 11111111 11111111 11111101 }}}
   */
  @inline final def >>>(x: Long): UInt = toUInt >>> x

  /** Returns this value bit-shifted left by the specified number of bits,
   *  filling in the right bits with the same value as the left-most bit of
   *  this.
   *  @example
   *    {{{ 4294967275 >> 3 == 4294967293 // in binary: 11111111 11111111
   *    11111111 11101011 >> 3 == // 11111111 11111111 11111111 11111101 }}}
   */
  @inline final def >>(x: Int): UInt = toUInt >> x

  /** Returns this value bit-shifted left by the specified number of bits,
   *  filling in the right bits with the same value as the left-most bit of
   *  this.
   *  @example
   *    {{{ 4294967275 >> 3 == 4294967293 // in binary: 11111111 11111111
   *    11111111 11101011 >> 3 == // 11111111 11111111 11111111 11111101 }}}
   */
  @inline final def >>(x: Long): UInt = toUInt >> x

  @inline override final def compareTo(x: UShort): Int =
    (underlyingValue & 0xffff) - (x.underlyingValue & 0xffff)

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline final def ==(x: UByte): Boolean = toUInt == x.toUInt

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline final def ==(x: UShort): Boolean =
    underlyingValue == x.underlyingValue

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline final def ==(x: UInt): Boolean = toUInt == x

  /** Returns `true` if this value is equal to x, `false` otherwise. */
  @inline final def ==(x: ULong): Boolean = toULong == x

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline final def !=(x: UByte): Boolean = toUInt != x.toUInt

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline final def !=(x: UShort): Boolean =
    underlyingValue != x.underlyingValue

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline final def !=(x: UInt): Boolean = toUInt != x

  /** Returns `true` if this value is not equal to x, `false` otherwise. */
  @inline final def !=(x: ULong): Boolean = toULong != x

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline final def <(x: UByte): Boolean = toUInt < x.toUInt

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline final def <(x: UShort): Boolean = toUInt < x.toUInt

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline final def <(x: UInt): Boolean = toUInt < x

  /** Returns `true` if this value is less than x, `false` otherwise. */
  @inline final def <(x: ULong): Boolean = toULong < x

  /** Returns `true` if this value is less than or equal to x, `false`
   *  otherwise.
   */
  @inline final def <=(x: UByte): Boolean = toUInt <= x.toUInt

  /** Returns `true` if this value is less than or equal to x, `false`
   *  otherwise.
   */
  @inline final def <=(x: UShort): Boolean = toUInt <= x.toUInt

  /** Returns `true` if this value is less than or equal to x, `false`
   *  otherwise.
   */
  @inline final def <=(x: UInt): Boolean = toUInt <= x

  /** Returns `true` if this value is less than or equal to x, `false`
   *  otherwise.
   */
  @inline final def <=(x: ULong): Boolean = toULong <= x

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline final def >(x: UByte): Boolean = toUInt > x.toUInt

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline final def >(x: UShort): Boolean = toUInt > x.toUInt

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline final def >(x: UInt): Boolean = toUInt > x

  /** Returns `true` if this value is greater than x, `false` otherwise. */
  @inline final def >(x: ULong): Boolean = toULong > x

  /** Returns `true` if this value is greater than or equal to x, `false`
   *  otherwise.
   */
  @inline final def >=(x: UByte): Boolean = toUInt >= x.toUInt

  /** Returns `true` if this value is greater than or equal to x, `false`
   *  otherwise.
   */
  @inline final def >=(x: UShort): Boolean = toUInt >= x.toUInt

  /** Returns `true` if this value is greater than or equal to x, `false`
   *  otherwise.
   */
  @inline final def >=(x: UInt): Boolean = toUInt >= x

  /** Returns `true` if this value is greater than or equal to x, `false`
   *  otherwise.
   */
  @inline final def >=(x: ULong): Boolean = toULong >= x

  /** Returns the bitwise OR of this value and `x`. */
  @inline final def |(x: UByte): UInt = this.toUInt | x.toUInt

  /** Returns the bitwise OR of this value and `x`. */
  @inline final def |(x: UShort): UInt = this.toUInt | x.toUInt

  /** Returns the bitwise OR of this value and `x`. */
  @inline final def |(x: UInt): UInt = this.toUInt | x

  /** Returns the bitwise OR of this value and `x`. */
  @inline final def |(x: ULong): ULong = this.toULong | x

  /** Returns the bitwise AND of this value and `x`. */
  @inline final def &(x: UByte): UInt = this.toUInt & x.toUInt

  /** Returns the bitwise AND of this value and `x`. */
  @inline final def &(x: UShort): UInt = this.toUInt & x.toUInt

  /** Returns the bitwise AND of this value and `x`. */
  @inline final def &(x: UInt): UInt = this.toUInt & x

  /** Returns the bitwise AND of this value and `x`. */
  @inline final def &(x: ULong): ULong = this.toULong & x

  /** Returns the bitwise XOR of this value and `x`. */
  @inline final def ^(x: UByte): UInt = this.toUInt ^ x.toUInt

  /** Returns the bitwise XOR of this value and `x`. */
  @inline final def ^(x: UShort): UInt = this.toUInt ^ x.toUInt

  /** Returns the bitwise XOR of this value and `x`. */
  @inline final def ^(x: UInt): UInt = this.toUInt ^ x

  /** Returns the bitwise XOR of this value and `x`. */
  @inline final def ^(x: ULong): ULong = this.toULong ^ x

  /** Returns the sum of this value and `x`. */
  @inline final def +(x: UByte): UInt = this.toUInt + x.toUInt

  /** Returns the sum of this value and `x`. */
  @inline final def +(x: UShort): UInt = this.toUInt + x.toUInt

  /** Returns the sum of this value and `x`. */
  @inline final def +(x: UInt): UInt = this.toUInt + x

  /** Returns the sum of this value and `x`. */
  @inline final def +(x: ULong): ULong = this.toULong + x

  /** Returns the difference of this value and `x`. */
  @inline final def -(x: UByte): UInt = this.toUInt - x.toUInt

  /** Returns the difference of this value and `x`. */
  @inline final def -(x: UShort): UInt = this.toUInt - x.toUInt

  /** Returns the difference of this value and `x`. */
  @inline final def -(x: UInt): UInt = this.toUInt - x

  /** Returns the difference of this value and `x`. */
  @inline final def -(x: ULong): ULong = this.toULong - x

  /** Returns the product of this value and `x`. */
  @inline final def *(x: UByte): UInt = this.toUInt * x.toUInt

  /** Returns the product of this value and `x`. */
  @inline final def *(x: UShort): UInt = this.toUInt * x.toUInt

  /** Returns the product of this value and `x`. */
  @inline final def *(x: UInt): UInt = this.toUInt * x

  /** Returns the product of this value and `x`. */
  @inline final def *(x: ULong): ULong = this.toULong * x

  /** Returns the quotient of this value and `x`. */
  @inline final def /(x: UByte): UInt = this.toUInt / x.toUInt

  /** Returns the quotient of this value and `x`. */
  @inline final def /(x: UShort): UInt = this.toUInt / x.toUInt

  /** Returns the quotient of this value and `x`. */
  @inline final def /(x: UInt): UInt = this.toUInt / x

  /** Returns the quotient of this value and `x`. */
  @inline final def /(x: ULong): ULong = this.toULong / x

  /** Returns the remainder of the division of this value by `x`. */
  @inline final def %(x: UByte): UInt = this.toUInt % x.toUInt

  /** Returns the remainder of the division of this value by `x`. */
  @inline final def %(x: UShort): UInt = this.toUInt % x.toUInt

  /** Returns the remainder of the division of this value by `x`. */
  @inline final def %(x: UInt): UInt = this.toUInt % x

  /** Returns the remainder of the division of this value by `x`. */
  @inline final def %(x: ULong): ULong = this.toULong % x

  @inline override final def toString(): String = toInt.toString()

  @inline override def hashCode(): Int = underlyingValue.##

  @inline override def equals(obj: Any): Boolean = obj match {
    case that: UShort => this.underlyingValue == that.underlyingValue
    case _            => false
  }

  // "Rich" API

  @inline final def max(that: UShort): UShort =
    this.toUInt.max(that.toUInt).toUShort
  @inline final def min(that: UShort): UShort =
    this.toUInt.min(that.toUInt).toUShort

  @inline final def toBinaryString: String = toUInt.toBinaryString
  @inline final def toHexString: String = toUInt.toHexString
  @inline final def toOctalString: String = toUInt.toOctalString
}

object UShort {

  /** The smallest value representable as a UShort. */
  final val MinValue = unsignedOf(0.toShort)

  /** The largest value representable as a UShort. */
  final val MaxValue = unsignedOf((-1).toShort)

  /** The String representation of the scala.UShort companion object. */
  override def toString(): String = "object scala.UShort"

  /** Language mandated coercions from UShort to "wider" types. */
  import scala.language.implicitConversions
  implicit def ubyte2uint(x: UShort): UInt = x.toUInt
  implicit def ubyte2ulong(x: UShort): ULong = x.toULong

  @inline def valueOf(shortValue: scala.Short): UShort = {
    import UShortCache.cache
    val byteValue = shortValue.toByte
    if (byteValue.toLong != shortValue) {
      new UShort(shortValue)
    } else {
      val idx = byteValue + 128
      val cached = cache(idx)
      if (cached ne null) cached
      else {
        val newBox = new UShort(shortValue)
        cache(idx) = newBox
        newBox
      }
    }
  }
}

private[unsigned] object UShortCache {
  private[unsigned] val cache = new Array[UShort](256)
}
