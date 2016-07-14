package scala.runtime

import scala.math.ScalaNumber
import scala.scalanative.native._

object BoxesRunTime {
  final val CHAR   = 0
  final val BYTE   = 1
  final val SHORT  = 2
  final val INT    = 3
  final val LONG   = 4
  final val FLOAT  = 5
  final val DOUBLE = 6
  final val OTHER  = 7

  def typeCode(a: Object) = a match {
    case _: java.lang.Integer   => INT
    case _: java.lang.Double    => DOUBLE
    case _: java.lang.Long      => LONG
    case _: java.lang.Character => CHAR
    case _: java.lang.Float     => FLOAT
    case _: java.lang.Byte      => INT
    case _: java.lang.Short     => INT
    case _                      => OTHER
  }

  /* BOXING ... BOXING ... BOXING ... BOXING ... BOXING ... BOXING ... BOXING ... BOXING */

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

  /* UNBOXING ... UNBOXING ... UNBOXING ... UNBOXING ... UNBOXING ... UNBOXING ... UNBOXING */

  def unboxToBoolean(o: java.lang.Object): scala.Boolean =
    if (o == null) false else o.asInstanceOf[java.lang.Boolean].booleanValue()
  def unboxToChar(o: java.lang.Object): scala.Char =
    if (o == null) 0 else o.asInstanceOf[java.lang.Character].charValue()
  def unboxToByte(o: java.lang.Object): scala.Byte =
    if (o == null) 0 else o.asInstanceOf[java.lang.Byte].byteValue()
  def unboxToShort(o: java.lang.Object): scala.Short =
    if (o == null) 0 else o.asInstanceOf[java.lang.Short].shortValue()
  def unboxToInt(o: java.lang.Object): scala.Int =
    if (o == null) 0 else o.asInstanceOf[java.lang.Integer].intValue()
  def unboxToLong(o: java.lang.Object): scala.Long =
    if (o == null) 0 else o.asInstanceOf[java.lang.Long].longValue()
  def unboxToFloat(o: java.lang.Object): scala.Float =
    if (o == null) 0 else o.asInstanceOf[java.lang.Float].floatValue()
  def unboxToDouble(o: java.lang.Object): scala.Double =
    if (o == null) 0 else o.asInstanceOf[java.lang.Double].doubleValue()

  /* COMPARISON ... COMPARISON ... COMPARISON ... COMPARISON ... COMPARISON ... COMPARISON */

  def equals(x: Object, y: Object) =
    if (x == y) true
    else equals2(x, y)

  def equals2(x: Object, y: Object) = x match {
    case x: java.lang.Number    => equalsNumObject(x, y)
    case x: java.lang.Character => equalsCharObject(x, y)
    case null                   => y == null
    case _                      => x.equals(y)
  }

  def equalsNumObject(x: java.lang.Number, y: Object) = y match {
    case y: java.lang.Number    => equalsNumNum(x, y)
    case y: java.lang.Character => equalsNumChar(x, y)
    case _ =>
      if (x == null) y == null
      else x.equals(y)
  }

  def equalsNumNum(x: java.lang.Number, y: java.lang.Number) = {
    val xcode   = typeCode(x)
    val ycode   = typeCode(y)
    val maxcode = if (ycode > xcode) ycode else xcode

    maxcode match {
      case INT    => x.intValue == y.intValue
      case LONG   => x.longValue == y.longValue
      case FLOAT  => x.floatValue == y.floatValue
      case DOUBLE => x.doubleValue == y.doubleValue
      case _ =>
        if (y.isInstanceOf[ScalaNumber] && !x.isInstanceOf[ScalaNumber])
          y.equals(x)
        else if (x == null) y == null
        else x.equals(y)
    }
  }

  def equalsCharObject(x: java.lang.Character, y: Object) = y match {
    case y: java.lang.Character => x.charValue == y.charValue
    case y: java.lang.Number    => equalsNumChar(y, x)
    case _ =>
      if (x == null) y == null
      else x.equals(y)
  }

  def equalsNumChar(x: java.lang.Number, y: java.lang.Character) =
    if (y == null) x == null
    else {
      val ch = y.charValue()

      typeCode(x) match {
        case INT    => x.intValue() == ch
        case LONG   => x.longValue() == ch
        case FLOAT  => x.floatValue() == ch
        case DOUBLE => x.doubleValue() == ch
        case _      => x.equals(y)
      }
    }

  def hashFromLong(n: java.lang.Long) = {
    val iv = n.intValue()
    if (iv == n.longValue()) iv
    else n.hashCode()
  }

  def hashFromDouble(n: java.lang.Double) = {
    val iv = n.intValue
    val dv = n.doubleValue

    if (iv == dv) iv
    else {
      val lv = n.longValue

      if (lv == dv) java.lang.Long.valueOf(lv).hashCode()
      else {
        val fv = n.floatValue

        if (fv == dv) java.lang.Float.valueOf(fv).hashCode()
        else n.hashCode
      }
    }
  }

  def hashFromFloat(n: java.lang.Float) = {
    val iv = n.intValue
    val fv = n.floatValue

    if (iv == fv) iv
    else {
      val lv = n.longValue

      if (lv == fv) java.lang.Long.valueOf(lv).hashCode()
      else n.hashCode()
    }
  }

  def hashFromNumber(n: java.lang.Number) = n match {
    case n: java.lang.Long   => hashFromLong(n)
    case n: java.lang.Double => hashFromDouble(n)
    case n: java.lang.Float  => hashFromFloat(n)
    case _                   => n.hashCode
  }

  def hashFromObject(a: Object) = a match {
    case a: java.lang.Number => hashFromNumber(a)
    case _                   => a.hashCode
  }

  def unboxCharOrInt(arg1: Object, code: Int): Int =
    if (code == CHAR) arg1.asInstanceOf[java.lang.Character].charValue
    else arg1.asInstanceOf[java.lang.Number].intValue

  def unboxCharOrLong(arg1: Object, code: Int): Long =
    if (code == CHAR) arg1.asInstanceOf[java.lang.Character].charValue
    else arg1.asInstanceOf[java.lang.Number].longValue

  def unboxCharOrFloat(arg1: Object, code: Int): Float =
    if (code == CHAR) arg1.asInstanceOf[java.lang.Character].charValue
    else arg1.asInstanceOf[java.lang.Number].floatValue

  def unboxCharOrDouble(arg1: Object, code: Int): Double =
    if (code == CHAR) arg1.asInstanceOf[java.lang.Character].charValue
    else arg1.asInstanceOf[java.lang.Number].doubleValue

  /* OPERATORS ... OPERATORS ... OPERATORS ... OPERATORS ... OPERATORS ... OPERATORS ... OPERATORS ... OPERATORS */
}
