package scala.runtime

import scala.math.ScalaNumber
import scala.annotation.{nowarn, switch}

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.unsigned._
import scala.scalanative.unsafe.Size
import scala.scalanative.unsafe.is32BitPlatform

class BoxesRunTime

/** An object (static class) that defines methods used for creating, reverting,
 *  and calculating with, boxed values. There are four classes of methods in
 *  this object:
 *    - Convenience boxing methods which call the static valueOf method on the
 *      boxed class, thus utilizing the JVM boxing cache.
 *    - Convenience unboxing methods returning default value on null.
 *    - The generalised comparison method to be used when an object may be a
 *      boxed value.
 *    - Standard value operators for boxed java.lang.Number and
 *      quasi-java.lang.Number values.
 */
object BoxesRunTime {
  private final val CHAR = 0
  private final val BYTE = 1
  private final val SHORT = 2
  private final val INT = 3
  private final val LONG = 4
  private final val FLOAT = 5
  private final val DOUBLE = 6
  private final val ULONG = 7 // special case for comparing unsigned types
  private final val OTHER = 8

  /** We don't need to return BYTE and SHORT, as everything which might care
   *  widens to INT.
   */
  private def typeCode(a: java.lang.Object): scala.Int = a match {
    case num: scala.math.ScalaNumber => typeCodeScalaNumber(num)
    case num: java.lang.Number       => typeCodeNumber(num)
    case _: java.lang.Character      => CHAR
    case _                           => OTHER
  }

  private def typeCodeNumber(a: java.lang.Number): scala.Int = a match {
    case _: java.lang.Integer => INT
    case _: java.lang.Double  => DOUBLE
    case _: java.lang.Long    => LONG
    case _: java.lang.Float   => FLOAT
    case _: java.lang.Byte    => INT
    case _: java.lang.Short   => INT
    case _                    => OTHER
  }

  private def typeCodeScalaNumber(num: scala.math.ScalaNumber): scala.Int =
    num match {
      case _: UByte  => INT
      case _: UShort => INT
      case _: UInt   => LONG
      case _: ULong  => ULONG
      case _: Size   => if (LinktimeInfo.is32BitPlatform) INT else LONG
      case _: USize  => if (LinktimeInfo.is32BitPlatform) LONG else ULONG
      case _         => OTHER
    }

  // Char is unsigned, we don't need to extended int/long
  private def typeCodeScalaNumberForChar(
      num: scala.math.ScalaNumber
  ): scala.Int =
    num match {
      case _: UByte  => INT
      case _: UShort => INT
      case _: UInt   => INT
      case _: ULong  => LONG
      case _: Size   => if (LinktimeInfo.is32BitPlatform) INT else LONG
      case _: USize  => if (LinktimeInfo.is32BitPlatform) INT else LONG
      case _         => OTHER
    }

  // Boxing
  @inline def boxToBoolean(v: scala.Boolean): java.lang.Boolean =
    java.lang.Boolean.valueOf(v)
  @inline def boxToCharacter(v: scala.Char): java.lang.Character =
    java.lang.Character.valueOf(v)
  @inline def boxToByte(v: scala.Byte): java.lang.Byte =
    java.lang.Byte.valueOf(v)
  @inline def boxToShort(v: scala.Short): java.lang.Short =
    java.lang.Short.valueOf(v)
  @inline def boxToInteger(v: scala.Int): java.lang.Integer =
    java.lang.Integer.valueOf(v)
  @inline def boxToLong(v: scala.Long): java.lang.Long =
    java.lang.Long.valueOf(v)
  @inline def boxToFloat(v: scala.Float): java.lang.Float =
    java.lang.Float.valueOf(v)
  @inline def boxToDouble(v: scala.Double): java.lang.Double =
    java.lang.Double.valueOf(v)

