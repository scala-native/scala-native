package java.lang

import scalanative.native._

final class Double(override val doubleValue: scala.Double)
    extends Number
    with Comparable[Double] {
  @inline def this(s: String) =
    this(Double.parseDouble(s))

  @inline override def byteValue(): scala.Byte =
    doubleValue.toByte

  @inline override def shortValue(): scala.Short =
    doubleValue.toShort

  @inline def intValue(): scala.Int =
    doubleValue.toInt

  @inline def longValue(): scala.Long =
    doubleValue.toLong

  @inline def floatValue(): scala.Float =
    doubleValue.toFloat

  @inline override def equals(that: Any): scala.Boolean =
    that match {
      case that: Double =>
        val a = doubleValue
        val b = that.doubleValue
        (a == b) || (Double.isNaN(a) && Double.isNaN(b))

      case _ =>
        false
    }

  @inline override def hashCode(): Int =
    Double.hashCode(doubleValue)

  @inline override def compareTo(that: Double): Int =
    Double.compare(doubleValue, that.doubleValue)

  @inline override def toString(): String =
    Double.toString(doubleValue)

  @inline def isNaN(): scala.Boolean =
    Double.isNaN(doubleValue)

  @inline def isInfinite(): scala.Boolean =
    Double.isInfinite(doubleValue)
}

object Double {
  final val BYTES             = 8
  final val MAX_EXPONENT      = 1023
  final val MAX_VALUE         = 1.79769313486231570E+308
  final val MIN_EXPONENT      = -1022
  final val MIN_NORMAL        = 2.2250738585072014E-308
  final val MIN_VALUE         = 5E-324
  final val NaN               = 0.0 / 0.0
  final val NEGATIVE_INFINITY = 1.0 / -0.0
  final val POSITIVE_INFINITY = 1.0 / 0.0
  final val SIZE              = 64
  final val TYPE              = classOf[scala.Double]

  @inline def compare(x: scala.Double, y: scala.Double): scala.Int =
    if (x > y) 1
    else if (x < y) -1
    else if (x == y && 0.0d != x) 0
    else {
      if (isNaN(x)) {
        if (isNaN(y)) 0
        else 1
      } else if (isNaN(y)) {
        -1
      } else {
        val d1 = doubleToRawLongBits(x)
        val d2 = doubleToRawLongBits(y)
        ((d1 >> 63) - (d2 >> 63)).toInt
      }
    }

  @inline def doubleToLongBits(value: scala.Double): scala.Long =
    if (value != value) 0x7ff8000000000000L
    else doubleToRawLongBits(value)

  @inline def doubleToRawLongBits(value: scala.Double): scala.Long =
    value.cast[scala.Long]

  @inline def hashCode(value: scala.Double): scala.Int = {
    val v = doubleToLongBits(value)
    (v ^ (v >>> 32)).toInt
  }

  @inline def isFinite(d: scala.Double): scala.Boolean =
    !isInfinite(d)

  @inline def isInfinite(v: scala.Double): scala.Boolean =
    v == POSITIVE_INFINITY || v == NEGATIVE_INFINITY

  @inline def isNaN(v: scala.Double): scala.Boolean =
    v != v

  @inline def longBitsToDouble(value: scala.Long): scala.Double =
    value.cast[scala.Double]

  @inline def max(a: scala.Double, b: scala.Double): scala.Double =
    Math.max(a, b)

  @inline def min(a: scala.Double, b: scala.Double): scala.Double =
    Math.min(a, b)

  def parseDouble(s: String): scala.Double = {
    val cstr = toCString(s)
    val end  = stackalloc[CString]

    errno.errno = 0
    val res = stdlib.strtod(cstr, end)

    if (errno.errno == 0) res
    else throw new NumberFormatException(s)
  }

  @inline def sum(a: scala.Double, b: scala.Double): scala.Double =
    a + b

  def toHexString(d: scala.Double): String = {
    if (d != d) {
      "NaN"
    } else if (d == POSITIVE_INFINITY) {
      "Infinity"
    } else if (d == NEGATIVE_INFINITY) {
      "-Infinity"
    } else {
      val bitValue    = doubleToLongBits(d)
      val negative    = (bitValue & 0x8000000000000000L) != 0
      val exponent    = (bitValue & 0x7FF0000000000000L) >>> 52
      var significand = bitValue & 0x000FFFFFFFFFFFFFL
      if (exponent == 0 && significand == 0) {
        if (negative) "-0x0.0p0"
        else "0x0.0p0"
      } else {
        val hexString = new java.lang.StringBuilder(24)

        if (negative) {
          hexString.append("-0x")
        } else {
          hexString.append("0x")
        }

        if (exponent == 0) {
          hexString.append("0.")
          var fractionDigits = 13
          while ((significand != 0) && ((significand & 0xF) == 0)) {
            significand >>>= 4
            fractionDigits -= 1
          }

          val hexSignificand = java.lang.Long.toHexString(significand)
          if (significand != 0 && fractionDigits > hexSignificand.length) {
            var digitDiff = fractionDigits - hexSignificand.length - 1
            while (digitDiff != 0) {
              hexString.append('0')
              digitDiff -= 1
            }
          }

          hexString.append(hexSignificand)
          hexString.append("p-1022")
        } else {
          hexString.append("1.")
          var fractionDigits = 13
          while ((significand != 0) && ((significand & 0xF) == 0)) {
            significand >>>= 4
            fractionDigits -= 1
          }

          val hexSignificand = java.lang.Long.toHexString(significand)
          if (significand != 0 && fractionDigits > hexSignificand.length) {
            var digitDiff = fractionDigits - hexSignificand.length - 1
            while (digitDiff != 0) {
              hexString.append('0')
              digitDiff -= 1
            }
          }

          hexString.append(hexSignificand)
          hexString.append('p')
          hexString.append(Long.toString(exponent - 1023))
        }

        hexString.toString
      }
    }
  }

  @inline def toString(d: scala.Double): String = {
    if (isNaN(d)) {
      "NaN"
    } else if (d == POSITIVE_INFINITY) {
      "Infinity"
    } else if (d == NEGATIVE_INFINITY) {
      "-Infinity"
    } else {
      val cstr = stackalloc[CChar](32)
      stdio.snprintf(cstr, 32, c"%f", d)
      fromCString(cstr)
    }
  }

  @inline def valueOf(d: scala.Double): Double =
    new Double(d)

  @inline def valueOf(s: String): Double =
    valueOf(parseDouble(s))
}
