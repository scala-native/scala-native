package java.lang

import scalanative.runtime.{select, divULong, remULong, undefined, Intrinsics}

final class Long(val _value: scala.Long) extends Number with Comparable[Long] {
  @inline def this(s: String) =
    this(Long.parseLong(s))

  @inline override def byteValue(): scala.Byte =
    _value.toByte

  @inline override def shortValue(): scala.Short =
    _value.toShort

  @inline override def intValue(): scala.Int =
    _value.toInt

  @inline override def longValue(): scala.Long =
    _value

  @inline override def floatValue(): scala.Float =
    _value.toFloat

  @inline override def doubleValue(): scala.Double =
    _value.toDouble

  @inline override def equals(that: Any): scala.Boolean =
    that match {
      case that: Long =>
        _value == that._value
      case _ =>
        false
    }

  @inline override def hashCode(): Int =
    Long.hashCode(_value)

  @inline override def compareTo(that: Long): Int =
    Long.compare(_value, that._value)

  @inline override def toString(): String =
    Long.toString(_value)

  @inline override def __scala_==(other: _Object): scala.Boolean =
    other match {
      case other: java.lang.Long      => _value == other._value
      case other: java.lang.Byte      => _value == other._value
      case other: java.lang.Short     => _value == other._value
      case other: java.lang.Integer   => _value == other._value
      case other: java.lang.Float     => _value == other._value
      case other: java.lang.Double    => _value == other._value
      case other: java.lang.Character => _value == other._value
      case _                          => super.__scala_==(other)
    }

  @inline override def __scala_## : scala.Int = {
    val lv = _value
    val iv = _value.toInt
    if (iv == lv) iv
    else Long.hashCode(lv)
  }

  /*
   * Ported from ScalaJS
   *
   * Methods on scala.Long
   * The following methods are only here to properly support reflective calls
   * on boxed primitive values. YOU WILL NOT BE ABLE TO USE THESE METHODS, since
   * we use the true javalib to lookup symbols, this file contains only
   * implementations.
   */
  protected def toByte: scala.Byte     = _value.toByte
  protected def toShort: scala.Short   = _value.toShort
  protected def toChar: scala.Char     = _value.toChar
  protected def toInt: scala.Int       = _value.toInt
  protected def toLong: scala.Long     = _value
  protected def toFloat: scala.Float   = _value.toFloat
  protected def toDouble: scala.Double = _value.toDouble

  protected def unary_~ : scala.Long = ~ _value
  protected def unary_+ : scala.Long = _value
  protected def unary_- : scala.Long = - _value

  protected def +(x: String): String = _value + x

  protected def <<(x: scala.Int): scala.Long   = _value << x
  protected def <<(x: scala.Long): scala.Long  = _value << x
  protected def >>>(x: scala.Int): scala.Long  = _value >>> x
  protected def >>>(x: scala.Long): scala.Long = _value >>> x
  protected def >>(x: scala.Int): scala.Long   = _value >> x
  protected def >>(x: scala.Long): scala.Long  = _value >> x

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

  protected def |(x: scala.Byte): scala.Long  = _value | x
  protected def |(x: scala.Short): scala.Long = _value | x
  protected def |(x: scala.Char): scala.Long  = _value | x
  protected def |(x: scala.Int): scala.Long   = _value | x
  protected def |(x: scala.Long): scala.Long  = _value | x

  protected def &(x: scala.Byte): scala.Long  = _value & x
  protected def &(x: scala.Short): scala.Long = _value & x
  protected def &(x: scala.Char): scala.Long  = _value & x
  protected def &(x: scala.Int): scala.Long   = _value & x
  protected def &(x: scala.Long): scala.Long  = _value & x

  protected def ^(x: scala.Byte): scala.Long  = _value ^ x
  protected def ^(x: scala.Short): scala.Long = _value ^ x
  protected def ^(x: scala.Char): scala.Long  = _value ^ x
  protected def ^(x: scala.Int): scala.Long   = _value ^ x
  protected def ^(x: scala.Long): scala.Long  = _value ^ x

  protected def +(x: scala.Byte): scala.Long     = _value + x
  protected def +(x: scala.Short): scala.Long    = _value + x
  protected def +(x: scala.Char): scala.Long     = _value + x
  protected def +(x: scala.Int): scala.Long      = _value + x
  protected def +(x: scala.Long): scala.Long     = _value + x
  protected def +(x: scala.Float): scala.Float   = _value + x
  protected def +(x: scala.Double): scala.Double = _value + x

  protected def -(x: scala.Byte): scala.Long     = _value - x
  protected def -(x: scala.Short): scala.Long    = _value - x
  protected def -(x: scala.Char): scala.Long     = _value - x
  protected def -(x: scala.Int): scala.Long      = _value - x
  protected def -(x: scala.Long): scala.Long     = _value - x
  protected def -(x: scala.Float): scala.Float   = _value - x
  protected def -(x: scala.Double): scala.Double = _value - x

  protected def *(x: scala.Byte): scala.Long     = _value - x
  protected def *(x: scala.Short): scala.Long    = _value - x
  protected def *(x: scala.Char): scala.Long     = _value - x
  protected def *(x: scala.Int): scala.Long      = _value - x
  protected def *(x: scala.Long): scala.Long     = _value - x
  protected def *(x: scala.Float): scala.Float   = _value - x
  protected def *(x: scala.Double): scala.Double = _value - x

  protected def /(x: scala.Byte): scala.Long     = _value / x
  protected def /(x: scala.Short): scala.Long    = _value / x
  protected def /(x: scala.Char): scala.Long     = _value / x
  protected def /(x: scala.Int): scala.Long      = _value / x
  protected def /(x: scala.Long): scala.Long     = _value / x
  protected def /(x: scala.Float): scala.Float   = _value / x
  protected def /(x: scala.Double): scala.Double = _value / x

  protected def %(x: scala.Byte): scala.Long     = _value % x
  protected def %(x: scala.Short): scala.Long    = _value % x
  protected def %(x: scala.Char): scala.Long     = _value % x
  protected def %(x: scala.Int): scala.Long      = _value % x
  protected def %(x: scala.Long): scala.Long     = _value % x
  protected def %(x: scala.Float): scala.Float   = _value % x
  protected def %(x: scala.Double): scala.Double = _value % x
}