  // Unboxing
  @inline def unboxToBoolean(o: java.lang.Object): scala.Boolean =
    if (o == null) false else o.asInstanceOf[java.lang.Boolean].booleanValue
  @inline def unboxToChar(o: java.lang.Object): scala.Char =
    if (o == null) 0 else o.asInstanceOf[java.lang.Character].charValue
  @inline def unboxToByte(o: java.lang.Object): scala.Byte =
    if (o == null) 0 else o.asInstanceOf[java.lang.Byte].byteValue
  @inline def unboxToShort(o: java.lang.Object): scala.Short =
    if (o == null) 0 else o.asInstanceOf[java.lang.Short].shortValue
  @inline def unboxToInt(o: java.lang.Object): scala.Int =
    if (o == null) 0 else o.asInstanceOf[java.lang.Integer].intValue
  @inline def unboxToLong(o: java.lang.Object): scala.Long =
    if (o == null) 0 else o.asInstanceOf[java.lang.Long].longValue
  @inline def unboxToFloat(o: java.lang.Object): scala.Float =
    if (o == null) 0 else o.asInstanceOf[java.lang.Float].floatValue
  @inline def unboxToDouble(o: java.lang.Object): scala.Double =
    if (o == null) 0 else o.asInstanceOf[java.lang.Double].doubleValue

  // Comparsion
  @inline def equals(x: java.lang.Object, y: java.lang.Object): Boolean = {
    if (x eq y) true
    else equals2(x, y)
  }

  def equals2(x: java.lang.Object, y: java.lang.Object): Boolean = x match {
    case x: java.lang.Number    => equalsNumObject(x, y)
    case x: java.lang.Character => equalsCharObject(x, y)
    case null                   => y == null
    case x                      => x.equals(y)
  }

  def equalsNumObject(xn: java.lang.Number, y: java.lang.Object): Boolean =
    y match {
      case y: java.lang.Number    => equalsNumNum(xn, y)
      case y: java.lang.Character => equalsNumChar(xn, y)
      case null                   => xn == null
      case y                      => xn.equals(y)
    }

  def equalsNumNum(xn: java.lang.Number, yn: java.lang.Number): Boolean = {
    if (xn == null) yn == null
    else {
      val xcode = typeCode(xn)
      val ycode = typeCode(yn)
      val maxcode = if (xcode > ycode) xcode else ycode
      (maxcode: @switch) match {
        case INT    => xn.intValue() == yn.intValue()
        case LONG   => xn.longValue() == yn.longValue()
        case FLOAT  => xn.floatValue() == yn.floatValue()
        case DOUBLE => xn.doubleValue() == yn.doubleValue()
        case ULONG  =>
          // todo: use extension to int128 when available
          val xnIsUnsigned = xn.isInstanceOf[ULong] || xn.isInstanceOf[USize]
          val longVal = if (xnIsUnsigned) xn else yn
          val otherVal = if (xnIsUnsigned) yn else xn
          otherVal match {
            case other: Size if !is32BitPlatform =>
              other.longValue() >= 0 && longVal.longValue == other.longValue
            case other: java.lang.Long =>
              other.longValue() >= 0 && longVal.longValue == other.longValue
            case other => longVal.longValue() == other.longValue()
          }
        case _ =>
          if (yn.isInstanceOf[ScalaNumber] && !xn.isInstanceOf[ScalaNumber])
            yn.equals(xn)
          else xn.equals(yn)
      }
    }
  }

  def equalsCharObject(xc: java.lang.Character, y: java.lang.Object): Boolean =
    y match {
      case y: java.lang.Character => xc.charValue() == y.charValue()
      case y: java.lang.Number    => equalsNumChar(y, xc)
      case null                   => xc == null
      case _                      => xc.equals(y)
    }

  def equalsNumChar(xn: java.lang.Number, yc: java.lang.Character): Boolean = {
    if (yc == null) xn == null
    else {
      val ch = yc.charValue()
      val typeCode = xn match {
        case that: ScalaNumber => typeCodeScalaNumberForChar(that)
        case that              => typeCodeNumber(that)
      }
      (typeCode: @switch) match {
        case INT    => xn.intValue() == ch
        case LONG   => xn.longValue() == ch
        case FLOAT  => xn.floatValue() == ch
        case DOUBLE => xn.doubleValue() == ch
        case _      => xn.equals(yc): @nowarn
      }
    }
  }

  private def unboxCharOrInt(
      arg1: java.lang.Object,
      code: scala.Int
  ): scala.Int = {
    if (code == CHAR)
      arg1.asInstanceOf[java.lang.Character].charValue()
    else
      arg1.asInstanceOf[java.lang.Number].intValue()
  }

  private def unboxCharOrLong(arg1: java.lang.Object, code: scala.Int): Long = {
    if (code == CHAR)
      arg1.asInstanceOf[java.lang.Character].charValue()
    else
      arg1.asInstanceOf[java.lang.Number].longValue()
  }

