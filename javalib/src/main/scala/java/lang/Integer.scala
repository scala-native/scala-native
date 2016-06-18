package java.lang

import scalanative.runtime.{divUInt, remUInt, undefined}

final class Integer(override val intValue: scala.Int)
    extends Number
    with Comparable[Integer] {
  def this(s: _String) =
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
        byteValue == that.intValue
      case _ =>
        false
    }

  @inline override def hashCode(): scala.Int =
    Integer.hashCode(intValue)

  @inline override def compareTo(that: Integer): scala.Int =
    Integer.compare(intValue, that.intValue)

  @inline override def toString(): String =
    Integer.toString(intValue)

  /* Methods of java.lang.Byte and java.lang.Short.
   * When calling a method of j.l.Byte or j.l.Short on a primitive value,
   * it appears to be called directly on the primitive value, which has type
   * IntType. Call resolution, by the analyzer and the optimizer, will then
   * look for the method in the class j.l.Integer instead of j.l.Byte or
   * j.l.Short. This is why we add here the methods of these two classes that
   * are not already in j.l.Integer.
   */

  @inline def compareTo(that: Byte): scala.Int =
    Integer.compare(intValue, that.intValue)

  @inline def compareTo(that: Short): scala.Int =
    Integer.compare(intValue, that.intValue)
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
  @inline
  private def parse(s: _String,
                    offset: scala.Int,
                    radix: scala.Int,
                    negative: scala.Boolean): scala.Int = {
    val max    = MIN_VALUE / radix
    val length = s.length()
    var result = 0
    var offset = 1
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
      if (result < 0) throw new NumberFormatException(s)
    }

    result
  }

  def bitCount(i: scala.Int): scala.Int = {
    /* See http://graphics.stanford.edu/~seander/bithacks.html#CountBitsSetParallel
     *
     * The original algorithm uses *logical* shift rights. Here we use
     * *arithmetic* shift rights instead. >> is shorter than >>>, especially
     * since the latter needs (a >>> b) | 0 in JS. It might also be the case
     * that >>> is a bit slower for that reason on some VMs.
     *
     * Using >> is valid because:
     * * For the 2 first >>, the possible sign bit extension is &'ed away
     * * For (t2 >> 4), t2 cannot be negative because it is at most the result
     *   of 2 * 0x33333333, which does not overflow and is positive.
     * * For the last >> 24, the left operand cannot be negative either.
     *   Assume it was, that means the result of a >>> would be >= 128, but
     *   the correct result must be <= 32. So by contradiction, it is positive.
     */
    val t1 = i - ((i >> 1) & 0x55555555)
    val t2 = (t1 & 0x33333333) + ((t1 >> 2) & 0x33333333)
    (((t2 + (t2 >> 4)) & 0xF0F0F0F) * 0x1010101) >> 24
  }

  @inline def compare(x: scala.Int, y: scala.Int): scala.Int =
    if (x == y) 0 else if (x < y) -1 else 1

  @inline def compareUnsigned(x: scala.Int, y: scala.Int): scala.Int =
    undefined

  @inline def decode(nm: _String): Integer = {
    val length = nm.length()
    if (length == 0) throw new NumberFormatException()

    var i     = 0
    var first = nm.charAt(i)
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

  @inline def getInteger(nm: _String): Integer =
    getInteger(nm, null)

  @inline def getInteger(nm: _String, v: scala.Int): Integer = {
    val result = getInteger(nm, null)
    if (result == null) new Integer(v)
    else result
  }

  @inline def getInteger(nm: _String, v: Integer): Integer =
    if (nm == null || nm.length() == 0) {
      valueOf(v)
    } else {
      val prop = System.getProperty(nm)
      if (prop == null) {
        valueOf(v)
      } else {
        try {
          decode(prop)
        } catch {
          case e: NumberFormatException =>
            valueOf(v)
        }
      }
    }

  @inline def hashCode(value: scala.Int): scala.Int =
    value

  @inline def highestOneBit(i: scala.Int): scala.Int =
    if (i == 0) 0
    else (1 << 31) >>> numberOfLeadingZeros(i)

  @inline def lowestOneBit(i: scala.Int): scala.Int =
    i & -i

  @inline def max(a: scala.Int, b: scala.Int): scala.Int =
    Math.max(a, b)

  @inline def min(a: scala.Int, b: scala.Int): scala.Int =
    Math.min(a, b)

  @inline def numberOfLeadingZeros(i: scala.Int): scala.Int =
    if (i == 0) {
      32
    } else {
      var x = i
      var r = 1
      if ((x & 0xffff0000) == 0) { x <<= 16; r += 16 }
      if ((x & 0xff000000) == 0) { x <<= 8; r += 8 }
      if ((x & 0xf0000000) == 0) { x <<= 4; r += 4 }
      if ((x & 0xc0000000) == 0) { x <<= 2; r += 2 }
      r + (x >> 31)
    }

  @inline def numberOfTrailingZeros(i: scala.Int): scala.Int =
    if (i == 0) 32
    else 31 - numberOfLeadingZeros(i & -i)

  @inline def parseInt(s: _String): scala.Int =
    parseInt(s, 10)

  @noinline def parseInt(s: _String, radix: scala.Int): scala.Int = {
    if (s == null || radix < Character.MIN_RADIX ||
        radix > Character.MAX_RADIX) throw new NumberFormatException(s)
    val length   = s.length()
    val negative = s.charAt(0) == '-'

    if (length == 0)
      throw new NumberFormatException(s)
    else if (negative && length == 1)
      throw new NumberFormatException(s)
    else
      parse(s, 1, radix, negative)
  }

  @inline def parseUnsignedInt(s: _String): scala.Int =
    parseUnsignedInt(s, 10)

  @noinline def parseUnsignedInt(s: _String, radix: scala.Int): scala.Int = {
    if (s == null || radix < Character.MIN_RADIX ||
        radix > Character.MAX_RADIX) throw new NumberFormatException(s)
    val length   = s.length()
    val negative = s.charAt(0) == '-'

    if (length == 0)
      throw new NumberFormatException(s)
    else if (negative && length == 1)
      throw new NumberFormatException(s)
    else
      parse(s, 1, radix, negative)
  }

  @inline
  def remainderUnsigned(dividend: scala.Int, divisor: scala.Int): scala.Int =
    remUInt(dividend, divisor)

  @inline def reverse(_i: scala.Int): scala.Int = {
    var i = _i
    i = (i & 0x55555555) << 1 | (i >> 1) & 0x55555555
    i = (i & 0x33333333) << 2 | (i >> 2) & 0x33333333
    i = (i & 0x0F0F0F0F) << 4 | (i >> 4) & 0x0F0F0F0F
    reverseBytes(i)
  }

  @inline def reverseBytes(i: scala.Int): scala.Int = {
    val byte3 = i >>> 24
    val byte2 = (i >>> 8) & 0xFF00
    val byte1 = (i << 8) & 0xFF0000
    val byte0 = i << 24
    byte0 | byte1 | byte2 | byte3
  }

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

  def toBinaryString(i: scala.Int): _String = {
    // toUnsignedString(i, 1)
    var count = 1
    if (i < 0) {
      count = 32
    } else {
      var j = i
      while (j != 0) {
        count += 1
        j >>>= 1
      }
    }

    var k = i
    val buffer = new Array[Char](count)
    do {
      count -= 1
      buffer(count) = ((k & 1) + '0').toChar
      k >>>= 1
    } while (count > 0)

    new _String(0, buffer.length, buffer)
  }

  def toHexString(i: scala.Int): _String = {
    // toUnsignedString(i, 4)
    var count = 1
    if (i < 0) {
      count = 8
    } else {
      var j = i
      while (j != 0) {
        count += 1
        j >>>= 4
      }
    }

    var k = i
    val buffer = new Array[Char](count)
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

    new _String(0, buffer.length, buffer)
  }

  def toOctalString(i: scala.Int): _String = {
    // toUnsignedString(i, 3)
    var count = 1
    if (i < 0) {
      count = 11
    } else {
      var j = i
      while (j != 0) {
        count += 1
        j >>>= 3
      }
    }

    var k = i
    val buffer = new Array[Char](count)
    do {
      count -= 1
      buffer(count) = ((k & 7) + '0').toChar
      k >>>= 3
    } while (count > 0)

    new _String(0, buffer.length, buffer)
  }

  @inline def toString(i: scala.Int): _String = {
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
          val res = quot / 10
          var digit_value = quot - ((res << 3) + (res << 1))
          digit_value += '0'
          last_digit += 1
          buffer(last_digit) = digit_value.toChar
          quot = res
        } while (quot != 0)

        last_digit -= 1
        val count = last_digit
        do {
          val tmp = buffer(last_digit)

          last_digit -= 1
          buffer(last_digit) = buffer(first_digit)

          first_digit += 1
          buffer(first_digit) = tmp
        } while (first_digit < last_digit)

        new _String(0, count, buffer)
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
              last_digit += 1
              buffer(last_digit) = '0'
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
            last_digit += 1
            buffer(last_digit) = (count + '0').toChar
          }

          k += 1
        }

        last_digit += 1
        buffer(last_digit) = (positive_value + '0').toChar
        last_digit -= 1
        count = last_digit

        new _String(0, count, buffer)
      }
    }
  }

  @inline def toString(_i: scala.Int, _radix: scala.Int): _String = {
    if (_i == 0) {
      "0"
    } else {
      val radix =
        if (_radix < Character.MIN_RADIX || _radix > Character.MAX_RADIX) 10
        else _radix
      val negative = _i < 0
      var (count, j) = if (negative) (2, _i) else (1, -_i)
      var i          = _i
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
        buffer(count) = ch.toChar
        j = j / radix
      } while (j != 0)

      if (negative) {
        buffer(0) = '-'
      }

      new _String(0, buffer.length, buffer)
    }
  }

  @inline def toUnsignedLong(x: scala.Int): scala.Long =
    undefined

  @inline def toUnsignedString(i: scala.Int): _String =
    toUnsignedString(i, 10)

  @inline def toUnsignedString(_i: scala.Int, _radix: scala.Int): _String = {
    val buffer = new Array[scala.Char](32)
    val length = buffer.length

    var j = 0
    while (j < length) {
      buffer(j) = '0'
      j += 1
    }

    var i       = _i
    var charPos = 32
    val radix = 1 << _radix
    val mask  = radix - 1
    do {
      charPos -= 1
      buffer(charPos) = digits(i & mask)
      i >>>= -radix
    } while (i != 0)

    new _String(buffer)
  }

  @inline def valueOf(i: scala.Int): Integer =
    new Integer(i)

  @inline def valueOf(s: _String): Integer =
    valueOf(parseInt(s))

  @inline def valueOf(s: _String, radix: scala.Int): Integer =
    valueOf(parseInt(s, radix))
}