object Long {
  final val TYPE      = classOf[scala.Long]
  final val MIN_VALUE = -9223372036854775808L
  final val MAX_VALUE = 9223372036854775807L
  final val SIZE      = 64
  final val BYTES     = 8

  @inline def bitCount(l: scala.Long): scala.Int =
    Intrinsics.`llvm.ctpop.i64`(l).toInt

  @inline def compare(x: scala.Long, y: scala.Long): scala.Int =
    if (x == y) 0
    else if (x < y) -1
    else 1

  @inline def compareUnsigned(x: scala.Long, y: scala.Long): scala.Int =
    compare(x ^ scala.Long.MinValue, y ^ scala.Long.MinValue)

  def decode(nm: String): Long = {
    val length = nm.length()
    if (length == 0) {
      throw new NumberFormatException()
    } else {
      var i          = 0
      var firstDigit = nm.charAt(i)
      val negative   = firstDigit == '-'

      if (negative) {
        if (length == 1) {
          throw new NumberFormatException(nm)
        }

        i += 1
        firstDigit = nm.charAt(i)
      }

      var base = 10
      if (firstDigit == '0') {
        i += 1
        if (i == length) {
          return valueOf(0L)
        }

        firstDigit = nm.charAt(i)
        if (firstDigit == 'x' || firstDigit == 'X') {
          if (i == length) {
            throw new NumberFormatException(nm)
          }
          i += 1
          base = 16
        } else {
          base = 8
        }
      } else if (firstDigit == '#') {
        if (i == length) {
          throw new NumberFormatException(nm)
        }
        i += 1
        base = 16
      }

      valueOf(parse(nm, i, base, negative))
    }
  }

  @inline
  def divideUnsigned(dividend: scala.Long, divisor: scala.Long): scala.Long =
    divULong(dividend, divisor)

  @inline def getLong(nm: String): Long =
    getLong(nm, null)

