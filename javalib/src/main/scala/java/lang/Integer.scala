package java.lang

import scalanative.runtime.{select, divUInt, remUInt, intToULong, Intrinsics}

final class Integer(override val intValue: scala.Int)
    extends Number
    with Comparable[Integer] {
  @inline def this(s: String) =
    this(Integer.parseInt(s))

  @inline override def byteValue(): scala.Byte =
    intValue.toByte

  @inline override def shortValue(): scala.Short =
    intValue.toShort

  @inline def longValue(): scala.Long =
    intValue.toLong

  @inline def floatValue(): scala.Float =
    intValue.toFloat

  @inline def doubleValue(): scala.Double =
    intValue.toDouble

  @inline override def equals(that: Any): scala.Boolean =
    that match {
      case that: Integer =>
        intValue == that.intValue
      case _ =>
        false
    }

  @inline override def hashCode(): scala.Int =
    Integer.hashCode(intValue)

  @inline override def compareTo(that: Integer): scala.Int =
    Integer.compare(intValue, that.intValue)

  @inline override def toString(): String =
    Integer.toString(intValue)
}

object Integer {
  final val TYPE      = classOf[scala.Int]
  final val MIN_VALUE = -2147483648
  final val MAX_VALUE = 2147483647
  final val SIZE      = 32
  final val BYTES     = 4

  private final val decimalScale: Array[scala.Int] = Array(1000000000,
                                                           100000000,
                                                           10000000,
                                                           1000000,
                                                           100000,
                                                           10000,
                                                           1000,
                                                           100,
                                                           10,
                                                           1)
  private final val digits = Array('0',
                                   '1',
                                   '2',
                                   '3',
                                   '4',
                                   '5',
                                   '6',
                                   '7',
                                   '8',
                                   '9',
                                   'a',
                                   'b',
                                   'c',
                                   'd',
                                   'e',
                                   'f',
                                   'g',
                                   'h',
                                   'i',
                                   'j',
                                   'k',
                                   'l',
                                   'm',
                                   'n',
                                   'o',
                                   'p',
                                   'q',
                                   'r',
                                   's',
                                   't',
                                   'u',
                                   'v',
                                   'w',
                                   'x',
                                   'y',
                                   'z')

  @inline def bitCount(i: scala.Int): scala.Int =
    Intrinsics.`llvm.ctpop.i32`(i)

  @inline def byteValue(i: scala.Int): scala.Byte =
    i.toByte

  @inline def compare(x: scala.Int, y: scala.Int): scala.Int =
    if (x == y) 0 else if (x < y) -1 else 1

  @inline def compareUnsigned(x: scala.Int, y: scala.Int): scala.Int =
    compare(x ^ scala.Int.MinValue, y ^ scala.Int.MinValue)

  def decode(nm: String): Integer = {
    val length = nm.length()
    if (length == 0) throw new NumberFormatException()

    var i        = 0
    var first    = nm.charAt(i)
    val negative = first == '-'
    if (negative) {
      if (length == 1) throw new NumberFormatException(nm)
      i += 1
      first = nm.charAt(i)
    }

    var base = 10
    if (first == '0') {
      i += 1
      if (i == length) return valueOf(0)
      first = nm.charAt(i)
      if (first == 'x' || first == 'X') {
        i += 1
        if (i == length) throw new NumberFormatException(nm)
        base = 16
      } else {
        base = 8
      }
    } else if (first == '#') {
      i += 1
      if (i == length) throw new NumberFormatException(nm)
      base = 16
    }

    valueOf(parse(nm, i, base, negative))
  }

  @inline
  def divideUnsigned(dividend: scala.Int, divisor: scala.Int): scala.Int =
    divUInt(dividend, divisor)

  @inline def getInteger(nm: String): Integer =
    getInteger(nm, null)

  @inline def getInteger(nm: String, v: scala.Int): Integer = {
    val result = getInteger(nm, null)
    if (result == null) new Integer(v)
    else result
  }