  private def unboxCharOrFloat(
      arg1: java.lang.Object,
      code: scala.Int
  ): Float = {
    if (code == CHAR)
      arg1.asInstanceOf[java.lang.Character].charValue()
    else
      arg1.asInstanceOf[java.lang.Number].floatValue()
  }

  private def unboxCharOrDouble(
      arg1: java.lang.Object,
      code: scala.Int
  ): Double = {
    if (code == CHAR)
      arg1.asInstanceOf[java.lang.Character].charValue()
    else
      arg1.asInstanceOf[java.lang.Number].doubleValue()
  }

  // Operators
  def add(arg1: java.lang.Object, arg2: java.lang.Object): java.lang.Object = {
    val code1 = typeCode(arg1)
    val code2 = typeCode(arg2)
    val maxcode = if (code1 < code2) code2 else code1
    if (maxcode <= INT)
      boxToInteger(unboxCharOrInt(arg1, code1) + unboxCharOrInt(arg2, code2))
    else if (maxcode <= LONG)
      boxToLong(unboxCharOrLong(arg1, code1) + unboxCharOrLong(arg2, code2))
    else if (maxcode <= FLOAT)
      boxToFloat(unboxCharOrFloat(arg1, code1) + unboxCharOrFloat(arg2, code2))
    else if (maxcode <= DOUBLE)
      boxToDouble(
        unboxCharOrDouble(arg1, code1) + unboxCharOrDouble(arg2, code2)
      )
    else
      throw new NoSuchMethodException()
  }

  def subtract(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    val code1 = typeCode(arg1)
    val code2 = typeCode(arg2)
    val maxcode = if (code1 < code2) code2 else code1
    if (maxcode <= INT)
      boxToInteger(unboxCharOrInt(arg1, code1) - unboxCharOrInt(arg2, code2))
    else if (maxcode <= LONG)
      boxToLong(unboxCharOrLong(arg1, code1) - unboxCharOrLong(arg2, code2))
    else if (maxcode <= FLOAT)
      boxToFloat(unboxCharOrFloat(arg1, code1) - unboxCharOrFloat(arg2, code2))
    else if (maxcode <= DOUBLE)
      boxToDouble(
        unboxCharOrDouble(arg1, code1) - unboxCharOrDouble(arg2, code2)
      )
    else
      throw new NoSuchMethodException()
  }

  def multiply(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    val code1 = typeCode(arg1)
    val code2 = typeCode(arg2)
    val maxcode = if (code1 < code2) code2 else code1
    if (maxcode <= INT)
      boxToInteger(unboxCharOrInt(arg1, code1) * unboxCharOrInt(arg2, code2))
    else if (maxcode <= LONG)
      boxToLong(unboxCharOrLong(arg1, code1) * unboxCharOrLong(arg2, code2))
    else if (maxcode <= FLOAT)
      boxToFloat(unboxCharOrFloat(arg1, code1) * unboxCharOrFloat(arg2, code2))
    else if (maxcode <= DOUBLE)
      boxToDouble(
        unboxCharOrDouble(arg1, code1) * unboxCharOrDouble(arg2, code2)
      )
    else
      throw new NoSuchMethodException()
  }

  def divide(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    val code1 = typeCode(arg1)
    val code2 = typeCode(arg2)
    val maxcode = if (code1 < code2) code2 else code1
    if (maxcode <= INT)
      boxToInteger(unboxCharOrInt(arg1, code1) / unboxCharOrInt(arg2, code2))
    else if (maxcode <= LONG)
      boxToLong(unboxCharOrLong(arg1, code1) / unboxCharOrLong(arg2, code2))
    else if (maxcode <= FLOAT)
      boxToFloat(unboxCharOrFloat(arg1, code1) / unboxCharOrFloat(arg2, code2))
    else if (maxcode <= DOUBLE)
      boxToDouble(
        unboxCharOrDouble(arg1, code1) / unboxCharOrDouble(arg2, code2)
      )
    else
      throw new NoSuchMethodException()
  }

  def takeModulo(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    val code1 = typeCode(arg1)
    val code2 = typeCode(arg2)
    val maxcode = if (code1 < code2) code2 else code1
    if (maxcode <= INT)
      boxToInteger(unboxCharOrInt(arg1, code1) % unboxCharOrInt(arg2, code2))
    else if (maxcode <= LONG)
      boxToLong(unboxCharOrLong(arg1, code1) % unboxCharOrLong(arg2, code2))
    else if (maxcode <= FLOAT)
      boxToFloat(unboxCharOrFloat(arg1, code1) % unboxCharOrFloat(arg2, code2))
    else if (maxcode <= DOUBLE)
      boxToDouble(
        unboxCharOrDouble(arg1, code1) % unboxCharOrDouble(arg2, code2)
      )
    else
      throw new NoSuchMethodException()
  }

