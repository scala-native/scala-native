package java.lang

final class Boolean(val booleanValue: scala.Boolean)
    extends Comparable[Boolean] {
  @inline def this(s: String) =
    this(Boolean.parseBoolean(s))

  @inline override def equals(that: Any): scala.Boolean =
    this eq that.asInstanceOf[AnyRef]

  @inline override def hashCode(): Int =
    Boolean.hashCode(booleanValue)

  @inline override def compareTo(that: Boolean): Int =
    Boolean.compare(booleanValue, that.booleanValue)

  @inline override def toString(): String =
    Boolean.toString(booleanValue)
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
