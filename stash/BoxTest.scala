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
