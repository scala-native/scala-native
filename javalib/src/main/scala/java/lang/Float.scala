package java.lang

import java.lang.IEEE754Helpers.parseIEEE754
import java.lang.constant.{Constable, ConstantDesc}
import java.{lang => jl, util => ju}

import scalanative.libc
import scalanative.runtime.Intrinsics
import scalanative.runtime.ieee754tostring.ryu.{RyuFloat, RyuRoundingMode}

final class Float(val _value: scala.Float)
    extends Number
    with Comparable[Float]
    with Constable
    with ConstantDesc {
  @inline def this(s: String) =
    this(Float.parseFloat(s))

  @inline override def byteValue(): scala.Byte =
    _value.toByte

  @inline override def shortValue(): scala.Short =
    _value.toShort

  @inline override def intValue(): scala.Int =
    _value.toInt

  @inline override def longValue(): scala.Long =
    _value.toLong

  @inline override def floatValue(): scala.Float =
    _value

  @inline override def doubleValue(): scala.Double =
    _value.toDouble

  @inline override def equals(that: Any): scala.Boolean =
    that match {
      case that: Float =>
        // As floats with different NaNs are considered equal,
        // use floatToIntBits instead of floatToRawIntBits
        val a = Float.floatToIntBits(_value)
        val b = Float.floatToIntBits(that._value)
        a == b

      case _ =>
        false
    }

  @inline override def hashCode(): Int =
    Float.hashCode(_value)

  @inline override def compareTo(that: Float): Int =
    Float.compare(_value, that._value)

  @inline override def toString(): String =
    Float.toString(_value)

  @inline def isNaN(): scala.Boolean =
    Float.isNaN(_value)

  @inline def isInfinite(): scala.Boolean =
    Float.isInfinite(_value)

  /*
   * Ported from ScalaJS
   *
   * Methods on scala.Float
   * The following methods are only here to properly support reflective calls
   * on boxed primitive values. YOU WILL NOT BE ABLE TO USE THESE METHODS, since
   * we use the true javalib to lookup symbols, this file contains only
   * implementations.
   */
  protected def toByte: scala.Byte = _value.toByte
  protected def toShort: scala.Short = _value.toShort
  protected def toChar: scala.Char = _value.toChar
  protected def toInt: scala.Int = _value.toInt
  protected def toLong: scala.Long = _value.toLong
  protected def toFloat: scala.Float = _value
  protected def toDouble: scala.Double = _value.toDouble

  protected def unary_+ : scala.Float = _value
  protected def unary_- : scala.Float = -_value

  protected def +(x: String): String = "" + _value + x

  protected def <(x: scala.Byte): scala.Boolean = _value < x
  protected def <(x: scala.Short): scala.Boolean = _value < x
  protected def <(x: scala.Char): scala.Boolean = _value < x
  protected def <(x: scala.Int): scala.Boolean = _value < x
  protected def <(x: scala.Long): scala.Boolean = _value < x
  protected def <(x: scala.Float): scala.Boolean = _value < x
  protected def <(x: scala.Double): scala.Boolean = _value < x

  protected def <=(x: scala.Byte): scala.Boolean = _value <= x
  protected def <=(x: scala.Short): scala.Boolean = _value <= x
  protected def <=(x: scala.Char): scala.Boolean = _value <= x
  protected def <=(x: scala.Int): scala.Boolean = _value <= x
  protected def <=(x: scala.Long): scala.Boolean = _value <= x
  protected def <=(x: scala.Float): scala.Boolean = _value <= x
  protected def <=(x: scala.Double): scala.Boolean = _value <= x

  protected def >(x: scala.Byte): scala.Boolean = _value > x
  protected def >(x: scala.Short): scala.Boolean = _value > x
  protected def >(x: scala.Char): scala.Boolean = _value > x
  protected def >(x: scala.Int): scala.Boolean = _value > x
  protected def >(x: scala.Long): scala.Boolean = _value > x
  protected def >(x: scala.Float): scala.Boolean = _value > x
  protected def >(x: scala.Double): scala.Boolean = _value > x

  protected def >=(x: scala.Byte): scala.Boolean = _value >= x
  protected def >=(x: scala.Short): scala.Boolean = _value >= x
  protected def >=(x: scala.Char): scala.Boolean = _value >= x
  protected def >=(x: scala.Int): scala.Boolean = _value >= x
  protected def >=(x: scala.Long): scala.Boolean = _value >= x
  protected def >=(x: scala.Float): scala.Boolean = _value >= x
  protected def >=(x: scala.Double): scala.Boolean = _value >= x

  protected def +(x: scala.Byte): scala.Float = _value + x
  protected def +(x: scala.Short): scala.Float = _value + x
  protected def +(x: scala.Char): scala.Float = _value + x
  protected def +(x: scala.Int): scala.Float = _value + x
  protected def +(x: scala.Long): scala.Float = _value + x
  protected def +(x: scala.Float): scala.Float = _value + x
  protected def +(x: scala.Double): scala.Double = _value + x

  protected def -(x: scala.Byte): scala.Float = _value - x
  protected def -(x: scala.Short): scala.Float = _value - x
  protected def -(x: scala.Char): scala.Float = _value - x
  protected def -(x: scala.Int): scala.Float = _value - x
  protected def -(x: scala.Long): scala.Float = _value - x
  protected def -(x: scala.Float): scala.Float = _value - x
  protected def -(x: scala.Double): scala.Double = _value - x

  protected def *(x: scala.Byte): scala.Float = _value * x
  protected def *(x: scala.Short): scala.Float = _value * x
  protected def *(x: scala.Char): scala.Float = _value * x
  protected def *(x: scala.Int): scala.Float = _value * x
  protected def *(x: scala.Long): scala.Float = _value * x
  protected def *(x: scala.Float): scala.Float = _value * x
  protected def *(x: scala.Double): scala.Double = _value * x

  protected def /(x: scala.Byte): scala.Float = _value / x
  protected def /(x: scala.Short): scala.Float = _value / x
  protected def /(x: scala.Char): scala.Float = _value / x
  protected def /(x: scala.Int): scala.Float = _value / x
  protected def /(x: scala.Long): scala.Float = _value / x
  protected def /(x: scala.Float): scala.Float = _value / x
  protected def /(x: scala.Double): scala.Double = _value / x

  protected def %(x: scala.Byte): scala.Float = _value % x
  protected def %(x: scala.Short): scala.Float = _value % x
  protected def %(x: scala.Char): scala.Float = _value % x
  protected def %(x: scala.Int): scala.Float = _value % x
  protected def %(x: scala.Long): scala.Float = _value % x
  protected def %(x: scala.Float): scala.Float = _value % x
  protected def %(x: scala.Double): scala.Double = _value % x
  /* Scala Native additions for features added after Java 8.
   */

  /** Since: Java 12 */
  def describeConstable(): ju.Optional[jl.Float] =
    ju.Optional.of(this)

  /** Since: Java 12
   *
   *  resolveConstantDesc requires reflection so its Test can not presently (SN
   *  0.5.12) be implemented.
   */
  // def resolveConstantDesc (): java.lang.Float
}