  def shiftSignedRight(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    val code1 = typeCode(arg1)
    val code2 = typeCode(arg2)
    if (code1 <= INT) {
      val val1 = unboxCharOrInt(arg1, code1)
      if (code2 <= INT) {
        val val2 = unboxCharOrInt(arg2, code2)
        return boxToInteger(val1 >> val2)
      } else if (code2 <= LONG) {
        val val2 = unboxCharOrLong(arg2, code2)
        return boxToInteger(val1 >> val2): @nowarn
      }
    }
    if (code1 <= LONG) {
      val val1 = unboxCharOrLong(arg1, code1)
      if (code2 <= INT) {
        val val2 = unboxCharOrInt(arg2, code2)
        return boxToLong(val1 >> val2)
      } else if (code2 <= LONG) {
        val val2 = unboxCharOrLong(arg2, code2)
        return boxToLong(val1 >> val2)
      }
    }
    throw new NoSuchMethodException()
  }

  def shiftSignedLeft(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    val code1 = typeCode(arg1)
    val code2 = typeCode(arg2)
    if (code1 <= INT) {
      val val1 = unboxCharOrInt(arg1, code1)
      if (code2 <= INT) {
        val val2 = unboxCharOrInt(arg2, code2)
        return boxToInteger(val1 << val2)
      } else if (code2 <= LONG) {
        val val2 = unboxCharOrLong(arg2, code2)
        return boxToInteger(val1 << val2): @nowarn
      }
    }
    if (code1 <= LONG) {
      val val1 = unboxCharOrLong(arg1, code1)
      if (code2 <= INT) {
        val val2 = unboxCharOrInt(arg2, code2)
        return boxToLong(val1 << val2)
      } else if (code2 <= LONG) {
        val val2 = unboxCharOrLong(arg2, code2)
        return boxToLong(val1 << val2)
      }
    }
    throw new NoSuchMethodException()
  }

  def shiftLogicalRight(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    val code1 = typeCode(arg1)
    val code2 = typeCode(arg2)
    if (code1 <= INT) {
      val val1 = unboxCharOrInt(arg1, code1)
      if (code2 <= INT) {
        val val2 = unboxCharOrInt(arg2, code2)
        return boxToInteger(val1 >>> val2)
      } else if (code2 <= LONG) {
        val val2 = unboxCharOrLong(arg2, code2)
        return boxToInteger(val1 >>> val2): @nowarn
      }
    }
    if (code1 <= LONG) {
      val val1 = unboxCharOrLong(arg1, code1)
      if (code2 <= INT) {
        val val2 = unboxCharOrInt(arg2, code2)
        return boxToLong(val1 >>> val2)
      } else if (code2 <= LONG) {
        val val2 = unboxCharOrLong(arg2, code2)
        return boxToLong(val1 >>> val2)
      }
    }
    throw new NoSuchMethodException()
  }

  def negate(arg: java.lang.Object): java.lang.Object = {
    val code = typeCode(arg)
    if (code <= INT) {
      val value = unboxCharOrInt(arg, code)
      boxToInteger(-value)
    } else if (code <= LONG) {
      val value = unboxCharOrLong(arg, code)
      boxToLong(-value)
    } else if (code <= FLOAT) {
      val value = unboxCharOrFloat(arg, code)
      boxToFloat(-value)
    } else if (code <= DOUBLE) {
      val value = unboxCharOrDouble(arg, code)
      boxToDouble(-value)
    } else {
      throw new NoSuchMethodException()
    }
  }

  def positive(arg: java.lang.Object): java.lang.Object = {
    val code = typeCode(arg)
    if (code <= INT) {
      boxToInteger(+unboxCharOrInt(arg, code))
    } else if (code <= LONG) {
      boxToLong(+unboxCharOrLong(arg, code))
    } else if (code <= FLOAT) {
      boxToFloat(+unboxCharOrFloat(arg, code))
    } else if (code <= DOUBLE) {
      boxToDouble(+unboxCharOrDouble(arg, code))
    } else {
      throw new NoSuchMethodException()
    }
  }

