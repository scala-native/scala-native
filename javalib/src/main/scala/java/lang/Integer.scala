package java.lang

final class Integer(val intValue: scala.Int)
object Integer {
  def valueOf(value: scala.Int): Integer = new Integer(value)
}
