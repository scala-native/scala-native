package java.lang

final class Boolean(val booleanValue: scala.Boolean)
object Boolean {
  def valueOf(value: scala.Boolean): Boolean = new Boolean(value)
}
