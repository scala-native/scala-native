package java.lang

import scalanative.native, native._

final class Float(val _value: scala.Float)
    extends Number
    with Comparable[Float] {
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

  @inline override def __scala_==(other: _Object): scala.Boolean =
    other match {
      case other: java.lang.Float     => _value == other._value
      case other: java.lang.Byte      => _value == other._value
      case other: java.lang.Short     => _value == other._value
      case other: java.lang.Integer   => _value == other._value
      case other: java.lang.Long      => _value == other._value
      case other: java.lang.Double    => _value == other._value
      case other: java.lang.Character => _value == other._value
      case _                          => super.__scala_==(other)
    }

  @inline override def __scala_## : scala.Int = {
    val fv = _value
    val iv = _value.toInt
    if (iv == fv) iv
    else {
      val lv = _value.toLong
      if (lv == fv) Long.hashCode(lv)
      else Float.hashCode(fv)
    }
  }

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
  protected def toByte: scala.Byte     = _value.toByte
  protected def toShort: scala.Short   = _value.toShort
  protected def toChar: scala.Char     = _value.toChar
  protected def toInt: scala.Int       = _value.toInt
  protected def toLong: scala.Long     = _value.toLong
  protected def toFloat: scala.Float   = _value
  protected def toDouble: scala.Double = _value.toDouble

  protected def unary_+ : scala.Float = _value
  protected def unary_- : scala.Float = - _value

  protected def +(x: String): String = _value + x

  protected def <(x: scala.Byte): scala.Boolean   = _value < x
  protected def <(x: scala.Short): scala.Boolean  = _value < x
  protected def <(x: scala.Char): scala.Boolean   = _value < x
  protected def <(x: scala.Int): scala.Boolean    = _value < x
  protected def <(x: scala.Long): scala.Boolean   = _value < x
  protected def <(x: scala.Float): scala.Boolean  = _value < x
  protected def <(x: scala.Double): scala.Boolean = _value < x

  protected def <=(x: scala.Byte): scala.Boolean   = _value <= x
  protected def <=(x: scala.Short): scala.Boolean  = _value <= x
  protected def <=(x: scala.Char): scala.Boolean   = _value <= x
  protected def <=(x: scala.Int): scala.Boolean    = _value <= x
  protected def <=(x: scala.Long): scala.Boolean   = _value <= x
  protected def <=(x: scala.Float): scala.Boolean  = _value <= x
  protected def <=(x: scala.Double): scala.Boolean = _value <= x

  protected def >(x: scala.Byte): scala.Boolean   = _value > x
  protected def >(x: scala.Short): scala.Boolean  = _value > x
  protected def >(x: scala.Char): scala.Boolean   = _value > x
  protected def >(x: scala.Int): scala.Boolean    = _value > x
  protected def >(x: scala.Long): scala.Boolean   = _value > x
  protected def >(x: scala.Float): scala.Boolean  = _value > x
  protected def >(x: scala.Double): scala.Boolean = _value > x

  protected def >=(x: scala.Byte): scala.Boolean   = _value >= x
  protected def >=(x: scala.Short): scala.Boolean  = _value >= x
  protected def >=(x: scala.Char): scala.Boolean   = _value >= x
  protected def >=(x: scala.Int): scala.Boolean    = _value >= x
  protected def >=(x: scala.Long): scala.Boolean   = _value >= x
  protected def >=(x: scala.Float): scala.Boolean  = _value >= x
  protected def >=(x: scala.Double): scala.Boolean = _value >= x

  protected def +(x: scala.Byte): scala.Float    = _value + x
  protected def +(x: scala.Short): scala.Float   = _value + x
  protected def +(x: scala.Char): scala.Float    = _value + x
  protected def +(x: scala.Int): scala.Float     = _value + x
  protected def +(x: scala.Long): scala.Float    = _value + x
  protected def +(x: scala.Float): scala.Float   = _value + x
  protected def +(x: scala.Double): scala.Double = _value + x

  protected def -(x: scala.Byte): scala.Float    = _value - x
  protected def -(x: scala.Short): scala.Float   = _value - x
  protected def -(x: scala.Char): scala.Float    = _value - x
  protected def -(x: scala.Int): scala.Float     = _value - x
  protected def -(x: scala.Long): scala.Float    = _value - x
  protected def -(x: scala.Float): scala.Float   = _value - x
  protected def -(x: scala.Double): scala.Double = _value - x

  protected def *(x: scala.Byte): scala.Float    = _value * x
  protected def *(x: scala.Short): scala.Float   = _value * x
  protected def *(x: scala.Char): scala.Float    = _value * x
  protected def *(x: scala.Int): scala.Float     = _value * x
  protected def *(x: scala.Long): scala.Float    = _value * x
  protected def *(x: scala.Float): scala.Float   = _value * x
  protected def *(x: scala.Double): scala.Double = _value * x

  protected def /(x: scala.Byte): scala.Float    = _value / x
  protected def /(x: scala.Short): scala.Float   = _value / x
  protected def /(x: scala.Char): scala.Float    = _value / x
  protected def /(x: scala.Int): scala.Float     = _value / x
  protected def /(x: scala.Long): scala.Float    = _value / x
  protected def /(x: scala.Float): scala.Float   = _value / x
  protected def /(x: scala.Double): scala.Double = _value / x

  protected def %(x: scala.Byte): scala.Float    = _value % x
  protected def %(x: scala.Short): scala.Float   = _value % x
  protected def %(x: scala.Char): scala.Float    = _value % x
  protected def %(x: scala.Int): scala.Float     = _value % x
  protected def %(x: scala.Long): scala.Float    = _value % x
  protected def %(x: scala.Float): scala.Float   = _value % x
  protected def %(x: scala.Double): scala.Double = _value % x
}

