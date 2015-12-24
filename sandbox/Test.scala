package test

import java.{lang => jl}

object Test {
  def boxFromValue: Unit = {
    new jl.Boolean(false)
    new jl.Character('0')
    new jl.Byte(0.toByte)
    new jl.Short(0.toShort)
    new jl.Integer(0)
    new jl.Long(0L)
    new jl.Float(0.0f)
    new jl.Double(0.0d)
  }

  def boxFromString: Unit = {
    new jl.Boolean("false")
    new jl.Byte("0")
    new jl.Short("0")
    new jl.Integer("0")
    new jl.Long("0")
    new jl.Float("0.0")
    new jl.Double("0.0")
  }

  def parse: Unit = {
    jl.Boolean.parseBoolean("false")
    jl.Byte.parseByte("0")
    jl.Byte.parseByte("0", 10)
    jl.Short.parseShort("0")
    jl.Short.parseShort("0", 10)
    jl.Integer.parseInt("0")
    jl.Integer.parseInt("0", 10)
    jl.Long.parseLong("0")
    jl.Long.parseLong("0", 10)
    jl.Float.parseFloat("0.0")
    jl.Double.parseDouble("0.0")
  }

  def boxValueOf: Unit = {
    jl.Boolean.valueOf(false)
    jl.Character.valueOf('0')
    jl.Byte.valueOf(0.toByte)
    jl.Short.valueOf(0.toShort)
    jl.Integer.valueOf(0)
    jl.Long.valueOf(0L)
    jl.Float.valueOf(0.0f)
    jl.Double.valueOf(0.0d)
  }

  def boxStringValueOf: Unit = {
    jl.Boolean.valueOf("false")
    jl.Byte.valueOf("0")
    jl.Byte.valueOf("0", 10)
    jl.Short.valueOf("0")
    jl.Short.valueOf("0", 10)
    jl.Integer.valueOf("0")
    jl.Integer.valueOf("0", 10)
    jl.Long.valueOf("0")
    jl.Long.valueOf("0", 10)
    jl.Float.valueOf("0.0")
    jl.Double.valueOf("0.0")
  }

  def unboxBoolean(box: jl.Boolean): Unit = box.booleanValue
  def unboxCharacter(box: jl.Character): Unit = box.charValue
  def unboxByte(box: jl.Byte): Unit = {
    box.byteValue
    box.shortValue
    box.intValue
    box.longValue
    box.floatValue
    box.doubleValue
  }
  def unboxShort(box: jl.Short): Unit = {
    box.byteValue
    box.shortValue
    box.intValue
    box.longValue
    box.floatValue
    box.doubleValue
  }
  def unboxInteger(box: jl.Integer): Unit = {
    box.byteValue
    box.shortValue
    box.intValue
    box.longValue
    box.floatValue
    box.doubleValue
  }
  def unboxLong(box: jl.Long): Unit = {
    box.byteValue
    box.shortValue
    box.intValue
    box.longValue
    box.floatValue
    box.doubleValue
  }

  def boxedBooleanToString(box: jl.Boolean): Unit   = box.toString
  def boxedCharToString   (box: jl.Character): Unit = box.toString
  def boxedByteToString   (box: jl.Byte): Unit      = box.toString
  def boxedShortToString  (box: jl.Short): Unit     = box.toString
  def boxedIntToString    (box: jl.Integer): Unit   = box.toString
  def boxedLongToString   (box: jl.Long): Unit      = box.toString
  def boxedFloatToString  (box: jl.Float): Unit     = box.toString
  def boxedDoubleToString (box: jl.Double): Unit    = box.toString

  def unboxedBooleanToString(box: Boolean): Unit = box.toString
  def unboxedCharToString   (box: Char): Unit    = box.toString
  def unboxedByteToString   (box: Byte): Unit    = box.toString
  def unboxedShortToString  (box: Short): Unit   = box.toString
  def unboxedIntToString    (box: Int): Unit     = box.toString
  def unboxedLongToString   (box: Long): Unit    = box.toString
  def unboxedFloatToString  (box: Float): Unit   = box.toString
  def unboxedDoubleToString (box: Double): Unit  = box.toString

  def primitiveToString: Unit = {
    jl.Boolean.toString(false)
    jl.Byte.toString(0.toByte)
    jl.Short.toString(0.toShort)
    jl.Integer.toString(0)
    jl.Integer.toString(0, 10)
    jl.Long.toString(0L)
    jl.Long.toString(0L, 10)
    jl.Float.toString(0F)
    jl.Double.toString(0D)
  }

  def unsignedParse: Unit = {
    jl.Integer.parseUnsignedInt("0")
    jl.Integer.parseUnsignedInt("0", 10)
    jl.Long.parseUnsignedLong("0")
    jl.Long.parseUnsignedLong("0", 10)
  }

  def unsignedToString: Unit = {
    jl.Integer.toUnsignedString(0)
    jl.Integer.toUnsignedString(0, 10)
    jl.Long.toUnsignedString(0L)
    jl.Long.toUnsignedString(0L, 10)
  }

  def unsignedDivision: Unit = {
    jl.Integer.divideUnsigned(0, 0)
    jl.Integer.remainderUnsigned(0, 0)
    jl.Long.divideUnsigned(0L, 0L)
    jl.Long.remainderUnsigned(0L, 0L)
  }

  def unsignedConversion: Unit = {
    jl.Byte.toUnsignedInt(0.toByte)
    jl.Byte.toUnsignedLong(0.toByte)
    jl.Short.toUnsignedInt(0.toShort)
    jl.Short.toUnsignedLong(0.toShort)
    jl.Integer.toUnsignedLong(0)
  }

  // TODO:
  // def primitiveCompare: Unit = ???
  // def boxedCompare: Unit = ???
  // def unsignedCompare: Unit = ???
}