  def getInteger(nm: String, v: Integer): Integer =
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

  @inline def hashCode(value: scala.Int): scala.Int =
    value

  @inline def highestOneBit(i: scala.Int): scala.Int =
    ((1 << 31) >>> numberOfLeadingZeros(i)) & i

  @inline def lowestOneBit(i: scala.Int): scala.Int =
    i & -i

  @inline def max(a: scala.Int, b: scala.Int): scala.Int =
    Math.max(a, b)

  @inline def min(a: scala.Int, b: scala.Int): scala.Int =
    Math.min(a, b)

  @inline def numberOfLeadingZeros(i: scala.Int): scala.Int =
    Intrinsics.`llvm.ctlz.i32`(i, iszeroundef = false)

  @inline def numberOfTrailingZeros(i: scala.Int): scala.Int =
    Intrinsics.`llvm.cttz.i32`(i, iszeroundef = false)

  @inline def parseInt(s: String): scala.Int =
    parseInt(s, 10)

  def parseInt(s: String, radix: scala.Int): scala.Int = {
    if (s == null || radix < Character.MIN_RADIX ||
        radix > Character.MAX_RADIX) {
      throw new NumberFormatException()
    }

    val length = s.length()
    if (length == 0) {
      throw new NumberFormatException(s)
    }

    val positive = s.charAt(0) == '+'
    val negative = s.charAt(0) == '-'
    val offset   = if (positive || negative) 1 else 0
    if (offset > 0 && length == 1) {
      throw new NumberFormatException(s)
    }

    parse(s, offset, radix, negative)
  }

  private def parse(s: String,
                    _offset: scala.Int,
                    radix: scala.Int,
                    negative: scala.Boolean): scala.Int = {
    val max    = MIN_VALUE / radix
    val length = s.length()
    var result = 0
    var offset = _offset

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
      if (result < 0) throw new NumberFormatException(s)
    }

