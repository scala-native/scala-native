package java.lang

final class Float(val floatValue: scala.Float)
    extends Number
    with Comparable[Float] {
  def this(s: String) = this(Float.parseFloat(s))

  @inline override def byteValue(): scala.Byte   = floatValue.toByte
  @inline override def shortValue(): scala.Short = floatValue.toShort
  @inline def intValue(): scala.Int              = floatValue.toInt
  @inline def longValue(): scala.Long            = floatValue.toLong
  @inline def doubleValue(): scala.Double        = floatValue.toDouble

  override def equals(that: Any): scala.Boolean = that match {
    case that: Float =>
      val a = floatValue
      val b = that.floatValue
      (a == b) || (Float.isNaN(a) && Float.isNaN(b))

    case _ =>
      false
  }

  @inline override def hashCode(): Int =
    Float.hashCode(floatValue)

  @inline override def compareTo(that: Float): Int =
    Float.compare(floatValue, that.floatValue)

  @inline override def toString(): String =
    Float.toString(floatValue)

  @inline def isNaN(): scala.Boolean =
    Float.isNaN(floatValue)

  @inline def isInfinite(): scala.Boolean =
    Float.isInfinite(floatValue)
}

object Float {
  final val TYPE              = classOf[scala.Float]
  final val POSITIVE_INFINITY = 1.0f / 0.0f
  final val NEGATIVE_INFINITY = 1.0f / -0.0f
  final val NaN               = 0.0f / 0.0f
  final val MAX_VALUE         = scala.Float.MaxValue
  final val MIN_VALUE         = scala.Float.MinPositiveValue
  final val MAX_EXPONENT      = 127
  final val MIN_EXPONENT      = -126
  final val SIZE              = 32

  @inline def valueOf(floatValue: scala.Float): Float = new Float(floatValue)

  @inline def valueOf(s: String): Float = valueOf(parseFloat(s))

  @inline def parseFloat(s: String): scala.Float = ???

  @inline def toString(f: scala.Float): String = String.valueOf(f)

  @inline def compare(a: scala.Float, b: scala.Float): scala.Int = ???

  @inline def isNaN(v: scala.Float): scala.Boolean =
    v != v

  @inline def isInfinite(v: scala.Float): scala.Boolean =
    v == POSITIVE_INFINITY || v == NEGATIVE_INFINITY

  @inline def intBitsToFloat(bits: scala.Int): scala.Float = ???

  @inline def floatToIntBits(value: scala.Float): scala.Int = ???

  @inline def hashCode(value: scala.Float): scala.Int = ???
}
