package java.lang

final class Double(val doubleValue: scala.Double)
    extends Number
    with Comparable[Double] {
  def this(s: String) = this(Double.parseDouble(s))

  @inline override def byteValue(): scala.Byte   = doubleValue.toByte
  @inline override def shortValue(): scala.Short = doubleValue.toShort
  @inline def intValue(): scala.Int              = doubleValue.toInt
  @inline def longValue(): scala.Long            = doubleValue.toLong
  @inline def floatValue(): scala.Float          = doubleValue.toFloat

  override def equals(that: Any): scala.Boolean = that match {
    case that: Double =>
      val a = doubleValue
      val b = that.doubleValue
      (a == b) || (Double.isNaN(a) && Double.isNaN(b))

    case _ =>
      false
  }

  @inline override def hashCode(): Int =
    Double.hashCode(doubleValue)

  @inline override def compareTo(that: Double): Int =
    Double.compare(doubleValue, that.doubleValue)

  @inline override def toString(): String =
    Double.toString(doubleValue)

  @inline def isNaN(): scala.Boolean =
    Double.isNaN(doubleValue)

  @inline def isInfinite(): scala.Boolean =
    Double.isInfinite(doubleValue)
}

object Double {
  final val TYPE              = classOf[scala.Double]
  final val POSITIVE_INFINITY = 1.0 / 0.0
  final val NEGATIVE_INFINITY = 1.0 / -0.0
  final val NaN               = 0.0 / 0.0
  final val MAX_VALUE         = scala.Double.MaxValue
  final val MIN_VALUE         = scala.Double.MinPositiveValue
  final val MAX_EXPONENT      = 1023
  final val MIN_EXPONENT      = -1022
  final val SIZE              = 64

  @inline def valueOf(doubleValue: scala.Double): Double =
    new Double(doubleValue)

  @inline def valueOf(s: String): Double = valueOf(parseDouble(s))

  def parseDouble(s: String): scala.Double = ???

  @inline def toString(d: scala.Double): String =
    "" + d

  def compare(a: scala.Double, b: scala.Double): scala.Int = ???

  @inline def isNaN(v: scala.Double): scala.Boolean =
    v != v

  @inline def isInfinite(v: scala.Double): scala.Boolean =
    v == POSITIVE_INFINITY || v == NEGATIVE_INFINITY

  @inline def longBitsToDouble(bits: scala.Long): scala.Double =
    ???

  @inline def doubleToLongBits(value: scala.Double): scala.Long =
    ???

  @inline def hashCode(value: scala.Double): scala.Int =
    ???
}
