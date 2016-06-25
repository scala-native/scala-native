package java.lang

import scalanative.runtime.undefined

final class Byte(override val byteValue: scala.Byte)
    extends Number
    with Comparable[Byte] {
  def this(s: _String) =
    this(Byte.parseByte(s))

  @inline override def shortValue(): scala.Short =
    byteValue.toShort

  @inline def intValue(): scala.Int =
    byteValue.toInt

  @inline def longValue(): scala.Long =
    byteValue.toLong

  @inline def floatValue(): scala.Float =
    byteValue.toFloat

  @inline def doubleValue(): scala.Double =
    byteValue.toDouble

  @inline override def equals(that: Any): scala.Boolean =
    that match {
      case that: Byte =>
        byteValue == that.byteValue
      case _ =>
        false
    }

  @inline override def hashCode(): Int =
    Byte.hashCode(byteValue)

  @inline override def compareTo(that: Byte): Int =
    Byte.compare(byteValue, that.byteValue)

  @inline override def toString(): String =
    Byte.toString(byteValue)
}

object Byte {
  final val TYPE = classOf[scala.Byte]
  final val SIZE = 8

  /* MIN_VALUE and MAX_VALUE should be 'final val's. But it is impossible to
   * write a proper Byte literal in Scala, that would both considered a Byte
   * and a constant expression (optimized as final val).
   * Since vals and defs are binary-compatible (although they're not strictly
   * speaking source-compatible, because of stability), we implement them as
   * defs. Source-compatibility is not an issue because user code is compiled
   * against the JDK .class files anyway.
   */
  def MIN_VALUE: scala.Byte = -128

  def MAX_VALUE: scala.Byte = 127

  @inline def compare(x: scala.Byte, y: scala.Byte): scala.Int =
    x - y

  @inline def decode(nm: _String): Byte = {
    val i = Integer.decode(nm).intValue
    val b = i.toByte
    if (b == i)
      valueOf(b)
    else
      throw new NumberFormatException()
  }

  @inline def hashCode(b: scala.Byte): scala.Int =
    b.toInt

  @inline def parseByte(s: _String, radix: scala.Int): scala.Byte = {
    val i = Integer.parseInt(s, radix)
    if (i < MIN_VALUE || i > MAX_VALUE)
      throw new NumberFormatException(s"""For input string: "$s"""")
    else
      i.toByte
  }

  @inline def parseByte(s: _String): scala.Byte =
    parseByte(s, 10)

  @inline def toString(b: scala.Byte): _String =
    Integer.toString(b)

  def toUnsignedInt(x: scala.Byte): scala.Int =
    x.toInt & 0xff

  def toUnsignedLong(x: scala.Byte): scala.Long =
    x.toLong & 0xffL

  @inline def valueOf(byteValue: scala.Byte): Byte =
    new Byte(byteValue)

  @inline def valueOf(s: _String): Byte =
    valueOf(parseByte(s))

  @inline def valueOf(s: _String, radix: scala.Int): Byte =
    valueOf(parseByte(s, radix))
}
