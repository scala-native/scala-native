package java.lang

import scalanative.runtime.{shortToUInt, shortToULong, Intrinsics}

final class Short(val _value: scala.Short)
    extends Number
    with Comparable[Short] {
  @inline def this(s: String) =
    this(Short.parseShort(s))

  @inline override def byteValue(): scala.Byte =
    _value.toByte

  @inline override def shortValue(): scala.Short =
    _value

  @inline override def intValue(): scala.Int =
    _value.toInt

  @inline override def longValue(): scala.Long =
    _value.toLong

  @inline override def floatValue(): scala.Float =
    _value.toFloat

  @inline override def doubleValue(): scala.Double =
    _value.toDouble

  @inline override def equals(that: Any): scala.Boolean =
    that match {
      case that: java.lang.Short =>
        _value == that._value
      case _ =>
        false
    }

  @inline override def hashCode(): scala.Int =
    _value

  @inline override def compareTo(that: Short): scala.Int =
    Short.compare(_value, that._value)

  @inline override def toString(): String =
    Short.toString(_value)

  @inline override def __scala_==(other: _Object): scala.Boolean =
    other match {
      case other: java.lang.Short     => _value == other._value
      case other: java.lang.Byte      => _value == other._value
      case other: java.lang.Integer   => _value == other._value
      case other: java.lang.Long      => _value == other._value
      case other: java.lang.Float     => _value == other._value
      case other: java.lang.Double    => _value == other._value
      case other: java.lang.Character => _value == other._value
      case _                          => super.__scala_==(other)
    }

  /*
   * Methods on scala.Short
   * The following methods are only here to properly support reflective calls
   * on boxed primitive values. YOU WILL NOT BE ABLE TO USE THESE METHODS, since
   * we use the true javalib to lookup symbols, this file contains only
   * implementations.
   */
  protected def toByte: scala.Byte     = _value.toByte
  protected def toShort: scala.Short   = _value
  protected def toChar: scala.Char     = _value.toChar
  protected def toInt: scala.Int       = _value.toInt
  protected def toLong: scala.Long     = _value.toLong
  protected def toFloat: scala.Float   = _value.toFloat
  protected def toDouble: scala.Double = _value.toDouble

  protected def unary_~ : scala.Int = ~ _value.toInt
  protected def unary_+ : scala.Int = _value.toInt
  protected def unary_- : scala.Int = - _value.toInt

  protected def +(x: String): String = _value + x

  protected def <<(x: scala.Int): scala.Int   = _value << x
  protected def <<(x: scala.Long): scala.Int  = _value << x
  protected def >>>(x: scala.Int): scala.Int  = _value >>> x
  protected def >>>(x: scala.Long): scala.Int = _value >>> x
  protected def >>(x: scala.Int): scala.Int   = _value >> x
  protected def >>(x: scala.Long): scala.Int  = _value >> x

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

  protected def |(x: scala.Byte): scala.Int  = _value | x
  protected def |(x: scala.Short): scala.Int = _value | x
  protected def |(x: scala.Char): scala.Int  = _value | x
  protected def |(x: scala.Int): scala.Int   = _value | x
  protected def |(x: scala.Long): scala.Long = _value | x

  protected def &(x: scala.Byte): scala.Int  = _value & x
  protected def &(x: scala.Short): scala.Int = _value & x
  protected def &(x: scala.Char): scala.Int  = _value & x
  protected def &(x: scala.Int): scala.Int   = _value & x
  protected def &(x: scala.Long): scala.Long = _value & x

  protected def ^(x: scala.Byte): scala.Int  = _value ^ x
  protected def ^(x: scala.Short): scala.Int = _value ^ x
  protected def ^(x: scala.Char): scala.Int  = _value ^ x
  protected def ^(x: scala.Int): scala.Int   = _value ^ x
  protected def ^(x: scala.Long): scala.Long = _value ^ x

  protected def +(x: scala.Byte): scala.Int      = _value + x
  protected def +(x: scala.Short): scala.Int     = _value + x
  protected def +(x: scala.Char): scala.Int      = _value + x
  protected def +(x: scala.Int): scala.Int       = _value + x
  protected def +(x: scala.Long): scala.Long     = _value + x
  protected def +(x: scala.Float): scala.Float   = _value + x
  protected def +(x: scala.Double): scala.Double = _value + x

  protected def -(x: scala.Byte): scala.Int      = _value - x
  protected def -(x: scala.Short): scala.Int     = _value - x
  protected def -(x: scala.Char): scala.Int      = _value - x
  protected def -(x: scala.Int): scala.Int       = _value - x
  protected def -(x: scala.Long): scala.Long     = _value - x
  protected def -(x: scala.Float): scala.Float   = _value - x
  protected def -(x: scala.Double): scala.Double = _value - x

  protected def *(x: scala.Byte): scala.Int      = _value * x
  protected def *(x: scala.Short): scala.Int     = _value * x
  protected def *(x: scala.Char): scala.Int      = _value * x
  protected def *(x: scala.Int): scala.Int       = _value * x
  protected def *(x: scala.Long): scala.Long     = _value * x
  protected def *(x: scala.Float): scala.Float   = _value * x
  protected def *(x: scala.Double): scala.Double = _value * x

  protected def /(x: scala.Byte): scala.Int      = _value / x
  protected def /(x: scala.Short): scala.Int     = _value / x
  protected def /(x: scala.Char): scala.Int      = _value / x
  protected def /(x: scala.Int): scala.Int       = _value / x
  protected def /(x: scala.Long): scala.Long     = _value / x
  protected def /(x: scala.Float): scala.Float   = _value / x
  protected def /(x: scala.Double): scala.Double = _value / x

  protected def %(x: scala.Byte): scala.Int      = _value % x
  protected def %(x: scala.Short): scala.Int     = _value % x
  protected def %(x: scala.Char): scala.Int      = _value % x
  protected def %(x: scala.Int): scala.Int       = _value % x
  protected def %(x: scala.Long): scala.Long     = _value % x
  protected def %(x: scala.Float): scala.Float   = _value % x
  protected def %(x: scala.Double): scala.Double = _value % x
}

