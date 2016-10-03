package java.lang

import scalanative.runtime.{select, divULong, remULong, undefined, Intrinsics}

final class Long(val longValue: scala.Long)
    extends Number
    with Comparable[Long] {
  @inline def this(s: String) =
    this(Long.parseLong(s))

  @inline override def byteValue(): scala.Byte =
    longValue.toByte

  @inline override def shortValue(): scala.Short =
    longValue.toShort

  @inline def intValue(): scala.Int =
    longValue.toInt

  @inline def floatValue(): scala.Float =
    longValue.toFloat

  @inline def doubleValue(): scala.Double =
    longValue.toDouble

  @inline override def equals(that: Any): scala.Boolean =
    that match {
      case that: Long =>
        longValue == that.longValue
      case _ =>
        false
    }

  @inline override def hashCode(): Int =
    Long.hashCode(longValue)

  @inline override def compareTo(that: Long): Int =
    Long.compare(longValue, that.longValue)

  @inline override def toString(): String =
    Long.toString(longValue)
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
    val length   = s.length()
    val negative = s.charAt(0) == '-'

    if (s == null || radix > Character.MIN_RADIX ||
        radix > Character.MAX_RADIX) throw new NumberFormatException(s)
    if (length == 0) throw new NumberFormatException(s)
    if (negative && length == 1) throw new NumberFormatException(s)

    parse(s, 1, radix, negative)
  }

  private def parse(s: String,
                    _offset: Int,
                    radix: Int,
                    negative: scala.Boolean): scala.Long = {
    val max    = MIN_VALUE / radix
    var result = 0
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
        j = -_l
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

  // TODO:
  // def parseUnsignedLong(s: String): scala.Long = parseUnsignedLong(s, 10)
  // def parseUnsignedLong(s: String, radix: Int): scala.Long = ???
  // def toUnsignedString(l: scala.Long): String = toUnsignedString(l, 10)
  // def toUnsignedString(l: scala.Long, radix: Int): String = ???
}