object Float {
  final val BYTES             = 4
  final val MAX_EXPONENT      = 127
  final val MAX_VALUE         = 3.40282346638528860e+38f
  final val MIN_EXPONENT      = -126
  final val MIN_NORMAL        = 1.17549435E-38f
  final val MIN_VALUE         = 1.40129846432481707e-45f
  final val NaN               = 0.0f / 0.0f
  final val NEGATIVE_INFINITY = 1.0f / -0.0f
  final val POSITIVE_INFINITY = 1.0f / 0.0f
  final val SIZE              = 32
  final val TYPE              = classOf[scala.Float]

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
    value.cast[scala.Int]

  @inline def hashCode(value: scala.Float): scala.Int =
    floatToIntBits(value)

  @inline def intBitsToFloat(value: scala.Int): scala.Float =
    value.cast[scala.Float]

  @inline def isFinite(f: scala.Float): scala.Boolean =
    !isInfinite(f)

  @inline def isInfinite(v: scala.Float): scala.Boolean =
    v == POSITIVE_INFINITY || v == NEGATIVE_INFINITY

  @inline def isNaN(v: scala.Float): scala.Boolean =
    v != v

  @inline def max(a: scala.Float, b: scala.Float): scala.Float =
    Math.max(a, b)

  @inline def min(a: scala.Float, b: scala.Float): scala.Float =
    Math.min(a, b)

  def parseFloat(s: String): scala.Float = {
    val cstr = toCString(s)
    val end  = stackalloc[CString]

    errno.errno = 0
    val res = stdlib.strtof(cstr, end)

    if (errno.errno == 0) res
    else throw new NumberFormatException(s)
  }

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
      val bitValue    = floatToIntBits(f)
      val negative    = (bitValue & 0x80000000) != 0
      val exponent    = (bitValue & 0x7f800000) >>> 23
      var significand = (bitValue & 0x007FFFFF) << 1

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
          while ((significand != 0) && ((significand & 0xF) == 0)) {
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
          while ((significand != 0) && ((significand & 0xF) == 0)) {
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
    if (isNaN(f)) {
      "NaN"
    } else if (f == POSITIVE_INFINITY) {
      "Infinity"
    } else if (f == NEGATIVE_INFINITY) {
      "-Infinity"
    } else {
      val cstr = stackalloc[CChar](32)
      stdio.snprintf(cstr, 32, c"%f", f.toDouble)
      fromCString(cstr)
    }
  }

  @inline def valueOf(s: String): Float =
    valueOf(parseFloat(s))

  @inline def valueOf(f: scala.Float): Float =
    new Float(f)
}
