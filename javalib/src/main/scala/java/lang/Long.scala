package java.lang

import scalanative.runtime.{divULong, remULong}
import scala.annotation.{switch, tailrec}

final class Long(val longValue: scala.Long)
    extends Number
    with Comparable[Long] {
  def this(s: String) = this(Long.parseLong(s))

  @inline override def byteValue(): scala.Byte   = longValue.toByte
  @inline override def shortValue(): scala.Short = longValue.toShort
  @inline def intValue(): scala.Int              = longValue.toInt
  @inline def floatValue(): scala.Float          = longValue.toFloat
  @inline def doubleValue(): scala.Double        = longValue.toDouble

  @inline override def equals(that: Any): scala.Boolean = that match {
    case that: Long => longValue == that.longValue
    case _          => false
  }

  @inline override def hashCode(): Int =
    Long.hashCode(longValue)

  @inline override def compareTo(that: Long): Int =
    Long.compare(longValue, that.longValue)

  @inline override def toString(): String =
    String.valueOf(longValue)
}

object Long {
  final val TYPE      = classOf[scala.Long]
  final val MIN_VALUE = -9223372036854775808L
  final val MAX_VALUE = 9223372036854775807L
  final val SIZE      = 64
  final val BYTES     = 8

  @inline def toString(i: scala.Long, radix: Int): String = ???

  @inline def toUnsignedString(i: scala.Long, radix: Int): String = ???

  @inline def toString(i: scala.Long): String = ???

  @inline def toUnsignedString(i: scala.Long): String = ???

  def parseLong(s: String, radix: Int): scala.Long = ???

  @inline def parseLong(s: String): scala.Long =
    parseLong(s, 10)

  def parseUnsignedLong(s: String, radix: Int): scala.Long = ???

  @inline def parseUnsignedLong(s: String): scala.Long =
    parseUnsignedLong(s, 10)

  def parseUnsignedLongInternal(
      s: String, radix: Int, start: Int): scala.Long = ???

  @inline def valueOf(longValue: scala.Long): Long = new Long(longValue)
  @inline def valueOf(s: String): Long             = valueOf(parseLong(s))

  @inline def valueOf(s: String, radix: Int): Long =
    valueOf(parseLong(s, radix))

  @inline def hashCode(value: scala.Long): Int =
    value.toInt ^ (value >>> 32).toInt

  @inline def compare(x: scala.Long, y: scala.Long): scala.Int = {
    if (x == y) 0
    else if (x < y) -1
    else 1
  }

  @inline def compareUnsigned(x: scala.Long, y: scala.Long): scala.Int = ???

  def divideUnsigned(dividend: scala.Long, divisor: scala.Long): scala.Long =
    divULong(dividend, divisor)

  def remainderUnsigned(
      dividend: scala.Long, divisor: scala.Long): scala.Long =
    remULong(dividend, divisor)

  def highestOneBit(i: scala.Long): scala.Long = ???

  def lowestOneBit(i: scala.Long): scala.Long = ???

  def bitCount(i: scala.Long): scala.Int = ???

  def reverseBytes(i: scala.Long): scala.Long = ???

  def rotateLeft(i: scala.Long, distance: scala.Int): scala.Long = ???

  def rotateRight(i: scala.Long, distance: scala.Int): scala.Long = ???

  def signum(i: scala.Long): Int = ???

  def numberOfLeadingZeros(l: scala.Long): Int = ???

  def numberOfTrailingZeros(l: scala.Long): Int = ???

  def toBinaryString(l: scala.Long): String = ???

  def toHexString(l: scala.Long): String = ???

  def toOctalString(l: scala.Long): String = ???

  @inline def sum(a: scala.Long, b: scala.Long): scala.Long =
    a + b

  @inline def max(a: scala.Long, b: scala.Long): scala.Long =
    Math.max(a, b)

  @inline def min(a: scala.Long, b: scala.Long): scala.Long =
    Math.min(a, b)
}
