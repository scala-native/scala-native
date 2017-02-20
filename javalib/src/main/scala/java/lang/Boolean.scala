package java.lang

final class Boolean(val _value: scala.Boolean) extends Comparable[Boolean] {
  def booleanValue(): scala.Boolean =
    _value

  @inline def this(s: String) =
    this(Boolean.parseBoolean(s))

  @inline override def equals(that: Any): scala.Boolean =
    this eq that.asInstanceOf[AnyRef]

  @inline override def hashCode(): Int =
    Boolean.hashCode(_value)

  @inline override def compareTo(that: Boolean): Int =
    Boolean.compare(_value, that._value)

  @inline override def toString(): String =
    Boolean.toString(_value)

  /*
   * Ported from ScalaJS
   *
   * Methods on scala.Boolean
   * The following methods are only here to properly support reflective calls
   * on boxed primitive values. YOU WILL NOT BE ABLE TO USE THESE METHODS, since
   * we use the true javalib to lookup symbols, this file contains only
   * implementations.
   */

  protected def unary_! : scala.Boolean             = ! _value
  protected def ||(x: scala.Boolean): scala.Boolean = _value || x
  protected def &&(x: scala.Boolean): scala.Boolean = _value && x
  protected def |(x: scala.Boolean): scala.Boolean  = _value | x
  protected def &(x: scala.Boolean): scala.Boolean  = _value & x
  protected def ^(x: scala.Boolean): scala.Boolean  = _value ^ x
}

object Boolean {
  final val TYPE           = classOf[scala.Boolean]
  final val TRUE: Boolean  = new Boolean(true)
  final val FALSE: Boolean = new Boolean(false)

  @inline def compare(x: scala.Boolean, y: scala.Boolean): scala.Int =
    if (x == y) 0 else if (x) 1 else -1

  @inline def getBoolean(name: String): scala.Boolean =
    parseBoolean(System.getProperty(name))

  @inline def hashCode(b: scala.Boolean): scala.Int =
    if (b) 1231 else 1237

  @inline def logicalAnd(a: scala.Boolean, b: scala.Boolean): scala.Boolean =
    a && b

  @inline def logicalOr(a: scala.Boolean, b: scala.Boolean): scala.Boolean =
    a || b

  @inline def logicalXor(a: scala.Boolean, b: scala.Boolean): scala.Boolean =
    a ^ b

  @inline def parseBoolean(s: String): scala.Boolean =
    (s != null) && s.equalsIgnoreCase("true")

  @inline def toString(b: scala.Boolean): String =
    if (b) "true" else "false"

  @inline def valueOf(booleanValue: scala.Boolean): Boolean =
    if (booleanValue) TRUE else FALSE

  @inline def valueOf(s: String): Boolean =
    valueOf(parseBoolean(s))
}
