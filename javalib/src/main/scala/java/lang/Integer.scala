package java.lang

import scalanative.runtime.Intrinsics.{divUInt, remUInt, intToULong}
import scalanative.runtime.LLVMIntrinsics
import java.lang.constant.{Constable, ConstantDesc}

final class Integer(val _value: scala.Int)
    extends Number
    with Comparable[Integer]
    with Constable
    with ConstantDesc {
  @inline def this(s: String) =
    this(Integer.parseInt(s))

  @inline override def byteValue(): scala.Byte =
    _value.toByte

  @inline override def shortValue(): scala.Short =
    _value.toShort

  @inline override def intValue(): scala.Int =
    _value

  @inline override def longValue(): scala.Long =
    _value.toLong

  @inline override def floatValue(): scala.Float =
    _value.toFloat

  @inline override def doubleValue(): scala.Double =
    _value.toDouble

  @inline override def equals(that: Any): scala.Boolean =
    that match {
      case that: Integer =>
        _value == that._value
      case _ =>
        false
    }

  @inline override def hashCode(): scala.Int =
    _value

  @inline override def compareTo(that: Integer): scala.Int =
    Integer.compare(_value, that._value)

  @inline override def toString(): String =
    Integer.toString(_value)

  /*
   * Ported from ScalaJS
   *
   * Methods on scala.Int
   * The following methods are only here to properly support reflective calls
   * on boxed primitive values. YOU WILL NOT BE ABLE TO USE THESE METHODS, since
   * we use the true javalib to lookup symbols, this file contains only
   * implementations.
   */
  protected def toByte: scala.Byte = _value.toByte
  protected def toShort: scala.Short = _value.toShort
  protected def toChar: scala.Char = _value.toChar
  protected def toInt: scala.Int = _value
  protected def toLong: scala.Long = _value.toLong
  protected def toFloat: scala.Float = _value.toFloat
  protected def toDouble: scala.Double = _value.toDouble

  protected def unary_~ : scala.Int = ~_value
  protected def unary_+ : scala.Int = _value
  protected def unary_- : scala.Int = -_value

  protected def +(x: String): String = "" + _value + x

  protected def <<(x: scala.Int): scala.Int = _value << x
  protected def <<(x: scala.Long): scala.Int = _value << x.toInt
  protected def >>>(x: scala.Int): scala.Int = _value >>> x
  protected def >>>(x: scala.Long): scala.Int = _value >>> x.toInt
  protected def >>(x: scala.Int): scala.Int = _value >> x
  protected def >>(x: scala.Long): scala.Int = _value >> x.toInt

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

  protected def |(x: scala.Byte): scala.Int = _value | x
  protected def |(x: scala.Short): scala.Int = _value | x
  protected def |(x: scala.Char): scala.Int = _value | x
  protected def |(x: scala.Int): scala.Int = _value | x
  protected def |(x: scala.Long): scala.Long = _value | x

  protected def &(x: scala.Byte): scala.Int = _value & x
  protected def &(x: scala.Short): scala.Int = _value & x
  protected def &(x: scala.Char): scala.Int = _value & x
  protected def &(x: scala.Int): scala.Int = _value & x
  protected def &(x: scala.Long): scala.Long = _value & x

  protected def ^(x: scala.Byte): scala.Int = _value ^ x
  protected def ^(x: scala.Short): scala.Int = _value ^ x
  protected def ^(x: scala.Char): scala.Int = _value ^ x
  protected def ^(x: scala.Int): scala.Int = _value ^ x
  protected def ^(x: scala.Long): scala.Long = _value ^ x

  protected def +(x: scala.Byte): scala.Int = _value + x
  protected def +(x: scala.Short): scala.Int = _value + x
  protected def +(x: scala.Char): scala.Int = _value + x
  protected def +(x: scala.Int): scala.Int = _value + x
  protected def +(x: scala.Long): scala.Long = _value + x
  protected def +(x: scala.Float): scala.Float = _value + x
  protected def +(x: scala.Double): scala.Double = _value + x

  protected def -(x: scala.Byte): scala.Int = _value - x
  protected def -(x: scala.Short): scala.Int = _value - x
  protected def -(x: scala.Char): scala.Int = _value - x
  protected def -(x: scala.Int): scala.Int = _value - x
  protected def -(x: scala.Long): scala.Long = _value - x
  protected def -(x: scala.Float): scala.Float = _value - x
  protected def -(x: scala.Double): scala.Double = _value - x

  protected def *(x: scala.Byte): scala.Int = _value * x
  protected def *(x: scala.Short): scala.Int = _value * x
  protected def *(x: scala.Char): scala.Int = _value * x
  protected def *(x: scala.Int): scala.Int = _value * x
  protected def *(x: scala.Long): scala.Long = _value * x
  protected def *(x: scala.Float): scala.Float = _value * x
  protected def *(x: scala.Double): scala.Double = _value * x

  protected def /(x: scala.Byte): scala.Int = _value / x
  protected def /(x: scala.Short): scala.Int = _value / x
  protected def /(x: scala.Char): scala.Int = _value / x
  protected def /(x: scala.Int): scala.Int = _value / x
  protected def /(x: scala.Long): scala.Long = _value / x
  protected def /(x: scala.Float): scala.Float = _value / x
  protected def /(x: scala.Double): scala.Double = _value / x

  protected def %(x: scala.Byte): scala.Int = _value % x
  protected def %(x: scala.Short): scala.Int = _value % x
  protected def %(x: scala.Char): scala.Int = _value % x
  protected def %(x: scala.Int): scala.Int = _value % x
  protected def %(x: scala.Long): scala.Long = _value % x
  protected def %(x: scala.Float): scala.Float = _value % x
  protected def %(x: scala.Double): scala.Double = _value % x
}