    result
  }

  @inline
  def remainderUnsigned(dividend: scala.Int, divisor: scala.Int): scala.Int =
    remUInt(dividend, divisor)

  @inline def reverse(i: scala.Int): scala.Int =
    Intrinsics.`llvm.bitreverse.i32`(i)

  @inline def reverseBytes(i: scala.Int): scala.Int =
    Intrinsics.`llvm.bswap.i32`(i)

  @inline def rotateLeft(i: scala.Int, distance: scala.Int): scala.Int =
    (i << distance) | (i >>> -distance)

  @inline def rotateRight(i: scala.Int, distance: scala.Int): scala.Int =
    (i >>> distance) | (i << -distance)

  @inline def signum(i: scala.Int): scala.Int =
    if (i == 0) 0
    else if (i < 0) -1
    else 1

  @inline def sum(a: scala.Int, b: scala.Int): scala.Int =
    a + b

  def toBinaryString(i: scala.Int): String = {
    var count =
      if (i == 0) 1
      else 32 - numberOfLeadingZeros(i)
    val buffer = new Array[Char](count)
    var k      = i
    do {
      count -= 1
      buffer(count) = ((k & 1) + '0').toChar
      k >>>= 1
    } while (count > 0)

    new String(buffer)
  }

  def toHexString(i: scala.Int): String = {
    var count =
      if (i == 0) 1
      else ((32 - numberOfLeadingZeros(i)) + 3) / 4
    val buffer = new Array[Char](count)
    var k      = i
    do {
      var t = k & 15
      if (t > 9) {
        t = t - 10 + 'a'
      } else {
        t += '0'
      }
      count -= 1
      buffer(count) = t.toChar
      k >>>= 4
    } while (count > 0)

    new String(buffer)
  }

  def toOctalString(i: scala.Int): String = {
    var count =
      if (i == 0) 1
      else ((32 - numberOfLeadingZeros(i)) + 2) / 3
    val buffer = new Array[Char](count)
    var k      = i
    do {
      count -= 1
      buffer(count) = ((k & 7) + '0').toChar
      k >>>= 3
    } while (count > 0)

    new String(buffer)
  }

  def toString(i: scala.Int): String = {
    if (i == 0) {
      "0"
    } else {
      val negative = i < 0

      if (i < 1000 && i > -1000) {
        val buffer = new Array[Char](4)
        val positive_value =
          if (negative) -i
          else i
        var first_digit = 0
        if (negative) {
          buffer(0) = '-'
          first_digit += 1
        }

        var last_digit = first_digit
        var quot       = positive_value
        do {
          val res         = quot / 10
          var digit_value = quot - ((res << 3) + (res << 1))
          digit_value += '0'
          buffer(last_digit) = digit_value.toChar
          last_digit += 1
          quot = res
        } while (quot != 0)

        val count = last_digit
        last_digit -= 1
        do {
          val tmp = buffer(last_digit)
          buffer(last_digit) = buffer(first_digit)
          last_digit -= 1
          buffer(first_digit) = tmp
          first_digit += 1
        } while (first_digit < last_digit)

        new String(buffer, 0, count)
      } else if (i == MIN_VALUE) {
        "-2147483648"
      } else {
        val buffer = new Array[Char](11)
        var positive_value =
          if (i < 0) -i
          else i
        var first_digit = 0
        if (negative) {
          buffer(0) = '-'
          first_digit += 1
        }

        var last_digit  = first_digit
        var count       = 0
        var number: Int = 0
        var start       = false
        var k           = 0
        while (k < 9) {
          count = 0
          number = decimalScale(k)
          if (positive_value < number) {
            if (start) {
              buffer(last_digit) = '0'
              last_digit += 1
            }
          }

          if (k > 0) {
            number = decimalScale(k) << 3
            if (positive_value >= number) {
              positive_value -= number
              count += 8
            }

            number = decimalScale(k) << 2
            if (positive_value >= number) {
              positive_value -= number
              count += 4
            }
          }

          number = decimalScale(k) << 1
          if (positive_value >= number) {
            positive_value -= number
            count += 2
          }

          if (positive_value >= decimalScale(k)) {
            positive_value -= decimalScale(k)
            count += 1
          }

          if (count > 0 && !start) {
            start = true
          }

          if (start) {
            buffer(last_digit) = (count + '0').toChar
            last_digit += 1
          }

          k += 1
        }

        buffer(last_digit) = (positive_value + '0').toChar
        last_digit += 1
        count = last_digit
        last_digit -= 1

        new String(buffer, 0, count)
      }
    }
  }

  def toString(_i: scala.Int, _radix: scala.Int): String = {
    if (_i == 0) {
      "0"
    } else {
      val radix =
        if (_radix < Character.MIN_RADIX || _radix > Character.MAX_RADIX) 10
        else _radix
      var i        = _i
      var j        = _i
      var count    = 2
      val negative = _i < 0
      if (!negative) {
        count = 1
        j = -i
      }
      i /= radix
      while (i != 0) {
        count += 1
        i = i / radix
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
        j /= radix
      } while (j != 0)

      if (negative) {
        buffer(0) = '-'
      }

      new String(buffer)
    }
  }

  @inline def toUnsignedLong(x: scala.Int): scala.Long =
    intToULong(x)

  @inline def valueOf(i: scala.Int): Integer =
    new Integer(i)

  @inline def valueOf(s: String): Integer =
    valueOf(parseInt(s))

  @inline def valueOf(s: String, radix: scala.Int): Integer =
    valueOf(parseInt(s, radix))

  // TODO:
  // def parseUnsignedInt(s: String): scala.Int = parseUnsignedInt(s, 10)
  // def parseUnsignedInt(s: String, radix: scala.Int): scala.Int = ???
  // def toUnsignedString(i: scala.Int): String = toUnsignedString(i, 10)
  // def toUnsignedString(_i: scala.Int, _radix: scala.Int): String = ???
}