  @inline def getLong(nm: String, v: scala.Long): Long = {
    val result = getLong(nm, null)
    if (result == null) new Long(v)
    else result
  }

  def getLong(nm: String, v: Long): Long =
    if (nm == null || nm.length() == 0) {
      v
    } else {
      val prop = System.getProperty(nm)
      if (prop == null) {
        v
      } else {
        try {
          decode(prop)
        } catch {
          case e: NumberFormatException =>
            v
        }
      }
    }

  @inline def hashCode(value: scala.Long): Int =
    value.toInt ^ (value >>> 32).toInt

  @inline def highestOneBit(i: scala.Long): scala.Long =
    ((1L << 63) >>> numberOfLeadingZeros(i)) & i

  @inline def lowestOneBit(i: scala.Long): scala.Long =
    i & (-i)

  @inline def max(a: scala.Long, b: scala.Long): scala.Long =
    Math.max(a, b)

  @inline def min(a: scala.Long, b: scala.Long): scala.Long =
    Math.min(a, b)

  @inline def numberOfLeadingZeros(l: scala.Long): Int =
    Intrinsics.`llvm.ctlz.i64`(l, iszeroundef = false).toInt

  @inline def numberOfTrailingZeros(l: scala.Long): Int =
    Intrinsics.`llvm.cttz.i64`(l, iszeroundef = false).toInt

  @inline def parseLong(s: String): scala.Long =
    parseLong(s, 10)

  @inline def parseLong(s: String, radix: Int): scala.Long = {
    if (s == null || radix < Character.MIN_RADIX ||
        radix > Character.MAX_RADIX) throw new NumberFormatException(s)

    val length = s.length()

    if (length == 0) throw new NumberFormatException(s)

    val negative    = s.charAt(0) == '-'
    val hasPlusSign = s.charAt(0) == '+'

    if ((negative || hasPlusSign) && length == 1)
      throw new NumberFormatException(s)

    val offset = if (negative || hasPlusSign) 1 else 0

    parse(s, offset, radix, negative)
  }

  private def parse(s: String,
                    _offset: Int,
                    radix: Int,
                    negative: scala.Boolean): scala.Long = {
    val max    = MIN_VALUE / radix
    var result = 0L
    var offset = _offset
    val length = s.length()
    while (offset < length) {
      val digit = Character.digit(s.charAt(offset), radix)
      offset += 1
      if (digit == -1) throw new NumberFormatException(s)
      if (max > result) throw new NumberFormatException(s)
      val next = result * radix - digit
      if (next > result) throw new NumberFormatException(s)
      result = next
    }

    if (!negative) {
      result = -result
      if (result < 0) {
        throw new NumberFormatException(s)
      }
    }

    result
  }

  @inline
  def remainderUnsigned(dividend: scala.Long,
                        divisor: scala.Long): scala.Long =
    remULong(dividend, divisor)

  @inline def reverse(l: scala.Long): scala.Long =
    Intrinsics.`llvm.bitreverse.i64`(l)

  @inline def reverseBytes(l: scala.Long): scala.Long =
    Intrinsics.`llvm.bswap.i64`(l)

  @inline def rotateLeft(i: scala.Long, distance: scala.Int): scala.Long =
    (i << distance) | (i >>> -distance)

  @inline def rotateRight(i: scala.Long, distance: scala.Int): scala.Long =
    (i >>> distance) | (i << -distance)

  @inline def signum(i: scala.Long): scala.Int =
    if (i == 0) 0
    else if (i < 0) -1
    else 1

  @inline def sum(a: scala.Long, b: scala.Long): scala.Long =
    a + b

  def toBinaryString(l: scala.Long): String = {
    var count =
      if (l == 0L) 1
      else 64 - numberOfLeadingZeros(l)
    val buffer = new Array[Char](count)
    var k      = l
    do {
      count -= 1
      buffer(count) = ((k & 1) + '0').toChar
      k >>= 1
    } while (count > 0)

    new String(buffer)
  }