private[lang] object IntegerCache {
  private[lang] val cache = new Array[java.lang.Integer](256)
}

object Integer {
  final val TYPE = scala.Predef.classOf[scala.scalanative.runtime.PrimitiveInt]
  final val MIN_VALUE = -2147483648
  final val MAX_VALUE = 2147483647
  final val SIZE = 32
  final val BYTES = 4

  @inline def bitCount(i: scala.Int): scala.Int =
    LLVMIntrinsics.`llvm.ctpop.i32`(i)

  @inline def byteValue(i: scala.Int): scala.Byte =
    i.toByte

  @inline def compare(x: scala.Int, y: scala.Int): scala.Int =
    if (x == y) 0 else if (x < y) -1 else 1

  @inline def compareUnsigned(x: scala.Int, y: scala.Int): scala.Int =
    compare(x ^ scala.Int.MinValue, y ^ scala.Int.MinValue)

  private def fail(s: String): Nothing =
    throw new NumberFormatException(s"""For input string: "$s"""")

  def decode(nm: String): Integer = {
    if (nm == null)
      throw new NumberFormatException("null")
    val length = nm.length()
    if (length == 0) fail(nm)

    var i = 0
    var first = nm.charAt(i)
    val negative = first == '-'
    val positive = first == '+'

    if (negative || positive) {
      if (length == 1) fail(nm)
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
        if (i == length) fail(nm)
        base = 16
      } else {
        base = 8
      }
    } else if (first == '#') {
      i += 1
      if (i == length) fail(nm)
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
    LLVMIntrinsics.`llvm.ctlz.i32`(i, iszeroundef = false)

  @inline def numberOfTrailingZeros(i: scala.Int): scala.Int =
    LLVMIntrinsics.`llvm.cttz.i32`(i, iszeroundef = false)

  @inline def parseInt(s: String): scala.Int =
    parseInt(s, 10)

  def parseInt(s: String, radix: scala.Int): scala.Int = {
    if (s == null)
      throw new NumberFormatException("null")
    if (radix < Character.MIN_RADIX)
      throw new NumberFormatException(
        s"radix $radix less than Character.MIN_RADIX"
      )
    if (radix > Character.MAX_RADIX)
      throw new NumberFormatException(
        s"radix $radix greater than Character.MAX_RADIX"
      )

    val length = s.length()
    if (length == 0) fail(s)

    val positive = s.charAt(0) == '+'
    val negative = s.charAt(0) == '-'
    val offset = if (positive || negative) 1 else 0
    if (offset > 0 && length == 1) fail(s)

    parse(s, offset, radix, negative)
  }

  private def parse(
      s: String,
      _offset: scala.Int,
      radix: scala.Int,
      negative: scala.Boolean
  ): scala.Int = {
    val max = MIN_VALUE / radix
    val length = s.length()
    var result = 0
    var offset = _offset

    while (offset < length) {
      val digit = Character.digit(s.charAt(offset), radix)
      offset += 1
      if (digit == -1) fail(s)
      if (max > result) fail(s)

      val next = result * radix - digit
      if (next > result) fail(s)
      result = next
    }

    if (!negative) {
      result = -result
      if (result < 0) fail(s)
    }

    result
  }

  @inline
  def remainderUnsigned(dividend: scala.Int, divisor: scala.Int): scala.Int =
    remUInt(dividend, divisor)

  @inline def reverse(i: scala.Int): scala.Int =
    LLVMIntrinsics.`llvm.bitreverse.i32`(i)

  @inline def reverseBytes(i: scala.Int): scala.Int =
    LLVMIntrinsics.`llvm.bswap.i32`(i)

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
    var k = i
    while ({
      count -= 1
      buffer(count) = ((k & 1) + '0').toChar
      k >>>= 1
      count > 0
    }) ()

    new String(buffer)
  }

  def toHexString(i: scala.Int): String = {
    var count =
      if (i == 0) 1
      else ((32 - numberOfLeadingZeros(i)) + 3) / 4
    val buffer = new Array[Char](count)
    var k = i
    while ({
      var t = k & 15
      if (t > 9) {
        t = t - 10 + 'a'
      } else {
        t += '0'
      }
      count -= 1
      buffer(count) = t.toChar
      k >>>= 4
      count > 0
    }) ()

    new String(buffer)
  }

  def toOctalString(i: scala.Int): String = {
    var count =
      if (i == 0) 1
      else ((32 - numberOfLeadingZeros(i)) + 2) / 3
    val buffer = new Array[Char](count)
    var k = i
    while ({
      count -= 1
      buffer(count) = ((k & 7) + '0').toChar
      k >>>= 3
      count > 0
    }) ()

    new String(buffer)
  }

  def toString(i: scala.Int): String = {
    if (i == 0) { "0" }
    else if (i == java.lang.Integer.MIN_VALUE) { "-2147483648" }
    else if (i == java.lang.Integer.MAX_VALUE) { "2147483647" }
    else {
      val negative = i < 0
      val bufferSize = if (i < 1000 && i > -1000) 4 else 11
      val buffer = new Array[Char](bufferSize)
      var positiveValue = Math.abs(i)
      var offset = bufferSize - 1
      while (positiveValue != 0 && offset > 0) {
        val next = positiveValue / 10
        buffer(offset) = ((positiveValue - next * 10) + '0').toChar
        offset = offset - 1
        positiveValue = next
      }

      if (negative) {
        buffer(offset) = '-'
      } else {
        offset = offset + 1
      }

      new String(buffer, offset, bufferSize - offset)
    }
  }

  def toString(_i: scala.Int, _radix: scala.Int): String = {
    if (_i == 0) {
      "0"
    } else {
      val radix =
        if (_radix < Character.MIN_RADIX || _radix > Character.MAX_RADIX) 10
        else _radix
      var i = _i
      var j = _i
      var count = 2
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
      while ({
        var ch = 0 - (j % radix)
        if (ch > 9) {
          ch = ch - 10 + 'a'
        } else {
          ch += '0'
        }
        count -= 1
        buffer(count) = ch.toChar
        j /= radix
        j != 0
      }) ()

      if (negative) {
        buffer(0) = '-'
      }

      new String(buffer)
    }
  }

  @inline def toUnsignedLong(x: scala.Int): scala.Long =
    intToULong(x)

  import IntegerCache.cache

  @inline def valueOf(intValue: scala.Int): Integer = {
    if (intValue.toByte.toInt != intValue) {
      new Integer(intValue)
    } else {
      val idx = intValue + 128
      val cached = cache(idx)
      if (cached != null) {
        cached
      } else {
        val newint = new Integer(intValue)
        cache(idx) = newint
        newint
      }
    }
  }

  @inline def valueOf(s: String): Integer =
    valueOf(parseInt(s))

  @inline def valueOf(s: String, radix: scala.Int): Integer =
    valueOf(parseInt(s, radix))

  @inline def parseUnsignedInt(s: String): scala.Int = parseUnsignedInt(s, 10)

  def parseUnsignedInt(s: String, radix: scala.Int): scala.Int = {
    if (s == null)
      throw new NumberFormatException("null")
    if (radix < Character.MIN_RADIX)
      throw new NumberFormatException(
        s"radix $radix less than Character.MIN_RADIX"
      )
    if (radix > Character.MAX_RADIX)
      throw new NumberFormatException(
        s"radix $radix greater than Character.MAX_RADIX"
      )

    val len = s.length()
    if (len == 0) fail(s)

    val hasPlusSign = s.charAt(0) == '+'
    val hasMinusSign = s.charAt(0) == '-'
    if ((hasPlusSign || hasMinusSign) && len == 1) fail(s)
    if (hasMinusSign)
      throw new NumberFormatException(
        s"""Illegal leading minus sign on unsigned string $s."""
      )

    val offset = if (hasPlusSign) 1 else 0

    parseUnsigned(s, offset, radix)
  }

  private def parseUnsigned(s: String, _offset: Int, radix: Int): scala.Int = {
    val unsignedIntMaxValue = -1
    val max = divideUnsigned(unsignedIntMaxValue, radix)
    var result = 0
    var offset = _offset
    val length = s.length()

    while (offset < length) {
      val digit = Character.digit(s.charAt(offset), radix)
      offset += 1

      if (digit == -1) fail(s)

      if (compareUnsigned(result, max) > 0) fail(s)

      result = result * radix + digit

      if (compareUnsigned(digit, result) > 0)
        throw new NumberFormatException(
          s"""String value $s exceeds range of unsigned int."""
        )
    }

    result
  }

  @inline def toUnsignedString(i: scala.Int): String = toUnsignedString(i, 10)

  def toUnsignedString(_i: scala.Int, _radix: scala.Int): String = {
    if (_i == 0) {
      "0"
    } else {

      val radix =
        if (_radix < Character.MIN_RADIX || _radix > Character.MAX_RADIX) {
          10
        } else _radix

      var j = _i
      var l = _i

      // calculate string size
      var count = 1
      l = divideUnsigned(l, radix)
      while (l != 0) {
        count += 1
        l = divideUnsigned(l, radix)
      }

      // populate string with characters
      val buffer = new Array[Char](count)
      while ({
        val digit = remainderUnsigned(j, radix)
        val ch = Character.forDigit(digit.toInt, radix)
        count -= 1
        buffer(count) = ch
        j = divideUnsigned(j, radix)
        j != 0
      }) ()

      new String(buffer)
    }
  }

  /** @since JDK 19 */
  // Ported from Scala.js, revision: f335260, dated 2025-06-21.

  def compress(i: scala.Int, mask: scala.Int): scala.Int = {
    // Hacker's Delight, Section 7-4, Figure 7-10

    val LogBitSize = 5 // log_2(32)

    // !!! Verbatim copy-paste of Long.compress

    var m = mask
    var x = i & mask // clear irrelevant bits
    var mk = ~m << 1 // we will count 0's to right

    var j = 0 // i in Hacker's Delight, but we already have an i
    while (j < LogBitSize) {
      val mp = parallelSuffix(mk)
      val mv = mp & m // bits to move
      m = (m ^ mv) | (mv >>> (1 << j)) // compress m
      val t = x & mv
      x = (x ^ t) | (t >>> (1 << j)) // compress x
      mk = mk & ~mp
      j += 1
    }

    x
  }

  /** @since JDK 19 */
  // Ported from Scala.js, revision: f335260, dated 2025-06-21.

  def expand(i: scala.Int, mask: scala.Int): scala.Int = {
    // Hacker's Delight, Section 7-5, Figure 7-12

    val LogBitSize = 5 // log_2(32)

    val array = new Array[scala.Int](LogBitSize)

    // !!! Verbatim copy-paste of Long.expand

    var m = mask
    var x = i
    var mk = ~m << 1 // we will count 0's to right

    var j = 0 // i in Hacker's Delight, but we already have an i
    while (j < LogBitSize) {
      val mp = parallelSuffix(mk)
      val mv = mp & m // bits to move
      array(j) = mv
      m = (m ^ mv) | (mv >>> (1 << j)) // compress m
      mk = mk & ~mp
      j += 1
    }

    j = LogBitSize - 1
    while (j >= 0) {
      val mv = array(j)
      val t = x << (1 << j)

      /* See the last line of the section text, but there is a mistake in the
       * book: y should be t. There is no y in this algorithm, so it doesn't
       * make sense. Plugging t instead matches the formula (c) of "Exchanging
       * Corresponding Fields of Registers" in Section 2-20.
       */
      x = ((x ^ t) & mv) ^ x

      j -= 1
    }

    x & mask // clear out extraneous bits
  }

  // Ported from Scala.js, revision: f335260, dated 2025-06-21.
  @inline
  private def parallelSuffix(x: Int): Int = {
    // Hacker's Delight, Section 5-2
    var y = x ^ (x << 1)
    y = y ^ (y << 2)
    y = y ^ (y << 4)
    y = y ^ (y << 8)
    y ^ (y << 16)
  }

}
