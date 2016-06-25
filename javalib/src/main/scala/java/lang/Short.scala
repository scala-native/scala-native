package java.lang

import scalanative.runtime.undefined

final class Short(override val shortValue: scala.Short)
    extends Number
    with Comparable[Short] {
  def this(s: _String) =
    this(Short.parseShort(s))

  @inline override def byteValue(): scala.Byte =
    shortValue.toByte

  @inline def intValue(): scala.Int =
    shortValue.toInt

  @inline def longValue(): scala.Long =
    shortValue.toLong

  @inline def floatValue(): scala.Float =
    shortValue.toFloat

  @inline def doubleValue(): scala.Double =
    shortValue.toDouble

  @inline override def equals(that: Any): scala.Boolean =
    that match {
      case that: Short =>
        shortValue == that.shortValue
      case _ =>
        false
    }

  @inline override def hashCode(): scala.Int =
    shortValue

  @inline override def compareTo(that: Short): scala.Int =
    Short.compare(shortValue, that.shortValue)

  @inline override def toString(): String =
    Short.toString(shortValue)
}

object Short {
  final val TYPE = classOf[scala.Short]
  final val SIZE = 16

  /* MIN_VALUE and MAX_VALUE should be 'final val's. But it is impossible to
   * write a proper Short literal in Scala, that would both considered a Short
   * and a constant expression (optimized as final val).
   * Since vals and defs are binary-compatible (although they're not strictly
   * speaking source-compatible, because of stability), we implement them as
   * defs. Source-compatibility is not an issue because user code is compiled
   * against the JDK .class files anyway.
   */
  def MIN_VALUE: scala.Short = -32768
  def MAX_VALUE: scala.Short = 32767

  @inline def compare(x: scala.Short, y: scala.Short): scala.Int =
    x - y

  def decode(nm: _String): Short = {
    val i = Integer.decode(nm).intValue
    val r = i.toShort
    if (r == i)
      valueOf(r)
    else
      throw new NumberFormatException()
  }

  @inline def hashCode(value: scala.Short): scala.Int =
    value.toInt

  @inline def parseShort(s: _String): scala.Short =
    parseShort(s, 10)

  @inline def parseShort(s: _String, radix: scala.Int): scala.Short = {
    val i = Integer.parseInt(s, radix)
    if (i < MIN_VALUE || i > MAX_VALUE)
      throw new NumberFormatException(s"""For input string: "$s"""")
    else
      i.toShort
  }

  @inline def reverseBytes(i: scala.Short): scala.Short =
    (((i >>> 8) & 0xff) + ((i & 0xff) << 8)).toShort

  @inline def toString(s: scala.Short): _String =
    Integer.toString(s)

  def toUnsignedInt(x: scala.Short): scala.Int =
    undefined

  def toUnsignedLong(x: scala.Short): scala.Long =
    undefined

  @inline def valueOf(shortValue: scala.Short): Short =
    new Short(shortValue)

  @inline def valueOf(s: _String): Short =
    valueOf(parseShort(s))

  @inline def valueOf(s: _String, radix: scala.Int): Short =
    valueOf(parseShort(s, radix))
}