  def toHexString(l: scala.Long): String = {
    var count =
      if (l == 0L) 1
      else ((64 - numberOfLeadingZeros(l)) + 3) / 4
    val buffer = new Array[Char](count)
    var k      = l
    do {
      var t = (k & 15).toInt
      if (t > 9) {
        t = t - 10 + 'a'
      } else {
        t += '0'
      }
      count -= 1
      buffer(count) = t.toChar
      k >>= 4
    } while (count > 0)

    new String(buffer)
  }

  def toOctalString(l: scala.Long): String = {
    var count =
      if (l == 0L) 1
      else ((64 - numberOfLeadingZeros(l)) + 2) / 3
    val buffer = new Array[Char](count)
    var k      = l
    do {
      count -= 1
      buffer(count) = ((k & 7) + '0').toChar
      k >>>= 3
    } while (count > 0)

    new String(buffer)
  }

  @inline def toString(l: scala.Long): String =
    toString(l, 10)

  def toString(_l: scala.Long, _radix: Int): String = {
    if (_l == 0) {
      "0"
    } else {
      val radix =
        if (_radix < Character.MIN_RADIX || _radix > Character.MAX_RADIX) 10
        else _radix
      val negative = _l < 0
      var count    = 2
      var j        = _l
      if (!negative) {
        count = 1
        j = - _l
      }

      var l = _l
      l /= radix
      while (l != 0) {
        count += 1
        l = l / radix
      }

      val buffer = new Array[Char](count)
      do {
        var ch = 0 - (j % radix)
        if (ch > 9) {
          ch = ch - 10 + 'a'
        } else {
          ch += '0'
        }
        count -= 1
        buffer(count) = ch.toChar
        j = j / radix
      } while (j != 0)

      if (negative) {
        buffer(0) = '-'
      }

      new String(buffer)
    }
  }

  @inline def valueOf(longValue: scala.Long): Long =
    new Long(longValue)

  @inline def valueOf(s: String): Long =
    valueOf(parseLong(s))

  @inline def valueOf(s: String, radix: Int): Long =
    valueOf(parseLong(s, radix))

  @inline def parseUnsignedLong(s: String): scala.Long =
    parseUnsignedLong(s, 10)

  def parseUnsignedLong(s: String, radix: Int): scala.Long = {
    if (s == null || radix < Character.MIN_RADIX ||
        radix > Character.MAX_RADIX) throw new NumberFormatException(s)

    val len = s.length()

    if (len == 0) throw new NumberFormatException(s)

    val hasPlusSign = s.charAt(0) == '+'

    if (hasPlusSign && len == 1) throw new NumberFormatException(s)

    val offset = if (hasPlusSign) 1 else 0

    parseUnsigned(s, offset, radix)
  }

  private def parseUnsigned(s: String, _offset: Int, radix: Int): scala.Long = {
    val unsignedLongMaxValue = -1L
    val max                  = divideUnsigned(unsignedLongMaxValue, radix)
    var result               = 0L
    var offset               = _offset
    val length               = s.length()

    while (offset < length) {
      val digit = Character.digit(s.charAt(offset), radix)
      offset += 1

      if (digit == -1) throw new NumberFormatException(s)

      if (compareUnsigned(result, max) > 0) throw new NumberFormatException(s)

      result = result * radix + digit

      if (compareUnsigned(digit, result) > 0)
        throw new NumberFormatException(s)
    }

    result
  }

  @inline def toUnsignedString(l: scala.Long): String = toUnsignedString(l, 10)

  def toUnsignedString(_l: scala.Long, _radix: Int): String = {
    if (_l == 0L) {
      "0"
    } else {

      val radix =
        if (_radix < Character.MIN_RADIX || _radix > Character.MAX_RADIX) {
          10
        } else _radix

      var j = _l
      var l = _l

      // calculate string size
      var count = 1
      l = divideUnsigned(l, radix)
      while (l != 0L) {
        count += 1
        l = divideUnsigned(l, radix)
      }

      // populate string with characters
      val buffer = new Array[Char](count)
      do {
        val digit = remainderUnsigned(j, radix)
        val ch    = Character.forDigit(digit.toInt, radix)
        count -= 1
        buffer(count) = ch
        j = divideUnsigned(j, radix)
      } while (j != 0)

      new String(buffer)
    }
  }

}