object Short {
  final val TYPE  = classOf[scala.Short]
  final val SIZE  = 16
  final val BYTES = 2

  /* MIN_VALUE and MAX_VALUE should be 'final val's. But it is impossible to
   * write a proper Short literal in Scala, that would both considered a Short
   * and a constant expression (optimized as final val).
   * Since vals and defs are binary-compatible (although they're not strictly
   * speaking source-compatible, because of stability), we implement them as
   * defs. Source-compatibility is not an issue because user code is compiled
   * against the JDK .class files anyway.
   */
  @inline def MIN_VALUE: scala.Short = -32768
  @inline def MAX_VALUE: scala.Short = 32767

  @inline def compare(x: scala.Short, y: scala.Short): scala.Int =
    x - y

  @inline def decode(nm: String): Short = {
    val i = Integer.decode(nm).intValue
    val r = i.toShort
    if (r == i)
      valueOf(r)
    else
      throw new NumberFormatException()
  }

  @inline def hashCode(value: scala.Short): scala.Int =
    value.toInt

  @inline def parseShort(s: String): scala.Short =
    parseShort(s, 10)

  @inline def parseShort(s: String, radix: scala.Int): scala.Short = {
    val i = Integer.parseInt(s, radix)
    if (i < MIN_VALUE || i > MAX_VALUE)
      throw new NumberFormatException(s"""For input string: "$s"""")
    else
      i.toShort
  }

  @inline def reverseBytes(i: scala.Short): scala.Short =
    Intrinsics.`llvm.bswap.i16`(i)

  @inline def toString(s: scala.Short): String =
    Integer.toString(s)

  @inline def toUnsignedInt(x: scala.Short): scala.Int =
    shortToUInt(x)

  @inline def toUnsignedLong(x: scala.Short): scala.Long =
    shortToULong(x)

  @inline def valueOf(shortValue: scala.Short): Short =
    new Short(shortValue)

  @inline def valueOf(s: String): Short =
    valueOf(parseShort(s))

  @inline def valueOf(s: String, radix: scala.Int): Short =
    valueOf(parseShort(s, radix))
}