object Float {
  final val BYTES = 4
  final val MAX_EXPONENT = 127
  final val MAX_VALUE = 3.40282346638528860e+38f
  final val MIN_EXPONENT = -126
  final val MIN_NORMAL = 1.17549435e-38f
  final val MIN_VALUE = 1.40129846432481707e-45f
  final val NaN = 0.0f / 0.0f
  final val NEGATIVE_INFINITY = 1.0f / -0.0f
  final val POSITIVE_INFINITY = 1.0f / 0.0f
  final val SIZE = 32
  final val TYPE =
    scala.Predef.classOf[scala.scalanative.runtime.PrimitiveFloat]

  /** Since: Java 19 */
  final val PRECISION = 24

  @inline def compare(x: scala.Float, y: scala.Float): scala.Int =
    if (x > y) 1
    else if (x < y) -1
    else if (x == y && 0.0f != x) 0
    else {
      if (isNaN(x)) {
        if (isNaN(y)) 0
        else 1
      } else if (isNaN(y)) {
        -1
      } else {
        val f1 = floatToRawIntBits(x)
        val f2 = floatToRawIntBits(y)
        (f1 >> 31) - (f2 >> 31)
      }
    }

  @inline def floatToIntBits(value: scala.Float): scala.Int =
    if (value != value) 0x7fc00000
    else floatToRawIntBits(value)

  @inline def floatToRawIntBits(value: scala.Float): scala.Int =
    Intrinsics.castFloatToInt(value)

  @inline def hashCode(value: scala.Float): scala.Int =
    floatToIntBits(value)

  @inline def intBitsToFloat(value: scala.Int): scala.Float =
    Intrinsics.castIntToFloat(value)

