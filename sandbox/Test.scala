package test

import java.{lang => jl}

object Test {
  def boxValue: Unit = {
    new jl.Boolean(false)
    new jl.Character('0')
    new jl.Byte(0.toByte)
    new jl.Short(0.toShort)
    new jl.Integer(0)
    new jl.Long(0L)
    new jl.Float(0.0f)
    new jl.Double(0.0d)
  }

  def boxString: Unit = {
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

  def unboxBoolean(box: jl.Boolean): Unit = {
    box.booleanValue
  }

  def unboxCharacter(box: jl.Character): Unit = {
    box.charValue
  }

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
}
