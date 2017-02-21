package scala.runtime

import scala.math.ScalaNumber
import scala.scalanative.native._

object BoxesRunTime {
  def boxToBoolean(v: scala.Boolean): java.lang.Boolean =
    java.lang.Boolean.valueOf(v)
  def boxToCharacter(v: scala.Char): java.lang.Character =
    java.lang.Character.valueOf(v)
  def boxToByte(v: scala.Byte): java.lang.Byte =
    java.lang.Byte.valueOf(v)
  def boxToShort(v: scala.Short): java.lang.Short =
    java.lang.Short.valueOf(v)
  def boxToInteger(v: scala.Int): java.lang.Integer =
    java.lang.Integer.valueOf(v)
  def boxToLong(v: scala.Long): java.lang.Long =
    java.lang.Long.valueOf(v)
  def boxToFloat(v: scala.Float): java.lang.Float =
    java.lang.Float.valueOf(v)
  def boxToDouble(v: scala.Double): java.lang.Double =
    java.lang.Double.valueOf(v)

  def unboxToBoolean(o: java.lang.Object): scala.Boolean =
    if (o == null) false else o.asInstanceOf[java.lang.Boolean].booleanValue
  def unboxToChar(o: java.lang.Object): scala.Char =
    if (o == null) 0 else o.asInstanceOf[java.lang.Character].charValue
  def unboxToByte(o: java.lang.Object): scala.Byte =
    if (o == null) 0 else o.asInstanceOf[java.lang.Byte].byteValue
  def unboxToShort(o: java.lang.Object): scala.Short =
    if (o == null) 0 else o.asInstanceOf[java.lang.Short].shortValue
  def unboxToInt(o: java.lang.Object): scala.Int =
    if (o == null) 0 else o.asInstanceOf[java.lang.Integer].intValue
  def unboxToLong(o: java.lang.Object): scala.Long =
    if (o == null) 0 else o.asInstanceOf[java.lang.Long].longValue
  def unboxToFloat(o: java.lang.Object): scala.Float =
    if (o == null) 0 else o.asInstanceOf[java.lang.Float].floatValue
  def unboxToDouble(o: java.lang.Object): scala.Double =
    if (o == null) 0 else o.asInstanceOf[java.lang.Double].doubleValue

  // Intrinsified as primitives. They are never called.
  def hashFromObject(o: java.lang.Object): Int = ???
  def hashFromNumber(o: java.lang.Number): Int = ???
  def hashFromFloat(o: java.lang.Float): Int   = ???
  def hashFromDouble(o: java.lang.Double): Int = ???
  def hashFromLong(o: java.lang.Long): Int     = ???
}