  // Ported from Scala.js commit: 217f3a3 dated: 2021-02-19
  @inline def isFinite(f: scala.Float): scala.Boolean =
    !isNaN(f) && !isInfinite(f)

  @inline def isInfinite(v: scala.Float): scala.Boolean =
    v == POSITIVE_INFINITY || v == NEGATIVE_INFINITY

  @inline def isNaN(v: scala.Float): scala.Boolean =
    v != v

  @inline def max(a: scala.Float, b: scala.Float): scala.Float =
    Math.max(a, b)

  @inline def min(a: scala.Float, b: scala.Float): scala.Float =
    Math.min(a, b)

  def parseFloat(s: String): scala.Float =
    parseIEEE754[scala.Float](s, libc.stdlib.strtof)

  @inline def sum(a: scala.Float, b: scala.Float): scala.Float =
    a + b

  def toHexString(f: scala.Float): String =
    if (f != f) {
      "NaN"
    } else if (f == POSITIVE_INFINITY) {
      "Infinity"
    } else if (f == NEGATIVE_INFINITY) {
      "-Infinity"
    } else {
      val bitValue = floatToIntBits(f)
      val negative = (bitValue & 0x80000000) != 0
      val exponent = (bitValue & 0x7f800000) >>> 23
      var significand = (bitValue & 0x007fffff) << 1

      if (exponent == 0 && significand == 0) {
        if (negative) "-0x0.0p0"
        else "0x0.0p0"
      } else {
        val hexString = new StringBuilder(10)

        if (negative) {
          hexString.append("-0x")
        } else {
          hexString.append("0x")
        }

        if (exponent == 0) {
          hexString.append("0.")

          var fractionDigits = 6
          while ((significand != 0) && ((significand & 0xf) == 0)) {
            significand >>>= 4
            fractionDigits -= 1
          }

          val hexSignificand = Integer.toHexString(significand)
          if (significand != 0 && fractionDigits > hexSignificand.length) {
            var digitDiff = fractionDigits - hexSignificand.length - 1
            while (digitDiff != 0) {
              hexString.append('0')
              digitDiff -= 1
            }
          }

          hexString.append(hexSignificand)
          hexString.append("p-126")
        } else {
          hexString.append("1.")

          var fractionDigits = 6
          while ((significand != 0) && ((significand & 0xf) == 0)) {
            significand >>>= 4
            fractionDigits -= 1
          }

          val hexSignificand = Integer.toHexString(significand)
          if (significand != 0 && fractionDigits > hexSignificand.length) {
            var digitDiff = fractionDigits - hexSignificand.length - 1
            while (digitDiff != 0) {
              hexString.append('0')
              digitDiff -= 1
            }
          }

          hexString.append(hexSignificand)
          hexString.append('p')
          hexString.append(Integer.toString(exponent - 127))
        }

        hexString.toString
      }
    }

  def toString(f: scala.Float): String = {
    val result = new scala.Array[Char](RyuFloat.RESULT_STRING_MAX_LENGTH)
    val strLen =
      RyuFloat.floatToChars(f, RyuRoundingMode.Conservative, result, 0)
    new _String(0, strLen, result).asInstanceOf[String]
  }

  @inline def valueOf(s: String): Float =
    valueOf(parseFloat(s))

  @inline def valueOf(f: scala.Float): Float =
    new Float(f)

  /* Scala Native additions for features added after Java 8.
   */