  def takeAnd(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    if (arg1.isInstanceOf[Boolean] || arg2.isInstanceOf[Boolean]) {
      if (arg1.isInstanceOf[Boolean] && arg2.isInstanceOf[Boolean]) {
        boxToBoolean(arg1.asInstanceOf[Boolean] & arg2.asInstanceOf[Boolean])
      } else {
        throw new NoSuchMethodException()
      }
    } else {
      val code1 = typeCode(arg1)
      val code2 = typeCode(arg2)
      val maxcode = if (code1 < code2) code2 else code1

      if (maxcode <= INT)
        boxToInteger(unboxCharOrInt(arg1, code1) & unboxCharOrInt(arg2, code2))
      else if (maxcode <= LONG)
        boxToLong(unboxCharOrLong(arg1, code1) & unboxCharOrLong(arg2, code2))
      else
        throw new NoSuchMethodException()
    }
  }

  def takeOr(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    if (arg1.isInstanceOf[Boolean] || arg2.isInstanceOf[Boolean]) {
      if (arg1.isInstanceOf[Boolean] && arg2.isInstanceOf[Boolean]) {
        boxToBoolean(arg1.asInstanceOf[Boolean] | arg2.asInstanceOf[Boolean])
      } else {
        throw new NoSuchMethodException()
      }
    } else {
      val code1 = typeCode(arg1)
      val code2 = typeCode(arg2)
      val maxcode = if (code1 < code2) code2 else code1

      if (maxcode <= INT)
        boxToInteger(unboxCharOrInt(arg1, code1) | unboxCharOrInt(arg2, code2))
      else if (maxcode <= LONG)
        boxToLong(unboxCharOrLong(arg1, code1) | unboxCharOrLong(arg2, code2))
      else
        throw new NoSuchMethodException()
    }
  }

  def takeXor(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    if (arg1.isInstanceOf[Boolean] || arg2.isInstanceOf[Boolean]) {
      if (arg1.isInstanceOf[Boolean] && arg2.isInstanceOf[Boolean]) {
        boxToBoolean(arg1.asInstanceOf[Boolean] ^ arg2.asInstanceOf[Boolean])
      } else {
        throw new NoSuchMethodException()
      }
    } else {
      val code1 = typeCode(arg1)
      val code2 = typeCode(arg2)
      val maxcode = if (code1 < code2) code2 else code1

      if (maxcode <= INT)
        boxToInteger(unboxCharOrInt(arg1, code1) ^ unboxCharOrInt(arg2, code2))
      else if (maxcode <= LONG)
        boxToLong(unboxCharOrLong(arg1, code1) ^ unboxCharOrLong(arg2, code2))
      else
        throw new NoSuchMethodException()
    }
  }

  def takeConditionalAnd(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    if (arg1.isInstanceOf[Boolean] && arg2.isInstanceOf[Boolean]) {
      boxToBoolean(arg1.asInstanceOf[Boolean] && arg2.asInstanceOf[Boolean])
    } else {
      throw new NoSuchMethodException()
    }
  }

  def takeConditionalOr(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    if (arg1.isInstanceOf[Boolean] && arg2.isInstanceOf[Boolean]) {
      boxToBoolean(arg1.asInstanceOf[Boolean] || arg2.asInstanceOf[Boolean])
    } else {
      throw new NoSuchMethodException()
    }
  }

  def complement(arg: java.lang.Object): java.lang.Object = {
    val code = typeCode(arg)
    if (code <= INT) {
      boxToInteger(~unboxCharOrInt(arg, code))
    } else if (code <= LONG) {
      boxToLong(~unboxCharOrLong(arg, code))
    } else {
      throw new NoSuchMethodException()
    }
  }

  def takeNot(arg: java.lang.Object): java.lang.Object = {
    if (arg.isInstanceOf[Boolean]) {
      boxToBoolean(!arg.asInstanceOf[Boolean])
    } else {
      throw new NoSuchMethodException()
    }
  }

  def testEqual(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    boxToBoolean(arg1 == arg2)
  }

  def testNotEqual(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    boxToBoolean(arg1 != arg2)
  }

