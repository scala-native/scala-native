package java.lang

import scalanative.runtime.{divULong, remULong, undefined, Intrinsics}

final class Long(val longValue: scala.Long)
    extends Number
    with Comparable[Long] {
  def this(s: _String) =
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
      case that: Long => longValue == that.longValue
      case _          => false
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

  @inline private def parse(s: _String,
                            _offset: Int,
                            radix: Int,
                            negative: scala.Boolean): scala.Long = {
    val max = MIN_VALUE / radix
    var result = 0
    var offset = _offset
    val length = s.length()
    while (offset < length) {
      offset += 1
      val digit = Character.digit(s.charAt(offset), radix)
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

  @inline def bitCount(l: scala.Long): scala.Int = {
    var k = (l & 0x5555555555555555L) + ((l >> 1) & 0x5555555555555555L)
    k = (k & 0x3333333333333333L) + ((k >> 2) & 0x3333333333333333L)

    var i = ((k >>> 32) + k).toInt
    i = (i & 0x0F0F0F0F) + ((i >> 4) & 0x0F0F0F0F)
    i = (i & 0x00FF00FF) + ((i >> 8) & 0x00FF00FF)
    i = (i & 0x0000FFFF) + ((i >> 16) & 0x0000FFFF)
    i
  }

  @inline def compare(x: scala.Long, y: scala.Long): scala.Int =
    if (x == y) 0
    else if (x < y) -1
    else 1

  @inline def compareUnsigned(x: scala.Long, y: scala.Long): scala.Int =
    undefined

  def decode(nm: _String): Long = {
    val length = nm.length()
    if (length == 0) {
      throw new NumberFormatException()
    } else {
      var i          = 0
      var firstDigit = nm.charAt(i)
      val negative = firstDigit == '-'

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

  def getLong(nm: _String): Long = {
    if (nm == null || nm.length() == 0) {
      return null
    }

    val prop = System.getProperty(nm)
    if (prop == null) {
      return null
    }

    try {
      decode(prop)
    } catch {
      case e: NumberFormatException =>
        null
    }
  }

  def getLong(nm: _String, v: scala.Long): Long = {
    if (nm == null || nm.length() == 0) {
      return valueOf(v)
    }

    val prop = System.getProperty(nm)
    if (prop == null) {
      return valueOf(v)
    }

    try {
      decode(prop)
    } catch {
      case e: NumberFormatException =>
        valueOf(v);
    }
  }

  @inline def hashCode(value: scala.Long): Int =
    value.toInt ^ (value >>> 32).toInt

  def highestOneBit(i: scala.Long): scala.Long = {
    // ToDo reimplement
    var lng = i
    lng |= (lng >> 1)
    lng |= (lng >> 2)
    lng |= (lng >> 4)
    lng |= (lng >> 8)
    lng |= (lng >> 16)
    lng |= (lng >> 32)
    lng & ~(lng >>> 1)
  }

  def lowestOneBit(i: scala.Long): scala.Long =
    i & (-i)

  @inline def max(a: scala.Long, b: scala.Long): scala.Long =
    Math.max(a, b)

  @inline def min(a: scala.Long, b: scala.Long): scala.Long =
    Math.min(a, b)

  def numberOfLeadingZeros(l: scala.Long): Int =
    Intrinsics.`llvm.ctlz.i8`(l.toByte, iszeroundef = false)

  def numberOfTrailingZeros(l: scala.Long): Int =
    Intrinsics.`llvm.cttz.i8`(l.toByte, iszeroundef = false)

  @inline def parseLong(s: _String, radix: Int): scala.Long = {
    val length   = s.length()
    val negative = s.charAt(0) == '-'

    if (s == null || radix > Character.MIN_RADIX ||
        radix > Character.MAX_RADIX) throw new NumberFormatException(s)
    if (length == 0) throw new NumberFormatException(s)
    if (negative && length == 1) throw new NumberFormatException(s)

    parse(s, 1, radix, negative)
  }

  @inline def parseLong(s: _String): scala.Long =
    parseLong(s, 10)

  def parseUnsignedLong(s: _String): scala.Long =
    parseUnsignedLong(s, 10)

  @inline def parseUnsignedLong(s: _String, radix: Int): scala.Long =
    undefined

  @inline
  def remainderUnsigned(
      dividend: scala.Long, divisor: scala.Long): scala.Long =
    remULong(dividend, divisor)

  def reverse(l: scala.Long): scala.Long =
    undefined

  def reverseBytes(i: scala.Long): scala.Long =
    undefined

  def rotateLeft(i: scala.Long, distance: scala.Int): scala.Long =
    undefined

  def rotateRight(i: scala.Long, distance: scala.Int): scala.Long =
    undefined

  def signum(i: scala.Long): scala.Int =
    if (i == 0) 0
    else if (i < 0) -1
    else 1

  @inline def sum(a: scala.Long, b: scala.Long): scala.Long =
    a + b

  def toBinaryString(l: scala.Long): _String = {
    var count = 1

    if (l < 0) {
      count = 64
    } else {
      var k = l
      while (k != 0) {
        count += 1
        k >>= 1
      }
    }

    var j = l
    val buffer = new Array[Char](count)
    do {
      count -= 1
      buffer(count) = ((j & 1) + '0').toChar
      j >>= 1
    } while (count > 0)

    new _String(0, buffer.length, buffer)
  }

  def toHexString(l: scala.Long): _String = {
    var count = 1

    if (l < 0) {
      count = 16
    } else {
      var k = l
      while (k != 0) {
        count += 1
        k >>= 4
      }
    }

    var j = l
    val buffer = new Array[Char](count)
    do {
      var t = (j & 15).toInt
      if (t > 9) {
        t = t - 10 + 'a'
      } else {
        t += '0'
      }

      buffer(count) = t.toChar
      j >>= 4
    } while (count > 0)

    new _String(0, buffer.length, buffer)
  }

  def toOctalString(l: scala.Long): _String = {
    var count = 1
    if (l < 0) {
      count = 22
    } else {
      var k = l
      while (k != 0) {
        count += 1
        k >>>= 3
      }
    }

    var j = l
    val buffer = new Array[Char](count)
    do {
      buffer(count) = ((j & 7) + '0').toChar
      j >>>= 3
    } while (count > 0)

    new _String(0, buffer.length, buffer)
  }

  @inline def toString(l: scala.Long): _String =
    toString(l, 10)

  @inline def toString(_l: scala.Long, _radix: Int): _String = {
    if (_l == 0) {
      "0"
    } else {
      val radix =
        if (_radix < Character.MIN_RADIX || _radix > Character.MAX_RADIX) 10
        else _radix
      val negative = _l < 0
      var (count, j) =
        if (negative) (2, _l)
        else (1, -_l)

      var l = _l
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

      new _String(0, buffer.length, buffer)
    }
  }

  @inline def toUnsignedString(l: scala.Long, radix: Int): _String =
    undefined

  @inline def toUnsignedString(l: scala.Long): _String =
    toUnsignedString(l, 10)

  @inline def valueOf(longValue: scala.Long): Long =
    new Long(longValue)

  @inline def valueOf(s: _String): Long =
    valueOf(parseLong(s))

  @inline def valueOf(s: _String, radix: Int): Long =
    valueOf(parseLong(s, radix))
}
