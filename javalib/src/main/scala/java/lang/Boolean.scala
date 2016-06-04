package java.lang

final class Boolean(val booleanValue: scala.Boolean)
    extends Comparable[Boolean] {
  def this(s: String) = this(Boolean.parseBoolean(s))

  @inline override def equals(that: Any): scala.Boolean =
    this eq that.asInstanceOf[AnyRef]

  @inline override def hashCode(): Int =
    if (booleanValue) 1231 else 1237

  @inline override def compareTo(that: Boolean): Int =
    Boolean.compare(booleanValue, that.booleanValue)

  @inline override def toString(): String =
    Boolean.toString(booleanValue)
}

object Boolean {
  final val TYPE           = classOf[scala.Boolean]
  final val TRUE: Boolean  = new Boolean(true)
  final val FALSE: Boolean = new Boolean(false)

  @inline def valueOf(booleanValue: scala.Boolean): Boolean =
    if (booleanValue) TRUE else FALSE

  @inline def valueOf(s: String): Boolean =
    valueOf(parseBoolean(s))

  @inline def parseBoolean(s: String): scala.Boolean =
    (s != null) && s.equalsIgnoreCase("true")

  @inline def toString(b: scala.Boolean): String =
    String.valueOf(b)

  @inline def compare(x: scala.Boolean, y: scala.Boolean): scala.Int =
    if (x == y) 0 else if (x) 1 else -1
}