  def testLessThan(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    val code1 = typeCode(arg1)
    val code2 = typeCode(arg2)
    val maxcode = if (code1 < code2) code2 else code1
    if (maxcode <= INT) {
      val val1 = unboxCharOrInt(arg1, code1)
      val val2 = unboxCharOrInt(arg2, code2)
      boxToBoolean(val1 < val2)
    } else if (maxcode <= LONG) {
      val val1 = unboxCharOrLong(arg1, code1)
      val val2 = unboxCharOrLong(arg2, code2)
      boxToBoolean(val1 < val2)
    } else if (maxcode <= FLOAT) {
      val val1 = unboxCharOrFloat(arg1, code1)
      val val2 = unboxCharOrFloat(arg2, code2)
      boxToBoolean(val1 < val2)
    } else if (maxcode <= DOUBLE) {
      val val1 = unboxCharOrDouble(arg1, code1)
      val val2 = unboxCharOrDouble(arg2, code2)
      boxToBoolean(val1 < val2)
    } else {
      throw new NoSuchMethodException()
    }
  }

  def testLessOrEqualThan(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    val code1 = typeCode(arg1)
    val code2 = typeCode(arg2)
    val maxcode = if (code1 < code2) code2 else code1
    if (maxcode <= INT) {
      val val1 = unboxCharOrInt(arg1, code1)
      val val2 = unboxCharOrInt(arg2, code2)
      boxToBoolean(val1 <= val2)
    } else if (maxcode <= LONG) {
      val val1 = unboxCharOrLong(arg1, code1)
      val val2 = unboxCharOrLong(arg2, code2)
      boxToBoolean(val1 <= val2)
    } else if (maxcode <= FLOAT) {
      val val1 = unboxCharOrFloat(arg1, code1)
      val val2 = unboxCharOrFloat(arg2, code2)
      boxToBoolean(val1 <= val2)
    } else if (maxcode <= DOUBLE) {
      val val1 = unboxCharOrDouble(arg1, code1)
      val val2 = unboxCharOrDouble(arg2, code2)
      boxToBoolean(val1 <= val2)
    } else {
      throw new NoSuchMethodException()
    }
  }

  def testGreaterOrEqualThan(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    val code1 = typeCode(arg1)
    val code2 = typeCode(arg2)
    val maxcode = if (code1 < code2) code2 else code1
    if (maxcode <= INT) {
      val val1 = unboxCharOrInt(arg1, code1)
      val val2 = unboxCharOrInt(arg2, code2)
      boxToBoolean(val1 >= val2)
    } else if (maxcode <= LONG) {
      val val1 = unboxCharOrLong(arg1, code1)
      val val2 = unboxCharOrLong(arg2, code2)
      boxToBoolean(val1 >= val2)
    } else if (maxcode <= FLOAT) {
      val val1 = unboxCharOrFloat(arg1, code1)
      val val2 = unboxCharOrFloat(arg2, code2)
      boxToBoolean(val1 >= val2)
    } else if (maxcode <= DOUBLE) {
      val val1 = unboxCharOrDouble(arg1, code1)
      val val2 = unboxCharOrDouble(arg2, code2)
      boxToBoolean(val1 >= val2)
    } else {
      throw new NoSuchMethodException()
    }
  }

  def testGreaterThan(
      arg1: java.lang.Object,
      arg2: java.lang.Object
  ): java.lang.Object = {
    val code1 = typeCode(arg1)
    val code2 = typeCode(arg2)
    val maxcode = if (code1 < code2) code2 else code1
    if (maxcode <= INT) {
      val val1 = unboxCharOrInt(arg1, code1)
      val val2 = unboxCharOrInt(arg2, code2)
      boxToBoolean(val1 > val2)
    } else if (maxcode <= LONG) {
      val val1 = unboxCharOrLong(arg1, code1)
      val val2 = unboxCharOrLong(arg2, code2)
      boxToBoolean(val1 > val2)
    } else if (maxcode <= FLOAT) {
      val val1 = unboxCharOrFloat(arg1, code1)
      val val2 = unboxCharOrFloat(arg2, code2)
      boxToBoolean(val1 > val2)
    } else if (maxcode <= DOUBLE) {
      val val1 = unboxCharOrDouble(arg1, code1)
      val val2 = unboxCharOrDouble(arg2, code2)
      boxToBoolean(val1 > val2)
    } else {
      throw new NoSuchMethodException()
    }
  }

  def isBoxedNumberOrBoolean(arg: java.lang.Object): Boolean = {
    arg.isInstanceOf[java.lang.Boolean] || isBoxedNumber(arg)
  }