  /** Since: Java 20 */
  def float16ToFloat(floatBinary16: scala.Short): scala.Float = {
    val signF16 = floatBinary16 >>> 31
    val biasedExponentF16 = (floatBinary16 & 0x7c00) >>> 10 // 5 bits
    val significandF16 = floatBinary16 & 0x3ff // Low 10 bits

    val absFloatBinary16 = floatBinary16 & (0x7fff)

    // For amortized performance, test most commonly expected cases first.
    if ((absFloatBinary16 >= 0x0400) &&
        (absFloatBinary16 <= 0x7bff)) { // arg is an binary16 normal
      // 127 is binary32 bias. 15 is binary16 bias.
      val biasedExponentF32 = biasedExponentF16 + (127 - 15)

      val intBits = (signF16 << 31)
        | (biasedExponentF32 << 23)
        | significandF16 << 13

      jl.Float.intBitsToFloat(intBits)
    } else if (floatBinary16 == 0x0) {
      0.0f
    } else if ((absFloatBinary16 > 0x0) &&
        (absFloatBinary16 <= 0x03ff)) { // arg is a binary16 subnormal
      /* Heads up! Here is where the magic happens.
       * The binary16 subnormal will always be a binary32 normal so
       * the former must be normalized.
       */

      val totalLeadingZeros = jl.Integer.numberOfLeadingZeros(significandF16)

      /* Magic number 22 bears close attention. Six leading zeros
       * come from the Short argument, or else execution would not have reached
       * here. Sixteen leading zeros were added when the 10 bit significand
       * field was extracted. The logical_and promoted its arguments and
       * returned a 32 bit Int.
       */
      val significandLeadingZeros = totalLeadingZeros - 22
      val exponentShiftCount = significandLeadingZeros + 1

      /* 127 is the binary32 bias. 15 is the binary32 bias.
       * significandLeadingZeros is normalization adjustment.
       */
      val biasedExponentF32 = (127 - 15) - significandLeadingZeros

      val intBits = (signF16 << 31)
        | (biasedExponentF32 << 23)
        | significandF16 << (13 + exponentShiftCount)

      jl.Float.intBitsToFloat(intBits)
    } else if (floatBinary16 == 0x7c00) {
      jl.Float.POSITIVE_INFINITY
    } else if (floatBinary16 == 0xfc00.toShort) { // drop set Int rhs high bits
      jl.Float.NEGATIVE_INFINITY
    } else if (floatBinary16 == 0x8000.toShort) { // drop set Int rhs high bits
      -0.0f
    } else if (biasedExponentF16 == 0x1f) { // 5 bits, all 1's
      jl.Float.NaN
    } else { // Should never reach here
      throw new IllegalArgumentException(
        "Fatal internal error in float16ToFloat() decision tree"
      )
    }
  }

  def floatToFloat16(floatBinary32: scala.Float): scala.Short = {

    def roundEvenTo10Bits(bits23: Int): Int = {
      // Precondition: floatBinary32 values which would cause an
      // f16 Infinity have been filtered out before calling this method.

      val low23Bits = (bits23 & 0x7fffff)

      val bit13 = (bits23 >>> 12) & 0x1
      val low12Bits = (bits23 & 0xfff)

      val roundUp =
        if (bit13 == 0) false
        else low12Bits != 0

      val base = bits23 >>> 13 // use high 10 bits

      /* If the addition happens, numerical overflow to +Infinity is not
       * a concern. F32 values which would cause an F16 Infinity
       * have been filtered out as a precondition. That leaves
       * 65504, the largest F16 as the largest candidate. That is
       * even, so it will not cause rounding. Incrementing any lesser
       * value, negative or positive will not cause overflow. QED.
       */

      if (!roundUp) base
      else base + 1
    }

    val absFloatBinary32 = Math.abs(floatBinary32)
    val f32Bits = jl.Float.floatToIntBits(floatBinary32)

    val signF32 = f32Bits >>> 31
    val biasedExponentF32 = (f32Bits & 0x7f800000) >>> 23 // 8 bits
    val significandF32 = f32Bits & 0x7fffff // Low 23 bits

    val smallestNormalF16 = 0.00006103515625f

    // For amortized performance, test most commonly expected cases first.
    if ((absFloatBinary32 >= smallestNormalF16)
        && (absFloatBinary32 <= 65504.0)) { // largest finite F16
      // result is F16 normal

      val biasedExponentF16 = biasedExponentF32 - 127 + 15

      val intBits = (signF32 << 15)
        | (biasedExponentF16 << 10)
        | roundEvenTo10Bits(significandF32)

      intBits.toShort
    } else if (absFloatBinary32 < smallestNormalF16) {
      if (absFloatBinary32 < 0.000000059604645f) { // smallest f16 subnorm
        // underflow
        if (signF32 == 0) 0.toShort
        else 0x8000.toShort
      } else { // result is an F16 subnormal
        val adjSignificandF32 = significandF32 | 0x800000

        val shift = -14 - (biasedExponentF32 - 127)

        val intBits = (signF32 << 15)
          | roundEvenTo10Bits(adjSignificandF32 >>> shift)

        intBits.toShort
      }
    } else if (absFloatBinary32 > 65504.0f) { // largest F16
      if (signF32 == 0) 0x7c00.toShort // positive Infinity
      else 0xfc00.toShort // negative Infinity
    } else if (floatBinary32.isNaN()) {
      0x7e00.toShort
    } else { // Should never reach here
      throw new IllegalArgumentException(
        "Fatal internal error in floatToFloat16() decision tree"
      )
    }
  }
}