  def isBoxedNumber(arg: java.lang.Object): Boolean = {
    arg match {
      case _: java.lang.Integer | _: java.lang.Long | _: java.lang.Double |
          _: java.lang.Float | _: java.lang.Short | _: java.lang.Character |
          _: java.lang.Byte =>
        true
      case _ => false
    }
  }

  def toCharacter(arg: java.lang.Object): java.lang.Character = arg match {
    case int: java.lang.Integer    => boxToCharacter(int.toChar)
    case short: java.lang.Short    => boxToCharacter(short.toChar)
    case char: java.lang.Character => char
    case long: java.lang.Long      => boxToCharacter(long.toChar)
    case byte: java.lang.Byte      => boxToCharacter(byte.toChar)
    case float: java.lang.Float    => boxToCharacter(float.toChar)
    case double: java.lang.Double  => boxToCharacter(double.toChar)
    case _                         => throw new NoSuchMethodException()
  }

  def toByte(arg: java.lang.Object): java.lang.Byte = arg match {
    case int: java.lang.Integer    => boxToByte(int.toByte)
    case char: java.lang.Character => boxToByte(char.toByte)
    case byte: java.lang.Byte      => byte
    case long: java.lang.Long      => boxToByte(long.toByte)
    case short: java.lang.Short    => boxToByte(short.toByte)
    case float: java.lang.Float    => boxToByte(float.toByte)
    case double: java.lang.Double  => boxToByte(double.toByte)
    case _                         => throw new NoSuchMethodException()
  }

  def toShort(arg: java.lang.Object): java.lang.Short = arg match {
    case int: java.lang.Integer    => boxToShort(int.toShort)
    case long: java.lang.Long      => boxToShort(long.toShort)
    case char: java.lang.Character => boxToShort(char.toShort)
    case byte: java.lang.Byte      => boxToShort(byte.toShort)
    case short: java.lang.Short    => short
    case float: java.lang.Float    => boxToShort(float.toShort)
    case double: java.lang.Double  => boxToShort(double.toShort)
    case _                         => throw new NoSuchMethodException()
  }

  def toInteger(arg: java.lang.Object): java.lang.Integer = arg match {
    case int: java.lang.Integer    => int
    case long: java.lang.Long      => boxToInteger(long.toInt)
    case double: java.lang.Double  => boxToInteger(double.toInt)
    case float: java.lang.Float    => boxToInteger(float.toInt)
    case char: java.lang.Character => boxToInteger(char.toInt)
    case byte: java.lang.Byte      => boxToInteger(byte.toInt)
    case short: java.lang.Short    => boxToInteger(short.toInt)
    case _                         => throw new NoSuchMethodException()
  }

  def toLong(arg: java.lang.Object): java.lang.Long = arg match {
    case int: java.lang.Integer    => boxToLong(int.toLong)
    case double: java.lang.Double  => boxToLong(double.toLong)
    case float: java.lang.Float    => boxToLong(float.toLong)
    case long: java.lang.Long      => long
    case char: java.lang.Character => boxToLong(char.toLong)
    case byte: java.lang.Byte      => boxToLong(byte.toLong)
    case short: java.lang.Short    => boxToLong(short.toLong)
    case _                         => throw new NoSuchMethodException()
  }

  def toFloat(arg: java.lang.Object): java.lang.Float = arg match {
    case int: java.lang.Integer    => boxToFloat(int.toFloat)
    case long: java.lang.Long      => boxToFloat(long.toFloat)
    case float: java.lang.Float    => float
    case double: java.lang.Double  => boxToFloat(double.toFloat)
    case char: java.lang.Character => boxToFloat(char.toFloat)
    case byte: java.lang.Byte      => boxToFloat(byte.toFloat)
    case short: java.lang.Short    => boxToFloat(short.toFloat)
    case _                         => throw new NoSuchMethodException()
  }

  def toDouble(arg: java.lang.Object): java.lang.Double = arg match {
    case int: java.lang.Integer    => boxToDouble(int.toDouble)
    case float: java.lang.Float    => boxToDouble(float.toDouble)
    case double: java.lang.Double  => double
    case long: java.lang.Long      => boxToDouble(long.toDouble)
    case char: java.lang.Character => boxToDouble(char.toDouble)
    case byte: java.lang.Byte      => boxToDouble(byte.toDouble)
    case short: java.lang.Short    => boxToDouble(short.toDouble)
    case _                         => throw new NoSuchMethodException()
  }
}
